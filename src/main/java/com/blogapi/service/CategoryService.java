package com.blogapi.service;

import com.blogapi.exception.InvalidRequestException;
import com.blogapi.exception.ResourceNotFoundException;
import com.blogapi.model.dto.CategoryRequest;
import com.blogapi.model.dto.CategoryResponse;
import com.blogapi.model.entity.Category;
import com.blogapi.repository.CategoryRepository;
import com.blogapi.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.debug("Fetching all categories");
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = findCategoryOrThrow(id);
        return toResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new InvalidRequestException("A category with name '" + request.getName() + "' already exists");
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        Category saved = categoryRepository.save(category);
        log.info("Created category id={} name='{}'", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryOrThrow(id);

        categoryRepository.findByNameIgnoreCase(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new InvalidRequestException("A category with name '" + request.getName() + "' already exists");
            }
        });

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        Category updated = categoryRepository.save(category);
        log.info("Updated category id={}", updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryOrThrow(id);
        if (postRepository.existsByCategoryId(id)) {
            throw new InvalidRequestException(
                    "Cannot delete category id=" + id + " because it still has associated posts");
        }
        categoryRepository.delete(category);
        log.info("Deleted category id={}", id);
    }

    // ---- helpers ----

    protected Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    private CategoryResponse toResponse(Category category) {
        long postCount = postRepository.countByCategoryId(category.getId());
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .postCount(postCount)
                .build();
    }
}
