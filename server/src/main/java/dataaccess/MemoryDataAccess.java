package dataaccess;

import java.util.HashMap;
import java.util.Map;

import model.*;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {
    private Map<String, AuthData> authTokens = new HashMap<>();
    private Map<Integer, GameData> games = new HashMap<>();
    private Map<String, UserData> users = new HashMap<>();

    @Override
    public void clearUsers() throws DataAccessException {
        users.clear();
    }

    @Override
    public void clearGames() throws DataAccessException {
        games.clear();
    }

    @Override
    public void clearAuth() throws DataAccessException {
        authTokens.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);
    }
}
