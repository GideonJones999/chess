package websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import websocket.commands.*;
import websocket.messages.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChessWebSocketHandler {

    private final Gson gson = new Gson();
    private final DataAccess dataAccess;

    private static final Map<Integer, Set<WsContext>> gameConnections = new ConcurrentHashMap<>();

    public ChessWebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public static void register(Javalin app) {
        app.ws("/ws", ws -> {
            ws.onMessage(this::onMessage);
            ws.onClose(this::onClose);
            ws.onConnect(ctx -> {});
        });
    }


}
