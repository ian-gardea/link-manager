package main.java.com.linkmanager.app.ui;

import main.java.com.linkmanager.app.LinkManager;

import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * This class represents a list of links, and defines the respective actions that can be 
 * performed. Each link list has a name, which will be displayed in the tab its 
 * contents are placed in.
 * 
 * @author Ian Gardea
 *
 */
public class CustomLinkList extends JList<CustomLink> {

	private static final long serialVersionUID = 1L;

	//JPanel variables
	private String                       listName;
	private DefaultListModel<CustomLink> listModel;

	/**
	 * This constructor defines a list of links using a default name.
	 * 
	 */
	public CustomLinkList() {
		listModel = new DefaultListModel<CustomLink>();
		setModel(listModel);

		this.listName = "Default";
	}

	/**
	 * @param listName - the name assigned to the link list.
	 */
	public CustomLinkList(String listName) {
		listModel = new DefaultListModel<CustomLink>();
		setModel(listModel);

		this.listName = listName;
	}

	/**
	 * @return - the name currently assigned to the link list.
	 */
	public String getName() {
		return listName;
	}

	/**
	 * @return - the link at the provided index in the link list. 
	 * @param index - the integer position of the link to retrieve from the link list.
	 */
	public CustomLink getLinkAt(int index) {
		return listModel.elementAt(index);
	}

	/**
	 * @return - the number of links in the link list
	 */
	public int getListSize() {
		return listModel.getSize();
	}

	/**
	 * @param listName - the name to assign to the link list.
	 */
	public void setName(String listName) {
		this.listName = listName;
	}

	/**
	 * Adds a link to the link list using the CustomLink object type.
	 * 
	 * @param link - the link object to be added to the link list.
	 */
	public void addElement(CustomLink link) {
		listModel.addElement(link);
	}

	/**
	 * Adds a link to the link list using a specific name, target, and link type
	 * as a String.
	 * This is essentially a wrapper that will create a CustomLink object.
	 * 
	 * @param name - the name of the link to be added to the link list.
	 * @param target - the target of the link to be added to the link list.
	 * @param type - the String type of the link to be added to the link list.
	 */
	public void addElement(String name, String target, String type) {
		listModel.addElement(new CustomLink(name, target, type));
	}

	/**
	 * Adds a link to the link list using a specific name, target, and link type
	 * as an integer.
	 * This is essentially a wrapper that will create a CustomLink object.
	 * 
	 * @param name - the name of the link to be added to the link list.
	 * @param target - the target of the link to be added to the link list.
	 * @param type - the integer type of the link to be added to the link list.
	 */
	public void addElement(String name, String target, int type) {
		listModel.addElement(new CustomLink(name, target, type));
	}

	/**
	 * Removes the link at the provided position from list of links.
	 * 
	 * This function overrides the parent function's behavior to avoid 
	 * any further, potentially unnecessary processing.
	 * 
	 * @param index - the integer position of the link to be removed from the link list.
	 */
	@Override
	public void remove(int index) {
		listModel.remove(index);
	}

	/**
	 * Swaps the location of two elements in the list of links.
	 * 
	 * @param start - the current position of the link in the link list.
	 * @param target - the new position of the link in the link list.
	 */
	public void swapElements(int start, int target) {
		try{
			// Only swap if the indices are within the bounds of the array.
			if (start >= 0 && start < listModel.getSize()
					&& target >= 0 && target < listModel.getSize()) {
				CustomLink temp = listModel.getElementAt(start);
				listModel.setElementAt(listModel.getElementAt(target), start);
				listModel.setElementAt(temp, target);
			}
		}
		catch(final ArrayIndexOutOfBoundsException e){
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(),
					"Unable to perform swap.", JOptionPane.ERROR_MESSAGE);
		}
		catch(final Exception e){
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(),
					"Unknown Error", JOptionPane.ERROR_MESSAGE);
		}
	}


	/**
	 * This function will prompt the user to rename the link at the specified index.
	 * It will return the original name if the user does not enter a name.
	 * 
	 * @param index - the integer position of the link in the link list.
	 */
	public void promptRenameLink(int index) {
		// Proceed as long as the current item is not a separator.
		if(this.getLinkAt(index).getType() != CustomLink.SEP) {
			// Prompt the user to enter a link name
			String input = (String) JOptionPane.showInputDialog(LinkManager.getFrame(), "Please enter a new name for this link.", "Enter Link Name",
					JOptionPane.PLAIN_MESSAGE, null, null, getLinkAt(index).getName());

			// If the user hits Cancel
			if (input == null) {
				return;
			} 
			// If the user hits OK, and does not enter a name, re-prompt.
			else if (input.equals("")) {
				promptRenameLink(index);
			} 
			else {
				this.getLinkAt(index).setName(input);
			}
		}
	}

	/**
	 * This function will prompt the user to rename the link's target at the specified index.
	 * It will return the original target if the user does not enter a target.
	 * 
	 * @param index - the integer position of the link in the link list.
	 */
	public void promptRetargetLink(int index) {
		// Proceed as long as the current item is not a separator.
		if(this.getLinkAt(index).getType() != CustomLink.SEP) {
			// Prompt the user to enter a link target
			String input = (String) JOptionPane.showInputDialog(LinkManager.getFrame(), "Please enter a new target for this link.", "Enter Link Target",
					JOptionPane.PLAIN_MESSAGE, null, null, getLinkAt(index).getTarget());

			// If the user hits Cancel
			if (input == null) {
				return;
			} 
			// If the user hits OK, and does not enter a name, re-prompt.
			else if (input.equals("")) {
				promptRenameLink(index);
			} 
			else {
				this.getLinkAt(index).setTarget(input);
				this.getLinkAt(index).setType(
						CustomLink.determineType(this.getLinkAt(index).getTarget()));
			}
		}
	}

	/**
	 * This function will prompt the user for the name and target of a new link to be added
	 * to the link list.
	 * 
	 */
	public void promptAddLink() {
		JTextField nField = new JTextField(10);
		JTextField tField = new JTextField(10);

		JPanel myPanel = new JPanel();
		myPanel.setLayout(new GridLayout(0,1));
		myPanel.add(new JLabel("Please enter the name and target for the new link to be added:"));
		myPanel.add(new JLabel("Name:"));
		myPanel.add(nField);
		myPanel.add(new JLabel("Target:"));
		myPanel.add(tField);

		int result = JOptionPane.showConfirmDialog(LinkManager.getFrame(), 
				myPanel, 
				"Enter Link Information", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			if (nField.getText().equals("") || tField.getText().equals("")) {
				promptAddLink();
			} 
			else {
				// Make the link and update the link list.
				CustomLink temp = new CustomLink(nField.getText(), tField.getText(), CustomLink.determineType(tField.getText()));
				listModel.addElement(temp);
			}
		}

		// If the user hits Cancel
		if (result == JOptionPane.CANCEL_OPTION) {
			return;
		} 
	}
	
	/**
	 * This function will prompt the user for the name of a new link to be added.
	 * 
	 * @param target - The target of the link to be added.
	 */
	public void promptAddLink(String target) {
		String input = JOptionPane.showInputDialog(LinkManager.getFrame(), "Please enter the name for the link pointing to:\n\"" + target + "\"", 
				"Enter Link Name",
				JOptionPane.PLAIN_MESSAGE);

		// If the user hits Cancel
		if (input == null) {
			return;
		} 
		// If the user hits OK, and does not enter a name, re-prompt.
		else if (input.equals("")) {
			promptAddLink(target);
		} 
		else {
			// Make the link and update the link list.
			CustomLink temp = new CustomLink(input, target, CustomLink.determineType(target));
			listModel.addElement(temp);
		}
	}
	
	/**
	 * This function will add a separator to the list of links. The separator is treated
	 * like a link to it can be rearranged or deleted, however it does not have a target.
	 * 
	 */
	public void addSeparator() {
		// Make the link and update the link list.
		CustomLink temp = new CustomLink(
				"______________________________________________________________________");
		listModel.addElement(temp);
	}

	/**
	 * 
	 */
	public String toString(){
		String str = "Links\n"; 

		for(int i=0; i<listModel.getSize(); i++){
			str += listModel.getElementAt(i).toString() + "\n";
		}

		return str;
	}
}