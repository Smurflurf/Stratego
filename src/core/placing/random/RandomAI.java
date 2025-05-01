package core.placing.random;

import core.Piece;
import core.placing.Placer;

/**
 * Places a teams Pieces randomly on its side of the board
 */
public class RandomAI extends Placer {
	
	/**
	 * @param team true is red (lower half of the board)
	 */
	public RandomAI(boolean team) {
		super(team);
	}
	
	@Override
	public Piece[] place() {
		for(Piece piece : pieces) {
			int x,y;
			do {
				x = random.nextInt(8);
				y = random.nextInt(3);
			} while (fieldIsOccupied(x, y));
			piece.setPos(x, y);
		}
		mirrorPlacing();
		return pieces;
	}
}
