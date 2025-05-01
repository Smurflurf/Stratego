package core;
	
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

/**
 * Represents a state of the game Stratego quick battle 
 */
public class GameState implements Cloneable {
	private boolean team;
	/**
	 * Shows if a Piece is currently chasing another Piece
	 * 0 = no chase, 
	 * 1 = red chases blue, 
	 * 2 = blue chases red.
	 */
	private byte inChase;
	/**
	 * represents the red repetitions and blue repetitions in one byte.
	 * repetitions will never exceed 3 so they can be mapped onto two bits.
	 * the bitmap is the following, where r are repetition reds bits and b are repetition blues bits.
	 * 0000rrbb
	 */
	private byte repetitions;
	private short repetitionRedFields;
	private short repetitionBlueFields;
	private Move firstRepetitionRedMove;
	private Move firstRepetitionBlueMove;
	private ShortOpenHashSet chasedFields;
	private Piece[][] pieces;
	private Piece[][] field;

	public GameState(Piece[] redPieces, Piece[] bluePieces) {
		field = new Piece[8][8];
		setTeam(true);
		pieces = new Piece[][] {redPieces, bluePieces};
		createField();
		setFirstRepetitionRedMove(null);
		setFirstRepetitionBlueMove(null);
		setRepetitionRedFields((short)0);
		setRepetitionBlueFields((short)0);
		setChasedFields(new ShortOpenHashSet());
	}

	public GameState(Piece[] redPieces, Piece[] bluePieces, boolean team, byte inChase, byte repetitions, Move firstRepetitionRedMove, Move firstRepetitionBlueMove,
			short repetitionRedFields, short repetitionBlueFields, ShortOpenHashSet chasedFields) {
		field = new Piece[8][8];
		setTeam(team);
		setInChase(inChase);
		setRepetitions(repetitions);
		setPieces(redPieces, bluePieces);
		createField();
		setFirstRepetitionRedMove(firstRepetitionRedMove);
		setFirstRepetitionBlueMove(firstRepetitionBlueMove);
		setRepetitionRedFields(repetitionRedFields);
		setRepetitionBlueFields(repetitionBlueFields);
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
		if(state2.getTeam() != getTeam()) return false;
		if(state2.getInChase() != getInChase()) return false;
		if(state2.getRepetitions() != getRepetitions()) return false;
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
			if(getRedPieces()[i] == null && state2.getRedPieces()[i] == null)
				continue;
			else if(getRedPieces()[i] == null && state2.getRedPieces()[i] != null)
				return false;
			else if(getRedPieces()[i] == null && state2.getRedPieces()[i] == null)
				continue;
			if(!getRedPieces()[i].equals(state2.getRedPieces()[i]))
				return false;
			
			if(getBluePieces()[i] == null && state2.getBluePieces()[i] == null)
				continue;
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
						chasedFields.clear();
				}
			} else {	// chaser does something
				if(lastMove.getStart() == move.getEnd()) {
					setInChase(move.getPiece().getTeam() ? (byte)1 : (byte)2);
				} else {
					setInChase((byte)0);
					if(getChasedFields().size() > 0)
						chasedFields.clear();
				}
			}
		}
	}

	public void updateLastMove(Move move) {
		Move first = getRepMove(move.getPiece().getTeam());
		if(first != null 
				&& first.getPiece().equals(move.getPiece())
				&& inMoveBounds(move)) {
			incrementCurrentRepetitions(move.getPiece().getTeam());
		} else {
			resetCurrentRepetitions(move.getPiece().getTeam());
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
			if(getBluePieces()[i] != null)
				blueClone[i] = getBluePieces()[i].clone();	
		Piece[] redClone = new Piece[10];
		for(int i=0; i<10; i++) 
			if(getRedPieces()[i] != null)
				redClone[i] = getRedPieces()[i].clone();

		GameState state = 
				new GameState(
						redClone, 
						blueClone, 
						team,
						inChase,
						repetitions,
						firstRepetitionRedMove, 
						firstRepetitionBlueMove, 
						repetitionRedFields, 
						repetitionBlueFields, 
						new ShortOpenHashSet(chasedFields)
						);

		if(firstRepetitionBlueMove != null)
			state.setFirstRepetitionBlueMove(firstRepetitionBlueMove.clone(state));
		if(firstRepetitionRedMove != null)
			state.setFirstRepetitionRedMove(firstRepetitionRedMove.clone(state));

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
			for(int i=0;i<10;i++) if(getBluePieces()[i] != null) blueClone[i] = getBluePieces()[i].clone((byte)-1);	
			for(int i=0; i<10; i++) if(getRedPieces()[i] != null) redClone[i] = getRedPieces()[i].clone();
		} else {
			for(int i=0;i<10;i++) if(getBluePieces()[i] != null) blueClone[i] = getBluePieces()[i].clone();	
			for(int i=0; i<10; i++) if(getRedPieces()[i] != null) redClone[i] = getRedPieces()[i].clone((byte)-1);
		}
		
		GameState state = 
				new GameState(
						redClone, 
						blueClone, 
						this.team, 
						inChase,
						repetitions,
						firstRepetitionRedMove, 
						firstRepetitionBlueMove, 
						repetitionRedFields, 
						repetitionBlueFields, 
						new ShortOpenHashSet(chasedFields)
						);

		if(firstRepetitionBlueMove != null)
			state.setFirstRepetitionBlueMove(firstRepetitionBlueMove.clone(state));
		if(firstRepetitionRedMove != null)
			state.setFirstRepetitionRedMove(firstRepetitionRedMove.clone(state));
		
		return state;
	}

	/**
	 * Removes a Piece from {@link #field} and {@link #getRedPieces()} or {@link #getBluePieces()}, depending on its team
	 * @param piece Piece to Thanos snap
	 */
	public boolean removePiece(Piece piece) {
		for(int i=0; i<10; i++) {
			if((piece.getTeam() ? getRedPieces() : getBluePieces())[i] == piece) {
				(piece.getTeam() ? getRedPieces() : getBluePieces())[i] = null;
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
	 * Places all pieces from {@link #getRedPieces()} and {@link #getBluePieces()} on the field.
	 */
	private void createField(){
		for(Piece piece : getRedPieces())
			if(piece != null)
				field[piece.getX()][piece.getY()] = piece;
		for(Piece piece : getBluePieces())
			if(piece != null)
				field[piece.getX()][piece.getY()] = piece;
	}
		
	/**
	 * If a chase is happening, it returns the Piece that initiates the chase.
	 * @return red Piece if it chases the blue Piece or vice versa
	 */
	public Piece getChaser() {
		if(getInChase() == 1) {
			return firstRepetitionRedMove.getPiece();
		} else if (getInChase() == 2){
			return firstRepetitionBlueMove.getPiece();
		} else {
			return null;
		}
	}

	public boolean isInChase() {
		return getInChase() != 0;
	}

	public void setInChase(byte inChase) {
		this.inChase = inChase;
	}

	public byte getInChase() {
		return inChase;
	}
	
	public void incrementCurrentRepetitions(boolean team) {
		if(team) {
			setRepetitionsRed(getRepetitionsRed() + 1);
		} else {
			setRepetitionsBlue(getRepetitionsBlue() + 1);
		}
	}

	public void resetCurrentRepetitions(boolean team) {
		if(team) {
			setRepetitionsRed((byte) 1);
		} else {
			setRepetitionsBlue((byte) 1);
		}
	}

	public int getCurrentRepetitions() {
		if(getTeam()) {
			return getRepetitionsRed();
		} else {
			return getRepetitionsBlue();
		}
	}
	
	public byte getRepetitions() {
		return repetitions;
	}
	
	public void setRepetitions(byte repetitions) {
		this.repetitions = repetitions;
	}
	
	public void setRepetitionsRed(int repetitionsRed) {
		setRepetitions((byte) ((repetitions & 0b11110011) | (repetitionsRed << 2)));
	}

	public int getRepetitionsRed() {
		return (repetitions & 0b00001100) >> 2;
	}

	public void setRepetitionsBlue(int repetitionsBlue) {
		setRepetitions((byte) ((repetitions & 0b11111100) | repetitionsBlue));
	}
	public int getRepetitionsBlue() {
		return repetitions & 0b00000011;
	}

	/**
	 * Returns the last move made
	 * @return last Move
	 */
	public Move getLastMove() {
		return getRepMove(!getTeam());
	}
	
	/**
	 * Returns the last Move made by {@link #team},
	 * if called before updating the repetition Move.
	 * Returns the last moved Piece
	 * @return last moved Piece
	 */
	public Move getBeforeLastMove() {
		return getRepMove(getTeam());
	}

	public Move getRepMove(boolean team) {
		if(team) {
			return getFirstRepetitionRedMove();
		} else {
			return getFirstRepetitionBlueMove();
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
		return getTeam() ? getRedPieces() : getBluePieces();
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

	public Piece[][] getPieces() {
		return pieces;
	}
	
	public void setPieces(Piece[] redPieces, Piece[] bluePieces) {
		pieces = new Piece[][] {redPieces, bluePieces};
	}
	
	public Piece[] getRedPieces() {
		return pieces[0];
	}

	public void setRedPieces(Piece[] redPieces) {
		pieces[0] = redPieces;
	}

	public Piece[] getBluePieces() {
		return pieces[1];
	}

	public void setBluePieces(Piece[] bluePieces) {
		pieces[1] = bluePieces;
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

	public ShortOpenHashSet getChasedFields() {
		return chasedFields;
	}

	public void setChasedFields(ShortOpenHashSet chasedFields) {
		this.chasedFields = chasedFields;
	}
}
