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
	
	public GameState(Piece[][] field, Piece[] redPieces, Piece[] bluePieces) {
		setTeam(true);
		setField(field);
		setRedPieces(redPieces);
		setBluePieces(bluePieces);
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

	public void setField(Piece[][] field) {
		this.field = field;
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
