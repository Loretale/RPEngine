package net.loretale.rpengine.model;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

public class Infraction {
    public InfractionType type;

    public LocalDateTime start;
    @Nullable
    public LocalDateTime end;

    public String reason;
}
