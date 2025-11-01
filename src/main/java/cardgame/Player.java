package cardgame;

import java.io.*;
import java.util.*;

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
                //log.println("player " + id + " initial hand " + handToString());
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
                            CardDeck firstLock = (leftDeck.getId() < rightDeck.getId()) ? leftDeck : rightDeck;
                            CardDeck secondLock = (leftDeck.getId() < rightDeck.getId()) ? rightDeck : leftDeck;

                            while (!gameController.isGameOver()) {
                                // Check game state BEFORE acquiring locks
                                if (gameController.isGameOver()) break;
                                
                                Card drawn = null;
                                Card discarded = null;
    
                                // Atomic operation: draw + discard
                                synchronized(firstLock) {
                                    synchronized(secondLock) {
                                        if (leftDeck.isEmpty()) {
                                            continue; 
                                        }
                                        
                                        drawn = leftDeck.draw();
                                        if (drawn == null) {
                                            continue;
                                        }
            
                                    hand.add(drawn);
                                    discarded = selectDiscard();
                                    
                                    if (discarded == null) {
                                        leftDeck.addCard(drawn);
                                        hand.remove(drawn);
                                        continue;
                                    }
            
                                    hand.remove(discarded);
                                    rightDeck.addCard(discarded);
            
                                    log.println("player " + id + " draws a " + drawn.getDenomination() + " from deck " + leftDeck.getId());
                                    log.println("player " + id + " discards a " + discarded.getDenomination() + " to deck " + rightDeck.getId());
                                    log.println("player " + id + " current hand is " + handToString());
                                    log.flush();
                                }
                            }
    
                            // Check win condition AFTER releasing locks
                            if (hasWinningHand()) {
                                gameController.declareWinner(id);
                            }
                            
                            // Thread.sleep(10); // Good for performance, keep it
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
