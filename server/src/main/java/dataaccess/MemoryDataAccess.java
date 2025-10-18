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
}
