package core.placing;

import java.util.SplittableRandom;

import core.Piece;
import core.PieceType;
import core.placing.barrage.HeuristicBarrage;
import core.placing.deboer.HeuristicDeBoer;
import core.placing.prebuilt.PrebuiltStates;
import core.placing.random.RandomAI;

public abstract class Placer {
	protected SplittableRandom random;
	public boolean team;
	/**
	 * Hard Coded Indexes:
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
	 */
	public Piece[] pieces;

	public Placer(boolean team) {
		random = new SplittableRandom();
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
		case DEBOER:
			placer = new HeuristicDeBoer(team);
			break;
		case BARRAGE:
			placer = new HeuristicBarrage(team);
			break;
		default: 
			placer = new RandomAI(team);
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
	 * Checks if a field specified by its x and y coordinates is already occupied by another piece
	 * @param x
	 * @param y
	 * @return true if an x|y position is already occupied
	 */
	protected boolean fieldIsOccupied(int x, int y) {
		for(Piece piece : pieces)
			if(piece.getX() == x && piece.getY() == y)
				return true;
		return false;
	}

	/**
	 * Checks if a field specified by its x and y coordinates is already occupied by another piece
	 * @param location x,y BitMapper byte representation
	 * @return true if an x|y position is already occupied
	 */
	protected boolean fieldIsOccupied(byte location) {
		for(Piece piece : pieces)
			if(piece.getPos() == location)
				return true;
		return false;
	}
	
	/**
	 * Call this after placing Pieces on red's side to mirror the y coordinate to player blue's side.
	 * Converts a placing for red to a placing for blue.
	 */
	protected void mirrorPlacing() {
		if(team)
			for(Piece piece : pieces) {
				piece.setY((byte)(7 - piece.getY()));
			}
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

	/**
	 * Deep Clone Times Hundred.
	 * Deep clones array and multiplies all entries with 100,
	 * @param array to clone
	 * @return cloned array
	 */
	public static int[][] deepCloneTH(int[][] array){
		int[][] clone = new int[array.length][array[0].length];
		for(int i=0; i<array.length; i++)
			for(int ii=0; ii<array[i].length; ii++)
				clone[i][ii] = array[i][ii] * 100;
		return clone;
	}
	
	/**
	 * Different AI Placer Types to place the Pieces on the board
	 */
	public enum Type {
		PREBUILT, RANDOM, DEBOER, BARRAGE;
	}
}
