package devforrest.mario.objects.mario;


import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;

import devforrest.mario.core.GameRenderer;
import devforrest.mario.core.animation.Animation;
import devforrest.mario.core.animation.CollidableObject;
import devforrest.mario.core.sound.specific.copy.MarioSoundManager10512Hz;
import devforrest.mario.core.sound.specific.copy.MarioSoundManager22050Hz;
import devforrest.mario.core.tile.GameTile;
import devforrest.mario.core.tile.TileMap;
import devforrest.mario.objects.base.Collision;
import devforrest.mario.objects.base.Creature;
import devforrest.mario.util.ImageManipulator;





/**
 * Mario is the main object in the game and is the center of the screen and attention at all
 * time. As a result, he is also the most complicated object in terms of animation, collision detection,
 * user input etc. 
 */

public class Mario extends CollidableObject{
	
	/* Static Constant Fields.
	 * Gravity:   Effects the amount of pull objects feel toward the ground. pixels/ms
	 * Friction:  Effects the amount of sliding an object displays before coming to a stop.
	 * S_X:       Starting X position of Mario.
	 * S_Y:       Starting Y position of Mario.
	 * S_DY:      Starting Dy of Mario.
	 * S_JH:      Effects the height of Mario's first jump.
	 * Anim_Time: The time between each of Mario's Animations. 
	 * 
	 * Terminal_Walking_Dx:  Max speed when Mario is walking.
	 * Terminal_R3unning_Dx:  Max speed when Mario is running.
	 * Terminal_Fall_Dy:     Max speed Mario can fall at.
	 * Walking_Dx_Inc:       The increase in speed per update when walking until terminal runnning is reached.
	 * Running_Dx_Inc:       The increase in speed per update when running until terminal walking is reached.
	 * Start_Run_Anim_Thres: The speed where mario switches to the running animation.
	 */

	public static final float GRAVITY = 0.0008f;
	public static final float FRICTION = 0.0004f;                   
	private static final int STARTING_X = 25;
	private static final int STARTING_Y = 140;
	private static final float STARTING_DY = .03f;
	private static final float INITIAL_JUMP_HEIGHT = -.34f; 
	private static final float JUMP_MULTIPLIER = .46f;
	private static final float TERMINAL_WALKING_DX = .10f;
	private static final float WALKING_DX_INC = .01f;
	private static final float TERMINAL_RUNNING_DX = .21f;
	private static final float START_RUN_ANIM_THRESHOLD = .2f;
	private static final float RUNNING_DX_INC = .001f;
	private static final float TERMINAL_FALL_DY = .22f;
	private static final int ANIM_TIME = 125;
	
	/* INITIAL_JUMP_HEIGHT + dx*JUMP_MULTIPLIER */
	private float jumpHeight; 
	
	/* Boolean variables used to identify which keys are pressed. */
	private boolean isDownHeld, isRightHeld, isLeftHeld, isShiftHeld, isSpaceHeld, isUpHeld;
	/* Boolean variables used to identify where Mario is with respect to Slopes. */
	private boolean isUpSlope, isDownSlope, onSlopedTile;
	/* Boolean variables used to identify the state of Mario. */
	private boolean isJumping, frictionLock, isInvisible;
	
	/* Animation variables. */
	private Animation walkLeft, runLeft, stillLeft, jumpLeft, crouchLeft, changeLeft, currLeftAnim;
	private Animation walkRight, runRight, stillRight, jumpRight, crouchRight, changeRight, currRightAnim;
	
	private MarioSoundManager10512Hz soundManager2;
	
	public Mario(MarioSoundManager22050Hz soundManager) {
		
		super(STARTING_X, STARTING_Y, soundManager);
		
		setIsJumping(true);
		dy = 0;
		jumpHeight = INITIAL_JUMP_HEIGHT;
		soundManager2 = new MarioSoundManager10512Hz(new AudioFormat(10512, 8, 1, true, true));
		
		BufferedImage[] l = { ImageManipulator.loadImage("mario/Mario_Big_Left_Still.png"), ImageManipulator.loadImage("mario/Mario_Big_Left_1.png"),
				ImageManipulator.loadImage("mario/Mario_Big_Left_2.png"), ImageManipulator.loadImage("mario/Mario_Big_Left_Run_1.png"),
				ImageManipulator.loadImage("mario/Mario_Big_Left_Run_2.png"), ImageManipulator.loadImage("mario/Mario_Big_Crouch_Left.png"),
				ImageManipulator.loadImage("mario/Mario_Big_Jump_Left.png"), ImageManipulator.loadImage("mario/Mario_Big_Change_Direction_Left.png") };
		
		BufferedImage[] r = { null, null, null, null, null, null, null, null };
		for(int i = 0; i < l.length; i++) {
			r[i] = ImageManipulator.horizontalFlip(l[i]); // Flip every image in l.
		}
				
		// Create left animations.
    	stillLeft = new Animation(ANIM_TIME).addFrame(l[0]);
		walkLeft = new Animation(ANIM_TIME).addFrame(l[1]).addFrame(l[2]);
		runLeft = new Animation(ANIM_TIME - 30).addFrame(l[3]).addFrame(l[4]);
		crouchLeft = new Animation(ANIM_TIME).addFrame(l[5]);
		jumpLeft = new Animation(ANIM_TIME).addFrame(l[6]);
		changeLeft = new Animation(ANIM_TIME).addFrame(l[7]);
		
		// Create right animations.
		stillRight = new Animation(ANIM_TIME).addFrame(r[0]);
		walkRight = new Animation(ANIM_TIME).addFrame(r[1]).addFrame(r[2]);
		runRight = new Animation(ANIM_TIME - 30).addFrame(r[3]).addFrame(r[4]);
		crouchRight = new Animation(ANIM_TIME).addFrame(r[5]);
		jumpRight = new Animation(ANIM_TIME).addFrame(r[6]);
		changeRight = new Animation(ANIM_TIME).addFrame(r[7]);
		
		setAnimation(stillRight);
		currLeftAnim = walkLeft;
		currRightAnim = walkRight;
	}
	
	public void setIsJumping(boolean isJumping) { this.isJumping = isJumping; }

	public boolean isJumping() { return isJumping; }
	
	private void slowSpeed(int slowFactor) { setdX(getdX()/slowFactor);	}
	
	private void accelerateFall() { setdY(-getdY()/4); }
	
	/**
	 * Fixes Y movement on tiles and platforms where animation height changes by setting the mario's y
	 * value to the difference between animation heights. 
	 */
	public void setAnimation(Animation newAnim) {
		if(currentAnimation() != null) {
			Animation currAnim = currentAnimation();
			int oldHeight = currAnim.getHeight();
			int newHeight = newAnim.getHeight();
			if(newHeight > oldHeight) {
				setY(getY() - (newHeight - oldHeight));	
			} else if(oldHeight > newHeight) {
				setY(getY() + oldHeight - newHeight);
			}
		}
		super.setAnimation(newAnim);
	}
	
	public void update(TileMap map, float time) {
		
		if (isLeftHeld && !isShiftHeld) {
			dx = -TERMINAL_WALKING_DX;
		} else if (isRightHeld && !isShiftHeld) {
			dx = TERMINAL_WALKING_DX;
		} else if (isLeftHeld && isShiftHeld) {
			dx = -TERMINAL_RUNNING_DX;
		} else if (isRightHeld && isShiftHeld) {
			dx = TERMINAL_RUNNING_DX;
		} else {
			dx = 0;
		}
		
		dy = 0;
		if (isDownHeld) {
			dy += TERMINAL_WALKING_DX;
		}
		
		if (isUpHeld) {
			dy -= TERMINAL_WALKING_DX;
		}
		
		
		
		// Calculate the new X position.
		float oldX = getX();
		float newXCalc = oldX + getdX()*time;
		
		// Calculate the new Y position.
		float oldY = getY();
		float newYCalc = oldY + getdY()*time;
		
		// Calculate all the tile collisions.
		ArrayList<Point> xTile = GameRenderer.getTileCollisionAll(map, this, getX(), getY(), newXCalc, getY());
		ArrayList<Point> yTile = GameRenderer.getTileCollisionAll(map, this, getX(), getY(), getX(), newYCalc); 
		int numOfXTiles = xTile.size();
		int numOfYTiles = yTile.size();

		// Manage collision in the X direction.
		if(oldX < 0) { // Collision with left side of map.
			setX(GameRenderer.tilesToPixels(0));
		} else if(oldX > GameRenderer.tilesToPixels(map.getWidth()) - 21) { // Collision with right side of map.
			setX(GameRenderer.tilesToPixels(map.getWidth()) - 21);
		} else {
			if(numOfXTiles == 0) { // No tile collision in the X direction
				setX(newXCalc);
			} else if(numOfXTiles >= 1) { // Tile collision in the X direction. For now, only worry
										  // about the first tile being collided with.
				
				Point xtp = xTile.get(0); // xTilePoint
				Collision c = Creature.tileCollisionX(map.getTile(xtp.x, xtp.y), this);
				if(c == Collision.EAST) { // Left of a tile.
					setX(GameRenderer.tilesToPixels(xtp.x) - getWidth());
				} else if(c == Collision.WEST) { // Right of a tile.
					setX(GameRenderer.tilesToPixels(xtp.x + 1));
				}
				setdX(0);
			}
		}
		
		super.update((int) time); // Update mario's animation.
		
		// Manage collision in the Y direction. 
		if(oldY > GameRenderer.tilesToPixels(map.getHeight()) - getHeight()) { // Off the bottom of the map.
			System.out.println("Mario has died.");
		} else { // No Y collision, allow Y position to update uninterrupted.
			if(numOfYTiles == 0) {
				setY(newYCalc);
			} else if(numOfYTiles >= 1) { // Y collision detected with a tile 
				Point ytp = yTile.get(0); // yTilePoint
				Collision c = Creature.tileCollisionY(map.getTile(ytp.x, ytp.y), this);
				if(c == Collision.NORTH) { // Downward collision with tile.
					setY(GameRenderer.tilesToPixels(ytp.y) - getHeight()); 
				} else if (c == Collision.SOUTH) { // Upward collision with tile.
					for(Point p : yTile) {
						GameTile tile = map.getTile(p.x, p.y);
						if(tile != null) { tile.doAction(); }
					}
					setY(GameRenderer.tilesToPixels(ytp.y + 1));
					soundManager.playBump();
				}
			}
		}
	}

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
    		isLeftHeld = true;
        }

        if(key == KeyEvent.VK_RIGHT) {
    		isRightHeld = true;
        }
        
        if(key == KeyEvent.VK_SHIFT) {
        	isShiftHeld = true;
        }
      
        if(key == KeyEvent.VK_DOWN) {
        	isDownHeld = true;
        }
        
        if(key == KeyEvent.VK_SPACE) {	
        	isSpaceHeld = true;
        }
        
        if(key == KeyEvent.VK_UP) {	
        	isUpHeld = true;
        }
        
    }
    
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if(key == KeyEvent.VK_UP) {
        	isUpHeld = false;
        }
        
        if (key == KeyEvent.VK_LEFT) {
        	isLeftHeld = false;
        }

        if(key == KeyEvent.VK_RIGHT) {
        	isRightHeld = false;
        }
        
        if(key == KeyEvent.VK_SHIFT) {
        	isShiftHeld = false;
        }
        
        // responsible for jumps of different heights
        if(key == KeyEvent.VK_SPACE) {
        	isSpaceHeld = false;
        }

        if(key == KeyEvent.VK_DOWN) {
        	isDownHeld = false;
        }
    }
}

