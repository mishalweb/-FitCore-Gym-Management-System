package com.fitcore.controller;

import com.fitcore.dao.GymRepository;
import com.fitcore.model.*;
import com.fitcore.util.Alerts;
import com.fitcore.util.Currency;
import com.fitcore.util.Navigation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

public class DashboardController {
    private final GymRepository repo = new GymRepository();
    private List<Member> memberCache = List.of();

    @FXML private Label adminLabel, membersCount, membershipCount, todayCount, revenueCount;
    @FXML private Label statusLabel;
    @FXML private Button logoutButton;

    @FXML private TableView<Member> membersTable;
    @FXML private TextField memberSearch, memberName, memberEmail, memberPhone;
    @FXML private ComboBox<String> memberStatus;

    @FXML private TableView<Trainer> trainersTable;
    @FXML private TextField trainerName, trainerSpecialty, trainerPhone;

    @FXML private TableView<Plan> plansTable;
    @FXML private TextField planName, planDuration, planPrice;

    @FXML private TableView<Subscription> subscriptionsTable;
    @FXML private ComboBox<Member> enrollmentMember;
    @FXML private ComboBox<Plan> enrollmentPlan;
    @FXML private ComboBox<String> enrollmentMethod;
    @FXML private DatePicker enrollmentStart;

    @FXML private TableView<Attendance> attendanceTable;
    @FXML private ComboBox<Member> checkinMember;

    @FXML private TableView<Payment> paymentsTable;
    @FXML private ComboBox<Member> paymentMember;
    @FXML private TextField paymentAmount, paymentNote;
    @FXML private ComboBox<String> paymentMethod;

    @FXML private void initialize() {
        column(membersTable, "ID", m -> String.valueOf(m.id()), 55);
        column(membersTable, "Full name", Member::name, 210);
        column(membersTable, "Email", m -> value(m.email()), 240);
        column(membersTable, "Phone", Member::phone, 145);
        column(membersTable, "Status", Member::status, 110);
        column(membersTable, "Joined", m -> m.joinedOn().toString(), 115);

        column(trainersTable, "ID", t -> String.valueOf(t.id()), 60);
        column(trainersTable, "Full name", Trainer::name, 230);
        column(trainersTable, "Specialty", Trainer::specialty, 240);
        column(trainersTable, "Phone", Trainer::phone, 160);

        column(plansTable, "ID", p -> String.valueOf(p.id()), 60);
        column(plansTable, "Plan", Plan::name, 220);
        column(plansTable, "Duration (days)", p -> String.valueOf(p.durationDays()), 150);
        column(plansTable, "Fee (BDT)", p -> Currency.bdt(p.price()), 150);

        column(subscriptionsTable, "ID", s -> String.valueOf(s.id()), 60);
        column(subscriptionsTable, "Member", Subscription::member, 190);
        column(subscriptionsTable, "Plan", Subscription::plan, 145);
        column(subscriptionsTable, "Starts", s -> s.start().toString(), 120);
        column(subscriptionsTable, "Ends", s -> s.end().toString(), 120);
        column(subscriptionsTable, "Record status", Subscription::status, 130);
        column(subscriptionsTable, "Fee", s -> Currency.bdt(s.price()), 130);

        column(attendanceTable, "ID", a -> String.valueOf(a.id()), 75);
        column(attendanceTable, "Member", Attendance::member, 240);
        column(attendanceTable, "Date", a -> a.day().toString(), 150);
        column(attendanceTable, "Checked in at", a -> format(a.checkedInAt()), 220);

        column(paymentsTable, "ID", p -> String.valueOf(p.id()), 65);
        column(paymentsTable, "Member", Payment::member, 200);
        column(paymentsTable, "Amount", p -> Currency.bdt(p.amount()), 145);
        column(paymentsTable, "Method", Payment::method, 100);
        column(paymentsTable, "Paid at", p -> format(p.paidAt()), 190);
        column(paymentsTable, "Note", p -> value(p.note()), 230);

        memberStatus.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        memberStatus.setValue("ACTIVE");
        enrollmentMethod.setItems(FXCollections.observableArrayList("CASH", "BKASH", "CARD"));
        enrollmentMethod.setValue("CASH");
        paymentMethod.setItems(FXCollections.observableArrayList("CASH", "BKASH", "CARD"));
        paymentMethod.setValue("CASH");
        enrollmentStart.setValue(LocalDate.now());
        memberSearch.textProperty().addListener((obs, before, after) -> filterMembers());
        membersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, m) -> {
            if (m != null) {
                memberName.setText(m.name()); memberEmail.setText(value(m.email()));
                memberPhone.setText(m.phone()); memberStatus.setValue(m.status());
            }
        });
        refreshAll();
    }

    public void setAdminName(String name) { adminLabel.setText("Welcome, " + name); }

    private static <T> void column(TableView<T> table, String title, Function<T, String> getter, int width) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> new SimpleStringProperty(value(getter.apply(c.getValue()))));
        col.setPrefWidth(width);
        table.getColumns().add(col);
    }
    private static String value(String value) { return value == null ? "" : value; }
    private static String format(java.time.LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm"));
    }
    private static String required(TextField field, String label) {
        String text = field.getText().strip();
        if (text.isBlank()) throw new IllegalArgumentException(label + " is required.");
        return text;
    }
    private static <T> T selected(ComboBox<T> box, String label) {
        if (box.getValue() == null) throw new IllegalArgumentException("Select " + label + ".");
        return box.getValue();
    }
    private void handle(ThrowingAction action, String success) {
        try { action.run(); refreshAll(); statusLabel.setText(success); }
        catch (Exception e) { Alerts.error(e.getMessage()); }
    }
    private interface ThrowingAction { void run() throws Exception; }

    @FXML private void refreshAll() {
        try {
            memberCache = repo.members();
            filterMembers();
            List<Trainer> trainers = repo.trainers();
            trainersTable.setItems(FXCollections.observableArrayList(trainers));
            List<Plan> plans = repo.plans();
            plansTable.setItems(FXCollections.observableArrayList(plans));
            subscriptionsTable.setItems(FXCollections.observableArrayList(repo.subscriptions()));
            attendanceTable.setItems(FXCollections.observableArrayList(repo.attendance()));
            paymentsTable.setItems(FXCollections.observableArrayList(repo.payments()));
            enrollmentMember.setItems(FXCollections.observableArrayList(memberCache));
            checkinMember.setItems(FXCollections.observableArrayList(memberCache));
            paymentMember.setItems(FXCollections.observableArrayList(memberCache));
            enrollmentPlan.setItems(FXCollections.observableArrayList(plans));
            Stats stats = repo.stats();
            membersCount.setText(String.valueOf(stats.members()));
            membershipCount.setText(String.valueOf(stats.activeSubscriptions()));
            todayCount.setText(String.valueOf(stats.todayCheckins()));
            revenueCount.setText(Currency.bdt(stats.revenue()));
            statusLabel.setText("Records refreshed successfully.");
        } catch (SQLException e) {
            statusLabel.setText("Database error — check MySQL and FITCORE_DB_* environment variables.");
            Alerts.error("Database connection failed: " + e.getMessage());
        }
    }
    private void filterMembers() {
        String q = memberSearch.getText() == null ? "" : memberSearch.getText().strip().toLowerCase();
        membersTable.setItems(FXCollections.observableArrayList(memberCache.stream()
            .filter(m -> m.name().toLowerCase().contains(q) || value(m.email()).toLowerCase().contains(q)
                || m.phone().contains(q)).toList()));
    }
    @FXML private void addMember() {
        handle(() -> {
            String name = required(memberName,"Member name");
            String phone = required(memberPhone,"Phone number");
            repo.addMember(name, memberEmail.getText().strip(), phone);
            memberName.clear(); memberEmail.clear(); memberPhone.clear();
        },"Member added.");
    }
    @FXML private void updateMember() {
        handle(() -> {
            Member member = membersTable.getSelectionModel().getSelectedItem();
            if (member == null) throw new IllegalArgumentException("Select a member row before updating.");
            repo.updateMember(member.id(), required(memberName,"Member name"), memberEmail.getText().strip(),
                    required(memberPhone,"Phone number"),selected(memberStatus,"status"));
        },"Member updated.");
    }
    @FXML private void addTrainer() {
        handle(() -> {
            repo.addTrainer(required(trainerName,"Trainer name"), required(trainerSpecialty,"Specialty"),
                required(trainerPhone,"Trainer phone"));
            trainerName.clear(); trainerSpecialty.clear(); trainerPhone.clear();
        },"Trainer added.");
    }
    @FXML private void addPlan() {
        handle(() -> {
            String name=required(planName,"Plan name");
            int days=Integer.parseInt(required(planDuration,"Duration"));
            BigDecimal fee=new BigDecimal(required(planPrice,"Price"));
            if (days<=0 || fee.signum()<0) throw new IllegalArgumentException("Duration must be positive and fee cannot be negative.");
            repo.addPlan(name, days, fee);
            planName.clear(); planDuration.clear(); planPrice.clear();
        },"Plan created.");
    }
    @FXML private void enrollMember() {
        handle(() -> {
            Member member=selected(enrollmentMember,"a member");
            Plan plan=selected(enrollmentPlan,"a plan");
            String method=selected(enrollmentMethod,"payment method");
            LocalDate start=enrollmentStart.getValue();
            if (start==null) throw new IllegalArgumentException("Choose a start date.");
            if (start.isBefore(LocalDate.now())) throw new IllegalArgumentException("Choose today or a future start date.");
            repo.enroll(member.id(), plan.id(), start, method);
        },"Membership enrolled and initial fee recorded as paid.");
    }
    @FXML private void checkIn() {
        handle(() -> {
            Member member=selected(checkinMember,"a member");
            repo.checkIn(member.id());
        },"Attendance registered for today.");
    }
    @FXML private void recordPayment() {
        handle(() -> {
            Member member=selected(paymentMember,"a member");
            BigDecimal amount=new BigDecimal(required(paymentAmount,"Amount"));
            if (amount.signum()<=0) throw new IllegalArgumentException("Payment amount must be positive.");
            repo.addPayment(member.id(),amount,selected(paymentMethod,"payment method"),paymentNote.getText());
            paymentAmount.clear(); paymentNote.clear();
        },"Additional payment recorded.");
    }
    @FXML private void logout() {
        try { Navigation.login((Stage)logoutButton.getScene().getWindow()); }
        catch (Exception e) { Alerts.error(e.getMessage()); }
    }
}
