package server;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import abstractClasses.Snake;
import abstractClasses.Snake.Direction;
import client.NPCSnake;
import exceptions.EmptyBufferException;
import server.GameState.GameTile;
import server.GameState.SnakeModel;

/**
 * Class for game server which handles game logic. Manages interactions between all
 * players and the game board, updates the gamestate and serves requests for the gamestate
 * from clients.
 */
public class GameServer implements Runnable {
	private static int serverEncryptionKey;
	private int numPlayers;
	private volatile ConcurrentHashMap<Integer, Snake> snakes;
	private GameState gameState;
	private int playersAuthenticated;
	private static ExecutorService serverExecutor;
	private UserDatabase userDB;

	ConcurrentHashMap<Integer, Future<?>> futuresMap;
	private int tickCount;
	private long tickTime;

	/**
	 * CONSTRUCTOR for game server.
	 * 
	 * @param numPlayers    the number of players who will login to this game
	 * @param encryptionKey encryption key used to decrypt user password
	 */
	public GameServer(int numPlayers, int encryptionKey) {
		serverEncryptionKey = encryptionKey;
		this.numPlayers = numPlayers;
		snakes = new ConcurrentHashMap<Integer, Snake>();
		gameState = new GameState();
		playersAuthenticated = 0;
		serverExecutor = Executors.newCachedThreadPool();
		userDB = new UserDatabase();
	}

	/**
	 * Runs the player login server. Once all players logged in runs the game.
	 */
	@Override
	public void run() {
		// wait for all players to log in
		while (playersAuthenticated < numPlayers) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// login completed
		System.out.println("Server completed login - number of snakes is " + snakes.size() + " in thread: "
				+ Thread.currentThread().getName());

		playGame();

		gameOver();
	}

	/**
	 * Play the game of snake. Game server gets snake directions from user input buffers
	 * and updates the gamestate. Submits the gamestate to the view-controller for display.
	 */
	private void playGame() {
		tickCount = 0;

		// play game in loop
		while (true) {

			// start time for this server tick
			tickTime = System.currentTimeMillis();

			// update snake models
			updateGameState();

			// check if any snakes alive after update
			if (!anySnakesAlive()) {
				// if all snakes dead exit playGame()
				return;
			}

			// send gamestate to clients
			publishGameState();

			// add food to board every 10th tick
			if (tickCount % 100 == 0) {
				gameState.addFood();
			}

			tickCount += 1;

			try {
				long sleepTime = 120 - (System.currentTimeMillis() - tickTime);
				if (sleepTime < 0) {
					sleepTime = 0;
				}
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Run end of game sequence.
	 * TODO shutdown? restart login? restart new game with existing logged in players?
	 */
	private void gameOver() {
	}

	/**
	 * Method to send the game state to clients for display
	 */
	private void publishGameState() {
		futuresMap = new ConcurrentHashMap<Integer, Future<?>>();

		// for each snake playing the game
		for (ConcurrentHashMap.Entry<Integer, Snake> snakeEntry : snakes.entrySet()) {

			// if snake is alive
			if (snakeEntry.getValue().isAlive()) {

				// start a worker thread to transmit game state to clients
				Future<?> future = serverExecutor
						.submit(new PublishGameStateWorker(snakeEntry, new GameState(gameState)));

				// store future in concurrent hash map
				futuresMap.put(snakeEntry.getKey(), future);
			}
		}
		
		System.out.println("Server has started " + futuresMap.size() +  " PublishGameStateWorkers");
		
		// wait for all snakes to be finished updating before exit
		boolean updatingSnakeModels = true;
		while (updatingSnakeModels) {
			updatingSnakeModels = false;
			// for each worker
			for (ConcurrentHashMap.Entry<Integer, Future<?>> futureEntry : futuresMap.entrySet()) {
				// if not done
				if (!futureEntry.getValue().isDone()) {
					// still updating
					updatingSnakeModels = true;
				}
			}
		}
	}

	/**
	 * Launches worker threads to get direction for all snakes in the game from the input buffers
	 * and then update snakemodels in the gamestate.
	 */
	private void updateGameState() {
		futuresMap = new ConcurrentHashMap<Integer, Future<?>>();

		// for each snake playing the game
		for (ConcurrentHashMap.Entry<Integer, Snake> snakeEntry : snakes.entrySet()) {

			// if snake is alive
			if (snakeEntry.getValue().isAlive()) {

				// start a worker thread to update snake model
				Future<?> snakeUpdateFuture = serverExecutor.submit(new GameStateUpdateWorker(snakeEntry));

				// store future in concurrent hash map
				futuresMap.put(snakeEntry.getKey(), snakeUpdateFuture);
			}
		}
		
		System.out.println("Server has started " + futuresMap.size() +  " GameStateUpdateWorkers");

		// wait for all snakes to be finished updating before exit
		boolean updatingSnakeModels = true;
		while (updatingSnakeModels) {
			updatingSnakeModels = false;
			// for each worker
			for (ConcurrentHashMap.Entry<Integer, Future<?>> futureEntry : futuresMap.entrySet()) {
				// if not done
				if (!futureEntry.getValue().isDone()) {
					// still updating
					updatingSnakeModels = true;
				}
			}
		}
	}

	/**
	 * Thread safe method to compare user details to user database for
	 * authentication. If player is authenticated create a snake model in the game
	 * state.
	 * 
	 * @param playerDetails object containing username and password
	 * @param snake         the snake trying to join the game
	 * @return              true if the player login details were successfully authenticated
	 */
	public synchronized Boolean authenticate(PlayerDetails playerDetails, Snake snake) {
		// authenticate user against user database
		Boolean authenticated = userDB.authenticate(playerDetails, serverEncryptionKey);
		if (authenticated) {
			this.playersAuthenticated++;
			this.addSnake(snake);
			System.out.println("added snake to game. number of snakes is " + snakes.size());
			return true;
		}
		return false;
	}

	/**
	 * Launches a worker thread to handle non-player snake login
	 * 
	 * @param snake         the snake trying to join the game
	 * @return              true if the player login details were successfully authenticated
	 */
	public Boolean npcLogin(NPCSnake npcSnake) {
		Future<?> npcLoginFuture = serverExecutor.submit(new NPCSnakeLoginWorker(npcSnake));
		System.out.println("started NPC login worker for NPCSnake " + npcSnake.getSnakeId() + " on thread: "
				+ Thread.currentThread().getName());
		// wait for login worker to finish
		while (!npcLoginFuture.isDone()) {
		}
		System.out.println("login worker for NPCSnake " + npcSnake.getSnakeId() + " finished");
		return true;
	}

	/**
	 * Check all the snakes in the game to see if any are still alive
	 * 
	 * @return true if at least one snake is alive, false otherwise
	 */
	private boolean anySnakesAlive() {
		for (ConcurrentHashMap.Entry<Integer, Snake> snakeEntry : snakes.entrySet()) {
			if (snakeEntry.getValue().isAlive()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Add the snake to the game and create a model representing the snake in the gamestate.
	 */
	private void addSnake(Snake snake) {
		// add snake
		this.snakes.put(snake.getSnakeId(), snake);
		// add snake model
		gameState.addSnakeModel(snake);
	}

	/**
	 * Thread safe access to clone the game board state from the server.
	 * 
	 * @return a 2-dimensional GameTile array representing the game board
	 */
	public GameTile[][] cloneBoard() {
		return gameState.cloneGameBoard();
	}

	/**
	 * Thread safe access to clone the game snake model state from the server.
	 * 
	 * @return a ConcurrentHashMap representing the position of all the snakes on the game
	 *         board mapped to snake ID
	 */
	public ConcurrentHashMap<Integer, SnakeModel> cloneSnakeModels() {
		return gameState.cloneSnakeModels();
	}

	// ========== INNER WORKER CLASSES ==========

	/**
	 * Worker class to get Snake directions from the input buffers and update the game
	 * state.
	 */
	private class GameStateUpdateWorker extends Thread {
		int snakeId;
		Snake snake;
		Direction direction;

		/**
		 * Construct a Worker to get Snake directions from the input buffers and update 
		 * the game state.
		 * 
		 * @param snakeEntry an entry in the gameserver ConcurrentHashMap of snakes
		 *                   in the game
		 */
		public GameStateUpdateWorker(Entry<Integer, Snake> snakeEntry) {
			snakeId = snakeEntry.getKey();
			snake = snakeEntry.getValue();
		}

		/**
		 * Runnable method to update snake direction from direction buffer then
		 * update snake models and the game state.
		 */
		public void run() {

			// consume from input buffers and update snake current heading.
			try {
				direction = snake.directionBufferConsume();
				// update snake current heading
				snake.updateCurrentHeading(direction);
			} catch (EmptyBufferException e) {
				// no new snake direction input - do nothing
			}

			// get snakes current heading
			direction = snake.getCurrentHeading();

			if (direction == null) {
				// start of game and snake has yet to move
			} else {
				// update snakemodel
				gameState.moveSnake(snakeId, direction);
			}
		}
	}

	/**
	 * Worker class to get Snake directions from the input buffers and update the game
	 * state.
	 */
	private class PublishGameStateWorker extends Thread {
		Snake snake;
		GameState serverGameState;

		/**
		 * Construct a Worker to send the game state to clients.
		 * 
		 * @param snakeEntry an entry in the gameserver ConcurrentHashMap of snakes
		 *                   in the game
		 */
		public PublishGameStateWorker(Entry<Integer, Snake> snakeEntry, GameState serverGameState) {
			snake = snakeEntry.getValue();
			this.serverGameState = serverGameState;
		}

		/**
		 * Runnable method to update clients with the current server game state,
		 */
		public void run() {
			snake.submitGameState(serverGameState);
		}
	}

	/**
	 * Worker class to login non-player-character snakes.
	 */
	private class NPCSnakeLoginWorker extends Thread {
		NPCSnake npcSnake;

		/**
		 * Construct a Worker to update the GameState representing the game board from the game server.
		 */
		public NPCSnakeLoginWorker(NPCSnake npcSnake) {
			this.npcSnake = npcSnake;
		}

		public void run() {
			// automatically login npc snakes
			addSnake(npcSnake);
			System.out.println("added NPCsnake " + npcSnake.getSnakeId() + " to game");
		}
	}
}
