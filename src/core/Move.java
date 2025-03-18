package core;

public class Move {
	int[] pos;
	Piece piece;
	
	public Move(Piece piece, int x, int y) {
		this.piece = piece;
		pos[0] = x;
		pos[1] = y;
	}
	
	public Move(Piece piece, int[] pos) {
		this.piece = piece;
		this.pos = pos;
	}
	
	@Override
	public String toString() {
		return piece.toString() + " to " + "["+pos[0]+"|"+pos[1]+"]";
	}
}
