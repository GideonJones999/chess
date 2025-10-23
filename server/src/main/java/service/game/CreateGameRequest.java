package service.game;

public record CreateGameRequest(String authToken, String gameName) {
}
