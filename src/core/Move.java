package core;

public class Move {
	private Piece piece;
	private byte start;
	private byte end;
	private byte fields;
	private Direction direction;
	
	/**
	 * Use with everything but SPAEHER for a "normal" move.
	 * Transforms direction and fields into coordinates (start and end),
	 * so the Move is not bound to the pieces location.
	 * @param piece
	 * @param direction
	 * @param fields
	 */
	public Move(Piece piece, Direction direction, int fields) {
		this.piece = piece;
		start = piece.getPos();
		calculateEndPos(direction, fields);
		this.fields = (byte)fields;
		this.direction = direction;
	}
	
	/**
	 * Constructor for very specific use cases.
	 * For example moving a Piece with GameState.move.
	 * Does NOT set {@link #fields} and {@link #direction}, only start and end positions.
	 * @param piece
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 */
	public Move(Piece piece, int startX, int startY, int endX, int endY) {
		this.piece = piece;
		setStartX(startX);
		setStartY(startY);
		setEndX(endX);
		setEndY(endY);
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
	public Move(Piece piece, byte start, byte end, Direction direction, byte fields) {
		this.piece = piece;;
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
		if(piece != null && piece.getTeam() == this.piece.getTeam())
			this.piece = piece;
		return this;
	}
	
	/**
	 * @return all fields {@link #piece} steps over in this Move
	 */
	public short getRelevantFields() {
		Direction dir = getDirection();
		int fields = getFields() +1;
		short positions = 0;
		for(int i=0; i<fields; i++) {
			positions = ByteMapper.add(positions, dir.translate(start, i));
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
	
	/**
	 * Calculates the end position X from startX, direction and fields
	 * @param startX
	 * @param direction
	 * @param fields
	 * @return
	 */
	public static int calcEndX(int startX, Direction direction, int fields) {
		if(direction.getTranslation()[0] == 0) {
			return startX;
		} else {
			return startX + direction.getOneDimTranslation() * fields;
		}
	}
	
	/**
	 * Calculates the end position Y from startY, direction and fields
	 * @param startY
	 * @param direction
	 * @param fields
	 * @return
	 */
	public static int calcEndY(int startY, Direction direction, int fields) {
		if(direction.getTranslation()[1] == 0) {
			return startY;
		} else {
			return startY + direction.getOneDimTranslation() * fields;
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
		clone = new Move(piece.clone(), start, end, direction, fields);
		clone.normalize(state);
		return clone;
	}
	
	public Move cloneWithoutNormalize() {
		return new Move(piece.clone(), start, end, direction, fields);
	}
	
	@Override
	public String toString() {
		return getPiece().toString() + "["+getStartX()+"|"+getStartY()+"]" + " to " + "["+getEndX()+"|"+getEndY()+"]";
	}

	public Piece getPiece() {
		return piece;
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

	public byte getStart() {
		return start;
	}
	
	public byte getEnd() {
		return end;
	}
	
	public int getStartX() {
		return ByteMapper.getX(start);
	}

	public void setStartX(int startX) {
		start = ByteMapper.setX(start, startX);
	}

	public int getStartY() {
		return ByteMapper.getY(start);
	}

	public void setStartY(int startY) {
		start = ByteMapper.setY(start, startY);
	}

	public int getEndX() {
		return ByteMapper.getX(end);
	}

	public void setEndX(int endX) {
		end = ByteMapper.setX(end, endX);
	}

	public int getEndY() {
		return ByteMapper.getY(end);
	}

	public void setEndY(int endY) {
		end = ByteMapper.setY(end, endY);
	}
}
