package com.uprr.game.gameoflife.ui;

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * UI widget for navigating the infinite game grid plane
 * 
 * @author slenagh@up.com
 *
 */
class GameGridNavigator extends Canvas {
	
	private static final long serialVersionUID = GameGridNavigator.class.getCanonicalName().hashCode();
	
	public final static int DEFAULT_BUTTON_SIZE_PX = 16;
	private int buttonSizeInPixels;
	
	private Set<GameGridNavigationListener> navigationListeners;
	
	protected static enum Direction {
		NORTH,
		SOUTH,
		WEST,
		EAST,
		NORTH_WEST,
		NORTH_EAST,
		SOUTH_WEST,
		SOUTH_EAST
	}	
	
	protected static enum MoveType {
		CELL,
		PAGE
	}
	
	protected static class NavButtonDefinition {
		
		public final Rectangle area;
		public final Image
			normalImage,
			hoverImage,
			pressedImage;
		public final Direction dir;
		public final MoveType moveType;
		
		public NavButtonDefinition(Direction dir, MoveType moveType, Rectangle area,
				Image normalImage, Image hoverImage, Image pressedImage)
		{
			this.dir = dir;
			this.moveType = moveType;
			this.area = area;
			this.normalImage = normalImage;
			this.hoverImage = hoverImage;
			this.pressedImage = pressedImage;
		}
	}
	
	private NavButtonDefinition[] buttons;
	
	private boolean isOverButtonArea = false,
		isButtonPressed = false;
	private NavButtonDefinition currentButton;
		
	public GameGridNavigator(int buttonSizeInPixels) {

		navigationListeners = new HashSet<GameGridNavigationListener>();
		
		this.buttonSizeInPixels = buttonSizeInPixels;
		
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		
	}
	
	public GameGridNavigator() {
		
		this(DEFAULT_BUTTON_SIZE_PX);
		
	}	
	
	/**
	 * The preferred size of the control. 
	 */
	public Dimension getPreferredSize() {
		return new Dimension(buttonSizeInPixels*5, buttonSizeInPixels*5);
	}

	/**
	 * The minimum size of the control. 
	 */
	public Dimension getMinimumSize() {
		return new Dimension(buttonSizeInPixels*5, buttonSizeInPixels*5);
	}
	
	public void addNavigationListener(GameGridNavigationListener ggnl) {
		navigationListeners.add(ggnl);
	}
	
	protected void processNavigationEvent(GameGridNavigationEvent ggne) {
		for (GameGridNavigationListener navListener : navigationListeners)
			navListener.navigate(ggne);
	}
	
	/**
	 * Handles mouse clicks of buttons
	 */
	public void processMouseEvent(MouseEvent e) {
		
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			isButtonPressed = true;
			repaint();
			processNavigationEvent(
				new GameGridNavigationEvent(currentButton.dir, currentButton.moveType));
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			isButtonPressed = false;
			repaint();
		} else if (e.getID() == MouseEvent.MOUSE_EXITED) {
			isOverButtonArea = false;
			repaint();
		}
		super.processMouseEvent(e);
		
	}  
	   
   /**
    * Handles determining current button and repainting as mouse moves
    */
   public void processMouseMotionEvent(MouseEvent e) {
	   
	   switch(e.getID()) {
		   case MouseEvent.MOUSE_MOVED:
			   if (currentButton == null || !currentButton.area.contains(e.getX(), e.getY())) {
				   currentButton = null;
				   for (NavButtonDefinition button : buttons) {
					   if (button.area.contains(e.getX(), e.getY())) {
						   isOverButtonArea = true;
						   currentButton = button;
						   break;
					   }
				   }
				   repaint();
			   }
			   break;
	   }
	   super.processMouseMotionEvent(e);  
   }	
	
	
	public void paint(Graphics g) {
		
		if (buttons == null)
			loadButtons();

		for (NavButtonDefinition button : buttons)
			if (button == currentButton) 
				g.drawImage(
					isButtonPressed ? button.pressedImage : (isOverButtonArea ? button.hoverImage : button.normalImage),
					button.area.x, button.area.y, button.area.width,
					button.area.height, this);
			else
				g.drawImage(button.normalImage,button.area.x, button.area.y,
						button.area.width, button.area.height, this);

		
	}
	
	public void loadButtons() {
		
		buttons = new NavButtonDefinition[17];
		buttons[0] = createArrowButton("Up", Direction.NORTH, buttonSizeInPixels*2, buttonSizeInPixels);
		buttons[1] = createArrowButton("Down", Direction.SOUTH, buttonSizeInPixels*2, buttonSizeInPixels*3);
		buttons[2] = createArrowButton("Right", Direction.EAST, buttonSizeInPixels*3, buttonSizeInPixels*2);
		buttons[3] = createArrowButton("Left", Direction.WEST, buttonSizeInPixels, buttonSizeInPixels*2);
		buttons[4] = createCircleButton("Center", buttonSizeInPixels*2, buttonSizeInPixels*2);
		buttons[5] = createCornersButton("Page Up", Direction.NORTH, buttonSizeInPixels*2, 0);
		buttons[6] = createCornersButton("Page Down", Direction.SOUTH, buttonSizeInPixels*2, buttonSizeInPixels*4);
		buttons[7] = createCornersButton("Page Right", Direction.EAST, buttonSizeInPixels*4, buttonSizeInPixels*2);
		buttons[8] = createCornersButton("Page Left", Direction.WEST, 0, buttonSizeInPixels*2);
		buttons[9] = createCornersButton("Page Upper Right", Direction.NORTH_EAST, buttonSizeInPixels*4, 0);
		buttons[10] = createCornersButton("Page Upper Left", Direction.NORTH_WEST, 0, 0);
		buttons[11] = createCornersButton("Page Lower Right", Direction.SOUTH_EAST, buttonSizeInPixels*4, buttonSizeInPixels*4);
		buttons[12] = createCornersButton("Page Lower Left", Direction.SOUTH_WEST, 0, buttonSizeInPixels*4);
		buttons[13] = createArrowButton("Lower Right", Direction.SOUTH_EAST, buttonSizeInPixels*3, buttonSizeInPixels*3);
		buttons[14] = createArrowButton("Lower Left", Direction.SOUTH_WEST, buttonSizeInPixels, buttonSizeInPixels*3);
		buttons[15] = createArrowButton("Upper Right", Direction.NORTH_EAST, buttonSizeInPixels*3, buttonSizeInPixels);
		buttons[16] = createArrowButton("Upper Left", Direction.NORTH_WEST, buttonSizeInPixels, buttonSizeInPixels);
	}
	
	private NavButtonDefinition createArrowButton(String name, Direction dir, int x, int y) {
		
		NavButtonDefinition button;
		Graphics buttonGraphics;
		Image normalImage, hoverImage, pressedImage;
		Rectangle area;
		
		Polygon arrowShape = createArrowShape(dir, true);
		
		Polygon offsetArrowShape = new Polygon(arrowShape.xpoints, arrowShape.ypoints, 3);
		offsetArrowShape.translate(1, 1);
		
		area = new Rectangle(x, y, buttonSizeInPixels, buttonSizeInPixels);
		normalImage = createImage(area.width, area.height);
		buttonGraphics = normalImage.getGraphics();
		buttonGraphics.setColor(Color.BLACK);
		buttonGraphics.drawPolygon(arrowShape);
		hoverImage = createImage(area.width, area.height);
		buttonGraphics = hoverImage.getGraphics();
		buttonGraphics.setColor(Color.BLACK);
		buttonGraphics.drawPolygon(arrowShape);
		buttonGraphics.fillPolygon(arrowShape);
		buttonGraphics.setColor(Color.GRAY);
		buttonGraphics.draw3DRect(0, 0, area.width-1, area.height-1, true);
		pressedImage = createImage(area.width, area.height);
		buttonGraphics = pressedImage.getGraphics();
		buttonGraphics.setColor(Color.GREEN);
		buttonGraphics.drawPolygon(offsetArrowShape);
		buttonGraphics.fillPolygon(offsetArrowShape);
		buttonGraphics.setColor(Color.GRAY);
		buttonGraphics.draw3DRect(0, 0, area.width-1, area.height-1, false);
		button = new NavButtonDefinition(dir, MoveType.CELL, area, normalImage, hoverImage, pressedImage);	
		
		return button;
		
	}
	
	private Polygon createArrowShape(Direction dir, boolean sizeBySimilar) {
		
		int buttonCenterX = buttonSizeInPixels/2;
		int buttonCenterY = buttonSizeInPixels/2;
		int buttonBorderWidth = (int)(buttonSizeInPixels*.1);
		int moveOffset = 1;
		int topAndLeftSpacing = buttonBorderWidth + 1;
		int bottomAndRightSpacing = moveOffset + buttonBorderWidth + 1;
		Rectangle iconBounds = new Rectangle(topAndLeftSpacing, topAndLeftSpacing,
				buttonSizeInPixels - bottomAndRightSpacing - topAndLeftSpacing - 1,
				buttonSizeInPixels - bottomAndRightSpacing - topAndLeftSpacing - 1);
		
		Polygon[] similarArrowShapes;
		int similarShapeMaxLength = 0;
		if (sizeBySimilar) {
			similarArrowShapes = new Polygon[3];
			List<Direction> cardinalDirections = Arrays.asList(
					new Direction[] { Direction.NORTH, Direction.SOUTH, 
						Direction.EAST, Direction.WEST });
			List<Direction> interDirections = Arrays.asList(
					new Direction[] { Direction.NORTH_EAST, Direction.SOUTH_EAST, 
						Direction.NORTH_WEST, Direction.SOUTH_WEST });
			if (cardinalDirections.contains(dir)) {
				for (int i = 0, j = 0; i < cardinalDirections.size(); i++)
					if (!cardinalDirections.get(i).equals(dir))
						similarArrowShapes[j++] = createArrowShape(cardinalDirections.get(i), false);
			} else {
				for (int i = 0, j = 0; i < interDirections.size(); i++)
					if (!interDirections.get(i).equals(dir))
						similarArrowShapes[j++] = createArrowShape(interDirections.get(i), false);
			}
			for (int i = 0; i < similarArrowShapes.length; i++)
				similarShapeMaxLength = (getPolygonEdgeLength(similarArrowShapes[i]) > similarShapeMaxLength
						? getPolygonEdgeLength(similarArrowShapes[i]) : similarShapeMaxLength);
		}
		
		int leftXInc = 0, leftYInc = 0, rightXInc = 0, rightYInc = 0, tipXInc = 0, tipYInc = 0;
		
		switch (dir) {
		case NORTH:
			leftXInc = -2;
			leftYInc = 1;
			rightXInc = 2;
			rightYInc = 1;
			tipXInc = 0;
			tipYInc = -1;
			break;
		case SOUTH:
			leftXInc = -2;
			leftYInc = -1;
			rightXInc = 2;
			rightYInc = -1;
			tipXInc = 0;
			tipYInc = 1;
			break;
		case EAST:
			leftXInc = -1;
			leftYInc = -2;
			rightXInc = -1;
			rightYInc = 2;
			tipXInc = 1;
			tipYInc = 0;
			break;
		case WEST:
			leftXInc = 1;
			leftYInc = -2;
			rightXInc = 1;
			rightYInc = 2;
			tipXInc = -1;
			tipYInc = 0;
			break;
		case NORTH_EAST:
			leftXInc = -2;
			leftYInc = 0;
			rightXInc = 0;
			rightYInc = 2;
			tipXInc = 1;
			tipYInc = -1;
			break;
		case NORTH_WEST:
			leftXInc = 0;
			leftYInc = 2;
			rightXInc = 2;
			rightYInc = 0;
			tipXInc = -1;
			tipYInc = -1;
			break;
		case SOUTH_EAST:
			leftXInc = 0;
			leftYInc = -2;
			rightXInc = -2;
			rightYInc = 0;
			tipXInc = 1;
			tipYInc = 1;
			break;
		case SOUTH_WEST: default:
			leftXInc = 2;
			leftYInc = 0;
			rightXInc = 0;
			rightYInc = -2;
			tipXInc = -1;
			tipYInc = 1;
			break;		
		}

		
		Polygon arrowShape = new Polygon(
				new int[] { buttonCenterX+leftXInc, buttonCenterX+tipXInc, buttonCenterX+rightXInc},
				new int[] { buttonCenterY+leftYInc, buttonCenterY+tipYInc, buttonCenterY+rightYInc},
				3);			
		for (Polygon proposedArrowShape = arrowShape;
			iconBounds.contains(proposedArrowShape.getBounds())
				&& (!sizeBySimilar || (getPolygonEdgeLength(proposedArrowShape) <= similarShapeMaxLength));
			proposedArrowShape.invalidate())
		{
			arrowShape = new Polygon(proposedArrowShape.xpoints, proposedArrowShape.ypoints, 3);;
			proposedArrowShape.xpoints[0] += leftXInc;
			proposedArrowShape.xpoints[1] += tipXInc;
			proposedArrowShape.xpoints[2] += rightXInc;
			proposedArrowShape.ypoints[0] += leftYInc;
			proposedArrowShape.ypoints[1] += tipYInc;
			proposedArrowShape.ypoints[2] += rightYInc;
		}	
		
		return arrowShape;
		
	}
	
	public int getPolygonEdgeLength(Polygon poly) {
		
		int size = 0;
		Point firstPoint, currentPoint, previousPoint;
		firstPoint = new Point(poly.xpoints[0], poly.ypoints[0]);
		previousPoint = currentPoint = firstPoint;
		
		for (int i = 1; i < poly.npoints; i++) {
			previousPoint = currentPoint;
			currentPoint = new Point(poly.xpoints[i], poly.ypoints[i]);
			size += currentPoint.distance(previousPoint);
		}
		if (!currentPoint.equals(firstPoint) && !previousPoint.equals(firstPoint))
			size += currentPoint.distance(firstPoint);
		
		return size;
	}
	
	
	
	private NavButtonDefinition createCornersButton(String name, Direction dir, int x, int y) {
		
		NavButtonDefinition button;
		Graphics buttonGraphics;
		Image normalImage, hoverImage, pressedImage;
		Rectangle area;
		
		Point centerPoint = new Point(buttonSizeInPixels/2, buttonSizeInPixels/2);
		int buttonBorderWidth = (int)(buttonSizeInPixels*.1);
		int moveOffset = 1;
		int topAndLeftSpacing = buttonBorderWidth + 1;
		int bottomAndRightSpacing = moveOffset + buttonBorderWidth + 1;
		Rectangle iconBounds = new Rectangle(topAndLeftSpacing, topAndLeftSpacing,
				buttonSizeInPixels - bottomAndRightSpacing - topAndLeftSpacing - 1,
				buttonSizeInPixels - bottomAndRightSpacing - topAndLeftSpacing - 1);
		
		int 
			leftXInc=0, leftYInc=0,
			rightXInc=0, rightYInc=0,
			tipXStart=0, tipYStart=0,
			transX=0, transY=0;
		
		switch(dir) {
		case NORTH:
			leftXInc = -1;
			leftYInc = 1;
			rightXInc = 1;
			rightYInc = 1;
			transX = 0;
			transY = 2;
			tipXStart = centerPoint.x;
			tipYStart = iconBounds.y;
			break;
		case SOUTH:
			leftXInc = -1;
			leftYInc = -1;
			rightXInc = 1;
			rightYInc = -1;
			transX = 0;
			transY = -2;
			tipXStart = centerPoint.x;
			tipYStart = iconBounds.y+iconBounds.height;
			break;
		case EAST:
			leftXInc = -1;
			leftYInc = -1;
			rightXInc = -1;
			rightYInc = 1;
			transX = -2;
			transY = 0;
			tipXStart = iconBounds.x+iconBounds.width;
			tipYStart = centerPoint.y;				
			break;
		case WEST:
			leftXInc = 1;
			leftYInc = -1;
			rightXInc = 1;
			rightYInc = 1;
			transX = 2;
			transY = 0;
			tipXStart = iconBounds.x;
			tipYStart = centerPoint.y;				
			break;
		case NORTH_EAST:
			leftXInc = -1;
			leftYInc = 0;
			rightXInc = 0;
			rightYInc = 1;
			transX = -2;
			transY = 2;
			tipXStart = iconBounds.x+iconBounds.width;
			tipYStart = iconBounds.y;				
			break;
		case NORTH_WEST:
			leftXInc = 0;
			leftYInc = 1;
			rightXInc = 1;
			rightYInc = 0;
			transX = 2;
			transY = 2;
			tipXStart = iconBounds.x;
			tipYStart = iconBounds.y;				
			break;
		case SOUTH_EAST:
			leftXInc = 0;
			leftYInc = -1;
			rightXInc = -1;
			rightYInc = 0;
			transX = -2;
			transY = -2;
			tipXStart = iconBounds.x+iconBounds.width;
			tipYStart = iconBounds.y+iconBounds.height;
			break;
		case SOUTH_WEST: default:
			leftXInc = 0;
			leftYInc = -1;
			rightXInc = 1;
			rightYInc = 0;
			transX = 2;
			transY = -2;
			tipXStart = iconBounds.x;
			tipYStart = iconBounds.y+iconBounds.height;
			break;
		}
			
		Polygon outerCornerShape = new Polygon(
				new int[] { tipXStart+leftXInc, tipXStart, tipXStart+rightXInc},
				new int[] { tipYStart+leftYInc, tipYStart, tipYStart+rightYInc},
				3);
		Polygon innerCornerShape = new Polygon(outerCornerShape.xpoints, outerCornerShape.ypoints, 3);
		innerCornerShape.translate(transX, transY);
		
		// Size arrows as big as we can
		for (Polygon propOutCorner = outerCornerShape, propInCorner = innerCornerShape;
			iconBounds.contains(propInCorner.getBounds());
			propOutCorner.invalidate(), propInCorner.invalidate())
		{
			outerCornerShape = new Polygon(propOutCorner.xpoints, propOutCorner.ypoints, 3);
			innerCornerShape = new Polygon(propInCorner.xpoints, propInCorner.ypoints, 3);
			propOutCorner.xpoints[0] += leftXInc;
			propOutCorner.xpoints[2] += rightXInc;
			propOutCorner.ypoints[0] += leftYInc;
			propOutCorner.ypoints[2] += rightYInc;
			propInCorner.xpoints[0] += leftXInc;
			propInCorner.xpoints[2] += rightXInc;
			propInCorner.ypoints[0] += leftYInc;
			propInCorner.ypoints[2] += rightYInc;
		}
		// Move them in toward the center as much as possible too
		for (Polygon propOutCorner = outerCornerShape, propInCorner = innerCornerShape;
			iconBounds.contains(propInCorner.getBounds());
			propOutCorner.invalidate(), propInCorner.invalidate())
		{
			// stop if we are centered
			if (centerPoint.distance(outerCornerShape.xpoints[1], outerCornerShape.ypoints[1]) == centerPoint.distance(innerCornerShape.xpoints[1], innerCornerShape.ypoints[1]))
				break;
			outerCornerShape = new Polygon(propOutCorner.xpoints, propOutCorner.ypoints, 3);
			innerCornerShape = new Polygon(propInCorner.xpoints, propInCorner.ypoints, 3);
			propOutCorner.translate(transX > 0 ? 1 : (transX < 0 ? -1 : 0), transY > 0 ? 1 : (transY < 0 ? -1 : 0));
			propInCorner.translate(transX > 0 ? 1 : (transX < 0 ? -1 : 0), transY > 0 ? 1 : (transY < 0 ? -1 : 0));
		}
		
		
		Polygon offsetCorner1Shape = new Polygon(outerCornerShape.xpoints, outerCornerShape.ypoints, 3);
		offsetCorner1Shape.translate(1, 1);
		Polygon offsetCorner2Shape = new Polygon(innerCornerShape.xpoints, innerCornerShape.ypoints, 3);
		offsetCorner2Shape.translate(1, 1);
		
		area = new Rectangle(x, y, buttonSizeInPixels, buttonSizeInPixels);
		normalImage = createImage(area.width, area.height);
		buttonGraphics = normalImage.getGraphics();
		buttonGraphics.setColor(Color.BLACK);
		buttonGraphics.drawPolyline(outerCornerShape.xpoints, outerCornerShape.ypoints, 3);
		buttonGraphics.drawPolyline(innerCornerShape.xpoints, innerCornerShape.ypoints, 3);
		hoverImage = createImage(area.width, area.height);
		buttonGraphics = hoverImage.getGraphics();
		buttonGraphics.setColor(Color.BLACK);
		buttonGraphics.drawPolyline(outerCornerShape.xpoints, outerCornerShape.ypoints, 3);
		buttonGraphics.drawPolyline(innerCornerShape.xpoints, innerCornerShape.ypoints, 3);
		buttonGraphics.setColor(Color.GRAY);
		buttonGraphics.draw3DRect(0, 0, area.width-1, area.height-1, true);
		pressedImage = createImage(area.width, area.height);
		buttonGraphics = pressedImage.getGraphics();
		buttonGraphics.setColor(Color.GREEN);
		buttonGraphics.drawPolyline(offsetCorner1Shape.xpoints, offsetCorner1Shape.ypoints, 3);
		buttonGraphics.drawPolyline(offsetCorner2Shape.xpoints, offsetCorner2Shape.ypoints, 3);
		buttonGraphics.setColor(Color.GRAY);
		buttonGraphics.draw3DRect(0, 0, area.width-1, area.height-1, false);
		button = new NavButtonDefinition(dir, MoveType.PAGE, area, normalImage, hoverImage, pressedImage);	
		
		return button;
		
	}	
	
	private NavButtonDefinition createCircleButton(String name, int x, int y) {
		
		NavButtonDefinition button;
		Graphics buttonGraphics;
		Image normalImage, hoverImage, pressedImage;
		Rectangle area;			

		int buttonBorderWidth = (int)(buttonSizeInPixels*.1);;
		int moveOffset = 1;	
		int circleWidthHeight = buttonSizeInPixels - 4 - buttonBorderWidth - moveOffset;
		
		area = new Rectangle(x, y, buttonSizeInPixels, buttonSizeInPixels);
		normalImage = createImage(area.width, area.height);
		buttonGraphics = normalImage.getGraphics();
		buttonGraphics.drawArc(2, 2, circleWidthHeight, circleWidthHeight, 0, 360);
		hoverImage = createImage(area.width, area.height);
		buttonGraphics = hoverImage.getGraphics();
		buttonGraphics.drawArc(2, 2, circleWidthHeight, circleWidthHeight, 0, 360);
		buttonGraphics.fillArc(2, 2, circleWidthHeight, circleWidthHeight, 0, 360);
		buttonGraphics.setColor(Color.GRAY);
		buttonGraphics.draw3DRect(0, 0, area.width-1, area.height-1, true);
		pressedImage = createImage(area.width, area.height);
		buttonGraphics = pressedImage.getGraphics();			
		buttonGraphics.setColor(Color.GREEN);
		buttonGraphics.drawArc(2+moveOffset, 2+moveOffset, circleWidthHeight, circleWidthHeight, 0, 360);
		buttonGraphics.fillArc(2+moveOffset, 2+moveOffset, circleWidthHeight, circleWidthHeight, 0, 360);
		buttonGraphics.setColor(Color.GRAY);
		buttonGraphics.draw3DRect(0, 0, area.width-1, area.height-1, false);
		button = new NavButtonDefinition(null, MoveType.PAGE, area, normalImage, hoverImage, pressedImage);					
		
		return button;
		
	}		
	
}	

