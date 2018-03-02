package com.uprr.game.gameoflife;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.uprr.game.gameoflife.Cell;
import com.uprr.game.gameoflife.Game;

import static org.junit.Assert.*;



public class GameTest {

	private Game game;
	
	@Before
	public void setUp() {
		game = new Game();
	}
	
	@Test
	public void create_DefaultCellCountIsZero() {
		assertEquals(0, game.getLiveCells().size());
	}
		
	@Test
	public void bringCellToLife_CountIncrementsByOne() {
		game.bringCellToLife(new Cell(12, 34));
		assertEquals(1, game.getLiveCells().size());		
	}	

	@Test(expected = IllegalArgumentException.class)
	public void bringCellToLife_AddToSameCoordThrowsException() {
		game.bringCellToLife(new Cell(12, 34));
		game.bringCellToLife(new Cell(12, 34));
	}	

	@Test
	public void bringCellToLife_CellIsNowInSet() {
		Cell liveCell = new Cell(23, 12);
		Set<Cell> liveCellSet = game.getLiveCells(); // exploit Java pass-by-ref
		assertFalse("Cell not there yet (sanity)", liveCellSet.contains(liveCell));
		game.bringCellToLife(liveCell);
		assertTrue("Cell is set now", liveCellSet.contains(liveCell));
	}	

	@Test
	public void killCell_CellNoLongerInSet() {
		Cell liveCell = new Cell(23, 12);
		Set<Cell> liveCellSet = game.getLiveCells(); // exploit Java pass-by-ref
		liveCellSet.add(liveCell);
		game.killCell(liveCell);
		assertFalse("Cell is dead now", liveCellSet.contains(liveCell));
	}	

	@Test(expected=IllegalArgumentException.class)
	public void killCell_DeadCellThrowsException() {
		Cell nonLivingCell = new Cell(23, 12);
		game.killCell(nonLivingCell);
	}		
	
	@Test
	public void isCellAlive_EmptyCellIsntAlive() {
		Cell emptyGamePoint = new Cell(23, 12);
		assertFalse(game.isCellAlive(emptyGamePoint));
	}

	@Test
	public void isCellAlive_AddedCellIsAlive() {
		Cell liveCell = new Cell(23, 12);
		game.bringCellToLife(liveCell);
		assertTrue(game.isCellAlive(liveCell));
	}

	@Test
	public void getNeighborCountAndDeadCells_EmptyBoardPointResultsInZero() {
		Cell point = new Cell(23, 12);
		assertEquals(0, game.getNeighborCountAndDeadCells(point, null));
	}
	
	@Test
	public void getNeighborCountAndDeadCells_LoneCellResultsInZero() {
		Cell point = new Cell(23, 12);
		game.bringCellToLife(point);
		assertEquals(0, game.getNeighborCountAndDeadCells(point, null));
	}

	@Test
	public void getNeighborCountAndDeadCells_NeighborPairEachResultInOne() {
		Cell point = new Cell(23, 12);
		Cell diagnolPoint = new Cell(22, 11);
		game.bringCellToLife(point);
		game.bringCellToLife(diagnolPoint);
		assertEquals("Point", 1,
			game.getNeighborCountAndDeadCells(point, null));
		assertEquals("Southwest diag pt", 1,
			game.getNeighborCountAndDeadCells(diagnolPoint, null));
	}

	@Test
	public void getNeighborCountAndDeadCells_DiagonalNeighborPairHasTwelveDeadNeighbors() {
		Cell point = new Cell(23, 12);
		Cell diagnolPoint = new Cell(22, 11);
		game.bringCellToLife(point);
		game.bringCellToLife(diagnolPoint);
		
		Set<Cell> deadNeighborPoints = new HashSet<Cell>();
		
		game.getNeighborCountAndDeadCells(point, deadNeighborPoints);
		game.getNeighborCountAndDeadCells(diagnolPoint, deadNeighborPoints);
		
		assertEquals("Cumulative dead neighbor count", 12, deadNeighborPoints.size());
	}
	
	
	@Test
	public void getNeighborCountAndDeadCells_NonNeighborsInDeadCellSet() {
		Cell point = new Cell(23, 12);
		Cell diagnolPoint = new Cell(22, 11);
		
		Cell southNeighborPoint = new Cell(23, 11);
		Cell northNeighborPoint = new Cell(23, 13);
		Cell northEastNeighborPoint = new Cell(24, 13);
		Cell southEastNeighborPoint = new Cell(22, 13);
		Cell northWestNeighborPoint = new Cell(22, 13);
		Cell eastNeighborPoint = new Cell(24, 12);
		Cell westNeighborPoint = new Cell(22, 12);
		
		game.bringCellToLife(point);
		game.bringCellToLife(diagnolPoint);
		
		Set<Cell> deadNeighbors = new HashSet<Cell>();
		game.getNeighborCountAndDeadCells(point, deadNeighbors);
		
		assertEquals("Dead neighbor count", 7, deadNeighbors.size());
		assertTrue(deadNeighbors.contains(southNeighborPoint));
		assertTrue(deadNeighbors.contains(northNeighborPoint));
		assertTrue(deadNeighbors.contains(northEastNeighborPoint));
		assertTrue(deadNeighbors.contains(southEastNeighborPoint));
		assertTrue(deadNeighbors.contains(northWestNeighborPoint));
		assertTrue(deadNeighbors.contains(eastNeighborPoint));
		assertTrue(deadNeighbors.contains(westNeighborPoint));
		
	}	
	
	@Test
	public void conwayRule1_LoneCellDies() {
		assertTrue(game.conwayRule1_DoesCellDieOfLoneliness(0));
	}

	@Test
	public void conwayRule1_OneNeighborCellsDie() {
		assertTrue(game.conwayRule1_DoesCellDieOfLoneliness(1));
	}	


	@Test
	public void conwayRule1_CellsWithTwoNeighborsLives() {
		assertFalse(game.conwayRule1_DoesCellDieOfLoneliness(2));
	}	


	@Test
	public void conwayRule2_CellsWithOverThreeNeighborsDies() {
		assertTrue(game.conwayRule2_DoesCellDieOfOvercrowding(4));
		assertTrue(game.conwayRule2_DoesCellDieOfOvercrowding(6));
	}


	@Test
	public void conwayRule3_CellsWithTwoOrThreeNeighborsLives() {	
		assertTrue("2 neighbors", game.conwayRule3_DoesSocialCellLiveOn(2));
		assertTrue("3 neighbors", game.conwayRule3_DoesSocialCellLiveOn(3));
		assertFalse("1 neighbors", game.conwayRule3_DoesSocialCellLiveOn(1));
		assertFalse("4 neighbors", game.conwayRule3_DoesSocialCellLiveOn(4));
	}	


	@Test
	public void conwayRule4_CellWithThreeNeighborsIsResurrected() {
		assertTrue(game.conwayRule4_DoesCellHaveEnoughSupportToResurrect(3));
	}
	
	@Test
	public void tick_LoneCellDies() {
		Cell point = new Cell(23, 12);
		game.bringCellToLife(point);
		game.tick();
		assertFalse(game.isCellAlive(point));		
	}

	@Test
	public void tick_PairOfCellsDie() {
		Cell point = new Cell(23, 12);
		Cell diagnolPoint = new Cell(22, 11);
		game.bringCellToLife(point);
		game.bringCellToLife(diagnolPoint);
		game.tick();
		assertFalse(game.isCellAlive(point));
		assertFalse(game.isCellAlive(diagnolPoint));	
	}
	
	@Test
	public void tick_TriangleOfThreeCellsLives() {
		Cell upperRightPoint = new Cell(23, 12);
		Cell cornerPoint = new Cell(23, 11);
		Cell lowerLeftPoint = new Cell(22, 11);
		game.bringCellToLife(upperRightPoint);
		game.bringCellToLife(cornerPoint);		
		game.bringCellToLife(lowerLeftPoint);
		game.tick();
		assertTrue("upper right corner of triangle",
				game.isCellAlive(upperRightPoint));
		assertTrue("corner of triangle",
				game.isCellAlive(cornerPoint));
		assertTrue("lower left corner of triangle",
				game.isCellAlive(lowerLeftPoint));
		
	}
	
	/**
	 *   ** * => ** 
	 *   *       **
	 */
	@Test
	public void getBornAndDeadCells_ExpectedCellsInPatternReturned() {
		Cell upperRightPoint = new Cell(23, 12);
		Cell upperLeftPoint = new Cell(22, 12);
		Cell lowerLeftPoint = new Cell(22, 13);
		Cell farRightLoneCell = new Cell(25, 12);
		
		Cell newLowerRightCornerPoint = new Cell(23, 13);
		
		game.bringCellToLife(upperRightPoint);
		game.bringCellToLife(upperLeftPoint);		
		game.bringCellToLife(lowerLeftPoint);		
		game.bringCellToLife(farRightLoneCell);		
		game.tick();
		
		Set<Cell> bornCells = game.getBornCells();
		Set<Cell> killedCells = game.getKilledCells();
		
		assertEquals("Born cell count", 1, bornCells.size());
		assertTrue("Born cells contains one cell in corner", bornCells.contains(newLowerRightCornerPoint));
		assertEquals("Killed cell count", 1, killedCells.size());
		assertTrue("Killed cells ontains loner on far right", killedCells.contains(farRightLoneCell));
	}	
	
	@Test
	public void tick_CellWithFourNeighborsDies() {
		Cell upperRightPoint = new Cell(23, 12);
		Cell upperLeftPoint = new Cell(22, 12);
		Cell lowerLeftPoint = new Cell(22, 11);
		Cell lowerMiddlePoint = new Cell(23, 11);
		Cell lowerFarRightPoint = new Cell(24, 11);
		game.bringCellToLife(upperRightPoint);
		game.bringCellToLife(upperLeftPoint);		
		game.bringCellToLife(lowerLeftPoint);		
		game.bringCellToLife(lowerMiddlePoint);		
		game.bringCellToLife(lowerFarRightPoint);
		game.tick();
		assertFalse(game.isCellAlive(lowerMiddlePoint));
	}

	@Test
	public void tick_DeadCellWithThreeNeighborsIsResurrected() {

		Cell lowerLeftPoint = new Cell(22, 11);
		Cell lowerMiddlePoint = new Cell(23, 11);
		Cell lowerFarRightPoint = new Cell(24, 11);
		Cell belowLowerMiddlePoint = new Cell(23, 10);
		
		game.bringCellToLife(lowerLeftPoint);		
		game.bringCellToLife(lowerMiddlePoint);		
		game.bringCellToLife(lowerFarRightPoint);
		
		assertFalse("sanity check for dead cell",
				game.isCellAlive(belowLowerMiddlePoint));
		
		game.tick();
		
		assertTrue(game.isCellAlive(belowLowerMiddlePoint));
	}

	/**
	 * Test the following transformation pattern:
	 * 
	 *  x      x        xx
	 *    x xxx  ==>  xxxx
	 *      x         xx
	 *      
	 *      x          xx
	 *      xx         xx
	 */
	@Test
	public void tick_ElaboratePatternIsProperlyTransformed() {

		Cell loneUpperFarLeftPoint = new Cell(18, 4);
		Cell loneFarLeftPoint = new Cell(20, 5);
		Cell upperHookLeftPoint = new Cell(22, 5);
		Cell upperHookMiddlePoint = new Cell(23, 5);
		Cell upperHookRightPoint = new Cell(24, 5);
		Cell highStragglingPoint = new Cell(25, 6);
		Cell upperHookUpperLeftPoint = new Cell(22, 4);
		Cell lowerTriangleUpperLeftPoint = new Cell(22, 2);
		Cell lowerTriangleLeftPoint = new Cell(22, 1);
		Cell lowerTriangleRightPoint = new Cell(23, 1);
			
		game.bringCellToLife(loneUpperFarLeftPoint);
		game.bringCellToLife(loneFarLeftPoint);		
		game.bringCellToLife(upperHookLeftPoint);		
		game.bringCellToLife(upperHookMiddlePoint);
		game.bringCellToLife(upperHookRightPoint);
		game.bringCellToLife(highStragglingPoint);
		game.bringCellToLife(upperHookUpperLeftPoint);
		game.bringCellToLife(lowerTriangleUpperLeftPoint);
		game.bringCellToLife(lowerTriangleLeftPoint);
		game.bringCellToLife(lowerTriangleRightPoint);

		assertEquals("Initial cell count (sanity)", 10, game.getLiveCells().size());		
		
		game.tick();
		
		// overall count
		assertEquals("Cell count", 12, game.getLiveCells().size());
		
		// living on
		assertTrue("upperHookLeftPoint",
				game.isCellAlive(upperHookLeftPoint));
		assertTrue("upperHookMiddlePoint",
				game.isCellAlive(upperHookMiddlePoint));
		assertTrue("upperHookRightPoint",
				game.isCellAlive(upperHookRightPoint));
		assertTrue("upperHookUpperLeftPoint",
				game.isCellAlive(upperHookUpperLeftPoint));
		assertTrue("lowerTriangleUpperLeftPoint",
				game.isCellAlive(lowerTriangleUpperLeftPoint));
		assertTrue("lowerTriangleLeftPoint",
				game.isCellAlive(lowerTriangleLeftPoint));
		assertTrue("lowerTriangleRightPoint",
				game.isCellAlive(lowerTriangleRightPoint));
		
		// deaths
		assertFalse("loneFarLeftPoint", game.isCellAlive(loneFarLeftPoint));
		
		// New (born again :-) points
		Cell expectedNewUpperHookUpperLeftPoint = new Cell(21, 5);
		Cell expectedNewUpperHookLowerLeftPoint = new Cell(21, 4);
		Cell expectedNewUpperHookHighRightPoint = new Cell(24, 6);
		Cell expectedNewUpperHookHighLeftPoint = new Cell(23, 6);
		Cell expectedNewLowerHookCornerPoint = new Cell(23, 2);
		assertTrue("expectedNewUpperHookUpperLeftPoint",
				game.isCellAlive(expectedNewUpperHookUpperLeftPoint));
		assertTrue("expectedNewUpperHookLowerLeftPoint",
				game.isCellAlive(expectedNewUpperHookLowerLeftPoint));
		assertTrue("expectedNewUpperHookHighRightPoint",
				game.isCellAlive(expectedNewUpperHookHighRightPoint));
		assertTrue("expectedNewUpperHookHighLeftPoint",
				game.isCellAlive(expectedNewUpperHookHighLeftPoint));
		assertTrue("expectedNewLowerHookCornerPoint",
				game.isCellAlive(expectedNewLowerHookCornerPoint));
	}
	
	@Test
	public void reset_CellCountBackToZero() {
		game.bringCellToLife(new Cell(12, 34));
		game.reset();
		assertEquals(0, game.getLiveCells().size());		
	}	
}
