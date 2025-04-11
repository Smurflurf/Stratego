package core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

class ByteMapperTest {
	@Test
	void testHash() {
		HashSet<Short> positions = new HashSet<Short>();
		for(int x1=0; x1<8; x1++) {
			for(int y1=0; y1<8; y1++) {
				for(int x2=0; x2<8; x2++) {
					for(int y2=0; y2<8; y2++) {
						assertTrue(positions.add(ByteMapper.hash(ByteMapper.toByte(x1, y1), ByteMapper.toByte(x2, y2))));
					}
				}
			}
		}
	}
	
	@Test
	void testAllNumbers() {
		for(int x=0; x<8; x++) {
			for(int y=0; y<8; y++) {
				byte control = (byte)((x<<4) + y);
				byte b = ByteMapper.toByte(x, y);
				assertEquals(control, b);
				assertEquals(control, ByteMapper.setX(b, x));
				assertEquals(control, ByteMapper.setY(b, y));
			}
		}
	}
	
	@Test
	void test() {
		int x = 7;
		int y = 1;
		
		byte b = ByteMapper.toByte(x, y);
		assertEquals(x+y, ByteMapper.getX(b) + ByteMapper.getY(b));
		
		b = ByteMapper.setX(b, 1);
		assertEquals(2, ByteMapper.getX(b) + ByteMapper.getY(b));
		assertEquals(1, ByteMapper.getX(b));
		assertEquals(1, ByteMapper.getY(b));
		
		b = ByteMapper.toByte(y, x);
		b = ByteMapper.setY(b, 1);
		assertEquals(2, ByteMapper.getY(b) + ByteMapper.getY(b));
		assertEquals(1, ByteMapper.getY(b));
		assertEquals(1, ByteMapper.getX(b));
		
		assertEquals(16, ByteMapper.toByte(1, 0));
		assertEquals(16, ByteMapper.setX(ByteMapper.toByte(0, 0), 1));
		assertEquals(1, ByteMapper.toByte(0, 1));
		assertEquals(1, ByteMapper.setY(ByteMapper.toByte(0, 0), 1));
		

		
		byte position = ByteMapper.toByte(7, 0);
		position = ByteMapper.setX(position, (byte) (7));
		assertEquals(7, ByteMapper.getX(position));
		position = ByteMapper.setX(position, (byte) (6));
		assertEquals(6, ByteMapper.getX(position));
	}

}
