package com.example.app.auth.security;

import com.example.app.auth.service.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements ClientHttpRequestInterceptor {
    private final AuthServiceImpl authService;
    private final AuthContext authContext;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (authContext.getAccessToken() != null) {
            request.getHeaders().setBearerAuth(authContext.getAccessToken());
        }

        ClientHttpResponse response = execution.execute(request, body);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            response.close();
            try {
                String newAccessToken = authService.refresh(authContext.getRefreshToken()).accessToken();
                authContext.setAccessToken(newAccessToken);
                request.getHeaders().setBearerAuth(newAccessToken);
                return execution.execute(request, body);
            } catch (Exception e) {
                return response;
            }
        }
        return response;
    }

    @Override
    public ClientHttpRequestInterceptor andThen(ClientHttpRequestInterceptor interceptor) {
        return ClientHttpRequestInterceptor.super.andThen(interceptor);
    }

    @Override
    public ClientHttpRequestExecution apply(ClientHttpRequestExecution execution) {
        return ClientHttpRequestInterceptor.super.apply(execution);
    }
}
