package com.library.librarymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller; // Import Controller
import org.springframework.web.bind.annotation.GetMapping; // Import GetMapping

@SpringBootApplication
@Controller // Mark the main application class as a Controller for the root mapping
public class LibraryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "index"; // This will resolve to src/main/resources/templates/index.html
    }
}