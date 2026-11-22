package com.fitcore.model;
import java.math.BigDecimal;
import java.time.LocalDate;
public record Subscription(int id, String member, String plan, LocalDate start, LocalDate end,
                           String status, BigDecimal price) { }
