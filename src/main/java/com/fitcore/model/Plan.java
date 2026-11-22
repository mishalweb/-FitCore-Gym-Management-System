package com.fitcore.model;
import java.math.BigDecimal;
public record Plan(int id, String name, int durationDays, BigDecimal price) {
    @Override public String toString() { return name + " (" + durationDays + " days)"; }
}
