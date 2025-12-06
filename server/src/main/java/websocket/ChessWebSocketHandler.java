package websocket;

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

import javax.xml.crypto.Data;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChessWebSocketHandler {

    private final Gson gson = new Gson();
    private final DataAccess dataAccess;

    private static final Map<Integer, Set<WsMessageContext>> gameConnections = new ConcurrentHashMap<>();

    public ChessWebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void register(Javalin app) {
        app.ws("/ws", ws -> {
            ws.onMessage(this::onMessage);
            ws.onClose(this::onClose);
            ws.onConnect(ctx -> {});
        });
    }

    private void onClose(WsCloseContext wsCloseContext) {
        gameConnections.values().forEach(set -> set.remove(wsCloseContext));
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

    private void handleConnect(WsMessageContext ctx, UserGameCommand cmd) throws DataAccessException {
        AuthData auth = reqAuth(cmd.getAuthToken());
        GameData gameData = reqGame(cmd.getGameID());
        int gameID = gameData.gameID();

        gameConnections
                .computeIfAbsent(gameID, id-> ConcurrentHashMap.newKeySet())
                .add(ctx);

        ctx.send(gson.toJson(new LoadGameMessage(gameData.game())));
        String role = determineRole(auth.username(), gameData);
        broadcastToOthers(gameID, ctx, new NotificationMessage(auth.username()) + " joined as " + role);
    }


    private AuthData reqAuth(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Invalid Auth Token");
        return auth;
    }

    private GameData reqGame(int gameID) throws DataAccessException {
        GameData game = dataAccess.getGame(gameID);
        if (game == null) throw new DataAccessException("Game not Found");
        return game;
    }


    private void sendError(WsMessageContext ctx, String errorText) {
        if (!errorText.toLowerCase().contains("error")) {
            errorText = "Error: " + errorText;
        }
        ctx.send(gson.toJson(new ErrorMessage(errorText)));
    }
}
