import java.io.PrintWriter;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Scanner;



static Scanner scan = init();

static Scanner init() {
	try {
		ServerSocket ss = initServer();
		Socket s = ss.accept();
		ss.close();
		Scanner out = new Scanner(s.getInputStream());
		return out;
	} catch (Exception e) {
		e.printStackTrace();
		return null;
	}
}

static ServerSocket initServer() {
	int port = 10000;
	ServerSocket ss = null;
	while (true) {
		try {
			ss = new ServerSocket(port);
			break;
		} catch (BindException be) {
			// Port already in use.
			port++;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	try {
		File file = new File(System.getProperty("user.home") + "/active_ports.txt");

		PrintWriter print = new PrintWriter(file);
		print.println(port);
		print.close();
	} catch (Exception e) {
		e.printStackTrace();
	}

	return ss;
}

static boolean readBoolean() {
	return Boolean.parseBoolean(scan.nextLine());
}

static char readChar() {
	return scan.nextLine().charAt(0);
}

static byte readByte() {
	return Byte.parseByte(scan.nextLine());
}

static short readShort() {
	return Short.parseShort(scan.nextLine());
}

static int readInteger() {
	return Integer.parseInt(scan.nextLine());
}

static float readFloat() {
	return Float.parseFloat(scan.nextLine());
}

static long readLong() {
	return Long.parseLong(scan.nextLine());
}

static double readDouble() {
	return Double.parseDouble(scan.nextLine());
}

static String readString() {
	return scan.nextLine();
}


/*** Prompts ***/

static boolean readBoolean(String s) {
	println(s);
	return Boolean.parseBoolean(scan.nextLine());
}

static char readChar(String s) {
	println(s);
	return scan.nextLine().charAt(0);
}

static byte readByte(String s) {
	println(s);
	return Byte.parseByte(scan.nextLine());
}

static short readShort(String s) {
	println(s);
	return Short.parseShort(scan.nextLine());
}

static int readInteger(String s) {
	println(s);
	return Integer.parseInt(scan.nextLine());
}

static float readFloat(String s) {
	println(s);
	return Float.parseFloat(scan.nextLine());
}

static long readLong(String s) {
	println(s);
	return Long.parseLong(scan.nextLine());
}

static double readDouble(String s) {
	println(s);
	return Double.parseDouble(scan.nextLine());
}

static String readString(String s) {
	println(s);
	return scan.nextLine();
}


/*** Aliases ***/

static boolean readBool() {
	return readBoolean();
}

static int readInt() {
	return readInteger();
}

static String readLine() {
	return readString();
}


/*** Aliases with Prompts ***/

static boolean readBool(String s) {
	println(s);
	return readBoolean();
}

static int readInt(String s) {
	println(s);
	return readInteger();
}

static String readLine(String s) {
	println(s);
	return readString();
}
