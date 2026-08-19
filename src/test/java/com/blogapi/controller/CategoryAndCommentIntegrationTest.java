package com.blogapi.controller;

import com.blogapi.model.dto.CategoryRequest;
import com.blogapi.model.dto.CommentRequest;
import com.blogapi.model.dto.PostRequest;
import com.blogapi.repository.CategoryRepository;
import com.blogapi.repository.CommentRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CategoryAndCommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void createCategory_shouldReturn409_whenDuplicateName() throws Exception {
        CategoryRequest request = new CategoryRequest("Duplicate", "desc");

        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void deleteCategory_shouldReturn400_whenCategoryHasPosts() throws Exception {
        String catResponse = mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CategoryRequest("HasPosts", "desc"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long categoryId = objectMapper.readTree(catResponse).get("id").asLong();

        mockMvc.perform(post("/api/posts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new PostRequest("Post", "Content", "Author", categoryId))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_shouldReturn201_thenListShouldContainIt() throws Exception {
        String catResponse = mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CategoryRequest("CommentCat", "desc"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long categoryId = objectMapper.readTree(catResponse).get("id").asLong();

        String postResponse = mockMvc.perform(post("/api/posts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new PostRequest("Post with comments", "Content", "Author", categoryId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long postId = objectMapper.readTree(postResponse).get("id").asLong();

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CommentRequest("Great post!", "Reader"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", is("Great post!")));

        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void addComment_shouldReturn400_whenModerationRejectsContent() throws Exception {
        String catResponse = mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CategoryRequest("ModCat", "desc"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long categoryId = objectMapper.readTree(catResponse).get("id").asLong();

        String postResponse = mockMvc.perform(post("/api/posts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new PostRequest("Post", "Content", "Author", categoryId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long postId = objectMapper.readTree(postResponse).get("id").asLong();

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new CommentRequest("this is scam", "Bot"))))
                .andExpect(status().isBadRequest());
    }
}
