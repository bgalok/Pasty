package com.alvinalexander.pasty;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

public class Pasty {

	public static void main(String[] args) {
		JFrame f = new JFrame("Pasty");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setLayout(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane();
		JTextPane tp = new JTextPane();
        tp.setFont(new Font("Monaco", Font.PLAIN, 13));
        tp.setMargin(new Insets(20, 20, 20, 20));
        tp.setBackground(new Color(218, 235, 218));
        tp.setPreferredSize(new Dimension(700, 800));
		scrollPane.getViewport().add(tp);
		scrollPane.getViewport().setPreferredSize(tp.getPreferredSize());

		try {
			f.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		} catch(RuntimeException e) {
			// do nothing
		}
	}

}
