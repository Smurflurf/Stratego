package core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MoveTest {

	@Test
	void testGetDirection() {
		Move move1 = new Move(new Piece(PieceType.SPAEHER, true, 4, 4), Direction.UP, 2);
		Move move2 = new Move(new Piece(PieceType.SPAEHER, true, 4, 4), Direction.DOWN, 2);
		Move move3 = new Move(new Piece(PieceType.SPAEHER, true, 4, 4), Direction.LEFT, 2);
		Move move4 = new Move(new Piece(PieceType.SPAEHER, true, 4, 4), Direction.RIGHT, 2);
		
		assertEquals(Direction.UP, move1.getDirection());
		assertEquals(Direction.DOWN, move2.getDirection());
		assertEquals(Direction.LEFT, move3.getDirection());
		assertEquals(Direction.RIGHT, move4.getDirection());
	}
	
	@Test
	void testGetFields() {
		Move move1 = new Move(new Piece(PieceType.SPAEHER, true, 4, 4), Direction.UP, 2);
		Move move2 = new Move(new Piece(PieceType.SPAEHER, true, 4, 4), Direction.DOWN, 3);
		Move move3 = new Move(new Piece(PieceType.SPAEHER, true, 4, 4), Direction.LEFT, 1);
		Move move4 = new Move(new Piece(PieceType.SPAEHER, true, 4, 4), Direction.RIGHT, 2);

		
		assertEquals(2, move1.getFields(move1.getDirection()));
		assertEquals(3, move2.getFields(move2.getDirection()));
		assertEquals(1, move3.getFields(move3.getDirection()));
		assertEquals(2, move4.getFields(move4.getDirection()));
	}

}
