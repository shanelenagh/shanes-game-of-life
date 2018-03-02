package com.uprr.game.gameoflife.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;

import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.uprr.game.gameoflife.Cell;
import com.uprr.game.gameoflife.Game;
import com.uprr.game.gameoflife.ui.GameGridNavigator.Direction;
import com.uprr.game.gameoflife.ui.GameGridNavigator.MoveType;

import static org.junit.Assert.*;

public class GameGridTest {
	
	private GameGridSpy gameGrid;
	private GameSpy gameSpy;
	private Frame frame;
	private static final int
		GRID_WIDTH = 10,
		GRID_HEIGHT = 10,
		CELL_SIZE = 10;
	
	private class GameSpy extends Game {
		
		public int tickCounter = 0;
		public boolean hasBeenReset = false;
		
		public void tick() {
			tickCounter++;
			super.tick();
		}
		
		public void reset() {
			hasBeenReset = true;
		}		
	}
	
	private class GameGridSpy extends GameGrid {
		public boolean painted = false;
		public List<Cell> clearedCells = new ArrayList<Cell>();
		public List<Cell> filledCells = new ArrayList<Cell>();
		
		public GameGridSpy(int width, int height, int cellSize, Game game) {
			super(width, height, cellSize, game);
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			painted = true;
		}
		
		protected void clearCell(Cell cell, Graphics g) {
			super.clearCell(cell, g);
			clearedCells.add(cell);
		}
		
		protected void fillCell(Cell cell, Graphics g) {
			super.fillCell(cell, g);
			filledCells.add(cell);
		}
		
		public void redrawGridNow() throws InterruptedException {
			painted = false;
			super.redrawGrid();
			while (!painted)
				Thread.sleep(5);
		}
	}
	
	@Before
	public void setUp() {
		
		gameSpy = new GameSpy();
		gameGrid = new GameGridSpy(GRID_WIDTH, GRID_HEIGHT, CELL_SIZE, gameSpy);
		
	}
	
	@After
	public void tearDown() {
		if (frame != null)
			frame.dispose();
	}
	
	public void setUpUI()  {
		
		frame = new Frame("Game Grid Unit Test Frame");
		frame.add(gameGrid, BorderLayout.CENTER);
		frame.pack();
		
		frame.addWindowListener(new WindowListener() {

			public void windowOpened(WindowEvent e) {
				Thread.currentThread().interrupt();
			}

			public void windowActivated(WindowEvent e) { }
			public void windowClosed(WindowEvent e) { }
			public void windowClosing(WindowEvent e) { }
			public void windowDeactivated(WindowEvent e) { }
			public void windowDeiconified(WindowEvent e) { }
			public void windowIconified(WindowEvent e) { }
		});		

		frame.setVisible(true);	
		frame.requestFocus();
		
		
	}
	
	@Test
	public void getMinimumSize_sizedByNumberOfCellsAndCellSize () {
		assertEquals("dimension of test grid",
				new Dimension(GRID_WIDTH*CELL_SIZE, GRID_HEIGHT*CELL_SIZE),
				gameGrid.getMinimumSize());
	}
	
	@Test
	public void getCellForCoordinates_nearOrigin() {
		assertEquals(new Cell(0, 0), gameGrid.getCellForCoordinate(5, 5));
	}

	@Test
	public void getCellForCoordinates_middleOfRemoteCell() {
		assertEquals(new Cell(2, 2), gameGrid.getCellForCoordinate(25, 25));
	}

	@Test
	public void getCellForCoordinates_onCorner() {
		assertEquals(new Cell(3, 3), gameGrid.getCellForCoordinate(30, 30));
	}

	@Test
	public void getCellForCoordinates_midCellAfterOriginMoveLowerRight() {
		Cell newOrigin = new Cell(GRID_WIDTH, GRID_HEIGHT);
		gameGrid.setOriginCell(newOrigin);
		assertEquals(new Cell(GRID_WIDTH+2, GRID_HEIGHT+2), gameGrid.getCellForCoordinate(25, 25));
	}
	
	@Test
	public void isCellVisible_offScreenCellYieldsFalse() {
		assertFalse(gameGrid.isCellVisible(new Cell(GRID_WIDTH+1, GRID_HEIGHT+1)));
	}
	
	@Test
	public void isCellVisible_originYieldsTrue() {
		assertTrue(gameGrid.isCellVisible(new Cell(0, 0)));
	}	
	
	@Test
	public void isCellVisible_onScreenCellYieldsTrue() {
		assertTrue(gameGrid.isCellVisible(new Cell(GRID_WIDTH-2, GRID_HEIGHT-2)));
	}

	@Test
	public void isCellVisible_moveOriginByScreenSizeYieldsFalse() {
		gameGrid.setOriginCell(new Cell(GRID_WIDTH, GRID_HEIGHT));
		assertFalse("Moved off to southeast", gameGrid.isCellVisible(new Cell(GRID_WIDTH-2, GRID_HEIGHT-2)));
		gameGrid.setOriginCell(new Cell(-GRID_WIDTH, GRID_HEIGHT));
		assertFalse("Moved off to southwest", gameGrid.isCellVisible(new Cell(GRID_WIDTH-2, GRID_HEIGHT-2)));
		gameGrid.setOriginCell(new Cell(GRID_WIDTH, -GRID_HEIGHT));
		assertFalse("Moved off to northeast", gameGrid.isCellVisible(new Cell(GRID_WIDTH-2, GRID_HEIGHT-2)));
		gameGrid.setOriginCell(new Cell(-GRID_WIDTH, -GRID_HEIGHT));
		assertFalse("Moved off to northwest", gameGrid.isCellVisible(new Cell(GRID_WIDTH-2, GRID_HEIGHT-2)));
		gameGrid.setOriginCell(new Cell(0, -GRID_HEIGHT));
		assertFalse("Moved off to north", gameGrid.isCellVisible(new Cell(GRID_WIDTH-2, GRID_HEIGHT-2)));
		gameGrid.setOriginCell(new Cell(GRID_WIDTH, 0));
		assertFalse("Moved off to east", gameGrid.isCellVisible(new Cell(GRID_WIDTH-2, GRID_HEIGHT-2)));
		gameGrid.setOriginCell(new Cell(0, GRID_HEIGHT));
		assertFalse("Moved off to south", gameGrid.isCellVisible(new Cell(GRID_WIDTH-2, GRID_HEIGHT-2)));
		gameGrid.setOriginCell(new Cell(-GRID_WIDTH, 0));
		assertFalse("Moved off to west", gameGrid.isCellVisible(new Cell(GRID_WIDTH-2, GRID_HEIGHT-2)));
	}


	
	@Test
	public void drawGridLines() throws AWTException, InterruptedException {
		
		setUpUI();
		
		Robot robot = new Robot();
		
		while(!gameGrid.painted)
			Thread.sleep(300);
		
		int gridX, gridY, actualX, actualY;
		Point gridLocation = gameGrid.getLocationOnScreen();
		
		// verify vertical lines
		gridY = GRID_HEIGHT / 2;
		for (gridX = 0; gridX < gameGrid.getWidth(); gridX++) {
			actualX = gridLocation.x+gridX;
			actualY = gridLocation.y+gridY;
			Color pixelColor = robot.getPixelColor(actualX, actualY);
			if (gridX % CELL_SIZE == 0)
				assertEquals(
					String.format("Vert cell boundary color at x=%d, y=%d", actualX, actualY),
					Color.BLACK,
					pixelColor);
			else
				assertEquals(
					String.format("Vert cell interior color at x=%d, y=%d", actualX, actualY),
					Color.WHITE,
					pixelColor);
		}
	
		// verify horizontal lines
		gridX = GRID_WIDTH / 2;
		for (gridY = 0; gridY < gameGrid.getHeight(); gridY++) {
			actualX = gridLocation.x+gridX;
			actualY = gridLocation.y+gridY;
			Color pixelColor = robot.getPixelColor(actualX, actualY);
			if (gridY % CELL_SIZE == 0)
				assertEquals(
					String.format("Horiz cell boundary color at x=%d, y=%d", actualX, actualY),
					Color.BLACK,
					pixelColor);
			else
				assertEquals(
					String.format("Horiz cell interior color at x=%d, y=%d", actualX, actualY),
					Color.WHITE,
					pixelColor);
		}		
	}

	@Test
	public void fillCell_filledCellIsBlack() throws Exception {
		
		final int FILLED_CELL_X = 5, FILLED_CELL_Y = 5;
		
		setUpUI();
		
		while(!gameGrid.painted)
			Thread.sleep(300);			
		
		Point gridLocation = gameGrid.getLocationOnScreen();
		int cellX = gridLocation.x + FILLED_CELL_X*gameGrid.cellSize + 1;
		int cellY = gridLocation.y + FILLED_CELL_Y*gameGrid.cellSize + 1;
		
		Robot robot = new Robot();
		
		// sanity
		assertEquals("Unfilled cell color (sanity)", Color.WHITE, robot.getPixelColor(cellX, cellY));
		
		Cell liveCell = new Cell(FILLED_CELL_X, FILLED_CELL_Y);
		gameSpy.bringCellToLife(liveCell);
		gameGrid.fillCell(liveCell);
		robot.waitForIdle();
		Thread.sleep(20);

		assertEquals("Filled cell color", Color.BLACK, robot.getPixelColor(cellX, cellY));
	}	

	@Test
	public void clearCell_clearedCellIsWhite() throws Exception {
		
		final int CLEARED_CELL_X = 5, CLEARED_CELL_Y = 5;
		Cell clearedCell = new Cell(CLEARED_CELL_X, CLEARED_CELL_Y);
		
		setUpUI();		
		
		while(!gameGrid.painted)
			Thread.sleep(300);		
		
		Robot robot = new Robot();

		gameSpy.bringCellToLife(clearedCell);
		gameGrid.fillCell(clearedCell);
		robot.waitForIdle();
		Thread.sleep(25);
		
		Point clearedCellCenter = getGridCellCenter(clearedCell);
		
		assertEquals("Filled cell color (sanity)", Color.BLACK, robot.getPixelColor(clearedCellCenter.x, clearedCellCenter.y));
		
		gameGrid.clearCell(clearedCell);
		robot.waitForIdle();
		Thread.sleep(30);

		assertEquals("Cleared cell color", Color.WHITE, robot.getPixelColor(clearedCellCenter.x, clearedCellCenter.y));
	}	

	
	@Test
	public void mousePressed_liveCellIsKilled() {
		
		Cell containingCell = new Cell(2, 3);
		gameSpy.bringCellToLife(containingCell);
		MouseListener ml = gameGrid.getMouseListeners()[0];

		setUpUI();
		
		final int IN_CELL_X = 25, IN_CELL_Y = 35;
		ml.mousePressed(new MouseEvent(gameGrid, 0, System.currentTimeMillis(), 0,
			IN_CELL_X, IN_CELL_Y, 1, false, MouseEvent.BUTTON1));
		assertFalse("Cell no longer there", gameSpy.isCellAlive(containingCell));
	}

	@Test
	public void mousePressed_deadCellIsBroughtToLife() {

		Cell newCell = new Cell(2, 3);
		MouseListener ml = gameGrid.getMouseListeners()[0];
		
		setUpUI();
		
		assertFalse("Cell is empty (sanity)", gameSpy.isCellAlive(newCell));
		final int IN_CELL_X = 25, IN_CELL_Y = 35;
		ml.mousePressed(new MouseEvent(gameGrid, 0, System.currentTimeMillis(), 0,
			IN_CELL_X, IN_CELL_Y, 1, false, MouseEvent.BUTTON1));
		assertTrue("Cell now alive", gameSpy.isCellAlive(newCell));
	}
	
	@Test
	public void reset_originReset() {
		gameGrid.setOriginCell(new Cell(10, 20));
		gameGrid.reset();
		assertEquals("Origin after reset", new Cell(0, 0), gameGrid.getOriginCell());	
	}

	@Test
	public void reset_gameResetToo() {
		gameGrid.reset();
		assertTrue("Game spy reset flag", gameSpy.hasBeenReset);	
	}

	@Test
	public void doTick_gameTickedToo() {
		gameGrid.doTick();
		assertEquals("Game spy tick count", 1, gameSpy.tickCounter);	
	}	

	@Test
	public void doTick_threeVisibleCellsInLineRedrawn() throws Exception {
		
		Cell cellBetween = new Cell(5, 5);
		Cell cellLeftSide = new Cell(4, 5);
		Cell cellRightSide = new Cell(6, 5);
		
		setUpUI();
		
		Point betweenCellPoint = getGridCellCenter(cellBetween);
		Point leftCellPoint = getGridCellCenter(cellLeftSide);
		Point rightCellPoint = getGridCellCenter(cellRightSide);
		
		Robot robot = new Robot();
		
		gameSpy.bringCellToLife(cellBetween);
		gameSpy.bringCellToLife(cellLeftSide);
		gameSpy.bringCellToLife(cellRightSide);
		gameGrid.redrawGridNow();
		Thread.sleep(300);
		
		assertEquals("Between cell color (sanity)", Color.BLACK, robot.getPixelColor(betweenCellPoint.x, betweenCellPoint.y));
		assertEquals("Right cell color (sanity)", Color.BLACK, robot.getPixelColor(rightCellPoint.x, rightCellPoint.y));
		assertEquals("Left cell color (sanity)", Color.BLACK, robot.getPixelColor(leftCellPoint.x, leftCellPoint.y));
		
		gameGrid.doTick();
		robot.waitForIdle();
		Thread.sleep(30);
		
		assertEquals("Between cell color after tick", Color.BLACK, robot.getPixelColor(betweenCellPoint.x, betweenCellPoint.y));
		assertEquals("Right cell color after tick", Color.WHITE, robot.getPixelColor(rightCellPoint.x, rightCellPoint.y));
		assertEquals("Left cell color after tick", Color.WHITE, robot.getPixelColor(leftCellPoint.x, leftCellPoint.y));
	}		
	
	@Test
	public void doTick_offscreenCellsNotFilledOrCleared() throws Exception {
		
		Cell cellBetween = new Cell(305, 5);
		Cell cellLeftSide = new Cell(304, 5);
		Cell cellRightSide = new Cell(306, 5);
		Cell cellUpperRightSide = new Cell(306, 4);
		
		gameSpy.bringCellToLife(cellBetween);
		gameSpy.bringCellToLife(cellLeftSide);
		gameSpy.bringCellToLife(cellRightSide);
		gameSpy.bringCellToLife(cellUpperRightSide);

		gameGrid.doTick();
		
		assertEquals("Cleared cell count", 0, gameGrid.filledCells.size());
		assertEquals("Filled cell count", 0, gameGrid.clearedCells.size());
		
		
	}
	
	@Test
	public void navigate_eventCausesGridOriginTranslation() {
		
		// Navigate by one cell
		gameGrid.setOriginCell(new Cell(0, 0));	// just to ensure baseline
		gameGrid.navigate(new GameGridNavigationEvent(Direction.NORTH, MoveType.CELL));
		assertEquals("Grid orgin after nav CELL, NORTH", new Cell(0, -1), gameGrid.getOriginCell());
		
		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.SOUTH, MoveType.CELL));
		assertEquals("Grid orgin after nav CELL, SOUTH", new Cell(0, 1), gameGrid.getOriginCell());
		
		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.EAST, MoveType.CELL));
		assertEquals("Grid orgin after nav CELL, EAST", new Cell(1, 0), gameGrid.getOriginCell());
		
		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.WEST, MoveType.CELL));
		assertEquals("Grid orgin after nav CELL, WEST", new Cell(-1, 0), gameGrid.getOriginCell());
		
		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.NORTH_EAST, MoveType.CELL));
		assertEquals("Grid orgin after nav CELL, NORTH_EAST", new Cell(1, -1), gameGrid.getOriginCell());
		
		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.NORTH_WEST, MoveType.CELL));
		assertEquals("Grid orgin after nav CELL, NORTH_WEST", new Cell(-1, -1), gameGrid.getOriginCell());

		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.SOUTH_EAST, MoveType.CELL));
		assertEquals("Grid orgin after nav CELL, SOUTH_EAST", new Cell(1, 1), gameGrid.getOriginCell());

		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.SOUTH_WEST, MoveType.CELL));
		assertEquals("Grid orgin after nav CELL, SOUTH_WEST", new Cell(-1, 1), gameGrid.getOriginCell());
		
		// Navigate by one page
		gameGrid.setOriginCell(new Cell(0, 0));	// just to ensure baseline
		gameGrid.navigate(new GameGridNavigationEvent(Direction.NORTH, MoveType.PAGE));
		assertEquals("Grid orgin after nav PAGE, NORTH", new Cell(0, -gameGrid.getGridHeight()), gameGrid.getOriginCell());
		
		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.SOUTH, MoveType.PAGE));
		assertEquals("Grid orgin after nav PAGE, SOUTH", new Cell(0, gameGrid.getGridHeight()), gameGrid.getOriginCell());
		
		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.EAST, MoveType.PAGE));
		assertEquals("Grid orgin after nav PAGE, EAST", new Cell(gameGrid.getGridWidth(), 0), gameGrid.getOriginCell());
		
		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.WEST, MoveType.PAGE));
		assertEquals("Grid orgin after nav PAGE, WEST", new Cell(-gameGrid.getGridWidth(), 0), gameGrid.getOriginCell());
		
		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.NORTH_EAST, MoveType.PAGE));
		assertEquals("Grid orgin after nav PAGE, NORTH_EAST", new Cell(gameGrid.getGridWidth(), -gameGrid.getGridHeight()), gameGrid.getOriginCell());
		
		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.NORTH_WEST, MoveType.PAGE));
		assertEquals("Grid orgin after nav PAGE, NORTH_WEST", new Cell(-gameGrid.getGridWidth(), -gameGrid.getGridHeight()), gameGrid.getOriginCell());

		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.SOUTH_EAST, MoveType.PAGE));
		assertEquals("Grid orgin after nav PAGE, SOUTH_EAST", new Cell(gameGrid.getGridWidth(), gameGrid.getGridHeight()), gameGrid.getOriginCell());

		gameGrid.setOriginCell(new Cell(0, 0));	
		gameGrid.navigate(new GameGridNavigationEvent(Direction.SOUTH_WEST, MoveType.PAGE));
		assertEquals("Grid orgin after nav PAGE, SOUTH_WEST", new Cell(-gameGrid.getGridWidth(), gameGrid.getGridHeight()), gameGrid.getOriginCell());
		
		// Center
		gameGrid.setOriginCell(new Cell(10, -20));
		gameGrid.navigate(new GameGridNavigationEvent(null, MoveType.PAGE));
		assertEquals("Grid orgin after nav PAGE, CENTER (null)", new Cell(0, 0), gameGrid.getOriginCell());
		
		
	}	
	
	private Point getGridCellCenter(Cell cell) {
		
		Point gridLocation = gameGrid.getLocationOnScreen();
		int cellMidAbsX = gridLocation.x + cell.getX()*gameGrid.cellSize
			+ (gameGrid.cellSize/2)*(cell.getX() < 0 ? -1 : 1);
		int cellMidAbsY = gridLocation.y + cell.getY()*gameGrid.cellSize
			+ (gameGrid.cellSize/2)*(cell.getY() < 0 ? -1 : 1);
		
		return new Point(cellMidAbsX, cellMidAbsY);
	}	
}
