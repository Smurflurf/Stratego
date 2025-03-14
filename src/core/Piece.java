package core;

public class Piece {
	private PieceType type;
	private boolean team;
	//byte pos; TODO x und y in eine byte Variable packen, mit bitmasken kann x und y extrahiert werden
	private byte x, y;
	
	/**
	 * Initialize a known piece with unknown placement.
	 * Places the piece on 0,0
	 * @param type PieceType
	 * @param team true is red, false is blue
	 */
	public Piece(PieceType type, boolean team) {
		this.type = type;
		this.team = team;
		setPos(0, 0);
	}
	
	/**
	 * Initialize a known piece
	 * @param type PieceType
	 * @param y
	 * @param x
	 */
	public Piece(PieceType type, boolean team, int x, int y) {
		this.type = type;
		this.team = team;
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
	
	public byte getX() {
		return x;
	}
	
	public void setX(byte x) {
		this.x = x;
	}
	
	public byte getY() {
		return y;
	}
	
	public void setY(byte y) {
		this.y = y;
	}
}
