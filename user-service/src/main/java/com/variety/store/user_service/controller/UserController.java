package com.variety.store.user_service.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/user/info")
    public String getUserInfo(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaim("preferred_username");
        return "Authenticated user: " + username;
    }


}
