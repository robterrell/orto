package jp.orto.xetris;

import orto.ui.*;

import java.util.Random;

public class Xetris implements ComponentListener {   
	// Map
	private Panel[][] map = new Panel[20][];
	// Falling block
	private Panel[] block = new Panel[4];
	// 8 colors block
	private Panel[] blockMasterPanel = new Panel[8];
	private Panel[] nextBlockMasterPanel = new Panel[8];
	// Next block
	private Panel[] nextBlock = new Panel[4];

	private Panel linePanel,scorePanel,gameOverPanel;

	// Current block state
	private int blockType,blockRotate;
	private int xPosition,yPosition;

	// Next block state
	private int nextBlockType,nextBlockRotate;

	// Score and lines
	private volatile int score = 0;
	private int lines = 0;

	private boolean onGame = false;
	private Random random = new Random();
	private BlockMover blockMover = new BlockMover();

	public Xetris() {
		Body body = Browser.getMainWindow().getBody();
		Panel gamePanel = body.getPanel("gamePanel");
		Panel gameAreaPanel = gamePanel.getPanel("gameArea");
		Panel nextBoxPanel = gamePanel.getPanel("nextBox");
		linePanel = gamePanel.getPanel("lineBox");
		scorePanel = gamePanel.getPanel("scoreBox");
		gameOverPanel = gamePanel.getPanel("gameOver");
		for (int i = 0; i < 8; i++) {
			blockMasterPanel[i] = gameAreaPanel.getPanel("blockMaster" + i);

			// Creates clones of block for the next. However, parent is different.
			nextBlockMasterPanel[i] = nextBoxPanel.createPanel();
			nextBlockMasterPanel[i].setLocation(0, 0);
			nextBlockMasterPanel[i].setHTML(blockMasterPanel[i].getHTML());
		}

		body.getPanel("startUp").setVisible(false);
		gamePanel.setVisible(true);
		onGame = true;

		for (int i = 0; i < 20; i++) {
			map[i] = new Panel[10];
		}

		// FIrst block
		nextBlockType = random.nextInt(7);
		nextBlockRotate = random.nextInt(blockData[nextBlockType][0][0]);
		createBlock();

		Browser.getMainWindow().addKeyDownListener(this);
		blockMover.start();
	}

	/**
	 * Creates new block
	 */
	private void createBlock() {
		xPosition = 5;
		yPosition = -1;

		blockType = nextBlockType;
		blockRotate = nextBlockRotate;

		nextBlockType = random.nextInt(7);
		nextBlockRotate = random.nextInt(blockData[nextBlockType][0][0]);

		for (int i = 0; i < 4; i++) {
			// Creates falling blocks.
			block[i] = blockMasterPanel[blockType].clone();

			if (nextBlock[i] != null) {
				nextBlock[i].destroy();
			}
			nextBlock[i] = nextBlockMasterPanel[nextBlockType].clone();
		}
		// Draws nextBox in proper state.
		drawBlock(nextBlock, nextBlockType, 2, 1, nextBlockRotate);

		// Checks whether the created blocks can put.
		// If cannot, the game is over.
		if (!moveBlock(0, 1, 4)) {
			gameOver();
		}
	}

	private void gameOver() {
		for (int i = 19; i >= 0; i--) {
			for (int x = 0; x < 10; x++) {
				if (map[i][x] != null) {
					map[i][x].setHTML(blockMasterPanel[7].getHTML());
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		// Display the "Game Over" words top most.
		gameOverPanel.setZIndex(1);
		gameOverPanel.setVisible(true);
	}

	private void drawBlock(Panel[] drawBlock, int drawBlockType, int x, int y, int rotate) {
		int reverseX = 0,reverseY = 0,right = 0;

		switch (rotate) {
			case 0:
				reverseX = 1;
				reverseY = 1;
				right = 0;
				break;
			case 1:
				reverseX = -1;
				reverseY = 1;
				right = 1;
				break;
			case 2:
				reverseX = -1;
				reverseY = -1;
				right = 0;
				break;
			case 3:
				reverseX = 1;
				reverseY = -1;
				right = 1;
				break;
		}
		for (int i = 0; i < 4; i++) {
			int drawX = x,drawY = y;
			if (i != 0) {
				drawX += reverseX * blockData[drawBlockType][i][right];
				drawY += reverseY * blockData[drawBlockType][i][1 - right];
			}
			drawBlock[i].setLocation(drawX * 16, drawY * 16);
			if (drawY < 0) {
				drawBlock[i].setVisible(false);
			} else {
				drawBlock[i].setVisible(true);
			}
		}
	}

	/**
	 * Moves the block for dx and dy and rotate r times.
	 * @return
	 */
	private synchronized boolean moveBlock(int dx, int dy, int r) {
		if (!onGame) {
			return false;
		}

		// Chages the parameter from rotate state.
		int reverseX = 0,reverseY = 0,right = 0;
		int rotate = (r + blockRotate) % blockData[blockType][0][0];
		switch (rotate) {
			case 0:
				reverseX = 1;
				reverseY = 1;
				right = 0;
				break;
			case 1:
				reverseX = -1;
				reverseY = 1;
				right = 1;
				break;
			case 2:
				reverseX = -1;
				reverseY = -1;
				right = 0;
				break;
			case 3:
				reverseX = 1;
				reverseY = -1;
				right = 1;
				break;
		}

		for (int i = 0; i < 4; i++) {
			// Checks whether 4 blocks doesn't hit to anywhere.
			int newX = xPosition + dx,newY = yPosition + dy;
			if (i != 0) {
				newX += reverseX * blockData[blockType][i][right];
				newY += reverseY * blockData[blockType][i][1 - right];
			}
			if (newY >= 0 && (newX < 0 || newX >= 10 || newY >= 20 || map[newY][newX] != null)) {
				// Hit
				blockMover.moveDownCount = blockMover.moveXDirectionCount =
						blockMover.rotateCount = 0;

				if (dy == 1) {
					// Fixs blocks to its place, if hits in falling.
					for (int j = 0; j < 4; j++) {
						int setX = xPosition,setY = yPosition;
						if (j != 0) {
							setX += reverseX * blockData[blockType][j][right];
							setY += reverseY * blockData[blockType][j][1 - right];
						}
						if (setY >= 0) {
							block[j].setLocation(setX * 16, setY * 16);
							block[j].setVisible(true);
							map[setY][setX] = block[j];
						}
					}

					// Erases lines.
					checkLine(yPosition);

					if (r == 4) {
						// If hit to somewhere when blocks are just created, it is game over.
						onGame = false;
					} else {
						// Creaet next blocks.
						createBlock();
					}
				}
				return false;
			}
		}
		// It is not hitted, so fix the movement.

		xPosition += dx;
		yPosition += dy;
		blockRotate = rotate;
		drawBlock(block, blockType, xPosition, yPosition, blockRotate);

		return true;
	}

	/**
	 * Checks for erasable line from 2 lines upper than line y to 2 line below line y.
	 * @param y
	 */
	private void checkLine(int y) {
		boolean hasShownMatchedLine = false;

		// Convert y to 2 - 17.
		if (y < 2) {
			y = 2;
		}
		if (y > 17) {
			y = 17;
		}

		int lines = 0;

		scan:
			for (int j = y - 2; j <= y + 2; j++) {
				for (int i = 0; i < 10; i++) {
					// Checks whether line is filled.
					if (map[j][i] == null) {
						continue scan;
					}
				}

				// Show the user that there is a line to delete.
				if (!hasShownMatchedLine) {
					hasShownMatchedLine = true;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}

				// Erases the line
				for (int i = 0; i < 10; i++) {
					map[j][i].destroy();
				}

				for (int k = j; k > 0; k--) {
					if (k != j) {
						// Drop the line
						for (int n = 0; n < 10; n++) {
							if (map[k][n] != null) {
								map[k][n].setLocation(n * 16, (k + 1) * 16);
							}
						}
					}
					// Replace the line to upper line.
					map[k] = map[k - 1];
				}
				// Creates the topmost line.
				map[0] = new Panel[10];
				lines++;
			}

		// Add scores.
		switch (lines) {
			case 1:
				score += 40;
				break;
			case 2:
				score += 100;
				break;
			case 3:
				score += 300;
				break;
			case 4:
				score += 1200;
				break;
		}
		this.lines += lines;
		linePanel.setHTML(String.valueOf(this.lines));
		scorePanel.setHTML(String.valueOf(score));
	}

	public boolean componentAction(Component source, int type, int param) {
		if (onGame && type == ComponentListener.ON_KEYDOWN) {
			switch (param) {
				case 37:
				case 'J':
				case 100:	// Ten keys
				case 57375: // Opera 7
					blockMover.moveXDirectionCount--;
					return false;
				case 38:
				case 'K':
				case 101:
				case 57373:
					blockMover.rotateCount++;
					return false;
				case 39:
				case 'L':
				case 102:
				case 57376:
					blockMover.moveXDirectionCount++;
					return false;
				case 40:
				case 'M':
				case 98:
				case 57374:
					blockMover.moveDownCount++;
					return false;
			}
		}
		return true;
	}

	private class BlockMover extends Thread {
		int moveXDirectionCount = 0;
		int rotateCount = 0;
		int moveDownCount = 0;

		public void run() {
			int dropWaitCounter = 0;
			while (onGame) {
				// Block movement on users key action.
				if (moveXDirectionCount < 0) {
					moveXDirectionCount++;
					moveBlock(-1, 0, 0);
				} else if (moveXDirectionCount > 0) {
					moveXDirectionCount--;
					moveBlock(1, 0, 0);
				}
				if (rotateCount > 0) {
					rotateCount = Math.min(rotateCount - 1, 3);
					moveBlock(0, 0, 1); // just rotate
				}
				if (moveDownCount > 0) {
					moveDownCount = 0;
					if (moveBlock(0, 1, 0))
						score += 2;
				}

				// Normal dropping. This is done only once for 4 times.
				dropWaitCounter = (dropWaitCounter + 1) % 4;
				if (dropWaitCounter == 0) {
					moveBlock(0, 1, 0);
				}

				try {
					Thread.sleep(75);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	// Block data
	private final int blockData[][][] =
			{
				// Relative positions of other 3 blocks. The origin is the rotate center block.
				// {Rotate patter},{x,y}
				{
					// Line
					{2}, {0, -1}, {0, 1}, {0, 2}
				},
				{
					// L style 1
					{4}, {1, -1}, {0, -1}, {0, 1}
				},
				{
					// L style 2
					{4}, {-1, -1}, {0, -1}, {0, 1}
				},
				{
					// Key 1
					{2}, {-1, 1}, {-1, 0}, {0, -1}
				},
				{
					// Key 2
					{2}, {0, 1}, {-1, 0}, {-1, -1}
				},
				{
					// T Style
					{4}, {0, -1}, {1, 0}, {0, 1}
				},
				{
					// Square
					{1}, {0, 1}, {-1, 0}, {-1, 1}
				},
			};
}
