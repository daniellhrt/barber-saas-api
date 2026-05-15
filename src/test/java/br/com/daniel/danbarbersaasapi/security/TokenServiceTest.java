package br.com.daniel.danbarbersaasapi.security;

import br.com.daniel.danbarbersaasapi.domain.user.User;
import br.com.daniel.danbarbersaasapi.domain.user.UserRole;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private static final String SECRET = "test-secret-key-test-secret-key-test-secret-key";

    private final TokenService tokenService = new TokenService();

    @Test
    @DisplayName("Deve gerar token com subject, issuer e claim de role corretos")
    void generateTokenShouldIncludeExpectedClaims() {
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
        User user = new User(UUID.randomUUID(), "admin@barber.com", "hashed-password", UserRole.ADMIN, null);

        String token = tokenService.generateToken(user);

        var decoded = JWT.decode(token);
        assertThat(decoded.getIssuer()).isEqualTo("barber-saas-api");
        assertThat(decoded.getSubject()).isEqualTo("admin@barber.com");
        assertThat(decoded.getClaim("role").asString()).isEqualTo("ADMIN");
        assertThat(decoded.getExpiresAtAsInstant()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Deve validar token assinado corretamente e retornar o e-mail do usuário")
    void validateTokenShouldReturnSubjectForValidToken() {
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
        User user = new User(UUID.randomUUID(), "barber@barber.com", "hashed-password", UserRole.BARBER, null);
        String token = tokenService.generateToken(user);

        String subject = tokenService.validateToken(token);

        assertThat(subject).isEqualTo("barber@barber.com");
    }

    @Test
    @DisplayName("Deve rejeitar token inválido ou adulterado")
    void validateTokenShouldRejectInvalidToken() {
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
        String invalidToken = JWT.create()
                .withIssuer("barber-saas-api")
                .withSubject("tampered@barber.com")
                .sign(Algorithm.HMAC256("another-secret-key-another-secret-key-another-secret"));

        assertThatThrownBy(() -> tokenService.validateToken(invalidToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token JWT inválido ou expirado");
    }
}

