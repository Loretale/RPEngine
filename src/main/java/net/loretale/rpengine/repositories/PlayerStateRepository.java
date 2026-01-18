package net.loretale.rpengine.repositories;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import net.loretale.rpengine.Database;
import net.loretale.rpengine.model.PlayerState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateRepository {
    private static final Map<UUID, PlayerState> onlinePlayers = new ConcurrentHashMap<>();

    public static void playerJoined(PlayerRef player) {
        PlayerState state = new PlayerState(player);

        Database.getPlayerCharacterRepository()
                .getActiveCharacter(player.getUuid())
                .ifPresent(state::setCharacter);

        onlinePlayers.put(player.getUuid(), state);
    }

    public static void playerLeft(UUID playerId) {
        onlinePlayers.remove(playerId);
    }

    public static PlayerState getState(UUID playerId) {
        return onlinePlayers.get(playerId);
    }
}
