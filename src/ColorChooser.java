/*
*	Author: Rhys B.
*	Created: 2021-12-22
*	Modified: 2021-12-22
*
*	Handles user input for colors for options.
*/


import java.awt.Color;
import java.awt.Component;

import java.awt.image.BufferedImage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class ColorChooser extends JPanel implements ActionListener {
	private JLabel label;
	private JButton button;
	private Color color;
	
	public ColorChooser() {
		//Default Constructor.
		this("Pick a color", Color.BLACK);
	}
	
	public ColorChooser(String title, Color color) {
		this.color = color;
		
		label = new JLabel(title);
		
		button = new JButton();
		setButton(color);
		button.addActionListener(this);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(label)
			.addComponent(button)
		);
		
		layout.setVerticalGroup(layout.createParallelGroup()
			.addComponent(label, 30, 30, 30)
			.addComponent(button, 30, 30, 30)
		);
	}
	
	public Color getColor() {
		return color;
	}
	
	public void actionPerformed(ActionEvent ae) {
		Color tmp = JColorChooser.showDialog(
			(Component)this, label.getText(), color
		);
		
		if (tmp != null) {
			color = tmp;
			setButton(color);
		}
	}
	
	private void setButton(Color c) {
		BufferedImage img = new BufferedImage(
			1, 1, BufferedImage.TYPE_INT_ARGB
		);
		
		img.setRGB(0, 0, c.getRGB());
		
		button.setIcon(new ImageIcon(img.getScaledInstance(
			30, 30, BufferedImage.SCALE_SMOOTH)
		));
	}
}