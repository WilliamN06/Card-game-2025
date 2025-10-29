package cardgame;

import java.io.*;
import java.util.*;

public class CardGame {
        private volatile boolean gameOver = false;
        private volatile int winnerId = -1;
        private CardPackLoader loader = new CardPackLoader();

        private final List<Player> players = new ArrayList<>();
        private final List<CardDeck> decks = new ArrayList<>();

        public static void main(String[] args) {
                CardGame game = new CardGame();
                game.runGame();
        }

        public void runGame() {
                try (Scanner sc = new Scanner(System.in)) {
                        int numPlayers = getNumberOfPlayers(sc);
                        File packFile = getPackFile(sc);
                        List<Card> pack = loader.loadPack(packFile, numPlayers);

                        if (pack == null) {
                                System.out.println("Invalid pack. Game aborted.");
                                return;
                        }

                        initialiseGame(numPlayers, pack);
                        startPlayers();
                        waitForPlayersToFinish();
                        writeDeckOutputs();

                } catch (Exception e) {
                        System.out.println("Unexpected error: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        public int getNumberOfPlayers(Scanner sc) {
                int n;
                while (true) {
                        System.out.print("Enter number of players: ");
                        if (sc.hasNextInt()) {
                                n = sc.nextInt();
                                sc.nextLine();
                                if (n > 0)
                                        break;
                        } else
                                sc.next();
                        System.out.println("Invalid input. Please enter a positive integer.");
                }
                return n;
        }

        public File getPackFile(Scanner sc) {
                File file;
                // add excpetion or such
                while (true) {
                        System.out.print("Enter path to pack file: ");
                        String path = sc.nextLine().trim();
                        file = new File(path);
                        // Debug information
                        System.out.println("Input path: '" + path + "'");
                        System.out.println("Absolute path: " + file.getAbsolutePath());
                        System.out.println("Exists: " + file.exists());
                        System.out.println("Can read: " + file.canRead());
                        System.out.println("Is file: " + file.isFile());
                        if (file.exists() && file.canRead())
                                break;
                        System.out.println("Invalid path. Please enter a valid file path.");
                }
                return file;
        }

        public List<Card> loadPack(File file, int n) {
                List<Card> pack = new ArrayList<>();
                try (Scanner reader = new Scanner(file)) {
                        while (reader.hasNextLine()) {
                                String line = reader.nextLine().trim();
                                if (line.isEmpty())
                                        continue;
                                int val = Integer.parseInt(line);
                                if (val < 0)
                                        throw new IllegalArgumentException();
                                pack.add(new Card(val));
                        }
                } catch (Exception e) {
                        System.out.println("Error reading pack file: " + e.getMessage());
                        return null;
                }

                if (pack.size() != 8 * n) {
                        System.out.println("Invalid pack: must contain exactly " + (8 * n) + " integers.");
                        return null;
                }

                return pack;
        }

        public void initialiseGame(int n, List<Card> pack) throws IOException {
                // Create decks
                for (int i = 1; i <= n; i++) {
                        decks.add(new CardDeck(i));
                }

                // Create players and link decks in ring topology
                for (int i = 1; i <= n; i++) {
                        CardDeck left = decks.get(i - 1);
                        CardDeck right = decks.get(i % n);
                        Player p = new Player(i, left, right, this);
                        players.add(p);
                }

                // Distribute cards (round-robin)
                distributeInitialHands(n, pack);
        }

        public void distributeInitialHands(int n, List<Card> pack) throws IOException {
                Iterator<Card> it = pack.iterator();

                // Create lists for each player first
                List<List<Card>> playerHands = new ArrayList<>();
                for (int i = 0; i < players.size(); i++) {
                        playerHands.add(new ArrayList<>());
                }

                // Round-robin distribution: deal one card to each player in sequence, 4 times
                for (int round = 0; round < 4; round++) {
                        for (int i = 0; i < players.size(); i++) {
                                if (!it.hasNext())
                                        throw new IllegalStateException("Insufficient cards");
                                playerHands.get(i).add(it.next()); // Add one card to each player's list
                        }
                }

                // Now set the initial hands for all players
                for (int i = 0; i < players.size(); i++) {
                        players.get(i).setInitialHand(playerHands.get(i));
                }

                // Fill decks with remaining cards
                int deckIndex = 0;
                while (it.hasNext()) {
                        decks.get(deckIndex).addCard(it.next());
                        deckIndex = (deckIndex + 1) % n;
                }
        }

        public void startPlayers() {
                for (Player p : players) {
                        p.start();
                }
        }

        public void waitForPlayersToFinish() {
                for (Player p : players) {
                        try {
                                p.join();
                        } catch (InterruptedException e) {
                                System.out.println("Player thread interrupted.");
                        }
                }
        }

        public void writeDeckOutputs() {
                for (CardDeck d : decks) {
                        try (PrintWriter pw = new PrintWriter(new FileWriter("deck" + d.getId() + "_output.txt"))) {
                                pw.println(d.getContentsString());
                        } catch (IOException e) {
                                System.out.println("Error writing deck file: " + e.getMessage());
                        }
                }
        }

        public synchronized void declareWinner(int id) {
                if (!gameOver) {
                        gameOver = true;
                        winnerId = id;
                        System.out.println("player " + id + " wins");
                }
        }

        public boolean isGameOver() {
                return gameOver;
        }

        public int getWinnerId() {
                return winnerId;
        }
}
