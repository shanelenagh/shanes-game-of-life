package com.uprr.game.gameoflife.ui;

import com.uprr.game.gameoflife.ui.GameGridNavigator.Direction;
import com.uprr.game.gameoflife.ui.GameGridNavigator.MoveType;

public class GameGridNavigationEvent {
	
	private static final long serialVersionUID = GameGridNavigator.class.getCanonicalName().hashCode();
	
	private Direction dir;
	private MoveType moveType;
	
	public GameGridNavigationEvent(Direction dir, MoveType moveType) {
		
		this.dir = dir;
		this.moveType = moveType;
	}
	
	public Direction getDirection()  {
		return dir;
	}
	
	public MoveType getMoveType() {
		return moveType;
	}
	
	public String toString() {
		return String.format("%s [dir=%s, moveType=%s]", 
				this.getClass().getCanonicalName(), dir.toString(), moveType.toString());
	}
	
}
