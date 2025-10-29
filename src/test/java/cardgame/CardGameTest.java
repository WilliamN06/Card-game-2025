package cardgame;

import cardgame.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.*;

public class CardGameTest {
        @Test
        public void testDeclareWinner() {
                CardGame game = new CardGame();
                assertFalse(game.isGameOver());
                game.declareWinner(3);
                assertTrue(game.isGameOver());
                assertEquals(3, game.getWinnerId());
        }

        @Test
        public void testInitialiseAndDistributeHands() throws IOException {
                CardGame game = new CardGame();
                int numPlayers = 2;
                List<Card> pack = Arrays.asList(
                                new Card(1), new Card(1), new Card(1), new Card(1),
                                new Card(2), new Card(2), new Card(2), new Card(2));
                // We just want the init and distribute functions to run and not throw
                game.initialiseGame(numPlayers, pack);
        }
}
