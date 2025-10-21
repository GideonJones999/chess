package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class LoginService {
    private DataAccess dataAccess;

    public LoginService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        if (request.username() == null || request.username().isEmpty() ||
                request.password() == null || request.password().isEmpty()) {
            throw new DataAccessException("Username or password is empty");
        }
        UserData user = dataAccess.getUser(request.username());
        if (user == null) {
            throw new DataAccessException("User not found");
        }
        if (!user.password().equals(request.password())) {
            throw new DataAccessException("Error: Unauthorized login");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, request.username());
        dataAccess.createAuth(authData);

        return new LoginResult(request.username(), authToken);
    }
}
