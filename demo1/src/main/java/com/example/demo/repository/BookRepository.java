package com.example.demo.repository;


import com.example.demo.model.Book;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class BookRepository {
    private final List<Book> books = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    public List<Book> findAll() {
        return new ArrayList<>(books);
    }

    public Book findById(Long id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId(counter.incrementAndGet());
            books.add(book);
        } else {
            deleteById(book.getId());
            books.add(book);
        }
        return book;
    }

    public void deleteById(Long id) {
        books.removeIf(book -> book.getId().equals(id));
    }
}