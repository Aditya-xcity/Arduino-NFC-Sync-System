package com.nfc.attendance.nfc;

import com.fazecast.jSerialComm.SerialPort;

/**
 * ArduinoController — Singleton that communicates with the Arduino Uno
 * over a serial / COM port.
 *
 * On each successful NFC scan the Java app sends 'S' and the Arduino
 * responds by blinking the red LED and sounding the buzzer.
 *
 * Usage:
 *   ArduinoController.getInstance().triggerScan();
 *
 * The COM port is auto-detected; override by calling
 *   ArduinoController.getInstance().setPortName("COM4");
 * before the first triggerScan() call.
 */
public class ArduinoController {

    private static final byte[] SIGNAL_SCAN = new byte[]{'S'};

    private static ArduinoController instance;

    private SerialPort serialPort;
    private String portName;   // null = auto-detect
    private boolean connected = false;

    public static synchronized ArduinoController getInstance() {
        if (instance == null) {
            instance = new ArduinoController();
        }
        return instance;
    }

    private ArduinoController() {
        // lazy-connect on first use
    }

    /** Override the COM port name before first use (e.g. "COM3", "COM5"). */
    public void setPortName(String portName) {
        this.portName = portName;
        // force reconnect with new port
        disconnect();
    }

    /**
     * Send signal 'S' to the Arduino (scan feedback).
     * Uses jSerialComm's native writeBytes() — more reliable than
     * getOutputStream() which can return a different instance per call
     * and silently drop unflushed data.
     */
    public void triggerScan() {
        if (!connected) {
            connect();
        }
        if (connected && serialPort != null) {
            int written = serialPort.writeBytes(SIGNAL_SCAN, SIGNAL_SCAN.length);
            if (written == SIGNAL_SCAN.length) {
                System.out.println("[Arduino] Signal 'S' sent successfully.");
            } else {
                System.err.println("[Arduino] writeBytes returned " + written + " — expected 1. Reconnecting next attempt.");
                connected = false;
            }
        } else {
            System.err.println("[Arduino] triggerScan skipped — not connected.");
        }
    }

    // ─────────────────── internal helpers ───────────────────

    private void connect() {
        SerialPort port = resolvePort();
        if (port == null) {
            System.err.println("[Arduino] No suitable COM port found. " +
                    "Plug in the Arduino and restart, or call setPortName(\"COMx\").");
            return;
        }

        port.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        // Use non-blocking write with a 2-second timeout so a stalled port never freezes the scan thread
        port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 2000);

        if (port.openPort()) {
            serialPort = port;
            connected  = true;
            System.out.println("[Arduino] Opened port: " + port.getSystemPortName()
                    + " — waiting 2 s for Arduino bootloader...");
            // Arduino Uno resets when DTR is asserted on port open.
            // Must wait for its bootloader to finish before sending any bytes.
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            System.out.println("[Arduino] Ready on " + port.getSystemPortName());
        } else {
            System.err.println("[Arduino] Could not open port: " + port.getSystemPortName()
                    + ". Is another program (Arduino IDE serial monitor) holding it?");
        }
    }

    private SerialPort resolvePort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) return null;

        // 1. User-specified port name
        if (portName != null) {
            for (SerialPort p : ports) {
                if (p.getSystemPortName().equalsIgnoreCase(portName)) return p;
            }
            System.err.println("[Arduino] Specified port '" + portName + "' not found.");
            return null;
        }

        // 2. Auto-detect: prefer a port whose description mentions Arduino / CH340 / USB-SERIAL
        for (SerialPort p : ports) {
            String desc = p.getPortDescription().toLowerCase();
            if (desc.contains("arduino") || desc.contains("ch340")
                    || desc.contains("usb-serial") || desc.contains("usb serial")) {
                return p;
            }
        }

        // 3. Fall back to first available port
        return ports[0];
    }

    /** Release the serial port (call on application exit). */
    public void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("[Arduino] Port closed.");
        }
        serialPort = null;
        connected  = false;
    }

    public boolean isConnected() {
        return connected;
    }
}
