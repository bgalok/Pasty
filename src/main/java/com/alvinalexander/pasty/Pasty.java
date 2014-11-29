package com.alvinalexander.pasty;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.text.Element;

public class Pasty {

	private JTextPane tp;
	private Document document;
	private final JScrollPane scrollPane = new JScrollPane();
	private static final int TAB_KEY = 9;
	private static final String TAB_AS_SPACES = "   ";
	private int currentRow = 0;
	private int currentCol = 0;

	// increase/decrease font size
	private Action decreaseFontSizeAction;
	private Action increaseFontSizeAction;
	private static final KeyStroke smallerFontSizeKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Event.META_MASK);
	// handle [meta][equals], [meta][equals][shift], and [meta][plus]
	private static final KeyStroke largerFontSizeKeystroke1 = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Event.META_MASK);
	private static final KeyStroke largerFontSizeKeystroke2 = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Event.META_MASK + Event.SHIFT_MASK);
	private static final KeyStroke largerFontSizeKeystroke3 = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, Event.META_MASK);

	// quit handler
	private Action quitAction;
	private static final KeyStroke quitKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.META_MASK);
	
	public Pasty() {
		JFrame f = new JFrame("Pasty");
		configureFrame(f);
		configureTextArea(scrollPane);

		addKeyListenerToTextArea();
		addCaretListenerToTextArea();
		configureFontSizeControls();
		configureQuitHandler();

		f.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent we) {
				  doExitAction();
			  }
	    });
		
		f.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
		makeFrameVisible(f);
	}

	private void configureQuitHandler() {
		quitAction = new CloseFileAction(this, "Quit", null);
		tp.getInputMap().put(quitKeystroke, "quitKeystroke");
		tp.getActionMap().put("quitKeystroke", quitAction);
	}

	private void configureFrame(JFrame f) {
		//f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setLayout(new BorderLayout());
	}

	private void makeFrameVisible(JFrame f) {
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	private void configureTextArea(JScrollPane scrollPane) {
		tp = new JTextPane();
		tp.setFont(new Font("Monaco", Font.PLAIN, 13));
		tp.setMargin(new Insets(20, 20, 20, 20));
		tp.setBackground(new Color(218, 235, 218));
		tp.setPreferredSize(new Dimension(700, 800));
		scrollPane.getViewport().add(tp);
		scrollPane.getViewport().setPreferredSize(tp.getPreferredSize());
		document = tp.getDocument();
	}

	private void addKeyListenerToTextArea() {
		tp.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				tpKeyPressed(e);
			}
		});
	}

	private void addCaretListenerToTextArea() {
		tp.addCaretListener(new javax.swing.event.CaretListener() {
			public void caretUpdate(CaretEvent e) {
				tpCaretUpdate(e);
			}
		});
	}

	/**
	 * Let the user (me) increase and decrease the font size.
	 */
	private void configureFontSizeControls() {
		decreaseFontSizeAction = new DecreaseFontSizeAction(this, "Font--", smallerFontSizeKeystroke.getKeyCode());
		tp.getInputMap().put(smallerFontSizeKeystroke,   "smallerFontSizeKeystroke");
		tp.getActionMap().put("smallerFontSizeKeystroke", decreaseFontSizeAction);
		increaseFontSizeAction = new IncreaseFontSizeAction(this, "Font++", largerFontSizeKeystroke1.getKeyCode());
		tp.getInputMap().put(largerFontSizeKeystroke1,   "largerFontSizeKeystroke");
		tp.getActionMap().put("largerFontSizeKeystroke",  increaseFontSizeAction);
		tp.getInputMap().put(largerFontSizeKeystroke2,   "largerFontSizeKeystroke2");
		tp.getActionMap().put("largerFontSizeKeystroke2", increaseFontSizeAction);
		tp.getInputMap().put(largerFontSizeKeystroke3,   "largerFontSizeKeystroke3");
		tp.getActionMap().put("largerFontSizeKeystroke3", increaseFontSizeAction);
	}

	private void tpKeyPressed(final KeyEvent e) {
		// convert TAB (w/ selected text) by shifting all text over three
		if ((e.getKeyCode() == TAB_KEY) && (!e.isShiftDown())
				&& (tp.getSelectedText() != null)) {
			String textAfterTabbing = EditActions.insertTabAtBeginningOfLine(tp
					.getSelectedText());
			int start = tp.getSelectionStart();
			int end = tp.getSelectionEnd();
			int originalLength = end - start;
			replaceSelectionAndKeepCursor(textAfterTabbing);
			e.consume();
			int newLength = textAfterTabbing.length();
			tp.select(start, end + newLength - originalLength);
		}
		// convert TAB (w/ no selected text) to spaces
		else if ((e.getKeyCode() == TAB_KEY) && (!e.isShiftDown())
				&& (tp.getSelectedText() == null)) {
			String textAfterTabbing = TAB_AS_SPACES;
			replaceSelectionAndKeepCursor(textAfterTabbing);
			e.consume();
		}
		// SHIFT-TAB w/ selected text
		else if ((e.getKeyCode() == TAB_KEY) && (e.isShiftDown())
				&& (tp.getSelectedText() != null)) {
			String textAfterTabbing = EditActions
					.removeTabFromBeginningOfLine(tp.getSelectedText());
			int start = tp.getSelectionStart();
			int end = tp.getSelectionEnd();
			int originalLength = end - start;
			replaceSelectionAndKeepCursor(textAfterTabbing);
			e.consume();
			int newLength = textAfterTabbing.length();
			tp.select(start, end + newLength - originalLength);
		}
		// SHIFT-TAB w/ NO selected text
		// @todo DON'T KNOW HOW TO DO THIS
		// @todo NEED HELP HERE
		// maybe determine the text range; manually select the text; then do the
		// same as is done for selected text above
		else if ((e.getKeyCode() == TAB_KEY) && (e.isShiftDown())
				&& (tp.getSelectedText() == null)) {
			Element root = document.getDefaultRootElement();
			Element element = root.getElement(currentRow);
			int startOffset = element.getStartOffset();
			int endOffset = element.getEndOffset();
			tp.select(startOffset, endOffset - 1);
			String textOfCurrentLine = getTextOfCurrentLine(element);
			String textAfterRemovingTabs = EditActions
					.removeTabFromBeginningOfLine(textOfCurrentLine);
			replaceSelectionAndKeepCursor(textAfterRemovingTabs);
			e.consume();
			// int originalLength = endOffset-startOffset;
			// int newLength = textAfterRemovingTabs.length();
			// tp.select(startOffset,endOffset+newLength-originalLength);
		}

		// if ( e.isControlDown() && (e.getKeyCode()==77) ) // CTRL-m activates
		// the popup menu
		// if ( e.isControlDown() && (e.getKeyCode()==83) ) // CTRL-s to save

	}

	private void tpCaretUpdate(final CaretEvent e) {
		Element root = document.getDefaultRootElement();
		int dot = e.getDot();
		int row = root.getElementIndex(dot);
		int col = dot - root.getElement(row).getStartOffset();
		currentRow = row;
		currentCol = col;
		// updateStatusBar(row+1, col+1);
	}

	private String getTextOfCurrentLine(Element element) {
		try {
			return element.getDocument().getText(element.getStartOffset(),
					(element.getEndOffset() - element.getStartOffset()));
		} catch (BadLocationException e) {
			// this is not a great way to do this, but hopefully it doesn't
			// matter
			e.printStackTrace();
			return "";
		}
	}

	private void replaceSelectionAndKeepCursor(final String newText) {
		tp.replaceSelection(newText);
		tp.repaint();
		tp.requestFocus();
	}
	
	public void doExitAction() {
		System.err.println("EXIT ACTION");
        int choice = JOptionPane.showOptionDialog(null,
		      "You really want to quit?",
		      "Quit?",
		      JOptionPane.YES_NO_OPTION,
		      JOptionPane.QUESTION_MESSAGE,
		      null, null, null);
		 
		  // interpret the user's choice
		  if (choice == JOptionPane.YES_OPTION) {
		    System.exit(0);
		  }
    }

	/**
	 * Reduce the size of the font in the editor area.
	 */
	public void decreaseFontSizeAction() {
		Font f = tp.getFont();
		Font f2 = new Font(f.getFontName(), f.getStyle(), f.getSize() - 1);
		tp.setFont(f2);
	}

	public void increaseFontSizeAction() {
		Font f = tp.getFont();
		Font f2 = new Font(f.getFontName(), f.getStyle(), f.getSize() + 1);
		tp.setFont(f2);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Pasty();
			}
		});
	}

}
