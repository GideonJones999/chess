package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.game.ListGamesRequest;
import service.game.ListGamesResult;
import service.game.ListGamesService;

import static org.junit.jupiter.api.Assertions.*;

public class ListGamesServiceTest {
    private MemoryDataAccess dataAccess;
    private ListGamesService listGamesService;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        listGamesService = new ListGamesService(dataAccess);
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        AuthData auth = new AuthData("validToken", "testUser");
        dataAccess.createAuth(auth);

        GameData game1 = new GameData(1, "white1", "black1", "Game1", new ChessGame());
        GameData game2 = new GameData(2, null, "black2", "Game2", new ChessGame());
        dataAccess.createGame(game1);
        dataAccess.createGame(game2);

        ListGamesRequest request = new ListGamesRequest("validToken");
        ListGamesResult result = listGamesService.listGames(request);

        assertNotNull(result, "Result should not be null");
        assertNotNull(result.games(), "Games Collection should not be null");
        assertEquals(2, result.games().size(), "Games Collection should contain 2 games");
    }

    @Test
    void listGamesUnauthorized() {
        ListGamesRequest request = new ListGamesRequest("invalidToken");
        DataAccessException e = assertThrows(
                DataAccessException.class,
                () -> listGamesService.listGames(request),
                "Should throw exception for invalid token"
        );

        assertTrue(e.getMessage().contains("Unauthorized"));
    }

    @Test
    void listGamesEmptyList() throws DataAccessException {
        AuthData auth = new AuthData("validToken", "testUser");
        dataAccess.createAuth(auth);

        ListGamesRequest request = new ListGamesRequest("validToken");
        ListGamesResult result = listGamesService.listGames(request);

        assertNotNull(result, "Result should not be null");
        assertNotNull(result.games(), "Games Collection should not be null");
        assertEquals(0, result.games().size(), "Games Collection should contain 0 games");
    }
}
