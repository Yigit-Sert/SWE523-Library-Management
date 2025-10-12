package com.library.librarymanagement.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BorrowerDto {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long bookId;
    private String bookTitle;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
}