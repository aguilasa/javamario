package devforrest.mario.core;

import devforrest.mario.core.animation.Sprite;

public class ScreenState {
	public int xLeft;
	public int xRight;
	public int yTop;
	public int yBottom;
	public int xOffset;
	public int yOffset;
	
	public ScreenState() {
		xLeft = 0;
		xRight = 0;
		yTop = 0;
		yBottom = 0;
		xOffset = 0;
		yOffset = 0;
	}
	
	boolean OnScreen(Sprite s) {
		int x = GameRenderer.pixelsToTiles(s.getX() + xOffset);
		int y = GameRenderer.pixelsToTiles(s.getY() + yOffset);
		
		if(xLeft <= x && xRight >= x && y >= yTop && y <= yBottom) {
			return true;
		}
		return false;
	}
}
