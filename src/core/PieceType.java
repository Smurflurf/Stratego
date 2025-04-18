package core;

/**
 * Represents the different Piece types.
 * Includes information about their attack strength, how they move and 
 */
public enum PieceType {
	MARSCHALL(1, 10, 1, (byte)6), 
	GENERAL(1, 9, 1, (byte)5), 
	MINEUR(1, 3, 2, (byte)4), 
	SPAEHER(7, 2, 2, (byte)3), 
	SPIONIN(1, 1, 1, (byte)2), 
	BOMBE(0, 0, 2, (byte)1), 
	FLAGGE(0, 0, 1, (byte)0),
	UNKNOWN(7, 10, 10, (byte)-1);
	
	private final int moves;
	private final int strength;
	private final int pieceCount;
	private final byte byteType;
	
	private PieceType(int moves, int strength, int pieceCount, byte byteType) {
		this.moves = moves;
		this.strength = strength;
		this.pieceCount = pieceCount;
		this.byteType = byteType;
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
	
	public byte getByte() {
		return byteType;
	}
	
	public static PieceType getType(byte b) {
		switch(b) {
		case 0: return PieceType.FLAGGE;
		case 1: return PieceType.BOMBE;
		case 2: return PieceType.SPIONIN;
		case 3: return PieceType.SPAEHER;
		case 4: return PieceType.MINEUR;
		case 5: return PieceType.GENERAL;
		case 6: return PieceType.MARSCHALL;
		default: return PieceType.UNKNOWN;
		}
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
