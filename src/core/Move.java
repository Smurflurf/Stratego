package core;

public class Move {
	private Piece piece;
	private Direction direction;
	private int fields;
	private Move firstMove;
	
	/**
	 * Use with everything but SPAEHER for a "normal" move
	 * @param piece
	 * @param direction
	 * @param fields
	 */
	public Move(Piece piece, Direction direction, int fields) {
		setPiece(piece);
		setDirection(direction);
		setFields(fields);
		setFirstMove(null);
	}
	
	/**
	 * Use with a SPAEHER, as he can move and fight at the same time.
	 * This Move always represents a fight.
	 * @param firstMove the first move, indicating where the Piece moves
	 * @param piece
	 * @param direction
	 * @param fields
	 */
	public Move(Move firstMove, Piece piece, Direction direction, int fields) {
		setPiece(piece);
		setDirection(direction);
		setFields(fields);
		setFirstMove(firstMove);
	}
	
	@Override
	public String toString() {
		return getPiece().toString() + " to " + "["+getPos()[0]+"|"+getPos()[1]+"]";
	}

	public int[] getPos() {
		return direction.translate(new int[] {piece.getX(),  piece.getY()} , fields);
	}

	public Piece getPiece() {
		return piece;
	}

	public void setPiece(Piece piece) {
		this.piece = piece;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public int getFields() {
		return fields;
	}

	public void setFields(int fields) {
		this.fields = fields;
	}

	public Move getFirstMove() {
		return firstMove;
	}

	public void setFirstMove(Move firstMove) {
		this.firstMove = firstMove;
	}
}
