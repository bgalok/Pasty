package com.alvinalexander.pasty;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.text.Element;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

import javax.swing.undo.*;

public class Pasty {

	// mac stuff
	Application thisApp = Application.getApplication();
			
	JFrame f = new JFrame("Pasty");
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

	// undo/redo
	protected UndoableEditListener undoHandler = new UndoHandler();
	protected UndoManager undoManager = new UndoManager();
	private UndoAction undoAction = null;
	private RedoAction redoAction = null;
	private static final KeyStroke undoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.META_MASK);
	private static final KeyStroke redoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.META_MASK);	
	
	public Pasty() {
		configureFrame(f);
		configureTextArea(scrollPane);
		
	    addKeyListenerToTextArea();
		addCaretListenerToTextArea();
		configureFontSizeControls();
	    configureUndoRedoActions();
		configureQuitHandler();
		
		f.setJMenuBar(createMenuBar());
		
		f.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
		makeFrameVisible(f);
	}

	private void configureUndoRedoActions() {
		undoAction = new UndoAction();
	    tp.getInputMap().put(undoKeystroke, "undoKeystroke");
	    tp.getActionMap().put("undoKeystroke", undoAction);

	    redoAction = new RedoAction();
	    tp.getInputMap().put(redoKeystroke, "redoKeystroke");
	    tp.getActionMap().put("redoKeystroke", redoAction);
	    
	    document.addUndoableEditListener(undoHandler);
	}

	  private JMenuBar createMenuBar()
	  {
	    // create the menubar
	    JMenuBar menuBar = new JMenuBar();

	    // File menu
	    JMenu fileMenu = new JMenu("File");

	    // Edit menu
	    JMenu editMenu = new JMenu("Edit");
	    JMenuItem undoMenuItem = new JMenuItem(undoAction);
	    JMenuItem redoMenuItem = new JMenuItem(redoAction);
	    editMenu.add(undoMenuItem);
	    editMenu.add(redoMenuItem);

	    // add the menus to the menubar
	    menuBar.add(fileMenu);
	    menuBar.add(editMenu);

	    return menuBar;
	  }
	  
	  
	// this is mac-specific
	private void configureQuitHandler() {
		thisApp.setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(QuitEvent quitEvent, QuitResponse quitResponse) {
				  boolean proceedWithExit = doQuitAction();
				  if (proceedWithExit == true) {
					  System.exit(0);
				  } else {
					  quitResponse.cancelQuit();
				  }
			}
		});
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
	
	/**
	 * Returns true if the user wants to exit. 
	 */
	public boolean doQuitAction() {
        int choice = JOptionPane.showOptionDialog(f,
		      "You really want to quit?",
		      "Quit?",
		      JOptionPane.YES_NO_OPTION,
		      JOptionPane.QUESTION_MESSAGE,
		      null, null, null);
		 
		  // interpret the user's choice
		  if (choice == JOptionPane.YES_OPTION) {
		      return true;
		  } else {
			  return false;
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

	  // /////////// handle undo and redo actions //////////////////

	  class UndoHandler implements UndoableEditListener
	  {

	    /**
	     * Messaged when the Document has created an edit, the edit is added to
	     * <code>undoManager</code>, an instance of UndoManager.
	     */
	    public void undoableEditHappened(UndoableEditEvent e)
	    {
	      undoManager.addEdit(e.getEdit());
	      undoAction.update();
	      redoAction.update();
	    }
	  }

	  class UndoAction extends AbstractAction
	  {
	    public UndoAction()
	    {
	      super("Undo");
	      setEnabled(false);
	    }

	    public void actionPerformed(ActionEvent e)
	    {
	      try
	      {
	        undoManager.undo();
	      }
	      catch (CannotUndoException ex)
	      {
	        // TODO log this or ignore it
	        //ex.printStackTrace();
	      }
	      update();
	      redoAction.update();
	    }

	    protected void update()
	    {
	      if (undoManager.canUndo())
	      {
	        setEnabled(true);
	        putValue(Action.NAME, undoManager.getUndoPresentationName());
	      }
	      else
	      {
	        setEnabled(false);
	        putValue(Action.NAME, "Undo");
	      }
	    }
	  }

	  class RedoAction extends AbstractAction
	  {
	    public RedoAction()
	    {
	      super("Redo");
	      setEnabled(false);
	    }

	    public void actionPerformed(ActionEvent e)
	    {
	      try
	      {
	        undoManager.redo();
	      }
	      catch (CannotRedoException ex)
	      {
	        // TODO log this or ignore it
	        //ex.printStackTrace();
	      }
	      update();
	      undoAction.update();
	    }

	    protected void update()
	    {
	      if (undoManager.canRedo())
	      {
	        setEnabled(true);
	        putValue(Action.NAME, undoManager.getRedoPresentationName());
	      }
	      else
	      {
	        setEnabled(false);
	        putValue(Action.NAME, "Redo");
	      }
	    }
	  }
	  

} // pasty











