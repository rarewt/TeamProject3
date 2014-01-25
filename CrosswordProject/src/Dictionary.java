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
	public void setupMatrices(){
		File dict = new File("matrices"+this.getWordSize()+".txt");
		int pos= this.getCount()-1;
		try {
			BufferedReader input = new BufferedReader(new FileReader(dict));
			Word cursor=this.words.get(pos);
			
			while(pos>0){
					int[][] index= new int[this.getWordSize()][26];
				
						for(int j=0;j<26;j++){
							String s=input.readLine();
							String val[]= s.split(" ");
							for(int i=0;i<this.getWordSize();i++){
								index[i][j]=Integer.parseInt(val[i]);
							}
						}
					pos--;
					cursor.setIndex(index);
					cursor=words.get(pos);

			}
			input.close();
		}  catch (IOException e) {
			processMatrices();
				}
			}

	private void processMatrices(){
		int location=getCount()-1;
		Word cursor=this.words.get(location);
		int pos=this.getCount()-1;
		int[][] index= new int[this.getWordSize()][26];
		//initialise all index values to avoid nulls
		for(int i=0;i<this.getWordSize();i++){
			for(int j=0;j<26;j++){
				index[i][j]=0;
			}
		}	
		 try {
			 File dict = new File("matrices"+this.getWordSize()+".txt");
	          BufferedWriter output = new BufferedWriter(new FileWriter(dict));
	          
	  		while(cursor!=this.words.get(0)){
				for(int i=0;i<this.getWordSize();i++){
					int j=(cursor.getWord().charAt(i)-'a');
					index[i][j]=pos;
				}
				location--;
				cursor=this.words.get(location);
				pos--;
			
				for(int j=0;j<26;j++){
					for(int i=0;i<this.getWordSize();i++){
					      output.write(index[i][j]+" ");
					}
					  output.write('\n');
				}
	          }
				
	          output.close();
	        } catch ( IOException e ) {
	           System.out.print("Could not create file"+this.getWordSize());
	        }
	}
		public Word getWord(int i){
			if(i>=0&&i<words.size()){
		return this.words.get(i);
			}
			return null;
	}

}
