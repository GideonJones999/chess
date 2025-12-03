package serverfacade;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

public class WebSocketClient extends WebSocket.Listener {
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
    String wsUrl = serverUrl.replace("http", "ws") + "/ws?token=" + authToken + "&gameID=" + gameID;

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
      webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "");
    }
  }

  @Override
  public void onOpen(WebSocket webSocket) {
    System.out.println("WebSocket connected");
  }

  @Override
  public CompletionStage<?> onText(WebSocket webSocket, String data, boolean last) {
    try {
      ServerMessage message = gson.fromJson(data, ServerMessage.class);
      messageHandler.onMessage(message);
    } catch (Exception e) {
      messageHandler.onError("Failed to parse message: " + e.getMessage());
    }
    return null;
  }

  @Override
  public void onError(WebSocket webSocket, Throwable error) {
    messageHandler.onError("WebSocket error: " + error.getMessage());
  }

  @Override
  public void onClose(WebSocket webSocket, int statusCode, String reason) {
    System.out.println("WebSocket closed: " + reason);
    messageHandler.onClose();
  }
}