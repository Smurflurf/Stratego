package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import core.placing.Placer;
import core.playing.random.RandomAI;

class GameStateTest {	
	
	@Test
	void testKnownAndDead() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);

		Piece piece = redPieces[0];
		state.removePiece(piece);
		assertEquals(1, state.getDeadRed());
		
		piece = redPieces[4];
		Utils.execute(state, new Move(piece, Direction.UP, 2));
		assertEquals(1, state.getKnownRed());
	}
	
	@Test
	void testKnown() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);

		assertEquals(0, state.getKnownRed());
		assertEquals(0, state.getKnownBlue());
		
		for(int i=1; i<11; i++) {
			state.incrementKnownRed();
			assertEquals(i, state.getKnownRed());
			assertEquals(0, state.getKnownBlue());
		}
		
		for(int i=1; i<11; i++) {
			state.incrementKnownBlue();
			assertEquals(10, state.getKnownRed());
			assertEquals(i, state.getKnownBlue());
		}
	}
	
	@Test
	void testDead() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);

		assertEquals(0, state.getDeadRed());
		assertEquals(0, state.getDeadBlue());
		
		for(int i=1; i<11; i++) {
			state.incrementDeadRed();
			assertEquals(i, state.getDeadRed());
			assertEquals(0, state.getDeadBlue());
		}
		
		for(int i=1; i<11; i++) {
			state.incrementDeadBlue();
			assertEquals(10, state.getDeadRed());
			assertEquals(i, state.getDeadBlue());
		}
	}
	
	@Test
	void testGetLastMove() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);
		
		Move move = new Move(redPieces[4], Direction.UP, 2);
		Utils.checkAndExecute(state, move);
		assertTrue(move.equals(state.getLastMove()));
	}

	@Test
	void testRepetitions() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);
		
		for(int i=0; i<4; i++)
			for(int ii=0; ii<4; ii++) {
				state.setRepetitionsRed(i);
				state.setRepetitionsBlue(ii);
				assertEquals(i, state.getRepetitionsRed());
				assertEquals(ii, state.getRepetitionsBlue());
				
				GameState clone = state.clone();
				assertTrue(clone.equals(state));
				assertTrue(clone.getRepetitions() == state.getRepetitions());
			}
		
		
		Utils.checkAndExecute(state, new Move(redPieces[4], Direction.UP, 2));
//		System.out.println(state.getRepetitionsRed() + " " + state.getRepetitionRedFields().size());
//		state.getRepetitionRedFields().forEach(b -> System.out.println(ByteMapper.getX(b) + " " + ByteMapper.getY(b)));
					
//		Utils.printField(state.getField());
	}
	
	@Test
	void testObfuscate() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);
		assertFalse(state.obfuscateFor(true).equals(state));
		assertFalse(state.obfuscateFor(false).equals(state));
	}

	@Test
	void testEquals(){
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);
		GameState state2 = state.clone();
		
		assertTrue(state.equals(state2));
		
//		AI randomAI = AI.Type.RANDOM.createAI(true, state);
		Utils.checkAndExecute(state, RandomAI.nextMove(state));
		state2 = state.clone();
		assertTrue(state.equals(state2));
	}
}
