package service.game;

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
        System.out.println("Received player color: " + request.playerColor());
        if (request.playerColor() == null || request.playerColor().isEmpty()) {
            throw new DataAccessException("Error: bad request");
        } else if (!request.playerColor().equalsIgnoreCase("WHITE") &&
                !request.playerColor().equalsIgnoreCase("BLACK")) {
            throw new DataAccessException("Error: bad request");
        }

        if(request.gameID() == null) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = dataAccess.getGame(request.gameID());
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        String username = authData.username();
        if (request.playerColor().equalsIgnoreCase("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: Forbidden");
            }
            GameData updatedGame = new GameData(
                    game.gameID(),
                    username,
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );
            dataAccess.updateGame(updatedGame);

        } else {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: Forbidden");
            }
            GameData updatedGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    username,
                    game.gameName(),
                    game.game()
            );
            dataAccess.updateGame(updatedGame);
        }
    }
}