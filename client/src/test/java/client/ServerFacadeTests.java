package client;

import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;

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
}
