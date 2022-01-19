/*
*	Author: Rhys B.
*	Created: 2021-12-28
*	Modified: 2022-01-05
*
*	Class for compiling and exporting.
*/


import java.io.File;
import java.io.PrintWriter;

import java.util.Scanner;


public class Compiler {
	private static Process p = null;

	public static void compile(File dir, boolean tmp) {
		try {
			writeStdin(dir);

			p = new ProcessBuilder(Defaults.working + "/processing-java",
				"--sketch=" + dir, "--run").start();

			listen(dir, tmp);
			Brocessing.getWindow().getTerminal().connect(p);
		} catch (Exception e) {
			ErrorMgr.show(e, "Error invoking the compiler.");
		}
	}

	public static void exportDir(File dir, boolean tmp) {
		ExportDialog ed = new ExportDialog(dir);
		ExportDialog.ExportData dat = ed.display();

		if (dat.getState() == ExportDialog.EXPORT) {
			try {
				writeStdin(dir);

				if (dat.embedJava()) {
					p = new ProcessBuilder(Defaults.working + "/processing-java",
						"--sketch=" + dat.getSource(),
						"--output=" + dat.getSink(), "--platform=" +
						dat.getPlatform(), "--export").start();
				} else {
					p = new ProcessBuilder(Defaults.working + "/processing-java",
						"--sketch=" + dat.getSource(),
						"--output=" + dat.getSink(), "--platform=" +
						dat.getPlatform(), "--no-java", "--export").start();
				}
			} catch (Exception e) {
				ErrorMgr.show(e, "Error invoking export.");
			}

			listen(dir, tmp);
		} else if (tmp) {
			dir.listFiles()[0].delete();
			dir.delete();
		}
	}

	public static void exportJar(File dir, boolean tmp) {
		// These two methods seem imposible to implement given the
		// current way that Processing.org decided to implement their
		// part of the compile, and without significant knowledge of the
		// inner workings of processing-java.
	}

	public static void exportExe(File dir, boolean tmp) {
		
	}

	public static void kill() {
		if (p == null) {
			Brocessing.getWindow().getTerminal().append("No program running.");
		} else {
			while (p != null && p.descendants().count() > 0) {
				p.descendants().skip(p.descendants().count() - 1)
					.findFirst().get().destroyForcibly();

				try {
					Thread.sleep(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			while (p != null) {
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					ErrorMgr.show(e, "Error blocking for process termination.");
				}
			}

			Brocessing.getWindow().getTerminal().append("Program Terminated.");
		}
	}

	private static void listen(File dir, boolean tmp) {
		new Thread(new Runnable() {
			public void run() {
				new Thread(new Runnable() {
					public void run() {
						Scanner s = new Scanner(p.getErrorStream());
						boolean ignoreTrace = false;
						while (s.hasNextLine()) {
							String str = s.nextLine();

							if (ignoreTrace) {
								if (str.startsWith("\t")) {
									return;
								} else {
									ignoreTrace = false;
								}
							}

							if (	str.startsWith(
								"Exception in thread \"main\" java." +
								"lang.ArrayIndexOutOfBoundsException"))
							{
								str = "Syntax error.";
								ignoreTrace = true;
							}

							ErrorParser.handle(str);

							Brocessing.getWindow().getTerminal().append(str);
							Brocessing.getWindow().getTerminal().kill();
						}
					}
				}).start();

				Scanner s = new Scanner(p.getInputStream());
				while (s.hasNextLine()) {
					Brocessing.getWindow().getTerminal().append(s.nextLine());
				}

				removeStdin(dir);
				if (tmp) {
					dir.listFiles()[0].delete();
					dir.delete();
				}

				Brocessing.getWindow().getTerminal().disconnect();
				p = null;
			}
		}).start();
	}

	private static void writeStdin(File dir) {
		try {
			Scanner scan=new Scanner(Compiler.class.getResourceAsStream("data/stdin.pde"));

			File stdin = new File(dir.getAbsolutePath() + "/stdin.pde");

			int timeout = 0, time = 100;
			while (stdin.exists() && timeout < 2000) {
				Thread.sleep(time);
				timeout += time;
			}

			PrintWriter writer = new PrintWriter(stdin);

			while (scan.hasNextLine()) {
				writer.println(scan.nextLine());
			}

			scan.close();
			writer.close();
		} catch (Exception e) {
			ErrorMgr.show(e, "Error writing input library.");
		}
	}

	private static void removeStdin(File dir) {
		File stdin = new File(dir.getAbsolutePath() + "/stdin.pde");
		stdin.delete();
	}
}
