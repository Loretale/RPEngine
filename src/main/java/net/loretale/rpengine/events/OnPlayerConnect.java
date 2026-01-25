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
    public static void onPlayerConnectEvent(PlayerConnectEvent event) {
        PlayerRef player = event.getPlayerRef();

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

        if (!Database.getLoretalePlayerRepository().exists(player.getUuid())
            && !Database.getLoretalePlayerRepository()
                .createPlayerFromAcceptedApplication(player.getUuid(), player.getUsername())) {
            player.getPacketHandler().disconnect("You are not whitelisted. Join us on discord https://discord.gg/FrKGmZf83V to apply.");
            return;
        }

        PlayerStateRepository.playerJoined(player);
    }
}
