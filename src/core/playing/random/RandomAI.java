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
	static SplittableRandom random = new SplittableRandom();

	ArrayList<Piece> pieces = new ArrayList<Piece>(8);
	ArrayList<int[]> dirMap = new ArrayList<int[]>();
	static ArrayList<Piece> staticPieces = new ArrayList<Piece>(8);
	static ArrayList<int[]> staticDirMap = new ArrayList<int[]>();

	public RandomAI(boolean team, GameState gameState) {
		super(team, gameState);
	}

	@Override
	public void update(AIInformer informer) {
		super.update(informer);
	}
	
	/**
	 * Selects a random Move and returns it
	 * @return a random valid Move, wrong Moves or null if no Move is possible
	 */
	@Override
	public Move nextMove() {
		Move move = null;
		pieces.clear();

		for(int i=0; i<7; i++)
			if(myPieces[i] != null && myPieces[i].getType().getMoves() > 0) pieces.add(myPieces[i]);
		
		while(pieces.size() > 0) {
			int randomInt = random.nextInt(pieces.size());
			Piece picked = pieces.get(randomInt);

			fillDirectionMap(gameState, picked, dirMap);
			while (dirMap.size() > 0) {
				move = getDirectionMove(dirMap, picked);

				if(isMovePossible(gameState, move)) {
					return move;
				}
			}
			pieces.remove(randomInt);
		}

		System.err.println("first move not possible, see RandomAI.java#136");
		return null;
	}
	
	/**
	 * Selects a random Move and returns it
	 * @param state the GameState to pick a Move from
	 * @return a random valid Move, wrong Moves or null if no Move is possible
	 */
	public static Move nextMove(GameState state) {
		Move move = null;
		staticPieces.clear();

		for(int i=0; i<7; i++)
			if(state.getCurrentPieces()[i] != null 
			&& state.getCurrentPieces()[i].getType().getMoves() > 0) 
				staticPieces.add(state.getCurrentPieces()[i]);

		while(staticPieces.size() > 0) {
			int randomInt = random.nextInt(staticPieces.size());
			Piece picked = staticPieces.get(randomInt);

			fillDirectionMap(state, picked, staticDirMap);
			while (staticDirMap.size() > 0) {
				move = getDirectionMove(staticDirMap, picked);

				if(isMovePossible(state, move)) {
					return move;
				}
			}
			staticPieces.remove(randomInt);
		}

		System.err.println("first move not possible, see RandomAI.java#136");
		return null;
	}
	
	/**
	 * At least in the Starting position faster than {@link #nextMove(GameState)}
	 * @param state
	 * @return
	 */
	public static Move nextMoveSimple(GameState state) {
		Move move = null;
		staticPieces.clear();
		for(int i=0; i<state.getCurrentPieces().length; i++)
			if(state.getCurrentPieces()[i] != null 
			&& state.getCurrentPieces()[i].getType().getMoves() > 0) 
				staticPieces.add(state.getCurrentPieces()[i]);
		
//		if(!anyMovePossible(gameState)) return move;
		do {
			Piece picked = staticPieces.get(random.nextInt(staticPieces.size()));
			if(picked == null || picked.getType().getMoves() == 0) continue;
			Direction dir = Direction.get(random.nextInt(4));
			int fields = random.nextInt(picked.getType().getMoves()) + 1;
			move = new Move(picked, dir, fields);
		} while (move == null || !isMovePossible(state, move));

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
			Piece picked = myPieces[random.nextInt(7)];
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
	
	/**
	 * Picks a random entry from dirMap, them creates a Move with picked
	 * @param dirMap ArrayList filled by {@link #fillDirectionMap(GameState, Piece, ArrayList)}
	 * @param picked Piece responsible for the possible direction-reach pairs in dirMap
	 * @return a Move created by a random direction-reach pair from dirMap
	 */
	public static Move getDirectionMove(ArrayList<int[]> dirMap, Piece picked) {
		int[] directionFields = dirMap.get(random.nextInt(dirMap.size()));
		dirMap.remove(directionFields);
		return new Move(picked, Direction.get(directionFields[0]), directionFields[1]);
	}
}
