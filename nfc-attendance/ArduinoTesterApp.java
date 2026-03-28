import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.fazecast.jSerialComm.SerialPort;

public class ArduinoTesterApp extends JFrame {
    private JComboBox<String> portCombo;
    private JButton refreshButton, connectButton, testButton;
    private JLabel statusLabel;
    private SerialPort serialPort;

    public ArduinoTesterApp() {
        setTitle("Arduino LED + Buzzer Test");
        setSize(460, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Arduino Hardware Test");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(title);
        container.add(Box.createVerticalStrut(12));

        JLabel info = new JLabel("Use this button to send the same serial signal used by the NFC app.\nYour Arduino sketch must already be uploaded and listening on 9600 baud.");
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(info);
        container.add(Box.createVerticalStrut(12));

        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        portPanel.add(new JLabel("COM Port:"));
        portCombo = new JComboBox<>();
        portCombo.setPreferredSize(new Dimension(120, 25));
        portPanel.add(portCombo);
        refreshButton = new JButton("Refresh");
        portPanel.add(refreshButton);
        connectButton = new JButton("Connect");
        portPanel.add(connectButton);
        container.add(portPanel);
        container.add(Box.createVerticalStrut(12));

        testButton = new JButton("Test LED + Buzzer");
        testButton.setEnabled(false);
        container.add(testButton);
        container.add(Box.createVerticalStrut(12));

        JLabel statusTitle = new JLabel("Status");
        statusTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        statusTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(statusTitle);
        container.add(Box.createVerticalStrut(4));

        statusLabel = new JLabel("Select a COM port and connect.");
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        statusLabel.setBackground(Color.WHITE);
        statusLabel.setPreferredSize(new Dimension(400, 30));
        container.add(statusLabel);

        add(container, BorderLayout.CENTER);

        refreshButton.addActionListener(e -> refreshPorts());
        connectButton.addActionListener(e -> toggleConnection());
        testButton.addActionListener(e -> triggerTest());

        refreshPorts();
    }

    private void refreshPorts() {
        portCombo.removeAllItems();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            portCombo.addItem(port.getSystemPortName());
        }
        if (portCombo.getItemCount() > 0) {
            portCombo.setSelectedIndex(0);
            statusLabel.setText("COM ports loaded. Connect to the Arduino board.");
        } else {
            statusLabel.setText("No COM ports found. Check USB cable and board connection.");
        }
    }

    private void toggleConnection() {
        if (serialPort != null && serialPort.isOpen()) {
            disconnect();
            return;
        }
        String portName = (String) portCombo.getSelectedItem();
        if (portName == null || portName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a COM port first.", "No Port", JOptionPane.WARNING_MESSAGE);
            return;
        }
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 2000);
        if (serialPort.openPort()) {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            connectButton.setText("Disconnect");
            testButton.setEnabled(true);
            statusLabel.setText("Connected to " + portName + ". Press the test button.");
        } else {
            serialPort = null;
            statusLabel.setText("Connection failed: Could not open port.");
            JOptionPane.showMessageDialog(this, "Connection failed: Could not open port.", "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
        serialPort = null;
        connectButton.setText("Connect");
        testButton.setEnabled(false);
        statusLabel.setText("Disconnected.");
    }

    private void triggerTest() {
        if (serialPort == null || !serialPort.isOpen()) {
            statusLabel.setText("Not connected to Arduino.");
            return;
        }
        testButton.setEnabled(false);
        statusLabel.setText("Sending test signal to Arduino...");
        new Thread(() -> {
            try {
                serialPort.writeBytes(new byte[]{'S'}, 1);
                serialPort.flushIOBuffers();
                SwingUtilities.invokeLater(() -> statusLabel.setText("Signal sent. Check the red LED and buzzer."));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> statusLabel.setText("Send failed: " + ex.getMessage()));
            } finally {
                SwingUtilities.invokeLater(() -> testButton.setEnabled(true));
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ArduinoTesterApp app = new ArduinoTesterApp();
            app.setVisible(true);
        });
    }
}
