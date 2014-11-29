package com.alvinalexander.pasty;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import javax.swing.text.Element;

public class Pasty {

	JTextPane tp;
	Document rgaDocument;
    private static final int TAB_KEY = 9;
    private String TAB_AS_SPACES = "   ";
    int currentRow = 0;
    int currentCol = 0;
	
	public Pasty() {
		JFrame f = new JFrame("Pasty");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setLayout(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane();
		tp = new JTextPane();
        tp.setFont(new Font("Monaco", Font.PLAIN, 13));
        tp.setMargin(new Insets(20, 20, 20, 20));
        tp.setBackground(new Color(218, 235, 218));
        tp.setPreferredSize(new Dimension(700, 800));
		scrollPane.getViewport().add(tp);
		scrollPane.getViewport().setPreferredSize(tp.getPreferredSize());
		
		rgaDocument = tp.getDocument();
		
	    tp.addKeyListener(new java.awt.event.KeyAdapter() {
	        public void keyPressed(KeyEvent e) {
	            tpKeyPressed(e);
	        }
	    });
	    
	    tp.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(CaretEvent e) {
	            tpCaretUpdate(e);
	        }
	    });

	    try {
			f.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		} catch(RuntimeException e) {
			// do nothing
		}
	}
	
	  private void tpKeyPressed(final KeyEvent e) {
	    // convert TAB (w/ selected text) by shifting all text over three
	    if ( (e.getKeyCode()==TAB_KEY) && (!e.isShiftDown()) && (tp.getSelectedText()!=null) )
	    {
	      String textAfterTabbing = EditActions.insertTabAtBeginningOfLine(tp.getSelectedText());
	      int start = tp.getSelectionStart();
	      int end = tp.getSelectionEnd();
	      int originalLength = end-start;
	      replaceSelectionAndKeepCursor(textAfterTabbing);
	      e.consume();
	      int newLength = textAfterTabbing.length();
	      tp.select(start,end+newLength-originalLength);
	    }
	    // convert TAB (w/ no selected text) to spaces
	    else if ( (e.getKeyCode()==TAB_KEY) && (!e.isShiftDown()) && (tp.getSelectedText()==null) )
	    {
	      String textAfterTabbing = TAB_AS_SPACES;
	      replaceSelectionAndKeepCursor(textAfterTabbing);
	      e.consume();
	    }
	    // SHIFT-TAB w/ selected text
	    else if ( (e.getKeyCode()==TAB_KEY) && (e.isShiftDown()) && (tp.getSelectedText()!=null) )
	    {
	      String textAfterTabbing = EditActions.removeTabFromBeginningOfLine(tp.getSelectedText());
	      int start = tp.getSelectionStart();
	      int end = tp.getSelectionEnd();
	      int originalLength = end-start;
	      replaceSelectionAndKeepCursor(textAfterTabbing);
	      e.consume();
	      int newLength = textAfterTabbing.length();
	      tp.select(start,end+newLength-originalLength);
	    }
	    // SHIFT-TAB w/ NO selected text
	    // @todo DON'T KNOW HOW TO DO THIS
	    // @todo NEED HELP HERE
	    // maybe determine the text range; manually select the text; then do the same as is done for selected text above
	    else if ( (e.getKeyCode()==TAB_KEY) && (e.isShiftDown()) && (tp.getSelectedText()==null) )
	    {
	      Element root = rgaDocument.getDefaultRootElement();
	      Element element = root.getElement(currentRow);
	      int startOffset = element.getStartOffset();
	      int endOffset = element.getEndOffset();
	      tp.select(startOffset,endOffset-1);
	      String textOfCurrentLine = getTextOfCurrentLine(element);
	      String textAfterRemovingTabs = EditActions.removeTabFromBeginningOfLine(textOfCurrentLine);
	      replaceSelectionAndKeepCursor(textAfterRemovingTabs);
	      e.consume();
	      //int originalLength = endOffset-startOffset;
	      //int newLength = textAfterRemovingTabs.length();
	      //tp.select(startOffset,endOffset+newLength-originalLength);
	    }

	    // if ( e.isControlDown() && (e.getKeyCode()==77) ) // CTRL-m activates the popup menu
	    // if ( e.isControlDown() && (e.getKeyCode()==83) ) // CTRL-s to save

	  }
	  
	  private void tpCaretUpdate(final CaretEvent e) {
	    Element root = rgaDocument.getDefaultRootElement();
	    int dot = e.getDot();
	    int row = root.getElementIndex( dot );
	    int col = dot - root.getElement( row ).getStartOffset();
	    currentRow = row;
	    currentCol = col;
	    //updateStatusBar(row+1, col+1);
	  }

	  private String getTextOfCurrentLine(Element element) {
	    try {
	      return element.getDocument().getText( element.getStartOffset(),(element.getEndOffset()-element.getStartOffset()) );
	    }
	    catch (BadLocationException e) {
	      // this is not a great way to do this, but hopefully it doesn't matter
	      e.printStackTrace();
	      return "";
	    }
	  }

	  private void replaceSelectionAndKeepCursor(final String newText) {
	    tp.replaceSelection(newText);
	    tp.repaint();
	    tp.requestFocus();
	  }
	  
	  public static void main(String[] args) {
	      SwingUtilities.invokeLater(new Runnable() {
	          public void run() {
	              new Pasty();
	          }
	      });
      }

}













