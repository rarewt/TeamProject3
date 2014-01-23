import java.awt.Font;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.alee.laf.WebLookAndFeel;

public class Main {

	private static Square[][] grid;
	private static JFrame frame;
	private static int mode;
	private static Crossword crossword;
	private static StartView startView;
	private static SetterView setterView;
	private static PlayerView playerView;
	private static MasterDictionary masterDictionary;

	public static void main(String[] args) {
		
		// custom look and feel
		WebLookAndFeel.install();
		
		// set up the main frame
		frame = new JFrame();
		frame.setTitle("Crossword Compiler");
		frame.setBackground(Color.decode("#F5F5F5"));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setSize(800, 560);

		while (true) {
		
			// loading phase
			displayLoadingBar();
			
			// display the main frame
			if (!frame.isVisible()) {
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
			
			// complete the dictionary
			if (masterDictionary == null) {
				masterDictionary = new MasterDictionary();
				File file = new File("word.txt");				
					try {
						Scanner input = new Scanner(file);
						while (input.hasNextLine()) {
							String s = input.nextLine();
							if (s.length() < 16 && s.length()>2) {
								masterDictionary.addWord(s);
							}
						}
						input.close();
						masterDictionary.process();
					} catch (FileNotFoundException e) {
						System.out.println("file not found");
					}
			}
			
			// set up the start view
			startView = new StartView();
			frame.getContentPane().removeAll();
			frame.getContentPane().add(startView.getView());
			frame.revalidate();
			frame.repaint();
			
			// wait until the 'Create Crossword' button is clicked
			while (!startView.isReady()) {
				try {Thread.sleep(100);}
				catch (InterruptedException e) {}
			}		
			
			// get the data from the start view
			grid = startView.getPreview().getGrid();
			mode = startView.getMode();
					
			// loading phase
			displayLoadingBar();
			
			boolean successfulCompilation = true;
			
			try {
				// create a filler class and complete the grid using the fill() method
				Filler filler = new Filler(grid, masterDictionary);
				filler.fill(1, 0); // initiate the grid-filling algorithm			
				crossword = new Crossword(filler.getGrid());
			}
			catch (Exception e) {
				successfulCompilation = false;
			}
			
			if (successfulCompilation) {
				// when the crossword is ready:
				if (mode == 0) { // setter mode
					frame.getContentPane().removeAll();
					setterView = new SetterView(crossword); // set up
					frame.getContentPane().add(setterView.getView());
					frame.revalidate();
					frame.repaint(); // display
					while (!setterView.isReady()) { // 'New Crossword' is clicked
						try {Thread.sleep(100);}
						catch (InterruptedException e) {}
					}
				}
				else { // player mode
					frame.getContentPane().removeAll();
					playerView = new PlayerView(crossword); // set up
					frame.getContentPane().add(playerView.getView());
					frame.revalidate();
					frame.repaint(); // display
					while (!playerView.isReady()) { // 'New Crossword' is clicked
						try {Thread.sleep(100);}
						catch (InterruptedException e) {}
					}
				}
			}
			// TODO else - panel with an error message + 'go back to start' button
			
		}
		
	}
	
	// shows a loading bar
	private static void displayLoadingBar() {
		JPanel loadingPanel = new JPanel(new GridBagLayout());
		loadingPanel.setBackground(Color.decode("#F5F5F5"));
		JProgressBar loadingBar = new JProgressBar();
		loadingBar.setIndeterminate(true);
		loadingBar.setStringPainted(true);
		loadingBar.setString("Loading");
		loadingBar.setFont(new Font(loadingBar.getFont().getName(), Font.BOLD, 16));
		loadingPanel.add(loadingBar, new GridBagConstraints());
		frame.getContentPane().removeAll();
		frame.getContentPane().add(loadingPanel);
		frame.revalidate();
		frame.repaint();
	}

}