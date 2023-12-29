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
import server.GameServer;

public class GameServerTest {

	// test variables
	private GameState mockGameBoard;
	private PlayerSnake mockPlayerSnake;
	private GameServer snakeGame;

	@BeforeAll
	public static void beforeAll() {

	}

	@BeforeEach
	public void beforeEach() {
		mockGameBoard = mock(GameState.class, RETURNS_DEEP_STUBS);
		mockPlayerSnake = mock(PlayerSnake.class, RETURNS_DEEP_STUBS);
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
	
	//=============================== TESTS =================================
}
