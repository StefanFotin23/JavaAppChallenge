package com.example.app.starwars.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.app.auth.security.SecurityUtils;
import com.example.app.shared.exception.GlobalExceptionHandler;
import com.example.app.starwars.dto.CharacterDTO;
import com.example.app.starwars.dto.CharacterResponseDTO;
import com.example.app.starwars.service.CharacterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class CharacterControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CharacterService characterService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CharacterController characterController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(characterController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetCharactersSuccess() throws Exception {
        CharacterDTO character = new CharacterDTO("Luke Skywalker", 172, 77, "19BBY", Collections.emptyList(), "2014-12-09T13:50:51.644000Z");
        when(characterService.getPeople(1)).thenReturn(Collections.singletonList(character));

        mockMvc.perform(get("/people?page=1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Luke Skywalker"));
    }

    @Test
    void testGetCharactersEmptyReturns404() throws Exception {
        when(characterService.getPeople(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/people?page=1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFavourites() throws Exception {
        CharacterResponseDTO character = new CharacterResponseDTO();
        character.setName("Yoda");
        when(characterService.getFavouriteCharacters("admin")).thenReturn(Collections.singletonList(character));

        Principal principal = () -> "admin";

        mockMvc.perform(get("/favourites")
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Yoda"));
    }

    @Test
    void testAddFavouriteSuccess() throws Exception {
        CharacterResponseDTO character = new CharacterResponseDTO();
        character.setName("Obi-Wan");
        when(characterService.addFavouriteCharacter(anyString(), any())).thenReturn(true);

        Principal principal = () -> "admin";

        mockMvc.perform(post("/favourites")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(character)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
}
