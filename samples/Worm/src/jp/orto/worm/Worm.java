package jp.orto.worm;

import orto.ui.*;

import java.util.*;

public class Worm implements ComponentListener {
	private Vector wormList = new Vector();
	private int xMoveDirection = 0;
	private int yMoveDirection = 1;
	private Random random = new Random();
	private int foodX, foodY;
	private Panel foodPanel;
	private Panel scorePanel;
    private int point = 0;

	public Worm() {
		// Make a worm part.
        for(int i=4; i>=0; i--) {
			wormList.addElement(new WormPart(10-i, 5));
		}

		// Puts food
		foodX = random.nextInt(19) + 1;
		foodY = random.nextInt(19) + 1;
		Window mainWindow = Browser.getMainWindow();
		foodPanel = mainWindow.getBody().createPanel();
		foodPanel.setBackgroundColor("blue");
		foodPanel.setSize(16, 16);
		foodPanel.setLocation(foodX * 16, foodY * 16);
		foodPanel.setVisible(true);

		// Sets the score panel.
		scorePanel = mainWindow.getBody().getPanel("scorePanel");
		scorePanel.setHTML("Score : " + point);

		mainWindow.addKeyDownListener(this);

		new WormMover();
	}

	public boolean componentAction(Component source, int type, int param) {
		if(type == ComponentListener.ON_KEYDOWN) {
			switch(param) {
				case 37:
				case 'J':
				case 57375: // Opera 7
					xMoveDirection = -1;
					yMoveDirection = 0;
					break;
				case 38:
				case 'I':
				case 57373:
					xMoveDirection = 0;
					yMoveDirection = -1;
					break;
				case 39:
				case 'L':
				case 57376:
					xMoveDirection = 1;
					yMoveDirection = 0;
					break;
				case 40:
				case 'M':
				case 57374:
					xMoveDirection = 0;
					yMoveDirection = 1;
					break;
			}
		}
		return false;
	}

	// Move the worm
	private class WormMover extends Thread {
		private boolean isFoodEated = false;

		WormMover() {
            start();
		}

		public void run() {
            while(true) {
				try {
					sleep(100);
				} catch (InterruptedException e) {
					return;
				}

                synchronized(wormList) {
					WormPart newHead = (WormPart)wormList.elementAt(0);
					if(!isFoodEated) {
						wormList.removeElementAt(0);
					} else {
						isFoodEated = false;
						newHead = new WormPart(newHead.x, newHead.y);
					}

					WormPart prevHead = (WormPart)wormList.lastElement();
					newHead.x = prevHead.x + xMoveDirection;
					newHead.y = prevHead.y + yMoveDirection;
					wormList.addElement(newHead);

                    newHead.panel.setLocation(newHead.x * 16, newHead.y * 16);

                    // Checks if he hits the wall.
					if(newHead.x <=0 || newHead.x >= 20 || newHead.y <=0 || newHead.y >= 20)
						return;
					// Checks if he hits himself.
					for(int i=0; i<wormList.size()-1; i++) {
						WormPart part = (WormPart)wormList.elementAt(i);
						if(part.x == newHead.x && part.y == newHead.y)
							return;
					}

					// When he hits the food.
					if(newHead.x == foodX && newHead.y == foodY) {
                        isFoodEated = true;
						foodX = random.nextInt(19) + 1;
						foodY = random.nextInt(19) + 1;
						foodPanel.setLocation(foodX * 16, foodY * 16);

						scorePanel.setHTML("Score : " + (++point));
					}
				}
			}
		}
	}

	// Part of worm
	private class WormPart {
		int x;
		int y;
		Panel panel;

		WormPart(int x, int y) {
			this.x = x;
			this.y = y;

			panel = Browser.getMainWindow().getBody().createPanel();
			panel.setLocation(x*16, y*16);
			panel.setSize(16, 16);
			panel.setBackgroundColor("black");
			panel.setVisible(true);
		}
	}
}
