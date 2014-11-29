package com.alvinalexander.pasty;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTextPane;

public class DecreaseFontSizeAction extends AbstractAction
{
	Pasty controller;
	JTextPane textPane;
	
	public DecreaseFontSizeAction(Pasty controller, JTextPane textPane, String name, Integer mnemonic) {
		super(name, null);
		this.controller = controller;
		this.textPane = textPane;
		putValue(MNEMONIC_KEY, mnemonic);
	}
	public void actionPerformed(ActionEvent e)
	{
		controller.decreaseFontSizeAction(textPane);
	}
}