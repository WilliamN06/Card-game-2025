package cardgame;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.TimeUnit;

public class Player extends Thread {
        public final int id;
        public final int preferredValue;
        public final List<Card> hand = new ArrayList<>(4);
        public final CardDeck leftDeck;
        public final CardDeck rightDeck;
        public final CardGame gameController;
        public PrintWriter log;

        public Player(int id, CardDeck left, CardDeck right, CardGame controller) throws IOException {
                this.id = id;
                this.preferredValue = id;
                this.leftDeck = left;
                this.rightDeck = right;
                this.gameController = controller;
                this.log = new PrintWriter(new BufferedWriter(new FileWriter("player" + id + "_output.txt")), true);

        }

        public List<Card> getHand() {
                return hand;
        }

        public void setInitialHand(List<Card> cards) {
                hand.clear();
                hand.addAll(cards);
                // log.println("player " + id + " initial hand " + handToString());
        }

        public String handToString() {
                StringBuilder sb = new StringBuilder();
                for (Card c : hand)
                        sb.append(c.getDenomination()).append(" ");
                return sb.toString().trim();
        }

        public boolean hasWinningHand() {
                if (hand.isEmpty())
                        return false;
                int firstValue = hand.get(0).getDenomination();
                for (Card c : hand)
                        if (c.getDenomination() != firstValue)
                                return false;
                return true;
        }

        @Override
        public void run() {
                try {
                        log.println("player " + id + " initial hand is " + handToString());

                        // Check if won immediately
                        if (hasWinningHand()) {
                                gameController.declareWinner(id);
                                return;
                        }

                        while (!gameController.isGameOver()) {
                                // Check game state BEFORE acquiring locks
                                if (gameController.isGameOver())
                                        break;
                                attemptAtomicTurn();

                                // Check win condition AFTER releasing locks
                                if (hasWinningHand()) {
                                        gameController.declareWinner(id);
                                }

                                Thread.sleep(10);// performance
                        }
                        if (gameController.getWinnerId() == id)
                                log.println("player " + id + " wins");
                        else
                                log.println("player " + gameController.getWinnerId() + " has informed player " + id
                                                + " that player " + gameController.getWinnerId() + " has won");

                        log.println("player " + id + " exits");
                        log.println("player " + id + " final hand: " + handToString());
                        log.close();

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public boolean attemptAtomicTurn() {
                boolean lockedLeft = false;
                boolean lockedRight = false;

                try {
                        // Try to acquire left deck lock with timeout
                        lockedLeft = leftDeck.tryLock(100, TimeUnit.MILLISECONDS);
                        if (!lockedLeft) {
                                return false; // Couldn't get left deck
                        }

                        // Try to acquire right deck lock with timeout
                        lockedRight = rightDeck.tryLock(100, TimeUnit.MILLISECONDS);
                        if (!lockedRight) {
                                return false; // Couldn't get right deck
                        }

                        // Both locks acquired - perform the atomic turn
                        return performTurnAtomic();

                } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                } finally {
                        // Always release locks in reverse order
                        if (lockedRight) {
                                rightDeck.unlock();
                        }
                        if (lockedLeft) {
                                leftDeck.unlock();
                        }
                }
        }

        public boolean performTurnAtomic() {
                // Check game state after acquiring locks
                if (gameController.isGameOver()) {
                        return false;
                }

                // Check if left deck is empty
                if (leftDeck.isEmpty()) {
                        return false;
                }

                // Perform the draw operation
                Card drawn = leftDeck.draw();
                if (drawn == null) {
                        return false; // No card drawn
                }

                try {
                        // Add to hand and select discard
                        hand.add(drawn);
                        Card discarded = selectDiscard(); // This never returns null now

                        // Remove from hand and add to right deck
                        hand.remove(discarded);
                        rightDeck.addCard(discarded);

                        // Log the successful turn
                        log.println("player " + id + " draws a " + drawn.getDenomination() + " from deck "
                                        + leftDeck.getId());
                        log.println("player " + id + " discards a " + discarded.getDenomination() + " to deck "
                                        + rightDeck.getId());
                        log.println("player " + id + " current hand is " + handToString());
                        log.flush();

                        return true; // Turn completed successfully

                } catch (Exception e) {
                        // Emergency rollback in case of unexpected errors
                        log.println("ERROR in turn - rolling back");
                        leftDeck.addCard(drawn);
                        hand.remove(drawn);
                        return false;
                }
        }

        public Card selectDiscard() {
                // Prefer not to discard preferred value, otherwise random non-preferred
                List<Card> nonPreferred = new ArrayList<>();
                for (Card c : hand)
                        if (c.getDenomination() != preferredValue)
                                nonPreferred.add(c);

                if (nonPreferred.isEmpty()) // all preferred, just discard random
                        return hand.get(new Random().nextInt(hand.size()));

                return nonPreferred.get(new Random().nextInt(nonPreferred.size()));
        }

}
