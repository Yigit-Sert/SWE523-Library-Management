package com.library.librarymanagement.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BookRequestDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long bookId;
    private String bookTitle;
    private LocalDate requestDate;
    private String status;
}