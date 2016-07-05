package com.jaewanyun.omoc;

import java.io.Serializable;

public class GameState implements Serializable {

	private static final long serialVersionUID = 31387291827391L;
	public static final int BLANK = 0;
	public static final int WHITE = 1;
	public static final int BLACK = 2;
	private volatile int[][] board;
	private volatile int teamTurn; // White = positive; Black = negative
	private GameSettings gameSettings;
	private int id;

	public GameState(int id) {
		gameSettings = new GameSettings();
		board = new int[gameSettings.gridWidth][gameSettings.gridHeight];
		teamTurn = 1;
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public int width() {
		return gameSettings.gridWidth;
	}

	public int height() {
		return gameSettings.gridHeight;
	}

	public synchronized int getBoard(int x, int y) {
		return board[x][y];
	}

	public synchronized void setBoard(int x, int y) {
		if(board[x][y] == 0) {
			board[x][y] = whosTurn();
			nextTurn();
		}
	}

	public synchronized void setBoard(int x, int y, int color) {
		board[x][y] = color;
	}

	public synchronized int whosTurn() {
		if(teamTurn > 0)
			return WHITE;
		else if(teamTurn < 0)
			return BLACK;
		else
			throw new IllegalStateException();
	}

	private synchronized void nextTurn() {
		if(teamTurn > 0) {
			teamTurn++;
			if(teamTurn > gameSettings.stonesPerTurn)
				teamTurn = -1;
		}
		else if(teamTurn < 0) {
			teamTurn--;
			if(Math.abs(teamTurn) > gameSettings.stonesPerTurn)
				teamTurn = 1;
		}
	}

	public GameSettings getSettings() {
		return gameSettings;
	}

	/*
	 * Returns the integer corresponding to the winner
	 * WHITE = 1
	 * BLACK = 2
	 */
	public synchronized int checkStatus() {
		for(int j = 0; j < board.length - 1; j++) {
			for(int k = 0; k < board[j].length; k++) {
				/*
				 * Center
				 */
				if(board[j][k] == BLANK) {
					continue;
				}
				int currentStone = board[j][k];


				/*
				 * North Eastward
				 */
				int connectedStones = 1;
				int x = j;
				int y = k;
				while(y > 0 && x < board.length - 2) {
					if(board[++x][--y] == currentStone) {
						connectedStones++;
					} else {
						break;
					}
				}
				if(connectedStones >= gameSettings.stonesToWin) { // Win
					return currentStone;
				}


				/*
				 * Eastward
				 */
				connectedStones = 1;
				x = j;
				y = k;
				while(x < board.length - 2) {
					if(board[++x][y] == currentStone) {
						connectedStones++;
					} else {
						break;
					}
				}
				if(connectedStones >= gameSettings.stonesToWin) { // Win
					return currentStone;
				}


				/*
				 * South Eastward
				 */
				connectedStones = 1;
				x = j;
				y = k;
				while(y < board[x].length - 2 && x < board.length - 2) {
					if(board[++x][++y] == currentStone) {
						connectedStones++;
					} else {
						break;
					}
				}
				if(connectedStones >= gameSettings.stonesToWin) { // Win
					return currentStone;
				}


				/*
				 * Southward
				 */
				connectedStones = 1;
				x = j;
				y = k;
				while(y < board[x].length - 2) {
					if(board[x][++y] == currentStone) {
						connectedStones++;
					} else {
						break;
					}
				}
				if(connectedStones >= gameSettings.stonesToWin) { // Win
					return currentStone;
				}
			}
		}

		return 0; // No winner
	}
}