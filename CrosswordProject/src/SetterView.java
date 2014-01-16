import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
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

import com.alee.extended.window.ComponentMoveAdapter;
import com.alee.laf.list.WebListCellRenderer;
import com.alee.managers.popup.WebPopup;
import com.alee.managers.popup.PopupStyle;


public class SetterView {

	private JPanel view, crosswordPanel, optionsPanel, cluePanel;
	private JMenuBar settingsMenuBar;
	private JMenu fileMenu, viewMenu, helpMenu;
	private JList clueList;
	private Crossword crossword;
	private JButton editWordButton, editClueButton, resetButton, confirmButton, cancelButton;
	private String selectedClue;
	private int selectedX, selectedY, editedX, editedY, originalWordLength, selectedDirection /* 0 - across / 1 - down */;
	private boolean acrossSelection, downSelection, listLocked, ready;
	private ListModel originalListModel;
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
		
		// enable the keyboard listening
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyDispatcher());
		
		// set the delay time for all tool tips
		ToolTipManager.sharedInstance().setInitialDelay(500);
		
		// top menu bar
		settingsMenuBar = new JMenuBar();
		springLayout.putConstraint(SpringLayout.NORTH, settingsMenuBar, 0, SpringLayout.NORTH, view);
		springLayout.putConstraint(SpringLayout.WEST, settingsMenuBar, 0, SpringLayout.WEST, view);
		springLayout.putConstraint(SpringLayout.SOUTH, settingsMenuBar, 38, SpringLayout.NORTH, view);
		springLayout.putConstraint(SpringLayout.EAST, settingsMenuBar, 0, SpringLayout.EAST, view);
		settingsMenuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#AAAAAA")));
		view.add(settingsMenuBar);
		
		// contains the crossword with the solution displayed	
		crosswordPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, crosswordPanel, 25, SpringLayout.SOUTH, settingsMenuBar);
		springLayout.putConstraint(SpringLayout.WEST, crosswordPanel, 19, SpringLayout.WEST, view);
		springLayout.putConstraint(SpringLayout.SOUTH, crosswordPanel, -26, SpringLayout.SOUTH, view);
		springLayout.putConstraint(SpringLayout.EAST, crosswordPanel, -333, SpringLayout.EAST, view);	
		crosswordPanel.setLayout(new BorderLayout());
		crosswordPanel.add(crossword.getVisuals());
		view.add(crosswordPanel);
		
		// contains options for editing the crossword
		optionsPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, optionsPanel, 25, SpringLayout.SOUTH, settingsMenuBar);
		springLayout.putConstraint(SpringLayout.WEST, optionsPanel, 28, SpringLayout.EAST, crosswordPanel);
		springLayout.putConstraint(SpringLayout.SOUTH, optionsPanel, -26, SpringLayout.SOUTH, view);
		springLayout.putConstraint(SpringLayout.EAST, optionsPanel, -23, SpringLayout.EAST, view);
		optionsPanel.setBackground(Color.decode("#F5F5F5"));
		SpringLayout optionsLayout = new SpringLayout();
		optionsPanel.setLayout(optionsLayout);
		view.add(optionsPanel);
		
		// contains the list of clues
		cluePanel = new JPanel();
		optionsLayout.putConstraint(SpringLayout.NORTH, cluePanel, 0, SpringLayout.NORTH, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.WEST, cluePanel, 0, SpringLayout.WEST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.SOUTH, cluePanel, -85, SpringLayout.SOUTH, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.EAST, cluePanel, 0, SpringLayout.EAST, optionsPanel);
		cluePanel.setLayout(new BorderLayout());
		optionsPanel.add(cluePanel);
				
		// individual menu components
		settingsMenuBar.add(new JLabel("    "));
		JMenu fileMenu = new JMenu("File");
		fileMenu.setFont(new Font(fileMenu.getFont().getName(), Font.PLAIN, 15));
		settingsMenuBar.add(fileMenu);
		settingsMenuBar.add(new JLabel(" "));
		
		// new crossword menu item
		JMenuItem newCrossword = new JMenuItem("New Crossword");
		newCrossword.setFont(new Font(newCrossword.getFont().getName(), Font.PLAIN, 15));
		newCrossword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inputPopup.hidePopup();
				ready = true;
			}
		});
		fileMenu.add(newCrossword);
		
		// exit program menu item
		JMenuItem exitProgram = new JMenuItem("Exit");
		exitProgram.setFont(new Font(exitProgram.getFont().getName(), Font.PLAIN, 15));
		exitProgram.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(exitProgram);
		
		JMenu viewMenu = new JMenu("View");
		viewMenu.setFont(new Font(viewMenu.getFont().getName(), Font.PLAIN, 15));
		settingsMenuBar.add(viewMenu);
		settingsMenuBar.add(new JLabel(" "));
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setFont(new Font(helpMenu.getFont().getName(), Font.PLAIN, 15));
		settingsMenuBar.add(helpMenu);
		
		// scroll pane for the list of clues
		JScrollPane clueScrollPane = new JScrollPane();
		cluePanel.add(clueScrollPane, BorderLayout.CENTER);
		
		// list of all clues
		if (!crossword.getAcross().isEmpty() || !crossword.getDown().isEmpty()) {
			Object[] across = crossword.getAcross().keySet().toArray();
			Object[] down = crossword.getDown().keySet().toArray();
			Arrays.sort(across); Arrays.sort(down);
			ArrayList<String> list = new ArrayList<String>();
			list.add("Across");
			for (int i = 0; i < across.length; i++)
				list.add(across[i] + "a. " + crossword.getAcross().get(across[i])[1] +
						 " (" + crossword.getAcross().get(across[i])[0].length() + ")");
			list.add("Down");
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
		clueList.setCellRenderer(new BoldItemRenderer());
		clueList.setFocusable(false);
		clueScrollPane.setViewportView(clueList);
		
		// set buttons
		
		// edit word button
		editWordButton = new JButton("Edit Word");
		editWordButton.addActionListener(new EditWordButtonListener());
		optionsLayout.putConstraint(SpringLayout.NORTH, editWordButton, 10, SpringLayout.SOUTH, cluePanel);
		optionsLayout.putConstraint(SpringLayout.WEST, editWordButton, 0, SpringLayout.WEST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.EAST, editWordButton, -140, SpringLayout.EAST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.SOUTH, editWordButton, -42, SpringLayout.SOUTH, optionsPanel);
		editWordButton.setFont(new Font(editWordButton.getFont().getName(), Font.PLAIN, 15));
		editWordButton.setFocusable(false);
		optionsPanel.add(editWordButton);
		
		// edit clue button
		editClueButton = new JButton("Edit Clue");
		editClueButton.addActionListener(new EditClueButtonListener());
		optionsLayout.putConstraint(SpringLayout.NORTH, editClueButton, 10, SpringLayout.SOUTH, cluePanel);
		optionsLayout.putConstraint(SpringLayout.WEST, editClueButton, 140, SpringLayout.WEST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.EAST, editClueButton, 0, SpringLayout.EAST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.SOUTH, editClueButton, -42, SpringLayout.SOUTH, optionsPanel);
		editClueButton.setFont(new Font(editClueButton.getFont().getName(), Font.PLAIN, 15));
		editClueButton.setFocusable(false);
		optionsPanel.add(editClueButton);
		
		// reset words button
		resetButton = new JButton("Reset Words + Clues");
		resetButton.addActionListener(new ResetButtonListener());
		optionsLayout.putConstraint(SpringLayout.NORTH, resetButton, 10, SpringLayout.SOUTH, editWordButton);
		optionsLayout.putConstraint(SpringLayout.WEST, resetButton, 0, SpringLayout.WEST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.EAST, resetButton, -0, SpringLayout.EAST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.SOUTH, resetButton, 0, SpringLayout.SOUTH, optionsPanel);
		resetButton.setFont(new Font(resetButton.getFont().getName(), Font.PLAIN, 15));
		resetButton.setFocusable(false);
		optionsPanel.add(resetButton);
		
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
            	if (!model.getElementAt(i).equals("Across") &&
            		!model.getElementAt(i).equals("Down"))
            		list.setToolTipText(model.getElementAt(i).toString());
            	else list.setToolTipText(null);
        }	
	}
    
	// renders 'Across' and 'Down' with bold font
	private class BoldItemRenderer extends WebListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
	                        int index, boolean isSelected, boolean cellHasFocus) {
	        JComponent component = (JComponent) super.getListCellRendererComponent(list,
	                							  value, index, isSelected, cellHasFocus);
	        if (index > -1 && (value.equals("Across") || (value.equals("Down"))))
	        	setFont(new Font(this.getFont().getName(), Font.BOLD, 15));
	        return component;
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
			placeEdit();
		}	
    }
    
    private class CancelButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			inputPopup.hidePopup(); // hide the pop up
		}
    }
    
    private class ResetButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
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
				if (clueList.getSelectedValue().equals("Across") || 
					clueList.getSelectedValue().equals("Down")) {
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
							if (crossword.getGrid()[x][y].getNote().getText().equals(
								(((String) clueList.getSelectedValue()).charAt(0)) + "")) { // target found
								// set the direction of the selection
								if (((String) clueList.getSelectedValue()).charAt(1) == 'a') {
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
						if (clue.substring(0, 2).equals(current.getNote().getText() + "a")) {
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
						if (clue.substring(0, 2).equals(current.getNote().getText() + "d")) {
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
		for (int y = 0; y < crossword.getSize(); y++)
			for (int x = 0; x < crossword.getSize(); x++) {
				// color the marked squares
				if (crossword.getGrid()[x][y].isMarked()) {
					crossword.getGrid()[x][y].getPanel().setBackground(Color.decode("#E2E2E2"));
					crossword.getGrid()[x][y].getDisplayed().setForeground(Color.BLACK);
					crossword.getGrid()[x][y].getNote().setForeground(Color.BLACK);
					crossword.getGrid()[x][y].fixNote();
				}
				// color the white squares
				else if (crossword.getGrid()[x][y].getLetter() != '-') {
					crossword.getGrid()[x][y].getPanel().setBackground(Color.WHITE);
					crossword.getGrid()[x][y].getDisplayed().setForeground(Color.BLACK);
					crossword.getGrid()[x][y].getNote().setForeground(Color.BLACK);
				}
				// color the black squares
				else crossword.getGrid()[x][y].getPanel().setBackground(Color.BLACK);
			}
	}
	
	public void placeEdit() {
		// word editing
		if (inputMode == 0) {
			int x = editedX;
			int y = editedY;
			String inputWord = inputField.getText().toUpperCase();
			if (selectedDirection == 0)
				for (int l = 0; l < originalWordLength; l++) {
					crossword.getGrid()[x][y].getDisplayed().setText("" + inputWord.charAt(l));
					x++;
				}
			else
				for (int l = 0; l < originalWordLength; l++) {
					crossword.getGrid()[x][y].getDisplayed().setText("" + inputWord.charAt(l));
					y++;
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

}
