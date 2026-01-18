package net.loretale.rpengine.repositories;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

public record ActiveBanInfo(String reason, @Nullable LocalDateTime end) {}
