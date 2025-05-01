package strados2.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import strados2.classic.ClassicPiece;
import strados2.classic.ClassicPiece.ClassicRank;
import strados2.tools.GeneralTools.RelativePosition;

/**
 * Saves and loads piece neighbor counts, associating them with piece types.
 * Uses strados2.classic.ClassicPiece.ClassicRank and GeneralTools.RelativePosition for saving identifiers
 * @author Gemini 2.5 pro Preview 03-25 and by Simon Stumpf
 */
public class NeighborIO {
	public static final String location = "src" + File.separator + "core" + File.separator + "placing" + File.separator;
	public static final String pyLocation = "src" + File.separator + "strados2" + File.separator + "py" + File.separator + "piece_distributions_";
	public static final String filename = location + "neighborCounts_";

	/**
	 * Prints all neighbor Counts to console, with rank1, relative position, rank2
	 * @param neighborCounts
	 */
	public static void printNeighborCounts(Map<ClassicPiece.ClassicRank, Map<RelativePosition, Map<ClassicPiece.ClassicRank, Double>>> neighborCounts) {
		for(ClassicRank rank : neighborCounts.keySet()) {
			System.out.println(rank + ": ");
			for(RelativePosition pos : neighborCounts.get(rank).keySet()) {
				System.out.print("\t" + pos.name() + " : \n");
				for(ClassicRank rank2 : neighborCounts.get(rank).get(pos).keySet()) {
					System.out.print("\t\t" + rank2 + " : ");
					System.out.println(neighborCounts.get(rank).get(pos).get(rank2));	
				}
			}
		}
	}
	
	/**
	 * Prints a LaTeX ready table containing the probabilities that one piece is next to another.
	 * @param ranks
	 * @param relevantBoards
	 * @param neighborCounts
	 */
	public static void printNeighborTable(
			ClassicRank[] ranks,
			Map<ClassicPiece.ClassicRank, Map<RelativePosition, Map<ClassicPiece.ClassicRank, Double>>> neighborCounts) {
		System.out.print("\n& ");
		for(ClassicRank rank2 : ranks)
			System.out.print((rank2.getName().substring(0,3) + (rank2 == ClassicRank.MARSCHALL ? " \\\\" : " & ")));
		System.out.print("\n\\hline");
		
		for(ClassicRank rank : ranks) {
			System.out.print("\n" + rank.getName() + " & ");
			for(ClassicRank rank2 : ranks) {
				double count = 0;
				for(GeneralTools.RelativePosition pos : GeneralTools.RelativePosition.values()) {
					if(!neighborCounts.containsKey(rank2) 
							|| !neighborCounts.get(rank).containsKey(pos) 
							|| !neighborCounts.get(rank).get(pos).containsKey(rank2)) continue;
					if(neighborCounts.get(rank).get(pos).get(rank2) != null)
						count += neighborCounts.get(rank).get(pos).get(rank2);
				}
				double prob = Math.round(count* 100.) / 100.;
				System.out.print((
						(prob == 0.0 ? "0" : prob)
						+ (rank2 == ClassicRank.MARSCHALL ? " \\\\" : " & "))
						);
			}
		}
	}
	
	/**
	 * Speichert die Nachbarschaftszählungen in eine Datei.
	 *
	 * @param neighborCounts Die zu speichernde Map.
	 * @param filename Der Dateiname.
	 * @return true bei Erfolg, false bei Fehlern.
	 */
	public static boolean saveNeighborCounts(
			String mode,
			Map<ClassicPiece.ClassicRank, Map<RelativePosition, Map<ClassicPiece.ClassicRank, Double>>> neighborCounts,
			String... location) {
		String filename = location.length > 0 ? location[0] : NeighborIO.filename + mode + ".txt";

		if (neighborCounts == null) {
			System.err.println("Error saving neighbor counts: Input map is null.");
			return false;
		}

		return write(neighborCounts, filename);
	}

	private static boolean write(
			Map<ClassicPiece.ClassicRank, Map<RelativePosition, Map<ClassicPiece.ClassicRank, Double>>> neighborCounts,
			String filename) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			// Sortiere optional nach Rank1 für konsistente Ausgabe
			for (ClassicPiece.ClassicRank rank1 : ClassicPiece.ClassicRank.values()) {
				if (!neighborCounts.containsKey(rank1)) continue; // Nur vorhandene Ränge speichern
				if (rank1 == ClassicPiece.ClassicRank.EMPTY || rank1 == ClassicPiece.ClassicRank.LAKE || rank1 == ClassicPiece.ClassicRank.UNKNOWN) continue; // Irrelevante Ränge überspringen


				Map<RelativePosition, Map<ClassicPiece.ClassicRank, Double>> positionMap = neighborCounts.get(rank1);
				writer.write(rank1.name()); // Schreibe Rank1 Namen
				writer.newLine();

				// Sortiere optional nach Position für konsistente Ausgabe
				for (RelativePosition position : RelativePosition.values()) {
					if (!positionMap.containsKey(position)) continue; // Nur vorhandene Positionen

					Map<ClassicPiece.ClassicRank, Double> rank2Map = positionMap.get(position);
					writer.write("\t" + position.name()); // Schreibe Positionsnamen (eingerückt)
					writer.newLine();

					// Sortiere optional nach Rank2 für konsistente Ausgabe
					for (ClassicPiece.ClassicRank rank2 : ClassicPiece.ClassicRank.values()) {
						if (!rank2Map.containsKey(rank2)) continue; // Nur vorhandene Rank2
						if (rank2 == ClassicPiece.ClassicRank.EMPTY || rank2 == ClassicPiece.ClassicRank.LAKE || rank2 == ClassicPiece.ClassicRank.UNKNOWN) continue; // Irrelevante Ränge überspringen

						Double count = rank2Map.get(rank2);
						writer.write("\t\t" + rank2.name() + " " + count); // Schreibe Rank2 + Count (doppelt eingerückt)
						writer.newLine();
					}
				}
			}
			System.out.println("Successfully saved neighbor counts to: " + filename);
			return true;
		} catch (IOException e) {
			System.err.println("Error writing neighbor counts to file '" + filename + "': " + e.getMessage());
			return false;
		}
	}

	/**
	 * Lädt die Nachbarschaftszählungen aus einer Datei.
	 *
	 * @param filename Der Dateiname.
	 * @return Die geladene Map oder null bei Fehlern.
	 */
	public static Map<ClassicPiece.ClassicRank, Map<RelativePosition, Map<ClassicPiece.ClassicRank, Double>>> loadNeighborCounts(String mode) {
		String filename = NeighborIO.filename + mode + ".txt";
		
		Map<ClassicPiece.ClassicRank, Map<RelativePosition, Map<ClassicPiece.ClassicRank, Double>>> loadedCounts =
				new EnumMap<>(ClassicPiece.ClassicRank.class);
		ClassicPiece.ClassicRank currentRank1 = null;
		RelativePosition currentPosition = null;
		Map<RelativePosition, Map<ClassicPiece.ClassicRank, Double>> currentPositionMap = null;
		Map<ClassicPiece.ClassicRank, Double> currentRank2Map = null;

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			String line;
			int lineNumber = 0;
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				String trimmedLine = line.trim();
				if (trimmedLine.isEmpty()) continue; // Leere Zeilen überspringen

				try {
					if (line.startsWith("\t\t")) { // Rank2 Count Zeile
						if (currentRank1 == null || currentPosition == null || currentRank2Map == null) {
							throw new IOException("Invalid file format: Rank2/Count line found without preceding Rank1/Position at line " + lineNumber);
						}
						String[] parts = trimmedLine.split("\\s+"); // Trenne bei Leerzeichen
						if (parts.length != 2) {
							throw new IOException("Invalid file format: Expected Rank2 Name and Count at line " + lineNumber + ", found: '" + trimmedLine + "'");
						}
						ClassicPiece.ClassicRank rank2 = ClassicPiece.ClassicRank.valueOf(parts[0]);
						double count = Double.parseDouble(parts[1]);
						currentRank2Map.put(rank2, count);

					} else if (line.startsWith("\t")) { // Position Zeile
						if (currentRank1 == null || currentPositionMap == null) {
							throw new IOException("Invalid file format: Position line found without preceding Rank1 at line " + lineNumber);
						}
						currentPosition = RelativePosition.valueOf(trimmedLine);
						currentRank2Map = currentPositionMap.computeIfAbsent(currentPosition, k -> new EnumMap<>(ClassicPiece.ClassicRank.class));

					} else { // Rank1 Zeile
						currentRank1 = ClassicPiece.ClassicRank.valueOf(trimmedLine);
						currentPositionMap = loadedCounts.computeIfAbsent(currentRank1, k -> new EnumMap<>(RelativePosition.class));
						currentPosition = null; // Reset position/rank2 map for new rank1
						currentRank2Map = null;
					}
				} catch (IllegalArgumentException |ArrayIndexOutOfBoundsException e) {
					throw new IOException("Invalid enum name or format error at line " + lineNumber + ": '" + trimmedLine + "'", e);
				} catch (Exception e) { // Catch other potential errors during parsing/map access
					throw new IOException("Error processing line " + lineNumber + ": '" + line + "'", e);
				}
			}
			System.out.println("Successfully loaded neighbor counts from: " + filename);
			return loadedCounts;
		} catch (IOException e) {
			System.err.println("Error reading neighbor counts from file '" + filename + "': " + e.getMessage());
			return null;
		}
	}
}
