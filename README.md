# FitCore — Gym Management System

**Java 21 · JavaFX 21 · MySQL · JDBC · Maven · MVC-inspired structure**

FitCore is a dark green/charcoal desktop app for local gym administration. It includes an administrator login, member directory, trainers, membership plans, plan enrollment, attendance and a payment ledger.

> **Scope:** University/portfolio demo for one gym on a local computer. It does **not** integrate with a card processor or bKash, offer customer self-service accounts, or provide a secure remote API. Payment methods are **recorded**, not charged. Do not distribute local database credentials with a public desktop application.

## Features

| Section | Features |
| --- | --- |
| Login | Administrator sign-in; salted PBKDF2-HMAC-SHA256 password hashes; no default password |
| Dashboard | Active member count, current subscriptions, today's check-ins, total recorded revenue |
| Members | Add, search, edit, mark inactive, view registration dates |
| Plans | Add name, duration in days and fee (BDT); seeded example plans |
| Trainers | Add trainer contact and specialty |
| Memberships | Enroll an active member, assign a plan, automatically record its initial payment |
| Attendance | Check in active members with a valid date-range membership; max one check-in/day |
| Payments | Review paid membership fees and manually record additional payments |

## 1. Install prerequisites

- JDK **21**: `java -version` and `javac -version` should both show 21.
- Apache Maven **3.9+**: `mvn -version` must succeed and should report Java 21.
- VS Code extensions: **Extension Pack for Java**, **Maven for Java**.
- MySQL Server 8.0+ and optionally MySQL Workbench.
- Internet access for the *first* Maven build, to download JavaFX and JDBC dependencies.

**Windows / your previous JDK path (example):**

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
mvn -version
```

If `mvn` is not recognized, download the **binary ZIP** from https://maven.apache.org/download.cgi, extract it, put `<MAVEN-FOLDER>\bin` on PATH, reopen VS Code, and verify `mvn -version`.

## 2. Open in VS Code

Extract the ZIP. Choose **File → Open Folder → FitCore-Gym**, specifically the folder with `pom.xml`. Open **Terminal → New Terminal** from this folder.

## 3. Initialize MySQL

In MySQL Workbench, execute the entire `database/fitcore_gym.sql` script. It creates `fitcore_gym_db`, 7 tables and four starter plans. No people or passwords are preloaded.

Check it with:

```sql
USE fitcore_gym_db;
SHOW TABLES;
SELECT * FROM plans;
```

Recommended: use a dedicated MySQL login rather than `root` (execute as MySQL admin, and **replace** the password):

```sql
CREATE USER IF NOT EXISTS 'fitcore_user'@'localhost'
  IDENTIFIED BY 'REPLACE_WITH_A_STRONG_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE ON fitcore_gym_db.* TO 'fitcore_user'@'localhost';
```

## 4. Configure DB credentials in PowerShell

In the **same terminal** from which you will run Maven, type:

```powershell
$env:FITCORE_DB_USER = 'fitcore_user'  # or your own local MySQL user
$env:FITCORE_DB_PASSWORD = 'YOUR_MYSQL_PASSWORD'
```

Connection URL defaults to `jdbc:mysql://localhost:3306/fitcore_gym_db?serverTimezone=UTC`. For a nonstandard local setup, optionally set `FITCORE_DB_URL`. Credentials are obtained from environment variables, never hard-coded or committed to Git.

## 5. Create your admin account once

From that same terminal, run:

```powershell
mvn clean compile
mvn -q exec:java -Dexec.mainClass=com.fitcore.tools.AdminSetup
```

Provide your full name, admin email and a password of at least 8 characters. A secure hash is stored in the database. If your terminal displays passwords as you type, avoid entering them in view of others.

## 6. Launch the gym GUI

```powershell
mvn javafx:run
```

Log in with the admin email and password you just created. Add a **member**, choose a **plan**, then enroll the member and record their **check-in**. Explore the payments ledger to confirm the membership fee was recorded.

### Common troubleshooting

- `mvn not recognized`: Maven's `bin` directory is not in Windows PATH; reopen VS Code after updating PATH.
- `Java version 22`: Ensure JAVA_HOME and your PATH select JDK 21 when using Maven.
- `Unknown database`: Execute the full SQL script in MySQL Workbench.
- `Access denied for user`: Recheck `FITCORE_DB_USER` / `FITCORE_DB_PASSWORD` and grants.
- `Communications link failure`: Start MySQL Server (Windows Services > MySQL80 or your installed service).
- `Duplicate entry` on attendance: the member has already checked in today.
- `Invalid email or password`: Run AdminSetup once, or check the credentials you created.

## Business rules and limitations

- Only administrators can log in; gym **members** are managed records, not system login accounts.
- Existing membership records are never deleted. Enrolling a member into a new plan marks their previous ACTIVE subscription as ENDED. If the new plan begins in the future, the member cannot check in until it starts.
- The plan end date is inclusive (`start + duration_days - 1`). The database server's date (`CURDATE()`) determines check-in eligibility.
- The `status='ACTIVE'` field on a subscription indicates whether it was superseded; an elapsed subscription can still retain this record status, while **current membership** requires today's date to be within its start/end dates.
- Enrolling assumes the customer paid the entire membership fee then and records it in the payment ledger. This does not process an actual payment.
- A member is allowed at most one attendance check-in per calendar date. Duplicate attempts generate an error.
- Payments and attendance are append-only in this GUI, preserving a simple audit trail. Existing plan fees are not edited; create a new plan for new pricing.
- No trainer scheduling, payroll, refunds, PDF invoices, or biometric devices are included in this starter version.

## Source structure

```text
FitCore-Gym/
├── pom.xml
├── .gitignore
├── .vscode/extensions.json
├── database/fitcore_gym.sql
├── README.md
└── src/main/
    ├── java/com/fitcore/
    │   ├── MainApp.java
    │   ├── config/Database.java
    │   ├── controller/LoginController.java
    │   ├── controller/DashboardController.java
    │   ├── dao/GymRepository.java
    │   ├── model/{Member,Trainer,Plan,Subscription,Attendance,Payment,Stats}.java
    │   ├── security/PasswordUtil.java
    │   ├── service/AuthService.java
    │   ├── tools/AdminSetup.java
    │   └── util/{Alerts,Currency,Navigation}.java
    └── resources/
        ├── fxml/{login,dashboard}.fxml
        └── css/dark.css
```

## Push to GitHub

From the project root (check `git status` first if you cloned an existing repo):

```powershell
git init
git branch -M main
git add .
git commit -m "Initial commit - FitCore Gym JavaFX"
git remote add origin https://github.com/YOUR_USERNAME/FitCore-Gym.git
git push -u origin main
```

Create an **empty** GitHub repository first (do not initialize a README), and never commit your password, `.env`, private SQL database dump, or credentials.
