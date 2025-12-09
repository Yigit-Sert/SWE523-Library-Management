package com.library.librarymanagement.controller;

import com.library.librarymanagement.dto.BookDto;
import com.library.librarymanagement.dto.BorrowerDto;
import com.library.librarymanagement.dto.MemberDto;
import com.library.librarymanagement.model.Borrower;
import com.library.librarymanagement.service.BorrowerService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/borrowings")
public class BorrowerController {

    private final BorrowerService borrowerService;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;

    @Value("${services.book.url}")
    private String bookServiceUrl;

    @Value("${services.member.url}")
    private String memberServiceUrl;

    @Autowired
    public BorrowerController(BorrowerService borrowerService, ModelMapper modelMapper, RestTemplate restTemplate) {
        this.borrowerService = borrowerService;
        this.modelMapper = modelMapper;
        this.restTemplate = restTemplate;
    }

    private BorrowerDto convertToDto(Borrower borrower) {
        BorrowerDto borrowerDto = modelMapper.map(borrower, BorrowerDto.class);

        // --- DÜZELTME BURADA YAPILDI ---
        // Eski: borrowerDto.setMemberId(borrower.getMember().getId());
        // Yeni: borrowerDto.setMemberId(borrower.getMemberId());

        borrowerDto.setMemberId(borrower.getMemberId());
        borrowerDto.setBookId(borrower.getBookId());

        // İsimleri almak için diğer servislere soruyoruz
        try {
            // Member Service'den üye ismini çek
            if (borrower.getMemberId() != null) {
                MemberDto member = restTemplate.getForObject(memberServiceUrl + "/" + borrower.getMemberId(), MemberDto.class);
                borrowerDto.setMemberName(member != null ? member.getName() : "Unknown Member");
            }

            // Book Service'den kitap ismini çek
            if (borrower.getBookId() != null) {
                BookDto book = restTemplate.getForObject(bookServiceUrl + "/" + borrower.getBookId(), BookDto.class);
                borrowerDto.setBookTitle(book != null ? book.getTitle() : "Unknown Book");
            }
        } catch (Exception e) {
            // Servis kapalıysa veya hata varsa
            borrowerDto.setMemberName("Service Unavailable");
            borrowerDto.setBookTitle("Service Unavailable");
            System.err.println("Microservice fetch error: " + e.getMessage());
        }

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