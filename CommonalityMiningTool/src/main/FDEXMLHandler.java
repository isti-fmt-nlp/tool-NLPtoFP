package main;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class FDEXMLHandler extends DefaultHandler {

	//view tags
	boolean inFeaturesList=false;
	boolean inConnectorsList=false;
	boolean inGroupsList=false;
	boolean inConstraintsList=false;	
	boolean inMisc=false;
	boolean inStartingComm=false;
	boolean inStartingVars=false;
	boolean inFeatureColors=false;		
	boolean inFeatureOccurrences=false;		
	boolean inFeatureVersions=false;	
	//model tags
	boolean inTree=false;
	boolean inModelConstraintsList=false;	

	//view tags
	public String featuresList=null;
	public String connectorsList=null;
	public String groupsList=null;
	public String constraintsList=null;
	public String misc=null;
	public String startingComm=null;
	public String startingVars=null;
	public String featureColors=null;	
	public String featureOccurrences=null;	
	public String featureVersions=null;		
	//model tags	
	public String featureTree=null;
	public String modelConstraintsList=null;
	
	//list of feature models as XML strings
	public ArrayList<String[]> featureModels= new ArrayList<String[]>();
	String[] tmp =null;
	
	
	public void startElement(String uri, String localName, String qName, Attributes atts) {
	  //view tags
	  if (qName.equals("features")) inFeaturesList=true;
	  else if (qName.equals("connectors")) inConnectorsList=true;
	  else if (qName.equals("groups")) inGroupsList=true;
	  else if (qName.equals("constraints")) inConstraintsList=true;	  
	  else if (qName.equals("misc")) inMisc=true;
	  else if (qName.equals("startingCommonalities")) inStartingComm=true;
	  else if (qName.equals("startingVariabilities")) inStartingVars=true;
	  else if (qName.equals("featureColors")) inFeatureColors=true;
	  else if (qName.equals("featureOccurrences")) inFeatureOccurrences=true;
	  else if (qName.equals("featureVersions")) inFeatureVersions=true;	  
	  //model tags
	  else if (qName.equals("feature_tree")) inTree=true;
	  else if(qName.equals("modelConstraints")) inModelConstraintsList=true;
	  System.out.println("*START*\nuri: "+uri+"\nlocalName: "+localName+"\nqName: "+qName+"\nAttribute:");
	  for(int i=0; i< atts.getLength(); ++i)
		System.out.println(atts.getLocalName(i)+"="+atts.getValue(i));
	  
	}
	
	public void endElement(String uri, String localName, String qName) {
	  //view tags
	  if (qName.equals("features")) inFeaturesList=false;
	  else if (qName.equals("connectors")) inConnectorsList=false;
	  else if (qName.equals("groups")) inGroupsList=false;
	  else if (qName.equals("constraints")) inConstraintsList=false;
	  else if (qName.equals("misc")) inMisc=false;
	  else if (qName.equals("startingCommonalities")) inStartingComm=false;
	  else if (qName.equals("startingVariabilities")) inStartingVars=false;
	  else if (qName.equals("featureColors")) inFeatureColors=false;
	  else if (qName.equals("featureOccurrences")) inFeatureOccurrences=false;
	  else if (qName.equals("featureVersions")) inFeatureVersions=false;
	  //model tags
	  else if (qName.equals("feature_tree")){
		inTree=false;
		tmp=new String[2];
		tmp[0]=featureTree;
		featureTree=null;
	  }
	  else if(qName.equals("modelConstraints")){
		inModelConstraintsList=false;
		tmp[1]=modelConstraintsList;
		featureModels.add(tmp);
		modelConstraintsList=null;
	  }
	  System.out.println("*END*\nuri: "+uri+"\nlocalName: "+localName+"\nqName: "+qName);
	}
	
	public void characters(char[ ] chars, int start, int length) {
	  String tmp=new String(chars, start, length);
	  System.out.println("Found String: "+tmp);

	  //view tags	  
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
	  else if (inConstraintsList){
		if (constraintsList==null) constraintsList=tmp;
		else constraintsList+=tmp;		  
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
	  else if (inFeatureOccurrences){
		if (featureOccurrences==null) featureOccurrences=tmp;
		else featureOccurrences+=tmp;		  
	  }
	  else if (inFeatureVersions){
		if (featureVersions==null) featureVersions=tmp;
		else featureVersions+=tmp;		  
	  }

	  //model tags
	  if(inTree){
		if (featureTree==null) featureTree=tmp;
		else featureTree+=tmp;		
	  }
	  else if(inModelConstraintsList){
		if (modelConstraintsList==null) modelConstraintsList=tmp;
		else modelConstraintsList+=tmp;		
	  }
	 
	}

}
