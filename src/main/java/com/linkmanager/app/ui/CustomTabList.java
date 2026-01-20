package main.java.com.linkmanager.app.ui;

import main.java.com.linkmanager.app.LinkManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class represents a list of tabs that can be sorted via drag and drop
 * (DnD) and renamed via double-click. Each instance will contain a
 * CustomLinkPane.
 * 
 * Tab configurations can be saved to, and re-opened from an XML file.
 * 
 * @author Ian Gardea
 *
 */
public class CustomTabList extends JTabbedPane {

	private static final long serialVersionUID = 1L;

	// XML file variables
	private TransformerFactory transformerFactory;
	private Transformer transformer;

	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;

	private Document doc;

	// Tab list
	private ArrayList<CustomLinkPane> tabs;

	/**
	 * This constructor will initialize the components to be added to the tab
	 * content pane.
	 * 
	 */
	public CustomTabList() {
		setTabPlacement(JTabbedPane.TOP);

		// Enable DnD and Key listeners.
		TabInputHandler.enableInputListeners(this);

		tabs = new ArrayList<CustomLinkPane>();

		// We will always have a default tab giving our list a minimum size of 1.
		// Make the tab and update the tab list.
		CustomLinkPane temp = new CustomLinkPane();
		tabs.add(temp);

		// Add the tab to the pane.
		add(temp, temp.getLinkList().getName());
	}

	/**
	 * Swaps the location of two elements in the tab list.
	 * 
	 * @param start  - the current position of the link in the tab list.
	 * @param target - the new position of the link in the tab list.
	 */
	public void swapTabs(int start, int target) {
		try {
			// Only swap if the indices are within the bounds of the array.
			if (start >= 0 && start < tabs.size() && target >= 0 && target < tabs.size()) {
				CustomLinkPane temp = tabs.get(start);
				tabs.set(start, tabs.get(target));
				tabs.set(target, temp);
			}
		} catch (final ArrayIndexOutOfBoundsException e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unable to perform swap.",
					JOptionPane.ERROR_MESSAGE);
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unknown Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This function will prompt the user to rename the tab at the specified index.
	 * It will return the original name if the user does not enter a name.
	 * 
	 * @param index - the integer position of the tab in the tab list.
	 */
	public void promptRenameTab(int index) {
		// Prompt the user to enter a link name
		String input = (String) JOptionPane.showInputDialog(LinkManager.getFrame(),
				"Please enter a new name for this tab.", "Enter Tab Name", JOptionPane.PLAIN_MESSAGE, null, null,
				tabs.get(index).getLinkList().getName());

		// If the user hits Cancel
		if (input == null) {
			return;
		}
		// If the user hits OK, and does not enter a name, re-prompt.
		else if (input.equals("")) {
			promptRenameTab(index);
		} else {
			this.setTitleAt(index, input);
		}
	}

	/**
	 * This function will prompt the user for the name of a new tab to be added to
	 * the tab list.
	 * 
	 */
	public void promptAddTab() {
		String input = JOptionPane.showInputDialog(LinkManager.getFrame(), "Please enter the name for this tab.",
				"Enter Tab Name", JOptionPane.PLAIN_MESSAGE);

		// If the user hits Cancel
		if (input == null) {
			return;
		}
		// If the user hits OK, and does not enter a name, re-prompt.
		else if (input.equals("")) {
			promptAddTab();
		} else {
			// Make the tab and update the tab list.
			CustomLinkPane temp = new CustomLinkPane();
			temp.getLinkList().setName(input);
			tabs.add(temp);

			// Add the tab to the pane with its own scroll bar.
			add(temp, temp.getLinkList().getName());
		}
	}

	/**
	 * This function will create a new tab, and update the tab list.
	 * 
	 * @param name - the name to be assigned to the new tab.
	 */
	public void addTab(String name) {
		CustomLinkPane temp = new CustomLinkPane();
		temp.getLinkList().setName(name);
		tabs.add(temp);

		// Add the tab to the pane with its own scroll bar.
		add(temp, temp.getLinkList().getName());
	}

	/**
	 * This function will delete the currently selected tab from the tab list.
	 * 
	 */
	public void deleteCurrentTab() {
		if (tabs.size() > 1) {
			tabs.remove(getSelectedIndex());
			this.remove(getSelectedIndex());
		} else {
			JOptionPane.showMessageDialog(LinkManager.getFrame(),
					"This is the last tab, and cannot be removed until more are added!", "Unable to delete tab",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @return - the list of tabs.
	 */
	public ArrayList<CustomLinkPane> getTabList() {
		return tabs;
	}

	/**
	 * This function renames the tab, overriding the parent function to provide
	 * additional functionality.
	 * 
	 * @param index - the integer position of the tab in the tab list.
	 * @param title - the name to be assigned to the new tab.
	 */
	@Override
	public void setTitleAt(int index, String title) {
		tabs.get(index).getLinkList().setName(title);

		super.setTitleAt(index, title);
	}

	/**
	 * This function will start a clean session after receiving confirmation from
	 * the user by deleting the session XML file.
	 * 
	 * @param f - the source XML session file.
	 */
	public void newDocument(File f) {
		int dialogResult;

		// If there is already a session file, do not show the confirmation message.
		if (f.exists()) {
			int dialogButton = JOptionPane.YES_NO_OPTION;
			dialogResult = JOptionPane.showConfirmDialog(LinkManager.getFrame(),
					"Erase the old session file, and start a new one?\n" + "This action cannot be reversed.",
					"Erase Existing Session", dialogButton);
		} else {
			dialogResult = JOptionPane.YES_OPTION;
		}

		if (dialogResult == JOptionPane.YES_OPTION) {
			try {
				// Clear the old file before proceeding.
				Files.deleteIfExists(Paths.get(f.getPath()));

				removeAll();
				tabs.clear();

				// We will always have a default tab giving our list a minimum size of 1.
				// Make the tab and update the tab list.
				CustomLinkPane temp = new CustomLinkPane();
				tabs.add(temp);

				// Add the tab to the pane.
				add(temp, temp.getLinkList().getName());

				tabs.get(0).refresh();

				saveDocument(f);
			} catch (final IOException e) {
				JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unable to update file",
						JOptionPane.ERROR_MESSAGE);
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unknown Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			return;
		}
	}

	/**
	 * This function will create/update the XML file based on the current tabs/links
	 * present in the session.
	 * 
	 * @param f - the XML session file to be created/updated.
	 */
	public void saveDocument(File f) {
		try {
			// Clear the old file before proceeding.
			Files.deleteIfExists(Paths.get(f.getCanonicalPath()));

			transformerFactory = TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();

			// Enable 'INDENT' and set the indent amount for the transformer.
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();

			// Root element
			doc = dBuilder.newDocument();
			Element rootElement = doc.createElement("Session");
			doc.appendChild(rootElement);

			for (int i = 0; i < tabs.size(); i++) {
				// Tab elements
				Element eTab = doc.createElement("Tab");

				Attr tID = doc.createAttribute("ID");
				tID.setValue("" + i); // Sneaky way to cast an int to a String
				eTab.setAttributeNode(tID);

				Attr tName = doc.createAttribute("Name");
				tName.setValue(tabs.get(i).getLinkList().getName());
				eTab.setAttributeNode(tName);

				rootElement.appendChild(eTab);

				for (int j = 0; j < tabs.get(i).getLinkList().getListSize(); j++) {
					// Link elements
					Element eLink = doc.createElement("Link");

					// set attribute to links element
					Attr lID = doc.createAttribute("ID");
					lID.setValue("" + j); // Sneaky way to cast an int to a String
					eLink.setAttributeNode(lID);

					Attr aName = doc.createAttribute("Name");
					aName.setValue(tabs.get(i).getLinkList().getLinkAt(j).getName());
					eLink.setAttributeNode(aName);

					Attr aType = doc.createAttribute("Type");
					aType.setValue(CustomLink.getTypeAsString(
							CustomLink.determineType(tabs.get(i).getLinkList().getLinkAt(j).getTarget())));

					eLink.setAttributeNode(aType);

					Attr aTarget = doc.createAttribute("Target");
					aTarget.setValue(tabs.get(i).getLinkList().getLinkAt(j).getTarget());
					eLink.setAttributeNode(aTarget);

					eTab.appendChild(eLink);
				}
			}

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(f);
			transformer.transform(source, result);
		} catch (final ParserConfigurationException e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unable to parse XML file",
					JOptionPane.ERROR_MESSAGE);
		} catch (final TransformerConfigurationException e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unable to create XML file",
					JOptionPane.ERROR_MESSAGE);
		} catch (final TransformerException e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unable to create XML file",
					JOptionPane.ERROR_MESSAGE);
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unknown Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This function will restore any uncommitted (non-saved) changes made in the
	 * session to its previous state.
	 * 
	 * @param f - the source XML session file.
	 */
	public void revertDocument(File f) {
		try {
			// If there is nothing to load, save the state of the current form, and load
			// that.
			if (!f.exists()) {
				saveDocument(f);
			}

			// Clear the current window.
			removeAll();
			tabs.clear();

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(f);

			// Optional, but recommended
			doc.getDocumentElement().normalize();

			// Cycle through all Tab tags
			NodeList nListTab = doc.getElementsByTagName("Tab");

			for (int i = 0; i < nListTab.getLength(); i++) {
				Node tNode = nListTab.item(i);

				if (tNode.getNodeType() == Node.ELEMENT_NODE) {

					Element tElement = (Element) tNode;

					// Add the tab
					addTab(tElement.getAttributes().getNamedItem("Name").getNodeValue());

					// Cycle through all link tags
					NodeList nListLink = tElement.getElementsByTagName("Link");

					for (int j = 0; j < nListLink.getLength(); j++) {
						Node lNode = nListLink.item(j);

						if (lNode.getNodeType() == Node.ELEMENT_NODE) {
							Element lElement = (Element) lNode;

							tabs.get(i).getLinkList().addElement(
									lElement.getAttributes().getNamedItem("Name").getNodeValue(),
									lElement.getAttributes().getNamedItem("Target").getNodeValue(),
									CustomLink.determineType(
											lElement.getAttributes().getNamedItem("Target").getNodeValue()));
						}
					}
				}
				tabs.get(i).refresh();
			}
		} catch (final FileNotFoundException e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unable to open XML file",
					JOptionPane.ERROR_MESSAGE);
		} catch (final ParserConfigurationException e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unable to parse XML file",
					JOptionPane.ERROR_MESSAGE);
		} catch (final SAXException e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unable to parse XML file",
					JOptionPane.ERROR_MESSAGE);
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unable to parse XML file",
					JOptionPane.ERROR_MESSAGE);
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(LinkManager.getFrame(), e.getLocalizedMessage(), "Unknown Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
