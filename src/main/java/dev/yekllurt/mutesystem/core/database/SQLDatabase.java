package dev.yekllurt.mutesystem.core.database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SQLDatabase {

    public enum ConnectionAttemptResult {
        ALREADY_CONNECTED, SUCCESSFULLY_CONNECTED, FAILED_CONNECTING, FAILED_CONNECTING_SSL_CONNECTION_REQUIRED_BUT_NOT_SUPPORTED_BY_SERVER;
    }

    public static final boolean successfulConnectionAttemptResult(ConnectionAttemptResult result) {
        return (result != ConnectionAttemptResult.ALREADY_CONNECTED &&
                result != ConnectionAttemptResult.SUCCESSFULLY_CONNECTED) ? false : true;
    }

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final JavaPlugin javaPlugin;

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean useSSL;
    private final boolean requireSSL;

    private Connection connection;

    public SQLDatabase(JavaPlugin javaPlugin, String host, int port, String database, String username, String password, boolean useSSL, boolean requireSSL) {
        this.javaPlugin = javaPlugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.useSSL = useSSL;
        this.requireSSL = requireSSL;
    }

    public ConnectionAttemptResult connect() {
        if (isConnected()) return ConnectionAttemptResult.ALREADY_CONNECTED;
        try {
            StringBuilder stringBuilderURL = new StringBuilder();
            stringBuilderURL.append("jdbc:mysql://");
            stringBuilderURL.append(this.host + ":" + this.port);
            stringBuilderURL.append("/" + this.database);
            stringBuilderURL.append("?useSSL=" + this.useSSL);
            stringBuilderURL.append("&requireSSL=" + this.requireSSL);

            this.connection = DriverManager.getConnection(stringBuilderURL.toString(), this.username, this.password);
        } catch (SQLException e) {
            if (e.getSQLState().equals("08001") &&
                    e.getMessage().equals("SSL Connection required, but not supported by server.")) {
                this.javaPlugin.getLogger().severe(
                        "Shutting down MuteSytem. Reason: SSL Connection required, but not supported by server.");
                Bukkit.getPluginManager().disablePlugin(this.javaPlugin);
                return ConnectionAttemptResult.FAILED_CONNECTING_SSL_CONNECTION_REQUIRED_BUT_NOT_SUPPORTED_BY_SERVER;
            }
            return ConnectionAttemptResult.FAILED_CONNECTING;
        }
        return ConnectionAttemptResult.SUCCESSFULLY_CONNECTED;
    }

    public void disconnect() {
        if (!isConnected()) return;
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return (this.connection != null && !this.connection.isClosed());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int executeUpdate(String sql, Object[] parameters) {
        if (!isConnected()) {
            ConnectionAttemptResult result = connect();
            if (!successfulConnectionAttemptResult(result)) return -1;
        }
        int result = -1;
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            result = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public SQLResult executeQuery(String sql, Object[] parameters) {
        if (!isConnected()) {
            ConnectionAttemptResult result = connect();
            if (!successfulConnectionAttemptResult(result)) return null;
        }
        SQLResult result = null;
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                result = new SQLResult(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void executeAsyncUpdate(String sql, Object[] parameters, Consumer<Integer> callback) {
        this.executorService.execute(() -> Bukkit.getScheduler().runTask(this.javaPlugin, () -> callback.accept(executeUpdate(sql, parameters))));
    }

    public void executeAsyncQuery(String sql, Object[] parameters, Consumer<SQLResult> callback) {
        this.executorService.execute(() -> Bukkit.getScheduler().runTask(this.javaPlugin, () -> callback.accept(executeQuery(sql, parameters))));
    }

}
