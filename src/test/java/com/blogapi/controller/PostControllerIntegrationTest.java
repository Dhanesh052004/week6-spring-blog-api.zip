package com.blogapi.controller;

import com.blogapi.model.dto.CategoryRequest;
import com.blogapi.model.dto.PostRequest;
import com.blogapi.repository.CategoryRepository;
import com.blogapi.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests exercising the controller -> service -> repository -> H2 database chain.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        postRepository.deleteAll();
        categoryRepository.deleteAll();

        CategoryRequest categoryRequest = new CategoryRequest("Integration Tests", "Category for tests");
        String response = mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        categoryId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void createPost_shouldReturn201_whenRequestIsValid() throws Exception {
        PostRequest request = new PostRequest("My First Post", "Some content here", "Test Author", categoryId);

        mockMvc.perform(post("/api/posts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("My First Post")))
                .andExpect(jsonPath("$.categoryId", is(categoryId.intValue())));
    }

    @Test
    void createPost_shouldReturn400_whenTitleIsBlank() throws Exception {
        PostRequest request = new PostRequest("", "Some content", "Test Author", categoryId);

        mockMvc.perform(post("/api/posts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title", notNullValue()));
    }

    @Test
    void createPost_shouldReturn404_whenCategoryDoesNotExist() throws Exception {
        PostRequest request = new PostRequest("Title", "Content", "Author", 99999L);

        mockMvc.perform(post("/api/posts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Category")));
    }

    @Test
    void getPostById_shouldReturn404_whenPostDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void getAllPosts_shouldReturnPaginatedResults() throws Exception {
        PostRequest request = new PostRequest("Post A", "Content A", "Author A", categoryId);
        mockMvc.perform(post("/api/posts")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/posts?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.pageable", notNullValue()));
    }

    @Test
    void fullCrudFlow_shouldWorkEndToEnd() throws Exception {
        PostRequest createRequest = new PostRequest("CRUD Post", "Initial content", "CRUD Author", categoryId);
        String createResponse = mockMvc.perform(post("/api/posts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long postId = objectMapper.readTree(createResponse).get("id").asLong();

        PostRequest updateRequest = new PostRequest("CRUD Post Updated", "Updated content", "CRUD Author", categoryId);
        mockMvc.perform(put("/api/posts/" + postId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("CRUD Post Updated")));

        mockMvc.perform(delete("/api/posts/" + postId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isNotFound());
    }
}
