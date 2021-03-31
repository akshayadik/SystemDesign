package com.nf.controller;


import com.nf.model.NewsFeed;
import com.nf.service.NewsFeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NewsFeedController {
    Logger log = LoggerFactory.getLogger(NewsFeedController.class);

    @Autowired
    NewsFeedService newsFeedService;

    @GetMapping("/newsfeed")
    public ResponseEntity<List<NewsFeed>> getNewsFeed(long userId){
        List<NewsFeed> feeds;
        try{
            feeds = newsFeedService.getNewsFeed(userId);
            return new ResponseEntity<List<NewsFeed>>(feeds, HttpStatus.OK);
        }catch (Exception e){
            log.error("Error while processing news feed request", e);
            e.printStackTrace();
        }
        return new ResponseEntity<List<NewsFeed>>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
