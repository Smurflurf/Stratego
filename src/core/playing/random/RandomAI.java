package core.playing.random;

import java.util.SplittableRandom;

import core.Direction;
import core.GameState;
import core.Move;
import core.Piece;
import core.playing.AI;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * AI that returns random Moves
 */
public class RandomAI extends AI {
	static SplittableRandom random = new SplittableRandom();

	
	ObjectArrayList<Piece> pieces = new ObjectArrayList<Piece>(8);
	ObjectArrayList<int[]> dirMap = new ObjectArrayList<int[]>();
	static ObjectArrayList<Piece> staticPieces = new ObjectArrayList<Piece>(8);
	static ObjectArrayList<int[]> staticDirMap = new ObjectArrayList<int[]>();

	public RandomAI(boolean team, GameState gameState) {
		super(team, gameState);
	}

	@Override
	public void update(AIInformer informer) {
		super.update(informer);
	}
	
	/**
	 * Selects a random Move and returns it
	 * @return a random valid Move (if possible)
	 */
	@Override
	public Move nextMove() {
		Move move = null;
		pieces.clear();
		for(int i=0; i<7; i++)
			if(gameState.getCurrentPieces()[i] != null 
			&& gameState.getCurrentPieces()[i].getType().getMoves() > 0) 
				pieces.add(gameState.getCurrentPieces()[i]);
		
		do {
			Piece picked = pieces.get(random.nextInt(pieces.size()));
			Direction dir = Direction.get(random.nextInt(4));
			int fields = random.nextInt(picked.getType().getMoves()) + 1;
			
			if(!isMovePossible(gameState, picked, Move.calcEndX(picked.getX(), dir, fields), Move.calcEndY(picked.getY(), dir, fields), dir, fields))
				continue;
			
			move = new Move(picked, dir, fields);
		} while (move == null);

		return move;
	}
	
	/**
	 * {@link #nextMove()} but static.
	 * @param state
	 * @return
	 */
	public static Move nextMove(GameState state) {
		Move move = null;
		staticPieces.clear();
		for(int i=0; i<7; i++)
			if(state.getCurrentPieces()[i] != null) 
				staticPieces.add(state.getCurrentPieces()[i]);
		
		do {
			Piece picked = staticPieces.get(random.nextInt(staticPieces.size()));
			Direction dir = Direction.get(random.nextInt(4));
			int fields = random.nextInt(picked.getType().getMoves()) + 1;
			
			if(!isMovePossible(state, picked, Move.calcEndX(picked.getX(), dir, fields), Move.calcEndY(picked.getY(), dir, fields), dir, fields))
				continue;

			move = new Move(picked, dir, fields);
		} while (move == null);

		return move;
	}
	
	/**
	 * Selects a random Move and returns it.
	 * Use {@link #nextMove(GameState)} instead
	 * @param state the GameState to pick a Move from
	 * @return a random valid Move, wrong Moves or null if no Move is possible
	 * @deprecated
	 */
	public static Move nextMoveComplex(GameState state) {
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

				if(isMovePossible(state, move.getPiece(), move.getEndX(), move.getEndY(), move.getDirection(), move.getFields())) {
					return move;
				}
			}
			staticPieces.remove(randomInt);
		}

		System.err.println("first move not possible, see RandomAI.java#136");
		return null;
	}
	
	/**
	 * Returns the next Move.
	 * Use {@link #nextMove()} instead
	 * @return a random valid Move, null if no Move is possible.
	 * @deprecated
	 */
	public Move nextMoveComplex() {
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

				if(isMovePossible(gameState, move.getPiece(), move.getEndX(), move.getEndY(), move.getDirection(), move.getFields())) {
					return move;
				}
			}
			pieces.remove(randomInt);
		}

		System.err.println("first move not possible, see RandomAI.java#136");
		return null;
	}
	
	/**
	 * Picks a random entry from dirMap, them creates a Move with picked
	 * @param dirMap ArrayList filled by {@link #fillDirectionMap(GameState, Piece, ObjectArrayList)}
	 * @param picked Piece responsible for the possible direction-reach pairs in dirMap
	 * @return a Move created by a random direction-reach pair from dirMap
	 */
	public static Move getDirectionMove(ObjectArrayList<int[]> dirMap, Piece picked) {
		int[] directionFields = dirMap.get(random.nextInt(dirMap.size()));
		dirMap.remove(directionFields);
		return new Move(picked, Direction.get(directionFields[0]), directionFields[1]);
	}
}
