package main;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ModelXMLHandler extends DefaultHandler {

	boolean inTree=false;
	boolean inConstraintsList=false;
	
	public String featureTree=null;
	public String constraints=null;
	
	public void startElement(String uri, String localName, String qName, Attributes atts) {
	  if (qName.equals("feature_tree")) inTree=true;
	  else if(qName.equals("constraints")) inConstraintsList=true;
	  System.out.println("*START*\nuri: "+uri+"\nlocalName: "+localName+"\nqName: "+qName+"\nAttribute:");
	  for(int i=0; i< atts.getLength(); ++i)
		System.out.println(atts.getLocalName(i)+"="+atts.getValue(i));
	  
	}
	
	public void endElement(String uri, String localName, String qName) {
	  if (qName.equals("feature_tree")) inTree=false;
	  else if(qName.equals("constraints")) inConstraintsList=false;
	  System.out.println("*END*\nuri: "+uri+"\nlocalName: "+localName+"\nqName: "+qName);
	}
	
	public void characters(char[ ] chars, int start, int length) {
	  String tmp=new String(chars, start, length);
	  System.out.println("Found String: "+tmp);
	  if(inTree){
		if (featureTree==null) featureTree=tmp;
		else featureTree+=tmp;		
	  }
	  else if(inConstraintsList){
		if (constraints==null) constraints=tmp;
		else constraints+=tmp;		
	  }
	}
}

