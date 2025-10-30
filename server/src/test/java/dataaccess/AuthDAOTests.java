package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOTests extends DataAccessTestBase {
    @Test
    public void createAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("token123", "testUser");
        dataAccess.createAuth(auth);
        AuthData retrievedAuth = dataAccess.getAuth("token123");
        assertNotNull(retrievedAuth, "Auth should exist in db");
        assertEquals("testUser", retrievedAuth.username());
        assertEquals("token123", retrievedAuth.authToken());
    }

    @Test
    public void createAuthDuplicate() throws DataAccessException {
        AuthData auth = new AuthData("dupe", "testUser");
        dataAccess.createAuth(auth);
        assertThrows(DataAccessException.class, () -> {
            dataAccess.createAuth(auth);
        }, "Creating duplicate auth should throw exception");
    }

    @Test
    public void getAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("findToken", "findme");
        dataAccess.createAuth(auth);

        AuthData found = dataAccess.getAuth("findToken");

        assertNotNull(found, "Auth should be found");
        assertEquals("findme", found.username());
        assertEquals("findToken", found.authToken());
    }

    @Test
    public void getAuthNotFound() throws DataAccessException {
        AuthData notFound = dataAccess.getAuth("nonexistent");
        assertNull(notFound, "Non-existent auth should return null");
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("deleteToken", "deleteMe");
        dataAccess.createAuth(auth);

        assertNotNull(dataAccess.getAuth("deleteToken"), "Auth should be found");
        dataAccess.deleteAuth("deleteToken");
        assertNull(dataAccess.getAuth("deleteToken"), "Auth should be deleted");
    }

    @Test
    public void deleteAuthNotFound() throws DataAccessException {
        assertDoesNotThrow(() -> {
            dataAccess.deleteAuth("nonexistent");
        }, "Deleting non-existent auth should not throw exception");
    }


    @Test
    public void clearAuthSuccess() throws DataAccessException {
        dataAccess.createAuth(new AuthData("token1", "user1"));
        dataAccess.createAuth(new AuthData("token2", "user2"));
        dataAccess.createAuth(new AuthData("token3", "user3"));

        dataAccess.clearAuth();

        assertNull(dataAccess.getAuth("token1"), "token1 should be deleted");
        assertNull(dataAccess.getAuth("token2"), "token2 should be deleted");
        assertNull(dataAccess.getAuth("token3"), "token3 should be deleted");
    }

}
