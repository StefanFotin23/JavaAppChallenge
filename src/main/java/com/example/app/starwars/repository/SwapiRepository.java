package com.example.app.starwars.repository;


import com.example.app.starwars.client.SwapiClient;
import com.example.app.starwars.dto.SwapiPageResponse;
import com.example.app.starwars.dto.CharacterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SwapiRepository {
    private final SwapiClient swapiClient;

    @Cacheable(value = "swapiPeople", key = "#page")
    public List<CharacterDTO> getPeople(int page) {
        SwapiPageResponse response = swapiClient.fetchPeople(page);
        return (response != null && response.results() != null)
                ? response.results()
                : Collections.emptyList();
    }

    @Cacheable(value = "characterDetails", key = "#id")
    public CharacterDTO getCharacterById(String id) {
        return swapiClient.fetchPeopleById(id);
    }
}
