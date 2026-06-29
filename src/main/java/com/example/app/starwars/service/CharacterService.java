package com.example.app.starwars.service;

import com.example.app.starwars.dto.CharacterDTO;
import com.example.app.starwars.dto.CharacterResponseDTO;

import java.util.List;

public interface CharacterService {
    List<CharacterDTO> getPeople(int page);
    CharacterResponseDTO getCharacterById(String id);
    List<CharacterResponseDTO> getFavouriteCharacters(String username);
    boolean addFavouriteCharacter(String username, CharacterResponseDTO character);
}
