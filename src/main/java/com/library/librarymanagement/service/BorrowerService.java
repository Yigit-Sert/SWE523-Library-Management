package com.library.librarymanagement.service;

import com.library.librarymanagement.model.Book;
import com.library.librarymanagement.model.Borrower;
import com.library.librarymanagement.model.Member;
import com.library.librarymanagement.repository.BorrowerRepository;
import com.library.librarymanagement.repository.BookRepository;
import com.library.librarymanagement.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    @Autowired
    public BorrowerService(BorrowerRepository borrowerRepository, MemberRepository memberRepository, BookRepository bookRepository) {
        this.borrowerRepository = borrowerRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
    }

    public List<Borrower> findAllBorrowings() {
        return borrowerRepository.findAllWithMemberAndBook();
    }
    public Optional<Borrower> findBorrowingById(Long id) {
        return borrowerRepository.findById(id);
    }

    @Transactional
    public Borrower issueBook(Long memberId, Long bookId, LocalDate issueDate, LocalDate dueDate) {
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
    public Borrower returnBook(Long borrowingId, LocalDate returnDate) {
        Borrower borrower = borrowerRepository.findByIdWithMemberAndBook(borrowingId)
                .orElseThrow(() -> new IllegalArgumentException("Borrowing record not found with ID: " + borrowingId));

        if (borrower.getReturnDate() != null) {
            throw new IllegalStateException("Book already returned for borrowing ID: " + borrowingId);
        }
        borrower.setReturnDate(returnDate);
        return borrower;
    }

    public void deleteBorrowingById(Long id) {
        borrowerRepository.deleteById(id);
    }

    // You can add more specific business logic here later, e.g., for overdue books.
}