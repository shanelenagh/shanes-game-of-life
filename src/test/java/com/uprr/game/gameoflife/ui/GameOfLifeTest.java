package com.uprr.game.gameoflife.ui;

import java.awt.Button;
import java.awt.Component;
import java.awt.Frame;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.security.Permission;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import static org.junit.Assert.*;

import com.uprr.game.gameoflife.Cell;
import com.uprr.game.gameoflife.Game;
import com.uprr.game.gameoflife.test.TestUtil;

public class GameOfLifeTest {

	private GameOfLife gameOfLife;
	private Game game;
	private DummyGameGridSpy gameGridSpy;
	
	private class DummyGameGridSpy extends GameGrid {
				
		public int tickCounter = 0;
		public boolean hasBeenReset = false;
		public boolean gridRedrawn = false;
		
		public DummyGameGridSpy(int width, int height, int cellSize, Game game) {
			super(width, height, cellSize, game);
		}
		
		public void doTick() {
			tickCounter++;
		}
		
		public void reset() {
			hasBeenReset = true;
		}
		
		public void redrawGrid() {
			gridRedrawn = true;
		}
		
	}
	
	@Before
	public void setUp() {
		game = new Game();
		gameGridSpy = new DummyGameGridSpy(20, 20, 10, game) {};
		gameOfLife = new GameOfLife(gameGridSpy);
	}
	
	@After
	public void tearDown() throws Exception {
		for (final Frame frame : Frame.getFrames()) {
			frame.dispose();
		}
	}
	
	@Test
	public void doTick_callsGridDoTickMethod() {
		
		assertEquals("Game grid initial tick counter (sanity)", 0, gameGridSpy.tickCounter);
		gameOfLife.doTick();
		assertEquals("Game grid tick counter after ticking", 1, gameGridSpy.tickCounter);
		
	}
	
	@Test
	public void reset_causesGridReset() {
		gameOfLife.reset();
		assertTrue("Spy's reset call flag", gameGridSpy.hasBeenReset);
	}
	
	@Test
	public void tickButton_causesGenerationAndGridTickToIncrease() throws Exception {
		
		Button tickButton = TestUtil.getPrivateField(gameOfLife, "tickButton");
		
		long initialGenCount = gameOfLife.getGenerationCount();
		long initialGridTickCount = gameGridSpy.tickCounter;
		
		gameOfLife.actionPerformed(new ActionEvent(tickButton, new Object().hashCode(), "Tick"));
		
		assertEquals("Gen count after tick (should be one more)", initialGenCount+1, gameOfLife.getGenerationCount());
		assertEquals("Grid tick count after tick (should be one more)", initialGridTickCount+1, gameGridSpy.tickCounter);
		
	}	
	
	@Test
	public void runStopButtons_causesGenerationAndGridTickToIncrease() throws Exception {
		
		Button runStopButton = TestUtil.getPrivateField(gameOfLife, "runStopButton");
		TextField delayField = TestUtil.getPrivateField(gameOfLife, "delayField");
		
		long genCount = gameOfLife.getGenerationCount();
		long gridTickCount = gameGridSpy.tickCounter;
		delayField.setText("5");
		
		gameOfLife.actionPerformed(new ActionEvent(runStopButton, new Object().hashCode(), "Start"));
		Thread.sleep(75);
		gameOfLife.actionPerformed(new ActionEvent(runStopButton, new Object().hashCode(), "Start"));
		
		assertTrue("Gen count at least 2 more: " + (gameOfLife.getGenerationCount()-genCount), (gameOfLife.getGenerationCount()-genCount) >= 2);
		assertTrue("Grid tick count at least 2 more: " + (gameGridSpy.tickCounter-gridTickCount), (gameGridSpy.tickCounter-gridTickCount) >= 2);
		
		genCount = gameOfLife.getGenerationCount();
		gridTickCount = gameGridSpy.tickCounter;
		
		Thread.sleep(75);
		
		assertEquals("Gen count change after stopping & waiting", genCount, gameOfLife.getGenerationCount());
		assertEquals("Grid tick count change after stopping & waiting", gridTickCount, gameGridSpy.tickCounter);
	}	

	@Test
	public void resetButton_pushCausesReturnToDefaults() throws Exception {
		
		Button resetButton = TestUtil.getPrivateField(gameOfLife, "resetButton");
		TextField delayField = TestUtil.getPrivateField(gameOfLife, "delayField");
		
		delayField.setText(Integer.toString(GameOfLife.DEFAULT_TICK_DELAY+3));
		gameGridSpy.setOriginCell(new Cell(10, 10));
		
		gameOfLife.actionPerformed(new ActionEvent(resetButton, new Object().hashCode(), "Reset"));
		
		assertTrue("Grid spy reset flag", gameGridSpy.hasBeenReset);
		assertEquals("Game generation count after reset", 0, gameOfLife.getGenerationCount());
		assertEquals("Delay text field val after reset", GameOfLife.DEFAULT_TICK_DELAY,
				Integer.parseInt(delayField.getText()));
		
	}
	
	@Test
	public void main_frameCreatedWithGameInIt() {
		
		Frame[] existingFrames = Frame.getFrames();
		GameOfLife.main(new String[] {});
		Frame[] frames = Frame.getFrames();
		
		assertEquals("Frames created by app", 1, frames.length-existingFrames.length);
		boolean gameInFrame = false;
		Component[] frameComponents = frames[frames.length-1].getComponents();
		for (int i = 0; i < frameComponents.length; i++)
			if (frameComponents[i] instanceof GameOfLife) {
				gameInFrame = true;
				break;
			}
		assertTrue("Frame has GameOfLife in it", gameInFrame);
		
	}
	
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    
    @Test
    public void main_closingFrameActuallyExits() {
		GameOfLife.main(new String[] {});
		Frame[] frames = Frame.getFrames();
		WindowListener wl = frames[frames.length-1].getWindowListeners()[0];
		exit.expectSystemExitWithStatus(0);
		wl.windowClosing(new WindowEvent(frames[0], WindowEvent.WINDOW_CLOSING));
    }        
	
	@Test
	public void resetButton_pushWhileRunningCausesStop() throws Exception {
		
		Button resetButton = TestUtil.getPrivateField(gameOfLife, "resetButton");
		TextField delayField = TestUtil.getPrivateField(gameOfLife, "delayField");
		
		delayField.setText("5");
		gameGridSpy.setOriginCell(new Cell(10, 10));
		gameOfLife.startGameRunning();
		
		gameOfLife.actionPerformed(new ActionEvent(resetButton, new Object().hashCode(), "Reset"));
		long genCount = gameOfLife.getGenerationCount();
		long gridTickCount = gameGridSpy.tickCounter;
		Thread.sleep(75);
		
		assertEquals("Gen count (not incrementing) after reset",
				genCount, gameOfLife.getGenerationCount());
		assertEquals("Tick count (not incrementing) after reset",
				gridTickCount, gameGridSpy.tickCounter);
		
	}	

	@Test
	public void actionPerformed_bogusActionDoesntResetTickOrRun() throws Exception {	// yeah, this one was for 100% branch coverage ;-)

		long genCount = gameOfLife.getGenerationCount();
		long gridTickCount = gameGridSpy.tickCounter;
		
		gameOfLife.actionPerformed(new ActionEvent(new Object(), new Object().hashCode(), "Bogus"));
		
		assertEquals("Tick count after reset (should be the same)",
				gridTickCount, gameGridSpy.tickCounter);
		assertEquals("Gen count after reset (should be the same)",
				genCount, gameOfLife.getGenerationCount());
		assertFalse("Running flag", ((Boolean)TestUtil.getPrivateField(gameOfLife, "isRunning")));
		
	}	
	
	@Test
	public void delayField_nonNumericEntryCausesRevertToDefault() throws Exception {
		
		TextField delayField = TestUtil.getPrivateField(gameOfLife, "delayField");
		
		delayField.setText("asdf");
		gameGridSpy.setOriginCell(new Cell(10, 10));
		gameOfLife.startGameRunning();
		
		assertEquals("Delay field after starting with non-numeric entry",
				Integer.toString(GameOfLife.DEFAULT_TICK_DELAY), delayField.getText());
		
	}
	
	@Test
	public void delayField_negativeEntryCausesRevertToDefault() throws Exception {
		
		TextField delayField = TestUtil.getPrivateField(gameOfLife, "delayField");
		
		delayField.setText("-20");
		gameGridSpy.setOriginCell(new Cell(10, 10));
		gameOfLife.startGameRunning();
		
		assertEquals("Delay field after starting with negative entry",
				Integer.toString(GameOfLife.DEFAULT_TICK_DELAY), delayField.getText());
		
	}		

	@Test
	public void delayField_zeroDelayWorks() throws Exception {
		
		TextField delayField = TestUtil.getPrivateField(gameOfLife, "delayField");
		
		delayField.setText("0");
		gameGridSpy.setOriginCell(new Cell(10, 10));
		gameOfLife.startGameRunning();
		
		assertEquals("Delay field after starting with negative entry",
				"0", delayField.getText());
		
	}	
	
	@Test
	public void componentResized_gridRedrawn() throws Exception {
		ComponentListener cl = TestUtil.getPrivateField(gameOfLife, "resizeHandler");
		cl.componentResized(new ComponentEvent(gameOfLife, ComponentEvent.COMPONENT_RESIZED));
		assertTrue("Grid redrawn", gameGridSpy.gridRedrawn);
	}
	
}
