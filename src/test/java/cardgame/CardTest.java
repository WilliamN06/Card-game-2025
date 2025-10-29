package cardgame;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CardTest {

    @Test
    public void testConstructorAndGetDenomination() {
        Card c = new Card(5);
        assertEquals(5, c.getDenomination());
    }

    @Test
    public void testConstructorZero() {
        Card c = new Card(0);
        assertEquals(0, c.getDenomination());
        assertEquals("0", c.toString());
    }

    @Test
    public void testConstructorNegativeThrows() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Card(-1)
        );
        assertEquals("Card denomination cannot be negative", thrown.getMessage());
    }

    @Test
    public void testToString() {
        Card c = new Card(3);
        assertEquals("3", c.toString());
    }

    @Test
    public void testEqualsSelf() {
        Card c = new Card(7);
        assertEquals(c, c);
    }

    @Test
    public void testEqualsSameDenomination() {
        Card c1 = new Card(7);
        Card c2 = new Card(7);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testEqualsDifferentDenomination() {
        Card c1 = new Card(2);
        Card c2 = new Card(3);
        assertNotEquals(c1, c2);
        assertNotEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testEqualsNull() {
        Card c = new Card(2);
        assertNotEquals(c, null);
    }

    @Test
    public void testEqualsDifferentClass() {
        Card c = new Card(2);
        assertNotEquals(c, "not a card");
    }
}

