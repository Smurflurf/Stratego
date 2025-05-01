package strados2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import strados2.classic.ClassicPiece;
import strados2.classic.ClassicPiece.ClassicRank;
import strados2.classic.XmlParser;
import strados2.tools.CompressedMapIO;
import strados2.tools.GeneralTools;
import strados2.tools.HeatMapGenerator;
import strados2.tools.LinePlotGenerator;
import strados2.tools.NeighborIO;
import strados2.tools.Scraper;

/**
 * Analyses the Stratego Games downloaded from Gravon.
 * If the files are not downloaded yet, it calls the downloader {@link Scraper}.
 */
public class GravonAnalyser extends GeneralTools {
	static String mode = "classic";
	static String modeName = mode.substring(0, 1).toUpperCase() + mode.substring(1);
	static String title = "";
	static ClassicRank[] ranks = null;
	static ArrayList<ClassicPiece[][]> relevantBoards;

	public static void main(String[] args) throws IOException {
		//mode = "barrage"; "classic"; "duell";

		init();

		ranks = new ClassicRank[] {
				ClassicRank.FLAGGE,
				ClassicRank.BOMBE,
				ClassicRank.SPIONIN,
				ClassicRank.SPAEHER,
				ClassicRank.MINEUR,
				ClassicRank.GENERAL,
				ClassicRank.MARSCHALL
		};
		
		/**
		 * Generates neighbor count Maps and saves them for later use in Heuristics.
		 * If commented in, prints neighbor counts to console and/or prints a Table containing commonly placed together Pieces.
		 **/
		/*NeighborIO.saveNeighborCounts(mode, neighborCounts(relevantBoards, mode));
		var neighborCounts = NeighborIO.loadNeighborCounts(mode);
		NeighborIO.printNeighborCounts(neighborCounts);
		NeighborIO.printNeighborTable(ranks, neighborCounts);*/


		/**
		 * Generate Heat Maps based on the Piece distributions in %. Saves them for later use in Heuristics.
		 **/
		/*int[][][] maps = percentageHeatMap();
		compressDistributionsHeatMaps(maps);*/

		
		
		// Saves the Piece distributions on x and y axis as xyChart
		/*analyseLinePlotX();
		analyseLinePlotY();*/
		
		// Saves Heat Maps with total Piece counts instead of probabilities
		/*analyseHeatMap();*/
	
		// DEPRECATED due to inaccuracy, use percentageHeatMap() and compressDistributionsHeatMaps()
		/*compressRelevantX(maps);
		compressRelevantY(maps);*/
	}


	/**
	 * More accurate version of {@link #getDistributionsHeatMaps(double[][], double[][])}.
	 * With a percentage distribution of all Pieces, it creates a compressed heat map to represent the 10x5 as an 8x4 field.
	 * Remove last arg in HeatMapGenerator.createHeatmapChart(...) to save into core.placing
	 * @param maps maps with percentage piece distributions created with {@link #percentageHeatMap()}
	 */
	static void compressDistributionsHeatMaps(int[][][] maps) {
		int[][][] compressedMaps = new int[ranks.length][8][4]; 
		for(int r=0; r<ranks.length; r++) {
			for(int x=0; x<maps[r].length; x++) {
				for(int y=0,compressedX=getCompressedX(x),compressedY=0; y<maps[r][x].length; y++, compressedY++) {
					if(y==2)
						--compressedY;
					//					if(compressedY==1 || compressedX==2 || compressedX==5)
					//						compressedMaps[r][compressedX][compressedY] = Integer.max(compressedMaps[r][compressedX][compressedY], maps[r][x][y]);
					//					else
					compressedMaps[r][compressedX][compressedY] += maps[r][x][y];
				}
			}
		}
		StringBuilder pieces = new StringBuilder();
		for(int r=0; r<ranks.length; r++) {
			pieces.append(ranks[r] + "_");
			title = ranks[r] + "_CompressedDistribution_" + mode;
			HeatMapGenerator.createHeatmapChart(compressedMaps[r], ranks[r].getName() + " " + modeName + " komprimiert", title);
		}

		CompressedMapIO.saveCompressedMaps(mode, compressedMaps, ranks, "charts"+File.separator+"piece_distribution_"+mode+"_"+pieces.toString()+".txt");
	}

	/**
	 * Used in {@link #getDistributionsHeatMaps(int[][][])} to get the current index of compressedMaps array, depending on x
	 * @param x
	 * @return compressedX
	 */
	private static int getCompressedX(int x) {
		if(x>6)
			return x-2;
		else if(x>2)
			return x-1;
		else
			return x;
	}

	static int[][][] percentageHeatMap() {
		int[][][] maps = new int[ranks.length][][];
		for(int r=0; r<ranks.length; r++) {
			int[][] boards = GeneralTools.aggregatePlacementCounts(relevantBoards, ranks[r]);
			int[][] halfed = GeneralTools.half(boards);
			int[][] percentaged = GeneralTools.percentageMap(halfed);
			maps[r] = percentaged;
			title = "" + ranks[r] + "_" + mode;
			HeatMapGenerator.createHeatmapChart(percentaged, ranks[r].getName() + " " + modeName, title);
		}
		return maps;
	}

	/**
	 * Creates an xyChart containing all PieceTypes in {@link #ranks} distribution.
	 */
	static void analyseLinePlotX() {
		if(ranks.length == 0) {	
			return;
		} else {
			double[][] rankDistributions = new double[ranks.length][GeneralTools.BOARD_SIZE];
			for(int r=0; r<ranks.length; r++) {
				int[][] boards = GeneralTools.aggregatePlacementCounts(relevantBoards, ranks[r]);
				double[] distribution = LinePlotGenerator.getPieceXDistribution(boards);
				rankDistributions[r] = distribution;
			}
			title = "Figurenverteilung_Spalten_" + (mode.equals("classic") ? "klassisch" : (mode.equals("duell") ? "Duell" :  "Trommelfeuer"));
			LinePlotGenerator.createLinePlot(rankDistributions, title, ranks, "Spalte");
		}
	}

	/**
	 * Creates an xyChart containing all PieceTypes in {@link #ranks} distribution.
	 */
	static void analyseLinePlotY() {
		if(ranks.length == 0) {	
			return;
		} else {
			double[][] rankDistributions = new double[ranks.length][GeneralTools.BOARD_SIZE];
			for(int r=0; r<ranks.length; r++) {
				int[][] boards = GeneralTools.aggregatePlacementCounts(relevantBoards, ranks[r]);
				double[] distribution = LinePlotGenerator.getPieceYDistribution(boards);
				rankDistributions[r] = distribution;
			}
			title = "Figurenverteilung_Reihen_" + (mode.equals("classic") ? "klassisch" : (mode.equals("duell") ? "Duell" :  "Trommelfeuer"));
			LinePlotGenerator.createLinePlot(rankDistributions, title, ranks, "Reihe aus Spieler Sicht");
		}
	}


	/**
	 * Creates a HeatMap for each PieceType in {@link #ranks}, showing the squares they are placed on in relevantBoards.
	 * Labels all cells with the total number of occurrences.
	 */
	static void analyseHeatMap() {
		if(ranks.length == 0) {	// analyse all Pieces
			int[][] heat = HeatMapGenerator.aggregatePlacementCounts(relevantBoards, null);
			title += "" + "all_Pieces_" + mode;
			HeatMapGenerator.createHeatmapChart(heat, "Alle Figuren " + mode, title);
			return;
		} else {				// analyse picked Pieces
			for(ClassicRank rank : ranks) {
				int[][] heat = HeatMapGenerator.aggregatePlacementCounts(relevantBoards, rank);
				title = "" + rank + "_" + mode;
				HeatMapGenerator.createHeatmapChart(heat, rank.getName() + " " + modeName, title);
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
		relevantBoards.removeIf(f -> f == null);
		System.out.println(relevantBoards.size() + " " + mode + " games to analyse");

		/*Files.walk(Paths.get(Scraper.EXTRACT_DIR))
		.filter(p -> !p.toString().contains(mode))
		.forEach(p -> new File(p.toString()).delete());*/
	}



	/*//////////////////////////////////////////////////////////////
	///
	///
	///		DEPRECATED
	///
	///
	/////////////////////////////////////////////////////////////*/

	/**
	 * Errors due to seperate x and y calculation.
	 * Use {@link #getDistributionsHeatMaps(int[][][])}
	 * @param xDistrib
	 * @param yDistrib
	 */
	@Deprecated
	static void getDistributionsHeatMaps(double[][] xDistrib, double[][] yDistrib) {
		int[][][] maps = new int[ranks.length][xDistrib[0].length][yDistrib[0].length];


		for(int r=0; r<ranks.length; r++) {
			for(int x=0; x<xDistrib[r].length; x++) {
				for(int y=0; y<yDistrib[r].length; y++) {
					maps[r][x][y] = (int)(Math.round(100 * xDistrib[r][x] * yDistrib[r][y]));
				}
			}
		}

		for(int r=0; r<ranks.length; r++) {
			title = "CompressedDistribution_" + ranks[r] + "_" + mode;
			HeatMapGenerator.createHeatmapChart(maps[r], ranks[r].getName() + " " + mode + " " + "komprimiert", title);
		}
	}

	/**
	 * Prints the Pieces y distribution on the board, returns the compressed 8 entry array.
	 * @param maps all heatmaps collected with {@link #percentageHeatMap()}
	 * @return compressed rank distributions for all Pieces in {@link #ranks}
	 */
	@Deprecated
	static double[][] compressRelevantY(int[][][] maps) {
		boolean print = false;

		double[][] rankDistributionsY = new double[ranks.length][8];
		for(int r=0; r<maps.length; r++) {
			int[] yPercentagesRaw = new int[5];

			for(int y=0; y<maps[r][0].length; y++) {
				int aggregatedY = 0;
				for(int x=0; x<maps[r].length; x++) {
					aggregatedY += maps[r][x][y];
					yPercentagesRaw[y] = aggregatedY;
				}
			}

			int[] yPercentagesCompressed = compressY(yPercentagesRaw);
			rankDistributionsY[r] = new double[] {yPercentagesCompressed[0]/100., yPercentagesCompressed[1]/100., yPercentagesCompressed[2]/100., yPercentagesCompressed[3]};
			if(print) {
				System.out.print("\n" + "int[] " + ranks[r].getName().toLowerCase() +"Y = new int[] {");
				for(int i=0; i<yPercentagesCompressed.length; i++)
					System.out.print(yPercentagesCompressed[i] + (i < yPercentagesCompressed.length -1 ? ", " : "};"));
			}
		}
		LinePlotGenerator.createLinePlot(rankDistributionsY, ("Figurenverteilung_Reihen_" + (mode.equals("classic") ? "klassisch" : (mode.equals("duell") ? "Duell" :  "Trommelfeuer")) + "_Compressed"), ranks, "Reihe");
		return rankDistributionsY;
	}


	/**
	 * Prints the Pieces x distribution on the board, returns the compressed 4 entry array.
	 * @param maps all heatmaps collected with {@link #percentageHeatMap()}
	 * @return compressed rank distributions for all Pieces in {@link #ranks}
	 */
	@Deprecated
	static double[][] compressRelevantX(int[][][] maps) {
		boolean print = false;

		double[][] rankDistributionsX = new double[ranks.length][4];
		for(int r=0; r<maps.length; r++) {
			int[] xPercentagesRaw = new int[10];

			for(int x=0; x<maps[r].length; x++) {
				int aggregatedX = 0;
				for(int y=0; y<maps[r][0].length; y++) {
					aggregatedX += maps[r][x][y];
					xPercentagesRaw[x] = aggregatedX;
				}
			}

			int[] xPercentagesCompressed = compressX(xPercentagesRaw);
			rankDistributionsX[r] = new double[] {xPercentagesCompressed[0]/100., xPercentagesCompressed[1]/100., xPercentagesCompressed[2]/100., xPercentagesCompressed[3]/100.,
					xPercentagesCompressed[4]/100., xPercentagesCompressed[5]/100., xPercentagesCompressed[6]/100., xPercentagesCompressed[7]/100.};

			if(print) {
				System.out.print("\n" + "int[] " + ranks[r].getName().toLowerCase() +"X = new int[] {");
				for(int i=0; i<xPercentagesCompressed.length; i++)
					System.out.print(xPercentagesCompressed[i] + (i < xPercentagesCompressed.length -1 ? ", " : "};"));
			}
		}

		LinePlotGenerator.createLinePlot(rankDistributionsX, ("Figurenverteilung_Spalten_" + (mode.equals("classic") ? "klassisch" : (mode.equals("duell") ? "Duell" :  "Trommelfeuer")) + "_Compressed"), ranks, "Spalte");
		return rankDistributionsX;
	}

}
