package cardgame;

import cardgame.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CardTest {
        @Test
        public void testConstructorAndGetDenomination() {
                Card c = new Card(5);
                assertEquals(5, c.getDenomination());
        }

        @Test
        public void testConstructorNegativeThrows() {
                assertThrows(IllegalArgumentException.class, () -> new Card(-1));
        }

        @Test
        public void testEqualsAndHashCode() {
                Card c1 = new Card(7);
                Card c2 = new Card(7);
                assertEquals(c1, c2);
                assertEquals(c1.hashCode(), c2.hashCode());
        }

        @Test
        public void testToString() {
                Card c = new Card(3);
                assertEquals("3", c.toString());
        }
}
