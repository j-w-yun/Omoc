package com.jaewanyun.omoc;

import java.io.Serializable;

public class GameSettings implements Serializable {

	private static final long serialVersionUID = 1907321982L;
	public int gridWidth;
	public int gridHeight;
	public int secondsAllotted;
	public int stonesPerTurn;
	public int stonesToWin;

	public GameSettings() {
		gridWidth = 30;
		gridHeight = 30;
		secondsAllotted = 10;
		stonesPerTurn = 1;
		stonesToWin = 5;
	}
}
