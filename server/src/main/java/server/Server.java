package server;

import io.javalin.*;
import io.javalin.http.Context;
import com.google.gson.Gson;
import java.util.Map;

// Data Access
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;

// Services
import io.javalin.json.JavalinGson;
import service.*;

public class Server {
    private final Javalin javalin;
    private final DataAccess dataAccess;
    private final ClearService clearService;
    private final RegisterService registerService;
    private final LoginService loginService;
    private final LogoutService logoutService;
    private final ListGamesService listGamesService;
    private final CreateGameService createGameService;
    private final JoinGameService joinGameService;
    private final Gson gson = new Gson();

    public Server() {
        dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);
        registerService = new RegisterService(dataAccess);
        loginService = new LoginService(dataAccess);
        logoutService = new LogoutService(dataAccess);
        listGamesService = new ListGamesService(dataAccess);
        createGameService = new CreateGameService(dataAccess);
        joinGameService = new JoinGameService(dataAccess);

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
        });

        // Register Endpoints Here
        javalin.delete("/db", this::handleClear);
        javalin.post("/user", this::handleRegister);
        javalin.post("/session", this::handleLogin);
        javalin.delete("/session", this::handleLogout);
        javalin.post("/game", this::handleCreateGame);
        javalin.get("/game", this::handleListGames);
        javalin.put("/game", this::handleJoinGame);

        // Register Exception Handlers Here
        javalin.exception(DataAccessException.class, this::handleDataAccessException);
        javalin.exception(Exception.class, this::handleException);
    }

    private void handleClear(Context ctx) throws Exception {
        clearService.clear();
        ctx.status(200);
        ctx.json("{}");
    }

    private void handleRegister(Context ctx) throws Exception {
        RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
        RegisterResult result = registerService.register(request);
        ctx.status(200);
        ctx.json(result);
    }

    private void handleLogin(Context ctx) throws Exception {
        LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
        LoginResult result = loginService.login(request);
        ctx.status(200);
        ctx.json(result);
    }

    private void handleLogout(Context ctx) throws Exception {
        String authToken = ctx.header("authorization");
        LogoutRequest request = new LogoutRequest(authToken);
        logoutService.logout(request);
        ctx.status(200);
        ctx.json("{}");
    }

    private void handleCreateGame(Context ctx) throws Exception {
        String authToken = ctx.header("authorization");
        Map<String, String> body = gson.fromJson(ctx.body(), Map.class);
        String gameTitle = body.get("gameTitle");

        CreateGameRequest request = new CreateGameRequest(authToken, gameTitle);
        CreateGameResult result = createGameService.createGame(request);
        ctx.status(200);
        ctx.json(result);
    }

    private void handleListGames(Context ctx) throws Exception {
        String authToken = ctx.header("authorization");
        ListGamesRequest request = new ListGamesRequest(authToken);
        ListGamesResult result = listGamesService.listGames(request);
        ctx.status(200);
        ctx.json(result);
    }

    private void handleJoinGame(Context ctx) throws Exception {
        String authToken = ctx.header("authorization");
        Map<String, Object> body = gson.fromJson(ctx.body(), Map.class);
        String playerColor = (String) body.get("playerColor");
        int gameID = ((Double) body.get("gameID")).intValue();
        JoinGameRequest request = new JoinGameRequest(authToken, playerColor, gameID);
        joinGameService.joinGame(request);
        ctx.status(200);
        ctx.json("{}");
    }

    private void handleDataAccessException(DataAccessException e, Context ctx) {
        if (e.getMessage().contains("Bad Request")) {
            ctx.status(400);
        } else if (e.getMessage().contains("Already Taken")) {
            ctx.status(403);
        } else if (e.getMessage().contains("Unauthorized")) {
            ctx.status(401);
        } else {
            ctx.status(500);
        }
        ctx.json(Map.of("message", e.getMessage()));
    }

    private void handleException(Exception e, Context ctx) {
        ctx.status(500);
        ctx.json(Map.of("message", "Error: "+e.getMessage()));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
