package com.example.app.starwars.controller;

import com.example.app.auth.security.SecurityUtils;
import com.example.app.starwars.dto.CharacterDTO;
import com.example.app.starwars.dto.CharacterResponseDTO;
import com.example.app.starwars.service.CharacterService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class CharacterController {
  private final SecurityUtils securityUtils;
  private final CharacterService characterService;

  @GetMapping("/people")
  public ResponseEntity<List<CharacterDTO>> getCharacters(
      @RequestParam(defaultValue = "1")
          @Min(value = 1, message = "Page number must be greater than or equal to 1")
          int page) {
    List<CharacterDTO> characters = characterService.getPeople(page);

    if (characters == null || characters.isEmpty()) {
      log.warn("No data found for page {}", page);
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(characters);
  }

  @GetMapping("/people/{id}")
  public ResponseEntity<CharacterResponseDTO> getCharacterById(
      @PathVariable
          @Pattern(regexp = "^[1-9]\\d*$", message = "ID must be a positive numeric value")
          String id) {
    CharacterResponseDTO character = characterService.getCharacterById(id);
    return ResponseEntity.ok(character);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/favourites")
  public ResponseEntity<List<CharacterResponseDTO>> getFavourites(Principal principal)
      throws AccessDeniedException {
    securityUtils.checkAuthorization(principal);
    String username = principal.getName();
    List<CharacterResponseDTO> characters = characterService.getFavouriteCharacters(username);
    return ResponseEntity.ok(characters);
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/favourites")
  public ResponseEntity<Boolean> addFavourite(
      @RequestBody CharacterResponseDTO characterDTO, Principal principal)
      throws AccessDeniedException {
    securityUtils.checkAuthorization(principal);
    String username = principal.getName();
    boolean success = characterService.addFavouriteCharacter(username, characterDTO);
    if (success) {
      return ResponseEntity.ok(true);
    }
    return ResponseEntity.internalServerError().build();
  }
}
