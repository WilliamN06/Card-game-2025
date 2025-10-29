package cardgame;

import cardgame.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.io.*;

public class PlayerTest {
        @Test
        public void testSetInitialHandAndHandToString() throws IOException {
                CardDeck left = new CardDeck(1), right = new CardDeck(2);
                CardGame cg = new CardGame();
                Player p = new Player(5, left, right, cg);
                List<Card> initial = Arrays.asList(new Card(2), new Card(2), new Card(2), new Card(2));
                p.setInitialHand(initial);
                String handStr = p.handToString();
                assertTrue(handStr.contains("2"));
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
        public void testSelectDiscardPrefersNonPreferred() throws IOException {
                CardDeck left = new CardDeck(1), right = new CardDeck(2);
                CardGame cg = new CardGame();
                Player p = new Player(3, left, right, cg);
                p.setInitialHand(Arrays.asList(new Card(3), new Card(7), new Card(3), new Card(2)));
                Card discard = p.selectDiscard();
                assertTrue(discard.getDenomination() != 3); // prefers not to discard preferred value
        }
}
