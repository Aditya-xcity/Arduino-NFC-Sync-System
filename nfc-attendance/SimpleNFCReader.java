import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

/**
 * Simple NFC Reader — Standalone example
 * 
 * Reads NFC card UID when card is placed on the reader.
 * 
 * Usage:
 *   javac SimpleNFCReader.java
 *   java SimpleNFCReader
 */
public class SimpleNFCReader {

    public static void main(String[] args) {
        System.out.println("=== Simple NFC Card Reader ===");
        System.out.println("Place an NFC card on the reader...\n");

        try {
            // 1. Get the default terminal factory
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();

            if (terminals.isEmpty()) {
                System.err.println("ERROR: No NFC readers found!");
                System.err.println("Make sure your NFC reader is connected.");
                return;
            }

            // 2. Get the first available terminal (NFC reader)
            CardTerminal terminal = terminals.get(0);
            System.out.println("Using reader: " + terminal.getName());
            System.out.println("Waiting for card...\n");

            // 3. Wait for a card to be placed
            if (!terminal.waitForCardPresent(0)) {
                System.out.println("No card detected.");
                return;
            }

            System.out.println("✓ Card detected!\n");
            Thread.sleep(100); // Small delay for stable connection

            // 4. Connect to the card
            Card card = terminal.connect("*");
            System.out.println("Connected to card. Protocol: " + card.getProtocol());

            // 5. Get the basic channel (used for sending APDU commands)
            CardChannel channel = card.getBasicChannel();

            // 6. Send APDU command to read UID
            // Command: FF CA 00 00 00 (get UID from any card type)
            byte[] getUID = {(byte) 0xFF, (byte) 0xCA, 0x00, 0x00, 0x00};
            ResponseAPDU response = channel.transmit(new CommandAPDU(getUID));

            // 7. Check if command succeeded (0x9000 = success)
            if (response.getSW() == 0x9000) {
                String uid = bytesToHex(response.getData());
                System.out.println("✓ Card UID: " + uid);
                System.out.println("Card Data (hex): " + bytesToHex(response.getBytes()));
            } else {
                System.err.println("ERROR: Command failed. Status: 0x" +
                        String.format("%04X", response.getSW()));
            }

            // 8. Disconnect
            card.disconnect(false);
            System.out.println("\nCard removed. Reader ready for next card.");

        } catch (CardException e) {
            System.err.println("Card Exception: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Interrupted: " + e.getMessage());
        }
    }

    /**
     * Convert byte array to hexadecimal string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}
