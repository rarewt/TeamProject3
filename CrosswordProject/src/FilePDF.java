import java.io.File;
import java.io.FileOutputStream;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JList;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;


public class FilePDF {
	
	private ArrayList<String> clueList;
	private Crossword crossword;
	private static String path = System.getProperty("user.dir");
	private static String FILE = path + File.separator + "Crossword.pdf";
	private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
	      Font.BOLD);
	private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
		      Font.BOLD);
	
	
	public FilePDF(Crossword c, ArrayList<String> clues){
		crossword = c;
		clueList = clues;
		
	try {
	      Document document = new Document();
	      PdfWriter.getInstance(document, new FileOutputStream(FILE));
	      document.open();
	      addMetaData(document);
	      addContent(document, clueList, crossword);
	      document.close();
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	}
	
	
	private static void addMetaData(Document document) {
	    document.addTitle("Crossword");
	    
	  }
	
private static void addContent(Document document, ArrayList<String> list, Crossword crossword) throws DocumentException {
		Square[][] grid = crossword.getGrid();
		int size = crossword.getSize();
		
		HashMap<Integer, String[]> across, down;
		
		across = crossword.getAcross();
		down = crossword.getDown();
		
		int height, bfSize, sfSize;
		
		switch (size){
		case 5:
			height = 80;
			bfSize = 30;
			break;
		case 7:
			height = 60;
			bfSize = 20;
			break;
		case 9:
			height = 50;
			bfSize = 20;
			break;
		case 11:
			height = 40;
			bfSize = 15;
			break;
		case 13:
			height = 35;
			bfSize = 10;
			break;
		default:
				height = 350 / size;
				bfSize = 20;
				break;
		}
		
		sfSize = bfSize /2;
		
		Paragraph paragraph1 = new Paragraph("Crossword", subFont);
		document.add(paragraph1);
		document.add(new Chunk(new LineSeparator()));
		document.add( Chunk.NEWLINE );
		document.add( Chunk.NEWLINE );
		
		PdfPTable table1 = new PdfPTable(size);
		table1.setWidthPercentage(100);
		table1.setSpacingBefore(10f);
		table1.setSpacingAfter(10f); 
		
		//PdfPTable table2 = new PdfPTable(size);
		//table2 = table1;
		
		Font bigFont =  new Font(Font.FontFamily.TIMES_ROMAN, bfSize,Font.BOLD);
	    Font smallFont = new Font(Font.FontFamily.TIMES_ROMAN, sfSize, Font.NORMAL);
		
	    for(int i=0; i<size; i++){
			for(int j=0; j<size;j++){
				
				PdfPCell cell = new PdfPCell();
				
				if(grid[j][i].getLetter() != '-'){
					Paragraph parag1=new Paragraph(grid[j][i].getNote().getText(),smallFont);
					parag1.setAlignment(Element.ALIGN_TOP);
				    cell.addElement(parag1);
				 
					}
				else{
					cell.setBackgroundColor(BaseColor.BLACK);
				}
				cell.setFixedHeight(height);
			    table1.addCell(cell);
			}
		}
		
		document.add(table1);
		
		document.newPage();
		
		Paragraph paragraph2 = new Paragraph("Clues", subFont);
		document.add(paragraph2);
		document.add(new Chunk(new LineSeparator()));
		document.add( Chunk.NEWLINE );
		document.add( Chunk.NEWLINE );
		
		Paragraph parClues = new Paragraph();
		
		for(int i=0; i< list.size(); i++){
			if(list.get(i).startsWith("<html")){
				parClues.add( Chunk.NEWLINE );
				parClues.add(list.get(i).substring(9, list.get(i).length() - 11) + " ");
				parClues.add( Chunk.NEWLINE );
			}
			else parClues.add(list.get(i) + " ");
			parClues.add(Chunk.NEWLINE);
			
		}
		document.add(parClues);
		
		document.newPage();
		
		
		Paragraph paragraph3 = new Paragraph("Solution", subFont);
		document.add(paragraph3);
		document.add(new Chunk(new LineSeparator()));
		document.add( Chunk.NEWLINE );
		document.add( Chunk.NEWLINE );
		
		document.add(new Paragraph("Across", subFont));
		for(Integer i: across.keySet()){
			document.add(new Paragraph(i + ". " + across.get(i)[0]));
		}
		document.add( Chunk.NEWLINE );
		
		document.add(new Paragraph("Down", subFont));
		for(Integer i: down.keySet()){
			document.add(new Paragraph(i + ". " + down.get(i)[0]));
		}
			
		
		
}

}
