package com.library.librarymanagement.controller;

import com.library.librarymanagement.model.Book;
import com.library.librarymanagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // Display a list of all books
    @GetMapping
    public String listBooks(Model model) {
        List<Book> books = bookService.findAllBooks();
        model.addAttribute("books", books);
        model.addAttribute("newBook", new Book()); // For the "Add Book" modal/form
        return "books/index"; // Corresponds to src/main/resources/templates/books/index.html
    }

    // Handle adding a new book
    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        bookService.saveBook(book);
        redirectAttributes.addFlashAttribute("message", "Book added successfully!");
        return "redirect:/books";
    }

    // Display form to edit an existing book
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return bookService.findBookById(id).map(book -> {
            model.addAttribute("book", book);
            return "books/edit"; // Corresponds to src/main/resources/templates/books/edit.html
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("error", "Book not found!");
            return "redirect:/books";
        });
    }

    // Handle updating an existing book
    @PostMapping("/update/{id}")
    public String updateBook(@PathVariable Long id, @ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        book.setId(id); // Ensure the ID from the path is set to the book object
        bookService.saveBook(book);
        redirectAttributes.addFlashAttribute("message", "Book updated successfully!");
        return "redirect:/books";
    }

    // Handle deleting a book
    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.deleteBookById(id);
        redirectAttributes.addFlashAttribute("message", "Book deleted successfully!");
        return "redirect:/books";
    }
}