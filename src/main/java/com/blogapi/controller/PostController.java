package com.blogapi.controller;

import com.blogapi.model.dto.ApiResponse;
import com.blogapi.model.dto.PostRequest;
import com.blogapi.model.dto.PostResponse;
import com.blogapi.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Endpoints for managing blog posts")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    private final PostService postService;

    @GetMapping
    @Operation(summary = "Get paginated list of posts", description = "Supports page, size and sort query parameters, e.g. ?page=0&size=10&sort=createdAt,desc")
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("GET /api/posts page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single post by id")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Post found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<PostResponse> getPostById(@Parameter(description = "Post id") @PathVariable Long id) {
        log.info("GET /api/posts/{}", id);
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get paginated posts belonging to a category")
    public ResponseEntity<Page<PostResponse>> getPostsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("GET /api/posts/category/{}", categoryId);
        return ResponseEntity.ok(postService.getPostsByCategory(categoryId, pageable));
    }

    @GetMapping("/author/{author}")
    @Operation(summary = "Get paginated posts written by a given author")
    public ResponseEntity<Page<PostResponse>> getPostsByAuthor(
            @PathVariable String author,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("GET /api/posts/author/{}", author);
        return ResponseEntity.ok(postService.getPostsByAuthor(author, pageable));
    }

    @PostMapping
    @Operation(summary = "Create a new post")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Post created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        log.info("POST /api/posts title='{}'", request.getTitle());
        PostResponse created = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing post")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @Valid @RequestBody PostRequest request) {
        log.info("PUT /api/posts/{}", id);
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a post by id")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        log.info("DELETE /api/posts/{}", id);
        postService.deletePost(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
