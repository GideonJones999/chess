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
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (request.gameName() == null || request.gameName().isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        GameData newGame = new GameData(0, null, null, request.gameName(), new ChessGame());
        GameData createdGame = dataAccess.createGame(newGame);
        Integer id = createdGame.gameID();  // auto-boxes int -> Integer
        return new CreateGameResult(id);
    }
}