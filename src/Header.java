/*
*	Author: Rhys B.
*	Created: 2021-12-23
*	Modified: 2021-12-23
*
*	GUI component for options panel for titles.
*/


import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class Header extends JPanel {
	private JLabel label;
	
	public Header() {
		// Default constructor
		this("");
	}
	
	public Header(String s) {
		label = new JLabel(s);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(getFont().deriveFont(Font.BOLD));
		label.setFont(getFont().deriveFont(20.0f));
		
		add(label);
	}
}
