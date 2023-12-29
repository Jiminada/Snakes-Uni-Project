package abstractClasses;

import static server.GameState.GAME_SIZE;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Abstract class for View and Control in the multiplayer snake game. Contains
 * methods for drawing the gamestate to the screen (VIEW) and capturing input
 * from a keyboard (CONTROL) shared by multiple threads (clients-players &
 * server-snakegame). Contains helper methods for easy window generation by
 * child classes and INNER CLASS DisplayPanel.
 */
@SuppressWarnings("serial")
public abstract class ViewController extends JPanel {
	public static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	protected JFrame frame;
	protected DisplayPanel displayPanel;

	/**
	 * CONSTRUCTOR for abstract class ViewController.
	 */
	public ViewController() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(false);
	}

	/**
	 * Draw the game board to the screen from the provided java.awt.Color array model.
	 * 
	 * @param gameBoardModel 2-dimensional java.awt.Color array representing the game board
	 */
	public void drawGameBoard(Color[][] gameBoardModel) {		
		// colour game tiles
		for (int row = 0; row < gameBoardModel.length; row++) {
			for (int col = 0; col < gameBoardModel[0].length; col++) {
					displayPanel.fillCell(row, col, gameBoardModel[row][col]);			
			}
		}
		displayPanel.repaint();
	}

	/**
	 * Sets the configuration for the view controller frame. Useful to provide frame
	 * initialisation.
	 * 
	 * @param dimension an integer dimension for the frame - useful to set height and width
	 * @param borderWidth an integer value for the frame - useful to set border size
	 */
	public abstract void configureFrame(int dimension, int borderWidth);

// =============== HELPER METHODS ========================
	
	// TODO NOTE: JFrame Dialog boxes block on the EDT. Not suitable for a single computer 
	//            implementation of the multiplayer game.          
	/**
	 * Creates a pop up window with a simple message.
	 * 
	 * @param title   the title for the pop up window
	 * @param message the message to display
	 */
	public final void boxNotify(Component frame, String title, String message) {
		JOptionPane.showMessageDialog(frame, message, title, JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Creates a pop up error window with an error message.
	 * 
	 * @param title        the title for the pop up window
	 * @param errorMessage the error message to display
	 */
	public final void boxError(String errorMessage) {
		JOptionPane.showMessageDialog(frame, errorMessage, "ERROR", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Creates a pop up window with a question and text box.
	 * 
	 * @param title    the title for the pop up window
	 * @param question the question to display
	 * @param prefill  the prefilled response for the input capture box
	 * @return string entered by user
	 */
	public final String boxQuestionCapture(String title, String question, String prefill) {
		String userInput = (String) JOptionPane.showInputDialog(frame, question, title, JOptionPane.QUESTION_MESSAGE,
				null, null, prefill);
		return userInput;
	}

//	=================================== INNER CLASS =====================================
	
	/**
	 * TODO write doc.
	 */
	public static final class DisplayPanel extends JPanel {
		private int panelWidth;
		private int panelHeight;
		private int xBorder;

		private List<Point> colorCells;
		private Color[][] colorMatrix;

		/**
		 * CONSTRUCTOR for a display panel.
		 * 
		 * @param xBorder
		 * @param panelWidth
		 * @param panelHeight
		 */
		public DisplayPanel(int xBorder, int panelWidth, int panelHeight) {
			colorCells = new ArrayList<>(GAME_SIZE);
			colorMatrix = new Color[GAME_SIZE][GAME_SIZE];
			this.panelWidth = panelWidth;
			this.panelHeight = panelHeight;
			this.xBorder = xBorder;
		}

		/**
		 * Draws the background game grid??
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			for (Point colorCell : colorCells) {
				int cellX = xBorder / 2 + (colorCell.x * 10);
				int cellY = (colorCell.y * 10);
				g.setColor(colorMatrix[colorCell.x][colorCell.y]);
				g.fillRect(cellX, cellY, 10, 10);
			}
			g.setColor(Color.BLACK);
			g.drawRect(xBorder / 2, 0, panelWidth, panelHeight);

			for (int i = 10; i <= panelWidth; i += 10) {
				g.drawLine(xBorder / 2 + i, 0, xBorder / 2 + i, panelHeight);
			}

			for (int i = 10; i <= panelHeight; i += 10) {
				g.drawLine(xBorder / 2, i, xBorder / 2 + panelWidth, i);
			}
		}

		/**
		 * TODO write doc.
		 * 
		 * @param x
		 * @param y
		 * @param color
		 */
		public void fillCell(int x, int y, Color color) {
			colorMatrix[x][y] = color;
			colorCells.add(new Point(x, y));
		}
	}
}