/*
*	Author: Rhys B.
*	Created: 2021-12-23
*	Modified: 2022-01-05
*
*	The terminal portion of the editor.
*/


import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.File;
import java.io.PrintWriter;

import java.net.Socket;

import java.util.Scanner;

import java.util.stream.Stream;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


@SuppressWarnings("serial")
public class Terminal extends JPanel implements ActionListener, KeyListener, Runnable {
	private boolean kill = false;

	private JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private JTextArea text = new JTextArea();
	private JTextField input = new JTextField();

	private Socket socket = null;
	private PrintWriter printer = null;

	private boolean firstLine = true;

	public Terminal() {
		setLayout(new BorderLayout());

		scroll.setViewportView(text);
		add(scroll);

		input.addActionListener(this);
		input.setEnabled(false);
		add(input, BorderLayout.SOUTH);

		text.addKeyListener(this);
		input.addKeyListener(this);

		text.setLineWrap(true);
		text.setEditable(false);
		updateOptions();
	}
	
	public void updateOptions() {
		text.setFont(Defaults.TERMINAL_FONT);
		text.setBackground(Defaults.TERMINAL_BACKGROUND_COLOR);
		text.setForeground(Defaults.TERMINAL_FONT_COLOR);

		input.setFont(Defaults.TERMINAL_FONT);
		input.setBackground(Defaults.TERMINAL_BACKGROUND_COLOR);
		input.setForeground(Defaults.TERMINAL_FONT_COLOR);
	}

	public void append(String s) {
		if (s.equals("Could not run the sketch (Target VM failed to initialize).")) {
			s = "An unknown error occured running the sketch.";
		} else if (s.startsWith("For more information, read Help ")) {
			return;
		} else if (s.equals("The output folder already exists. Use --force to remove it.")) {
			s = "The output folder already exists. Remove it first.";
		} else if (s.equals("Could not run the sketch.")) {
			return;
		} else if (s.equals("stdin.pde:11:1:11:1: Syntax Error - Incomplete " +
			"statement or extra code near 'extraneous input 'Scanner' expecting " +
			"{'abstract', 'class', 'final', 'interface', 'private', 'protected', " +
			"'public', 'static', 'strictfp', '@'}'?"))
		{
			s = "Brocessing requires all commands to be in a function.\n" +
				"For script-like functionality, use setup().";
		}

		if (firstLine) {
			text.setText(s);
			firstLine = false;
		} else {
			text.setText(text.getText() + "\n" + s);
		}

		SwingUtilities.invokeLater(this);
	}

	public void run() {
		JScrollBar bar = scroll.getVerticalScrollBar();
		bar.setValue(bar.getMaximum() - bar.getVisibleAmount());
	}

	public void clear() {
		firstLine = true;
		text.setText("");
	}

	public void actionPerformed(ActionEvent ae) {
		try {
			String s = input.getText();
			if (!s.equals("")) {
				printer.println(s);
				append(s);
				input.setText("");
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error writing to stdin.");
		}
	}

	public void connect(Process p) {
		try {
			File ports = new File(System.getProperty("user.home") + "/active_ports.txt");

			while (!ports.exists() && !kill) {
				Thread.sleep(100);
			}

			if (kill) {
				kill = false;
				return;
			}

			Scanner scan = new Scanner(ports);
			int port = Integer.parseInt(scan.nextLine());
			scan.close();
			ports.delete();

			socket = new Socket("127.0.0.1", port);
			printer = new PrintWriter(socket.getOutputStream(), true);

			input.setEnabled(true);
		} catch (Exception e) {
			ErrorMgr.show(e, "Error connecting input to program.");
		}
	}

	public void disconnect() {
		if (printer != null) {
			printer.close();

			try {
				socket.close();
			} catch (Exception e) {
				ErrorMgr.show(e, "Error closing connection to stdin.");
			}

			printer = null;
			socket = null;
		}

		input.setEnabled(false);
		kill = false;
	}

	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_C && ke.isControlDown() && ke.isShiftDown()) {
			Compiler.kill();
		}
	}

	public void kill() {
		kill = true;
	}

	public void keyTyped(KeyEvent ke) {}
	public void keyReleased(KeyEvent ke) {}
}
