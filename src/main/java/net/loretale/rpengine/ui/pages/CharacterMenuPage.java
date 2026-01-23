package net.loretale.rpengine.ui.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.loretale.rpengine.Database;
import net.loretale.rpengine.model.PlayerCharacter;
import net.loretale.rpengine.model.PlayerState;
import net.loretale.rpengine.repositories.PlayerStateRepository;

import javax.annotation.Nonnull;
import java.util.List;

public class CharacterMenuPage
        extends InteractiveCustomUIPage<CharacterMenuPage.CharacterMenuEventData> {

    private static final String LAYOUT = "Pages/CharacterMenuPage.ui";

    private final List<PlayerCharacter> characters;
    private final PlayerState state;
    private final Player player;

    public CharacterMenuPage(Player player, PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, CharacterMenuEventData.CODEC);
        this.characters = Database.getPlayerCharacterRepository()
                .getAllCharacters(playerRef.getUuid());
        this.state = PlayerStateRepository.getState(playerRef.getUuid());
        this.player = player;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commands,
            @Nonnull UIEventBuilder events,
            @Nonnull Store<EntityStore> store
    ) {
        commands.append(LAYOUT);

        commands.clear("#CharacterList");

        PlayerCharacter currentCharacter = state.getCharacter();

        for (int i = 0; i < characters.size(); i++) {
            int charIndex = characters.size() - 1 - i;
            PlayerCharacter c = characters.get(charIndex);

            UICommandBuilder row = commands.append("#CharacterList", "Pages/CharacterRow.ui");

            if (currentCharacter != null && c.id.toString().equals(currentCharacter.id.toString())) {
                row.set("#CharacterList[" + i + "] #SwitchBtn.Disabled", true);
            }

            row.set("#CharacterList[" + i + "] #CharacterName.Text", c.name);

            events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#CharacterList[" + i + "] #SwitchBtn",
                    EventData.of("Action", "switch")
                            .append("Index", String.valueOf(charIndex))
            );

            events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#CharacterList[" + i + "] #EditBtn",
                    EventData.of("Action", "edit")
                            .append("Index", String.valueOf(charIndex))
            );
        }

        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CreateBtn",
                EventData.of("Action", "create")
        );

        events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseBtn",
                EventData.of("Action", "close")
        );
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull CharacterMenuEventData data
    ) {
        switch (data.action) {
            case "switch" -> switchCharacter(Integer.parseInt(data.index));
            case "create" ->
                    player.getPageManager().openCustomPage(
                            ref, store, new CharacterCreatePage(playerRef)
                    );
            case "edit" ->
                    player.getPageManager().openCustomPage(
                            ref, store, new CharacterEditPage(characters.get(Integer.parseInt(data.index)), playerRef)
                    );
            case "close" -> close();
        }
    }

    private void switchCharacter(int index) {
        PlayerCharacter c = characters.get(index);
        Database.getLoretalePlayerRepository()
                .setActiveCharacter(playerRef.getUuid(), c.id);
        Database.getLoretalePlayerRepository()
                .updateLastChangedCharacter(playerRef.getUuid());
        state.setCharacter(c);
        close();
    }

    public static class CharacterMenuEventData {
        public static final BuilderCodec<CharacterMenuEventData> CODEC =
                BuilderCodec.builder(CharacterMenuEventData.class, CharacterMenuEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (d, v) -> d.action = v, d -> d.action)
                        .add()
                        .append(new KeyedCodec<>("Index", Codec.STRING),
                                (d, v) -> d.index = v, d -> d.index)
                        .add()
                        .build();

        public String action;
        public String index;
    }
}

