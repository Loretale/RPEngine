package net.loretale.rpengine.model;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

public class PlayerCharacter {
    public UUID id;

    public CharacterType type;

    public String name;
    @Nullable
    public Gender gender;
    public long birthDate;
    @Nullable
    public Race race;
    @Nullable
    public String description;

    public int lives;

    public Color chatColor;
}

