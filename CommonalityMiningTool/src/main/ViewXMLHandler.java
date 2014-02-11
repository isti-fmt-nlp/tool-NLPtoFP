package main;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ViewXMLHandler extends DefaultHandler {

	boolean inFeaturesList=false;
	boolean inConnectorsList=false;
	boolean inGroupsList=false;
	boolean inMisc=false;
	boolean inStartingComm=false;
	boolean inStartingVars=false;

	public String featuresList="";
	public String connectorsList="";
	public String groupsList="";
	public String misc="";
	public String startingComm="";
	public String startingVars="";
	
	public void startElement(String uri, String localName, String qName, Attributes atts) {
	  if (qName.equals("features")) inFeaturesList=true;
	  else if (qName.equals("connectors")) inConnectorsList=true;
	  else if (qName.equals("groups")) inGroupsList=true;
	  else if (qName.equals("misc")) inMisc=true;
	  else if (qName.equals("startingCommonalities")) inStartingComm=true;
	  else if (qName.equals("startingVariabilities")) inStartingVars=true;
	  
	  System.out.println("*START*\nuri: "+uri+"\nlocalName: "+localName+"\nqName: "+qName+"\nAttribute:");
	  for(int i=0; i< atts.getLength(); ++i)
		System.out.println(atts.getLocalName(i)+"="+atts.getValue(i));
	  
	}
	
	public void endElement(String uri, String localName, String qName) {
	  if (qName.equals("features")) inFeaturesList=false;
	  else if (qName.equals("connectors")) inConnectorsList=false;
	  else if (qName.equals("groups")) inGroupsList=false;
	  else if (qName.equals("misc")) inMisc=false;
	  else if (qName.equals("startingCommonalities")) inStartingComm=false;
	  else if (qName.equals("startingVariabilities")) inStartingVars=false;
		  
	  System.out.println("*END*\nuri: "+uri+"\nlocalName: "+localName+"\nqName: "+qName);
	}
	
	public void characters(char[ ] chars, int start, int length) {
	  String tmp=new String(chars, start, length);
	  System.out.println("Found String: "+tmp);
	  
	  if (inFeaturesList) featuresList+=tmp;
	  else if (inConnectorsList) connectorsList+=tmp;
	  else if (inGroupsList) groupsList+=tmp;
	  else if (inMisc) misc+=tmp;
	  else if (inStartingComm) startingComm+=tmp;
	  else if (inStartingVars) startingVars+=tmp;
	}
}

