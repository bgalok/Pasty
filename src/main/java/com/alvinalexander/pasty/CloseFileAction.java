package com.alvinalexander.pasty;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

public class CloseFileAction extends AbstractAction
{
  Pasty controller;
  
  public CloseFileAction(Pasty controller, String name, Integer mnemonic) {
    super(name, null);
    this.controller = controller;
    putValue(MNEMONIC_KEY, mnemonic);
  }

  /**
   * If editing a file currently, close it, and clear out the editing area.
   * If not editing a file currently, not much to do.
   */
  public void actionPerformed(ActionEvent e) {
    controller.doExitAction();
  }
}