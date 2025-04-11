package core;

/**
 * Maps a byte to an x,y representation where the first 4 bits represent x and the last for y.
 */
public class ByteMapper {
	public static byte toByte(int x, int y) {
		return (byte) ((x << 4) | (y));
	}
	
	/**
	 * b must be < 8
	 * @param b
	 * @return
	 */
	public static int getX(byte b) {
		return b >> 4;
	}
	/**
	 * b must be < 8
	 * @param b
	 * @return
	 */
	public static int getY(byte b) {
		return b & 0x0F;
	}

	public static byte setX(byte b, int x) {
		return (byte) ((b & 0x0F) | (x << 4));
	}
	public static byte setY(byte b, int y) {
		return (byte) ((b & 0xF0) | (y & 0x0F));
	}
	
	public static short hash(byte b1, byte b2) {
		int upper = (b1 & 0xFF) << 8;
	    int lower = (b2 & 0xFF);
	    return (short) (upper | lower);
	}
}
