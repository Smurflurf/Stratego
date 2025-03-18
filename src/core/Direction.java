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
}
