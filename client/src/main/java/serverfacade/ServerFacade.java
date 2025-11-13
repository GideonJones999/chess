package serverfacade;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import model.*;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    static final HttpClient httpClient = HttpClient.newHttpClient();
    static final Gson gson = new Gson();

    private <R> R makeRequest(String method, String endpoint, Object reqBody, Class<R> responseType, String authToken)
            throws ServerException {
        try {
            String url = serverUrl + endpoint;
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json");

            if (authToken != null && !authToken.isBlank()) {
                requestBuilder.header("Authorization", authToken);
            }

            if (reqBody != null) {
                String jsonBody = gson.toJson(reqBody);
                requestBuilder.header("Content-Type", "application/json").method(method,
                        HttpRequest.BodyPublishers.ofString(jsonBody));
            } else {
                if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
                    requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
                } else {
                    requestBuilder.header("Content-Type", "application/json").method(method,
                            HttpRequest.BodyPublishers.noBody());
                }
            }
            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body() == null ? "" : response.body().trim();

            if (statusCode >= 200 && statusCode < 300) {
                if (responseType == null || responseType == Void.class || responseBody.isEmpty()) {
                    return null;
                }
                try {
                    return gson.fromJson(responseBody, responseType);
                } catch (JsonSyntaxException e) {
                    if (responseType == String.class) {
                        return responseType.cast(responseBody);
                    }
                    throw new ServerException("Failed to parse server response: " + e.getMessage());
                }
            } else {
                String msg = responseBody.isEmpty() ? ("HTTP " + statusCode) : responseBody;
                throw new ServerException(statusCode, msg);
            }
        } catch (IOException | InterruptedException e) {
            throw new ServerException("Request failed: " + e.getMessage());
        }
    }

    public <R> R get(String endpoint, Class<R> responseType, String authToken) throws ServerException {
        return makeRequest("GET", endpoint, null, responseType, authToken);
    }

    public <Q, R> R post(String endpoint, Q body, Class<R> responseType, String authToken) throws ServerException {
        return makeRequest("POST", endpoint, body, responseType, authToken);
    }

    public <Q, R> R put(String endpoint, Q body, Class<R> responseType, String authToken) throws ServerException {
        return makeRequest("PUT", endpoint, body, responseType, authToken);
    }

    public <R> R delete(String endpoint, Class<R> responseType, String authToken) throws ServerException {
        return makeRequest("DELETE", endpoint, null, responseType, authToken);
    }

    public RegisterResult register(RegisterRequest request) throws ServerException {
        return post("/user", request, RegisterResult.class, null);
    }

    public LoginResult login(LoginRequest request) throws ServerException {
        return post("/session", request, LoginResult.class, null);
    }

    public void logout(String authToken) throws ServerException {
        delete("/session", Void.class, authToken);
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws ServerException {
        return post("/game", request, CreateGameResult.class, authToken);
    }

//    Convenience Overload
    public CreateGameResult createGame(String gameName, String authToken) throws ServerException {
        return createGame(new CreateGameRequest(gameName), authToken);
    }

    public ListGamesResult listGames(String authToken) throws ServerException {
        return get("/game", ListGamesResult.class, authToken);
    }

    public void joinGame(JoinGameRequest request, String authToken) throws ServerException {
        put("/game", request, Void.class, authToken);
    }

    
}
