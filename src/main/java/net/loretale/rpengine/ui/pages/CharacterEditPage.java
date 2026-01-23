package net.loretale.rpengine.ui.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.loretale.rpengine.Database;
import net.loretale.rpengine.model.PlayerCharacter;
import net.loretale.rpengine.repositories.PlayerStateRepository;

import javax.annotation.Nonnull;

public class CharacterEditPage
        extends InteractiveCustomUIPage<CharacterEditPage.CharacterEditEventData> {

    private static final String LAYOUT = "Pages/CharacterEditPage.ui";
    private final PlayerCharacter character;

    public CharacterEditPage(PlayerCharacter character, PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, CharacterEditEventData.CODEC);
        this.character = character;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commands,
            @Nonnull UIEventBuilder events,
            @Nonnull Store<EntityStore> store
    ) {
        commands.append(LAYOUT);

        String desc = character.description == null ? "" : character.description;

        commands.set("#NameInput.Value", character.name);
        commands.set("#DescInput.Value", desc);

        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SaveBtn",
                EventData.of("Action", "save")
                        .append("@Name", "#NameInput.Value")
                        .append("@Description", "#DescInput.Value")
        );

        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CancelBtn",
                EventData.of("Action", "close")
        );
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull CharacterEditEventData data
    ) {
        if ("save".equals(data.action)) {
            Database.getPlayerCharacterRepository()
                    .updateName(character.id, data.name);
            Database.getPlayerCharacterRepository()
                    .updateDescription(character.id, data.description);

            PlayerCharacter playerCharacter = PlayerStateRepository
                    .getState(playerRef.getUuid())
                    .getCharacter();

            if (playerCharacter != null && playerCharacter.id.toString().equals(character.id.toString())) {
                playerCharacter.name = data.name;
                playerCharacter.description = data.description;
            }

            playerRef.sendMessage(
                    Message.raw("Succeeded editing character.")
            );

            close();
        } else {
            close();
        }
    }

    public static class CharacterEditEventData {
        public static final BuilderCodec<CharacterEditEventData> CODEC =
                BuilderCodec.builder(CharacterEditEventData.class, CharacterEditEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (d, v) -> d.action = v, d -> d.action)
                        .add()
                        .append(new KeyedCodec<>("@Name", Codec.STRING),
                                (d, v) -> d.name = v, d -> d.name)
                        .add()
                        .append(new KeyedCodec<>("@Description", Codec.STRING),
                                (d, v) -> d.description = v, d -> d.description)
                        .add()
                        .build();

        public String action;
        public String name;
        public String description;
    }
}

