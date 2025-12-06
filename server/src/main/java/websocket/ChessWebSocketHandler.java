package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.Javalin;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.*;
import websocket.messages.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChessWebSocketHandler {

    private final Gson gson = new Gson();
    private final DataAccess dataAccess;

    private static final Map<Integer, Map<String, WsMessageContext>> GAME_CONNECTIONS = new ConcurrentHashMap<>();

    public ChessWebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void register(Javalin app) {
        app.ws("/ws", ws -> {
            ws.onMessage(this::onMessage);
            ws.onClose(this::onClose);
        });
    }

    private void onClose(WsCloseContext ctx) {
        GAME_CONNECTIONS.values().forEach(map -> map.remove(ctx.sessionId()));
    }

    private void onMessage(WsMessageContext ctx) {
        String json = ctx.message();
        UserGameCommand base = gson.fromJson(json, UserGameCommand.class);
        if (base == null || base.getCommandType() == null) {
            sendError(ctx, "Error: Invalid Command Received");
            return;
        }
        try {
            switch (base.getCommandType()) {
                case CONNECT -> handleConnect(ctx, base);
                case MAKE_MOVE -> {
                    MakeMoveCommand mm = gson.fromJson(json, MakeMoveCommand.class);
                    handleMakeMove(ctx, mm);
                }
                case LEAVE -> handleLeave(ctx, base);
                case RESIGN -> handleResign(ctx, base);
            }
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleMakeMove(WsMessageContext ctx, MakeMoveCommand cmd) throws DataAccessException {
        AuthData auth = reqAuth(cmd.getAuthToken());
        GameData gameData = reqGame(cmd.getGameID());
        ChessGame game = gameData.game();
        ChessMove move = cmd.getMove();

        if (game.isGameOver()) {
            sendError(ctx, "Game is already over.");
            return;
        }

        String role = determineRole(auth.username(), gameData);
        if (role.equals("observer")) {
            sendError(ctx, "Observers cannot make moves.");
            return;
        }

        ChessGame.TeamColor currentTurn = game.getTeamTurn();
        if ((currentTurn == ChessGame.TeamColor.WHITE && !role.equals("white")) ||
                (currentTurn == ChessGame.TeamColor.BLACK && !role.equals("black"))) {
            sendError(ctx, "Not your turn.");
            return;
        }


        try {
            game.makeMove(move);
        } catch (Exception e) {
            sendError(ctx, "Error: Illegal Move");
            return;
        }

        GameData updated = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game);

        dataAccess.updateGame(updated);
        broadcastToAll(cmd.getGameID(), new LoadGameMessage(updated));
        broadcastToOthers(cmd.getGameID(), ctx, new NotificationMessage(auth.username() + " moved " + move));
    }

    private void handleConnect(WsMessageContext ctx, UserGameCommand cmd) throws DataAccessException {
        AuthData auth = reqAuth(cmd.getAuthToken());
        GameData gameData = reqGame(cmd.getGameID());
        int gameID = gameData.gameID();

        GAME_CONNECTIONS.computeIfAbsent(gameID, id -> new ConcurrentHashMap<>())
                .put(ctx.sessionId(), ctx);

        ctx.send(gson.toJson(new LoadGameMessage(gameData)));

        String role = determineRole(auth.username(), gameData);
        NotificationMessage notif = new NotificationMessage(auth.username() + " joined as " + role);
        broadcastToOthers(gameID, ctx, notif);
    }

    private void handleLeave(WsMessageContext ctx, UserGameCommand cmd) throws DataAccessException {
        AuthData auth = reqAuth(cmd.getAuthToken());
        GameData gameData = reqGame(cmd.getGameID());
        String username = auth.username();
        int gameID = cmd.getGameID();

        Map<String, WsMessageContext> group = GAME_CONNECTIONS.get(gameID);
        if (group == null) {
            System.out.println("No Group");
            return;
        }
        boolean removed = false;

        for (var entry : group.entrySet()) {
            WsMessageContext existingCtx = entry.getValue();
            if (existingCtx.sessionId().equals(ctx.sessionId())) {
                group.remove(entry.getKey()); // remove by the actual sessionId key
                removed = true;
                break;
            }
        }
        String role = determineRole(username, gameData);

        if (!removed) {
            return;
        }


        System.out.println(role + " Pre-Broadcast");
        broadcastToOthers(gameID, ctx, new NotificationMessage(username + "left the game."));
        System.out.println(role + " Post-Broadcast");

        if (auth.username().equals(gameData.whiteUsername())) {
            dataAccess.updateGame(new GameData(
                    gameID,
                    null,
                    gameData.blackUsername(),
                    gameData.gameName(),
                    gameData.game()
            ));
        } else if (auth.username().equals(gameData.blackUsername())) {
            dataAccess.updateGame(new GameData(
                    gameID,
                    gameData.whiteUsername(),
                    null,
                    gameData.gameName(),
                    gameData.game()
            ));
        }
    }

    private void handleResign(WsMessageContext ctx, UserGameCommand cmd) throws DataAccessException {
        AuthData auth = reqAuth(cmd.getAuthToken());
        GameData gameData = reqGame(cmd.getGameID());
        ChessGame game = gameData.game();
        int gameID = gameData.gameID();

        if (game.isGameOver()) {
            sendError(ctx, "Game Already Concluded");
            return;
        }

        String role = determineRole(auth.username(), gameData);
        if (role.equals("observer")) {
            sendError(ctx, "Only Players may Resign");
            return;
        }

        game.setGameOver(true);
        GameData updated = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );
        dataAccess.updateGame(updated);

        broadcastToAll(gameID, new NotificationMessage(auth.username() + " resigned. Game Over."));
    }

    private AuthData reqAuth(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Invalid Auth Token");
        }
        return auth;
    }

    private GameData reqGame(int gameID) throws DataAccessException {
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not Found");
        }
        return game;
    }

    private String determineRole(String username, GameData gameData) {
        if (username == null) { return null; }
        if (username.equals(gameData.whiteUsername())) {
            return "white";
        }
        if (username.equals(gameData.blackUsername())) {
            return "black";
        }
        return "observer";
    }

    private void broadcastToOthers(int gameID, WsMessageContext sender, ServerMessage message) {
        var clients = GAME_CONNECTIONS.get(gameID);
        if (clients == null) {
            return;
        }

        String json = gson.toJson(message);

        clients.values().stream()
                .filter(c -> !c.sessionId().equals(sender.sessionId()))
                .forEach(c -> c.send(json));
    }

    private void broadcastToAll(int gameID, ServerMessage message) {
        var clients = GAME_CONNECTIONS.get(gameID);
        if (clients == null) {
            return;
        }
        String json = gson.toJson(message);
        clients.values().forEach(c -> c.send(json));
    }

    private void sendError(WsMessageContext ctx, String errorText) {
        if (!errorText.toLowerCase().contains("error")) {
            errorText = "Error: " + errorText;
        }
        ctx.send(gson.toJson(new ErrorMessage(errorText)));
    }
}
