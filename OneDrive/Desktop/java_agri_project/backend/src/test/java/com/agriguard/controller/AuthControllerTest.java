package com.agriguard.controller;

import com.agriguard.dto.AuthRequest;
import com.agriguard.dto.AuthResponse;
import com.agriguard.dto.RegisterRequest;
import com.agriguard.entity.User;
import com.agriguard.service.AuthService;
import com.agriguard.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtils jwtUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/auth/register should successfully register user")
    void testRegisterUser() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("green_farmer");
        req.setEmail("farmer@agri.com");
        req.setPassword("pass12345");
        req.setFullName("John Appleseed");

        AuthResponse resp = new AuthResponse("mock-jwt-token", 1L, "green_farmer", "farmer@agri.com", "John Appleseed", "USER", "User registered successfully");
        when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value("green_farmer"))
                .andExpect(jsonPath("$.fullName").value("John Appleseed"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return JWT on valid credentials")
    void testLoginUser() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("green_farmer");
        req.setPassword("pass12345");

        AuthResponse resp = new AuthResponse("jwt-valid-token", 1L, "green_farmer", "farmer@agri.com", "John Appleseed", "USER", "Login successful");
        when(authService.login(any(AuthRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-valid-token"))
                .andExpect(jsonPath("$.username").value("green_farmer"));
    }

    @Test
    @DisplayName("GET /api/auth/me should return current user when token is valid")
    void testGetCurrentUser() throws Exception {
        String token = "valid-bearer-token";
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(token)).thenReturn("green_farmer");

        User user = new User("green_farmer", "farmer@agri.com", "hash", "John Appleseed", "USER");
        user.setId(1L);
        when(authService.findByUsername("green_farmer")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("green_farmer"))
                .andExpect(jsonPath("$.email").value("farmer@agri.com"));
    }
}
