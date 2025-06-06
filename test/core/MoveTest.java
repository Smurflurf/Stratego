package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MoveTest {

	@Test
	void getRelevantFields() {
		Move move1 = new Move(new Piece(PieceType.MARSCHALL.getByte(), true, ByteMapper.toByte(4, 4)), Direction.UP, 1);
		short fields = move1.getRelevantFields();
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(4, 4)));
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(4, 3)));
		
		Move move2 = new Move(new Piece(PieceType.SPAEHER.getByte(), true, ByteMapper.toByte(4, 4)), Direction.UP, 4);
		fields = move2.getRelevantFields();
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(4, 4)));
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(4, 3)));
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(4, 2)));
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(4, 1)));
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(4, 0)));
		
		Move move3 = new Move(new Piece(PieceType.SPAEHER.getByte(), true, ByteMapper.toByte(4, 4)), Direction.RIGHT, 3);
		fields = move3.getRelevantFields();
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(4, 4)));
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(5, 4)));
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(6, 4)));
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(7, 4)));

		Move move4 = new Move(new Piece(PieceType.MINEUR.getByte(), false, ByteMapper.toByte(7, 0)), Direction.LEFT, 1);
		fields = move4.getRelevantFields();

		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(7, 0)));
		assertTrue(ByteMapper.contains(fields, ByteMapper.toByte(6, 0)));
	}
	
	@Test
	void testGetDirection() {
		Move move1 = new Move(new Piece(PieceType.SPAEHER.getByte(), true, ByteMapper.toByte(4, 4)), Direction.UP, 2);
		Move move2 = new Move(new Piece(PieceType.SPAEHER.getByte(), true, ByteMapper.toByte(4, 4)), Direction.DOWN, 2);
		Move move3 = new Move(new Piece(PieceType.SPAEHER.getByte(), true, ByteMapper.toByte(4, 4)), Direction.LEFT, 2);
		Move move4 = new Move(new Piece(PieceType.SPAEHER.getByte(), true, ByteMapper.toByte(4, 4)), Direction.RIGHT, 2);
		
		assertEquals(Direction.UP, move1.getDirection());
		assertEquals(Direction.DOWN, move2.getDirection());
		assertEquals(Direction.LEFT, move3.getDirection());
		assertEquals(Direction.RIGHT, move4.getDirection());
	}
	
	@Test
	void testGetFields() {
		Move move1 = new Move(new Piece(PieceType.SPAEHER.getByte(), true, ByteMapper.toByte(4, 4)), Direction.UP, 2);
		Move move2 = new Move(new Piece(PieceType.SPAEHER.getByte(), true, ByteMapper.toByte(4, 4)), Direction.DOWN, 3);
		Move move3 = new Move(new Piece(PieceType.SPAEHER.getByte(), true, ByteMapper.toByte(4, 4)), Direction.LEFT, 1);
		Move move4 = new Move(new Piece(PieceType.SPAEHER.getByte(), true, ByteMapper.toByte(4, 4)), Direction.RIGHT, 2);

		
		assertEquals(2, move1.getFields());
		assertEquals(3, move2.getFields());
		assertEquals(1, move3.getFields());
		assertEquals(2, move4.getFields());
	}

}
