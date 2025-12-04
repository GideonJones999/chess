package server.websocket;

import io.javalin.websocket.WsContext;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {

    private static final Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();
    private static final Map<WsContext, Integer> userGameMap = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();
    private static DataAccess dao;

    public static void initialize(DataAccess dataAccess) {
        dao = dataAccess;
    }

    public static void connect(WsContext ctx) {
        // No gameID yet — wait for CONNECT command
    }

    public static void disconnect(WsContext ctx) {
        // When disconnected, remove from game map
        Integer gameID = userGameMap.remove(ctx);
        if (gameID != null) {
            Set<WsContext> connections = gameConnections.get(gameID);
            if (connections != null) {
                connections.remove(ctx);
            }
        }
    }

    public static void recieve(WsContext ctx) {
        try {
            var json = ctx.message();
            UserGameCommand cmd = gson.fromJson(json, UserGameCommand.class);

            switch (cmd.getCommandType()) {
                case CONNECT -> handleConnect(ctx, cmd);
                case MAKE_MOVE -> handleMove(ctx, cmd);
                case LEAVE -> handleLeave(ctx, cmd);
                case RESIGN -> handleResign(ctx, cmd);
            }
        } catch (Exception e) {
            sendError(ctx, "Error Recieving");
        }
    }

    private static void handleConnect(WsContext ctx, UserGameCommand cmd) {
        try {
            // Validate auth token
            AuthData auth = dao.getAuth(cmd.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: unauthorized");
                return;
            }

            // Get game
            GameData game = dao.getGame(cmd.getGameID());
            if (game == null) {
                sendError(ctx, "Error: game not found");
                return;
            }

            // Add to game connections
            Integer gameID = cmd.getGameID();
            gameConnections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(ctx);
            userGameMap.put(ctx, gameID);

            // Send LOAD_GAME to this client
            LoadGameMessage loadMsg = new LoadGameMessage(game);
            ctx.send(gson.toJson(loadMsg));

            // Notify others that user joined
            String username = auth.username();
            String notification = username + " joined the game.";
            broadcastToGame(gameID, new NotificationMessage(notification), ctx);

        } catch (DataAccessException e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private static void handleMove(WsContext ctx, UserGameCommand cmd) {
        try {
            // Validate auth token
            AuthData auth = dao.getAuth(cmd.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: unauthorized");
                return;
            }

            // Get game
            GameData game = dao.getGame(cmd.getGameID());
            if (game == null) {
                sendError(ctx, "Error: game not found");
                return;
            }

            // Check if game is over
            if (game.game().isGameOver()) {
                sendError(ctx, "Error: game is already over");
                return;
            }

            // Make the move
            chess.ChessGame chessGame = game.game();
            try {
                chessGame.makeMove(cmd.move);
                dao.updateGame(game);

                // Broadcast updated game to all clients
                LoadGameMessage loadMsg = new LoadGameMessage(game);
                broadcastToGame(cmd.getGameID(), loadMsg, null);

                // Notify about the move
                String username = auth.username();
                String notification = username + " made a move.";
                broadcastToGame(cmd.getGameID(), new NotificationMessage(notification), ctx);

            } catch (chess.InvalidMoveException e) {
                sendError(ctx, "Error: invalid move - " + e.getMessage());
            }

        } catch (DataAccessException e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private static void handleLeave(WsContext ctx, UserGameCommand cmd) {
        try {
            // Validate auth token
            AuthData auth = dao.getAuth(cmd.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: unauthorized");
                return;
            }

            Integer gameID = cmd.getGameID();
            Integer connectedGameID = userGameMap.get(ctx);
            if (connectedGameID == null || !connectedGameID.equals(gameID)) {
                sendError(ctx, "Error: not in this game");
                return;
            }

            // Remove from connections
            Set<WsContext> connections = gameConnections.get(gameID);
            if (connections != null) {
                connections.remove(ctx);
            }
            userGameMap.remove(ctx);

            // Notify others
            String username = auth.username();
            String notification = username + " left the game.";
            broadcastToGame(gameID, new NotificationMessage(notification), null);

            ctx.closeSession();

        } catch (DataAccessException e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private static void handleResign(WsContext ctx, UserGameCommand cmd) {
        try {
            // Validate auth token
            AuthData auth = dao.getAuth(cmd.getAuthToken());
            if (auth == null) {
                sendError(ctx, "Error: unauthorized");
                return;
            }

            // Get game
            GameData game = dao.getGame(cmd.getGameID());
            if (game == null) {
                sendError(ctx, "Error: game not found");
                return;
            }

            // Check if already over
            if (game.game().isGameOver()) {
                sendError(ctx, "Error: game is already over");
                return;
            }

            // Mark game as over
            chess.ChessGame chessGame = game.game();
            chessGame.setGameOver(true);
            dao.updateGame(game);

            // Notify all players
            String username = auth.username();
            String notification = username + " resigned. Game over.";
            broadcastToGame(cmd.getGameID(), new NotificationMessage(notification), null);

            // Send updated game state
            LoadGameMessage loadMsg = new LoadGameMessage(game);
            broadcastToGame(cmd.getGameID(), loadMsg, null);

        } catch (DataAccessException e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private static void broadcastToGame(Integer gameID, ServerMessage message, WsContext excludeCtx) {
        Set<WsContext> connections = gameConnections.get(gameID);
        if (connections != null) {
            String json = gson.toJson(message);
            for (WsContext client : connections) {
                if (excludeCtx == null || !client.equals(excludeCtx)) {
                    client.send(json);
                }
            }
        }
    }

    private static void sendError(WsContext ctx, String message) {
        ErrorMessage errorMsg = new ErrorMessage(message);
        ctx.send(gson.toJson(errorMsg));
    }
}
