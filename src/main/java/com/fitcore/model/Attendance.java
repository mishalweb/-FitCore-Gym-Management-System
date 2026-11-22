package com.fitcore.model;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record Attendance(int id, String member, LocalDate day, LocalDateTime checkedInAt) { }
