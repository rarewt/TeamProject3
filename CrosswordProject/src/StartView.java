import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SpringLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JPanel;

public class StartView {
	
	private TemplateData data;
	private OptionsInterface options;
	private JPanel view, previewPanel, optionsPanel, sizePanel, templatePanel, modePanel;
	private JRadioButton setterButton, playerButton;
	private JButton createButton;
	private JLabel sizeLabel;
	private JSlider sizeSlider;
	private JList templateList;
	private int selectedMode; // 0 for player / 1 for setter
	private ArrayList<GridPreview> previewCache;
	private GridPreview selectedPreview;
	private boolean ready;
	private Crossword loadedCrossword; // used for direct loading
	
	public StartView() {		
		data = new TemplateData();
		previewCache = new ArrayList<GridPreview>();
		for (HashMap<String, String> group : data.getTemplates().values())
			for (String template : group.values()) previewCache.add(new GridPreview(template));
		selectedPreview = new GridPreview("");
		selectedMode = 1; // setter is default
		ready = false;

		view = new JPanel();
		view.setBackground(Color.decode("#F5F5F5"));
		SpringLayout springLayout = new SpringLayout();
		view.setLayout(springLayout);
		
		// top menu bar
		optionsPanel = new JPanel();
		optionsPanel.setLayout(new BorderLayout());
		optionsPanel.setBackground(Color.decode("#F5F5F5"));
		springLayout.putConstraint(SpringLayout.NORTH, optionsPanel, 0, SpringLayout.NORTH, view);
		springLayout.putConstraint(SpringLayout.WEST, optionsPanel, 0, SpringLayout.WEST, view);
		springLayout.putConstraint(SpringLayout.SOUTH, optionsPanel, 38, SpringLayout.NORTH, view);
		springLayout.putConstraint(SpringLayout.EAST, optionsPanel, 0, SpringLayout.EAST, view);
		options = new OptionsInterface(0); // represents the top menu bar
		optionsPanel.add(options.getMenuBar(), BorderLayout.CENTER);
		// view-specific functionality
		options.getImportTemplate().addActionListener(new ImportTemplateListener());
		options.getExportTemplate().addActionListener(new ExportTemplateListener());
		options.getLoadCrossword().addActionListener(new LoadCrosswordListener());
		view.add(optionsPanel);
		
		// editable grid preview 	
		previewPanel = new JPanel();
		previewPanel.setLayout(new BorderLayout());
		previewPanel.setBackground(Color.WHITE);
		springLayout.putConstraint(SpringLayout.NORTH, previewPanel, 63, SpringLayout.NORTH, view);
		springLayout.putConstraint(SpringLayout.WEST, previewPanel, 25, SpringLayout.WEST, view);
		springLayout.putConstraint(SpringLayout.SOUTH, previewPanel, -26, SpringLayout.SOUTH, view);
		springLayout.putConstraint(SpringLayout.EAST, previewPanel, -327, SpringLayout.EAST, view);
		view.add(previewPanel);
		
		// this panel contains options for
		//    - changing the size of the crossword grid
		//    - selecting one of the predefined grid templates 
		//    - importing a grid template
		//    - selecting a mode
		optionsPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, optionsPanel, 0, SpringLayout.NORTH, previewPanel);
		springLayout.putConstraint(SpringLayout.WEST, optionsPanel, 18, SpringLayout.EAST, previewPanel);
		springLayout.putConstraint(SpringLayout.EAST, optionsPanel, -13, SpringLayout.EAST, view);
		springLayout.putConstraint(SpringLayout.SOUTH, optionsPanel, 0, SpringLayout.SOUTH, previewPanel);
		optionsPanel.setBackground(Color.decode("#F5F5F5"));
		SpringLayout optionsLayout = new SpringLayout();
		optionsPanel.setLayout(optionsLayout);
		view.add(optionsPanel);
		
		// placing the components into smaller panels allows having a better layout
		
		sizePanel = new JPanel();
		optionsLayout.putConstraint(SpringLayout.NORTH, sizePanel, 0, SpringLayout.NORTH, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.WEST, sizePanel, 10, SpringLayout.WEST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.SOUTH, sizePanel, 48, SpringLayout.NORTH, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.EAST, sizePanel, -10, SpringLayout.EAST, optionsPanel);
		sizePanel.setBackground(Color.decode("#F5F5F5"));
		sizePanel.setLayout(new GridLayout(2, 1, 0, 0));
		optionsPanel.add(sizePanel);
		
		modePanel = new JPanel();
		optionsLayout.putConstraint(SpringLayout.WEST, modePanel, 11, SpringLayout.WEST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.EAST, modePanel, -10, SpringLayout.EAST, optionsPanel);		
		optionsLayout.putConstraint(SpringLayout.NORTH, modePanel, -24, SpringLayout.SOUTH, optionsPanel);
		modePanel.setBackground(Color.decode("#F5F5F5"));
		modePanel.setLayout(new GridLayout(1, 2, 0, 0));
		optionsPanel.add(modePanel);
		
		templatePanel = new JPanel();
		optionsLayout.putConstraint(SpringLayout.NORTH, templatePanel, 6, SpringLayout.SOUTH, sizePanel);
		optionsLayout.putConstraint(SpringLayout.WEST, templatePanel, 10, SpringLayout.WEST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.SOUTH, templatePanel, -6, SpringLayout.NORTH, modePanel);
		optionsLayout.putConstraint(SpringLayout.EAST, templatePanel, -10, SpringLayout.EAST, optionsPanel);
		optionsLayout.putConstraint(SpringLayout.SOUTH, modePanel, 0, SpringLayout.SOUTH, optionsPanel);
		templatePanel.setBackground(Color.decode("#F5F5F5"));
		templatePanel.setLayout(new BorderLayout(0, 0));
		optionsPanel.add(templatePanel);
		
		// individual components
		
		// label showing the currently selected size
		sizeLabel = new JLabel();
		sizeLabel.setFont(new Font(sizeLabel.getFont().getName(), Font.PLAIN, 15));
		sizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sizeLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		sizePanel.add(sizeLabel);
		
		// slider changing the size of the crossword grid - having values from 1 to 5
		// and using sizeSlider.getValue()*2+3 gets us the odd numbers from 5 to 13
		sizeSlider = new JSlider(1,5);
		sizeSlider.setFocusable(false);
		sizeSlider.setBackground(Color.decode("#F5F5F5"));
		sizeSlider.addChangeListener(new sizeSliderListener());
		sizeLabel.setText("Size: " + (sizeSlider.getValue()*2+3) + " x " + (sizeSlider.getValue()*2+3));
		sizePanel.add(sizeSlider);
		sizeSlider.setValue(3);
		
		JScrollPane templateScrollPane = new JScrollPane();
		templatePanel.add(templateScrollPane, BorderLayout.CENTER);
		
		// list for selecting a predefined template (that can be further edited in the grid preview)
		if (data.getTemplates().get(sizeSlider.getValue()*2+3) != null) {
			Object[] list = data.getTemplates().get(sizeSlider.getValue()*2+3).keySet().toArray();
			Arrays.sort(list); // order the templates by name
			templateList = new JList(list);
		}
		else templateList = new JList(new Object[0]);
		templateList.setFont(new Font(templateList.getFont().getName(), Font.PLAIN, 15));
		templateList.addListSelectionListener(new TemplateListListener());	
		templateList.setSelectedIndex(0);
		templateList.setFocusable(false);
		templateScrollPane.setViewportView(templateList);
		
		// button that initiates the next phase of the application
		createButton = new JButton("Generate Crossword");
		createButton.addActionListener(new CreateButtonListener());	
		createButton.setFont(new Font(createButton.getFont().getName(), Font.PLAIN, 15));
		createButton.setFocusable(false);
		templatePanel.add(createButton, BorderLayout.SOUTH);
		
		// radio buttons for switching between setter and player mode
		ButtonGroup modeButtons = new ButtonGroup();
		setterButton = new JRadioButton(" Editing Mode");
		setterButton.addActionListener(new SetterButtonListener());	
		setterButton.setFont(new Font(setterButton.getFont().getName(), Font.PLAIN, 15));
		setterButton.setBackground(Color.decode("#F5F5F5"));
		setterButton.setFocusable(false);
		modeButtons.add(setterButton);	
		modePanel.add(setterButton);	
		playerButton = new JRadioButton(" Solving Mode");
		playerButton.addActionListener(new PlayerButtonListener());	
		playerButton.setFont(new Font(playerButton.getFont().getName(), Font.PLAIN, 15));
		playerButton.setBackground(Color.decode("#F5F5F5"));
		playerButton.setFocusable(false);
		modeButtons.add(playerButton);
		modePanel.add(playerButton);
		setterButton.setSelected(true);
	}
	
    private class sizeSliderListener implements ChangeListener {
        public void stateChanged(ChangeEvent event) {
        	if (selectedPreview.getSize() == sizeSlider.getValue()*2+3) return;
        	sizeLabel.setText("Size: " + (sizeSlider.getValue()*2+3) + " x " + (sizeSlider.getValue()*2+3));
        	if (data.getTemplates().get(sizeSlider.getValue()*2+3) != null) {
        		// the contents of the list change depending on the currently selected size
        		Object[] list = data.getTemplates().get(sizeSlider.getValue()*2+3).keySet().toArray();
        		Arrays.sort(list); // order the templates by name
        		templateList.setListData(list);
        		templateList.setSelectedIndex(0);
        	}
        	else { // no templates for that size => empty list
        		templateList.setListData(new Object[0]);
        		previewPanel.removeAll(); // no preview
        		previewPanel.revalidate();
        		previewPanel.repaint();
        		selectedPreview = new GridPreview("");
        	}
        }
    }
    
    private class TemplateListListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (templateList.getSelectedValue() != null) {
				String templateName = (String) templateList.getSelectedValue();
				selectTemplate(data.getTemplates().get(sizeSlider.getValue()*2+3).get(templateName), false);
			}
		}
    }
    
    private class CreateButtonListener implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		if (selectedPreview.isValid()) ready = true;
    	}	
    }
    
    private class PlayerButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			selectedMode = 0;
		}
    }
    
    private class SetterButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			selectedMode = 1;
		}
    }
    
    // this method is invoked every time a template is selected from the list;
    // it creates a new grid preview (using the template) to replace
    // the current one and visualizes it in the previewPanel
    public void selectTemplate(String template, boolean imported) {
    	if (selectedPreview.toString().equals(template)) return;
		previewPanel.removeAll();
		selectedPreview = setupPreview(template, imported);
		previewPanel.add(selectedPreview.getVisuals(), BorderLayout.CENTER);
		previewPanel.revalidate();
		previewPanel.repaint();
		if (selectedPreview.isValid()) // remove any highlights
			for (int y = 0; y < selectedPreview.getSize(); y++)
				for (int x = 0; x < selectedPreview.getSize(); x++)
					selectedPreview.getGrid()[x][y].getPanel().setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    
    // searches the cache for a certain preview and returns it
    // if that preview is not in the cache - returns a new one
    public GridPreview setupPreview(String template, boolean imported) {
    	if (!imported) {
			for (int i = 0; i < previewCache.size(); i++)
				if (previewCache.get(i).toString().equals(template)) {
					previewCache.get(i).reset(); // removing this line enables saving preview edits
					return previewCache.get(i);
				}
    	}
		GridPreview newPreview = new GridPreview(template);
		previewCache.add(newPreview);
		return newPreview;
    }
    
	public JPanel getView() {
		return view;
	}

	public GridPreview getPreview() {
		return selectedPreview;
	}
	
	public int getMode() {
		return selectedMode;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	private class ImportTemplateListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
    		chooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
    		chooser.setDialogTitle("Select File");
			int returnValue = chooser.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				try { // process the selected file
					if (!chooser.getSelectedFile().getName().endsWith("txt")) return;
					FileReader reader = new FileReader(chooser.getSelectedFile());
					Scanner lineScanner = new Scanner(reader);
					String importedTemplate = "";
					while (true) {
						importedTemplate += lineScanner.nextLine();
						if (lineScanner.hasNext()) importedTemplate += ",";
						else break;
					}
					selectTemplate(importedTemplate, true);
					lineScanner.close();
					reader.close();
				} catch (IOException e) {}
				templateList.clearSelection();
			}
		}	
	}
	
	private class ExportTemplateListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
    		chooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
    		chooser.setDialogTitle("Select File");
			int returnValue = chooser.showSaveDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				try {
					File file = new File(chooser.getSelectedFile() + ".txt");
					PrintWriter writer = new PrintWriter(file, "UTF-8");
					String output = "";
					for (int y=0; y < selectedPreview.getSize(); y++) {
						for (int x=0; x < selectedPreview.getSize(); x++) {
							if (selectedPreview.getGrid()[x][y].getPanel().getBackground() == Color.WHITE)
								output += "-";
							else
								output += "#";
						}
						output += "\n";
					}
					writer.println(output);
					writer.close();
				} catch (IOException e) {}
			}
		}	
	}
	
	private class LoadCrosswordListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// just replace this line with code that builds a complete crossword from a text file:
			loadedCrossword = new Crossword();
			// with this line, the Main moves forward and the next view works with loadedCrossword
			ready = true;
		}	
	}
	
	public Crossword getLoadedCrossword() {
		return loadedCrossword;
	}
	
}