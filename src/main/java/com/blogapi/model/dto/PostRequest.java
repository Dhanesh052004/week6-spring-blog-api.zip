package com.blogapi.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating or updating a blog post.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload used to create or update a blog post")
public class PostRequest {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(description = "Title of the blog post", example = "Getting Started with Spring Boot")
    private String title;

    @NotBlank(message = "Content must not be blank")
    @Schema(description = "Full content/body of the blog post", example = "Spring Boot makes backend development easier...")
    private String content;

    @NotBlank(message = "Author must not be blank")
    @Size(max = 100, message = "Author name must not exceed 100 characters")
    @Schema(description = "Author of the blog post", example = "John Doe")
    private String author;

    @NotNull(message = "Category id must not be null")
    @Schema(description = "Id of the category this post belongs to", example = "1")
    private Long categoryId;
}
