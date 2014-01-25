import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class OptionsInterface {
	
	private JMenuBar menuBar;
	private JMenu optionsMenu, helpMenu;
	private JMenuItem newCrossword, exitProgram, loadCrossword, saveCrossword,
					  importTemplate, exportTemplate, openGuide, openAbout;
	
	public OptionsInterface(int mode) { // 0 for startup; anything else for player/setter
		// main container
		menuBar = new JMenuBar();
		menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#AAAAAA")));
		menuBar.add(new JLabel("   "));
		
		// 'Options' menu		
		optionsMenu = new JMenu("Options");
		optionsMenu.setFont(new Font(optionsMenu.getFont().getName(), Font.PLAIN, 15));
		menuBar.add(optionsMenu);
		menuBar.add(new JLabel(" "));
		
		// 'Options > New Crossword' menu item
		newCrossword = new JMenuItem("New Crossword");
		newCrossword.setFont(new Font(newCrossword.getFont().getName(), Font.PLAIN, 15));
		optionsMenu.add(newCrossword);
		
		// 'Options > Save Crossword' menu item
		saveCrossword = new JMenuItem("Save Crossword");
		saveCrossword.setFont(new Font(saveCrossword.getFont().getName(), Font.PLAIN, 15));
		optionsMenu.add(saveCrossword);
		
		// 'Options > Load Crossword' menu item
		loadCrossword = new JMenuItem("Load Crossword");
		loadCrossword.setFont(new Font(newCrossword.getFont().getName(), Font.PLAIN, 15));
		optionsMenu.add(loadCrossword);
		
		// 'Options > Export Grid Template' menu item
		exportTemplate = new JMenuItem("Export Grid Template");
		exportTemplate.setFont(new Font(exportTemplate.getFont().getName(), Font.PLAIN, 15));
		optionsMenu.add(exportTemplate);
		
		// 'Options > Import Grid Template' menu item
		importTemplate = new JMenuItem("Import Grid Template");
		importTemplate.setFont(new Font(importTemplate.getFont().getName(), Font.PLAIN, 15));
		optionsMenu.add(importTemplate);
		
		// 'Options > Exit' menu item
		exitProgram = new JMenuItem("Exit");
		exitProgram.addActionListener(new ExitListener());
		exitProgram.setFont(new Font(exitProgram.getFont().getName(), Font.PLAIN, 15));
		optionsMenu.add(exitProgram);
		
		// 'Help' menu		
		helpMenu = new JMenu("Help");
		helpMenu.setFont(new Font(helpMenu.getFont().getName(), Font.PLAIN, 15));
		menuBar.add(helpMenu);
		
		// 'Help > Guide' menu item
		openGuide = new JMenuItem("Guide");
		openGuide.addActionListener(new GuideListener());
		openGuide.setFont(new Font(openGuide.getFont().getName(), Font.PLAIN, 15));
		helpMenu.add(openGuide);
		
		// 'Help > About' menu item
		openAbout = new JMenuItem("About");
		openAbout.addActionListener(new AboutListener());
		openAbout.setFont(new Font(openAbout.getFont().getName(), Font.PLAIN, 15));
		helpMenu.add(openAbout);
		
		if (mode == 0) {
			// those are disabled in the startup view
			newCrossword.setEnabled(false);
			saveCrossword.setEnabled(false);
		}
		else {
			// those are disabled in the player/setter view
			importTemplate.setEnabled(false);
		}
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}
	
	public JMenuItem getNewCrossword() {
		return newCrossword;
	}

	public JMenuItem getExitProgram() {
		return exitProgram;
	}

	public JMenuItem getLoadCrossword() {
		return loadCrossword;
	}

	public JMenuItem getSaveCrossword() {
		return saveCrossword;
	}

	public JMenuItem getImportTemplate() {
		return importTemplate;
	}

	public JMenuItem getExportTemplate() {
		return exportTemplate;
	}

	public JMenuItem getOpenGuide() {
		return openGuide;
	}

	public JMenuItem getOpenAbout() {
		return openAbout;
	}
	
	// those are common across all views
	
	private class ExitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}	
	}
	
	private class GuideListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
		}	
	}
	
	private class AboutListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
		}	
	}
	
}
