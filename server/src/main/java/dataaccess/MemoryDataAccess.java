package dataaccess;

import java.util.*;

import model.*;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {
    private Map<String, AuthData> authTokens = new HashMap<>();
    private Map<Integer, GameData> games = new HashMap<>();
    private Map<String, UserData> users = new HashMap<>();
    private int nextGameID = 1;

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

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        return games.get(gameId);
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        int gameId = (game.gameID() == 0) ? nextGameID ++: game.gameID();

        GameData newGame = new GameData(
                gameId,
                game.whiteUsername(),
                game.blackUsername(),
                game.gameTitle(),
                game.game()
        );
        games.put(gameId, newGame);
        return newGame;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("Game does not exist");
        }
        games.put(game.gameID(), game);
    }
}
