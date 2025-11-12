package serverfacade;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import model.RegisterResult;
import model.RegisterRequest;

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

    private <T> T makeRequest(String method, String endpoint, Object reqBody, Class<T> responseType, String authToken)
            throws ServerException {
        try {
            String url = serverUrl + endpoint;
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json");

            if (authToken != null && !authToken.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + authToken);
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

    public <T> T get(String endpoint, Class<T> responseType, String authToken) throws ServerException {
        return makeRequest("GET", endpoint, null, responseType, authToken);
    }

    public <T> T post(String endpoint, Object body, Class<T> responseType, String authToken) throws ServerException {
        return makeRequest("POST", endpoint, body, responseType, authToken);
    }

    public <T> T put(String endpoint, Object body, Class<T> responseType, String authToken) throws ServerException {
        return makeRequest("PUT", endpoint, body, responseType, authToken);
    }

    public <T> T delete(String endpoint, Class<T> responseType, String authToken) throws ServerException {
        return makeRequest("DELETE", endpoint, null, responseType, authToken);
    }

    public RegisterResult register(RegisterRequest request) throws ServerException {
        return post("/user", request, RegisterResult.class, null);
    }
}
