package dataaccess;

import model.AuthData;
import model.UserData;

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
}
