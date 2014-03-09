import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Square {
	
	private JPanel panel; // background of the square
	private JLabel note; // word number (if the square begins a word)
	private JLabel displayed; // the letter currently displayed on the square
	private char letter; // the letter that the square should actually display
	private boolean acrossWord; // shows whether the square begins an across word
	private boolean downWord; // shows whether the square begins a down word
	private boolean selected; // shows whether this square is selected in Setter/Player mode
	private boolean marked; // shows whether this square is marked in Setter/Player mode
	private int size; // references the size of the grid formed by similar squares
	private int originalColor; // 0 for white and 1 for black
	
	public Square(int n) {
		acrossWord = false;
		downWord = false;
		letter = '-';
		
		selected = false;
		marked = false;
		size = n;
		
		panel = new JPanel();
		panel.setLayout(new GridLayout(3, 1, 0, 0));
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel.setFocusable(false);
		
		note = new JLabel("");
		// font size depends on grid size
		note.setFont(new Font(note.getFont().getName(), Font.PLAIN, 14 - size/2));
		panel.add(note);
		
		displayed = new JLabel(""); // empty by default
		displayed.setHorizontalAlignment(SwingConstants.CENTER);
		displayed.setVerticalAlignment(SwingConstants.CENTER);
		// font size depends on grid size
		displayed.setFont(new Font(displayed.getFont().getName(), Font.PLAIN, adjustFontSize(size)));
		panel.add(displayed);
	}
	
	// returns a suitable font size
	private int adjustFontSize(int size) {
		if (size == 13) return 13;
		else if (size == 11) return 15;
		else if (size == 9) return 18;
		else if (size == 7) return 22;
		else return 30;
	}

	public JPanel getPanel() {
		return panel;
	}
	
	public JLabel getNote() {
		return note;
	}

	public JLabel getDisplayed() {
		return displayed;
	}
	
	public char getLetter() {
		return letter;
	}
	
	public void setLetter(char c) {
		letter = c;
	}
	
	public boolean startsAcrossWord() {
		return acrossWord;
	}
	
	public boolean startsDownWord() {
		return downWord;
	}
	
	public void setStartsAcrossWord(boolean value) {
		acrossWord = value;
	}
	
	public void setStartsDownWord(boolean value) {
		downWord = value;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean value) {
		selected = value;
	}
	
	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean value) {
		marked = value;
	}
	
	public void fixNote() {
		// make some space between the grid border and the annotation
		if (size == 13) note.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
		else if (size == 11) note.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		else if (size == 9) note.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		else if (size == 7) note.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
		else note.setBorder(BorderFactory.createEmptyBorder(0, 11, 0, 0));
	}
	
	public void check() {
		if (!("" + letter).toUpperCase().equals(displayed.getText())) {
			displayed.setText("");
		}
	}
	
	public void solve() {
		displayed.setText(("" + letter).toUpperCase());
	}
	
	public String toString(int showLetters) {
		if (showLetters == 0) { // 0 returns the annotation or the square color
			if (panel.getBackground() == Color.WHITE) {
				if (acrossWord == true || downWord == true) return note.getText();
				else return "-";
			}
			else return "#";
		}
		else { // anything else returns the letter
			if (panel.getBackground() == Color.WHITE) return "" + letter;
			else return "#";
		}
	}
	
	public int getOriginalColor() {
		return originalColor;
	}
	
	public void assignOriginalColor() {
		if (panel.getBackground() == Color.WHITE) {
			originalColor = 0;
			return;
		}
		originalColor = 1;
	}
	
	public void makeBlack() {
		panel.setBackground(Color.BLACK);
		originalColor = 1;
	}
	
}