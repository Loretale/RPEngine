package net.loretale.rpengine.model;

import com.hypixel.hytale.server.core.Message;

import java.awt.*;

public enum ChatChannel {
    W(2, Message.raw("[W]").color("#61baff"), true, false, false),
    WOOC(2, Message.raw("[WOOC]").color("#61baff"), false, false, false),
    Q(8, Message.raw("[Q]").color("#67c4c7"), true, false, false),
    QOOC(8, Message.raw("[QOOC]").color("#67c4c7"), false, false, false),
    RP(20, Message.raw("[RP]").color("#167500"), true, false, false),
    LOOC(20, Message.raw("[LOOC]").color(Color.GRAY), false, false, false),
    S(40, Message.raw("[S]").color("#ba1e1e"), true, false, false),
    SOOC(40, Message.raw("[SOOC]").color("#ba1e1e"), false, false, false),
    LEB(100, Message.raw("[EVENT]").color("#b5b200"), true, false, true),
    GEB(-1, Message.raw("[EVENT]").color("#b5b200"), true, false, true),
    GLOBAL(-1, Message.raw("[!]").color(Color.GRAY), false, true, false);

    public final int range;
    public final Message prefix;
    public final boolean isRoleplayChannel;
    public final boolean canLeave;
    public final boolean staffOnly;

    ChatChannel(int range, Message prefix, boolean isRoleplayChannel, boolean canLeave, boolean staffOnly) {
        this.range = range;
        this.prefix = prefix;
        this.isRoleplayChannel = isRoleplayChannel;
        this.canLeave = canLeave;
        this.staffOnly = staffOnly;
    }

    public boolean isGlobal() {
        return range < 0;
    }
}
