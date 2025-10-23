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
    private final ClearService clearService;
    private final RegisterService registerService;
    private final LoginService loginService;
    private final LogoutService logoutService;
    private final ListGamesService listGamesService;
    private final CreateGameService createGameService;
    private final JoinGameService joinGameService;
    private final Gson gson = new Gson();

    public Server() {
        DataAccess dataAccess = new MemoryDataAccess();
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
        String gameName = body != null ? body.get("gameName") : null;

        CreateGameRequest request = new CreateGameRequest(authToken, gameName);
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
        Object gameIDObj = body.get("gameID");
        // Handle gameID being null or wrong type
        int gameID;
        if (gameIDObj == null) {
            throw new DataAccessException("Error: bad request");
        } else if (gameIDObj instanceof Double) {
            gameID = ((Double) gameIDObj).intValue();
        } else if (gameIDObj instanceof Integer) {
            gameID = (Integer) gameIDObj;
        } else {
            throw new DataAccessException("Error: bad request");
        }        JoinGameRequest request = new JoinGameRequest(authToken, playerColor, gameID);
        joinGameService.joinGame(request);
        ctx.status(200);
        ctx.json("{}");
    }

    private void handleDataAccessException(DataAccessException e, Context ctx) {
        if (e.getMessage().toLowerCase().contains("bad request")) {
            ctx.status(400);
        } else if (e.getMessage().toLowerCase().contains("forbidden")) {
            ctx.status(403);
        } else if (e.getMessage().toLowerCase().contains("unauthorized")) {
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
