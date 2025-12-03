package server.websocket;

import io.javalin.websocket.WsContext;
import com.google.gson.Gson;
import dataaccess.DataAccess;   // your DAO interface
import model.GameData;
import websocket.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {

    private static final Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();
    private static final Map<WsContext, String> users = new ConcurrentHashMap<>();
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
        for (var set : gameConnections.values()) {
            set.remove(ctx);
        }
        users.remove(ctx);
    }

    public static void receive(WsContext ctx) {
        var json = ctx.message();
        var cmd = gson.fromJson(json, UserGameCommand.class);

        switch (cmd.commandType) {
            case CONNECT -> handleConnect(ctx, cmd);
            case MAKE_MOVE -> handleMove(ctx, cmd);
            case LEAVE -> handleLeave(ctx, cmd);
            case RESIGN -> handleResign(ctx, cmd);
        }
    }

    private static void handleConnect(WsContext ctx, UserGameCommand cmd) {
        // TODO: implement
    }

    private static void handleMove(WsContext ctx, UserGameCommand cmd) {
        // TODO: implement
    }

    private static void handleLeave(WsContext ctx, UserGameCommand cmd) {
        // TODO: implement
    }

    private static void handleResign(WsContext ctx, UserGameCommand cmd) {
        // TODO: implement
    }
}
