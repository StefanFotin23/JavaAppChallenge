package com.example.app.starwars.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.app.shared.exception.ExternalApiException;
import com.example.app.starwars.dto.SwapiPageResponse;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class SwapiClientTest {

  @Mock private RestClient.Builder builder;

  @Mock private RestClient restClient;

  @InjectMocks private SwapiClient swapiClient;

  @BeforeEach
  void setUp() {
    when(builder.baseUrl(anyString())).thenReturn(builder);
    when(builder.build()).thenReturn(restClient);
    ReflectionTestUtils.setField(swapiClient, "baseUrl", "https://swapi.dev/api");
    swapiClient.init();
  }

  @Test
  @SuppressWarnings("unchecked")
  void testFetchPeopleSuccess() {
    SwapiPageResponse mockResponse =
        new SwapiPageResponse(82, "nextUrl", null, Collections.emptyList());
    RestClient.RequestHeadersUriSpec requestHeadersUriSpec =
        mock(RestClient.RequestHeadersUriSpec.class);
    RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object[].class)))
        .thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.body(SwapiPageResponse.class)).thenReturn(mockResponse);

    SwapiPageResponse result = swapiClient.fetchPeople(1);
    assertNotNull(result);
    assertEquals(82, result.count());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testFetchPeopleByIdError() {
    RestClient.RequestHeadersUriSpec requestHeadersUriSpec =
        mock(RestClient.RequestHeadersUriSpec.class);
    RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object[].class)))
        .thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    when(responseSpec.onStatus(any(), any()))
        .thenThrow(new ExternalApiException(HttpStatus.INTERNAL_SERVER_ERROR, "SWAPI is down"));

    assertThrows(ExternalApiException.class, () -> swapiClient.fetchPeopleById("999"));
  }
}
