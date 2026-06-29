package com.example.app.starwars.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public record CharacterDTO(
        String name,
        Integer height,
        Integer mass,
        String birthYear,
        List<String> films,
        String created
) {
    @JsonCreator
    public CharacterDTO(
            @JsonProperty("name") String name,
            @JsonProperty("height") String height,
            @JsonProperty("mass") String mass,
            @JsonProperty("birth_year") String birthYear,
            @JsonProperty("films") List<String> films,
            @JsonProperty("created") String created
    ) {
        this(
                name,
                parseInteger(height, "height"),
                parseInteger(mass, "mass"),
                birthYear,
                films,
                created
        );
    }

    private static Integer parseInteger(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return -1;
        }

        try {
            String sanitized = value.replace(",", ".");
            double d = Double.parseDouble(sanitized);
            return (int) d;
        } catch (NumberFormatException | NullPointerException e) {
            log.error("Failed to parse {}: '{}'. Defaulting to -1", fieldName, value);
            return -1;
        }
    }
}