package core;

public class Move {
	private int[] pos;
	private Piece piece;
	
	public Move(Piece piece, int x, int y) {
		this.setPiece(piece);
		getPos()[0] = x;
		getPos()[1] = y;
	}
	
	public Move(Piece piece, int[] pos) {
		this.setPiece(piece);
		this.setPos(pos);
	}
	
	@Override
	public String toString() {
		return getPiece().toString() + " to " + "["+getPos()[0]+"|"+getPos()[1]+"]";
	}

	public int[] getPos() {
		return pos;
	}

	public void setPos(int[] pos) {
		this.pos = pos;
	}

	public Piece getPiece() {
		return piece;
	}

	public void setPiece(Piece piece) {
		this.piece = piece;
	}
}
