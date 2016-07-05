package com.jaewanyun.omoc.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

public class TextPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static JTextArea chatArea;
	private static JTextField inputArea;

	public TextPanel() {
		super(new BorderLayout());
		setPreferredSize(new Dimension(500, 100));

		// Chat viewing area
		chatArea = new JTextArea();
		chatArea.setEditable(false);
		chatArea.setWrapStyleWord(true);
		chatArea.setFont(new Font("Lucida Sans Typewriter", Font.BOLD, 10));
		DefaultCaret caret = (DefaultCaret) chatArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		// Add to scroll pane so that the text area is scrollable
		JScrollPane scrollPane = new JScrollPane(chatArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// Text input area
		inputArea = new JTextField();
		inputArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(MainFrame.hasNametag()) {
					MainFrame.send(MainFrame.getNametag() + ": " + inputArea.getText());
				} else {
					MainFrame.send(MainFrame.getId() + ": " + inputArea.getText());
				}
				inputArea.setText("");
			}
		});

		add(scrollPane, BorderLayout.CENTER);
		add(inputArea, BorderLayout.SOUTH);

		// UI
		chatArea.setBorder(BorderFactory.createEmptyBorder());
		chatArea.setMargin(new Insets(5, 5, 5, 5));
		chatArea.setBackground(new Color(60, 80, 90));
		chatArea.setForeground(new Color(219, 240, 254));
	}

	static void append(String append) {
		chatArea.append(append);
	}
}
