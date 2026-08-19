package com.blogapi.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload used to create or update a comment")
public class CommentRequest {

    @NotBlank(message = "Comment content must not be blank")
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    @Schema(description = "Content of the comment", example = "Great article, very helpful!")
    private String content;

    @NotBlank(message = "Author must not be blank")
    @Size(max = 100, message = "Author name must not exceed 100 characters")
    @Schema(description = "Author of the comment", example = "Jane Smith")
    private String author;
}
