package core;

public class Piece implements Cloneable {
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

	public void setPos(int[] pos) {
		this.x = (byte)pos[0];
		this.y = (byte)pos[1];
	}

	/**
	 * Returns the LOSER between this Piece as attacker and piece2 as defender.
	 * If both Pieces are equal, null gets returned.
	 * @param piece2 defender
	 * @return null if no winner, this if defender wins, piece2 if attacker wins
	 */
	public Piece attack(Piece piece2) {
		if(getType() == piece2.getType())
			return null;
		
		return getType().attack(piece2.getType()) ? piece2 : this;
	}
	
	public boolean equals(Piece piece2) {
		return piece2 != null &&
				getType() == piece2.getType() && 
				getX() == piece2.getX() && 
				getY() == piece2.getY() &&
				getTeam() == piece2.getTeam();
	}

	public String toString() {
		return (team ? "r" : "b") + "_" + (type.getStrength() == 0 ? 
				(type == PieceType.FLAGGE ? "F" : "B") : 
					type.getStrength());
	}	

	public String coords() {
		return " ["+x+"|"+y+"]";
	}
	
	@Override
	public Piece clone() {
		return new Piece(type, team, x, y);
	}

	public Piece clone(PieceType type) {
		return new Piece(type, team, x, y);
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
