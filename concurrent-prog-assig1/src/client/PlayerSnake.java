package client;

import static java.awt.Color.BLUE;
import static java.awt.Color.CYAN;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static javax.swing.SwingUtilities.invokeLater;
import static server.GameState.GAME_SIZE;

import java.awt.Color;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jnativehook.keyboard.NativeKeyEvent;

import abstractClasses.Snake;
import server.GameServer;
import server.GameState;
import server.PlayerDetails;

// TODO NOTE: only repaint(), revalidate() and add/remove listener methods of Swing are thread safe and should be used without invokeLater().
//            can also use methods on components before they are set visible (before setVisible(true))

/**
 * Runnable class for a client (user) to play the snake game. Supports up to 4
 * players with unique keymaps for game control. Handles all logic operations for
 * player snakes including:<p>
 * - user input validation and submission to the server.<p>
 * - updating local gamestate by submitting requests to the server.<p>
 * - generation of a color matrix representing the gamestate and submission to a
 *   view-controller for display to the screen.<p>
 */
public class PlayerSnake extends Snake {
	private static int snakeEncryptionKey;
	private GameState localGameState;
	private Boolean authenticated;
	private PlayerDetails playerDetails;

	private static ExecutorService playerExecutor;
	private volatile Color[][] gameBoardModel; // displayable model of gamestate
	private PlayerVC playerVC; // for display and user input capture (view-controller)
	// control scheme variables
	private int up;
	private int down;
	private int left;
	private int right;

	/**
	 * CONSTRUCTOR for a player controlled Snake.
	 * 
	 * @param gameServer    the server the Snake will be played on
	 * @param snakeId       unique identifier for the snake
	 * @param encryptionKey encryption key used to encrypt user password
	 */
	public PlayerSnake(GameServer gameServer, int snakeId, int encryptionKey) {
		super(gameServer, snakeId);
		snakeEncryptionKey = encryptionKey;
		localGameState = new GameState();
		authenticated = false;
		playerDetails = new PlayerDetails();
		playerExecutor = Executors.newCachedThreadPool();
	}

	/**
	 * Assigns player control-scheme based on snake ID and launches a view-controller
	 * that can handle player input (control) and display to the screen (view).
	 * Starts the login sequence on the view-controller.
	 */
	@Override
	public void run() {
		// launch player view controller
		playerVC = new PlayerVC(this);
		// set player controls
		setScheme(getSnakeId());
		System.out.println("Player " + getSnakeId() + " running on " + Thread.currentThread().getName());
		// launch login
		invokeLater(new Runnable() {
			public void run() {
				playerVC.showLogin();
			}
		});
	}

	/**
	 * Accepts username and password, encrypts the details and submits them to the game server
	 * for authentication. If player details are not authenticated; relaunches login. On successful
	 * authentication; starts the game playing sequence in the view-controller.
	 * 
	 * @param username String of the users name
	 * @param password 1-dimensional character array of the users password
	 */
	public void authenticate(String username, char[] password) {
		// store user input in PlayerDetails object
		playerDetails = new PlayerDetails();
		playerDetails.setUsername(username);
		playerDetails.setPassword(Arrays.toString(password));

		// encrypt user details
		PlayerDetails encryptedPlayerDetails = playerDetails.encrypt(snakeEncryptionKey);

		// send player details to server for authentication
		authenticated = gameServer.authenticate(encryptedPlayerDetails, this);

		if (authenticated) {
			invokeLater(new Runnable() {
				public void run() {
					playerVC.loginSuccess(username); // success pop-up window
				}
			});

			System.out.println("login details for Player " + getSnakeId() + " are authenticated");

			// start playing the game on player executor service - to avoid running on VC
			// thread pool
			playerExecutor.submit(new GameStartWorker());

			// launch listener for player keyboard input during gameplay on view-controller
			invokeLater(new Runnable() {
				public void run() {
					playerVC.startGame();
				}
			});

		} else {
			invokeLater(new Runnable() {
				public void run() {
					playerVC.loginFailed(); // error pop-up window
				}
			});

			invokeLater(new Runnable() {
				public void run() {
					playerVC.showLogin(); // relaunch login
				}
			});
		}
	}

	/**
	 * Wait in a loop until snake dies. Key input handled by view-controller,
	 * Server prompts gamestate update for client.
	 */
	public void playGame() {
		gameBoardModel = new Color[GAME_SIZE][GAME_SIZE];

		// play game until snake dies
		while (this.isAlive()) {
		}
		
		gameOver();
	}

	private void gameOver() {
		System.out.println("#####   Player " + snakeId + " has died!!!   - GAME OVER -   #####");		
	}

	/**
	 * Method for the GameServer to submit gamestate updates to the client. Launches a
	 * client thread to produce a gameBoardModel and submits it to the view-controller.
	 */
	public synchronized void submitGameState(GameState serverGameState) {
		localGameState = serverGameState;
		playerExecutor.submit(new GameStateUpdateWorker());
	}

	/**
	 * Method to validate keycodes to see if they correspond to this snakes movement.
	 * If valid, puts corresponding snake control to the direction buffer.
	 * 
	 * @param keyCode the keycode representing a keyboard button press
	 */
	protected synchronized void directionInput(int keyCode) {
		// console display of operation
		System.out.println(
				"PlayerSnake.directionInput() - " + NativeKeyEvent.getKeyText(keyCode) + " registered by player "
						+ getSnakeId() + " running in worker thread: " + Thread.currentThread().getName());

		try {
			if (keyCode == up) {
				directionBufferProduce(Direction.UP);
			} else if (keyCode == left) {
				directionBufferProduce(Direction.LEFT);
			} else if (keyCode == down) {
				directionBufferProduce(Direction.DOWN);
			} else if (keyCode == right) {
				directionBufferProduce(Direction.RIGHT);
			} else {
				// do nothing - not part of this snakes control scheme
			}
		} catch (InterruptedException ie) {
			System.out.println("Player " + getSnakeId() + " interrupted while waiting to put Direction on buffer");
		}
	}

	// ========== PRIVATE METHODS ==========

	// TODO make game server draw gameboard so this method has something to draw.
	// Currently localGameState.gameBoard is always empty.
	/**
	 * Generate a java.awt.Color array to represent the game board from current game state.
	 * Player snake will be coloured cyan while other snakes are blue. The color
	 * array can be passed to the View-Controller for display to the screen.
	 */
	private synchronized Color[][] buildGameBoardModel() {
		// create color matrix
		Color[][] colorMatrix = new Color[GAME_SIZE][GAME_SIZE];

		// colour game tiles from gamestate
		for (int row = 0; row < gameBoardModel.length; row++) {
			for (int col = 0; col < gameBoardModel.length; col++) {
				if (localGameState.getGameTile(row, col) != null) {
					switch (localGameState.getGameTile(row, col)) {

					case SNAKE:
						// if it is players snake colour it cyan
						if (localGameState.givenSnakeOccupies(this.getSnakeId(), row, col)) {
							colorMatrix[row][col] = CYAN;
						} else {
							// colour other snakes blue
							colorMatrix[row][col] = BLUE;
						}
						break;

					case FOOD_BONUS:
						colorMatrix[row][col] = RED;
						break;

					case FOOD_MALUS:
						colorMatrix[row][col] = GREEN;
						break;

					default:
						// cannot get here
						break;
					}
				} else {
					colorMatrix[row][col] = WHITE;
				}
			}
		}
		return colorMatrix;
	}

	/**
	 * Sets the player control scheme according to player id and displays a message
	 * to the console to inform the player of their control scheme. Also sets
	 * control scheme string in player view-controller for display in the window title.
	 * 
	 * @param snakeId The players snake identification number
	 */
	private void setScheme(int snakeId) {
		switch (snakeId) {
		case 1:
			up = NativeKeyEvent.VC_W;
			left = NativeKeyEvent.VC_A;
			down = NativeKeyEvent.VC_S;
			right = NativeKeyEvent.VC_D;
			playerVC.setControls("W,A,S,D");
			break;

		case 2:
			up = NativeKeyEvent.VC_I;
			left = NativeKeyEvent.VC_J;
			down = NativeKeyEvent.VC_K;
			right = NativeKeyEvent.VC_L;
			playerVC.setControls("I,J,K,L");
			break;

		case 3:
			up = NativeKeyEvent.VC_UP;
			left = NativeKeyEvent.VC_LEFT;
			down = NativeKeyEvent.VC_DOWN;
			right = NativeKeyEvent.VC_RIGHT;
			playerVC.setControls("ARROWS");
			break;

		case 4:
			up = NativeKeyEvent.VC_8;
			left = NativeKeyEvent.VC_4;
			down = NativeKeyEvent.VC_5;
			right = NativeKeyEvent.VC_6;
			playerVC.setControls("NUM 8,4,5,6");
			break;

		default:
			// cannot get here
		}
	}

	// ========== INNER WORKER CLASSES ==========

	/**
	 * Worker class to start playing the game on the playerSnake executor service.
	 */
	private class GameStateUpdateWorker extends Thread {

		/**
		 * Construct a Worker to start playing the game on the playerSnake executor service.
		 */
		public GameStateUpdateWorker() {
		}

		public void run() {
			// build game board model
			gameBoardModel = buildGameBoardModel();

			// submit game board model to view controller for display
			invokeLater(new Runnable() {
				public void run() {
					playerVC.drawGameBoard(gameBoardModel);
				}
			});
		}
	}

	/**
	 * Worker class to start playing the game on the playerSnake executor service.
	 */
	private class GameStartWorker extends Thread {

		/**
		 * Construct a Worker to start playing the game on the playerSnake executor service.
		 */
		public GameStartWorker() {
		}

		public void run() {
			// play game on playerSnake executor service
			playGame();
		}
	}
}
