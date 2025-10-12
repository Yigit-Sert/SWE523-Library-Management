package com.library.librarymanagement.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BookDto {
    private Long id;
    private String title;
    private String publisher;
    private LocalDate publishDate;
}