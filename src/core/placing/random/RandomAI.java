package core.placing.random;

import java.util.SplittableRandom;

import core.Piece;
import core.placing.Placer;

/**
 * Places a teams Pieces randomly on its side of the board
 */
public class RandomAI extends Placer {
	private SplittableRandom random;
	
	/**
	 * @param team true is red (lower half of the board)
	 */
	public RandomAI(boolean team) {
		super(team);
		random = new SplittableRandom();
	}
	
	@Override
	public Piece[] place() {
		for(Piece piece : pieces) {
			int x,y;
			do {
				x = random.nextInt(8);
				y = random.nextInt(3) + (team ? 5 : 0);
			} while (fieldIsOccupied(x, y));
			piece.setPos(x, y);
		}
		return pieces;
	}
	
	/**
	 * Checks if a field specified by its x and y coordinates is already occupied by another piece
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean fieldIsOccupied(int x, int y) {
		for(Piece piece : pieces)
			if(piece.getX() == x && piece.getY() == y)
				return true;
		return false;
	}
}
