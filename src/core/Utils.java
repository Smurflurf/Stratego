package core;

import java.util.ArrayList;

import core.placing.Placer;

/**
 * Useful utilities for the whole project.
 * The methods do not alter their parameters.
 * All methods are static, so extending from Utils is not necessary.
 */
public class Utils {
	public static boolean checkAndExecute(GameState state, Move move) {
		if(move.getFirstMove() != null) {
			if(!isMovePossible(state, move.getFirstMove())) return false;
			makeMove(state, move.getFirstMove());
			if(!sightLine(state.getField(), move.getPiece(), move.getFields(), move.getDirection())) return false;
			makeMove(state, move);
		} else {
			if(!isMovePossible(state, move)) return false;
			makeMove(state, move);
		}
		return true;
	}

	public static boolean isMovePossible(GameState state, Move move) {
		if(outOfBounds(move.getPos())) return false;
		if(!canReach(move.getPiece(), move.getPos())) return false;
		if(!sightLine(state.getField(), move.getPiece(), move.getFields(), move.getDirection()))
			System.out.println(move);

		return true;
	}

	/**
	 * Fills dirMap with direction-reach pairs to represent how many fields a Piece can walk into a given direction
	 * @param state
	 * @param picked
	 * @param dirMap
	 */
	public static void fillDirectionMap(GameState state, Piece picked, ArrayList<int[]> dirMap){
		dirMap.clear();
		for(int direction=0; direction<4; direction++) {
			int reach = reach(state.getField(), picked, direction);
			if(reach > 0) dirMap.add(new int[] {direction, reach});
		}
	}
	
	/**
	 * Executes move on state. 
	 * Does not check if move is possible or if the Piece exists on the board.
	 * @param state GameState to execute move on
	 * @param move move being executed on state
	 */
	public static void makeMove(GameState state, Move move) {
		if(!(state.inspect(move.getPos()) instanceof Piece piece)) {
			state.move(move);
		} else {
			move.getPiece().fight(piece);
		}
	}

	public static void main(String[] args) {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);

		printField(state.getField());
		System.out.println(state.getRedPieces()[1]);

		long start = System.currentTimeMillis();
		for(int i=0; i<1000000000; i++)
			sightLine(state.getField(), state.getRedPieces()[5], 2, Direction.UP);
		long stop = System.currentTimeMillis();
		System.out.print(stop - start + " ms");
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
	 * @return the fields a Piece can walk into the given direction
	 */
	public static int reach(Piece[][] field, Piece piece, int direction) {
		int maxReach = piece.getType().getMoves();
		switch(direction) {
		case 0: 
			for(int y=piece.getY() - 1; y>=piece.getY() - maxReach; y--) {
				if(outOfBounds(y))
					return piece.getY() - y -1;
				switch(blockedTeamSensitive(field, piece.getX(), y, piece.getTeam())){
				case -1: return piece.getY() - y -1;
				case 1: return piece.getY() - y -1;
				case 2: return piece.getY() - y;
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
				case 2: return y - piece.getY();
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
				case 2: return piece.getX() - x;
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
				case 2: return x - piece.getX();
				}
			}
			break;
		}
		return maxReach;
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
	public static boolean canReach(Piece piece, int[] target) {
		if(piece.getX() != target[0]) {
			if(piece.getY() != target[1]) return false;
			if(piece.getType().getMoves() >= Math.abs(piece.getX() - target[0])) return true;
		} else {
			if(piece.getType().getMoves() >= Math.abs(piece.getY() - target[1])) return true;
		}
		if(piece.getX() != target[0] && piece.getY() != target[1]) return false;

		return false;
	}

	/**
	 * Checks if a given set of coordinates is out of bounds
	 * @param coordinates coordinates to check
	 * @return true if the coordinates are out of bounds, i.e. smaller than 0 or bigger than 7
	 */
	public static boolean outOfBounds(int[] coordinates) {
		if(coordinates[0] < 0 || coordinates[0] > 7 ||
				coordinates[1] < 0 || coordinates[1] > 7)
			return true;
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
	 * Generates a list of all possible Moves (based on gameState current team).
	 * Does not check if the game is still going i.e. no checks for the existence of flags are done here.
	 * @param gameState the gameState to analyze. Its team attribute is considered.
	 * @return all possible moves in gameState
	 */
	public static Move[] getAllPossibleMoves(GameState gameState) {
		return null;
	}

	/**
	 * Checks if any Move is possible in gameState.
	 * Uses the same algorithm as {@link #getAllPossibleMoves(GameState)} but stops after finding the first Move.
	 * Use this Method for anything related to possible Move checks, rather than {@link #getAllPossibleMoves(GameState)}.
	 * @return false if no Move is possible in gameState
	 */
	public static boolean anyMovePossible(GameState state) {
		ArrayList<Piece> list = new ArrayList<Piece>();
		for(int i=0; i<10; i++)
			if(state.getCurrentPieces()[i] != null) list.add(state.getCurrentPieces()[i]);
		ArrayList<Direction> directions = new ArrayList(4);
		return false;
	}

	public static boolean isGameOver(GameState gameState) {
		// TODO consider flags, pieces and so on
		return !anyMovePossible(gameState);
	}

	public static void printField(Piece[][] field) {
		for(int y=-1; y<8; y++) {
			System.out.print(y == -1 ? "y" : (y + " "));
			for(int x=0; x<8; x++) {
				System.out.print(y == -1 ? (x == 0 ? "\\x 0" : "  " + x + " "): (
						field[x][y] == null ? 
								(blockedByLake(x, y) ? "  x " : "  . " ) : 
									((field[x][y].getTeam() ? "r_" : "b_") + 
											(field[x][y].getType().getStrength() == 0 ? 
													(field[x][y].getType() == PieceType.FLAGGE ? "F" : "B") : 
														field[x][y].getType().getStrength()) + " ")));
			}
			System.out.println();
		}
	}
}
