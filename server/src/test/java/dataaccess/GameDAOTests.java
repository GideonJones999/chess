package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import model.GameData;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameDAOTests extends DataAccessTestBase {
    @Test
    public void createGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "TestGame", new ChessGame());

        GameData created = dataAccess.createGame(game);

        assertNotNull(created, "Game should exist in db");
        assertTrue(created.gameID() > 0, "Game ID should be positive");
        assertEquals("TestGame", created.gameName());
        assertNotNull(created.game(), "ChessGame should not be null");
    }

    @Test
    public void createGameWithPlayers() throws DataAccessException {
        GameData game = new GameData(0, "white", "black", "PlayerGame", new ChessGame());

        GameData created = dataAccess.createGame(game);

        assertEquals("white", created.whiteUsername());
        assertEquals("black", created.blackUsername());
        assertEquals("PlayerGame", created.gameName());
    }

    @Test
    public void getGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "FindMe", new ChessGame());
        GameData created = dataAccess.createGame(game);

        GameData found = dataAccess.getGame(created.gameID());

        assertNotNull(found, "Game should be found");
        assertEquals(created.gameID(), found.gameID());
        assertEquals("FindMe", found.gameName());
        assertNotNull(found.game(), "ChessGame should be deserialized");
    }

    @Test
    public void getGameNotFound() throws DataAccessException {
        GameData notFound = dataAccess.getGame(999);
        assertNull(notFound, "Non-existent game should return null");
    }

    @Test
    public void clearGameSuccess() throws DataAccessException {
        dataAccess.createGame(new GameData(1, null, null, "game1", new ChessGame()));
        dataAccess.createGame(new GameData(2, null, null, "game2", new ChessGame()));
        dataAccess.createGame(new GameData(3, null, null, "game3", new ChessGame()));

        dataAccess.clearGames();

        assertNull(dataAccess.getGame(1), "game1 should be deleted");
        assertNull(dataAccess.getGame(2), "game2 should be deleted");
        assertNull(dataAccess.getGame(2), "game3 should be deleted");

        Collection<GameData> games = dataAccess.listGames();
        assertEquals(0, games.size(), "Should have 0 games after clear");
    }

    @Test
    public void listGamesEmpty() throws DataAccessException {
        Collection<GameData> games = dataAccess.listGames();
        assertNotNull(games, "Games collection should not be null");
        assertEquals(0, games.size(), "Should have 0 games");
    }

    @Test
    public void listGamesMultiple() throws DataAccessException {
        dataAccess.createGame(new GameData(0, null, null, "Game1", new ChessGame()));
        dataAccess.createGame(new GameData(0, "white", null, "Game2", new ChessGame()));
        dataAccess.createGame(new GameData(0, "white", "black", "Game3", new ChessGame()));

        Collection<GameData> games = dataAccess.listGames();

        assertNotNull(games, "Games collection should not be null");
        assertEquals(3, games.size(), "Should have 3 games");
    }

    @Test
    public void updateGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "UpdateMe", new ChessGame());
        GameData created = dataAccess.createGame(game);

        GameData updated = new GameData(
                created.gameID(),
                "newWhite",
                "newBlack",
                "updatedName",
                created.game()
        );
        dataAccess.updateGame(updated);

        GameData retrieved = dataAccess.getGame(created.gameID());
        assertEquals("newWhite", retrieved.whiteUsername());
        assertEquals("newBlack", retrieved.blackUsername());
        assertEquals("updatedName", retrieved.gameName());
    }

    @Test
    public void updateGameWithMoves() throws DataAccessException, InvalidMoveException {
        GameData game = new GameData(0, null, null, "UpdateMe", new ChessGame());
        GameData created = dataAccess.createGame(game);

        ChessGame modifiedGame = created.game();
        modifiedGame.makeMove(new ChessMove(new ChessPosition(2,1), new ChessPosition(3,1), null));

        GameData updated = new GameData(
                created.gameID(),
                "newWhite",
                "newBlack",
                "updatedName",
                modifiedGame
        );
        dataAccess.updateGame(updated);

        GameData retrieved = dataAccess.getGame(created.gameID());
        assertNotNull(retrieved.game(), "ChessGame should be preserved");
    }

    @Test
    public void updateGameNotFound() {
        GameData nonexistent = new GameData(9999, "white", "black", "Fake", new ChessGame());

        assertDoesNotThrow(() -> {
            dataAccess.updateGame(nonexistent);
        });
    }
}
