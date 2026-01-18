package net.loretale.rpengine.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.loretale.rpengine.model.ChatChannel;
import net.loretale.rpengine.model.PlayerState;
import net.loretale.rpengine.repositories.PlayerStateRepository;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ChatCommand extends AbstractPlayerCommand {

    public ChatCommand() {
        super("chat", "Chat channel commands");
    }

    private final RequiredArg<String> actionArg =
            this.withRequiredArg("action", "join|leave|focus|list", ArgTypes.STRING);

    private final OptionalArg<String> channelArg =
            this.withOptionalArg("channel", "Channel name", ArgTypes.STRING);

    @Override
    protected void execute(
            @Nonnull CommandContext commandContext,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        String action = actionArg.get(commandContext).toLowerCase();
        PlayerState state = PlayerStateRepository.getState(playerRef.getUuid());

        if (state == null) {
            playerRef.sendMessage(Message.raw("Player state not loaded").color(Color.RED));
            return;
        }

        switch (action) {
            case "join" -> handleJoin(playerRef, state, commandContext);
            case "leave" -> handleLeave(playerRef, state, commandContext);
            case "focus" -> handleFocus(playerRef, state, commandContext);
            case "list" -> handleList(playerRef, state);
            default -> playerRef.sendMessage(Message.raw("Invalid action").color(Color.RED));
        }
    }

    private void handleJoin(PlayerRef player, PlayerState state, CommandContext ctx) {
        String channelName = channelArg.get(ctx).toUpperCase();

        try {
            ChatChannel channel = ChatChannel.valueOf(channelName);
            state.joinChannel(channel);
            player.sendMessage(Message.raw("Joined " + channel.name()).color(Color.GREEN));
        } catch (IllegalArgumentException e) {
            player.sendMessage(Message.raw("Channel does not exist").color(Color.RED));
        }
    }


    private void handleLeave(PlayerRef player, PlayerState state, CommandContext ctx) {
        String channelName = channelArg.get(ctx).toUpperCase();

        try {
            ChatChannel channel = ChatChannel.valueOf(channelName);
            if (channel.canLeave) {
                state.leaveChannel(channel);
                player.sendMessage(Message.raw("Left " + channel.name()).color(Color.YELLOW));
            } else {
                player.sendMessage(Message.raw("You cannot leave this channel.").color(Color.RED));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(Message.raw("Channel does not exist. Available channels: " + getChannelsList()).color(Color.RED));
        }
    }

    private void handleFocus(PlayerRef player, PlayerState state, CommandContext ctx) {
        String channelName = channelArg.get(ctx).toUpperCase();

        try {
            ChatChannel channel = ChatChannel.valueOf(channelName);
            state.setFocusedChannel(channel);
            player.sendMessage(Message.raw("Focused " + channel.name()).color("#8371eb"));
        } catch (IllegalArgumentException e) {
            player.sendMessage(Message.raw("Channel does not exist. Available channels: " + getChannelsList()).color(Color.RED));
        }
    }

    private void handleList(PlayerRef player, PlayerState state) {
        player.sendMessage(Message.raw("Joined: " + state.getJoinedChannels()).color(Color.GRAY));
        player.sendMessage(Message.raw("Focused: " + state.getFocusedChannel()).color(Color.GRAY));
    }

    private String getChannelsList() {
        return Arrays.stream(ChatChannel.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}