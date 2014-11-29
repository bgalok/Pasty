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
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Element;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

import javax.swing.undo.*;

public class Pasty {

	// mac stuff
	Application thisApp = Application.getApplication();
			
	JFrame mainFrame = new JFrame("Scratchpad");
//	private JTextPane tp;
//	private Document document;
	
	// "current" things
	private Document currentDocument;
	private JScrollPane currentScrollPane;
	private JTextPane currentTextPane;
	private List<JTextPane> textPanes = new ArrayList<JTextPane>();
	private List<JScrollPane> scrollPanes = new ArrayList<JScrollPane>();
	
	private final JScrollPane scrollPane = new JScrollPane();
	private final JTabbedPane tabPane = new JTabbedPane();
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
	
	// tabs to spaces
	private Action tabsToSpacesAction = null;
	private static final KeyStroke tabsToSpacesKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.META_MASK);

	// new tab action
	private Action newTabAction = null;
	private Action renameTabAction = null;
	private Action closeTabAction = null;
	private Action nextTabAction = null;
	private Action previousTabAction = null;
	private static final KeyStroke newTabKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.META_MASK);
	private static final KeyStroke renameTabKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.META_MASK);
	private static final KeyStroke closeTabKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.META_MASK);
	private static final KeyStroke nextTabKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.META_MASK);
	private static final KeyStroke previousTabKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.META_MASK);

	
	public Pasty() {
		configureFrame(mainFrame);
		
		currentTextPane = createNewJTextPane();
		currentScrollPane = createNewScrollPaneWithEditor(currentTextPane);
		textPanes.add(currentTextPane);
		scrollPanes.add(currentScrollPane);
		tabPane.add(currentScrollPane, "main");
		
		configureQuitHandler();

	    mainFrame.setJMenuBar(createMenuBar());
		mainFrame.getContentPane().add(tabPane, java.awt.BorderLayout.CENTER);
		makeFrameVisible(mainFrame);
	}
	
	private void addAllListenersToTextPane(JTextPane aTextPane) {
	    addKeyListenerToTextArea(aTextPane);
		addCaretListenerToTextArea(aTextPane);
		configureFontSizeControls(aTextPane);
	    configureUndoRedoActions(aTextPane);
		configureTabsToSpacesAction(aTextPane);
		configureNewTabAction(aTextPane);
		configureRenameTabAction(aTextPane);
		configureCloseTabAction(aTextPane);
		configureNextTabAction(aTextPane);
		configurePreviousTabAction(aTextPane);
	}
	
	private JTextPane createNewJTextPane() {
		JTextPane aTextPane = new JTextPane();
		aTextPane.setFont(new Font("Monaco", Font.PLAIN, 13));
		aTextPane.setMargin(new Insets(20, 20, 20, 20));
		aTextPane.setBackground(new Color(218, 235, 218));
		aTextPane.setBackground(new Color(245, 245, 245));
		aTextPane.setPreferredSize(new Dimension(700, 800));
		addAllListenersToTextPane(aTextPane);
		return aTextPane;
	}
	
	void handleNewTabRequest(String tabName) {
		JTextPane newTextPane = createNewJTextPane();
		JScrollPane newScrollPane = createNewScrollPaneWithEditor(newTextPane);
		currentTextPane = newTextPane;
		currentScrollPane = newScrollPane;
		textPanes.add(currentTextPane);
		scrollPanes.add(currentScrollPane);
		tabPane.add(currentScrollPane, tabName);
		tabPane.setSelectedComponent(currentScrollPane);
		currentTextPane.requestFocus();
	}

	void handleRenameTabRequest(String newTabName, int selectedIndex) {
		tabPane.setTitleAt(selectedIndex, newTabName);
	}

	private JScrollPane createNewScrollPaneWithEditor(JTextPane aTextPane) {
		JScrollPane aScrollPane = new JScrollPane();
		aScrollPane.getViewport().add(aTextPane);
		aScrollPane.getViewport().setPreferredSize(aTextPane.getPreferredSize());
		return aScrollPane;
	}

	private void configureTabsToSpacesAction(JTextPane textPane) {
		tabsToSpacesAction = new TabsToSpacesAction(this, textPane, "Tabs -> Spaces", tabsToSpacesKeystroke.getKeyCode());
		textPane.getInputMap().put(tabsToSpacesKeystroke, "tabsToSpacesKeystroke");
		textPane.getActionMap().put("tabsToSpacesKeystroke", tabsToSpacesAction);
	}

	private void configureNewTabAction(JTextPane textPane) {
		newTabAction = new NewTabAction(this, textPane, "New Tab", newTabKeystroke.getKeyCode());
		textPane.getInputMap().put(newTabKeystroke, "newTabKeystroke");
		textPane.getActionMap().put("newTabKeystroke", newTabAction);
	}

	private void configureRenameTabAction(JTextPane textPane) {
		renameTabAction = new RenameTabAction(this, textPane, "Rename a Tab", renameTabKeystroke.getKeyCode());
		textPane.getInputMap().put(renameTabKeystroke, "renameTabKeystroke");
		textPane.getActionMap().put("renameTabKeystroke", renameTabAction);
	}

	private void configureCloseTabAction(JTextPane textPane) {
		closeTabAction = new CloseTabAction(this, textPane, "Close a Tab", closeTabKeystroke.getKeyCode());
		textPane.getInputMap().put(closeTabKeystroke, "closeTabKeystroke");
		textPane.getActionMap().put("closeTabKeystroke", closeTabAction);
	}

	private void configureNextTabAction(JTextPane textPane) {
		nextTabAction = new NextTabAction(this, "Next Tab", nextTabKeystroke.getKeyCode());
		textPane.getInputMap().put(nextTabKeystroke, "nextTabKeystroke");
		textPane.getActionMap().put("nextTabKeystroke", nextTabAction);
	}

	private void configurePreviousTabAction(JTextPane textPane) {
		previousTabAction = new PreviousTabAction(this, "Previous Tab", previousTabKeystroke.getKeyCode());
		textPane.getInputMap().put(previousTabKeystroke, "previousTabKeystroke");
		textPane.getActionMap().put("previousTabKeystroke", previousTabAction);
	}

	private void configureUndoRedoActions(JTextPane textPane) {
		undoAction = new UndoAction();
		textPane.getInputMap().put(undoKeystroke, "undoKeystroke");
		textPane.getActionMap().put("undoKeystroke", undoAction);

	    redoAction = new RedoAction();
	    textPane.getInputMap().put(redoKeystroke, "redoKeystroke");
	    textPane.getActionMap().put("redoKeystroke", redoAction);
	    
	    Document document = textPane.getDocument();
	    document.addUndoableEditListener(undoHandler);
	}

	private void addKeyListenerToTextArea(final JTextPane textPane) {
		textPane.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				textPaneKeyPressed(e, textPane);
			}
		});
	}

	private void addCaretListenerToTextArea(final JTextPane textPane) {
		textPane.addCaretListener(new javax.swing.event.CaretListener() {
			public void caretUpdate(CaretEvent e) {
				tpCaretUpdate(e, textPane);
			}
		});
	}

	/**
	 * Let the user (me) increase and decrease the font size.
	 */
	private void configureFontSizeControls(JTextPane textPane) {
		decreaseFontSizeAction = new DecreaseFontSizeAction(this, textPane, "Font--", smallerFontSizeKeystroke.getKeyCode());
		textPane.getInputMap().put(smallerFontSizeKeystroke,   "smallerFontSizeKeystroke");
		textPane.getActionMap().put("smallerFontSizeKeystroke", decreaseFontSizeAction);

		increaseFontSizeAction = new IncreaseFontSizeAction(this, textPane, "Font++", largerFontSizeKeystroke1.getKeyCode());
		textPane.getInputMap().put(largerFontSizeKeystroke1,   "largerFontSizeKeystroke");
		textPane.getActionMap().put("largerFontSizeKeystroke",  increaseFontSizeAction);
		textPane.getInputMap().put(largerFontSizeKeystroke2,   "largerFontSizeKeystroke2");
		textPane.getActionMap().put("largerFontSizeKeystroke2", increaseFontSizeAction);
		textPane.getInputMap().put(largerFontSizeKeystroke3,   "largerFontSizeKeystroke3");
		textPane.getActionMap().put("largerFontSizeKeystroke3", increaseFontSizeAction);
	}
	  
	// this is mac-specific
	private void configureQuitHandler() {
		thisApp.setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(QuitEvent quitEvent, QuitResponse quitResponse) {
				  boolean proceedWithExit = userWantsToProceedWithQuitAction();
				  if (proceedWithExit == true) {
					  System.exit(0);
				  } else {
					  quitResponse.cancelQuit();
				  }
			}
		});
	}

	private JMenuBar createMenuBar() {
	    // create the menubar
	    JMenuBar menuBar = new JMenuBar();

	    // File menu
	    JMenu fileMenu = new JMenu("File");

	    // Edit menu
	    JMenu editMenu = new JMenu("Edit");
	    JMenuItem undoMenuItem = new JMenuItem(undoAction);
	    JMenuItem redoMenuItem = new JMenuItem(redoAction);
	    JMenuItem tabsToSpacesMenuItem = new JMenuItem(tabsToSpacesAction);
	    editMenu.add(undoMenuItem);
	    editMenu.add(redoMenuItem);
	    editMenu.add(tabsToSpacesMenuItem);

	    // tabs
	    JMenu tabsMenu = new JMenu("Tabs");
	    JMenuItem newTabMenuItem = new JMenuItem(newTabAction);
	    JMenuItem renameTabMenuItem = new JMenuItem(renameTabAction);
	    JMenuItem nextTabMenuItem = new JMenuItem(nextTabAction);
	    JMenuItem previousTabMenuItem = new JMenuItem(previousTabAction);
	    tabsMenu.add(newTabMenuItem);
	    tabsMenu.add(renameTabMenuItem);
	    tabsMenu.add(nextTabMenuItem);
	    tabsMenu.add(previousTabMenuItem);

	    // add the menus to the menubar
	    menuBar.add(fileMenu);
	    menuBar.add(editMenu);
	    menuBar.add(tabsMenu);

	    return menuBar;
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

//	private void configureTextArea(JScrollPane scrollPane) {
//		tp = new JTextPane();
//		tp.setFont(new Font("Monaco", Font.PLAIN, 13));
//		tp.setMargin(new Insets(20, 20, 20, 20));
//		tp.setBackground(new Color(218, 235, 218));
//		tp.setBackground(new Color(245, 245, 245));
//		tp.setPreferredSize(new Dimension(700, 800));
//		scrollPane.getViewport().add(tp);
//		scrollPane.getViewport().setPreferredSize(tp.getPreferredSize());
//		document = tp.getDocument();
//	}


	private void textPaneKeyPressed(final KeyEvent e, final JTextPane tp) {
		// convert TAB (w/ selected text) by shifting all text over three
		if ((e.getKeyCode() == TAB_KEY) && (!e.isShiftDown())
				&& (tp.getSelectedText() != null)) {
			String textAfterTabbing = EditActions.insertTabAtBeginningOfLine(tp
					.getSelectedText());
			int start = tp.getSelectionStart();
			int end = tp.getSelectionEnd();
			int originalLength = end - start;
			replaceSelectionAndKeepCursor(textAfterTabbing, tp);
			e.consume();
			int newLength = textAfterTabbing.length();
			tp.select(start, end + newLength - originalLength);
		}
		// convert TAB (w/ no selected text) to spaces
		else if ((e.getKeyCode() == TAB_KEY) && (!e.isShiftDown())
				&& (tp.getSelectedText() == null)) {
			String textAfterTabbing = TAB_AS_SPACES;
			replaceSelectionAndKeepCursor(textAfterTabbing, tp);
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
			replaceSelectionAndKeepCursor(textAfterTabbing, tp);
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
			Document document = tp.getDocument();
			Element root = document.getDefaultRootElement();
			Element element = root.getElement(currentRow);
			int startOffset = element.getStartOffset();
			int endOffset = element.getEndOffset();
			tp.select(startOffset, endOffset - 1);
			String textOfCurrentLine = getTextOfCurrentLine(element);
			String textAfterRemovingTabs = EditActions
					.removeTabFromBeginningOfLine(textOfCurrentLine);
			replaceSelectionAndKeepCursor(textAfterRemovingTabs, tp);
			e.consume();
			// int originalLength = endOffset-startOffset;
			// int newLength = textAfterRemovingTabs.length();
			// tp.select(startOffset,endOffset+newLength-originalLength);
		}

		// if ( e.isControlDown() && (e.getKeyCode()==77) ) // CTRL-m activates
		// the popup menu
		// if ( e.isControlDown() && (e.getKeyCode()==83) ) // CTRL-s to save

	}

	private void tpCaretUpdate(final CaretEvent e, JTextPane tp) {
		Document document = tp.getDocument();
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

	private void replaceSelectionAndKeepCursor(final String newText, final JTextPane tp) {
		tp.replaceSelection(newText);
		tp.repaint();
		tp.requestFocus();
	}
	
	/**
	 * Returns true if the user wants to exit. 
	 */
	public boolean userWantsToProceedWithQuitAction() {
        int choice = JOptionPane.showOptionDialog(mainFrame,
		      "You really want to quit?",
		      "Quit?",
		      JOptionPane.YES_NO_OPTION,
		      JOptionPane.WARNING_MESSAGE,
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
	public void decreaseFontSizeAction(final JTextPane tp) {
		Font f = tp.getFont();
		Font f2 = new Font(f.getFontName(), f.getStyle(), f.getSize() - 1);
		tp.setFont(f2);
	}

	public void increaseFontSizeAction(final JTextPane tp) {
		Font f = tp.getFont();
		Font f2 = new Font(f.getFontName(), f.getStyle(), f.getSize() + 1);
		tp.setFont(f2);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new Pasty();
				} catch (Throwable t) {
					// this might help keep the app alive in the event of something bad,
					// which will hopefully give me enough time to copy what i have in
					// the text areas.
				}
			}
		});
	}
	
	
	class NewTabAction extends AbstractAction {
		JTextPane tp;
		Pasty controller;
		public NewTabAction(final Pasty controller, final JTextPane tp, String name, Integer mnemonic) {
			super(name, null);
			putValue(MNEMONIC_KEY, mnemonic);
			this.controller = controller;
			this.tp = tp;
		}
		public void actionPerformed(ActionEvent e)
		{
			// show a dialog requesting a tab name
			String tabName = JOptionPane.showInputDialog(mainFrame, "Name for the new tab:");
			if (tabName == null && tabName.trim().equals("")) {
				// do nothing
			} else {
				controller.handleNewTabRequest(tabName);
			}
		}
	}	
	
	
	class RenameTabAction extends AbstractAction {
		JTextPane tp;
		Pasty controller;
		public RenameTabAction(final Pasty controller, final JTextPane tp, String name, Integer mnemonic) {
			super(name, null);
			putValue(MNEMONIC_KEY, mnemonic);
			this.controller = controller;
			this.tp = tp;
		}
		public void actionPerformed(ActionEvent e) {
			int tabIndex = tabPane.getSelectedIndex();
			String oldName = tabPane.getTitleAt(tabIndex);
			String tabName = JOptionPane.showInputDialog(mainFrame, "New name for the tab:");
			if (tabName == null && tabName.trim().equals("")) {
				// do nothing
			} else {
				controller.handleRenameTabRequest(tabName, tabIndex);
			}
		}
	}	
	
	class NextTabAction extends AbstractAction {
		Pasty controller;
		public NextTabAction(final Pasty controller, String name, Integer mnemonic) {
			super(name, null);
			putValue(MNEMONIC_KEY, mnemonic);
			this.controller = controller;
		}
		public void actionPerformed(ActionEvent e) {
			int tabCount = tabPane.getTabCount();
			if (tabCount == 1) return;
			int newTabIndex = tabPane.getSelectedIndex() + 1;  // 0-based
			if (newTabIndex > tabCount-1) {
				tabPane.setSelectedIndex(0);
			} else {
				tabPane.setSelectedIndex(newTabIndex);
			}
		}
	}	
	
	class PreviousTabAction extends AbstractAction {
		Pasty controller;
		public PreviousTabAction(final Pasty controller, String name, Integer mnemonic) {
			super(name, null);
			putValue(MNEMONIC_KEY, mnemonic);
			this.controller = controller;
		}
		public void actionPerformed(ActionEvent e) {
			int tabCount = tabPane.getTabCount();
			if (tabCount == 1) return;
			int newTabIndex = tabPane.getSelectedIndex() - 1;  // 0-based
			if (newTabIndex < 0) {
				tabPane.setSelectedIndex(tabCount-1);
			} else {
				tabPane.setSelectedIndex(newTabIndex);
			}
		}
	}	
	
	class CloseTabAction extends AbstractAction {
		JTextPane tp;
		Pasty controller;
		public CloseTabAction(final Pasty controller, final JTextPane tp, String name, Integer mnemonic) {
			super(name, null);
			putValue(MNEMONIC_KEY, mnemonic);
			this.controller = controller;
			this.tp = tp;
		}
		public void actionPerformed(ActionEvent e) {
			int tabCount = tabPane.getTabCount();
			if (tabCount == 1) {
				if (controller.userWantsToProceedWithQuitAction()) {
					System.exit(0);
				}
			} else {
				// confirm
				int choice = JOptionPane.showOptionDialog(null,
				    "Close this tab?",
					"Close Tab?",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null, null, null);
				if (choice == JOptionPane.YES_OPTION) {
				    int tabIndex = tabPane.getSelectedIndex();
				    tabPane.remove(tabIndex);
				}
			}
		}
	}	
	
	/**
	 * Convert tabs to spaces
	 */
	public class TabsToSpacesAction extends AbstractAction
	{
		JTextPane tp;
		public TabsToSpacesAction(final Pasty controller, final JTextPane tp, String name, Integer mnemonic) {
			super(name, null);
			putValue(MNEMONIC_KEY, mnemonic);
			this.tp = tp;
		}
		public void actionPerformed(ActionEvent e)
		{
			String text = tp.getText();
			String newText = text.replaceAll("\t", "    ");
			tp.setText(newText);
		}
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











