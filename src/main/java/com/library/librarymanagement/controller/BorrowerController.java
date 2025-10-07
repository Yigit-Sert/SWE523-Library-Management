package com.library.librarymanagement.controller;

import com.library.librarymanagement.model.Borrower;
import com.library.librarymanagement.model.Member;
import com.library.librarymanagement.model.Book;
import com.library.librarymanagement.service.BorrowerService;
import com.library.librarymanagement.service.MemberService;
import com.library.librarymanagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/borrowings")
public class BorrowerController {

    private final BorrowerService borrowerService;
    private final MemberService memberService;
    private final BookService bookService;

    @Autowired
    public BorrowerController(BorrowerService borrowerService, MemberService memberService, BookService bookService) {
        this.borrowerService = borrowerService;
        this.memberService = memberService;
        this.bookService = bookService;
    }

    // Display a list of all borrowing records
    @GetMapping
    public String listBorrowings(Model model) {
        List<Borrower> borrowings = borrowerService.findAllBorrowings();
        model.addAttribute("borrowings", borrowings);
        model.addAttribute("newBorrowing", new Borrower()); // For the "Issue Book" form

        // Also add members and books to the model for dropdowns in the issue form
        List<Member> members = memberService.findAllMembers();
        List<Book> books = bookService.findAllBooks();
        model.addAttribute("members", members);
        model.addAttribute("books", books);

        return "borrowings/index"; // Corresponds to src/main/resources/templates/borrowings/index.html
    }

    // Handle issuing a new book (POST request from the form)
    @PostMapping("/issue")
    public String issueBook(@RequestParam Long memberId,
                            @RequestParam Long bookId,
                            @RequestParam LocalDate issueDate,
                            @RequestParam LocalDate dueDate,
                            RedirectAttributes redirectAttributes) {
        try {
            borrowerService.issueBook(memberId, bookId, issueDate, dueDate);
            redirectAttributes.addFlashAttribute("message", "Book issued successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/borrowings";
    }

    // Handle returning a book
    @PostMapping("/return/{id}")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            borrowerService.returnBook(id, LocalDate.now()); // Set return date to today
            redirectAttributes.addFlashAttribute("message", "Book returned successfully!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/borrowings";
    }

    // Handle deleting a borrowing record
    @PostMapping("/delete/{id}")
    public String deleteBorrowing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        borrowerService.deleteBorrowingById(id);
        redirectAttributes.addFlashAttribute("message", "Borrowing record deleted successfully!");
        return "redirect:/borrowings";
    }
}