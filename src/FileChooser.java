/*
*	Author: Rhys B.
*	Created: 2021-12-29
*	Modified: 2021-12-29
*
*	GUI component that allows the user to choose a file.
*/


import java.awt.Image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.io.File;

import javax.imageio.ImageIO;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


@SuppressWarnings("serial")
public class FileChooser extends JPanel implements ActionListener, FocusListener {
	private File file;
	private boolean dirOnly;

	private JTextField text;
	private JButton button;
	private JLabel label;

	public FileChooser() {
		// Default constructor
		this("Choose a file", new File(System.getProperty("user.home")), true);
	}

	public FileChooser(String title, File init, boolean dirOnly) {
		this.dirOnly = dirOnly;

		text = new JTextField();
		button = new JButton();
		label = new JLabel(title);

		label.setHorizontalAlignment(SwingConstants.RIGHT);

		button.addActionListener(this);

		text.addFocusListener(this);

		try {
			button.setIcon(new ImageIcon(ImageIO.read(
				FileChooser.class.getResourceAsStream("data/file.png"))
				.getScaledInstance(22, 22, Image.SCALE_SMOOTH)));
		} catch (Exception e) {
			// Do nothing.
		}

		setFile(init);

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(label, 100, 100, 100)
			.addComponent(text)
			.addComponent(button, 30, 30, 30)
		);

		layout.setVerticalGroup(layout.createParallelGroup()
			.addComponent(label, 30, 30, 30)
			.addComponent(text, 30, 30, 30)
			.addComponent(button, 30, 30, 30)
		);
	}

	public void setFile(File f) {
		file = f;
		text.setText(f.getAbsolutePath());
	}

	public File getFile() {
		return file;
	}

	public void actionPerformed(ActionEvent ae) {
		JFileChooser chooser = new JFileChooser(file);

		if (dirOnly) {
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}

		int i = chooser.showDialog(this, "Select");

		if (i == JFileChooser.APPROVE_OPTION) {
			setFile(chooser.getSelectedFile());
		}
	}

	public void focusLost(FocusEvent fe) {
		file = new File(text.getText());
	}

	public void focusGained(FocusEvent fe) {}
}
