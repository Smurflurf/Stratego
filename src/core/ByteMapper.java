package core;

/**
 * Maps x,y coordinates onto bytes and shorts.
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
	
	/**
	 * Position b1 takes the first 8 bit in a short, b2 the later, to generate a unique hash.
	 * They get "glued" together
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static short hash(byte b1, byte b2) {
	    return (short) (((b1 & 0xFF) << 8) | ((b2 & 0xFF)));
	}
	
	/**
	 * s does not represent a short as {@link #hashCode()}, it contains a field representation x,y.
	 * 8 left bit represent x, 8 right bit represent y.
	 * 00000001 is 1, 00000010 is 2, 00000100 is 3, ...
	 * @param s short to add the coordinate
	 * @param b byte coordinate to add to s
	 * @return s with a new coordinate added
	 */
	public static short add(short s, byte b) {
		return (short) (s 
				| 1 << getX(b) << 8 		// generate x, shift to 8 left bit
				| 1 << getY(b));			// generate y
	}
	
	/**
	 * Checks if a with {@link #add(short, byte)} altered short contains a bit coordinate b
	 * @param s short to check in
	 * @param b coordinate to check for
	 * @return true if b occurs in s
	 */
	public static boolean contains(short s, byte b) {
		int pos = 
				(1 << getX(b) << 8			// generate x, shift to 8 left bit
				| 1 << getY(b))				// generate y
				^ 0xFFFF;					// flip bits
		return (short)(s | pos) == -1;
	}
}
