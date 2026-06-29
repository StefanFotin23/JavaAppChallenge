package com.example.app.starwars.mapper;

import com.example.app.starwars.dto.CharacterDTO;
import com.example.app.starwars.dto.CharacterResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CharacterMapper {
    @Mapping(target = "name", source = "name")
    @Mapping(target = "height", source = "height", qualifiedByName = "centimetersToMeters")
    @Mapping(target = "mass", source = "mass")
    @Mapping(target = "birthYear", source = "birthYear")
    @Mapping(target = "numberOfFilms", expression = "java(dto.films() != null ? dto.films().size() : 0)")
    @Mapping(target = "dateAdded", source = "created", qualifiedByName = "formatDate")
    CharacterResponseDTO toResponse(CharacterDTO dto);

    @Named("centimetersToMeters")
    default Double centimetersToMeters(Integer cm) {
        if (cm == null || cm == -1) {
            return -1D;
        }
        return cm / 100.0;
    }

    @Named("formatDate")
    default String formatDate(String isoDate) {
        if (isoDate == null) return "N/A";
        try {
            java.time.Instant instant = java.time.Instant.parse(isoDate);
            return java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    .withZone(java.time.ZoneId.systemDefault())
                    .format(instant);
        } catch (Exception e) {
            return "N/A";
        }
    }
}
