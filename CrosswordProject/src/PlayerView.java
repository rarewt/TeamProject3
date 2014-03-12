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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileNameExtensionFilter;

public class PlayerView {

	private JPanel view, crosswordPanel, controlsPanel, cluePanel, optionsPanel;
	private JList clueList;
	ArrayList<String> list;
	private JButton checkButton, checkAllButton, cheatButton, solutionButton;
	private Crossword crossword;
	private String selectedClue;
	private int selectedX, selectedY, selectedDirection /* 0 - across / 1 - down */;
	private boolean acrossSelection, downSelection, listLocked, ready, reloading;
	private OptionsInterface options;
	private Color selectionColor;
	
	public PlayerView(Crossword c) {
		crossword = c;
		view = new JPanel();
		view.setBackground(Color.decode("#F5F5F5"));
		SpringLayout springLayout = new SpringLayout();
		view.setLayout(springLayout);
		// enable the selection of words
		for (int y = 0; y < crossword.getSize(); y++)
			for (int x = 0; x < crossword.getSize(); x++) {
				crossword.getGrid()[x][y].getPanel().addMouseListener(new MouseSelectionListener());
				if (crossword.getGrid()[x][y].getOriginalColor() == 1)
					crossword.getGrid()[x][y].getDisplayed().setForeground(Color.BLACK);
			}
		view.addMouseListener(new MouseDeselectionListener());
		selectedClue = "";
		selectedX = 0;
		selectedY = 0;
		selectedDirection = 0;
		listLocked = false;
		selectionColor = Color.decode("#4281F4");
		ready = false;
		reloading = false;
		
		// enable the keyboard listening
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
		
		// contains the crossword with the solution not displayed	
		crosswordPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, crosswordPanel, 25, SpringLayout.SOUTH, optionsPanel);
		springLayout.putConstraint(SpringLayout.WEST, crosswordPanel, 25, SpringLayout.WEST, view);
		springLayout.putConstraint(SpringLayout.SOUTH, crosswordPanel, -26, SpringLayout.SOUTH, view);
		springLayout.putConstraint(SpringLayout.EAST, crosswordPanel, -327, SpringLayout.EAST, view);
		crosswordPanel.setLayout(new BorderLayout());
		crosswordPanel.add(crossword.getVisuals());
		view.add(crosswordPanel);
		
		// contains controls for solving the crossword
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
		clueList.setFont(new Font(clueList.getFont().getName(), Font.PLAIN, 15));
		clueList.addListSelectionListener(new ClueListListener());
		clueList.addMouseMotionListener(new ToolTipSupport());
		clueList.setFocusable(false);
		clueScrollPane.setViewportView(clueList);
		
		// play buttons
		
		// check button
		checkButton = new JButton("Check");
		checkButton.addActionListener(new CheckButtonListener());
		controlsLayout.putConstraint(SpringLayout.NORTH, checkButton, 10, SpringLayout.SOUTH, cluePanel);
		controlsLayout.putConstraint(SpringLayout.WEST, checkButton, 0, SpringLayout.WEST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.EAST, checkButton, -153, SpringLayout.EAST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.SOUTH, checkButton, -42, SpringLayout.SOUTH, controlsPanel);
		checkButton.setFont(new Font(checkButton.getFont().getName(), Font.PLAIN, 15));
		checkButton.setFocusable(false);
		controlsPanel.add(checkButton);
		
		// check all button
		checkAllButton = new JButton("Check All");
		checkAllButton.addActionListener(new CheckAllButtonListener());
		controlsLayout.putConstraint(SpringLayout.NORTH, checkAllButton, 10, SpringLayout.SOUTH, cluePanel);
		controlsLayout.putConstraint(SpringLayout.WEST, checkAllButton, 121, SpringLayout.WEST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.EAST, checkAllButton, 0, SpringLayout.EAST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.SOUTH, checkAllButton, -42, SpringLayout.SOUTH, controlsPanel);
		checkAllButton.setFont(new Font(checkAllButton.getFont().getName(), Font.PLAIN, 15));
		checkAllButton.setFocusable(false);
		controlsPanel.add(checkAllButton);
		
		// cheat button
		cheatButton = new JButton("Cheat");
		cheatButton.addActionListener(new CheatButtonListener());
		controlsLayout.putConstraint(SpringLayout.NORTH, cheatButton, 10, SpringLayout.SOUTH, checkButton);
		controlsLayout.putConstraint(SpringLayout.WEST, cheatButton, 0, SpringLayout.WEST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.EAST, cheatButton, -153, SpringLayout.EAST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.SOUTH, cheatButton, 0, SpringLayout.SOUTH, controlsPanel);
		cheatButton.setFont(new Font(cheatButton.getFont().getName(), Font.PLAIN, 15));
		cheatButton.setFocusable(false);
		controlsPanel.add(cheatButton);
		
		// show solution button
		solutionButton = new JButton("Solution");
		solutionButton.addActionListener(new SolutionButtonListener());
		controlsLayout.putConstraint(SpringLayout.NORTH, solutionButton, 10, SpringLayout.SOUTH, checkAllButton);
		controlsLayout.putConstraint(SpringLayout.WEST, solutionButton, 121, SpringLayout.WEST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.EAST, solutionButton, 0, SpringLayout.EAST, controlsPanel);
		controlsLayout.putConstraint(SpringLayout.SOUTH, solutionButton, 0, SpringLayout.SOUTH, controlsPanel);
		solutionButton.setFont(new Font(solutionButton.getFont().getName(), Font.PLAIN, 15));
		solutionButton.setFocusable(false);
		controlsPanel.add(solutionButton);
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
	
	// listeners for the play buttons
	
    private class CheckButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			crosswordPanel.setVisible(false);
			for (int y = 0; y < crossword.getSize(); y++)
				for (int x = 0; x < crossword.getSize(); x++)
					if (crossword.getGrid()[x][y].isMarked())
						crossword.getGrid()[x][y].check();
			crosswordPanel.setVisible(true);
		}
    }
    
    private class CheckAllButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			crosswordPanel.setVisible(false);
			for (int y = 0; y < crossword.getSize(); y++)
				for (int x = 0; x < crossword.getSize(); x++)
					crossword.getGrid()[x][y].check();
			crosswordPanel.setVisible(true);
		}
    }
    
    private class CheatButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			crosswordPanel.setVisible(false);
			for (int y = 0; y < crossword.getSize(); y++)
				for (int x = 0; x < crossword.getSize(); x++)
					if (crossword.getGrid()[x][y].isMarked())
						crossword.getGrid()[x][y].solve();
			crosswordPanel.setVisible(true);
		}
    }
     	
    private class SolutionButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			crosswordPanel.setVisible(false);
			for (int y = 0; y < crossword.getSize(); y++)
				for (int x = 0; x < crossword.getSize(); x++)
					crossword.getGrid()[x][y].solve();
			crosswordPanel.setVisible(true);
		}
    }
	
	// listener for selections in the list of clues
    private class ClueListListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
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
							else if (x == selectedX && y == selectedY && shared(x, y)) {
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
		crosswordPanel.setVisible(false);
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
	
	public JPanel getView() {
		return view;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	// returns whether the square with coordinates x and y is shared between 2 words
	public boolean shared(int x, int y) {
		boolean left = false;
		boolean right = false;
		boolean up = false;
		boolean down = false;
		if (x > 0) if (crossword.getGrid()[x-1][y].getOriginalColor() != 1) left = true;
		if (x + 1 < crossword.getSize()) if (crossword.getGrid()[x+1][y].getOriginalColor() != 1) right = true;
		if (y > 0) if (crossword.getGrid()[x][y-1].getOriginalColor() != 1) up = true;
		if (y + 1 < crossword.getSize()) if (crossword.getGrid()[x][y+1].getOriginalColor() != 1) down = true;
		return (left && up) || (left && down) || (right && up) || (right && down);
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
			FilePDF pdf = new FilePDF(crossword, list);
		}
	}
	
	public boolean isReloading() {
		return reloading;
	}
	
	public Crossword getCrossword() {
		return crossword;
	}

}
