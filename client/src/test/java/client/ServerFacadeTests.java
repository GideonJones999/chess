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

}
