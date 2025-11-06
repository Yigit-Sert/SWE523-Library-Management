package com.library.librarymanagement.controller;

import com.library.librarymanagement.exception.ResourceNotFoundException;
import com.library.librarymanagement.model.BookRequest;
import com.library.librarymanagement.model.User;
import com.library.librarymanagement.repository.UserRepository;
import com.library.librarymanagement.service.BookRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class BookRequestController {

    @Autowired
    private BookRequestService bookRequestService;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser(OAuth2User principal) {
        String email = principal.getAttribute("email");
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in DB"));
    }

    @PostMapping
    public ResponseEntity<BookRequest> createBookRequest(@RequestBody Map<String, Long> payload, @AuthenticationPrincipal OAuth2User principal) {
        User user = getAuthenticatedUser(principal);
        Long bookId = payload.get("bookId");
        BookRequest newRequest = bookRequestService.createRequest(user, bookId);
        return new ResponseEntity<>(newRequest, HttpStatus.CREATED);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<BookRequest>> getMyRequests(@AuthenticationPrincipal OAuth2User principal) {
        User user = getAuthenticatedUser(principal);
        List<BookRequest> requests = bookRequestService.findMyRequests(user);
        return ResponseEntity.ok(requests);
    }

    @GetMapping
    public ResponseEntity<List<BookRequest>> getAllRequests() {
        List<BookRequest> requests = bookRequestService.findAllRequests();
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<BookRequest> approveRequest(@PathVariable Long id) {
        BookRequest approvedRequest = bookRequestService.approveRequest(id);
        return ResponseEntity.ok(approvedRequest);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<BookRequest> rejectRequest(@PathVariable Long id) {
        BookRequest rejectedRequest = bookRequestService.rejectRequest(id);
        return ResponseEntity.ok(rejectedRequest);
    }
}