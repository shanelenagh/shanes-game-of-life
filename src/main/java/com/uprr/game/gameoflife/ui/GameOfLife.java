package com.uprr.game.gameoflife.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.applet.Applet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Date;

import com.uprr.game.gameoflife.Game;

public class GameOfLife extends Applet
	implements ActionListener, Runnable
{
	private static final long serialVersionUID = GameOfLife.class.getCanonicalName().hashCode();
	
	public static final int
		DEFAULT_GRID_WIDTH = 20,
		DEFAULT_GRID_HEIGHT = 20,
		DEFAULT_CELL_SIZE = 10;
	public final static int DEFAULT_TICK_DELAY = 100;
	
	private boolean isRunning;
	private int tickDelayMilliseconds = DEFAULT_TICK_DELAY;
	private long generationCount;
	private long startGenCount;
	private Date startTime, endTime;	
	
	private Thread gameRunner;
	private GameGrid gameGrid;
	private Button tickButton, resetButton, runStopButton;
	private GameGridNavigator navigatorControl;
	private Label generationCounterLabel;
	private TextField delayField;
	private ComponentListener resizeHandler;
	 
	private class ResizeHandler extends ComponentAdapter {
		public void componentResized(ComponentEvent e) { 
			gameGrid.redrawGrid();
		}
	}

	public static void main(String[] args) {
		
		Frame gameFrame = new Frame("Game of Life - Standalone Mode");
		final GameOfLife gameOfLife = new GameOfLife(new GameGrid(DEFAULT_GRID_WIDTH, DEFAULT_GRID_HEIGHT, 
				DEFAULT_CELL_SIZE, new Game()));
		
		gameFrame.add(gameOfLife, BorderLayout.CENTER);
		gameFrame.pack();
		
		gameFrame.addWindowListener(
			new WindowAdapter() {
			    public void windowClosing(WindowEvent e) {
			    	System.exit(0);
			    }				
			}
		);
		gameFrame.addComponentListener(gameOfLife.resizeHandler);		
		
		gameFrame.setVisible(true);
	}
	
	public GameOfLife(GameGrid grid) {
		
		gameGrid = grid;
		
		setLayout(new BorderLayout());
		
		add(gameGrid, BorderLayout.CENTER);

		Panel controlPanel = new Panel();
		tickButton = new Button("Tick");
		tickButton.addActionListener(this);
		resetButton = new Button("Reset");
		controlPanel.add(tickButton);
		resetButton.addActionListener(this);
		controlPanel.add(resetButton);
		
		controlPanel.add(new Label("Delay (ms):"));
		delayField = new TextField("100", 4);
		controlPanel.add(delayField);
		controlPanel.add(new Label("Generation:"));
		generationCounterLabel = new Label(generationCount+"     ");
		controlPanel.add(generationCounterLabel);
		runStopButton = new Button("Run");
		controlPanel.add(runStopButton);
		runStopButton.addActionListener(this);
		
		navigatorControl = new GameGridNavigator();	// Needed to navigate infinite plane
		navigatorControl.addNavigationListener(gameGrid);
		controlPanel.add(navigatorControl);
		add(controlPanel, BorderLayout.NORTH);	
		
		resizeHandler = new ResizeHandler();
		
	}	

	
	public void actionPerformed(ActionEvent event) {
		
		if (event.getSource() == tickButton) {
			doTick();
		} else if (event.getSource() == resetButton) {
			reset();
		} else if (event.getSource() == runStopButton) {
			if (isRunning) {
				stopGameRunning();
			} else {
				startGameRunning();
			}
		}
		
	}

	
	protected void reset() {
		if (isRunning)
			stopGameRunning();
		generationCount = 0;
		generationCounterLabel.setText(Long.toString(generationCount));
		delayField.setText(Integer.toString(DEFAULT_TICK_DELAY));
		gameGrid.reset();		
	}
	
	protected void startGameRunning() {
		
		startGenCount = generationCount;
		gameRunner = new Thread(this, "Game Runner");
		boolean isValidDelay = true;
		
		try {
			tickDelayMilliseconds = Integer.parseInt(delayField.getText());
			isValidDelay = tickDelayMilliseconds >= 0;
		} catch (NumberFormatException nfe) {
			isValidDelay = false;
		}
		if (!isValidDelay) {
			tickDelayMilliseconds = DEFAULT_TICK_DELAY;
			delayField.setText(Integer.toString(DEFAULT_TICK_DELAY));					
		}
		
		isRunning = true;
		runStopButton.setLabel("Stop");
		startTime = new Date();
		gameRunner.start();
		
	}
	
	protected void stopGameRunning() {
		
		isRunning = false;
		gameRunner.interrupt();
		
		endTime = new Date();
		long timeDelay = Math.abs(endTime.getTime()	- startTime.getTime());				
		runStopButton.setLabel("Run");
		System.out.println(
			String.format("Ran %d generations in %dms => %d gen/sec",
					generationCount-startGenCount, timeDelay,
					(int)((generationCount-startGenCount)/(timeDelay/1000.0))));		
	}
	
	protected void doTick() {
		
		gameGrid.doTick();
		
		generationCounterLabel.setText(Long.toString(++generationCount));
	}
	
	public void run() {
		while (isRunning) {
			doTick();
			if (tickDelayMilliseconds > 0)
				try {
					Thread.sleep(tickDelayMilliseconds);
				} catch (InterruptedException ie) {
				}
		}
	}
	

	
	public long getGenerationCount() {
		return generationCount;
	}
}
