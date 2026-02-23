package net.loretale.rpengine.repositories;

import net.loretale.rpengine.Database;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Repository {
    protected Connection getConnection() throws SQLException {
        return Database.getConnection();
    }
}
