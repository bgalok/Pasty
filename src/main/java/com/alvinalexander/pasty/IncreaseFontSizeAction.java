package com.alvinalexander.pasty;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class IncreaseFontSizeAction extends AbstractAction
{
	Pasty controller;
	
	public IncreaseFontSizeAction(Pasty controller, String name, Integer mnemonic) {
		super(name, null);
		this.controller = controller;
		putValue(MNEMONIC_KEY, mnemonic);
	}
	public void actionPerformed(ActionEvent e)
	{
		controller.increaseFontSizeAction();
	}
}