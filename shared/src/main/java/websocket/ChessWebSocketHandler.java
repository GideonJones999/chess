package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import websocket.commands.*;
import websocket.messages.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChessWebSocketHandler {

    private static final Gson gson = new Gson();

    // Stores gameID -> Set of WebSocket connections
    private static final Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();


    public static void configure(io.javalin.Javalin app) {

        app.ws("/ws", ws -> {

            ws.onConnect(ctx -> {
                // Do nothing here yet — we wait for CONNECT command message
            });

            ws.onClose(ctx -> {
                // TODO: Handle unexpected disconnects if desired
            });

            ws.onMessage(ctx -> {
                String rawMessage = ctx.message();

                // First: decode base command
                UserGameCommand baseCommand = gson.fromJson(rawMessage, UserGameCommand.class);

                switch (baseCommand.getCommandType()) {

                    case CONNECT -> handleConnect(ctx, baseCommand);

                    case MAKE_MOVE -> {
                        MakeMoveCommand moveCommand = gson.fromJson(rawMessage, MakeMoveCommand.class);
                        handleMakeMove(ctx, moveCommand);
                    }

                    case LEAVE -> handleLeave(ctx, baseCommand);

                    case RESIGN -> handleResign(ctx, baseCommand);

                    default -> ctx.send(gson.toJson(new ErrorMessage("Error: Unknown command")));
                }
            });
        });
    }

    // -------------------- Handlers --------------------------

    private static void handleConnect(WsContext ctx, UserGameCommand cmd) {
        int gameID = cmd.getGameID();

        gameConnections.putIfAbsent(gameID, ConcurrentHashMap.newKeySet());
        gameConnections.get(gameID).add(ctx);

        // (EXAMPLE) Send LOAD_GAME to this client
        LoadGameMessage loadMessage = new LoadGameMessage(/* TODO: pass current game state */ null);
        ctx.send(gson.toJson(loadMessage));

        // Send NOTIFICATION to everyone else
        NotificationMessage notif = new NotificationMessage("A player joined game " + gameID);
        broadcastToOthers(gameID, ctx, notif);
    }


    private static void handleMakeMove(WsContext ctx, MakeMoveCommand cmd) {
        int gameID = cmd.getGameID();

        // TODO: Validate move using your ChessGame object
        // TODO: Update DB or in-memory model

        // Send updated board to ALL players
        LoadGameMessage boardUpdate = new LoadGameMessage(/* updated game */ null);
        broadcastToAll(gameID, boardUpdate);

        // Notify OTHER players
        NotificationMessage notif = new NotificationMessage("A move was made in game " + gameID);
        broadcastToOthers(gameID, ctx, notif);
    }


    private static void handleLeave(WsContext ctx, UserGameCommand cmd) {
        int gameID = cmd.getGameID();

        if (gameConnections.containsKey(gameID)) {
            gameConnections.get(gameID).remove(ctx);

            NotificationMessage notif =
                    new NotificationMessage("A player left game " + gameID);

            broadcastToOthers(gameID, ctx, notif);
        }
    }


    private static void handleResign(WsContext ctx, UserGameCommand cmd) {
        int gameID = cmd.getGameID();

        NotificationMessage notif =
                new NotificationMessage("A player resigned from game " + gameID);

        broadcastToAll(gameID, notif);

        // TODO: Mark game complete in DB so no more moves allowed
    }

    // -------------------- Broadcast Helpers --------------------------

    private static void broadcastToAll(int gameID, ServerMessage message) {
        Set<WsContext> clients = gameConnections.get(gameID);
        if (clients == null) return;

        String json = gson.toJson(message);
        clients.forEach(c -> c.send(json));
    }

    private static void broadcastToOthers(int gameID, WsContext sender, ServerMessage message) {
        Set<WsContext> clients = gameConnections.get(gameID);
        if (clients == null) return;

        String json = gson.toJson(message);
        clients.stream()
                .filter(c -> !c.equals(sender))
                .forEach(c -> c.send(json));
    }
}
