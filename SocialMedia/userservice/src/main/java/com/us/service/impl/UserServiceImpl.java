package com.us.service.impl;

import com.us.enumtype.UserFollowType;
import com.us.model.Post;
import com.us.model.User;
import com.us.model.UserFollow;
import com.us.repository.PostRepository;
import com.us.repository.UserFollowRepository;
import com.us.repository.UserRepository;
import com.us.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserFollowRepository userFollowRepository;
    @Autowired
    PostRepository postRepository;

    @Override
    public Optional<Post> createPost(long userId, long postId, String content) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            Post post = Post.builder().postId(postId).creationDate(LocalDateTime.now())
                    .description(content).userId(user.get().getUserId()).build();
            return Optional.of(postRepository.save(post));
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserFollow> followUser(long followerId, long followeeId, boolean flag) {
        Optional<User> followerUser = userRepository.findById(followeeId);
        Optional<User> followeeUser = userRepository.findById(followeeId);
        if(!followerUser.isPresent() || !followeeUser.isPresent()){
            log.warn(String.format("User with user id %d or %d is not available in the database", followerId, followeeId));
            return Optional.empty();
        }
        //Check if already User has follower
        UserFollow userFollow = userFollowRepository.findByUserId(followerId);
        if(userFollow == null && !flag){
            log.warn(String.format("Followee %d is not following follower %d. Can't unfollow the user.", followeeId, followerId));
            return Optional.empty();
        }
        if(userFollow == null){
            Set<User> users = new HashSet<>();
            Optional<User> user = userRepository.findById(followeeId);
            if(user.isPresent()) users.add(user.get());
            userFollow = UserFollow.builder().followers(users)
                    .type(UserFollowType.FRIEND.name()).userId(followerId).build();
            return Optional.of(userFollowRepository.save(userFollow));
        }
        if(flag){
            userFollow.getFollowers().add(followeeUser.get());
        }else {
            Set<User> followers = userFollow.getFollowers();
            userFollow.setFollowers(followers.stream()
                    .filter(e->Long.compare(e.getUserId(), followeeUser.get().getUserId()) != 0)
                    .collect(Collectors.toSet()));
        }
        return Optional.of(userFollowRepository.save(userFollow));
    }

    @Override
    public User createUser(User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());
        if(existingUser != null){
            return existingUser;
        }else{
            user.setCreationDate(LocalDateTime.now());
            return userRepository.save(user);
        }
    }

    @Override
    public Optional<List<Post>> getUserPost(long userId) {
        Optional<User> followerUser = userRepository.findById(userId);
        if(followerUser.isPresent()){
            UserFollow userFollow = userFollowRepository.findByUserId(followerUser.get().getUserId());
            List<Post> post;
            Pageable paging = PageRequest.of(0, 20, Sort.by("creationDate").descending());
            List<Post> posts = postRepository.findAllByUserId(userId, paging);
            if(userFollow != null){
                post = new ArrayList<>(userFollow.getFollowers().size()*20+20);
                userFollow.getFollowers().stream().forEach(user->{
                    List<Post> userPost = postRepository.findAllByUserId(user.getUserId(), paging);
                    post.addAll(userPost.stream().collect(Collectors.toSet()));
                });
            }else{
                post = new ArrayList<>(20);
            }
            post.addAll(posts.stream().collect(Collectors.toSet()));

            Collections.sort(post, Comparator.comparing(Post::getCreationDate));
            return Optional.of(post.stream().limit(20).collect(Collectors.toList()));
        }
        return Optional.empty();
    }
}
