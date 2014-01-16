import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
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
import javax.swing.ListModel;
import javax.swing.SpringLayout;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.alee.laf.list.WebListCellRenderer;

public class PlayerView {

	private JPanel view, crosswordPanel, optionsPanel, cluePanel;
	private JMenuBar settingsMenuBar;
	private JMenu fileMenu, viewMenu, helpMenu;
	private JList clueList;
	private JButton checkButton, checkAllButton, cheatButton, solutionButton;
	private Crossword crossword;
	private String selectedClue;
	private int selectedX, selectedY, selectedDirection /* 0 - across / 1 - down */;
	private boolean acrossSelection, downSelection, listLocked, ready;
	private Color selectionColor;
	
	public PlayerView(Crossword c) {
		crossword = c;
		view = new JPanel();
		view.setBackground(Color.decode("#F5F5F5"));
		SpringLayout springLayout = new SpringLayout();
		view.setLayout(springLayout);
		// enable the selection of words
		for (int y = 0; y < crossword.getSize(); y++)
			for (int x = 0; x < crossword.getSize(); x++)
				crossword.getGrid()[x][y].getPanel().addMouseListener(new MouseSelectionListener());
		view.addMouseListener(new MouseDeselectionListener());
		selectedClue = "";
		selectedX = 0;
		selectedY = 0;
		selectedDirection = 0;
		listLocked = false;
		selectionColor = Color.decode("#4281F4");
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
		
		// contains the crossword with the solution not displayed	
		crosswordPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, crosswordPanel, 25, SpringLayout.SOUTH, settingsMenuBar);
		springLayout.putConstraint(SpringLayout.WEST, crosswordPanel, 25, SpringLayout.WEST, view);
		springLayout.putConstraint(SpringLayout.SOUTH, crosswordPanel, -26, SpringLayout.SOUTH, view);
		springLayout.putConstraint(SpringLayout.EAST, crosswordPanel, -327, SpringLayout.EAST, view);
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
		clueList.setFont(new Font(clueList.getFont().getName(), Font.PLAIN, 15));
		clueList.addListSelectionListener(new ClueListListener());
		clueList.addMouseMotionListener(new ToolTipSupport());
		clueList.setCellRenderer(new BoldItemRenderer());
		clueList.setFocusable(false);
		clueScrollPane.setViewportView(clueList);
		
		// play buttons
		
		// check button
		checkButton = new JButton("Check");
		checkButton.addActionListener(new CheckButtonListener());
		optionsLayout.putConstraint(SpringLayout.NORTH, checkButton, 10, SpringLayout.SOUTH, cluePanel);
		optionsLayout.putConstraint(SpringLayout.WEST, checkButton, 0, SpringLayout.WEST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.EAST, checkButton, -153, SpringLayout.EAST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.SOUTH, checkButton, -42, SpringLayout.SOUTH, optionsPanel);
		checkButton.setFont(new Font(checkButton.getFont().getName(), Font.PLAIN, 15));
		checkButton.setFocusable(false);
		optionsPanel.add(checkButton);
		
		// check all button
		checkAllButton = new JButton("Check All");
		checkAllButton.addActionListener(new CheckAllButtonListener());
		optionsLayout.putConstraint(SpringLayout.NORTH, checkAllButton, 10, SpringLayout.SOUTH, cluePanel);
		optionsLayout.putConstraint(SpringLayout.WEST, checkAllButton, 121, SpringLayout.WEST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.EAST, checkAllButton, 0, SpringLayout.EAST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.SOUTH, checkAllButton, -42, SpringLayout.SOUTH, optionsPanel);
		checkAllButton.setFont(new Font(checkAllButton.getFont().getName(), Font.PLAIN, 15));
		checkAllButton.setFocusable(false);
		optionsPanel.add(checkAllButton);
		
		// cheat button
		cheatButton = new JButton("Cheat");
		cheatButton.addActionListener(new CheatButtonListener());
		optionsLayout.putConstraint(SpringLayout.NORTH, cheatButton, 10, SpringLayout.SOUTH, checkButton);
		optionsLayout.putConstraint(SpringLayout.WEST, cheatButton, 0, SpringLayout.WEST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.EAST, cheatButton, -153, SpringLayout.EAST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.SOUTH, cheatButton, 0, SpringLayout.SOUTH, optionsPanel);
		cheatButton.setFont(new Font(cheatButton.getFont().getName(), Font.PLAIN, 15));
		cheatButton.setFocusable(false);
		optionsPanel.add(cheatButton);
		
		// show solution button
		solutionButton = new JButton("Solution");
		solutionButton.addActionListener(new SolutionButtonListener());
		optionsLayout.putConstraint(SpringLayout.NORTH, solutionButton, 10, SpringLayout.SOUTH, checkAllButton);
		optionsLayout.putConstraint(SpringLayout.WEST, solutionButton, 121, SpringLayout.WEST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.EAST, solutionButton, 0, SpringLayout.EAST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.SOUTH, solutionButton, 0, SpringLayout.SOUTH, optionsPanel);
		solutionButton.setFont(new Font(solutionButton.getFont().getName(), Font.PLAIN, 15));
		solutionButton.setFocusable(false);
		optionsPanel.add(solutionButton);
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
	
	// listeners for the play buttons
	
    private class CheckButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			for (int y = 0; y < crossword.getSize(); y++)
				for (int x = 0; x < crossword.getSize(); x++)
					if (crossword.getGrid()[x][y].isMarked())
						crossword.getGrid()[x][y].check();
		}
    }
    
    private class CheckAllButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			for (int y = 0; y < crossword.getSize(); y++)
				for (int x = 0; x < crossword.getSize(); x++)
					crossword.getGrid()[x][y].check();
		}
    }
    
    private class CheatButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			for (int y = 0; y < crossword.getSize(); y++)
				for (int x = 0; x < crossword.getSize(); x++)
					if (crossword.getGrid()[x][y].isMarked())
						crossword.getGrid()[x][y].solve();
		}
    }
     	
    private class SolutionButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			for (int y = 0; y < crossword.getSize(); y++)
				for (int x = 0; x < crossword.getSize(); x++)
					crossword.getGrid()[x][y].solve();
		}
    }
	
	// listener for selections in the list of clues
    private class ClueListListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
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
							// case 2 - clicking on the same square
							else if (x == selectedX && y == selectedY) {
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
							// case 3 - clicking on a different square in the same word - repeat selection
							else {
								acrossSelection = (selectedDirection == 0);
								downSelection = (selectedDirection == 1);
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
			if (source.getBackground() == Color.BLACK || source.getBackground() == selectionColor) return;
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
	
	// enables typing letters via the keyboard
    private class KeyDispatcher implements KeyEventDispatcher {
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getID() == KeyEvent.KEY_PRESSED) {
            	if (event.getKeyCode() == KeyEvent.VK_DELETE) delete(false);
            	else if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE) delete(true);
            	else if (event.getKeyCode() == KeyEvent.VK_SPACE) type('a', true);
            	else type(event.getKeyChar(), false);
            }
            return false;
        }
    }
    
    // enters a letter in the crossword
	public void type(char c, boolean space) {
		// obtain an upper case letter from c
		String letter = ("" + c).toUpperCase();
		if (space || letter.matches("[A-Z]")) {
			// put the character in the selected square
			if (space) crossword.getGrid()[selectedX][selectedY].getDisplayed().setText("");
			else crossword.getGrid()[selectedX][selectedY].getDisplayed().setText(letter);
			// move the selection to the next square
			if (selectedDirection == 0) {
				if (selectedX + 1 < crossword.getSize())
					if (crossword.getGrid()[selectedX+1][selectedY].getPanel().getBackground() != Color.BLACK) {
						crossword.getGrid()[selectedX+1][selectedY].setSelected(true);
						crossword.getGrid()[selectedX][selectedY].setSelected(false);
						selectedX++;
					}
			}
			else {
				if (selectedY + 1 < crossword.getSize())
					if (crossword.getGrid()[selectedX][selectedY+1].getPanel().getBackground() != Color.BLACK) {
						crossword.getGrid()[selectedX][selectedY+1].setSelected(true);
						crossword.getGrid()[selectedX][selectedY].setSelected(false);
						selectedY++;
					}
			}
			// redraw the crossword
			recolor();
		}
	}
	
    // deletes a letter in the crossword
	public void delete(boolean backspace) {
		crossword.getGrid()[selectedX][selectedY].getDisplayed().setText("");
		if (backspace) { // move the selection back
			if (selectedDirection == 0) {
				if (selectedX > 0)
					if (crossword.getGrid()[selectedX-1][selectedY].getPanel().getBackground() != Color.BLACK) {
						crossword.getGrid()[selectedX-1][selectedY].setSelected(true);
						crossword.getGrid()[selectedX][selectedY].setSelected(false);
						selectedX--;
					}
			}
			else {
				if (selectedY > 0)
					if (crossword.getGrid()[selectedX][selectedY-1].getPanel().getBackground() != Color.BLACK) {
						crossword.getGrid()[selectedX][selectedY-1].setSelected(true);
						crossword.getGrid()[selectedX][selectedY].setSelected(false);
						selectedY--;
					}
			}
			// redraw the crossword
			recolor();
		}
	}
	
	// utility methods
	
	// recolors the grid
	private void recolor() {	
		for (int y = 0; y < crossword.getSize(); y++)
			for (int x = 0; x < crossword.getSize(); x++) {
				// fix the borders
				crossword.getGrid()[x][y].getPanel().setBorder(BorderFactory.createLineBorder(Color.BLACK));
				// color the selected square
				if (crossword.getGrid()[x][y].isSelected()) {
					crossword.getGrid()[x][y].getPanel().setBackground(selectionColor);
					crossword.getGrid()[x][y].getDisplayed().setForeground(Color.WHITE);
					crossword.getGrid()[x][y].getNote().setForeground(Color.WHITE);
					crossword.getGrid()[x][y].fixNote();
					continue;
				}
				// the rest are for the unselected squares
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
	
	public JPanel getView() {
		return view;
	}
	
	public boolean isReady() {
		return ready;
	}

}
