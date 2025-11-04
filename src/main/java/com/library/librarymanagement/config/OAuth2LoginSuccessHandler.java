package com.library.librarymanagement.config;

import com.library.librarymanagement.service.CustomOAuth2UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
        String principalName = authentication.getName();

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                clientRegistrationId,
                principalName
        );

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

        OAuth2UserRequest userRequest = new OAuth2UserRequest(
                authorizedClient.getClientRegistration(),
                accessToken
        );

        customOAuth2UserService.loadUser(userRequest);

        setDefaultTargetUrl("/api/books");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}