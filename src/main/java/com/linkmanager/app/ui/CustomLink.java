package main.java.com.linkmanager.app.ui;

import main.java.com.linkmanager.app.LinkManager;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Paths;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * This class will represent a standard link that has a name, target, and type.
 * 
 * The link type will be used to determine what action to take when it is
 * clicked. For instance, a web URL will be treated differently than a file path
 * or a remote path, etc.
 * 
 * @author Ian Gardea
 *
 */
public class CustomLink extends JLabel {

	private static final long serialVersionUID = 1L;

	public static final int HTTP = 0; // Web URL
	public static final int FILE = 1; // Regular file path
	public static final int FTP = 2; // Remote path
	public static final int CMD = 3; // DOS command
	public static final int SEP = 4; // Separator

	// String representation of the above integer variables.
	private static final String strHTTP = "http";
	private static final String strFILE = "file";
	private static final String strFTP = "ftp";
	private static final String strCMD = "cmd";
	private static final String strSEP = "sep";

	// Local variables.
	private String name;
	private String target;
	private int type;

	/**
	 * This constructor defines a link using only a name. These will be treated as
	 * separators, which take no action when clicked.
	 * 
	 * This is done so the separator appears in link list as a click-able object
	 * that can be re-arranged, deleted, etc. just like any other link.
	 * 
	 * @param name - the name of the link.
	 */
	public CustomLink(String name) {
		super();

		this.name = name;
		this.target = "SEPARATOR";
		this.type = CustomLink.SEP;
	}

	/**
	 * This constructor defines a link that has a name, target, and an integer
	 * representation of the type.
	 * 
	 * @param name   - the name of the link.
	 * @param target - the target/file-path of the link.
	 * @param type   - the type of the link as an integer.
	 */
	public CustomLink(String name, String target, int type) {
		super();

		this.name = name;
		this.target = target;
		this.type = type;
	}

	/**
	 * This constructor defines a link that has a name, target, and a String
	 * representation of the type.
	 * 
	 * @param name   - the name of the link.
	 * @param target - the target/file-path of the link.
	 * @param type   - the type of the link as a String.
	 */
	public CustomLink(String name, String target, String type) {
		super();

		this.name = name;
		this.target = target;

		// Convert the type so only one value type needs to be considered going forward.
		this.type = setType(type);
	}

	/**
	 * @return - the name assigned to the link.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return - the target/file-path currently assigned to the link.
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @return - the integer type currently assigned to the link.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return - the String type currently assigned to the link.
	 * @param type - the integer type to be converted.
	 */
	public static String getTypeAsString(int type) {
		switch (type) {
		case CustomLink.HTTP:
			return strHTTP;
		case CustomLink.FILE:
			return strFILE;
		case CustomLink.FTP:
			return strFTP;
		case CustomLink.CMD:
			return strCMD;
		case CustomLink.SEP:
			return strSEP;
		default:
			return strFILE;
		}
	}

	/**
	 * @param name - the name to be assigned to the link.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param target - the target/file-path to be assigned to the link.
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * This function determines the type assigned to the customized link.
	 * 
	 * @return - the converted integer type of the link.
	 * @param sType - the String type currently assigned to the link.
	 */
	public int setType(String sType) {
		switch (sType) {
		case strHTTP:
			return setType(CustomLink.HTTP);
		case strFILE:
			return setType(CustomLink.FILE);
		case strFTP:
			return setType(CustomLink.FTP);
		case strCMD:
			return setType(CustomLink.CMD);
		case strSEP:
			return setType(CustomLink.SEP);
		default:
			return CustomLink.FILE;
		}
	}

	/**
	 * This setter will assign the type to the specified value, and then return the
	 * value that was assigned.
	 * 
	 * @return - the integer type assigned to the link.
	 * @param iType - the integer type to be assigned to the link.
	 */
	public int setType(int iType) {
		switch (iType) {
		case CustomLink.HTTP:
			type = CustomLink.HTTP;
			return type;
		case CustomLink.FILE:
			type = CustomLink.FILE;
			return type;
		case CustomLink.FTP:
			type = CustomLink.FTP;
			return type;
		case CustomLink.CMD:
			type = CustomLink.CMD;
			return type;
		case CustomLink.SEP:
			type = CustomLink.SEP;
			return type;
		default:
			return CustomLink.FILE;
		}
	}

	/**
	 * This function will determine what type to assign to a link by parsing the
	 * target string passed in.
	 * 
	 * @return - the calculated integer type of the link.
	 * @param target - the String target of the link.
	 */
	public static int determineType(String target) {
		// Standard checks.
		if (target.toUpperCase().startsWith("HTTP:")) {
			return CustomLink.HTTP;
		} else if (target.toUpperCase().startsWith("HTTPS:")) {
			return CustomLink.HTTP;
		} else if (target.toUpperCase().startsWith("WWW.")) {
			return CustomLink.HTTP;
		} else if (target.toUpperCase().startsWith("\\\\")) {
			return CustomLink.FTP;
		}

		// If no match was found, try matching properties that apply to Windows only.
		else if (target.toUpperCase().startsWith("%WINDIR%")) {
			return CustomLink.FTP;
		} else if (target.toUpperCase().startsWith("COMMAND: ")) {
			return CustomLink.CMD;
		} else if (target.toUpperCase().startsWith("SEPARATOR")) {
			return CustomLink.SEP;
		}

		// If no match was found, then try using the file extension.
		// If all else fails, then assume that this the link type is a file.
		int extIndex = target.lastIndexOf(".");
		if (extIndex == -1) {
			return CustomLink.FILE;
		} else {
			switch (target.substring(extIndex + 1).toUpperCase()) {
			case "URL":
				return CustomLink.HTTP;
			default:
				return CustomLink.FILE;
			}
		}
	}

	/**
	 * This function will utilize the Desktop framework to open/browse the link's
	 * target path.
	 * 
	 * This framework will most likely only work properly on Windows platforms.
	 * 
	 * @return - the success/failure of the browse operation.
	 */
	public boolean browse() {
		boolean success = true;

		try {
			// Get the link target, and replace the custom variable placeholder String with
			// the actual value assigned.
			String temp = target.replace("CUSTOM_VAR", LinkManager.CUSTOM_VAR);

			// Process the link based on its type.
			if (type == CustomLink.FILE) {
				// Use the desktop framework to browse, if supported.
				if (Desktop.isDesktopSupported()) {
					URI uri = Paths.get(temp).toUri();
					uri.normalize();
					Desktop.getDesktop().browse(uri);
				} else {
					success = false;
				}
			} else if (type == CustomLink.HTTP) {
				// Convert the URL to a URI, then use the desktop framework to browse, if
				// supported.
				if (Desktop.isDesktopSupported()) {
					URL url = new URL(temp);
					Desktop.getDesktop().browse(url.toURI());
				} else {
					success = false;
				}
			} else if (type == CustomLink.FTP || type == CustomLink.CMD) {
				try {
					// Now things get interesting...
					// The basic idea of the below code is to create a batch file that will do the
					// heavy lifting and process
					// commands like they would be on the Windows OS.

					// Create a temporary batch file that will be used to run the commands written
					// to it.
					final File file = new File("run.bat");
					String fName = file.getName();

					file.createNewFile();
					PrintWriter writer = new PrintWriter(file, "UTF-8");

					// Set the batch to not display unnecessary information.
					writer.println("@echo off");

					// If this is a DOS command, pass the command specified after the "COMMAND: "
					// tag.
					if (type == CustomLink.CMD) {
						temp = URLDecoder.decode(temp, "UTF-8"); // Decode the URL for compatibility.
						temp = temp.substring(9); // Truncate the "COMMAND: " in the beginning of the String.

						// Keep the command prompt on screen after processing PING, IPCONFIG, or
						// NSLOOKUP command.
						if (temp.toUpperCase().contains("PING") || temp.toUpperCase().contains("NSLOOKUP")
								|| temp.toUpperCase().contains("IPCONFIG")) {
							writer.println("start call " + temp);
						} else {
							writer.println("start " + temp);
						}
					}
					// If this is a remote path, then surround the provided target with quotes, and
					// run it in Windows explorer.
					else if (temp.startsWith("\\\\")) {
						writer.println("%SystemRoot%\\explorer.exe \"" + temp + "\"");
					} else if (temp.toUpperCase().startsWith("%WINDIR%")) {
						writer.println("start " + temp);
					} else {
						success = false;
					}

					// Suspend the thread to prevent undesirable effects.
					// For instance, setting this sleep timer allow tabs to be created in web
					// browsers when multiple URLs are being opened.
					// If this is not done, then it possible that not every selected link will run.
					writer.println("ping 1.1.1.1 -n 1 -w " + LinkManager.SLEEP_TIME + ">nul");
					writer.close();

					// The below will run the windows batch file based on the version of windows
					// being used.

					/*
					 * Properly handle commands sent to and from the batch file. Credit: JAVA TRAPS
					 * By Michael C. Daconta
					 * http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.
					 * html?page=2
					 */

					String osName = System.getProperty("os.name");
					String[] cmd = new String[3];
					if (osName.equals("Windows NT")) {
						cmd[0] = "cmd.exe";
					} else if (osName.equals("Windows 95")) {
						cmd[0] = "command.com";
					} else {
						cmd[0] = "cmd";
					}

					cmd[1] = "/C";
					cmd[2] = fName; // Batch file name.

					Runtime rt = Runtime.getRuntime();
					System.out.println("Executing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
					Process proc = rt.exec(cmd);

					// Errors
					StreamInterpreter errorInterpreter = new StreamInterpreter(proc.getErrorStream(), "ERROR");

					// General output
					StreamInterpreter outputInterpreter = new StreamInterpreter(proc.getInputStream(), "OUTPUT");

					// Kick off the I/O stream
					errorInterpreter.start();
					outputInterpreter.start();

					// Any errors?
					int exitVal = proc.waitFor();

					System.out.println("ExitValue: " + exitVal);
					// End of credited code.

					// Delete the temporary batch file.
					file.delete();
				} catch (Throwable t) {
					JOptionPane.showMessageDialog(LinkManager.getFrame(), t.getLocalizedMessage(), "I/O Error",
							JOptionPane.ERROR_MESSAGE);
					success = false;
				}
			} else {
				success = false;
			}
		} catch (URISyntaxException e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "URI Syntax Exception",
					JOptionPane.ERROR_MESSAGE);
			success = false;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "I/O Exception",
					JOptionPane.ERROR_MESSAGE);
			success = false;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unknown Error",
					JOptionPane.ERROR_MESSAGE);
			success = false;
		}

		return success;
	}

	/**
	 * 
	 */
	public String toString() {
		String str = "Link Name:   " + name + "\nLink Target: " + target + "\nLink Type: " + type;

		return str;
	}
}
