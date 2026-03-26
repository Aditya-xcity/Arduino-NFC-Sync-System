import threading
import time
import tkinter as tk
from tkinter import messagebox, ttk

import serial
from serial.tools import list_ports


class ArduinoTesterApp:
    def __init__(self, root: tk.Tk) -> None:
        self.root = root
        self.root.title("Arduino LED + Buzzer Test")
        self.root.geometry("460x260")
        self.root.resizable(False, False)

        self.serial_connection = None

        self.port_var = tk.StringVar()
        self.status_var = tk.StringVar(value="Select a COM port and connect.")

        self._build_ui()
        self.refresh_ports()
        self.root.protocol("WM_DELETE_WINDOW", self.on_close)

    def _build_ui(self) -> None:
        container = ttk.Frame(self.root, padding=16)
        container.pack(fill="both", expand=True)

        title = ttk.Label(container, text="Arduino Hardware Test", font=("Segoe UI", 16, "bold"))
        title.pack(anchor="w", pady=(0, 12))

        info = ttk.Label(
            container,
            text="Use this button to send the same serial signal used by the NFC app.\n"
                 "Your Arduino sketch must already be uploaded and listening on 9600 baud.",
            justify="left",
        )
        info.pack(anchor="w", pady=(0, 12))

        port_frame = ttk.Frame(container)
        port_frame.pack(fill="x", pady=(0, 12))

        ttk.Label(port_frame, text="COM Port:").pack(side="left")

        self.port_combo = ttk.Combobox(port_frame, textvariable=self.port_var, state="readonly", width=18)
        self.port_combo.pack(side="left", padx=(8, 8))

        ttk.Button(port_frame, text="Refresh", command=self.refresh_ports).pack(side="left", padx=(0, 8))
        self.connect_button = ttk.Button(port_frame, text="Connect", command=self.toggle_connection)
        self.connect_button.pack(side="left")

        test_frame = ttk.Frame(container)
        test_frame.pack(fill="x", pady=(8, 12))

        self.test_button = ttk.Button(
            test_frame,
            text="Test LED + Buzzer",
            command=self.trigger_test,
            state="disabled",
        )
        self.test_button.pack(fill="x")

        status_title = ttk.Label(container, text="Status", font=("Segoe UI", 10, "bold"))
        status_title.pack(anchor="w", pady=(12, 4))

        status_label = ttk.Label(
            container,
            textvariable=self.status_var,
            relief="solid",
            padding=10,
            justify="left",
        )
        status_label.pack(fill="x")

    def refresh_ports(self) -> None:
        ports = [port.device for port in list_ports.comports()]
        self.port_combo["values"] = ports
        if ports:
            if self.port_var.get() not in ports:
                self.port_var.set(ports[0])
            self.status_var.set("COM ports loaded. Connect to the Arduino board.")
        else:
            self.port_var.set("")
            self.status_var.set("No COM ports found. Check USB cable and board connection.")

    def toggle_connection(self) -> None:
        if self.serial_connection and self.serial_connection.is_open:
            self.disconnect()
            return

        port_name = self.port_var.get().strip()
        if not port_name:
            messagebox.showwarning("No Port", "Select a COM port first.")
            return

        try:
            self.serial_connection = serial.Serial(port_name, 9600, timeout=1, write_timeout=1)
            time.sleep(2)
            self.connect_button.config(text="Disconnect")
            self.test_button.config(state="normal")
            self.status_var.set(f"Connected to {port_name}. Press the test button.")
        except serial.SerialException as exc:
            self.serial_connection = None
            self.status_var.set(f"Connection failed: {exc}")
            messagebox.showerror("Connection Error", str(exc))

    def disconnect(self) -> None:
        if self.serial_connection and self.serial_connection.is_open:
            self.serial_connection.close()
        self.serial_connection = None
        self.connect_button.config(text="Connect")
        self.test_button.config(state="disabled")
        self.status_var.set("Disconnected.")

    def trigger_test(self) -> None:
        if not self.serial_connection or not self.serial_connection.is_open:
            self.status_var.set("Not connected to Arduino.")
            return

        self.test_button.config(state="disabled")
        self.status_var.set("Sending test signal to Arduino...")
        threading.Thread(target=self._send_test_signal, daemon=True).start()

    def _send_test_signal(self) -> None:
        try:
            self.serial_connection.write(b"S")
            self.serial_connection.flush()
            self.root.after(0, lambda: self.status_var.set("Signal sent. Check the red LED and buzzer."))
        except serial.SerialException as exc:
            self.root.after(0, lambda: self.status_var.set(f"Send failed: {exc}"))
        finally:
            self.root.after(0, lambda: self.test_button.config(state="normal"))

    def on_close(self) -> None:
        self.disconnect()
        self.root.destroy()


def main() -> None:
    root = tk.Tk()
    app = ArduinoTesterApp(root)
    root.mainloop()


if __name__ == "__main__":
    main()