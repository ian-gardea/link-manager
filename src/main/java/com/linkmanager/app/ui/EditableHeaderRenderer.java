package main.java.com.linkmanager.app.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

class EditableHeaderRenderer implements TableCellRenderer {

	private JTable table = null;
	private MouseEventReposter reporter = null;
	private JComponent editor;

	/**
	 * This constructor will initialize a JComponent's border property.
	 * 
	 * @param editor - the JComponent to modify.
	 */
	public EditableHeaderRenderer(JComponent editor) {
		this.editor = editor;
		this.editor.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	}

	/**
	 * This method is used to configure the renderer appropriately before drawing.
	 * 
	 * @param table      - The JTable object.
	 * @param value      - The value in the table cell.
	 * @param isSelected - A boolean indicating if the cell is selected.
	 * @param hasFocus   - A boolean indicating if the cell currently has focus
	 *                   (e.g. if it is clicked).
	 * @param row        - An integer representing the row position of the cell.
	 * @param col        - An integer representing the column position of the cell.
	 * @return - The component used for drawing the cell.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int col) {
		if (table != null && this.table != table) {
			this.table = table;
			final JTableHeader header = table.getTableHeader();
			if (header != null) {
				this.editor.setForeground(header.getForeground());
				this.editor.setBackground(header.getBackground());
				this.editor.setFont(header.getFont());
				reporter = new MouseEventReposter(header, col, this.editor);
				header.addMouseListener(reporter);
			}
		}

		if (reporter != null)
			reporter.setColumn(col);

		return this.editor;
	}

	/**
	 * This class will dispatch or "repost" mouse events to a parent component that
	 * holds a JTableHeader object.
	 * 
	 * @author Ian
	 */
	static public class MouseEventReposter extends MouseAdapter {

		private Component dispatchComponent;
		private JTableHeader header;
		private int column = -1;
		private Component editor;

		/**
		 * @param header - The JTableHeader object.
		 * @param column - The column position within the JTableHeader.
		 * @param editor - The Component containing the JTableHeader object.
		 */
		public MouseEventReposter(JTableHeader header, int column, Component editor) {
			this.header = header;
			this.column = column;
			this.editor = editor;
		}

		/**
		 * @param column - The column position within the JTableHeader.
		 */
		public void setColumn(int column) {
			this.column = column;
		}

		/**
		 * This function will assign the deepest, visible descendant Component within
		 * the parent that contains the location x, y if that position lies withing the
		 * JTableHeader columns.
		 * 
		 * @param e - The MouseEvent handle.
		 */
		private void setDispatchComponent(MouseEvent e) {
			int col = header.getTable().columnAtPoint(e.getPoint());
			if (col != column || col == -1)
				return;

			Point p = e.getPoint();
			Point p2 = SwingUtilities.convertPoint(header, p, editor);
			dispatchComponent = SwingUtilities.getDeepestComponentAt(editor, p2.x, p2.y);
		}

		/**
		 * This function will repost the mouse event to the parent of the dispatch
		 * component.
		 * 
		 * @param e - The MouseEvent handle.
		 * @return - The success/failure of the function to repost the event.
		 */
		private boolean repostEvent(MouseEvent e) {
			if (dispatchComponent == null) {
				return false;
			}
			MouseEvent e2 = SwingUtilities.convertMouseEvent(header, e, dispatchComponent);
			dispatchComponent.dispatchEvent(e2);
			return true;
		}

		/**
		 * The mouse pressed event.
		 * 
		 * @param e - The MouseEvent handle.
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			if (header.getResizingColumn() == null) {
				Point p = e.getPoint();

				int col = header.getTable().columnAtPoint(p);
				if (col != column || col == -1)
					return;

				int index = header.getColumnModel().getColumnIndexAtX(p.x);
				if (index == -1)
					return;

				editor.setBounds(header.getHeaderRect(index));
				header.add(editor);
				editor.validate();
				setDispatchComponent(e);
				repostEvent(e);
			}
		}

		/**
		 * The mouse pressed event.
		 * 
		 * @param e - The MouseEvent handle.
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			repostEvent(e);
			dispatchComponent = null;
			header.remove(editor);
		}
	}
}