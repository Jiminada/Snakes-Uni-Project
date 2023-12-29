package abstractClasses;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import exceptions.EmptyBufferException;
import server.GameServer;
import server.GameState;

/**
 * Abstract class for snakes in the multiplayer snake game. Contains methods for
 * producing to and consuming from a buffer that stores snake direction
 * commands. Also has setters and getters for the snakes score in the game.
 */
public abstract class Snake implements Runnable {
	public static final int BUFFER_SIZE = 5;
    protected int snakeId;
	private volatile LinkedBlockingDeque<Direction> directionBuffer;
	private volatile int score;
	protected Boolean alive;
	protected GameServer gameServer;
	private Direction currentHeading;

	/**
	 * Direction for a snake movement on the gameboard.
	 */
	public enum Direction {
		UP, DOWN, LEFT, RIGHT
	}

	/**
	 * CONSTRUCTOR for abstract class Snake.
	 */
	public Snake(GameServer gameServer, int snakeId) {
		this.snakeId = snakeId;
		this.gameServer = gameServer;
		directionBuffer = new LinkedBlockingDeque<>();
		score = 0;
		currentHeading = null;
		alive = true;
	}

	/**
	 * A thread-safe method to add to directionBuffer. Will block if the buffer is full
	 * until space becomes available.
	 * 
	 * @param direction The Direction (UP, DOWN, LEFT, RIGHT) to be added to the
	 *                  buffer.
	 * @throws InterruptedException
	 */
	public synchronized void directionBufferProduce(Direction direction) throws InterruptedException {
		directionBuffer.put(direction);
	}

	/**
	 * A thread-safe method to get a direction from the directionBuffer.
	 *
	 * @return Direction enum type (UP, DOWN, LEFT, RIGHT) *
	 * @throws EmptyBufferException with thread name string if buffer is currently
	 *                              empty
	 */
	public synchronized Direction directionBufferConsume() throws EmptyBufferException {
		Direction direction;
		if (directionBuffer.isEmpty()) {
			throw new EmptyBufferException(Thread.currentThread().getName());
		}
		
		// stack implementation
		direction = directionBuffer.pollLast();   // get top of stack
		// clear stack if more than 3 saved user inputs in stack
		if(directionBuffer.size() > 3) {
			directionBuffer.clear();
			System.out.println("@@@@@@@@@@@@@@@ CLEARED BUFFER FOR SNAKE " + snakeId);
		}		                  
		
		// queue implementation
//		direction = directionBuffer.pollFirst();  // get front of queue		
		return direction;
	}
	
	/**
	 * Checks the argument direction (to be supplied from the input buffer) and
	 * updates the snakes current heading if valid.
	 * 
	 * @param direction The direction for the snake from the input buffer
	 */
	public void updateCurrentHeading(Direction direction) {
		if(direction == null) {
			return;
		}
		if (direction == Direction.UP && this.currentHeading != Direction.DOWN
				|| direction == Direction.DOWN && this.currentHeading != Direction.UP
				|| direction == Direction.LEFT && this.currentHeading != Direction.RIGHT
				|| direction == Direction.RIGHT && this.currentHeading != Direction.LEFT) {
			this.currentHeading = direction;
		}
	}
	
	public void adjustScore(int scoreChange) {
		score += scoreChange;		
	}
	
	// ============ SETTER & GETTER METHODS ============

	/**
	 * Get the snakes score.
	 * 
	 * @return integer of snakes score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * set
	 * 
	 * @param score
	 */
	public void setScore(int score) {
		this.score = score;
	}

	/**
	 * @param alive
	 */
	public void setAlive(Boolean alive) {
		this.alive = alive;
	}

	/**
	 * @return
	 */
	public Boolean isAlive() {
		return alive;
	}

	/**
	 * @return
	 */
	public int getSnakeId() {
		return snakeId;
	}

	/**
	 * Get the current heading of the snake.
	 * 
	 * @return current heading for the snake as validated by the gameserver
	 */
	public Direction getCurrentHeading() {
		return currentHeading;
	}

	/**
	 * Method for the GameServer to submit gamestate updates to the client. Launches a
	 * client thread to produce a gameBoardModel and submits it to the view-controller.
	 */
	public abstract void submitGameState(GameState serverGamestate);
}
