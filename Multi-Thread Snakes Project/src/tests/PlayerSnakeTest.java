package tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import abstractClasses.Snake.Direction;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import client.PlayerSnake;
import exceptions.EmptyBufferException;
import server.GameServer;

public class PlayerSnakeTest {

	// test variables
	private GameServer mockSnakeGame;
	private PlayerSnake testPlayerSnake;

	@BeforeAll
	public static void beforeAll() {

	}

	@BeforeEach
	public void beforeEach() {
		testPlayerSnake = new PlayerSnake(mockSnakeGame, 0, 0);
		mockSnakeGame = mock(GameServer.class, RETURNS_DEEP_STUBS);
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

	@Test
	public void validPlayerID() {
	}

	@Test
	public void DirectionBuffer() throws InterruptedException, EmptyBufferException {
		testPlayerSnake.directionBufferProduce(Direction.UP);
		Direction first = testPlayerSnake.directionBufferConsume();

		testPlayerSnake.directionBufferProduce(Direction.DOWN);
		Direction second = testPlayerSnake.directionBufferConsume();

		testPlayerSnake.directionBufferProduce(Direction.LEFT);
		testPlayerSnake.directionBufferProduce(Direction.RIGHT); // try add to full buffer
		Direction third = testPlayerSnake.directionBufferConsume();

		testPlayerSnake.directionBufferProduce(Direction.UP);
		Direction fourth = testPlayerSnake.directionBufferConsume();

		assertEquals(first, Direction.UP);
		assertEquals(second, Direction.DOWN);
		assertEquals(third, Direction.LEFT);
		assertEquals(fourth, Direction.UP);
	}

	@Test
	public void BufferGetInitial() throws EmptyBufferException {
		Direction direction = testPlayerSnake.directionBufferConsume();
		// initial should be null
		assertEquals(null, direction);
	}

	@Test
	public void BufferGetUp() throws EmptyBufferException {
		Direction direction = testPlayerSnake.directionBufferConsume();
		// initial should be null
		assertEquals(null, direction);
	}
}
