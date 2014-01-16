import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class GridPreview {
	
	private JPanel visuals;
	private Square[][] grid; // the crossword grid is visualized via a 2D array
	private int size;
	private boolean valid;
	private String unedited;

	public GridPreview(String template) {
		unedited = template;
		visuals = new JPanel();
		visuals.setBackground(Color.decode("#F5F5F5"));
		try {
			String[] rows = template.split(",");
			size = rows.length;
			visuals.setLayout(new GridLayout(size, size));
			grid = new Square[size][size];
			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					grid[x][y] = new Square(size);
					String row = rows[y];
					// throw an exception if there is an
					// error in the template for the grid
					if (row.length() != rows.length)
					throw new Exception("Bad Template");
					// assign color to the square
					if (row.charAt(x) == '#')
						grid[x][y].getPanel().setBackground(Color.BLACK);
					else if (row.charAt(x) == '-')
						grid[x][y].getPanel().setBackground(Color.WHITE);
					else 
						throw new Exception ("Bad Template");
					// the panels in the grid react to mouse movements and clicks
					grid[x][y].getPanel().addMouseListener(new SquareListener());
					visuals.add(grid[x][y].getPanel());
				}
			}
			update();
			valid = true;
		}
		// deal with templates that contain errors
		catch (Exception e) {
			visuals.setBackground(Color.WHITE);
			visuals.setLayout(new BorderLayout());
			JLabel warningLabel = new JLabel("Bad Template");
			warningLabel.setFont(new Font(warningLabel.getFont().getName(), Font.PLAIN, 15));
			warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
			visuals.add(warningLabel);
			valid = false;
		}
}

	public JPanel getVisuals() {
		return visuals;
	}

	public Square[][] getGrid() {
		return grid;
	}
	
	public int getSize() {
		return size;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	// returns a string representation
	// of the unedited preview
	public String toString() {
		return unedited;
	}
	
	// each crossword square in the preview has one of those listeners attached to it
	// clicking on a square will change its color and update the preview
	// also - the squares are highlighted on mouse over
	private class SquareListener implements MouseListener {
		public void mousePressed(MouseEvent event) {
			JPanel source = (JPanel) event.getSource();
			if (source.getBackground() == Color.BLACK)
				source.setBackground(Color.WHITE);
			else
				source.setBackground(Color.BLACK);
			update();
		}
		public void mouseEntered(MouseEvent event) {
			JPanel source = (JPanel) event.getSource();
			if (size >= 11) source.setBorder(BorderFactory.createLineBorder(Color.decode("#4281F4"), 3));
			else if (size >= 7) source.setBorder(BorderFactory.createLineBorder(Color.decode("#4281F4"), 4));
			else source.setBorder(BorderFactory.createLineBorder(Color.decode("#4281F4"), 5));
		}
		public void mouseExited(MouseEvent event) {
			JPanel source = (JPanel) event.getSource();
			source.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}
		public void mouseClicked(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}
		
	}
	
	public void update() {
		// process the across direction
		for (int y=0; y < size; y++) {
			for (int x=0; x < size; x++) {
				// also - reset all squares to default
				grid[x][y].setStartsAcrossWord(false);
				grid[x][y].setStartsDownWord(false);
				boolean feasible = (x == 0 || grid[x-1][y].getPanel().getBackground() == Color.BLACK);
				if (x+2 > size-1 || grid[x][y].getPanel().getBackground() == Color.BLACK) feasible = false;
				if (x+1 <= size-1) {if (grid[x+1][y].getPanel().getBackground() == Color.BLACK) feasible = false;}
				if (x+2 <= size-1) {if (grid[x+2][y].getPanel().getBackground() == Color.BLACK) feasible = false;}
				if (feasible) grid[x][y].setStartsAcrossWord(true);
			}
		}
		// process the down direction
		for (int x=0; x < size; x++) {
			for (int y=0; y < size; y++) {
				boolean feasible = (y == 0 || grid[x][y-1].getPanel().getBackground() == Color.BLACK);
				if (y+2 > size-1 || grid[x][y].getPanel().getBackground() == Color.BLACK) feasible = false;
				if (y+1 <= size-1) {if (grid[x][y+1].getPanel().getBackground() == Color.BLACK) feasible = false;}
				if (y+2 <= size-1) {if (grid[x][y+2].getPanel().getBackground() == Color.BLACK) feasible = false;}
				if (feasible) grid[x][y].setStartsDownWord(true);
			}
		}
		// update the preview
		valid = false;
		int count = 1;
		for (int y=0; y < size; y++) {
			for (int x=0; x < size; x++) {
				grid[x][y].getNote().setText("");
				if (grid[x][y].startsAcrossWord() || grid[x][y].startsDownWord() ) {
					grid[x][y].getNote().setText("" + count); // set annotation
					valid = true;
					count++;
				}
				grid[x][y].fixNote();
			}
		}
		visuals.revalidate();
		visuals.repaint();
	}
	
	// clears all edits from the preview
	public void reset() {
		if (valid) {
			String[] rows = unedited.split(",");
			for (int y=0; y < size; y++)
				for (int x=0; x < size; x++) {
					String row = rows[y];
					if (row.charAt(x) == '#')
						grid[x][y].getPanel().setBackground(Color.BLACK);
					else if (row.charAt(x) == '-')
						grid[x][y].getPanel().setBackground(Color.WHITE);
				}
			update();
		}
	}
}