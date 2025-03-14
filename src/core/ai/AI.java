package core.ai;

import core.Utils;
import core.ai.random.RandomAI;
import core.GameState;
import core.Move;

public abstract class AI extends Utils {
	private boolean team;
	private GameState gameState;
	
	public AI(boolean player, GameState gameState) {
		this.team = player;
		this.gameState = gameState;
	};

	public boolean getTeam() {
		return team;
	}
	
	/**
	 * TODO updated den GameState mit einem Move.
	 * @param move
	 */
	public void update(Move move) {
		gameState.update(move);
	}
	
	abstract public Move nextMove();
	
	/**
	 * Different AI Types to simulate with
	 */
	public enum Type {
		RANDOM;
		
		public AI createAI(boolean team, GameState gameState) {
			AI ai = 
					this == RANDOM ? new RandomAI(true, gameState) :
						new RandomAI(true, gameState);
			return ai;
		}
	}
}
