package core;

public class Move {
	byte x, y;
	Piece piece;
	
	public Move(Piece piece, byte x, byte y) {
		this.piece = piece;
		this.x = x;
		this.y = y;
	}
}
