import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class OptionsInterface {
	
	private JMenuBar menuBar;
	private JMenu fileMenu, helpMenu;
	private JMenuItem newCrossword, exitProgram, loadCrossword, saveCrossword,
					  importTemplate, exportTemplate, exportPdfTemplate, openManual, openAbout;
	
	public OptionsInterface(int mode) { // 0 for startup; anything else for player/setter
		// main container
		menuBar = new JMenuBar();
		menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#AAAAAA")));
		menuBar.add(new JLabel("    "));
		
		// 'File' menu		
		fileMenu = new JMenu("File");
		fileMenu.setFont(new Font(fileMenu.getFont().getName(), Font.PLAIN, 15));
		menuBar.add(fileMenu);
		menuBar.add(new JLabel("  "));
		
		// 'File > New Crossword' menu item
		newCrossword = new JMenuItem("New Crossword");
		newCrossword.setFont(new Font(newCrossword.getFont().getName(), Font.PLAIN, 15));
		fileMenu.add(newCrossword);
		
		// 'File > Save Crossword' menu item
		saveCrossword = new JMenuItem("Save Crossword");
		saveCrossword.setFont(new Font(saveCrossword.getFont().getName(), Font.PLAIN, 15));
		// fileMenu.add(saveCrossword);
		
		// 'File > Load Crossword' menu item
		loadCrossword = new JMenuItem("Load Crossword");
		loadCrossword.setFont(new Font(newCrossword.getFont().getName(), Font.PLAIN, 15));
		// fileMenu.add(loadCrossword);
		
		// 'File > Export Grid Template' menu item
		exportTemplate = new JMenuItem("Export Template");
		exportTemplate.setFont(new Font(exportTemplate.getFont().getName(), Font.PLAIN, 15));
		fileMenu.add(exportTemplate);
		
		// 'File > Import Grid Template' menu item
		importTemplate = new JMenuItem("Import Template");
		importTemplate.setFont(new Font(importTemplate.getFont().getName(), Font.PLAIN, 15));
		fileMenu.add(importTemplate);
		
		// 'File > Export as PDF' menu item
		exportPdfTemplate = new JMenuItem("Export as PDF");
		exportPdfTemplate.setFont(new Font(exportPdfTemplate.getFont().getName(), Font.PLAIN, 15));
		fileMenu.add(exportPdfTemplate);
		
		// 'File > Exit' menu item
		exitProgram = new JMenuItem("Exit");
		exitProgram.addActionListener(new ExitListener());
		exitProgram.setFont(new Font(exitProgram.getFont().getName(), Font.PLAIN, 15));
		fileMenu.add(exitProgram);
		
		// 'Help' menu		
		helpMenu = new JMenu("Help");
		helpMenu.setFont(new Font(helpMenu.getFont().getName(), Font.PLAIN, 15));
		menuBar.add(helpMenu);
		
		// 'Help > Manual' menu item
		openManual = new JMenuItem("Manual");
		openManual.addActionListener(new ManualListener());
		openManual.setFont(new Font(openManual.getFont().getName(), Font.PLAIN, 15));
		helpMenu.add(openManual);
		
		// 'Help > About' menu item
		openAbout = new JMenuItem("About");
		openAbout.addActionListener(new AboutListener());
		openAbout.setFont(new Font(openAbout.getFont().getName(), Font.PLAIN, 15));
		helpMenu.add(openAbout);
		
		if (mode == 0) {
			// those are disabled in the startup view
			newCrossword.setEnabled(false);
			saveCrossword.setEnabled(false);
			exportPdfTemplate.setEnabled(false);
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
	
	public JMenuItem getExportPdfTemplate(){
		return exportPdfTemplate;
	}

	public JMenuItem getOpenManual() {
		return openManual;
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
	
	// the google docs should not be editable in the final release
	private class ManualListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
				 Desktop.getDesktop().browse(
						 new URL("https://docs.google.com/document/d/1hNYoJ2jyC_ltZiyLx6tmeh_PPe2JvUFmYdrw_AfOcLo/edit?usp=sharing").toURI());
			}
			catch (Exception e) {}
		}	
	}
	
	private class AboutListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
				 Desktop.getDesktop().browse(
						 new URL("https://docs.google.com/document/d/1CvpjAF1VvyazZF7xjmhEVP3pjd9z1gdawAYRTf9NTzs/edit?usp=sharing").toURI());
			}
			catch (Exception e) {}
		}	
	}
	
}
