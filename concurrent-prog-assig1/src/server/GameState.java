package server;

import static server.GameState.GameTile.FOOD_BONUS;
import static server.GameState.GameTile.FOOD_MALUS;
import static server.GameState.GameTile.SNAKE;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import abstractClasses.Snake;
import abstractClasses.Snake.Direction;

/**
 * Class to represent the game state for a game of snake. Inner class SnakeModel
 * represents a snakes position on the game board.
 */
public class GameState {
	public static final int GAME_SIZE = 40;
	// shared variables
	private volatile GameTile[][] gameBoard;
	private volatile ConcurrentHashMap<Integer, SnakeModel> snakeModels;

	/**
	 * Enumerated type for the game tiles in the game of snake.
	 */
	public enum GameTile {
		FOOD_BONUS, FOOD_MALUS, SNAKE
	}

	/**
	 * CONSTRUCTOR for GameState.
	 */
	public GameState() {
		gameBoard = new GameTile[GAME_SIZE][GAME_SIZE];
		snakeModels = new ConcurrentHashMap<Integer, SnakeModel>();
	}
	
	/**
	 * COPY CONSTRUCTOR for GameState.
	 */
	public GameState(GameState gameState) {
		gameBoard = gameState.cloneGameBoard();
		snakeModels = gameState.cloneSnakeModels();
	}

	/**
	 * Adds a new snake model representing the snake with supplied ID to the set of
	 * snakemodels in the gamestate. Snake is placed in a random unoccupied starting
	 * location on the game board.
	 * 
	 * @param snakeId snakes id number
	 */
	protected synchronized void addSnakeModel(Snake snake) {
		int[] startLocation = randomEmptyTile();
		SnakeModel newSnake = new SnakeModel(startLocation, snake);
		getSnakeModels().put(snake.getSnakeId(), newSnake);
	}

	/**
	 * Thread safe method that randomly adds 1 piece each of bonus and malus food
	 * to unoccupied tiles on the game board.
	 */
	protected void addFood() {
		// add bonus food
		int[] coordinate = randomEmptyTile();
		setGameTile(coordinate, FOOD_BONUS);
		// add malus food
		coordinate = randomEmptyTile();
		setGameTile(coordinate, FOOD_MALUS);
	}

	/**
	 * Method to check if a given snakeModel occupies the given coordinate on
	 * the game board.
	 * 
	 * @param snakeId the id number of the snake to check
	 * @param row     row coordinate to check
	 * @param column  column coordinate to check
	 * @return        true if the snake with given ID occupies the given game
	 *                board coordinate, false otherwise
	 */
	public boolean givenSnakeOccupies(int snakeId, int row, int column) {
		return snakeModels.get(snakeId).occupiesTile(row, column);
	}

	/**
	 * Thread safe method to clone a copy of the gameBoard.
	 * 
	 * @return a deep copy of the game board
	 */
	public final synchronized GameTile[][] cloneGameBoard() {
		GameTile[][] gameBoardCopy = new GameTile[gameBoard.length][];
		for (int i = 0; i < gameBoard.length; i++) {
			// for each row clone the column
			gameBoardCopy[i] = gameBoard[i].clone();
		}
		return gameBoardCopy;
	}

	/**
	 * Thread safe method that clones the snakeModels concurrent hash map.
	 * 
	 * @return a deep copy of the snakeModels ConcurrentHashMap.
	 */
	public final synchronized ConcurrentHashMap<Integer, SnakeModel> cloneSnakeModels() {
		ConcurrentHashMap<Integer, SnakeModel> SnakeModelsClone = new ConcurrentHashMap<Integer, SnakeModel>();
		// for each key,value pair in original
		for (ConcurrentHashMap.Entry<Integer, SnakeModel> snakeModelEntry : getSnakeModels().entrySet()) {
			// put a cloned key,value pair in the clone
			SnakeModelsClone.put(snakeModelEntry.getKey(), new SnakeModel(snakeModelEntry.getValue()));
		}
		return SnakeModelsClone;
	}

	/**
	 * Thread safe method to update the specified Snakemodel in the gamestate given the provided direction.
	 * 
	 * @param snakeId   the snake identification number for the snake to move
	 * @param direction the direction to move the snake
	 */
	public synchronized void moveSnake(int snakeId, Direction direction) {
		SnakeModel snakemodel = getSnakeModel(snakeId);
		snakemodel.moveSnake(direction);
	}

	// ============ SETTER & GETTER METHODS ============

	/**
	 * Sets the gameboard to match the argument game board array.
	 * 
	 * @param gameBoard a 2-dimensional GameTile array representing the game board
	 */
	public synchronized void setGameBoard(GameTile[][] gameBoard) {
		this.gameBoard = gameBoard;
	}

	/**
	 * Sets snakeModels to match the argument snakeModels.
	 * 
	 * @param snakeModels a HashMap representing the position of all the snakes on
	 *                    the game board mapped to snake ID
	 */
	public synchronized void setSnakeModels(ConcurrentHashMap<Integer, SnakeModel> snakeModels) {
		this.snakeModels = snakeModels;
	}

	/**
	 * Get the GameTile on the game board at the coordinate supplied
	 * 
	 * @param row    row coordinate for GameTile
	 * @param column column coordinate for GameTile
	 * @return       the GameTile at the give coordinate
	 */
	public synchronized GameTile getGameTile(int row, int column) {
		return gameBoard[row][column];
	}

	/**
	 * Thread-safe getter for the SnakeModel in the gamestate with given snakeID.
	 * 
	 * @param snakeId  the id for the snakeModel
	 * @return         the SnakeModel with given snakeId if it exists, null otherwise
	 */
	public synchronized SnakeModel getSnakeModel(int snakeId) {
		return snakeModels.get(snakeId);
	}

	// ================ PRIVATE METHODS ================

	/**
	 * Set the specified gameboard coordinate to the specified GameTile type.
	 * 
	 * @param coordinate int array representing [x,y] coordinate on the gameboard
	 * @param tileType a GameTile enumerated type
	 */
	private synchronized void setGameTile(int[] coordinate, GameTile tileType) {
		gameBoard[coordinate[0]][coordinate[1]] = tileType;
	}

	/**
	 * Returns a random integer between min (inclusive) and max (exclusive) argument
	 * values.
	 * 
	 * @param min minimum value for random number range (inclusive)
	 * @param max maximum value for random number range (exclusive)
	 * @return a random integer
	 */
	private int randomInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max);
	}

	/**
	 * Find random coordinates of an empty tile on the game board.
	 * 
	 * @return int array representing [x,y] coordinate of an empty tile on the gameboard
	 */
	private synchronized int[] randomEmptyTile() {
		GameTile tile;
		Boolean validPosition = false;
		int[] coordinate = { 0, 0 };

		while (!validPosition) {
			// randomise tile coordinate
			coordinate[0] = randomInt(0, GAME_SIZE);
			coordinate[1] = randomInt(0, GAME_SIZE);
			// get tile at random coordinate
			tile = gameBoard[coordinate[0]][coordinate[1]];
			if (tile == null) {
				// if empty tile position is valid
				validPosition = true;
			}
		}
		return coordinate;
	}

	public ConcurrentHashMap<Integer, SnakeModel> getSnakeModels() {
		return snakeModels;
	}

	// =================== SNAKE MODEL CLASS ======================
	/**
	 * Inner class for the model of the snake on the game board.
	 */
	public final class SnakeModel {
		private LinkedList<int[]> model = new LinkedList<int[]>();
		private Snake snake;

		/**
		 * CONSTRUCTOR for snake model.
		 * 
		 * @param head  an int array representing [x,y] game board coordinates for the
		 *              starting position of the snake.
		 * @param snake the snake this model represents
		 */
		public SnakeModel(int head[], Snake snake) {
			this.snake = snake;
			model.offerFirst(head);
			setGameTile(head, SNAKE);
		}

		/**
		 * COPY CONSTRUCTOR.
		 */
		public SnakeModel(SnakeModel snakeModel) {
			this.model = new LinkedList<int[]>(snakeModel.model);
			this.snake = snakeModel.snake;
		}

		/**
		 * Check if this snake model occupies a given location on the game board.
		 * 
		 * @param row    row coordinate to check
		 * @param column column coordinate to check
		 * @return            true if this snakemodel occupies the given coordinate on the
		 *                    gameboard, false otherwise
		 */
		public boolean occupiesTile(int row, int column) {
			int[] coordinate;
			for (int i = 0; i < model.size(); i++) {
				coordinate = model.get(i);
				if (coordinate[0] == row && coordinate[1] == column) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Compares the Linked list model for equality. Checks the snakemodel head
		 * location only. Other variables are ignored. Used to facilitate checking for
		 * occupied locations when initiating snakes before a game starts.
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			SnakeModel snakeModel = (SnakeModel) o;
			// field comparison
			return (this.model.getFirst() == snakeModel.model.getFirst());
		}

		/**
		 * Moves a Snake on the gameBoard. Will increase snake length if fruit was
		 * consumed by the snake.
		 * 
		 * @param direction the direction the snake is moving in
		 */
		public synchronized void moveSnake(Direction direction) {
			int[] tail;
			int[] newHead = getNewHead(direction);

			if (gameBoard[newHead[0]][newHead[1]] == FOOD_BONUS) {
				
				// gain score
				snake.adjustScore(1);
				
				// move head
				gameBoard[newHead[0]][newHead[1]] = SNAKE;

			} else if (gameBoard[newHead[0]][newHead[1]] == FOOD_MALUS) {
				
				// remove tail
				tail = model.removeLast();
				gameBoard[tail[0]][tail[1]] = null;
				
				// move tail
				tail = model.removeLast();
				gameBoard[tail[0]][tail[1]] = null;
				
				// lose score
				snake.adjustScore(-1);
				
				if(model.isEmpty()) {
					// set snake dead
					snake.setAlive(false);
				}
				
				// move head
				gameBoard[newHead[0]][newHead[1]] = SNAKE;

			} else if (gameBoard[newHead[0]][newHead[1]] == SNAKE) {
				
				// clear all snake tiles
				while (!model.isEmpty()) {
					tail = model.removeLast();
					gameBoard[tail[0]][tail[1]] = null;
				}
				
				// set snake dead
				snake.setAlive(false);

			} else {
				
				// move tail
				tail = model.removeLast();
				gameBoard[tail[0]][tail[1]] = null;
				
				// move head
				gameBoard[newHead[0]][newHead[1]] = SNAKE;
			}
			model.offerFirst(newHead);
		}

		/**
		 * Returns the model representation of a snake as gameBoard coordinates in a
		 * linked list of integer pairs.
		 * 
		 * @return A linked list of integer pairs to represent [x][y] coordinates for
		 *         the body of the snake body
		 */
		public LinkedList<int[]> getSnake() {
			return model;
		}

		/**
		 * Determines the location for the new snake head based on direction.
		 * 
		 * @return [x][y] coordinates for new head location as integer array
		 */
		private int[] getNewHead(Direction direction) {

			int[] head = model.peekFirst();
			// get x and y coordinates - NOTE: (0,0) is top left
			int x = head[0];
			int y = head[1];

			// use modulo operation: exiting one side of board = enter other side
			switch (direction) {
			case UP:
				// gameSize added as Java can produce negative result from modulo operation
				y = (GAME_SIZE + y - 1) % GAME_SIZE;
				break;
			case DOWN:
				y = (y + 1) % GAME_SIZE;
				break;
			case LEFT:
				x = (GAME_SIZE + x - 1) % GAME_SIZE;
				break;
			case RIGHT:
				x = (x + 1) % GAME_SIZE;
			}
			// create new head
			int[] newHead = { x, y };
			return newHead;
		}
	}
}
