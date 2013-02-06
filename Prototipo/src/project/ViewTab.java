package project;

import java.awt.Color;
import java.io.*;

import javax.swing.*; 
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;

public class ViewTab extends JPanel implements ViewTabI
{ 
  /* */
  private File fileAnalysis[] = null;
  
  private final Highlighter.HighlightPainter[] highlightPainter = {
		  new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW),
		  new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN)
		};
  
  @Override
  public void createTab() 
  {
	  JTabbedPane jtp = new JTabbedPane();
	  jtp.setBounds(0, 0, 610, 466);  
	  
	  setLayout(null);
	  setBounds(320, 65, 610, 456);
		
	  for(int i = 0; i < 4; i++)
	    {
	    	switch(i)
	    	{
	    		case 0:
	    		{
	    			jtp.addTab("Text to analyze", tabTxt(fileAnalysis[i].getAbsolutePath())); 
	    			break;
	    		}
	    		case 1:
	    		{
	    			jtp.addTab("Sentence splitting", tabHtml(fileAnalysis[i].getAbsolutePath())); 
	    			break;
	    		}
	    		case 2:
	    		{
	    			jtp.addTab("Term extraction", tabHtml(fileAnalysis[i].getAbsolutePath())); 
	    			break;
	    		}
	    		case 3:
	    		{
	    			jtp.addTab("Annotation", tabHtml(fileAnalysis[i].getAbsolutePath())); 
	    			break;
	    		}
	    	}
	    }
	    add(jtp); 
  }
  
  
  private JScrollPane tabTxt(String s1)
  {
	  try
	  {
		  String tmp, s = "";
		  
		  BufferedReader br =
	              new BufferedReader(
	            		  new FileReader(s1));    
	      
	      while((tmp = br.readLine()) != null)
	    	  s = s + tmp + "\n";

	      br.close();
	      
	      JTextArea jta = new JTextArea(s);  
	      jta.setLineWrap(true);
	      setHighlight(jta, "train");
	      
	      JScrollPane jsp = new JScrollPane(jta);
		  
		  return jsp;
	  }
	  catch(FileNotFoundException e)
	  {
		  return null;
	  } 
	  catch (IOException e) 
	  {
		  return null;
	  }
  }
  
  private JScrollPane tabHtml(String s) 
  {		
	  FileReader fr = null;
	  
	  JEditorPane ep = new JEditorPane();
	  ep.setEditorKit(new HTMLEditorKit());

	  try 
	  {
		fr = new FileReader(s);
		
		ep.read(fr, s);
		fr.close();
	  } 
	  catch (FileNotFoundException e)
	  {

	  } 
	  catch (IOException e) 
	  {

	  }
	  
	  JScrollPane jsp = new JScrollPane(ep);
	    
	  return jsp;
  }   
  
  public void setHighlight(JTextComponent jtc, String pattern) 
  {
	  try
	  {   
		  Highlighter hilite = jtc.getHighlighter();
	    
		  Document doc = jtc.getDocument();
	    
		  String text = doc.getText(0, doc.getLength());
	    
		  int pos = 0;
	    
		  while((pos = text.indexOf(pattern, pos)) >= 0) 
		  {	
			  hilite.addHighlight(pos, pos+pattern.length(), highlightPainter[0]);
			  pos += pattern.length();
		  }    
	   }
	   catch(BadLocationException e) 
	   {
		   
	   }
	}
  
  public void setFileAnalysis(File f[])
  {
	  fileAnalysis = f;
  }
}