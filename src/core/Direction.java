package core;

public enum Direction {
	UP(new int[] {0,-1}, -1),
	DOWN(new int[] {0,1}, 1),
	LEFT(new int[] {-1,0}, -1),
	RIGHT(new int[] {1,0}, 1);
	
	private int[] translation;
	private int oneDimTranslation;
	
	private Direction(int[] translation, int oneDimTranslation) {
		this.translation = translation;
		this.oneDimTranslation = oneDimTranslation;
	}
	
	public int[] getTranslation() {
		return translation;
	}
	
	public int getOneDimTranslation() {
		return oneDimTranslation;
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
