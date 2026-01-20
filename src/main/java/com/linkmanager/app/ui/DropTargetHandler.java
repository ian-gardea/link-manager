package main.java.com.linkmanager.app.ui;

import main.java.com.linkmanager.app.LinkManager;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * This class will enable drag and drop (DnD) operations on a CustomLinkPane so
 * that links can be created from Windows explorer.
 * 
 * @author Ian Gardea
 *
 */
public class DropTargetHandler extends DropTarget {

	// Placing static definitions at the top of this class.
	private static final long serialVersionUID = 1L;

	/**
	 * Enables the DnD listener.
	 * 
	 * @param pane - the CustomLinkPane object to be set as a drop target.
	 */
	public static void enableDragListeners(CustomLinkPane pane) {
		DropTargetHandler handler = new DropTargetHandler(pane);
		pane.setDropTarget(handler);
	}

	/**
	 * Disables the DnD listener.
	 * 
	 * @param pane - the CustomLinkPane object to remove the drop target assignment
	 *             from.
	 */
	public static void disableDragListeners(CustomLinkPane pane) {
		pane.setDropTarget(null);
	}

	private CustomLinkPane linkPane;

	/**
	 * @param pane - the link pane to be initialized by this constructor.
	 */
	protected DropTargetHandler(CustomLinkPane pane) {
		this.linkPane = pane;
	}

	/**
	 * This method accepts a File object as parameter and returns the extension of
	 * the file.
	 * 
	 * @param file - a file object to analyze.
	 * @return -String - the extension of the file.
	 */
	private String getFileExtension(File file) {
		if (file == null) {
			return null;
		}
		String name = file.getName();
		int extIndex = name.lastIndexOf(".");
		if (extIndex == -1) {
			return "";
		} else {
			return name.substring(extIndex + 1);
		}
	}

	/**
	 * This method accepts a File object as parameter and returns the true target
	 * URL, if found in the meta-data.
	 * 
	 * @param file - a file object to be analyzed.
	 * @return -String - the URL of the file found in the meta-data.
	 */
	private String getUrl(File f) throws IOException {
		// Read all file contents and store into a byte array.
		byte[] encoded = Files.readAllBytes(Paths.get(f.getPath()));

		// Store byte array into an encoded screen for readability.
		String fileContent = new String(encoded, Charset.defaultCharset());
		fileContent = fileContent.trim();

		String[] contentList = fileContent.split("\r\n");
		for (String content : contentList) {
			if (content.toUpperCase().startsWith("URL")) {
				int i = content.indexOf("=");
				return content.substring(i + 1);
			}
		}

		// Not a valid URL if reached.
		return null;
	}

	/**
	 * This function utilizes recursion to search through a list of files and/or
	 * directories. It will only copy the files found in the folders/sub-folders to
	 * the target drop panel. It will not copy the folder itself.
	 * 
	 * @param source - The file/directory being copied.
	 */
	private void copyFilesAndDirectories(File source) {
		try {
			// If we encounter a folder, recursively call the function to extract the
			// contained files.
			if (source.isDirectory()) {
				File[] files = source.listFiles();

				for (File file : files) {
					copyFilesAndDirectories(file);
				}
			} else {
				// If this is a shortcut, we will want to copy the true target.
				if (getFileExtension(source).equals("lnk")) {
					LinkParser lParser = new LinkParser(source);

					if (lParser.isLocal()) {
						linkPane.getLinkList().addElement(
								new CustomLink(source.getName(), lParser.getRealFilename(), CustomLink.FILE));
					} else {
						linkPane.getLinkList().addElement(
								new CustomLink(source.getName(), lParser.getRealFilename(), CustomLink.FTP));
					}
				}
				// The same goes for URLs, however we will use an alternate method.
				else if (getFileExtension(source).equals("url")) {
					linkPane.getLinkList()
							.addElement(new CustomLink(source.getName(), getUrl(source), CustomLink.HTTP));
				}
				// Use the canonical path of the file.
				else {
					linkPane.getLinkList().addElement(new CustomLink(source.getName(), source.getCanonicalPath(),
							CustomLink.determineType(source.getCanonicalPath())));
				}
			}
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "File I/O Error.",
					JOptionPane.ERROR_MESSAGE);
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "File I/O Error.",
					JOptionPane.ERROR_MESSAGE);
		} finally {
			linkPane.refresh();
		}
	}

	/**
	 * Listener for external DnD functionality.
	 * 
	 * @param dtde - the drag and drop event.
	 */
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	/**
	 * Listener for external DnD functionality.
	 * 
	 * @param dtde - the drag and drop event.
	 */
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	/**
	 * Listener for external DnD functionality.
	 * 
	 * @param dtde - the drag and drop event.
	 */
	@Override
	public void dragExit(DropTargetEvent dtde) {
	}

	/**
	 * Listener for external DnD functionality.
	 * 
	 * @param dtde - the drag and drop event.
	 */
	@Override
	public void dragOver(DropTargetDragEvent dtde) {
	}

	/**
	 * Listener for external DnD functionality.
	 * 
	 * @param dtde - the drag and drop event.
	 */
	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
			// Determine what the dropped object is.
			Transferable transfer = dtde.getTransferable();

			if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

				List<?> objects = (List<?>) transfer.getTransferData(DataFlavor.javaFileListFlavor);

				for (Object object : objects) {
					// Was a file dropped?
					if (object instanceof File) {
						File source = (File) object;
						copyFilesAndDirectories(source);
					}
				}
			} else if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				String type = (String) transfer.getTransferData(DataFlavor.stringFlavor);

				JOptionPane.showMessageDialog(null, "The data flavor " + type + " is not supported.",
						"Data flavor not supported.", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "The data flavor is not supported.", "Data flavor not supported.",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (final UnsupportedFlavorException e) {
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Data flavor not supported.",
					JOptionPane.ERROR_MESSAGE);
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Data flavor not supported.",
					JOptionPane.ERROR_MESSAGE);
		} finally {
			dtde.dropComplete(true);
			linkPane.refresh();
		}
	}
}
