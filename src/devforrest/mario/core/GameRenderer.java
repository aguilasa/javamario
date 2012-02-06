package devforrest.mario.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

import devforrest.mario.core.animation.Sprite;
import devforrest.mario.core.tile.GameTile;
import devforrest.mario.core.tile.Tile;
import devforrest.mario.core.tile.TileMap;
import devforrest.mario.objects.base.Creature;
import devforrest.mario.objects.creatures.Platform;
import devforrest.mario.objects.mario.Mario;


// The TileMapRenderer class is responsible for all the drawing onto the screen.
// Also contains useful static methods for converting tiles->pixels, pixels->tiles
// and a method for locating which tile a sprite has collided with.

public class GameRenderer {
	
	// AdjustYScroll is used to record the previous value of player.getY(). This way I can 
	// continue to draw on the same y level if there is no significant change in Y. I use 
	// the player jumping as a measure of significant change. Hides errors in my animations, 
	// keeping the screen from bobbing when there is a change in height of the player animation. 
	private int AdjustYScroll = 0;

	// the size in bits of the tile
    private static final int TILE_SIZE = 16;
    // Math.pow(2, TILE_SIZE_BITS) == TILE_SIZE
    private static final int TILE_SIZE_BITS = 4;

    private Image background;

    // Converts a pixel position to a tile position.
    public static int pixelsToTiles(float pixels) {
        return pixelsToTiles(Math.round(pixels));
    }

    // Converts a pixel position to a tile position.
    public static int pixelsToTiles(int pixels) {
        // use shifting to get correct values for negative pixels
        return pixels >> TILE_SIZE_BITS;
        // or, for tile sizes that aren't a power of two,
        // use the floor function: return (int)Math.floor((float)pixels / TILE_SIZE);
    }

    // Converts a tile position to a pixel position.
    public static int tilesToPixels(int numTiles) {
        // no real reason to use shifting here. it's slighty faster, but doesn't add up to much
        // on modern processors.
        return numTiles << TILE_SIZE_BITS;
        // use this if the tile size isn't a power of 2:
        //return numTiles * TILE_SIZE;
    }

    // Sets the background to draw.
    public void setBackground(BufferedImage background) {
        this.background = background;
    }
    
	// Returns the tile that a Sprite has collided with. Returns null if no 
	// collision was detected. The last parameter, right, is used to check if multiple blocks
	// are hit when a sprite jumps.
	public static Point getTileCollision(TileMap map, Sprite sprite, float currX, float currY, float newX, float newY) {

	    float fromX = Math.min(currX, newX);
	    float fromY = Math.min(currY, newY);
	    float toX = Math.max(currX, newX);
	    float toY = Math.max(currY, newY);
	
	    // get the tile locations
	    int fromTileX = GameRenderer.pixelsToTiles(fromX);
	    int fromTileY = GameRenderer.pixelsToTiles(fromY);
	    int toTileX = GameRenderer.pixelsToTiles(toX + sprite.getWidth() - 1);
	    int toTileY = GameRenderer.pixelsToTiles(toY + sprite.getHeight() - 1);
	
	    // check each tile for a collision
	    for (int x=fromTileX; x<=toTileX; x++) {
	        for (int y=fromTileY; y<=toTileY; y++) {
	            if (x < 0 || x >= map.getWidth() || map.getImage(x, y) != null) {
	            	Tile tile = map.getTile(x,y);
	            	if(tile != null && map.getTile(x, y).isCollidable()) {
	                // collision found and the tile is collidable, return the tile
	            		return new Point(x,y);
	            	} 
	            }
	        }
	    }
	    // no collision found, return null
	    return null;
	}
	
	/**
	 * @return A List of Points, where each Point corresponds to the location of a tile the sprite is 
	 * colliding with in map.tiles().
	 */
	public static ArrayList<Point> getTileCollisionAll(TileMap map, Sprite sprite, float currX, float currY, float newX, float newY) {
		
		ArrayList<Point> collisionPoints = new ArrayList<Point>(); 
	    float fromX = Math.min(currX, newX);
	    float fromY = Math.min(currY, newY);
	    float toX = Math.max(currX, newX);
	    float toY = Math.max(currY, newY);
	
	    // get the tile locations
	    int fromTileX = GameRenderer.pixelsToTiles(fromX);
	    int fromTileY = GameRenderer.pixelsToTiles(fromY);
	    int toTileX = GameRenderer.pixelsToTiles(toX + sprite.getWidth() - 1);
	    int toTileY = GameRenderer.pixelsToTiles(toY + sprite.getHeight() - 1);
	
	    // check each tile for a collision
	    for (int x=fromTileX; x<=toTileX; x++) {
	        for (int y=fromTileY; y<=toTileY; y++) {
	            if (x < 0 || x >= map.getWidth() || map.getImage(x, y) != null) {
	            	Tile tile = map.getTile(x,y);
	            	if(tile != null && map.getTile(x, y).isCollidable()) {
	                // collision found and the tile is collidable, return the tile
	            		collisionPoints.add(new Point(x,y));
	            	} 
	            }
	        }
	    }
	    // no collision found, return null
	    return collisionPoints;
	}
    
    /**
     * Draws all game elements. I did the best I can to seperate all updating from drawing. However, it 
     * seems its much more efficient to do some updating here where I have all the information I need
     * to make important decisions. So calling draw() DOES change the game state.
     */
    public void draw(Graphics2D g, TileMap mainMap, int screenWidth, int screenHeight) {
    	
    	// add the three maps to the list of maps to draw, only mainMap is interactive
        Mario player = mainMap.getPlayer();
        int mapWidth = tilesToPixels(mainMap.getWidth());
        int mapHeight = tilesToPixels(mainMap.getHeight());
        
        // get the scrolling position of the map based on player's position...
        
        int offsetX = screenWidth/2 - Math.round(player.getX()) - TILE_SIZE;
        offsetX = Math.min(offsetX, 0); // if this gets set to 0, player is within a screen width
        offsetX = Math.max(offsetX, screenWidth - mapWidth);
        
        int round = Math.round(player.getY());
        
        // initialize AdjustYScroll
        if (AdjustYScroll == 0) {
        	AdjustYScroll = round;
        }
        
        // if the player is jumping, change the level at which the screen is drawn.
        if(player.isJumping()) {
        	AdjustYScroll = round;
        }
        
        int offsetY = screenHeight/2 - AdjustYScroll - TILE_SIZE;
        offsetY = Math.min(offsetY, 0);
        offsetY = Math.max(offsetY, screenHeight - mapHeight - 25); // 25 fixs the JPanel height error

        // draw parallax background image
        if (background != null) {
        	// x and y are responsible for fitting the background image to the size of the map
            int x = offsetX * (screenWidth - background.getWidth(null)) / (screenWidth - mapWidth);
            int y = offsetY * (screenHeight - background.getHeight(null)) / (screenHeight - mapHeight);
            g.drawImage(background, x, y, null);
        }


        int firstTileX = pixelsToTiles(-offsetX);
        int lastTileX = firstTileX + pixelsToTiles(screenWidth) + 1;
        int firstTileY = pixelsToTiles(-offsetY);
        int lastTileY = firstTileY + pixelsToTiles(screenHeight) + 1;
        
        mainMap.screenState.xLeft = firstTileX;
        mainMap.screenState.xRight = lastTileX;
        mainMap.screenState.yTop = firstTileY;
        mainMap.screenState.yBottom = lastTileY;
        mainMap.screenState.xOffset = offsetX;
        mainMap.screenState.yOffset = offsetY;
      
        // draw the visible tiles
    	if(mainMap != null) {
    		for (int y=firstTileY; y<= lastTileY; y++) {
                for (int x=firstTileX; x <= lastTileX; x++) {
                	GameTile tile = mainMap.getTile(x, y);
    	            if(tile != null) {
    	            	tile.draw(g, tilesToPixels(x), tilesToPixels(y), 
    	            			tile.getOffsetX() + offsetX, tile.getOffsetY() + offsetY);
                    }
                }
            }
    	}
        	
        player.draw(g, Math.round(player.getX()), Math.round(player.getY()),
        		player.getOffsetX() +offsetX , player.getOffsetY() + offsetY);
        
        for(Creature c : mainMap.creatures()) {
        	c.draw(g, (int) c.getX() + offsetX, (int) c.getY() + offsetY, (int) c.getOffsetX(), (int) c.getOffsetY());
        }      
    }
}
