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
			pieces[0].setPos(3, 7);
			pieces[1].setPos(7, 5);
			pieces[2].setPos(3, 5);
			pieces[3].setPos(4, 5);
			pieces[4].setPos(1, 5);
			pieces[5].setPos(6, 5);
			pieces[6].setPos(2, 6);
			pieces[7].setPos(5, 7);
			pieces[8].setPos(4, 6);
			pieces[9].setPos(4, 7);
		}

	}

	private void placeBlue(int version) {
		switch(version) {
		case 0:
		default:
			pieces[0].setPos(4, 0);
			pieces[1].setPos(0, 2);
			pieces[2].setPos(3, 2);
			pieces[3].setPos(4, 2);
			pieces[4].setPos(1, 2);
			pieces[5].setPos(6, 2);
			pieces[6].setPos(5, 1);
			pieces[7].setPos(2, 0);
			pieces[8].setPos(3, 1);
			pieces[9].setPos(3, 0);
		}
	}
}
