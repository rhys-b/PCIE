/*
*	Author: Rhys B.
*	Created: 2021-10-17
*	Modified: 2021-12-23
*
*	This class is used by the HighlightManager to highlight a specified
*	group of words using the styles contained in the SimpleAttributeSet it extends.
*/


import java.awt.Color;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


@SuppressWarnings("serial")
public class Attribute extends SimpleAttributeSet {
	private String[] words;
	private StyledDocument doc;
	private boolean functions, usePeriodDelimiter;
	
	public Attribute(	StyledDocument doc,
				String[] words,
				Color color,
				boolean bold,
				boolean functions,
				boolean usePeriodDelimiter)
	{
		
		this.doc = doc;
		this.words = words;
		this.functions = functions;
		this.usePeriodDelimiter = usePeriodDelimiter;
		
		StyleConstants.setForeground(this, color);
		StyleConstants.setBold(this, bold);
	}
	
	public void highlight(Range range) {
		// Called by the HighlightManager to highlight the
		// text in the styled document in the specified range.
		
		if (range.getLength() > 1) {
			try {
				String string = doc.getText(range.getOffset(), range.getLength());
				
				int start = indexOfNextPair(string, 0);
				
				while (start != -1) {
					int end;
					
					if (functions) {
						end = indexOfEndDeclaration(string, start);

						if (end == -1) {
							start = indexOfNextPair(string, start + 1);

							continue;
						}
					} else {
						end = indexOfNextDelimiter(string, start + 1);
						
						if (end == -1) {
							end = string.length();
						}
					}

					String sub = string.substring(start, end);
					if (search(sub)) {
						doc.setCharacterAttributes(range.getOffset() + start,
									end - start, this, true);
					}

					start = indexOfNextPair(string, end);
				}
			} catch (Exception e) {
				ErrorMgr.show(e, "Error highlighting the text.");
			}
		}
	}
	
	public static boolean isDelimiter(char c) {
		return !EditorPane.charArrayContains(EditorPane.ANTI_DELIMITERS, c);
	}
	
	public int indexOfNextDelimiter(String s, int start) {
		for (; start < s.length(); start++) {
			char c = s.charAt(start);
			if (isDelimiter(c) && (usePeriodDelimiter || c != '.')) {
				return start;
			}
		}
		
		return -1;
	}
	
	public int indexOfNextPair(String s, int start) {
		// Returns the index of the next character after start which is not
		// a delimiter, but the preceeding character is a delimiter.
		
		if (start == 0 && !isDelimiter(s.charAt(0))) {
			return 0;
		} else {
			start = indexOfNextDelimiter(s, start);
		}
		
		while (start != -1 && start < s.length() - 1) {
			if (!isDelimiter(s.charAt(start + 1))) {
				return start + 1;
			}
			
			start = indexOfNextDelimiter(s, start + 1);
		}
		
		return -1;
	}
	
	public int indexOfEndDeclaration(String s, int start) {
		// Starts at start, and looks through String s for the next non-whitespace delimiter.
		// If it is a '(', then it returns the previous character before any whitespace.
		// If it is not, or if the end of the string is reached, it returns -1.
		// NOTE: the start-th character in String s will never be a delimiter.

		int lastNonDelimiter = start;
		boolean passedWhitespace = false;

		for (start++; start < s.length(); start++) {
			char c = s.charAt(start);

			if (isDelimiter(c)) {
				if (c == '(') {
					return lastNonDelimiter + 1;
				} else if (Character.isWhitespace(c)) {
					passedWhitespace = true;
				} else {
					return -1;
				}
			} else {
				if (passedWhitespace) {
					return -1;
				}

				lastNonDelimiter++;
			}
		}
		
		return -1;
	}

	public boolean search(String needle) {
		return binarySearch(-1, words.length, needle);
	}

	public boolean binarySearch(int start, int end, String needle) {
		if (start + 1 == end) {
			return false;
		}

		int mid = (start + end) / 2;
		int compare = words[mid].compareTo(needle);

		if (compare == 0) {
			return true;
		} else if (compare > 0) {
			return binarySearch(start, mid, needle);
		} else {
			return binarySearch(mid, end, needle);
		}
	}
	
	public void updateOptions(Color color, boolean bold) {
		StyleConstants.setForeground(this, color);
		StyleConstants.setBold(this, bold);
	}
}
