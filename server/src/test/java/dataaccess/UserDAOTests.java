package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTests extends DataAccessTestBase {
    @Test
    public void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@email.com");
        dataAccess.createUser(user);
        UserData retrievedUser = dataAccess.getUser("testUser");
        assertNotNull(retrievedUser, "User should exist in db");
        assertEquals("testUser", retrievedUser.username());
        assertEquals("test@email.com", retrievedUser.email());

        assertNotEquals("password123", retrievedUser.password(), "Password should be hashed");
        assertTrue(BCrypt.checkpw("password123", retrievedUser.password()),
                "Password should match when checked with BCrypt");
    }

    @Test
    public void createUserDuplicate() throws DataAccessException {
        UserData user = new UserData("dupe", "pass", "test@email.com");
        dataAccess.createUser(user);
        assertThrows(DataAccessException.class, () -> {
            dataAccess.createUser(user);
        }, "Creating duplicate user should throw exception");
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        UserData user = new UserData("findme", "pass", "find@email.com");
        dataAccess.createUser(user);

        UserData found = dataAccess.getUser("findme");

        assertNotNull(found, "User should be found");
        assertEquals("findme", found.username());
        assertEquals("find@email.com", found.email());
    }

    @Test
    public void getUserNotFound() throws DataAccessException {
        UserData notFound = dataAccess.getUser("nonexistent");
        assertNull(notFound, "Non-existent user should return null");
    }

    @Test
    public void clearUsersSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("user1", "pass1", "email1@test.com"));
        dataAccess.createUser(new UserData("user2", "pass2", "email2@test.com"));
        dataAccess.createUser(new UserData("user3", "pass3", "email3@test.com"));

        dataAccess.clearUsers();

        assertNull(dataAccess.getUser("user1"), "User1 should be deleted");
        assertNull(dataAccess.getUser("user2"), "User2 should be deleted");
        assertNull(dataAccess.getUser("user3"), "User3 should be deleted");
    }

}
