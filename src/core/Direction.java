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
	
	/**
	 * Modifies original by modifier in {@link #Direction(int[], int)}
	 * @param original unmodified x,y coordinates
	 * @param modifier fields to go into {@link #Direction(int[], int)}
	 * @return modified position represented as an int[x,y] array
	 */
	public int[] translate(int[] original, int modifier) {
		if(translation[0] != 0) {
			original[0] += translation[0] * modifier;
		} else {
			original[1] += translation[1] * modifier;
		}
		return original;
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
	
	public static Direction get(int xFrom, int xTo, int yFrom, int yTo) {
		int i = xFrom - xTo;
		if(i > 0)
			return LEFT;
		else if(i < 0)
			return RIGHT;
		i = yFrom - yTo;
		if(i > 0)
			return UP;
		else if(i < 0)
			return DOWN;
		return UP;
	}
}
