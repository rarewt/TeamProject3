public class Word {

	String Word;
	int[][] index;
	
	// constructor with parameter for word string
	public Word(String word) {
		Word = word;

		index = new int[26][word.length()];
	}

	// null constructor
	public Word() {
		Word = "";

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

}
