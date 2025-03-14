package core.ai.random;

import core.GameState;
import core.Move;
import core.ai.AI;

public class RandomAI extends AI {
	public RandomAI(boolean team, GameState gameState) {
		super(team, gameState);
	}
	
	@Override
	public Move nextMove() {
		// TODO Auto-generated method stub
		System.out.println("Random");
		return null;
	}
	
}
