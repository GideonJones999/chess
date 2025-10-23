package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
