package main.java.com.linkmanager.app.ui;

import main.java.com.linkmanager.app.LinkManager;

import java.awt.BorderLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * This class represents a list of links that can be sorted and added via drag and drop (DnD).
 * Each instance will contain a CustomLinkList.
 * 
 * @author Ian Gardea
 *
 */
public class CustomLinkPane extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private final CustomLinkList linkList = new CustomLinkList();
	
	private JTable  tablePanel;
	private JPanel  buttonPanel; 
	private JButton runButton;
	private JButton deleteButton;

	/**
	 * This constructor will initialize the components to be added to the link content pane.
	 * 
	 */
	public CustomLinkPane() {
		// Inherit all of JPanel's native traits.
		super();
		setLayout(new BorderLayout());
		
		// Enable DnD
		DropTargetHandler.enableDragListeners(this);
		
		// Assigning null value since these are created dynamically.
		tablePanel      = null; 
		buttonPanel     = null;
		runButton       = null;
		deleteButton    = null;
	}
	
	/**
	 * @return - the list of links
	 */
	public CustomLinkList getLinkList(){
		return linkList;
	}
	
	/**
	 * @return - the table that contains the list of links
	 */
	public JTable getLinkTable(){
		return tablePanel;
	}
	
	/**
	 * This function will create the various components to be added to the tab's content pane.
	 * 
	 * It is designed to allow dynamic creation of links.
	 * 
	 */
	public void refresh() {
		// Clear the panel of all content, and rebuild it based off of what is currently in the link list.
		removeAll();

		// Create a button that will run selected check box's file path
		runButton = new JButton("Run");
		
		// Set key bindings that will "click" this button. Note: this is for Swing only.
		InputMap runInputMap = runButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke run = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0);
		runInputMap.put(run, "Run");

		// Create a button that will delete selected check box's file path
		deleteButton = new JButton("Delete");
		
		// Set key bindings that will "click" this button. Note: this is for Swing only.
		InputMap deleteInputMap = deleteButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
		deleteInputMap.put(delete, "Delete");
				
		// Create a new panel for the buttons to allow control of the button's size free of layout manager.
		buttonPanel = new JPanel(); 
		
		// Create a "select all" check box.
		final JCheckBox selectAllBox   = new JCheckBox();
		final JLabel    selectAllLabel = new JLabel();
		selectAllLabel.setText("Select All");
		
		// This will be the boolean representation of the click state for each check box (checked = true, unchecked = false).
		final Boolean[] listSelection = new Boolean[linkList.getModel().getSize()];
		
		// Create a new table that will hold the check-boxes, and an adjacent list of links.
		// Customize the table model by disabling the editing features, which maintaining the check-box functionality.
		final DefaultTableModel tableModel = new DefaultTableModel() {

		    /**
			 * Disable editing features of the JTable.
			 */
			private static final long serialVersionUID = 1L;

			@Override
		    public boolean isCellEditable(int row, int column) {
		       // Maintain check-box functionality.
		       if(column == 0){
		    	   return true;
		       }
		       return false;
		    }
		};
		
		// This definition only serves to override to cell rendering such that the first column is always a check-box.
		tablePanel = new JTable(tableModel) {

            private static final long serialVersionUID = 1L;
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0:
                    	return Boolean.class;
                    case 1:
                        return String.class;
                    default:
                    	return String.class;   
                }
            }
        };
        
		tablePanel.setFillsViewportHeight(true);
		tablePanel.setShowGrid(false);                           // Hide cell grid
		tablePanel.setBackground(SystemColor.menu);              // Match background color
		tablePanel.setRowSelectionAllowed(false);                // Disable highlighting
		tablePanel.getTableHeader().setReorderingAllowed(false); // Disable column sorting
		
		tableModel.setRowCount(listSelection.length);            // The number of links.
		tableModel.setColumnCount(2);                            // Check box and the adjacent link.
		
		// Represents the row that contains check-boxes.
		final int checkBoxRow = 0;
		
		// Re-size table.
	    TableColumn column = null;
	    for (int i=0; i<tablePanel.getColumnCount(); i++) {
	        column = tablePanel.getColumnModel().getColumn(i);
	        if (i == 0) {
	        	// Set to a static size, and ensure it cannot be changed.
	            column.setMaxWidth(24);
	            column.setMinWidth(24);
	        } 
	        else {
	            column.setPreferredWidth(418);
	        }
	    }  
	    
	    // Enable drag and drop if the GUI is not locked.
	    if(!LinkManager.isLocked()) {
	    	TableTransferHandler.enableTransferListeners(tablePanel, this);
	    }

		// Add and item listener for the "select all" check box.
		selectAllBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED){
					for(int i=0; i<listSelection.length; i++){
						tableModel.setValueAt(true, i, checkBoxRow);
					}
					selectAllLabel.setText("Select None");
					repaint();
				}
				else{
					for(int i=0; i<listSelection.length; i++){
						tableModel.setValueAt(false, i, checkBoxRow);
					}
					selectAllLabel.setText("Select All");
					repaint();
				}
			}
		});
	
		// Add an action listener for the run button.
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int unselectedCount = 0;
				for(int i=0; i<listSelection.length; i++){
					if((boolean) tableModel.getValueAt(i, checkBoxRow)){
						final CustomLink temp = linkList.getLinkAt(i);

						// Create a new thread for each active check box item after all pending threads are finished
						SwingUtilities.invokeLater(new Runnable(){
							public void run(){
								try {			
									temp.browse();

									// Suspend the thread to prevent undesirable effects.
									// For instance, setting this sleep timer allow tabs to be created in web 
									// browsers when multiple URLs are being opened.
									// If this is not done, then it possible that not every selected link will run.
									Thread.sleep(LinkManager.SLEEPTIME);
								} 
								catch (final InterruptedException e) {
									// Show message box with error details
									SwingUtilities.invokeLater(new Runnable(){
										public void run(){
											JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(),
													"Unable to open file", JOptionPane.ERROR_MESSAGE);
										}
									});
								}
							}
						});
					}
					else{
						unselectedCount++;
						if(unselectedCount == linkList.getListSize()){
							JOptionPane.showMessageDialog(LinkManager.getFrame(), "Nothing has been selected!",
									"Notification", JOptionPane.WARNING_MESSAGE);
						}
					}
				}
				// This is ran to auto-clear selections when a link is ran.
				refresh();
			}
		});
		
		// Add an action listener for the delete button.
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int unselectedCount = 0;
				
				for(int i=listSelection.length-1; i>=0; i--){
					if((boolean) tableModel.getValueAt(i, checkBoxRow)){
						unselectedCount--;
						linkList.remove(i);
						refresh();
					}
					else{
						unselectedCount++;
						if(unselectedCount == linkList.getListSize()){
							JOptionPane.showMessageDialog(LinkManager.getFrame(), "Nothing has been selected!",
									"Notification", JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			}
		});

		// Fill the list selection check-boxes.	By default, this will always empty selections on refresh.
		for(int i=0; i<listSelection.length; i++){
			// JTables use boolean values to render check-boxes.
			tableModel.setValueAt(false, i, 0);
			tableModel.setValueAt(linkList.getModel().getElementAt(i).getName(), i, 1);

		}
		
		tablePanel.getColumnModel().getColumn(0).setHeaderRenderer(new EditableHeaderRenderer(selectAllBox));
		tablePanel.getColumnModel().getColumn(1).setHeaderRenderer(new EditableHeaderRenderer(selectAllLabel));
		
		JScrollPane scrollPane = new JScrollPane(tablePanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		// Add components
		add(scrollPane, BorderLayout.WEST);
		
		buttonPanel.add(runButton);
		buttonPanel.add(deleteButton);
		
		add(buttonPanel, BorderLayout.SOUTH);
		
		// Required to see components after they are dynamically created (such as via DnD).
		revalidate();
		repaint();
	}
	
	/**
	 * Forces a click of the run button.
	 * 
	 */
	public void doRun() {
		if(runButton != null) {
			runButton.doClick();
		}
	}
	
	/**
	 * Forces a click of the delete button.
	 * 
	 */
	public void doDelete() {
		if(deleteButton != null) {
			deleteButton.doClick();
		}
	}
}