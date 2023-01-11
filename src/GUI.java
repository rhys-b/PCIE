/*
*	Author: Rhys B.
*	Created: 2021-08-20
*	Modified: 2021-12-30
*
*	The main window for the PCIE editor.
*/

import javax.imageio.ImageIO;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.awt.Desktop;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

import java.io.File;

import java.net.URI;

import java.util.ArrayList;


@SuppressWarnings("serial")
public class GUI extends JFrame implements WindowListener {
	// Tabbed to indicate parent-child relationships.
	private JMenuBar menuBar = new JMenuBar();
		private JMenu fileMenu = new JMenu("File");
			private JMenuItem open = new JMenuItem("Open");
			private JMenuItem save = new JMenuItem("Save");
			private JMenuItem saveAs = new JMenuItem("Save As");
			private JMenuItem newTab = new JMenuItem("New Tab");
			private JMenuItem closeTab = new JMenuItem("Close Tab");
			private JMenuItem reopenTab = new JMenuItem("Reopen Tab");
		private JMenu edit = new JMenu("Edit");
			private JMenuItem cut = new JMenuItem("Cut");
			private JMenuItem copy = new JMenuItem("Copy");
			private JMenuItem paste = new JMenuItem("Paste");
			private JMenuItem undo = new JMenuItem("Undo");
			private JMenuItem redo = new JMenuItem("Redo");
			private JMenuItem find = new JMenuItem("Find");
			private JMenuItem findLine = new JMenuItem("Find Line");
			private JMenuItem replace = new JMenuItem("Find & Replace");
		private JMenu misc = new JMenu("Misc");
			private JMenuItem formatIndentation = new JMenuItem("Format Indentation");
			private JMenuItem setAndGet = new JMenuItem("Generate Setters and Getters");
			private JMenuItem nextTab = new JMenuItem("Next Tab");
			private JMenuItem lastTab = new JMenuItem("Last Tab");
			private JMenuItem options = new JMenuItem("Options");
		private JMenu app = new JMenu("App");
			private JMenuItem compile = new JMenuItem("Compile & Run");
			private JMenuItem kill = new JMenuItem("Kill");
			private JMenuItem exportDir = new JMenuItem("Export to Directory");
			private JMenuItem exportJar = new JMenuItem("Export to .jar");
			private JMenuItem exportExe = new JMenuItem("Export to .exe");
		private JMenu help = new JMenu("Help");
			private JMenuItem reference = new JMenuItem("Reference");
	private JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		private TabbedPane tabs = new TabbedPane();
		private Terminal terminal = new Terminal();
		
	public static final KeyAdapter tabSwitcher = new KeyAdapter() {
		public void keyPressed(KeyEvent ke) {
			if (ke.getKeyCode() == KeyEvent.VK_TAB && ke.isControlDown()) {
				if (ke.isShiftDown()) {
					PCIE.getWindow().getTabbedPane().last();
				} else {
					PCIE.getWindow().getTabbedPane().next();
				}
			}
		}
	};

	public GUI(File[] files) {
		try {
			setIconImage(ImageIO.read(GUI.class.getResourceAsStream("data/icon.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}

		open.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
		save.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK));
		saveAs.setAccelerator(KeyStroke.getKeyStroke('S',
			InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		newTab.setAccelerator(KeyStroke.getKeyStroke('T', InputEvent.CTRL_DOWN_MASK));
		closeTab.setAccelerator(KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK));
		reopenTab.setAccelerator(KeyStroke.getKeyStroke('T',
			InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

		cut.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
		copy.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
		paste.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
		undo.setAccelerator(KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK));
		redo.setAccelerator(KeyStroke.getKeyStroke('Z',
			InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		find.setAccelerator(KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK));
		findLine.setAccelerator(KeyStroke.getKeyStroke('I', InputEvent.CTRL_DOWN_MASK));
		replace.setAccelerator(KeyStroke.getKeyStroke('F',
			InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
			
		formatIndentation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
									InputEvent.CTRL_DOWN_MASK));
		nextTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
									InputEvent.CTRL_DOWN_MASK));
		lastTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
			InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

		compile.setAccelerator(KeyStroke.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK));
		kill.setAccelerator(KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK));

		fileMenu.add(open);
		fileMenu.add(save);
		fileMenu.add(saveAs);
		fileMenu.addSeparator();
		fileMenu.add(newTab);
		fileMenu.add(closeTab);
		fileMenu.add(reopenTab);

		edit.add(cut);
		edit.add(copy);
		edit.add(paste);
		edit.addSeparator();
		edit.add(undo);
		edit.add(redo);
		edit.addSeparator();
		edit.add(find);
		edit.add(findLine);
		edit.add(replace);
		
		misc.add(formatIndentation);
		misc.add(setAndGet);
		misc.addSeparator();
		misc.add(nextTab);
		misc.add(lastTab);
		misc.addSeparator();
		misc.add(options);

		app.add(compile);
		app.add(kill);
		app.addSeparator();
		app.add(exportDir);
		//app.add(exportJar);
		//app.add(exportExe);

		help.add(reference);

		menuBar.add(fileMenu);
		menuBar.add(edit);
		menuBar.add(misc);
		menuBar.add(app);
		menuBar.add(help);

		split.setTopComponent(tabs);
		split.setBottomComponent(terminal);
		split.setResizeWeight(0.85);

		setJMenuBar(menuBar);
		add(split);

		if (files == null) {
			tabs.open(null, false);
		} else {
			for (int i = 0; i < files.length; i++) {
				tabs.open(files[i], false);
			}
		}

		newTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.open(null, true);
			}
		});

		closeTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				closeActiveTab();
			}
		});

		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JFileChooser chooser = new JFileChooser(Defaults.CURRENT_DIRECTORY);
				chooser.setFileFilter(Defaults.FILE_FILTER);
				chooser.setMultiSelectionEnabled(true);
				if (chooser.showOpenDialog(PCIE.getWindow()) ==
									JFileChooser.APPROVE_OPTION) {
					File[] files = chooser.getSelectedFiles();
					for (int i = 0; i < files.length; i++) {
						tabs.open(files[i], false);
					}
					Defaults.CURRENT_DIRECTORY = files[files.length - 1]
										.getParentFile();
				}
			}
		});

		reopenTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.reopen();
			}
		});

		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				save();
			}
		});

		saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				saveAs();
			}
		});

		cut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.cut();
			}
		});

		copy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.copy();
			}
		});

		paste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.paste();
			}
		});

		undo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.undo();
			}
		});

		redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.redo();
			}
		});
		
		find.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.find();
			}
		});
		
		findLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.findLine();
			}
		});

		replace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.replace();
			}
		});
		
		nextTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.next();
			}
		});
		
		lastTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.last();
			}
		});
		
		formatIndentation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tabs.formatIndentation();
			}
		});
		
		options.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Settings.show();
			}
		});

		compile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Compiler.kill();
				terminal.clear();

				File file = tabs.getActiveComponent().getFile();

				if (file == null) {
					File dir = createTmp(tabs.getActiveTitle(),
						tabs.getActiveComponent().getEditor());

					Compiler.compile(dir, true);
				} else if (isProcessingProject(file.getParentFile())) {
					tabs.saveKnown();
					Compiler.compile(file.getParentFile(), false);
				} else {
					projectError();
				}
			}
		});

		kill.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Compiler.kill();
			}
		});

		exportDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				terminal.clear();

				if (!tabs.getActiveComponent().hasFile()) {
					File dir = createTmp(tabs.getActiveTitle(),
						tabs.getActiveComponent().getEditor());

					Compiler.exportDir(dir, true);
				} else if (isProcessingProject(Defaults.CURRENT_DIRECTORY)) {
					tabs.saveKnown();
					Compiler.exportDir(Defaults.CURRENT_DIRECTORY, false);
				} else {
					projectError();
				}
			}
		});

		exportJar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				terminal.clear();

				if (!tabs.getActiveComponent().hasFile()) {
					File dir = createTmp(tabs.getActiveTitle(),
						tabs.getActiveComponent().getEditor());

					Compiler.exportJar(dir, true);
				} else if (isProcessingProject(Defaults.CURRENT_DIRECTORY)) {
					tabs.saveKnown();
					Compiler.exportJar(Defaults.CURRENT_DIRECTORY, false);
				} else {
					projectError();
				}
			}
		});

		exportExe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				terminal.clear();

				if (!tabs.getActiveComponent().hasFile()) {
					File dir = createTmp(tabs.getActiveTitle(),
						tabs.getActiveComponent().getEditor());

					Compiler.exportJar(dir, true);
				} else if (isProcessingProject(Defaults.CURRENT_DIRECTORY)) {
					tabs.saveKnown();
					Compiler.exportExe(Defaults.CURRENT_DIRECTORY, false);
				} else {
					projectError();
				}
			}
		});

		setAndGet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				SetterAndGetter.generate(tabs.getActiveComponent());
			}
		});

		reference.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					Desktop.getDesktop().browse(
							new URI("https://processing.org/reference/"));
				} catch (Exception e) {
					ErrorMgr.show(e, "Invalid URL.", false);
				}
			}
		});
		
		terminal.addKeyListener(tabSwitcher);
		terminal.setFocusTraversalKeysEnabled(false);

		setTitle("PCIE");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(700, 500);
		setLocationRelativeTo(null);
		addWindowListener(this);
		setVisible(true);
	}

	private boolean isProcessingProject(File dir) {
		File[] files = dir.listFiles();

		String name = dir.getName() + ".pde";

		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals(name)) {
				return true;
			}
		}

		return false;
	}

	private void projectError() {
		JOptionPane.showMessageDialog(this,
			"The current folder is not a Processing project.");
	}

	private File createTmp(String s, EditorPane editor) {
		File dir = new File(Defaults.working + "/brocessing_tmp_" + s);
		dir.mkdir();

		File f = new File(dir.getAbsolutePath() + "/brocessing_tmp_" + s + ".pde");

		editor.save(f);
		return dir;
	}
	
	// Returns true if the file was actually saved or false if it was not.
	public boolean save() {
		return save(tabs.getPosition());
	}
	
	public boolean save(int index) {
		if (tabs.getComponentAt(index).hasFile()) {
			tabs.save(index);
			return true;
		} else {
			return saveAs(index);
		}
	}

	// Returns true if the file was actually saved or false if not.
	public boolean saveAs() {
		return saveAs(tabs.getPosition());
	}
	
	public boolean saveAs(int index) {
		JFileChooser chooser = new JFileChooser(Defaults.CURRENT_DIRECTORY);
		chooser.setFileFilter(Defaults.FILE_FILTER);
		File file = null;
		
		while (chooser.showSaveDialog(tabs.getActiveComponent())==JFileChooser.APPROVE_OPTION){
			file = chooser.getSelectedFile();

			if (!isValidName(file)) {
				showInvalidName();
				file = null;
				continue;
			}

			if (!file.getName().contains(".")) {
				file = new File(file.getPath() + ".pde");
			}
			
			if (file.exists()) {
				String[] options = {"Overwrite File", "Choose New Name"};
				int choice = JOptionPane.showOptionDialog(
					this,
					file.getName() + " already exists.",
					"File Already Exists",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					options,
					options[0]
				);
			
				if (choice == 0) {
					break;
				} else {
					file = null;
				}
			} else {
				break;
			}
		}
		
		if (file == null) {
			return false;
		} else {
			file = checkMakeDir(file);
			tabs.saveAs(file);
			Defaults.CURRENT_DIRECTORY = file.getParentFile();
			return true;
		}
	}

	private boolean isValidName(File f) {
		String name = f.getName();

		for (int i = 0; i < name.length(); i++) {
			if (!isValidChar(name.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	private boolean isValidChar(char c) {
		return	(c >= 'a' && c <= 'z') ||
			(c >= 'A' && c <= 'Z') ||
			(c >= '0' && c <= '9') ||
			c == '_' ||
			c == '.';
	}

	private void showInvalidName() {
		JOptionPane.showMessageDialog(this,
			"The name may only contain alphanumeric characters, " +
			"periods, and underscores.");
	}

	private File checkMakeDir(File f) {
		File par = f.getParentFile();
		String name = f.getName();

		if (!isProcessingProject(par)) {
			File dir = new File(par.getAbsolutePath() + "/" + name.substring(0,
									name.lastIndexOf(".")));
			dir.mkdir();

			f = new File(dir.getAbsolutePath() + "/" + name);
		}

		return f;
	}
	
	// Closes the active tab, asking the user what to do if the file is unsaved(save, cancel, close).
	// If the user clicks cancel, false is returned to indicate that furthur closing procedures
	// should be halted(a.k.a. disposing of the window).
	public boolean closeActiveTab() {
		return closeTab(tabs.getPosition(), false);
	}
	
	public boolean closeTab(int index, boolean onlyTest) {
		if (tabs.getSaved(index)) {
			tabs.removeFile(index);
		} else if (!tabs.getSaved(index)) {
			String[] options = {"Save", "Cancel", "Close Without Saving"};
			int choice = JOptionPane.showOptionDialog(
				this,							// Parent
				"File \"" + tabs.getTitle(index) + "\" is not saved.",	// Message
				"File Not Saved",					// Title
				JOptionPane.DEFAULT_OPTION,				// Option type
				JOptionPane.WARNING_MESSAGE,				// Style
				null,							// Icon
				options,						// Options
				options[0]						// Default
			);
			
			if (choice == 1) {
				return false;
			} else if (choice == 2) {
				if (!onlyTest) {
					tabs.removeFile(index);
				}
			} else {
				if (save(index)) {
					if (!onlyTest) {
						tabs.removeFile(index);
					}
				} else {
					return false;
				}
			}
		}
		
		return true;
	}

	public TabbedPane getTabbedPane() {
		return tabs;
	}
	
	// Called when the user closes the window.
	public void windowClosing(WindowEvent we) {
		// Go through the open tabs and create a list of indexes of unsaved ones.
		ArrayList<Integer> unsavedTabs = new ArrayList<Integer>();
		
		for (int i = 0; i < tabs.getTabCount(); i++) {
			if (!tabs.getSaved(i)) {
				unsavedTabs.add(i);
			}
		}
		
		// Check with the user to see what they would like to to about each unsaved tab.
		// If the user cancels, then the user should not be asked for any other files.
		boolean canceled = false;
		for (int i = 0; i < unsavedTabs.size(); i++) {
			if (!closeTab(unsavedTabs.get(i), true)) {
				canceled = true;
				break;
			}
		}
		
		// Close all the tabs for real, assuming the user didn't cancel the close,
		// and then close the window.
		if (!canceled) {
			while (tabs.getTabCount() > 0) {
				tabs.removeFile(0);
			}
			
			dispose();
			
			Defaults.writeDefaults();
		}
	}
	
	public void updateOptions() {
		tabs.updateOptions();
		terminal.updateOptions();
	}

	public Terminal getTerminal() {
		return terminal;
	}
	
	// Residue of implementing WindowListener.
	public void windowDeactivated(WindowEvent we) {}
	public void windowActivated(WindowEvent we) {}
	public void windowDeiconified(WindowEvent we) {}
	public void windowIconified(WindowEvent we) {}
	public void windowClosed(WindowEvent we) {}
	public void windowOpened(WindowEvent we) {}
}
