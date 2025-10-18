package model;
import chess.ChessGame;

public record GameData (int gameID, String whiteUsername, String blackUsername, String gameTitle, ChessGame game) {
}
