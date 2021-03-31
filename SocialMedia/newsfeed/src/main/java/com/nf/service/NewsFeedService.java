package com.nf.service;

import com.nf.model.NewsFeed;

import java.util.List;

public interface NewsFeedService {
    List<NewsFeed> getNewsFeed(Long userId);
}
