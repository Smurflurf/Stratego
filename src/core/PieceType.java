package core;

/**
 * Represents the different Piece types.
 * Includes information about their attack strength, how they move and 
 */
public enum PieceType {
	MARSCHALL(1, 10, 1), 
	GENERAL(1, 9, 1), 
	MINEUR(1, 3, 2), 
	SPAEHER(7, 1, 2), 
	SPIONIN(1, 2, 1), 
	BOMBE(0, 0, 2), 
	FLAGGE(0, 0, 1),
	UNKNOWN(-1, -1, 10);
	
	private int moves;
	private int strength;
	private int pieceCount;
	
	private PieceType(int moves, int strength, int pieceCount) {
		this.moves = moves;
		this.strength = strength;
		this.pieceCount = pieceCount;
	}

	public int getMoves() {
		return moves;
	}

	public int getStrength() {
		return strength;
	}

	public int getPieceCount() {
		return pieceCount;
	}
}
