package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;

public class LogoutService {
    private final DataAccess dataAccess;
    public LogoutService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public void logout(LogoutRequest request) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(request.authToken());
        if (authData == null) {
            throw new DataAccessException("Error: Unauthorized");
        }
        dataAccess.deleteAuth(request.authToken());
    }
}
