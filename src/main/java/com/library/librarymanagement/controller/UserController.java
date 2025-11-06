package com.library.librarymanagement.controller;

import com.library.librarymanagement.service.FileStorageService;
import com.library.librarymanagement.service.UserService;
import com.library.librarymanagement.model.User;
import com.library.librarymanagement.repository.UserRepository;
import com.library.librarymanagement.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @PostMapping("/profile/picture")
    public ResponseEntity<String> uploadProfilePicture(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal OAuth2User principal) {
        String filename = fileStorageService.store(file);
        String email = principal.getAttribute("email");

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/users/profile/picture/")
                .path(filename)
                .toUriString();

        userService.updateProfilePictureUrl(email, fileDownloadUri);

        return ResponseEntity.ok("File uploaded successfully. Download URI: " + fileDownloadUri);
    }

    @GetMapping("/profile/picture/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = fileStorageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return ResponseEntity.ok(user);
    }
}