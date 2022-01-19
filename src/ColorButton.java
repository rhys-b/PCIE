/*
*	Author: Rhys B
*	Created: 2021-12-23
*	Modified: 2021-12-23
*
*	GUI component that allows the user to change it's color.
*/


import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;


@SuppressWarnings("serial")
public class ColorButton extends JButton implements ActionListener {
	private Color color;
	
	public ColorButton() {
		// Default constructor
		this(Color.BLACK);
	}
	
	public ColorButton(Color c) {
		setColor(c);
		addActionListener(this);
	}
	
	public void setColor(Color c) {
		BufferedImage img = new BufferedImage(
			1, 1, BufferedImage.TYPE_INT_ARGB
		);
		
		img.setRGB(0, 0, c.getRGB());
		
		setIcon(new ImageIcon(img.getScaledInstance(
			30, 30, BufferedImage.SCALE_FAST))
		);
		
		color = c;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void actionPerformed(ActionEvent ae) {
		Color tmp = JColorChooser.showDialog(
			this, "Choose a color", color
		);
		
		if (tmp != null) {
			setColor(tmp);
		}
	}
}