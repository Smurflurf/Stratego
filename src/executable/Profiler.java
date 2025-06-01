package executable;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;

import core.Direction;
import core.GameState;
import core.Move;
import core.Piece;
import core.PieceType;
import core.Utils;
import core.placing.Placer;
import core.playing.AI;
import core.playing.heuristic.HeuristicAI;
import core.playing.mcts.TreeNode;
import core.playing.random.RandomAI;

/**
 * Contains diverse Methods to test xxx
 */
public class Profiler {
	public static void main(String[] args) {
		memoryFootprint(Class.Piece);

		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		AI ai = AI.Type.HEURISTIC.createAI(true, new GameState(redPieces, bluePieces));
		
//		ai.gameState.removePiece(ai.gameState.getRedPieces()[0]);
//		ai.gameState.removePiece(ai.gameState.getRedPieces()[1]);
//		ai.gameState.removePiece(ai.gameState.getRedPieces()[2]);
//		ai.gameState.removePiece(ai.gameState.getRedPieces()[3]);
//		ai.gameState.removePiece(ai.gameState.getRedPieces()[4]);
//		ai.gameState.removePiece(ai.gameState.getRedPieces()[5]);
//		nextMoves(ai, 1000000);
//		((HeuristicAI)ai).disableTerminalHeuristic();
//		nextMoves(ai, 1000000);
	}

	public static void nextMoves(AI ai, int repetitions) {
		long start = System.currentTimeMillis();
		for (int i=0; i<repetitions; i++) {
			ai.nextMove();
		}
		long end = System.currentTimeMillis();
		System.out.println(String.format("%,d", (end - start)) + " ms for " +String.format("%,d", (repetitions))+ " repetitions");
	}

	/**
	 * Prints out a classes memory Footprint
	 * @param c Class to analyze
	 */
	public static void memoryFootprint(Class c) {
		Object object = null;
		switch(c) {
		case GameState:
			object = new GameState(Placer.placePiecesWith(true, Placer.Type.PREBUILT), Placer.placePiecesWith(false, Placer.Type.PREBUILT));
			Utils.checkAndExecute((GameState) object, RandomAI.nextMove((GameState) object));
			break;
		case Piece:
			object = new Piece(PieceType.MARSCHALL, true);
			break;
		case Move:
			object = new Move(new Piece(PieceType.SPAEHER, true), Direction.UP, 1);
		case TreeNode:
			GameState state = new GameState(Placer.placePiecesWith(true, Placer.Type.PREBUILT), Placer.placePiecesWith(false, Placer.Type.PREBUILT));
			Move move = RandomAI.nextMove(state);
			Utils.checkAndExecute(state, move);
			object = new TreeNode(state, null, move);
		}

		System.out.println(ClassLayout.parseInstance(object).toPrintable());
		//		System.out.println(GraphLayout.parseInstance(object).toPrintable());
		System.out.println(GraphLayout.parseInstance(object).toFootprint());
	}

	enum Class {
		GameState,
		Piece,
		Move,
		TreeNode;
	};
}
