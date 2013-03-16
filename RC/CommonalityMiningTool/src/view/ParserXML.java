/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

	/** Effettua il parsing del file xml */
 	public class ParserXML extends DefaultHandler
    {  	
    	private  boolean r = false, n = false, l = false, p = false;
    	
    	private int i = 0;

    	private ArrayList <String> nameInput = new ArrayList <String> (), pathInput = new ArrayList <String> ();
    	
    	@Override
        public void startElement(String uri, String localName, String gName, Attributes attributes)
        {
    		if(gName.equals("root"))
	        	r = true;
        	
        	else if(gName.equals("node"))
        		n = true;
        	
        	else if(gName.equals("leaf"))
        		l = true;
        	
        	else
        		p = true;
        }
        
    	@Override
        public void characters(char [] ch, int start, int length)
        {
    		if(p)
    			pathInput.add(new String(ch,start,length));
        	
        	else if(i == 0 && l && !p)
        		nameInput.add(new String(ch, start, length));
        		
        	else 
        	{
        		n = r;
        		r = n;
        	}
        }
    	
    	@Override
        public void endElement(String uri, String localName, String gName)
        {
    		if(gName.equals("root"))
	        	r = false;
        	
        	else if(gName.equals("node"))
        	{
        		i = i + 1;
        		n = false;
        	}
        	else if(gName.equals("leaf"))
        		l = false;
        	
        	else
        		p = false;
        }
    	
    	/** Lettura delle path dei file 
    	 * 
    	 * @return pathInput
    	 */
    	public ArrayList <String> readPathInput()
    	{
    		return pathInput;
    	}
    	
    	/** Lettura dei nomi dei file
    	 * 
    	 * @return
    	 */
    	public ArrayList <String> readNameFile()
    	{
    		return nameInput;
    	}
    }
