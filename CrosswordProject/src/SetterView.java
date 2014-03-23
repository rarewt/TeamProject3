import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.alee.extended.window.ComponentMoveAdapter;
import com.alee.managers.popup.WebPopup;
import com.alee.managers.popup.PopupStyle;
import com.itextpdf.text.Chunk;

public class SetterView {

	private JPanel view, crosswordPanel, controlsPanel, cluePanel, optionsPanel;
	private JMenu fileMenu, viewMenu, helpMenu;
	private JList clueList;
	ArrayList<String> list;
	private Crossword crossword;
	private JButton editWordButton, editClueButton, resetButton, confirmButton, cancelButton;
	private String selectedClue;
	private int selectedX, selectedY, editedX, editedY, originalWordLength, selectedDirection /* 0 - across / 1 - down */;
	private boolean acrossSelection, downSelection, listLocked, ready, reloading;
	private ListModel originalListModel;
	private OptionsInterface options;
	private JTextField inputField;
	private WebPopup inputPopup; // dependent on the custom look and feel - but pretty!
	private int inputMode; // 0 for word, 1 for clue
	
	public SetterView(Crossword c) {
		crossword = c;
		view = new JPanel();
		view.setBackground(Color.decode("#F5F5F5"));
		SpringLayout springLayout = new SpringLayout();
		view.setLayout(springLayout);
		// display the solution and enable the selection of words
		for (int y = 0; y < crossword.getSize(); y++)
			for (int x = 0; x < crossword.getSize(); x++) {
				crossword.getGrid()[x][y].solve();
				crossword.getGrid()[x][y].getPanel().addMouseListener(new MouseSelectionListener());
				if (crossword.getGrid()[x][y].getOriginalColor() == 1)
					crossword.getGrid()[x][y].getDisplayed().setForeground(Color.BLACK);
			}
		view.addMouseListener(new MouseDeselectionListener());
		selectedClue = "";
		selectedX = 0;
		selectedY = 0;
		editedX = 0;
		editedY = 0;
		listLocked = false;
		selectedDirection = 0;
		originalWordLength = 0;
		ready = false;
		reloading = false;
		
		// enable keyboard listening
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyDispatcher());
		
		// set the delay time for all tool tips
		ToolTipManager.sharedInstance().setInitialDelay(500);
		
		// top menu bar
		optionsPanel = new JPanel();
		optionsPanel.setLayout(new BorderLayout());
		optionsPanel.setBackground(Color.decode("#F5F5F5"));
		springLayout.putConstraint(SpringLayout.NORTH, optionsPanel, 0, SpringLayout.NORTH, view);
		springLayout.putConstraint(SpringLayout.WEST, optionsPanel, 0, SpringLayout.WEST, view);
		springLayout.putConstraint(SpringLayout.SOUTH, optionsPanel, 38, SpringLayout.NORTH, view);
		springLayout.putConstraint(SpringLayout.EAST, optionsPanel, 0, SpringLayout.EAST, view);
		options = new OptionsInterface(1); // represents the top menu bar
		optionsPanel.add(options.getMenuBar(), BorderLayout.CENTER);
		// view-specific functionality
		options.getNewCrossword().addActionListener(new NewCrosswordListener());
		options.getSaveCrossword().addActionListener(new SaveCrosswordListener());
		options.getLoadCrossword().addActionListener(new LoadCrosswordListener());
		options.getExportTemplate().addActionListener(new ExportTemplateListener());
		options.getExportPdfTemplate().addActionListener(new exportPdfListener());
		view.add(optionsPanel);
		
		// contains the crossword with the solution displayed	
		crosswordPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, crosswordPanel, 25, SpringLayout.SOUTH, optionsPanel);
		springLayout.putConstraint(SpringLayout.WEST, crosswordPanel, 19, SpringLayout.WEST, view);
		springLayout.putConstraint(SpringLayout.SOUTH, crosswordPanel, -26, SpringLayout.SOUTH, view);
		springLayout.putConstraint(SpringLayout.EAST, crosswordPanel, -333, SpringLayout.EAST, view);	
		crosswordPanel.setLayout(new BorderLayout());
		crosswordPanel.add(crossword.getVisuals());
		view.add(crosswordPanel);
		
		// contains controls for editing the crossword
		controlsPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, controlsPanel, 25, SpringLayout.SOUTH, optionsPanel);
		springLayout.putConstraint(SpringLayout.WEST, controlsPanel, 28, SpringLayout.EAST, crosswordPanel);
		springLayout.putConstraint(SpringLayout.SOUTH, controlsPanel, -26, SpringLayout.SOUTH, view);
		springLayout.putConstraint(SpringLayout.EAST, controlsPanel, -23, SpringLayout.EAST, view);
		controlsPanel.setBackground(Color.decode("#F5F5F5"));
		SpringLayout controlsLayout = new SpringLayout();
		controlsPanel.setLayout(controlsLayout);
		view.add(controlsPanel);
		
		// contains the list of clues
		cluePanel = new JPanel();
		controlsLayout.putConstraint(SpringLayout.NORTH, cluePanel, 0, SpringLayout.NORTH, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.WEST, cluePanel, 0, SpringLayout.WEST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.SOUTH, cluePanel, -85, SpringLayout.SOUTH, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.EAST, cluePanel, 0, SpringLayout.EAST, controlsPanel);
		cluePanel.setLayout(new BorderLayout());
		controlsPanel.add(cluePanel);
				
		// scroll pane for the list of clues
		JScrollPane clueScrollPane = new JScrollPane();
		cluePanel.add(clueScrollPane, BorderLayout.CENTER);
		
		// list of all clues
		if (!crossword.getAcross().isEmpty() || !crossword.getDown().isEmpty()) {
			Object[] across = crossword.getAcross().keySet().toArray();
			Object[] down = crossword.getDown().keySet().toArray();
			Arrays.sort(across); Arrays.sort(down);
			list = new ArrayList<String>();
			list.add("<html><b>Across</b></html>");
			for (int i = 0; i < across.length; i++)
				list.add(across[i] + "a. " + crossword.getAcross().get(across[i])[1] +
						 " (" + crossword.getAcross().get(across[i])[0].length() + ")");
			list.add("<html><b>Down</b></html>");
			for (int i = 0; i < down.length; i++)
				list.add(down[i] + "d. " + crossword.getDown().get(down[i])[1] +
						 " (" + crossword.getDown().get(down[i])[0].length() + ")");
			clueList = new JList(list.toArray());
		}
		else clueList = new JList(new Object[0]);
		originalListModel = clueList.getModel();
		clueList.setFont(new Font(clueList.getFont().getName(), Font.PLAIN, 15));
		clueList.addListSelectionListener(new ClueListListener());
		clueList.addMouseMotionListener(new ToolTipSupport());
		clueList.setFocusable(false);
		clueScrollPane.setViewportView(clueList);
		
		// set buttons
		
		// edit word button
		editWordButton = new JButton("Edit Word");
		editWordButton.addActionListener(new EditWordButtonListener());
		controlsLayout.putConstraint(SpringLayout.NORTH, editWordButton, 10, SpringLayout.SOUTH, cluePanel);
		controlsLayout.putConstraint(SpringLayout.WEST, editWordButton, 0, SpringLayout.WEST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.EAST, editWordButton, -140, SpringLayout.EAST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.SOUTH, editWordButton, -42, SpringLayout.SOUTH, controlsPanel);
		editWordButton.setFont(new Font(editWordButton.getFont().getName(), Font.PLAIN, 15));
		editWordButton.setFocusable(false);
		controlsPanel.add(editWordButton);
		
		// edit clue button
		editClueButton = new JButton("Edit Clue");
		editClueButton.addActionListener(new EditClueButtonListener());
		controlsLayout.putConstraint(SpringLayout.NORTH, editClueButton, 10, SpringLayout.SOUTH, cluePanel);
		controlsLayout.putConstraint(SpringLayout.WEST, editClueButton, 140, SpringLayout.WEST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.EAST, editClueButton, 0, SpringLayout.EAST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.SOUTH, editClueButton, -42, SpringLayout.SOUTH, controlsPanel);
		editClueButton.setFont(new Font(editClueButton.getFont().getName(), Font.PLAIN, 15));
		editClueButton.setFocusable(false);
		controlsPanel.add(editClueButton);
		
		// reset words button
		resetButton = new JButton("Reset Words & Clues");
		resetButton.addActionListener(new ResetButtonListener());
		controlsLayout.putConstraint(SpringLayout.NORTH, resetButton, 10, SpringLayout.SOUTH, editWordButton);
		controlsLayout.putConstraint(SpringLayout.WEST, resetButton, 0, SpringLayout.WEST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.EAST, resetButton, -0, SpringLayout.EAST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.SOUTH, resetButton, 0, SpringLayout.SOUTH, controlsPanel);
		resetButton.setFont(new Font(resetButton.getFont().getName(), Font.PLAIN, 15));
		resetButton.setFocusable(false);
		controlsPanel.add(resetButton);
		
		// pop up for editing a word/clue in the crossword
		inputPopup = new WebPopup();
		inputPopup.setSize(200, 25);	
		inputPopup.setPopupStyle(PopupStyle.light);
		inputPopup.setMargin(6);
		ComponentMoveAdapter.install(inputPopup, inputPopup);
		JPanel inputPopupContent = new JPanel(new GridLayout(2, 1, 0, 3));
		inputPopupContent.setBackground(Color.WHITE);
		
		inputField = new JTextField("");
		inputField.getDocument().addDocumentListener(new InputFieldListener());
		inputField.setPreferredSize(new Dimension(200, 33));
		inputField.setFont(new Font(inputField.getFont().getName(), Font.PLAIN, 15));
		inputField.setHorizontalAlignment(SwingConstants.CENTER);	
		JPanel inputButtons = new JPanel(new GridLayout(1, 2, -1, 3));
		inputButtons.setBackground(Color.WHITE);
		confirmButton = new JButton("OK");
		confirmButton.addActionListener(new ConfirmButtonListener());
		confirmButton.setFont(new Font(confirmButton.getFont().getName(), Font.PLAIN, 15));
		confirmButton.setFocusable(false);
		inputButtons.add(confirmButton);	
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CancelButtonListener());
		cancelButton.setFont(new Font(cancelButton.getFont().getName(), Font.PLAIN, 15));
		cancelButton.setFocusable(false);
		inputButtons.add(cancelButton);
		
		inputPopupContent.add(inputField);
		inputPopupContent.add(inputButtons);
		inputPopup.setDefaultFocusComponent(inputField);
		inputPopup.add(inputPopupContent);
	}
	
	// provides tool tips for the elements in the list of clues
	private class ToolTipSupport implements MouseMotionListener {
		public void mouseDragged(MouseEvent event) {}
		public void mouseMoved(MouseEvent event) {
            JList list = (JList) event.getSource();
            ListModel model = list.getModel();
            int i = list.locationToIndex(event.getPoint());
            if (i > -1)
            	if (!model.getElementAt(i).equals("<html><b>Across</b></html>") &&
            		!model.getElementAt(i).equals("<html><b>Down</b></html>"))
            		list.setToolTipText(model.getElementAt(i).toString());
            	else list.setToolTipText(null);
        }	
	}
	
	// listeners for the set buttons
	
    private class EditWordButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (clueList.isSelectionEmpty()) return;
			if (inputMode == 0 && inputPopup.isShowing()) return;
			inputMode = 0;
			String word = "";
			// search for the selected word
			if (selectedDirection == 0) {
				int y = editedY;
				for (int x = editedX; x <= crossword.getGrid().length-1; x++) {
					if (crossword.getGrid()[x][y].getPanel().getBackground() == Color.BLACK) break;
					word += crossword.getGrid()[x][y].getDisplayed().getText();
				}
			}
			else {
				int x = editedX;
				for (int y = editedY; y <= crossword.getGrid().length-1; y++) {
					if (crossword.getGrid()[x][y].getPanel().getBackground() == Color.BLACK) break;
					word += crossword.getGrid()[x][y].getDisplayed().getText();
				}
			}
			inputField.setText(word.toLowerCase());
			confirmButton.setEnabled(true);
			originalWordLength = word.length();
			inputPopup.showAsPopupMenu(editWordButton);
		}
    }
    
    private class EditClueButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (clueList.isSelectionEmpty()) return;
			if (inputMode == 1 && inputPopup.isShowing()) return;
			inputMode = 1;
			String cropped = "";
			String[] separated = selectedClue.split(" ");
			for (int i = 1; i < separated.length - 1; i++) {
				cropped += separated[i];
				if (i != separated.length - 2) cropped += " ";
			}
			inputField.setText(cropped);
			confirmButton.setEnabled(true);
			inputPopup.showAsPopupMenu(editClueButton);
		}
    }
    
    // checks the input for correctness
    private class InputFieldListener implements DocumentListener {
		public void changedUpdate(DocumentEvent arg0) {
			verify();
		}
		public void insertUpdate(DocumentEvent arg0) {
			verify();
		}
		public void removeUpdate(DocumentEvent arg0) {
			verify();
		}
		public void verify() {
			if (inputMode == 0) {
				boolean valid = true;
				String input = inputField.getText().toLowerCase();
				if (input.length() != originalWordLength) valid = false;
				for (int i = 0; i < input.length(); i++)
					if (!("" + input.charAt(i)).matches("[a-z]")) valid = false;
				if (valid) confirmButton.setEnabled(true);
				else confirmButton.setEnabled(false);	
			}
		}
     }
    
    private class ConfirmButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			crosswordPanel.setVisible(false);
			placeEdit(); // modify the word in the crossword
			crosswordPanel.setVisible(true);
		}	
    }
    
    private class CancelButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			inputPopup.hidePopup(); // hide the pop up
		}
    }
    
    private class ResetButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			crosswordPanel.setVisible(false);
			inputPopup.hidePopup(); // hide the pop up
			for (int y = 0; y < crossword.getSize(); y++)
				for (int x = 0; x < crossword.getSize(); x++) {
					crossword.getGrid()[x][y].setSelected(false); // reset the selection
					crossword.getGrid()[x][y].setMarked(false); // reset the markings
					crossword.getGrid()[x][y].solve(); // return the original letter
				}
			clueList.clearSelection();
			clueList.setModel(originalListModel);
			selectedClue = "";
			recolor();
			crosswordPanel.setVisible(true);
		}
    }
    
    // selection and deselection support
    
	// listener for selections in the list of clues
    private class ClueListListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			inputPopup.hidePopup(); // hide the pop up
			if (clueList.getSelectedValue() != null) {
				// do nothing if the same value is selected again
				if (selectedClue.equals((String) clueList.getSelectedValue())) return;
				// avoid selecting 'Across' and 'Down'
				if (clueList.getSelectedValue().equals("<html><b>Across</b></html>") || 
					clueList.getSelectedValue().equals("<html><b>Down</b></html>")) {
					if (selectedClue.equals("")) clueList.clearSelection();
					else clueList.setSelectedValue(selectedClue, false);
					return;
				}
				// make a new selection
				if (!listLocked) {
					selectedClue = (String) clueList.getSelectedValue();
					for (int y = 0; y < crossword.getSize(); y++)
						for (int x = 0; x < crossword.getSize(); x++) {
							crossword.getGrid()[x][y].setSelected(false); // reset the selection
							crossword.getGrid()[x][y].setMarked(false); // reset the markings
						}
					// iterate to find the target
					for (int y = 0; y < crossword.getSize(); y++)
						for (int x = 0; x < crossword.getSize(); x++)
							if (findPosition((String) clueList.getSelectedValue()).matches(
								crossword.getGrid()[x][y].getNote().getText() + ".")) { // target found
								// set the direction of the selection
								if (findPosition((String) clueList.getSelectedValue()).endsWith("a")) {
									acrossSelection = true;
									downSelection = false;
								}
								else {
									acrossSelection = false;
									downSelection = true;
								}
								// complete the selection
								selectedX = x;
								selectedY = y;
								crossword.getGrid()[x][y].setSelected(true);
								makeSelection();
							}
				}
			}
		}
    }
    
    // mouse listening
    
    // deals with mouse selection events (clicks on squares)
    private class MouseSelectionListener implements MouseListener {
		public void mousePressed(MouseEvent event) {
			inputPopup.hidePopup(); // hide the pop up
			JPanel source = (JPanel) event.getSource();
			if (source.getBackground() != Color.BLACK) { // ignore black squares
				// make a new selection
				for (int y = 0; y < crossword.getSize(); y++)
					for (int x = 0; x < crossword.getSize(); x++) {
						crossword.getGrid()[x][y].setSelected(false); // reset the selection
						crossword.getGrid()[x][y].setMarked(false); // reset the markings
					}
				// iterate to find the target
				for (int y = 0; y < crossword.getSize(); y++)
					for (int x = 0; x < crossword.getSize(); x++) {						
						if (crossword.getGrid()[x][y].getPanel().equals(source)) { // target found
							// case 1 - clicking on a white square - make normal selection
							if (crossword.getGrid()[x][y].getPanel().getBackground() == Color.WHITE) {
								acrossSelection = true;
								downSelection = true;
							}
							// case 2 - clicking on a marked square
							else {
								// across word - make down selection
								if (selectedDirection == 0) {
									acrossSelection = false;
									downSelection = true;
								}
								// down word - make across selection
								else {
									acrossSelection = true;
									downSelection = false;
								}
							}
							// complete the selection
							selectedX = x;
							selectedY = y;
							crossword.getGrid()[x][y].setSelected(true);
							makeSelection();
						}				
					}
			}
		}
		public void mouseEntered(MouseEvent event) {
			JPanel source = (JPanel) event.getSource();
			if (source.getBackground() == Color.BLACK) return;
			if (crossword.getSize() >= 11) source.setBorder(BorderFactory.createLineBorder(Color.decode("#4281F4"), 3));
			else if (crossword.getSize() >= 7) source.setBorder(BorderFactory.createLineBorder(Color.decode("#4281F4"), 4));
			else source.setBorder(BorderFactory.createLineBorder(Color.decode("#4281F4"), 5));
		}
		public void mouseExited(MouseEvent event) {
			JPanel source = (JPanel) event.getSource();
			source.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}
		public void mouseClicked(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}	
    }
    
    // deals with mouse deselection events (clicks on the panel)
    private class MouseDeselectionListener implements MouseListener {
		public void mousePressed(MouseEvent event) {
			inputPopup.hidePopup(); // hide the pop up
			for (int y = 0; y < crossword.getSize(); y++)
				for (int x = 0; x < crossword.getSize(); x++) {
					crossword.getGrid()[x][y].setSelected(false); // reset the selection
					crossword.getGrid()[x][y].setMarked(false); // reset the markings
				}
			clueList.clearSelection();
			selectedClue = "";
			recolor();
		}
		public void mouseEntered(MouseEvent event) {}
		public void mouseExited(MouseEvent event) {}
		public void mouseClicked(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}

    }
	
	// selects a word in the crossword grid
	private void makeSelection() {
		// deal with across selection
		if (acrossSelection)
			for (int x = selectedX; x >= 0; x--) {
				Square current = crossword.getGrid()[x][selectedY];
				if (current.getPanel().getBackground() == Color.BLACK) break;
				if (current.startsAcrossWord()) {
					for (int pos = x; pos <= crossword.getGrid().length-1; pos++) {
						if (crossword.getGrid()[pos][selectedY].getPanel().getBackground() == Color.BLACK) break;
						crossword.getGrid()[pos][selectedY].setMarked(true);
					}
					editedX = x;
					editedY = selectedY;
					selectedDirection = 0;
					// cause list selection
					listLocked = true;
					for (int i = 0; i < clueList.getModel().getSize(); i++) {
						String clue = clueList.getModel().getElementAt(i).toString();
						if (clue.startsWith(current.getNote().getText() + "a")) {
							clueList.setSelectedValue(clue, true);
							selectedClue = clue;
						}
					}
					listLocked = false;
					recolor();
					return;
				}
			}
		// deal with down selection
		if (downSelection)
			for (int y = selectedY; y >= 0; y--) {
				Square current = crossword.getGrid()[selectedX][y];
				if (current.getPanel().getBackground() == Color.BLACK) break;
				if (current.startsDownWord()) {
					for (int pos = y; pos <= crossword.getGrid().length-1; pos++) {
						if (crossword.getGrid()[selectedX][pos].getPanel().getBackground() == Color.BLACK) break;
						crossword.getGrid()[selectedX][pos].setMarked(true);
					}
					editedX = selectedX;
					editedY = y;
					selectedDirection = 1;
					// cause list selection
					listLocked = true;
					for (int i = 0; i < clueList.getModel().getSize(); i++) {
						String clue = clueList.getModel().getElementAt(i).toString();
						if (clue.startsWith(current.getNote().getText() + "d")) {
							clueList.setSelectedValue(clue, true);
							selectedClue = clue;
						}
					}
					listLocked = false;
					recolor();
					return;
				}
			}
	}
	
	// keyboard listening
	
    private class KeyDispatcher implements KeyEventDispatcher {
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getID() == KeyEvent.KEY_PRESSED && inputPopup.isShowing())
            	if (event.getKeyCode() == KeyEvent.VK_ENTER) placeEdit();
            	else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) inputPopup.hidePopup();
            return false;
        }
    }
	
	// utility methods
	
	// recolors the grid
	private void recolor() {
		crosswordPanel.setVisible(false);
		for (int y = 0; y < crossword.getSize(); y++)
			for (int x = 0; x < crossword.getSize(); x++) {
				// color the marked squares
				if (crossword.getGrid()[x][y].isMarked()) {
					crossword.getGrid()[x][y].getPanel().setBackground(Color.decode("#D3D3D3"));
					crossword.getGrid()[x][y].getDisplayed().setForeground(Color.BLACK);
					crossword.getGrid()[x][y].getNote().setForeground(Color.BLACK);
					crossword.getGrid()[x][y].fixNote();
				}
				// color the white squares
				else if (crossword.getGrid()[x][y].getOriginalColor() != 1) {
					crossword.getGrid()[x][y].getPanel().setBackground(Color.WHITE);
					crossword.getGrid()[x][y].getDisplayed().setForeground(Color.BLACK);
					crossword.getGrid()[x][y].getNote().setForeground(Color.BLACK);
				}
				// color the black squares
				else crossword.getGrid()[x][y].getPanel().setBackground(Color.BLACK);
			}
		crosswordPanel.setVisible(true);
	}
	
	public void placeEdit() {
		// word editing
		if (inputMode == 0) {
			if (!confirmButton.isEnabled()) return;
			int x = editedX;
			int y = editedY;
			String inputWord = inputField.getText().toLowerCase();
			if (selectedDirection == 0) {
				// change the across hashmap
				int wordNumber = Integer.parseInt(crossword.getGrid()[x][y].getNote().getText());
				String clue = "" + crossword.getAcross().get(wordNumber)[1];
				crossword.getAcross().remove(wordNumber);
				String[] wordData = {inputWord, clue};
				crossword.getAcross().put(wordNumber, wordData);
				// change the displayed grid
				inputWord = inputWord.toUpperCase();
				for (int l = 0; l < originalWordLength; l++) {
					crossword.getGrid()[x][y].getDisplayed().setText("" + inputWord.charAt(l));
					x++;
				}
			}
			else {
				// change the down hashmap
				int wordNumber = Integer.parseInt(crossword.getGrid()[x][y].getNote().getText());
				String clue = "" + crossword.getDown().get(wordNumber)[1];
				crossword.getDown().remove(wordNumber);
				String[] wordData = {inputWord, clue};
				crossword.getDown().put(wordNumber, wordData);
				// change the displayed grid
				inputWord = inputWord.toUpperCase();
				for (int l = 0; l < originalWordLength; l++) {
					crossword.getGrid()[x][y].getDisplayed().setText("" + inputWord.charAt(l));
					y++;
				}
			}
		}
		// clue editing
		else {
			// change the value in the list
			String[] separated = selectedClue.split(" ");
			ListModel model = clueList.getModel();
			Object[] data = new Object[model.getSize()];
			for (int i = 0; i < model.getSize(); i++)
				if (i == clueList.getSelectedIndex())
					data[i] = separated[0] + " " + inputField.getText() + " " + separated[separated.length-1];
				else
					data[i] = model.getElementAt(i);
			clueList.setListData(data);
			// cause list selection
			for (int i = 0; i < clueList.getModel().getSize(); i++) {
				String clue = clueList.getModel().getElementAt(i).toString();
				if (clue.substring(0, 2).equals(selectedClue.substring(0, 2))) {
					clueList.setSelectedValue(clue, true);
					selectedClue = clue;
				}
			}
		}
		inputPopup.hidePopup(); // hide the pop up
	}
	
	public JPanel getView() {
		return view;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	// when used with "10a. <Clue Name> (5)" - returns "10a"
	public String findPosition(String selectedItem) {
		String position = "";
		for (int i = 0; i < selectedItem.length(); i++) {
			position += selectedItem.charAt(i);
			if (selectedItem.charAt(i) == 'a' || selectedItem.charAt(i) == 'd') break;
		}
		return position;
	}
	
	private class NewCrosswordListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ready = true;
		}	
	}
	
	private class SaveCrosswordListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// placeholder
		}	
	}
	
	private class LoadCrosswordListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// just replace this line with code that builds a complete crossword from a text file:
			crossword = new Crossword();
			reloading = true;
			ready = true;
		}	
	}
	
	private class ExportTemplateListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
    		chooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
    		chooser.setDialogTitle("Select File");
			int returnValue = chooser.showSaveDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				try {
					File file = new File(chooser.getSelectedFile() + ".txt");
					PrintWriter writer = new PrintWriter(file, "UTF-8");
					String output = "";
					for (int y=0; y < crossword.getSize(); y++) {
						for (int x=0; x < crossword.getSize(); x++) {
							if (crossword.getGrid()[x][y].getOriginalColor() == 0)
								output += "-";
							else
								output += "#";
						}
						output += "\n";
					}
					writer.println(output);
					writer.close();
				} catch (IOException e) {}
			}
		}	
	}
	
	public class exportPdfListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			// grab the modified clues
			ArrayList<String> clues = new ArrayList<String>();
			ListModel model = clueList.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				if (((String) model.getElementAt(i)).startsWith("<html")) {
					clues.add(" ");
					clues.add(list.get(i).substring(9, list.get(i).length() - 11) + " ");
					clues.add(" ");
				}
				else {
					String cropped = "";
					String[] separated = ((String) model.getElementAt(i)).split(" ");
					for (int j = 0; j < separated.length - 1; j++) {
						cropped += separated[j];
						if (j != separated.length - 2) cropped += " ";
					}
					clues.add(cropped);
				}
			}
			FilePDF pdf = new FilePDF(crossword, clues);
		}
	}
	
	public boolean isReloading() {
		return reloading;
	}
	
	public Crossword getCrossword() {
		return crossword;
	}

}
