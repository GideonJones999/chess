package client;

import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;

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
        model.RegisterRequest req = new model.RegisterRequest("newuser", "password123", "user@example.com");
        model.RegisterResult result = facade.register(req);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("newuser", result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void testRegisterDuplicateUser() throws Exception {
        ServerFacade facade = new ServerFacade(port);
        model.RegisterRequest req = new model.RegisterRequest("testuser", "pass", "test@example.com");
        facade.register(req); // First registration succeeds

        // Try to register same user again
        Assertions.assertThrows(serverfacade.ServerException.class, () -> {
            facade.register(req);
        });
    }

}
