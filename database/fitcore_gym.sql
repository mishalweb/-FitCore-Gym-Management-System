CREATE DATABASE IF NOT EXISTS fitcore_gym_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitcore_gym_db;

CREATE TABLE IF NOT EXISTS admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(30) NOT NULL,
    status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    joined_on DATE NOT NULL DEFAULT (CURRENT_DATE),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS trainers (
    trainer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS plans (
    plan_id INT AUTO_INCREMENT PRIMARY KEY,
    plan_name VARCHAR(100) NOT NULL UNIQUE,
    duration_days INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    CHECK (duration_days > 0),
    CHECK (price >= 0)
);

CREATE TABLE IF NOT EXISTS subscriptions (
    subscription_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    plan_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE','ENDED') NOT NULL DEFAULT 'ACTIVE',
    fee_at_signup DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(member_id),
    FOREIGN KEY (plan_id) REFERENCES plans(plan_id),
    CHECK (end_date >= start_date)
);

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    checked_in_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(member_id),
    UNIQUE KEY unique_daily_checkin (member_id, attendance_date)
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    subscription_id INT NULL,
    amount DECIMAL(10,2) NOT NULL,
    method ENUM('CASH','BKASH','CARD') NOT NULL,
    note VARCHAR(255),
    paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(member_id),
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(subscription_id),
    CHECK (amount > 0)
);

INSERT IGNORE INTO plans (plan_id, plan_name, duration_days, price) VALUES
(1, 'Monthly', 30, 2000.00),
(2, 'Quarterly', 90, 5500.00),
(3, 'Half-Yearly', 180, 10000.00),
(4, 'Annual', 365, 18000.00);
