/*
*	Author: Rhys B.
*	Created: 2021-12-22
*	Modified: 2022-01-01
*
*	Handles the settings page.
*/


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpinnerNumberModel;


public class Settings {
	public static void show() {
		JDialog dialog = new JDialog(
					PCIE.getWindow(), "Options", true
		);
		JPanel cont = new JPanel();
		JScrollPane scroll = new JScrollPane(
				cont,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		);
		JPanel baseplate = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton save = new JButton("Save");
		JButton close = new JButton("Close");
		FontChooser editorFont = new FontChooser(
			"Pick a font for the editor",
			Defaults.EDITOR_FONT
		);
		ColorChooser editorBackground = new ColorChooser(
			"Pick the editor's background",
			Defaults.EDITOR_BACKGROUND_COLOR
		);
		ColorChooser editorForeground = new ColorChooser(
			"Pick the editor text color",
			Defaults.EDITOR_FONT_COLOR
		);
		FontChooser terminalFont = new FontChooser(
			"Pick a font for the terminal",
			Defaults.TERMINAL_FONT
		);
		ColorChooser terminalBackground = new ColorChooser(
			"Pick the terminal's background",
			Defaults.TERMINAL_BACKGROUND_COLOR
		);
		ColorChooser terminalForeground = new ColorChooser(
			"Pick the terminal's text color",
			Defaults.TERMINAL_FONT_COLOR
		);
		BoolChooser autoTab = new BoolChooser(
			"Auto-tab",
			Defaults.AUTO_TAB
		);
		BoolChooser insertEndChars = new BoolChooser(
			"Insert end characters",
			Defaults.INSERT_END_CHARS
		);
		BoolChooser ignoreCase = new BoolChooser(
			"Ignore case in search by default",
			Defaults.IGNORE_CASE
		);
		NumberChooser tabWidth = new NumberChooser(
			"Tab width",
			new SpinnerNumberModel(Defaults.TAB_WIDTH, 1, 24, 1)
		);
		BoolChooser highlight = new BoolChooser(
			"Highlighting",
			Defaults.HIGHLIGHT
		);
		BoolChooser activeLine = new BoolChooser(
			"Active line indication",
			Defaults.ACTIVE_LINE
		);
		BoolChooser showWidth = new BoolChooser(
			"Show document width mask",
			Defaults.SHOW_WIDTH
		);
		NumberChooser widthIndex = new NumberChooser(
			"Document width in characters",
			new SpinnerNumberModel(Defaults.WIDTH_INDEX, 1, 1000, 1)
		);
		BoolChooser findError = new BoolChooser(
			"Go to errenous lines",
			Defaults.FIND_ERROR
		);
		
		HighlightChooser access = new HighlightChooser(
			"Access Modifiers",
			Defaults.ACCESS,
			Defaults.ACCESS_B
		);
		HighlightChooser classes = new HighlightChooser(
			"Class related",
			Defaults.CLASS,
			Defaults.CLASS_B
		);
		HighlightChooser composite = new HighlightChooser(
			"Composite types",
			Defaults.COMPOSITE,
			Defaults.COMPOSITE_B
		);
		HighlightChooser definable = new HighlightChooser(
			"Definable functions",
			Defaults.DEFINABLE,
			Defaults.DEFINABLE_B
		);
		HighlightChooser flow = new HighlightChooser(
			"Flow control",
			Defaults.FLOW,
			Defaults.FLOW_B
		);
		HighlightChooser imports = new HighlightChooser(
			"Import",
			Defaults.IMPORT,
			Defaults.IMPORT_B
		);
		HighlightChooser object = new HighlightChooser(
			"Object related",
			Defaults.OBJECT,
			Defaults.OBJECT_B
		);
		HighlightChooser predefined = new HighlightChooser(
			"Predefined functions",
			Defaults.PREDEFINED,
			Defaults.PREDEFINED_B
		);
		HighlightChooser primitive = new HighlightChooser(
			"Primitive types",
			Defaults.PRIMITIVE,
			Defaults.PRIMITIVE_B
		);
		HighlightChooser tfn = new HighlightChooser(
			"true, false, & null",
			Defaults.TFN,
			Defaults.TFN_B
		);
		HighlightChooser vars = new HighlightChooser(
			"Predefined variables",
			Defaults.VARS,
			Defaults.VARS_B
		);
		HighlightChooser string = new HighlightChooser(
			"Strings",
			Defaults.STRING,
			Defaults.STRING_B
		);
		HighlightChooser comment = new HighlightChooser(
			"Comments",
			Defaults.COMMENT,
			Defaults.COMMENT_B
		);
		HighlightChooser chars = new HighlightChooser(
			"Characters",
			Defaults.CHAR,
			Defaults.CHAR_B
		);
		HighlightChooser num = new HighlightChooser(
			"Numbers",
			Defaults.NUM,
			Defaults.NUM_B
		);
		HighlightChooser escape = new HighlightChooser(
			"Escape Characters",
			Defaults.ESCAPE,
			Defaults.ESCAPE_B
		);
		
		scroll.getVerticalScrollBar().setUnitIncrement(10);
		
		baseplate.add(close);
		baseplate.add(save);
		
		cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
		cont.add(editorFont);
		cont.add(editorBackground);
		cont.add(editorForeground);
		cont.add(terminalFont);
		cont.add(terminalBackground);
		cont.add(terminalForeground);
		cont.add(autoTab);
		cont.add(insertEndChars);
		cont.add(ignoreCase);
		cont.add(tabWidth);
		cont.add(activeLine);
		cont.add(showWidth);
		cont.add(widthIndex);
		cont.add(findError);
		cont.add(new Header("Highlighting"));
		cont.add(highlight);
		cont.add(access);
		cont.add(classes);
		cont.add(composite);
		cont.add(definable);
		cont.add(flow);
		cont.add(imports);
		cont.add(object);
		cont.add(predefined);
		cont.add(primitive);
		cont.add(tfn);
		cont.add(vars);
		cont.add(string);
		cont.add(comment);
		cont.add(chars);
		cont.add(num);
		cont.add(escape);

		
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setSize(500, 500);
		dialog.setMinimumSize(new Dimension(500, 500));
		dialog.setLocationRelativeTo(PCIE.getWindow());
		dialog.setLayout(new BorderLayout());
		dialog.add(scroll);
		dialog.add(baseplate, BorderLayout.SOUTH);
		
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Defaults.EDITOR_FONT_SIZE =editorFont.gitSize();
				Defaults.EDITOR_FONT = editorFont.gitFont();
				Defaults.EDITOR_BACKGROUND_COLOR =
						editorBackground.getColor();
				Defaults.EDITOR_FONT_COLOR =
						editorForeground.getColor();
				Defaults.TERMINAL_FONT_SIZE =
						terminalFont.gitSize();
				Defaults.TERMINAL_FONT = terminalFont.gitFont();
				Defaults.TERMINAL_BACKGROUND_COLOR =
						terminalBackground.getColor();
				Defaults.TERMINAL_FONT_COLOR =
						terminalForeground.getColor();
				Defaults.AUTO_TAB = autoTab.isSelected();
				Defaults.INSERT_END_CHARS =
						insertEndChars.isSelected();
				Defaults.IGNORE_CASE = ignoreCase.isSelected();
				Defaults.TAB_WIDTH = tabWidth.getInt();
				Defaults.HIGHLIGHT = highlight.isSelected();
				Defaults.ACTIVE_LINE = activeLine.isSelected();
				Defaults.SHOW_WIDTH = showWidth.isSelected();
				Defaults.WIDTH_INDEX = widthIndex.getInt();
				Defaults.FIND_ERROR = findError.isSelected();
				
				Defaults.ACCESS = access.getColor();
				Defaults.CLASS = classes.getColor();
				Defaults.COMPOSITE = composite.getColor();
				Defaults.DEFINABLE = definable.getColor();
				Defaults.FLOW = flow.getColor();
				Defaults.IMPORT = imports.getColor();
				Defaults.OBJECT = object.getColor();
				Defaults.PREDEFINED = predefined.getColor();
				Defaults.PRIMITIVE = primitive.getColor();
				Defaults.TFN = tfn.getColor();
				Defaults.VARS = vars.getColor();
				Defaults.STRING = string.getColor();
				Defaults.COMMENT = comment.getColor();
				Defaults.CHAR = chars.getColor();
				Defaults.NUM = num.getColor();
				Defaults.ESCAPE = escape.getColor();
				
				Defaults.ACCESS_B = access.isSelected();
				Defaults.CLASS_B = classes.isSelected();
				Defaults.COMPOSITE_B = composite.isSelected();
				Defaults.DEFINABLE_B = definable.isSelected();
				Defaults.FLOW_B = flow.isSelected();
				Defaults.IMPORT_B = imports.isSelected();
				Defaults.OBJECT_B = object.isSelected();
				Defaults.PREDEFINED_B = predefined.isSelected();
				Defaults.PRIMITIVE_B = primitive.isSelected();
				Defaults.TFN_B = tfn.isSelected();
				Defaults.VARS_B = vars.isSelected();
				Defaults.STRING_B = string.isSelected();
				Defaults.COMMENT_B = comment.isSelected();
				Defaults.CHAR_B = chars.isSelected();
				Defaults.NUM_B = num.isSelected();
				Defaults.ESCAPE_B = escape.isSelected();
				
				PCIE.updateOptions();
				dialog.dispose();
			}
		});
		
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dialog.dispose();
			}
		});
		
		dialog.setVisible(true);
	}
}
