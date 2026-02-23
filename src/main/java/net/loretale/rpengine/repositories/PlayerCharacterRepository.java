package net.loretale.rpengine.repositories;

import net.loretale.rpengine.Database;
import net.loretale.rpengine.model.CharacterType;
import net.loretale.rpengine.model.Gender;
import net.loretale.rpengine.model.PlayerCharacter;
import net.loretale.rpengine.model.Race;

import javax.annotation.Nullable;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerCharacterRepository extends Repository {
    public PlayerCharacterRepository() { }

    private PlayerCharacter mapCharacter(ResultSet rs) throws SQLException {
        PlayerCharacter c = new PlayerCharacter();
        c.id = (UUID) rs.getObject("id");
        c.type = CharacterType.valueOf(rs.getString("type"));
        c.name = rs.getString("name");

        String gender = rs.getString("gender");
        c.gender = gender == null ? null : Gender.valueOf(gender);

        c.birthDate = rs.getLong("birth_date");

        String race = rs.getString("race");
        c.race = race == null ? null : Race.valueOf(race);

        c.description = rs.getString("description");
        c.lives = rs.getInt("lives");
        c.chatColor = new Color(rs.getInt("chat_color"));

        return c;
    }

    public Optional<PlayerCharacter> getActiveCharacter(UUID playerId) {
        String sql = """
            SELECT pc.*
            FROM player_characters pc
            JOIN loretale_players lp ON lp.active_character_id = pc.id
            JOIN hytale_users hu ON hu.player_id = lp.id
            WHERE hu.id = ?
        """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, playerId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return Optional.empty();

            return Optional.of(mapCharacter(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update last seen", e);
        }
    }

    public List<PlayerCharacter> getAllCharacters(UUID playerId) {
        String sql = """
            SELECT pc.*
            FROM player_characters pc
            JOIN hytale_users hu ON hu.player_id = pc.player_id
            WHERE hu.id = ?
        """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, playerId);
            ResultSet rs = ps.executeQuery();

            List<PlayerCharacter> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapCharacter(rs));
            }

            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch characters", e);
        }
    }

    public List<PlayerCharacter> getAliveCharacters(UUID playerId) {
        String sql = """
            SELECT pc.*
            FROM player_characters pc
            JOIN hytale_users hu ON hu.player_id = pc.player_id
            WHERE hu.id = ?
                AND pc.lives > 0
        """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, playerId);
            ResultSet rs = ps.executeQuery();

            List<PlayerCharacter> list = new ArrayList<>();

            while (rs.next()) {
                list.add(mapCharacter(rs));
            }

            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch alive characters", e);
        }
    }

    public Optional<PlayerCharacter> getById(UUID characterId) {
        String sql = "SELECT * FROM player_characters WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, characterId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return Optional.empty();

            return Optional.of(mapCharacter(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch character by id", e);
        }
    }

    public void updateName(UUID characterId, String name) {
        String sql = "UPDATE player_characters SET name = ? WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setObject(2, characterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update character name", e);
        }
    }

    public void updateGender(UUID characterId, @Nullable Gender gender) {
        String sql = "UPDATE player_characters SET gender = ?::gender WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (gender == null) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, gender.name());
            }
            ps.setObject(2, characterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update character gender", e);
        }
    }

    public void updateBirthDate(UUID characterId, long birthDate) {
        String sql = "UPDATE player_characters SET birth_date = ? WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, birthDate);
            ps.setObject(2, characterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update character birthdate", e);
        }
    }

    public void updateRace(UUID characterId, @Nullable Race race) {
        String sql = "UPDATE player_characters SET race = ?::race WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (race == null) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, race.name());
            }
            ps.setObject(2, characterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update character race", e);
        }
    }

    public void updateDescription(UUID characterId, @Nullable String description) {
        String sql = "UPDATE player_characters SET description = ? WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (description == null) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, description);
            }
            ps.setObject(2, characterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update character description", e);
        }
    }

    public void updateLives(UUID characterId, int lives) {
        String sql = "UPDATE player_characters SET lives = ? WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, lives);
            ps.setObject(2, characterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update character lives", e);
        }
    }

    public void updateChatColor(UUID characterId, Color chatColor) {
        String sql = "UPDATE player_characters SET chat_color = ? WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, chatColor.getRGB());
            ps.setObject(2, characterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update chat color", e);
        }
    }

    public PlayerCharacter createCharacter(UUID playerId, String name) {
        String sql = """
        INSERT INTO player_characters (
            id, player_id, type, name, lives, chat_color
        ) VALUES (?, ?, ?::character_type, ?, ?, ?)
        RETURNING *
    """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, UUID.randomUUID());
            ps.setObject(2, Database.getLoretalePlayerRepository().getPlayerIdFromHytaleUser(playerId));
            ps.setString(3, CharacterType.Player.name());
            ps.setString(4, name);
            ps.setInt(5, 3); // default lives
            ps.setInt(6, Color.GRAY.getRGB());

            ResultSet rs = ps.executeQuery();
            rs.next();

            return mapCharacter(rs);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create character", e);
        }
    }
}
