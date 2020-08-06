package com.uprr.game.gameoflife;
import java.util.HashSet;
import java.util.Set;

/**
 * Game of Life core game object
 * 
 * @author slenagh@up.com
 *
 */
public class Game {

	private Set<Cell> liveCells = new HashSet<Cell>();
	private Set<Cell> killedCells, bornCells;
	
	public Set<Cell> getLiveCells() {
		return liveCells;
	}
	
	public Set<Cell> getBornCells() {
		return bornCells;
	}

	public Set<Cell> getKilledCells() {
		return killedCells;
	}	
	
	public void bringCellToLife(Cell cell) {
		
		if (isCellAlive(cell))
			throw new IllegalArgumentException("Cell already alive at this location: "+cell);
		
		liveCells.add(cell);
	}
	
	public void killCell(Cell cell) {
		
		if (!isCellAlive(cell))
			throw new IllegalArgumentException("No cell alive at this location: "+cell);
		
		liveCells.remove(cell);
	}
	
	
	public boolean isCellAlive(Cell cell) {
		return liveCells.contains(cell);
	}
	
	public int getNeighborCountAndDeadCells(Cell cell, Set<Cell> deadNeighbors) {
		
		int neighborCount = 0;
		
		for (int x = cell.getX() - 1; x <= cell.getX()+1; x++) {
			for (int y = cell.getY() - 1; y <= cell.getY()+1; y++)  {
				
				if (x == cell.getX() && y == cell.getY()) // Don't count myself	
					continue;
				
				Cell adjacentCell = new Cell(x, y);
				if (isCellAlive(adjacentCell))
					neighborCount++;
				else if (deadNeighbors != null && !deadNeighbors.contains(adjacentCell))
					deadNeighbors.add(adjacentCell);
			}
		}
		
		return neighborCount;
	}
	
	protected boolean conwayRule1_DoesCellDieOfLoneliness(int neighborCount) {
		return neighborCount < 2;
	}
	
	protected boolean conwayRule2_DoesCellDieOfOvercrowding(int neighborCount) {
		return neighborCount > 3;
	}	

	protected boolean conwayRule3_DoesSocialCellLiveOn(int neighborCount) {
		return (neighborCount == 2 || neighborCount == 3);
	}	
	
	protected boolean conwayRule4_DoesCellHaveEnoughSupportToResurrect(int neighborCount) {
		return neighborCount == 3;
	}
	
	public synchronized void  tick() {
		
		Set<Cell> deadNeighborCells = new HashSet<Cell>();
		Set<Cell> cellsToKill = new HashSet<Cell>();
		Set<Cell> cellsToBirth = new HashSet<Cell>();
		
		// see if any live cells no longer have support to continue living
		for (Cell liveCell : liveCells) {

			int neighborCount = getNeighborCountAndDeadCells(liveCell, deadNeighborCells);			
			
			// If cells don't meet Conway's rules for life then schedule their execution
			if (conwayRule1_DoesCellDieOfLoneliness(neighborCount)
					|| conwayRule2_DoesCellDieOfOvercrowding(neighborCount))
					//|| !conwayRule3_DoesSocialCellLiveOn(neighborCount)) <-- Coverage analysis shows this is redundant
			{
				cellsToKill.add(liveCell);
			}			
		}
		
		// determine dead/empty cells that have support to come to life
		for (Cell deadNeighborCell : deadNeighborCells) {
			
			int neighborCount = getNeighborCountAndDeadCells(deadNeighborCell, null);
			
			if (conwayRule4_DoesCellHaveEnoughSupportToResurrect(neighborCount)) {	
				cellsToBirth.add(deadNeighborCell);
			}				
		}

		// finalize executions :-(
		for (Cell doomedCell : cellsToKill) {
			killCell(doomedCell);
		}
		this.killedCells = cellsToKill;
		
		// perform births/resurrections :-)
		for (Cell birthingCell : cellsToBirth) {
			bringCellToLife(birthingCell);
		}
		this.bornCells = cellsToBirth;
		
	}
	
	public void reset() {
		liveCells.clear();
	}
}
