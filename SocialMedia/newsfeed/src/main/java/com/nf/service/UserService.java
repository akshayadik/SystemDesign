package com.nf.service;

import com.nf.model.NewsFeed;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "userservice", url = "${spring.newsfeed.url}")
public interface UserService {
    @RequestMapping(method = RequestMethod.GET, value = "/findpost")
    List<NewsFeed> getPostById(@RequestParam(name="userId") Long userId);
}
