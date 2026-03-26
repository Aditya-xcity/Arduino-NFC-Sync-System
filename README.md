# 🚀 NFC Attendance System

A smart, automated attendance system built using **NFC technology + Arduino + JavaFX**. This project eliminates manual attendance headaches and replaces them with a fast, secure, and real-time tracking system.

---

## ✨ What This Project Does

Instead of calling out names or signing sheets, students simply **tap their NFC cards** — and boom, attendance is recorded instantly with timestamps.

It’s designed for:

* Colleges
* Schools
* Labs
* Events

Basically anywhere attendance matters.

---

## 🔥 Key Features

### 📡 NFC Integration

* Arduino-powered NFC reader
* Instant scan detection
* LED + buzzer feedback for confirmation

### ⏱️ Automated Attendance

* Real-time attendance logging
* Timestamp-based tracking
* No manual intervention needed

### 👥 User Management

* Separate roles: **Admin & Student**
* Secure login system
* Password hashing for safety

### 📊 Dashboard System

* Clean JavaFX UI
* Role-based access
* Live attendance updates

### 📅 Session Handling

* Create multiple attendance sessions
* Manage classes/events efficiently

### 📄 PDF Reports

* Export attendance reports instantly
* Organized and printable format

### 🗄️ Database Support

* SQLite-based storage
* Lightweight and auto-managed

---

## 🧠 Tech Stack

| Layer      | Technology       |
| ---------- | ---------------- |
| Backend    | Java (JDK 11+)   |
| UI         | JavaFX           |
| Build Tool | Maven            |
| Database   | SQLite           |
| Hardware   | Arduino + NFC    |
| Scripting  | Python (testing) |

---

## 📁 Project Structure

```
nfc-attendance/
├── src/
│   ├── main/
│   │   ├── java/com/nfc/attendance/
│   │   │   ├── controller/      # UI controllers
│   │   │   ├── database/        # DB operations
│   │   │   ├── model/           # Data models
│   │   │   ├── nfc/             # NFC handling
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── service/         # Business logic
│   │   │   ├── report/          # PDF generation
│   │   │   └── util/            # Helpers
│   │   └── resources/
│   │       ├── fxml/            # UI layouts
│   │       ├── css/             # Styling
│   │       └── StudentData.json
│   └── test/
├── arduino/
│   └── nfc_buzzer_led/
├── attendance_data/
├── data/
│   ├── photos/
│   └── reports/
├── pom.xml
└── arduino_tkinter_test.py
```

---

## ⚙️ Requirements

Before running, make sure you have:

* Java JDK 11+
* Maven 3.6+
* Arduino IDE
* Python 3.7+

No need to manually install SQLite — it’s handled automatically.

---

## 🛠️ Setup Guide

### 💻 Run the Java App

```bash
cd nfc-attendance
mvn clean install
mvn javafx:run
```

---

### 🔌 Setup Arduino

1. Open Arduino IDE
2. Load:

   ```
   arduino/nfc_buzzer_led/nfc_buzzer_led.ino
   ```
3. Select board + COM port
4. Upload code

---

### 🗃️ Database Setup

* Auto-created on first run
* Import students via:

  ```
  StudentData.json
  ```

---

## 🚀 How to Use

### 🆕 First Time

1. Start the app
2. Login as admin
3. Upload student data
4. Connect NFC hardware

### 📅 Daily Workflow

1. Open app
2. Create/select session
3. Students tap NFC cards
4. Monitor live dashboard
5. Export reports if needed

---

## ⚙️ Configuration Files

* `pom.xml` → Dependencies & build
* `StudentData.json` → Student records
* `dark.css` → UI styling

---

## 🧩 Database Design

Main tables:

* **students** → Student info
* **admins** → Admin accounts
* **sessions** → Attendance sessions
* **attendance** → Records

---

## 🛠️ Troubleshooting

| Problem              | Fix                       |
| -------------------- | ------------------------- |
| Arduino not detected | Check COM port            |
| NFC not scanning     | Test with Python script   |
| DB errors            | Ensure folder permissions |
| JavaFX issues        | Verify JDK version        |

---

## 🤝 Contributing

Want to improve this project?

* Follow clean Java coding practices
* Update tests
* Test with real hardware
* Keep README updated

---

## 📜 License

Add your license here.

---

## 💬 Support

If something breaks or confuses you, reach out to the dev team or debug step-by-step using logs.

---

## ⚡ Final Thought

This project is a solid mix of **hardware + software + real-world problem solving** — exactly the kind of thing that stands out in projects and interviews.

If you build on top of this (like adding cloud sync or mobile app), it becomes even more powerful 🚀
