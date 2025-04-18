package strados2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import strados2.classic.ClassicPiece;
import strados2.classic.XmlParser;
import strados2.tools.GeneralTools;
import strados2.tools.HeatMapGenerator;
import strados2.tools.LinePlotGenerator;
import strados2.tools.Scraper;

/**
 * Analyses the Stratego Games downloaded from Gravon.
 * If the files are not downloaded yet, it calls the downloader {@link Scraper}.
 */
public class GravonAnalyser {
	static String mode = "barrage";
	static String title = "";
	static ClassicPiece.ClassicRank[] ranks = null;
	static ArrayList<ClassicPiece[][]> relevantBoards;

	public static void main(String[] args) throws IOException {
		init();
		
		ranks = new ClassicPiece.ClassicRank[] {
				ClassicPiece.ClassicRank.FLAGGE,
				ClassicPiece.ClassicRank.BOMBE,
				ClassicPiece.ClassicRank.SPIONIN,
				ClassicPiece.ClassicRank.SPAEHER,
				ClassicPiece.ClassicRank.MINEUR,
				ClassicPiece.ClassicRank.GENERAL,
				ClassicPiece.ClassicRank.MARSCHALL
		};
		
//		analyseHeatMap(relevantBoards);
		analyseLinePlot(relevantBoards);

	}
	
	/**
	 * Creates an xyChart containing all PieceTypes in {@link #ranks} distribution.
	 * @param relevantBoards all boards to analyse
	 */
	static void analyseLinePlot(ArrayList<ClassicPiece[][]> relevantBoards) {
		if(ranks.length == 0) {	
			return;
		} else {
			double[][] rankDistributions = new double[ranks.length][GeneralTools.BOARD_SIZE];
			for(int r=0; r<ranks.length; r++) {
				int[][] boards = HeatMapGenerator.aggregatePlacementCounts(relevantBoards, ranks[r]);
				double[] distribution = LinePlotGenerator.getPieceYDistribution(boards);
				rankDistributions[r] = distribution;
			}
			title = "Figurenverteilung " + (mode.equals("classic") ? "klassisch" : "Trommelfeuer");
			LinePlotGenerator.createLinePlot(rankDistributions, title, ranks);
		}
	}
	
	
	/**
	 * Creates a HeatMap for each PieceType in {@link #ranks}, showing the squares they are placed on in relevantBoards.
	 * @param relevantBoards all boards to analyse
	 */
	static void analyseHeatMap(ArrayList<ClassicPiece[][]> relevantBoards) {
		if(ranks.length == 0) {	// analyse all Pieces
			int[][] heat = HeatMapGenerator.aggregatePlacementCounts(relevantBoards, null);
			title = "" + "all Pieces " + mode;
			HeatMapGenerator.createHeatmapChart(heat, title, title);
			return;
		} else {				// analyse picked Pieces
			for(ClassicPiece.ClassicRank rank : ranks) {
				int[][] heat = HeatMapGenerator.aggregatePlacementCounts(relevantBoards, rank);
				title = "" + rank + " " + mode;
				HeatMapGenerator.createHeatmapChart(heat, title, title);
			}
		}
	}
	
	/**
	 * Downloads all available games from Gravon if necessary,
	 * then puts all starting positions from the desired game mode {@link #mode} in {@link #relevantBoards}.
	 * @throws IOException
	 */
	static void init() throws IOException {
		if(Files.notExists(Paths.get(Scraper.EXTRACT_DIR)))
			Scraper.main(null);

		relevantBoards = new ArrayList<>();
		Files.walk(Paths.get(Scraper.EXTRACT_DIR))
		.filter(p -> p.toString().contains(mode))
		.forEach(p -> relevantBoards.add(XmlParser.parseInitialSetup(p)));
		System.out.println(relevantBoards.size() + " " + mode + " games to analyse");
		
		/*Files.walk(Paths.get(Scraper.EXTRACT_DIR))
		.filter(p -> !p.toString().contains(mode))
		.forEach(p -> new File(p.toString()).delete());*/
	}
}
