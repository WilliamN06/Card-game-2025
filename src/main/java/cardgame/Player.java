package cardgame;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.TimeUnit;


/*
 Class for player in the card game, child thread class.y
 Each player runs in its own thread, implements their game strategy,
 and manages its hand of cards. Players draw from left deck first, then discard to right deck.
 */
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

                        if (hasWinningHand()) {
                                gameController.declareWinner(id);
                                return;
                        }


                            while (!gameController.isGameOver()) {
                                if (gameController.isGameOver()) break;
                                attemptAtomicTurn();

    
    
                            if (hasWinningHand()) {
                                gameController.declareWinner(id);
                            }
                            
                            Thread.sleep(10);//performance
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
        //Tries to lock both left and right deck using timeouts and if successful calls performturnAtomic which performs draw-discard action  
        public boolean attemptAtomicTurn() {
                boolean lockedLeft = false;
                boolean lockedRight = false;

                try {
                    lockedLeft = leftDeck.tryLock(100, TimeUnit.MILLISECONDS);
                    if (!lockedLeft) {
                        return false; 
                    }
        
                            lockedRight = rightDeck.tryLock(100, TimeUnit.MILLISECONDS);
                    if (!lockedRight) {
                        return false; 
                    }

                   return performTurnAtomic();

                } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                } finally {
                       
                        if (lockedRight) {
                                rightDeck.unlock();
                        }
                        if (lockedLeft) {
                                leftDeck.unlock();
                        }
                }
        }

        //method for atomic draw discard action and outputting to the according player txt file
        public boolean performTurnAtomic() {
                if (gameController.isGameOver()) {
                        return false;
                }

                if (leftDeck.isEmpty()) {
                        return false;
                }

                Card drawn = leftDeck.draw();
                if (drawn == null) {
                        return false; 
                }

                try {
                        hand.add(drawn);
                        Card discarded = selectDiscard(); 

                        hand.remove(discarded);
                        rightDeck.addCard(discarded);

                        log.println("player " + id + " draws a " + drawn.getDenomination() + " from deck "
                                        + leftDeck.getId());
                        log.println("player " + id + " discards a " + discarded.getDenomination() + " to deck "
                                        + rightDeck.getId());
                        log.println("player " + id + " current hand is " + handToString());
                        log.flush();

                        return true; 

                } catch (Exception e) {
                        log.println("ERROR in turn - rolling back");
                        leftDeck.addCard(drawn);
                        hand.remove(drawn);
                        return false;
                }
        }

        public Card selectDiscard() {
                List<Card> nonPreferred = new ArrayList<>();
                for (Card c : hand)
                        if (c.getDenomination() != preferredValue)
                                nonPreferred.add(c);

                if (nonPreferred.isEmpty()) 
                        return hand.get(new Random().nextInt(hand.size()));

                return nonPreferred.get(new Random().nextInt(nonPreferred.size()));
        }

}
