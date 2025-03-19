import core.GameState;
import core.Move;
import core.Piece;
import core.PieceType;
import core.Utils;

/**
 * Manages the GameState while simulating games.
 * Obfuscated a teams pieces, updates the GameState and rejects impossible Moves.
 */
public class Mediator {
	private GameState gameState;
	
	public Mediator(GameState gameState) {
		this.gameState = gameState;
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
		return gameState.getTeam();
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
	 * @param move Move to make
	 * @return true if move is possible and was executed
	 */
	public boolean makeMove(Move move) {
		if(Utils.isMovePossible(move, gameState))
			Utils.makeMove(gameState, move);
		else
			return false;
		return true;
	}
	
	/**
	 * Obfuscates the GameState for a given player.
	 * Replaces all known PieceTypes of the enemy team in {@link #gameState} with the UNKNOWN type.
	 * @param team true if player blue will be obfuscated	 
	 */
	public GameState obfuscateFor(boolean team) {
		GameState obfuscated = gameState.clone();
		
		for(int x=0; x<8; x++)
			for(int y=0; y<8; y++) {
				Piece piece = gameState.getField()[y][x];
				if(piece != null && piece.getTeam() != team)
					piece.setType(PieceType.UNKNOWN);
			}
		
		Piece[] pieces = team ? gameState.getBluePieces() : gameState.getRedPieces();
		for(Piece piece : pieces)
			piece.setType(PieceType.UNKNOWN);
		
		return obfuscated;
	}
	
	public GameState getGameState() {
		return gameState;
	}
}
