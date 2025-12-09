package com.library.librarymanagement.service;

import com.library.librarymanagement.model.Borrower;
import com.library.librarymanagement.repository.BorrowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;
    private final RestTemplate restTemplate;

    @Value("${services.book.url}")
    private String bookServiceUrl;

    @Value("${services.member.url}")
    private String memberServiceUrl;

    @Autowired
    public BorrowerService(BorrowerRepository borrowerRepository, RestTemplate restTemplate) {
        this.borrowerRepository = borrowerRepository;
        this.restTemplate = restTemplate;
    }

    public List<Borrower> findAllBorrowings() {
        return borrowerRepository.findAll();
    }

    public Optional<Borrower> findBorrowingById(Long id) {
        return borrowerRepository.findById(id);
    }

    @Transactional
    public Borrower issueBook(Long memberId, Long bookId, LocalDate issueDate, LocalDate dueDate) {
        // 1. Verify Member existence
        try {
            restTemplate.getForEntity(memberServiceUrl + "/" + memberId, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Member not found with ID: " + memberId);
        } catch (Exception e) {
            // Log warning but proceed if service is temporarily unreachable (circuit breaker logic could go here)
            System.err.println("Warning: Could not verify member existence via microservice.");
        }

        // 2. Verify Book existence
        try {
            restTemplate.getForEntity(bookServiceUrl + "/" + bookId, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Book not found with ID: " + bookId);
        }

        // 3. Create Borrowing Record
        Borrower borrower = new Borrower();
        borrower.setMemberId(memberId);
        borrower.setBookId(bookId);
        borrower.setIssueDate(issueDate);
        borrower.setDueDate(dueDate);

        return borrowerRepository.save(borrower);
    }

    @Transactional
    public Borrower returnBook(Long borrowingId, LocalDate returnDate) {
        Borrower borrower = borrowerRepository.findById(borrowingId)
                .orElseThrow(() -> new IllegalArgumentException("Borrowing record not found with ID: " + borrowingId));

        if (borrower.getReturnDate() != null) {
            throw new IllegalStateException("Book already returned for borrowing ID: " + borrowingId);
        }
        borrower.setReturnDate(returnDate);
        return borrowerRepository.save(borrower);
    }

    public void deleteBorrowingById(Long id) {
        borrowerRepository.deleteById(id);
    }
}