package server;

import javax.swing.JPanel;

import abstractClasses.ViewController;

/**
 * Display pre-game windows to configure game set up from user input.
 *
 */
@SuppressWarnings("serial")
public class SetupVC extends ViewController {
	private TitleScreen titlePanel;

	/**
	 * CONSTRUCTOR for game setup View-Controller.
	 */
	public SetupVC() {
		super();
		configureFrame(1000, 100);
		welcome();
	}

	/**
	 * Launch game title screen.
	 */
	public void welcome() {
		// TODO make game title screen
	}
	
	@Override
	public void configureFrame(int dimension, int borderWidth) {
		frame.setTitle("Snake Game Title Screen");
		frame.setSize(dimension, dimension);
		frame.setLocation(screenSize.width / 2 - frame.getWidth() / 2, screenSize.height / 2 - frame.getHeight() / 2);
		frame.setVisible(true);
		titlePanel = new TitleScreen();
		frame.add(titlePanel);
		
	}

	/**
	 * Window to ask how many NPC snakes to add to game. Requires a valid
	 * user input of an integer value between 0 and NPCMax before it will return.
	 * 
	 * @param NPCMax Maximum number of non-player-characters the game can support
	 * @return number of snakes to add to game
	 */
	public Integer selectNPCNumber(int NPCMax) {
		Integer numNPC = 0;
		boolean valid = false;
		String userInput;
		// until valid input
		while (!valid) {
			userInput = boxQuestionCapture("Non-player controlled snake selection",
					"How many non-player snakes do you want to play against?", "Enter between 0 and " + NPCMax);

			if (userInput != null) {
				// convert userInput to number and make that many NPC snake threads
				try {
					numNPC = Integer.valueOf(userInput);
					// validate range
					if (numNPC < 0 || numNPC > NPCMax) {
						// invalid input
						throw new NumberFormatException();
					} else {
						valid = true;
					}
				} catch (NumberFormatException nfe) {
					// launch pop up window declaring input requirements
					this.boxError("Input must be a number between 0 and " + NPCMax);
				}
			}
		}
		frame.setVisible(false);
		return numNPC;
	}

	/**
	 * Window to ask how many players to add to game lobby. Requires a valid
	 * user input of an integer value between 1 and playersMax before it will return.
	 * 
	 * @param playersMax Maximum number of players the game can support
	 * @return number of players to add to game lobby
	 */
	public int selectPlayers(int playersMax) {
		Integer numPlayers = 0;
		boolean valid = false;
		String userInput;
		// until valid input
		while (!valid) {
			userInput = boxQuestionCapture("Player number selection",
					"How many players are going to play?", "Enter between 1 and " + playersMax);

			if (userInput != null) {
				// convert userInput to number and make that many NPC snake threads
				try {
					numPlayers = Integer.valueOf(userInput);
					// validate range
					if (numPlayers < 1 || numPlayers > playersMax) {
						// invalid input
						throw new NumberFormatException();
					} else {
						valid = true;
					}
				} catch (NumberFormatException nfe) {
					// launch pop up window declaring input requirements
					this.boxError("Input must be a number between 1 and " + playersMax);
				}
			}
		}
		return numPlayers;
	}
	
	// ========== INNER CLASS ==========
	
	/**
	 * Class to display the gamestate as a panel on an underlying View-Controller
	 * window.
	 */
	public static final class TitleScreen extends JPanel { // TODO display game title screen

		/**
		 * CONSTRUCTOR for a Title screen.
		 */
		public TitleScreen() {
			
		}

	}
}
