package cardgame;

import java.io.*;
import java.util.*;

/**
 * Handles loading and validation of card packs from text files.
 * Separates file I/O concerns from main game logic.
 */
public class CardPackLoader {

        /**
         * Loads and validates a card pack from the specified file
         * 
         * @param filename        Path to the pack file
         * @param numberOfPlayers Number of players for validation
         * @return List of Card objects representing the pack
         * @throws IOException If file cannot be read or is invalid
         */
        public static List<Card> loadPack(File file, int numberOfPlayers) throws IOException {
                if (numberOfPlayers <= 0) {
                        throw new IllegalArgumentException("Number of players must be positive");
                }

                String filename = file.getName();
                ;
                if (!file.exists()) {
                        throw new FileNotFoundException("Pack file not found: " + filename);
                }
                if (!file.canRead()) {
                        throw new IOException("Cannot read pack file: " + filename);
                }

                List<Card> pack = new ArrayList<>();
                int lineNumber = 0;

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;

                        while ((line = reader.readLine()) != null) {
                                lineNumber++;
                                line = line.trim();

                                // Skip empty lines
                                if (line.isEmpty()) {
                                        continue;
                                }

                                // Parse card value
                                try {
                                        int cardValue = Integer.parseInt(line);
                                        if (cardValue < 0) {
                                                throw new IOException("Invalid card value at line " + lineNumber +
                                                                ": " + cardValue + " (cannot be negative)");
                                        }
                                        pack.add(new Card(cardValue));

                                } catch (NumberFormatException e) {
                                        throw new IOException("Invalid card value at line " + lineNumber +
                                                        ": '" + line + "' (must be an integer)");
                                }
                        }
                } catch (IOException e) {
                        throw new IOException("Failed to read pack file: " + e.getMessage(), e);
                }

                // Validate pack size
                int expectedSize = 8 * numberOfPlayers;
                if (pack.size() != expectedSize) {
                        throw new IOException("Invalid pack size: expected " + expectedSize +
                                        " cards for " + numberOfPlayers + " players, but found " + pack.size());
                }

                System.out.println("✓ Pack loaded successfully: " + pack.size() + " cards");
                return pack;
        }

        /**
         * Prints pack statistics for debugging
         */
        public static void printPackStatistics(List<Card> pack, int numberOfPlayers) {
                Map<Integer, Integer> frequency = new HashMap<>();

                for (Card card : pack) {
                        int value = card.getDenomination();
                        frequency.put(value, frequency.getOrDefault(value, 0) + 1);
                }

                System.out.println("Pack Statistics:");
                System.out.println("Total cards: " + pack.size());
                System.out.println("Unique values: " + frequency.size());
                System.out.println("Value frequencies:");

                frequency.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .forEach(entry -> System.out.println(
                                                "  Value " + entry.getKey() + ": " + entry.getValue() + " cards"));
        }
}
