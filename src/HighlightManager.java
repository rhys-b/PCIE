/*
*	Author: Rhys B
*	Created: 2021-12-13
*	Modified: 2021-12-29
*
*	A third attempt at making a highlight manager that is both
*	robust and fast.
*	This balance is proving to be somewhat difficult to get right. :(
*/


import java.util.ArrayList;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


@SuppressWarnings({"unchecked","rawtypes"})
public class HighlightManager {
	private StyledDocument doc;

	private ArrayList[] lists = {
		new ArrayList<Range>(),
		new ArrayList<Range>(),
		new ArrayList<Range>(),
		new ArrayList<Range>()
	};
	private ArrayList[] oldLists = {
		new ArrayList<Range>(),
		new ArrayList<Range>(),
		new ArrayList<Range>(),
		new ArrayList<Range>()
	};

	private SimpleAttributeSet[] styles = new SimpleAttributeSet[4];

	private static SimpleAttributeSet numAttr;
	private static SimpleAttributeSet normAttr;
	private static SimpleAttributeSet escapeAttr;
	private static boolean stylesSet = false;

	private static final int 	STRING = 0,
					SL_COMMENT = 1,
					ML_COMMENT = 2,
					CHAR = 3;

	private Attribute[] attrs = new Attribute[11];
	private static final int	ACCESS = 0,
					CLASS = 10,
					COMPOSITE = 2,
					DEFINABLE = 7,
					FLOW = 4,
					IMPORT = 5,
					OBJECT = 6,
					PREDEFINED = 3,
					PRIMITIVE = 8,
					TFN = 9,
					VARS = 1;

	public HighlightManager() {
		this(null);
	}

	public HighlightManager(StyledDocument doc) {
		if (!stylesSet) {
			generateStyles();
		}

		setStyledDocument(doc);
	}
	
	public void updateOptions() {
		attrs[ACCESS].updateOptions(Defaults.ACCESS, Defaults.ACCESS_B);
		attrs[CLASS].updateOptions(Defaults.CLASS, Defaults.CLASS_B);
		attrs[COMPOSITE].updateOptions(Defaults.COMPOSITE,
							Defaults.COMPOSITE_B);
		attrs[DEFINABLE].updateOptions(Defaults.DEFINABLE,
							Defaults.DEFINABLE_B);
		attrs[FLOW].updateOptions(Defaults.FLOW, Defaults.FLOW_B);
		attrs[IMPORT].updateOptions(Defaults.IMPORT, Defaults.IMPORT_B);
		attrs[OBJECT].updateOptions(Defaults.OBJECT, Defaults.OBJECT_B);
		attrs[PREDEFINED].updateOptions(Defaults.PREDEFINED,
							Defaults.PREDEFINED_B);
		attrs[PRIMITIVE].updateOptions(Defaults.PRIMITIVE,
							Defaults.PRIMITIVE_B);
		attrs[TFN].updateOptions(Defaults.TFN, Defaults.TFN_B);
		attrs[VARS].updateOptions(Defaults.VARS, Defaults.VARS_B);
		
		generateStyles();
		
		if (Defaults.HIGHLIGHT) {
			repaint(new Range(0, doc.getLength()));
		} else {
			try {
				doc.setCharacterAttributes(0, doc.getLength(),
								normAttr, true);
			} catch (Exception e) {
				ErrorMgr.show(e,
					"Error clearing document highlighting");
			}
		}
	}

	public void setStyledDocument(StyledDocument i) {
		doc = i;

		if (doc != null) {
			generateAttributes();
			recalculate();
			repaint();
		}
	}

	public StyledDocument getStyledDocument() {
		return doc;
	}

	private void generateAttributes() {
		Attribute access = new Attribute(	doc,
							Defaults.ACCESS_L,
							Defaults.ACCESS,
							Defaults.ACCESS_B,
							false,
							true);

		Attribute classes = new Attribute(	doc,
							Defaults.CLASS_L,
							Defaults.CLASS,
							Defaults.CLASS_B,
							false,
							true);

		Attribute composite = new Attribute(	doc,
							Defaults.COMPOSITE_L,
							Defaults.COMPOSITE,
							Defaults.COMPOSITE_B,
							false,
							false);

		Attribute definable = new Attribute(	doc,
							Defaults.DEFINABLE_L,
							Defaults.DEFINABLE,
							Defaults.DEFINABLE_B,
							true,
							true);

		Attribute flow = new Attribute(		doc,
							Defaults.FLOW_L,
							Defaults.FLOW,
							Defaults.FLOW_B,
							false,
							false);

		Attribute imports = new Attribute(	doc,
							Defaults.IMPORT_L,
							Defaults.IMPORT,
							Defaults.IMPORT_B,
							false,
							true);

		Attribute object = new Attribute(	doc,
							Defaults.OBJECT_L,
							Defaults.OBJECT,
							Defaults.OBJECT_B,
							false,
							true);

		Attribute predefined = new Attribute(	doc,
							Defaults.PREDEFINED_L,
							Defaults.PREDEFINED,
							Defaults.PREDEFINED_B,
							true,
							false);

		Attribute primitive = new Attribute(	doc,
							Defaults.PRIMITIVE_L,
							Defaults.PRIMITIVE,
							Defaults.PRIMITIVE_B,
							false,
							false);

		Attribute tfn = new Attribute(		doc,
							Defaults.TFN_L,
							Defaults.TFN,
							Defaults.TFN_B,
							false,
							false);

		Attribute vars = new Attribute(		doc,
							Defaults.VARS_L,
							Defaults.VARS,
							Defaults.VARS_B,
							false,
							false);

		attrs[ACCESS] = access;
		attrs[CLASS] = classes;
		attrs[COMPOSITE] = composite;
		attrs[DEFINABLE] = definable;
		attrs[FLOW] = flow;
		attrs[IMPORT] = imports;
		attrs[OBJECT] = object;
		attrs[PREDEFINED] = predefined;
		attrs[PRIMITIVE] = primitive;
		attrs[TFN] = tfn;
		attrs[VARS] = vars;
	}

	private void generateStyles() {
		styles[STRING] = new SimpleAttributeSet();
		styles[SL_COMMENT] = new SimpleAttributeSet();
		styles[CHAR] = new SimpleAttributeSet();
		numAttr = new SimpleAttributeSet();
		normAttr = new SimpleAttributeSet();
		escapeAttr = new SimpleAttributeSet();

		StyleConstants.setForeground(styles[STRING], Defaults.STRING);
		StyleConstants.setForeground(styles[SL_COMMENT],
							Defaults.COMMENT);
		StyleConstants.setForeground(styles[CHAR], Defaults.CHAR);
		StyleConstants.setForeground(numAttr, Defaults.NUM);
		StyleConstants.setForeground(
					normAttr, Defaults.EDITOR_FONT_COLOR);
		StyleConstants.setForeground(escapeAttr, Defaults.ESCAPE);

		StyleConstants.setBold(styles[STRING], Defaults.STRING_B);
		StyleConstants.setBold(styles[SL_COMMENT], Defaults.COMMENT_B);
		StyleConstants.setBold(styles[CHAR], Defaults.CHAR_B);
		StyleConstants.setBold(numAttr, Defaults.NUM_B);
		StyleConstants.setBold(escapeAttr, Defaults.ESCAPE_B);
		// Normal text cannot be bolded.

		// Intentional reference duplication.
		styles[ML_COMMENT] = styles[SL_COMMENT];
	}

	public void recalculate() {
		if (!Defaults.HIGHLIGHT) {
			return;
		}
		
		// Recalculates the indexes of the strings, comments, and chars.

		backupComments();
		String s = getSafeText();

		int i = 0, len = s.length();
		char a, b;
		while (i < len) {
			a = s.charAt(i);
			b = getSafeChar(s, i + 1);

			if (a == '"') {
				// String found.
				int start = i;
				boolean negateNext = false, terminated = false;

				i++;

				while (i < len) {
					a = s.charAt(i);

					if (a == '\n') {
						break;
					} else if (a == '\\' && !negateNext) {
						negateNext = true;
					} else if (a == '"' && !negateNext) {
						terminated = true;
						break;
					} else {
						negateNext = false;
					}

					i++;
				}

				int length = i - start + 1;
				lists[STRING].add(new Range(start, length));
			} else if (a == '\'') {
				// Character found.

				int start = i;
				boolean negateNext = false, terminated = false;

				i++;

				while (i < len) {
					a = s.charAt(i);

					if (a == '\n') {
						break;
					} else if (a == '\\' && !negateNext) {
						negateNext = true;
					} else if (a == '\'' && !negateNext) {
						terminated = true;
						break;
					} else {
						negateNext = false;
					}

					i++;
				}

				if (	terminated &&
					isChar(s.substring(start, i + 1)))
				{
					lists[CHAR].add(new Range(
							start, i - start + 1));
				}
			} else if (a == '/' && b == '/') {
				// Single line comment found.

				int start = i;

				i += 2;

				while (i < len) {
					a = s.charAt(i);

					if (a == '\n') {
						break;
					}

					i++;
				}

				int length = i - start + 1;
				
				lists[SL_COMMENT].add(new Range(start, length));
			} else if (a == '/' && b == '*') {
				// Multiline comment found.

				int start = i;
				boolean terminated = false;

				i += 2;

				while (i + 1 < len) {
					a = s.charAt(i);
					b = s.charAt(i + 1);

					if (a == '*' && b == '/') {
						terminated = true;
						break;
					}

					i++;
				}

				int length = i - start + 2;
				lists[ML_COMMENT].add(new Range(start, length));
			}

			i++;
		}
	}

	private void backupComments() {
		for (int i = 0; i < lists.length; i++) {
			oldLists[i] = lists[i];
			lists[i] = new ArrayList<Range>();
		}
	}

	private String getSafeText() {
		return getSafeText(new Range(0, doc.getLength()));
	}

	private String getSafeText(Range r) {
		try {
			return doc.getText(r.getOffset(), r.getLength());
		} catch (Exception e) {
			ErrorMgr.show(e, "Error while getting text to color.");
			return null;
		}
	}

	private char getSafeChar(String s, int i) {
		try {
			return s.charAt(i);
		} catch (IndexOutOfBoundsException e) {
			return '\0';
		}
	}

	private boolean isChar(String s) {
		// Returns true if the given string is an example of a
		// character that should be highlighted. It is assumed that
		// the string starts and ends with single quotes.

		if (s.length() == 3) {
			return true;
		} else if (s.length() == 4) {
			if (s.charAt(1) == '\\') {
				return true;
			}

			return false;
		} else if (s.length() == 8) {
			if (	s.charAt(1) == '\\' &&
				s.charAt(2) == 'u' &&
				onlyContainsHex(s.substring(3, 7)))
			{
				return true;
			}
		}

		return false;
	}

	private boolean onlyContainsHex(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!isHex(s.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	private boolean isHex(char c) {
		return	(c >= 'a' && c <= 'f') ||
			(c >= 'A' && c <= 'F') ||
			(c >= '0' && c <= '9');
	}

	public void repaint() {
		repaint(new Range(0, doc.getLength()));
	}

	public void repaint(Range range) {
		if (!Defaults.HIGHLIGHT) {
			return;
		}
		// Hands the correct styles and positions to the styled document
		// for it to repaint.
		// Only artifacts inside the range are considered.

		expand(range);

		String text = getSafeText(range);
		if (text.contains("/*") || text.contains("*/")) {
			range.setEnd(findRejoin(lists,oldLists,range.getEnd()));
		}

		try {
			doc.setCharacterAttributes(	range.getOffset(),
							range.getLength(),
							normAttr,
							true);

			for (int i = 0; i < attrs.length; i++) {
				attrs[i].highlight(range);
			}

			repaintNumbers(range);

			for (int i = 0; i < styles.length; i++) {
				repaintList(lists[i], range, styles[i]);
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error highlighting text.");
		}
	}

	private void repaintList(	ArrayList<Range> list,
					Range r,
					SimpleAttributeSet attr)
	{
		try {
			int i = 0;
			while (	i < list.size() &&
				list.get(i).getEnd() < r.getOffset())
			{
				i++;
			}

			while (	i < list.size() &&
				list.get(i).getOffset() < r.getEnd())
			{
				doc.setCharacterAttributes(
					list.get(i).getOffset(),
					list.get(i).getLength(),
					attr,
					false
				);

				if (	list.equals(lists[STRING]) ||
					list.equals(lists[CHAR]))
				{
					paintEscapes(list.get(i));
				}

				i++;
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error painting list.");
		}
	}

	private void repaintNumbers(Range r) {
		String s = getSafeText(r);
		Range[] rarr = splitByDelimiter(s);
		String[] sarr = stringsFromRanges(s, rarr);

		for (int i = 0; i < sarr.length; i++) {
			if (isDecimalNum(sarr[i])) {
				colorNum(rarr[i].getOffset() + r.getOffset(),
							rarr[i].getLength());
			} else if (isColorNum(sarr[i])) {
				colorNum(rarr[i].getOffset() + r.getOffset(),
							rarr[i].getLength());
			}
		}
	}

	private void paintEscapes(Range r) {
		String text = getSafeText(r);

		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\\') {
				if (text.length() > i + 1) {
					char esc = text.charAt(i + 1);

					if (	esc == 't' ||
						esc == 'r' ||
						esc == '\\' ||
						esc == 'n' ||
						esc == '\'' ||
						esc == '"' ||
						esc == 'b' ||
						esc == 'f' ||
						esc == '0')
					{
						docSetEscape(r.getOffset()+i,2);
					} else if (esc == 'u') {
						if (text.length() > i + 5 &&
							onlyContainsHex(
							text.substring(i + 2,
							i + 6)))
						{
							docSetEscape(
								r.getOffset()+i,
								6
							);
						}
					}
				}
			}
		}
	}

	private void docSetEscape(int i, int l) {
		try {
			doc.setCharacterAttributes(i, l, escapeAttr, false);
		} catch (Exception e) {
			ErrorMgr.show(e, "Error highlighting escapes.");
		}
	}

	private int findRejoin(ArrayList[] a, ArrayList[] b, int start) {
		for (int i = 0; i < a.length; i++) {
			int i1 = findNextInList(a[i], start);
			int i2 = findNextInList(b[i], start);

			if (i1 == -1 || i2 == -1) {
				continue;
			}

			int offset = i2 - i1;

			int j1 = findNextInList(a[i], i1 + 1);
			int j2 = findNextInList(b[i], i2 + 1);

			if (j1 == -1 || j2 == -1) {
				continue;
			}

			if (offset == (j2 - j1)) {
				return i1;
			}
		}

		return doc.getLength() - 1;
	}

	private int findNextInList(ArrayList<Range> list, int k) {
		int i = 0;
		while (i < list.size() && list.get(i).getOffset() < k) {
			i++;
		}

		if (i == list.size()) {
			return -1;
		}

		return list.get(i).getOffset();
	}

	private Range[] splitByDelimiter(String s) {
		ArrayList<Range> list = new ArrayList<Range>();
		char c;
		boolean lastWasDelimiter = true;
		int start = -1;

		for (int i = 0; i < s.length(); i++) {
			c = s.charAt(i);

			if (EditorPane.isDelimiter(c) && c != '.' && c != '#') {
				if (!lastWasDelimiter) {
					list.add(new Range(start, i - start));
					lastWasDelimiter = true;
					start = -1;
				}
			} else {
				if (lastWasDelimiter) {
					start = i;
					lastWasDelimiter = false;
				}
			}
		}

		if (start != -1) {
			list.add(new Range(start, s.length() - start));
		}

		return list.toArray(new Range[0]);
	}

	private String[] stringsFromRanges(String s, Range[] r) {
		String[] out = new String[r.length];

		for (int i = 0; i < r.length; i++) {
			out[i] = s.substring(r[i].getOffset(), r[i].getEnd()+1);
		}

		return out;
	}

	private boolean isDecimalNum(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			try {
				Long.parseLong(s);
				return true;
			} catch (NumberFormatException f) {
				try {
					Long.parseLong(s.substring(
							0, s.length() - 1));

					char c = s.charAt(s.length() - 1);
					if (c == 'l' || c == 'L') {
						return true;
					}

					return false;
				} catch (NumberFormatException g) {
					return false;
				}
			}
		}
	}

	private boolean isColorNum(String s) {
		if (s.charAt(0) == '#' && onlyContainsHex(s.substring(1))) {
			return true;
		}

		return false;
	}

	private void colorNum(int start, int length) {
		try {
			doc.setCharacterAttributes(	start,
							length,
							numAttr,
							false);
		} catch (Exception e) {
			ErrorMgr.show(e, "Error coloring numbers.");
		}
	}

	private void expand(Range r) {
		checkForMultilineComments(r);

		int start = Math.max(r.getOffset() - 1, 0);
		int end = Math.min(doc.getLength(), r.getEnd() + 1);

		try {
			while (start > 0 &&!doc.getText(start, 1).equals("\n")){
				start--;
			}

			while (	end < doc.getLength() &&
				!doc.getText(end, 1).equals("\n"))
			{
				end++;
			}

			r.setOffset(start);
			r.setEnd(end);
		} catch (Exception e) {
			ErrorMgr.show(e, "Error expanding to line.");
		}
	}

	private boolean checkForMultilineComments(Range range) {
		Range a = rangeArrayContains(oldLists[ML_COMMENT], range.getOffset());
		Range b = rangeArrayOverlaps(lists[ML_COMMENT], range);

		if (a == null && b == null) {
			return false;
		}

		int aStart = (a == null) ? Integer.MAX_VALUE : a.getOffset();
		int bStart = (b == null) ? Integer.MAX_VALUE : b.getOffset();

		int start = Math.min(aStart, bStart);

		range.setOffset(start);
		range.setEnd(doc.getLength() - 1);
		return true;
	}

	private Range rangeArrayContains(ArrayList<Range> list, int index) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).contains(index)) {
				return list.get(i);
			}
		}

		return null;
	}

	private boolean rangeArrayContains(ArrayList<Range> list, Range r) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).contains(r)) {
				return true;
			}
		}

		return false;
	}

	private Range rangeArrayOverlaps(ArrayList<Range> list, Range r) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).overlaps(r)) {
				return list.get(i);
			}
		}

		return null;
	}

	public boolean shouldInsert(int index) {
		for (int i = 0; i < lists.length; i++) {
			ArrayList<Range> list = lists[i];
			for (int j = 0; j < list.size(); j++) {
				if (list.get(j).contains(index)) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean shouldInsertIgnoreEnd(int index) {
		for (int i = 0; i < lists.length; i++) {
			ArrayList<Range> list = lists[i];
			for (int j = 0; j < list.size(); j++) {
				if (list.get(j).containsIgnoreEnd(index)) {
					return false;
				}
			}
		}

		return true;
	}
	
	public boolean shouldRemove(int start, int length) {
		Range r = new Range(start, length);
		
		for (int i = 0; i < lists.length; i++) {
			ArrayList<Range> list = lists[i];
			for (int j = 0; j < list.size(); j++) {
				Range range = list.get(j);
				
				if (r.contains(range.getOffset())) {
					return true;
				} else if (range.overlaps(r)) {
					return false;
				}
			}
		}
		
		return true;
	}
}
