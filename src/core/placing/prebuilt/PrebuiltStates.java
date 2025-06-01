package core.placing.prebuilt;
import core.Piece;
import core.placing.Placer;

/**
 * Returns start GameStates
 */
public class PrebuiltStates extends Placer {
	private static int availableStartStates = 1;

	public PrebuiltStates(boolean team) {
		super(team);
	}

	@Override
	public Piece[] place() {
		if(team)
			placeRed(random.nextInt(availableStartStates));
		else
			placeBlue(random.nextInt(availableStartStates));
		return pieces;
	}

	private void placeRed(int version) {
		switch(version) {
		case 0:
		default:
			pieces[0].setStartPos(3, 7);
			pieces[1].setStartPos(7, 5);
			pieces[2].setStartPos(3, 5);
			pieces[3].setStartPos(4, 5);
			pieces[4].setStartPos(1, 5);
			pieces[5].setStartPos(6, 5);
			pieces[6].setStartPos(2, 6);
			pieces[7].setStartPos(5, 7);
			pieces[8].setStartPos(4, 6);
			pieces[9].setStartPos(4, 7);
		}

	}

	private void placeBlue(int version) {
		switch(version) {
		case 0:
		default:
			pieces[0].setStartPos(4, 0);
			pieces[1].setStartPos(0, 2);
			pieces[2].setStartPos(3, 2);
			pieces[3].setStartPos(4, 2);
			pieces[4].setStartPos(1, 2);
			pieces[5].setStartPos(6, 2);
			pieces[6].setStartPos(5, 1);
			pieces[7].setStartPos(2, 0);
			pieces[8].setStartPos(3, 1);
			pieces[9].setStartPos(3, 0);
		}
	}
}
