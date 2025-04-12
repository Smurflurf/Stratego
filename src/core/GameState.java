package core;
	
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

/**
 * Represents a state of the game Stratego quick battle 
 */
public class GameState implements Cloneable {
	/**
	 * true is red, the beginning team
	 */
	private boolean team;
	/**
	 * 0: no chase
	 * 1: red chases blue
	 * 2: blue chases red
	 */
	private byte inChase;
	private byte repetitionsRed;
	private byte repetitionsBlue;
	private short repetitionRedFields;
	private short repetitionBlueFields;
	private Move firstRepetitionRedMove;
	private Move firstRepetitionBlueMove;
	private ShortOpenHashSet chasedFields;
	private Piece[] redPieces;
	private Piece[] bluePieces;
	private Piece[][] field;

	public GameState(Piece[] redPieces, Piece[] bluePieces) {
		field = new Piece[8][8];
		setTeam(true);
		setRedPieces(redPieces);
		setBluePieces(bluePieces);
		createField();
		setFirstRepetitionRedMove(null);
		setFirstRepetitionBlueMove(null);
		setRepetitionsRed((byte)0);
		setRepetitionsBlue((byte)0);
		setRepetitionRedFields((short)0);
		setRepetitionBlueFields((short)0);
		setInChase((byte)0);
		setChasedFields(new ShortOpenHashSet());
	}

	public GameState(Piece[] redPieces, Piece[] bluePieces, boolean team, Move firstRepetitionRedMove, Move firstRepetitionBlueMove, byte repetitionsRed, byte repetitionsBlue,
			short repetitionRedFields, short repetitionBlueFields, byte inChase, ShortOpenHashSet chasedFields) {
		field = new Piece[8][8];
		setTeam(team);
		setRedPieces(redPieces);
		setBluePieces(bluePieces);
		createField();
		setFirstRepetitionRedMove(firstRepetitionRedMove);
		setFirstRepetitionBlueMove(firstRepetitionBlueMove);
		setRepetitionsRed(repetitionsRed);
		setRepetitionsBlue(repetitionsBlue);
		setRepetitionRedFields(repetitionRedFields);
		setRepetitionBlueFields(repetitionBlueFields);
		setInChase(inChase);
		setChasedFields(chasedFields);
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
	 * Only moves a Piece to its new place in move.
	 * Alters {@link #field} and the Piece itself to represent the new location.
	 * TODO call {@link #updateChase(Move)} and {@link #updateLastMove(Move)}
	 * @param move contains the Piece and new position
	 */
	public void move(Move move) {
		//		updateLastMove(move);	
		//		updateChase(move);

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

		if(getFirstRepetitionRedMove() != null)
			if(!getFirstRepetitionRedMove().equals(state2.getFirstRepetitionRedMove())) return false;
		if(getFirstRepetitionBlueMove() != null)
			if(!getFirstRepetitionBlueMove().equals(state2.getFirstRepetitionBlueMove())) return false;
		if(getRepetitionsRed() != state2.getRepetitionsRed()) return false;
		if(getRepetitionsBlue() != state2.getRepetitionsBlue()) return false;
		if(isInChase() != state2.isInChase()) return false;
		if(getChasedFields().size() != state2.getChasedFields().size()) return false;

		return true;
	}

	/**
	 * First checks if a chase is happening
	 * @param move
	 */
	public void updateChase(Move move) {
		Move lastMove = getLastMove();
		checkIfChase(lastMove, move);
		if(isInChase()) {
			getChasedFields().add(ByteMapper.hash(lastMove.getEnd(), move.getEnd()));
		}
	}

	/**
	 * Updates {@link #inChase}
	 * @param lastMove
	 * @param move
	 */
	private void checkIfChase(Move lastMove, Move move) {
		if(lastMove == null)
			return;

		if(!isInChase()) {
			if(lastMove.getStart() == move.getEnd()) {
				setInChase(move.getPiece().getTeam() ? (byte)1 : (byte)2);
			}
		} else {
			if(getChaser() != null 
					&& getChaser().getTeam() != move.getPiece().getTeam()) {	// chased piece does something
				if(move.getEnd() == getChaser().getPos() // chased piece fights back
						|| !move.getPiece().equals(getBeforeLastMove().getPiece())){ 	// other piece than the chased one gets used
					setInChase((byte)0);
					if(getChasedFields().size() > 0)
						setChasedFields(new ShortOpenHashSet());	//TODO implementation so this HashSet does not get altered by cloning
				}
			} else {	// chaser does something
				if(lastMove.getStart() == move.getEnd()) {
					setInChase(move.getPiece().getTeam() ? (byte)1 : (byte)2);
				} else {
					setInChase((byte)0);
					if(getChasedFields().size() > 0)
						setChasedFields(new ShortOpenHashSet());	//TODO implementation so this HashSet does not get altered by cloning
				}
			}
		}
	}

	public void updateLastMove(Move move) {
		Move first = getRepMove(move.getPiece().getTeam());
		if(first != null 
				&& first.getPiece().equals(move.getPiece())
				&& inMoveBounds(move)) {
			incrementRep(move.getPiece().getTeam());
		} else {
			resetRep(move.getPiece().getTeam());
			setRepMove(move);
		}
	}

	/**
	 * Checks if a new Move is in the boundaries of the old Move, meaning a repetition takes place
	 * @param move
	 * @return
	 */
	public boolean inMoveBounds(Move next) {
		if(next.getPiece().getTeam()) {
			if(ByteMapper.contains(repetitionRedFields, ByteMapper.toByte(next.getEndX(), next.getEndY())))
				return true;
		} else {
			if(ByteMapper.contains(repetitionBlueFields, ByteMapper.toByte(next.getEndX(), next.getEndY())))
				return true;
		}
		return false;
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

		GameState state = 
				new GameState(
						redClone, 
						blueClone, 
						team, 
						firstRepetitionRedMove, 
						firstRepetitionBlueMove, 
						repetitionsRed, 
						repetitionsBlue,
						repetitionRedFields, 
						repetitionBlueFields, 
						inChase, 
						chasedFields
						);

		if(firstRepetitionBlueMove != null)
			state.setFirstRepetitionBlueMove(firstRepetitionBlueMove.normalize(state));
		if(firstRepetitionRedMove != null)
			state.setFirstRepetitionRedMove(firstRepetitionRedMove.normalize(state));

		return state;
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
			for(int i=0;i<10;i++) if(bluePieces[i] != null) blueClone[i] = bluePieces[i].clone((byte)-1);	
			for(int i=0; i<10; i++) if(redPieces[i] != null) redClone[i] = redPieces[i].clone();
		} else {
			for(int i=0;i<10;i++) if(bluePieces[i] != null) blueClone[i] = bluePieces[i].clone();	
			for(int i=0; i<10; i++) if(redPieces[i] != null) redClone[i] = redPieces[i].clone((byte)-1);
		}
		
		GameState state = 
				new GameState(
						redClone, 
						blueClone, 
						this.team, 
						firstRepetitionRedMove, 
						firstRepetitionBlueMove, 
						repetitionsRed, 
						repetitionsBlue,
						repetitionRedFields, 
						repetitionBlueFields, 
						inChase, 
						chasedFields
						);

		if(firstRepetitionBlueMove != null)
			state.setFirstRepetitionBlueMove(firstRepetitionBlueMove.normalize(state));
		if(firstRepetitionRedMove != null)
			state.setFirstRepetitionRedMove(firstRepetitionRedMove.normalize(state));
		
		return state;
	}

	/**
	 * Removes a Piece from {@link #field} and {@link #redPieces} or {@link #bluePieces}, depending on its team
	 * @param piece Piece to Thanos snap
	 */
	public boolean removePiece(Piece piece) {
		for(int i=0; i<10; i++) {
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

	/**
	 * If a chase is happening, it returns the Piece that initiates the chase.
	 * @return red Piece if it chases the blue Piece or vice versa
	 */
	public Piece getChaser() {
		if(inChase == 1) {
			return firstRepetitionRedMove.getPiece();
		} else if (inChase == 2){
			return firstRepetitionBlueMove.getPiece();
		} else {
			return null;
		}
	}

	public boolean isInChase() {
		return inChase != 0;
	}

	/**
	 * TODO DOCUMENTATION ANPASSEN
	 * Returns the last moved Piece
	 * @return last moved Piece
	 */
	public Move getLastMove() {
		return getRepMove(!team);
	}
	/**
	 * TODO DOCUMENTATION ANPASSEN
	 * Returns the last moved Piece
	 * @return last moved Piece
	 */
	public Move getBeforeLastMove() {
		return getRepMove(team);
	}

	public Move getRepMove(boolean team) {
		if(team) {
			return getFirstRepetitionRedMove();
		} else {
			return getFirstRepetitionBlueMove();
		}
	}

	public void incrementRep(boolean team) {
		if(team) {
			repetitionsRed++;
		} else {
			repetitionsBlue++;
		}
	}

	public void resetRep(boolean team) {
		if(team) {
			repetitionsRed = 1;
		} else {
			repetitionsBlue = 1;
		}
	}

	public void setRepMove(Move move) {
		if(move.getPiece().getTeam()) {
			setFirstRepetitionRedMove(move);
//			ByteOpenHashSet moves = new ByteOpenHashSet(move.getRelevantFields());
			setRepetitionRedFields(move.getRelevantFields());
		} else {
			setFirstRepetitionBlueMove(move);
//			ByteOpenHashSet moves = new ByteOpenHashSet(move.getRelevantFields());
			setRepetitionBlueFields(move.getRelevantFields());
		}
	}

	public Piece[] getCurrentPieces() {
		return team ? redPieces : bluePieces;
	}

	public int getRepetitions() {
		if(team) {
			return repetitionsRed;
		} else {
			return repetitionsBlue;
		}
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

	public Move getFirstRepetitionRedMove() {
		return firstRepetitionRedMove;
	}

	public void setFirstRepetitionRedMove(Move firstRepetitionRedMove) {
		this.firstRepetitionRedMove = firstRepetitionRedMove;
	}

	public Move getFirstRepetitionBlueMove() {
		return firstRepetitionBlueMove;
	}

	public void setFirstRepetitionBlueMove(Move firstRepetitionBlueMove) {
		this.firstRepetitionBlueMove = firstRepetitionBlueMove;
	}

	public int getRepetitionsRed() {
		return repetitionsRed;
	}

	public void setRepetitionsRed(byte repetitionsRed) {
		this.repetitionsRed = repetitionsRed;
	}

	public int getRepetitionsBlue() {
		return repetitionsBlue;
	}

	public void setRepetitionsBlue(byte repetitionsBlue) {
		this.repetitionsBlue = repetitionsBlue;
	}

	public short getRepetitionRedFields() {
		return repetitionRedFields;
	}

	public void setRepetitionRedFields(short repetitionRedFields) {
		this.repetitionRedFields = repetitionRedFields;
	}

	public short getRepetitionBlueFields() {
		return repetitionBlueFields;
	}

	public void setRepetitionBlueFields(short repetitionBlueFields) {
		this.repetitionBlueFields = repetitionBlueFields;
	}

	public void setInChase(byte inChase) {
		this.inChase = inChase;
	}

	public byte getInChase() {
		return inChase;
	}

	public ShortOpenHashSet getChasedFields() {
		return chasedFields;
	}

	public void setChasedFields(ShortOpenHashSet chasedFields) {
		this.chasedFields = chasedFields;
	}
}
