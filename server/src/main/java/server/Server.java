package server;

import io.javalin.*;
import io.javalin.http.Context;
import com.google.gson.Gson;
import java.util.Map;

// Data Access
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;

// Services
import service.ClearService;

public class Server {
    private final Javalin javalin;
    private final DataAccess dataAccess;
    private final ClearService clearService;
    private final Gson gson = new Gson();

    public Server() {
        dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register Endpoints Here
        javalin.delete("/db", this::handleClear);

        // Register Exception Handlers Here
        javalin.exception(Exception.class, this::handleException);
    }

    private void handleClear(Context ctx) throws Exception {
        clearService.clear();
        ctx.status(200);
        ctx.json("{}");
    }

    private void handleException(Exception e, Context ctx) {
        ctx.status(500);
        ctx.json(Map.of("message", "Error: "+e.getMessage()));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
