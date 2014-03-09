import java.awt.Color;
import java.util.HashMap;
import java.util.ArrayList;

public class Filler {

	private Square[][] grid;
	private HashMap<String, int[]> places; // contains coordinates
	private int total;
	boolean success = false;
	MasterDictionary dict;
	// ArrayList<String> dict = new ArrayList<String>();
	/*
	 * File file = new File("dict.txt"); {
	 * 
	 * try { Scanner input = new Scanner(file); while (input.hasNextLine()) {
	 * dict.add(input.nextLine()); } } catch (FileNotFoundException e) {
	 * System.out.println("file not found"); }
	 * 
	 * }
	 */
	ArrayList<String> exists = new ArrayList<String>();

	public Filler(Square[][] sq, MasterDictionary d) {
		grid = sq;
		dict = d;
		places = new HashMap<String, int[]>();
		for (int y = 0; y < grid.length; y++)
			for (int x = 0; x < grid.length; x++) {
				if (grid[x][y].getNote().getText() != "") {
					int[] position = { x, y }; // use (int[]) when getting
					places.put(grid[x][y].getNote().getText(), position);
				}
				grid[x][y].assignOriginalColor();
			}
		total = places.keySet().toArray().length;
		success = false;
	}

	// using this with '1' returns the square that
	// begins the word(s) 1a and/or 1d and so on
	public Square findSquare(int i) {
		int x = ((int[]) places.get("" + i))[0];
		int y = ((int[]) places.get("" + i))[1];
		return grid[x][y];
	}

	// using this with '1' returns the word for 1a
	// if the word is incomplete, each missing letter will be replaced by '-'
	public String findAcrossWord(int i) {
		String word = "";
		int y = ((int[]) places.get("" + i))[1];
		for (int x = ((int[]) places.get("" + i))[0]; x <= grid.length - 1; x++) {
			if (grid[x][y].getPanel().getBackground() == Color.BLACK)
				break;
			word += grid[x][y].getLetter();
		}
		return word;
	}

	// using this with '1' returns the word for 1d
	// if the word is incomplete, each missing letter will be replaced by '-'
	public String findDownWord(int i) {
		String word = "";
		int x = ((int[]) places.get("" + i))[0];
		for (int y = ((int[]) places.get("" + i))[1]; y <= grid.length - 1; y++) {
			if (grid[x][y].getPanel().getBackground() == Color.BLACK)
				break;
			word += grid[x][y].getLetter();
		}
		return word;
	}

	// using this with '1' makes 1a equal to s
	public void putAcrossWord(int i, String s) {
		int x = ((int[]) places.get("" + i))[0];
		int y = ((int[]) places.get("" + i))[1];
		for (int l = 0; l < s.length(); l++) {
			grid[x][y].setLetter(s.charAt(l));
			x++;
		}
	}

	// using this with '1' makes 1d equal to s
	public void putDownWord(int i, String s) {
		int x = ((int[]) places.get("" + i))[0];
		int y = ((int[]) places.get("" + i))[1];
		for (int l = 0; l < s.length(); l++) {
			grid[x][y].setLetter(s.charAt(l));
			y++;
		}
	}

	// print all annotations
	public void printGrid() {
		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < grid.length; x++)
				System.out.print(grid[x][y].toString(0));
			System.out.print("\n");
		}
		System.out.println();
	}

	// print all letters
	public void printLetters() {
		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < grid.length; x++)
				System.out.print(grid[x][y].toString(1));
			System.out.print("\n");
		}
		System.out.println();
	}

	// recursive function to fill grid with letters
	public void fill(int i, int direction) {
		if (direction == 0) {
			if (findSquare(i).startsAcrossWord()) {
				applyAcrossWord(i);
			} else
				fill(i, 1);
		}

		else {
			if (findSquare(i).startsDownWord()) {
				applyDownWord(i);
			} else
				fill(i + 1, 0);
		}
	}

	private boolean matches(String word, String pattern) {
		if (word.length() != pattern.length())
			return false;
		else {
			for (int i = 0; i < word.length(); ++i) {
				if (pattern.charAt(i) != '-' && pattern.charAt(i) != word.charAt(i))
					return false;
			}
		}
		return true;
	}

	private boolean contains(ArrayList<String> dict, String word) {
		for (int i = 0; i < dict.size(); ++i)
			if (dict.get(i).equals(word))
				return true;
		return false;
	}

	private void applyAcrossWord(int i) {
		ArrayList<String> wordsTried=new ArrayList<String>();
		String pattern = findAcrossWord(i);
		Word word = dict.getFeasibleWord(pattern, dict.getRandom(pattern.length()));
		//System.out.println(pattern);
		while (!success && word != null) {

			if (matches(word.getWord(), pattern) && !contains(exists, word.getWord())) {
				putAcrossWord(i, word.getWord());
				exists.add(word.getWord());
				if (i == total)
					success = true;
				else
					fill(i, 1);
			}
			if (!success) {
				putAcrossWord(i, pattern);
				if(exists.contains(word.getWord())){
				exists.remove(word.getWord());}
				wordsTried.add(word.getWord());
			}
			word = dict.getFeasibleWord(pattern, word);
			if(wordsTried.contains(word.getWord())){
				word=null;
			}
		}
	}

	private void applyDownWord(int i) {
		ArrayList<String> wordsTried=new ArrayList<String>();
		String pattern = findDownWord(i);
		Word word = dict.getFeasibleWord(pattern, dict.getRandom(pattern.length()));
		//System.out.println(pattern);

		while (!success && word != null) {

			if (matches(word.getWord(), pattern) && !contains(exists, word.getWord())) {
				putDownWord(i, word.getWord());
				exists.add(word.getWord());
				if (i == total)
					success = true;
				else
					fill(i + 1, 0);
			}
			if (!success) {
				putDownWord(i, pattern);
				if(exists.contains(word.getWord())){
				exists.remove(word.getWord());}
				wordsTried.add(word.getWord());
			}
			word = dict.getFeasibleWord(pattern, word);
			if(wordsTried.contains(word.getWord())){
				word=null;
			}
		}

	}

	public Square[][] getGrid() {
		return grid;
	}
	
	public boolean emptyGrid() {
		for (int y = 0; y < grid.length; y++)
			for (int x = 0; x < grid.length; x++)
				if (grid[x][y].getLetter() != '-' && grid[x][y].getOriginalColor() == 0) return false;
		return true;
	}

	public void resetGrid() {
		for (int y = 0; y < grid.length; y++)
			for (int x = 0; x < grid.length; x++)
				if (grid[x][y].getOriginalColor() == 0) grid[x][y].setLetter('-');
	}
}
