package net.loretale.rpengine.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import net.loretale.rpengine.Database;
import net.loretale.rpengine.repositories.ActiveBanInfo;
import net.loretale.rpengine.repositories.PlayerStateRepository;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

public class OnPlayerConnect {
    private static final Set<String> TEST_PLAYERS = Set.of("muse", "_luca", "laplus", "pank");

    public static void onPlayerConnectEvent(PlayerConnectEvent event) {
        PlayerRef player = event.getPlayerRef();

        if (!TEST_PLAYERS.contains(player.getUsername().toLowerCase())) {
            player.getPacketHandler().disconnect("We're currently still under construction. Join us on discord https://discord.gg/FrKGmZf83V to apply to play when we launch.");
            return;
        }

        Optional<ActiveBanInfo> activeBan = Database.getLoretalePlayerRepository().getActiveBanReason(player.getUuid());

        if (activeBan.isPresent()) {
            ActiveBanInfo activeBanInfo = activeBan.get();

            String message = "You are banned. Reason: " + activeBanInfo.reason() + " | ";

            if (activeBanInfo.end() == null) {
                message += "This is a permanent ban.";
            } else {
                message += "Ban lasts until " + activeBanInfo.end().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ".";
            }

            player.getPacketHandler()
                    .disconnect(message);

            return;
        }

        if (!Database.getLoretalePlayerRepository().exists(player.getUuid())) {
            player.getPacketHandler().disconnect("You are not whitelisted. Join us on discord https://discord.gg/FrKGmZf83V to apply.");
            return;
        }

        PlayerStateRepository.playerJoined(player);
    }
}
