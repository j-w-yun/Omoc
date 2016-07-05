package com.jaewanyun.omoc.gui;

import java.awt.BorderLayout;
import java.util.Random;

import javax.swing.JFrame;

import com.jaewanyun.omoc.GameState;
import com.jaewanyun.omoc.net.Client;
import com.jaewanyun.omoc.net.JayList;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static DisplayPanel displayPanel;
	private static TextPanel chatPanel;
	private static GameState gameState;
	private static int id;
	private static String name;
	private static boolean playingWhite;
	@SuppressWarnings("rawtypes")
	private static JayList in;
	@SuppressWarnings("rawtypes")
	private static JayList out;
	private static MainFrame mainFrame;

	public static MainFrame createMainFrame(String title) {
		return mainFrame == null ? mainFrame = new MainFrame(title) : mainFrame;
	}

	private MainFrame(String title) {
		super(title);

		displayPanel = new DisplayPanel();
		add(displayPanel, BorderLayout.CENTER);

		chatPanel = new TextPanel();
		add(chatPanel, BorderLayout.SOUTH);

		id = connect();

		if(gameState == null) {
			gameState = new GameState(id);
			displayPanel.update(gameState);
		}

		send(gameState);

		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	static GameState getGameState() {
		return gameState;
	}

	static int getId() {
		return id;
	}

	static String getNametag() {
		return name;
	}

	static boolean hasNametag() {
		return name != null;
	}

	static boolean playingWhite() {
		return playingWhite;
	}

	static boolean playingBlack() {
		return !playingWhite;
	}

	static void reset() {
		gameState = new GameState(id);
	}

	static void delete(int x, int y) {
		gameState.setBoard(x, y, 0);
	}

	@SuppressWarnings("unchecked")
	private static int connect() {
		Client client = new Client("100.6.20.129", 19203, true);

		while(!client.isConnected()) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {}
		}

		in = new JayList<>();
		out = new JayList<>();
		client.tether(out, in);

		new Thread(() -> {
			while(true) {
				try {
					while(!in.isEmpty()) {
						if(in.getFirst() instanceof GameState) {
							gameState = (GameState) in.removeFirst();
							displayPanel.update(gameState);
						} else if (in.getFirst() instanceof String) {
							String potentialCommand = (String) in.removeFirst();
							TextPanel.append(potentialCommand + "\n");
							String serverMessage = "";

							/*
							 * View help
							 */
							if(potentialCommand.contains("help")) {
								serverMessage = "Server: Welcome!\n"
										+ "Possible commands are listed below:\n"
										+ "reset > resets the game board\n"
										+ "delete > deletes a grid; (bottom left: 0 0, one higher: 1 0)\n"
										+ "name (desired name) > sets your name; e.g. name Steve\n"
										+ "set (white/black) > sets your color in game; e.g. set black\n"
										+ "stones (number of stones per turn) > sets the game setting; e.g. stones 2\n"
										+ "towin (number of stones aligned to win) > sets the game setting; e.g. towin 7"
										+ "";
							}

							/*
							 * Resets a grid block to default
							 */
							else if(potentialCommand.contains("delete")) {
								int readFrom = potentialCommand.indexOf("delete");
								String builder = "";
								int x = 0;
								int y = 0;
								for(int j = readFrom + 7; j < potentialCommand.length(); j++) {
									if(potentialCommand.charAt(j) == ' ') {
										x = Integer.parseInt(builder);
										builder = "";
									} else {
										builder += potentialCommand.charAt(j);
									}
								}
								y = Integer.parseInt(builder);
								delete(x, y);
								send(gameState);
							}

							/*
							 * Reset the game
							 */
							else if(potentialCommand.contains("reset")) {
								reset();
								send(gameState);
							}

							/*
							 * Set nametag
							 */
							else if(potentialCommand.contains("name") && (potentialCommand.contains(Integer.toString(id)) || potentialCommand.contains(name))) {
								int readFrom = potentialCommand.indexOf("name");
								String builder = "";
								for(int j = readFrom + 5; j < potentialCommand.length(); j++) {
									builder += potentialCommand.charAt(j);
								}
								name = builder;
								int rand = (new Random()).nextInt(9);
								if(rand == 0) {
									TextPanel.append("\nServer: Our next gentleman needs no introduction because you don’t know who he\nis anyway-\n");
									try {
										Thread.sleep(2000);
										TextPanel.append("Server: Please welcome!\n\n");
									} catch (Exception e) {}

								}
								else if(rand == 1)
									serverMessage = "Server: Please welcome to the stage, Mr. " + builder;
								else if(rand == 2) {
									TextPanel.append("\nServer: Our next gentleman is a legend-\n");
									try {
										Thread.sleep(2000);
										TextPanel.append("Server: in his own mind, Mr. " + builder + "\n\n");
									} catch (Exception e) {}
								}
								else if(rand == 3) {
									TextPanel.append("\nServer: This next gentleman has just finished his first movie.\n");
									try {
										Thread.sleep(2000);
										TextPanel.append("Server: Right after the game he’s going on netflix to watch another.\n\n");
									} catch (Exception e) {}
								}
								else if(rand == 4) {
									TextPanel.append("\nServer: Please welcome to the stage... (wait for applause)\n");
									try {
										Thread.sleep(2000);
										TextPanel.append("Server: Wait for it...\n");
										Thread.sleep(2000);
										TextPanel.append("Server: Wait...\n");
										Thread.sleep(2000);
										TextPanel.append("Server: Well, I'll see you out Mr. " + builder + "\n\n");
									} catch (Exception e) {}
								}
								else if(rand == 5 || rand == 6) {
									TextPanel.append("\nServer: Our next gentleman is autistic-\n");
									try {
										Thread.sleep(2000);
										TextPanel.append("Server: for real though. Welcome, Mr. Autistic!\n\n");
										name = "Mr. Autistic";
									} catch (Exception e) {}
								}
								else if(rand == 7) {
									TextPanel.append("\nServer: I don't care man\n");
									try {
										Thread.sleep(2000);
										TextPanel.append("Server: I don't care\n\n");
									} catch (Exception e) {}
								}
								else if(rand == 8) {
									TextPanel.append("\nServer: I’d like to introduce myself.\n");
									try {
										Thread.sleep(2000);
										TextPanel.append("Server: I’d really like to, but unfortunately I have to introduce Mr. " + builder + "\n");
										Thread.sleep(2000);
										TextPanel.append("Server: \t\t\t\t\t\t\tcunt\n");
									} catch (Exception e) {}
								}
							}

							/*
							 * Set color
							 */
							else if(potentialCommand.contains("set")) {
								if(potentialCommand.contains("white")) {
									if(potentialCommand.contains(Integer.toString(id)))
										playingWhite = true;
									else if(name != null) {
										if(potentialCommand.contains(name))
											playingWhite = true;
									}
									serverMessage = "Server: Okay, mister.";
								} else {
									if(potentialCommand.contains(Integer.toString(id)))
										playingWhite = false;
									else if(name != null) {
										if(potentialCommand.contains(name))
											playingWhite = false;
									}
									serverMessage = "Server: No.";
								}
							}

							/*
							 * Game settings
							 */
							else if(potentialCommand.contains("stones")) {
								int readFrom = potentialCommand.indexOf("stones");
								String builder = "";
								for(int j = readFrom + 7; j < potentialCommand.length(); j++) {
									builder += potentialCommand.charAt(j);
								}

								try {
									gameState.getSettings().stonesPerTurn = Integer.parseInt(builder);
									serverMessage = "Server: Stones per turn set to " + Integer.parseInt(builder);
								} catch (Exception e) {}
							}
							else if(potentialCommand.contains("towin")) {
								int readFrom = potentialCommand.indexOf("towin");
								String builder = "";
								for(int j = readFrom + 6; j < potentialCommand.length(); j++) {
									builder += potentialCommand.charAt(j);
								}

								try {
									gameState.getSettings().stonesToWin = Integer.parseInt(builder);
									serverMessage = "Server: Stones to win set to " + Integer.parseInt(builder);
								} catch (Exception e) {}
							}

							if(serverMessage.length() > 0)
								TextPanel.append("\n" + serverMessage + "\n\n");
							displayPanel.update(gameState);
						}
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {
						// Do nothing
					}
				} catch (NullPointerException npe) {}
			}
		}).start();

		return client.id();
	}

	@SuppressWarnings("unchecked")
	static void send(Object obj) {
		out.addLast(obj);
	}
}
