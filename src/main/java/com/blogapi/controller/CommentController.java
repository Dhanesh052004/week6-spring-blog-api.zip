package com.blogapi.controller;

import com.blogapi.model.dto.ApiResponse;
import com.blogapi.model.dto.CommentRequest;
import com.blogapi.model.dto.CommentResponse;
import com.blogapi.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Endpoints for managing comments on blog posts")
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    @Operation(summary = "Get paginated comments for a post")
    public ResponseEntity<Page<CommentResponse>> getCommentsByPost(
            @PathVariable Long postId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("GET /api/posts/{}/comments", postId);
        return ResponseEntity.ok(commentService.getCommentsByPost(postId, pageable));
    }

    @PostMapping("/api/posts/{postId}/comments")
    @Operation(summary = "Add a new comment to a post")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long postId, @Valid @RequestBody CommentRequest request) {
        log.info("POST /api/posts/{}/comments", postId);
        CommentResponse created = commentService.addComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/api/comments/{id}")
    @Operation(summary = "Update an existing comment")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        log.info("PUT /api/comments/{}", id);
        return ResponseEntity.ok(commentService.updateComment(id, request));
    }

    @DeleteMapping("/api/comments/{id}")
    @Operation(summary = "Delete a comment by id")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        log.info("DELETE /api/comments/{}", id);
        commentService.deleteComment(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
