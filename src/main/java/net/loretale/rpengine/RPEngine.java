package net.loretale.rpengine;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import net.loretale.rpengine.commands.CharacterCommand;
import net.loretale.rpengine.config.DatabaseConfig;
import net.loretale.rpengine.events.OnPlayerChat;
import net.loretale.rpengine.events.OnPlayerConnect;

import javax.annotation.Nonnull;
import java.sql.SQLException;

public class RPEngine extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static RPEngine instance;

    private static Config<DatabaseConfig> DbConfig;

    public static RPEngine getInstance() {
        return instance;
    }

    public RPEngine(@Nonnull JavaPluginInit init) throws SQLException {
        super(init);

        instance = this;

        DbConfig = this.withConfig("Database", DatabaseConfig.CODEC);

        LOGGER.atInfo().log("Starting " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());

        DbConfig.save();

        try {
            Database.init(
                    DbConfig.get().getUrl(),
                    DbConfig.get().getUsername(),
                    DbConfig.get().getPassword()
            );
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't load DB", e);
        }

        registerEvents();
        registerCommands();
    }

    private void registerEvents() {
        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, OnPlayerConnect::onPlayerConnectEvent);
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, OnPlayerChat::onPlayerChat);
    }

    private void registerCommands() {
        this.getCommandRegistry().registerCommand(new CharacterCommand());
    }
}