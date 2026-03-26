package com.nfc.attendance.nfc;

import javax.smartcardio.*;
import java.util.List;

/**
 * NFC Card Reader — singleton for NFC card communication.
 * Merged: Draft's clean singleton architecture + Main's contactless filter and "*" protocol.
 */
public class NFCCardReader {

    private static NFCCardReader instance;
    private TerminalFactory factory;
    private CardTerminal terminal;
    private Card card;

    public static synchronized NFCCardReader getInstance() {
        if (instance == null) {
            instance = new NFCCardReader();
        }
        return instance;
    }

    private NFCCardReader() {
        try {
            factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();

            if (terminals.isEmpty()) {
                System.out.println("No NFC card terminals found");
                terminal = null;
            } else {
                // Prefer contactless reader (from main project logic)
                terminal = terminals.stream()
                        .filter(t -> t.getName().toLowerCase().contains("contactless"))
                        .findFirst()
                        .orElse(terminals.get(0));
                System.out.println("NFC card reader initialized: " + terminal.getName());
            }
        } catch (Exception e) {
            System.err.println("Error initializing NFC reader: " + e.getMessage());
            terminal = null;
        }
    }

    public boolean isReaderAvailable() {
        return terminal != null;
    }

    public boolean waitForCard(long timeout) {
        if (terminal == null) return false;
        try {
            return terminal.waitForCardPresent(timeout);
        } catch (CardException e) {
            System.err.println("Error waiting for card: " + e.getMessage());
            return false;
        }
    }

    public boolean waitForCardAbsent(long timeout) {
        if (terminal == null) return false;
        try {
            return terminal.waitForCardAbsent(timeout);
        } catch (CardException e) {
            System.err.println("Error waiting for card removal: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reads the UID of the inserted NFC card.
     * Uses "*" protocol (from main project) for broader compatibility.
     */
    public String readCardUID() {
        if (terminal == null) return null;

        try {
            if (!terminal.isCardPresent()) return null;

            card = terminal.connect("*");
            CardChannel channel = card.getBasicChannel();

            byte[] getUID = {(byte) 0xFF, (byte) 0xCA, 0x00, 0x00, 0x00};
            ResponseAPDU response = channel.transmit(new CommandAPDU(getUID));

            if (response.getSW() == 0x9000) {
                return bytesToHex(response.getData());
            } else {
                System.out.println("Error reading card UID. SW: " +
                        String.format("0x%04X", response.getSW()));
                return null;
            }
        } catch (CardException e) {
            System.err.println("Error reading card: " + e.getMessage());
            return null;
        } finally {
            disconnectCard();
        }
    }

    /**
     * Waits for a card and reads its UID
     */
    public String waitAndReadCardUID(long timeout) {
        if (!waitForCard(timeout)) return null;
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        return readCardUID();
    }

    public void disconnectCard() {
        if (card != null) {
            try {
                card.disconnect(false);
            } catch (CardException e) {
                // ignore
            }
            card = null;
        }
    }

    public CardTerminal getTerminal() {
        return terminal;
    }

    public void closeReader() {
        disconnectCard();
        System.out.println("NFC reader closed");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}

