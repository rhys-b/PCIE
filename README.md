# Processing Command-line Input Editor

## [Processing Language](https://processing.org/) editor that allows for standard input.

*Formerly Brocessing*

The Processing IDE does not allow for users to enter values that would normally
be passed to standard input, which is an unfortunate limitiation for Processing,
a language many people use as a stepping-stone for more general-purpose languages
which are almost guaranteed to read from standard input.

PCIE uses Processing's `processing-java` preprocessor and some trickery using sockets
to allow for `readByte()`, `readChar()`, `readBoolean()`, `readShort()`, `readInt()`,
`readFloat()`, `readLong()`, `readDouble()`, and `readString()`. Other improvements
like easier customization of colors, the default use of tabs (instead of spaces),
automatic closing bracket insertion, and arrangable document tabs, are also present.

[FlatLAF](https://www.formdev.com/flatlaf/) was used for styling.

## Running

![Screenshot](https://github.com/rhys-b/PCIE/blob/main/screenshot2.png)

### Linux
Download both `pcie-1.0.0.jar` and `pcie` either from the `src` directory,
or from the [Releases](https://github.com/rhys-b/Brocessing/releases) tab,
and place them both into the top-level Processing directory. Run `pcie`
from there or create a desktop file for convenience.

### Windows
Download `pcie-1.0.0.exe` from either the `src` directory or the
[Releases](https://github.com/rhys-b/Brocessing/releases) tab.
Double click to run from there, or pin to taskbar, start, or desktop.

### Mac
Download `pcie.jar` from either the `src` directory or the
[Releases](https://github.com/rhys-b/Brocessing/releases) tab.
On Mac, you must have a version of the Java Runtime Environment in your PATH
environment variable. Double clicking on the .jar will run PCIE, but
feel free to create a shortcut to it. NOTE: PCIE is untested on Mac,
and there's a chance that it won't work.

For testing on any platform, it is worth changing this line in `Compiler.java`
```java
18 |	private static final String processingDirectory = Defaults.working;
```
into
```java
18 |	private static final String processingDirectory = "path/to/your/processing/directory/processing-4.1.1",
```
Don't put an ending slash, and don't forget to change it back if you export to a .jar.

## Compiling
From the top level directory, on Windows type `compile`, on Linux type
`./compile` and on Mac, type `sh compile`.
