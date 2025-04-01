package core.playing;

import core.Utils;
import core.playing.random.RandomAI;
import core.GameState;
import core.Move;
import core.Piece;

public abstract class AI extends Utils {
	private boolean team;
	public Piece[] myPieces;
	protected Piece[] enemyPieces;
	public GameState gameState;

	public AI(boolean player, GameState gameState) {
		team = player;
		setArraysAndGameState(gameState);
	};

	public boolean getTeam() {
		return team;
	}

	/**
	 * updates the GameState and the Pieces Arrays.
	 * @param state new GameState
	 */
	public void update(GameState state) {
		setArraysAndGameState(state);
	}
	
	protected void setArraysAndGameState(GameState gameState) {
		if(team) { 
			myPieces = gameState.getRedPieces(); 
			enemyPieces = gameState.getBluePieces();
		} else {
			myPieces = gameState.getBluePieces();
			enemyPieces = gameState.getRedPieces();
		}
		this.gameState = gameState;
	}

	public void print() {
		System.out.println(getClass().getSimpleName() + (team ? " red" : " blue") + ":");
		printField(gameState.getField());
	}
	
	abstract public Move nextMove();

	/**
	 * Different AI Types to simulate with
	 */
	public enum Type {
		RANDOM;

		public AI createAI(boolean team, GameState gameState) {
			AI ai; 
			switch(this) {
			case RANDOM:  
				ai = new RandomAI(team, gameState);
				break;
			default: 
				ai = new RandomAI(team, gameState);
				break;
			}
			return ai;
		}
	}
}
