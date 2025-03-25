package core;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.placing.Placer;

class UtilsTest extends Utils {
	GameState state;
	
	@Test
	void testReach() {
		Piece piece = state.getRedPieces()[1];		
		assertEquals(1, reach(state.getField(), piece, 0));
		assertEquals(1, reach(state.getField(), piece, 1));
		assertEquals(0, reach(state.getField(), piece, 2));
		assertEquals(0, reach(state.getField(), piece, 3));
	
		piece = state.getRedPieces()[5];
		assertEquals(3, reach(state.getField(), piece, 0));
		assertEquals(2, reach(state.getField(), piece, 1));
		assertEquals(1, reach(state.getField(), piece, 2));
		assertEquals(0, reach(state.getField(), piece, 3));
		
		piece= state.getBluePieces()[4];
		assertEquals(2, reach(state.getField(), piece, 0));
		assertEquals(3, reach(state.getField(), piece, 1));
		assertEquals(0, reach(state.getField(), piece, 2));
		assertEquals(1, reach(state.getField(), piece, 3));
		

		piece= state.getBluePieces()[2];
		assertEquals(0, reach(state.getField(), piece, 0));
		assertEquals(1, reach(state.getField(), piece, 1));
		assertEquals(1, reach(state.getField(), piece, 2));
		assertEquals(0, reach(state.getField(), piece, 3));	
	}

	@Test
	void testMakeMove() {
		int oldX = state.getRedPieces()[5].getX();
		int oldY = state.getRedPieces()[5].getY();
		int newX = 0;
		int newY = 5;
		
		Move move = new Move(state.getRedPieces()[5], Direction.LEFT, 6);
		state.move(move);
		
		assertTrue(state.getField()[oldX][oldY] == null);
		assertTrue(state.getField()[newX][newY] != null);
		assertTrue(state.getField()[newX][newY] == move.getPiece());
		assertTrue(state.getField()[newX][newY] == state.getRedPieces()[5]);
	}

	@Test
	void testSightLine() {
		assertTrue(sightLine(state.getField(), state.getRedPieces()[4], 1, Direction.LEFT));
		assertFalse(sightLine(state.getField(), state.getRedPieces()[5], 4, Direction.LEFT));

		assertTrue(sightLine(state.getField(), state.getRedPieces()[5], 3, Direction.UP));
		assertTrue(sightLine(state.getField(), state.getBluePieces()[5], 3, Direction.DOWN));
		assertTrue(sightLine(state.getField(), state.getRedPieces()[5], 2, Direction.LEFT));
		assertTrue(sightLine(state.getField(), state.getBluePieces()[4], 2, Direction.RIGHT));
		
		assertFalse(sightLine(state.getField(), state.getBluePieces()[4], 3, Direction.RIGHT));
		assertFalse(sightLine(state.getField(), state.getBluePieces()[4], 5, Direction.RIGHT));
		assertFalse(sightLine(state.getField(), state.getBluePieces()[5], 4, Direction.DOWN));
		assertFalse(sightLine(state.getField(), state.getBluePieces()[5], 8, Direction.UP));
	}

	@Test
	void testCanReach() {
		Piece[] arrays = Stream.concat(Arrays.stream(state.getRedPieces()), Arrays.stream(state.getBluePieces())).toArray(Piece[]::new);
		
		assertFalse(canReach(state.getRedPieces()[9], new int[] {0,5}));	//Piece: FLAGGE cannot walk
		assertFalse(canReach(state.getRedPieces()[8], new int[] {0,5}));	//Piece: BOMBE cannot walk
		assertFalse(canReach(state.getRedPieces()[7], new int[] {0,5}));	//Piece: BOMBE cannot walk
		assertFalse(canReach(state.getRedPieces()[6], new int[] {0,5}));	//Piece: SPIONIN on 2,6. Can only walk 1 and thus not reach 0,5
		assertTrue(canReach(state.getRedPieces()[5], new int[] {0,5}));		//Piece: SPAEHER on 6,5. Can walk 7 and thus reach 0,5
		assertTrue(canReach(state.getRedPieces()[4], new int[] {0,5}));		//Piece: SPAEHER on 1,5. Can reach 0,5
		assertFalse(canReach(state.getRedPieces()[3], new int[] {0,5}));	//Piece: MINEUR on 4,5. Can only walk 1 and thus not reach 0,5
		assertFalse(canReach(state.getRedPieces()[2], new int[] {0,5}));	//Piece: MINEUR on 3,5. Can only walk 1 and thus not reach 0,5
		assertFalse(canReach(state.getRedPieces()[1], new int[] {0,5}));	//Piece: GENERAL on 7,5. Can only walk 1 and thus not reach 0,5
		assertFalse(canReach(state.getRedPieces()[0], new int[] {0,5}));	//Piece: MARSCHALL on 3,7. Can only walk 1 and thus not reach 0,5
		
		for(Piece piece : arrays) {	
			for(int x=1; x<7; x++) {	//no piece can walk diagonally
				assertFalse(canReach(piece, new int[] {piece.getX()-x, piece.getY()-x}));
				assertFalse(canReach(piece, new int[] {piece.getX()+x, piece.getY()-x}));
				assertFalse(canReach(piece, new int[] {piece.getX()-x, piece.getY()+x}));
				assertFalse(canReach(piece, new int[] {piece.getX()+x, piece.getY()+x}));
			}
		}
	}

	@Test
	void testOutOfBounds() {
		assertTrue(outOfBounds(new int[] {-1, 0}));
		assertTrue(outOfBounds(new int[] {0, -1}));
		assertTrue(outOfBounds(new int[] {-1, -1}));
		assertTrue(outOfBounds(new int[] {10, 0}));
		assertTrue(outOfBounds(new int[] {0, 10}));
		assertTrue(outOfBounds(new int[] {10, 10}));
		for(int x=0; x<8; x++)
			for(int y=0; y<8; y++)
				assertFalse(outOfBounds(new int[] {x, y}));
	}

	@BeforeEach
	void init() {
		Piece[] redPieces = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] bluePieces = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		state = new GameState(redPieces, bluePieces);
	}
}
