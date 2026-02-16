
import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.service.PasswordHasher;
import cncs.academy.ess.service.TodoUserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TodoUserServiceTest {

    private UserRepository repository;
    private TodoUserService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        service = new TodoUserService(repository);
    }

    @Test
    void login_shouldReturnValidJWTTokenWhenCredentialsMatch() throws NoSuchAlgorithmException {
        // Arrange
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getId()).thenReturn(123);
        when(user.getPasswordHash()).thenReturn("fakeHash");
        when(user.getPasswordSalt()).thenReturn("fakeSalt");

        when(repository.findByUsername("testuser")).thenReturn(user);

        // Mock static PBKDF2 verification
        try (MockedStatic<PasswordHasher> mocked = mockStatic(PasswordHasher.class)) {
            mocked.when(() -> PasswordHasher.verifyPBKDF2(
                    eq("password"),
                    eq("fakeHash"),
                    eq("fakeSalt")
            )).thenReturn(true);

            // Act
            String token = service.login("testuser", "password");

            // Assert (1) começa por "Bearer "
            assertNotNull(token);
            assertTrue(token.startsWith("Bearer "), "Token deve começar por 'Bearer '");

            // Assert (2) JWT válido + claims corretas
            String jwt = token.substring("Bearer ".length()).trim();
            String[] parts = jwt.split("\\.");
            assertEquals(3, parts.length, "JWT deve ter 3 partes: header.payload.signature");

            // Verificar assinatura + claims com a mesma config do JwtService
            Algorithm alg = Algorithm.HMAC256("um_segredo_longo_e_aleatorio_do_grupo_um");
            DecodedJWT decoded = JWT.require(alg)
                    .withIssuer("cncs.academy.ess")
                    .build()
                    .verify(jwt);

            assertEquals("cncs.academy.ess", decoded.getIssuer());
            assertEquals("testuser", decoded.getSubject());
            assertEquals(123, decoded.getClaim("userId").asInt());

            Date iat = decoded.getIssuedAt();
            Date exp = decoded.getExpiresAt();
            assertNotNull(iat, "JWT deve conter iat");
            assertNotNull(exp, "JWT deve conter exp");
            assertTrue(exp.after(iat), "exp deve ser posterior a iat");
        }

        verify(repository, times(1)).findByUsername("testuser");
    }
}
