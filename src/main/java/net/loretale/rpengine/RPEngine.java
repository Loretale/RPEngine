package net.loretale.rpengine;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import net.loretale.rpengine.commands.CharacterCommand;
import net.loretale.rpengine.commands.ChatCommand;
import net.loretale.rpengine.config.DatabaseConfig;
import net.loretale.rpengine.events.OnPlayerChat;
import net.loretale.rpengine.events.OnPlayerConnect;
import net.loretale.rpengine.model.OnlinePlayerInfo;
import net.loretale.rpengine.repositories.PlayerStateRepository;

import javax.annotation.Nonnull;
import java.sql.SQLException;

public class RPEngine extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static RPEngine instance;

    public static RPEngine getInstance() {
        return instance;
    }

    public RPEngine(@Nonnull JavaPluginInit init) throws SQLException {
        super(init);

        instance = this;

        //Config<DatabaseConfig> databaseConfig = this.withConfig("Database", DatabaseConfig.CODEC);
        Database.init(
                "jdbc:postgresql://localhost:5432/loretale", //databaseConfig.get().getUrl(),
                "dev", //databaseConfig.get().getUsername(),
                "devpassword" //databaseConfig.get().getPassword()
        );

        LOGGER.atInfo().log("Starting " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());

        registerEvents();
        registerCommands();
    }

    private void registerEvents() {
        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, OnPlayerConnect::onPlayerConnectEvent);
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, OnPlayerChat::onPlayerChat);
    }

    private void registerCommands() {
        this.getCommandRegistry().registerCommand(new CharacterCommand());
        this.getCommandRegistry().registerCommand(new ChatCommand());
    }
}