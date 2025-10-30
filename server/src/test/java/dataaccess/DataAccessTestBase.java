package dataaccess;
import org.junit.jupiter.api.BeforeEach;

public class DataAccessTestBase {
    protected MySQLDataAccess dataAccess;

    @BeforeEach
    public void setup() throws DataAccessException {
        dataAccess = new MySQLDataAccess();
        dataAccess.clearUsers();
        dataAccess.clearGames();
        dataAccess.clearAuth();
    }
}
