/*
*	Author: Rhys B
*	Created: 2021-12-23
*	Modified: 2021-12-23
*
*	GUI component for the highlighting colors.
*/


import java.awt.Color;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class HighlightChooser extends JPanel {
	private JLabel label;
	private ColorButton button;
	private JCheckBox check;
	
	public HighlightChooser() {
		// Default constructor.
		this("Choose a color", Color.BLACK, false);
	}
	
	public HighlightChooser(String title, Color color, boolean checked) {
		label = new JLabel(title);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		
		button = new ColorButton(color);
		
		check = new JCheckBox("Bold", checked);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(label, 0, 100, 200)
			.addComponent(button)
			.addComponent(check, 0, 100, 200)
		);
		
		layout.setVerticalGroup(layout.createParallelGroup()
			.addComponent(label, 30, 30, 30)
			.addComponent(button, 30, 30, 30)
			.addComponent(check, 30, 30, 30)
		);
	}
	
	public Color getColor() {
		return button.getColor();
	}
	
	public boolean isSelected() {
		return check.isSelected();
	}
}