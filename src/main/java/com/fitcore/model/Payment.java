package com.fitcore.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record Payment(int id, String member, BigDecimal amount, String method,
                      LocalDateTime paidAt, String note) { }
