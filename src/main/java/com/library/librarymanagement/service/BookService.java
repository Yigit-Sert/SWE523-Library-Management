package com.library.librarymanagement.service;

import com.library.librarymanagement.exception.ResourceNotFoundException;
import com.library.librarymanagement.model.Book;
import com.library.librarymanagement.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAllBooks() {
        log.info("Fetching all books from the database...");
        return bookRepository.findAll();
    }

    @Cacheable(value = "books", key = "#id")
    public Book findBookById(Long id) {
        log.info("Fetching book from database: id={}", id);
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    public Book saveBook(Book book) {
        log.info("Saving new book to the database...");
        return bookRepository.save(book);
    }

    @CachePut(value = "books", key = "#id")
    public Book updateBook(Long id, Book bookDetails) {
        log.info("Updating book and cache: id={}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        book.setTitle(bookDetails.getTitle());
        book.setPublisher(bookDetails.getPublisher());
        book.setPublishDate(bookDetails.getPublishDate());

        return bookRepository.save(book);
    }

    @CacheEvict(value = "books", key = "#id")
    public void deleteBookById(Long id) {
        log.info("Deleting book and evicting from cache: id={}", id);
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }
}