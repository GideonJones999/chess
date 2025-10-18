package dataaccess;

public interface DataAccess {
    void clearUsers() throws DataAccessException;
    void clearGames() throws DataAccessException;
    void clearAuth() throws DataAccessException;
}
