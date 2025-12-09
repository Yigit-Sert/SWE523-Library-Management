package com.library.librarymanagement.service;

import com.library.librarymanagement.exception.ResourceNotFoundException;
import com.library.librarymanagement.model.User;
import com.library.librarymanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User changeUserRole(String email, User.Role newRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setRole(newRole);
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfilePictureUrl(String email, String fileUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        user.setProfilePictureUrl(fileUrl);
        return userRepository.save(user);
    }
}