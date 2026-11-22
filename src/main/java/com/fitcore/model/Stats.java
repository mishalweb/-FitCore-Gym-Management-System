package com.fitcore.model;
import java.math.BigDecimal;
public record Stats(int members, int activeSubscriptions, int todayCheckins, BigDecimal revenue) { }
