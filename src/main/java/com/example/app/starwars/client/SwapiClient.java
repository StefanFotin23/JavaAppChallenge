package com.example.app.starwars.client;

import com.example.app.shared.exception.ExternalApiException;
import com.example.app.starwars.dto.CharacterDTO;
import com.example.app.starwars.dto.SwapiPageResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class SwapiClient {
  @Value("${swapi.base-url}")
  private String baseUrl;

  private final RestClient.Builder builder;
  private RestClient swapiRestClient;

  @PostConstruct
  public void init() {
    this.swapiRestClient = builder.baseUrl(baseUrl).build();
  }

  public SwapiPageResponse fetchPeople(int page) {
    return swapiRestClient
        .get()
        .uri("/people?page={page}", page)
        .retrieve()
        .onStatus(
            HttpStatusCode::isError,
            (request, response) -> {
              throw new ExternalApiException(response.getStatusCode(), response.getStatusText());
            })
        .body(SwapiPageResponse.class);
  }

  public CharacterDTO fetchPeopleById(String id) {
    return swapiRestClient
        .get()
        .uri("/people/{id}", id)
        .retrieve()
        .onStatus(
            HttpStatusCode::isError,
            (request, response) -> {
              throw new ExternalApiException(response.getStatusCode(), response.getStatusText());
            })
        .body(CharacterDTO.class);
  }
}
