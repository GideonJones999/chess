package server;

import com.google.gson.Gson;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class JavalinGson implements JsonMapper {
    private final Gson gson = new Gson();

    @Override
    public String toJsonString(@NotNull Object obj, @NotNull Type type) {
        return gson.toJson(obj, type);
    }

    @Override
    public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
        return gson.fromJson(json, targetType);
    }

    @Override
    public InputStream toJsonStream(@NotNull Object obj, @NotNull Type type) {
        return new ByteArrayInputStream(toJsonString(obj, type).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public <T> T fromJsonStream(@NotNull InputStream json, @NotNull Type targetType) {
        return gson.fromJson(new String(readAllBytes(json), StandardCharsets.UTF_8), targetType);
    }

    private byte[] readAllBytes(InputStream inputStream) {
        try {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}