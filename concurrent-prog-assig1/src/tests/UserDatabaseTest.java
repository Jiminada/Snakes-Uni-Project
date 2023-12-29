package tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import client.PlayerSnake;
import server.GameState;
import server.PlayerDetails;
import server.UserDatabase;
import server.GameServer;

class UserDatabaseTest {

	// test variables
	private GameState gameBoard;
	private PlayerSnake playerSnake;
	private GameServer snakeGame;
	private PlayerDetails playerDetails;
	private UserDatabase userDatabase;

	@BeforeAll
	public void beforeAll() {

	}

	@BeforeEach
	public void beforeEach() {
		gameBoard = mock(GameState.class, RETURNS_DEEP_STUBS);
		playerSnake = mock(PlayerSnake.class, RETURNS_DEEP_STUBS);
		playerDetails = mock(PlayerDetails.class, RETURNS_DEEP_STUBS);
		userDatabase = new UserDatabase();
		playerDetails = new PlayerDetails("Ryan", "123");
		
	}

	@AfterAll
	public static void ReportCard() {
		System.out.println("Tests complete");
	}

	@Test
	public void testPass() {
		assertTrue(true);
	}

	@Test
	public void testFail() {
		assertTrue(false);
	}

	// =============================== TESTS =================================
	@Test
	void correctLogin() {
		int encryptionKey = 5;
		userDatabase = new UserDatabase();
		playerDetails = new PlayerDetails("Ryan", "123");
		assertTrue(userDatabase.authenticate(playerDetails, encryptionKey));
	}

	@Test
	void incorrectUser() {
		int encryptionKey = 5;
		userDatabase = new UserDatabase();
		playerDetails = new PlayerDetails("Bryan", "123");
		assertTrue(userDatabase.authenticate(playerDetails, encryptionKey));
	}

	@Test
	void incorrectPassword() {
		int encryptionKey = 5;
		userDatabase = new UserDatabase();
		playerDetails = new PlayerDetails("Ryan", "abc");
		assertTrue(userDatabase.authenticate(playerDetails, encryptionKey));
	}
	
	@Test
	void incorrectEncryptionKey() {
		int encryptionKey = 5;
		userDatabase = new UserDatabase();
		playerDetails = new PlayerDetails("Ryan", "abc");
		assertTrue(userDatabase.authenticate(playerDetails, 6));
	}
}
