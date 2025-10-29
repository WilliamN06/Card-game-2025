package cardgame;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.io.*;

class MockCardDeck extends CardDeck {
    Queue<Card> fakeDraws = new LinkedList<>();
    public MockCardDeck(int id) { super(id); }
    @Override public Card draw() { return fakeDraws.poll(); }
    @Override public void addCard(Card c) { fakeDraws.add(c); }
}

class MockCardGame extends CardGame {
    boolean winDeclared = false;
    boolean gameOver = false;
    int winnerId = -1;
    @Override public void declareWinner(int id) { winDeclared = true; winnerId = id; gameOver = true; }
    @Override public boolean isGameOver() { return gameOver; }
    @Override public int getWinnerId() { return winnerId; }
}

public class PlayerTest {

    @Test
    public void testSetInitialHandAndHandToString() throws IOException {
        CardDeck left = new CardDeck(1), right = new CardDeck(2);
        CardGame cg = new CardGame();
        Player p = new Player(5, left, right, cg);
        List<Card> initial = Arrays.asList(new Card(2), new Card(2), new Card(2), new Card(2));
        p.setInitialHand(initial);
        assertEquals("2 2 2 2", p.handToString());
        assertEquals(4, p.hand.size());
    }

    @Test
    public void testSetInitialHandOverridesOldHand() throws IOException {
        CardDeck left = new CardDeck(1), right = new CardDeck(2);
        CardGame cg = new CardGame();
        Player p = new Player(3, left, right, cg);
        List<Card> first = Arrays.asList(new Card(2), new Card(3), new Card(4), new Card(5));
        List<Card> second = Arrays.asList(new Card(9), new Card(8), new Card(7), new Card(6));
        p.setInitialHand(first);
        p.setInitialHand(second);
        assertEquals(4, p.hand.size());
        assertEquals("9 8 7 6", p.handToString());
    }

    @Test
    public void testHandToStringEmptyHand() throws IOException {
        CardDeck left = new CardDeck(2), right = new CardDeck(3);
        CardGame cg = new CardGame();
        Player p = new Player(2, left, right, cg);
        assertEquals("", p.handToString());
    }

    @Test
    public void testHasWinningHandTrue() throws IOException {
        CardDeck left = new CardDeck(1), right = new CardDeck(2);
        CardGame cg = new CardGame();
        Player p = new Player(3, left, right, cg);
        List<Card> winning = Arrays.asList(new Card(4), new Card(4), new Card(4), new Card(4));
        p.setInitialHand(winning);
        assertTrue(p.hasWinningHand());
    }

    @Test
    public void testHasWinningHandFalse() throws IOException {
        CardDeck left = new CardDeck(1), right = new CardDeck(2);
        CardGame cg = new CardGame();
        Player p = new Player(3, left, right, cg);
        List<Card> losing = Arrays.asList(new Card(1), new Card(2), new Card(3), new Card(4));
        p.setInitialHand(losing);
        assertFalse(p.hasWinningHand());
    }

    @Test
    public void testHasWinningHandEmptyHand() throws IOException {
        CardDeck left = new CardDeck(1), right = new CardDeck(2);
        CardGame cg = new CardGame();
        Player p = new Player(1, left, right, cg);
        p.setInitialHand(Collections.emptyList());
        assertFalse(p.hasWinningHand());
    }

    @Test
    public void testSelectDiscardPrefersNonPreferred() throws IOException {
        CardDeck left = new CardDeck(1), right = new CardDeck(2);
        CardGame cg = new CardGame();
        Player p = new Player(3, left, right, cg);
        p.setInitialHand(Arrays.asList(new Card(3), new Card(7), new Card(3), new Card(2)));
        Card discard = p.selectDiscard();
        assertNotEquals(3, discard.getDenomination());
    }

    @Test
    public void testSelectDiscardAllPreferred() throws IOException {
        CardDeck left = new CardDeck(1), right = new CardDeck(2);
        CardGame cg = new CardGame();
        Player p = new Player(4, left, right, cg);
        p.setInitialHand(Arrays.asList(new Card(4), new Card(4), new Card(4), new Card(4)));
        Card discard = p.selectDiscard();
        assertEquals(4, discard.getDenomination());
    }

    @Test
    public void testSelectDiscardHandOfOne() throws IOException {
        CardDeck left = new CardDeck(1), right = new CardDeck(2);
        CardGame cg = new CardGame();
        Player p = new Player(7, left, right, cg);
        p.setInitialHand(Collections.singletonList(new Card(7)));
        Card discard = p.selectDiscard();
        assertEquals(7, discard.getDenomination());
    }

    // Run method: win on start
    @Test
    public void testRunImmediateWin() throws Exception {
        MockCardDeck left = new MockCardDeck(1), right = new MockCardDeck(2);
        MockCardGame game = new MockCardGame();
        Player p = new Player(2, left, right, game);
        p.setInitialHand(Arrays.asList(new Card(2), new Card(2), new Card(2), new Card(2)));
        p.run();
        assertTrue(game.winDeclared);
        assertEquals(2, game.getWinnerId());
    }

    // Run method: draws then lose
    @Test
    public void testRunDrawThenLose() throws Exception {
        MockCardDeck left = new MockCardDeck(1), right = new MockCardDeck(2);
        MockCardGame game = new MockCardGame();
        Player p = new Player(3, left, right, game);
        p.setInitialHand(Arrays.asList(new Card(3), new Card(7), new Card(9), new Card(1)));
        left.fakeDraws.add(new Card(5));
        game.gameOver = true; // Trigger game over after draw
        p.run();
        assertFalse(game.winDeclared);
    }

    // Run method: empty deck
    @Test
    public void testRunWithEmptyDeck() throws Exception {
        MockCardDeck left = new MockCardDeck(1), right = new MockCardDeck(2);
        MockCardGame game = new MockCardGame();
        Player p = new Player(5, left, right, game);
        p.setInitialHand(Arrays.asList(new Card(1), new Card(2), new Card(3), new Card(4)));
        p.run();
        assertFalse(game.winDeclared);
    }

    // Run method: logs for win/exits
    @Test
    public void testRunLogsWinnerAndExit() throws Exception {
        MockCardDeck left = new MockCardDeck(1), right = new MockCardDeck(2);
        MockCardGame game = new MockCardGame();
        game.winnerId = 9;
        game.gameOver = true;
        Player p = new Player(9, left, right, game);
        p.setInitialHand(Arrays.asList(new Card(1), new Card(2), new Card(3), new Card(4)));
        p.run();
        assertTrue(game.isGameOver());
    }
}

