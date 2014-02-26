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
 	public class ParserXML extends DefaultHandler{  	
    	private  boolean root = false, node = false, leaf = false, p = false;
    	
    	private int i = 0;

    	private ArrayList<String> nameInput = new ArrayList <String> ();
    	private ArrayList<String> pathInput = new ArrayList <String> ();
    	
    	@Override
        public void startElement(String uri, String localName, String gName, Attributes attributes){
    		if(gName.equals("root")) root = true;        	
        	else if(gName.equals("node")) node = true;        	
        	else if(gName.equals("leaf")) leaf = true;        	
        	else p = true;
        }
        
    	@Override
        public void characters(char [] ch, int start, int length){
    		if(p) pathInput.add(new String(ch,start,length));        	
        	else if(i == 0 && leaf) nameInput.add(new String(ch, start, length));        		
        	else{
        		node = root;
        		root = node;
        	}
        }
    	
    	@Override
        public void endElement(String uri, String localName, String gName){
    		if(gName.equals("root")) root = false;        	
        	else if(gName.equals("node")){ ++i; node = false;}
        	else if(gName.equals("leaf"))leaf = false;        	
        	else p = false;
        }
    	
    	/**
    	 * Reads files paths.
    	 * 
    	 * @return - a ArrayList<String> containing the files paths.
    	 */
    	public ArrayList<String> readPathInput(){
    		return pathInput;
    	}
    	
    	/** 
    	 * Reads files names.
    	 * 
    	 * @return - a ArrayList<String> containing the files names.
    	 */
    	public ArrayList<String> readNameFile(){
    		for(String name : nameInput) System.out.println("Input file name: "+name);
    		return nameInput;
    	}
    }
