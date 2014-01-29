public class Word {

	String Word;
	String Clue;
	int[][] index;
	
	// constructor with parameter for word string
	public Word(String word, String clue) {
		Word = word;
		Clue = clue;
		index = new int[26][word.length()];
	}

	// null constructor
	public Word() {
		Word = "";
		Clue = "Placeholder";
	}

	// getters and setters
	public int[][] getIndex() {
		return index;
	}

	public void setIndex(int[][] index) {
		this.index = index;
	}

	public String getWord() {
		return Word;
	}

	public void setWord(String word) {
		Word = word;
	}
	
	public void setClue(String clue) {
		Clue = clue;
	}
	
	public String getClue() {
		return Clue;
	}

}
