package net.loretale.rpengine.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V1_PlayersAndCharacters implements Migration{
    @Override
    public int version() {
        return 1;
    }

    @Override
    public void apply(Connection connection) throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.execute("""
                CREATE TYPE character_type AS ENUM ('Player', 'Event');
            """);

            s.execute("""
                CREATE TYPE gender AS ENUM ('Male', 'Female', 'None', 'Other')
            """);

            s.execute("""
                CREATE TYPE infraction_type AS ENUM ('Ban', 'Mute');
            """);

            s.execute("""
                CREATE TYPE race AS ENUM ('Human');
            """);

            s.execute("""
                CREATE EXTENSION IF NOT EXISTS pgcrypto;
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS loretale_players (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    accepted_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    last_seen_in_game TIMESTAMP,
                    last_changed_character TIMESTAMP,
                    active_character_id UUID
                );
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS discord_ids (
                    id SERIAL PRIMARY KEY,
                    player_id UUID NOT NULL,
                    discord_id TEXT NOT NULL
                );
            """);

            s.execute("""
                ALTER TABLE discord_ids
                ADD CONSTRAINT fk_discord_player
                FOREIGN KEY (player_id)
                REFERENCES loretale_players(id)
                ON DELETE CASCADE;
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS hytale_users (
                    id UUID PRIMARY KEY,
                    player_id UUID NOT NULL,
                    name TEXT NOT NULL
                );
            """);

            s.execute("""
                ALTER TABLE hytale_users
                ADD CONSTRAINT fk_hytale_user_player
                FOREIGN KEY (player_id)
                REFERENCES loretale_players(id)
                ON DELETE CASCADE;
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS hytale_user_name_history (
                    id SERIAL PRIMARY KEY,
                    hytale_user_id UUID NOT NULL,
                    name TEXT NOT NULL
                );
            """);

            s.execute("""
                ALTER TABLE hytale_user_name_history
                ADD CONSTRAINT fk_hytale_user_history
                FOREIGN KEY (hytale_user_id)
                REFERENCES hytale_users(id)
                ON DELETE CASCADE;
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS player_characters (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    player_id UUID,
                    type character_type NOT NULL,
                    name TEXT NOT NULL,
                    gender gender,
                    birth_date BIGINT,
                    race race,
                    description TEXT,
                    lives INTEGER NOT NULL,
                    chat_color INTEGER NOT NULL
                );
            """);

            s.execute("""
                ALTER TABLE player_characters
                ADD CONSTRAINT fk_character_player
                FOREIGN KEY (player_id)
                REFERENCES loretale_players(id)
                ON DELETE CASCADE;
            """);

            s.execute("""
                ALTER TABLE loretale_players
                ADD CONSTRAINT fk_active_character
                FOREIGN KEY (active_character_id)
                REFERENCES player_characters(id);
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS infractions (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    player_id UUID NOT NULL,
                    type infraction_type NOT NULL,
                    start TIMESTAMP NOT NULL,
                    "end" TIMESTAMP,
                    reason TEXT NOT NULL
                );
            """);

            s.execute("""
                ALTER TABLE infractions
                ADD CONSTRAINT fk_infraction_player
                FOREIGN KEY (player_id)
                REFERENCES loretale_players(id)
                ON DELETE CASCADE;
            """);
        }
    }
}
