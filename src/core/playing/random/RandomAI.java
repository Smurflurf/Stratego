package core.playing.random;

import core.Direction;
import core.GameState;
import core.Move;
import core.Piece;
import core.Utils;
import core.playing.AI;

import java.util.SplittableRandom;

public class RandomAI extends AI {
	SplittableRandom random;
	
	public RandomAI(boolean team, GameState gameState) {
		super(team, gameState);
		random = new SplittableRandom();
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
			int[] target = piece.createPos(); 
			dir.translate(target, fields);
			move = new Move(piece, target);
//			System.out.println("Piece " + piece.toString() + " on [" + piece.getX() + "|" + piece.getY() + "] to " + fields + " " + dir + "  result: [" + target[0] +"|"+ target[1] + "]" + " is possible? " + Utils.isMovePossible(move, gameState));
			if(Utils.isMovePossible(move, gameState))
				return move;
		}
	}
}
