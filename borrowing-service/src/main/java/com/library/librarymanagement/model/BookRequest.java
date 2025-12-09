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

    // ESKİ: private User requestingUser;
    // YENİ: Kullanıcı ID'si. (User entity'si Member servisinde kaldı)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ESKİ: private Book book;
    // YENİ: Kitap ID'si.
    @Column(name = "book_id", nullable = false)
    private Long bookId;

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