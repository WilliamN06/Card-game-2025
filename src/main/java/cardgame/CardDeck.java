package cardgame;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.TimeUnit;


public class CardDeck {
        public final int id;
        public final Queue<Card> cards = new LinkedList<>();
        private final Lock lock = new ReentrantLock(true); 

        public CardDeck(int id) {
                this.id = id;
        }

        public synchronized void addCard(Card card) {
                cards.add(card);
        }

        public synchronized Card draw() {
                return cards.poll(); // returns null if empty
        }

        public synchronized boolean isEmpty() {
                return cards.isEmpty();
        }

        public synchronized String getContentsString() {
                StringBuilder sb = new StringBuilder("deck" + id + " contents: ");
                for (Card c : cards)
                        sb.append(c.getDenomination()).append(" ");
                return sb.toString().trim();
        }

        public synchronized List<Card> getContents() {
                return new ArrayList<>(cards);
        }

        public int getId() {
                return id;
        }
        public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
                return lock.tryLock(timeout, unit);
    }

       public void unlock() {
        lock.unlock();
    }
}
