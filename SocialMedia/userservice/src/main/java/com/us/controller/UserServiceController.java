package com.us.controller;

import com.us.exception.UserServiceException;
import com.us.model.Post;
import com.us.model.User;
import com.us.model.UserFollow;
import com.us.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class UserServiceController {
    Logger log = LoggerFactory.getLogger(UserServiceController.class);

    @Autowired
    UserService userService;

    @PostMapping("/createpost")
    public ResponseEntity<Post> createPost(long userId, long postId, String content) {
        Optional<Post> post = userService.createPost(userId, postId, content);
        if (post.isPresent()) {
            return new ResponseEntity<Post>(post.get(), HttpStatus.CREATED);
        }
        return new ResponseEntity<Post>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/follow")
    public ResponseEntity<UserFollow> follow(long followerId, long followeeId) {
        Optional<UserFollow> follow = userService.followUser(followerId, followeeId, true);
        if (follow.isPresent()) {
            return new ResponseEntity<UserFollow>(follow.get(), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<UserFollow>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/unfollow")
    public ResponseEntity<UserFollow> unfollow(long followerId, long followeeId) {
        Optional<UserFollow> unfollow = userService.followUser(followerId, followeeId, false);
        if (unfollow.isPresent()) {
            return new ResponseEntity<UserFollow>(unfollow.get(), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<UserFollow>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/findpost")
    public ResponseEntity<List<Post>> findByUserId(@RequestParam(name="userId") Long userId) throws UserServiceException {
        try {
            Optional<List<Post>> posts = userService.getUserPost(userId);
            if (posts.isPresent()) {
                return new ResponseEntity<List<Post>>(posts.get(), HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<List<Post>>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            throw new UserServiceException("Unable to process the request", e);
        }
    }


    @PutMapping("/createuser")
    public ResponseEntity<User> createUser(User user) {
        try {
            user = userService.createUser(user);
            return new ResponseEntity<User>(user, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while creating user", e);
        }
        return new ResponseEntity<User>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
