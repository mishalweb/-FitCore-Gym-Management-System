package com.fitcore.model;
import java.time.LocalDate;
public record Member(int id, String name, String email, String phone, String status, LocalDate joinedOn) {
    @Override public String toString() { return name + " (#" + id + ")"; }
}
