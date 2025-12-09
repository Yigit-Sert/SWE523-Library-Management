package com.library.librarymanagement.service;

import com.library.librarymanagement.model.Member;
import com.library.librarymanagement.model.User;
import com.library.librarymanagement.repository.MemberRepository;
import com.library.librarymanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOidcUserService extends OidcUserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOidcUserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oidcUser.getAttributes();

        User user = findOrCreateUser(attributes);

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        log.info("User '{}' authenticated. Granting authorities: {}", user.getEmail(), authorities);

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    private User findOrCreateUser(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            log.info("Found existing user: {}", email);
            return userOptional.get();
        } else {
            log.info("Creating new user and member profile for: {}", email);

            // Create Member profile first
            Member newMemberProfile = new Member();
            newMemberProfile.setName((String) attributes.get("name"));
            memberRepository.save(newMemberProfile);

            // Create User account linked to Member
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName((String) attributes.get("name"));
            newUser.setProfilePictureUrl((String) attributes.get("picture"));
            newUser.setRole(User.Role.MEMBER);
            newUser.setMemberProfile(newMemberProfile);

            return userRepository.save(newUser);
        }
    }
}