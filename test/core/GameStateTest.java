package core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import core.placing.Placer;

class GameStateTest {

	@Test
	void testObfuscate() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(redPieces, bluePieces);
		assertFalse(state.obfuscateFor(true).equals(state));
		assertFalse(state.obfuscateFor(false).equals(state));
	}
}
