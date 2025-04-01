package core;

/**
 * Represents the different Piece types.
 * Includes information about their attack strength, how they move and 
 */
public enum PieceType {
	MARSCHALL(1, 10, 1), 
	GENERAL(1, 9, 1), 
	MINEUR(1, 3, 2), 
	SPAEHER(7, 2, 2), 
	SPIONIN(1, 1, 1), 
	BOMBE(0, 0, 2), 
	FLAGGE(0, 0, 1),
	UNKNOWN(7, 10, 10);
	
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
	
	/**
	 * If a fight between two different PieceTypes takes place, the winner gets calculated here.
	 * This is always the attacking Piece
	 * @param type2 defending Piece that gets attacked by this
	 * @return true if this wins, false if type2 wins
	 */
	public boolean attack(PieceType type2) {
		if(type2 == BOMBE) {
			return this == MINEUR;
		} else if(this == SPIONIN) {
			return (type2 == MARSCHALL);
		}
		
		return getStrength() > type2.getStrength();
	}
}
