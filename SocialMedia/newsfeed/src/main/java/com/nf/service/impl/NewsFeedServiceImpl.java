package com.nf.service.impl;


import com.nf.model.NewsFeed;
import com.nf.service.NewsFeedService;
import com.nf.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsFeedServiceImpl implements NewsFeedService{
    @Autowired
    UserService userService;
    @Override
    public List<NewsFeed> getNewsFeed(Long userId) {
        return userService.getPostById(userId);
    }
}
