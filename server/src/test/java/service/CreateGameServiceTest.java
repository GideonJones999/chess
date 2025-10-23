package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.game.CreateGameRequest;
import service.game.CreateGameResult;
import service.game.CreateGameService;

import static org.junit.jupiter.api.Assertions.*;

class CreateGameServiceTest {

    private MemoryDataAccess dataAccess;
    private CreateGameService createGameService;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        createGameService = new CreateGameService(dataAccess);
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        AuthData auth = new AuthData("validToken", "testUser");
        dataAccess.createAuth(auth);

        // Create a game
        CreateGameRequest request = new CreateGameRequest("validToken", "MyChessGame");
        CreateGameResult result = createGameService.createGame(request);

        // Verify result
        assertNotNull(result, "Result should not be null");
        assertTrue(result.gameID() > 0, "Game ID should be positive");

        // Verify game was created in database
        assertNotNull(dataAccess.getGame(result.gameID()), "Game should be in database");
    }

    @Test
    void createGameUnauthorized() {
        // Try to create game with invalid token
        CreateGameRequest request = new CreateGameRequest("invalidToken", "MyChessGame");

        // Should throw unauthorized exception
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> createGameService.createGame(request),
                "Should throw exception for invalid token"
        );

        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    void createGameBadRequest() throws DataAccessException {
        // Create an auth token
        AuthData auth = new AuthData("validToken", "testUser");
        dataAccess.createAuth(auth);

        // Try to create game with empty name
        CreateGameRequest request = new CreateGameRequest("validToken", "");

        // Should throw bad request exception
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> createGameService.createGame(request),
                "Should throw exception for empty game name"
        );

        assertTrue(exception.getMessage().contains("bad request"));
    }

    @Test
    void createMultipleGames() throws DataAccessException {
        // Create an auth token
        AuthData auth = new AuthData("validToken", "testUser");
        dataAccess.createAuth(auth);

        // Create multiple games
        CreateGameRequest request1 = new CreateGameRequest("validToken", "Game1");
        CreateGameRequest request2 = new CreateGameRequest("validToken", "Game2");

        CreateGameResult result1 = createGameService.createGame(request1);
        CreateGameResult result2 = createGameService.createGame(request2);

        // Verify they have different IDs
        assertNotEquals(result1.gameID(), result2.gameID(), "Game IDs should be unique");
    }
}