package strados2.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.internal.chartpart.Chart;

import it.unimi.dsi.fastutil.Pair;
import strados2.classic_board_representation.ClassicPiece;
import strados2.classic_board_representation.ClassicPiece.ClassicColor;
import strados2.classic_board_representation.ClassicPiece.ClassicRank;

public class GeneralTools {
	public static final String folderName = "charts";
	public static final int BOARD_SIZE = 10;
	static final int[] COL_LABELS = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	static final int[] ROW_LABELS = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

	/**
	 * Compresses an Array that contains 10 entries for rows into only 8 by compressing the 2,3 and 6,7 entry into one.
	 * @param xPercentagesRaw
	 * @return compressed Array containing 8 entries
	 */
	public static int[] compressX(int[] xPercentagesRaw) {
		int[] xPercentagesCompressed = new int[8];
		for(int i=0, compressed=0; i<xPercentagesRaw.length; i++, compressed++) {
			xPercentagesCompressed[compressed] = xPercentagesCompressed[compressed] > xPercentagesRaw[i] ? xPercentagesCompressed[compressed] : xPercentagesRaw[i];
			if(i==2 || i==6)
				--compressed;
		}
		return xPercentagesCompressed;
	}

	/**
	 * Compresses an Array that contains 5 entries for columns into only 4 by compressing the 1,2 entry into one.
	 * @param yPercentagesRaw
	 * @return compressed Array containing 4 entries, last one is 0.
	 */
	public static int[] compressY(int[] yPercentagesRaw) {
		int[] yPercentagesCompressed = new int[4];
		for(int i=0, compressed=0; i<yPercentagesRaw.length -1; i++, compressed++) {
			yPercentagesCompressed[compressed] += yPercentagesRaw[i];
			if(i==1)
				--compressed;
			else if(i==2)
				yPercentagesCompressed[compressed] = Math.round(yPercentagesCompressed[compressed] / 2);
		}
		yPercentagesCompressed[3] = 0;
		return yPercentagesCompressed;
	}

	/**
	 * Saves chart as a png and svg
	 *
	 * @param chart saves this chart
	 * @param baseFileName name of the saved file
	 */
	public static void saveChart(@SuppressWarnings("rawtypes") Chart chart, String baseFileName) {
		baseFileName =  folderName + File.separator + baseFileName;
		try {
			if(!Files.exists(Paths.get(folderName)))
				Files.createDirectory(Paths.get(folderName));
			BitmapEncoder.saveBitmapWithDPI(chart, baseFileName + ".png", BitmapEncoder.BitmapFormat.PNG, 300);
			System.out.println("Chart gespeichert als: " + baseFileName + ".png");

			VectorGraphicsEncoder.saveVectorGraphic(chart, baseFileName + ".svg", VectorGraphicsEncoder.VectorGraphicsFormat.SVG);
			System.out.println("Chart gespeichert als: " + baseFileName + ".svg");

		} catch (IOException e) {
			System.err.println("Fehler beim Speichern des Diagramms '" + baseFileName + "': " + e.getMessage());
		}
	}

	/**
	 * Aggregates a (or all) Pieces location over the boards.
	 * Ignores all Pieces which are not part of Stratego: Quick Battle
	 *
	 * @param allBoards list of ClassicPiece[][] boards to analyse
	 * @param targetRank PieceType to count or null for all Pieces
	 * @return 10x10 int-Array with aggregated Piece counts
	 */
	public static int[][] aggregatePlacementCounts(List<ClassicPiece[][]> allBoards, ClassicRank targetRank) {
		int[][] counts = new int[BOARD_SIZE][BOARD_SIZE];
		for (int i=0; i< allBoards.size(); i++) {
			if (allBoards.get(i) == null) continue;
			for (int x = 0; x<BOARD_SIZE; x++) {
				for (int y=0; y<BOARD_SIZE; y++) {
					ClassicPiece piece = allBoards.get(i)[x][y];
					if (piece != null && piece.getColor() != ClassicColor.NONE && piece.getRank().getRelevant()) {
						if (targetRank == null) { // Zähle jede Figur
							counts[x][y]++;
						} else if (piece.getRank() == targetRank) { // Zähle nur den Ziel-Rang
							counts[x][y]++;
						}
					}
				}
			}
		}
		return counts;
	}

	public static int[][] half(int[][] boards) {
		int[][] half = new int[10][5];
		for(int x=0; x<10; x++)
			for(int y=0; y<boards[0].length; y++)
				if(y<boards[0].length / 2)
					half[x][y] += boards[x][y];
				else
					half[x][boards[0].length -1 -y] += boards[x][y];
		return half;
	}

	public static int[][] percentageMap(int[][] boards ){
		double total = 0;
		for(int[] i : boards)
			for(int ii : i)
				total += ii;

		int[][] percentage = new int[boards.length][boards[0].length];
		for(int x=0; x<boards.length; x++)
			for(int y=0; y<boards[0].length; y++)
				percentage[x][y] = (int)Math.round((boards[x][y] / total) * 100);
		return percentage;
	}

	/** 
	 * Extrahiert die Häufigkeit, mit der Paare von Figurentypen benachbart sind.
	 *
	 * @param boards Liste der ClassicPiece[][] Spielbretter
	 * @param mode Spielmodus
	 * @return Map<Rank1, Map<RelativePosition, Map<Rank2, Count>>>
	 */
	public static Map<ClassicRank, Map<RelativePosition, Map<ClassicRank, Double>>> neighborCounts(ArrayList<ClassicPiece[][]> boards, String mode) {
		Map<ClassicRank, Map<RelativePosition, Map<ClassicRank, Integer>>> neighborCounts = new EnumMap<>(ClassicRank.class);

		fillNeighborCounts(boards, neighborCounts);

		Map<ClassicRank, Map<RelativePosition, Map<ClassicRank, Double>>> neighborCountsProb = new EnumMap<>(ClassicRank.class);
		for(ClassicRank rank : neighborCounts.keySet()) {
			HashMap<RelativePosition, Map<ClassicRank, Double>> posProbMap = new HashMap<RelativePosition, Map<ClassicRank, Double>>();
			for(RelativePosition pos : neighborCounts.get(rank).keySet()) {
				Map<ClassicRank, Double> probMap = new HashMap<ClassicRank, Double>();
				double allNeighbors = 0;
				for(ClassicRank rank2 : neighborCounts.get(rank).get(pos).keySet())
					allNeighbors += neighborCounts.get(rank).get(pos).get(rank2);

				for(ClassicRank rank2 : neighborCounts.get(rank).get(pos).keySet()) {
					int count = neighborCounts.get(rank).get(pos).get(rank2);
					probMap.put(rank2, count / allNeighbors);
					posProbMap.put(pos, probMap);
				}
			}
			neighborCountsProb.put(rank, posProbMap);
		}

		return neighborCountsProb;
	}

	public static void fillNeighborCounts(ArrayList<ClassicPiece[][]> boards, Map<ClassicRank, Map<RelativePosition, Map<ClassicRank, Integer>>> neighborCounts) {
		for(ClassicPiece[][] board : boards) {
			if(board == null) continue;

			for(int y=0; y<10; y++) {
				for(int x=0; x<10; x++) {
					ClassicPiece piece = board[x][y];
					if(piece == null || !piece.getRank().getRelevant()) continue;
					ClassicRank rank = piece.getRank();

					// Initialisiere die Maps für r1, falls noch nicht geschehen
					Map<RelativePosition, Map<ClassicRank, Integer>> posMap =
							neighborCounts.computeIfAbsent(rank, k -> new EnumMap<>(RelativePosition.class));

					for(RelativePosition position : RelativePosition.values()) {
						if(position.transformX(x) >= 0 && position.transformX(x) < 10 
								&& position.transformY(y) >= 0 && position.transformY(y) < 10) {
							ClassicPiece other = board[position.transformX(x)][position.transformY(y)];
							if(other == null || !other.getRank().getRelevant()) continue;
							ClassicRank otherRank = other.getRank();

							Map<ClassicRank, Integer> rankDistribs = posMap.computeIfAbsent(position, k -> new EnumMap<>(ClassicRank.class));

							rankDistribs.put(otherRank, rankDistribs.getOrDefault(otherRank, 0) + 1);
						}
					}
				}
			}
		}
	}

	/**
	 * Generates all 49 Rank x Rank pairs with their total neighbor count
	 * @param neighborCounts
	 * @return
	 */
	public static Map<Pair<ClassicRank, ClassicRank>, Integer> neighborCountsAllDirections(
			Map<ClassicRank, Map<RelativePosition, Map<ClassicRank, Integer>>> neighborCounts) {
		
		Map<Pair<ClassicRank, ClassicRank>, Integer> neighborCountsAllDir = new HashMap<Pair<ClassicRank, ClassicRank>, Integer>();
		
		//fill map with all 7x7 pairs
		for(ClassicRank rank : neighborCounts.keySet()) {
			for(ClassicRank rank2 : neighborCounts.keySet()) {
				Pair<ClassicRank, ClassicRank> rank2Map = Pair.of(rank, rank2);
				neighborCountsAllDir.put(rank2Map, 0);
			}
		}
		
		for(ClassicRank rank : neighborCounts.keySet()) {
			for(RelativePosition pos : neighborCounts.get(rank).keySet()) {
				for(ClassicRank rank2 : neighborCounts.get(rank).get(pos).keySet()) {
					Pair<ClassicRank, ClassicRank> pair = Pair.of(rank, rank2);
					int count = neighborCounts.get(rank).get(pos).get(rank2);
					neighborCountsAllDir.put(pair, neighborCountsAllDir.get(pair) + count);
				}
			}
		}

		return neighborCountsAllDir;
	}

	public enum RelativePosition {
		ABOVE(0, -1), 
		BELOW(0, 1), 
		LEFT(-1, 0), 
		RIGHT(1, 0), 
		ABOVE_LEFT(ABOVE.getXTransformation() + LEFT.getXTransformation(), 
				ABOVE.getYTransformation() + LEFT.getYTransformation()),
		ABOVE_RIGHT(ABOVE.getXTransformation() + RIGHT.getXTransformation(), 
				ABOVE.getYTransformation() + RIGHT.getYTransformation()), 
		BELOW_LEFT(BELOW.getXTransformation() + LEFT.getXTransformation(), 
				BELOW.getYTransformation() + LEFT.getYTransformation()), 
		BELOW_RIGHT(BELOW.getXTransformation() + RIGHT.getXTransformation(), 
				BELOW.getYTransformation() + RIGHT.getYTransformation());

		private RelativePosition(int xTransformation, int yTransformation) {
			this.xTransformation = xTransformation;
			this.yTransformation = yTransformation;
		}

		int xTransformation;
		int yTransformation;

		/**
		 * Returns true if x and y transformed are a valid position on the Quick Battle field
		 * @param x
		 * @param y
		 * @return true if position is valid
		 */
		public boolean qbValid(int x, int y) {
			return transformX(x) >= 0 && transformX(x) < 8 && transformY(y) >= 0 && transformY(y) < 3;
		}

		public int transformX(int x) {
			return x + xTransformation;
		}

		public int transformY(int y) {
			return y + yTransformation;
		}

		int getXTransformation() {
			return this.xTransformation;
		}

		int getYTransformation() {
			return this.yTransformation;
		}
	}
}
