package com.library.librarymanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate; // For handling dates
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 32, nullable = false)
    private String title;

    @Column(name = "publisher", length = 32)
    private String publisher;

    @Column(name = "publish_date") // Will be mapped as a DATE type in MySQL
    private LocalDate publishDate; // Using LocalDate for date fields

    // --- Relationship with Borrowers ---
    // A Book can be part of many Borrower records (one-to-many relationship)
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Borrower> borrowings = new HashSet<>();
}