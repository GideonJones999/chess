package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.auth.RegisterRequest;
import service.auth.RegisterResult;
import service.auth.RegisterService;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterServiceTest {
    private MemoryDataAccess dataAccess;
    private RegisterService registerService;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        registerService = new RegisterService(dataAccess);
    }

    @Test
    void registerSuccess() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("newUser", "password", "user@email.com");
        RegisterResult result = registerService.register(request);
        assertNotNull(result, "Result should not be null");
        assertEquals("newUser", result.username(), "Username should match");
        assertNotNull(result.authToken(), "Auth token should be Generated");

        UserData user = dataAccess.getUser("newUser");
        assertNotNull(user, "User should be in database");
        assertEquals("newUser", user.username(), "Username should match");
    }

    @Test
    void registerDuplicateUsername() throws DataAccessException {
        RegisterRequest request1 = new RegisterRequest("duplicateUser", "password123", "user1@email.com");
        registerService.register(request1);
        RegisterRequest request2 = new RegisterRequest("duplicateUser", "differentPassword", "user2@email.com");
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> registerService.register(request2),
                "Should throw exception for duplicate username"
        );
        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Error: Forbidden"));
    }

    @Test
    void registerBadRequest() {
        RegisterRequest request = new RegisterRequest(null, "password123", "user@email.com");
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> registerService.register(request),
                "Should throw exception for null username"
        );
        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Error: Bad Request"));
    }
}
