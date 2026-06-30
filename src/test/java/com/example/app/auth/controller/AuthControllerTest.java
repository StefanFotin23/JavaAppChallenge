package com.example.app.auth.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.app.auth.dto.AuthResponse;
import com.example.app.auth.service.AuthService;
import com.example.app.shared.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private AuthService authService;

  @InjectMocks private AuthController authController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void testLoginSuccess() throws Exception {
    AuthResponse mockResponse = new AuthResponse("mockAccess", "mockRefresh", "admin");
    when(authService.login("admin", "password")).thenReturn(mockResponse);

    Map<String, String> credentials = Map.of("username", "admin", "password", "password");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("mockAccess"))
        .andExpect(jsonPath("$.refreshToken").value("mockRefresh"))
        .andExpect(jsonPath("$.username").value("admin"));
  }

  @Test
  void testRefreshSuccess() throws Exception {
    AuthResponse mockResponse = new AuthResponse("newMockAccess", "mockRefresh", "admin");
    when(authService.refresh("mockRefresh")).thenReturn(mockResponse);

    Map<String, String> body = Map.of("refreshToken", "mockRefresh");

    mockMvc
        .perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("newMockAccess"));
  }

  @Test
  void testGetCurrentUser() throws Exception {
    var auth = new UsernamePasswordAuthenticationToken("testuser", null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    try {
      mockMvc
          .perform(get("/auth/user"))
          .andExpect(status().isOk())
          .andExpect(content().string("You are authenticated as: testuser"));
    } finally {
      SecurityContextHolder.clearContext();
    }
  }
}
