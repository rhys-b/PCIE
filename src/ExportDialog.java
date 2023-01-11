/*
*	Author: Rhys B.
*	Created: 2021-12-29
*	Modified: 2021-12-29
*
*	Dialog box for options pertaining to exporting a project.
*/


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class ExportDialog extends JDialog implements ActionListener {

	public class ExportData {
		private File source, sink;
		private boolean java;
		private String platform;
		private int state;

		public ExportData(	File source,
					File sink,
					boolean java,
					String platform,
					int state)
		{
			this.source = source;
			this.sink = sink;
			this.java = java;
			this.platform = platform;
			this.state = state;
		}

		public File getSource() {
			return source;
		}

		public File getSink() {
			return sink;
		}

		public boolean embedJava() {
			return java;
		}

		public String getPlatform() {
			return platform;
		}

		public int getState() {
			return state;
		}
	}


	public static final int CLOSE = 0, EXPORT = 1;

	private FileChooser source, sink;
	private JCheckBox java;
	private ComboChooser platform;
	private JButton close, export;
	private int state;

	public ExportDialog() {
		// Default Constructor.
		this(new File(System.getProperty("user.home")));
	}

	public ExportDialog(File init) {
		super(PCIE.getWindow(), "Export", true);

		source = new FileChooser("Source", init, true);
		sink = new FileChooser("Output",
			new File(System.getProperty("user.home") +
			"/" + init.getName()), true);
		java = new JCheckBox("Embed Java (Recommended)", true);
		platform = new ComboChooser("Platform", "linux", "windows", "macosx");
		close = new JButton("Close");
		export = new JButton("Export");

		JPanel javaContainer = new JPanel();
		javaContainer.add(java);
		javaContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

		if (Defaults.PLATFORM == Defaults.Platform.LINUX) {
			platform.setSelectedIndex(0);
		} else if (Defaults.PLATFORM == Defaults.Platform.WINDOWS) {
			platform.setSelectedIndex(1);
		} else {
			platform.setSelectedIndex(2);
		}

		close.addActionListener(this);
		export.addActionListener(this);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(close);
		buttonPanel.add(export);

		JPanel cont = new JPanel();
		cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
		cont.add(source);
		cont.add(sink);
		cont.add(javaContainer);
		cont.add(platform);

		setSize(400, 250);
		setLocationRelativeTo(PCIE.getWindow());
		setLayout(new BorderLayout());
		add(cont);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	public ExportData display() {
		setVisible(true);

		return new ExportData(	source.getFile(),
					sink.getFile(),
					java.isSelected(),
					platform.getSelection(),
					state);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == export) {
			state = EXPORT;
		} else {
			state = CLOSE;
		}

		dispose();
	}
}
