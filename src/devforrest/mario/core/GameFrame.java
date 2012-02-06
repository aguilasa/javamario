package devforrest.mario.core;

import javax.swing.JFrame;

public class GameFrame extends JFrame {
	
	public GameFrame() {		
		int w = 400;
		int h = 380;       
		setSize(w, h);
		setResizable(false);
		setTitle("Game Frame"); 
		GamePanel panel = new GamePanel(w, h);
		add(panel);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);	
	}
	
	public static void main(String[] args) {
		new GameFrame();
	}

}
