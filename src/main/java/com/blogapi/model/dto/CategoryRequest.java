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
@Schema(description = "Request payload used to create or update a category")
public class CategoryRequest {

    @NotBlank(message = "Category name must not be blank")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Schema(description = "Name of the category", example = "Technology")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Short description of the category", example = "Posts about technology trends")
    private String description;
}
