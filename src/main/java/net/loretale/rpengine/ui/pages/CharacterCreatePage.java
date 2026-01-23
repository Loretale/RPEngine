package net.loretale.rpengine.ui.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.loretale.rpengine.Database;
import net.loretale.rpengine.model.Gender;
import net.loretale.rpengine.model.PlayerCharacter;
import net.loretale.rpengine.model.PlayerState;
import net.loretale.rpengine.model.Race;
import net.loretale.rpengine.repositories.PlayerStateRepository;

import javax.annotation.Nonnull;
import java.util.UUID;

public class CharacterCreatePage
        extends InteractiveCustomUIPage<CharacterCreatePage.CharacterCreateEventData> {

    private static final String LAYOUT = "Pages/CharacterCreatePage.ui";

    public CharacterCreatePage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, CharacterCreateEventData.CODEC);
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commands,
            @Nonnull UIEventBuilder events,
            @Nonnull Store<EntityStore> store
    ) {
        commands.append(LAYOUT);

        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CreateBtn",
                EventData.of("Action", "create")
                        .append("@Name", "#NameInput.Value")
        );

        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CancelBtn",
                EventData.of("Action", "cancel")
                        .append("@Name", "#NameInput.Value")
        );
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull CharacterCreateEventData data
    ) {
        super.handleDataEvent(ref, store, data);

        if (data.action == null) {
            sendUpdate();
            return;
        }

        switch (data.action) {
           case "create" -> createCharacter(ref, store, data);
           case "cancel" -> close();
        }
    }

    private void createCharacter(
            Ref<EntityStore> ref,
            Store<EntityStore> store,
            CharacterCreateEventData data
    ) {

        if (data.name == null || data.name.isBlank()) {
            close();
            return;
        }

        UUID playerId = playerRef.getUuid();

        PlayerCharacter character = Database.getPlayerCharacterRepository()
                        .createCharacter(playerId, data.name);

        Database.getLoretalePlayerRepository()
                .setActiveCharacter(playerId, character.id);

        PlayerState state = PlayerStateRepository.getState(playerId);
        state.setCharacter(character);

        close();
    }

    public static class CharacterCreateEventData {
        public static final BuilderCodec<CharacterCreateEventData> CODEC =
                BuilderCodec.builder(CharacterCreateEventData.class, CharacterCreateEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (d, v) -> d.action = v, d -> d.action)
                        .add()
                        .append(new KeyedCodec<>("@Name", Codec.STRING),
                                (d, v) -> d.name = v, d -> d.name)
                        .add()
                        .build();

        public String action;
        public String name;
        public Gender gender;
        public Race race;
    }
}

