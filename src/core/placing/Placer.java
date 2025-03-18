package core.placing;

import core.Piece;
import core.PieceType;
import core.placing.prebuilt.PrebuiltStates;
import core.placing.random.RandomAI;

public abstract class Placer {
	public boolean team;
	public Piece[] pieces;
	
	public Placer(boolean team) {
		this.team = team;
		pieces = createPieceArray(team);
	}
	
	/**
	 * Places a teams pieces on the board with the selected placing algorithm
	 * @param team the team which pieces will be placed
	 * @param type the algorithm responsible for placing the pieces
	 * @return an Array containing all the teams pieces with their correctly placed locations (x and y initialized)
	 */
	public static Piece[] placePiecesWith(boolean team, Type type) {
		Placer placer;
		switch(type) {
		case PREBUILT: 
			placer = new PrebuiltStates(team);
			break;
		case RANDOM:
			placer = new RandomAI(team);
			break;
		case HEURISTIC:
			placer = null;
			break;
		default: 
			placer = null;
			break;
		}
		return placer.place();
	}
	
	/**
	 * Places a teams pieces on the board.
	 * The team is 
	 */
	abstract public Piece[] place();

	
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
	protected static Piece[] createPieceArray(boolean team) {
		Piece[] pieces = new Piece[10];
		int piece = 0;
		for(PieceType type : PieceType.values()) {
			for(int count = type.getPieceCount(); count > 0 && type != PieceType.UNKNOWN; count--) {
				pieces[piece++] = new Piece(type, team);
			}
		}
		return pieces;
	}
	
	/**
	 * Different AI Placer Types to place the Pieces on the board
	 */
	public enum Type {
		PREBUILT, RANDOM, HEURISTIC;
	}
}
