package main.java.com.linkmanager.app.ui;

import main.java.com.linkmanager.app.LinkManager;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.event.MouseInputAdapter;

/**
 * Credit for the basis of this class goes to Patrick Gotthardt and http://www.jroller.com/
 * for enhancements to the base JTabbedPane class.
 * 
 * This class will allow re-ordering of tabs.
 * 
 * @author Ian Gardea
 *
 */
public class TabInputHandler extends MouseInputAdapter implements KeyListener, ClipboardOwner  {
	private static TabInputHandler handler;

	/**
	 * Enables the input listeners.
	 * 
	 * @param pane - the CustomTabList that will have the listeners added.
	 */
	public static void enableInputListeners(CustomTabList pane) {
		handler = new TabInputHandler(pane);
		pane.addMouseListener(handler);
		pane.addMouseMotionListener(handler);
		pane.addKeyListener(handler);
	}

	/**
	 * Disables the input listeners.
	 * 
	 * @param pane - the CustomTabList that will have the listeners added.
	 */
	public static void disableInputListeners(CustomTabList pane) {
		pane.removeMouseListener(handler);
		pane.removeMouseMotionListener(handler);
		pane.removeKeyListener(handler);
	}

	private CustomTabList tabPane;
	private int draggedTabIndex;

	/**
	 * This constructor will initialize the CustomLTabList.
	 * 
	 * @param pane - the CustomLTabList.
	 */
	protected TabInputHandler(CustomTabList pane) {
		this.tabPane = pane;
		draggedTabIndex = -1;
	}

	/**
	 * Mouse click event listener for dragging tabs.
	 * 
	 * @param e - the mouse event handle.
	 */
	@Override 
	public void mouseClicked(MouseEvent me) {
		Rectangle rect = tabPane.getUI().getTabBounds(tabPane, tabPane.getSelectedIndex());
		
		if (rect != null && rect.contains(me.getPoint()) && me.getClickCount() == 2) {
			tabPane.promptRenameTab(tabPane.getSelectedIndex());
			tabPane.revalidate();
		}
	}

	/**
	 * Mouse pressed event listener for dragging tabs.
	 * 
	 * @param e - the mouse event handle.
	 */
	public void mousePressed(MouseEvent e) { 
		draggedTabIndex = tabPane.getUI().tabForCoordinate(tabPane, e.getX(), e.getY());
	}

	/**
	 * Mouse released event listener for dragging tabs.
	 * 
	 * @param e - the mouse event handle.
	 */
	public void mouseReleased(MouseEvent e) {
		draggedTabIndex = -1;
	}

	/**
	 * Mouse dragged event listener for dragging tabs.
	 * 
	 * @param e - the mouse event handle.
	 */
	public void mouseDragged(MouseEvent e) {
		if(draggedTabIndex == -1) {
			return;
		}

		int targetTabIndex = tabPane.getUI().tabForCoordinate(tabPane,
				e.getX(), e.getY());

		// Swap the tab list elements here before the variables are further changed.
		tabPane.swapTabs(draggedTabIndex, targetTabIndex);

		if(targetTabIndex != -1 && targetTabIndex != draggedTabIndex) {
			boolean isForwardDrag = targetTabIndex > draggedTabIndex;
			tabPane.insertTab(tabPane.getTitleAt(draggedTabIndex),
					tabPane.getIconAt(draggedTabIndex),
					tabPane.getComponentAt(draggedTabIndex),
					tabPane.getToolTipTextAt(draggedTabIndex),
					isForwardDrag ? targetTabIndex+1 : targetTabIndex);
			draggedTabIndex = targetTabIndex;
			tabPane.setSelectedIndex(draggedTabIndex);
		}
	}

	/**
	 * Key pressed event listener for keyboard commands.
	 * 
	 * @param e - the key event handle.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) {
			if(!getClipboardContents().equals("")) {
				CustomLinkPane currentTab = tabPane.getTabList().get(tabPane.getSelectedIndex());
				currentTab.getLinkList().promptAddLink(getClipboardContents());
				currentTab.refresh();
			}
		}
	}

	/**
	 * Key released event listener for keyboard commands.
	 * 
	 * @param e - the key event handle.
	 */
	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	/**
	 * Key typed event listener for keyboard commands.
	 * 
	 * @param e - the key event handle.
	 */
	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	
	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
	}

	/**
	 * Places a String on the clip-board, and make this class the
	 * owner of the Clipboard's contents.
	 * 
	 * @param aString - the String assigned to the clipboard.
	 */
	@SuppressWarnings("unused")
	private void setClipboardContents(String aString){
		StringSelection stringSelection = new StringSelection(aString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, this);
	}

	/**
	 * Get the String residing on the clip-board.
	 *
	 * @return - Any text found on the clip-board. If none found, return an empty String.
	 */
	private String getClipboardContents() {
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText =
				(contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		
		if (hasTransferableText) {
			try {
				result = (String)contents.getTransferData(DataFlavor.stringFlavor);
			}
			catch (UnsupportedFlavorException e){
				JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(),
						"Unsupported Flavor Exception", JOptionPane.ERROR_MESSAGE);
			}
			catch (IOException e){
				JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(),
						"I/O Exception", JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception e){
				JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(),
						"Unknown Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return result;
	}
} 