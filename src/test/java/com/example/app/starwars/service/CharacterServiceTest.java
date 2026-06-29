package com.example.app.starwars.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.app.starwars.dto.CharacterDTO;
import com.example.app.starwars.dto.CharacterResponseDTO;
import com.example.app.starwars.mapper.CharacterMapper;
import com.example.app.starwars.repository.SwapiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock
    private SwapiRepository swapiRepository;

    @Mock
    private RedisTemplate<String, CharacterResponseDTO> characterRedisTemplate;

    @Mock
    private CharacterMapper mapper;

    @InjectMocks
    private CharacterServiceImpl characterService;

    @Test
    void testGetCharacterById() {
        String id = "1";
        CharacterDTO characterDTO = new CharacterDTO("Luke Skywalker", 172, 77, "19BBY", Collections.emptyList(), "2014-12-09T13:50:51.644000Z");
        CharacterResponseDTO mappedResponse = new CharacterResponseDTO();
        mappedResponse.setName("Luke Skywalker");
        mappedResponse.setHeight(1.72);

        when(swapiRepository.getCharacterById(id)).thenReturn(characterDTO);
        when(mapper.toResponse(characterDTO)).thenReturn(mappedResponse);

        CharacterResponseDTO result = characterService.getCharacterById(id);

        assertNotNull(result);
        assertEquals("Luke Skywalker", result.getName());
        assertEquals(1.72, result.getHeight());

        verify(swapiRepository).getCharacterById(id);
        verify(mapper).toResponse(characterDTO);
    }
}
