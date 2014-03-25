import java.awt.Font;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.alee.laf.WebLookAndFeel;

public class Main {

	private static Square[][] grid;
	private static JFrame frame;
	private static int mode;
	private static Filler filler;
	private static Crossword crossword;
	private static StartView startView;
	private static SetterView setterView;
	private static PlayerView playerView;
	private static MasterDictionary masterDictionary;
	private static boolean cancelled;

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
			
			cancelled = false;
		
			// loading phase
			displayLoadingBar(0);
			
			// display the main frame
			if (!frame.isVisible()) { // executes only in the 1st cycle
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
			
			// complete the dictionary
			if (masterDictionary == null) { // executes only in the 1st cycle
				masterDictionary = new MasterDictionary();
				File projectDir = new File(System.getProperty("user.dir"));
				File dataDir = new File(projectDir, "data");
			    File matricesDir = new File(dataDir, "matrices");
				File file = new File(matricesDir, "dictionary.txt");				
					try {
						Scanner input = new Scanner(file);
						while (input.hasNextLine()) {
							String wrd = input.nextLine().toLowerCase();
							String clu = input.nextLine();
							input.nextLine();
							if (wrd.length() < 16 && wrd.length()>2) {
								masterDictionary.addWord(wrd, clu);
							}
						}
						input.close();
						masterDictionary.process();
					} catch (FileNotFoundException e) {
						System.out.println("file not found");
					}
			}
			
			// set up the start view
			if (startView == null) startView = new StartView();
			else startView.reset();
			frame.getContentPane().setVisible(false);
			frame.getContentPane().removeAll();
			frame.getContentPane().add(startView.getView());
			frame.revalidate();
			frame.repaint();
			frame.getContentPane().setVisible(true);
			
			// wait until the 'Create Crossword' button is clicked
			while (!startView.isReady()) {
				try {Thread.sleep(100);}
				catch (InterruptedException e) {}
			}		
			
			// get the data from the start view
			grid = startView.getPreview().getGrid();
			mode = startView.getMode();
			
			// generate a new crossword or use a ready one
			if (startView.getLoadedCrossword() == null) {
				// loading phase
				displayLoadingBar(1);
				// attempt to fill the grid
				for (int i = 0; i < 1; i++) {
					if (cancelled == true) break;
					filler = new Filler(grid, masterDictionary);
					try {filler.fill(1, 0);}
					catch (Exception e) {i--; filler.resetGrid(); continue;}
					if (filler.emptyGrid()) {i--; continue;}
				}
				// use the grid to generate a crossword
				if (!cancelled) crossword = new Crossword(filler.getGrid(), masterDictionary);
			}
			else crossword = startView.getLoadedCrossword();
			
			if (!cancelled) {
				while (true) {
					if (mode == 0) { // player mode
						frame.getContentPane().removeAll();
						playerView = new PlayerView(crossword); // set up
						frame.getContentPane().add(playerView.getView());
						frame.revalidate();
						frame.repaint(); // display
						while (!playerView.isReady()) { // 'New Crossword' is clicked
							try {Thread.sleep(100);}
							catch (InterruptedException e) {}
						}	
						if (playerView.isReloading()) crossword = playerView.getCrossword();
						else break;
					}
					else { // setter mode
						frame.getContentPane().removeAll();
						setterView = new SetterView(crossword); // set up
						frame.getContentPane().add(setterView.getView());
						frame.revalidate();
						frame.repaint(); // display
						while (!setterView.isReady()) { // 'New Crossword' is clicked
							try {Thread.sleep(100);}
							catch (InterruptedException e) {}
						}
						if (setterView.isReloading()) crossword = setterView.getCrossword();
						else break;
					}
				}
			}
			
		}
		
	}
	
	// shows a loading bar
	private static void displayLoadingBar(int cancellable) {
		JPanel loadingPanel = new JPanel(new GridBagLayout());
		loadingPanel.setBackground(Color.decode("#F5F5F5"));
		final JProgressBar loadingBar = new JProgressBar();
		loadingBar.setIndeterminate(true);
		loadingBar.setStringPainted(true);
		if (cancellable != 1) loadingBar.setString("Loading Templates");
		else loadingBar.setString("Compiling (click to cancel)");
		loadingBar.setFont(new Font(loadingBar.getFont().getName(), Font.BOLD, 16));
		loadingPanel.add(loadingBar, new GridBagConstraints());
		if (cancellable == 1) {
			loadingBar.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {}
				public void mouseEntered(MouseEvent arg0) {}
				public void mouseExited(MouseEvent arg0) {}
				public void mousePressed(MouseEvent arg0) {
					loadingBar.setString("Cancelling");
					cancelled = true;
					filler.setAborted(true);
				}
				public void mouseReleased(MouseEvent arg0) {}
	        });
		}
		frame.getContentPane().removeAll();
		frame.getContentPane().add(loadingPanel);
		frame.revalidate();
		frame.repaint();
	}

}