package core;

public enum Direction {
	UP(new int[] {0,-1}),
	DOWN(new int[] {0,1}),
	LEFT(new int[] {-1,0}),
	RIGHT(new int[] {1,0});
	
	private int[] translation;
	
	private Direction(int[] translation) {
		this.translation = translation;
	}
	
	public int[] getTranslation() {
		return translation;
	}
	
	public void translate(int[] original, int modifier) {
		if(translation[0] != 0) {
			original[0] += translation[0] * modifier;
		} else {
			original[1] += translation[1] * modifier;
		}
	}
	
	public static Direction get(int dir) {
		switch(dir) {
		case 0:
			return UP;
		case 1:
			return DOWN;
		case 2:
			return LEFT;
		case 3:
			return RIGHT;
		}
		return null;
	}
}
