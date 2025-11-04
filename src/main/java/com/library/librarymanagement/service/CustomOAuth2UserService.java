package com.library.librarymanagement.service;

import com.library.librarymanagement.model.User;
import com.library.librarymanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    // logging
    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");

        log.info("Loading OAuth2 user. Email: {}", email);

        User user = findOrCreateUser(attributes);

        log.info("User found or created in database. Role: {}", user.getRole());

        Set<GrantedAuthority> authorities = new HashSet<>();
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());
        authorities.add(authority);

        log.info("Authorities granted to user: {}", authorities);

        return new DefaultOAuth2User(authorities, attributes, "name");
    }

    private User findOrCreateUser(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        log.info("Entering findOrCreateUser method. Email: {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            log.info("Existing user found in the database.");
            return userOptional.get();
        } else {
            log.info("Creating a new user.");
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName((String) attributes.get("name"));
            newUser.setProfilePictureUrl((String) attributes.get("picture"));
            newUser.setRole(User.Role.USER);

            try {
                User savedUser = userRepository.save(newUser);
                log.info("New user successfully saved to the database. ID: {}", savedUser.getId());
                return savedUser;
            } catch (Exception e) {
                log.error("!!! AN ERROR OCCURRED WHILE SAVING THE USER !!!", e);
                throw e;
            }
        }
    }
}