package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MySQLDataAccess implements DataAccess {
    private final Gson gson = new Gson();

    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (var conn = DatabaseManager.getConnection()) {
            String[] createStatements = {
                """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(255) NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT PRIMARY KEY AUTO_INCREMENT,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255) NOT NULL,
                    game TEXT NOT NULL
                )
                """
            };
            for (var statement: createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure database: " + e.getMessage());
        }
    }

    @Override
    public void clearUsers() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("TRUNCATE TABLE users")) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users: " + e.getMessage());
        }
    }

    @Override
    public void clearGames() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("TRUNCATE TABLE games")) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage());
        }

    }

    @Override
    public void clearAuth() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("TRUNCATE TABLE auth")) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing auth: " + e.getMessage());
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        return null;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }
}
