package com.us.repository;

import com.us.model.User;
import com.us.model.UserFollow;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User,Long>{
    User findByEmail(String email);
}
