import core.GameState;
import core.Move;
import core.Utils;

/**
 * Manages the GameState while simulating games.
 * Obfuscated a teams pieces, updates the GameState and rejects impossible Moves.
 */
public class Mediator {
	private GameState gameState;
	private Move lastMove;
	
	public Mediator(GameState gameState) {
		this.gameState = gameState;
		setLastMove(null);
	}

	/**
	 * Prints the current GameState
	 */
	public void print() {
		Utils.printField(gameState.getField());
	}

	/**
	 * Gives false information if game is not over yet, use after {@link #isGameOver()}.
	 * @return true if team red, false if team blue won
	 */
	public boolean getWinnerTeam() {
		return !gameState.getTeam();
	}

	/**
	 * Returns the current team.
	 * @return current team in {@link #gameState}
	 */
	public boolean getCurrentTeam() {
		return gameState.getTeam();
	}

	/**
	 * Uses core.Utils for checking if the game is over.
	 * @return true if the game over conditions are met
	 */
	public boolean isGameOver() {
		return Utils.isGameOver(gameState);
	}

	/**
	 * Executes move on {@link #gameState}, if move is not possible, it returns false.
	 * Checks if the Piece in move and move.firstMove has moved, 
	 * checks with Utils.
	 * @param move Move to make
	 * @return true if move is possible and was executed
	 */
	public boolean makeMove(Move move) {
		move.normalize(gameState);
		setLastMove(move);
		
		if(move.getStartX() == move.getEndX() &&
				move.getStartY() == move.getEndY())
			return false;
		
		boolean executed = Utils.checkAndExecute(gameState, move);
		if(executed) gameState.changeTeam();
		return executed;
	}

	/**
	 * Obfuscates the GameState for a given player.
	 * Replaces all known PieceTypes of the enemy team in {@link #gameState} with the UNKNOWN type.
	 * @param team true if player blue will be obfuscated	 
	 */
	public GameState obfuscateFor(boolean team) {
		return gameState.obfuscateFor(team);
	}

	public GameState getGameState() {
		return gameState;
	}

	public Move getLastMove() {
		return lastMove;
	}

	public void setLastMove(Move lastMove) {
		this.lastMove = lastMove;
	}
}
