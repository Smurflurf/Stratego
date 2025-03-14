import core.GameState;
import core.Piece;

import java.util.SplittableRandom;

/**
 * Returns start GameStates
 */
public class StartStates {
	private static int availableStartStates = 0;
	
	/**
	 * Generates a GameState as a starting point for simulations
	 * @param gameState no input for a random GameState, an int between zero and {@link #availableStartStates} for a specific GameState
	 * @return one of a few manually created GameStates
	 */
	public static GameState generateStartState(int ... gameState) {
		if(gameState != null && gameState.length > 0 && gameState[0] >= 0 && gameState [0] < availableStartStates)
			return getGameState(0);
		return getGameState(new SplittableRandom().nextInt(availableStartStates));
	}
	
	
	
	private static GameState getGameState(int gameState) {
		GameState state;
		
		switch(gameState) {
		default:
			Piece[][] field = new Piece[8][8];
			state = new GameState(field, null, null);
		}
		
		return state;
	}
}
