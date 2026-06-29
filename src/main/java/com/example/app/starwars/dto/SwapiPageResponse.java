package com.example.app.starwars.dto;

import java.util.List;

public record SwapiPageResponse(
        int count,
        String next,
        String previous,
        List<CharacterDTO> results
) {}
