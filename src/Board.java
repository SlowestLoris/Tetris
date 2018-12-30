/*
 * Raymond Zhang
 * Mr. Benum
 * ICS4UE
 * December 12, 2018
 */
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;
import java.io.*;

public class Board extends JPanel implements ActionListener {
	//fields
	private final int BOARD_WIDTH = 10, BOARD_HEIGHT = 24;
	private Timer timer;
	private boolean isFallingFinished = false, isStarted = false, isPaused = false;
	private int curScore = 0, curX = 0, curY = 0, curms, delay = 400;
	private Tetris parent;
	private JLabel statusbar;
	private BlockHolder placeholder;
	private Block curPiece, blockHeld, nextBlock;
	private Block.Shape[] board;
	private Scanner sc;

	//constructors
	public Board(Tetris parent) {
		initBoard(parent);
	}

	//methods
	private void initBoard(Tetris parent) {
		setFocusable(true);
		curPiece = new Block();
		blockHeld = new Block();
		nextBlock = new Block();
		nextBlock.setRandomShape();

		timer = new Timer(delay, this);
		timer.start();

		this.parent = parent;
		statusbar =  parent.getStatusBar();
		placeholder = parent.getBlockHolder();
		placeholder.updateNextBlock(nextBlock);
		board = new Block.Shape[BOARD_WIDTH * BOARD_HEIGHT];
		addKeyListener(new TAdapter());
		clearBoard();
		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(350,840));

		try {
			sc = new Scanner(new File("src/resources/highscores.txt"));
		} catch (Exception e) {System.out.println(e);};
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (isFallingFinished) {
			isFallingFinished = false;
			newPiece();
		} else oneLineDown();
		curms += 400;
		if(curms >= 20000 && delay > 100) {
			curms = 0;
			delay -= 50;
			timer.setDelay(delay);
		}
	}

	private int squareWidth() { return (int) getSize().getWidth() / BOARD_WIDTH; }
	private int squareHeight() { return (int) getSize().getHeight() / BOARD_HEIGHT; }
	private Block.Shape shapeAt(int x, int y) { return board[(y * BOARD_WIDTH) + x]; }


	public void start()  {
		if (isPaused)
			return;

		isStarted = true;
		isFallingFinished = false;
		curScore = 0;
		clearBoard();

		newPiece();
		timer.start();
	}

	private void pause()  {
		if (!isStarted) return;

		isPaused = !isPaused;

		if (isPaused) {
			timer.stop();
			statusbar.setText("paused");
		} else {
			timer.start();
			statusbar.setText("Score: " + curScore);
		}

		repaint();
	}

	private void draw(Graphics g) {
		Dimension size = getSize();
		int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();

		for (int i=0;i<BOARD_HEIGHT;i++) {
			for (int j=0;j<BOARD_WIDTH;j++) {
				Block.Shape shape = shapeAt(j, BOARD_HEIGHT - i - 1);

				if (shape != Block.Shape.NoShape) drawSquare(g, 0 + j * squareWidth(), boardTop + i * squareHeight(), shape);
			}
		}
		ArrayList<Integer> xs = new ArrayList<Integer>();
		if (curPiece.getShape() != Block.Shape.NoShape) {
			for (int i=0;i<4;i++) {
				int x = curX + curPiece.x(i);
				if(!xs.contains(x)) xs.add(x);
			}
			for(int i=0;i<xs.size();i++) drawIndicator(g, 0+xs.get(i)*squareWidth());
			for (int i=0;i<4;i++) {
				int x = curX + curPiece.x(i);
				int y = curY - curPiece.y(i);
				drawSquare(g, 0 + x * squareWidth(), boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(), curPiece.getShape());
			}
		}        
	}

	@Override
	public void paintComponent(Graphics g) { 
		super.paintComponent(g);
		draw(g);
	}

	private void dropDown() {
		int newY = curY;
		while (newY > 0) {
			if (!tryMove(curPiece, curX, newY - 1)) break;
			--newY;
		}
		blockDropped();
	}

	private void oneLineDown()  {
		if (!tryMove(curPiece, curX, curY - 1)) blockDropped();
	}


	private void clearBoard() {
		for (int i=0;i<BOARD_HEIGHT * BOARD_WIDTH;i++) board[i] = Block.Shape.NoShape;
	}

	private void blockDropped() {
		for (int i=0;i<4;i++) {
			int x = curX + curPiece.x(i);
			int y = curY - curPiece.y(i);
			board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
		}

		removeFullLines();

		if (!isFallingFinished) newPiece();
	}

	private void newPiece()  {
		curPiece.setShape(nextBlock.getShape());
		curX = BOARD_WIDTH / 2 + 1;
		curY = BOARD_HEIGHT - 1 + curPiece.minY();
		nextBlock.setRandomShape();
		placeholder.updateNextBlock(nextBlock);

		if (!tryMove(curPiece, curX, curY)) {
			try {
				boolean done = false;
				ArrayList<String> toDo = new ArrayList<String>();
				while(sc.hasNextInt()) {
					int score = sc.nextInt();
					if(score <= curScore && !done) {
						done = true;
						toDo.add(""+ curScore);
					}
					toDo.add(""+score);
					if(toDo.size()==10) break;
				}
				System.setOut(new PrintStream(new FileOutputStream("src/resources/highscores.txt")));
				for(int i=0;i<toDo.size();i++) System.out.println(toDo.get(i));
			} catch(Exception e) {};
			curPiece.setShape(Block.Shape.NoShape);
			timer.stop();
			isStarted = false;
			statusbar.setText("game over");
			parent.reset();
		}
	}

	private void loadPiece() {
		curX = BOARD_WIDTH / 2 + 1;
		curY = BOARD_HEIGHT - 1 + curPiece.minY();

		if (!tryMove(curPiece, curX, curY)) {
			curPiece.setShape(Block.Shape.NoShape);
			timer.stop();
			isStarted = false;
			statusbar.setText("game over");
		}
	}

	private void holdBlock() {
		if(blockHeld.getShape() == Block.Shape.NoShape) {
			blockHeld.setShape(curPiece.getShape());
			newPiece();
			placeholder.updateBlockHeld(blockHeld);
			return;
		}
		Block temp = new Block();
		temp.setShape(curPiece.getShape());
		curPiece.setShape(blockHeld.getShape());
		blockHeld.setShape(temp.getShape());
		loadPiece();
		placeholder.updateBlockHeld(blockHeld);
	}

	private boolean tryMove(Block newPiece, int newX, int newY) {
		for (int i=0;i<4;i++) {
			int x = newX + newPiece.x(i);
			int y = newY - newPiece.y(i);

			if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) return false;

			if (shapeAt(x, y) != Block.Shape.NoShape) return false;
		}

		curPiece = newPiece;
		curX = newX;
		curY = newY;

		repaint();

		return true;
	}

	private void removeFullLines() {
		int numFullLines = 0;

		for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
			boolean lineIsFull = true;

			for (int j = 0; j < BOARD_WIDTH; ++j) {
				if (shapeAt(j, i) == Block.Shape.NoShape) {
					lineIsFull = false;
					break;
				}
			}

			if (lineIsFull) {
				++numFullLines;
				for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
					for (int j = 0; j < BOARD_WIDTH; ++j) {
						board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
					}
				}
			}
		}

		if (numFullLines > 0) {
			if(numFullLines == 1) curScore += 100;
			else if(numFullLines == 2) curScore += 300;
			else if(numFullLines == 3) curScore += 500;
			else curScore += 800;
			statusbar.setText("Score: " + curScore);
			isFallingFinished = true;
			curPiece.setShape(Block.Shape.NoShape);
			repaint();
		}
	}

	private void drawSquare(Graphics g, int x, int y, Block.Shape shape)  {
		Color colors[] = { new Color(0, 0, 0), new Color(170, 6, 6), 
				new Color(3, 165, 43), new Color(1, 193, 181), 
				new Color(109, 1, 191), new Color(191, 178, 0), 
				new Color(201, 115, 2), new Color(0, 33, 201)
		};

		Color color = colors[shape.ordinal()];

		g.setColor(color);
		g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

		g.setColor(color.brighter());
		g.drawLine(x, y + squareHeight() - 1, x, y);
		g.drawLine(x, y, x + squareWidth() - 1, y);

		g.setColor(color.darker());
		g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
		g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
	}

	private void drawIndicator(Graphics g, int x) {
		Color color = new Color(255,255,255,100);
		g.setColor(color);
		g.fillRect(x, 0, squareWidth()-1, (int)getSize().getHeight());
	}

	class TAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {

			if (!isStarted || curPiece.getShape() == Block.Shape.NoShape) return;

			int keycode = e.getKeyCode();

			if (keycode == 'P') {
				pause();
				return;
			}

			if (isPaused) return;

			switch (keycode) {

			case KeyEvent.VK_LEFT:
				tryMove(curPiece, curX - 1, curY);
				break;

			case KeyEvent.VK_RIGHT:
				tryMove(curPiece, curX + 1, curY);
				break;

			case 'Z':
				tryMove(curPiece.rotateRight(), curX, curY);
				break;

			case KeyEvent.VK_UP:
				tryMove(curPiece.rotateLeft(), curX, curY);
				break;

			case KeyEvent.VK_SPACE:
				dropDown();
				break;

			case KeyEvent.VK_DOWN:
				oneLineDown();
				break;

			case KeyEvent.VK_SHIFT:
				holdBlock();
				break;
			}
		}
	}
}