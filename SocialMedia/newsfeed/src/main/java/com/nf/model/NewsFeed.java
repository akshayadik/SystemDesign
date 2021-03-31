package com.nf.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NewsFeed {
    @Id Long newsFeedId;
    Long userId;
    String description;
    LocalDateTime creationDate;
}
