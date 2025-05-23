package core;

import java.util.ArrayList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Useful utilities for the whole project.
 * The methods do not alter their parameters.
 * All methods are static, so extending from Utils is not necessary.
 */
public class Utils {
	public static boolean checkAndExecute(GameState state, Move move) {
		if(!isMovePossible(state, move.getPiece(), move.getEndX(), move.getEndY(), move.getDirection(), move.getFields())) 
			return false;
		return execute(state, move);
	}
	
	/**
	 * Checks if a Move is an attacking Move
	 * @param state
	 * @param move
	 * @return
	 */
	public static boolean isAttack(GameState state, Move move) {
		return state.inspect(move.getEndX(), move.getEndY()) instanceof Piece;
	}

	/**
	 * Executes move on state without checking if the Move is possible
	 * @param state GameState
	 * @param move Move
	 * @return true after execution
	 */
	public static boolean execute(GameState state, Move move) {
		state.updateChase(move);
		state.updateLastMove(move);	
		if(!makeMove(state, move))
			return false;
		state.changeTeam();
		return true;
	}

	/**
	 * Delay to use without adding too much code.
	 * Stops the code when an Exception is thrown.
	 * @param ms sleep time
	 */
	public static void sleep(int ms) {
		try{
			Thread.sleep(ms);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Executes move on state. 
	 * Does not check if move is possible or if the Piece exists on the board.
	 * @param state GameState to execute move on
	 * @param move move being executed on state
	 */
	private static boolean makeMove(GameState state, Move move) {
		if(!(state.inspect(move.getEndX(), move.getEndY()) instanceof Piece defender)) {
			state.move(move);
			if(move.getFields() > 1) {
				revealPiece(move.getPiece(), state);
			}
		} else {
			Piece loser = move.getPiece().attack(defender);
			if(loser == null) {	
				revealPiece(move.getPiece(), state);
				if(!state.removePiece(move.getPiece())) return false;
				revealPiece(defender, state);
				if(!state.removePiece(defender)) return false;
			} else {
				revealPiece(loser, state);
				if(!state.removePiece(loser)) return false;
				if(loser != move.getPiece()) {
					revealPiece(move.getPiece(), state);
					state.move(move);
				} else {
					revealPiece(defender, state);
				}
			}
		}
		return true;
	}
	
	private static void revealPiece(Piece piece, GameState state) {
		if(!piece.getKnown()) {
			piece.setKnown(true);
			state.incrementKnown(piece.getTeam());
		}
	}

	/**
	 * 
	 * @param state
	 * @param piece
	 * @param x end x
	 * @param y end y
	 * @param dir
	 * @param fields
	 * @return
	 */
	public static boolean isMovePossible(GameState state, Piece piece, int x, int y, Direction dir, int fields) {
		if(state.getTeam() != piece.getTeam()) return false;	// is Pieces turn?
		if(isOutOfBoundsOrBlocked(state.getField(), x, y, piece.getTeam())) return false;
		
		if(!canReach(piece,x, y)) return false;	// is Piece reachable?
		if(!sightLine(state.getField(), piece, fields, dir)) return false;

		if(twoSquaresRule(state, piece, x, y)) return false;
		if(moreSquaresRule(state, ByteMapper.toByte(x, y))) return false;

		return true;
	}

	/**
	 * Fills dirMap with Direction-reach pairs to represent how many fields a Piece can walk into a given direction.
	 * @param state
	 * @param picked
	 * @param dirMap
	 */
	public static void fillDirectionMap(GameState state, Piece picked, ObjectArrayList<int[]> dirMap){
		dirMap.clear();
		for(int direction=0; direction<4; direction++) {
			int reach = reach(state.getField(), picked, direction, null);
			if(reach > 0) dirMap.add(new int[] {direction, reach});
		}
	}

	/**
	 * Checks if a Piece and its target position do not have any Pieces or Lakes in between.
	 * @param field	the field from a GameState
	 * @param piece Piece that moves
	 * @param fields how many steps into a Direction
	 * @param direction Direction the piece wants to move
	 * @return true if nothing blocks the pieces line of sight within fields steps into direction
	 */
	public static boolean sightLine(Piece[][] field, Piece piece, int fields, Direction direction) {
		if(fields < 2) return true;

		switch(direction) {
		case UP:			
			for(int y=-1; fields > 1; y--, --fields) {
				int newY = piece.getY() + y;
				if(outOfBounds(newY) || blocked(field, piece.getX(), newY)) {
					return false;
				}
			}
			break;
		case DOWN:
			for(int y=1; fields > 1; y++, --fields) {
				int newY = piece.getY() + y;
				if(outOfBounds(newY) || blocked(field, piece.getX(), newY))
					return false;
			}
			break;
		case LEFT:			
			for(int x=-1; fields > 1; x--, --fields) {
				int newX = piece.getX() + x;
				if(outOfBounds(newX) || blocked(field, newX, piece.getY()))
					return false;
			}
			break;
		case RIGHT:
			for(int x=1; fields > 1; x++, --fields) {
				int newX = piece.getX() + x;
				if(outOfBounds(newX) || blocked(field, newX, piece.getY()))
					return false;
			}
			break;
		}
		return true;
	}

	/**
	 * Returns a Pieces reach into a given direction.
	 * The reach is impacted by the field border, same team Pieces and the lakes.
	 * 
	 * @param field	the field from a GameState
	 * @param piece Piece that moves
	 * @param direction Direction int the piece wants to move
	 * @param enemyPieces a list containing enemy Pieces that can be reached by piece. If a new one is found, it gets added to the list.
	 * @return the fields a Piece can walk into the given direction
	 */
	public static int reach(Piece[][] field, Piece piece, int direction, ArrayList<int[]> enemyPieces) {
		int maxReach = piece.getType().getMoves();
		switch(direction) {
		case 0: 
			for(int y=piece.getY() - 1; y>=piece.getY() - maxReach; y--) {
				if(outOfBounds(y))
					return piece.getY() - y -1;
				switch(blockedTeamSensitive(field, piece.getX(), y, piece.getTeam())){
				case -1: 
					return piece.getY() - y -1;
				case 1: return piece.getY() - y -1;
				case 2: 
					int reach = piece.getY() - y;
					if(enemyPieces != null)
						enemyPieces.add(new int[] {direction, reach}); 
					return reach;
				}
			}
			break;
		case 1:
			for(int y=piece.getY() + 1; y<=piece.getY() + maxReach; y++) {
				if(outOfBounds(y))
					return y - piece.getY() -1;
				switch(blockedTeamSensitive(field, piece.getX(), y, piece.getTeam())){
				case -1: return y - piece.getY() -1;
				case 1: return y - piece.getY() -1;
				case 2:
					int reach = y - piece.getY();
					if(enemyPieces != null)
						enemyPieces.add(new int[] {direction, reach}); 
					return reach;
				}
			}
			break;
		case 2:
			for(int x=piece.getX() - 1; x>=piece.getX() - maxReach; x--) {
				if(outOfBounds(x))
					return piece.getX() - x -1;
				switch(blockedTeamSensitive(field, x, piece.getY(), piece.getTeam())){
				case -1: return piece.getX() - x -1;
				case 1: return piece.getX() - x -1;
				case 2: 
					int reach = piece.getX() - x;
					if(enemyPieces != null)
						enemyPieces.add(new int[] {direction, reach}); 
					return reach;
				}
			}
			break;
		case 3:
			for(int x=piece.getX() + 1; x<=piece.getX() + maxReach; x++) {
				if(outOfBounds(x))
					return x - piece.getX() -1;
				switch(blockedTeamSensitive(field, x, piece.getY(), piece.getTeam())){
				case -1: return x - piece.getX() -1;
				case 1: return x - piece.getX() -1;
				case 2: 
					int reach = x - piece.getX();
					if(enemyPieces != null)
						enemyPieces.add(new int[] {direction, reach}); 
					return reach;
				}
			}
			break;
		}
		return maxReach;
	}

	/**
	 * Does the {@link #outOfBounds(int)} and {@link #blockedTeamSensitive(Piece[][], int, int, boolean)} check
	 * @param field GameState field
	 * @param x end x
	 * @param y end y
	 * @param team moving Piece team
	 * @return true if the move is not valid
	 */
	public static boolean isOutOfBoundsOrBlocked(Piece[][] field, int x, int y, boolean team) {
		if(outOfBounds(x) || outOfBounds(y)) return true;	// is Move out of bounds?
		int blockState = blockedTeamSensitive(field, x, y, team);
		if(blockState == -1 || blockState == 1) return true;	// is field blocked by lake or same team Piece?
		return false;
	}
	
	/**
	 * Checks for different things blocking a field, returns what exactly is blocking
	 * @param field
	 * @param x
	 * @param y
	 * @param team
	 * @return -1 if blocked by lake, 0 if not blocked, 1 if blocked by same team, 2 if blocked by different team
	 */
	public static int blockedTeamSensitive(Piece[][] field, int x, int y, boolean team) {
		if(blockedByLake(x, y)) return -1;
		if(field[x][y] == null) return 0;
		else
			return (field[x][y].getTeam() == team ? 1 : 2);
	}

	/**
	 * Checks if a Piece or lake blocks a field
	 * @param state
	 * @param x
	 * @param y
	 * @return
	 */
	public static boolean blocked(Piece[][] field, int x, int y) {
		return blockedByLake(x,y) ? 
				true : field[x][y] != null;
	}

	/**
	 * Checks if a lake blocks a field
	 * @param state
	 * @param x
	 * @param y
	 * @return
	 */
	public static boolean blockedByLake(int x, int y) {
		if(x == 2 || x == 5)
			if(y == 3 || y == 4)
				return true;
		return false;
	}

	/**
	 * Checks if a Piece can reach a target destination.
	 * Does not check if the way is unobstructed, use with {@link #sightLine(Piece, byte[])}
	 * @param piece Piece that wants to move
	 * @param target the Pieces target destination
	 * @return true if the Piece can reach the position
	 */
	public static boolean canReach(Piece piece, int x, int y) {
		if(piece.getX() != x) {
			if(piece.getY() != y) return false;
			if(piece.getType().getMoves() >= Math.abs(piece.getX() - x)) return true;
		} else {
			if(piece.getType().getMoves() >= Math.abs(piece.getY() - y)) return true;
		}
		if(piece.getX() != x && piece.getY() != y) return false;

		return false;
	}
	
	/**
	 * Checks if a given integer is out of bounds
	 * @param i integer to check
	 * @return true if the integer is out of bounds, i.e. smaller than 0 or bigger than 7
	 */
	public static boolean outOfBounds(int i) {
		return i<0 || i>7;
	}

	/**
	 * Tests if the two square rule applies, i.e. if there are more than 3 repetitions
	 * @param state
	 * @param piece
	 * @param x end x
	 * @param y end y
	 * @return true if the rule applies, move is invalid
	 */
	public static boolean twoSquaresRule(GameState state, Piece piece, int x, int y) {
		return state.getCurrentRepetitions() > 2
				&& state.inMoveBounds(piece, x, y) 
				&& state.getFirstRepetitionMove(piece.getTeam()).getPiece().equals(piece);
	}

	/**
	 * Tests if the more square rule applies, i.e. if a chase  is happening and the chaser and chased are on positions that already happened once.
	 * @param state
	 * @param endPos endPosition
	 * @return true if the rule applies, move is invalid
	 */
	public static boolean moreSquaresRule(GameState state, byte endPos) {
		if(state.isInChase()) {
			return state.getChasedFields().contains(ByteMapper.hash(state.getLastMove().getEnd(), endPos));
		} else {
			return false;
		}
	}

	/**
	 * Generates a list of all possible Moves (based on gameState current team).
	 * Does not check if the game is still going i.e. no checks for the existence of flags are done here.
	 * @param state the gameState to analyze. Its team attribute is considered.
	 * @return all possible moves in gameState
	 */
	public static ObjectArrayList<Move> getAllPossibleMoves(GameState state) {
		ObjectArrayList<Move> moves = new ObjectArrayList<Move>();
		for(int i=0; i<7; i++) {
			if(state.getCurrentPieces()[i] == null) continue;
			for(int reach=0; reach<state.getCurrentPieces()[i].getType().getMoves(); reach++) {
				for(Direction dir : Direction.values()) {
					Piece piece = state.getCurrentPieces()[i];
					if(isMovePossible(state, piece, 
							Move.calcEndX(piece.getX(), dir, reach+1), 
							Move.calcEndY(piece.getY(), dir, reach+1),
							dir, reach+1)) {
						moves.add(new Move(state.getCurrentPieces()[i], dir, reach+1));
					} else {
						continue;
					}
				}
			}
		}
		return moves;
	}

	/**
	 * Generates a list of all possible Moves for piece.
	 * piece's team must be state's current team.
	 * Does not check if the game is still going i.e. no checks for the existence of flags are done here.
	 * @param state the gameState to analyze. Its team attribute is considered.
	 * @return all possible moves for piece in gameState
	 */
	public static Move[] getPiecePossibleMoves(GameState state, Piece piece) {
		ArrayList<Move> moves = new ArrayList<Move>();
		for(int i=0; i<8; i++) {
			if(state.getCurrentPieces()[i] == null
					|| !state.getCurrentPieces()[i].equals(piece)) continue;
			for(Direction dir : Direction.values()) {
				for(int reach=0; reach<state.getCurrentPieces()[i].getType().getMoves(); reach++) {
					if(isMovePossible(state, piece, 
							Move.calcEndX(piece.getX(), dir, reach+1), 
							Move.calcEndY(piece.getY(), dir, reach+1),
							dir, reach+1)) {
						moves.add(new Move(state.getCurrentPieces()[i], dir, reach+1));
					} else {
						continue;
					}
				}
			}
		}
		return moves.toArray(Move[]::new);
	}

	/**
	 * Checks if any Move is possible in gameState.
	 * Use this Method for anything related to possible Move checks, rather than {@link #getAllPossibleMoves(GameState)}.
	 * @return false if no Move is possible in gameState
	 */
	public static boolean anyMovePossible(GameState state) {
		for(int i=0; i<7; i++) {
			if(state.getCurrentPieces()[i] == null) continue;
			for(Direction dir : Direction.values()) {
				Piece piece = state.getCurrentPieces()[i];
				if(isMovePossible(state, piece, 
						Move.calcEndX(piece.getX(), dir, 1), 
						Move.calcEndY(piece.getY(), dir, 1),
						dir, 1)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Relies on the Fag being [9] in the Pieces array. 
	 * Also relies on Bombs being [8] and [7] in the array
	 * @param gameState
	 * @return
	 */
	public static boolean isGameOver(GameState gameState) {
		Piece[] pieces = gameState.getCurrentPieces();
		return flagGoneCheck(pieces) || piecesGoneCheck(pieces) || !anyMovePossible(gameState);
	}

	/**
	 * Returns the winner, in case there is one.
	 * More sophisticated check than {@link #isGameOver(GameState)}, supports draw.
	 * @param gameState to check
	 * @return 0: red wins, 1: blue wins, 2: draw, 3: game is not over
	 */
	public static int getWinner(GameState gameState) {
		if(flagGoneCheck(gameState.getRedPieces()))			//flag gone check red and blue
			return 1;
		else if(flagGoneCheck(gameState.getBluePieces()))
			return 0;

		if(piecesGoneCheck(gameState.getRedPieces()))		//pieces gone check red + draw check
			if(piecesGoneCheck(gameState.getBluePieces()))
				return 2;
			else
				return 1;

		if(piecesGoneCheck(gameState.getBluePieces()))		//pieces gone check blue + draw check
			if(piecesGoneCheck(gameState.getRedPieces()))
				return 2;
			else
				return 0;

		if(!anyMovePossible(gameState)) {
			gameState.changeTeam();
			if(!anyMovePossible(gameState)) {
				gameState.changeTeam();
				return 2;
			}
			gameState.changeTeam();
			return gameState.getTeam() ? 1 : 0;
		}

		return 3;
	}



	/**
	 * Checks if all Pieces are gone from one teams pieces
	 * @implSpec only checks pieces[9], the Flag should be located there
	 * @param pieces
	 * @return 
	 */
	public static boolean flagGoneCheck(Piece[] pieces) {
		return pieces[9] == null;
	}
	/**
	 * Checks if all Pieces are gone from one teams pieces
	 * @implSpec does not check pieces[7,8,9], the Flag and two Bombs should be located there
	 * @param pieces
	 * @return
	 */
	public static boolean piecesGoneCheck(Piece[] pieces) {
		for(int i=0; i<7; i++) {
			if(pieces[i] != null) {
				return false;
			}
		}
		return true;
	}

	public static void printField(Piece[][] field) {
		for(int y=-1; y<8; y++) {
			System.out.print(y == -1 ? "y" : (y + " "));
			for(int x=0; x<8; x++) {
				System.out.print(y == -1 ? (x == 0 ? "\\x 0" : "  " + x + " "): (
						field[x][y] == null ? 
								(blockedByLake(x, y) ? "  x " : "  . " ) : 
									field[x][y] + " "));
			}
			System.out.println();
		}
	}

	public static String fieldToString(Piece[][] field) {
		StringBuilder sb = new StringBuilder();
		for(int y=-1; y<8; y++) {
			sb.append(y == -1 ? "y" : (y + " "));
			for(int x=0; x<8; x++) {
				sb.append(y == -1 ? (x == 0 ? "\\x 0" : "  " + x + " "): (
						field[x][y] == null ? 
								(blockedByLake(x, y) ? "  x " : "  . " ) : 
									field[x][y] + " "));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
