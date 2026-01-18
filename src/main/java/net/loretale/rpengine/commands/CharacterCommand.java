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
import net.loretale.rpengine.Database;
import net.loretale.rpengine.model.Gender;
import net.loretale.rpengine.model.PlayerCharacter;
import net.loretale.rpengine.model.PlayerState;
import net.loretale.rpengine.model.Race;
import net.loretale.rpengine.repositories.PlayerStateRepository;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CharacterCommand extends AbstractPlayerCommand {

    public CharacterCommand() {
        super("character", "Character management command");
    }

    private final RequiredArg<String> actionArg =
            this.withRequiredArg("action", "create|list|current|switch|setname|setgender|setbirth|setrace|setdesc|setcolor", ArgTypes.STRING);

    private final OptionalArg<String> arg1 = this.withOptionalArg("arg1", "First argument", ArgTypes.STRING);

    @Override
    protected void execute(
            @Nonnull CommandContext commandContext,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        String action = actionArg.get(commandContext).toLowerCase();

        switch (action) {
            case "create" -> handleCreate(playerRef, commandContext);
            case "list" -> handleList(playerRef);
            case "current" -> handleCurrent(playerRef);
            case "switch" -> handleSwitch(playerRef, commandContext);
            case "setname" -> handleSetName(playerRef, commandContext);
            case "setgender" -> handleSetGender(playerRef, commandContext);
            case "setbirth" -> handleSetBirth(playerRef, commandContext);
            case "setrace" -> handleSetRace(playerRef, commandContext);
            case "setdesc" -> handleSetDesc(playerRef, commandContext);
            case "setcolor" -> handleSetColor(playerRef, commandContext);
            default -> playerRef.sendMessage(Message.raw("Invalid action").color(Color.RED));
        }
    }

    private void handleCreate(PlayerRef player, CommandContext ctx) {
        String name = arg1.get(ctx);
        if (name == null) {
            player.sendMessage(Message.raw("Usage: /character create <name>").color(Color.RED));
            return;
        }

        Database.getPlayerCharacterRepository().createCharacter(player.getUuid(), name);
        player.sendMessage(Message.raw("Character created: " + name).color(Color.GREEN));
    }

    private void handleList(PlayerRef player) {
        List<PlayerCharacter> characters = Database.getPlayerCharacterRepository()
                .getAllCharacters(player.getUuid());
        if (characters.isEmpty()) {
            player.sendMessage(Message.raw("No characters found").color(Color.RED));
            return;
        }

        StringBuilder sb = new StringBuilder("Your characters:\n");
        for (PlayerCharacter c : characters) {
            sb.append("- ").append(c.name).append(" (").append(c.id).append(")\n");
        }
        player.sendMessage(Message.raw(sb.toString()).color(Color.BLUE));
    }

    private void handleCurrent(PlayerRef player) {
        PlayerState state = PlayerStateRepository.getState(player.getUuid());
        if (state == null || state.getCharacter() == null) {
            player.sendMessage(Message.raw("No active character").color(Color.RED));
            return;
        }
        player.sendMessage(Message.raw("Current character: " + state.getCharacter().name).color(Color.BLUE));
    }

    private void handleSwitch(PlayerRef player, CommandContext ctx) {
        String name = arg1.get(ctx);
        if (name == null) {
            player.sendMessage(Message.raw("Usage: /character switch <name>").color(Color.RED));
            return;
        }

        List<PlayerCharacter> chars = Database.getPlayerCharacterRepository().getAllCharacters(player.getUuid());
        Optional<PlayerCharacter> match = chars.stream()
                .filter(c -> c.name.equalsIgnoreCase(name))
                .findFirst();

        if (match.isEmpty()) {
            player.sendMessage(Message.raw("Character not found").color(Color.RED));
            return;
        }

        PlayerCharacter c = match.get();
        Database.getLoretalePlayerRepository().setActiveCharacter(player.getUuid(), c.id);
        Database.getLoretalePlayerRepository().updateLastChangedCharacter(player.getUuid());
        PlayerStateRepository.getState(player.getUuid()).setCharacter(c);

        player.sendMessage(Message.raw("Switched to: " + c.name).color(Color.GREEN));
    }

    private void handleSetName(PlayerRef player, CommandContext ctx) {
        String name = arg1.get(ctx);

        PlayerState state = PlayerStateRepository.getState(player.getUuid());
        if (state == null || state.getCharacter() == null) {
            player.sendMessage(Message.raw("No active character.").color(Color.RED));
            return;
        }

        if (name == null) {
            player.sendMessage(Message.raw("Usage: /character setname <name>").color(Color.RED));
            return;
        }

        Database.getPlayerCharacterRepository().updateName(state.getCharacter().id, name);
        if (state.getCharacter() != null) state.getCharacter().name = name;
        player.sendMessage(Message.raw("Name updated").color(Color.GREEN));
    }

    private void handleSetGender(PlayerRef player, CommandContext ctx) {
        String gender = arg1.get(ctx);

        PlayerState state = PlayerStateRepository.getState(player.getUuid());
        if (state == null || state.getCharacter() == null) {
            player.sendMessage(Message.raw("No active character.").color(Color.RED));
            return;
        }

        if (gender == null) {
            player.sendMessage(Message.raw("Usage: /character setgender <gender>").color(Color.RED));
            return;
        }

        Database.getPlayerCharacterRepository().updateGender(state.getCharacter().id, Gender.valueOf(gender));
        if (state.getCharacter() != null) state.getCharacter().gender = Gender.valueOf(gender);
        player.sendMessage(Message.raw("Gender updated").color(Color.GREEN));
    }


    private void handleSetBirth(PlayerRef player, CommandContext ctx) {
        String birth = arg1.get(ctx);

        PlayerState state = PlayerStateRepository.getState(player.getUuid());
        if (state == null || state.getCharacter() == null) {
            player.sendMessage(Message.raw("No active character.").color(Color.RED));
            return;
        }

        if (birth == null) {
            player.sendMessage(Message.raw("Usage: /character setbirth <birthDate>").color(Color.RED));
            return;
        }

        Database.getPlayerCharacterRepository().updateBirthDate(state.getCharacter().id, Long.parseLong(birth));
        if (state.getCharacter() != null) state.getCharacter().birthDate = Long.parseLong(birth);
        player.sendMessage(Message.raw("Birth date updated").color(Color.GREEN));
    }

    private void handleSetRace(PlayerRef player, CommandContext ctx) {
        String race = arg1.get(ctx);

        PlayerState state = PlayerStateRepository.getState(player.getUuid());
        if (state == null || state.getCharacter() == null) {
            player.sendMessage(Message.raw("No active character.").color(Color.RED));
            return;
        }

        if (race == null) {
            player.sendMessage(Message.raw("Usage: /character setrace <race>").color(Color.RED));
            return;
        }

        Database.getPlayerCharacterRepository().updateRace(state.getCharacter().id, Race.valueOf(race));
        if (state.getCharacter() != null) state.getCharacter().race = Race.valueOf(race);
        player.sendMessage(Message.raw("Race updated").color(Color.GREEN));
    }

    private void handleSetDesc(PlayerRef player, CommandContext ctx) {
        String desc = arg1.get(ctx);

        PlayerState state = PlayerStateRepository.getState(player.getUuid());
        if (state == null || state.getCharacter() == null) {
            player.sendMessage(Message.raw("No active character.").color(Color.RED));
            return;
        }

        if (desc == null) {
            player.sendMessage(Message.raw("Usage: /character setdesc <desc>").color(Color.RED));
            return;
        }

        Database.getPlayerCharacterRepository().updateDescription(state.getCharacter().id, desc);
        if (state.getCharacter() != null) state.getCharacter().description = desc;
        player.sendMessage(Message.raw("Description updated").color(Color.GREEN));
    }

    private void handleSetColor(PlayerRef player, CommandContext ctx) {
        String colorStr = arg1.get(ctx);

        PlayerState state = PlayerStateRepository.getState(player.getUuid());
        if (state == null || state.getCharacter() == null) {
            player.sendMessage(Message.raw("No active character.").color(Color.RED));
            return;
        }

        if (colorStr == null) {
            player.sendMessage(Message.raw("Usage: /character setcolor <color>").color(Color.RED));
            return;
        }

        Color parsed;
        try {
            parsed = parseColor(colorStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Message.raw("Invalid color format. Use #RRGGBB or R,G,B or color name.").color(Color.RED));
            return;
        }

        Database.getPlayerCharacterRepository().updateChatColor(state.getCharacter().id, parsed);
        if (state.getCharacter() != null) state.getCharacter().chatColor = parsed;
        player.sendMessage(Message.raw("Color updated").color(Color.GREEN));
    }

    private Color parseColor(String input) {
        input = input.trim().toLowerCase();

        // 1) Hex format (#RRGGBB or RRGGBB)
        if (input.startsWith("#")) input = input.substring(1);
        if (input.matches("^[0-9a-f]{6}$")) {
            return new Color(Integer.parseInt(input, 16));
        }
        if (input.matches("^[0-9a-f]{8}$")) {
            return new Color((int) Long.parseLong(input, 16), true);
        }

        // 2) RGB format (R,G,B)
        if (input.matches("^\\d{1,3},\\d{1,3},\\d{1,3}$")) {
            String[] parts = input.split(",");
            int r = Integer.parseInt(parts[0]);
            int g = Integer.parseInt(parts[1]);
            int b = Integer.parseInt(parts[2]);
            return new Color(r, g, b);
        }

        // 3) Color name (red, blue, etc.)
        try {
            Field field = Color.class.getField(input);
            return (Color) field.get(null);
        } catch (Exception ignored) {}

        throw new IllegalArgumentException("Invalid color format");
    }
}
