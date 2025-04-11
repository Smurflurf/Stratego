package core;

public class Piece implements Cloneable {
	private boolean team;
	private byte pos;
	private byte type;

	/**
	 * Initialize a known piece with unknown placement.
	 * Places the piece on 0,0
	 * @param type PieceType
	 * @param team true is red, false is blue
	 */
	public Piece(PieceType type, boolean team) {
		this.type = type.getByte();
		this.team = team;
		setPos(0, 0);
	}

	/**
	 * Initialize a known piece
	 * @param type PieceType
	 * @param y
	 * @param x
	 */
	public Piece(byte type, boolean team, byte pos) {
		setType(type);
		setTeam(team);
		setPos(pos);
	}

	public void setPos(int x, int y) {
		pos = ByteMapper.toByte(x, y);
	}

	public void setPos(byte pos) {
		this.pos = pos;
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
		PieceType type = PieceType.getType(this.type);
		return (team ? "r" : "b") + "_" + (type.getStrength() == 0 ? 
				(type == PieceType.FLAGGE ? "F" : "B") : 
					type.getStrength());
	}	

	public String coords() {
		return " ["+getX()+"|"+getY()+"]";
	}

	@Override
	public Piece clone() {
		return new Piece(type, team, pos);
	}

	public Piece clone(byte type) {
		return new Piece(type, team, pos);
	}

	public PieceType getType() {
		return PieceType.getType(type);
	}

	public void setType(byte type) {
		this.type = type;
	}

	public boolean getTeam() {
		return team;
	}

	public void setTeam(boolean team) {
		this.team = team;
	}

	public byte getPos() {
		return pos;
	}
	
	public int getX() {
		return ByteMapper.getX(pos);
	}

	public void setX(byte x) {
		this.pos = ByteMapper.setX(pos, x);
	}

	public int getY() {
		return ByteMapper.getY(pos);
	}

	public void setY(byte y) {
		this.pos = ByteMapper.setY(pos, y);
	}
}
