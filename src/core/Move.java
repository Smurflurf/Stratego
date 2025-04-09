package core;

public class Move {
	private Piece piece;
	private byte start;
	private byte end;
	private byte fields;
	private Direction direction;
//	private int startX;
//	private int startY;
//	private int endX;
//	private int endY;
	
	/**
	 * Use with everything but SPAEHER for a "normal" move.
	 * Transforms direction and fields into coordinates (start and end),
	 * so the Move is not bound to the pieces location.
	 * @param piece
	 * @param direction
	 * @param fields
	 */
	public Move(Piece piece, Direction direction, int fields) {
		setPiece(piece);
		start = ByteMapper.toByte(piece.getX(), piece.getY());
		calculateEndPos(direction, fields);
		this.fields = (byte)fields;
		this.direction = direction;
	}
	
	/**
	 * Clone constructor
	 * @param piece
	 * @param firstMove
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 */
	/*public Move(Piece piece, int startX, int startY, int endX, int endY) {
		setPiece(piece);
		setStartX(startX);
		setStartY(startY);
		setEndX(endX);
		setEndY(endY);
	}*/
	
	/**
	 * Clone constructor
	 * @param piece
	 * @param firstMove
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 */
	public Move(Piece piece, byte start, byte end, Direction direction, byte fields) {
		setPiece(piece);
		this.start = start;
		this.end = end;
		this.direction = direction;
		this.fields = fields;
	}
	
	/**
	 * Normalizes the Move, i.e. if this Moves Piece is not from state, the reference from the according state Piece gets used.
	 * @param state
	 */
	public Move normalize(GameState state) {
		Piece piece = state.getField()[getStartX()][getStartY()];
		if(piece != null)
			setPiece(piece);
		
		return this;
	}
	
	/**
	 * @return all fields {@link #piece} steps over in this Move
	 */
	public Byte[] getRelevantFields() {
		Direction dir = getDirection();
		int fields = getFields() +1;
		Byte[] positions = new Byte[fields];
		for(int i=0; i<fields; i++) {
			positions[i] = dir.translate(start, i);
		}
		return positions;
	}
	
	/**
	 * Calculates {@link #endX} and {@link #endY}, {@link #startX} and {@link #startY} have to be set first.
	 * @param direction
	 * @param fields
	 */
	private void calculateEndPos(Direction direction, int fields) {
		fields = direction.getOneDimTranslation() * fields;
		if(direction.getTranslation()[0] == 0) {
			end = ByteMapper.toByte(ByteMapper.getX(start), ByteMapper.getY(start) + fields);
		} else {
			end = ByteMapper.toByte(ByteMapper.getX(start) + fields, ByteMapper.getY(start) );
		}
	}
	
	public boolean equals(Move move2) {
		if(move2 == null) return false;
		if(!piece.equals(move2.piece)) return false;
		if(getStartX() != move2.getStartX()) return false;
		if(getStartY() != move2.getStartY()) return false;
		if(getEndX() != move2.getEndX()) return false;
		if(getEndY() != move2.getEndY()) return false;
		return true;
	}
	
	public Move clone(GameState state) {
		Move clone = null;
		clone = new Move(piece, start, end, direction, fields);
		clone.normalize(state);
		return clone;
	}
	
	@Override
	public String toString() {
		return getPiece().toString() + "["+getStartX()+"|"+getStartY()+"]" + " to " + "["+getEndX()+"|"+getEndY()+"]";
	}

	public Piece getPiece() {
		return piece;
	}

	public void setPiece(Piece piece) {
		this.piece = piece;
	}

	public Direction getDirection() {
//		return Direction.get(ByteMapper.getX(start), ByteMapper.getX(end), ByteMapper.getY(start), ByteMapper.getY(end));
		return direction;
	}

	public int getFields() {
		return fields;
//		switch(dir) {
//		case LEFT:
//			return startX - endX;
//		case RIGHT:
//			return endX - startX;
//		case UP:
//			return startY - endY;
//		default:
//			return endY - startY;
//		}
	}

	public int getStartX() {
		return ByteMapper.getX(start);
	}

	public void setStartX(int startX) {
		ByteMapper.setX(start, startX);
	}

	public int getStartY() {
		return ByteMapper.getY(start);
	}

	public void setStartY(int startY) {
		ByteMapper.setY(start, startY);
	}

	public int getEndX() {
		return ByteMapper.getX(end);
	}

	public void setEndX(int endX) {
		ByteMapper.setX(end, endX);
	}

	public int getEndY() {
		return ByteMapper.getY(end);
	}

	public void setEndY(int endY) {
		ByteMapper.setY(end, endY);
	}
}
