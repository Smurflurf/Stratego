package executable;
import core.GameState;
import core.Move;
import core.PieceType;
import core.Utils;
import core.playing.AI.AIInformer;

/**
 * Manages the GameState while simulating games.
 * Obfuscated a teams pieces, updates the GameState and rejects impossible Moves.
 */
public class Mediator {
	private GameState gameState;
	private Move lastMove;
	private boolean wasLastMoveAnAttack;
	private PieceType attacker;
	private PieceType defender;

	public Mediator(GameState gameState) {
		this.gameState = gameState;
		setLastMove(null, false, null, null);
	}

	public AIInformer getAIInformer(boolean obfuscateFor) {
		return new AIInformer(gameState.obfuscateFor(obfuscateFor), getObfuscatedLastMove(), wasLastMoveAnAttack, attacker, defender);
	}

	/**
	 * Prints the current GameState
	 */
	public void print() {
		Utils.printField(gameState.getField());
	}

	/**
	 * If game is not over yet, use after {@link #isGameOver()}.
	 * @return 0: red wins, 1: blue wins, 2: draw, 3: game is not over
	 */
	public int getWinnerTeam() {
		return Utils.getWinner(gameState);
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

		if(move.getStartX() == move.getEndX() &&
				move.getStartY() == move.getEndY()) {
			System.err.println("Move ain't moving -> Mediator:62");
			return false;
		}

		boolean isAttack = Utils.isAttack(gameState, move);
		PieceType attacker = gameState.inspect(move.getStartX(), move.getStartY()) != null 
				? gameState.inspect(move.getStartX(), move.getStartY()).getType() : null;
		PieceType defender = gameState.inspect(move.getEndX(), move.getEndY()) != null 
				? gameState.inspect(move.getEndX(), move.getEndY()).getType() : null;
		boolean moved = Utils.checkAndExecute(gameState, move);
		if(moved)
			setLastMove(move, isAttack, attacker, defender);
		return moved;
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
	
	public Move getObfuscatedLastMove() {
		if(lastMove == null) return null;
		Move lastMove = this.lastMove.cloneWithoutNormalize();
		lastMove.getPiece().setType(PieceType.UNKNOWN.getByte());
		return lastMove;
	}

	public void setLastMove(Move lastMove, boolean isAttack, PieceType attacker, PieceType defender) {
		if(lastMove != null)  {
			lastMove = lastMove.cloneWithoutNormalize();
			if(isAttack) {
				lastMove.getPiece().setKnown(true);
				this.wasLastMoveAnAttack = true;
				this.attacker = attacker;
				this.defender = defender;
			} else {
//				lastMove.getPiece().setType(PieceType.UNKNOWN.getByte());
				this.wasLastMoveAnAttack = false;
				this.attacker = null;
				this.defender = null;
			}
			this.lastMove = lastMove;
		}
	}
}
