package com.uprr.game.gameoflife.ui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.uprr.game.gameoflife.Cell;
import com.uprr.game.gameoflife.Game;
import com.uprr.game.gameoflife.ui.GameGridNavigator.Direction;
import com.uprr.game.gameoflife.ui.GameGridNavigator.MoveType;

/**
 * Grid on which Game of Life organisms live
 * 
 * @author slenagh@up.com
 *
 */
class GameGrid extends Canvas
	implements GameGridNavigationListener
{
	
	private static final long serialVersionUID = GameGrid.class.getCanonicalName().hashCode();
	
	private Cell originCell = new Cell(0, 0);
	
	private Game game;
	public int cellSize, gridWidth, gridHeight;
	private Image backBuffer;
	private Graphics backBufferGraphics;
	
	private class CellClickHandler extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			
			Cell clickedCell = getCellForCoordinate(e.getX(), e.getY());
			
			if (!game.isCellAlive(clickedCell)) {
				game.bringCellToLife(clickedCell);
				// single cell changes painted directly (no back-buffered repaint)
				fillCell(clickedCell);	
			} else {
				game.killCell(clickedCell);
				// single cell changes painted directly (no back-buffered repaint)
				clearCell(clickedCell);
			}	
			
		}		
	}
	
	public GameGrid(int width, int height, int cellSizeInPixels, Game game) {

		this.cellSize = cellSizeInPixels;
		this.game = game;
		this.gridWidth = width;
		this.gridHeight = height;

		setSize(width*cellSizeInPixels, height*cellSizeInPixels);
		
		addMouseListener(new CellClickHandler());
		
	}
	
	public int getGridWidth() {
		return this.gridWidth;
	}
	
	public int getGridHeight() {
		return this.gridHeight;
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(gridWidth*cellSize, gridHeight*cellSize);
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(gridWidth*cellSize, gridHeight*cellSize);
	}
	
	
	public void update(Graphics g) {

		if (backBufferGraphics == null)
			setupBackBuffer();
		else
			backBufferGraphics.clearRect(0, 0, getWidth(), getHeight());
			
		drawGridlines(backBufferGraphics);
		drawLiveCells(backBufferGraphics);
		
		g.drawImage(backBuffer, 0, 0, this);
		
	}
	
	private void setupBackBuffer() {
		backBuffer = this.createImage(getWidth(), getHeight());
		backBufferGraphics = backBuffer.getGraphics();	
	}
	
	public void redrawGrid() {
    	setupBackBuffer();
    	repaint();		
	}
	
	public void paint(Graphics g) {
		update(g);
	}
	
	public void reset() {
		game.reset();
		setOriginCell(new Cell(0, 0));
		repaint();			
	}
	
	public void doTick() {
		
		game.tick();
		
		// Save memory/cycles by just redrawing changed cells (if they are visible)
		//repaint();
		clearDeadCells(getGraphics());
		drawLiveCells(getGraphics());
	}

	private void drawGridlines(Graphics g) {
		// Draw gridlines
		int gridWidth = 0, gridHeight = 0;
		for (int x = 0; x < getWidth(); x = x + cellSize, gridWidth++)
			g.drawLine(x, 0, x, getHeight());
		for (int y = 0; y < getHeight(); y = y + cellSize, gridHeight++)
			g.drawLine(0, y, getWidth(), y);
		
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
	}

	protected boolean isCellVisible(Cell cell) {
		return cell.getX() >= originCell.getX()
				&& cell.getY() >= originCell.getY()
				&& cell.getX() < (originCell.getX()+getGridWidth())
				&& cell.getY() < (originCell.getY()+getGridHeight());
	}
	
	private void drawLiveCells(Graphics g) {
		for (Cell liveCell : game.getLiveCells())
			if (isCellVisible(liveCell))
				fillCell(liveCell, g);	
	}
	
	private void clearDeadCells(Graphics g) {
		for (Cell killedCell : game.getKilledCells())
			if (isCellVisible(killedCell))
				clearCell(killedCell, g);	
	}
	

	/** 
	 * Overload to allow direct painting to components current/default
	 * Graphics (no backfuffering)
	 */
	protected void clearCell(Cell cell) {
		clearCell(cell, getGraphics());
	}

	protected void clearCell(Cell cell, Graphics g) {
		Color bgColor = getBackground();
		g.setColor(bgColor);
		g.fillRect((cell.getX()-originCell.getX())*cellSize+1,
				(cell.getY()-originCell.getY())*cellSize+1, cellSize-1, cellSize-1);
	}		
	
	/** 
	 * Overload to allow direct painting to components current/default
	 * Graphics (no backfuffering)
	 */		
	protected void fillCell(Cell cell) {
		fillCell(cell, getGraphics());
	}		
	
	protected void fillCell(Cell cell, Graphics g) {
		g.fillRect((cell.getX()-originCell.getX())*cellSize,
				(cell.getY()-originCell.getY())*cellSize, cellSize, cellSize);			
	}
	
	protected Cell getCellForCoordinate(int x, int y) {
		return new Cell((int)x/cellSize+originCell.getX(), (int)y/cellSize+originCell.getY());
	}
	
	public void navigate(GameGridNavigationEvent ggne) {
		
		// Adjust viewport for proper rendering
			int moveX = 0, moveY = 0;
			Cell newOrigin;
			if (ggne.getDirection() != null) {
				switch (ggne.getDirection()) {
				case SOUTH:
					moveX = 0;
					moveY = ggne.getMoveType() == MoveType.CELL ? 1 : getGridHeight();
					break;
				case NORTH:
					moveX = 0;
					moveY = ggne.getMoveType() == MoveType.CELL ? -1 : -getGridHeight();
					break;
				case EAST:
					moveX = ggne.getMoveType() == MoveType.CELL ? 1 : getGridWidth();
					moveY = 0;
					break;
				case WEST:
					moveX = ggne.getMoveType() == MoveType.CELL ? -1 : -getGridWidth();
					moveY = 0;
					break;				
				case SOUTH_EAST:
					moveX = ggne.getMoveType() == MoveType.CELL ? 1 : getGridWidth();
					moveY = ggne.getMoveType() == MoveType.CELL ? 1 : getGridHeight();
					break;
				case NORTH_EAST:
					moveX = ggne.getMoveType() == MoveType.CELL ? 1 : getGridWidth();
					moveY = ggne.getMoveType() == MoveType.CELL ? -1 : -getGridHeight();
					break;
				case SOUTH_WEST:
					moveX = ggne.getMoveType() == MoveType.CELL ? -1 : -getGridWidth();
					moveY = ggne.getMoveType() == MoveType.CELL ? 1 : getGridHeight();
					break;
				case NORTH_WEST: default:
					moveX = ggne.getMoveType() == MoveType.CELL ? -1 : -getGridWidth();
					moveY = ggne.getMoveType() == MoveType.CELL ? -1 : -getGridHeight();
					break;		
				}
				newOrigin = new Cell(originCell.getX()+moveX, originCell.getY()+moveY);
			} else
				newOrigin = new Cell(0, 0);
			originCell = newOrigin;
		repaint();
	}	
	
	protected Cell getOriginCell() {
		return this.originCell;
	}
	
	protected void setOriginCell(Cell origin) {
		this.originCell = origin;
	}
	
}

