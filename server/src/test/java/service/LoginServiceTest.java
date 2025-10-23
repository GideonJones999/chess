package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginServiceTest {
    private MemoryDataAccess dataAccess;
    private LoginService loginService;

    @BeforeEach
    void setUp() throws DataAccessException{
        dataAccess = new MemoryDataAccess();
        loginService = new LoginService(dataAccess);

        UserData user = new UserData("testUser", "password123", "test@email.com");
        dataAccess.createUser(user);
    }

    @Test
    void testLoginSuccessful() throws DataAccessException{
        LoginRequest request = new LoginRequest("testUser", "password123");
        LoginResult result = loginService.login(request);

        assertNotNull(request, "Result should not be null");
        assertEquals("testUser", result.username(), "Username should match");
        assertNotNull(result.authToken(), "Auth token should be Generated");
        assertNotNull(dataAccess.getAuth(result.authToken()), "Auth token should be in Database");
    }

    @Test
    void testLoginWrongPassword() {
        LoginRequest request = new LoginRequest("testUser", "wrongPassword");
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> loginService.login(request),
                "Should throw Exception for Wrong Password"
        );
        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Error: Unauthorized"));
    }

    @Test
    void testLoginUserNotExist() {
        LoginRequest request = new LoginRequest("noUser", "password123");

        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> loginService.login(request),
                "Should throw Exception for Non-Existing User"
        );
        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

    @Test
    void testLoginBadRequest() {
        LoginRequest request = new LoginRequest(null, "wrongPassword");
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> loginService.login(request),
                "Should throw exception for null username"
        );
        assertTrue(exception.getMessage().contains("Bad Request"));
    }

}
