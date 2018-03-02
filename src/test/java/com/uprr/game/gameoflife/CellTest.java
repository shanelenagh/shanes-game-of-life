package com.uprr.game.gameoflife;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.uprr.game.gameoflife.Cell;

import static org.junit.Assert.*;

public class CellTest {

	private Cell cell;
	private static final int TEST_X_VALUE = 12, TEST_Y_VALUE = 23;
	
	@Before
	public void setUp() {
		cell = new Cell(TEST_X_VALUE, TEST_Y_VALUE);
	}
	
	@Test
	public void coordinates_DefaultToZero() {
		cell = new Cell();
		assertEquals("X", 0, cell.getX());
		assertEquals("y", 0, cell.getY());
	}
	
	@Test
	public void coordinates_AreSettable() {
		cell.setX(-32);
		cell.setY(456);
		assertEquals("X", -32, cell.getX());
		assertEquals("Y", 456, cell.getY());
	}
	
	@Test
	public void create_CoordConstructor() {
		assertEquals("X", TEST_X_VALUE, cell.getX());
		assertEquals("Y", TEST_Y_VALUE, cell.getY());
	}
	
	@Test
	public void hash_ValuesOfDifferentCoordsUnique() {
		Cell cell2 = new Cell(-TEST_X_VALUE+2, TEST_Y_VALUE+234);
		assertNotSame("hashes of different coords", cell.hashCode(), cell2.hashCode());
	}
	
	@Test
	public void hash_ValuesOfSameCoordsAreEqual() {
		Cell cell2 = new Cell(TEST_X_VALUE, TEST_Y_VALUE);
		assertEquals("hashes of same coords", cell.hashCode(), cell2.hashCode());
	}

	@Test
	public void hash_valuesOfInvertedCoordsAreNotEqual() {
		Cell cell = new Cell(TEST_X_VALUE, TEST_Y_VALUE);
		Cell invertedCell = new Cell(TEST_Y_VALUE, TEST_X_VALUE);
		assertNotSame("hashes of inverted coords", cell.hashCode(), invertedCell.hashCode());
	}	
	
	@Test
	public void hash_100DifferentCellsNotEqual() {
		Cell cell;
		HashMap<Integer, Cell> cellHashMap = new HashMap<Integer, Cell>();
		for (int x = -50; x < 50; x++)
			for (int y = -50; y < 50; y++) {
				cell = new Cell(x, y);
				assertFalse(
					String.format("Hash set already contains hash %d for cell %s (dupe is %s)",
							cell.hashCode(), cell.toString(), cellHashMap.get(cell.hashCode())),
					cellHashMap.containsKey(cell.hashCode()));
				cellHashMap.put(cell.hashCode(), cell);
			}
	}			
	
	@Test
	public void equals_SameCoordsEqualEachOther() {
		Cell cell2 = new Cell(TEST_X_VALUE, TEST_Y_VALUE);
		assertTrue(cell.equals(cell2));
	}	

	@Test
	public void equals_DifferentCoordsDontEqualEachOther() {
		assertFalse("x off", cell.equals(new Cell(TEST_X_VALUE-11, TEST_Y_VALUE)));
		assertFalse("y off", cell.equals(new Cell(TEST_X_VALUE, TEST_Y_VALUE+5)));
		assertFalse("x & y off", cell.equals(new Cell(TEST_X_VALUE-22, TEST_Y_VALUE+78)));
	}	
	
	@Test
	public void equals_NullNotEquals() {
		assertFalse(cell.equals(null));
	}

	@Test
	public void equals_AnotherTypeNotEquals() {
		assertFalse(cell.equals("A string, oops, not a cell"));
	}	
	
	@Test
	public void equals_DifferentCoordsDontEuqalEachOther() {
		Cell cell2 = new Cell(-TEST_X_VALUE+2, TEST_Y_VALUE+234);
		assertFalse(cell.equals(cell2));
	}
	
	@Test
	public void toString_Format() {
		assertEquals("Cell (x=" + TEST_X_VALUE + ", y=" + TEST_Y_VALUE + ")",
				cell.toString());
	}
}
