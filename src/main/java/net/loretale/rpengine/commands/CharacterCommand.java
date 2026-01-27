package net.loretale.rpengine.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.loretale.rpengine.ui.pages.CharacterMenuPage;

import javax.annotation.Nonnull;
import java.awt.*;

public class CharacterCommand extends AbstractPlayerCommand {

    public CharacterCommand() {
        super("character", "Open character management UI");
        setPermissionGroup(GameMode.Adventure);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        if (playerRef.getReference() == null) return;

        Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());

        if (player == null) return;

        player.getPageManager().openCustomPage(ref, store, new CharacterMenuPage(player, playerRef));
    }
}