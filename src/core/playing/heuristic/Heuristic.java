package core.playing.heuristic;

import core.GameState;

public class Heuristic implements core.playing.Heuristic{
	boolean myTeam;
	public int myKnownMultiplicator;
	public int myDeadMultiplicator;
	public int enemyKnownMultiplicator;
	public int enemyDeadMultiplicator;
	
	
	public Heuristic(boolean myTeam) {
		this.myTeam = myTeam;
		myKnownMultiplicator = 1;
		myDeadMultiplicator = 3;
		enemyKnownMultiplicator = 1;
		enemyDeadMultiplicator = 3;
	}


	@Override
	public int evaluate(GameState state) {
		int red, blue;
		if(myTeam) {
			red = myKnownMultiplicator * state.getKnownRed() + 
					myDeadMultiplicator * state.getDeadRed();
			blue = enemyKnownMultiplicator * state.getKnownBlue() + 
					enemyDeadMultiplicator * state.getDeadBlue();
		} else {
			red = state.getKnownRed() + state.getDeadRed();
			blue = state.getKnownBlue() + state.getDeadBlue();
		}
		return blue - red;
	}
}
