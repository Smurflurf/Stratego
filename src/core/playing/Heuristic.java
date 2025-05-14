package core.playing;

import core.GameState;

public interface Heuristic {
	/**
	 * Evaluates a score representing the players winning chances.
	 * @param state GameState to analyze
	 * @return an Integer the describes the game,
	 * 			>0:	team red got a better position
	 * 			<0: team blue got a better position
	 */
	public int evaluate(GameState state);
}
