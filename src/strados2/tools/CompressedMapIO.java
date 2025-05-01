package strados2.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap; // Import HashMap
import java.util.Map;     // Import Map
import java.util.StringJoiner;

import strados2.classic.ClassicPiece; // Import für ClassicPiece.ClassicRank
import core.PieceType;                 // Import für core.PieceType

/**
 * Saves and loads piece distribution heatmaps, associating them with piece types.
 * Uses strados2.classic.ClassicPiece.ClassicRank for saving identifiers
 * and maps them to core.PieceType upon loading.
 *
 * @author Gemini 2.5 pro Preview 03-25, modified by Simon Stumpf
 */
public class CompressedMapIO {
	public static final String location = "src" + File.separator + "core" + File.separator + "placing" + File.separator;
	public static final String pyLocation = "src" + File.separator + "strados2" + File.separator + "py" + File.separator + "piece_distributions_";
    public static final String filename = location + "piece_distributions_";
    /**
     * Saves loaded maps, {@link #loadCompressedMaps()} will always return a clone of the here loaded map to minimize IO time.
     */
    private static HashMap<String, Map<PieceType, int[][]>> loadedMaps = new HashMap<String, Map<PieceType, int[][]>>();

    /**
     * Saves an int[][][] Array associated with ClassicPiece Ranks to a file.
     * File Format:
     * Line 1: numberOfRanks xDim yDim
     * Then for each rank:
     *   Line: CLASSIC_RANK_NAME (e.g., "BOMBE")
     *   Next yDim lines: int[][] data for this rank, row by row, values separated by spaces.
     *
     * @param data  The int[][][] heatmap data. data.length must equal ranks.length.
     * @param ranks The strados2.classic.ClassicPiece.ClassicRank array corresponding to the first dimension of data.
     * @param location to use something else than the default location, e.g. for testing
     * @return false if anything went wrong, true otherwise.
     */
    public static boolean saveCompressedMaps(String mode, int[][][] data, ClassicPiece.ClassicRank[] ranks, String... location) {
    	String filename = location.length > 0 ? location[0] : CompressedMapIO.filename + mode + ".txt";
        if (data == null || ranks == null || data.length == 0 || ranks.length == 0 || data.length != ranks.length) {
            System.err.println("Error saving: Input data or ranks array is null, empty, or lengths do not match.");
            return false;
        }
        if (data[0] == null || data[0].length == 0 || data[0][0] == null || data[0][0].length == 0) {
             System.err.println("Error saving: Input data array has invalid inner dimensions.");
             return false;
        }


        int numRanks = data.length;
        int xDim = data[0].length;
        int yDim = data[0][0].length;

        // Optional: Deeper consistency check (already partially done in original code)
        for (int r = 0; r < numRanks; r++) {
            if (data[r] == null || data[r].length != xDim || data[r][0] == null || data[r][0].length != yDim) {
                System.err.println("Error saving: Inconsistent dimensions at rank index " + r);
                return false;
            }
        }

        if(location.length == 0)
        	write(numRanks, xDim, yDim, ranks, data, (CompressedMapIO.pyLocation + mode + ".txt"));
        return write(numRanks, xDim, yDim, ranks, data, filename);
    }
    
    private static boolean write(int numRanks, int xDim, int yDim, ClassicPiece.ClassicRank[] ranks, int[][][] data, String filename) {
    	try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // 1. Write dimensions (number of ranks, x, y)
            writer.write(numRanks + " " + xDim + " " + yDim);
            writer.newLine();

            // 2. Write data Rank by Rank
            for (int r = 0; r < numRanks; r++) {
                // 2a. Write the Classic Rank Name
                writer.write(ranks[r].name()); // Use .name() to get the enum constant name (e.g., "BOMBE")
                writer.newLine();

                // 2b. Write the int[][] data for this rank
                int[][] currentRankMap = data[r];
                for (int y = 0; y < yDim; y++) {
                    StringJoiner sj = new StringJoiner(" ");
                    for (int x = 0; x < xDim; x++) {
                        sj.add(String.valueOf(currentRankMap[x][y])); // Assuming x is outer, y is inner for the int[][]
                    }
                    writer.write(sj.toString());
                    writer.newLine();
                }
            }
            System.out.println("Successfully saved compressed maps with rank names to: " + filename);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing compressed maps to file '" + filename + "': " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Loads piece distribution heatmaps from a file created by {@link #saveCompressedMaps(int[][][], ClassicPiece.ClassicRank[])}.
     * Maps the saved Classic Rank names to core.PieceType.
     * Saves the loaded map into {@link #loadedMap} to clone it in the future, minimizing IO times.
     *
     * @return A Map where keys are core.PieceType and values are the corresponding int[][] heatmaps.
     *         Returns null if loading fails (file not found, format error, etc.).
     *         Mappings for unknown/unsupported Classic Ranks might be skipped.
     */
    public static Map<PieceType, int[][]> loadCompressedMaps(String mode) {
    	String filename = CompressedMapIO.filename + mode + ".txt";
    	if(loadedMaps.containsKey(mode)) {
    		Map<PieceType, int[][]> loadedMap = loadedMaps.get(mode);
    		Map<PieceType, int[][]> map = new HashMap<>();
    		for(PieceType type : loadedMap.keySet())
    			map.put(type, deepCloneArray(loadedMap.get(type)));
    		return map;
    	}
    	
        Map<PieceType, int[][]> loadedData = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // 1. Read dimensions
            String dimensionLine = reader.readLine();
            if (dimensionLine == null) {
                System.err.println("Error loading: File is empty or invalid: " + filename);
                return null;
            }
            String[] dims = dimensionLine.trim().split("\\s+");
            if (dims.length != 3) {
                System.err.println("Error loading: Invalid dimension line format in file: " + filename);
                return null;
            }

            int expectedRanks, xDim, yDim;
            try {
                expectedRanks = Integer.parseInt(dims[0]);
                xDim = Integer.parseInt(dims[1]);
                yDim = Integer.parseInt(dims[2]);
                if (expectedRanks <= 0 || xDim <= 0 || yDim <= 0) {
                    System.err.println("Error loading: Invalid dimensions (<= 0) in file: " + filename);
                    return null;
                }
            } catch (NumberFormatException e) {
                System.err.println("Error loading: Could not parse dimensions in file: " + filename + " - " + e.getMessage());
                return null;
            }

            // 2. Read data for each expected rank
            for (int r = 0; r < expectedRanks; r++) {
                // 2a. Read the Classic Rank Name line
                String classicRankNameLine = reader.readLine();
                if (classicRankNameLine == null) {
                    System.err.println("Error loading: Unexpected end of file while expecting rank name for rank index " + r + " in file: " + filename);
                    return null;
                }
                String classicRankName = classicRankNameLine.trim();

                // 2b. Map Classic Rank Name to core.PieceType
                PieceType coreType = mapClassicRankToCorePieceType(classicRankName);

                // 2c. Create and populate the int[][] map for this rank
                int[][] rankMap = new int[xDim][yDim];
                for (int y = 0; y < yDim; y++) {
                    String dataLine = reader.readLine();
                    if (dataLine == null) {
                        System.err.println("Error loading: Unexpected end of file while reading data for rank '" + classicRankName + "', y=" + y + " in file: " + filename);
                        return null;
                    }
                    String[] values = dataLine.trim().split("\\s+");
                    if (values.length != xDim) {
                        System.err.println("Error loading: Incorrect number of values (" + values.length + ") for xDim (" + xDim + ") for rank '" + classicRankName + "', y=" + y + " in file: " + filename);
                        return null;
                    }

                    try {
                        for (int x = 0; x < xDim; x++) {
                            rankMap[x][y] = Integer.parseInt(values[x]);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error loading: Could not parse integer value for rank '" + classicRankName + "', y=" + y + " in file: " + filename + " - " + e.getMessage());
                        return null; // Or handle more gracefully depending on needs
                    }
                }

                // 2d. Add to map IF the coreType is valid/known
                if (coreType != null && coreType != PieceType.UNKNOWN) {
                     if (loadedData.containsKey(coreType)) {
                         // This shouldn't happen if the input file is well-formed
                         // but handle it just in case (e.g., log a warning, overwrite, skip)
                         System.err.println("Warning: Duplicate entry found for core.PieceType." + coreType.name() + ". Overwriting previous data.");
                     }
                     loadedData.put(coreType, rankMap);
                } else {
                    System.out.println("Info: Skipped loading data for unmapped or unknown classic rank: '" + classicRankName + "'");
                }
            }

//            System.out.println("Successfully loaded compressed maps from: " + filename + " into Map<core.PieceType, int[][]>");
            loadedMaps.put(mode, loadedData);
            return loadedData;

        } catch (IOException e) {
            System.err.println("Error reading compressed maps from file '" + filename + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to map the String name of a ClassicPiece.ClassicRank
     * to the corresponding core.PieceType enum constant.
     *
     * @param classicRankName The name string (e.g., "BOMBE", "FLAGGE").
     * @return The matching core.PieceType, or core.PieceType.UNKNOWN if no match found.
     */
    private static PieceType mapClassicRankToCorePieceType(String classicRankName) {
        // Using equalsIgnoreCase for robustness, although enum names are typically uppercase
        if (classicRankName.equalsIgnoreCase("MARSCHALL")) return PieceType.MARSCHALL;
        if (classicRankName.equalsIgnoreCase("GENERAL")) return PieceType.GENERAL;
        if (classicRankName.equalsIgnoreCase("MINEUR")) return PieceType.MINEUR;
        if (classicRankName.equalsIgnoreCase("SPAEHER")) return PieceType.SPAEHER;
        if (classicRankName.equalsIgnoreCase("SPIONIN")) return PieceType.SPIONIN;
        if (classicRankName.equalsIgnoreCase("BOMBE")) return PieceType.BOMBE;
        if (classicRankName.equalsIgnoreCase("FLAGGE")) return PieceType.FLAGGE;

        // Add other mappings if core.PieceType expands or if other classic ranks are relevant
        // e.g., if (classicRankName.equalsIgnoreCase("SERGEANT")) return PieceType.SERGEANT;

        // Default for unmapped types
        return PieceType.UNKNOWN;
    }
    
    /**
     * Helper Method to deep clone two dimensional arrays
     * @param array
     * @return clone of array
     */
    static int[][] deepCloneArray(int[][] array){
    	int[][] clone = new int[array.length][array[0].length];
    	for(int i=0; i<array.length; i++)
    		clone[i] = array[i].clone();
    	return clone;
    }
}