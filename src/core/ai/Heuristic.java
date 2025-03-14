package core.ai;

import core.GameState;

public interface Heuristic {
	public int evaluate(GameState state);
}
