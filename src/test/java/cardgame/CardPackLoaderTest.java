package cardgame;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

public class CardPackLoaderTest {

    @Test
    public void testLoadPackFileNormal() throws Exception {
        File temp = File.createTempFile("testpack", ".txt");
        try (PrintWriter pw = new PrintWriter(temp)) {
            for (int i = 0; i < 8; i++) pw.println("2");
        }
        List<Card> pack = CardPackLoader.loadPack(temp, 1);
        assertEquals(8, pack.size());
        temp.delete();
    }

    @Test
    public void testLoadPackFileInvalidPlayers() {
        File temp = new File("doesnotexist.txt");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> CardPackLoader.loadPack(temp, 0));
        assertTrue(ex.getMessage().contains("Number of players"));
    }

    @Test
    public void testLoadPackFileFileNotFound() {
        File temp = new File("missing_pack.txt");
        assertThrows(FileNotFoundException.class, () -> CardPackLoader.loadPack(temp, 1));
    }

    @Test
    public void testLoadPackFileWrongCards() throws Exception {
        File temp = File.createTempFile("testpack2", ".txt");
        try (PrintWriter pw = new PrintWriter(temp)) { for (int i = 0; i < 7; i++) pw.println("3"); }
        IOException ex = assertThrows(IOException.class, () -> CardPackLoader.loadPack(temp, 1));
        assertTrue(ex.getMessage().contains("Invalid pack size"));
        temp.delete();
    }

    @Test
    public void testLoadPackFileNegativeCardValue() throws Exception {
        File temp = File.createTempFile("testpack3", ".txt");
        try (PrintWriter pw = new PrintWriter(temp)) {
            for (int i = 0; i < 7; i++) pw.println("3");
            pw.println("-2");
        }
        IOException ex = assertThrows(IOException.class, () -> CardPackLoader.loadPack(temp, 1));
        assertTrue(ex.getMessage().contains("cannot be negative"));
        temp.delete();
    }

    @Test
    public void testLoadPackFileBoundaryCardValueZeroAllowed() throws Exception {
        File temp = File.createTempFile("testpack_zero", ".txt");
        try (PrintWriter pw = new PrintWriter(temp)) {
            for (int i = 0; i < 8; i++) pw.println("0");
        }
        List<Card> pack = CardPackLoader.loadPack(temp, 1);
        assertEquals(8, pack.size());
        temp.delete();
    }

    @Test
    public void testNegativeValueLineNumberInMessage() throws Exception {
        File temp = File.createTempFile("testpack6", ".txt");
        try (PrintWriter pw = new PrintWriter(temp)) {
            for (int i = 0; i < 7; i++) pw.println("3");
            pw.println("-6");
        }
        IOException ex = assertThrows(IOException.class, () -> CardPackLoader.loadPack(temp, 1));
        assertTrue(ex.getMessage().contains("line 8"));
        temp.delete();
    }

    @Test
    public void testLoadPackFileNonIntegerCardValue() throws Exception {
        File temp = File.createTempFile("testpack4", ".txt");
        try (PrintWriter pw = new PrintWriter(temp)) {
            for (int i = 0; i < 7; i++) pw.println("3");
            pw.println("hello");
        }
        IOException ex = assertThrows(IOException.class, () -> CardPackLoader.loadPack(temp, 1));
        assertTrue(ex.getMessage().contains("must be an integer"));
        temp.delete();
    }

    @Test
    public void testLoadPackSkipEmptyLines() throws Exception {
        File temp = File.createTempFile("testpack5", ".txt");
        try (PrintWriter pw = new PrintWriter(temp)) {
            pw.println("   ");
            for (int i = 0; i < 8; i++) pw.println("2");
            pw.println("");
        }
        List<Card> pack = CardPackLoader.loadPack(temp, 1);
        assertEquals(8, pack.size());
        temp.delete();
    }

    @Test
    public void testPackSizeWithMultiplePlayers() throws Exception {
        File temp = File.createTempFile("testpack7", ".txt");
        try (PrintWriter pw = new PrintWriter(temp)) { for (int i = 0; i < 16; i++) pw.println("2"); }
        List<Card> pack = CardPackLoader.loadPack(temp, 2);
        assertEquals(16, pack.size());
        temp.delete();

        File temp2 = File.createTempFile("testpack8", ".txt");
        try (PrintWriter pw = new PrintWriter(temp2)) { for (int i = 0; i < 8; i++) pw.println("2"); }
        assertThrows(IOException.class, () -> CardPackLoader.loadPack(temp2, 2));
        temp2.delete();
    }

    @Test
    public void testPrintPackStatisticsOutput() {
        List<Card> pack = Arrays.asList(new Card(2), new Card(2), new Card(3), new Card(4), new Card(0));
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        CardPackLoader.printPackStatistics(pack, 1);
        System.setOut(originalOut);
        String output = outContent.toString();
        assertTrue(output.contains("Pack loaded successfully") || output.contains("Pack Statistics"));
        assertTrue(output.contains("Total cards: 5"));
        assertTrue(output.contains("Unique values: 4"));
        assertTrue(output.contains("Value 2: 2 cards"));
        assertTrue(output.contains("Value 0: 1 cards"));
        assertTrue(output.contains("Value 3: 1 cards"));
    }
}

