package core;

/**
 * Represents a state of the game Stratego quick battle 
 */
public class GameState {
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
	
	@Override
	public GameState clone() {
		return null;
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
