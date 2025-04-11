package core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import core.placing.Placer;
import core.playing.AI;

class GameStateTest {	
	@Test
	void testGetLastMove() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);
		
		Move move = new Move(redPieces[4], Direction.UP, 2);
		Utils.checkAndExecute(state, move);
		assertEquals(move, state.getLastMove());
	}

	@Test
	void testRepetitions() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);
		
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
		
		AI randomAI = AI.Type.RANDOM.createAI(true, state);
		Utils.checkAndExecute(state, randomAI.nextMove());
		state2 = state.clone();
		assertTrue(state.equals(state2));
	}
}
