package core;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.placing.Placer;

class UtilsTest extends Utils {
	GameState state;
	
	@Test
	void testTwoSquaresRule1() {
		Move move = new Move(state.getCurrentPieces()[4], Direction.UP, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// first rep red
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.UP, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// first rep blue
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// second rep red
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.DOWN, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// second rep blue
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.UP, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// third rep red
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.UP, 1);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// third rep blue
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.DOWN, 1);
		assertTrue(Utils.twoSquaresRule(state, move));
		assertFalse(checkAndExecute(state, move));								// fourth rep red
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.DOWN, 1);
		assertTrue(Utils.twoSquaresRule(state, move));
		assertFalse(checkAndExecute(state, move));								// fourth rep blue
		state.changeTeam();
		
		assertTrue(Utils.twoSquaresRule(state, move));
	}
	
	@Test
	void testTwoSquaresRule2() {
		Move move = new Move(state.getCurrentPieces()[4], Direction.UP, 2);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// first rep red
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.UP, 2);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// first rep blue
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.DOWN, 2);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// second rep red
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.DOWN, 2);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// second rep blue
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.UP, 2);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// third rep red
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.UP, 2);
		assertFalse(Utils.twoSquaresRule(state, move));
		assertTrue(checkAndExecute(state, move));								// third rep blue
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.DOWN, 2);
		assertTrue(Utils.twoSquaresRule(state, move));
		assertFalse(checkAndExecute(state, move));								// fourth rep red
		state.changeTeam();
		
		move = new Move(state.getCurrentPieces()[4], Direction.DOWN, 2);
		assertTrue(Utils.twoSquaresRule(state, move));
		assertFalse(checkAndExecute(state, move));								// fourth rep blue
		state.changeTeam();
		
		assertTrue(Utils.twoSquaresRule(state, move));
	}
	
	@Test
	void testTwoSquaresRuleMore() throws InterruptedException {
		Piece[] redPieces = new Piece[10];
		Piece[] bluePieces = new Piece[10];
		redPieces[9] = new Piece(PieceType.FLAGGE, true);
		redPieces[9].setPos(0, 7);
		redPieces[1] = new Piece(PieceType.SPAEHER, true);
		redPieces[1].setPos(7, 7);
		bluePieces[9] = new Piece(PieceType.FLAGGE, false);
		bluePieces[9].setPos(0, 0);
		bluePieces[0] = new Piece(PieceType.SPIONIN, false);
		bluePieces[0].setPos(7, 0);
		bluePieces[1] = new Piece(PieceType.SPAEHER, false);
		bluePieces[1].setPos(2, 0);
		state = new GameState(redPieces, bluePieces);
//		UI ui = new UI();
//		ui.updateBoard(state, null);
		
		Move move = new Move(redPieces[1], Direction.UP, 7);
		assertTrue(Utils.checkAndExecute(state, move));				// 1 : attack UP
		state.changeTeam();
//		ui.updateBoard(state, move);
//		Thread.sleep(1000);
		
		move = new Move(bluePieces[1], Direction.DOWN, 1);
		assertTrue(Utils.checkAndExecute(state, move));
		state.changeTeam();
//		ui.updateBoard(state, move);
//		Thread.sleep(1000);
		
		move = new Move(redPieces[1], Direction.DOWN, 2);			
		assertTrue(Utils.checkAndExecute(state, move));				// 2 : go 2 DOWN
		state.changeTeam();
//		ui.updateBoard(state, move);
//		Thread.sleep(500);
		
		move = new Move(bluePieces[1], Direction.UP, 1);
		assertTrue(Utils.checkAndExecute(state, move));				
		state.changeTeam();
//		ui.updateBoard(state, move);
//		Thread.sleep(500);
		
		move = new Move(redPieces[1], Direction.UP, 1);				
		assertTrue(Utils.checkAndExecute(state, move));				// 3 : go 1 UP
		state.changeTeam();
//		ui.updateBoard(state, move);
//		Thread.sleep(500);
		
		move = new Move(bluePieces[1], Direction.DOWN, 1);
		assertTrue(Utils.checkAndExecute(state, move));				
		state.changeTeam();
//		ui.updateBoard(state, move);
//		Thread.sleep(500);
		
		move = new Move(redPieces[1], Direction.DOWN, 3);				
		assertFalse(Utils.checkAndExecute(state, move));				// 4 : go 3 DOWN
		state.changeTeam();
//		ui.updateBoard(state, move);
//		Thread.sleep(3000);
	}
	
	@Test
	void testCheckAndExecute() {
		// not possible not walk out of bounds
		state.changeTeam();
		Move move = new Move(state.getBluePieces()[5], Direction.RIGHT, 1);
		assertTrue(checkAndExecute(state, move));
		Move move2 = new Move(state.getBluePieces()[5], Direction.RIGHT, 1);
		assertFalse(checkAndExecute(state, move2));
		init();
		
		// Move b2 1,2 to 2,2
		// Move b3 3,2 to 2,2
		state.changeTeam();
		move = new Move(state.getBluePieces()[4], Direction.RIGHT, 1);
		assertTrue(checkAndExecute(state, move));
		move2 = new Move(state.getBluePieces()[2], Direction.LEFT, 1);
		assertFalse(checkAndExecute(state, move2));
		init();
	}
	
//	@Test
//	void testRemovePiece() {
//		Move preMove = new Move(state.getRedPieces()[4], Direction.DOWN, 2);
//		Move move = new Move(preMove, Direction.UP, 5);
//		Utils.checkAndExecute(state, move);
//	}
	
	@Test
	void testIsMovePossible() {
		//Piece cannot walk out of bounds
		state.changeTeam();
		Move move = new Move(state.getBluePieces()[5], Direction.RIGHT, 1);
		assertTrue(isMovePossible(state, move));
		makeMove(state, move);
		Move move2 = new Move(state.getBluePieces()[5], Direction.RIGHT, 1);
		assertFalse(isMovePossible(state, move2));
		init();
		
		//Piece cannot walk on another Piece from the same team
		state.changeTeam();
		move = new Move(state.getBluePieces()[4], Direction.RIGHT, 1);
		assertTrue(isMovePossible(state, move));
		makeMove(state, move);
		move2 = new Move(state.getBluePieces()[2], Direction.LEFT, 1);
		assertFalse(isMovePossible(state, move2));
	}
	
	@Test
	void testReach() {
		ArrayList<int[]> enemyPieces = new ArrayList<int[]>();
		
		Piece piece = state.getRedPieces()[1];
		assertEquals(1, reach(state.getField(), piece, 0, enemyPieces));
		assertEquals(1, reach(state.getField(), piece, 1, enemyPieces));
		assertEquals(0, reach(state.getField(), piece, 2, enemyPieces));
		assertEquals(0, reach(state.getField(), piece, 3, enemyPieces));
	
		piece = state.getRedPieces()[5];
		assertEquals(3, reach(state.getField(), piece, 0, enemyPieces));
		assertEquals(2, reach(state.getField(), piece, 1, enemyPieces));
		assertEquals(1, reach(state.getField(), piece, 2, enemyPieces));
		assertEquals(0, reach(state.getField(), piece, 3, enemyPieces));
		
		piece = state.getBluePieces()[4];
		assertEquals(2, reach(state.getField(), piece, 0, enemyPieces));
		assertEquals(3, reach(state.getField(), piece, 1, enemyPieces));
		assertEquals(0, reach(state.getField(), piece, 2, enemyPieces));
		assertEquals(1, reach(state.getField(), piece, 3, enemyPieces));
		

		piece = state.getBluePieces()[2];
		assertEquals(0, reach(state.getField(), piece, 0, enemyPieces));
		assertEquals(1, reach(state.getField(), piece, 1, enemyPieces));
		assertEquals(1, reach(state.getField(), piece, 2, enemyPieces));
		assertEquals(0, reach(state.getField(), piece, 3, enemyPieces));	
		
		assertTrue(enemyPieces.size() == 2);
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
		
		assertFalse(canReach(state.getRedPieces()[9], 0, 5));	//Piece: FLAGGE cannot walk
		assertFalse(canReach(state.getRedPieces()[8], 0, 5));	//Piece: BOMBE cannot walk
		assertFalse(canReach(state.getRedPieces()[7], 0, 5));	//Piece: BOMBE cannot walk
		assertFalse(canReach(state.getRedPieces()[6], 0, 5));	//Piece: SPIONIN on 2,6. Can only walk 1 and thus not reach 0,5
		assertTrue(canReach(state.getRedPieces()[5], 0, 5));		//Piece: SPAEHER on 6,5. Can walk 7 and thus reach 0,5
		assertTrue(canReach(state.getRedPieces()[4], 0, 5));		//Piece: SPAEHER on 1,5. Can reach 0,5
		assertFalse(canReach(state.getRedPieces()[3], 0, 5));	//Piece: MINEUR on 4,5. Can only walk 1 and thus not reach 0,5
		assertFalse(canReach(state.getRedPieces()[2], 0, 5));	//Piece: MINEUR on 3,5. Can only walk 1 and thus not reach 0,5
		assertFalse(canReach(state.getRedPieces()[1], 0, 5));	//Piece: GENERAL on 7,5. Can only walk 1 and thus not reach 0,5
		assertFalse(canReach(state.getRedPieces()[0], 0, 5));	//Piece: MARSCHALL on 3,7. Can only walk 1 and thus not reach 0,5
		
		for(Piece piece : arrays) {	
			for(int x=1; x<7; x++) {	//no piece can walk diagonally
				assertFalse(canReach(piece, piece.getX()-x, piece.getY()-x));
				assertFalse(canReach(piece, piece.getX()+x, piece.getY()-x));
				assertFalse(canReach(piece, piece.getX()-x, piece.getY()+x));
				assertFalse(canReach(piece, piece.getX()+x, piece.getY()+x));
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
