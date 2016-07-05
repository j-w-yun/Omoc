package com.jaewanyun.omoc.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.jaewanyun.omoc.GameState;

public class DisplayPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static JButton[][] buttons;

	public DisplayPanel() {
		super();
		setPreferredSize(new Dimension(500, 500));

		setBackground(new Color(60, 80, 90));
	}

	public void update(GameState gameState) {

		int width = gameState.width();
		int height = gameState.height();

		setLayout(new GridLayout(width, height));

		if(buttons == null) {
			buttons = new JButton[width][height];
		}

		for(int j = 0; j < width; j++) {
			for(int k = 0; k < height; k++) {
				if(buttons[j][k] == null) { // Initialize buttons if they have not been
					buttons[j][k] = new JButton("");
					buttons[j][k].setToolTipText(null);
					buttons[j][k].setBorder(BorderFactory.createLineBorder(new Color(51, 102, 153)));
					buttons[j][k].addActionListener(new ButtonListener(j, k));
					buttons[j][k].setEnabled(false);
					add(buttons[j][k], j, k);
				}

				if (MainFrame.playingWhite() && gameState.whosTurn() == GameState.WHITE) {
					buttons[j][k].setEnabled(true);
				} else if (!MainFrame.playingWhite() && gameState.whosTurn() == GameState.BLACK) {
					buttons[j][k].setEnabled(true);
				} else {
					buttons[j][k].setEnabled(false);
				}

				int stoneColor = gameState.getBoard(j, k);
				if(stoneColor == GameState.BLANK)
					buttons[j][k].setBackground(new Color(205, 205, 150));
				else if(stoneColor == GameState.WHITE)
					buttons[j][k].setBackground(Color.WHITE);
				else if(stoneColor == GameState.BLACK)
					buttons[j][k].setBackground(Color.BLACK);

			}
		}


		revalidate();
		repaint();
	}

	class ButtonListener implements ActionListener {
		int j;
		int k;

		public ButtonListener(int j, int k) {
			super();
			this.j = j;
			this.k = k;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			MainFrame.getGameState().setBoard(j, k);
			DisplayPanel.this.update(MainFrame.getGameState());
			MainFrame.send(MainFrame.getGameState());

			if(MainFrame.getGameState().checkStatus() == GameState.BLACK) {
				MainFrame.send("BLACK WINS!");
			} else if (MainFrame.getGameState().checkStatus() == GameState.WHITE) {
				MainFrame.send("WHITE WINS!");
			}
		}
	}
}
