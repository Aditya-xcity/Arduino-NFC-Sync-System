// Question: Add I2C LCD display to NFC Attendance Feedback System without modifying existing logic
/*
Name - ADITYA BHARDWAJ
Section - D2
Roll No - 07
Course – B TECH
Branch – CSE
*/

#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// LCD setup (change 0x27 to 0x3F if needed)
LiquidCrystal_I2C lcd(0x27, 16, 2);

const int LED_PIN    = 7;
const int BUZZER_PIN = 8;

void setup() {
  Serial.begin(9600);

  pinMode(LED_PIN,    OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);

  digitalWrite(LED_PIN,    LOW);
  digitalWrite(BUZZER_PIN, LOW);

  // LCD initialization
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("System Ready");
}

void loop() {
  if (Serial.available() > 0) {
    char cmd = (char)Serial.read();

    if (cmd == 'S') {
      // Existing logic (UNCHANGED)
      for (int i = 0; i < 2; i++) {
        digitalWrite(LED_PIN, HIGH);
        tone(BUZZER_PIN, 1000);
        delay(150);
        digitalWrite(LED_PIN, LOW);
        noTone(BUZZER_PIN);
        delay(100);
      }

      // New LCD Feedback
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Attendance OK");
      lcd.setCursor(0, 1);
      lcd.print("Scan Success");

      delay(1500);

      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("System Ready");
    }
  }
}