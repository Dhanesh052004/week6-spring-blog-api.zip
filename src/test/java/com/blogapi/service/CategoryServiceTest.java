package com.blogapi.service;

import com.blogapi.exception.InvalidRequestException;
import com.blogapi.exception.ResourceNotFoundException;
import com.blogapi.model.dto.CategoryRequest;
import com.blogapi.model.dto.CategoryResponse;
import com.blogapi.model.entity.Category;
import com.blogapi.repository.CategoryRepository;
import com.blogapi.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Technology").description("Tech posts").build();
    }

    @Test
    void createCategory_shouldSucceed_whenNameIsUnique() {
        CategoryRequest request = new CategoryRequest("Technology", "Tech posts");
        when(categoryRepository.existsByNameIgnoreCase("Technology")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(postRepository.countByCategoryId(1L)).thenReturn(0L);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.getName()).isEqualTo("Technology");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_shouldThrow_whenNameAlreadyExists() {
        CategoryRequest request = new CategoryRequest("Technology", "Tech posts");
        when(categoryRepository.existsByNameIgnoreCase("Technology")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoryById_shouldThrow_whenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllCategories_shouldReturnAllMappedResponses() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(postRepository.countByCategoryId(1L)).thenReturn(3L);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPostCount()).isEqualTo(3);
    }

    @Test
    void deleteCategory_shouldThrow_whenCategoryHasPosts() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(postRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("associated posts");

        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_shouldSucceed_whenCategoryHasNoPosts() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(postRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void updateCategory_shouldThrow_whenNewNameCollidesWithAnotherCategory() {
        Category other = Category.builder().id(2L).name("Programming").build();
        CategoryRequest request = new CategoryRequest("Programming", "desc");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Programming")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> categoryService.updateCategory(1L, request))
                .isInstanceOf(InvalidRequestException.class);
    }
}
