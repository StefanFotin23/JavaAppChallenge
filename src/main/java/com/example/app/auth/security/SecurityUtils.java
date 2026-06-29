package com.example.app.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.security.Principal;

@Slf4j
@Component
public class SecurityUtils {
    public void checkAuthorization(Principal principal) throws AccessDeniedException {
        if (principal == null || principal.getName() == null) {
            throw new AccessDeniedException("User is not authenticated");
        }
    }
}
