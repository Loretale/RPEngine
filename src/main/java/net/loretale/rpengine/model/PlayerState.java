package net.loretale.rpengine.model;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

public class PlayerState {
    private ChatChannel focusedChannel;
    private final Set<ChatChannel> joinedChannels = EnumSet.allOf(ChatChannel.class);
    private @Nullable PlayerCharacter character;
    private PlayerRef player;

    public PlayerState(PlayerRef player) {
        focusedChannel = ChatChannel.RP;
        this.player = player;
    }

    public PlayerRef getPlayer() {
        return player;
    }

    public ChatChannel getFocusedChannel() {
        return focusedChannel;
    }

    public void setFocusedChannel(ChatChannel channel) {
        joinedChannels.add(channel);
        focusedChannel = channel;
    }

    public Set<ChatChannel> getJoinedChannels() {
        return joinedChannels;
    }

    public void joinChannel(ChatChannel channel) {
        joinedChannels.add(channel);
    }

    public void leaveChannel(ChatChannel channel) {
        joinedChannels.remove(channel);
        if (focusedChannel == channel) {
            focusedChannel = ChatChannel.RP;
        }
    }

    public boolean isInChannel(ChatChannel channel) {
        return joinedChannels.contains(channel);
    }

    @Nullable
    public PlayerCharacter getCharacter() {
        return character;
    }

    public void setCharacter(PlayerCharacter character) {
        this.character = character;
    }
}
