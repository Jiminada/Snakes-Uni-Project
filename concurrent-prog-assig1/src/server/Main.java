package server;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import client.NPCSnake;
import client.PlayerSnake;

/**
 * Launches setup windows to allow pre-game setup. Then starts the required
 * threads for the game.
 */
public final class Main {
	// CONSTANTS
	private static final int MAX_NPCS = 100;
	private static final int MAX_PLAYERS = 4; // cannot exceed 4 (only 4 unique keyboard mappings)
	public static final int ENCRYPTION_KEY = new Random().nextInt(20) + 1;

	private static int npcs;
	private static int players;
	private static SetupVC setupDisplay;
	private static GameServer snakeGame;
	private static ExecutorService NPCExecutor;

	// MAIN
	public static void main(String[] args) throws InterruptedException {

		// ============================ GAME SET UP ============================

		// create display window for pre-game set up
		setupDisplay = new SetupVC();
		// prompt user to select number of players
		players = setupDisplay.selectPlayers(MAX_PLAYERS);
		// prompt user to select number of non-player characters for game
		npcs = setupDisplay.selectNPCNumber(MAX_NPCS);		

		// create and start game thread (server)
		snakeGame = new GameServer(players, ENCRYPTION_KEY);
		Thread serverThread = new Thread(snakeGame, "Server");
		serverThread.start();
		System.out.println("Starting Game Server...");

		// create and start player threads (clients)
		for (int i = 1; i <= players; i++) { // number players from 1 (not 0)
			PlayerSnake playerSnake = new PlayerSnake(snakeGame, i, ENCRYPTION_KEY);
			Thread playerThread = new Thread(playerSnake, "Player-Thread-" + i);
			playerThread.start();
		}

		// initialise executor service for npc threads
		if (npcs > 0) {
			NPCExecutor = Executors.newFixedThreadPool(npcs); // can probably do this with fewer threads
		}
		// create npc snake threads and start them
		for (int i = 1; i <= npcs; i++) {
			NPCExecutor.submit(new NPCSnake(snakeGame, i + players)); // reserve lowest id numbers for players
		}

		// hide title screen
		setupDisplay.setVisible(false);
	}
}
