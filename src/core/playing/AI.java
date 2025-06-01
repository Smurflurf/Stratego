package core.playing;

import core.GameState;
import core.Move;
import core.Piece;
import core.PieceType;
import core.Utils;
import core.playing.guesser.Guesser;
import core.playing.heuristic.HeuristicAI;
import core.playing.human.HumanInput;
import core.playing.mcts.MCTS;
import core.playing.random.RandomAI;

public abstract class AI extends Utils {
	private boolean team;
	public Piece[] myPieces;
	protected Piece[] enemyPieces;
	public GameState gameState;
	public Guesser guesser;
	public Move lastMove;

	public AI(boolean player, GameState gameState, String ...guesserProbs ) {
		team = player;
		guesser = new Guesser(gameState, null, guesserProbs);
		gameState = guesser.converge(team);
		setArraysAndGameState(gameState);
	};
	
	public boolean getTeam() {
		return team;
	}

	/**
	 * Updates the GameState and the Pieces Arrays.
	 * Override in more sophisticated AIs.
	 * Due to shuffle the Piece indexes in the state enemy Piece array won't stay the same.
	 * @param state new GameState
	 */
	public void update(AIInformer informer) {
		guesser.update(informer);
		GameState newState = guesser.converge(team);
		this.lastMove = guesser.getLastMove(newState);
		setArraysAndGameState(newState);
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
		RANDOM, HUMAN, MCTS, HEURISTIC;

		public AI createAI(boolean team, GameState gameState, String ... guesserProbs) {
			AI ai; 
			switch(this) {
			case RANDOM:  
				ai = new RandomAI(team, gameState);
				break;
			case HUMAN:
				ai = new HumanInput(team, gameState);
				break;
			case MCTS:
				ai = new MCTS(team, gameState, guesserProbs);
				break;
			case HEURISTIC:
				ai = new HeuristicAI(team, gameState);
				break;
			default: 
				ai = new RandomAI(team, gameState);
				break;
			}
			return ai;
		}
	}

	public record AIInformer(GameState obfuscatedGameState, Move lastMove, boolean wasAttack, PieceType attacker, PieceType defender) {};
}
