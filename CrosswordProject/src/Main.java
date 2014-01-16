import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.alee.laf.WebLookAndFeel;

public class Main {

	private static TemplateData templateData;
	private static Square[][] grid;
	private static JFrame frame;
	private static int mode;
	private static Crossword crossword;
	private static StartView startView;
	private static SetterView setterView;
	private static PlayerView playerView;

	// plus any other necessary variables

	public static void main(String[] args) {
		
		// custom look and feel
		WebLookAndFeel.install();
		
		// any dictionary processing goes here
		
		// load the crossword grid templates
		templateData = new TemplateData();
		
		// set up the main frame
		frame = new JFrame();
		frame.setTitle("Crossword Compiler");
		frame.setBackground(Color.decode("#F5F5F5"));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setSize(800, 560);
		
		// preload the start view
		startView = new StartView(templateData);
		
		while (true) {
		
			// set up the start view
			startView.reset();
			frame.getContentPane().removeAll();
			frame.getContentPane().add(startView.getView());
			frame.revalidate();
			frame.repaint();
			
			if (!frame.isVisible()) frame.setVisible(true);
			
			// wait until the 'Create Crossword' button is clicked
			while (!startView.isReady()) {
				try {Thread.sleep(100);}
				catch (InterruptedException e) {}
			}		
			
			// get the data from with the start view
			grid = startView.getPreview().getGrid();
			mode = startView.getMode();
					
			// may add add a loading sign / progress bar here
			
			// next - create a filler class and complete the grid using the fill() method
			// so the code should look like
			// Filler filler = new Filler(grid, . . .);
			// filler.fill(1, 0);			

			// crossword = filler.getCrossword();
			// or
			// crossword = new Crossword(filler.getGrid());
			
			// sample 5x5 crossword
			crossword = new Crossword();
			
			if (mode == 0) { // setter mode
				frame.getContentPane().removeAll();
				setterView = new SetterView(crossword);
				frame.getContentPane().add(setterView.getView());
				frame.revalidate();
				frame.repaint();
				while (!setterView.isReady()) {
					try {Thread.sleep(100);}
					catch (InterruptedException e) {}
				}
			}
			else { // player mode
				frame.getContentPane().removeAll();
				playerView = new PlayerView(crossword);
				frame.getContentPane().add(playerView.getView());
				frame.revalidate();
				frame.repaint();
				while (!playerView.isReady()) {
					try {Thread.sleep(100);}
					catch (InterruptedException e) {}
				}
			}
			
		}
		
	}

}