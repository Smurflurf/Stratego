import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import core.ByteMapper;
import core.Direction;
import core.GameState;
import core.Move;
import core.Piece;
import core.PieceType;
import core.Utils;
import core.placing.Placer;
import core.playing.AI;
import core.playing.AI.AIInformer;
import core.playing.random.RandomAI;
import executable.Mediator;

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
		
		GameState obfusRed = mediator.obfuscateFor(false);
		GameState obfusBlue = mediator.obfuscateFor(false);
		for(int i=0; i<10; i++) {
			assertTrue(obfusRed.getRedPieces()[i] != redPieces[i]);
			assertTrue(obfusRed.getBluePieces()[i] != bluePieces[i]);
			assertTrue(obfusBlue.getRedPieces()[i] != redPieces[i]);
			assertTrue(obfusBlue.getBluePieces()[i] != bluePieces[i]);
		}
	}
	
	@Test
	void testTwoSquaresRule1() throws InterruptedException {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		redPieces[4].setY((byte)(redPieces[4].getY() +1));
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);
		Mediator mediator = new Mediator(state);	
		Move move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[4], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// first rep red
		assertEquals(0, mediator.getGameState().getCurrentRepetitions());
		
		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[4], Direction.UP, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// first rep blue
		assertEquals(1, mediator.getGameState().getCurrentRepetitions());
		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[4], Direction.UP, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// second rep red
		assertEquals(1, mediator.getGameState().getCurrentRepetitions());
		
		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[4], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// second rep blue
		assertEquals(2, mediator.getGameState().getCurrentRepetitions());

		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[4], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// third rep red
		assertEquals(2, mediator.getGameState().getCurrentRepetitions());
		
		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[4], Direction.UP, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// third rep blue
		assertEquals(3, mediator.getGameState().getCurrentRepetitions());
		
		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[4], Direction.UP, 1);
		assertTrue(Utils.twoSquaresRule(state, move));
		assertFalse(mediator.makeMove(move));								// fourth rep red
		assertEquals(3, mediator.getGameState().getCurrentRepetitions());

		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[6], Direction.LEFT, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// breaking repetitions red
		assertEquals(3, mediator.getGameState().getCurrentRepetitions());
		
		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[4], Direction.DOWN, 1);
		assertTrue(Utils.twoSquaresRule(state, move));
		assertFalse(mediator.makeMove(move));								// fourth rep blue
		assertEquals(3, mediator.getGameState().getCurrentRepetitions());
		
		assertTrue(Utils.twoSquaresRule(state, move));

		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[5], Direction.UP, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// breaking repetitions blue
		assertEquals(1, mediator.getGameState().getCurrentRepetitions());
	}
	
	@Test
	void testTwoSquaresRule2() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		redPieces[4].setY((byte)(redPieces[4].getY() +1));
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		bluePieces[4].setY((byte)(redPieces[4].getY() -1));
		GameState state = new GameState(redPieces, bluePieces);
		Mediator mediator = new Mediator(state);	
		
		Move move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[4], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// first rep red
		assertEquals(0, mediator.getGameState().getCurrentRepetitions());
		
		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[4], Direction.UP, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// first rep blue
		assertEquals(1, mediator.getGameState().getCurrentRepetitions());
		
		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[4], Direction.UP, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// second rep red
		assertEquals(1, mediator.getGameState().getCurrentRepetitions());
		
		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[4], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// second rep blue
		assertEquals(2, mediator.getGameState().getCurrentRepetitions());

		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[4], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// third rep red
		assertEquals(2, mediator.getGameState().getCurrentRepetitions());
		
		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[4], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// third rep blue
		assertEquals(3, mediator.getGameState().getCurrentRepetitions());

		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[4], Direction.UP, 1);
		assertTrue(Utils.twoSquaresRule(state, move));
		assertFalse(mediator.makeMove(move));								// breaking repetitions red
		assertEquals(3, mediator.getGameState().getCurrentRepetitions());
		
		assertTrue(Utils.twoSquaresRule(state, move));

		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[4], Direction.LEFT, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// breaking repetitions blue
		assertEquals(1, mediator.getGameState().getCurrentRepetitions());
	}
	
	@Test
	void testTwoSquaresRule3() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		redPieces[0].setPos(ByteMapper.toByte(3, 5));	// relevant
		redPieces[1] = null;
		redPieces[2].setPos(ByteMapper.toByte(3, 4));
		redPieces[3] = null;
		redPieces[4] = null;
		redPieces[5] = null;
		redPieces[6] = null;
		redPieces[7].setPos(ByteMapper.toByte(6, 5));
		redPieces[8] = null;
		redPieces[9].setPos(ByteMapper.toByte(1, 5));

		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		bluePieces[0] = null;
		bluePieces[1] = null;
		bluePieces[2] = null;
		bluePieces[3].setPos(ByteMapper.toByte(0, 6)); 	// relevant
		bluePieces[4] = null;
		bluePieces[5] = null;
		bluePieces[6] = null;
		bluePieces[7].setPos(ByteMapper.toByte(1, 1));
		bluePieces[8].setPos(ByteMapper.toByte(5, 1));
		bluePieces[9].setPos(ByteMapper.toByte(4, 0));

		GameState state = new GameState(redPieces, bluePieces);
		state.setTeam(false);
		Mediator mediator = new Mediator(state);	
//		AI red = new RandomAI(true, mediator.obfuscateFor(true));
//		AI blue = new RandomAI(false, mediator.obfuscateFor(false));
		
		Move move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[3], Direction.RIGHT, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// move blue Mineur 1 right
		assertEquals(1, mediator.getGameState().getRepetitionsBlue());
//		AIInformer inform = new AIInformer(mediator.obfuscateFor(true), move, false, null, null);
//		red.update(inform);
//		AIInformer inform2 = new AIInformer(mediator.obfuscateFor(false), move, false, null, null);
//		blue.update(inform2);
		
		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[0], Direction.LEFT, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// move red Marschall 1 
		assertEquals(1, mediator.getGameState().getRepetitionsRed());
//		inform = new AIInformer(mediator.obfuscateFor(true), move, false, null, null);
//		red.update(inform);
//		inform2 = new AIInformer(mediator.obfuscateFor(false), move, false, null, null);
//		blue.update(inform2);
		
		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[3], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// move blue Mineur 1 down
		assertEquals(1, mediator.getGameState().getRepetitionsBlue());
//		inform = new AIInformer(mediator.obfuscateFor(true), move, false, null, null);
//		red.update(inform);
//		inform2 = new AIInformer(mediator.obfuscateFor(false), move, false, null, null);
//		blue.update(inform2);
		
		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[0], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// move red Marshall 1 down
		assertEquals(1, mediator.getGameState().getRepetitionsRed());
//		inform = new AIInformer(mediator.obfuscateFor(true), move, false, null, null);
//		red.update(inform);
//		inform2 = new AIInformer(mediator.obfuscateFor(false), move, false, null, null);
//		blue.update(inform2);

		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[3], Direction.UP, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// move blue Mineur 1 up, REP: 2
		assertEquals(2, mediator.getGameState().getRepetitionsBlue());
//		inform = new AIInformer(mediator.obfuscateFor(true), move, false, null, null);
//		red.update(inform);
//		inform2 = new AIInformer(mediator.obfuscateFor(false), move, false, null, null);
//		blue.update(inform2);
	
		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[2], Direction.RIGHT, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// move red Mineur 1 right
		assertEquals(1, mediator.getGameState().getRepetitionsRed());
//		inform = new AIInformer(mediator.obfuscateFor(true), move, false, null, null);
//		red.update(inform);
//		inform2 = new AIInformer(mediator.obfuscateFor(false), move, false, null, null);
//		blue.update(inform2);

		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[3], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// move blue Mineur 1 down, REP: 3
		assertEquals(3, mediator.getGameState().getRepetitionsBlue());
//		inform = new AIInformer(mediator.obfuscateFor(true), move, false, null, null);
//		red.update(inform);
//		inform2 = new AIInformer(mediator.obfuscateFor(false), move, false, null, null);
//		blue.update(inform2);

		
		move = new Move(mediator.obfuscateFor(true).getCurrentPieces()[0], Direction.LEFT, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(mediator.makeMove(move));								// move red Marshall 1 left
		assertEquals(1, mediator.getGameState().getRepetitionsRed());
//		inform = new AIInformer(mediator.obfuscateFor(true), move, false, null, null);
//		red.update(inform);
//		inform2 = new AIInformer(mediator.obfuscateFor(false), move, false, null, null);
//		blue.update(inform2);

		
		for(int i=0; i<10000; i++) {
			move = RandomAI.nextMove(mediator.getGameState());
//			move = blue.nextMove();
			if(move.getEndY() == 6) {
				fail("RandomAI not knowing twoSquaresRule!");
			}
		}
		
		move = new Move(mediator.obfuscateFor(false).getCurrentPieces()[3], Direction.UP, 1);
		assertTrue(Utils.twoSquaresRule(state, move));
		assertFalse(mediator.makeMove(move));								// move blue Mineur 1 left, REP: 4
		assertEquals(3, mediator.getGameState().getRepetitionsBlue());
	}
}
