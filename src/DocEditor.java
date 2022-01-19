/*
*	Author: Rhys B.
*	Created: 2021-08-20
*	Modified: 2022-01-01
*
*	Contains the find manager and scrollbar, which also has the line numbers on the side.
*/


import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;

import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import java.io.File;
import java.io.IOException;


@SuppressWarnings("serial")
public class DocEditor extends JPanel implements ComponentListener {
	private final int SCROLL_SPEED = 30;

	private EditorPane editor = new EditorPane();
	private JScrollPane scroll = new JScrollPane(editor);
	private FindManager findMgr = new FindManager(this, editor, editor.getHighlightManager());
	private LineNumbers lines = new LineNumbers(editor, scroll);

	public DocEditor(File file) throws Exception {
		scroll.setRowHeaderView(lines);
		scroll.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
		scroll.getHorizontalScrollBar().setUnitIncrement(SCROLL_SPEED);

		if (file != null && !editor.open(file)) {
			throw new IOException("File " + file + " could not be opened.");
		} else {
			lines.recalculateLines();
		}
		
		addComponentListener(this);
		
		setLayout(null);
		add(scroll);
	}

	public void save() {
		editor.save();
	}

	public void saveAs(File file) {
		editor.saveAs(file);	
	}

	public boolean inUse() {
		return editor.inUse();
	}

	@Override
	public void requestFocus() {
		editor.requestFocus();
	}

	@Override
	public void addKeyListener(KeyListener kl) {
		editor.addKeyListener(kl);
	}

	public boolean hasFile() {
		return editor.hasFile();
	}

	public boolean cut() {
		boolean b = editor.cut();
		lines.recalculateLines();
		return b;
	}

	public boolean paste() {
		boolean b = editor.paste();
		lines.recalculateLines();
		return b;
	}

	public boolean copy() {
		return editor.copy();
	}

	public boolean undo() {
		boolean b = editor.undo();
		lines.recalculateLines();
		return b;
	}

	public boolean redo() {
		boolean b = editor.redo();
		lines.recalculateLines();
		return b;
	}

	public String getSelectedText() {
		return editor.getSelectedText();
	}

	public String getText() {
		return editor.getText();
	}

	public File getFile() {
		return editor.getFile();
	}
	
	public void find() {
		findMgr.find();
	}
	
	public void replace() {
		findMgr.replace();
	}
	
	public boolean containsText() {
		return editor.containsText();
	}
	
	public void formatIndentation() {
		editor.formatIndentation();
	}
	
	public void findLine() {
		findMgr.findLine();
	}

	public FindManager getFindManager() {
		return findMgr;
	}
	
	@Override
	public boolean isOptimizedDrawingEnabled() {
		return false;
	}
	
	public void componentResized(ComponentEvent ce) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				scroll.setBounds(0, 0, getWidth(), getHeight());
				revalidate();
			}
		});
	}
	
	public void recalculateLines() {
		lines.recalculateLines();
		
		// This is required because the scrollpane only revalidates
		// the main viewport when revalidate() is called on it,
		// whereas here the headers must also be revalidated.
		scroll.getVerticalScrollBar().setValue(
			scroll.getVerticalScrollBar().getValue() + 1);
	}
	
	public void updateOptions() {
		editor.updateOptions();
		findMgr.updateOptions();
		lines.updateOptions();
	}
	
	public EditorPane getEditor() {
		return editor;
	}

	public void componentMoved(ComponentEvent ce) {}
	public void componentShown(ComponentEvent ce) {}
	public void componentHidden(ComponentEvent ce) {}
}
