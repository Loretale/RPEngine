package net.loretale.rpengine.events;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import net.loretale.rpengine.model.ChatChannel;
import net.loretale.rpengine.model.PlayerCharacter;
import net.loretale.rpengine.model.PlayerState;
import net.loretale.rpengine.repositories.PlayerStateRepository;

import java.awt.*;
import java.util.UUID;

public class OnPlayerChat {
    public static void onPlayerChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        UUID userId = sender.getUuid();
        String content = event.getContent();
        PlayerState state = PlayerStateRepository.getState(userId);

        event.setCancelled(true);

        if (state == null) {
            sender.sendMessage(
                    Message.raw("Something went wrong. Please try relogging.")
                            .color(Color.RED)
            );
            return;
        }

        PlayerCharacter character = state.getCharacter();

        if (character == null) {
            sender.sendMessage(
                    Message.raw("You have no character. Use /character [Name] to make one.")
                            .color(Color.RED)
            );
            return;
        }


        ChatChannel channel = state.getFocusedChannel();
        String displayName = channel.isRoleplayChannel
                ? character.name
                : sender.getUsername();

        Message message;

        if (channel.isRoleplayChannel) {
            // Parse roleplay, get color, etc
            message = Message.join(
                    channel.prefix,
                    Message.raw(" "),
                    Message.raw(displayName + ": ").color(Color.GRAY),
                    parseRoleplayMessage(content, character.chatColor)
            );

        } else {
            message = Message.join(
                    channel.prefix,
                    Message.raw(" "),
                    Message.raw(displayName + ": ").color(Color.GRAY),
                    Message.raw(content).color(Color.WHITE)
            );
        }

        broadCastMessage(sender, channel, message);
    }

    private static void broadCastMessage(PlayerRef sender, ChatChannel channel, Message message) {
        Vector3d senderPosition = sender.getTransform().getPosition();

        UUID worldId = sender.getWorldUuid();

        if (worldId == null) {
            sender.sendMessage(Message.raw("Something went wrong. Please try relogging.")
                    .color(Color.RED));
            return;
        }

        World world = Universe.get().getWorld(sender.getWorldUuid());

        if (world == null) {
            sender.sendMessage(Message.raw("Something went wrong. Please try relogging.")
                    .color(Color.RED));
            return;
        }

        for (PlayerRef recipient : world.getPlayerRefs()) {
            Vector3d recipientPosition = recipient.getTransform().getPosition();
            PlayerState recipientState = PlayerStateRepository.getState(recipient.getUuid());

            if (recipientState == null) continue;
            if (channel.canLeave && !recipientState.isInChannel(channel)) return;

            if (channel.isGlobal() ||
                senderPosition.distanceSquaredTo(recipientPosition) <= channel.range * channel.range) {
                recipient.sendMessage(message);
            }
        }
    }

    private static Message parseRoleplayMessage(String input, Color baseColor) {
        Message result = Message.empty();
        baseColor = baseColor == null ? Color.GRAY : baseColor;

        boolean inQuotes = false;
        boolean italic = false;
        boolean bold = false;
        StringBuilder buffer = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c == '"') {
                if (inQuotes) buffer.append(c);
                flushBuffer(result, buffer, baseColor, inQuotes, italic, bold);
                if (!inQuotes) buffer.append(c);
                inQuotes = !inQuotes;
                continue;
            }
            if (c == '_') {
                flushBuffer(result, buffer, baseColor, inQuotes, italic, bold);
                italic = !italic;
                continue;
            }
            if (c == '*') {
                flushBuffer(result, buffer, baseColor, inQuotes, italic, bold);
                bold = !bold;
                continue;
            }
            buffer.append(c);
        }
        flushBuffer(result, buffer, baseColor, inQuotes, italic, bold);
        return result;
    }

    private static void flushBuffer(Message parent, StringBuilder buffer, Color baseColor, boolean inQuotes, boolean italic, boolean bold) {
        if (buffer.isEmpty()) return;

        Message part = Message.raw(buffer.toString());
        part = part.color(inQuotes ? Color.WHITE : baseColor);

        if (italic) part = part.italic(true);
        if (bold) part = part.bold(true);

        parent.insert(part);
        buffer.setLength(0);
    }
}
