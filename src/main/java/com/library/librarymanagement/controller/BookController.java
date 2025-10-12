package com.library.librarymanagement.controller;

import com.library.librarymanagement.dto.BookDto;
import com.library.librarymanagement.model.Book;
import com.library.librarymanagement.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final ModelMapper modelMapper;

    @Autowired
    public BookController(BookService bookService, ModelMapper modelMapper) {
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public List<BookDto> getAllBooks() {
        return bookService.findAllBooks().stream()
                .map(book -> modelMapper.map(book, BookDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable Long id) {
        Book book = bookService.findBookById(id);
        BookDto bookDto = modelMapper.map(book, BookDto.class);
        return ResponseEntity.ok(bookDto);
    }

    @PostMapping
    public ResponseEntity<BookDto> createBook(@RequestBody BookDto bookDto) {
        Book bookRequest = modelMapper.map(bookDto, Book.class);
        Book createdBook = bookService.saveBook(bookRequest);
        BookDto bookResponse = modelMapper.map(createdBook, BookDto.class);
        return new ResponseEntity<>(bookResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(@PathVariable Long id, @RequestBody BookDto bookDto) {
        Book bookRequest = modelMapper.map(bookDto, Book.class);
        Book updatedBook = bookService.updateBook(id, bookRequest);
        BookDto bookResponse = modelMapper.map(updatedBook, BookDto.class);
        return ResponseEntity.ok(bookResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }
}