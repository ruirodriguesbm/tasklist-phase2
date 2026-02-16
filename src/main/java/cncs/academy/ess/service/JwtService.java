package cncs.academy.ess.service;

import cncs.academy.ess.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.time.Instant;
import java.util.Date;

public class JwtService {

    private static final String JWT_SECRET = "um_segredo_longo_e_aleatorio_do_grupo_um";
    private static final String JWT_ISSUER = "cncs.academy.ess";
    private static final long JWT_TTL_SECONDS = 60 * 60; // 1h
    private static final Algorithm alg = Algorithm.HMAC256(JWT_SECRET);

    private DecodedJWT verify(String token) {
        return JWT.require(alg)
                .withIssuer(JWT_ISSUER)
                .build()
                .verify(token);
    }

    public String getUsernameFromToken(String token) {
        try {
            return verify(token).getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public Integer getUserIdFromToken(String token) {
        try {
            return verify(token).getClaim("userId").asInt();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String createAuthToken(User user) {
        Instant now = Instant.now();

        String token = JWT.create()
                .withIssuer(JWT_ISSUER)
                .withSubject(user.getUsername())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(JWT_TTL_SECONDS)))
                .withClaim("userId", user.getId())
                .sign(alg);

        return "Bearer " + token;
    }
}
