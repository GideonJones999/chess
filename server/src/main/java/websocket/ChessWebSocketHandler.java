package websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import io.javalin.Javalin;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.*;
import websocket.messages.*;

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
            sendError(ctx, "Error: " + ex.getMessage());
        }
    }

    private void onClose(WsMessageContext ctx) {
        gameConnections.values().forEach(set -> set.remove(ctx));
    }


    private void sendError(WsMessageContext ctx, String errorText) {
        if (!errorText.toLowerCase().contains("error")) {
            errorText = "Error: " + errorText;
        }
        ctx.send(gson.toJson(new ErrorMessage(errorText)));
    }
}
