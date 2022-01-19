/*
*	Author: Rhys B.
*	Created: 2021-08-30
*	Modified: 2021-11-03
*
*	The tab bar portion of the tabbed pane, responsible for
*	rendering each tab.
*/


import javax.swing.JComponent;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;

import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.ArrayList;
import java.util.Arrays;

import java.io.File;


@SuppressWarnings("serial")
public class TabBar extends JComponent implements MouseMotionListener,MouseListener,MouseWheelListener{
	private final Color TAB_COLOR = new Color(125, 125, 125);
	private final Color ACTIVE_TAB_COLOR = new Color(187, 187, 187);
	private final Color TEXT_COLOR = Color.BLACK;
	private final Color BUTTON_COLOR = new Color(220, 220, 220);
	private final Color X_COLOR = Color.BLACK;
	private final Color ADD_X_COLOR = new Color(125, 125, 125);

	private final BasicStroke ADD_X_STROKE =
		new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private final BasicStroke INSERT_STROKE =
		new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	private final int TAB_WIDTH = 150, TAB_HEIGHT = 20, TEXT_OFFSET = 10;
	private final int CLOSE_SIZE = 30, BUTTON_SIZE = 15, X_SIZE = 8;
	private final int ADD_WIDTH = 30, ADD_BUTTON_SIZE = 15, ADD_X_SIZE = 8;
	private final int SCROLL_SPEED = 30, DRAG_SCROLL = 20, DRAG_SCROLL_SPEED = 10;
	private final int UNSAVED_POSITION = 2, UNSAVED_SIZE = 7;
	private final int ANIMATION_SPEED = 25;

	private final Font font;
	private final Color[] FADE = {new Color(0, 0, 0, 64),
		new Color(0, 0, 0, 127), new Color(0, 0, 0, 191)};

	private TabbedPane parent;

	private ArrayList<TabInfo> info = new ArrayList<TabInfo>();
	private int mouseX, mouseY;
	private int scrollOffset = 0;
	private int active = -1;
	private boolean addActive = false;
	private int removeActive = -1;
	private int dragging = -1, dragOffset, originalDrag;
	private int closing = -1, opening = -1;

	public TabBar(TabbedPane parent) {
		this.parent = parent;
		setPreferredSize(new Dimension(0, TAB_HEIGHT));

		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);

		String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
									.getAvailableFontFamilyNames();
		String fontName;
		if (Arrays.binarySearch(fonts, "Arial") != -1) {
			fontName = "Arial";
		} else {
			fontName = "DejaVu Sans";
		}

		font = new Font(fontName, Font.BOLD, 16);
	}

	public void paintComponent(Graphics g) {
		if (info.size() > 0) {
			Graphics2D gg = (Graphics2D) g;

			gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
				
			gg.setFont(font);

			if (opening == -1) {
				int scrollDiff = info.get(info.size() - 1).position +
					TAB_WIDTH - getWidth() + ADD_WIDTH;
					
				if (scrollDiff < 0) {
					scrollOffset = 0;
				} else if (scrollOffset > scrollDiff) {
					scrollOffset = scrollDiff;
				}
			}

			int middle = TAB_HEIGHT / 2;
			removeActive = -1;

			if (opening != -1) {
				drawTab(gg, middle, opening);

				TabInfo infoBuff = info.get(opening);
				if (infoBuff.position == infoBuff.preferredPosition) {
					opening = -1;
				}
			}

			if (closing != -1) {
				drawTab(gg, middle, closing);

				TabInfo infoBuff = info.get(closing);
				if (infoBuff.position == infoBuff.preferredPosition) {
					if (closing < active) {
						active--;
					}

					info.remove(closing);
					closing = -1;
				}
			}

			for (int i = 0; i < info.size(); i++) {
				if (dragging != i && closing != i && opening != i) {
					drawTab(gg, middle, i);
				}
			}

			int addX;
			if (info.get(info.size() - 1).position + TAB_WIDTH > getWidth() - ADD_WIDTH) {
				addX = getWidth() - ADD_WIDTH / 2;
			} else {
				addX = info.get(info.size() - 1).position + TAB_WIDTH + ADD_WIDTH / 2;
			}

			if (dragging != -1) {
				info.get(dragging).position = mouseX - dragOffset;
				drawTab(gg, middle, dragging);

				int totalWidth = info.size() * TAB_WIDTH;
				if (totalWidth > getWidth() - ADD_WIDTH) {
					int upperBound = totalWidth - getWidth() + ADD_WIDTH;
					if (scrollOffset < upperBound && mouseX >
						getWidth() - ADD_WIDTH - DRAG_SCROLL) {
							
						if (scrollOffset + DRAG_SCROLL_SPEED > upperBound) {
							dragOffset -= scrollOffset - upperBound;
							scrollOffset = upperBound;
						} else {
							scrollOffset += DRAG_SCROLL_SPEED;
							dragOffset -= DRAG_SCROLL_SPEED;
						}
					} else if (scrollOffset > 0 && mouseX < DRAG_SCROLL) {
						if (scrollOffset - DRAG_SCROLL_SPEED < 0) {
							dragOffset += scrollOffset;
							scrollOffset = 0;
						} else {
							scrollOffset -= DRAG_SCROLL_SPEED;
							dragOffset += DRAG_SCROLL_SPEED;
						}
					}

					dragSwap();

					try {
						Thread.sleep(1000/60);
					} catch (Exception e) {
						ErrorMgr.show(e, "TabBar animation sleep exception.");
					}

					repaint();
				}
			}

			gg.setColor(getBackground());
			gg.fillOval(addX - ADD_WIDTH / 2, middle - ADD_WIDTH / 2, ADD_WIDTH,ADD_WIDTH);
			gg.fillRect(addX, 0, getWidth(), TAB_HEIGHT);

			if (distance(mouseX, mouseY, addX, middle) < ADD_BUTTON_SIZE / 2) {
				gg.setColor(ACTIVE_TAB_COLOR);
				gg.fillOval(addX - ADD_BUTTON_SIZE / 2, middle - ADD_BUTTON_SIZE / 2,
					ADD_BUTTON_SIZE, ADD_BUTTON_SIZE);
				addActive = true;
			} else {
				addActive = false;
			}

			gg.setColor(ADD_X_COLOR);
			gg.setStroke(ADD_X_STROKE);
			gg.drawLine(addX, middle - ADD_X_SIZE / 2, addX, middle + ADD_X_SIZE / 2);
			gg.drawLine(addX - ADD_X_SIZE / 2, middle, addX + ADD_X_SIZE / 2, middle);

			Defaults.TOOLKIT.sync();
		}
	}

	public void addFile(File f) {
		TabInfo newTab = new TabInfo(createTitle(f), (info.size() - 1) * TAB_WIDTH);
		newTab.animateTo(info.size() * TAB_WIDTH);
		info.add(newTab);
		
		setActive(info.size() - 1);
		opening = active;
	}

	public void removeFile(int index) {
		if (info.size() == 1) {
			Brocessing.getWindow().dispose();
		}

		closing = index;
		TabInfo infoBuff;
		for (int i = index; i < info.size(); i++) {
			infoBuff = info.get(i);
			infoBuff.animateTo(infoBuff.position - TAB_WIDTH);
		}
	}

	public void setFile(int index, File f) {
		info.get(index).title = createTitle(f);
		info.get(index).isSaved = true;
		repaint();
	}

	public int getActive() {
		return active;
	}
	
	public void setActive(int index) {
		active = index;
		parent.setPosition(active);
		
		// Animate the newly-active tab to be completely visible.
		new Thread(new Runnable() {
			public void run() {
				int upper = (getWidth() - ADD_WIDTH) - ((active + 1) * TAB_WIDTH)
											+ scrollOffset;
				int lower = active * TAB_WIDTH - scrollOffset;
				
				for (; lower < 0; lower += ANIMATION_SPEED) {
					scrollOffset -= ANIMATION_SPEED;
					
					if (scrollOffset < 0) {
						scrollOffset = 0;
					}
					
					redraw();
				}
				
				for (; upper < 0; upper += ANIMATION_SPEED) {
					scrollOffset += ANIMATION_SPEED;
					
					if (getWidth() + scrollOffset - ADD_WIDTH > info.size() *
											TAB_WIDTH)
					{
						scrollOffset = info.size() * TAB_WIDTH - getWidth() +
											ADD_WIDTH;
					}
					
					redraw();
				}
			}
			
			public void redraw() {
				try {
					Defaults.TOOLKIT.sync();
					Thread.sleep(1000 / 60);
					repaint();
				} catch (Exception e) {
					ErrorMgr.show(e, "Something went wrong animating.");
				}
			}
		}).start();
	}

	public void mouseMoved(MouseEvent me) {
		mouseX = me.getX();
		mouseY = me.getY();

		repaint();
	}

	public void mouseDragged(MouseEvent me) {
		if (dragging == -1) {
			int actualX = mouseX + scrollOffset;
			int buff = actualX / TAB_WIDTH;
			if (buff < info.size()) {
				dragging = buff;
				originalDrag = dragging;
				dragOffset = actualX % TAB_WIDTH - scrollOffset;
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

				mouseX = me.getX();
				mouseY = me.getY();
			}
			
			setActive(buff);
		} else {
			mouseX = me.getX();
			mouseY = me.getY();

			dragSwap();
		}

		repaint();
	}

	public void mouseExited(MouseEvent me) {
		mouseX = -1;
		mouseY = -1;

		repaint();
	}

	public void mousePressed(MouseEvent me) {
		int button = me.getButton();
		if (button == 4 || button == 5) {
			int wr = (button == 4) ? -1 : 1;
			scrollEvent(wr);
			
			me.consume();
			return;
		}
		
		if (opening == -1 && closing == -1) {
			if (!addActive && removeActive == -1) {
				int index = (me.getX() + scrollOffset) / TAB_WIDTH;
				if (index < info.size()) {
					setActive(index);
				}
			}
		}
	}

	public void mouseReleased(MouseEvent me) {
		if (me.getButton() != 4 && me.getButton() != 5) {
			if (dragging != -1) {
				info.get(dragging).animateTo(dragging * TAB_WIDTH);
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				parent.moveTo(originalDrag, dragging);
				dragging = -1;
			} else {
				if (addActive) {
					parent.open(null, true);
				} else if (removeActive != -1 && closing == -1) {
					Brocessing.getWindow().closeTab(removeActive, false);
				}
			}

			repaint();
		}
	}

	public void mouseWheelMoved(MouseWheelEvent mwe) {
		scrollEvent(mwe.getWheelRotation());
	}
	
	// Called when the mouse wheel or trackpad is scrolled upon.
	public void scrollEvent(int wr) {
		int totalWidth = info.size() * TAB_WIDTH;
		if (totalWidth > getWidth() - ADD_WIDTH) {
			int upperBound = totalWidth - getWidth() + ADD_WIDTH;
			if (scrollOffset < upperBound && wr > 0) {
				if (scrollOffset + wr * SCROLL_SPEED > upperBound) {
					scrollOffset = upperBound;
				} else {
					scrollOffset += wr * SCROLL_SPEED;
				}
			} else if (scrollOffset > 0 && wr < 0) {
				if (scrollOffset + wr * SCROLL_SPEED < 0) {
					scrollOffset = 0;
				} else {
					scrollOffset += wr * SCROLL_SPEED;
				}
			}

			repaint();
		}
	}

	public void setActiveSaved(boolean saveState) {
		info.get(active).isSaved = saveState;
		repaint();
	}
	
	public void setSavedAt(int index, boolean state) {
		info.get(index).isSaved = state;
		repaint();
	}
	
	public boolean getActiveSaved() {
		return info.get(active).isSaved;
	}
	
	public boolean getSavedAt(int index) {
		return info.get(index).isSaved;
	}
	
	public String getActiveTitle() {
		return info.get(active).title;
	}
	
	public String getTitleAt(int index) {
		return info.get(index).title;
	}

	private double distance(int x1, int y1, int x2, int y2) {
		int diffX = x2 - x1, diffY = y2 - y1;
		return Math.sqrt((diffX * diffX) + (diffY * diffY));
	}

	private void drawTab(Graphics2D gg, int middle, int i) {
		FontMetrics metrics = gg.getFontMetrics();
		int width, total, j, k;
		int midline = middle + ((metrics.getAscent() - metrics.getDescent()) / 2);
		String buffer;
		char cBuff;
		int buttonX, halfBtnSize, halfXSize;

		if (i == active) {
			gg.setColor(ACTIVE_TAB_COLOR);
		} else {
			gg.setColor(TAB_COLOR);
		}

		gg.fillRoundRect(info.get(i).position - scrollOffset, 0, TAB_WIDTH, TAB_HEIGHT, 10,10);

		gg.setColor(TEXT_COLOR);
		width = metrics.stringWidth(info.get(i).title);

		if (width + TEXT_OFFSET >= TAB_WIDTH - CLOSE_SIZE) {
			total = TEXT_OFFSET;
			j = 0;

			while (total < TAB_WIDTH - CLOSE_SIZE) {
				total += metrics.charWidth(info.get(i).title.charAt(j));
				j++;
			}

			buffer = info.get(i).title.substring(0, j - 4);
			total = metrics.stringWidth(buffer);

			gg.drawString(buffer, info.get(i).position + TEXT_OFFSET-scrollOffset,midline);

			for (k = 4; k > 1; k--) {
				gg.setColor(FADE[k - 2]);
				cBuff = info.get(i).title.charAt(j - k);
				gg.drawString(Character.toString(cBuff),
					info.get(i).position + TEXT_OFFSET+total-scrollOffset,midline);
				total += metrics.charWidth(cBuff);
			}
		} else {
			gg.drawString(info.get(i).title, info.get(i).position +
								TEXT_OFFSET - scrollOffset, midline);
		}

		buttonX = info.get(i).position + TAB_WIDTH - (CLOSE_SIZE / 2) - scrollOffset;
		halfBtnSize = BUTTON_SIZE / 2;
		if (distance(mouseX, mouseY, buttonX, middle) < halfBtnSize) {
			gg.setColor(BUTTON_COLOR);
			gg.fillOval(buttonX - halfBtnSize, middle - halfBtnSize, 
				BUTTON_SIZE, BUTTON_SIZE);
			removeActive = i;
		}

		halfXSize = X_SIZE / 2;
		gg.setColor(X_COLOR);
		gg.drawLine(buttonX - halfXSize, middle - halfXSize,
			buttonX + halfXSize, middle + halfXSize);
		gg.drawLine(buttonX + halfXSize, middle - halfXSize,
			buttonX - halfXSize, middle + halfXSize);

		if (!info.get(i).isSaved) {
			gg.setColor(Color.RED);
			gg.fillOval(info.get(i).position + UNSAVED_POSITION - scrollOffset,
				UNSAVED_POSITION, UNSAVED_SIZE, UNSAVED_SIZE);
		}
	}

	private void dragSwap() {
		if (dragging != info.size() - 1 &&
			mouseX - dragOffset + TAB_WIDTH / 2 > TAB_WIDTH * (dragging + 1)) {

			TabInfo buffer = info.get(dragging + 1);
			buffer.animateTo(dragging * TAB_WIDTH);
			info.set(dragging + 1, info.get(dragging));
			info.set(dragging, buffer);
			dragging++;
			active = dragging;
		} else if (dragging != 0 &&
			mouseX - dragOffset + TAB_WIDTH / 2 < TAB_WIDTH * (dragging)) {

			TabInfo buffer = info.get(dragging - 1);
			buffer.animateTo(dragging * TAB_WIDTH);
			info.set(dragging - 1, info.get(dragging));
			info.set(dragging, buffer);
			dragging--;
			active = dragging;
		}
	}
	
	// If File f != null, then this returns the filename, otherwise it
	// calculates the next unused name based on the format: Untitled 1, Untitled 2...etc.
	private String createTitle(File f) {
		if (f == null) {
			int i = 1;
			while (true) {
				String test = "Untitled_" + i;
				int j = 0;
				
				while (j < info.size()) {
					if (info.get(j).title.equals(test)) {
						break;
					}
					
					j++;
				}
				
				if (j == info.size()) {
					return test;
				}
				
				i++;
			}
		} else {
			return f.getName();
		}
	}

	public int getIndexOfName(String s) {
		for (int i = 0; i < info.size(); i++) {
			if (info.get(i).title.equals(s)) {
				return i;
			}
		}

		return -1;
	}

	public void mouseEntered(MouseEvent me) {}
	public void mouseClicked(MouseEvent me) {}

	private class TabInfo implements Runnable {
		private String title;
		private int position, preferredPosition;
		private boolean isSaved = true;

		public TabInfo(String title, int position) {
			this.title = title;
			this.position = position;
		}

		public void animateTo(int preferredPosition) {
			this.preferredPosition = preferredPosition;
			new Thread(this).start();
		}

		public void run() {
			boolean gettingLarger = true;
			if (position > preferredPosition) {
				gettingLarger = false;
			}

			while (position != preferredPosition) {
				if (gettingLarger) {
					if (position + ANIMATION_SPEED > preferredPosition) {
						position = preferredPosition;
					} else {
						position += ANIMATION_SPEED;
					}
				} else {
					if (position - ANIMATION_SPEED < preferredPosition) {
						position = preferredPosition;
					} else {
						position -= ANIMATION_SPEED;
					}
				}

				TabBar.this.repaint();
				try {
					Thread.sleep(1000/60);
				} catch (Exception e) {
					ErrorMgr.show(e, "Thread sleep exception.");
				}
			}
		}
	}
}
