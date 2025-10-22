package service;

public record CreateGameRequest(String authToken, String gameTitle) {
}
