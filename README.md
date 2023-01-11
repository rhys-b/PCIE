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

Download [PCIE.jar](https://github.com/rhys-b/PCIE/releases/download/v1.0.0/pcie.jar) to get started.

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
