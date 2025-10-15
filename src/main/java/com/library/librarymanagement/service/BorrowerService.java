package com.library.librarymanagement.service;

import com.library.librarymanagement.model.Book;
import com.library.librarymanagement.model.Borrower;
import com.library.librarymanagement.model.Member;
import com.library.librarymanagement.repository.BorrowerRepository;
import com.library.librarymanagement.repository.BookRepository;
import com.library.librarymanagement.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowerService {

    private static final Logger log = LoggerFactory.getLogger(BorrowerService.class);
    private final BorrowerRepository borrowerRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    @Autowired
    public BorrowerService(BorrowerRepository borrowerRepository, MemberRepository memberRepository, BookRepository bookRepository) {
        this.borrowerRepository = borrowerRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
    }

    @Cacheable("borrowings_all")
    public List<Borrower> findAllBorrowings() {
        log.info("Fetching all borrowings from the database...");
        return borrowerRepository.findAllWithMemberAndBook();
    }

    @Cacheable(value = "borrowings", key = "#id")
    public Optional<Borrower> findBorrowingById(Long id) {
        log.info("Fetching borrowing from database: id={}", id);
        return borrowerRepository.findById(id);
    }

    @Transactional
    @CacheEvict(value = "borrowings_all", allEntries = true)
    public Borrower issueBook(Long memberId, Long bookId, LocalDate issueDate, LocalDate dueDate) {
        log.info("Issuing a book and evicting 'borrowings_all' cache...");
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + bookId));

        Borrower borrower = new Borrower();
        borrower.setMember(member);
        borrower.setBook(book);
        borrower.setIssueDate(issueDate);
        borrower.setDueDate(dueDate);

        return borrowerRepository.save(borrower);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "borrowings", key = "#borrowingId"),
            @CacheEvict(value = "borrowings_all", allEntries = true)
    })
    public Borrower returnBook(Long borrowingId, LocalDate returnDate) {
        log.info("Returning a book and evicting caches for id={} and 'borrowings_all'", borrowingId);
        Borrower borrower = borrowerRepository.findByIdWithMemberAndBook(borrowingId)
                .orElseThrow(() -> new IllegalArgumentException("Borrowing record not found with ID: " + borrowingId));

        if (borrower.getReturnDate() != null) {
            throw new IllegalStateException("Book already returned for borrowing ID: " + borrowingId);
        }
        borrower.setReturnDate(returnDate);
        return borrower;
    }

    @Caching(evict = {
            @CacheEvict(value = "borrowings", key = "#id"),
            @CacheEvict(value = "borrowings_all", allEntries = true)
    })
    public void deleteBorrowingById(Long id) {
        log.info("Deleting borrowing and evicting caches for id={} and 'borrowings_all'", id);
        borrowerRepository.deleteById(id);
    }
}