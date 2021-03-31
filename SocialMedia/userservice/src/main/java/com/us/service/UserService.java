package com.us.service;

import com.us.model.Post;
import com.us.model.User;
import com.us.model.UserFollow;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserService {
    Optional<Post> createPost(long userId, long postId, String content);
    Optional<UserFollow> followUser(long followerId, long followeeId, boolean flag);
    User createUser(User user);
    Optional<List<Post>> getUserPost(long userId);
}
