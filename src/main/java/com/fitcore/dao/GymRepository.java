package com.fitcore.dao;

import com.fitcore.config.Database;
import com.fitcore.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class GymRepository {
    public List<Member> members() throws SQLException {
        List<Member> out = new ArrayList<>();
        try (Connection c = Database.connect();
             PreparedStatement s = c.prepareStatement(
                "SELECT member_id, full_name, email, phone, status, joined_on FROM members ORDER BY member_id DESC");
             ResultSet r = s.executeQuery()) {
            while (r.next()) out.add(new Member(r.getInt(1), r.getString(2), r.getString(3),
                r.getString(4), r.getString(5), r.getDate(6).toLocalDate()));
        }
        return out;
    }
    public void addMember(String name, String email, String phone) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement s = c.prepareStatement(
                 "INSERT INTO members(full_name,email,phone) VALUES(?,?,?)")) {
            s.setString(1, name); s.setString(2, nullable(email)); s.setString(3, phone);
            s.executeUpdate();
        }
    }
    public void updateMember(int id, String name, String email, String phone, String status) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement s = c.prepareStatement(
                 "UPDATE members SET full_name=?,email=?,phone=?,status=? WHERE member_id=?")) {
            s.setString(1, name); s.setString(2, nullable(email)); s.setString(3, phone);
            s.setString(4, status); s.setInt(5, id);
            s.executeUpdate();
        }
    }
    public List<Trainer> trainers() throws SQLException {
        List<Trainer> out = new ArrayList<>();
        try (Connection c = Database.connect();
             PreparedStatement s = c.prepareStatement(
                 "SELECT trainer_id,full_name,specialty,phone FROM trainers ORDER BY trainer_id DESC");
             ResultSet r = s.executeQuery()) {
            while (r.next()) out.add(new Trainer(r.getInt(1),r.getString(2),r.getString(3),r.getString(4)));
        }
        return out;
    }
    public void addTrainer(String name, String specialty, String phone) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement s = c.prepareStatement(
                 "INSERT INTO trainers(full_name,specialty,phone) VALUES(?,?,?)")) {
            s.setString(1,name); s.setString(2,specialty); s.setString(3,phone);
            s.executeUpdate();
        }
    }
    public List<Plan> plans() throws SQLException {
        List<Plan> out = new ArrayList<>();
        try (Connection c = Database.connect();
             PreparedStatement s = c.prepareStatement(
                 "SELECT plan_id,plan_name,duration_days,price FROM plans ORDER BY duration_days");
             ResultSet r = s.executeQuery()) {
            while (r.next()) out.add(new Plan(r.getInt(1),r.getString(2),r.getInt(3),r.getBigDecimal(4)));
        }
        return out;
    }
    public void addPlan(String name, int days, BigDecimal price) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement s = c.prepareStatement(
                 "INSERT INTO plans(plan_name,duration_days,price) VALUES(?,?,?)")) {
            s.setString(1,name); s.setInt(2,days); s.setBigDecimal(3,price);
            s.executeUpdate();
        }
    }
    public List<Subscription> subscriptions() throws SQLException {
        List<Subscription> out = new ArrayList<>();
        String q = """
            SELECT s.subscription_id,m.full_name,p.plan_name,s.start_date,s.end_date,
                   s.status,s.fee_at_signup
            FROM subscriptions s JOIN members m ON m.member_id=s.member_id
            JOIN plans p ON p.plan_id=s.plan_id ORDER BY s.subscription_id DESC
            """;
        try (Connection c = Database.connect();
             PreparedStatement s = c.prepareStatement(q);
             ResultSet r = s.executeQuery()) {
            while (r.next()) out.add(new Subscription(r.getInt(1),r.getString(2),r.getString(3),
                r.getDate(4).toLocalDate(),r.getDate(5).toLocalDate(),r.getString(6),r.getBigDecimal(7)));
        }
        return out;
    }
    public void enroll(int memberId, int planId, LocalDate start, String paymentMethod) throws SQLException {
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                String memberStatus;
                try (PreparedStatement s = c.prepareStatement(
                    "SELECT status FROM members WHERE member_id=? FOR UPDATE")) {
                    s.setInt(1,memberId);
                    try (ResultSet r=s.executeQuery()) {
                        if (!r.next()) throw new SQLException("Member not found.");
                        memberStatus=r.getString(1);
                    }
                }
                if (!"ACTIVE".equals(memberStatus)) throw new SQLException("Member is inactive.");
                int duration;
                BigDecimal price;
                try (PreparedStatement s = c.prepareStatement(
                    "SELECT duration_days,price FROM plans WHERE plan_id=?")) {
                    s.setInt(1,planId);
                    try (ResultSet r=s.executeQuery()) {
                        if (!r.next()) throw new SQLException("Plan not found.");
                        duration=r.getInt(1); price=r.getBigDecimal(2);
                    }
                }
                LocalDate end=start.plusDays(duration-1);
                try (PreparedStatement s = c.prepareStatement(
                    "UPDATE subscriptions SET status='ENDED' WHERE member_id=? AND status='ACTIVE'")) {
                    s.setInt(1,memberId); s.executeUpdate();
                }
                int subscriptionId;
                try (PreparedStatement s = c.prepareStatement(
                    "INSERT INTO subscriptions(member_id,plan_id,start_date,end_date,fee_at_signup) VALUES(?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                    s.setInt(1,memberId); s.setInt(2,planId); s.setDate(3,Date.valueOf(start));
                    s.setDate(4,Date.valueOf(end)); s.setBigDecimal(5,price);
                    s.executeUpdate();
                    try (ResultSet r=s.getGeneratedKeys()) {
                        if (!r.next()) throw new SQLException("No subscription ID returned.");
                        subscriptionId=r.getInt(1);
                    }
                }
                if (price.signum()>0) {
                    try (PreparedStatement s=c.prepareStatement(
                        "INSERT INTO payments(member_id,subscription_id,amount,method,note) VALUES(?,?,?,?,?)")) {
                        s.setInt(1,memberId);s.setInt(2,subscriptionId);s.setBigDecimal(3,price);
                        s.setString(4,paymentMethod);s.setString(5,"Membership enrollment");s.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback(); throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
    public List<Attendance> attendance() throws SQLException {
        List<Attendance> out=new ArrayList<>();
        String q="""
            SELECT a.attendance_id,m.full_name,a.attendance_date,a.checked_in_at
            FROM attendance a JOIN members m ON m.member_id=a.member_id
            ORDER BY a.checked_in_at DESC LIMIT 250
            """;
        try (Connection c=Database.connect(); PreparedStatement s=c.prepareStatement(q);
             ResultSet r=s.executeQuery()) {
            while (r.next()) out.add(new Attendance(r.getInt(1),r.getString(2),
                r.getDate(3).toLocalDate(),r.getTimestamp(4).toLocalDateTime()));
        }
        return out;
    }
    public void checkIn(int memberId) throws SQLException {
        try (Connection c=Database.connect()) {
            // MySQL server's CURDATE() determines the attendance date.
            String q="""
                INSERT INTO attendance(member_id,attendance_date)
                SELECT m.member_id,CURDATE() FROM members m
                WHERE m.member_id=? AND m.status='ACTIVE'
                  AND EXISTS(SELECT 1 FROM subscriptions s WHERE s.member_id=m.member_id
                      AND s.status='ACTIVE' AND CURDATE() BETWEEN s.start_date AND s.end_date)
                """;
            try (PreparedStatement s=c.prepareStatement(q)) {
                s.setInt(1,memberId);
                if (s.executeUpdate()==0)
                    throw new SQLException("Member must be active with a valid current membership.");
            }
        }
    }
    public List<Payment> payments() throws SQLException {
        List<Payment> out=new ArrayList<>();
        String q="""
            SELECT p.payment_id,m.full_name,p.amount,p.method,p.paid_at,p.note
            FROM payments p JOIN members m ON m.member_id=p.member_id
            ORDER BY p.payment_id DESC LIMIT 250
            """;
        try (Connection c=Database.connect();PreparedStatement s=c.prepareStatement(q);
             ResultSet r=s.executeQuery()) {
            while(r.next()) out.add(new Payment(r.getInt(1),r.getString(2),r.getBigDecimal(3),
                r.getString(4),r.getTimestamp(5).toLocalDateTime(),r.getString(6)));
        }
        return out;
    }
    public void addPayment(int memberId,BigDecimal amount,String method,String note) throws SQLException {
        try (Connection c=Database.connect(); PreparedStatement s=c.prepareStatement(
            "INSERT INTO payments(member_id,amount,method,note) VALUES(?,?,?,?)")) {
            s.setInt(1,memberId);s.setBigDecimal(2,amount);s.setString(3,method);s.setString(4,nullable(note));
            s.executeUpdate();
        }
    }
    public Stats stats() throws SQLException {
        String q="""
            SELECT
            (SELECT COUNT(*) FROM members WHERE status='ACTIVE'),
            (SELECT COUNT(*) FROM subscriptions WHERE status='ACTIVE'
                 AND CURDATE() BETWEEN start_date AND end_date),
            (SELECT COUNT(*) FROM attendance WHERE attendance_date=CURDATE()),
            (SELECT COALESCE(SUM(amount),0) FROM payments)
            """;
        try (Connection c=Database.connect();PreparedStatement s=c.prepareStatement(q);
             ResultSet r=s.executeQuery()) {
            r.next();return new Stats(r.getInt(1),r.getInt(2),r.getInt(3),r.getBigDecimal(4));
        }
    }
    private static String nullable(String s) { return s==null || s.isBlank()?null:s.strip(); }
}
