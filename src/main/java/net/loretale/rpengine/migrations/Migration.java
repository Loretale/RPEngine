package net.loretale.rpengine.migrations;

import java.sql.Connection;
import java.sql.SQLException;

public interface Migration {
    int version();
    void apply(Connection connection) throws SQLException;
}
