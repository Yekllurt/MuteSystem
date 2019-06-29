package dev.yekllurt.mutesystem.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MojangAPI {

    private static final String UUID_AT_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";
    private static final String PLAYERPROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private static final JsonParser JSON_PARSER = new JsonParser();

    private static final long CACHE_TIME = 300000L;

    private static final ConcurrentMap<String, UUID> UUID_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Long> UUID_CACHE_INSERT = new ConcurrentHashMap();
    private static final ConcurrentMap<UUID, String> NAME_CACHE = new ConcurrentHashMap();
    private static final ConcurrentMap<UUID, Long> NAME_CACHE_INESRT = new ConcurrentHashMap();

    public static void getUUIDAt(String name, long timestamp, Consumer<UUID> callback) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(callback);
        EXECUTOR_SERVICE.execute(() -> callback.accept(getUUIDAt(name, timestamp)));
    }

    public static UUID getUUIDAt(String name, long timestamp) {
        Objects.requireNonNull(name);
        if (UUID_CACHE.get(name) != null &&
                (System.currentTimeMillis() - UUID_CACHE_INSERT.get(name)) < CACHE_TIME) {
            return UUID_CACHE.get(name);
        }
        try {
            URL url = new URL(String.format(UUID_AT_URL, name, timestamp));
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(5000);
            JsonElement result = JSON_PARSER.parse(new InputStreamReader(httpsURLConnection.getInputStream()));
            if (result.isJsonObject() == false) {
                return null;
            }
            JsonObject jsonObject = result.getAsJsonObject();
            UUID uuid = UUID.fromString(toValidUUIDFormat(jsonObject.get("id").getAsString()));
            UUID_CACHE_INSERT.put(name, System.currentTimeMillis());
            UUID_CACHE.put(name, uuid);
            return uuid;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getUUID(String name, Consumer<UUID> callback) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(callback);
        EXECUTOR_SERVICE.execute(() -> callback.accept(getUUID(name)));
    }

    public static UUID getUUID(String name) {
        Objects.requireNonNull(name);
        return getUUIDAt(name, System.currentTimeMillis());
    }

    public static void getName(UUID uuid, Consumer<String> callback) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(callback);
        EXECUTOR_SERVICE.execute(() -> callback.accept(getName(uuid)));
    }

    public static String getName(UUID uuid) {
        Objects.requireNonNull(uuid);
        if (NAME_CACHE.get(uuid) != null &&
                (System.currentTimeMillis() - NAME_CACHE_INESRT.get(uuid)) < CACHE_TIME) {
            return NAME_CACHE.get(uuid);
        }
        try {
            URL url = new URL(String.format(PLAYERPROFILE_URL, uuid.toString().replaceAll("-", "")));
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(5000);

            JsonElement result = JSON_PARSER.parse(new InputStreamReader(httpsURLConnection.getInputStream()));
            if (result.isJsonObject() == false) {
                return null;
            }
            JsonObject raw = result.getAsJsonObject();
            String name = raw.get("name").getAsString();
            NAME_CACHE_INESRT.put(uuid, System.currentTimeMillis());
            NAME_CACHE.put(uuid, name);
            return name;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String toValidUUIDFormat(String incompleteUUID) {
        Objects.requireNonNull(incompleteUUID);
        return new StringBuffer(incompleteUUID)
                .insert(8, "-")
                .insert(13, "-")
                .insert(18, "-")
                .insert(23, "-").toString();
    }

}
