package main.java.com.linkmanager.app.ui;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * This class will allow re-ordering of elements within a JTable.
 * 
 * @author Ian Gardea
 *
 */
public class TableTransferHandler extends MouseAdapter {

	// Placing static definitions at the top of this class.
	private static TableTransferHandler handler;

	/**
	 * Enables the transfer listener.
	 * 
	 * @param table - the JTable to add the listener(s) to.
	 * @param pane  - the CustomLinkPane containing the JTable.
	 */
	public static void enableTransferListeners(JTable table, CustomLinkPane pane) {
		handler = new TableTransferHandler(table, pane);
		table.addMouseListener(handler);
		table.addMouseMotionListener(handler);
	}

	/**
	 * Disables the transfer listener.
	 * 
	 * @param table - the JTable to add the listener(s) to.
	 * @param pane  - the CustomLinkPane containing the JTable.
	 */
	public static void disableTransferListeners(JTable table, CustomLinkPane pane) {
		table.removeMouseListener(handler);
		table.removeMouseMotionListener(handler);
	}

	private JTable table;
	private CustomLinkPane pane;
	private int pressIndex = 0;
	private int releaseIndex = 0;

	/**
	 * This constructor will initialize the JTable and CustomLinkPane.
	 * 
	 * @param table - the JTable to add the listener(s) to.
	 * @param pane  - the CustomLinkPane containing the JTable.
	 */
	public TableTransferHandler(JTable table, CustomLinkPane pane) {
		if (!(table.getModel() instanceof DefaultTableModel)) {
			throw new IllegalArgumentException("List must have a DefaultTableModel");
		}
		if (!(pane.getLinkList().getModel() instanceof DefaultListModel)) {
			throw new IllegalArgumentException("List must have a DefaultTableModel");
		}
		this.table = table;
		this.pane = pane;
	}

	/**
	 * Mouse click event listener for dragging links.
	 * 
	 * @param e - the mouse event handle.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		pressIndex = table.rowAtPoint(e.getPoint());

		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		Rectangle rect = table.getCellRect(row, col, true);

		// If a row is double-clicked, prompt to re-name the tab.
		if (rect != null && rect.contains(e.getPoint()) && e.getClickCount() == 2) {
			pane.getLinkList().promptRenameLink(pressIndex);
			pane.refresh();
		}
		// If row is right-clicked, show the currently assigned target.
		if (rect != null && rect.contains(e.getPoint()) && SwingUtilities.isRightMouseButton(e)) {
			pane.getLinkList().promptRetargetLink(pressIndex);
		}
	}

	/**
	 * Mouse pressed event listener for dragging links.
	 * 
	 * @param e - the mouse event handle.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		pressIndex = table.rowAtPoint(e.getPoint());
	}

	/**
	 * Mouse released event listener for dragging links.
	 * 
	 * @param e - the mouse event handle.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		releaseIndex = table.rowAtPoint(e.getPoint());
		if (releaseIndex != pressIndex && releaseIndex != -1) {
			reorder();
		}
	}

	/**
	 * Mouse dragged event listener for dragging links.
	 * 
	 * @param e - the mouse event handle.
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		mouseReleased(e);
		pressIndex = releaseIndex;
	}

	/**
	 * This function will perform the re-order operation.
	 */
	private void reorder() {
		try {
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.moveRow(pressIndex, pressIndex, releaseIndex);
			pane.getLinkList().swapElements(pressIndex, releaseIndex);
		} catch (Exception e) {

		}
	}
}
