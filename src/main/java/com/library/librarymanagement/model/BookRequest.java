package com.library.librarymanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "book_requests")
@Getter
@Setter
public class BookRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User requestingUser;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}