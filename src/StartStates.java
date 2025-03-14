import core.GameState;
import core.Piece;
import core.PieceType;
import core.Utils;

import java.util.SplittableRandom;

/**
 * Returns start GameStates
 */
public class StartStates {
	private static int availableStartStates = 0;
	
	/**
	 * Generates a GameState as a starting point for simulations
	 * @param gameState no input for a random GameState, an int between zero and {@link #availableStartStates} for a specific GameState
	 * @return one of a few manually created GameStates
	 */
	public static GameState generateStartState(int ... gameState) {
		if(gameState != null && gameState.length > 0 && gameState[0] >= 0 && gameState [0] < availableStartStates)
			return getGameState(0);
		return getGameState(new SplittableRandom().nextInt(availableStartStates));
	}
	
	public static void main(String[] args) {
		Utils.printField(getGameState(0).getField());
	}
	
	private static GameState getGameState(int gameState) {
		Piece[] red = createPieceArray(true);
		Piece[] blue = createPieceArray(false);
		switch(gameState) {
		default:
			red[0].setPos(3, 7);
			red[1].setPos(7, 5);
			red[2].setPos(3, 5);
			red[3].setPos(4, 5);
			red[4].setPos(1, 5);
			red[5].setPos(6, 5);
			red[6].setPos(2, 6);
			red[7].setPos(5, 7);
			red[8].setPos(4, 6);
			red[9].setPos(4, 7);
			blue[0].setPos(4, 0);
			blue[1].setPos(0, 2);
			blue[2].setPos(3, 2);
			blue[3].setPos(4, 2);
			blue[4].setPos(1, 2);
			blue[5].setPos(6, 2);
			blue[6].setPos(5, 1);
			blue[7].setPos(2, 0);
			blue[8].setPos(3, 1);
			blue[9].setPos(3, 0);
		}
		
		return new GameState(red, blue);
	}
	
	/**
	 * Creates an array with all 8 Pieces a team got in the beginning.
	 * Places the Pieces in the following order (first one being pieces[0]):
	 * 0 MARSCHALL
	 * 1 GENERAL
	 * 2 MINEUR
	 * 3 MINEUR
	 * 4 SPAEHER
	 * 5 SPAEHER
	 * 6 SPIONIN
	 * 7 BOMBE
	 * 8 BOMBE
	 * 9 FLAGGE
	 * @param team true for red
	 * @return array with 8 Pieces, all placed on 0,0
	 */
	public static Piece[] createPieceArray(boolean team) {
		Piece[] pieces = new Piece[10];
		int piece = 0;
		for(PieceType type : PieceType.values()) {
			for(int count = type.getPieceCount(); count > 0 && type != PieceType.UNKNOWN; count--) {
				pieces[piece++] = new Piece(type, team);
			}
		}
		return pieces;
	}
}
