package com.uprr.game.gameoflife;

/**
 * Cell in Conway's Game of Life
 * 
 * @author slenagh@up.com
 *
 */
public class Cell {
	
	private static final String STRING_FORMAT = "Cell (x=%d, y=%d)";
	private int x, y;
	
	public Cell() {
		x = 0;
		y = 0;
	}
	
	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Cell))
			return false;
		else {
			Cell otherCell = (Cell)o;
			return otherCell.x == this.x && otherCell.y == this.y;
		}
	}
	
	public String toString() {
		return String.format(STRING_FORMAT, x, y);
	}
}
