/*
*	Author: Rhys B.
*	Created: 2021-12-22
*	Modified: 2021-12-23
*
*	A GUI component for the option pane that handles check boxes.
*/


import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class BoolChooser extends JPanel implements MouseListener {
	private JLabel label;
	private JCheckBox check;
	
	public BoolChooser() {
		// Default constructor.
		this("True/False", false);
	}
	
	public BoolChooser(String title, boolean b) {
		label = new JLabel(title);
		check = new JCheckBox("", b);
		
		label.addMouseListener(this);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(	label,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE)
			.addComponent(	check,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE)
		);
		
		layout.setVerticalGroup(layout.createParallelGroup()
			.addComponent(label, 30, 30, 30)
			.addComponent(check, 30, 30, 30)
		);
	}
	
	public boolean isSelected() {
		return check.isSelected();
	}
	
	public void mousePressed(MouseEvent me) {
		check.setSelected(!check.isSelected());
	}
	
	public void mouseReleased(MouseEvent me) {}
	public void mouseClicked(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
}