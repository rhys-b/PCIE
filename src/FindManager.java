/*
*	Author: Rhys B.
*	Created: 2021-09-16
*	Modified: 2021-12-23
*
*	Handles the components and algorithms associated with
*	and find & replace functionality. Find functionality
*	is in the FindPanel class.
*/


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.awt.Image;

import javax.imageio.ImageIO;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.text.StyledDocument;


public class FindManager implements ComponentListener, KeyListener {
	private JComponent parent;
	
	private JTextPane text;
	private EditorPane editor;
	private FindPanel findPanel, replaceFind;
	private JPanel replacePanel;
	private EditManager editMgr;
	private StyledDocument doc;
	private HighlightManager highlightMgr;
	private LineFinder linefinder;
	
	private JTextField replaceText;
	private JCheckBox ignoreCase;
	
	public FindManager(JComponent parent, EditorPane editor, HighlightManager highlightMgr) {
		this.parent = parent;
		this.editor = editor;
		this.highlightMgr = highlightMgr;
		
		text = editor.getTextPane();
		editMgr = editor.getEditManager();
		doc = text.getStyledDocument();
		
		text.addKeyListener(this);
		
		findPanel = new FindPanel(editor.getTextPane(), this);
		findPanel.setVisible(false);
		
		replaceText = new JTextField();
		replaceText.setFocusTraversalKeysEnabled(false);
		
		replacePanel = new JPanel();
		replaceFind = new FindPanel(editor.getTextPane(), this) {
			@Override
			public void closePressed() {
				replacePanel.setVisible(false);
			}
			
			@Override
			public void tabPressed() {
				replaceText.requestFocus();
			}
		};
		
		parent.addComponentListener(this);
		parent.add(findPanel);
		
		JLabel findLabel = new JLabel("Find");
		findLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		JLabel replaceLabel = new JLabel("Replace");
		replaceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JButton replaceUp, replaceDown;
		try {
			replaceUp = new JButton(new ImageIcon(ImageIO.read(FindManager.class
				.getResourceAsStream("data/find_up.png"))
				.getScaledInstance(30, 15, Image.SCALE_SMOOTH)));
			replaceDown = new JButton(new ImageIcon(ImageIO.read(FindManager.class
				.getResourceAsStream("data/find_down.png"))
				.getScaledInstance(30, 15, Image.SCALE_SMOOTH)));
		} catch (Exception e) {
			replaceUp = new JButton("\u2191");
			replaceDown = new JButton("\u2193");
		}
		
		ignoreCase = new JCheckBox("Ignore Case", Defaults.IGNORE_CASE);
		JButton replaceAll = new JButton("Replace All");
		
		// Create and populate the layout.
		GroupLayout replaceLayout = new GroupLayout(replacePanel);
		replaceLayout.setAutoCreateContainerGaps(true);
		replaceLayout.setHorizontalGroup(replaceLayout.createParallelGroup()
			.addGroup(replaceLayout.createSequentialGroup()
				.addComponent(findLabel, 60, 60, 60)
				.addGap(5, 5, 5)
				.addComponent(replaceFind, 0, 300, Integer.MAX_VALUE)
			)
			.addGroup(replaceLayout.createSequentialGroup()
				.addComponent(replaceLabel, 60, 60, 60)
				.addGap(5, 5, 5)
				.addComponent(replaceText, 0, 100, Integer.MAX_VALUE)
				.addGroup(replaceLayout.createParallelGroup()
					.addComponent(replaceUp, 30, 30, 30)
					.addComponent(replaceDown, 30, 30, 30)
				)
			)
			.addGroup(replaceLayout.createSequentialGroup()
				.addComponent(ignoreCase, 0, 100, Integer.MAX_VALUE)
				.addComponent(replaceAll, 0, 100, Integer.MAX_VALUE)
			)
		);
		replaceLayout.setVerticalGroup(replaceLayout.createSequentialGroup()
			.addGroup(replaceLayout.createParallelGroup()
				.addComponent(findLabel, 30, 30, 30)
				.addComponent(replaceFind, 30, 30, 30)
			)
			.addGap(5, 5, 5)
			.addGroup(replaceLayout.createParallelGroup()
				.addComponent(replaceLabel, 28, 30, 32)
				.addComponent(replaceText, 28, 30, 32)
				.addGroup(replaceLayout.createSequentialGroup()
					.addComponent(replaceUp, 14, 15, 16)
					.addComponent(replaceDown, 14, 15, 16)
				)
			)
			.addGap(5, 5, 5)
			.addGroup(replaceLayout.createParallelGroup()
				.addComponent(ignoreCase, 0, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE)
				.addComponent(replaceAll, 0, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE)
			)
		);
		
		KeyAdapter closeListener = new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {
				if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
					replaceFind.close();
				}
			}
		};
		replaceUp.addKeyListener(closeListener);
		replaceDown.addKeyListener(closeListener);
		replaceText.addKeyListener(closeListener);
		ignoreCase.addKeyListener(closeListener);
		replaceAll.addKeyListener(closeListener);
		
		replacePanel.setLayout(replaceLayout);
		replacePanel.setVisible(false);
		parent.add(replacePanel);
		
		replaceUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				replaceSelection(true);
			}
		});
		
		replaceDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				replaceSelection(false);
			}
		});
		
		// The keys for the replace panel work like this:
		// shift means go to the last, default is next;
		// control means find(don't replace); default is replace;
		replaceText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {
				if (ke.getKeyCode() == KeyEvent.VK_TAB) {
					replaceFind.requestFocus();
				} else if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
					if (ke.isControlDown()) {
						if (ke.isShiftDown()) {
							replaceFind.findLast();
						} else {
							replaceFind.findNext();
						}
					} else {
						if (ke.isShiftDown()) {
							replaceSelection(true);
						} else {
							replaceSelection(false);
						}
					}
				}
			}
		});
		
		ignoreCase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				replaceFind.setIgnoreCase(ignoreCase.isSelected());
				replaceFind.calculate();
			}
		});
		
		replaceAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int[] indexes = replaceFind.getIndexes();
				
				if (indexes.length > 0) {
					int findStringLen = replaceFind.getText().length();
					String replaceString = FindPanel.expand(replaceText.getText());
					String[] oldStrings = new String[indexes.length];
					
					try {
						// This is done backwards so that the indexes
						// line up properly, even after some of them
						// have been changed.
						
						for (int i = indexes.length - 1; i >= 0; i--) {
							oldStrings[i] = doc.getText(indexes[i], findStringLen);
							doc.remove(indexes[i], findStringLen);
							doc.insertString(indexes[i], replaceString, null);
						}
						
						editMgr.replaceAll(indexes, oldStrings, replaceString);

						highlightMgr.recalculate();
						highlightMgr.repaint();
					} catch (Exception e) {
						ErrorMgr.show(e, "Error replacing all text.");
					}
					
					PCIE.getWindow().getTabbedPane().setActiveSaved(false);
					replaceFind.setCountString("0/0");
				}
			}
		});
		
		linefinder = new LineFinder(editor, parent);
	}
	
	public void updateOptions() {
		ignoreCase.setSelected(Defaults.IGNORE_CASE);
	}
	
	// Replaces the current selection if it is part of a find,
	// and then moves to either the next or last index to find.
	public void replaceSelection(boolean last) {
		String selectedText = text.getSelectedText();
		
		if (selectedText != null) {
			String replaceString = FindPanel.expand(replaceText.getText());
			
			int start = text.getSelectionStart();
			editMgr.replace(selectedText, replaceString, start);
			
			text.replaceSelection(replaceString);

			highlightMgr.recalculate();
			highlightMgr.repaint(new Range(start, replaceString.length()));
				
			PCIE.getWindow().getTabbedPane().setActiveSaved(false);
			PCIE.getWindow().getTabbedPane().getActiveComponent().recalculateLines();
		}
		
		replaceFind.calculate();
		
		if (last) {
			replaceFind.findLast();
		} else {
			replaceFind.findNext();
		}
	}
	
	// Called when the user clicks 'find'.
	public void find() {
		linefinder.close();
		
		if (replacePanel.isVisible()) {
			replaceFind.close();
		}
		
		findPanel.display();
	}
	
	// Called when the user clicks 'find & replace'.
	public void replace() {
		findPanel.close();
		linefinder.close();
		
		replacePanel.setVisible(true);
		replaceFind.display();
	}
	
	public void findLine() {
		findPanel.close();
		
		if (replacePanel.isVisible()) {
			replaceFind.close();
		}
		
		linefinder.display();
	}
	
	// Use setBounds to make sure the child components are in the right place.
	public void componentResized(ComponentEvent ce) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int width = parent.getWidth();
				
				findPanel.setBounds(width - 320, 5, 270, 30);
				replacePanel.setBounds(width - 365, 5, 350, 100);
			}
		});
	}
	
	public void keyTyped(KeyEvent ke) {
		int k = (int)ke.getKeyChar();
		
		if (k == KeyEvent.VK_ESCAPE) {
			findPanel.close();
			replaceFind.close();
		}
	}
	
	// Set both find panels to the state of the given panel.
	public void update(FindPanel source) {
		if (source == findPanel) {
			replaceFind.setText(findPanel.getText());
			replaceFind.setCountString(findPanel.getCountString());
		} else if (source == replaceFind) {
			findPanel.setText(replaceFind.getText());
			findPanel.setCountString(replaceFind.getCountString());
		}
	}

	public LineFinder getLineFinder() {
		return linefinder;
	}
	
	
	// Residue of implementing ComponentListener and KeyListener.
	public void componentMoved(ComponentEvent ce) {}
	public void componentHidden(ComponentEvent ce) {}
	public void componentShown(ComponentEvent ce) {}
	public void keyPressed(KeyEvent ke) {}
	public void keyReleased(KeyEvent ke) {}
}
