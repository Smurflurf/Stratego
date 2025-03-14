package core;

import core.ai.AI;
import core.ai.random.RandomAI;

/**
 * Useful utilities for the whole project.
 * The methods do not alter their parameters.
 * All methods are static, so extending from Utils is not necessary.
 */
public class Utils {
	/**
	 * Generates a list of all possible Moves (based on gameState current team).
	 * Does not check if the game is still going i.e. no checks for the existence of flags are done here.
	 * @param gameState the gameState to analyze. Its team attribute is considered.
	 * @return all possible moves in gameState
	 */
	public static Move[] getAllPossibleMoves(GameState gameState) {
		return null;
	}
	
	/**
	 * Checks if any Move is possible in gameState.
	 * Uses the same algorithm as {@link #getAllPossibleMoves(GameState)} but stops after finding the first Move.
	 * Use this Method for anything related to possible Move checks, rather than {@link #getAllPossibleMoves(GameState)}.
	 * @return false if no Move is possible in gameState
	 */
	public static boolean anyMovePossible(GameState gameState) {
		return false;
	}

	public static boolean isMovePossible(Move move, GameState gameState) {
		return true;
	}
	
	public static boolean isGameOver(GameState gameState) {
		// TODO consider flags, pieces and so on
		return !anyMovePossible(gameState);
	}
	
	public static void printField(Piece[][] field) {
		for(int y=0; y<8; y++) {
			for(int x=0; x<8; x++) {
				System.out.print(field[x][y] == null ? "  . " : 
					((field[x][y].getTeam() ? "r_" : "b_") + 
							(field[x][y].getType().getStrength() == 0 ? 
									(field[x][y].getType() == PieceType.FLAGGE ? "F" : "B") : 
										field[x][y].getType().getStrength()) + " "));
			}
			System.out.println();
		}
	}
}
