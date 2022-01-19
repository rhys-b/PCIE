# Brocessing

## Processing Language editor that allows for standard input.
## Rhys Byers

The Processing IDE does not allow for users to enter values that would normally
be passed to standard input, which is an unfortunate limitiation for Processing,
which many people use as a stepping-stone toward more general-purpose languages
which are almost guaranteed to read from standard in.

Brocessing uses the processing-java preprocessor and some trickery using sockets
to allow for readByte(), readChar(), readBoolean(), readShort(), readInt(),
readFloat(), readLong(), readDouble(), and readString(). Other improvements
like easier customization of colors, the use of tabs (instead of spaces),
automatic end character insertion, and arrangable tabs, are also present.

[FlatLAF](https://www.formdev.com/flatlaf/) was used for styling.

## Running
### Linux
Download both `Brocessing.jar` and `Brocessing` either from the `src` directory,
or from the Releases tab, and place them both into the top-level Processing
directory. Run `Brocessing` from there or create a desktop file for convenience.

### Windows
Download `Brocessing.exe` from either the `src` directory or the Releases tab.
Double click to run from there, or pin to taskbar, start, or desktop.

### Mac
Download `Brocessing.jar` from either the `src` directory or the Releases tab.
On Mac, you must have a version of the Java Runtime Environment in your PATH
environment variable. Double clicking on the .jar will run Brocessing, but
feel free to create a shortcut to it. NOTE: I haven't tested Brocessing on Mac,
and there's a chance that it won't work.

For testing on any platform, it is worth changing this line:
```java
23 |	p = new ProcessBuilder(Defaults.working + "/processing-java",
```
into
```java
23 |	p = new ProcessBuilder("path/to/your/processing/directory",
```
Don't forget to change it back when you export to a .jar.

## Compiling
From the top level directory, on Windows type `compile`, on Linux type
`./compile` and on Mac, type `sh compile`.
