import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import java.awt.Font;
import java.awt.BorderLayout;

public class ErrorMgr {
	public static void show(Exception e, String message) {
		// Create a string containing the stack trace.
		StackTraceElement[] trace = e.getStackTrace();
		String string = e.getMessage();
		for (int i = 0; i < trace.length; i++) {
			string += "\n	" + trace[i].toString();
		}

		// Create a text area containing the string.
		JTextArea text = new JTextArea(string);
		text.setEditable(false);

		JScrollPane scroller = new JScrollPane(
				text,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
		);

		JLabel messageLabel = new JLabel(message);
		messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD));

		// Create the dialog.
		JDialog dialog = new JDialog(Brocessing.getWindow(), "Fatal Error - Terminating", true);
		dialog.setLayout(new BorderLayout());
		if (message != null) {
			dialog.add(messageLabel, BorderLayout.NORTH);
		}
		dialog.add(scroller);
		dialog.setSize(1000, 700);
		dialog.setLocationRelativeTo(Brocessing.getWindow());
		dialog.setVisible(true);

		Brocessing.getWindow().dispose();
	}
}
