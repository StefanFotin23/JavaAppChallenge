package com.example.app.starwars.dto;

import lombok.Data;

@Data
public class CharacterResponseDTO {
    private String name;
    private Integer height;
    private Integer mass;
    private String birthYear;
    private Integer numberOfFilms;
    private String dateAdded;
}
