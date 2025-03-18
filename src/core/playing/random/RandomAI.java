package core.playing.random;

import core.GameState;
import core.Move;
import core.playing.AI;

import java.util.SplittableRandom;

public class RandomAI extends AI {
	SplittableRandom random;
	
	public RandomAI(boolean team, GameState gameState) {
		super(team, gameState);
		random = new SplittableRandom();
	}
	
	@Override
	public Move nextMove() {
		
		return null;
	}
	
	public Move nextMoveSimple() {
		Move move = null;
		while(move == null) {
			
		}
		return null;
	}
}
