/*
*	Author: Rhys B.
*	Created: 2021-12-17
*	Modified: 2021-12-17
*
*	Contains the GUI components and methods required
*	to search for a line in a document.
*/


import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.text.StyledDocument;

import javax.swing.JComponent;
import javax.swing.JTextField;


@SuppressWarnings("serial")
public class LineFinder extends JTextField implements KeyListener, ComponentListener, FocusListener {
	private EditorPane editor;
	private StyledDocument doc;
	private JComponent parent;
	
	public LineFinder() {
		// Default constructor.
		
		this(null, null);
	}
	
	public LineFinder(EditorPane editor, JComponent parent) {
		addKeyListener(this);
		addFocusListener(this);
		setEditor(editor);
		setParent(parent);
		setVisible(false);
	}
	
	public void setEditor(EditorPane editor) {
		this.editor = editor;

		if (editor != null) {
			doc = editor.getStyledDocument();
		} else {
			doc = null;
		}
	}
	
	public EditorPane getEditor() {
		return editor;
	}

	public void setParent(JComponent parent) {
		this.parent = parent;

		if (parent != null) {
			parent.addComponentListener(this);
			parent.add(this);
		}
	}

	public JComponent getParent() {
		return parent;
	}
	
	public void display() {
		// Open the GUI component.
		setText("");
		setVisible(true);
		requestFocus();
	}
	
	public void close() {
		// Close the GUI component.
		setVisible(false);
		revalidate();
	}
	
	public void keyPressed(KeyEvent ke) {
		// Called when the user presses a key.
		
		if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
			// Find the index of the start of the line.
			
			String text = getText();
			
			if (!text.equals("")) {
				setToIndexForLine(Integer.parseInt(text));
				close();
			}
		} else if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
			// Close this panel.
			close();
		}
	}

	public void setToIndexForLine(int line) {
		int p = findIndexForLine(line);
				
		if (p != -1) {
			editor.setCaretPosition(p);
			editor.refreshActiveLine();
		}
	}
	
	public void keyTyped(KeyEvent ke) {
		// Called when the user types something. Makes
		// sure they are only typing numbers.
		
		char c = ke.getKeyChar();
		if (c < '0' || c > '9') {
			ke.consume();
		}
	}
	
	public void componentResized(ComponentEvent ce) {
		// Called when the parent is resized. Used to adjust position.
		
		int width = parent.getWidth();
		setBounds(width - 150, 5, 100, 30);
	}
	
	public void focusLost(FocusEvent fe) {
		// Close this panel.
		close();
	}
	
	public int findIndexForLine(int line) {
		// Finds the index of the start of the given line.
		// Line 1 is the first line.
		
		if (line < 0) {
			return -1;
		}
		
		String text = getSafeText();
		int ln = 1;
		
		for (int i = 0; i < text.length(); i++) {
			if (ln == line) {
				return i;
			}
			
			if (text.charAt(i) == '\n') {
				ln++;
			}
		}

		return -1;
	}
	
	private String getSafeText() {
		try {
			return doc.getText(0, doc.getLength());
		} catch (Exception e) {
			return null;
		}
	}
	
	// Residue of imlementing ComponentListener.
	public void componentHidden(ComponentEvent ce) {}
	public void componentShown(ComponentEvent ce) {}
	public void componentMoved(ComponentEvent ce) {}
	
	// Residue of implementing KeyListener.
	public void keyReleased(KeyEvent ke) {}

	// Residue of implementing FocusListener.
	public void focusGained(FocusEvent fe) {}
}
