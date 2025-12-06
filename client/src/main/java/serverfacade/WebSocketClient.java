package serverfacade;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocketClient implements WebSocket.Listener {
  private WebSocket webSocket;
  private final Gson gson = new Gson();
  private final WebSocketMessageHandler messageHandler;
  private final String serverUrl;

  public interface WebSocketMessageHandler {
    void onMessage(ServerMessage message);

    void onError(String errorMessage);

    void onClose();
  }

  public WebSocketClient(String serverUrl, WebSocketMessageHandler messageHandler) {
    this.serverUrl = serverUrl;
    this.messageHandler = messageHandler;
  }

  public void connect(String authToken, int gameID) throws Exception {
    String wsUrl = serverUrl.replace("http", "ws") + "/ws";

    HttpClient httpClient = HttpClient.newHttpClient();
    this.webSocket = httpClient.newWebSocketBuilder()
        .buildAsync(URI.create(wsUrl), this)
        .join();
  }

  public void sendCommand(UserGameCommand command) {
    if (webSocket != null) {
      String json = gson.toJson(command);
      webSocket.sendText(json, true);
    }
  }

  public void disconnect() {
    if (webSocket != null) {
      webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client Disconnected");
    }
  }

  @Override
  public void onOpen(WebSocket webSocket) {
    System.out.println("WebSocket connected");
  }

  @Override
  public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
    String str = data.toString();
    try {
      ServerMessage base = gson.fromJson(str, ServerMessage.class);
      switch (base.getServerMessageType()) {
        case LOAD_GAME -> {
          LoadGameMessage load = gson.fromJson(str, LoadGameMessage.class);
          messageHandler.onMessage(load);
        }
        case NOTIFICATION -> {
          NotificationMessage notif = gson.fromJson(str, NotificationMessage.class);
          messageHandler.onMessage(notif);
        }
        case ERROR -> {
          ErrorMessage err = gson.fromJson(str, ErrorMessage.class);
          messageHandler.onError(err.getErrorMessage());
        }
      }
    } catch (Exception e) {
      messageHandler.onError("Error: Failed to parse message -> " + e.getMessage());
    }
    return null;
  }

  @Override
  public void onError(WebSocket webSocket, Throwable error) {
    messageHandler.onError("WebSocket error: " + error.getMessage());
  }

  @Override
  public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
    System.out.println("WebSocket closed: " + reason);
    messageHandler.onClose();
    return CompletableFuture.completedFuture(null);
  }
}