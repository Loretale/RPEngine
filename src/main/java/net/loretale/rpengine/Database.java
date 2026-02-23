package net.loretale.rpengine;

import net.loretale.rpengine.migrations.MigrationManager;
import net.loretale.rpengine.repositories.InfractionRepository;
import net.loretale.rpengine.repositories.LoretalePlayerRepository;
import net.loretale.rpengine.repositories.PlayerCharacterRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static String url;
    private static String username;
    private static String password;

    private static Connection connection;

    private static InfractionRepository infractionRepository;
    private static LoretalePlayerRepository loretalePlayerRepository;
    private static PlayerCharacterRepository playerCharacterRepository;

    public static InfractionRepository getInfractionRepository() {
        return infractionRepository;
    }

    public static LoretalePlayerRepository getLoretalePlayerRepository() {
        return loretalePlayerRepository;
    }

    public static PlayerCharacterRepository getPlayerCharacterRepository() {
        return playerCharacterRepository;
    }

    public static void init(String url, String username, String password) throws SQLException {
        Database.url = url;
        Database.username = username;
        Database.password = password;

        try { Class.forName("org.postgresql.Driver"); } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        connection = DriverManager.getConnection(Database.url, Database.username, Database.password);

        MigrationManager.migrate(connection);

        infractionRepository = new InfractionRepository();
        loretalePlayerRepository = new LoretalePlayerRepository();
        playerCharacterRepository = new PlayerCharacterRepository();
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(5)) {
            connection = DriverManager.getConnection(Database.url, Database.username, Database.password);
        }
        return connection;
    }
}

