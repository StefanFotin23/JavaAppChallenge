package com.example.app.starwars.service;

import com.example.app.starwars.dto.CharacterDTO;
import com.example.app.starwars.dto.CharacterResponseDTO;
import com.example.app.starwars.mapper.CharacterMapper;
import com.example.app.starwars.repository.SwapiRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterServiceImpl implements CharacterService {
  private static final String FAVOURITES = "FAVOURITES:";
  private final SwapiRepository swapiRepository;
  private final RedisTemplate<String, CharacterResponseDTO> characterRedisTemplate;
  private final CharacterMapper mapper;

  public List<CharacterDTO> getPeople(int page) {
    return swapiRepository.getPeople(page);
  }

  public CharacterResponseDTO getCharacterById(String id) {
    CharacterDTO characterDTO = swapiRepository.getCharacterById(id);
    return mapper.toResponse(characterDTO);
  }

  @Override
  public List<CharacterResponseDTO> getFavouriteCharacters(String username) {
    return characterRedisTemplate.opsForList().range(FAVOURITES + username, 0, -1);
  }

  @Override
  public boolean addFavouriteCharacter(String username, CharacterResponseDTO character) {
    Long newIndex = characterRedisTemplate.opsForList().rightPush(FAVOURITES + username, character);
    if (newIndex == null || newIndex == 0) {
      log.error("Failed to add character to Redis for user: {} character: {}", username, character);
      throw new RuntimeException("Could not save to favourites");
    }
    log.info("Successfully added character. New list size: {}", newIndex);
    return true;
  }
}
