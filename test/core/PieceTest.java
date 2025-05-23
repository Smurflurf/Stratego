package core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PieceTest {

	@Test
	void testKnownAndType() {
		Piece piece = new Piece(PieceType.MARSCHALL, true);
		assertEquals(-1, piece.getPos());
		assertEquals(PieceType.MARSCHALL, piece.getType());
		assertFalse(piece.getKnown());
		assertTrue(piece.getTeam());
		
		piece.setPos(5, 3);
		assertEquals(ByteMapper.toByte(5, 3), piece.getPos());
		assertEquals(PieceType.MARSCHALL, piece.getType());
		assertFalse(piece.getKnown());
		assertTrue(piece.getTeam());
		
		piece.setTeam(false);
		assertEquals(ByteMapper.toByte(5, 3), piece.getPos());
		assertEquals(PieceType.MARSCHALL, piece.getType());
		assertFalse(piece.getKnown());
		assertFalse(piece.getTeam());

		piece.setKnown(true);
		assertEquals(ByteMapper.toByte(5, 3), piece.getPos());
		assertEquals(PieceType.MARSCHALL, piece.getType());
		assertTrue(piece.getKnown());
		assertFalse(piece.getTeam());

		piece.setType(PieceType.GENERAL.getByte());
		assertEquals(ByteMapper.toByte(5, 3), piece.getPos());
		assertEquals(PieceType.GENERAL, piece.getType());
		assertTrue(piece.getKnown());
		assertFalse(piece.getTeam());
		
		piece.setKnown(false);
		assertEquals(ByteMapper.toByte(5, 3), piece.getPos());
		assertEquals(PieceType.GENERAL, piece.getType());
		assertFalse(piece.getKnown());
		assertFalse(piece.getTeam());
		
		piece.setType(PieceType.UNKNOWN.getByte());
		assertEquals(ByteMapper.toByte(5, 3), piece.getPos());
		assertEquals(PieceType.UNKNOWN, piece.getType());
		assertFalse(piece.getKnown());
		assertFalse(piece.getTeam());
	}
}
