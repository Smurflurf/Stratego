package core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ByteMapperTest {
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
