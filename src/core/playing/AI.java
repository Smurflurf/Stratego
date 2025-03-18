package core.playing;

import core.Utils;
import core.playing.random.RandomAI;
import core.GameState;
import core.Move;
import core.Piece;

public abstract class AI extends Utils {
	private boolean team;
	protected Piece[] myPieces;
	protected Piece[] enemyPieces;
	private GameState gameState;
	
	public AI(boolean player, GameState gameState) {
		team = player;
		if(team) { 
			myPieces = gameState.getRedPieces(); 
			enemyPieces = gameState.getBluePieces();
		} else {
			myPieces = gameState.getBluePieces();
			enemyPieces = gameState.getRedPieces();
		}
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
