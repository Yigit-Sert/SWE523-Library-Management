package com.library.librarymanagement.controller;

import com.library.librarymanagement.model.User;
import com.library.librarymanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public List<User> listUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/users/{email}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable String email, @RequestBody Map<String, String> roleMap) {
        try {
            User.Role newRole = User.Role.valueOf(roleMap.get("role").toUpperCase());
            User updatedUser = userService.changeUserRole(email, newRole);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}