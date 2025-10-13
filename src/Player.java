import java.io.*;
import java.util.*;

public class Player extends Thread {
    private final int id;
    private final int preferredValue;
    private final List<Card> hand = new ArrayList<>(4);
    private final Deck leftDeck;
    private final Deck rightDeck;
    private final CardGame gameController;
    private final PrintWriter log;

    public Player(int id, Deck left, Deck right, CardGame controller) throws IOException {
        this.id = id;
        this.preferredValue = id;
        this.leftDeck = left;
        this.rightDeck = right;
        this.gameController = controller;
        this.log = new PrintWriter(new BufferedWriter(new FileWriter("player" + id + "_output.txt")));
    }

    public void setInitialHand(List<Card> cards) {
        hand.clear();
        hand.addAll(cards);
        log.println("player " + id + " initial hand " + handToString());
    }

    private String handToString() {
        StringBuilder sb = new StringBuilder();
        for (Card c : hand) sb.append(c.getValue()).append(" ");
        return sb.toString().trim();
    }

    private boolean hasWinningHand() {
        if (hand.isEmpty()) return false;
        int firstValue = hand.get(0).getValue();
        for (Card c : hand) if (c.getValue() != firstValue) return false;
        return true;
    }

    @Override
    public void run() {
        try {
            // Check if won immediately
            if (hasWinningHand()) {
                gameController.declareWinner(id);
                return;
            }

            while (!gameController.isGameOver()) {
                // Draw card
               synchronized (leftDeck) {
                synchronized (rightDeck) {
                        // now you own both locks, no other thread can touch them
                           Card drawn = leftDeck.draw();
                        if (drawn == null) return; // if deck empty, skip safely

                        hand.add(drawn);
                        log.println("player " + id + " draws a " + drawn.getValue() + " from deck " + leftDeck.getId());

                        // Choose and discard
                        Card discard = selectDiscard();
                        hand.remove(discard);
                        rightDeck.addCard(discard);

                        log.println("player " + id + " discards a " + discard.getValue() + " to deck " + rightDeck.getId());
                        log.println("player " + id + " current hand is " + handToString());
                        log.flush();

                        // Check win
                        if (hasWinningHand()) {
                            game.declareWinner(id);
                        }
                    }
}
                //Thread.sleep(10); // added to avoid aggressive spinning decide to keep or remove
            }

            if (gameController.getWinnerId() == id)
                log.println("player " + id + " wins");
            else
                log.println("player " + gameController.getWinnerId() + " has informed player " + id + " that player " + gameController.getWinnerId() + " has won");

            log.println("player " + id + " exits");
            log.println("player " + id + " final hand: " + handToString());
            log.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Card selectDiscard() {
        // Prefer not to discard preferred value, otherwise random non-preferred
        List<Card> nonPreferred = new ArrayList<>();
        for (Card c : hand)
            if (c.getValue() != preferredValue)
                nonPreferred.add(c);

        if (nonPreferred.isEmpty()) // all preferred, just discard random
            return hand.get(new Random().nextInt(hand.size()));

        return nonPreferred.get(new Random().nextInt(nonPreferred.size()));
    }
}
