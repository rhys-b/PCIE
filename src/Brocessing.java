/*
*	Author: Rhys B.
*	Created: 2021-08-20
*	Modified: 2021-12-22
*
*	Main class for the Brocessing editor.
*/


import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;

import java.io.File;


public class Brocessing {
	private static GUI gui;

	public static void main(String[] args) {
		setLAF();
		Defaults.init(args);
		gui = new GUI(parseFiles(args));
	}

	public static void setLAF() {
		try {
			UIManager.setLookAndFeel(new FlatDarkLaf());
			UIManager.put("TextComponent.selectAllOnFocusPolicy", "always");
		} catch (Exception e) {
			ErrorMgr.show(e, "Look and feel not found.");
		}
	}

	public static GUI getWindow() {
		return gui;
	}

	public static File[] parseFiles(String[] s) {
		if (s.length == 0) {
			return null;
		} else {
			File[] files = new File[s.length];

			for (int i = 0; i < s.length; i++) {
				files[i] = new File(s[i]);
			}

			return files;
		}
	}
	
	public static void updateOptions() {
		gui.updateOptions();
	}
}
