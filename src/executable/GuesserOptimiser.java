package executable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import core.GameState;
import core.Piece;
import core.PieceType;
import core.placing.Placer;
import core.playing.guesser.Guesser;
import it.unimi.dsi.fastutil.Pair;

public class GuesserOptimiser {
	static PieceType[] RANKS = new PieceType[] {
			PieceType.FLAGGE,
			PieceType.SPAEHER, 
			PieceType.BOMBE,
			PieceType.GENERAL, 
			PieceType.MARSCHALL,
			PieceType.MINEUR,
			PieceType.SPIONIN
	};

	public static void main(String[] args) {
		RANKS = new PieceType[] {
				PieceType.GENERAL, PieceType.SPIONIN, PieceType.MARSCHALL, PieceType.MINEUR, PieceType.SPAEHER, PieceType.BOMBE, PieceType.FLAGGE // worst
//				PieceType.SPIONIN, PieceType.FLAGGE, PieceType.BOMBE, PieceType.MARSCHALL, PieceType.MINEUR, PieceType.GENERAL, PieceType.SPAEHER // best
		};
		
		profileGuesser(Placer.Type.BARRAGE, Placer.Type.BARRAGE, 10_000);
//		findBestRankSequence(Placer.Type.BARRAGE, Placer.Type.BARRAGE, 1_000);
		
	}


	 public static void findBestRankSequence(Placer.Type redPlacer, Placer.Type bluePlacer, int repetitions) {

	        List<PieceType[]> permutations = generatePermutations(Arrays.asList(RANKS));
	        System.out.println("Anzahl der zu testenden Rang-Permutationen: " + permutations.size()); // 7! = 5040

	        PieceType[] bestOrder = null;
	        double bestCombinedAccuracy = -1.0;

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
	                        Placer.placePiecesWith(true, redPlacer),	    
	                        Placer.placePiecesWith(false, bluePlacer)
	                );
	                
	                Guesser guesser = new Guesser(groundTruthState, currentOrder);

	                // Bewerte die Genauigkeit (Diese Methode muss im Guesser existieren oder hier implementiert werden)
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
	                // Kombiniere die Genauigkeit (z.B. Durchschnitt oder gewichtet)
	                double combinedAccuracy = (avgRedAccuracy + avgBlueAccuracy) / 2.0;

	                // Prüfe, ob diese Reihenfolge besser ist
	                if (combinedAccuracy > bestCombinedAccuracy) {
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
	    * Muss in Guesser implementiert sein oder hier statisch.
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
}
