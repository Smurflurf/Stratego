package core;

public class Move {
	private Piece piece;
	private Move firstMove;
	private int startX;
	private int startY;
	private int endX;
	private int endY;
	
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
		setStartX(piece.getX());
		setStartY(piece.getY());
		calculateEndPos(direction, fields);
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
	public Move(Move firstMove, Direction direction, int fields) {
		setFirstMove(firstMove);
		setStartX(firstMove.getEndX());
		setStartY(firstMove.getEndY());
		calculateEndPos(direction, fields);
	}
	
	/**
	 * Normalizes the Move, i.e. if this Moves Piece is not from state, the reference from the according state Piece gets used.
	 * @param state
	 */
	public void normalize(GameState state) {
		//TODO check this, should work but not sure
		for(Piece piece : this.piece.getTeam() ? state.getRedPieces() : state.getBluePieces()) {
			if(piece != null && 
					piece.getX() == (firstMove != null ? firstMove.getStartX() : getStartX()) && 
					piece.getY() == (firstMove != null ? firstMove.getStartY() : getStartY())) {
				if(!piece.equals(this.piece))
//					System.err.println("HS " + piece + piece.coords()+" <-> "+ this.piece + this.piece.coords());
				if(firstMove != null)
					firstMove.setPiece(piece);
				setPiece(piece);
//				System.out.println("" + piece + piece.coords()+" <-> "+ this.piece + this.piece.coords());
				break;
			}
				/*if(piece != null && piece.equals(firstMove != null ? firstMove.getPiece() : this.piece)) {
				if(firstMove != null)
					firstMove.setPiece(piece);
				setPiece(piece);
				break;
			}*/
		}
	}
	
	/**
	 * Calculates {@link #endX} and {@link #endY}, {@link #startX} and {@link #startY} have to be set first.
	 * @param direction
	 * @param fields
	 */
	private void calculateEndPos(Direction direction, int fields) {
		fields = direction.getOneDimTranslation() * fields;
		if(direction.getTranslation()[0] == 0) {
			setEndX(startX);
			setEndY(startY + fields);
		} else {
			setEndX(startX + fields);
			setEndY(startY);
		}
	}
	
	@Override
	public String toString() {
		if(firstMove == null)
			return getPiece().toString() + "["+getStartX()+"|"+getStartY()+"]" + " to " + "["+getEndX()+"|"+getEndY()+"]";
		else
			return "<" + firstMove.toString() + "> then " + getPiece().toString() + " to " + "["+getEndX()+"|"+getEndY()+"]";
	}

	public Piece getPiece() {
		return piece;
	}

	public void setPiece(Piece piece) {
		this.piece = piece;
	}

	public Direction getDirection() {
		return Direction.get(startX, endX, startY, endY);
	}

	public int getFields(Direction dir) {
		switch(dir) {
		case LEFT:
			return startX - endX;
		case RIGHT:
			return endX - startX;
		case UP:
			return startY - endY;
		default:
			return endY - startY;
		}
	}

	public Move getFirstMove() {
		return firstMove;
	}

	public void setFirstMove(Move firstMove) {
		this.firstMove = firstMove;
		if(firstMove != null)
			piece = firstMove.piece;
	}

	public int getStartX() {
		return startX;
	}

	public void setStartX(int startX) {
		this.startX = startX;
	}

	public int getStartY() {
		return startY;
	}

	public void setStartY(int startY) {
		this.startY = startY;
	}

	public int getEndX() {
		return endX;
	}

	public void setEndX(int endX) {
		this.endX = endX;
	}

	public int getEndY() {
		return endY;
	}

	public void setEndY(int endY) {
		this.endY = endY;
	}
}
