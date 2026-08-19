package com.blogapi.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload representing a comment")
public class CommentResponse {

    private Long id;
    private String content;
    private String author;
    private Long postId;
    private LocalDateTime createdAt;
}
