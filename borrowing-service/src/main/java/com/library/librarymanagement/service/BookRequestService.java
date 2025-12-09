package com.library.librarymanagement.service;

import com.library.librarymanagement.model.BookRequest;
import com.library.librarymanagement.repository.BookRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class BookRequestService {

    private final BookRequestRepository bookRequestRepository;
    private final BorrowerService borrowerService;
    private final RestTemplate restTemplate;

    @Value("${services.book.url}")
    private String bookServiceUrl;

    @Value("${services.member.url}")
    private String memberServiceUrl;

    @Autowired
    public BookRequestService(BookRequestRepository bookRequestRepository,
                              BorrowerService borrowerService,
                              RestTemplate restTemplate) {
        this.bookRequestRepository = bookRequestRepository;
        this.borrowerService = borrowerService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public BookRequest createRequest(Long userId, Long bookId) {
        // 1. Validate Book existence via Book Service
        try {
            restTemplate.getForEntity(bookServiceUrl + "/" + bookId, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Book not found with ID: " + bookId);
        }

        // 2. Create Request
        BookRequest newRequest = new BookRequest();
        newRequest.setUserId(userId);
        newRequest.setBookId(bookId);
        newRequest.setRequestDate(LocalDate.now());
        newRequest.setStatus(BookRequest.RequestStatus.PENDING);

        return bookRequestRepository.save(newRequest);
    }

    public List<BookRequest> findMyRequests(Long userId) {
        return bookRequestRepository.findByUserId(userId);
    }

    public List<BookRequest> findAllRequests() {
        return bookRequestRepository.findAll();
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public BookRequest approveRequest(Long requestId) {
        BookRequest request = bookRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (request.getStatus() != BookRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in PENDING state.");
        }

        // Fetch Member details using User ID to link the borrowing record correctly.
        Long memberId = null;
        try {
            // Target URL: member-service/api/users/{id}
            String userUrl = memberServiceUrl.replace("/members", "/users") + "/" + request.getUserId();
            Map<String, Object> userResponse = restTemplate.getForObject(userUrl, Map.class);

            if (userResponse != null && userResponse.get("memberProfile") != null) {
                Map<String, Object> memberProfile = (Map<String, Object>) userResponse.get("memberProfile");
                memberId = ((Number) memberProfile.get("id")).longValue();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not fetch User details from Member Service. Approval failed.", e);
        }

        if (memberId == null) {
            throw new IllegalStateException("User does not have a linked Member profile.");
        }

        request.setStatus(BookRequest.RequestStatus.APPROVED);

        // Issue the book via BorrowerService
        borrowerService.issueBook(
                memberId,
                request.getBookId(),
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        return bookRequestRepository.save(request);
    }

    @Transactional
    public BookRequest rejectRequest(Long requestId) {
        BookRequest request = bookRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (request.getStatus() != BookRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in PENDING state.");
        }

        request.setStatus(BookRequest.RequestStatus.REJECTED);
        return bookRequestRepository.save(request);
    }
}