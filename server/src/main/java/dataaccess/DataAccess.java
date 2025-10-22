package dataaccess;

import model.*;
import java.util.Collection;


public interface DataAccess {
//    Clear Methods
    void clearUsers() throws DataAccessException;
    void clearGames() throws DataAccessException;
    void clearAuth() throws DataAccessException;

//    User Methods
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

//    Auth Methods
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

//    Game Methods
    GameData createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
}
