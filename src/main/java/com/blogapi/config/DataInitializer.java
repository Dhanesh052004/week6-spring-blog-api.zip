package com.blogapi.config;

import com.blogapi.model.entity.Category;
import com.blogapi.model.entity.Comment;
import com.blogapi.model.entity.Post;
import com.blogapi.repository.CategoryRepository;
import com.blogapi.repository.CommentRepository;
import com.blogapi.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Seeds the H2 in-memory database with sample data when running under the
 * "dev" profile, so the API is immediately explorable via Swagger/Postman.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("Sample data already present, skipping seeding");
            return;
        }

        log.info("Seeding sample development data...");

        Category technology = categoryRepository.save(Category.builder()
                .name("Technology")
                .description("Posts about technology trends and innovations")
                .build());

        Category programming = categoryRepository.save(Category.builder()
                .name("Programming")
                .description("Posts about programming languages and best practices")
                .build());

        Category webDevelopment = categoryRepository.save(Category.builder()
                .name("Web Development")
                .description("Posts about building for the web")
                .build());

        Post post1 = postRepository.save(Post.builder()
                .title("Getting Started with Spring Boot")
                .content("Spring Boot makes backend development easier by providing sensible defaults and auto-configuration, letting you focus on business logic instead of boilerplate.")
                .author("John Doe")
                .category(technology)
                .build());

        Post post2 = postRepository.save(Post.builder()
                .title("Understanding the JVM Memory Model")
                .content("The JVM manages memory through the heap and stack, using generational garbage collection to reclaim unused objects efficiently.")
                .author("Jane Smith")
                .category(programming)
                .build());

        Post post3 = postRepository.save(Post.builder()
                .title("Building RESTful APIs with Java 17")
                .content("Java 17 brings records, sealed classes, and pattern matching that make writing clean REST APIs more expressive than ever.")
                .author("John Doe")
                .category(programming)
                .build());

        Post post4 = postRepository.save(Post.builder()
                .title("Responsive Design Principles for 2026")
                .content("Modern responsive design goes beyond media queries, leaning on container queries and fluid typography for adaptable layouts.")
                .author("Alice Johnson")
                .category(webDevelopment)
                .build());

        Post post5 = postRepository.save(Post.builder()
                .title("A Deep Dive into JPA Relationships")
                .content("Choosing the right fetch strategy and cascade type for @OneToMany and @ManyToOne relationships is critical for JPA application performance.")
                .author("Jane Smith")
                .category(technology)
                .build());

        commentRepository.save(Comment.builder()
                .content("Great introduction, this really helped me get started!")
                .author("Reader One")
                .post(post1)
                .build());

        commentRepository.save(Comment.builder()
                .content("Could you cover Spring Security next?")
                .author("Reader Two")
                .post(post1)
                .build());

        commentRepository.save(Comment.builder()
                .content("Very clear explanation of generational GC.")
                .author("Reader Three")
                .post(post2)
                .build());

        commentRepository.save(Comment.builder()
                .content("Records have been a game changer for our DTOs.")
                .author("Reader Four")
                .post(post3)
                .build());

        commentRepository.save(Comment.builder()
                .content("Container queries finally solved my layout headaches.")
                .author("Reader Five")
                .post(post4)
                .build());

        log.info("Seeded {} categories, {} posts, and sample comments",
                categoryRepository.count(), postRepository.count());
    }
}
