package net.loretale.rpengine.repositories;

import java.sql.Connection;

public abstract class Repository {
    protected Connection connection;

    public Repository(Connection connection) {
        this.connection = connection;
    }
}
