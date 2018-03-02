package com.uprr.game.gameoflife.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.uprr.game.gameoflife.test.TestUtil;
import com.uprr.game.gameoflife.ui.GameGridNavigator.Direction;
import com.uprr.game.gameoflife.ui.GameGridNavigator.MoveType;

public class GameGridNavigatorTest implements GameGridNavigationListener {

	private GameGridNavigatorPaintSpy navigator;
	private static int BUTTON_SIZE = GameGridNavigator.DEFAULT_BUTTON_SIZE_PX;
	private Frame frame;
	private List<GameGridNavigationEvent> eventStore; 
	
	private class GameGridNavigatorPaintSpy extends GameGridNavigator {
		public boolean painted = false;
		public int repaintCtr = 0;
		
		public GameGridNavigatorPaintSpy(int buttonSize) {
			super(buttonSize);
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			painted = true;
		}
		
		public void repaint() {
			super.repaint();
			repaintCtr++;
		}
		
	}
	
	@Before
	public void setUp() {
		navigator = new GameGridNavigatorPaintSpy(BUTTON_SIZE);
		eventStore = new ArrayList<GameGridNavigationEvent>();
	}
	
	public void setUpUI() throws Exception {
		
		if (frame == null) {
			frame = new Frame("Game Grid Navigator Unit Test Frame");
			frame.add(navigator, BorderLayout.CENTER);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					frame.dispose();
				}
			});
			frame.pack();	
			frame.setVisible(true);
			frame.requestFocus();
		}
		
		while(!navigator.painted)
			Thread.sleep(5);		

	}	
	
	@After
	public void tearDown() {
		if (frame != null)
			frame.dispose();
	}
	
	@Test
	public void getPolygonEdgeLength_dotSizeIsZero() {
		
		Polygon dot = new Polygon(new int[] { 1 }, new int[] { 1 }, 1);
		assertEquals("Size of dot", 0, navigator.getPolygonEdgeLength(dot));
		
	}
	
	@Test
	public void getPolygonEdgeLength_verticalLineSize() {
		
		Polygon line = new Polygon(new int[] { 1, 1 }, new int[] { 2, 4 }, 2);
		assertEquals("line two dots apart", 2, navigator.getPolygonEdgeLength(line));
		
	}	
	
	@Test
	public void getPolygonEdgeLength_rectangle() {
		
		Polygon rect = new Polygon(new int[] { 1, 4, 4, 1 }, new int[] { 3, 3, 5, 5 }, 4);
		assertEquals("rect size", 10, navigator.getPolygonEdgeLength(rect));
		
	}
	
	@Test
	public void getPolygonEdgeLength_triangle() {
		
		Polygon tri = new Polygon(new int[] { 1, 2, 3 }, new int[] { 1, 3, 1 }, 3);
		assertEquals("triangle size", 6, navigator.getPolygonEdgeLength(tri));
		
	}
	
	@Test
	public void getMinimumSize_isFiveTimesButtonSize() {
		
		assertEquals("Minimum size", new Dimension(5*BUTTON_SIZE, 5*BUTTON_SIZE), navigator.getMinimumSize());
		
	}	
	
	
	@Test
	public void buttons_buttonPressesFireEvents() throws Exception {
	
		setUpUI();		
		
		navigator.addNavigationListener(this);
		
		validateButtonPressProducesEvent(Direction.NORTH_WEST, MoveType.PAGE);
		validateButtonPressProducesEvent(Direction.WEST, MoveType.CELL);
		//validateButtonPressProducesEvent(Direction.SOUTH, MoveType.CELL);

	}
	
	@Test
	public void buttons_hoverChangesBorder() throws Exception {
	
		setUpUI();		
		
		Robot robot = new Robot();	
		
		Direction buttonDir;
		MoveType buttonMoveType;
		
		buttonDir = Direction.NORTH_WEST;
		buttonMoveType = MoveType.PAGE;
		
		Point buttonBorderPoint = getButtonScreenPoint(buttonDir, buttonMoveType, 0, 0);
		
		Thread.sleep(500);	// Initial painting takes some time
		assertEquals("Pre-hover border (sanity)", Color.WHITE, robot.getPixelColor(buttonBorderPoint.x, buttonBorderPoint.y));
		
		hoverButton(buttonDir, buttonMoveType);
		Thread.sleep(20);
		
		assertFalse("Hover border color changed",
			Color.WHITE.equals(robot.getPixelColor(buttonBorderPoint.x, buttonBorderPoint.y)));

		leaveButton(buttonDir, buttonMoveType);
		Thread.sleep(20);
		
		assertEquals("Exit hover border", Color.WHITE, robot.getPixelColor(buttonBorderPoint.x, buttonBorderPoint.y));
		
	}
	
	@Test
	public void buttons_hoverSwitchButtonsAndPressFiresNewButtonEvent() throws Exception {
		
		Direction
			oldDirection = Direction.NORTH_WEST,
			newDirection = Direction.WEST;
		MoveType
			oldMoveType = MoveType.PAGE,
			newMoveType = MoveType.CELL;
		
		setUpUI();	
		
		eventStore.clear();
		
		navigator.addNavigationListener(this);

		hoverButton(oldDirection, oldMoveType);
		clickButton(newDirection, newMoveType);

		validateEvent(newDirection, newMoveType);
		
	}	
	
	@Test
	public void buttons_hoverTwiceThenMoveToNew_StoresLastButton() throws Exception {
		
		setUpUI();	
		
		hoverButton(Direction.NORTH_WEST, MoveType.PAGE);
		hoverButton(Direction.NORTH_WEST, MoveType.PAGE);
		leaveButton(Direction.NORTH_WEST, MoveType.PAGE);
		hoverButton(Direction.NORTH_WEST, MoveType.CELL);
		
		GameGridNavigator.NavButtonDefinition curButton =
			TestUtil.getPrivateField(navigator, "currentButton");
		
		assertEquals("Cur button dir", Direction.NORTH_WEST, curButton.dir);
		assertEquals("Cur button move type", MoveType.CELL, curButton.moveType);
	}	
	
	@Test
	public void buttons_hoverInSpaceLeavesNullButtonSelected() throws Exception {
		
		setUpUI();	
		
		navigator.processMouseMotionEvent(
				getButtonMouseMoveEvent(Direction.NORTH_WEST, MoveType.PAGE,
						134, 275));
		
		GameGridNavigator.NavButtonDefinition curButton = 
			TestUtil.getPrivateField(navigator, "currentButton");
		
		assertNull("Cur button", curButton);
	}	
	
	@Test
	public void processMouseEvent_unhandledEventDoesntRepaint() {
		
		int initialPaintCounter = navigator.repaintCtr;
		
		navigator.processMouseEvent(new MouseEvent(navigator, MouseEvent.MOUSE_WHEEL, System.currentTimeMillis(),
				MouseEvent.BUTTON1_DOWN_MASK, 0, 0, 1, false));
		
		assertEquals("Paint counter vs. initial", initialPaintCounter, navigator.repaintCtr);
	}
	
	@Test
	public void processMouseEvent_unhandledMouseMotionEventDoesntRepaint() {
		
		int initialPaintCounter = navigator.repaintCtr;
		
		navigator.processMouseMotionEvent(new MouseEvent(navigator, MouseEvent.MOUSE_WHEEL, System.currentTimeMillis(),
				MouseEvent.BUTTON1_DOWN_MASK, 0, 0, 1, false));
		
		assertEquals("Paint counter vs. initial", initialPaintCounter, navigator.repaintCtr);
	}	
	
	@Test
	public void MoveType_values_EqualExpected() {
		assertEquals("Values equal expected array", Arrays.asList(new MoveType[] { MoveType.CELL, MoveType.PAGE }), Arrays.asList(MoveType.values()));
	}

	@Test  /* Silly eclemma coverage hack */
	public void MoveType_valueOf_EqualExpected() {
		assertEquals("valueOf() CELL", MoveType.CELL, MoveType.valueOf("CELL"));
	}

	@Test  /* Silly eclemma coverage hack */
	public void Direction_valueOf_EqualExpected() { 
		assertEquals("valueOf() NORTH", Direction.NORTH, Direction.valueOf("NORTH"));
	}
	
	
	public void validateButtonPressProducesEvent(Direction dir, MoveType navType) throws Exception {
		
		if (eventStore.size() > 0)
			eventStore.clear();
		
		clickButton(dir, navType);
		
		validateEvent(dir, navType);
		
	}
	
	public void validateEvent(Direction dir, MoveType navType) {
		String buttonDesc = String.format("%s [dir=%s, moveType=%s]", GameGridNavigationEvent.class.getCanonicalName(),
				dir, navType);
		assertEquals("Event count after pushing button "+buttonDesc, 1, eventStore.size());
		GameGridNavigationEvent navEvent = eventStore.get(0);
		assertEquals("Event dir after pushing button "+buttonDesc, dir, navEvent.getDirection());
		assertEquals("Event move type after pushing button "+buttonDesc, navType, navEvent.getMoveType());
		assertEquals("Event string", buttonDesc, navEvent.toString());
	}
	
	public void navigate(GameGridNavigationEvent ggne) {
		eventStore.add(ggne);
	}
	
	private void clickButton(Direction dir, MoveType navType) throws Exception {
		
		navigator.processMouseMotionEvent(getButtonMouseMoveEvent(dir, navType, 0, 0));
		navigator.processMouseEvent(getButtonMousePress(dir, navType));
		Thread.sleep(20);
		navigator.processMouseEvent(getButtonMouseRelease(dir, navType));
		
	}	

	private void hoverButton(Direction dir, MoveType navType) {
		
		navigator.processMouseMotionEvent(getButtonMouseMoveEvent(dir, navType, 0, 0));
		
	}	

	private void leaveButton(Direction dir, MoveType navType) {
		
		navigator.processMouseMotionEvent(getButtonMouseMoveEvent(dir, navType, BUTTON_SIZE, BUTTON_SIZE));
		navigator.processMouseEvent(getButtonMouseExit(dir, navType));
		
		
	}	
	
	private MouseEvent getButtonMousePress(Direction dir, MoveType moveType) {
		
		Point buttonPoint = getButtonMidPoint(dir, moveType);
		return new MouseEvent(navigator, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
				MouseEvent.BUTTON1_DOWN_MASK, buttonPoint.x, buttonPoint.y, 1, false);
		
	}

	private MouseEvent getButtonMouseRelease(Direction dir, MoveType moveType) {
		
		Point buttonPoint = getButtonMidPoint(dir, moveType);
		return new MouseEvent(navigator, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
				MouseEvent.BUTTON1_DOWN_MASK, buttonPoint.x, buttonPoint.y, 1, false);
		
	}	

	private MouseEvent getButtonMouseExit(Direction dir, MoveType moveType) {
		
		Point buttonPoint = getButtonMidPoint(dir, moveType);
		return new MouseEvent(navigator, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(),
				0, buttonPoint.x+BUTTON_SIZE, buttonPoint.y+BUTTON_SIZE, 1, false);
		
	}	
	
	private MouseEvent getButtonMouseMoveEvent(Direction dir, MoveType moveType, int x, int y) {
		
		Point buttonPoint = getButtonPoint(dir, moveType, x, y);
		return new MouseEvent(navigator, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(),
				0, buttonPoint.x, buttonPoint.y, 0, false);
		
	}
	
	private Point getButtonMidPoint(Direction dir, MoveType moveType) {
		
		return getButtonPoint(dir, moveType,
				GameGridNavigator.DEFAULT_BUTTON_SIZE_PX/2,
				GameGridNavigator.DEFAULT_BUTTON_SIZE_PX/2);
		
	}
	
	private Point getButtonPoint(Direction dir, MoveType moveType, int x, int y) {
		
		int BUTTON_X = 0, BUTTON_Y = 0;
		
		if (dir == Direction.NORTH_WEST && moveType == MoveType.PAGE) {
			BUTTON_X = 0;
			BUTTON_Y = 0;
		} else if (dir == Direction.NORTH_WEST && moveType == MoveType.CELL) {
			BUTTON_X = GameGridNavigator.DEFAULT_BUTTON_SIZE_PX;
			BUTTON_Y = GameGridNavigator.DEFAULT_BUTTON_SIZE_PX;			
		} else if (dir == Direction.WEST && moveType == MoveType.CELL) {
			BUTTON_X = GameGridNavigator.DEFAULT_BUTTON_SIZE_PX;
			BUTTON_Y = GameGridNavigator.DEFAULT_BUTTON_SIZE_PX*2;			
		} else
			throw new IllegalArgumentException("Not implemented");
		
		return new Point(BUTTON_X+x, BUTTON_Y+y);
		
	}
	
	private Point getButtonScreenPoint(Direction dir, MoveType moveType, int x, int y) {
		
		Point relativePoint = getButtonPoint(dir, moveType, x, y);
		
		Point screenLocPoint = navigator.getLocationOnScreen();
		
		return new Point(screenLocPoint.x+relativePoint.x,
			screenLocPoint.y+relativePoint.y);
		
	}	
	
}	
