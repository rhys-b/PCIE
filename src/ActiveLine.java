/*
*	Author: Rhys B.
*	Created: 2021-12-18
*	Modified: 2021-12-31
*
*	Handles the change in color for the current line.
*/


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import javax.swing.text.StyledDocument;


@SuppressWarnings("serial")
public class ActiveLine extends JComponent implements	KeyListener,
							MouseListener,
							MouseMotionListener,
							ComponentListener,
							CaretListener,
							Runnable
{
	private JTextPane text;
	private StyledDocument doc;
	private int currentLine = 0;
	
	private int height, descent;
	
	public ActiveLine() {
		// Default constructor.
		
		this(null);
	}
	
	public ActiveLine(JTextPane text) {
		setTextPane(text);
		updateOptions();
	}
	
	public void updateOptions() {
		FontMetrics fontMetrics = getFontMetrics(Defaults.EDITOR_FONT);
		height = fontMetrics.getHeight();
		descent = fontMetrics.getDescent();

		setLine(currentLine, true);
		
		repaint();
	}
	
	public void setTextPane(JTextPane t) {
		// Remove this from the old editor's listeners.
		if (text != null) {
			text.removeKeyListener(this);
			text.removeMouseListener(this);
			text.removeMouseMotionListener(this);
			text.removeComponentListener(this);
			text.removeCaretListener(this);
		}
		
		text = t;
		
		// Add this to the new editor's listeners.
		if (text != null) {
			text.addKeyListener(this);
			text.addMouseListener(this);
			text.addMouseMotionListener(this);
			text.addComponentListener(this);
			text.addCaretListener(this);
			doc = text.getStyledDocument();
		} else {
			doc = null;
		}
	}
	
	public JTextPane getTextPane() {
		return text;
	}
	
	public void keyPressed(KeyEvent ke) {
		int k = ke.getKeyCode();
		
		if (	k == KeyEvent.VK_ENTER ||
			k == KeyEvent.VK_DOWN ||
			k == KeyEvent.VK_UP ||
			k == KeyEvent.VK_LEFT ||
			k == KeyEvent.VK_RIGHT ||
			k == KeyEvent.VK_BACK_SPACE ||
			k == KeyEvent.VK_TAB ||
			k == KeyEvent.VK_KP_UP ||
			k == KeyEvent.VK_KP_DOWN ||
			k == KeyEvent.VK_KP_LEFT ||
			k == KeyEvent.VK_KP_RIGHT ||
			k == KeyEvent.VK_PAGE_UP ||
			k == KeyEvent.VK_PAGE_DOWN)
		{
			SwingUtilities.invokeLater(this);
		}
	}
	
	public void mousePressed(MouseEvent me) {
		SwingUtilities.invokeLater(this);
	}
	
	public void mouseDragged(MouseEvent me) {
		SwingUtilities.invokeLater(this);
	}
	
	public void caretUpdate(CaretEvent ce) {
		SwingUtilities.invokeLater(this);
	}
	
	public void run() {
		if (Defaults.ACTIVE_LINE) {
			setLine(findLine(text.getCaretPosition()));
		}
	}
	
	public void componentResized(ComponentEvent ce) {
		Dimension a = text.getSize();
		Rectangle b = getBounds();
		
		if (a.getWidth() != b.getWidth()) {
			setBounds(0, (int)b.getY(), (int)a.getWidth(), height + descent - 2);
		}
	}
	
	private int findLine(int index) {
		// Finds the line that the given index is on.
		// The first line is 0.
		
		if (index < 0) {
			return -1;
		}
		
		String s = null;
		try {
			s = doc.getText(0, doc.getLength());
		} catch (Exception e) {
			ErrorMgr.show(e, "Error getting text for active line");
		}
		
		int ln = 0;
		for (int i = 0; i < s.length(); i++) {
			if (i == index) {
				return ln;
			}
			
			if (s.charAt(i) == '\n') {
				ln++;
			}
		}

		return ln;
	}

	private void setLine(int line) {
		setLine(line, false);
	}
	
	private void setLine(int line, boolean force) {
		if (currentLine == line && !force) {
			return;
		}
		
		int top = (height * line) + 1;
		setBounds(0, top, text.getWidth(), height + descent - 2);
		currentLine = line;
	}

	public int getCurrentLine() {
		return currentLine;
	}
	
	public void paintComponent(Graphics g) {
		if (Defaults.ACTIVE_LINE) {
			Color c = Defaults.EDITOR_FONT_COLOR;
			g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
			g.drawRect(0, 0, getWidth(), getHeight() - 1);
		}
	}

	public void keyTyped(KeyEvent ke) {}
	public void mouseReleased(MouseEvent me) {}
	public void mouseClicked(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void keyReleased(KeyEvent ke) {}
	public void componentHidden(ComponentEvent ce) {}
	public void componentShown(ComponentEvent ce) {}
	public void componentMoved(ComponentEvent ce) {}
	public void mouseMoved(MouseEvent me) {}
}
