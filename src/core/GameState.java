package core;

import java.lang.reflect.Array;

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
	 * Update the GameState with move
	 * @param move the Move to make
	 */
	public void update(Move move) {
		Utils.makeMove(this, move);
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
	 * Returns the Piece on x , y in {@link #field}, null if no Piece is present
	 * @param x position
	 * @param y position
	 * @return Piece standing on pos or null
	 */
	public Piece inspect(int x, int y) {
		return field[x][y];
	}
	
	/**
	 * Moves a Piece to its new place in move.
	 * Alters {@link #field} and the Piece itself to represent the new location.
	 * @param move contains the Piece and new position
	 */
	public void move(Move move) {
		field[move.getStartX()][move.getStartY()] = null;
		field[move.getEndX()][move.getEndY()] = move.getPiece();
		move.getPiece().setPos(move.getEndX(), move.getEndY());
	}

	public boolean equals(GameState state2) {
		if(state2.getTeam() != team) return false;
		for(int x=0; x<8; x++)
			for(int y=0; y<8; y++) {
				if(getField()[x][y] == null) {
					if(state2.getField()[x][y] != null) {
						return false;
					}
					continue;
				}
				if(!getField()[x][y].equals(state2.getField()[x][y])) {
					return false;
				}
			}
		for(int i=0; i<10; i++) {
			if(getRedPieces()[i] == null && state2.getRedPieces()[i] != null)
				return false;
			else if(getRedPieces()[i] == null && state2.getRedPieces()[i] == null)
				continue;
			if(!getRedPieces()[i].equals(state2.getRedPieces()[i]))
				return false;
			if(getBluePieces()[i] == null && state2.getBluePieces()[i] != null)
				return false;
			else if(getBluePieces()[i] == null && state2.getBluePieces()[i] == null)
				continue;
			if(!getBluePieces()[i].equals(state2.getBluePieces()[i]))
				return false;
		}
		
		return true;
	}
	
	@Override
	public GameState clone() {
		Piece[] blueClone = new Piece[10];
		for(int i=0;i<10;i++)
			if(bluePieces[i] != null)
				blueClone[i] = bluePieces[i].clone();	
		Piece[] redClone = new Piece[10];
		for(int i=0; i<10; i++) 
			if(redPieces[i] != null)
				redClone[i] = redPieces[i].clone();
		return new GameState(redClone, blueClone);
	}
	
	/**
	 * Obfuscates this GameState for team.
	 * If team all blue PieceTypes get set to UNKNOWN, if !team all red PieceTypes get replaced.
	 * @param team true to obfuscate all blue Pieces, false to obfuscate all red Pieces
	 * @return an obfuscated copy of this GameState
	 */
	public GameState obfuscateFor(boolean team) {
		Piece[] blueClone = new Piece[10];
		Piece[] redClone = new Piece[10];
		if(team) {
			for(int i=0;i<10;i++) if(bluePieces[i] != null) blueClone[i] = bluePieces[i].clone(PieceType.UNKNOWN);	
			for(int i=0; i<10; i++) if(redPieces[i] != null) redClone[i] = redPieces[i].clone();
		} else {
			for(int i=0;i<10;i++) if(bluePieces[i] != null) blueClone[i] = bluePieces[i].clone();	
			for(int i=0; i<10; i++) if(redPieces[i] != null) redClone[i] = redPieces[i].clone(PieceType.UNKNOWN);
		}
		GameState obfuscated = new GameState(redClone, blueClone); 
		obfuscated.setTeam(this.team);
		return obfuscated;
	}

	/**
	 * Removes a Piece from {@link #field} and {@link #redPieces} or {@link #bluePieces}, depending on its team
	 * @param piece Piece to Thanos snap
	 */
	public boolean removePiece(Piece piece) {
		for(int i=0; i<10; i++) {
//			if((piece.getTeam() ? redPieces : bluePieces)[i] != null)
//				System.out.println((piece.getTeam() ? redPieces : bluePieces)[i] + " " + (piece.getTeam() ? redPieces : bluePieces)[i].getX() + " " + (piece.getTeam() ? redPieces : bluePieces)[i].getY());
			if((piece.getTeam() ? redPieces : bluePieces)[i] == piece) {
				(piece.getTeam() ? redPieces : bluePieces)[i] = null;
				field[piece.getX()][piece.getY()] = null;
				return true;
			}
		}
		System.err.println("\nCould not remove " + piece + " [" +piece.getX() + "|" + piece.getY() + "] from GameState.");
		for(var el : Thread.currentThread().getStackTrace())
			System.err.println(el);
		return false;
	}

	/**
	 * Places all pieces from {@link #redPieces} and {@link #bluePieces} on the field.
	 */
	private void createField(){
		for(Piece piece : redPieces)
			if(piece != null)
				field[piece.getX()][piece.getY()] = piece;
		for(Piece piece : bluePieces)
			if(piece != null)
				field[piece.getX()][piece.getY()] = piece;
	}

	public Piece[] getCurrentPieces() {
		return team ? redPieces : bluePieces;
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
