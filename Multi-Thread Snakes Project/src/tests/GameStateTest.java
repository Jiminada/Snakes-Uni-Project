package tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.LinkedList;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import client.PlayerSnake;
import server.GameServer;
import server.GameState;
import server.GameState.SnakeModel;
import server.PlayerDetails;
import server.UserDatabase;

public class GameStateTest {
	
	// test variables
	private GameState testGameBoard;
	private PlayerSnake mockPlayerSnake;

	@BeforeAll
	public static void beforeAll() {		

	}

	@BeforeEach
	public void beforeEach() {
		testGameBoard = new GameState();
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

	@Test
	public void getNewHead() {		

	}
	
	@Test
	public void givenSnakeOccupiesTest() {
//		int snakeId = 1;
//		GameServer gameserver = new GameServer(1, 5);
//		PlayerSnake snake = new PlayerSnake(gameserver, snakeId, 5);
//		int[] startLocation = {1,1};	
//		GameState gamestate = new GameState();
//		SnakeModel snakemodel = gamestate.new SnakeModel(startLocation, snake);
//		
//		gamestate.getSnakeModels().put(snakeId, snakemodel);
//		
//
//		assertTrue(gamestate.givenSnakeOccupies(1, 1, 1));
		
		LinkedList<int[]> testlist = new LinkedList<int[]>();
		
		int[] coordinate = {1,1};
		
		testlist.add(coordinate);
		coordinate[0] = 7;
		coordinate[1] = 5;
		
//		testlist.add(coordinate);
		
		int[] testcoordinate = {1,1};
		
		assertTrue(testlist.contains(coordinate)); // tests for memory loccation not value
		assertTrue(testlist.contains(testcoordinate));
		

	}
	
	@Test
	public void addSnakeModel() {
//		int snakeID0 = testGameBoard.addSnakeModel();
//		int snakeID1 = testGameBoard.addSnakeModel();
//		int snakeID2 = testGameBoard.addSnakeModel();
//		int snakeID3 = testGameBoard.addSnakeModel();
//		int snakeID4 = testGameBoard.addSnakeModel();
//		assertEquals(0, snakeID0);
//		assertEquals(1, snakeID1);
//		assertEquals(2, snakeID2);
//		assertEquals(3, snakeID3);
//		assertEquals(4, snakeID4);
	}
}
