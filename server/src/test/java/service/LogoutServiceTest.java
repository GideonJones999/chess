package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutServiceTest {
    private MemoryDataAccess dataAccess;
    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        logoutService = new LogoutService(dataAccess);
    }

    @Test
    void logoutSuccess() throws DataAccessException {
        AuthData auth = new AuthData("validToken123", "testUser");
        dataAccess.createAuth(auth);

        LogoutRequest request = new LogoutRequest("validToken123");
        assertDoesNotThrow(() -> logoutService.logout(request));

        assertNull(dataAccess.getAuth("validToken123"), "Auth token should be deleted");
    }

    @Test
    void logoutInvalidToken() {
        LogoutRequest request = new LogoutRequest("invalidToken");
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> logoutService.logout(request),
                "Should throw Exception for Invalid Token"
        );

        assertTrue(exception.getMessage().contains("unauthorized"));
    }
}
