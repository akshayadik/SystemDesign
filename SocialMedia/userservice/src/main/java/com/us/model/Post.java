package com.us.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@RedisHash("post")
public class Post implements Serializable {

    @Id Long postId;
    String description;
    LocalDateTime creationDate;
    @Indexed Long userId;

    public Post(String type, String description){
        this.description = description;
        creationDate = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o.getClass() != this.getClass()) return false;
        final Post post = (Post) o;
        if(Long.compare(post.getPostId(), this.postId) != 0)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + ((int) (this.postId ^ (this.postId >>> 32)));
        return hash;
    }
}
