package com.example.demo.controller;


import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.apache.catalina.User;
import org.springframework.boot.actuate.autoconfigure.wavefront.WavefrontProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;



@RestController
@RequestMapping("/")
public class BookController {
    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @RateLimiter(name = "userServiceRateLimiter", fallbackMethod = "rateLimitFallback")
    @GetMapping("/users")
    public ResponseEntity<String> getUsers() {
        return ResponseEntity.ok("гуд");
    }

    // Метод fallback должен иметь тот же возвращаемый тип
    public ResponseEntity<String> rateLimitFallback(Exception ex) {
        return ResponseEntity.status(429).body("Too many requests! Please try again later.");
    }
    // Get all books
    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Get a single book by ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Book book = bookRepository.findById(id);
        if (book != null) {
            return ResponseEntity.ok(book);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Create a new book
//    @PostMapping("/api/v1/predict_bert")
//    @ResponseStatus(HttpStatus.OK)
//    public Book createBook(@RequestBody Book book) {
//        return bookRepository.save(book);
//    }

    @PostMapping(value = "/v2/models/bert_ner_rus/infer", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getInferenceResult() {
        try {
            ClassPathResource resource = new ClassPathResource("llm_response.json");
            String content = Files.readString(Paths.get(resource.getURI()));
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Failed to load response file\"}");
        }
    }

    @PostMapping(value = "/api/v1/predict_bert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getInferenceResult2() {
        try {
            ClassPathResource resource = new ClassPathResource("response_for_censor.json");
            String content = Files.readString(Paths.get(resource.getURI()));
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Failed to load response file\"}");
        }
    }


    // Update an existing book
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        Book existingBook = bookRepository.findById(id);
        if (existingBook != null) {
            book.setId(id);
            return ResponseEntity.ok(bookRepository.save(book));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a book
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        Book book = bookRepository.findById(id);
        if (book != null) {
            bookRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}