package client;

import java.util.Random;

import abstractClasses.Snake;
import server.GameServer;
import server.GameState;

/**
 * Runnable class for a client (non-player-character) to play the snake game.
 * NPC snake has no independent display and moves without any intelligence
 * (purely random).
 */
public class NPCSnake extends Snake {
	public static final int MOVE_DELAY_MAX = 4000; // max time between snake moves in milliseconds
	Random rand;// random number generator for NPC snake
	Direction[] directions = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };

	/**
	 * CONSTRUCTOR for non-player-controlled snake.
	 * 
	 * @param game the GameServer this snake is playing on
	 * @param id   the identifier number for this NPC snake
	 */
	public NPCSnake(GameServer gameServer, int snakeId) {
		super(gameServer, snakeId);
		rand = new Random();
	}

	/**
	 * Method for the GameServer to submit gamestate updates to the client. Launches a
	 * client thread to produce a gameBoardModel and submits it to the view-controller.
	 */
	public synchronized void submitGameState(GameState serverGameState) {
		// NPC snakes don't have a display - does nothing
	}

	/**
	 * Places random directions in the input buffer at random intervals to simulate
	 * a snake in the game.
	 *
	 */
	@Override
	public void run() {
		int moveDelay;

		// auto login for NPCs
		alive = gameServer.npcLogin(this);

		// endless loop
		while (alive) {
			move();

			// get random time to pause
			moveDelay = rand.nextInt(MOVE_DELAY_MAX);

			// pause NPC snake thread
			try {
				Thread.sleep(moveDelay); // wait for random time
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Randomly generates a direction and puts it on the direction buffer.
	 * 
	 */
	private void move() {
		try {
			directionBufferProduce(directions[rand.nextInt(4)]);

		} catch (InterruptedException e) {
			System.out.println("NPCSnake " + getSnakeId() + " interrupted while waiting to put Direction on buffer");
		}
	}
}
