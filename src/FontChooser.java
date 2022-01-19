/*
*	Author: Rhys B.
*	Created: 2021-12-22
*	Modified: 2021-12-22
*
*	Allows the user to change the font.
*/


import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.GroupLayout;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class FontChooser extends JPanel {
	private JLabel label;
	private JComboBox<String> fonts;
	private JSpinner size;
	
	public FontChooser() {
		// Default constructor.
		this("Choose a font", new Font(Font.MONOSPACED, Font.PLAIN,16));
	}
	
	public FontChooser(String title, Font font) {
		String[] fontNames = GraphicsEnvironment
						.getLocalGraphicsEnvironment()
						.getAvailableFontFamilyNames();
		fonts = new JComboBox<String>(fontNames);
		fonts.setSelectedItem(font.getName());
		
		label = new JLabel(title);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		
		size = new JSpinner(new SpinnerNumberModel(
			font.getSize(), 0, 100, 1)
		);
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(label)
			.addComponent(fonts)
			.addComponent(size)
		);
		
		layout.setVerticalGroup(layout.createParallelGroup()
			.addComponent(label, 30, 30, 30)
			.addComponent(fonts, 30, 30, 30)
			.addComponent(size, 30, 30, 30)
		);
	}
	
	public Font gitFont() {
		return new Font(	(String)fonts.getSelectedItem(),
					Font.PLAIN,
					(int)size.getValue());
	}
	
	public int gitSize() {
		return (int)size.getValue();
	}
}