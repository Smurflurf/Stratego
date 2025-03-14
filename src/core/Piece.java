package core;

public abstract class Piece {
	PieceType type;
	boolean team;
	//byte pos; TODO x und y in eine byte Variable packen, mit bitmasken kann x und y extrahiert werden
	byte x, y;
	
	/**
	 * Initialize an unknown piece.
	 */
	public Piece(boolean team, int x, int y) {
		this.type = PieceType.UNKNOWN;
		setPos(x, y);
	}
	
	/**
	 * Initialize a piece with known strength
	 * @param type PieceType
	 * @param y
	 * @param x
	 */
	public Piece(PieceType type, boolean team, int x, int y) {
		this.type = type;
		setPos(x, y);
	}
	
	public void setPos(int x, int y) {
		this.x = (byte)x;
		this.y = (byte)y;
	}
	
	public Piece fight(Piece piece2) {
		return piece2;
	}
	

	public PieceType getType() {
		return type;
	}

	public void setType(PieceType type) {
		this.type = type;
	}

	public boolean getTeam() {
		return team;
	}

	public void setTeam(boolean team) {
		this.team = team;
	}
}
