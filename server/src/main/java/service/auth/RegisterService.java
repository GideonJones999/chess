package service.auth;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class RegisterService {
    private final DataAccess dataAccess;

    public RegisterService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        if (request.username() == null || request.username().isEmpty() ||
            request.password() == null || request.password().isEmpty() ||
            request.email() == null || request.email().isEmpty()) {
            throw new DataAccessException("Error: Bad Request");
        }

        UserData existingUser = dataAccess.getUser(request.username());
        if (existingUser != null) {
            throw new DataAccessException("Error: Forbidden");
        }

        UserData newUser = new UserData(request.username(), request.password(), request.email());
        dataAccess.createUser(newUser);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, request.username());
        dataAccess.createAuth(authData);

        return new RegisterResult(request.username(), authToken);
    }
}
