/*
*	Author: Rhys B.
*	Created: 2021-08-30
*	Modfied: 2021-12-24
*
*	Handles the undo and redo for a document.
*/


import java.util.ArrayList;

import javax.swing.JTextPane;

import javax.swing.text.StyledDocument;


public class EditManager {
	private enum Action {
		NONE,
		ADD,
		REMOVE,
		REPLACE,
		REPLACE_ALL
	}

	private class Edit {
		private String data, datb;
		private int offset, caretOverride;
		private Action action;
		
		// Only used for replace all.
		private int[] offsets;
		private String[] olds;

		private Edit() {
			action = Action.NONE;
			caretOverride = -1;
		}
		
		@Override
		public String toString() {
			return action + " at " + offset + " data: '" + data + "' datb: '" + datb + "'";
		}
	}

	private ArrayList<Edit> list = new ArrayList<Edit>();
	private JTextPane text;
	private StyledDocument doc;
	private int index;
	private HighlightManager highlightMgr;

	public EditManager(JTextPane text, HighlightManager highlightMgr) {
		this.text = text;
		doc = text.getStyledDocument();
		
		this.highlightMgr = highlightMgr;

		list.add(new Edit());
	}

	public boolean undo() {
		if (index < list.size()) {
			try {
				Edit e = list.get(index);

				if (e.action == Action.ADD) {
					doc.remove(e.offset, e.data.length());

					highlightMgr.recalculate();
					highlightMgr.repaint(new Range(e.offset, 0));
					
					text.setCaretPosition(e.offset);
				} else if (e.action == Action.REMOVE) {
					doc.insertString(e.offset, e.data, null);

					highlightMgr.recalculate();
					highlightMgr.repaint(new Range(e.offset, e.data.length()));
					
					text.setCaretPosition(e.offset + e.data.length());
				} else if (e.action == Action.REPLACE) {
					doc.remove(e.offset, e.data.length());
					doc.insertString(e.offset, e.datb, null);
					
					highlightMgr.recalculate();
					highlightMgr.repaint(new Range(e.offset, e.datb.length()));
					
					text.setCaretPosition(e.offset + e.datb.length());
				} else if (e.action == Action.REPLACE_ALL) {
					int initialCaret = text.getCaretPosition();
					int offset = 0;
					
					for (int i = 0; i < e.offsets.length; i++) {
						doc.remove(e.offsets[i], e.data.length());
						doc.insertString(e.offsets[i], e.olds[i], null);
						
						if (e.offsets[i] < initialCaret) {
							offset += (e.olds[i].length() - e.data.length());
						}
					}

					highlightMgr.recalculate();
					highlightMgr.repaint();
					
					text.setCaretPosition(initialCaret + offset);
				}

				index++;
				return true;
			} catch (Exception e) {
				ErrorMgr.show(e, "Something went wrong trying to undo.");
			}
		}

		return false;
	}

	public boolean redo() {
		if (index > 0 && list.get(index - 1).action != Action.NONE) {
			try {
				index--;

				Edit e = list.get(index);
				if (e.action == Action.ADD) {
					doc.insertString(e.offset, e.data, null);
					highlightMgr.recalculate();
					highlightMgr.repaint(new Range(e.offset, e.data.length()));
					
					if (e.caretOverride != -1) {
						text.setCaretPosition(e.caretOverride);
					} else {
						text.setCaretPosition(e.offset + e.data.length());
					}
				} else if (e.action == Action.REMOVE) {
					doc.remove(e.offset, e.data.length());
					highlightMgr.recalculate();
					highlightMgr.repaint(new Range(e.offset, 0));
					
					text.setCaretPosition(e.offset);
				} else if (e.action == Action.REPLACE) {
					doc.remove(e.offset, e.datb.length());
					doc.insertString(e.offset, e.data, null);
					
					highlightMgr.recalculate();
					highlightMgr.repaint(new Range(e.offset, e.data.length()));
					
					if (e.caretOverride != -1) {
						text.setCaretPosition(e.caretOverride);
					} else {
						text.setCaretPosition(e.offset + e.data.length());
					}
				} else if (e.action == Action.REPLACE_ALL) {
					// This is done backwards so all the indexes line
					// up properly, even after text has been changed.
					
					int initialCaret = text.getCaretPosition();
					int offset = 0;
					
					for (int i = e.offsets.length - 1; i >= 0; i--) {
						doc.remove(e.offsets[i], e.olds[i].length());
						doc.insertString(e.offsets[i], e.data, null);
						
						if (e.offsets[i] < initialCaret) {
							offset += (e.data.length() - e.olds[i].length());
						}
					}
					
					highlightMgr.recalculate();
					highlightMgr.repaint();
					
					text.setCaretPosition(initialCaret + offset);
				}
				
				return true;
			} catch (Exception e) {
				ErrorMgr.show(e, "Something went wrong trying to redo.");
			}
		}

		return false;
	}
	
	public void add(String string, int offset) {
		add(string, offset, -1);
	}

	public void add(String string, int offset, int caret) {
		pop();

		if (!(list.get(0).action == Action.ADD || list.get(0).action == Action.REPLACE)) {
			crack();
		}

		if (list.get(0).action == Action.NONE) {
			list.get(0).action = Action.ADD;
			list.get(0).data = string;
			list.get(0).offset = offset;
			list.get(0).caretOverride = caret;
		} else {
			list.get(0).data += string;
			
			if (caret != -1) {
				list.get(0).caretOverride = caret;
			}
		}
		index = 0;
	}

	public void remove(String string, int offset, boolean insertBefore) {
		pop();

		if (list.get(0).action != Action.REMOVE) {
			crack();
		}

		if (list.get(0).action == Action.NONE) {
			list.get(0).action = Action.REMOVE;
			list.get(0).data = string;
		} else {
			if (insertBefore) {
				list.get(0).data = string + list.get(0).data;
			} else {
				list.get(0).data += string;
			}
		}

		if (offset != -1) {
			list.get(0).offset = offset;
		}

		index = 0;
	}

	public void replace(String old, String replace, int offset) {
		replace(old, replace, offset, -1);
	}
	
	public void replace(String old, String replace, int offset, int caretOverride) {
		pop();
		crack();
		
		list.get(0).action = Action.REPLACE;
		list.get(0).offset = offset;
		list.get(0).datb = old;
		list.get(0).data = replace;
		list.get(0).caretOverride = caretOverride;
		
		index = 0;
	}
	
	// Replaces all the given instances 'indexes' of String 'string' with String 'replace'.
	public void replaceAll(int[] indexes, String[] olds, String replace) {
		pop();
		crack();
		
		list.get(0).action = Action.REPLACE_ALL;
		list.get(0).offsets = indexes;
		list.get(0).olds = olds;
		list.get(0).data = replace;
		
		index = 0;
	}

	public void crack() {
		if (list.get(0).action != Action.NONE) {
			push();
		}
	}

	public void pop() {
		if (index != 0) {
			for (; index > 0; index--) {
				list.remove(0);
			}

			push();
		}
	}

	public void push() {
		list.add(0, new Edit());
		index++;
	}
}
