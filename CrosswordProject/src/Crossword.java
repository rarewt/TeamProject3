import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JPanel;

public class Crossword {
	
	// a 2D array of Square objects that represents the grid
	// of squares and the letters assigned to each square
	private Square[][] grid;
	
	// a panel that contains all basic panels from the Square objects and serves
	// as a visualization of the crossword (like the one in GridPreview)
	private JPanel visuals;
	
	// hash maps that map starting positions to words and clues -
	// e.g. key - "1" / value - ["crossword", "a type of word puzzle"]
	private HashMap<Integer, String[]> across;
	private HashMap<Integer, String[]> down;
	
	// crossword size
	private int size;

	// getters for all of the above variables
	public Square[][] getGrid() {
		return grid;
	}

	public JPanel getVisuals() {
		return visuals;
	}

	public HashMap<Integer, String[]> getAcross() {
		return across;
	}
	
	public HashMap<Integer, String[]> getDown() {
		return down;
	}
	
	public int getSize() {
		return size;
	}
	
	// final constructor
	public Crossword(Square[][] selectedGrid, MasterDictionary masterDictionary){
		grid = selectedGrid;
		size = grid.length;
		
		// presume grid is filled thanks to the filler class already
		
		// add the square panels to the visualization
		visuals = new JPanel();
		visuals.setBackground(Color.decode("#F5F5F5"));
		visuals.setLayout(new GridLayout(size, size));
		for (int y=0; y < size; y++) {
			for (int x=0; x < size; x++) {
				for (MouseListener ml : grid[x][y].getPanel().getMouseListeners())
					grid[x][y].getPanel().removeMouseListener(ml); // remove the old mouse listeners
				if (grid[x][y].getOriginalColor() == 0 && grid[x][y].getLetter() == '-')
					grid[x][y].makeBlack(); // self-fixing
				visuals.add(grid[x][y].getPanel());
			}
		}
				
		// create mappings to words and clues
		across = new HashMap<Integer, String[]>();
		for (int y=0; y < size; y++)
			for (int x=0; x < size; x++)
				if (grid[x][y].startsAcrossWord()) {
					String[] wordData = {findAcrossWord(x, y), masterDictionary.selectDictionary(findAcrossWord(x, y).length()).getClue(findAcrossWord(x, y))};
					across.put(Integer.parseInt(grid[x][y].getNote().getText()), wordData);
				}
		down = new HashMap<Integer, String[]>();
		for (int x=0; x < size; x++)
			for (int y=0; y < size; y++)
				if (grid[x][y].startsDownWord()) {
					String[] wordData = {findDownWord(x, y), masterDictionary.selectDictionary(findDownWord(x, y).length()).getClue(findDownWord(x, y))};
					down.put(Integer.parseInt(grid[x][y].getNote().getText()), wordData);
				}
		
		// improve the appearance of the clues
		for (String[] value : across.values()) {
			String str = "";
			boolean copyChar = false;
			for (int i = 0; i < value[1].length(); i++) {
				if (str.endsWith(".") && !(str.endsWith("Alt.") || str.endsWith("Pl.") || str.endsWith("P."))) break;
				if (value[1].charAt(i) == ')') {copyChar = true; continue;}
				if (value[1].charAt(i) == ';') break;
				if (copyChar) str += value[1].charAt(i);
			}
			if (!str.endsWith(".")) str += ".";
			str = str.substring(1);
			str = str.substring(0, 1).toUpperCase() + str.substring(1);
			value[1] = str;
		}
		for (String[] value : down.values()) {
			String str = "";
			boolean copyChar = false;
			for (int i = 0; i < value[1].length(); i++) {
				if (str.endsWith(".") && !(str.endsWith("Alt.") || str.endsWith("Pl."))) break;
				if (value[1].charAt(i) == ')') {copyChar = true; continue;}
				if (value[1].charAt(i) == ';') break;
				if (copyChar) str += value[1].charAt(i);
			}
			if (!str.endsWith(".")) str += ".";
			str = str.substring(1);
			str = str.substring(0, 1).toUpperCase() + str.substring(1);
			value[1] = str;
		}
	}	
	
	// the no argument constructor creates a test 5x5 crossword in the format
	// -----
	// -###-
	// -----
	// -###-
	// -----
	public Crossword() {
		// create an empty grid
		grid = new Square[5][5];
		for (int y = 0; y < 5; y++) {
			for (int x = 0; x < 5; x++) {
				grid[x][y] = new Square(5);
				grid[x][y].getPanel().setBackground(Color.WHITE);
			}
		}
		
		size = grid.length;
		
		grid[1][1].getPanel().setBackground(Color.BLACK);
		grid[2][1].getPanel().setBackground(Color.BLACK);
		grid[3][1].getPanel().setBackground(Color.BLACK);
		grid[1][3].getPanel().setBackground(Color.BLACK);
		grid[2][3].getPanel().setBackground(Color.BLACK);
		grid[3][3].getPanel().setBackground(Color.BLACK);
		
		grid[0][0].setStartsAcrossWord(true);
		grid[0][2].setStartsAcrossWord(true);
		grid[0][4].setStartsAcrossWord(true);
		grid[0][0].setStartsDownWord(true);
		grid[4][0].setStartsDownWord(true);
		
		grid[0][0].getNote().setText("" + 1);
		grid[0][0].fixNote();
		grid[4][0].getNote().setText("" + 2);
		grid[4][0].fixNote();
		grid[0][2].getNote().setText("" + 3);
		grid[0][2].fixNote();
		grid[0][4].getNote().setText("" + 4);
		grid[0][4].fixNote();
		
		// manually fill the grid with letters that form words
		grid[0][0].setLetter('a');
		grid[1][0].setLetter('g');
		grid[2][0].setLetter('i');
		grid[3][0].setLetter('l');
		grid[4][0].setLetter('e');
		
		grid[0][2].setLetter('s');
		grid[1][2].setLetter('o');
		grid[2][2].setLetter('r');
		grid[3][2].setLetter('t');
		grid[4][2].setLetter('s');
		
		grid[0][4].setLetter('t');
		grid[1][4].setLetter('a');
		grid[2][4].setLetter('l');
		grid[3][4].setLetter('e');
		grid[4][4].setLetter('s');
		
		grid[0][0].setLetter('a');
		grid[0][1].setLetter('s');
		grid[0][2].setLetter('s');
		grid[0][3].setLetter('e');
		grid[0][4].setLetter('t');
		
		grid[4][0].setLetter('e');
		grid[4][1].setLetter('a');
		grid[4][2].setLetter('s');
		grid[4][3].setLetter('e');
		grid[4][4].setLetter('s');
		
		// add the square panels to the visualization
		visuals = new JPanel();
		visuals.setBackground(Color.decode("#F5F5F5"));
		visuals.setLayout(new GridLayout(5, 5));
		for (int y=0; y < 5; y++) {
			for (int x=0; x < 5; x++) {
				visuals.add(grid[x][y].getPanel());
			}
		}
		
		// create mappings to words and clues
		across = new HashMap<Integer, String[]>();
		down = new HashMap<Integer, String[]>();
		
		String[] word1 = {"agile", "Characterized by quickness, lightness, and ease of movement"};
		across.put(1, word1);
		String[] word2 = {"asset", "Resource with economic value"};
		down.put(1, word2);
		String[] word3 = {"eases", "Makes easier"};
		down.put(2, word3);
		String[] word4 = {"sorts", "Places in order"};
		across.put(3, word4);
		String[] word5 = {"tales", "Traditional stories told in folklore"};
		across.put(4, word5);
	}
	
	public String findAcrossWord(int xCord, int yCord) {
		String word = "";
		for (int x = xCord; x <= grid.length-1; x++) {
			if (grid[x][yCord].getPanel().getBackground() == Color.BLACK) break;
			word += grid[x][yCord].getLetter();
		}
		return word;
	}
	
	public String findDownWord(int xCord, int yCord) {
		String word = "";
		for (int y = yCord; y <= grid.length-1; y++) {
			if (grid[xCord][y].getPanel().getBackground() == Color.BLACK) break;
			word += grid[xCord][y].getLetter();
		}
		return word;
	}
	
}
