package strados2.classic;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets; // Wichtig für UTF-8
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses an xml strados2 document into a {@link ClassicPiece} board representation.
 * @author Gemini 2.5 pro Preview 03-25
 */
public class XmlParser {
static int i = 0;
    // Regex, um das content-Attribut im field-Tag zu finden
    // Macht Annahmen über die XML-Struktur, aber flexibler als Stringsuche
    private static final Pattern FIELD_CONTENT_PATTERN = Pattern.compile(
            "<field\\s+[^>]*content\\s*=\\s*\"([A-Z_]{100})\"[^>]*>", // Erwartet 100 Großbuchstaben oder _
            Pattern.CASE_INSENSITIVE);

    /**
     * Liest eine XML-Spieldatei und extrahiert die Anfangsaufstellung.
     *
     * @param xmlFilePath Der Pfad zur XML-Datei.
     * @return Ein 10x10 ClassicPiece-Array, das die Startaufstellung darstellt, oder null bei Fehlern.
     */
    public static ClassicPiece[][] parseInitialSetup(Path xmlFilePath) {
        StringBuilder xmlContent = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(xmlFilePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                xmlContent.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei " + xmlFilePath + ": " + e.getMessage());
            return null;
        }
        Matcher matcher = FIELD_CONTENT_PATTERN.matcher(xmlContent);
        if (matcher.find()) {
            String contentString = matcher.group(1); // Gruppe 1 ist der Inhalt in den Anführungszeichen
            if (contentString != null && contentString.length() == 100) {
                return createBoardFromString(contentString);
            } else {
                System.err.println("Fehler: 'content'-String in " + xmlFilePath + " hat ungültige Länge: " + (contentString != null ? contentString.length() : "null"));
                return null;
            }
        } else {
            System.err.println("Fehler: Konnte kein '<field content=\"...\">' Tag in " + xmlFilePath + " finden.");
            return null;
        }
    }

    /**
     * Erstellt das 10x10 Spielbrett aus dem 100-Zeichen-String.
     *
     * @param contentString Der 100-Zeichen-String aus dem content-Attribut.
     * @return Das 10x10 ClassicPiece-Array.
     */
    private static ClassicPiece[][] createBoardFromString(String contentString) {
        ClassicPiece[][] board = new ClassicPiece[10][10]; // 10 Reihen, 10 Spalten
        for (int i = 0; i < 100; i++) {
            int row = i / 10; // Integer-Division gibt die Reihe (0-9)
            int col = i % 10; // Modulo gibt die Spalte (0-9)
            char pieceChar = contentString.charAt(i);
            board[col][row] = ClassicPiece.fromChar(pieceChar);
        }
        return board;
    }

    /**
     * Hilfsmethode zum Drucken des Bretts in der Konsole.
     * @param board Das zu druckende Brett.
     */
    public static void printBoard(ClassicPiece[][] board) {
        if (board == null) {
            System.out.println("Brett ist null.");
            return;
        }
        System.out.println("   0  1  2  3  4  5  6  7  8  9");
        System.out.println("  -------------------------------");
        for (int row = 0; row < 10; row++) {
            System.out.print(row + "|");
            for (int col = 0; col < 10; col++) {
                System.out.printf("%-3s", board[row][col]); // Links ausgerichtet, 3 Zeichen breit
            }
            System.out.println("|");
        }
        System.out.println("  -------------------------------");
    }
}