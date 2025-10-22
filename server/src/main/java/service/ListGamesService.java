package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import java.util.Collection;

public class ListGamesService {
    private final DataAccess dataAccess;

    public ListGamesService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public ListGamesResult listGames(ListGamesRequest request) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new DataAccessException("Error: Unauthorized");
        }
        Collection<GameData> games = dataAccess.listGames();

        return new ListGamesResult(games);
    }
}
