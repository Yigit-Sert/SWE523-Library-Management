package com.library.librarymanagement.service;

import com.library.librarymanagement.exception.ResourceNotFoundException;
import com.library.librarymanagement.model.*;
import com.library.librarymanagement.repository.BookRepository;
import com.library.librarymanagement.repository.BookRequestRepository;
import com.library.librarymanagement.repository.BorrowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookRequestService {

    @Autowired
    private BookRequestRepository bookRequestRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BorrowerService borrowerService;

    @Transactional
    public BookRequest createRequest(User user, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        BookRequest newRequest = new BookRequest();
        newRequest.setRequestingUser(user);
        newRequest.setBook(book);
        newRequest.setRequestDate(LocalDate.now());
        newRequest.setStatus(BookRequest.RequestStatus.PENDING);

        return bookRequestRepository.save(newRequest);
    }

    public List<BookRequest> findMyRequests(User user) {
        return bookRequestRepository.findByRequestingUser(user);
    }

    public List<BookRequest> findAllRequests() {
        return bookRequestRepository.findAllWithDetails();
    }

    @Transactional
    public BookRequest approveRequest(Long requestId) {
        BookRequest request = bookRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (request.getStatus() != BookRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in PENDING state.");
        }

        request.setStatus(BookRequest.RequestStatus.APPROVED);

        borrowerService.issueBook(
                request.getRequestingUser().getMemberProfile().getId(),
                request.getBook().getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        return bookRequestRepository.save(request);
    }

    @Transactional
    public BookRequest rejectRequest(Long requestId) {
        BookRequest request = bookRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (request.getStatus() != BookRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in PENDING state.");
        }

        request.setStatus(BookRequest.RequestStatus.REJECTED);
        return bookRequestRepository.save(request);
    }
}