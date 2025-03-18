package core.playing;

import core.GameState;

public interface Heuristic {
	public int evaluate(GameState state);
}
