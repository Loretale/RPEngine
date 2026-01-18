package net.loretale.rpengine.repositories;

import net.loretale.rpengine.model.Infraction;
import net.loretale.rpengine.model.InfractionType;

import javax.annotation.Nullable;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InfractionRepository extends Repository {
    public InfractionRepository(Connection connection) {
        super(connection);
    }

    private Infraction mapInfraction(ResultSet rs) throws SQLException {
        Infraction infraction = new Infraction();

        infraction.type = InfractionType.valueOf(rs.getString("type"));
        infraction.start = rs.getTimestamp("start").toLocalDateTime();

        Timestamp endTs = rs.getTimestamp("end");
        infraction.end = endTs == null ? null : endTs.toLocalDateTime();

        infraction.reason = rs.getString("reason");

        return infraction;
    }

    public List<Infraction> getInfractionHistory(UUID hytaleUserId) {
        String sql = """
            SELECT i.*
            FROM infractions i
            JOIN hytale_users hu ON hu.player_id = i.player_id
            WHERE hu.id = ?
            ORDER BY i.start DESC
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, hytaleUserId);
            ResultSet rs = ps.executeQuery();

            List<Infraction> infractions = new ArrayList<>();

            while (rs.next()) {
                infractions.add(mapInfraction(rs));
            }

            return infractions;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch infraction history", e);
        }
    }

    public void addInfraction(
            UUID hytaleUserId,
            InfractionType type,
            @Nullable LocalDateTime end,
            String reason
    ) {
        String sql = """
            INSERT INTO infractions (player_id, type, start, "end", reason)
            SELECT hu.player_id, ?::infraction_type, CURRENT_TIMESTAMP, ?, ?
            FROM hytale_users hu
            WHERE hu.id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, type.name());

            if (end == null) {
                ps.setNull(2, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(2, Timestamp.valueOf(end));
            }

            ps.setString(3, reason);
            ps.setObject(4, hytaleUserId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to add infraction", e);
        }
    }
}
