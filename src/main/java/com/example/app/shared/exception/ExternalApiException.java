package com.example.app.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ExternalApiException extends RuntimeException {
    public ExternalApiException(HttpStatusCode statusCode, String statusText) {
        super(String.format("SWAPI error [%s]: %s", statusCode, statusText));
    }
}
