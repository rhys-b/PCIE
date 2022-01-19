/*
*	Author: Rhys B.
*	Created: 2021-12-29
*	Modified: 2022-01-01
*
*	GUI component for the user to choose between some options using
*	a combo box.
*/


import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class ComboChooser extends JPanel {
	private JComboBox<String> combo;

	public ComboChooser() {
		// Default constructor.
		this("Choose", "a", "b", "c");
	}

	public ComboChooser(String title, String ...s) {
		combo = new JComboBox<String>(s);
		JLabel label = new JLabel(title);
		
		label.setHorizontalAlignment(SwingConstants.RIGHT);

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(label, 100, 100, 100)
			.addComponent(combo)
		);

		layout.setVerticalGroup(layout.createParallelGroup()
			.addComponent(label, 30, 30, 30)
			.addComponent(combo, 30, 30, 30)
		);
	}

	public String getSelection() {
		return (String)combo.getSelectedItem();
	}

	public void setSelectedIndex(int i) {
		combo.setSelectedIndex(i);
	}
}
