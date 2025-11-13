package client;

import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerException;
import serverfacade.ServerFacade;

import javax.sql.rowset.serial.SerialException;
import java.util.UUID;

public class ServerFacadeTests {

    private static Server server;
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @AfterEach
    public void cleanUp() throws Exception{
        ServerFacade facade = new ServerFacade(port);
        try {
            facade.delete("/db", Void.class, null);
        } catch (ServerException e) {
            System.err.println("Database clear failed: " + e.getMessage());
        }
    }

    @Test
    public void serverFacadeConnectivity() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        try {
            String body = facade.get("/", String.class, null);
            Assertions.assertNotNull(body, "Expected a response body (may be empty string)");
        } catch (serverfacade.ServerException e) {
            Assertions.assertTrue(e.getStatusCode() != -1);
        }
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        String username = "user_" + UUID.randomUUID().toString().replace("-","").substring(0,12);
        model.RegisterRequest req = new model.RegisterRequest(username, "password123", username+"@example.com");
        model.RegisterResult result = facade.register(req);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(username, result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void testRegisterDuplicateUser() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        String username = "dup_"+UUID.randomUUID().toString().replace("-","").substring(0,12);
        model.RegisterRequest req = new model.RegisterRequest(username, "pass", username+"@example.com");
        facade.register(req); // First registration succeeds

        // Try to register same user again
        Assertions.assertThrows(serverfacade.ServerException.class, () -> {
            facade.register(req);
        });
    }

    @Test
    public void testLoginSuccess() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        String username = "user_" + UUID.randomUUID().toString().replace("-","").substring(0,12);
        String password = "password123";

        // register first
        model.RegisterRequest regReq = new model.RegisterRequest(username, password, username+"@example.com");
        model.RegisterResult regRes = facade.register(regReq);
        Assertions.assertNotNull(regRes);

        // now login
        model.LoginRequest loginReq = new model.LoginRequest(username, password);
        model.LoginResult loginRes = facade.login(loginReq);
        Assertions.assertNotNull(loginRes);
        Assertions.assertEquals(username, loginRes.username());
        Assertions.assertNotNull(loginRes.authToken());
    }

    @Test
    public void testLoginBadPassword() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        String username = "user_" + UUID.randomUUID().toString().replace("-","").substring(0,12);
        String password = "password123";

        model.RegisterRequest regReq = new model.RegisterRequest(username, password, username+"@example.com");
        facade.register(regReq);

        // wrong password
        model.LoginRequest bad = new model.LoginRequest(username, "wrongpass");
        serverfacade.ServerException ex = Assertions.assertThrows(serverfacade.ServerException.class, () -> {
            facade.login(bad);
        });
        // server maps unauthorized -> 401
        Assertions.assertTrue(ex.getStatusCode() == 401);
    }

    @Test
    public void testCreateGameSuccess() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        String username = "user_" + UUID.randomUUID().toString().replace("-","").substring(0,12);
        String password = "password123";

        // Register and login
        model.RegisterRequest regReq = new model.RegisterRequest(username, password, username+"@example.com");
        facade.register(regReq);
        model.LoginRequest loginReq = new model.LoginRequest(username, password);
        model.LoginResult loginRes = facade.login(loginReq);

        // Create a game
        String gameName = "TestGame_" + UUID.randomUUID().toString().substring(0, 8);
        model.CreateGameRequest createReq = new model.CreateGameRequest(gameName);
        model.CreateGameResult createRes = facade.createGame(createReq, loginRes.authToken());

        Assertions.assertNotNull(createRes);
        Assertions.assertNotNull(createRes.gameID());
        Assertions.assertTrue(createRes.gameID() > 0);
    }

    @Test
    public void testCreateGameUnauthorized() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        String gameName = "TestGame_" + UUID.randomUUID().toString().substring(0, 8);
        model.CreateGameRequest createReq = new model.CreateGameRequest(gameName);

        // Try to create game without auth token
        serverfacade.ServerException ex = Assertions.assertThrows(serverfacade.ServerException.class, () -> {
            facade.createGame(createReq, null);
        });

        // Server should return 401 Unauthorized
        Assertions.assertTrue(ex.getStatusCode() == 401);
    }

    @Test
    public void testListGamesSuccess() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        String username = "user_" + UUID.randomUUID().toString().replace("-","").substring(0,12);
        String password = "password123";

        // Register and login
        model.RegisterRequest regReq = new model.RegisterRequest(username, password, username+"@example.com");
        facade.register(regReq);
        model.LoginRequest loginReq = new model.LoginRequest(username, password);
        model.LoginResult loginRes = facade.login(loginReq);

        // Create a couple games
        facade.createGame("Game1", loginRes.authToken());
        facade.createGame("Game2", loginRes.authToken());

        // List games
        model.ListGamesResult result = facade.listGames(loginRes.authToken());

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.games());
        Assertions.assertTrue(result.games().size() >= 2);
    }

    @Test
    public void testListGamesUnauthorized() throws Exception {
        ServerFacade facade = new ServerFacade(port);

        // Try to list games without auth token
        serverfacade.ServerException ex = Assertions.assertThrows(serverfacade.ServerException.class, () -> {
            facade.listGames(null);
        });

        // Server should return 401 Unauthorized
        Assertions.assertTrue(ex.getStatusCode() == 401);
    }

    @Test
    public void testJoinGameSuccess() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        String username = "user_" + UUID.randomUUID().toString().replace("-","").substring(0,12);
        String password = "password123";

        // Register and login
        model.RegisterRequest regReq = new model.RegisterRequest(username, password, username+"@example.com");
        facade.register(regReq);
        model.LoginRequest loginReq = new model.LoginRequest(username, password);
        model.LoginResult loginRes = facade.login(loginReq);

        // Create a game
        String gameName = "TestGame_" + UUID.randomUUID().toString().substring(0, 8);
        model.CreateGameResult createRes = facade.createGame(gameName, loginRes.authToken());

        // Join the game as WHITE
        model.JoinGameRequest joinReq = new model.JoinGameRequest("WHITE", createRes.gameID());
        Assertions.assertDoesNotThrow(() -> {
            facade.joinGame(joinReq, loginRes.authToken());
        });
    }

    @Test
    public void testJoinGameUnauthorized() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        String username = "user_" + UUID.randomUUID().toString().replace("-","").substring(0,12);
        String password = "password123";

        // Register, login, and create a game
        model.RegisterRequest regReq = new model.RegisterRequest(username, password, username+"@example.com");
        facade.register(regReq);
        model.LoginRequest loginReq = new model.LoginRequest(username, password);
        model.LoginResult loginRes = facade.login(loginReq);
        model.CreateGameResult createRes = facade.createGame("TestGame", loginRes.authToken());

        // Try to join without auth token
        model.JoinGameRequest joinReq = new model.JoinGameRequest("WHITE", createRes.gameID());
        serverfacade.ServerException ex = Assertions.assertThrows(serverfacade.ServerException.class, () -> {
            facade.joinGame(joinReq, null);
        });

        Assertions.assertTrue(ex.getStatusCode() == 401);
    }

    @Test
    public void testJoinGameAlreadyTaken() throws Exception {
        ServerFacade facade = new ServerFacade(port);

        // Create two users
        String user1 = "user1_" + UUID.randomUUID().toString().replace("-","").substring(0,12);
        String user2 = "user2_" + UUID.randomUUID().toString().replace("-","").substring(0,12);
        String password = "password123";

        // Register and login both users
        facade.register(new model.RegisterRequest(user1, password, user1+"@example.com"));
        facade.register(new model.RegisterRequest(user2, password, user2+"@example.com"));

        model.LoginResult login1 = facade.login(new model.LoginRequest(user1, password));
        model.LoginResult login2 = facade.login(new model.LoginRequest(user2, password));

        // Create a game with user1
        model.CreateGameResult createRes = facade.createGame("TestGame", login1.authToken());

        // User1 joins as WHITE
        model.JoinGameRequest join1 = new model.JoinGameRequest("WHITE", createRes.gameID());
        facade.joinGame(join1, login1.authToken());

        // User2 tries to join as WHITE (should fail)
        model.JoinGameRequest join2 = new model.JoinGameRequest("WHITE", createRes.gameID());
        serverfacade.ServerException ex = Assertions.assertThrows(serverfacade.ServerException.class, () -> {
            facade.joinGame(join2, login2.authToken());
        });

        Assertions.assertTrue(ex.getStatusCode() == 403); // Already taken
    }

    @Test
    public void testLogoutSuccess() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        String username = "user_" + UUID.randomUUID().toString().replace("-","").substring(0,12);
        String password = "password123";

        // Register and login
        model.RegisterRequest regReq = new model.RegisterRequest(username, password, username+"@example.com");
        facade.register(regReq);
        model.LoginRequest loginReq = new model.LoginRequest(username, password);
        model.LoginResult loginRes = facade.login(loginReq);

        // Logout should succeed
        Assertions.assertDoesNotThrow(() -> {
            facade.logout(loginRes.authToken());
        });

        // After logout, using the same auth token should fail
        serverfacade.ServerException ex = Assertions.assertThrows(serverfacade.ServerException.class, () -> {
            facade.listGames(loginRes.authToken());
        });

        Assertions.assertTrue(ex.getStatusCode() == 401);
    }

    @Test
    public void testLogoutUnauthorized() throws Exception {
        ServerFacade facade = new ServerFacade(port);

        // Try to logout without auth token
        serverfacade.ServerException ex = Assertions.assertThrows(serverfacade.ServerException.class, () -> {
            facade.logout(null);
        });

        Assertions.assertTrue(ex.getStatusCode() == 401);
    }
}
