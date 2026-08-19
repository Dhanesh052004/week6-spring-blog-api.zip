package com.blogapi.service;

import com.blogapi.exception.ResourceNotFoundException;
import com.blogapi.model.dto.PostRequest;
import com.blogapi.model.dto.PostResponse;
import com.blogapi.model.entity.Category;
import com.blogapi.model.entity.Post;
import com.blogapi.repository.CategoryRepository;
import com.blogapi.repository.CommentRepository;
import com.blogapi.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostService postService;

    private Category category;
    private Post post;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Technology").description("Tech posts").build();
        post = Post.builder()
                .id(1L)
                .title("Test Post")
                .content("Test content")
                .author("John Doe")
                .category(category)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createPost_shouldSucceed_whenCategoryExists() {
        PostRequest request = new PostRequest("Test Post", "Test content", "John Doe", 1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(commentRepository.countByPostId(1L)).thenReturn(0L);

        PostResponse response = postService.createPost(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Post");
        assertThat(response.getCategoryName()).isEqualTo("Technology");
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void createPost_shouldThrow_whenCategoryDoesNotExist() {
        PostRequest request = new PostRequest("Test Post", "Test content", "John Doe", 99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");

        verify(postRepository, never()).save(any());
    }

    @Test
    void getPostById_shouldReturnPost_whenExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.countByPostId(1L)).thenReturn(2L);

        PostResponse response = postService.getPostById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCommentCount()).isEqualTo(2);
    }

    @Test
    void getPostById_shouldThrow_whenNotFound() {
        when(postRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("10");
    }

    @Test
    void updatePost_shouldSucceed_whenPostAndCategoryExist() {
        PostRequest request = new PostRequest("Updated Title", "Updated content", "Jane Doe", 1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(commentRepository.countByPostId(1L)).thenReturn(0L);

        PostResponse response = postService.updatePost(1L, request);

        assertThat(response).isNotNull();
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void deletePost_shouldSucceed_whenPostExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).delete(post);

        postService.deletePost(1L);

        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void deletePost_shouldThrow_whenPostDoesNotExist() {
        when(postRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    void getAllPosts_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> page = new PageImpl<>(List.of(post), pageable, 1);
        when(postRepository.findAll(pageable)).thenReturn(page);
        when(commentRepository.countByPostId(1L)).thenReturn(0L);

        Page<PostResponse> result = postService.getAllPosts(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Post");
    }

    @Test
    void getPostsByCategory_shouldThrow_whenCategoryDoesNotExist() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> postService.getPostsByCategory(42L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
