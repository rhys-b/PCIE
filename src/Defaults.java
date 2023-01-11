/*
*	Author: Rhys B.
*	Created: 2021-08-30
*	Modified: 2022-01-01
*
*	Handles the reading and writing of the preferences
*	file, and communicates that information to widgets
*	as required.
*/


import java.io.File;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Scanner;

import java.awt.Toolkit;
import java.awt.Font;
import java.awt.Color;

import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.image.BufferedImage;

public class Defaults {
	public enum Platform {
		WINDOWS,
		LINUX,
		MAC
	};
	
	private static char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
											'D', 'E', 'F'};

	public static File CURRENT_DIRECTORY;
	public static FileNameExtensionFilter FILE_FILTER =
						new FileNameExtensionFilter("Processing Files", "pde");
	public static Font EDITOR_FONT;
	public static int EDITOR_FONT_SIZE;
	public static Font TERMINAL_FONT;
	public static int TERMINAL_FONT_SIZE;
	public static int FILE_VERSION = 0;
	public static Toolkit TOOLKIT = Toolkit.getDefaultToolkit();
	public static Color EDITOR_FONT_COLOR, EDITOR_BACKGROUND_COLOR;
	public static Color TERMINAL_FONT_COLOR, TERMINAL_BACKGROUND_COLOR;
	public static boolean AUTO_TAB, INSERT_END_CHARS;
	public static boolean IGNORE_CASE;
	public static int TAB_WIDTH;
	public static boolean HIGHLIGHT;
	public static final Platform PLATFORM = getPlatform();
	public static boolean ACTIVE_LINE;
	public static boolean SHOW_WIDTH;
	public static int WIDTH_INDEX;
	public static boolean FIND_ERROR;
	
	public static final String working = new File(Defaults.class.getProtectionDomain()
		.getCodeSource().getLocation().getPath().replace((CharSequence)"%20"," ")).getParent();

	private static final File file = new File(working + "/brocessing_user_data.txt");
	
	public static Color ACCESS, CLASS, COMPOSITE, DEFINABLE, FLOW, IMPORT, OBJECT,
		PREDEFINED, PRIMITIVE, TFN, VARS, STRING, COMMENT, CHAR, NUM, ESCAPE;
	public static boolean ACCESS_B, CLASS_B, COMPOSITE_B, DEFINABLE_B, FLOW_B,
		IMPORT_B, OBJECT_B, PREDEFINED_B, PRIMITIVE_B, TFN_B, VARS_B, STRING_B,
		COMMENT_B, CHAR_B, NUM_B, ESCAPE_B;
	public static String[] ACCESS_L, CLASS_L, COMPOSITE_L, DEFINABLE_L,
		FLOW_L, IMPORT_L, OBJECT_L, PREDEFINED_L, PRIMITIVE_L, TFN_L, VARS_L;
		
	public static void init(String[] args) {
		if (args.length == 0) {
			CURRENT_DIRECTORY = new File(System.getProperty("user.home"));
		} else {
			CURRENT_DIRECTORY = new File(args[args.length - 1]).getParentFile();
		}

		try {
			useDefaults();

			if (file.exists()) {
				readDefaults(file);
			} else {
				writeDefaults();
			}
		} catch (Exception e) {
			ErrorMgr.show(e, "There was an error reading the preferences file.");
		}
		
		ACCESS_L = readWordsFromFile("access");
		CLASS_L = readWordsFromFile("class");
		COMPOSITE_L = readWordsFromFile("composite");
		DEFINABLE_L = readWordsFromFile("definable");
		FLOW_L = readWordsFromFile("flow");
		IMPORT_L = readWordsFromFile("import");
		OBJECT_L = readWordsFromFile("object");
		PREDEFINED_L = readWordsFromFile("predefined");
		PRIMITIVE_L = readWordsFromFile("primitive");
		TFN_L = readWordsFromFile("tfn");
		VARS_L = readWordsFromFile("vars");
	}
	
	public static Platform getPlatform() {
		String os = System.getProperty("os.name");
		
		if (os.equals("Linux")) {
			return Platform.LINUX;
		} else if (os.toLowerCase().contains("windows")) {
			return Platform.WINDOWS;
		} else {
			return Platform.MAC;
		}
	}

	private static void readDefaults(File file) {
		try {
			Scanner scanner = new Scanner(file);
			String line, data;

			if (!scanner.hasNextLine()) {
				corrupted(file);
				return;
			}

			do {
				line = scanner.nextLine();
			} while (line.startsWith("#") || line.equals(""));

			if (!line.startsWith("File Version")) {
				scanner.close();
				corrupted(file);
				return;
			} else if (Integer.parseInt(getData(line)) > FILE_VERSION) {
				String[] options = {"Continue Anyway", "Quit"};

				int ret = JOptionPane.showOptionDialog(
					PCIE.getWindow(),
					"The preferences file is from a newer version than this " +
					"program. Not all your settings may be saved if you continue.",
					"Preferences Version",
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					options,
					options[0]
				);

				if (ret == 1) {
					System.exit(0);
				}
			}

			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				
				if (!line.equals("") && !line.startsWith("#")) {
					data = getData(line);

					if (line.startsWith("Editor Font Size:")) {
						EDITOR_FONT_SIZE = Integer.parseInt(data);
					} else if (line.startsWith("Editor Font:")) {
						EDITOR_FONT = new Font(data, Font.PLAIN,
										EDITOR_FONT_SIZE);
					} else if (line.startsWith("Terminal Font Size:")) {
						TERMINAL_FONT_SIZE = Integer.parseInt(data);
					} else if (line.startsWith("Terminal Font:")) {
						TERMINAL_FONT = new Font(data, Font.PLAIN,
										TERMINAL_FONT_SIZE);
					} else if (line.startsWith("Editor Font Color:")) {
						EDITOR_FONT_COLOR = hexToColor(data);
					} else if (line.startsWith("Editor Background Color:")) {
						EDITOR_BACKGROUND_COLOR = hexToColor(data);
					} else if (line.startsWith("Terminal Font Color:")) {
						TERMINAL_FONT_COLOR = hexToColor(data);
					} else if (line.startsWith("Terminal Background Color:")) {
						TERMINAL_BACKGROUND_COLOR = hexToColor(data);
					} else if (line.startsWith("Auto-Tab:")) {
						AUTO_TAB = Boolean.parseBoolean(data);
					} else if (line.startsWith("Insert End Characters:")) {
						INSERT_END_CHARS = Boolean.parseBoolean(data);
					} else if (line.startsWith("Ignore Case:")) {
						IGNORE_CASE = Boolean.parseBoolean(data);
					} else if (line.startsWith("Highlight:")) {
						HIGHLIGHT = Boolean.parseBoolean(data);
					} else if (line.startsWith("Tab Width:")) {
						TAB_WIDTH = Integer.parseInt(data);
					} else if (line.startsWith("Access Modifiers:")) {
						ACCESS = hexToColor(data);
					} else if (line.startsWith("Class-related:")) {
						CLASS = hexToColor(data);
					} else if (line.startsWith("Composites:")) {
						COMPOSITE = hexToColor(data);
					} else if (line.startsWith("Definable Functions:")) {
						DEFINABLE = hexToColor(data);
					} else if (line.startsWith("Flow Control:")) {
						FLOW = hexToColor(data);
					} else if (line.startsWith("Import:")) {
						IMPORT = hexToColor(data);
					} else if (line.startsWith("Object-related:")) {
						OBJECT = hexToColor(data);
					} else if (line.startsWith("Predefined Functions:")) {
						PREDEFINED = hexToColor(data);
					} else if (line.startsWith("Primitives:")) {
						PRIMITIVE = hexToColor(data);
					} else if (line.startsWith("True/False/Null:")) {
						TFN = hexToColor(data);
					} else if (line.startsWith("Language Variables:")) {
						VARS = hexToColor(data);
					} else if (line.startsWith("Strings:")) {
						STRING = hexToColor(data);
					} else if (line.startsWith("Comments:")) {
						COMMENT = hexToColor(data);
					} else if (line.startsWith("Characters:")) {
						CHAR = hexToColor(data);
					} else if (line.startsWith("Numbers:")) {
						NUM = hexToColor(data);
					} else if (line.startsWith("Escapes:")) {
						ESCAPE = hexToColor(data);
					} else if (line.startsWith("Access Modifiers Bold:")) {
						ACCESS_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Class-related Bold:")) {
						CLASS_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Composites Bold:")) {
						COMPOSITE_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Definable Functions Bold:")) {
						DEFINABLE_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Flow Control Bold:")) {
						FLOW_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Import Bold:")) {
						IMPORT_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Object-related Bold:")) {
						OBJECT_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Predefined Functions Bold:")) {
						PREDEFINED_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Primitives Bold:")) {
						PRIMITIVE_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("True/False/Null Bold:")) {
						TFN_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Language Variables Bold:")) {
						VARS_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Strings Bold:")) {
						STRING_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Comments Bold:")) {
						COMMENT_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Characters Bold:")) {
						CHAR_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Numbers Bold:")) {
						NUM_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Escapes Bold:")) {
						ESCAPE_B = Boolean.parseBoolean(data);
					} else if (line.startsWith("Active Line:")) {
						ACTIVE_LINE = Boolean.parseBoolean(data);
					} else if (line.startsWith("Show Width:")) {
						SHOW_WIDTH = Boolean.parseBoolean(data);
					} else if (line.startsWith("Width Index: ")) {
						WIDTH_INDEX = Integer.parseInt(data);
					} else if (line.startsWith("Find Error Line: ")) {
						FIND_ERROR = Boolean.parseBoolean(data);
					}
				}
			}

			scanner.close();
		} catch (Exception e) {
			ErrorMgr.show(e, "There was an error reading the preferences file.");
		}
	}

	public static void writeDefaults() {
		try {
			PrintWriter writer = new PrintWriter(file);

			writer.println("File Version: " + FILE_VERSION + "\n");

			writer.println("Editor Font Size: " + EDITOR_FONT.getSize());
			writer.println("Editor Font: " + EDITOR_FONT.getName());
			writer.println("Terminal Font Size: " + TERMINAL_FONT.getSize());
			writer.println("Terminal Font: " + TERMINAL_FONT.getName());
			writer.println("Editor Font Color: " + colorToHex(EDITOR_FONT_COLOR));
			writer.println("Editor Background Color: " +
								colorToHex(EDITOR_BACKGROUND_COLOR));
			writer.println("Terminal Font Color: " + colorToHex(TERMINAL_FONT_COLOR));
			writer.println("Terminal Background Color: " +
							colorToHex(TERMINAL_BACKGROUND_COLOR) + "\n");
			
			writer.println("Auto-Tab: " + AUTO_TAB);
			writer.println("Insert End Characters: " + INSERT_END_CHARS + "\n");
			
			writer.println("Ignore Case: " + IGNORE_CASE + "\n");
			
			writer.println("Tab Width: " + TAB_WIDTH + "\n");
			
			writer.println("Highlight: " + HIGHLIGHT);
			writer.println("Active Line: " + ACTIVE_LINE + "\n");
			
			writer.println("Show Width: " + SHOW_WIDTH);
			writer.println("Width Index: " + WIDTH_INDEX + "\n");
			
			writer.println("Access Modifiers: " + colorToHex(ACCESS));
			writer.println("Class-related: " + colorToHex(CLASS));
			writer.println("Composites: " + colorToHex(COMPOSITE));
			writer.println("Definable Functions: " + colorToHex(DEFINABLE));
			writer.println("Flow Control: " + colorToHex(FLOW));
			writer.println("Import: " + colorToHex(IMPORT));
			writer.println("Object-related: " + colorToHex(OBJECT));
			writer.println("Predefined Functions: " + colorToHex(PREDEFINED));
			writer.println("Primitives: " + colorToHex(PRIMITIVE));
			writer.println("True/False/Null: " + colorToHex(TFN));
			writer.println("Language Variables: " + colorToHex(VARS));
			writer.println("Strings: " + colorToHex(STRING));
			writer.println("Comments: " + colorToHex(COMMENT));
			writer.println("Characters: " + colorToHex(CHAR));
			writer.println("Numbers: " + colorToHex(NUM));
			writer.println("Escapes: " + colorToHex(ESCAPE) + "\n");
			
			writer.println("Access Modifiers Bold: " + ACCESS_B);
			writer.println("Class-related Bold: " + CLASS_B);
			writer.println("Composites Bold: " + COMPOSITE_B);
			writer.println("Definable Functions Bold: " + DEFINABLE_B);
			writer.println("Flow Control Bold: " + FLOW_B);
			writer.println("Import Bold: " + IMPORT_B);
			writer.println("Object-related Bold: " + OBJECT_B);
			writer.println("Predefined Functions Bold: " + PREDEFINED_B);
			writer.println("Primitives Bold: " + PRIMITIVE_B);
			writer.println("True/False/Null Bold: " + TFN_B);
			writer.println("Language Variables Bold: " + VARS_B);
			writer.println("Strings Bold: " + STRING_B);
			writer.println("Comments Bold: " + COMMENT_B);
			writer.println("Characters Bold: " + CHAR_B);
			writer.println("Numbers Bold: " + NUM_B);
			writer.println("Escapes Bold: " + ESCAPE_B);

			writer.println("Find Error Line: " + FIND_ERROR);
			
			writer.close();
		} catch (Exception e) {
			ErrorMgr.show(e, "There was an error creating the preferences file.");
		}
	}

	private static void useDefaults() {
		EDITOR_FONT_SIZE = 16;
		EDITOR_FONT = new Font(Font.MONOSPACED, Font.PLAIN, EDITOR_FONT_SIZE);
		TERMINAL_FONT_SIZE = 14;
		TERMINAL_FONT = new Font(Font.MONOSPACED, Font.PLAIN, TERMINAL_FONT_SIZE);
		EDITOR_FONT_COLOR = new Color(225, 225, 225);
		TERMINAL_FONT_COLOR = new Color(255, 255, 255);
		EDITOR_BACKGROUND_COLOR = new Color(69, 73, 74);
		TERMINAL_BACKGROUND_COLOR = new Color(0, 0, 0);
		AUTO_TAB = true;
		INSERT_END_CHARS = true;
		IGNORE_CASE = true;
		TAB_WIDTH = 8;
		HIGHLIGHT = true;
		ACTIVE_LINE = true;
		SHOW_WIDTH = true;
		WIDTH_INDEX = 80;
		
		ACCESS = new Color(87, 224, 29);
		CLASS = new Color(87, 224, 29);
		COMPOSITE = new Color(206, 125, 18);
		DEFINABLE = new Color(76, 125, 239);
		FLOW = new Color(255, 255, 255);
		IMPORT = new Color(176, 124, 239);
		OBJECT = new Color(255, 255, 255);
		PREDEFINED = new Color(46, 232, 242);
		PRIMITIVE = new Color(46, 242, 121);
		TFN = new Color(242, 104, 46);
		VARS = new Color(242, 92, 197);
		STRING = new Color(242, 229, 46);
		COMMENT = new Color(160, 160, 160);
		CHAR = new Color(196, 127, 0);
		NUM = new Color(213, 247, 22);
		ESCAPE = new Color(244, 151, 22);
		
		ACCESS_B = true;
		CLASS_B = true;
		COMPOSITE_B = false;
		DEFINABLE_B = true;
		FLOW_B = true;
		IMPORT_B = false;
		OBJECT_B = true;
		PREDEFINED_B = false;
		PRIMITIVE_B = true;
		TFN_B = false;
		VARS_B = false;
		ESCAPE_B = false;

		FIND_ERROR = true;
	}

	private static String getData(String line) {
		return line.substring(line.indexOf(":") + 1).trim();
	}

	private static void corrupted(File file) {
		String[] options = {"Recreate file with defaults", "Quit", "Use defaults this time"};

		int ret = JOptionPane.showOptionDialog(
			PCIE.getWindow(),
			"The preferences file does not contain essential data.",
			"Preferences File Corrupted",
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE,
			null,
			options,
			options[0]
		);

		if (ret == 0 || ret == 2) {
			useDefaults();

			if (ret == 0) {
				writeDefaults();
			}
		} else if (ret == 1 || ret == -1) {
			System.exit(0);
		}
	}

	private static String colorToHex(Color color) {
		String out = "#";

		out += singleDigToHex(color.getRed(), out);
		out += singleDigToHex(color.getGreen(), out);
		out += singleDigToHex(color.getBlue(), out);

		return out;
	}

	private static String singleDigToHex(int i, String out) {
		return hex[(i & 0xF0) >> 4] + Character.toString(hex[i & 0xF]);
	}

	private static Color hexToColor(String input) {
		int r = hexIndexOf(input.charAt(1)) << 4;
		r = r | hexIndexOf(input.charAt(2));

		int g = hexIndexOf(input.charAt(3)) << 4;
		g = g | hexIndexOf(input.charAt(4));

		int b = hexIndexOf(input.charAt(5)) << 4;
		b = b | hexIndexOf(input.charAt(6));

		return new Color(r, g, b);
	}

	private static int hexIndexOf(char c) {
		for (int i = 0; i < hex.length; i++) {
			if (c == hex[i]) {
				return i;
			}
		}

		return '\0';
	}
	
	private static String[] createListFromString(String input) {
		// Creates a string array with no whitespace out of the CSV input.
		
		String[] out = input.split(",");
		
		for (int i = 0; i < out.length; i++) {
			out[i] = out[i].trim();
		}
		
		return out;
	}
	
	private static String createStringFromList(String[] manta) {
		// Creates a CSV string from the list.
		
		String out = "";
		for (int i = 0; i < manta.length - 1; i++) {
			out += manta[i] + ", ";
			
		}
		
		out += manta[manta.length - 1];
		
		return out;
	}
	
	private static String[] readWordsFromFile(String filename) {
		try {
			Scanner s = new Scanner(Defaults.class.getResourceAsStream("data/keywords/" +
										filename + ".txt"));
			ArrayList<String> list = new ArrayList<String>();
			
			while (s.hasNextLine()) {
				list.add(s.nextLine());
			}
			
			return list.toArray(new String[0]);
		} catch (Exception e) {
			ErrorMgr.show(e, "Error retrieving keywords from file.");
		}
		
		return null;
	}
}
