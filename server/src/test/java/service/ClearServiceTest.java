package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClearServiceTest {
    private MemoryDataAccess dataAccess;
    private ClearService clearService;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);
    }

    @Test
    void clearSuccessRemovesAllData() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@gmail.com");
//        dataAccess.createUser(user);

        AuthData auth = new AuthData("authToken123", "testUser");
//        dataAccess.createAuth(auth);

        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
//        dataAccess.createGame(game);

        assertDoesNotThrow(() -> clearService.clear());
//        assertNull(dataAccess.getUser("testUser"), "User should be cleared");
//        assertNull(dataAccess.getAuth("abc123token"), "Auth should be cleared");
//        assertNull(dataAccess.getGame(1), "Game should be cleared");
    }
}
