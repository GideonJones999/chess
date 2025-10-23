package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JoinGameServiceTest {

    private MemoryDataAccess dataAccess;
    private JoinGameService joinGameService;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        joinGameService = new JoinGameService(dataAccess);
    }

    @Test
    void joinGameSuccess() throws DataAccessException {
        // Create auth token
        AuthData auth = new AuthData("validToken", "player1");
        dataAccess.createAuth(auth);

        // Create a game
        GameData game = new GameData(1, null, null, "TestGame", new ChessGame());
        dataAccess.createGame(game);

        // Join as white
        JoinGameRequest request = new JoinGameRequest("validToken", "WHITE", 1);
        assertDoesNotThrow(() -> joinGameService.joinGame(request));

        // Verify player was added
        GameData updatedGame = dataAccess.getGame(1);
        assertEquals("player1", updatedGame.whiteUsername(), "White player should be set");
        assertNull(updatedGame.blackUsername(), "Black player should still be null");
    }

    @Test
    void joinGameColorAlreadyTaken() throws DataAccessException {
        // Create two auth tokens
        AuthData auth1 = new AuthData("token1", "player1");
        AuthData auth2 = new AuthData("token2", "player2");
        dataAccess.createAuth(auth1);
        dataAccess.createAuth(auth2);

        // Create a game with white already taken
        GameData game = new GameData(1, "player1", null, "TestGame", new ChessGame());
        dataAccess.createGame(game);

        // Try to join as white again
        JoinGameRequest request = new JoinGameRequest("token2", "WHITE", 1);

        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> joinGameService.joinGame(request),
                "Should throw exception when color is taken"
        );

        assertEquals("Error: Forbidden", exception.getMessage());
    }

    @Test
    void joinGameUnauthorized() {
        // Try to join with invalid token
        JoinGameRequest request = new JoinGameRequest("invalidToken", "WHITE", 1);

        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> joinGameService.joinGame(request),
                "Should throw exception for invalid token"
        );

        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void joinGameDoesNotExist() throws DataAccessException {
        // Create auth token
        AuthData auth = new AuthData("validToken", "player1");
        dataAccess.createAuth(auth);

        // Try to join non-existent game
        JoinGameRequest request = new JoinGameRequest("validToken", "WHITE", 999);

        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> joinGameService.joinGame(request),
                "Should throw exception when game doesn't exist"
        );

        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    void joinGameBothColors() throws DataAccessException {
        // Create two auth tokens
        AuthData auth1 = new AuthData("token1", "player1");
        AuthData auth2 = new AuthData("token2", "player2");
        dataAccess.createAuth(auth1);
        dataAccess.createAuth(auth2);

        // Create a game
        GameData game = new GameData(1, null, null, "TestGame", new ChessGame());
        dataAccess.createGame(game);

        // Player 1 joins as white
        JoinGameRequest request1 = new JoinGameRequest("token1", "WHITE", 1);
        joinGameService.joinGame(request1);

        // Player 2 joins as black
        JoinGameRequest request2 = new JoinGameRequest("token2", "BLACK", 1);
        joinGameService.joinGame(request2);

        // Verify both players are in the game
        GameData updatedGame = dataAccess.getGame(1);
        assertEquals("player1", updatedGame.whiteUsername());
        assertEquals("player2", updatedGame.blackUsername());
    }
}