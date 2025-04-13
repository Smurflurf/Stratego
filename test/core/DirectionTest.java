package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DirectionTest {

	@Test
	void testTranslate() {
		byte original = ByteMapper.toByte(4, 4);
		for(int y=0; y<4; y++) {
			assertEquals(Direction.UP.translate(original, y),ByteMapper.toByte(4, 4-y));
			assertEquals(Direction.DOWN.translate(original, y), ByteMapper.toByte(4, 4+y));
			assertEquals(Direction.LEFT.translate(original, y), ByteMapper.toByte(4-y, 4));
			assertEquals(Direction.RIGHT.translate(original, y), ByteMapper.toByte(4+y, 4));
		}
	}

	@Test
	void testGet() {
		int[] from = new int[] {4,4};
		
		int[] to = new int[] {2,4};
		assertTrue(Direction.get(from[0], to[0], from[1], to[1]) == Direction.LEFT);
		to = new int[] {6,4};
		assertTrue(Direction.get(from[0], to[0], from[1], to[1]) == Direction.RIGHT);
		to = new int[] {4,2};
		assertTrue(Direction.get(from[0], to[0], from[1], to[1]) == Direction.UP);
		to = new int[] {4,6};
		assertTrue(Direction.get(from[0], to[0], from[1], to[1]) == Direction.DOWN);
 	}

}
