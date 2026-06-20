package com.gabriel.fintransfer.auth;

import com.gabriel.fintransfer.auth.dto.LoginRequest;
import com.gabriel.fintransfer.auth.dto.LoginResponse;
import com.gabriel.fintransfer.user.domain.UserType;
import com.gabriel.fintransfer.user.dto.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
class AuthControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldRegisterAndReturnToken() {
        CreateUserRequest request = new CreateUserRequest(
                "Test User", "11122233344", "test@email.com", "password123", UserType.COMMON
        );

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, LoginResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
        assertThat(response.getBody().user().name()).isEqualTo("Test User");
        assertThat(response.getBody().user().email()).isEqualTo("test@email.com");
    }

    @Test
    void shouldLoginWithValidCredentials() {
        CreateUserRequest register = new CreateUserRequest(
                "Login User", "99988877766", "login@email.com", "password123", UserType.COMMON
        );
        restTemplate.postForEntity("/api/v1/auth/register", register, LoginResponse.class);

        LoginRequest login = new LoginRequest("login@email.com", "password123");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login", login, LoginResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
        assertThat(response.getBody().user().email()).isEqualTo("login@email.com");
    }

    @Test
    void shouldRejectInvalidLogin() {
        LoginRequest login = new LoginRequest("nobody@email.com", "wrongpass");
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/login", login, String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRequireAuthForProtectedEndpoints() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/transactions/transfer", "{}", String.class
        );

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }
}
