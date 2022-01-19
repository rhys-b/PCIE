/*
*	Author: Rhys B.
*	Created: 2021-08-25
*	Modified: 2022-01-01
*
*	Handles drawing the line numbers
*	to the left of the editor pane.
*/


import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;


@SuppressWarnings("serial")
public class LineNumbers extends JComponent implements KeyListener, Runnable {
	private EditorPane editor;
	private JScrollBar scrollBar;
	private JScrollPane scroller;
	private int lines = 1, lineHeight, offset, charWidth, width;
	private FontMetrics metrics;

	public LineNumbers(EditorPane editor, JScrollPane scroller) {
		this.editor = editor;
		this.scroller = scroller;
		this.scrollBar = scroller.getVerticalScrollBar();

		this.editor.addKeyListener(this);

		updateOptions();
	}
	
	public void updateOptions() {
		metrics = getFontMetrics(Defaults.EDITOR_FONT);
		lineHeight = metrics.getHeight();
		offset = 2 - metrics.getDescent();
		charWidth = metrics.charWidth('3');
		recalculateLines();
		setLines(lines);
		repaint();
	}

	public void paintComponent(Graphics g) {
		Graphics2D gg = (Graphics2D) g;
		gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		gg.setColor(Defaults.EDITOR_BACKGROUND_COLOR.darker());
		gg.fillRect(0, 0, getWidth(), getHeight());

		gg.setFont(Defaults.EDITOR_FONT);
		gg.setColor(Defaults.EDITOR_FONT_COLOR);

		int sw;
		String s;
		int scroll = scrollBar.getValue();
		int num = (scroll / lineHeight) + 1;
		int finalNum = num + (scroller.getHeight() / lineHeight) + 1;
		for (; num <= finalNum && num <= lines; num++) {
			s = Integer.toString(num);
			sw = metrics.stringWidth(s);

			gg.drawString(s, width - sw, lineHeight * num + offset);
		}

		Defaults.TOOLKIT.sync();
	}

	public void keyPressed(KeyEvent ke) {
		SwingUtilities.invokeLater(this);
	}

	public void run() {
		recalculateLines();
	}

	public void recalculateLines() {
		int newLines = 1;
		String string = editor.getText();
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == '\n') {
				newLines++;
			}
		}

		if (lines != newLines) {
			setLines(newLines);
		}
	}

	private void setLines(int newLines) {
		lines = newLines;
		width = ((int)Math.log10(lines) + 1) * charWidth;
		
		setPreferredSize(new Dimension(width, editor.getPreferredHeight()));
		revalidate();
		repaint();
	}

	public void keyReleased(KeyEvent ke) {}
	public void keyTyped(KeyEvent ke) {}
}
