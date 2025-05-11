package core.playing.guesser;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.DoubleStream;

import core.GameState;
import core.Move;
import core.Piece;
import core.PieceType;
import core.Utils;
import core.playing.AI.AIInformer;
import strados2.tools.CompressedMapIO;

public class Guesser {
	GameState startState;
	public GameState currentState;
	Move lastMove;
	PieceType[] RANKS = {PieceType.SPIONIN, PieceType.FLAGGE, PieceType.BOMBE, PieceType.MARSCHALL, PieceType.MINEUR, PieceType.GENERAL, PieceType.SPAEHER};
	Map<Piece, double[]> redPieces;
	Map<Piece, double[]> bluePieces;
	Set<Piece> deadPieces;
	Map<PieceType, Integer> redDeadPieces;
	Map<PieceType, Integer> blueDeadPieces;
	Map<PieceType, int[][]> probabilities;
	Map<Piece, Piece> startToCurrentStateMap;
	Map<Piece, Piece> currentToStartStateMap;
	/**
	 * Used in {@link #converge()}, decides if pieces get converged by rank or probability
	 */
	boolean legacySearch;

	/**
	 * Guesses team Pieces.
	 * @param gameState starting position
	 * @param RANKS null to use default, otherwise permutation of {@link #RANKS}
	 */
	public Guesser(GameState gameState, PieceType[] RANKS) {
		if(RANKS != null)
			this.RANKS = RANKS;
		legacySearch = true;


		redPieces = new HashMap<Piece, double[]> ();
		bluePieces = new HashMap<Piece, double[]> ();
		redDeadPieces = new HashMap<PieceType, Integer>();
		blueDeadPieces = new HashMap<PieceType, Integer>();
		probabilities = CompressedMapIO.loadCompressedMaps("barrage");
		//		Map<PieceType, Map<RelativePosition, Map<PieceType, Double>>> neigbors = NeighborIO.loadNeighborCounts("barrage");
		currentState = gameState.clone();
		startState = gameState.clone();
		startToCurrentStateMap = new HashMap<Piece, Piece>();
		currentToStartStateMap = new HashMap<Piece, Piece>();
		deadPieces = new HashSet<Piece>();

		for(int i=0; i<2; i++)
			for(Piece startPiece : startState.getPieces()[i]) {
				for(Piece piece : currentState.getPieces()[i]) {
					if(startPiece.getPos() == piece.getPos()) {
						startToCurrentStateMap.put(startPiece, piece);
						currentToStartStateMap.put(piece, startPiece);
					}
				}
			}

		for(int i=0; i<2; i++)
			for(Piece piece : startState.getPieces()[i]) {
				initPiece(piece);
			}
		normalize();
	}

	/**
	 * Initializes piece with start probabilities int {@link #redPieces} or {@link #bluePieces} depending on team.
	 * Piece initialization is hard coded here!
	 * @param piece Piece to init
	 */
	private void initPiece(Piece piece) {
		int x = piece.getX();
		int y = piece.getY() > 4 ? 7 - piece.getY() : piece.getY();

		double[] probabilities = new double[RANKS.length];

		if(piece.getKnown()) {
			for(int i=0; i<RANKS.length; i++)
				if(piece.getType() == RANKS[i])
					probabilities[i] = 1.0;
		} else {
			for(int i=0; i<RANKS.length; i++)
				probabilities[i] = this.probabilities.get(RANKS[i])[x][y];
		}

		if(piece.getTeam()) {
			redPieces.put(piece, probabilities);
		} else {
			bluePieces.put(piece, probabilities);
		}
	}

	/**
	 * Call to converge the GameState to a GameState without PieceType.UNKNOWN.
	 * Call only after {@link #normalize()} normalized the GameState (happens in constructor).
	 * @param myTeam the team which won't get guessed but taken from {@link #currentState}
	 * @return GameState with guessed ranks
	 */
	public GameState converge(boolean myTeam) {
		GameState cloneWithPieces = legacySearch ? convergeToStartStateLegacy(!myTeam ? 1 : 2) : convergeToStartState(!myTeam ? 1 : 2);
		this.mapStartToCurrent(cloneWithPieces);

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
					bestPieceToAssign.setKnown(true);
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
					fallbackPiece.setKnown(true);
					unassignedPieces.remove(fallbackPiece);
					remainingRankCounts.put(fallbackRank, remainingRankCounts.get(fallbackRank) - 1);
				}
			}
		}
		return clone;
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
				for(int alive=aliveRanks.get(RANKS[rank]); alive>0; alive--) {
					guess(pieces, rank);
				}
			}
		}
		return clone;
	}

	private void guess(List<Piece> pieces, int rank) {
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
	private void normalize() {
		double epsilon = 0.01;
		boolean updated = true;
		long time = System.currentTimeMillis();

		while(updated) {
			if(System.currentTimeMillis() - time > (epsilon * 100) * 100) {
				System.out.println("Time threshold reached. "
						+ "Probably wrong initialized pieces (Guesser.java). "
						+ "Does not matter for big simulations. Use \"barrage\" probabilities.");
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
	 * Normalizes the ranks probabilities for one Piece so the values add up to 1.
	 * Only normalizes if the probabilities don't already add up to 1.
	 * i. e. normalizes the d values in "P: d,d,d,d,d,d,d"
	 * @param pieces Map to get probabilities double array to normalize
	 * @param epsilon probability deviation above and below 1
	 */
	private boolean normalizeRanks(Map<Piece, double[]> pieces, double epsilon) {
		boolean updated = false;
		for(Piece p : pieces.keySet()) {
			double sum = DoubleStream.of(pieces.get(p)).sum();
			if(Math.abs(sum - 1.0) > epsilon) {
				updated = true;
				for(int i=0; i<pieces.get(p).length; i++)
					pieces.get(p)[i] /= sum;
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
			for(Piece piece : pieces.keySet())
				sum += pieces.get(piece)[i];

			double targetRank = RANKS[i].getPieceCount();//ranksInGame.get(RANKS[i]);
			if(Math.abs(sum - targetRank) > epsilon) {
				updated = true;
				double factor = targetRank / sum;
				for(Piece piece : pieces.keySet())
					if(pieces.get(piece)[i] < 1) 
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
			}
		}

		if (!Utils.execute(currentState, move))
			System.err.println("Error in Guesser : update, Move not executed: " + move);

		this.lastMove = move;
		this.normalize();
	}

	private void updateDeadPiecesMap(Piece pieceInCurrentState, PieceType typeOfDeadPiece) {
		Map<PieceType, Integer> deadMap = pieceInCurrentState.getTeam() ? redDeadPieces : blueDeadPieces;
		deadMap.put(typeOfDeadPiece, deadMap.getOrDefault(typeOfDeadPiece, 0) + 1);
		deadPieces.add(pieceInCurrentState);
	}

	/**
	 * Sets a Pieces rank to type, declares it as known and sets is probability to 1 at {@link #RANKS}[type].
	 * @param piece
	 * @param type
	 */
	private void revealPiece(Piece piece, PieceType type) {
		piece.setKnown(true);
		piece.setType(type.getByte());
		setRankProbability(piece, type, true);
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
		return lastMove.clone(state);
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
