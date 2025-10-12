package com.library.librarymanagement.controller;

import com.library.librarymanagement.dto.BorrowerDto;
import com.library.librarymanagement.model.Borrower;
import com.library.librarymanagement.service.BorrowerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/borrowings")
public class BorrowerController {

    private final BorrowerService borrowerService;
    private final ModelMapper modelMapper;

    @Autowired
    public BorrowerController(BorrowerService borrowerService, ModelMapper modelMapper) {
        this.borrowerService = borrowerService;
        this.modelMapper = modelMapper;
    }

    private BorrowerDto convertToDto(Borrower borrower) {
        BorrowerDto borrowerDto = modelMapper.map(borrower, BorrowerDto.class);
        borrowerDto.setMemberId(borrower.getMember().getId());
        borrowerDto.setMemberName(borrower.getMember().getName());
        borrowerDto.setBookId(borrower.getBook().getId());
        borrowerDto.setBookTitle(borrower.getBook().getTitle());
        return borrowerDto;
    }

    @GetMapping
    public List<BorrowerDto> getAllBorrowings() {
        return borrowerService.findAllBorrowings().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/issue")
    public ResponseEntity<BorrowerDto> issueBook(@RequestBody BorrowerDto borrowerDto) {
        Borrower newBorrowing = borrowerService.issueBook(
                borrowerDto.getMemberId(),
                borrowerDto.getBookId(),
                borrowerDto.getIssueDate(),
                borrowerDto.getDueDate()
        );
        return new ResponseEntity<>(convertToDto(newBorrowing), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowerDto> returnBook(@PathVariable Long id) {
        Borrower returnedBorrowing = borrowerService.returnBook(id, LocalDate.now());
        return ResponseEntity.ok(convertToDto(returnedBorrowing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBorrowing(@PathVariable Long id) {
        borrowerService.deleteBorrowingById(id);
        return ResponseEntity.noContent().build();
    }
}