/*
*	Author: Rhys B.
*	Created: 2021-08-30
*	Modified: 2021-12-31
*
*	A custom tabbed pane that allows for dragging
*	of tabs into a new order.
*/


import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import java.io.File;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class TabbedPane extends JPanel {
	private TabBar bar = new TabBar(this);
	private ArrayList<DocEditor> editors = new ArrayList<DocEditor>();
	private ArrayList<File> closed = new ArrayList<File>();
	private DocEditor activeComponent = null;

	public TabbedPane() {
		setLayout(new BorderLayout());
		add(bar, BorderLayout.NORTH);
	}

	// Called when the user opens either a new file, or a new tab.
	// If a new tab, then File f is null. forceNewTab says whether
	// a new tab should be created unconditionally, or if the method
	// should decide if a new one is needed.
	public void open(File f, boolean forceNewTab) {
		try {
			DocEditor editor = new DocEditor(f);

			int active = bar.getActive();

			if (editors.size() > 0) {
				if (editors.get(active).inUse() || forceNewTab) {
					addLast(editor, f);
				} else {
					editors.set(active, editor);
					bar.setFile(active, f);
					bar.setActive(active);
				}
			} else {
				addLast(editor, f);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Intentionally empty.
		}
	}

	public void setActiveIndex(int i) {
		bar.setActive(i);
	}

	// Adds an editor to the list, and the corresponding filename to the bar.
	private void addLast(DocEditor e, File f) {
		editors.add(e);
		bar.addFile(f);
	}

	public void removeFile(int index) {
		if (editors.get(index).getFile() != null) {
			closed.remove(editors.get(index).getFile());
			closed.add(0, editors.get(index).getFile());
		}

		editors.remove(index);
		bar.removeFile(index);
		
		int position = getPosition();
		if (position == index) {
			if (editors.size() == index) {
				bar.setActive(position - 1);
			} else {
				bar.setActive(position);
			}
		} else if (position > index) {
			setPosition(position - 1); // NOT bar.setActive
		} else {
			bar.setActive(position);
		}
	}

	// Reopens the previously closed tabs.
	public void reopen() {
		if (closed.size() > 0) {
			open(closed.get(0), false);
			closed.remove(0);
		}
	}

	public void removeActive() {
		removeFile(bar.getActive());
	}

	public void moveTo(int from, int to) {
		DocEditor buffer = editors.get(from);
		int i;
		if (to > from) {
			for (i = from; i < to; i++) {
				editors.set(i, editors.get(i + 1));
			}
		} else {
			for (i = from; i > to; i--) {
				editors.set(i, editors.get(i - 1));
			}
		}

		editors.set(i, buffer);
	}

	public void setPosition(int position) {
		if (position >= 0 && position < editors.size()) {
			if (activeComponent != null) {
				remove(activeComponent);
			}

			activeComponent = editors.get(position);
			add(activeComponent);
			activeComponent.requestFocus();

			revalidate();
			repaint();
		}
	}
	
	public int getPosition() {
		return bar.getActive();
	}

	public DocEditor getActiveComponent() {
		return editors.get(bar.getActive());
	}

	public void save() {
		save(getPosition());
	}
	
	public void save(int index) {
		getComponentAt(index).save();
		setSavedAt(index, true);
	}

	public void saveAs(File file) {
		getActiveComponent().saveAs(file);
		bar.setFile(bar.getActive(), file);
		setActiveSaved(true);
	}

	public void copy() {
		getActiveComponent().copy();
	}

	public void cut() {
		if (getActiveComponent().cut()) {
			setActiveSaved(false);
		}
	}

	public void paste() {
		if (getActiveComponent().paste()) {
			setActiveSaved(false);
		}
	}

	public void undo() {
		if (getActiveComponent().undo()) {
			setActiveSaved(false);
		}
	}

	public void redo() {
		if (getActiveComponent().redo()) {
			setActiveSaved(false);
		}
	}

	public void setActiveSaved(boolean saved) {
		bar.setSavedAt(getPosition(), saved);
	}
	
	public void setSavedAt(int index, boolean saved) {
		bar.setSavedAt(index, saved);
	}
	
	public boolean getActiveSaved() {
		return bar.getActiveSaved();
	}
	
	public void find() {
		getActiveComponent().find();
	}

	public void replace() {
		getActiveComponent().replace();
	}
	
	// Moves to the right tab, wrapping around if necessary.
	public void next() {
		int index = bar.getActive();
		
		if (index + 1 >= editors.size()) {
			index = 0;
		} else {
			index++;
		}
		
		bar.setActive(index);
	}
	
	// Moves to the left tab, wrapping around if necessary.
	public void last() {
		int index = bar.getActive();
		
		if (index - 1 < 0) {
			index = editors.size() - 1;
		} else {
			index--;
		}
		
		bar.setActive(index);
	}
	
	public String getActiveTitle() {
		return bar.getActiveTitle();
	}
	
	public int getTabCount() {
		return editors.size();
	}
	
	public boolean getSaved(int index) {
		return bar.getSavedAt(index);
	}
	
	public String getTitle(int index) {
		return bar.getTitleAt(index);
	}
	
	public DocEditor getComponentAt(int index) {
		return editors.get(index);
	}
	
	public void formatIndentation() {
		getActiveComponent().formatIndentation();
	}
	
	public void findLine() {
		getActiveComponent().findLine();
	}
	
	public void updateOptions() {
		for (int i = 0; i < editors.size(); i++) {
			editors.get(i).updateOptions();
		}
	}

	public void saveKnown() {
		for (int i = 0; i < editors.size(); i++) {
			if (editors.get(i).hasFile()) {
				save(i);
			}
		}
	}

	public DocEditor getComponentWithName(String s) {
		int index = bar.getIndexOfName(s);
		if (index != -1) {
			return editors.get(index);
		}

		return null;
	}

	public int getIndexOfTitle(String s) {
		return bar.getIndexOfName(s);
	}
}
