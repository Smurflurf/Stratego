package executable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Function;

import org.knowm.xchart.XYChart;

import core.GameState;
import core.Piece;
import core.PieceType;
import core.placing.Placer;
import core.playing.guesser.Guesser;
import it.unimi.dsi.fastutil.Pair;
import strados2.tools.GeneralTools;

/**
 * Tries all possible rank permutations to find the best one to guess possibilities in Guesser.java
 * 
 * @author base code by Simon Stumpf, 
 * main part of {@link #findBestRankSequence(core.placing.Placer.Type, core.placing.Placer.Type, int)} by Gemini 2.5 pro Preview 03-25
 */
public class GuesserOptimiser {
	static SplittableRandom rand = new SplittableRandom();
	static PieceType[] RANKS = null;

	public static void main(String[] args) {
//		RANKS = new PieceType[] {
//		PieceType.GENERAL, PieceType.SPIONIN, PieceType.MARSCHALL, PieceType.MINEUR, PieceType.SPAEHER, PieceType.BOMBE, PieceType.FLAGGE // worst
//		PieceType.SPIONIN, PieceType.FLAGGE, PieceType.BOMBE, PieceType.MARSCHALL, PieceType.MINEUR, PieceType.GENERAL, PieceType.SPAEHER // best
//		};
		
//		Guesser.mode = "barrage";
//		System.out.println(
//				"+BARRAGE +b " + averageAccuracyInGame(Placer.Type.BARRAGE, Placer.Type.BARRAGE, false, true, 1, 2_5000));
		
//		randomAccuracyXYPlot(5000);
//		buffAccuracyXYPlot(5000);
//		nerfAccuracyXYPlot(5000);
//		accuracyXYPlot(false, false, 1_000);
		
//		profileGuesser(Placer.Type.BARRAGE, Placer.Type.BARRAGE, 1000);
//		findBestRankSequence(Placer.Type.BARRAGE, 1_000);
	}
	
	
	/**
	 * Calculates the average amount of Pieces that has to be revealed, till threshold Pieces are guessed correctly.
	 * Even though redPlacer and bluePlacer are given separate, the returned accuracy assumes they are equal.
	 * Both, red and blue accuracies are calculated separately.
	 * @param redPlacer Placement for Blue
	 * @param bluePlacer Placement for Red
	 * @param legacySearch true to use rank guessing Art2 from the BA, false to use the default and better Art3
	 * @param applyNeighborCounts true to use neighbor relations in the Guesser
	 * @param threshold 0.1-1, represents the amount of correct guessed Pieces
	 * @param repetitions more repetitions increase the accuracy, 25000 is pretty decent.
	 * @param randomInit uses random rank probability init
	 * @return average revealed Pieces till threshold Pieces are correctly guessed. Based on repetitions * 2 samples.
	 */
	public static double averageAccuracyInGame(Placer.Type redPlacer, Placer.Type bluePlacer, 
			boolean legacySearch, boolean applyNeighborCounts,double threshold, int repetitions, boolean randomInit) {
		List<Integer> redPiecesNeeded = new ArrayList<Integer>();	
		List<Integer> bluePiecesNeeded = new ArrayList<Integer>();	

		for(int i=0; i<repetitions; i++) {
			GameState state = new GameState(
					Placer.placePiecesWith(true, redPlacer),
					Placer.placePiecesWith(false, bluePlacer));
			Guesser guesser = new Guesser(state, RANKS, randomInit ? "random" : null);
			guesser.legacySearch = legacySearch;
			guesser.applyNeighborCounts = applyNeighborCounts;
			ArrayList<Piece> redPieces = new ArrayList<Piece>();
			ArrayList<Piece> bluePieces = new ArrayList<Piece>();
			for(Piece p : guesser.startState.getRedPieces()) redPieces.add(p);
			for(Piece p : guesser.startState.getBluePieces()) bluePieces.add(p);

			while(guesser.accuracy(state, true) < threshold) {
				Piece randomBlue = bluePieces.remove(rand.nextInt(bluePieces.size()));
				randomBlue.setKnown(true);
				guesser.startState.incrementKnownBlue();
				guesser.revealPiece(randomBlue, randomBlue.getType());
				guesser.normalize();
			}
			while(guesser.accuracy(state, false) < threshold) {
				Piece randomRed = redPieces.remove(rand.nextInt(redPieces.size()));
				randomRed.setKnown(true);
				guesser.startState.incrementKnownRed();
				guesser.revealPiece(randomRed, randomRed.getType());
				guesser.normalize();
			}
			redPiecesNeeded.add(10 - redPieces.size());
			bluePiecesNeeded.add(10 - bluePieces.size());
		}
		return (redPiecesNeeded.stream().mapToDouble(d -> d).average().getAsDouble() + 
				bluePiecesNeeded.stream().mapToDouble(d -> d).average().getAsDouble())
				/ 2;
	}

	/**
	 * Calculates the best Rank permutation for placer based on repetition * 2 samples.
	 * @param placer
	 * @param repetitions
	 */
	public static void findBestRankSequence(Placer.Type placer, int repetitions) {
		List<PieceType[]> permutations = generatePermutations(Arrays.asList(RANKS));
		System.out.println("Anzahl der zu testenden Rang-Permutationen: " + permutations.size()); // 7! = 5040

		PieceType[] bestOrder = null;
		double bestCombinedAccuracy = 100.0;

		int permutationCount = 0;
		for (PieceType[] currentOrder : permutations) {
			permutationCount++;
			if (permutationCount % 100 == 0) {
				System.out.print(" at " + permutationCount);
			}

			double totalRedAccuracy = 0;
			double totalBlueAccuracy = 0;
			int validRepetitions = 0; 

			for (int i = 0; i < repetitions; i++) {
				GameState groundTruthState = new GameState(
						Placer.placePiecesWith(true, placer),	    
						Placer.placePiecesWith(false, placer)
						);

				Guesser guesser = new Guesser(groundTruthState, currentOrder);
				Pair<Double, Double> accuracies = evaluateSingleGuess(guesser, groundTruthState);
				if (accuracies != null) {
					totalRedAccuracy += accuracies.left();
					totalBlueAccuracy += accuracies.right();
					validRepetitions++;
				}
			}

			if (validRepetitions > 0) {
				double avgRedAccuracy = totalRedAccuracy / validRepetitions;
				double avgBlueAccuracy = totalBlueAccuracy / validRepetitions;
				double combinedAccuracy = (avgRedAccuracy + avgBlueAccuracy) / 2.0;

				if (combinedAccuracy < bestCombinedAccuracy) {
					bestCombinedAccuracy = combinedAccuracy;
					bestOrder = currentOrder.clone(); // Wichtig: Kopie speichern!
					System.out.println("\nNeue beste Reihenfolge gefunden (Gesamtgenauigkeit: "
							+ String.format("%.2f", bestCombinedAccuracy * 100) + "%): "
							+ Arrays.toString(bestOrder));
					System.out.println("  (Rot: " + String.format("%.2f", avgRedAccuracy * 100)
					+ "%, Blau: " + String.format("%.2f", avgBlueAccuracy * 100) + "%)");
				}
			}
		}

		System.out.println("\nOptimierung abgeschlossen.");
		if (bestOrder != null) {
			System.out.println("Beste gefundene Reihenfolge: " + Arrays.toString(bestOrder));
			System.out.println("Beste erreichte Gesamtgenauigkeit: " + String.format("%.2f", bestCombinedAccuracy * 100) + "%");
		} else {
			System.out.println("Keine gültige Reihenfolge gefunden (möglicherweise Fehler in evaluateSingleGuess).");
		}
	}

	/**
	 * Generiert alle Permutationen einer gegebenen Liste von PieceTypes.
	 *
	 * @param items Die Liste der zu permutierenden Elemente.
	 * @return Eine Liste aller möglichen Permutationen als PieceType-Arrays.
	 */
	public static List<PieceType[]> generatePermutations(List<PieceType> items) {
		List<PieceType[]> result = new ArrayList<>();
		generatePermutationsHelper(new ArrayList<>(items), new ArrayList<>(), result);
		return result;
	}

	private static void generatePermutationsHelper(List<PieceType> remaining, List<PieceType> current, List<PieceType[]> result) {
		if (remaining.isEmpty()) {
			result.add(current.toArray(new PieceType[0]));
			return;
		}
		for (int i = 0; i < remaining.size(); i++) {
			PieceType element = remaining.get(i);
			List<PieceType> nextCurrent = new ArrayList<>(current);
			nextCurrent.add(element);
			List<PieceType> nextRemaining = new ArrayList<>(remaining);
			nextRemaining.remove(i);
			generatePermutationsHelper(nextRemaining, nextCurrent, result);
		}
	}

	/**
	 * Bewertet die Genauigkeit für einen einzelnen Durchlauf.
	 *
	 * @param guesser Der Guesser, der die Wahrscheinlichkeiten hält.
	 * @param groundTruthState Der wahre Zustand.
	 * @return Pair<Double, Double> (redAccuracy, blueAccuracy) oder null bei Fehler.
	 */
	private static Pair<Double, Double> evaluateSingleGuess(Guesser guesser, GameState groundTruthState) {
		return Pair.of(guesser.accuracy(groundTruthState, true), guesser.accuracy(groundTruthState, false));
	}

	public static void profileGuesser(Placer.Type redPlacer, Placer.Type bluePlacer, int repetitions) {
		List<Double> red = new ArrayList<Double>();	
		List<Double> blue = new ArrayList<Double>();	

		for(int i=0; i<repetitions; i++) {
			GameState state = new GameState(
					Placer.placePiecesWith(true, redPlacer),
					Placer.placePiecesWith(false, bluePlacer));
			Guesser guesser = new Guesser(state, RANKS);
			red.add(guesser.accuracy(state, true));
			blue.add(guesser.accuracy(state, false));
		}

		double maxR = 0;
		for(double d : red)
			maxR += d;
		System.out.println("red accuracy in % : " + maxR / red.size() * 100);
		double maxB = 0;
		for(double b : blue)
			maxB += b;
		System.out.println("blue accuracy in % : " + maxB / blue.size() * 100);
	}
	
	/**
	 * Creates and saves a png and svg as "GuesserAccuracy_XY_Chart.xxx", showing the accuracy of 
	 * Barrage Art 2 best case, Barrage Art 2 worst case and Barrage Art 3, for 1 to 10 revealed Pieces.
	 * @param legacySearch true to use rank guessing Art2 from the BA, false to use the default and better Art3
	 * @param applyNeighborCounts true to use neighbor relations in the Guesser
	 * @param repetitions more repetitions increase the accuracy, 5000 is decent.
	 */
	public static void accuracyXYPlot(boolean legacySearch, boolean applyNeighborCounts, int repetitions) {
		XYChart chart = new org.knowm.xchart.XYChartBuilder()
				.width(600)
				.height(600)
				.xAxisTitle("Erratene Figuren")
				.yAxisTitle("Enttarnte Figuren")
				.build();

		chart.getStyler()
		.setSeriesColors(new Color[] {Color.red, Color.orange, Color.blue, Color.cyan, Color.magenta, Color.pink, Color.black})
		.setChartBackgroundColor(Color.white)
		.setLegendVisible(true);
		Placer.Type placer = null;

		for(int test=0; test<3; test++) {
			String legendString = "";
			switch(test) {
			case 0: 
				legendString = "- Barrage Art 2 BC"; 
				placer = Placer.Type.BARRAGE; 
				legacySearch = true;
				applyNeighborCounts = false; 
				break;
//			case 1:
//				legendString = "+ Barrage Art 2 BC"; 
//				placer = Placer.Type.BARRAGE; 
//				legacySearch = true;
//				applyNeighborCounts = true; 
//				break;
			case 1: 
				RANKS = new PieceType[] {
					PieceType.GENERAL, PieceType.SPIONIN, PieceType.MARSCHALL, PieceType.MINEUR, PieceType.SPAEHER, PieceType.BOMBE, PieceType.FLAGGE // worst
				};
				legendString = "- Barrage Art 2 WC"; 
				placer = Placer.Type.BARRAGE; 
				legacySearch = true;
				applyNeighborCounts = false; 
				break;
			case 2:
				RANKS = new PieceType[] {
					PieceType.SPIONIN, PieceType.FLAGGE, PieceType.BOMBE, PieceType.MARSCHALL, PieceType.MINEUR, PieceType.GENERAL, PieceType.SPAEHER // best
				};
				legendString = "- Barrage Art 3"; 
				placer = Placer.Type.BARRAGE; 
				legacySearch = false;
				applyNeighborCounts = false; 
				break;
//			case 3:
//				legendString = "+ Barrage Art 3"; 
//				placer = Placer.Type.BARRAGE; 
//				legacySearch = false;
//				applyNeighborCounts = true; 
//				break;
			
//			case 5:
//				RANKS = new PieceType[] {
//						PieceType.GENERAL, PieceType.SPIONIN, PieceType.MARSCHALL, PieceType.MINEUR, PieceType.SPAEHER, PieceType.BOMBE, PieceType.FLAGGE // worst
//				};
//				legendString = "+ Barrage Art 2 WC"; 
//				placer = Placer.Type.BARRAGE; 
//				legacySearch = true;
//				applyNeighborCounts = true; 
//				break;
			}

			double[] averageAccuracies = new double[10];
			int i=0;

			for(double threshold=0.1; threshold<=1; threshold+=0.1) {
				averageAccuracies[i++] = averageAccuracyInGame(placer, placer, legacySearch, applyNeighborCounts, threshold, repetitions, true);
				System.out.println("test " + test + " t: " + threshold + ", accuracy " + averageAccuracies[i-1]);
			}


			chart.addSeries(legendString, averageAccuracies);
		}
		GeneralTools.saveChart(chart, "GuesserAccuracy_XY_Chart");		
	}
	
	
	/**
	 * Creates and saves a png and svg as "GuesserAccuracy_Nerf_XY_Chart.xxx", showing the Guessers accuracy
	 * with different nerfFactor values
	 * @param repetitions more repetitions increase the accuracy, 2500 is decent.
	 */
	public static void nerfAccuracyXYPlot(int repetitions) {
		XYChart chart = new org.knowm.xchart.XYChartBuilder()
				.width(600)
				.height(600)
				.xAxisTitle("Erratene Figuren")
				.yAxisTitle("Enttarnte Figuren")
				.build();

		chart.getStyler()
		.setSeriesColors(new Color[] {Color.red, Color.orange, Color.blue, Color.cyan, Color.magenta, Color.pink, Color.black})
		.setChartBackgroundColor(Color.white)
		.setLegendVisible(true);
		Placer.Type placer = null;

		for(int test=0; test<4; test++) {
			String legendString = "";
			switch(test) {
			case 0: 
				legendString = "NF=1"; 
				Guesser.neighborNerfFactor = 1;
				placer = Placer.Type.BARRAGE; 
				break;
			case 1: 
				legendString = "NF=0.6"; 
				Guesser.neighborNerfFactor = 0.6;
				placer = Placer.Type.BARRAGE; 
				break;
			case 2: 
				legendString = "NF=0.3"; 
				Guesser.neighborNerfFactor = 0.3;
				placer = Placer.Type.BARRAGE; 
				break;
			case 3:
				legendString = "NF=0"; 
				Guesser.neighborNerfFactor = 0.0;
				placer = Placer.Type.BARRAGE; 
				break;
			}

			double[] averageAccuracies = new double[10];
			int i=0;

			for(double threshold=0.1; threshold<=1; threshold+=0.1) {
				averageAccuracies[i++] = averageAccuracyInGame(placer, placer, false, true, threshold, repetitions, false);
				System.out.println("test " + test + " t: " + threshold + ", accuracy " + averageAccuracies[i-1]);
			}


			chart.addSeries(legendString, averageAccuracies);
		}
		GeneralTools.saveChart(chart, "GuesserAccuracy_Nerf_XY_Chart");		
	}
	
	/**
	 * Creates and saves a png and svg as "GuesserAccuracy_Buff_XY_Chart.xxx", showing the Guessers accuracy
	 * with different buffFactor values
	 * @param repetitions more repetitions increase the accuracy, 2500 is decent.
	 */
	public static void buffAccuracyXYPlot(int repetitions) {
		XYChart chart = new org.knowm.xchart.XYChartBuilder()
				.width(600)
				.height(600)
				.xAxisTitle("Erratene Figuren")
				.yAxisTitle("Enttarnte Figuren")
				.build();

		chart.getStyler()
		.setSeriesColors(new Color[] {Color.red, Color.orange, Color.blue, Color.cyan, Color.magenta, Color.pink, Color.black})
		.setChartBackgroundColor(Color.white)
		.setLegendVisible(true);
		Placer.Type placer = null;

		for(int test=0; test<5; test++) {
			String legendString = "";
			switch(test) {
			case 4: 
				legendString = "BF=0"; 
				Guesser.neighborBuffFactor = 0;
				placer = Placer.Type.BARRAGE; 
				break;
			case 3: 
				legendString = "BF=1"; 
				Guesser.neighborBuffFactor = 1;
				placer = Placer.Type.BARRAGE; 
				break;
			case 2: 
				legendString = "BF=3"; 
				Guesser.neighborBuffFactor = 3;
				placer = Placer.Type.BARRAGE; 
				break;
			case 1:
				legendString = "BF=6"; 
				Guesser.neighborBuffFactor = 6;
				placer = Placer.Type.BARRAGE; 
				break;
			case 0:
				legendString = "BF=10"; 
				Guesser.neighborBuffFactor = 10;
				placer = Placer.Type.BARRAGE; 
				break;
			}
			
			Guesser.neighborNerfFactor = 0;

			double[] averageAccuracies = new double[10];
			int i=0;

			for(double threshold=0.1; threshold<=1; threshold+=0.1) {
				averageAccuracies[i++] = averageAccuracyInGame(placer, placer, false, true, threshold, repetitions, false);
				System.out.println("test " + test + " t: " + threshold + ", accuracy " + averageAccuracies[i-1]);
			}


			chart.addSeries(legendString, averageAccuracies);
		}
		GeneralTools.saveChart(chart, "GuesserAccuracy_Buff_XY_Chart");		
	}
	
	/**
	 * Creates and saves a png and svg as "GuesserAccuracy_Random_XY_Chart.xxx", showing the Guessers accuracy
	 * guessing with random and barrage probabilities. 
	 * @param repetitions more repetitions increase the accuracy, 2500 is decent.
	 */
	public static void randomAccuracyXYPlot(int repetitions) {
		XYChart chart = new org.knowm.xchart.XYChartBuilder()
				.width(600)
				.height(600)
				.xAxisTitle("Enttarnte Figuren")
				.yAxisTitle("Erratene Figuren")
				.build();

		chart.getStyler()
		.setSeriesColors(new Color[] {Color.red, Color.orange, Color.blue, Color.cyan, Color.magenta, Color.pink, Color.black})
		.setChartBackgroundColor(Color.white)
		.setLegendVisible(true);
		chart.getStyler().setxAxisTickLabelsFormattingFunction(d -> ("" + (int)(d-1)));
		
		Placer.Type placer = null;
		boolean useRandomInit = false;
		
		for(int i=0; i<3; i++) {
			String legendString = "";
			switch(i) {
			case 0:
				legendString = "Zufällig"; 
				useRandomInit = true;
				placer = Placer.Type.BARRAGE; 
				Guesser.neighborNerfFactor = 0;
				Guesser.neighborBuffFactor = 0;
				break;
			case 1:
				legendString = "Barrage";
				useRandomInit = false; 
				placer = Placer.Type.BARRAGE; 
				Guesser.neighborNerfFactor = 0;
				Guesser.neighborBuffFactor = 0;
				break;
			case 2:
				legendString = "Classic";
				useRandomInit = false; 
				placer = Placer.Type.DEBOER; 
				Guesser.neighborNerfFactor = 0;
				Guesser.neighborBuffFactor = 0;
				break;
			}

			double[] averageThresholds = new double[11];


			for(int pieces=0; pieces<=10; pieces++) {
				averageThresholds[pieces] = averageThresholdForAccuracy(placer, pieces, repetitions, useRandomInit);
				System.out.println(" pieces: " + pieces + ", threshold " + averageThresholds[pieces]);
			}


			chart.addSeries(legendString, averageThresholds);
		}
		GeneralTools.saveChart(chart, "GuesserAccuracy_Random_XY_Chart");		
	}
	
	
	public static double averageThresholdForAccuracy(Placer.Type placer, int pieces, int repetitions, boolean randomInit) {
		List<Double> redAccuracy = new ArrayList<Double>();	
		List<Double> blueAccuracy = new ArrayList<Double>();	

		for(int i=0; i<repetitions; i++) {
			GameState state = new GameState(
					Placer.placePiecesWith(true, placer),
					Placer.placePiecesWith(false, placer));
			Guesser guesser = new Guesser(state, RANKS, randomInit ? "random" : null);
			guesser.legacySearch = false;
			guesser.applyNeighborCounts = false;
			ArrayList<Piece> redPieces = new ArrayList<Piece>();
			ArrayList<Piece> bluePieces = new ArrayList<Piece>();
			for(Piece p : guesser.startState.getRedPieces()) redPieces.add(p);
			for(Piece p : guesser.startState.getBluePieces()) bluePieces.add(p);

			while(bluePieces.size() > 10-pieces) {
				Piece randomBlue = bluePieces.remove(rand.nextInt(bluePieces.size()));
				randomBlue.setKnown(true);
				guesser.startState.incrementKnownBlue();
				guesser.revealPiece(randomBlue, randomBlue.getType());
				guesser.normalize();
			}
			while(redPieces.size() > 10-pieces) {
				Piece randomRed = redPieces.remove(rand.nextInt(redPieces.size()));
				randomRed.setKnown(true);
				guesser.startState.incrementKnownRed();
				guesser.revealPiece(randomRed, randomRed.getType());
				guesser.normalize();
			}
			
			redAccuracy.add(guesser.accuracy(state, true));
			blueAccuracy.add(guesser.accuracy(state, false));
		}
		
		return (redAccuracy.stream().mapToDouble(d -> d).average().getAsDouble() + 
				blueAccuracy.stream().mapToDouble(d -> d).average().getAsDouble())
				/ 2;
	}
}
