package cardgame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.*;

public class CardGameTest {

        private ByteArrayOutputStream outputStream;
        private PrintStream originalOut;

        @BeforeEach
        public void setUpStreams() {
                outputStream = new ByteArrayOutputStream();
                originalOut = System.out;
                System.setOut(new PrintStream(outputStream));
        }

        @AfterEach
        public void restoreStreams() {
                System.setOut(originalOut);
        }

        @Test
        public void testDeclareWinner() {
                CardGame game = new CardGame();
                assertFalse(game.isGameOver());
                game.declareWinner(3);
                assertTrue(game.isGameOver());
                assertEquals(3, game.getWinnerId());
        }

        @Test
        public void testDeclareWinnerIdempotent() {
                CardGame game = new CardGame();
                game.declareWinner(2);
                assertEquals(2, game.getWinnerId());
                game.declareWinner(5);
                assertEquals(2, game.getWinnerId());
        }

        @Test
        public void testInitialiseAndDistributeHands_Valid() throws IOException {
                CardGame game = new CardGame();
                int numPlayers = 2;
                List<Card> pack = Arrays.asList(
                                new Card(1), new Card(1), new Card(1), new Card(1),
                                new Card(2), new Card(2), new Card(2), new Card(2),
                                new Card(3), new Card(3), new Card(3), new Card(3),
                                new Card(4), new Card(4), new Card(4), new Card(4));
                game.initialiseGame(numPlayers, pack);
                assertEquals(2, game.players.size());
                assertEquals(2, game.decks.size());
                for (Player p : game.players) {
                        assertEquals(4, p.getHand().size());
                }
        }

        @Test
        public void testInitialiseAndDistributeHands_InsufficientCards() {
                CardGame game = new CardGame();
                int numPlayers = 2;
                List<Card> pack = Arrays.asList(new Card(1), new Card(1));
                assertThrows(IllegalStateException.class, () -> game.initialiseGame(numPlayers, pack));
        }

        @Test
        public void testGetNumberOfPlayersValidInput() {
                CardGame game = new CardGame();
                Scanner sc = new Scanner("3\n");
                assertEquals(3, game.getNumberOfPlayers(sc));
        }

        @Test
        public void testGetNumberOfPlayersInvalidRecovery() {
                CardGame game = new CardGame();
                Scanner sc = new Scanner("hi\n0\n-2\n2\n");
                assertEquals(2, game.getNumberOfPlayers(sc));
        }

        @Test
        public void testLoadPackValid() throws IOException {
                CardGame game = new CardGame();
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp)) {
                        for (int i = 0; i < 8; i++)
                                pw.println("1");
                }
                List<Card> cards = game.loadPack(temp, 1);
                assertNotNull(cards);
                assertEquals(8, cards.size());
        }

        @Test
        public void testLoadPackInvalidValueAndLength() throws IOException {
                CardGame game = new CardGame();
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp)) {
                        pw.println("1");
                        pw.println("-5");
                }
                assertNull(game.loadPack(temp, 1));

                File temp2 = File.createTempFile("pack", ".txt");
                temp2.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp2)) {
                        pw.println("1");
                }
                assertNull(game.loadPack(temp2, 1));
        }

        @Test
        public void testGetPackFileValidAndInvalid() throws IOException {
                CardGame game = new CardGame();
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                Scanner sc = new Scanner(temp.getAbsolutePath() + "\nmissing.txt\n" + temp.getAbsolutePath() + "\n");
                File chosen = game.getPackFile(sc);
                assertTrue(chosen.exists());
        }

        @Test
        public void testWriteDeckOutputsAndException() throws IOException {
                CardGame game = new CardGame();
                game.writeDeckOutputs();
        }

        @Test
        public void testDeclareWinnerOutputMessage() {
                CardGame game = new CardGame();
                game.declareWinner(7);
                String output = outputStream.toString();
                assertTrue(output.contains("player 7 wins"));
        }

        @Test
        public void testGetNumberOfPlayersPrompt() {
                CardGame game = new CardGame();
                Scanner sc = new Scanner("3\n");
                game.getNumberOfPlayers(sc);
                String output = outputStream.toString();
                assertTrue(output.contains("Enter number of players: "));
        }

        @Test
        public void testGetNumberOfPlayersErrorMessage() {
                CardGame game = new CardGame();
                Scanner sc = new Scanner("invalid\n2\n");
                game.getNumberOfPlayers(sc);
                String output = outputStream.toString();
                assertTrue(output.contains("Invalid input. Please enter a positive integer."));
        }

        @Test
        public void testGetPackFilePrompt() throws IOException {
                CardGame game = new CardGame();
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                Scanner sc = new Scanner(temp.getAbsolutePath() + "\n");
                game.getPackFile(sc);
                String output = outputStream.toString();
                assertTrue(output.contains("Enter path to pack file: "));
        }

        @Test
        public void testGetPackFilePrintsInvalidPath() throws IOException {
                CardGame game = new CardGame();
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                Scanner sc = new Scanner("notfound.txt\n" + temp.getAbsolutePath() + "\n");
                game.getPackFile(sc);
                String output = outputStream.toString();
                assertTrue(output.contains("Invalid path. Please enter a valid file path."));
        }

        @Test
        public void testLoadPackErrorMessage() throws IOException {
                CardGame game = new CardGame();
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp)) {
                        pw.println("not_a_number");
                }
                List<Card> result = game.loadPack(temp, 1);
                assertNull(result);
                String output = outputStream.toString();
                assertTrue(output.contains("Error reading pack file:"));
        }

        @Test
        public void testLoadPackSizeErrorMessage() throws IOException {
                CardGame game = new CardGame();
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp)) {
                        pw.println("1");
                        pw.println("2");
                }
                List<Card> result = game.loadPack(temp, 1);
                assertNull(result);
                String output = outputStream.toString();
                assertTrue(output.contains("Invalid pack: must contain exactly"));
        }

        @Test
        public void testLoadPackAcceptsZero() throws IOException {
                CardGame game = new CardGame();
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp)) {
                        for (int i = 0; i < 7; i++)
                                pw.println("1");
                        pw.println("0");
                }
                List<Card> cards = game.loadPack(temp, 1);
                assertNotNull(cards);
                assertEquals(8, cards.size());
        }

        @Test
        public void testLoadPackRejectsNegative() throws IOException {
                CardGame game = new CardGame();
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp)) {
                        for (int i = 0; i < 7; i++)
                                pw.println("1");
                        pw.println("-1");
                }
                List<Card> cards = game.loadPack(temp, 1);
                assertNull(cards);
        }

        @Test
        public void testLoadPackSizeCalculationMultiplePlayerCounts() throws IOException {
                CardGame game = new CardGame();
                File temp1 = File.createTempFile("pack1", ".txt");
                temp1.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp1)) {
                        for (int i = 0; i < 8; i++)
                                pw.println("1");
                }
                List<Card> cards1 = game.loadPack(temp1, 1);
                assertNotNull(cards1);
                assertEquals(8, cards1.size());

                File temp2 = File.createTempFile("pack2", ".txt");
                temp2.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp2)) {
                        for (int i = 0; i < 16; i++)
                                pw.println("1");
                }
                List<Card> cards2 = game.loadPack(temp2, 2);
                assertNotNull(cards2);
                assertEquals(16, cards2.size());

                File temp3 = File.createTempFile("pack3", ".txt");
                temp3.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp3)) {
                        for (int i = 0; i < 24; i++)
                                pw.println("1");
                }
                List<Card> cards3 = game.loadPack(temp3, 3);
                assertNotNull(cards3);
                assertEquals(24, cards3.size());
        }

        @Test
        public void testInitialiseGameBoundaryConditions() throws IOException {
                CardGame game = new CardGame();
                List<Card> pack = new ArrayList<>();
                for (int i = 0; i < 32; i++)
                        pack.add(new Card(1));
                game.initialiseGame(4, pack);
                assertEquals(4, game.players.size());
                assertEquals(4, game.decks.size());
        }

        @Test
        public void testDistributeInitialHandsBoundaryConditions() throws IOException {
                CardGame game = new CardGame();
                List<Card> pack = new ArrayList<>();
                for (int i = 0; i < 12; i++)
                        pack.add(new Card(i % 4 + 1));
                game.initialiseGame(3, pack);
                assertEquals(3, game.players.size());
        }

        @Test
        public void testDistributeInitialHandsExactCards() throws IOException {
                CardGame game = new CardGame();
                int numPlayers = 3;
                List<Card> pack = new ArrayList<>();
                for (int i = 0; i < 24; i++)
                        pack.add(new Card(i % 4 + 1));
                game.initialiseGame(numPlayers, pack);
                for (Player p : game.players) {
                        assertEquals(4, p.getHand().size());
                }
                int totalDeckCards = 0;
                for (CardDeck d : game.decks)
                        totalDeckCards += d.getContents().size();
                assertEquals(12, totalDeckCards);
        }

        @Test
        public void testDistributeInitialHandsOneCardMissingThrows() throws IOException {
                CardGame game = new CardGame();
                int numPlayers = 3;
                List<Card> pack = new ArrayList<>();
                for (int i = 0; i < 11; i++)
                        pack.add(new Card(1));
                assertThrows(IllegalStateException.class, () -> game.initialiseGame(numPlayers, pack));
        }

        @Test
        public void testDistributeInitialHandsDeckFilling() throws IOException {
                CardGame game = new CardGame();
                int numPlayers = 2;
                List<Card> pack = new ArrayList<>();
                for (int i = 0; i < 16; i++)
                        pack.add(new Card(2));
                game.initialiseGame(numPlayers, pack);
                for (CardDeck deck : game.decks) {
                        assertEquals(4, deck.getContents().size());
                }
        }

        @Test
        public void testDistributeInitialHandsCallsSetInitialHand() throws IOException {
                CardGame game = new CardGame();
                List<Card> pack = new ArrayList<>();
                for (int i = 0; i < 16; i++)
                        pack.add(new Card(1));
                game.initialiseGame(2, pack);
                for (Player p : game.players) {
                        assertFalse(p.getHand().isEmpty());
                }
        }

        @Test
        public void testLoadPackWithEmptyLines() throws IOException {
                CardGame game = new CardGame();
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp)) {
                        pw.println("1");
                        pw.println("");
                        pw.println("2");
                        pw.println("");
                        for (int i = 0; i < 6; i++)
                                pw.println("3");
                }
                List<Card> cards = game.loadPack(temp, 1);
                assertNotNull(cards);
                assertEquals(8, cards.size());
        }

        @Test
        public void testGetNumberOfPlayersMultipleInvalidInputs() {
                CardGame game = new CardGame();
                Scanner sc = new Scanner("abc\n-5\n0\nxyz\n4\n");
                int result = game.getNumberOfPlayers(sc);
                assertEquals(4, result);
                String output = outputStream.toString();
                assertTrue(output.contains("Invalid input. Please enter a positive integer."));
        }

        @Test
        public void testGetPackFileInvalidThenValid() throws IOException {
                CardGame game = new CardGame();
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                Scanner sc = new Scanner("nonexistent.txt\n" + temp.getAbsolutePath() + "\n");
                File result = game.getPackFile(sc);
                assertEquals(temp.getAbsolutePath(), result.getAbsolutePath());
        }

        @Test
        public void testAllOutputPathsCovered() throws IOException {
                CardGame game = new CardGame();
                Scanner sc1 = new Scanner("abc\n-1\n0\n5\n");
                game.getNumberOfPlayers(sc1);
                File temp = File.createTempFile("pack", ".txt");
                temp.deleteOnExit();
                Scanner sc2 = new Scanner("nonexistent\n" + temp.getAbsolutePath() + "\n");
                game.getPackFile(sc2);
                File temp2 = File.createTempFile("pack2", ".txt");
                temp2.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp2)) {
                        pw.println("abc");
                }
                game.loadPack(temp2, 1);
                File temp3 = File.createTempFile("pack3", ".txt");
                temp3.deleteOnExit();
                try (PrintWriter pw = new PrintWriter(temp3)) {
                        pw.println("1");
                        pw.println("2");
                }
                game.loadPack(temp3, 1);
                game.declareWinner(9);
                String output = outputStream.toString();
                assertFalse(output.isEmpty());
        }
}
