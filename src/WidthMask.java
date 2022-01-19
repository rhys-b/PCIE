/*
*	Author: Rhys B.
*	Created: 2021-12-20
*	Modified: 2021-12-23
*
*	Displays a visible indication of the preferred
*	maximum width of the document(usually at 80 characters)
*/


import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import javax.swing.JTextPane;


@SuppressWarnings("serial")
public class WidthMask extends JComponent implements ComponentListener {
	private JTextPane text;
	
	private int offset;
	
	public WidthMask() {
		// Default constructor.
		
		this(null);
	}
	
	public WidthMask(JTextPane text) {
		setTextPane(text);
	}
	
	public void updateOptions() {
		recalculateWidth();
		recalculateBounds();
	}
	
	public void setTextPane(JTextPane t) {
		if (text != null) {
			text.removeComponentListener(this);
		}
		
		text = t;
		recalculateWidth();
		
		if (text != null) {
			text.addComponentListener(this);
		}
	}
	
	public JTextPane getTextPane() {
		return text;
	}
	
	public void componentResized(ComponentEvent ce) {
		recalculateBounds();
	}
	
	public void recalculateBounds() {
		setBounds(offset - 2, 0, text.getWidth() - offset, text.getHeight());
	}
	
	public void recalculateWidth() {
		FontMetrics fontMetrics = getFontMetrics(Defaults.EDITOR_FONT);
		offset = fontMetrics.stringWidth("m") * Defaults.WIDTH_INDEX + 10;
	}
	
	public void paintComponent(Graphics g) {
		if (Defaults.SHOW_WIDTH) {
			Color c = Defaults.EDITOR_BACKGROUND_COLOR.darker();
			c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 50);
			g.setColor(c);
			
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}
	
	public void componentShown(ComponentEvent ce) {}
	public void componentHidden(ComponentEvent ce) {}
	public void componentMoved(ComponentEvent ce) {}
}