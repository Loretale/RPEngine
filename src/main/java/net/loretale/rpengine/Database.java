package net.loretale.rpengine;

import net.loretale.rpengine.migrations.MigrationManager;
import net.loretale.rpengine.repositories.InfractionRepository;
import net.loretale.rpengine.repositories.LoretalePlayerRepository;
import net.loretale.rpengine.repositories.PlayerCharacterRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
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
        connection = DriverManager.getConnection(url, username, password);

        MigrationManager.migrate(connection);

        infractionRepository = new InfractionRepository(connection);
        loretalePlayerRepository = new LoretalePlayerRepository(connection);
        playerCharacterRepository = new PlayerCharacterRepository(connection);
    }

    public static Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("Database not initialized");
        }
        return connection;
    }
}

