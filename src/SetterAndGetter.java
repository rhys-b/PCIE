/*
*	Author: Rhys B.
*	Created: 2021-12-29
*	Modified: 2022-01-05
*
*	Contains methods for creating setters and getters.
*/


import java.util.ArrayList;

import javax.swing.JTextPane;

import javax.swing.text.StyledDocument;


public class SetterAndGetter {
	public static void generate(DocEditor docEditor) {
		String s = getSafeText(docEditor.getEditor());
		ArrayList<Range> classes = splitIntoClasses(s);
		int start = 0;
		String out = "";

		JTextPane pane = docEditor.getEditor().getTextPane();
		int caret = pane.getCaretPosition();

		Range r;
		for (int i = 0; i < classes.size(); i++) {
			r = classes.get(i);

			out += s.substring(start, r.getOffset());

			ArrayList<Variable> vars = parse(s, r);
			out += insertSettersAndGetters(s, r, vars);

			start = r.getEnd() + 1;
		}

		out += s.substring(start);

		pane.setText(out);
		pane.setCaretPosition(caret);

		docEditor.getEditor().getEditManager().replace(s, out, 0);

		docEditor.getEditor().getHighlightManager().recalculate();
		docEditor.getEditor().getHighlightManager().repaint();

		PCIE.getWindow().getTabbedPane().setActiveSaved(false);

		docEditor.recalculateLines();
	}

	public static String insertSettersAndGetters(String s, Range r, ArrayList<Variable> vars) {
		String out = s.substring(r.getOffset(), r.getEnd() - 1) + "\n";

		for (int i = 0; i < vars.size(); i++) {
			Variable v = vars.get(i);

			if (!alreadyContains(true, v, s, r)) {
				out += v.getSetter();
			}

			if (!alreadyContains(false, v, s, r)) {
				out += v.getGetter();
			}
		}

		return out + "}";
	}

	public static boolean alreadyContains(boolean setter, Variable v, String s, Range r) {
		String test;

		if (setter) {
			test = v.getSetterName();
		} else {
			test = v.getGetterName();
		}

		return s.substring(r.getOffset(), r.getEnd() + 1).contains(test);
	}

	public static ArrayList<Range> splitIntoClasses(String s) {
		int len = s.length();
		char[] hist = createNullCharArray(5);
		int depth = 0;
		boolean inClass = false;
		ArrayList<Range> out = new ArrayList<Range>();
		int start = 0;

		for (int i = 0; i < len; i++) {
			push(hist, s.charAt(i));

			if (lastEqu(hist, "class")) {
				inClass = true;
			} else if (lastEqu(hist, "{")) {
				if (depth == 0 && inClass) {
					start = i;
				}

				depth++;
			} else if (lastEqu(hist, "}")) {
				depth--;

				if (depth == 0 && inClass) {
					inClass = false;
					out.add(new Range(start, i - start + 1));
				}
			} else if (lastEqu(hist, "\"")) {
				i = skipString(i, s, "\"", len, hist);
			} else if (lastEqu(hist, "'")) {
				i = skipString(i, s, "'", len, hist);
			} else if (lastEqu(hist, "/*")) {
				i = skip(i, s, "*/", len, hist);
			} else if (lastEqu(hist, "//")) {
				i = skip(i, s, "\n", len, hist);
			}
		}

		return out;
	}

	public static ArrayList<Variable> parse(String s, Range r) {
		int len = r.getEnd();
		char[] hist = createNullCharArray(8);
		boolean priv = false, stat = false;
		int depth = 0;
		ArrayList<Variable> out = new ArrayList<Variable>();
		int newline = 0;
		boolean primed = false;

		for (int i = r.getOffset(); i < len; i++) {
			push(hist, s.charAt(i));

			if (lastEqu(hist, "\"")) {
				i = skipString(i, s, "\"", len, hist);
			} else if (lastEqu(hist, "'")) {
				i = skipString(i, s, "'", len, hist);
			} else if (lastEqu(hist, "/*")) {
				i = skip(i, s, "*/", len, hist);
			} else if (lastEqu(hist, "//")) {
				i = skip(i, s, "\n", len, hist);
			} else if (lastEqu(hist, "}")) {
				depth--;
			} else if (lastEqu(hist, "{")) {
				depth++;

				priv = false;
				stat = false;

				primed = true;
			} else if (lastEqu(hist, ";")) {
				if (priv && !stat) {
					append(out, parseLine(s.substring(newline, i)));

					priv = false;
				}

				stat = false;
				primed = true;
			} else if (lastEqu(hist, "static\0")) {
				stat = true;
			} else if (lastEqu(hist, "private\0")) {
				priv = true;
			} else if (lastEqu(hist, "\n")) {
				if (primed) {
					newline = i;
					primed = false;
				}
			}
		}

		return out;
	}

	public static ArrayList<Variable> parseLine(String s) {
		int start = s.indexOf("private") + "private".length();
		ArrayList<Variable> out = new ArrayList<Variable>();

		while (!isAlphanumeric(s.charAt(start))) {
			start++;
		}

		String type = "";
		while (isAlphanumeric(s.charAt(start))) {
			type += s.charAt(start);
			start++;
		}

		while (start < s.length()) {
			while (start < s.length() && !isAlphanumeric(s.charAt(start))) {
				start++;
			}

			String name = "";
			while (start < s.length() && isAlphanumeric(s.charAt(start))) {
				name += s.charAt(start);
				start++;
			}

			while (start < s.length() && s.charAt(start) != ',') {
				start++;
			}

			out.add(new Variable(type, name));
		}

		return out;
	}

	public static boolean isAlphanumeric(char c) {
		return	(c >= 'a' && c <= 'z') ||
			(c >= 'A' && c <= 'Z') ||
			(c >= '0' && c <= '9') ||
			c == '[' || c == ']' ||
			c == '<' || c == '>' ||
			c == '_';
	}

	public static int skip(int i, String s, String escape, int len, char[] hist) {
		i++;
		while (i < len) {
			push(hist, s.charAt(i));

			if (lastEqu(hist, escape)) {
				break;
			}

			i++;
		}

		return i;
	}

	public static int skipString(int i, String s, String escape, int len, char[] hist) {
		boolean negateNext = false;

		i++;
		while (i < len) {
			push(hist, s.charAt(i));

			if (lastEqu(hist, "\n")) {
				break;
			}

			if (!negateNext) {
				if (lastEqu(hist, "\\")) {
					negateNext = true;
				} else if (lastEqu(hist, escape)) {
					break;
				}
			} else {
				negateNext = false;
			}

			i++;
		}

		return i;
	}

	public static String getSafeText(EditorPane editor) {
		try {
			StyledDocument doc = editor.getStyledDocument();
			return doc.getText(0, doc.getLength());
		} catch (Exception e) {
			ErrorMgr.show(e, "Error getting text to insert setters and getters.");
			return "";
		}
	}

	public static char[] createNullCharArray(int size) {
		char[] out = new char[size];

		for (int i = 0; i < out.length; i++) {
			out[i] = '\0';
		}

		return out;
	}

	public static void push(char[] arr, char c) {
		for (int i = arr.length - 1; i > 0; i--) {
			arr[i] = arr[i - 1];
		}

		arr[0] = c;
	}

	public static boolean lastEqu(char[] arr, String s) {
		if (s.length() > arr.length) {
			return false;
		}

		int j = 0;
		char c;
		for (int i = s.length() - 1; i >= 0; i--) {
			c = s.charAt(i);

			if (c == '\0') {
				if (isAlphanumeric(arr[j])) {
					return false;
				}
			} else if (arr[j] != c) {
				return false;
			}

			j++;
		}

		return true;
	}

	public static String firstUpper(String s) {
		if (s.length() == 0) {
			return "";
		}

		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	public static void append(ArrayList<Variable> base, ArrayList<Variable> app) {
		for (int i = 0; i < app.size(); i++) {
			base.add(app.get(i));
		}
	}
}
