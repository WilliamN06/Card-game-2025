/*
Potential class to use

public class DeckPair {
    private final Deck left;
    private final Deck right;

    public DeckPair(Deck left, Deck right) {
        this.left = left;
        this.right = right;
    }

    public void atomicDrawDiscard(Player p) {
        synchronized (left) {
            synchronized (right) {
                Card drawn = left.draw();
                if (drawn == null) return;
                p.addCard(drawn);
                Card discard = p.selectDiscard();
                p.removeCard(discard);
                right.addCard(discard);
            }
        }
    }
}
*/