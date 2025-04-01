package core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class DirectionTest {

	@Test
	void testTranslate() {
		int[] original = new int[] {0,0};
		for(int y=0; y<8; y++) {
			assertTrue(Arrays.equals(Direction.UP.translate(original.clone(), y), new int[] {0, -y}));
			assertTrue(Arrays.equals(Direction.DOWN.translate(original.clone(), y), new int[] {0, y}));
			assertTrue(Arrays.equals(Direction.LEFT.translate(original.clone(), y), new int[] {-y, 0}));
			assertTrue(Arrays.equals(Direction.RIGHT.translate(original.clone(), y), new int[] {y, 0}));
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
