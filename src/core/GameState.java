package core;

/**
 * Represents a state of the game Stratego quick battle 
 */
public class GameState implements Cloneable {
	/**
	 * true is red, the beginning team
	 */
	private boolean team;
	private Piece[][] field;
	private Piece[] redPieces;
	private Piece[] bluePieces;

	public GameState(Piece[] redPieces, Piece[] bluePieces) {
		field = new Piece[8][8];
		setTeam(true);
		setRedPieces(redPieces);
		setBluePieces(bluePieces);
		createField();
	}

	/**
	 * TODO
	 * @param move
	 */
	public void update(Move move) {

	}

	/**
	 * Returns the Piece on pos in {@link #field}, null if no Piece is present
	 * @param pos the x,y position to return
	 * @return Piece standing on pos or null
	 */
	public Piece inspect(int[] pos) {
		return field[pos[0]][pos[1]];
	}
	
	/**
	 * Moves a Piece to its new place in move.
	 * Alters {@link #field} and the Piece itself to represent the new location.
	 * @param move contains the Piece and new position
	 */
	public void move(Move move) {
		field[move.getPiece().getX()][move.getPiece().getY()] = null;
		field[move.getPos()[0]][move.getPos()[1]] = move.getPiece();
		move.getPiece().setPos(move.getPos());
	}

	@Override
	public GameState clone() {
		Piece[] blueClone = bluePieces.clone();	
		Piece[] redClone = redPieces.clone();
		return new GameState(blueClone, redClone);
	}

	/**
	 * Places all pieces from {@link #redPieces} and {@link #bluePieces} on the field.
	 */
	private void createField(){
		for(Piece piece : redPieces)
			field[piece.getX()][piece.getY()] = piece;
		for(Piece piece : bluePieces)
			field[piece.getX()][piece.getY()] = piece;
	}

	public boolean getTeam() {
		return team;
	}

	public void setTeam(boolean team) {
		this.team = team;
	}

	public void changeTeam() {
		team = !team;
	}

	public Piece[][] getField() {
		return field;
	}

	public Piece[] getRedPieces() {
		return redPieces;
	}

	public void setRedPieces(Piece[] redPieces) {
		this.redPieces = redPieces;
	}

	public Piece[] getBluePieces() {
		return bluePieces;
	}

	public void setBluePieces(Piece[] bluePieces) {
		this.bluePieces = bluePieces;
	}
}
