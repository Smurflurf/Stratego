package core.playing.random;

import core.Direction;
import core.GameState;
import core.Move;
import core.Piece;
import core.Utils;
import core.placing.Placer;
import core.playing.AI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.SplittableRandom;

public class RandomAI extends AI {
	SplittableRandom random;
	
	public RandomAI(boolean team, GameState gameState) {
		super(team, gameState);
		random = new SplittableRandom();
	}
	
	public static void main(String[] args) {
		GameState state = new GameState(Placer.placePiecesWith(true, Placer.Type.PREBUILT), Placer.placePiecesWith(false, Placer.Type.PREBUILT));
		RandomAI rand = new RandomAI(true, state);
		printField(state.getField());
		System.out.println(rand.next());
		
		long start = System.currentTimeMillis();
//		for(int i=0; i<100000000; i++)
//			rand.next();
		long stop = System.currentTimeMillis();
		System.out.println(stop - start + " ms");
		
	}
	/**
	 * Returns a random Move, if a Move is possible.
	 * Null otherwise.
	 * @return
	 */
	public Move next() {
		Move move = getFirstMove();
		return move;
	}
	
	private Move getFirstMove() {
		ArrayList<Piece> pieces = new ArrayList<Piece>();
		ArrayList<int[]> dirMap = new ArrayList<int[]>();
		
		for(int i=0; i<10; i++)
			if(myPieces[i] != null) pieces.add(myPieces[i]);

		while(pieces.size() > 0) {
			int randomInt = random.nextInt(pieces.size());
			Piece picked = pieces.get(randomInt);
			
			fillDirectionMap(gameState, picked, dirMap);
	        if (dirMap.size() > 0) {
	          return getDirectionMove(dirMap, picked);
	        } else {
	          pieces.remove(randomInt);
	          continue;
	        }
		}
	
		return null;
	}
	
	public Move getDirectionMove(ArrayList<int[]> dirMap, Piece picked) {
		int[] directionFields = dirMap.get(random.nextInt(dirMap.size()));
		return new Move(picked, Direction.get(directionFields[0]), directionFields[1]);
	}
	
	@Override
	//TODO simpel, muss überarbeitet werden!
	public Move nextMove() {
		Move move = null;
		Piece piece = null;
		while(true) {
			while(piece == null)
				piece = myPieces[random.nextInt(10)];
			Direction dir = Direction.get(random.nextInt(4));
			int fields = random.nextInt(piece.getType().getMoves()) + 1;
			move = new Move(piece, dir, fields);
//			System.out.println("Piece " + piece.toString() + " on [" + piece.getX() + "|" + piece.getY() + "] to " + fields + " " + dir + "  result: [" + target[0] +"|"+ target[1] + "]" + " is possible? " + Utils.isMovePossible(move, gameState));
			if(Utils.isMovePossible(gameState, move))
				return move;
		}
	}
}
