package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

public class CreateGameService {
    private final DataAccess dataAccess;

    public CreateGameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException {
        if (request.gameTitle() == null || request.gameTitle().isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData newGame = new GameData(0, null, null, request.gameTitle(), new ChessGame());
        GameData createdGame = dataAccess.createGame(newGame);

        return new CreateGameResult(createdGame.gameID());
    }
}