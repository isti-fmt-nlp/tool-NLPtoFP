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
	boolean inFeatureColors=false;	

	public String featuresList=null;
	public String connectorsList=null;
	public String groupsList=null;
	public String misc=null;
	public String startingComm=null;
	public String startingVars=null;
	public String featureColors=null;	
	
	public void startElement(String uri, String localName, String qName, Attributes atts) {
	  if (qName.equals("features")) inFeaturesList=true;
	  else if (qName.equals("connectors")) inConnectorsList=true;
	  else if (qName.equals("groups")) inGroupsList=true;
	  else if (qName.equals("misc")) inMisc=true;
	  else if (qName.equals("startingCommonalities")) inStartingComm=true;
	  else if (qName.equals("startingVariabilities")) inStartingVars=true;
	  else if (qName.equals("featureColors")) inFeatureColors=true;
	  
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
	  else if (qName.equals("featureColors")) inFeatureColors=false;
		  
	  System.out.println("*END*\nuri: "+uri+"\nlocalName: "+localName+"\nqName: "+qName);
	}
	
	public void characters(char[ ] chars, int start, int length) {
	  String tmp=new String(chars, start, length);
	  System.out.println("Found String: "+tmp);
	  
	  if (inFeaturesList){
		if (featuresList==null) featuresList=tmp;
		else featuresList+=tmp;
	  }
	  else if (inConnectorsList){
		if (connectorsList==null) connectorsList=tmp;
		else connectorsList+=tmp;		  
	  }
	  else if (inGroupsList){
		if (groupsList==null) groupsList=tmp;
		else groupsList+=tmp;		  
	  }
	  else if (inMisc){
		if (misc==null) misc=tmp;
		else misc+=tmp;		  
	  }
	  else if (inStartingComm){
		if (startingComm==null) startingComm=tmp;
		else startingComm+=tmp;		  
	  }
	  else if (inStartingVars){
		if (startingVars==null) startingVars=tmp;
		else startingVars+=tmp;		  
	  }
	  else if (inFeatureColors){
		if (featureColors==null) featureColors=tmp;
		else featureColors+=tmp;		  
	  }
	}
}

