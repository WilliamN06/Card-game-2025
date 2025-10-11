import java.util.LinkedList;
import java.util.Queue;

public class Deck {
    private final int id;
    private final Queue<Card> cards = new LinkedList<>();

    public Deck(int id) {
        this.id = id;
    }

    public synchronized void addCard(Card card) {
        cards.add(card);
    }

    public synchronized Card draw() {
        return cards.poll(); //returns null if empty
    }

    public synchronized boolean isEmpty() {
        return cards.isEmpty();
    }

    public synchronized String getContentsString() {
        StringBuilder sb = new StringBuilder("deck" + id + " contents: ");
        for (Card c : cards) sb.append(c.getValue()).append(" ");
        return sb.toString().trim();
    }

    public int getId() {
        return id;
    }
}
