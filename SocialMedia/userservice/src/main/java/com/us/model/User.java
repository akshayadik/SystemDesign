package com.us.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
@RedisHash("user")
public class User implements Serializable {

    @Id Long userId;
    String name;
    @Indexed String email;
    LocalDate dateOfBirth;
    LocalDateTime creationDate;
    LocalDateTime lastLogin;

    public User(String name, String email){
        this.name = name;
        this.email = email;
        this.creationDate = LocalDateTime.now();
    }

    public User(String name, String email, String dateOfBirth){
        this(name, email);
        //Default formatter is ISO_LOCAL_DATE (e.g. YYYY-mm-dd)
        this.dateOfBirth = LocalDate.parse(dateOfBirth);
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o.getClass() != this.getClass()) return false;
        final User user = (User) o;
        if(Long.compare(user.getUserId(), this.userId) != 0
                && !this.email.equals(user.getEmail()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + ((int) (this.userId ^ (this.userId >>> 32)));
        hash = 31 * hash + ((int) (this.email != null ? this.email.hashCode(): 0));
        return hash;
    }
}
