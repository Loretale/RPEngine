package net.loretale.rpengine.model;

import com.hypixel.hytale.server.core.Message;

import java.awt.*;

public enum ChatChannel {
    RP(20, Message.raw("[RP]").color("#167500"), true, false),
    LOOC(20, Message.raw("[LOOC]").color(Color.GRAY), false, false),
    GLOBAL(-1, Message.raw("[!]").color(Color.GRAY), false, true);


    public final int range;
    public final Message prefix;
    public final boolean isRoleplayChannel;
    public final boolean canLeave;

    ChatChannel(int range, Message prefix, boolean isRoleplayChannel, boolean canLeave) {
        this.range = range;
        this.prefix = prefix;
        this.isRoleplayChannel = isRoleplayChannel;
        this.canLeave = canLeave;
    }

    public boolean isGlobal() {
        return range < 0;
    }
}
