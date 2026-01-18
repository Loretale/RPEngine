package net.loretale.rpengine.model;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class LoretalePlayer {
    public ArrayList<HytaleUser> hytaleUsers;
    public ArrayList<String> discordIds;

    public LocalDateTime acceptedOn;
    @Nullable
    public LocalDateTime lastSeenInGame;

    @Nullable
    public LocalDateTime lastChangedCharacter;
    @Nullable
    public PlayerCharacter activeCharacter;
    public ArrayList<PlayerCharacter> characters;

    public ArrayList<Infraction> infractions;
}
