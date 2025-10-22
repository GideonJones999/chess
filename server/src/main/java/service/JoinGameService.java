package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

public class JoinGameService {
    private final DataAccess dataAccess;

    public JoinGameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void joinGame(JoinGameRequest request) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (request.playerColor() == null) {
            throw new DataAccessException("Error: bad request");
        }
        GameData game = dataAccess.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }
        String username = authData.username();
        if (request.playerColor().equalsIgnoreCase("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            GameData updatedGame = new GameData(
                    game.gameID(),
                    username,
                    game.blackUsername(),
                    game.gameTitle(),
                    game.game()
            );
            dataAccess.updateGame(updatedGame);

        } else if (request.playerColor().equalsIgnoreCase("BLACK")) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            GameData updatedGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    username,
                    game.gameTitle(),
                    game.game()
            );
            dataAccess.updateGame(updatedGame);
        } else {
            throw new DataAccessException("Error: bad request");
        }
    }
}