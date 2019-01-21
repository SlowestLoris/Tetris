/*
 * Raymond Zhang
 * Mr. Benum
 * ICS4UE
 * December 12, 2018
 */
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Tetris extends JFrame {

	//fields
	private JLabel statusbar;
	private BlockHolder placeholder;
	private JButton start, end;
	private Board board;
	private PrintStream stdout = System.out;
	private Clip clip;

	//constructors
	public Tetris() {
		setTitle("Tetris");
		setSize(222, 655);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		addKeyListener(new TAdapter());
		setFocusable(true);
		initUI();
	}

	//methods

	/**
	 * Plays the in game music
	 */
	private void playGameTheme() {
		try {
			if(clip.isOpen()) clip.stop();
			// Open an audio input stream.           
			File soundFile = new File("src/resources/TetrisGameTheme.wav");
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);              
			// Get a sound clip resource.
			clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioIn);
			clip.start();
			clip.loop(clip.LOOP_CONTINUOUSLY);
		} catch(Exception e) {e.printStackTrace();};
	}

	/**
	 * Plays the menu music
	 */
	private void playMenuTheme() {
		try {
			if(clip != null && clip.isOpen()) clip.stop();
			// Open an audio input stream.           
			File soundFile = new File("src/resources/TetrisMenuTheme.wav");
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);              
			// Get a sound clip resource.
			clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioIn);
			clip.start();
			clip.loop(clip.LOOP_CONTINUOUSLY);
		} catch(Exception e) {e.printStackTrace();};
	}

	/**
	 * Reset the game
	 */
	public void reset() {
		getContentPane().removeAll();
		revalidate();
		initEnd();
	}

	/**
	 * Loads the end game screen
	 */
	private void initEnd() {
		if(clip.isOpen()) clip.stop();
		end = new JButton();
		try {
			end.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("resources/endscreen.png"))));
		} catch (Exception ex) {System.out.println(ex);};
		end.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getContentPane().removeAll();
				revalidate();
				initUI();
			}
		});
		add(end);
		revalidate();
	}

	/**
	 * Loads the start game screen
	 */
	private void initUI() {
		start = new JButton();
		try {
			start.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("resources/homescreen.png"))));
		} catch (Exception ex) {System.out.println(ex);};
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getContentPane().removeAll();
				revalidate();
				initGame();
			}
		});
		add(start);
		revalidate();
		playMenuTheme();
	}

	/**
	 * Resize an image
	 * @param srcImg
	 * @param w
	 * @param h
	 * @return new image with specified dimensions
	 */
	private Image resizeImage(Image srcImg, int w, int h){
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return resizedImg;
	}

	/**
	 * Load the game screen
	 */
	private void initGame() {
		placeholder = new BlockHolder();
		add(placeholder, BorderLayout.SOUTH);

		statusbar = new JLabel("Score: 0");
		add(statusbar, BorderLayout.NORTH);

		board = new Board(this);
		add(board);
		board.start();

		board.requestFocus();

		pack();
		playGameTheme();
		revalidate();
	}

	/**
	 * Allows access to statusbar
	 * @return the statusbar
	 */
	public JLabel getStatusBar() {
		return statusbar;
	}

	/**
	 * Allows access to placeholder
	 * @return the placeholder
	 */
	public BlockHolder getBlockHolder() {
		return placeholder;
	}

	/**
	 * Opens up a link using the desktop
	 * @param uri
	 */
	public void openLink(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {};
		}
	}

	//allows for keyboard controls
	class TAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {

			int keycode = e.getKeyCode();

			if(keycode == 'H') {
				//open up help screen on desktop
				try {
					openLink(new URL("https://github.com/SlowestLoris/Tetris/wiki").toURI());
				} catch (Exception err) {};
			} else if(keycode == 'T') {
				//view high scores
				System.setOut(stdout);
				try {
					Scanner sc = new Scanner(new File("src/resources/highscores.txt"));
					String S = "These are the top scores:\n";
					while(sc.hasNextLine()) {
						String cur = sc.next();
						int score = sc.nextInt();
						S += cur + " " + score + "\n";
					}
					JOptionPane.showMessageDialog(null, S);
				} catch (Exception err) {JOptionPane.showMessageDialog(null, err);};
			}
		}
	}

	public static void main(String[] args) {
		//load game
		EventQueue.invokeLater(() -> {
			Tetris game = new Tetris();
			game.setVisible(true);
		});
	}

}