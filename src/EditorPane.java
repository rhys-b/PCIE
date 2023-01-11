/*
*	Author: Rhys B
*	Created: 2021-08-20
*	Modified: 2021-12-28
*
*	This contains the JTextPane, which needs to be in
*	a JPanel to prevent it from line-wrapping.
*/


import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import javax.swing.text.Caret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.image.BufferedImage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.ArrayList;


@SuppressWarnings("serial")
public class EditorPane extends JPanel implements KeyListener, 
		MouseListener, MouseMotionListener, Runnable {
			
	private static final Cursor invisible = Toolkit.getDefaultToolkit()
		.createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
		new Point(0, 0), null);
			
	public static final char[] ANTI_DELIMITERS = generateDelimiters();
	private static char[] OPENERS = {'{', '[', '(', '<', '"', '\''};
	private static char[] CLOSERS = {'}', ']', ')', '>', '"', '\''};

	private JTextPane text = new JTextPane() {
		@Override
		public void cut() {
			getTabbedPane().cut();
		}

		@Override
		public void copy() {
			getTabbedPane().copy();
		}

		@Override
		public void paste() {
			getTabbedPane().paste();
		}

		private TabbedPane getTabbedPane() {
			return PCIE.getWindow().getTabbedPane();
		}
	};

	private File file = null;
	private StyledDocument doc = text.getStyledDocument();
	private HighlightManager highlightMgr = new HighlightManager(doc);
	private EditManager editMgr = new EditManager(text, highlightMgr);
	private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	private Caret docCaret = text.getCaret();
	private ActiveLine activeLine = new ActiveLine(text);
	private WidthMask widthMask = new WidthMask(text);
	private BracketMatch bracketMatch = new BracketMatch(text, highlightMgr);

	private int startingCaret = 0, activeClickCount,
		wordSelectionStart, wordSelectionEnd,
		wordOriginStart, wordOriginEnd;
	private int lastTypedChar;
	private boolean unbrokenOpening = false;
	private ArrayList<Integer> centerPressedLast = new ArrayList<Integer>();
	
	private JPopupMenu popup = new JPopupMenu();
	private JMenuItem cut = new JMenuItem("Cut");
	private JMenuItem copy = new JMenuItem("Copy");
	private JMenuItem paste = new JMenuItem("Paste");
	private JMenuItem undo = new JMenuItem("Undo");
	private JMenuItem redo = new JMenuItem("Redo");

	public EditorPane() {
		setTabs();
		
		updateOptions();

		text.addKeyListener(this);
		text.addMouseListener(this);
		text.addMouseMotionListener(this);
		text.setFocusTraversalKeysEnabled(false);
		text.addKeyListener(GUI.tabSwitcher);
		text.setLayout(null);
		text.add(activeLine);
		text.add(widthMask);

		setLayout(new BorderLayout());
		add(text);
		
		createPopupMenu();
	}
	
	public void updateOptions() {
		text.setFont(Defaults.EDITOR_FONT);
		text.setBackground(Defaults.EDITOR_BACKGROUND_COLOR);
		text.setForeground(Defaults.EDITOR_FONT_COLOR);
		text.setSelectedTextColor(Defaults.EDITOR_FONT_COLOR);
		setTabs();
		
		highlightMgr.updateOptions();
		widthMask.updateOptions();
		activeLine.updateOptions();
		bracketMatch.updateOptions();
	}
	
	private void createPopupMenu() {
		cut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				PCIE.getWindow().getTabbedPane().cut();
			}
		});
		
		copy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				PCIE.getWindow().getTabbedPane().copy();
			}
		});
		
		paste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				PCIE.getWindow().getTabbedPane().paste();
			}
		});
		
		undo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				PCIE.getWindow().getTabbedPane().undo();
			}
		});
		
		redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				PCIE.getWindow().getTabbedPane().redo();
			}
		});
		
		popup.add(cut);
		popup.add(copy);
		popup.add(paste);
		popup.addSeparator();
		popup.add(undo);
		popup.add(redo);
	}
	
	public void setTabs() {
		// Sets the tab width for the editor based on Defaults.TAB_WIDTH.
		
		FontMetrics fontMetrics = getFontMetrics(Defaults.EDITOR_FONT);
		int spaceWidth = fontMetrics.stringWidth(" ");
		
		TabStop[] stops = new TabStop[1000];
		for (int i = 0; i < stops.length; i++) {
			stops[i] = new TabStop((i + 1) * spaceWidth * Defaults.TAB_WIDTH,
				TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);
		}
		TabSet tabSet = new TabSet(stops);
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributeSet, tabSet);
		StyledDocument doc = text.getStyledDocument();
		doc.setParagraphAttributes(0, doc.getLength(), attributeSet, false);
	}

	public File save() {
		return save(file);
	}

	public File save(File f) {
		if (f == null) {
			throw new NullPointerException("A file was not set");
		}

		try {
			FileWriter writer = new FileWriter(f);
			writer.write(ensureEndline(text.getText()));
			writer.close();
		} catch (FileNotFoundException fnfe) {
			f = null;
			saveError("You do not have permission to save there.");
		} catch (Exception e) {
			f = null;
			saveError("The file could not be saved for an unknown reason.");
		}

		return f;
	}
	
	private String ensureEndline(String s) {
		if (s.length() == 0) {
			return "\n";
		}

		if (s.charAt(s.length() - 1) != '\n') {
			s += '\n';
		}
		
		return s;
	}

	public void saveAs(File file) {
		this.file = file;
		save();
	}

	public void setCaretPosition(int pos) {
		text.setCaretPosition(pos);
	}

	public StyledDocument getStyledDocument() {
		return text.getStyledDocument();
	}

	public boolean open(File file) {
		try {
			FileInputStream input = new FileInputStream(file);
			byte[] data = new byte[(int)file.length()];

			input.read(data);
			input.close();

			bracketMatch.clear();
			
			String s = new String(data, "UTF-8");
			if (s.charAt(s.length() - 1) == '\n') {
				s = s.substring(0, s.length() - 1);
			}

			text.setText(s);
			text.setCaretPosition(0);

			highlightMgr.recalculate();
			highlightMgr.repaint();

			bracketMatch.highlight();

			this.file = file;

			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(PCIE.getWindow(),
				"The selected file could not be opened.",
				"File Error",
				JOptionPane.ERROR_MESSAGE
			);

			return false;
		}
	}

	private void saveError(String message) {
		String[] options = {"Try Again", "Close Dialog", "Quit"};
		int option = JOptionPane.showOptionDialog(
			text,
			message,
			"Save Error",
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE,
			null,
			options,
			options[0]
		);

		if (option == 0) {
			save();
		} else if (option == 2) {
			PCIE.getWindow().dispose();
		}
	}

	public boolean inUse() {
		return (file != null || !text.getText().equals(""));
	}

	@Override
	public void requestFocus() {
		text.requestFocus();
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		text.addKeyListener(listener);
	}

	public int getPreferredHeight() {
		return text.getPreferredSize().height;
	}

	public String getText() {
		return text.getText();
	}

	public void setText(String s) {
		text.setText(s);
	}

	public boolean hasFile() {
		return (file != null);
	}
	
	public JTextPane getTextPane() {
		return text;
	}

	public boolean cut() {
		if (setClipboard()) {
			try {
				int start = text.getSelectionStart();
				String selection = text.getSelectedText();

				editMgr.crack();
				editMgr.remove(selection, start, false);
				editMgr.crack();
				
				bracketMatch.clear();

				doc.remove(start, text.getSelectionEnd() - start);

				highlightMgr.recalculate();
				highlightMgr.repaint(new Range(start, 0));

				bracketMatch.highlight();

				return true;
			} catch (Exception e) {
				ErrorMgr.show(e, "Something went wrong during cutting.");
			}
		}

		return false;
	}

	public boolean copy() {
		return setClipboard();
	}

	public boolean paste() {
		try {
			String s = getClipboardData();
			
			if (s != null) {
				String selection = text.getSelectedText();

				editMgr.crack();
				
				int start;
				if (selection == null) {
					start = text.getCaretPosition();
					editMgr.add(s, start);
				} else {
					start = text.getSelectionStart();
					
					editMgr.replace(selection, s, start);
					doc.remove(text.getSelectionStart(), selection.length());
				}

				int caretPosition = text.getCaretPosition();

				bracketMatch.clear();

				doc.insertString(caretPosition, s, null);
				editMgr.crack();

				highlightMgr.recalculate();
				highlightMgr.repaint(new Range(caretPosition, s.length()));

				bracketMatch.highlight();

				return true;
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Something went wrong during pasting.");
		}

		return false;
	}

	public boolean setClipboard() {
		String s = getSelectedText();

		if (s != null) {
			clipboard.setContents(new StringSelection(s), null);
			return true;
		}

		return false;
	}
	
	// Return the contents of the clipboard, or null if the clipboard does not contain text.
	public String getClipboardData() {
		String s = null;
		try {
			if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
				s = (String)clipboard.getContents(null)
					.getTransferData(DataFlavor.stringFlavor);
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error retieving clipboard data.");
		}
		
		return s;
	}

	public boolean undo() {
		return editMgr.undo();
	}

	public boolean redo() {
		return editMgr.redo();
	}

	public String getSelectedText() {
		return text.getSelectedText();
	}

	public void keyPressed(KeyEvent ke) {
		int k = ke.getKeyCode();
		String selection = text.getSelectedText();
		int caret = text.getCaretPosition();
		int selStart = text.getSelectionStart(), selEnd = text.getSelectionEnd();
		
		if (k == KeyEvent.VK_O && ke.isControlDown() && ke.isShiftDown()) {
			// This is a keybind that does something weird and I don't know
			// why the Java developers implemented it but I don't like it.
			ke.consume();
			return;
		}

		if (	k == KeyEvent.VK_UP || k == KeyEvent.VK_DOWN ||
			k == KeyEvent.VK_LEFT || k == KeyEvent.VK_RIGHT ||
			k == KeyEvent.VK_HOME || k == KeyEvent.VK_END) {
			
			if (ke.isShiftDown()) {
				if (ke.isControlDown()) {
					if (k == KeyEvent.VK_RIGHT) {
						if (selection == null) {
							if (caret < doc.getLength() - 1) {
								wordSelectionStart = caret;
								wordSelectionEnd = getNextIndexOf(
									ANTI_DELIMITERS, true,caret+1);
							} else {
								wordSelectionStart = caret;
								wordSelectionEnd = doc.getLength();
							}
							
							wordOriginStart = caret;
							wordOriginEnd = caret;
						} else {
							if (selStart < wordOriginStart) {
								wordSelectionStart =
									tryIncrementSelection(
									getNextIndexOf(ANTI_DELIMITERS,
										true, selStart + 1),
									wordSelectionEnd);
									
								if(wordSelectionStart>wordOriginStart){
									wordSelectionEnd =
										wordSelectionStart;
									wordSelectionStart =
										wordOriginStart;
								}
							} else if (wordSelectionEnd<doc.getLength()-1){
								wordSelectionEnd = getNextIndexOf(
									ANTI_DELIMITERS,true,selEnd+1);
							}
						}
					} else if (k == KeyEvent.VK_LEFT) {
						if (selection == null) {
							if (caret > 0) {
								wordSelectionEnd = caret;
								wordSelectionStart =
									tryIncrementSelection(
										getLastIndexOf(
										ANTI_DELIMITERS,
										true, selStart - 1),
									wordSelectionEnd);
							} else {
								wordSelectionStart = 0;
								wordSelectionEnd = caret;
							}
							
							wordOriginStart = caret;
							wordOriginEnd = caret;
						} else {
							if (wordSelectionEnd > wordOriginEnd) {
								wordSelectionEnd = getLastIndexOf(
									ANTI_DELIMITERS, true, selEnd);
								
								if (wordSelectionEnd < wordOriginEnd) {
									wordSelectionStart =
										wordSelectionEnd;
									wordSelectionEnd=wordOriginEnd;
								}
							} else if (wordSelectionStart > 0) {
								wordSelectionStart =
									tryIncrementSelection(
										getLastIndexOf(
										ANTI_DELIMITERS, true,
										selStart - 1),
									wordSelectionEnd);
							}
						}
					}
					
					ke.consume();
					select(wordSelectionStart, wordSelectionEnd);
				} else if (selection == null) {
						wordOriginStart = caret;
						wordOriginEnd = caret;
						
						wordSelectionStart = caret;
						wordSelectionEnd = caret;
				}
			} else if (selection == null) {
				if (ke.isControlDown()) {
					if (k == KeyEvent.VK_LEFT) {
						if (caret > 0) {
							int newCaret = getLastIndexOf(
								ANTI_DELIMITERS, true, caret - 1);
							
							try {
								if (!charArrayContains(ANTI_DELIMITERS,
									doc.getText(newCaret, 1)
											.charAt(0)))
								{
									newCaret++;
								}
							} catch (Exception e) {
								ErrorMgr.show(e, "Error moving the" +
									" caret backward a word.");
							}
							
							text.setCaretPosition(newCaret);
							
							ke.consume();
						}
					} else if (k == KeyEvent.VK_RIGHT) {
						if (doc.getLength() > caret + 1) {
							int newCaret = getNextIndexOf(
								ANTI_DELIMITERS, true, caret + 1);
							
							text.setCaretPosition(newCaret);
							
							ke.consume();
						}
					}
				}
			} else {
				if (k == KeyEvent.VK_LEFT) {
					text.setCaretPosition(selStart);
					ke.consume();
				} else if (k == KeyEvent.VK_RIGHT) {
					text.setCaretPosition(text.getSelectionEnd());
					ke.consume();
				}
			}
			
			editMgr.crack();
			unbrokenOpening = false;
		} else if (k == KeyEvent.VK_ENTER) {
			editMgr.crack();

			if (selection == null) {
				try {
					// Overview: Create a string with at least a new line,
					// but possibly also the automatically-inserted tabs
					// and an extra newline with its own tabs depending if
					// the editor should auto-tab and/or insert end characters.
					// The string is then inserted into the document
					// and the edit manager is informed.
					
					// Create the string, with a newline as default.
					String insert = "\n";
					
					// Get the line the caret is on.
					char[] newline = {'\n'};
					int start = getLastIndexOf(newline, false, caret);
					String line = doc.getText(start, caret - start);
					int newCaret = caret + 1;
					
					if (!line.equals("")) {
						// Create a string containing the same number
						// of tabs as the previous line.
						int index = 0;
						String tabs = "";
						if (line.charAt(0) == '\n') {
							index = 1;
						}
						while(line.length()>index && line.charAt(index)=='\t'){
							tabs += "	";
							index++;
						}
						
						// If supposed to auto-tab, add the tab string
						// to insert, and if the caret line ends with a '{',
						// then add an extra tab.
						if (Defaults.AUTO_TAB) {
							if (line.charAt(line.length() - 1) == '{') {
								insert += tabs + "	";
							} else {
								insert += tabs;
							}
							
							newCaret = caret + insert.length();
						}
						
						// If end characters are auto-inserted, and the
						// character after the caret is a '}',
						// then create a new line for that closing bracket.
						if (Defaults.INSERT_END_CHARS && doc.getLength()>caret
							&& doc.getText(caret, 1).equals("}"))
						{
								
							insert += "\n" + tabs;
						}
					}
					
					// Finishing.
					bracketMatch.clear();
					doc.insertString(caret, insert, null);
					editMgr.add(insert, caret, newCaret);
					text.setCaretPosition(newCaret);

					highlightMgr.recalculate();
					highlightMgr.repaint(new Range(caret, insert.length()));

					bracketMatch.highlight();
					
					// Everything's now been handled.
					ke.consume();
				} catch (Exception e) {
					ErrorMgr.show(e, "Error inserting a new line.");
				}
			} else {
				bracketMatch.clear();
				
				editMgr.replace(selection, "\n", selStart);

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						highlightMgr.recalculate();
						highlightMgr.repaint(new Range(selStart, 1));

						bracketMatch.highlight();
					}
				});
			}
			
			editMgr.crack();
			PCIE.getWindow().getTabbedPane().setActiveSaved(false);
		} else if (k == KeyEvent.VK_TAB) {
			if (!ke.isControlDown()) {
				boolean setUnsaved = true;
				
				if (selection == null) {
					if (ke.isShiftDown()) {
						try {
							bracketMatch.clear();
							
							editMgr.add("	", caret);
							doc.insertString(caret, "	", null);
							
							highlightMgr.recalculate();
							highlightMgr.repaint(new Range(caret, 1));

							bracketMatch.highlight();
						} catch (Exception e) {
							ErrorMgr.show(e,"Error in manual tab insert.");
						}
						
						return;
					}
					
					boolean handled = false;
					int index = charAfterWhitespace(text.getCaretPosition());
					
					try {
						if (index != -1) {
							char nextChar =doc.getText(index, 1).charAt(0);
							if (charArrayContains(CLOSERS, nextChar) &&
								Defaults.INSERT_END_CHARS &&
								unbrokenOpening)
							{
								
								text.setCaretPosition(index + 1);
								handled = true;
								ke.consume();
								editMgr.crack();
								
								setUnsaved = false;
							}
						}
					} catch (Exception e) {
						ErrorMgr.show(e, "Error while moving the caret.");
					}
					
					if (!ke.isShiftDown() && !handled) {
						editMgr.add("	", caret);
						bracketMatch.clear();

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								highlightMgr.recalculate();
								highlightMgr.repaint(
										new Range(caret,1));

								bracketMatch.highlight();
							}
						});
					}
				} else {
					try {
						String string;

						if (ke.isShiftDown()) {
							string = selection.replace("\n	", "\n");

							char before = string.charAt(0);
							int start;

							if (before == '	') {
								string = string.substring(1);
								start = selStart;
							} else if (selStart > 0) {
								if (doc.getText(selStart - 1, 1)
									.equals("	"))
								{
									start = selStart - 1;
								} else {
									start = selStart;
								}
							} else {
								start = 0;
							}

							int len = text.getSelectionEnd() - start;
							String old = doc.getText(start, len);

							editMgr.replace(old, string, start);
							bracketMatch.clear();

							doc.remove(start, len);
							doc.insertString(start, string, null);

							highlightMgr.recalculate();
							highlightMgr.repaint(new Range(
									start, string.length()));

							bracketMatch.highlight();

							select(start, start + string.length());
						} else {
							string = indent(selection);

							editMgr.replace(selection, string, selStart);
							bracketMatch.clear();

							doc.remove(selStart, selection.length());
							doc.insertString(selStart, string, null);

							highlightMgr.recalculate();
							highlightMgr.repaint(
								new Range(selStart, string.length()));


							bracketMatch.highlight();

							select(selStart, selStart + string.length());
						}
					} catch (Exception e) {
						ErrorMgr.show(e,"Something went wrong block-tabbing.");
					}

					ke.consume();
				}
				
				if (setUnsaved) {
					PCIE.getWindow().getTabbedPane().setActiveSaved(false);
				}
			}
		} else if (k == KeyEvent.VK_BACK_SPACE) {
			try {
				if (ke.isControlDown()) {
					if (caret >= 2) {
						if (selection == null) {
							if (ke.isShiftDown()) {
								char[] newline = {'\n'};
								char before = doc.getText(caret - 1, 1)
											.charAt(0);

								int start;
								if (before == '\n') {
									start = getLastIndexOf(newline,
										false, caret - 1);
								} else {
									start = getLastIndexOf(
										newline, false, caret);
								}

								if(doc.getText(start, 1).equals("\n")){
									start++;
								}

								int length = caret - start;

								tryClosingBack(doc.getText(
										start, length), start);
							} else {
								String last2 = doc.getText(caret -2,2);
								boolean lastIsDelimiter =
									!charArrayContains(
										ANTI_DELIMITERS,
										last2.charAt(1));
								boolean secondLastIsDelimiter =
									!charArrayContains(
										ANTI_DELIMITERS,
										last2.charAt(0));
								int start, length;
								String string;

								if (lastIsDelimiter &&
									last2.charAt(0) ==
									last2.charAt(1))
								{
									char[] lim = {last2.charAt(0)};
									start = getLastIndexOf(
										lim, true, caret) + 1;
									length = caret - start;
									string = doc.getText(
											start, length);
									
									tryClosingBack(string, start);
								} else if (secondLastIsDelimiter &&
										!lastIsDelimiter)
								{
									start = caret - 1;
									tryClosingBack(doc.getText(
										start, 1), start);
								} else {
									start = getLastIndexOf(
										ANTI_DELIMITERS, true,
										caret - 1);
									length = caret - start;
									string = doc.getText(
										start, length);

									if (!charArrayContains(
										ANTI_DELIMITERS,
										string.charAt(0)))
									{
										string = string
											.substring(1);
										start++;
										length--;
									}
									
									tryClosingBack(string, start);
								}
							}
						} else {
							removeString(selection, selStart, false);
						}

						PCIE.getWindow().getTabbedPane()
										.setActiveSaved(false);
					} else {
						tryClosingBack(doc.getText(0, caret), 0);
					}
				} else {
					if (selection == null) {
						if (caret > 0) {
							tryClosingBack(doc.getText(caret - 1, 1),
											caret - 1);
							PCIE.getWindow().getTabbedPane()
										.setActiveSaved(false);
						}
					} else {
						removeString(selection, selStart, false);
						PCIE.getWindow().getTabbedPane()
										.setActiveSaved(false);
					}
				}

				ke.consume();
			} catch (Exception e) {
				ErrorMgr.show(e, "Something went wrong removing text.");
			}
		} else if (k == KeyEvent.VK_DELETE) {
			try {
				if (selection == null) {
					if (caret < doc.getLength()) {
						if (ke.isControlDown() && caret < doc.getLength() - 1){
							int index, len;

							if (ke.isShiftDown()) {
								char[] newline = {'\n'};
								char next = doc.getText(caret, 1)
											.charAt(0);

								if (next == '\n') {
									index = getNextIndexOf(
										newline, false,
										caret + 1);
								} else {
									index = getNextIndexOf(newline,
											false, caret);
								}

								len = index - caret;
								removeString(doc.getText(caret, len),
											caret, false);
							} else {
								String next2 = doc.getText(caret, 2);
								char nextChar = next2.charAt(0);
								boolean isNextDelimiter =
									!charArrayContains(
									ANTI_DELIMITERS, nextChar);
								boolean isSecondNextDelimiter =
									!charArrayContains(
										ANTI_DELIMITERS,
										next2.charAt(1));

								if (!isNextDelimiter &&
										isSecondNextDelimiter)
								{
									removeString(Character
										.toString(nextChar),
										caret, false);
								} else if (isNextDelimiter &&
									next2.charAt(0) ==
										next2.charAt(1))
								{
									char[] arr = {next2.charAt(0)};
									
									index = getNextIndexOf(arr,
											true, caret);
									len = index - caret;

									removeString(doc
										.getText(caret, len),
										caret, false);
								} else {
									index = getNextIndexOf(
										ANTI_DELIMITERS,
										true, caret + 1);
									len = index - caret;
									removeString(doc
										.getText(caret, len),
										caret, false);
								}
							}
						} else {
							removeString(doc.getText(caret,1),caret,false);
						}
						
						PCIE.getWindow().getTabbedPane()
										.setActiveSaved(false);
					}
				} else {
					removeString(selection, selStart, false);
					PCIE.getWindow().getTabbedPane().setActiveSaved(false);
				}

				ke.consume();
			} catch (Exception e) {
				ErrorMgr.show(e, "Something went wrong removing text.");
			}
		}
	}

	public void keyTyped(KeyEvent ke) {
		char c = ke.getKeyChar();
		int k = (int)c;
		String selection = text.getSelectedText();
		int caret = text.getCaretPosition();

		if (!ke.isControlDown() && !(k == KeyEvent.VK_ENTER || k == KeyEvent.VK_BACK_SPACE ||
			k == KeyEvent.VK_DELETE || k == KeyEvent.VK_TAB || k == KeyEvent.VK_ESCAPE)) {
			
			hideCursor();

			if (selection == null) {
				boolean handled = false;
				
				char prev = '\0';
				if (caret > 0) {
					try {
						prev = doc.getText(caret - 1, 1).charAt(0);
					} catch (Exception e) {
						ErrorMgr.show(e, "Error getting previous character.");
					}
				}
				
				if (Defaults.AUTO_TAB) {
					try {
						if (	c == '}' &&
							prev == '	' &&
							lastTypedChar == KeyEvent.VK_ENTER) {
								
							editMgr.replace("	", "}", caret - 1);

							doc.remove(caret - 1, 1);
							doc.insertString(caret - 1, "}", null);

							// highlight manager does not care

							handled = true;
						}
					} catch (Exception e) {
						ErrorMgr.show(e, "Error while removing extra tabs.");
					}
				}
				
				if (!handled && Defaults.INSERT_END_CHARS) {
					// Get the next character.
					char next = '\0';
					try {
						next = doc.getText(caret, 1).charAt(0);
					} catch (Exception e) {
						// Leave empty.
					}

					// Get the corresponding closing character.
					// (In case it's needed).
					char close = getClosingChar(c);

					// Get the second last character.
					char prev2 = '\0';
					try {
						prev2 = doc.getText(caret - 2, 1).charAt(0);
					} catch (Exception e) {
						// Leave empty.
					}

					// If the user enters a closing character,
					// then check if that is a single or double quote.
					if (charArrayContains(CLOSERS, c)) {
						if (c == '"') {
							// If it is a double quote, then check if the
							// PREVIOUS position should insert.

							if (!highlightMgr
								.shouldInsertIgnoreEnd(caret - 1))
							{
								// If it should NOT insert, then check
								// if the next character is also a
								// double quote, in which case that
								// quote should just be skipped over,
								// assuming the previous character is
								// not a backslash, or the previous
								// character is a backslash but so is
								// the one before that.

								if (	next == '"' &&
									(prev != '\\' ||
									(prev == '\\' &&
									prev2 == '\\')))
								{
									// Skip over a character.
									bracketMatch.clear();

									text.setCaretPosition(caret+1);
									handled = true;

									bracketMatch.highlight();
									
									editMgr.crack();
								} else {
									// If it should NOT insert but
									// any other conditions
									// are not met, then a single
									// quote should be added.

									try {
										editMgr.add("\"",
												caret);
										bracketMatch.clear();
										doc.insertString(caret,
											"\"", null);

										highlightMgr
											.recalculate();
										highlightMgr.repaint(
											new Range(
											caret, 1));

										bracketMatch
											.highlight();

										handled = true;
									} catch (Exception e) {
										ErrorMgr.show(e,
											"Error " +
											"inserting" +
											"a quote.");
									}
								}
							}
							// If it should insert,
							// then don't do anything and just go to the
							// second part, where inserting happens.
						} else if (c == '\'') {
							// If it is a single quote, and the position
							// should NOT insert, and the next character
							// is also a single quote and the previous
							// character is not a backslash(uness the one
							// before that is also a backslash), then skip
							// over the next character.

							if (	!highlightMgr.shouldInsert(caret) &&
								next == '\'' &&
								(prev != '\\' ||
								(prev == '\\' &&
								prev2 == '\\')))
							{
								bracketMatch.clear();

								text.setCaretPosition(caret + 1);
								handled = true;

								bracketMatch.highlight();
								
								editMgr.crack();
							}
							// If any of the immediately above are not met,
							// (other than the single quote)
							// then don't do anything and just go to the
							// second part, where inserting happens.
						} else if (c == next && unbrokenOpening) {
							// If it is not a single quote, nor a double
							// quote, and the next character is the same as
							// the typed character, and there is an
							// unbroken opening, then skip over the next
							// character.

							bracketMatch.clear();

							text.setCaretPosition(caret + 1);
							handled = true;

							bracketMatch.highlight();
							
							editMgr.crack();
						}
					}

					if (!handled && charArrayContains(OPENERS, c)) {
						// If the user did not type an ending character,
						// but rather typed an opening character,
						// then check if the posiiton should insert.
						
						boolean proceed = true;
						if ((c == '"' || c == '\'') &&
							(next == '\'' && (prev == '\'' ||
							(prev == '\\' && prev2 == '\''))))
						{
							proceed = false;
						}

						if (proceed && highlightMgr
							.shouldInsertIgnoreEnd(caret - 1) &&
							(!(c == '<' && !((prev >= 'A' && prev <= 'Z') ||
							(prev >= 'a' && prev <= 'z')))) &&
							!(prev == '\'' && next == '\''))
						{
							// Insert the character along
							// with its closing counterpart.

							try {
								bracketMatch.clear();
							
								String string = c + "" + close;
								doc.insertString(caret, string, null);

								editMgr.add(string, caret, caret + 1);
								editMgr.crack();

								highlightMgr.recalculate();
								highlightMgr.repaint(new Range(caret,
										string.length()));

								text.setCaretPosition(caret + 1);
								handled = true;

								bracketMatch.highlight();

								unbrokenOpening = true;
							} catch (Exception e) {
								ErrorMgr.show(e, "Error inserting" +
										" ending character.");
							}
						}
						// If the position should NOT insert,
						// then let the next 'if' handle this event.
					}
				}

				if (!handled) {
					try {
						bracketMatch.clear();
						
						String string = Character.toString(c);
						doc.insertString(caret, string, null);
						editMgr.add(string, caret);

						highlightMgr.recalculate();
						highlightMgr.repaint(new Range(caret,string.length()));

						bracketMatch.highlight();
					} catch (Exception e) {
						ErrorMgr.show(e, "Error typing in the document.");
					}
				}
			} else {
				int start = text.getSelectionStart();
				String string = Character.toString(c);
				
				editMgr.replace(selection, string, start);
				bracketMatch.clear();

				try {
					doc.remove(start, selection.length());
					doc.insertString(start, string, null);
				} catch (Exception e) {
					ErrorMgr.show(e, "Error replacing text.");
				}

				highlightMgr.recalculate();
				highlightMgr.repaint(new Range(start, string.length()));

				bracketMatch.highlight();
			}

			PCIE.getWindow().getTabbedPane().setActiveSaved(false);
		}
		
		lastTypedChar = k;
		ke.consume();
	}
	
	private int tryIncrementSelection(int start, int end) {
		try {
			if (end > start && !charArrayContains(ANTI_DELIMITERS, doc.getText(
				start, end - start).charAt(0))) {
				
				return start + 1;
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error incrementing for arrow-powered word find.");
		}
		
		return start;
	}
	
	private void insertEndChar(char c, int caret) {
		try {
			String string = Character.toString(c);

			editMgr.add(string, caret + 1);
			editMgr.crack();
			
			bracketMatch.clear();

			doc.insertString(caret + 1, string, null);

			highlightMgr.recalculate();
			highlightMgr.repaint(new Range(caret + 1, string.length()));

			text.setCaretPosition(caret + 1);

			bracketMatch.highlight();
			
			unbrokenOpening = true;
		} catch (Exception e) {
			ErrorMgr.show(e, "Error auto-inserting end character.");
		}
	}

	public void mousePressed(MouseEvent me) {
		showCursor();
		
		int button = me.getButton();
		
		if (button == 4 || button == 5) {
			JScrollBar bar=((JScrollPane)getParent().getParent()).getHorizontalScrollBar();
			
			if (button == 4) {
				bar.setValue(bar.getValue() - bar.getUnitIncrement());
			} else {
				bar.setValue(bar.getValue() + bar.getUnitIncrement());
			}
			
			me.consume();
			return;
		}
		
		if (button == 2 && Defaults.PLATFORM == Defaults.Platform.LINUX) {
			// This exists to handle pasting from the XSelection
			// clipboard on X11 based linuxes.
			
			centerPressedLast.add(doc.getLength());
			new Thread(this).start();
			return;
		}
		
		if (button == 3) {
			// Open the menu.
			popup.show(this, me.getX(), me.getY());
		}
		
		if (doc.getLength() > 0) {
			activeClickCount = me.getClickCount();	// Needed for mouseReleased.

			if (activeClickCount == 1) {
				startingCaret = text.getCaretPosition();
			} else if (activeClickCount == 2) {
				try {
					boolean flip = false;
					char c;
					int last, next;
					
					if (startingCaret > 0) {
						c = doc.getText(startingCaret - 1, 1).charAt(0);
					} else {
						c = doc.getText(0, 1).charAt(0);
					}
					
					if (charArrayContains(ANTI_DELIMITERS, c)) {
						last = getLastIndexOf(ANTI_DELIMITERS, true,
											startingCaret);
						next = getNextIndexOf(ANTI_DELIMITERS, true,
											startingCaret);
						
						if (!charArrayContains(ANTI_DELIMITERS,
									doc.getText(last,1).charAt(0)))
						{
							last++;
						}
					} else {
						char[] arr = {c};
						last = getLastIndexOf(arr, true, startingCaret);
						next = getNextIndexOf(arr, true, startingCaret);
						
						if (doc.getText(last, 1).charAt(0) != c) {
							last++;
						}
					}

					wordOriginStart = last;
					wordOriginEnd = next;

					wordSelectionStart = wordOriginStart;
					wordSelectionEnd = wordOriginEnd;

					select(wordSelectionStart, wordSelectionEnd);
				} catch (Exception e) {
					ErrorMgr.show(e, "Error while highlighting a word.");
				}
			} else if (activeClickCount == 3) {
				char[] newline = {'\n'};
				int last = getLastIndexOf(newline, false, startingCaret);
				int next = getNextIndexOf(newline, false, startingCaret);

				try {
					if (doc.getText(last, 1).equals("\n")) {
						last++;
					}
				} catch (Exception e) {
					ErrorMgr.show(e, "Something went wrong" +
									" highlighting a whole line.");
				}

				wordOriginStart = last;
				wordOriginEnd = next;

				wordSelectionStart = wordOriginStart;
				wordSelectionEnd = wordOriginEnd;

				select(wordSelectionStart, wordSelectionEnd);
			}

			editMgr.crack();
			unbrokenOpening = false;
		}
	}

	public void mouseReleased(MouseEvent me) {
		// This counteracts the default behaviour of JTextPane that
		// selects the next character after a word if the selection
		// requires the scroll pane to move. It cannot be stopped
		// using consume(), and definately might be a bug. This is
		// the only way I can stop it.
		if (activeClickCount == 2 && !(me.getButton() == 4 || me.getButton() == 5)) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (	text.getSelectionStart() != wordSelectionStart ||
						text.getSelectionEnd() != wordSelectionEnd) {
						
						select(wordSelectionStart, wordSelectionEnd);
					}
				}
			});
		}
		
		activeClickCount = 0;
	}

	public void mouseDragged(MouseEvent me) {
		try {
			int caret = text.getCaretPosition();

			if (activeClickCount == 2) {
				if (caret > wordOriginEnd) {
					wordSelectionEnd = getNextIndexOf(ANTI_DELIMITERS, true,caret);
				} else if (caret < wordOriginStart) {
					wordSelectionStart =getLastIndexOf(ANTI_DELIMITERS,true,caret);
					if (!charArrayContains(ANTI_DELIMITERS,
							doc.getText(wordSelectionStart, 1).charAt(0)))
					{
						wordSelectionStart++;
					}
				} else {
					wordSelectionEnd = wordOriginEnd;
					wordSelectionStart = wordOriginStart;
				}

				// Must be here, because JTextPane's default behaviour is to highlight
				// incorrectly, so this overrides that.
				select(wordSelectionStart, wordSelectionEnd);
			} else if (activeClickCount == 3) {
				char[] newline = {'\n'};

				if (caret > wordOriginEnd) {
					wordSelectionEnd = getNextIndexOf(newline, false, caret);
				} else if (caret < wordOriginStart) {
					wordSelectionStart = getLastIndexOf(newline, false, caret);
					if (doc.getText(wordSelectionStart, 1).equals("\n")) {
						wordSelectionStart++;
					}
				} else {
					wordSelectionStart = wordOriginStart;
					wordSelectionEnd = wordOriginEnd;
				}

				select(wordSelectionStart, wordSelectionEnd);
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Something went wrong maintaining a word highlight.");
		}
	}

	public void select(int from, int to) {
		// Highlights text starting at 'from' and going to 'to'. The reason it is so convoluded
		// is because the scroll pane only moves once everything is done, which means for a
		// highlight, the scroll only goes as far as the caret-side of the highlight.
		// Therefore, if the highlight is off the screen on the left side, the highlight must
		// select backwards, and then wait for a repaint on the scroll pane.
		
		text.setCaretPosition(to);
		text.moveCaretPosition(from);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				text.setCaretPosition(from);
				text.moveCaretPosition(to);
			}
		});
	}

	public File getFile() {
		return file;
	}
	
	public EditManager getEditManager() {
		return editMgr;
	}
	
	// Finds the last(before int start) index of String find in the body of text,
	// returning -1 if there is no instance of String find, or it is beyond int maxSize.
	public int getLastIndexOf(String find, int start, int maxSize) {
		try {
			int len = find.length();
			
			if (start >= len) {
				int i = start - len;
				
				while (i >= 0 && start - len - i < maxSize) {
					String compare = doc.getText(i, len);
					
					if (compare.equals(find)) {
						return i;
					}
					
					i--;
				}
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error retrieving text to find string.");
		}
		
		return -1;
	}
	
	// Find the next(after int start) index of String find in the body of text,
	// returning -1 if there is no instance of String find, or it is beyond int maxSize.
	public int getNextIndexOf(String find, int start, int maxSize) {
		try {
			int len = find.length();
			int docLen = doc.getLength();
			
			if (start <= docLen - len) {
				int i = start;
				
				while (i <= docLen - len && i - start < maxSize) {
					String compare = doc.getText(i, len);
					
					if (compare.equals(find)) {
						return i;
					}
					
					i--;
				}
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error retrieving text to find string.");
		}
		
		return -1;
	}
	
	// Returns if there's text in this editor.
	public boolean containsText() {
		return doc.getLength() > 0;
	}
	
	public HighlightManager getHighlightManager() {
		return highlightMgr;
	}
	
	public void formatIndentation() {
		// Indents one tab whenever one of '{', '[' or '(' is found in the file,
		// and reduces the number of tabs when a closing character is found.
		
		String input = null, output = "";
		String insert;
		char c;
		boolean isClosingChar, changesMade = false;
		int i = 0;
		int startingCaret = text.getCaretPosition();
		int actualIndent;
		int correctIndent = 0;	// Maintains the number of tabs that should be
					// at the beginning of the next line.
		
		try {
			input = doc.getText(0, doc.getLength());
		} catch (Exception e) {
			ErrorMgr.show(e, "Error getting text from document.");
		}
		
		// Skip over any starting tabs.
		while (i < input.length() && input.charAt(i) == '	') {
			changesMade = true;
			i++;
		}
		
		// Iterate through the loop, looking for any valid characters that are openers.
		while (i < input.length()) {
			c = input.charAt(i);
			
			if (highlightMgr.shouldInsert(i)) {
				if (c == '\n') {
					actualIndent = 0;
					while (i + 1 < input.length()) {
						c = input.charAt(i + 1);
						if (c != '	') {
							break;
						}
						
						actualIndent++;
						i++;
					}
					
					if (c == '}' || c == ')' || c == ']') {
						correctIndent--;
						i++;
						isClosingChar = true;
					} else {
						isClosingChar = false;
					}
					
					if (actualIndent != correctIndent) {
						changesMade = true;
					}
					
					insert = "\n";
					for (int j = 0; j < correctIndent; j++) {
						insert += "	";
					}
					
					if (isClosingChar) {
						insert += c;
					}
					
					output += insert;
				} else {
					if (c == '{' || c == '[' || c == '(') {
						correctIndent++;
					} else if (c == '}' || c == ']' || c == ')') {
						correctIndent--;
					}
					
					output += c;
				}
			} else {
				output += c;
			}
			
			i++;
		}
		
		if (changesMade) {
			startingCaret = Math.min(startingCaret, output.length());

			bracketMatch.clear();

			text.setText(output);
			text.setCaretPosition(startingCaret);

			PCIE.getWindow().getTabbedPane().setActiveSaved(false);
			editMgr.replace(input, output, 0, startingCaret);

			highlightMgr.recalculate();
			highlightMgr.repaint();

			bracketMatch.highlight();
		}
	}
	
	// Returns the value that indent should be. It returns the same as the input if there
	// are no closing characters, or if indent is 0.
	public int checkReduceIndent(int indent, char c) {
		if (c == '}' || c == ')') {
			if (indent > 0) {
				indent--;
			}
		}
		
		return indent;
	}

	// Returns the last index of any character in limits, starting at index,
	// Unless flip is true, in which case it finds the last index that is NOT in limits.
	private int getLastIndexOf(char[] limits, boolean flip, int index) {
		String string;
		int size = 100;
		char c;
		boolean atStart = false;
		int start;

		try {
			while (true) {
				if (index > size) {
					start = index - size;
					string = doc.getText(start, size);
				} else {
					start = 0;
					string = doc.getText(start, index);
					if (string.equals("")) return 0;
					atStart = true;
				}

				for (int i = string.length() - 1; i >= 0; i--) {
					c = string.charAt(i);

					if (flip != charArrayContains(limits, c)) {
						return i + start;
					}
				}

				if (atStart) {
					return 0;
				} else {
					size += 500;
				}
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error using remove word.");
		}

		return -1; // Should never get here.
	}

	private int getNextIndexOf(char[] limits, boolean flip, int index) {
		String string;
		int size = 100;
		char c;
		boolean atEnd = false;
		int len = doc.getLength();
		int offset;

		try {
			while (true) {
				if (index + size > len) {
					atEnd = true;
					string = doc.getText(index, len - index);
				} else {
					string = doc.getText(index, size);
				}

				for (int i = 0; i < string.length(); i++) {
					if (flip != charArrayContains(limits, string.charAt(i))) {
						return i + index;
					}
				}

				if (atEnd) {
					return len;
				} else {
					size += 500;
				}
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error deleting a word.");
		}

		return -1; // Should never get here.
	}

	public static boolean charArrayContains(char[] arr, char c) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == c) {
				return true;
			}
		}

		return false;
	}

	private String indent(String input) {
		// Takes String input, assuming it is code, and indents each
		// line(that is not empty) one tab, returning the result.

		String out = "	";
		char c, next = input.charAt(0);

		for (int i = 0; i < input.length() - 1; i++) {
			c = next;
			next = input.charAt(i + 1);

			if (c == '\n' && next != '\n') {
				out += "\n	";
			} else {
				out += c;
			}
		}
		out += next;

		return out;
	}

	private static char[] generateDelimiters() {
		char[] arr = new char[64];

		for (int i = 0; i < 26; i++) {
			arr[i] = (char)(i + 65);
			arr[i + 26] = (char)(i + 97);
		}

		for (int i = 0; i < 10; i++) {
			arr[i + 52] = (char)(i + 48);
		}

		arr[62] = (char)95;
		arr[63] = (char)45;
		
		return arr;
	}

	private char getClosingChar(char in) {
		// Return the closing variant of the opening characters.

		for (int i = 0; i < OPENERS.length; i++) {
			if (in == OPENERS[i]) {
				return CLOSERS[i];
			}
		}

		return '\0';
	}
	
	private void removeCheckClosingBack(String string, int location) {
		// Checks the string to be removed for characters after the caret that might
		// have been added automatically, and removes them as well.
		
		ArrayList<Character> list = new ArrayList<Character>();
		
		// Populate the list with closing characters immediately after the caret.
		int index = text.getCaretPosition();
		char c;
		try {
			while (doc.getLength() > index) {
				c = doc.getText(index, 1).charAt(0);
				if (charArrayContains(CLOSERS, c)) {
					list.add(c);
				} else {
					break;
				}

				index++;
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error getting text from the document.");
		}
		
		if (list.size() == 0) {
			removeString(string, location, true);
			return;
		}
		
		// The first loop only goes to index 2, because there needs to
		// be a check to make sure that quotes are not negated by backquotes,
		// and those backquotes are not negated by another backquote.
		// Then, the other if statements clean up the first two characters,
		// including the characters before the input string.
		
		// Go backwards through the string to be removed and check if the
		// characters in list are present(in the reverse order).
		index = 0;
		for (int i = string.length() - 1; i > 1 && index < list.size(); i--) {
			if (!((string.charAt(i) == '"' || string.charAt(i) == '\'') &&
				string.charAt(i - 2) != '\\' && string.charAt(i - 1) == '\\') &&
				getClosingChar(string.charAt(i)) == list.get(index)) {
				
				index++;
			}
		}
		
		// Get the characters before the string.
		char before = '\0', beforeBefore = '\0';
		try {
			before = doc.getText(location - 1, 1).charAt(0);
			beforeBefore = doc.getText(location - 2, 1).charAt(0);
		} catch (Exception e) {
			// Intentionally left empty.
		}
		
		// Check the second character in the string.
		if (string.length() >= 2 &&
			!((string.charAt(1) == '"' || string.charAt(1) == '\'') &&
			string.charAt(0) == '\\' && before != '\\') &&
			index < list.size() &&
			getClosingChar(string.charAt(1)) == list.get(index)) {
			
			index++;
		}
		
		// Check the first character in the string.
		if (string.length() >= 1 &&
			!((string.charAt(0) == '"' || string.charAt(0) == '\'') &&
			before == '\\' && beforeBefore != '\\') &&
			index < list.size() &&
			getClosingChar(string.charAt(0)) == list.get(index)) {
			
			index++;
		}
	
		// Combine the string with any characters to be removed.
		String remove = string;
		
		for (int i = 0; i < index; i++) {
			remove += list.get(i);
		}
		
		removeString(remove, location, true);
	}
	
	private void tryClosingBack(String string, int location) {
		// Decides whether to pass a remove string command to the
		// removeCheckClosingBack or removeString methods.
		
		if (Defaults.INSERT_END_CHARS && highlightMgr.shouldRemove(location, string.length())){
			removeCheckClosingBack(string, location);
		} else {
			removeString(string, location, true);
		}
	}
	
	private void removeString(String s, int loc, boolean reverse) {
		// Removes a string from the document.
		
		try {
			bracketMatch.clear();
			
			editMgr.remove(s, loc, reverse);
			doc.remove(loc, s.length());

			highlightMgr.recalculate();
			highlightMgr.repaint(new Range(loc, 0));

			bracketMatch.highlight();
		} catch (Exception e) {
			ErrorMgr.show(e, "Error removing text from the document.");
		}
	}
	
	// Returns the index of the next character after any whitespace.
	private int charAfterWhitespace(int start) {
		try {
			while (start < doc.getLength()) {
				char c = doc.getText(start, 1).charAt(0);
				if (!Character.isWhitespace(c)) {
					return start;
				}
				
				start++;
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error while searching through whitespace.");
		}
		
		return -1;
	}
	
	// Checks the flag that is set to true when the center
	// mouse button is clicked on X11-based linux. This handles
	// telling the edit manager about the paste.
	public void run() {
		try {
			// This delay is required, because center click is managed
			// by X11, and is called after all other events, so 250ms
			// seems to be long enough to guarantee X11 has pasted text,
			// if it is going to.
			Thread.sleep(250);
			
			int len = doc.getLength() - centerPressedLast.get(0);
			centerPressedLast.remove(0);
			
			if (len > 0) {
				int caret = text.getCaretPosition();
				
				bracketMatch.clear();
				
				int start = caret - len;
				String newText = doc.getText(start, len);
				
				editMgr.crack();
				editMgr.add(newText, start);

				highlightMgr.recalculate();
				highlightMgr.repaint(new Range(start, newText.length()));

				bracketMatch.highlight();
				
				((DocEditor)getParent().getParent().getParent())  // Gets the DocEditor
					.recalculateLines();
				PCIE.getWindow().getTabbedPane().setActiveSaved(false);
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "Error retrieving text pasted during center click.");
		}
	}

	public static boolean isDelimiter(char c) {
		return !charArrayContains(ANTI_DELIMITERS, c);
	}
	
	public void mouseMoved(MouseEvent me) {
		showCursor();
	}
	
	private void showCursor() {
		text.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	}
	
	private void hideCursor() {
		text.setCursor(invisible);
	}
	
	public void refreshActiveLine() {
		activeLine.run();
	}

	public ActiveLine getActiveLine() {
		return activeLine;
	}
	
	public void keyReleased(KeyEvent ke) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mouseClicked(MouseEvent me) {}
}
