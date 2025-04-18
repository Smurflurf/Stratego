package strados2.tools;

import java.io.IOException;
import java.util.List;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.internal.chartpart.Chart;

import strados2.classic.ClassicPiece;
import strados2.classic.ClassicPiece.ClassicColor;
import strados2.classic.ClassicPiece.ClassicRank;

public class GeneralTools {
	public static final int BOARD_SIZE = 10;
	static final int[] COL_LABELS = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	static final int[] ROW_LABELS = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	
	/**
	 * Saves chart as a png and svg
	 *
	 * @param chart saves this chart
	 * @param baseFileName name of the saved file
	 */
	public static void saveChart(@SuppressWarnings("rawtypes") Chart chart, String baseFileName) {
		try {
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
		for (ClassicPiece[][] board : allBoards) {
			if (board == null) continue;
			for (int x = 0; x<BOARD_SIZE; x++) {
				for (int y=0; y<BOARD_SIZE; y++) {
					ClassicPiece piece = board[x][y];
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
}
