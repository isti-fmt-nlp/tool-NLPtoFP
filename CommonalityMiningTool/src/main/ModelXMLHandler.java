package main;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ModelXMLHandler extends DefaultHandler {

	boolean inTree=false;
	public String xml="";
	
	public void startElement(String uri, String localName, String qName, Attributes atts) {
	  if (qName.equals("feature_tree")) inTree=true;
	  System.out.println("*START*\nuri: "+uri+"\nlocalName: "+localName+"\nqName: "+qName+"\nAttribute:");
	  for(int i=0; i< atts.getLength(); ++i)
		System.out.println(atts.getLocalName(i)+"="+atts.getValue(i));
	  
	}
	
	public void endElement(String uri, String localName, String qName) {
	  if (qName.equals("feature_tree")) inTree=false;
	  System.out.println("*END*\nuri: "+uri+"\nlocalName: "+localName+"\nqName: "+qName);
	}
	
	public void characters(char[ ] chars, int start, int length) {
	  String tmp=new String(chars, start, length);
	  System.out.println("Found String: "+tmp);
	  if(inTree) xml+=tmp;
	}
}

