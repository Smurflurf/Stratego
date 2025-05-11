package core;

public class Piece implements Cloneable {
	private boolean team;
	/**
	 * Represents the x,y position as a 0xxx0yyy byte. Bitmasking extracts the coordinates.
	 */
	private byte pos;
	/**
	 * Contains the information if a Piece is known to the enemy.
	 * Also represents the PieceType as a byte to save space.
	 * k000tttt: byte representation: the left bit represents known, the right four the PieceType
	 */
	private byte knownAndType;

	/**
	 * Initialize a known piece with unknown placement.
	 * Places the piece on 0,0
	 * @param type PieceType
	 * @param team true is red, false is blue
	 */
	public Piece(PieceType type, boolean team) {
		knownAndType = type.getByte();
		this.team = team;
		pos = -1;
	}

	/**
	 * Initialize a known piece
	 * @param knownAndType known and PieceType bit
	 * @param team true for red
	 * @param pos position
	 */
	public Piece(byte knownAndType, boolean team, byte pos) {
		this.knownAndType = knownAndType;
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
				getPos() == piece2.getPos() &&
				getTeam() == piece2.getTeam();
	}

	public String toString() {
		PieceType type = PieceType.getType(knownAndType);
		return (team ? "r" : "b") + "_" + (type.getStrength() == 0 ? 
				(type == PieceType.FLAGGE ? "F" : "B") : 
					type.getStrength());
	}	

	public String coords() {
		return " ["+getX()+"|"+getY()+"]";
	}

	@Override
	public Piece clone() {
		return new Piece(knownAndType, team, pos);
	}

	public Piece clone(byte type) {
		return new Piece(type, team, pos);
	}

	public PieceType getType() {
		return PieceType.getType(knownAndType);
	}

	public boolean getKnown() {
		return 0b11111111 == (0b01111111 | knownAndType);
	}
	
	public void setKnown(boolean known) {
		if(known) {
			knownAndType |= 0b10000000;
		} else {
			knownAndType &= 0b01111111;
		}
	}
	
	/**
	 * Sets only the PieceType
	 * @param type
	 */
	public void setType(byte type) {
		this.knownAndType = (byte) (knownAndType & 0b10000000 | type);
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
