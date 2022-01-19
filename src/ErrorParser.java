/*
*	Author: Rhys B.
*	Created: 2022-01-01
*	Modified: 2022-01-03
*
*	Handles the automatic selecting of erronous lines.
*/


public class ErrorParser {
	public static void handle(String err) {
		if (!Defaults.FIND_ERROR) {
			return;
		}

		int colon = err.indexOf(":");
		if (colon == -1) {
			return;
		}
		String filename = err.substring(0, colon);

		int nextColon = err.indexOf(":", colon + 1);
		if (nextColon == -1) {
			return;
		}

		String linestr = err.substring(colon + 1, nextColon);
		int linenum;
		try {
			linenum = Integer.parseInt(linestr);
		} catch (Exception e) {
			linenum = -1;
		}

		String title;
		if (Brocessing.getWindow().getTabbedPane().getActiveComponent().hasFile()) {
			title = filename;
		} else {
			try {
				title = filename.substring(filename.indexOf("Untitled"),
										filename.length() - 4);
			} catch (Exception e) {
				// The exception was not explicity caused by a file.
				return;
			}
		}

		if (Defaults.FIND_ERROR && linenum != -1) {
			TabbedPane tabs = Brocessing.getWindow().getTabbedPane();
			int index = tabs.getIndexOfTitle(title);

			if (index == -1) {
				return;
			}

			DocEditor docEditor = tabs.getComponentAt(index);

			tabs.setActiveIndex(index);
			if (tabs.getComponentAt(index).getEditor().getActiveLine().getCurrentLine() + 1
											!= linenum)
			{
				docEditor.getFindManager().getLineFinder().setToIndexForLine(linenum);
			}
		}
	}
}
