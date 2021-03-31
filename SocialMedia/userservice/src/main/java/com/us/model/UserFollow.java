package com.us.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@RedisHash("userfollow")
public class UserFollow implements Serializable {
    @Id Long userFollowId;
    @Indexed Long userId;
    String type;
    Set<User> followers = new HashSet<>();

    public UserFollow(long userId){ this.userId = userId; }
    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o.getClass() != this.getClass()) return false;
        final UserFollow follow = (UserFollow) o;
        if(Long.compare(follow.getUserFollowId(), this.userFollowId) != 0
                && Long.compare(follow.getUserId(), this.userId) != 0)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + ((int) (this.userId ^ (this.userId >>> 32)));
        hash = 31 * hash + ((int) (this.userFollowId ^ (this.userFollowId >>> 32)));
        return hash;
    }
}
