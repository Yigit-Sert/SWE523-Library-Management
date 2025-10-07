package com.library.librarymanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate; // For handling dates

@Entity
@Table(name = "borrowers")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Optional: If you want to define a composite primary key explicitly
// @IdClass(BorrowerId.class) // We'll create BorrowerId later if needed for explicit composite PK
public class Borrower {

    // For simplicity, let's start with a single auto-generated ID for Borrower records.
    // This allows a member to borrow the same book multiple times (each borrow is a new record).
    // If we wanted (member_id, book_id, issue_date) as the composite PK, it would be more complex.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Relationships ---
    // Many Borrowers can belong to one Member (many-to-one relationship)
    @ManyToOne(fetch = FetchType.LAZY) // FetchType.LAZY means load member details only when accessed
    @JoinColumn(name = "member_id", nullable = false) // Foreign key column in 'borrowers' table
    private Member member;

    // Many Borrowers can refer to one Book (many-to-one relationship)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false) // Foreign key column in 'borrowers' table
    private Book book;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date") // Can be null if the book hasn't been returned yet
    private LocalDate returnDate;

    // A utility method to set the member and book for a borrowing record
    // This helps manage both sides of the relationship
    public void setMemberAndBook(Member member, Book book) {
        this.member = member;
        this.book = book;
        member.getBorrowings().add(this); // Add this borrowing to the member's list
        book.getBorrowings().add(this);   // Add this borrowing to the book's list
    }
}