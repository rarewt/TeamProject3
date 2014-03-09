public class MasterDictionary {
	// this class store all Dictionaries of of one size

	Dictionary head, tail;
	int count;
	int maxLength = 15;
	int minLength = 3;

	boolean success = false; // for choose method (backtracking example)

	public MasterDictionary() {
		count = 0;
		Dictionary d;
		// initialise first dictionary with size 2
		for (int i = minLength; i <= maxLength; i++) {

			d = new Dictionary(i);
			addDictionary(d);
			count++;
		}
	}

	// getters and setters
	public Dictionary getHead() {
		return head;
	}

	public void setHead(Dictionary head) {
		this.head = head;
	}

	public Dictionary getTail() {
		return tail;
	}

	public void setTail(Dictionary tail) {
		this.tail = tail;
	}

	public boolean isEmpty() {
		return head == null;
	}

	// add a new instance of a dictionary to the master dictionary
	public void addDictionary(Dictionary d) {
		// if the master dictionary is empty set head and tail
		if (isEmpty()) {
			this.head = d;
			this.tail = this.head;
		}
		// else set tail to new dictionary
		else {
			tail.setNext(d);
			d.setPrev(tail);
			this.tail = d;
		}
		count++;
	}

	public void process() {
		Dictionary cursor = this.getHead();
		while (cursor != null) {
			cursor.setupMatrices();
			cursor = cursor.getNext();
		}
	}

	// add a new word into its relevant dictionary
	public void addWord(String wrd, String clu) {
		Dictionary d = selectDictionary(wrd.length());
		Word w = new Word(wrd, clu);
		d.add(w);
	}

	// get first word in the list
	public Word getWord(int length) {
		Dictionary d = selectDictionary(length);
		Word w;

		w = d.getWord(0);
		return w;
	}

	public Word getRandom(int length) {
		Dictionary d = selectDictionary(length);
		Word w;
		w = d.getWord((int) (Math.random() * d.getCount()));
		return w;
	}

	// get next word after w
	public Word getFeasibleWord(String s, Word pre) {
		Dictionary d = this.selectDictionary(s.length());
		int start = d.words.indexOf(pre) + 1;
		int jump = -1;
		int first = -1;
		boolean looped = false;
		boolean done = false;
		// incase the search starts at the very end this avoids a null pointer
		if (start == d.getCount()) {
			start = 0;
		}
		Word w = d.getWord(start);
		Word test=w;
		while (!done) {
			//if(first>=1) System.out.println(d.words.get(first).Word+" "+test.getWord()+" "+w.getWord() +" "+ jump + " " + first);

			// for every letter
			for (int i = 0; i < s.length(); i++) {
				// ensures valid characters are used
				if ((s.charAt(i) - 'a') >= 0 && (s.charAt(i) - 'a') < 26) {
					// if loops round to zero or jump is less than next index
					// address

					if (w.getIndex()[i][s.charAt(i) - 'a'] == 0
							|| jump < (w.getIndex()[i][s.charAt(i) - 'a'])) {
						jump = w.getIndex()[i][s.charAt(i) - 'a'];

					}
				}
			}
			// get next word
			// System.out.println(w.getWord());
			if (jump > -1) {
				w = this.selectDictionary(s.length()).getWord(jump);
				if (first == -1) {
					first = jump;
				} else if (looped && (jump >= first||jump==0)) {
					return null;
				}
				if (jump == 0)
					looped = true;
			}
			// check if word fits pattern
			if (matches(w.getWord(), s)) {
				done = true;
			}
		}
		// return word
		//System.out.println();
		//System.out.println();
		return w;
	}

	private boolean matches(String word, String pattern) {
		if (word.length() != pattern.length())
			return false;
		else {
			for (int i = 0; i < word.length(); ++i) {
				if (pattern.charAt(i) != '-'
						&& pattern.charAt(i) != word.charAt(i))
					return false;
			}
		}
		return true;
	}

	// select appropriate dictionary for a word
	public Dictionary selectDictionary(int length) {
		Dictionary d = head;
		while (d.getWordSize() < length) {
			d = d.getNext();
		}
		return d;
	}

}
