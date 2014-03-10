import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Dictionary {
	// this class is used to store words of the same size in each instance
	//

	// initialise variables
	ArrayList<Word> words;
	Dictionary next, prev;
	int count;
	int wordSize;

	// null constructor
	public Dictionary() {
		words = new ArrayList<Word>();
		count = 0;
		wordSize = 0;
	}

	// constructor using size parameter
	public Dictionary(int s) {
		words = new ArrayList<Word>();
		count = 0;
		wordSize = s;
	}

	// getters and setters
	public Dictionary getNext() {
		return next;
	}

	public void setNext(Dictionary next) {
		this.next = next;
	}

	public Dictionary getPrev() {
		return prev;
	}

	public void setPrev(Dictionary prev) {
		this.prev = prev;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getWordSize() {
		return wordSize;
	}

	public void setWordSize(int size) {
		this.wordSize = size;
	}

	// check if dictionary is empty
	public boolean isEmpty() {
		return words.isEmpty();
	}

	// add a new word into the dictionary
	public void add(Word w) {
		this.words.add(w);
		count++;
	}

	public void setupMatrices() {
		File projectDir = new File(System.getProperty("user.dir"));
		File dataDir = new File(projectDir, "data");
	    File matricesDir = new File(dataDir, "matrices");
		File m = new File(matricesDir, "matrices" + this.getWordSize() + ".txt");
		if (!m.exists()) {
			CreateMatrices();
		}
		int pos = this.getCount();
		try {
			BufferedReader input = new BufferedReader(new FileReader(m));
			Word cursor = null;

			while (pos > 0) {
				pos--;
				int[][] index = new int[this.getWordSize()][26];

				for (int j = 0; j < 26; j++) {
					String s = input.readLine();
					String val[] = s.split(" ");
					for (int i = 0; i < this.getWordSize(); i++) {
						index[i][j] = Integer.parseInt(val[i]);
					}
				}
				cursor = words.get(pos);

				cursor.setIndex(index);
			}
			input.close();
		} catch (IOException e) {
		}
	}

	private void CreateMatrices() {
		int pos = this.getCount();
		Word cursor = null;
		int[][] index = new int[this.getWordSize()][26];
		// initialise all index values to avoid nulls
		for (int i = 0; i < this.getWordSize(); i++) {
			for (int j = 0; j < 26; j++) {
				index[i][j] = 0;
			}
		}
		try {
			File projectDir = new File(System.getProperty("user.dir"));
			File dataDir = new File(projectDir, "data");
		    File matricesDir = new File(dataDir, "matrices");
			File m = new File(matricesDir, "matrices" + this.getWordSize() + ".txt");
			BufferedWriter output = new BufferedWriter(new FileWriter(m));

			while (cursor != this.words.get(0)) {

				pos--;
				cursor = this.words.get(pos);

				for (int j = 0; j < 26; j++) {
					for (int i = 0; i < this.getWordSize(); i++) {
						output.write(index[i][j] + " ");
					}
					output.write('\n');
				}
				for (int i = 0; i < this.getWordSize(); i++) {
					int j = (cursor.getWord().charAt(i) - 'a');
					index[i][j] = pos;
				}
			}

			output.close();
		} catch (IOException e) {
			System.out.print("Could not create file" + this.getWordSize());
		}
	}

	public Word getWord(int i) {
		if (i >= 0 && i < words.size()) {
			return this.words.get(i);
		}
		return null;
	}

	//find word in dictionary to get appropriate clue
	public String getClue(String word) {
		for (int i = 0; i < words.size(); ++i)
			if (word.equals(words.get(i).getWord()))
				return words.get(i).getClue();
		return null;
	}

}
