package core.playing.random;

import java.util.ArrayList;
import java.util.SplittableRandom;

import core.Direction;
import core.GameState;
import core.Move;
import core.Piece;
import core.Utils;
import core.playing.AI;

public class RandomAI extends AI {
	SplittableRandom random;

	public RandomAI(boolean team, GameState gameState) {
		super(team, gameState);
		random = new SplittableRandom();
	}

	/**
	 * Returns the next Move only relying on randomness, without structures to inhibit potential repetitions.
	 * Does a possible Moves check to prevent infinite loops.
	 * If a SPAEHER is moving, it uses {@link #getAttackingMove(Move, GameState)}, just as {@link #nextMove()}
	 * @return a random Move, null if no Move is possible.
	 */
	public Move nextMoveSimple() {
		Move move = null;
		if(!anyMovePossible(gameState)) return move;
		do {
			Piece picked = myPieces[random.nextInt(10)];
			if(picked == null || picked.getType().getMoves() == 0) continue;
			Direction dir = Direction.get(random.nextInt(4));
			int fields = random.nextInt(picked.getType().getMoves()) + 1;
			move = new Move(picked, dir, fields);
		} while (move == null || !isMovePossible(gameState, move));


		//		if(move.getPiece().getType() == PieceType.SPAEHER && gameState.inspect(move.getEndX(), move.getEndY()) == null) {
		//			if(random.nextBoolean()) {
		//				GameState gameState = this.gameState.clone();
		//				Utils.makeMove(gameState, move);
		//				
		//				move = getAttackingMove(move, gameState);
		//			}
		//		}

		return move;
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
	 * Selects a random Move.
	 * @param enemyPieces
	 * @return
	 */
	@Override
	public Move nextMove() {
		Move move = null;
		ArrayList<Piece> pieces = new ArrayList<Piece>();
		ArrayList<int[]> dirMap = new ArrayList<int[]>();

		for(int i=0; i<10; i++)
			if(myPieces[i] != null) pieces.add(myPieces[i]);

		while(pieces.size() > 0) {
			int randomInt = random.nextInt(pieces.size());
			Piece picked = pieces.get(randomInt);

			fillDirectionMap(gameState, picked, dirMap);
			while (dirMap.size() > 0) {
				move = getDirectionMove(dirMap, picked);

				if(Utils.isMovePossible(gameState, move))
					return move;
			}
			pieces.remove(randomInt);
		}

		if(move == null)
			System.err.println("first move not possible, see RandomAI.java#136");
		return move;
	}

	public Move getDirectionMove(ArrayList<int[]> dirMap, Piece picked) {
		int[] directionFields = dirMap.get(random.nextInt(dirMap.size()));
		dirMap.remove(directionFields);
		return new Move(picked, Direction.get(directionFields[0]), directionFields[1]);
	}
}
