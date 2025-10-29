package cardgame;

import cardgame.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.*;

public class CardPackLoaderTest {
        @Test
        public void testLoadPackFileNormal() throws Exception {
                File temp = File.createTempFile("testpack", ".txt");
                try (PrintWriter pw = new PrintWriter(temp)) {
                        for (int i = 0; i < 8; i++)
                                pw.println("2");
                }
                List<Card> pack = CardPackLoader.loadPack(temp, 1);
                assertEquals(8, pack.size());
                temp.delete();
        }

        @Test
        public void testLoadPackFileInvalidPlayers() {
                File temp = new File("doesnotexist.txt");
                assertThrows(IllegalArgumentException.class, () -> CardPackLoader.loadPack(temp, 0));
        }

        @Test
        public void testLoadPackFileFileNotFound() {
                File temp = new File("missing_pack.txt");
                assertThrows(FileNotFoundException.class, () -> CardPackLoader.loadPack(temp, 1));
        }

        @Test
        public void testLoadPackFileWrongCards() throws Exception {
                File temp = File.createTempFile("testpack2", ".txt");
                try (PrintWriter pw = new PrintWriter(temp)) {
                        for (int i = 0; i < 7; i++)
                                pw.println("3");
                }
                assertThrows(IOException.class, () -> CardPackLoader.loadPack(temp, 1));
                temp.delete();
        }

        @Test
        public void testPrintPackStatisticsForPack() {
                List<Card> pack = Arrays.asList(new Card(2), new Card(2), new Card(3), new Card(4));
                // Just check for no exception
                CardPackLoader.printPackStatistics(pack, 1);
        }
}
