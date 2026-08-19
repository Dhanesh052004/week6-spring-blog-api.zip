package com.blogapi.service;

import com.blogapi.exception.InvalidRequestException;
import com.blogapi.exception.ResourceNotFoundException;
import com.blogapi.model.dto.CommentRequest;
import com.blogapi.model.dto.CommentResponse;
import com.blogapi.model.entity.Comment;
import com.blogapi.model.entity.Post;
import com.blogapi.repository.CommentRepository;
import com.blogapi.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    /** Very small illustrative denylist used for basic comment moderation. */
    private static final Set<String> BLOCKED_WORDS = Set.of("spam", "scam");

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable) {
        ensurePostExists(postId);
        return commentRepository.findByPostId(postId, pageable).map(this::toResponse);
    }

    @Transactional
    public CommentResponse addComment(Long postId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        moderate(request.getContent());

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(request.getAuthor())
                .post(post)
                .build();

        Comment saved = commentRepository.save(comment);
        log.info("Added comment id={} to post id={}", saved.getId(), postId);
        return toResponse(saved);
    }

    @Transactional
    public CommentResponse updateComment(Long id, CommentRequest request) {
        Comment comment = findCommentOrThrow(id);
        moderate(request.getContent());

        comment.setContent(request.getContent());
        comment.setAuthor(request.getAuthor());
        Comment updated = commentRepository.save(comment);
        log.info("Updated comment id={}", updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public void deleteComment(Long id) {
        Comment comment = findCommentOrThrow(id);
        commentRepository.delete(comment);
        log.info("Deleted comment id={}", id);
    }

    // ---- helpers ----

    /**
     * Simple moderation check: rejects comments containing blocked terms.
     * This can be extended with a more sophisticated moderation service later.
     */
    private void moderate(String content) {
        String lower = content.toLowerCase();
        for (String blocked : BLOCKED_WORDS) {
            if (lower.contains(blocked)) {
                throw new InvalidRequestException("Comment rejected by moderation: contains disallowed content");
            }
        }
    }

    private void ensurePostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
    }

    private Comment findCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAuthor())
                .postId(comment.getPost().getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
