/*
*	Author: Rhys B.
*	Created: 2021-12-22
*	Modified: 2021-12-22
*
*	Handles number input from the user for the options page.
*/


import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class NumberChooser extends JPanel {
	private JLabel label;
	private JSpinner spinner;
	
	public NumberChooser() {
		//Default constructor
		this("Enter a number", new SpinnerNumberModel(0, 0, 100, 1));
	}
	
	public NumberChooser(String title, SpinnerNumberModel model) {
		label = new JLabel(title);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		
		spinner = new JSpinner(model);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(	label,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE)
			.addComponent(	spinner,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE)
		);
		
		layout.setVerticalGroup(layout.createParallelGroup()
			.addComponent(label, 30, 30, 30)
			.addComponent(spinner, 30, 30, 30)
		);
	}
	
	public double getDouble() {
		return (double)spinner.getValue();
	}
	
	public int getInt() {
		return (int)spinner.getValue();
	}
}