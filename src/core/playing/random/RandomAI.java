package core.playing.random;

import java.util.ArrayList;
import java.util.SplittableRandom;

import core.Direction;
import core.GameState;
import core.Move;
import core.Piece;
import core.playing.AI;

/**
 * AI that returns random Moves
 */
public class RandomAI extends AI {
	SplittableRandom random;

	ArrayList<Piece> pieces = new ArrayList<Piece>(8);
	ArrayList<int[]> dirMap = new ArrayList<int[]>();

	public RandomAI(boolean team, GameState gameState) {
		super(team, gameState);
		random = new SplittableRandom();
	}

	/**
	 * Selects a random Move and returns it
	 * @return a random valid Move, wrong Moves or null if no Move is possible
	 */
	public Move nextMove() {
		Move move = null;
		pieces.clear();

		for(int i=0; i<8; i++)
			if(myPieces[i] != null) pieces.add(myPieces[i]);

		while(pieces.size() > 0) {
			int randomInt = random.nextInt(pieces.size());
			Piece picked = pieces.get(randomInt);

			fillDirectionMap(gameState, picked, dirMap);
			while (dirMap.size() > 0) {
				move = getDirectionMove(dirMap, picked);

				if(isMovePossible(gameState, move))
					return move;
			}
			pieces.remove(randomInt);
		}

		if(move == null)
			System.err.println("first move not possible, see RandomAI.java#136");
		return move;
	}
	
	/**
	 * Returns the next Move only relying on randomness, without structures to inhibit potential repetitions
	 * @return a random valid Move, null if no Move is possible.
	 */
	public Move nextMoveSimple() {
		Move move = null;
//		if(!anyMovePossible(gameState)) return move;
		do {
			Piece picked = myPieces[random.nextInt(10)];
			if(picked == null || picked.getType().getMoves() == 0) continue;
			Direction dir = Direction.get(random.nextInt(4));
			int fields = random.nextInt(picked.getType().getMoves()) + 1;
			move = new Move(picked, dir, fields);
		} while (move == null || !isMovePossible(gameState, move));

		return move;
	}
	
	/**
	 * Extremely simple implementation of a RandomAI.
	 * Uses {@link #getAllPossibleMoves(GameState)}, then picks a random Move from that
	 * @return a random valid Move
	 */
	public Move nextMoveSuperSimple() {
		Move[] moves = getAllPossibleMoves(gameState);
		return moves[random.nextInt(moves.length)];
	}

	//	private Move getAttackingMove(Move move, GameState gameState) {
	//		ArrayList<int[]> reachableEnemyPieces = new ArrayList<int[]>();
	//		for(int i=0; i<4; i++)
	//			reach(gameState.getField(), move.getPiece(), i, reachableEnemyPieces);
	//		if(reachableEnemyPieces.size() == 0)
	//			return move;
	//		int[] randomPiece = reachableEnemyPieces.get(random.nextInt(reachableEnemyPieces.size()));
	//		return new Move(move, Direction.get(randomPiece[0]), randomPiece[1]);
	//	}

	/**
	 * Picks a random entry from dirMap, them creates a Move with picked
	 * @param dirMap ArrayList filled by {@link #fillDirectionMap(GameState, Piece, ArrayList)}
	 * @param picked Piece responsible for the possible direction-reach pairs in dirMap
	 * @return a Move created by a random direction-reach pair from dirMap
	 */
	public Move getDirectionMove(ArrayList<int[]> dirMap, Piece picked) {
		int[] directionFields = dirMap.get(random.nextInt(dirMap.size()));
		dirMap.remove(directionFields);
		return new Move(picked, Direction.get(directionFields[0]), directionFields[1]);
	}
}
