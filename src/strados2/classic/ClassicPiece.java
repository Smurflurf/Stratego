package strados2.classic;

import java.util.Map;
import java.util.HashMap;
/**
 * Classic Piece representation
 * @author Gemini 2.5 pro Preview 03-25, modified by Simon Stumpf
 */
public class ClassicPiece {
    private final ClassicRank rank;
    private final ClassicColor color;

    public ClassicPiece(ClassicRank rank, ClassicColor color) {
        this.rank = rank;
        this.color = color;
    }

    public ClassicRank getRank() {
        return rank;
    }

    public ClassicColor getColor() {
        return color;
    }
    
    public static ClassicPiece fromChar(char c) {
        // Fallback für unbekannte Zeichen
        return charToPieceMap.getOrDefault(c, new ClassicPiece(ClassicRank.UNKNOWN, ClassicColor.NONE));
    }
    
    @Override
    public String toString() {
        return color.getSymbol() + rank.getSymbol();
    }

    private static final Map<Character, ClassicPiece> charToPieceMap = new HashMap<>();

    static {
        // Empty and Lake
        charToPieceMap.put('A', new ClassicPiece(ClassicRank.EMPTY, ClassicColor.NONE));

        // Red Pieces (B-M)
        charToPieceMap.put('B', new ClassicPiece(ClassicRank.BOMBE, ClassicColor.RED));
        charToPieceMap.put('C', new ClassicPiece(ClassicRank.SPIONIN, ClassicColor.RED));      // 1
        charToPieceMap.put('D', new ClassicPiece(ClassicRank.SPAEHER, ClassicColor.RED));    // 2
        charToPieceMap.put('E', new ClassicPiece(ClassicRank.MINEUR, ClassicColor.RED));    // 3
        charToPieceMap.put('F', new ClassicPiece(ClassicRank.SERGEANT, ClassicColor.RED)); // 4
        charToPieceMap.put('G', new ClassicPiece(ClassicRank.LIEUTENANT, ClassicColor.RED));// 5
        charToPieceMap.put('H', new ClassicPiece(ClassicRank.CAPTAIN, ClassicColor.RED));  // 6
        charToPieceMap.put('I', new ClassicPiece(ClassicRank.MAJOR, ClassicColor.RED));    // 7
        charToPieceMap.put('J', new ClassicPiece(ClassicRank.COLONEL, ClassicColor.RED));  // 8
        charToPieceMap.put('K', new ClassicPiece(ClassicRank.GENERAL, ClassicColor.RED));  // 9
        charToPieceMap.put('L', new ClassicPiece(ClassicRank.MARSCHALL, ClassicColor.RED));  // X (10)
        charToPieceMap.put('M', new ClassicPiece(ClassicRank.FLAGGE, ClassicColor.RED));     // F

        // Blue Pieces (N-Y)
        charToPieceMap.put('N', new ClassicPiece(ClassicRank.BOMBE, ClassicColor.BLUE));
        charToPieceMap.put('O', new ClassicPiece(ClassicRank.BOMBE, ClassicColor.BLUE));     // 1
        charToPieceMap.put('P', new ClassicPiece(ClassicRank.SPAEHER, ClassicColor.BLUE));   // 2
        charToPieceMap.put('Q', new ClassicPiece(ClassicRank.MINEUR, ClassicColor.BLUE));   // 3
        charToPieceMap.put('R', new ClassicPiece(ClassicRank.SERGEANT, ClassicColor.BLUE)); // 4
        charToPieceMap.put('S', new ClassicPiece(ClassicRank.LIEUTENANT, ClassicColor.BLUE));// 5
        charToPieceMap.put('T', new ClassicPiece(ClassicRank.CAPTAIN, ClassicColor.BLUE)); // 6
        charToPieceMap.put('U', new ClassicPiece(ClassicRank.MAJOR, ClassicColor.BLUE));   // 7
        charToPieceMap.put('V', new ClassicPiece(ClassicRank.COLONEL, ClassicColor.BLUE)); // 8
        charToPieceMap.put('W', new ClassicPiece(ClassicRank.GENERAL, ClassicColor.BLUE)); // 9
        charToPieceMap.put('X', new ClassicPiece(ClassicRank.MARSCHALL, ClassicColor.BLUE)); // X (10)
        charToPieceMap.put('Y', new ClassicPiece(ClassicRank.FLAGGE, ClassicColor.BLUE));    // F
    }
    
    public enum ClassicRank {
        FLAGGE("F", 0, true), SPIONIN("1", 1, true), SPAEHER("2", 2, true), MINEUR("3", 3, true), SERGEANT("4", 4, false),
        LIEUTENANT("5", 5, false), CAPTAIN("6", 6, false), MAJOR("7", 7, false), COLONEL("8", 8, false),
        GENERAL("9", 9, true), MARSCHALL("X", 10, true), BOMBE("B", 11, true),
        EMPTY("-", -1, false), LAKE("#", -2, false), UNKNOWN("?", -3, false);

        private final String symbol;
        private final int strength;
        private final boolean relevant;

        ClassicRank(String symbol, int strength, boolean relevant) {
            this.symbol = symbol;
            this.strength = strength;
            this.relevant = relevant;
        }
        public String getName() { 
        	StringBuilder sb = new StringBuilder().append(this.toString().charAt(0));
        	sb.append(this.toString().substring(1).toLowerCase()); 
        	return sb.toString();
        };
        public String getSymbol() { return symbol; }
        public int getStrength() { return strength; }
        public boolean getRelevant() { return relevant; }
    }

    public enum ClassicColor {
        RED("R"), BLUE("B"), NONE("");

        private final String symbol;

        ClassicColor(String symbol) { this.symbol = symbol; }

        public String getSymbol() { return symbol; }

        @Override
        public String toString() { return symbol; }
    }

}