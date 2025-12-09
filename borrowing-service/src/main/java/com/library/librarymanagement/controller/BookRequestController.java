package com.library.librarymanagement.controller;

import com.library.librarymanagement.dto.BookDto;
import com.library.librarymanagement.dto.BookRequestDto;
import com.library.librarymanagement.dto.UserDto;
import com.library.librarymanagement.model.BookRequest;
import com.library.librarymanagement.service.BookRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/requests")
public class BookRequestController {

    @Autowired
    private BookRequestService bookRequestService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.member.url}")
    private String memberServiceUrl;

    @Value("${services.book.url}")
    private String bookServiceUrl;

    private BookRequestDto convertToDto(BookRequest request) {
        BookRequestDto dto = new BookRequestDto();
        dto.setId(request.getId());
        dto.setUserId(request.getUserId());
        dto.setBookId(request.getBookId());
        dto.setRequestDate(request.getRequestDate());
        dto.setStatus(request.getStatus().name());

        try {
            String userUrl = memberServiceUrl.replace("/members", "/users") + "/" + request.getUserId();
            UserDto user = restTemplate.getForObject(userUrl, UserDto.class);
            dto.setUserName(user != null ? user.getName() : "Unknown User");
        } catch (Exception e) {
            dto.setUserName("Service Unavailable");
        }

        try {
            String bookUrl = bookServiceUrl + "/" + request.getBookId();
            BookDto book = restTemplate.getForObject(bookUrl, BookDto.class);
            dto.setBookTitle(book != null ? book.getTitle() : "Unknown Book");
        } catch (Exception e) {
            dto.setBookTitle("Service Unavailable");
        }

        return dto;
    }

    private Long getAuthenticatedUserId(OAuth2User principal) {
        String email = principal.getAttribute("email");
        String url = memberServiceUrl.replace("/members", "/users") + "/search?email=" + email;

        try {
            UserDto user = restTemplate.getForObject(url, UserDto.class);
            if (user == null) {
                throw new RuntimeException("User not found for email: " + email);
            }
            return user.getId();
        } catch (Exception e) {
            throw new RuntimeException("Could not fetch user details.", e);
        }
    }

    @PostMapping
    public ResponseEntity<BookRequest> createBookRequest(@RequestBody Map<String, Long> payload, @AuthenticationPrincipal OAuth2User principal) {
        Long userId = getAuthenticatedUserId(principal);
        Long bookId = payload.get("bookId");
        BookRequest newRequest = bookRequestService.createRequest(userId, bookId);
        return new ResponseEntity<>(newRequest, HttpStatus.CREATED);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<BookRequestDto>> getMyRequests(@AuthenticationPrincipal OAuth2User principal) {
        Long userId = getAuthenticatedUserId(principal);
        List<BookRequest> requests = bookRequestService.findMyRequests(userId);

        List<BookRequestDto> dtos = requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping
    public ResponseEntity<List<BookRequestDto>> getAllRequests() {
        List<BookRequest> requests = bookRequestService.findAllRequests();
        List<BookRequestDto> dtos = requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
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