package cardgame;

import cardgame.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CardDeckTest {
        @Test
        public void testAddCardAndDraw() {
                CardDeck deck = new CardDeck(1);
                Card c1 = new Card(2);
                deck.addCard(c1);
                assertFalse(deck.isEmpty());
                Card drawn = deck.draw();
                assertEquals(c1, drawn);
                assertTrue(deck.isEmpty());
        }

        @Test
        public void testDrawEmptyReturnsNull() {
                CardDeck deck = new CardDeck(2);
                assertNull(deck.draw());
        }

        @Test
        public void testGetContentsString() {
                CardDeck deck = new CardDeck(3);
                deck.addCard(new Card(1));
                deck.addCard(new Card(5));
                String output = deck.getContentsString();
                assertTrue(output.contains("deck3 contents"));
                assertTrue(output.contains("1"));
                assertTrue(output.contains("5"));
        }

        @Test
        public void testGetId() {
                CardDeck deck = new CardDeck(8);
                assertEquals(8, deck.getId());
        }
}
