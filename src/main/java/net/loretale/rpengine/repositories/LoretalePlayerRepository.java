package net.loretale.rpengine.repositories;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LoretalePlayerRepository extends Repository {
    public LoretalePlayerRepository(Connection connection) {
        super(connection);
    }

    public boolean exists(UUID playerId) {
        String sql = "SELECT 1 FROM hytale_users WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, hytaleUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update last changed character timestamp", e);
        }
    }

    public UUID getPlayerIdFromHytaleUser(UUID hytaleUserId) {
        String sql = "SELECT player_id FROM hytale_users WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        String sql = """
        WITH accepted_app AS (
            SELECT user_id AS discord_id, username
            FROM applications
            WHERE status = 'ACCEPTED'
              AND LOWER(username) = LOWER(?)
        ),
        new_player AS (
            INSERT INTO loretale_players DEFAULT VALUES
            SELECT 1 FROM accepted_app
            RETURNING id
        ),
        insert_hytale_user AS (
            INSERT INTO hytale_users (id, player_id, name)
            SELECT ?, new_player.id, accepted_app.username
            FROM new_player
            JOIN accepted_app ON TRUE
        )
        INSERT INTO discord_ids (player_id, discord_id)
        SELECT new_player.id, accepted_app.discord_id
        FROM new_player
        JOIN accepted_app ON TRUE;
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setObject(2, hytaleUserId);

            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create player from accepted application", e);
        }
    }
}

