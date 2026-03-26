/*
 * NFC Attendance Pro — Arduino Feedback Module
 *
 * Wiring:
 *   Red LED  : Anode → Pin 7 → 220Ω resistor → GND (cathode)
 *   Buzzer   : Positive (+) → Pin 8 | Negative (–) → GND
 *
 * Serial protocol (9600 baud):
 *   Java sends 'S' → blink red LED + play buzzer (successful scan)
 */

const int LED_PIN    = 7;
const int BUZZER_PIN = 8;

void setup() {
  Serial.begin(9600);
  pinMode(LED_PIN,    OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  digitalWrite(LED_PIN,    LOW);
  digitalWrite(BUZZER_PIN, LOW);
}

void loop() {
  if (Serial.available() > 0) {
    char cmd = (char)Serial.read();

    if (cmd == 'S') {
      // Blink red LED and beep buzzer — 2 short pulses
      for (int i = 0; i < 2; i++) {
        digitalWrite(LED_PIN, HIGH);
        tone(BUZZER_PIN, 1000);   // 1 kHz tone
        delay(150);
        digitalWrite(LED_PIN, LOW);
        noTone(BUZZER_PIN);
        delay(100);
      }
    }
  }
}
