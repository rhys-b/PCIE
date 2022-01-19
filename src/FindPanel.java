/*
*	Author: Rhys B
*	Created: 2021-12-21
*	Modified: 2021-12-25
*
*	Handles find functionality.
*/


import java.awt.Color;
import java.awt.Image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;

import javax.imageio.ImageIO;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


@SuppressWarnings("serial")
public class FindPanel extends JPanel implements	KeyListener,
							MouseListener,
							Runnable
{
	private JTextField find;
	private JButton up, down;
	private JButton close;
	private JLabel position;
	private JTextPane textPane;
	private StyledDocument doc;
	private FindManager mgr;
	
	private ArrayList<Integer> list = new ArrayList<Integer>();
	private SimpleAttributeSet attr, reset;
	private int len;
	
	private boolean ignoreCase, active;
	
	public FindPanel() {
		// Default constructor.
		this(null, null);
	}
	
	public FindPanel(JTextPane tp, FindManager fm) {
		createGUI();
		setIgnoreCase(true);
		setTextPane(tp);
		generateStyles();
		setManager(fm);
	}
	
	public void keyPressed(KeyEvent ke) {
		if (active) {
			if (ke.getSource().equals(find)) {
				int k = ke.getKeyCode();
				
				if (k == KeyEvent.VK_UP) {
					findLast();
				} else if (k == KeyEvent.VK_DOWN) {
					findNext();
				} else if (k == KeyEvent.VK_TAB) {
					tabPressed();
				} else if (k == KeyEvent.VK_ESCAPE) {
					close();
				} else if (k == KeyEvent.VK_ENTER) {
					if (ke.isShiftDown()) {
						findLast();
					} else {
						findNext();
					}
				} else {
					SwingUtilities.invokeLater(this);
				}
			} else {
				SwingUtilities.invokeLater(this);
			}
		}
	}
	
	public void mousePressed(MouseEvent me) {
		setCountString(Integer.toString(list.size()));
	}
	
	public void run() {
		calculate();
	}
	
	public void display() {
		active = true;
		
		String selection = textPane.getSelectedText();
		if (selection != null) {
			find.setText(selection);
		}
		
		setVisible(true);
		find.requestFocus();
		
		if (!find.getText().equals("")) {
			calculate();
			
			// The shinanigans you're witnessing definately need a
			// detailed comment explaining what is going on.
			// The if statement is self-explainatory: this only
			// activates if the user has selected some text, which
			// hints that this is to select the text that the user
			// has already selected. This is neccessary because
			// the JTextPane does not want to show selected text
			// if it is not active, as in when the find panel is
			// being used to find text. Therefore, a solution is
			// needed to re-select(or at least re-show) the text
			// that the user selected. The reason there are two
			// invokeLater calls is because of the way Swing works.
			// Long story short, my understanding is that Swing
			// waits until a method that makes changes returns in
			// order to repaint, presumably for efficency. But doing
			// this causes the order to be incorrect, so the
			// solution is to use invokeLater once to wait for a
			// repaint and then again to make sure it happens after
			// everything else repaints. It sounds like it's about
			// time to switch to GTK.
			if (selection != null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								textPane
								.setCaretPosition(
								textPane.getSelectionStart());
								
								findNext();
							}
						});
					}
				});
			}
		}
	}
	
	public void findNext() {
		// This will throw a NullPointerException if the JTextPane is not set.
		int i = 0;
		if (list.size() > 0) {
			int caret = textPane.getSelectionEnd();
			
			while (list.get(i) < caret) {
				i++;
				
				if (i >= list.size()) {
					i = 0;
					break;
				}
			}
			
			select(list.get(i));
		}
		
		setCountString((i + 1) + "/" + list.size());
	}
	
	public void findLast() {
		// This will throw a NullPointerException if the JTextPane is not set.
		if (list.size() > 0) {
			int caret = textPane.getSelectionStart();
			
			int i = list.size() - 1;
			while (list.get(i) >= caret) {
				i--;
				
				if (i < 0) {
					i = list.size() - 1;
					break;
				}
			}
			
			select(list.get(i));
			setCountString((i + 1) + "/" + list.size());
		} else {
			setCountString("0/0");
		}
	}
	
	private void select(int start) {
		textPane.setCaretPosition(start);
		textPane.moveCaretPosition(start + len);
		
		showSelection();
	}
	
	private void showSelection() {
		textPane.getCaret().setSelectionVisible(true);
	}
	
	public void close() {
		reset();
		mgr.update(this);
		closePressed();
		active = false;
	}
	
	public void closePressed() {
		setVisible(false);
	}
	
	public void tabPressed() {
		find.setText(find.getText() + "\\t");
	}
	
	public void setIgnoreCase(boolean b) {
		ignoreCase = b;
	}
	
	public int[] getIndexes() {
		int[] out = new int[list.size()];
		for (int i = 0; i < out.length; i++) {
			out[i] = list.get(i);
		}
		
		return out;
	}
	
	public void setText(String s) {
		find.setText(s);
	}
	
	public String getText() {
		return find.getText();
	}
	
	public void setCountString(String s) {
		position.setText(s);
	}
	
	public String getCountString() {
		return position.getText();
	}
	
	public void setTextPane(JTextPane tp) {
		if (textPane != null) {
			textPane.removeKeyListener(this);
			textPane.removeMouseListener(this);
		}
		
		textPane = tp;
		
		if (textPane != null) {
			textPane.addKeyListener(this);
			textPane.addMouseListener(this);
			doc = textPane.getStyledDocument();
		} else {
			doc = null;
		}
	}
	
	public void setManager(FindManager fm) {
		mgr = fm;
	}
	
	public void calculate() {
		reset();
		
		String needle = expand(find.getText());
		len = needle.length();
		list.clear();
		
		if (len > 0) {
			String haystack = getSafeText();
			for (int i = 0; i < haystack.length() - len + 1; i++) {
				if ((	ignoreCase &&
					haystack.substring(i, i + len).equalsIgnoreCase(needle)) ||
					(!ignoreCase && haystack.substring(i, i + len).equals(needle)))
				{
					list.add(i);
					doc.setCharacterAttributes(i, len, attr, false);
				}
			}
		
			setCountString(Integer.toString(list.size()));
		} else {
			setCountString("0");
		}
	}
	
	private void reset() {
		for (int i = 0; i < list.size(); i++) {
			doc.setCharacterAttributes(list.get(i), len, reset, false);
		}
	}
	
	private String getSafeText() {
		try {
			return doc.getText(0, doc.getLength());
		} catch (Exception e) {
			ErrorMgr.show(e, "Error getting text to find.");
			return null;
		}
	}
	
	private void createGUI() {
		find = new JTextField();
		find.addKeyListener(this);
		find.setFocusTraversalKeysEnabled(false);
		
		try {
			up = new JButton(new ImageIcon(ImageIO.read(FindPanel.class
				.getResourceAsStream("data/find_up.png"))
				.getScaledInstance(30, 15, Image.SCALE_SMOOTH)));
			down = new JButton(new ImageIcon(ImageIO.read(FindPanel.class
				.getResourceAsStream("data/find_down.png"))
				.getScaledInstance(30, 15, Image.SCALE_SMOOTH)));
			
			close = new JButton(new ImageIcon(ImageIO.read(FindPanel.class
				.getResourceAsStream("data/find_close_x.png"))
				.getScaledInstance(20, 30, Image.SCALE_SMOOTH)));
		} catch (Exception e) {
			up = new JButton("\u2191");
			down = new JButton("\u2193");
			close = new JButton("X");
		}
		
		position = new JLabel("/");
		position.setHorizontalAlignment(SwingConstants.CENTER);
		
		GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(find, 50, 100, 300)
			.addComponent(position)
			.addGroup(layout.createParallelGroup()
				.addComponent(up, 30, 30, 30)
				.addComponent(down, 30, 30, 30))
			.addComponent(close, 20, 20, 20)
		);
		layout.setVerticalGroup(layout.createParallelGroup()
			.addComponent(find, 30, 30, 30)
			.addComponent(position, 30, 30, 30)
			.addGroup(layout.createSequentialGroup()
				.addComponent(up, 15, 15, 15)
				.addComponent(down, 15, 15, 15))
			.addComponent(close, 30, 30, 30)
		);
		setLayout(layout);
		
		up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				findLast();
			}
		});
		
		down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				findNext();
			}
		});
		
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				close();
			}
		});
	}
	
	private void generateStyles() {
		attr = new SimpleAttributeSet();
		reset = new SimpleAttributeSet();
		
		Color color = new Color(214, 101, 157, 100);
		Color clear = new Color(0, 0, 0, 0);
		
		StyleConstants.setBackground(attr, color);
		StyleConstants.setBackground(reset, clear);
	}
	
	public static String expand(String s) {
		return s.replace((CharSequence)"\\t", "	")
				.replace((CharSequence)"\\n", "\n");
	}
	
	@Override
	public void requestFocus() {
		find.requestFocus();
	}
	
	public void keyTyped(KeyEvent ke) {}
	public void keyReleased(KeyEvent ke) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mouseReleased(MouseEvent me) {}
	public void mouseClicked(MouseEvent me) {}
}