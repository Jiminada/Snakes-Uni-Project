package client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.dispatcher.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import abstractClasses.ViewController;

/**
 * View-Controller class for human controlled players of the game of snake.
 * Supports up to 4 players sharing a common screen each with their own virtual
 * display in seperate windows.
 */
@SuppressWarnings("serial")
public class PlayerVC extends ViewController implements NativeKeyListener {
	private PlayerSnake playerSnake;
	private String controls = "";
	private static ExecutorService playerVCExecutor = Executors.newCachedThreadPool();

	/**
	 * CONSTRUCTOR for player View-Controller.
	 * 
	 * @param playerSnake the playerSnake which this view-controller belongs to
	 */
	public PlayerVC(PlayerSnake playerSnake) {
		super();
		this.playerSnake = playerSnake;
		frame = new JFrame();
	}

	@Override
	public void configureFrame(int dimension, int borderWidth) {
		frame.setFocusableWindowState(true);
		frame.setSize(417, 440);
		splitscreen(playerSnake.getSnakeId());
		displayPanel = new DisplayPanel(0, 400, 400);
		frame.add(displayPanel);
		frame.setTitle("Player: " + playerSnake.getSnakeId() + " - controls: " + controls);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

	/**
	 * Creates login window for snake game which prompts user for login details.
	 */
	public void showLogin() {
		// configure game frame
		configureFrame(450, 50);
		// username field
		JLabel user_label = new JLabel("User Name :");
		JTextField userName_text = new JTextField();
		// password field
		JLabel password_label = new JLabel("Password :");
		JPasswordField password_text = new JPasswordField();
		// submit button
		JLabel message = new JLabel(); // blank label
		JButton submitButton = new JButton("SUBMIT");
		// construct panel
		JPanel panel = new JPanel(new GridLayout(3, 1));
		panel.add(user_label);
		panel.add(userName_text);
		panel.add(password_label);
		panel.add(password_text);
		panel.add(message);
		panel.add(submitButton);
		// configure frame
		JFrame loginFrame = new JFrame("Player " + playerSnake.getSnakeId() + " Login");
		loginFrame.setAlwaysOnTop(true);
		loginFrame.getRootPane().setDefaultButton(submitButton);
		loginFrame.setResizable(false);
		loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginFrame.add(panel, BorderLayout.CENTER);
		loginFrame.setSize(300, 100);
		loginFrame.setLocationRelativeTo(frame);
		loginFrame.setVisible(true);
		// Create action listener for submit button
		ActionListener submitListener = new ActionListener() {
			/**
			 * If the submit button is pressed this function is called on the
			 * event dispatch thread. Launches a worker thread to run player
			 * authentication.
			 */
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (ae.getActionCommand().equals("SUBMIT")) {
					loginFrame.setVisible(false);
					// start a worker thread to process user input and free EDT
					playerVCExecutor.submit(new SubmitWorker(userName_text.getText(), password_text.getPassword()));
				}
			}
		};
		submitButton.addActionListener(submitListener);
	}

	/**
	 * Creates a global listener which registers user input on keyboard. Also starts
	 * a thread to play the game for the player snake.
	 */
	public void startGame() {
		System.out.println("Player " + playerSnake.getSnakeId() + " View-Controller is running on "
				+ Thread.currentThread().getName());
		
		// Set the event dispatcher to a swing safe executor service.
		GlobalScreen.setEventDispatcher(new SwingDispatchService());

		// hook into OS io
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		
		// suppress spam to console from native key listener
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);

		// set player VC to listen to global screen for keyboard input
		GlobalScreen.addNativeKeyListener(this);
	}

	/**
	 * Display confirmation of login window.
	 */
	public void loginSuccess(String username) {
//		boxNotify(frame, "SUCCESS", "Welcome, " + username + "!!!"); // blocks EDT - do not use
		// TODO write non-blocking success window
	}

	/**
	 * Display failure to login window.
	 */
	public void loginFailed() {
//		boxError("User Details Invalid"); // blocks EDT - do not use
		// TODO write non-blocking failure window
	}

	/**
	 * Invoked when a key is pressed, this method will spawn a worker thread to
	 * process user input.
	 * 
	 * @param event A globally detected key event
	 */
	public void nativeKeyPressed(NativeKeyEvent event) {
		System.out.println("Thread : " + Thread.currentThread().getName() + " registered key press "
				+ NativeKeyEvent.getKeyText(event.getKeyCode()));
		// start a worker thread to process user input and free EDT
		playerVCExecutor.submit(new InputWorker(event.getKeyCode()));
	}
	
	// ============ SETTER & GETTER METHODS ============
	
	/**
	 * Set string representation of player control scheme for window title display.
	 * 
	 * @param controls
	 */
	public void setControls(String controls) {
		this.controls = controls;
	}
	
	// ========== PRIVATE METHODS ==========
	
	/**
	 * Place the player display on the shared screen based on player snake id.
	 * 
	 * @param snakeID snake id number
	 */
	private void splitscreen(int snakeId) {
		switch (snakeId) {
		case 1:
			// top left
			frame.setLocation(0, 0);
			return;
			
		case 2:
			// bottom left
			frame.setLocation(0, screenSize.height - frame.getSize().height);
			return;
			
		case 3:
			// top right
			frame.setLocation(screenSize.width - frame.getSize().width, 0);
			return;
			
		case 4:
			// bottom right
			frame.setLocation(screenSize.width - frame.getSize().width, screenSize.height - frame.getSize().height);
			return;
			
		default:
			// can only have 4 player controlled characters
		}
		return;
	}

	// ========== INNER WORKER CLASSES ==========

	/**
	 * Worker class to run thread for submitting player input during gameplay.
	 */
	private class InputWorker extends Thread {
		private int keyCode;

		public InputWorker(int keyCode) {
			this.keyCode = keyCode;
		}

		public void run() {
			// validate user input and update buffer as required
			playerSnake.directionInput(keyCode);
		}
	}

	/**
	 * Worker class to run submit thread for authenticating player details.
	 */
	private class SubmitWorker extends Thread {
		String username;
		char[] password;

		public SubmitWorker(String username, char[] password) {
			this.password = password;
			this.username = username;
		}

		public void run() {
			// authenticate user details
			playerSnake.authenticate(username, password);			
		}
	}

	// ========== UNUSED ==========

	/**
	 * Unused in this program - required to satisfy interface
	 */
	@Override
	public void nativeKeyTyped(NativeKeyEvent nativeEvent) {
	}

	/**
	 * Unused in this program - required to satisfy interface
	 */
	@Override
	public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
	}
}