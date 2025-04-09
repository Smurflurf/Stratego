import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;

import core.Direction;
import core.GameState;
import core.Move;
import core.Piece;
import core.PieceType;
import core.Utils;
import core.placing.Placer;
import core.playing.AI;

public class Profiler {
	public static void main(String[] args) {
		memoryFootprint(Class.Move);
	}
	
	public static void memoryFootprint(Class c) {
		Object object = null;
		switch(c) {
		case GameState:
			object = new GameState(Placer.placePiecesWith(true, Placer.Type.PREBUILT), Placer.placePiecesWith(false, Placer.Type.PREBUILT));
			Utils.checkAndExecute((GameState) object, AI.Type.RANDOM.createAI(true, (GameState) object).nextMove());
			break;
		case Piece:
			object = new Piece(PieceType.MARSCHALL, true);
			break;
		case Move:
			object = new Move(new Piece(PieceType.SPAEHER, true), Direction.UP, 1);
		}
		
		System.out.println(ClassLayout.parseInstance(object).toPrintable());
//		System.out.println(GraphLayout.parseInstance(object).toPrintable());
		System.out.println(GraphLayout.parseInstance(object).toFootprint());
	}
	
	enum Class {
		GameState,
		Piece,
		Move;
	};
}
