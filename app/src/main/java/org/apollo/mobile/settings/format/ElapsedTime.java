package org.apollo.mobile.settings.format;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public final class ElapsedTime {
    public enum Unit {
        NOW,
        MINUTES,
        HOURS,
        DAYS,
        MONTHS,
        YEARS
    }

    private final Unit unit;
    private final int amount;

    private ElapsedTime(Unit unit, int amount) {
        this.unit = unit;
        this.amount = amount;
    }

    public Unit getUnit() {
        return unit;
    }

    public int getAmount() {
        return amount;
    }

    public static LocalDateTime parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String text = value.trim();
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return OffsetDateTime.parse(text).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    public static ElapsedTime between(String value, LocalDateTime now) {
        LocalDateTime moment = parse(value);
        if (moment == null || now == null) {
            return null;
        }
        long seconds = Duration.between(moment, now).getSeconds();
        if (seconds < 60) {
            return new ElapsedTime(Unit.NOW, 0);
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return new ElapsedTime(Unit.MINUTES, (int) minutes);
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return new ElapsedTime(Unit.HOURS, (int) hours);
        }
        long days = hours / 24;
        if (days < 30) {
            return new ElapsedTime(Unit.DAYS, (int) days);
        }
        if (days < 365) {
            return new ElapsedTime(Unit.MONTHS, (int) (days / 30));
        }
        return new ElapsedTime(Unit.YEARS, (int) (days / 365));
    }
}
