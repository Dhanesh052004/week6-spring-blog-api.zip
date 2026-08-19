package com.blogapi.service;

import com.blogapi.exception.ResourceNotFoundException;
import com.blogapi.model.dto.PostRequest;
import com.blogapi.model.dto.PostResponse;
import com.blogapi.model.entity.Category;
import com.blogapi.model.entity.Post;
import com.blogapi.repository.CategoryRepository;
import com.blogapi.repository.CommentRepository;
import com.blogapi.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        log.debug("Fetching posts page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return postRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        Post post = findPostOrThrow(id);
        return toResponse(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }
        return postRepository.findByCategoryId(categoryId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByAuthor(String author, Pageable pageable) {
        return postRepository.findByAuthorIgnoreCase(author, pageable).map(this::toResponse);
    }

    @Transactional
    public PostResponse createPost(PostRequest request) {
        Category category = findCategoryOrThrow(request.getCategoryId());

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(request.getAuthor())
                .category(category)
                .build();

        Post saved = postRepository.save(post);
        log.info("Created post id={} title='{}'", saved.getId(), saved.getTitle());
        return toResponse(saved);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        Post post = findPostOrThrow(id);
        Category category = findCategoryOrThrow(request.getCategoryId());

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(request.getAuthor());
        post.setCategory(category);

        Post updated = postRepository.save(post);
        log.info("Updated post id={}", updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = findPostOrThrow(id);
        postRepository.delete(post);
        log.info("Deleted post id={}", id);
    }

    // ---- helpers ----

    protected Post findPostOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }

    private PostResponse toResponse(Post post) {
        long commentCount = commentRepository.countByPostId(post.getId());
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .categoryId(post.getCategory().getId())
                .categoryName(post.getCategory().getName())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .commentCount((int) commentCount)
                .build();
    }
}
