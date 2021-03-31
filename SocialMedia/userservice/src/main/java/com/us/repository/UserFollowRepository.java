package com.us.repository;

import com.us.model.UserFollow;
import org.springframework.data.repository.CrudRepository;

public interface UserFollowRepository extends CrudRepository<UserFollow,Long> {
    UserFollow findByUserId(Long userId);
}
