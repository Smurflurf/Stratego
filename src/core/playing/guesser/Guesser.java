package core.playing.guesser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import core.GameState;
import core.Move;
import core.Piece;
import core.PieceType;
import core.Utils;
import core.playing.AI.AIInformer;
import strados2.tools.CompressedMapIO;
import strados2.tools.GeneralTools.RelativePosition;
import strados2.tools.NeighborIO;

public class Guesser {
	public GameState startState;
	public GameState currentState;
	Move lastMove;
	public PieceType[] RANKS = {PieceType.SPIONIN, PieceType.FLAGGE, PieceType.BOMBE, 
	PieceType.MARSCHALL, PieceType.MINEUR, PieceType.GENERAL, PieceType.SPAEHER}; // best Permutation
	
//	PieceType[] RANKS = {PieceType.FLAGGE, PieceType.BOMBE,PieceType.SPIONIN, 
//	PieceType.SPAEHER, PieceType.MINEUR, PieceType.GENERAL, PieceType.MARSCHALL}; // Permutation used for rankMap in nerfing (use Strg+F)
	Map<Byte, Piece> startPosToPieces;
	Map<Piece, double[]> redPieces;
	Map<Piece, double[]> bluePieces;
	Set<Piece> deadPieces;
	Map<PieceType, Integer> redDeadPieces;
	Map<PieceType, Integer> blueDeadPieces;
	Map<PieceType, int[][]> probabilities;
	Set<Piece> movable;
	Map<PieceType, Map<RelativePosition, Map<PieceType, Double>>> neighborProbabilities;
	Map<Piece, Piece> startToCurrentStateMap;
	Map<Piece, Piece> currentToStartStateMap;
	/**
	 * Used in {@link #converge()}, decides if pieces get converged by rank or probability
	 */
	public boolean legacySearch = false;
	/**
	 * Used in {@link #normalize()}, applies neighbor counts to probabilities
	 */
	public boolean applyNeighborCounts = false;
	/**
	 * Used in {@link #initPieces()}, initializes pieces with random probabilities
	 */
	public boolean useRandomInit = false;
	
	public String mode = "barrage";
	
	public static double epsilon = 0.01;
	public static double neighborBuffFactor = 1;
	public static double neighborNerfFactor = 0;
	
	/**
	 * Guesses team Pieces.
	 * @param gameState starting position
	 * @param RANKS null to use default, otherwise permutation of {@link #RANKS}
	 */
	public Guesser(GameState gameState, PieceType[] RANKS, String ... guesserProbs) {
		if(RANKS != null)
			this.RANKS = RANKS;
		
		redPieces = new HashMap<Piece, double[]> ();
		bluePieces = new HashMap<Piece, double[]> ();
		redDeadPieces = new HashMap<PieceType, Integer>();
		blueDeadPieces = new HashMap<PieceType, Integer>();
		if(guesserProbs != null && guesserProbs.length == 1) {
			if(guesserProbs[0].equals("random"))
				useRandomInit = true;
			else
				mode = guesserProbs[0];
		}
			
		probabilities = CompressedMapIO.loadCompressedMaps(mode);
		neighborProbabilities = NeighborIO.loadNeighborCounts(mode);
		startPosToPieces = new HashMap<Byte, Piece>();
		currentState = gameState.clone();
		startState = gameState.clone();
		startToCurrentStateMap = new HashMap<Piece, Piece>();
		currentToStartStateMap = new HashMap<Piece, Piece>();
		deadPieces = new HashSet<Piece>();
		movable = new HashSet<Piece>();

		for(int i=0; i<2; i++)
			for(Piece startPiece : startState.getPieces()[i]) {
				startPosToPieces.put(startPiece.getStartPos(), startPiece);
				for(Piece piece : currentState.getPieces()[i]) {
					if(startPiece.getPos() == piece.getPos()) {
						startToCurrentStateMap.put(startPiece, piece);
						currentToStartStateMap.put(piece, startPiece);
					}
				}
			}
		initPieces();
		normalize();
	}


	/**
	 * Initializes all Pieces with start probabilities int {@link #redPieces} or {@link #bluePieces} depending on team.
	 * Piece initialization is hard coded here!
	 */
	private void initPieces() {
		redPieces.clear();
		bluePieces.clear();
		
		for(int team=0; team<2; team++) {
			// create probability arrays, put raw probabilities for unknown Pieces in there
			for(Piece piece : startState.getPieces()[team]) {
				int x = piece.getX();
				int y = piece.getY() > 4 ? 7 - piece.getY() : piece.getY();

				double[] probabilities = new double[RANKS.length];

				if(!piece.getKnown()) {
					for(int i=0; i<RANKS.length; i++) {
						if((RANKS[i] == PieceType.BOMBE 
								|| RANKS[i] == PieceType.FLAGGE)
								&& movable.contains(piece)) {
							probabilities[i] = 0;
						} else {
							if(!useRandomInit) {
								probabilities[i] = this.probabilities.get(RANKS[i])[x][y] + 1;
							} else {
								probabilities[i] = Math.random() * 100 +1;
							}
						}
					}
				}

				(piece.getTeam() ? redPieces : bluePieces).put(piece, probabilities);
			}
			
			// normalize the probabilities
			for(Piece piece : startState.getPieces()[team]) {
				double[] probabilities = (piece.getTeam() ? redPieces : bluePieces).get(piece);
				double sum = DoubleStream.of(probabilities).sum();
				for(int r=0; r<RANKS.length; r++)
					probabilities[r] /= sum;
			}
			
			// put known Pieces probabilities in there
			for(Piece piece : startState.getPieces()[team]) {
				int rank = 0;
				if(piece.getKnown()) {
					for(int r=0; r<RANKS.length; r++)
						if(piece.getType() == RANKS[r])
							rank = r;
					(piece.getTeam() ? redPieces : bluePieces).get(piece)[rank] = 1;
				}
			}
			
			// remove rank probabilities for ranks which are already completely known
			for(int r=0; r<RANKS.length; r++) {
				int knownRankCount = 0;
				for(Piece p : (team == 0 ? redPieces : bluePieces).keySet()) 
					knownRankCount += (team == 0 ? redPieces : bluePieces).get(p)[r] == 1 ? 1 : 0;
				
				if(knownRankCount == 0) // no piece known
					continue;
				else if (knownRankCount != RANKS[r].getPieceCount()) { // 1 out of 2 Pieces known
					double sum = -1;
					for(Piece p : (team == 0 ? redPieces : bluePieces).keySet()) {
						sum += (team == 0 ? redPieces : bluePieces).get(p)[r];
					}
					for(Piece p : (team == 0 ? redPieces : bluePieces).keySet()) {
						double[] probs = (team == 0 ? redPieces : bluePieces).get(p);
						probs[r] = probs[r] == 1 ? 1 : probs[r] / sum; 
					}
				} else { // both Pieces known
					for(Piece p : (team == 0 ? redPieces : bluePieces).keySet()) {
						double[] probs = (team == 0 ? redPieces : bluePieces).get(p);
						probs[r] = probs[r] == 1 ? 1 : 0; 
					}
				}
			}
		}
		
//		System.out.println("\n\n\n in init \n");
//		print();
	}

	
	/**
	 * Call to converge the GameState to a GameState without PieceType.UNKNOWN.
	 * Call only after {@link #normalize()} normalized the GameState (happens in constructor).
	 * @param myTeam the team which won't get guessed but taken from {@link #currentState}
	 * @return GameState with guessed ranks
	 */
	public GameState converge(boolean myTeam) {
		GameState cloneWithPieces = legacySearch ? convergeToStartStateLegacy(!myTeam ? 1 : 2) : convergeToStartState(!myTeam ? 1 : 2);
		mapStartToCurrent(cloneWithPieces);
		GameState cloneWithEverythingElse = currentState.clone();
		cloneWithEverythingElse.setPiecesAndField(cloneWithPieces.getPieces(), cloneWithPieces.getField());
		return cloneWithEverythingElse;
	}

	/**
	 * Call to converge the GameState to a GameState without PieceType.UNKNOWN.
	 * Call only after {@link #normalize()} normalized the GameState (happens in constructor).
	 * @return GameState with guessed ranks
	 */
	public GameState converge() {
		GameState cloneWithPieces = legacySearch ? convergeToStartStateLegacy(0) : convergeToStartState(0);
		mapStartToCurrent(cloneWithPieces);

		GameState cloneWithEverythingElse = currentState.clone();
		cloneWithEverythingElse.setPiecesAndField(cloneWithPieces.getPieces(), cloneWithPieces.getField());

		return cloneWithEverythingElse;
	}

	/**
	 * Map Piece locations in clone to their current location from {@link #currentState}
	 * @param clone GameState to modify
	 */
	private void mapStartToCurrent(GameState clone) {
		ArrayList<Move> alterClone = new ArrayList<Move>();
		for(int team=0; team<2; team++) {
			for(Piece clonePiece : clone.getPieces()[team]) {
				for(Piece startPiece : startState.getPieces()[team]) {

					if(clonePiece.getPos() == startPiece.getPos()) {
						Piece current = startToCurrentStateMap.get(startPiece);
						if(currentState.inspect(current.getX(), current.getY()) == null || 
								deadPieces.contains(current)) {
							clone.removePiece(clonePiece);
						} else if (current.getPos() != clonePiece.getPos()){
							Move move = new Move(clonePiece, clonePiece.getX(), clonePiece.getY(), current.getX(), current.getY());
							alterClone.add(move);
						}
						break;
					}
				}
			}
		}

		for(Move move : alterClone) {
			clone.getField()[move.getStartX()][move.getStartY()] = null;
		}
		for(Move move : alterClone) {
			clone.getField()[move.getEndX()][move.getEndY()] = move.getPiece();
			move.getPiece().setPos(move.getEndX(), move.getEndY());
		}
	}

	/**
	 * Converges the Pieces in a clone of {@link #startState} and returns it.
	 * This version iterates by finding the globally highest probability for a piece-rank pair.
	 * @param notConvergedTeam 1: team red wont be converged; 2: team blue wont be converged; 0 both teams will be converged
	 * @return converged clone of {@link #startState}
	 */
	public GameState convergeToStartState(int notConvergedTeam) {
		GameState clone = startState.clone();

		for(int teamIndex=notConvergedTeam==2 ? 1 : 0; teamIndex<(notConvergedTeam==1 ? 1 : 2); teamIndex++) {
			Map<Piece, double[]> currentTeamProbabilities = (teamIndex == 0) ? redPieces : bluePieces;
			List<Piece> unassignedPieces = new ArrayList<>();
			for (Piece piece : clone.getPieces()[teamIndex]) {
				unassignedPieces.add(piece);
			}

			Map<PieceType, Integer> remainingRankCounts = getRankCounts();

			while (!unassignedPieces.isEmpty() && remainingRankCounts.values().stream().anyMatch(count -> count > 0)) {
				Piece bestPieceToAssign = null;
				PieceType bestRankToAssign = null;
				double maxProbability = -1.0;

				for (Piece clonePiece : unassignedPieces) {
					double[] probabilities = currentTeamProbabilities.get(
							startState.inspect(clonePiece.getX(), clonePiece.getY()));

					for (int rankIndex = 0; rankIndex < RANKS.length; rankIndex++) {
						PieceType currentRank = RANKS[rankIndex];
						if (remainingRankCounts.getOrDefault(currentRank, 0) > 0) {
							if (probabilities[rankIndex] > maxProbability) {
								maxProbability = probabilities[rankIndex];
								bestPieceToAssign = clonePiece;
								bestRankToAssign = currentRank;
							}
						}
					}
				}

				if (bestPieceToAssign != null && bestRankToAssign != null) {	// normaler Weg, sollte in 99.9% genommen weerden
					bestPieceToAssign.setType(bestRankToAssign.getByte());
					//					bestPieceToAssign.setKnown(true);
					unassignedPieces.remove(bestPieceToAssign);
					remainingRankCounts.put(bestRankToAssign, remainingRankCounts.get(bestRankToAssign) - 1);
				} else {	// bei Fehlern. Sehr selten, eigentlich nur bei Laden der classic Probabilities bei mit Barrage platziert
					Piece fallbackPiece = unassignedPieces.get(0);
					PieceType fallbackRank = null;
					for (PieceType rank : RANKS) { // Finde den ersten verfügbaren Rang
						if (remainingRankCounts.getOrDefault(rank, 0) > 0) {
							fallbackRank = rank;
							break;
						}
					}
					fallbackPiece.setType(fallbackRank.getByte());
					//					fallbackPiece.setKnown(true);
					unassignedPieces.remove(fallbackPiece);
					remainingRankCounts.put(fallbackRank, remainingRankCounts.get(fallbackRank) - 1);
				}
			}
		}
		return unshuffle(clone);
	}

	/**
	 * Converges the UNKNOWN Pieces in a clone of {@link #startState} and returns it
	 * @param notConvergedTeam 1: team red wont be converged; 2: team blue wont be converged; 0 both teams will be converged
	 * @return converged clone of {@link #startState}
	 */
	public GameState convergeToStartStateLegacy(int notConvergedTeam) {
		GameState clone = startState.clone();
		for(int teamIndex=notConvergedTeam==2 ? 1 : 0; teamIndex< (notConvergedTeam==1 ? 1 : 2); teamIndex++) {
			var aliveRanks = getRankCounts();
			List<Piece> pieces = new ArrayList<Piece>();
			for(Piece p : clone.getPieces()[teamIndex]) if(p != null) pieces.add(p);

			for(int rank=0; rank<RANKS.length; rank++) {
				for(int leftToGuess=aliveRanks.get(RANKS[rank]); leftToGuess>0; leftToGuess--) {
					guess(pieces, rank, leftToGuess);
				}
			}
		}
		return unshuffle(clone);
	}

	private void guess(List<Piece> pieces, int rank, int leftToGuess) {
		for(Piece p : pieces) {
			if(p.getKnown() && p.getType() == RANKS[rank]) {
				pieces.remove(p);
				return;
			}
		}

		Piece best = null;
		double max = -1.;
		for(Piece piece : pieces) {
			Piece startPieceEquivalent = startState.inspect(piece.getX(), piece.getY());
			double prob = (piece.getTeam() ? redPieces.get(startPieceEquivalent) : bluePieces.get(startPieceEquivalent))[rank];
			if(prob > max) {
				best = piece;
				max = prob;
			}
		}

		if(best == null)	// Für sehr seltenen Fall dass eine Piece an einer Stelle steht, die nicht durch probabilities abgedeckt wird.
			best = pieces.removeFirst();

		pieces.remove(best);
		best.setType(RANKS[rank].getByte());
	}

	/**
	 * Iteratively normalizes the rank:piece matrix, or in this code the matrix equivalent.
	 * Calls {@link #normalizeRanks(Map, double)} and {@link #normalizePieces(Map, boolean, double)}
	 * till the difference is smaller than the threshold epsilon.
	 */
	public void normalize() {
		boolean updated = true;
		long time = System.currentTimeMillis();

//		initPieces();
//		System.out.println("\n\nAfter Initialisation");
//		print();
		if(applyNeighborCounts)
			applyNeighborCounts(neighborNerfFactor, neighborBuffFactor);
//		System.out.println("After Neighbor Counts");
//		print();
		
		int iterations=0;
		while(updated) {
			iterations++;
			if(System.currentTimeMillis() - time > 100) {
				System.out.println("Time threshold reached in (Guesser.java). "
						+ " Placement does not represent the known probabilities.");
				
				double totalPiece = 0;
				for(Piece piece : redPieces.keySet()) {
					totalPiece += DoubleStream.of(redPieces.get(piece)).sum();
//					System.out.println(piece + " " + DoubleStream.of(redPieces.get(piece)).sum());
				}

				double totalRank = 0;
				for(int rank=0; rank<RANKS.length; rank++) {
					double rankProb = 0;
					for(Piece piece : redPieces.keySet()) {
						rankProb += redPieces.get(piece)[rank];
					}
					totalRank += rankProb;
//					System.out.println(RANKS[rank] + " " + rankProb);
				}
				print();
				System.out.println("totalPieces: " + totalPiece);
				System.out.println("totalRanks: " + totalRank);
				System.out.println("iterations: " + iterations);
				
				return;
			}

			updated = false;
			
			for(int i=0; i<2; i++) {
				Map<Piece, double[]> pieces = i==0 ? redPieces : bluePieces;
				updated |= normalizeRanks(pieces, epsilon);
				updated |= normalizePieces(pieces, i==0, epsilon);
			}
		}
	}
   
	/**
	 * Applies neighbor relations to the probabilities
	 * 
	 * @param nerfFactor weight of the neighbor probabilities
	 * @param buffFactor weight of the neighbor probabilities
	 */
	private void applyNeighborCounts(double nerfFactor, double buffFactor) {
		for (int team = 0; team < 2; team++) {
			for (Piece known : startState.getPieces()[team]) {
				if(!known.getKnown()) continue;
				buffDirectNeighbors(known, buffFactor);
				nerfFarPieces(known, nerfFactor);
			}
		}
	}

	/**
	 * Magnifies all rank probabilities for Pieces directly connected to known based on the {@link #neighborProbabilities} relation
	 * @param known center Piece, PieceType is known
	 * @param buffFactor weight of the neighbor probabilities
	 */
	private void buffDirectNeighbors(Piece known, double buffFactor) {
		Map<Piece, double[]> rankProbs = (known.getTeam()) ? redPieces : bluePieces;
		for (RelativePosition pos : RelativePosition.values()) {
			if(!known.getTeam()) pos = pos.mirror();
			int x = pos.transformX(known.getX());
			int y = pos.transformY(known.getY());

			if (x >= 0 && x < 8 && y >= 0 && y < 8) {
				Piece neighbor = startState.inspect(x, y);
				if(	// is neighbor not null and unknown?
						neighbor != null 
						&& !neighbor.getKnown()) {
					if ( // does the known-pos pair exist in neighborProbabilities?
							neighborProbabilities.get(known.getType()) != null
							&& neighborProbabilities.get(known.getType()).get(pos) != null) {
						Map<PieceType, Double> neighbors = neighborProbabilities.get(known.getType()).get(pos);
						if (neighbors != null) {
							double[] neighborProbs = rankProbs.get(neighbor);

							for (int r=0; r<RANKS.length; r++) {
								if(neighbors.get(RANKS[r]) != null) {
									neighborProbs[r] *= 1 + buffFactor * neighbors.get(RANKS[r]);
								}
							}
						}
					}
					
//					for(int r=0; r<RANKS.length; r++) {
//							if(neighborProbabilities.get(RANKS[r]) != null
//									&& neighborProbabilities.get(RANKS[r]).get(pos.invert()) != null
//									&& neighborProbabilities.get(RANKS[r]).get(pos.invert()).get(known.getType()) != null) {
//								double knownAsNeighborProb = neighborProbabilities.get(RANKS[r]).get(pos.invert()).get(known.getType());
//								
//								double[] neighborProbs = rankProbs.get(neighbor);
//								neighborProbs[r] *= 1 + buffFactor * knownAsNeighborProb;
//								
//							}
//					}
				}
			}
		}
	}
		
	/**
	 * Calculates the euclidean distance between known location and all other unknown Pieces.
	 * If the distance is smaller than 1.5, meaning the fields right around piece, nothing happens.
	 * Otherwise the unknown Pieces rank possibilities get lowered by p percent, 
	 * where p is the combined probability of the unknown Piece being knowns neighbor.
	 * @param known center Piece, Pieces around it won't be nerfed
	 * @param nerfFactor weight of the neighbor probabilities
	 */
	public void nerfFarPieces(Piece known, double nerfFactor) {
		Map<Piece, double[]> rankProbs = (known.getTeam()) ? redPieces : bluePieces;
	
		// TODO following 4 lines are used for testing normalized probabilities. 
		// if commented in, comment the for{} after combinedProb = ... out and comment everything regarding rankMap[] in.
//		int knownRank = 0; 
//		for(int rank=0; rank<RANKS.length; rank++)
//			if(known.getType() == RANKS[rank])
//				knownRank=rank;
		
		for(int r=0; r<RANKS.length; r++) {
			double combinedProb = 0; //rankMap[knownRank][r];
			for(RelativePosition pos : RelativePosition.values()) {
				if(		neighborProbabilities.get(known.getType()) != null
						&& neighborProbabilities.get(known.getType()).get(pos) != null
						&& neighborProbabilities.get(known.getType()).get(pos).get(RANKS[r]) != null) {
					combinedProb += neighborProbabilities.get(known.getType()).get(pos).get(RANKS[r]);
				}
			}
			
			for(Piece notNeighbor : rankProbs.keySet()) {
				if(!notNeighbor.getKnown()
						&& Math.sqrt((known.getY() - notNeighbor.getY()) * (known.getY() - notNeighbor.getY()) 
								+ (known.getX() - notNeighbor.getX()) * (known.getX() - notNeighbor.getX())) > 1.5) {
					rankProbs.get(notNeighbor)[r] *= 1 - nerfFactor * combinedProb;
				}
			}
		}
	}
	
//	static double[][] rankMap = new double[7][7];
//	static {
//		rankMap [0] = new double[]{0, 0.33, 0.04, 0.26, 0.14, 0.11, 0.12};
//		rankMap [1] = new double[]{0.22, 0.08, 0.03, 0.26, 0.15, 0.15, 0.11};
//		rankMap [2] = new double[]{0.1, 0.1, 0, 0.23, 0.18, 0.23, 0.15};
//		rankMap [3] = new double[]{0.16, 0.24, 0.06, 0.18, 0.13, 0.11, 0.13};
//		rankMap [4] = new double[]{0.16, 0.25, 0.09, 0.25, 0, 0.12, 0.14};
//		rankMap [5] = new double[]{0.13, 0.26, 0.12, 0.22, 0.13, 0, 0.14};
//		rankMap [6] = new double[]{0.15, 0.21, 0.08, 0.26, 0.15, 0.16, 0};
//		}
	
	/**
	 * Normalizes the ranks probabilities for one Piece so the values add up to 1.
	 * Only normalizes if the probabilities don't already add up to 1.
	 * i. e. normalizes the d values in "P: d,d,d,d,d,d,d"
	 * @param pieces Map to get probabilities double array to normalize
	 * @param epsilon probability deviation above and below 1
	 */
	private boolean normalizeRanks(Map<Piece, double[]> pieces, double epsilon) {
		boolean updated = false;
		for(Piece p : pieces.keySet()) {
			double sum = 0;
			for(int d=0; d<pieces.get(p).length; d++) {
				sum += pieces.get(p)[d];
			}

			if(Math.abs(1.0 - sum) > epsilon) {
				updated = true;
				for(int i=0; i<pieces.get(p).length; i++) {
					pieces.get(p)[i] /= sum;
				}
			}
		}
		return updated;
	}

	/**
	 * Normalizes probabilities across Pieces
	 * @param pieces pieces to normalize
	 * @param team true = red
	 * @param epsilon probability deviation above and below 1
	 */
	private boolean normalizePieces(Map<Piece, double[]> pieces, boolean team, double epsilon) {
		boolean updated = false;
		//		Map<PieceType, Integer> ranksInGame = getInGameRankCounts(team);

		for(int i=0; i<RANKS.length; i++) {
			double sum = 0;
			for(Piece piece : pieces.keySet()) {
				double value = pieces.get(piece)[i];
				sum += value;
			}
			
			double targetRank = RANKS[i].getPieceCount();//ranksInGame.get(RANKS[i]);
			if(Math.abs(targetRank - sum) > epsilon) {
				updated = true;
				double factor = targetRank / sum;
				for(Piece piece : pieces.keySet())
					pieces.get(piece)[i] *= factor;
			}
		}

		return updated;
	}

	/**
	 * Returns how many Pieces of each rank has
	 * @return <type - Piece Counts> Map
	 */
	Map<PieceType, Integer> getRankCounts() {
		Map<PieceType, Integer> rankCounts = new EnumMap<>(PieceType.class);
		for (PieceType type : RANKS) { 
			rankCounts.put(type, type.getPieceCount()); 
		}
		return rankCounts;
	}

	/**
	 * Returns how many Pieces of each rank are still alive
	 * @param team to check
	 * @return <type - alive Pieces> Map
	 */
	@Deprecated
	Map<PieceType, Integer> getInGameRankCounts(boolean team) {
		Map<PieceType, Integer> rankCounts = new EnumMap<>(PieceType.class);
		for (PieceType type : RANKS) { 
			var pieces = (team ? redDeadPieces : blueDeadPieces);
			rankCounts.put(type, type.getPieceCount() - (pieces.get(type) == null ? 0 : pieces.get(type))); 
		}
		return rankCounts;
	}

	public void print() {
		for(Piece p : redPieces.keySet()) {
			System.out.print(p + ": ");
			for(double d : redPieces.get(p))
				System.out.print((Math.round(d * 100.) / 100.) + " ");
			System.out.println();
		}
		System.out.println("--- ||| ---");
		for(Piece p : bluePieces.keySet()) {
			System.out.print(p + ": ");
			for(double d : bluePieces.get(p))
				System.out.print((Math.round(d * 100.) / 100.) + " ");
			System.out.println();
		}
	}

	public PieceType guess(Piece piece, Map<PieceType, Integer> aliveRanks) {
		piece = startState.inspect(piece.getX(), piece.getY());

		double[] probabilities = piece.getTeam() ? redPieces.get(piece) : bluePieces.get(piece);
		int maxIndex = -1;
		double maxProbability = -1;
		for(int i=0; i<probabilities.length; i++) {
			if(aliveRanks.get(RANKS[i]) <= 0)  continue;

			if(probabilities[i] > maxProbability) {
				maxIndex = i;
				maxProbability = probabilities[maxIndex];
			}
		}
		int newAlive = aliveRanks.remove(RANKS[maxIndex]) -1;
		aliveRanks.put(RANKS[maxIndex], newAlive);
		return RANKS[maxIndex];
	}

	public void update(AIInformer informer) {
		Move move = informer.lastMove().normalize(currentState);

		Piece attackerInCurrentStateBeforeMove = currentState.inspect(move.getStartX(), move.getStartY());
		Piece defenderInCurrentStateBeforeMove = currentState.inspect(move.getEndX(), move.getEndY());
		Piece attackerInStartState = currentToStartStateMap.get(attackerInCurrentStateBeforeMove);
		Piece defenderInStartState = currentToStartStateMap.get(defenderInCurrentStateBeforeMove);

		if (informer.wasAttack()) {
			Piece targetOnInformer = informer.obfuscatedGameState().inspect(move.getEndX(), move.getEndY());

			if (targetOnInformer == null) {	// Both died
				revealPiece(attackerInStartState, informer.attacker());
				revealPiece(attackerInCurrentStateBeforeMove, informer.attacker());

				revealPiece(defenderInStartState, informer.defender());
				revealPiece(defenderInCurrentStateBeforeMove, informer.defender());

				updateDeadPiecesMap(attackerInCurrentStateBeforeMove, informer.attacker());
				updateDeadPiecesMap(defenderInCurrentStateBeforeMove, informer.defender());
			} else if (targetOnInformer.getTeam() == move.getPiece().getTeam()) { // Attacker won
				revealPiece(attackerInStartState, informer.attacker());
				revealPiece(attackerInCurrentStateBeforeMove, informer.attacker());

				revealPiece(defenderInStartState, informer.defender());
				revealPiece(defenderInCurrentStateBeforeMove, informer.defender());

				updateDeadPiecesMap(defenderInCurrentStateBeforeMove, informer.defender());
			} else { // Attacker lost
				revealPiece(attackerInStartState, informer.attacker());
				revealPiece(attackerInCurrentStateBeforeMove, informer.attacker());

				revealPiece(defenderInStartState, informer.defender());
				revealPiece(defenderInCurrentStateBeforeMove, informer.defender());

				updateDeadPiecesMap(attackerInCurrentStateBeforeMove, informer.attacker());
			}
		} else { // Not an attack
			if (move.getFields() > 1) { // Scout moved multiple fields
				revealPiece(attackerInStartState, PieceType.SPAEHER);
				revealPiece(attackerInCurrentStateBeforeMove, PieceType.SPAEHER);
			} else { // Moved one field, cannot be Bomb or Flag
				setRankProbability(attackerInStartState, PieceType.FLAGGE, false);
				setRankProbability(attackerInStartState, PieceType.BOMBE, false);
				movable.add(attackerInStartState);
			}
		}

		if (!Utils.execute(currentState, move))
			System.err.println("Error in Guesser : update, Move not executed: " + move);

		if(attackerInCurrentStateBeforeMove.getKnown()) {
			attackerInStartState.setKnown(true);
			if(defenderInStartState != null) { 
				defenderInStartState.setKnown(true);
			}
		}
		
		
		this.lastMove = move;
		
		this.normalize();
	}

	private void updateDeadPiecesMap(Piece pieceInCurrentState, PieceType typeOfDeadPiece) {
		Map<PieceType, Integer> deadMap = pieceInCurrentState.getTeam() ? redDeadPieces : blueDeadPieces;
		deadMap.put(typeOfDeadPiece, deadMap.getOrDefault(typeOfDeadPiece, 0) + 1);
		deadPieces.add(pieceInCurrentState);
	}

	/**
	 * Sets a Pieces rank to type and sets is probability to 1 at {@link #RANKS}[type].
	 * The Piece won't be declared as known to use that information within the AI.
	 * @param piece
	 * @param type
	 */
	public void revealPiece(Piece piece, PieceType type) {
		piece.setType(type.getByte());
		setRankProbability(piece, type, true);

		inferLastPiece(piece.getTeam());
	}
	
	private void inferLastPiece(boolean team) {
		if((team ? startState.getKnownRed() : startState.getKnownBlue()) == 9 ) {
			PieceType missingRank = null;
			for(PieceType rank : RANKS) {
				int count = rank.getPieceCount();

				for(Piece p: startState.getPieces()[team ? 0 : 1])
					if(p.getType() == rank && p.getKnown()) {
						count --;
					}
				if(count > 0) {
					missingRank = rank;
					break;
				}
			}
			for(Piece p : startState.getPieces()[team ? 0 : 1]) {
				if(!p.getKnown()) {
					p.setKnown(true);
					startToCurrentStateMap.get(p).setKnown(true);
					startState.incrementKnown(team);
					currentState.incrementKnown(team);
					revealPiece(p, missingRank);
				}
			}
		}
	}

	/**
	 * Sets the possibility for piece to be from rank type to 0 or 1.
	 * @param piece
	 * @param type
	 * @param toOne true to set type possibility to 1 and everything else to 0. False to keep everything but set type to 0.
	 */
	private void setRankProbability(Piece piece, PieceType type, boolean toOne) {
		double[] probs = (piece.getTeam() ? redPieces : bluePieces).get(piece);
		if(probs != null) {
			int rank = 0;
			for(int i=0; i<RANKS.length; i++)
				if(RANKS[i] == type)
					rank = i;
			for(int i=0; i<probs.length; i++)
				probs[i] = toOne ? 0 : probs[i];
			probs[rank] = toOne ? 1 : 0;
		}
	}

	public Move getLastMove(GameState state) {
		if(lastMove != null)
			return lastMove.clone(state);
		return lastMove;
	}

	@SuppressWarnings("incomplete-switch")
	private GameState unshuffle(GameState state) {
		for(int team=0; team<2; team++) {
			List<Piece> pieces = new ArrayList<Piece>();
			Stream.of(state.getPieces()[team]).forEach(p -> pieces.add(p));
			Arrays.fill(state.getPieces()[team], null);

			int doubleCountBombe = 0;
			int doubleCountScout = 0;
			int doubleCountMineur = 0;
			while(pieces.size() > 0) {
				Piece piece = pieces.removeFirst();
				switch(piece.getType()) {
				case FLAGGE:
					state.getPieces()[team][9] = piece;
					break;
				case BOMBE:
					state.getPieces()[team][8 - doubleCountBombe++] = piece;
					break;
				case SPIONIN:
					state.getPieces()[team][6] = piece;
					break;
				case SPAEHER:
					state.getPieces()[team][5 - doubleCountScout++] = piece;
					break;
				case MINEUR:
					state.getPieces()[team][3 - doubleCountMineur++] = piece;
					break;
				case GENERAL:
					state.getPieces()[team][1] = piece;
					break;
				case MARSCHALL:
					state.getPieces()[team][0] = piece;
					break;
				}
			}
		}

		return state;
	}
	
	public double[] getPieceProbsFromStartPos(byte startPos) {
		Piece piece = startPosToPieces.get(startPos);
		
		return piece.getTeam() ? redPieces.get(piece) : bluePieces.get(piece);
	}

	/**
	 * Returns how accurately the Pieces of team got guessed.
	 * @param original
	 * @param myTeam the team which won't get guessed but taken from {@link #currentState}
	 * @return
	 */
	public double accuracy(GameState original, boolean myTeam) {
		int pieces = 0;
		double guessed = 0;

		GameState state = legacySearch ? convergeToStartStateLegacy(!myTeam ? 1 : 2) : convergeToStartState(!myTeam ? 1 : 2);

		for(int x=0; x<8; x++)
			for(int y=0; y<8; y++) {
				Piece originalPiece = original.inspect(x, y);
				Piece guessedPiece = state.inspect(x, y);
				if(originalPiece != null && originalPiece.getTeam() != myTeam) {
					pieces++;
					if(originalPiece.getType() == guessedPiece.getType())
						guessed++;
				}
			}

		return guessed / pieces;
	}
}
