package com.us.repository;

import com.us.model.Post;
import com.us.model.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PostRepository extends PagingAndSortingRepository<Post,Long> {
    List<Post> findAllByUserId(Long userId, Pageable pageable);
    Slice<Post> findByUserId(Long userId, Pageable pageable);
}
