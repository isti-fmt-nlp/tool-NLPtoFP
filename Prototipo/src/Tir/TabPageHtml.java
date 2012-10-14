import java.io.*;

import javax.swing.*; 
import javax.swing.border.EtchedBorder;
import javax.swing.text.html.HTMLEditorKit;

public class TabPageHtml extends JPanel 
{ 
  /** Costruttore

      @param record

  */
  public TabPageHtml(RecordPdf record)
  {  
	setLayout(null);
	setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
	setBounds(50, 50, 500, 600);
	
	JTabbedPane jtp = new JTabbedPane();
    
    jtp.setBounds(0, 0, 630, 450);
    
    for(int i = 0; i < 4; i++)
    {
    	switch(i)
    	{
    		case 0:
    		{
    			jtp.addTab("Text to analyze", tabTxt(record)); 
    			break;
    		}
    		case 1:
    		{
    			jtp.addTab("Sentence splitting", tabHtml(record.getPageHtml(i))); 
    			break;
    		}
    		case 2:
    		{
    			jtp.addTab("Term extraction", tabHtml(record.getPageHtml(i))); 
    			break;
    		}
    		case 3:
    		{
    			jtp.addTab("Annotation", tabHtml(record.getPageHtml(i))); 
    			break;
    		}
    	}
    }
    this.add(jtp); 
  } 
  
  private JScrollPane tabTxt(RecordPdf record)
  {
	  try
	  {
		  BufferedReader reader =
	              new BufferedReader(
	                  new FileReader(
	                		  record.getPathTxt()));
	                  
	      String tmp, s = "";
	      
	      while((tmp = reader.readLine()) != null)
	      {
	    	  s = s + tmp + "\n";
	    	  
	      }
	      reader.close();
	      
	      JTextArea jta = new JTextArea(s);
	      
	      jta.setLineWrap(true);
	      
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
	  JEditorPane jep = new JEditorPane();
	    
	  jep.setEditorKit(new HTMLEditorKit());

	  FileReader fr;
	  
	  try 
	  {
		fr = new FileReader(s);
		
		jep.read(fr, s);
		
		fr.close();
	  } 
	  catch (FileNotFoundException e)
	  {

	  } 
	  catch (IOException e) 
	  {

	  }
	  
	  JScrollPane jsp = new JScrollPane(jep);
	    
	  return jsp;
  }
}