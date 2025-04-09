import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import core.Direction;
import core.GameState;
import core.Move;
import core.Piece;
import core.PieceType;
import core.Utils;
import core.placing.Placer;

class MediatorTest {

	@Test
	void testMakeMove() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);	//Fight between two SPAEHER
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		Mediator mediator = new Mediator(new GameState(redPieces, bluePieces));	
		Move valid = new Move(redPieces[5], Direction.UP, 3);
		assertTrue(mediator.makeMove(valid));
//		mediator.print();
		
		redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);	//Move Flag
		bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		mediator = new Mediator(new GameState(redPieces, bluePieces));	
		Move invalid = new Move(redPieces[9], Direction.RIGHT, 0);
		assertFalse(mediator.makeMove(invalid));
//		mediator.print();


		redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);	//Blue Piece moves when red should
		bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		mediator = new Mediator(new GameState(redPieces, bluePieces));	
		invalid = new Move(bluePieces[1], Direction.DOWN, 1);
//		System.out.println(bluePieces[1] + " " + invalid);
		assertFalse(mediator.makeMove(invalid));
//		mediator.print();
		
		redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);	//Blue Piece moves when blue should
		bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState gs = new GameState(redPieces, bluePieces);
		gs.changeTeam();
		mediator = new Mediator(gs);	
		valid = new Move(bluePieces[1], Direction.DOWN, 1);
		assertTrue(mediator.makeMove(invalid));
//		mediator.print();
		

		// game that failed in practice even though it should not have failed
		// makes moves with mediator and directly to gameState, then checks for equality
		/*mediator = new Mediator(
				new GameState(
						Placer.placePiecesWith(true, Placer.Type.PREBUILT), 
						Placer.placePiecesWith(false, Placer.Type.PREBUILT)));
		GameState state = mediator.getGameState().clone();
		Move firstMove = new Move(mediator.getGameState().getRedPieces()[1], Direction.UP, 1);
		assertTrue(mediator.makeMove(firstMove));
//		mediator.print();
		Move secondMove = new Move(mediator.getGameState().getBluePieces()[0], Direction.DOWN, 1);
		assertTrue(mediator.makeMove(secondMove));
//		mediator.print();
		Move thirdMove = new Move(mediator.getGameState().getRedPieces()[4], Direction.DOWN, 2);
		Move thirdAttack = new Move(thirdMove, Direction.UP, 5);
		assertTrue(mediator.makeMove(thirdAttack));
//		mediator.print();
		
		firstMove = new Move(state.getRedPieces()[1], Direction.UP, 1);
		assertTrue(Utils.checkAndExecute(state, firstMove));
		state.changeTeam();
		secondMove = new Move(state.getBluePieces()[0], Direction.DOWN, 1);
		assertTrue(Utils.checkAndExecute(state, secondMove));
		state.changeTeam();
		thirdMove = new Move(state.getRedPieces()[4], Direction.DOWN, 2);
		thirdAttack = new Move(thirdMove, Direction.UP, 5);
		assertTrue(Utils.checkAndExecute(state, thirdAttack));
		state.changeTeam();
		
		assertTrue(mediator.getGameState().equals(state));*/
	}
	
	@Test
	public void testObfuscateFor() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		Mediator mediator = new Mediator(new GameState(redPieces, bluePieces));
		
		for(Piece p : mediator.obfuscateFor(true).getBluePieces())
			assertEquals(PieceType.UNKNOWN, p.getType());
		for(Piece p: mediator.obfuscateFor(true).getRedPieces())
			assertNotEquals(PieceType.UNKNOWN, p.getType());
		for(Piece p : mediator.obfuscateFor(false).getRedPieces())
			assertEquals(PieceType.UNKNOWN, p.getType());
		for(Piece p: mediator.obfuscateFor(false).getBluePieces())
			assertNotEquals(PieceType.UNKNOWN, p.getType());
		
		GameState obfusRed = mediator.obfuscateFor(true);
		GameState obfusBlue = mediator.obfuscateFor(false);
		for(int i=0; i<10; i++) {
			assertTrue(obfusRed.getRedPieces()[i] != redPieces[i]);
			assertTrue(obfusRed.getBluePieces()[i] != bluePieces[i]);
			assertTrue(obfusBlue.getRedPieces()[i] != redPieces[i]);
			assertTrue(obfusBlue.getBluePieces()[i] != bluePieces[i]);
		}
	}
}
