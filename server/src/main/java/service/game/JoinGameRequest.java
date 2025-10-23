package service.game;

public record JoinGameRequest(String authToken, String playerColor, Integer gameID) {
}
