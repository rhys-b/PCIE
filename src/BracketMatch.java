/*
*	Author: Rhys B.
*	Created: 2021-12-23
*	Modified: 2021-12-24
*
*	Handles bracket matching. Only highlights if highlighting is on.
*/


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class BracketMatch implements MouseListener, KeyListener, Runnable {
	private class Pair {
		private int open;
		private int close;
		
		private Pair(int open, int close) {
			this.open = open;
			this.close = close;
		}
		
		private boolean equals(Pair p) {
			return p.open == open && p.close == close;
		}

		public String toString() {
			return "[open=" + open + ",close=" + close + "]";
		}
	}
	
	private static final char[] starts = {'{', '(', '['};
	private static final char[] ends = {'}', ')', ']'};
	
	private static SimpleAttributeSet attr, reset;
	
	private JTextPane textPane;
	private HighlightManager mgr;
	private StyledDocument doc;
	
	private Pair active = null;
	
	public BracketMatch() {
		// Default constructor
		this(null, null);
	}
	
	public BracketMatch(JTextPane textPane, HighlightManager mgr) {
		if (attr == null) {
			attr = new SimpleAttributeSet();
			reset = new SimpleAttributeSet();
			
			updateOptions();
		}
		
		setTextPane(textPane);
		setHighlightManager(mgr);
	}
	
	public void setTextPane(JTextPane tp) {
		if (textPane != null) {
			textPane.removeMouseListener(this);
			textPane.removeKeyListener(this);
		}

		textPane = tp;
		
		if (textPane != null) {
			textPane.addMouseListener(this);
			textPane.addKeyListener(this);
			doc = textPane.getStyledDocument();
		} else {
			doc = null;
		}
	}
	
	public void setHighlightManager(HighlightManager m) {
		mgr = m;
	}
	
	public JTextPane getTextPane() {
		return textPane;
	}
	
	public HighlightManager getHighlightManager() {
		return mgr;
	}

	public void highlight() {
		if (!Defaults.HIGHLIGHT) {
			return;
		}
		
		String text = null;
		try {
			text = doc.getText(0, doc.getLength());
		} catch (Exception e) {
			ErrorMgr.show(e, "Error getting text for bracket match.");
		}
		
		int caret = textPane.getCaretPosition();
		
		char next = '\0';
		if (caret != text.length()) {
			next = text.charAt(caret);
		}
		
		char last = '\0';
		if (caret != 0) {
			last = text.charAt(caret - 1);
		}
		
		ArrayList<Pair> list = null;
		int index = -1;
		if (	EditorPane.charArrayContains(starts, next) ||
			EditorPane.charArrayContains(ends, next))
		{
			list = createList(text, next);
			index = indexOf(list, caret);
		}
		else if (	EditorPane.charArrayContains(starts, last) ||
				EditorPane.charArrayContains(ends, last))
		{
			list = createList(text, last);
			index = indexOf(list, caret - 1);
		}
		
		paintBrackets((index == -1) ? null : list.get(index));
	}

	public void clear() {
		try {
			if (active != null) {
				doc.setCharacterAttributes(
					active.open,
					1,
					reset,
					false
				);
				
				doc.setCharacterAttributes(
					active.close,
					1,
					reset,
					false
				);

			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error clearing bracket match.");
		}
	}
	
	private ArrayList<Pair> createList(String s, char c) {
		char open, close;
		int index = indexOf(starts, c);
		
		if (index == -1) {
			index = indexOf(ends, c);
		}
		
		open = starts[index];
		close = ends[index];
		
		ArrayList<Integer> openers = new ArrayList<Integer>();
		ArrayList<Integer> closers = new ArrayList<Integer>();
		
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			
			if (mgr.shouldInsert(i)) {
				if (ch == open) {
					openers.add(i);
					closers.add(null);
				} else if (ch == close) {
					for (int j = closers.size() - 1; j >= 0; j--) {
						if (closers.get(j) == null) {
							closers.set(j, i);
							break;
						}
					}
				}
			}
		}
		
		ArrayList<Pair> out = new ArrayList<Pair>();
		for (int i = 0; i < openers.size() && i < closers.size(); i++) {
			if (closers.get(i) != null) {
				out.add(new Pair(openers.get(i), closers.get(i)));
			}
		}
		
		return out;
	}
	
	private int indexOf(char[] arr, char c) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == c) {
				return i;
			}
		}
		
		return -1;
	}
	
	private int indexOf(ArrayList<Pair> list, int index) {
		for (int i = 0; i < list.size(); i++) {
			Pair p = list.get(i);
			if (p.open == index || p.close == index) {
				return i;
			}
		}
		
		return -1;
	}
	
	private void paintBrackets(Pair p) {
		if (Defaults.HIGHLIGHT) {
			try {
				active = p;

				if (active != null) {
					// Add
					doc.setCharacterAttributes(
						active.open,
						1,
						attr,
						false
					);
					
					doc.setCharacterAttributes(
						active.close,
						1,
						attr,
						false
					);
				}
			} catch (Exception e) {
				ErrorMgr.show(e,
					"Error highlighting matching brackets."
				);
			}
		}
	}
	
	public void updateOptions() {
		StyleConstants.setForeground(attr, Defaults.COMPOSITE);
		StyleConstants.setForeground(reset, Defaults.EDITOR_FONT_COLOR);
		
		paintBrackets(active);
	}

	public void mousePressed(MouseEvent me) {
		SwingUtilities.invokeLater(this);
	}

	public void keyPressed(KeyEvent ke) {
		int k = ke.getKeyCode();

		if (	k == KeyEvent.VK_UP ||
			k == KeyEvent.VK_DOWN ||
			k == KeyEvent.VK_LEFT ||
			k == KeyEvent.VK_RIGHT ||
			k == KeyEvent.VK_PAGE_UP ||
			k == KeyEvent.VK_PAGE_DOWN ||
			k == KeyEvent.VK_KP_UP ||
			k == KeyEvent.VK_KP_DOWN ||
			k == KeyEvent.VK_KP_LEFT ||
			k == KeyEvent.VK_KP_RIGHT ||
			k == KeyEvent.VK_TAB)
		{
			SwingUtilities.invokeLater(this);
		}
	}

	public void run() {
		clear();
		highlight();
	}

	public void mouseReleased(MouseEvent me) {}
	public void mouseClicked(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void keyReleased(KeyEvent ke) {}
	public void keyTyped(KeyEvent ke) {}
}
