package com.blogapi.service;

import com.blogapi.exception.InvalidRequestException;
import com.blogapi.exception.ResourceNotFoundException;
import com.blogapi.model.dto.CommentRequest;
import com.blogapi.model.dto.CommentResponse;
import com.blogapi.model.entity.Comment;
import com.blogapi.model.entity.Post;
import com.blogapi.repository.CommentRepository;
import com.blogapi.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        post = Post.builder().id(1L).title("Test Post").build();
        comment = Comment.builder()
                .id(1L)
                .content("Nice post!")
                .author("Reader")
                .post(post)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void addComment_shouldSucceed_whenPostExistsAndContentIsClean() {
        CommentRequest request = new CommentRequest("Nice post!", "Reader");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = commentService.addComment(1L, request);

        assertThat(response.getContent()).isEqualTo("Nice post!");
        assertThat(response.getPostId()).isEqualTo(1L);
    }

    @Test
    void addComment_shouldThrow_whenPostDoesNotExist() {
        CommentRequest request = new CommentRequest("Nice post!", "Reader");
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_shouldThrow_whenContentContainsBlockedWord() {
        CommentRequest request = new CommentRequest("This is spam content", "Reader");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> commentService.addComment(1L, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("moderation");

        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteComment_shouldSucceed_whenCommentExists() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L);

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void deleteComment_shouldThrow_whenCommentDoesNotExist() {
        when(commentRepository.findById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(50L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateComment_shouldSucceed_whenCommentExists() {
        CommentRequest request = new CommentRequest("Updated content", "Reader");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = commentService.updateComment(1L, request);

        assertThat(response).isNotNull();
        verify(commentRepository).save(any(Comment.class));
    }
}
