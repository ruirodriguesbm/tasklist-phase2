package cncs.academy.ess.controller;

import cncs.academy.ess.service.JwtService;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpStatus;
import org.casbin.jcasbin.main.Enforcer;

public class AuthorizationMiddleware {

    private final Enforcer enforcer;
    private final JwtService jwtService;

    public AuthorizationMiddleware(Enforcer enforcer, JwtService jwtService) {
        this.enforcer = enforcer;
        this.jwtService = jwtService;
    }

    public void handle(Context ctx) {

        // deixar passar preflight CORS
        if ("OPTIONS".equalsIgnoreCase(ctx.method().toString())) return;

        // rotas públicas
        String path = ctx.path();
        if (path.equals("/login")) return;

        String auth = ctx.header("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            ctx.status(HttpStatus.UNAUTHORIZED).result("Missing Authorization header");
            return;
        }

        String token = auth.substring("Bearer ".length()).trim();
        String username = jwtService.getUsernameFromToken(token);
        Integer userId = jwtService.getUserIdFromToken(token);

        if (username == null) {
            ctx.status(HttpStatus.UNAUTHORIZED).result("Invalid token");
            return;
        }

        boolean allowed = enforcer.enforce(
                username,
                path,
                ctx.method().toString()
        );



        if (!allowed) {
            throw new ForbiddenResponse("Forbidden");
        }

        ctx.attribute("user", username);
        ctx.attribute("userId", userId);
    }
}
