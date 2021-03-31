package com.us;

import com.us.model.Post;
import com.us.model.User;
import com.us.model.UserFollow;
import com.us.repository.PostRepository;
import com.us.repository.UserFollowRepository;
import com.us.repository.UserRepository;
import com.us.service.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@RunWith(SpringRunner.class)
//@DataRedisTest
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
class UserserviceApplicationTests {

	@Autowired
	UserService userService;
	@Autowired
	UserRepository userRepository;
	@Autowired
	UserFollowRepository userFollowRepository;
	@Autowired
	PostRepository postRepository;

	@Autowired
	TestRestTemplate template;

	static private RedisServer redisServer;

	@BeforeAll
	public static void beforeAll(){
		redisServer = RedisServer.builder()
				.port(6379).setting("maxmemory 128M").build();
		redisServer.start();
	}

	@AfterAll
	public static void afterAll() {
		redisServer.stop();
	}

	@Test
	void testFollowUser() {
		User user1 = new User("Akshay", "akshay@gmail.com", "1990-06-01");
		User user2 = new User("Santosh", "santosh@gmail.com", "1995-06-01");
		user1 = userService.createUser(user1);
		user2 = userService.createUser(user2);

		Optional<UserFollow> follow = userService.followUser(user1.getUserId(), user2.getUserId(), true);
		assertTrue("User is not following other user", follow.isPresent());

		User user3 = userService.createUser(User.builder().creationDate(LocalDateTime.now())
				.email("Abhay@gmail.com").name("Abhay").build());
		follow = userService.followUser(user1.getUserId(), user3.getUserId(), true);

		User user4 = userService.createUser(User.builder().creationDate(LocalDateTime.now())
				.email("Ziya@gmail.com").name("Ziya").build());
		follow = userService.followUser(user1.getUserId(), user4.getUserId(), true);

		//Access the repository and check if user following entry match
		assertEquals("User follow count does not match",3, follow.get().getFollowers().size());

		follow = userService.followUser(user1.getUserId(), user2.getUserId(), false);

		//Access the repository and check if user following entry match
		assertEquals("User follow count does not match",2, follow.get().getFollowers().size());
	}



	@Test
	void testCreatePost() {
		User user = User.builder().name("Santosh").email("santosh@yahoo.com").creationDate(LocalDateTime.now()).build();
		user = userService.createUser(user);
		Optional<Post> post = userService.createPost(user.getUserId(), 1L, "Sample Post");
		assertTrue("Post is not created", post.isPresent());
		assertEquals("Post message does not match", "Sample Post", post.get().getDescription());
	}

	@Test
	void testUserPostList() {
		User a = User.builder().creationDate(LocalDateTime.now())
				.email("a@gmail.com").name("a").build();
		User b = User.builder().creationDate(LocalDateTime.now())
				.email("b@gmail.com").name("b").build();
		User c = User.builder().creationDate(LocalDateTime.now())
				.email("c@gmail.com").name("c").build();
		User d = User.builder().creationDate(LocalDateTime.now())
				.email("d@gmail.com").name("d").build();
		User e = User.builder().creationDate(LocalDateTime.now())
				.email("e@gmail.com").name("e").build();

		a = userService.createUser(a);
		b = userService.createUser(b);
		c = userService.createUser(c);
		d = userService.createUser(d);
		e = userService.createUser(e);

		userService.createPost(a.getUserId(), 1L, "a1");
		userService.createPost(a.getUserId(), 2L, "a2");
		userService.createPost(a.getUserId(), 3L, "a3");

		userService.createPost(b.getUserId(), 4L, "b1");
		userService.createPost(b.getUserId(), 5L, "b2");
		userService.createPost(b.getUserId(), 6L, "b3");

		userService.createPost(c.getUserId(), 7L, "c1");
		userService.createPost(c.getUserId(), 8L, "c2");
		userService.createPost(c.getUserId(), 9L, "c3");

		userService.createPost(d.getUserId(), 10L, "d1");
		userService.createPost(d.getUserId(), 11L, "d2");
		userService.createPost(d.getUserId(), 12L, "d3");

		userService.createPost(e.getUserId(), 13L, "e1");
		userService.createPost(e.getUserId(), 14L, "e2");
		userService.createPost(e.getUserId(), 15L, "e3");

		userService.createPost(a.getUserId(), 16L, "a4");
		userService.createPost(a.getUserId(), 17L, "a5");
		userService.createPost(a.getUserId(), 18L, "a6");

		userService.createPost(e.getUserId(), 19L, "e4");
		userService.createPost(e.getUserId(), 20L, "e5");

		userService.createPost(d.getUserId(), 21L, "d4");
		userService.createPost(d.getUserId(), 22L, "d5");

		Optional<List<Post>> posts = userService.getUserPost(a.getUserId());
		assertTrue("Post is empty", posts.isPresent());
		assertEquals("User post list is empty",6, posts.get().size());

		//Follow users
		userService.followUser(a.getUserId(), b.getUserId(), true);
		userService.followUser(a.getUserId(), c.getUserId(), true);
		userService.followUser(a.getUserId(), d.getUserId(), true);
		userService.followUser(a.getUserId(), e.getUserId(), true);
		posts = userService.getUserPost(a.getUserId());
		//Access the repository and check if user following entry match
		assertEquals("User post list is empty",20, posts.get().size());
	}

}
