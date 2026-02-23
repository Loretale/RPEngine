package net.loretale.rpengine.repositories;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LoretalePlayerRepository extends Repository {
    public LoretalePlayerRepository() { }

    public boolean exists(UUID playerId) {
        String sql = "SELECT 1 FROM hytale_users WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, playerId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check player existence", e);
        }
    }

    public Optional<ActiveBanInfo> getActiveBanReason(UUID playerId) {
        String sql = """
                SELECT i.reason, i."end"
                FROM infractions i
                JOIN hytale_users hu ON hu.player_id = i.player_id
                WHERE hu.id = ?
                    AND type = 'Ban'
                    AND (i."end" IS NULL OR i."end" > CURRENT_TIMESTAMP)
                ORDER BY i.start DESC
                LIMIT 1
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, playerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String reason = rs.getString("reason");
                Timestamp endTs = rs.getTimestamp("end");
                LocalDateTime end = endTs == null ? null : endTs.toLocalDateTime();

                return Optional.of(new ActiveBanInfo(reason, end));
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch active ban", e);
        }
    }

    public void updateLastSeen(UUID playerId) {
        String sql = """
            UPDATE loretale_players lp
            SET lp.last_seen_in_game = CURRENT_TIMESTAMP
            FROM hytale_users hu
            WHERE hu.player_id = lp.id
                AND hu.id = ?
        """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, playerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update last seen", e);
        }
    }

    public List<String> getAllHytaleUsernames(UUID playerId) {
        String sql = """
            SELECT h.name AS current_name, hnh.name AS old_name
            FROM hytale_users h
            LEFT JOIN hytale_user_name_history hnh
                ON hnh.hytale_user_id = h.id
            WHERE h.id = ?
        """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, playerId);
            ResultSet rs = ps.executeQuery();

            List<String> names = new ArrayList<>();
            while (rs.next()) {
                String current = rs.getString("current_name");
                String old = rs.getString("old_name");
                if (current != null) names.add(current);
                if (old != null) names.add(old);
            }
            return names;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch hytale usernames", e);
        }
    }

    public void setActiveCharacter(UUID hytaleUserId, UUID characterId) {
        String sql = """
            UPDATE loretale_players lp
            SET active_character_id = ?
            FROM hytale_users hu
            WHERE hu.player_id = lp.id
                AND hu.id = ?
        """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, characterId);
            ps.setObject(2, hytaleUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to set active character", e);
        }
    }

    public void updateLastChangedCharacter(UUID hytaleUserId) {
        String sql = """
            UPDATE loretale_players lp
            SET last_changed_character = CURRENT_TIMESTAMP
            FROM hytale_users hu
            WHERE hu.player_id = lp.id
                AND hu.id = ?
        """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, hytaleUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update last changed character timestamp", e);
        }
    }

    public UUID getPlayerIdFromHytaleUser(UUID hytaleUserId) {
        String sql = "SELECT player_id FROM hytale_users WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, hytaleUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("player_id");
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find player id from hytale user", e);
        }
    }

    public boolean createPlayerFromAcceptedApplication(
            UUID hytaleUserId,
            String username
    ) {
        try {
            getConnection().setAutoCommit(false);

            String discordId;
            String dbUsername;

            try (PreparedStatement ps = getConnection().prepareStatement("""
                SELECT user_id, username
                FROM applications
                WHERE status = 'ACCEPTED'
                AND LOWER(username) = LOWER(?)
                LIMIT 1
                """)) {
                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        getConnection().rollback();
                        return false;
                    }
                    discordId = rs.getString("user_id");
                    dbUsername = rs.getString("username");
                }
            }

            UUID playerId;

            try (PreparedStatement ps = getConnection().prepareStatement("""
                INSERT INTO loretale_players DEFAULT VALUES
                RETURNING id
                """)) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    playerId = rs.getObject("id", UUID.class);
                }
            }

            try (PreparedStatement ps = getConnection().prepareStatement("""
                INSERT INTO hytale_users (id, player_id, name)
                VALUES (?, ?, ?)
                """)) {
                ps.setObject(1, hytaleUserId);
                ps.setObject(2, playerId);
                ps.setString(3, dbUsername);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = getConnection().prepareStatement("""
                INSERT INTO discord_ids (player_id, discord_id)
                VALUES (?, ?)
                """)) {
                ps.setObject(1, playerId);
                ps.setString(2, discordId);
                ps.executeUpdate();
            }

            getConnection().commit();
            return true;

        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {}
            throw new DataAccessException(
                    "Failed to create player from accepted application",
                    e
            );
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

}

