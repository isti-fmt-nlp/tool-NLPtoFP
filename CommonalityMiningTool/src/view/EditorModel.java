package view;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import main.FDEXMLHandler;
import main.FeatureNode;
import main.FeatureNode.FeatureTypes;
import main.GroupNode.GroupTypes;
import main.ModelXMLHandler;
import main.GroupNode;

/**
 * INVARIANTS: 
 * - a groupNode exists only when its size() is > 0
 * - names of FeaturePanel objects correspond to name of corresponding FeatureNode objects
 * - names of GroupPanel objects correspond to name of corresponding GroupNode objects
 */
public class EditorModel extends Observable{

	/**
	 * Wraps a String in a wrapper object. 
	 * Used as a turn-around for java Strings immutability.
	 */
	static class StringWrapper {
		
	    public String string;
	    
	    /**
	     * Returns a new StringWrapper containig the String s.
	     * @param s - the String to be wrapped
	     */
	    public StringWrapper(String s) {
	        this.string = s;
	    }

	    /**
	     * Returns the length of the contained string. The length is equal to the number of Unicode code units in the string.
	     * This behaves exactly like the length() method in String.
	     * 
	     * @return the length of the sequence of characters represented by this object.
	     */
//		public int length() { return string.length();}
	}
	
	private static boolean debug = true;

	/** enumeration used to specify a group type in the model*/
	public static enum ConstraintTypes { INCLUDES, EXCLUDES};
	
//	/** Tells if the model has been modified after last save*/
//	private boolean modified=true;

//	/** root feature of the feature model*/
//	private FeatureNode featureRoot = null;
	
//	/** feature nodes that are descendant of root */
//	private HashMap<String, FeatureNode> rootLinkedFeatures = new HashMap<String, FeatureNode>();

	/** feature nodes that are not descendant of root */
	private HashMap<String, FeatureNode> featuresList = new HashMap<String, FeatureNode>();

	/** groups(with at least a member)*/
	private HashMap<String, GroupNode> groups= new HashMap<String, GroupNode>();

//	/** include constraints in the model */
//	private HashMap<String, String> includeConstraints= new HashMap<String, String>();
//
//	/** exclude constraints in the model */
//	private HashMap<String, String> excludeConstraints= new HashMap<String, String>();

	/** constraints in the model, each element has 3 Strings in this order:
	 *  constraint name, starting feature name, ending feature name */
	private ArrayList<String[]> constraints= new ArrayList<String[]>();

	/** prefix of any excludes constraint name*/
	public static String excludesConstraintNamePrefix="---EXCLUDE---#";
	
	/** prefix of any includes constraint name*/
	public static String includesConstraintNamePrefix="---INCLUDE---#";
	
//	/** Number of includes constraint created*/
//	private int includesCount=0;
//	/** Number of excludes constraint created*/
//	private int excludesCount=0;
	
	

	/**
	 * Creates a new empty editor model.
	 * 
	 */
	public EditorModel(){}

	
	/**
	 * Creates an editor model and adds to it the features contained in commonalities and variabilities lists.
	 * 
	 * @param commonalitiesSelected - list of commonality names
	 * @param variabilitiesSelected - list of variabilities names
	 */
/*
 * 	public EditorModel(ArrayList<String> commonalitiesSelected, ArrayList<String> variabilitiesSelected) {
	  if(commonalitiesSelected!=null)
		for(String name : commonalitiesSelected) addUnrootedFeature(name, FeatureTypes.COMMONALITY);
	  if(variabilitiesSelected!=null)
	    for(String name : variabilitiesSelected) addUnrootedFeature(name, FeatureTypes.VARIABILITY);
	}
*/
	/**
	 * Adds a newly created Commonality feature named name to the unrooted features.
	 * 
	 * @param name - String containing the name of the new feature
	 * @param id - String containing the ID of the new feature
	 */
	public void addUnrootedCommonality(String name, String id){
		/* ***DEBUG*** */
		if(debug) System.out.println("Creating new commonality: "+name);
		/* ***DEBUG*** */

		addUnrootedFeature(name, id, /*1*/FeatureTypes.COMMONALITY);
	}

	/**
	 * Adds a newly created Variability feature named name to the unrooted features.
	 * 
	 * @param name - String containing the name of the new feature
	 * @param id - String containing the ID of the new feature
	 */
	public void addUnrootedVariability(String name, String id){
		/* ***DEBUG*** */
		if(debug) System.out.println("Creating new variability: "+name);
		/* ***DEBUG*** */

		addUnrootedFeature(name, id, /*0*/FeatureTypes.VARIABILITY);
	}

	/**
	 * Adds a newly created feature named name of the specified type to the unrooted features.
	 * 
	 * @param name - String containing the name of the new feature
	 * @param id - String containing the ID of the new feature
	 * @param type - type of the feature to create, a value from the FeatureTypes enum type
	 */
	private void addUnrootedFeature(String name, String id, FeatureTypes type/*int minCard*/) {
		FeatureNode newFeature = new FeatureNode(type, name, id, /*minCard*/(type == FeatureTypes.COMMONALITY)? 1 : 0, 1);
		featuresList.put(id, newFeature);
		setChanged();
		notifyObservers("New Feature Correctly Added");
	}

	/**
	 * Adds a newly created feature named name of the specified type to the unrooted features,
	 *  without notifying the observers afterward.
	 * 
	 * @param featureName - String containing the name of the new feature
	 * @param id - String containing the ID of the new feature
	 * @param type - type of the feature to create, a value from the FeatureTypes enum type
	 */
	public void addUnrootedFeatureNoNotify(String featureName, String id, /*int minCard*/FeatureTypes type) {
		FeatureNode newFeature = new FeatureNode(type, featureName, id, /*minCard*/(type == FeatureTypes.COMMONALITY)? 1 : 0, 1);
		System.out.println("adding: unrooted feature: "+id);
		featuresList.put(id, newFeature);
	}

	/**
	 * Adds a newly created feature named name of the specified type and adds it as a subfeature to parent feature,
	 *  without notifying the observers afterward.
	 * 
	 * @param newFeatureName - the name of the new child feature
	 * @param parentNameID - the ID of parent feature
	 * @param id - String containing the ID of the new feature
	 * @param type - type of the new child feature, a value from the FeatureTypes enum type
	 */
	public void addSubFeatureNoNotify(String newFeatureName, String parentNameID, String id, /*int minCard*/FeatureTypes type) {
		FeatureNode child = new FeatureNode(type, newFeatureName, id, (type==FeatureTypes.COMMONALITY)? 1: 0/*minCard*/, 1);
		featuresList.put(id, child);
		
		FeatureNode parent= searchFeature(parentNameID);
		System.out.println("parentNameID: "+parentNameID+"\tparent="+parent);
		if ( parent!=null/* && child!=null && parent!=child && child.getParent()==null  
			&& !isDescendantOf(parent, child) && !isDescendantOf(child, parent)*/){
		  parent.getSubFeatures().add(child);
		  child.setParent(parent);
		}

	}

	/**
	 * Adds a newly created feature named name of the specified type to the unrooted features.
	 * 
	 * @param name - String containing the name of the new features
	 * @param type - type of the feature to create, a value from the FeatureTypes enum type
	 */
	public void addUnrootedNamedFeature(String name, String id, FeatureTypes type) {
		FeatureNode newFeature = new FeatureNode(type, name, id, 1, 1);
		featuresList.put(id, newFeature);
		setChanged();
		notifyObservers("New Named Feature Correctly Added");
	}

	/**
	 * Returns the HashMap containing all features that are not descendant of root.
	 * 
	 * @return - featuresList, an object of type: HashMap<String, FeatureNode>
	 */
	public HashMap<String, FeatureNode> getUnrootedFeatures(){
		return featuresList;
	}

	/**
	 * Returns the list containing all constraints in the model.
	 * 
	 * @return - constraints, an object of type: ArrayList<String[]>
	 */
	public ArrayList<String[]> getConstraints(){
		return constraints;
	}
	
	/**
	 * Group the feature groupMember with the group groupName of the feature groupOwner.<br>
	 * If the two features are already connected in any way, this method does nothing.<br>
	 * This method is called when an existing ending anchor of a group is dropped, <br>
	 * to merge connectors see mergeConnectorWithGroup(String, String, String).
	 * 
	 * @param groupOwnerID - the ID of feature owner of the group
	 * @param groupMemberID - the ID of feature to be grouped
	 * @param groupName - the name of the group
	 * @param type 
	 * 
	 * @see {@link EditorModel#mergeConnectorWithGroup(String, String, String)}
	 */
	public void addFeatureToGroup(String groupOwnerID, String groupMemberID, String groupName, GroupTypes type){
	  FeatureNode parent= null;
	  GroupNode group = searchGroup(groupName);
	  FeatureNode sub= searchFeature(groupMemberID);
	  boolean groupFound= (group==null)? false : true;
	  int maxCardinality=0;
	  
	  if(groupOwnerID!=null) parent=searchFeature(groupOwnerID);
		  
	  //if the candidate member is not found, operation is aborted
	  if (sub==null){ 
		setChanged();
		notifyObservers("Not Grouped a Feature");
		return;
	  }

	  //if the operation would induce a cycle in the diagram, it is aborted
	  if ( parent!=null && (parent==sub || isDescendantOf(parent, sub) || isDescendantOf(sub, parent)) ){
		setChanged();
		notifyObservers("Not Grouped a Feature");		
		return;
	  }
	  
	  //if the group was not found, it is created
	  if (!groupFound){
		switch (type){
		  case ALT_GROUP: maxCardinality=1; break;
		  case OR_GROUP: maxCardinality=2; break;
		  case N_M_GROUP: maxCardinality=GroupNode.CARD_UNDEF; break;
		}
		
		group = new GroupNode(groupName, 1, maxCardinality, new ArrayList<FeatureNode>());
		groups.put(groupName, group);
		//if group icon was over a feature in the diagram, the new GroupNode is added to the corresponding FeatureNode
		if(parent!=null) parent.getSubGroups().add(group);
	  }
	  
	  //adding the feature to the group
	  group.getSubFeatures().add(sub);
	  if (parent!=null) sub.setParent(parent);
	  setChanged();
	  notifyObservers("Grouped a Feature");		
	}
	
	/**
	 * Merges a connector with a group.<br>
	 * This method is called when a starting anchor must be merged with a group, <br>
	 * to drop a group ending anchor on a feature see addFeatureToGroup(String, String, String).
	 * 
	 * @param groupOwnerID - ID of the feature owner of the group, or null if the group is not owned by a feature
	 * @param groupMemberID - ID of the feature to be grouped, or null if only an anchor must be grouped
	 * @param groupName - the name of the group	 
	 * @param type 
	 * 
	 * @see {@link EditorModel#addFeatureToGroup(String, String, String)}
	 */
	public void mergeConnectorWithGroup(String groupOwnerID, String groupMemberID, String groupName, GroupTypes type){
	  FeatureNode parent = null;
	  GroupNode group = searchGroup(groupName);
	  FeatureNode sub = null;
	  boolean groupFound = false;
	  int maxCardinality=0;
	  
	  if(groupOwnerID!=null) parent = searchFeature(groupOwnerID);
	  if(groupMemberID!=null) sub = searchFeature(groupMemberID);
	  groupFound = (group==null)? false : true;
	  
	  //if the candidate member is not found, merging does not modify the model
	  if(sub==null){
		setChanged();
		notifyObservers("Merged a Connector");	
		return;
	  }

	  //if the operation would induce a cycle in the diagram, it is aborted
	  if ( parent!=null && (parent==sub || isDescendantOf(parent, sub) || isDescendantOf(sub, parent)) ){
		setChanged();
		notifyObservers("Not Merged a Connector");		
		return;
	  }
	  
	  //if the group was not found, it is created
	  if (!groupFound){
		switch (type){
		  case ALT_GROUP: maxCardinality=1; break;
		  case OR_GROUP: maxCardinality=2; break;
		  case N_M_GROUP: maxCardinality=GroupNode.CARD_UNDEF; break;
		}
		
		group = new GroupNode(groupName, 1, maxCardinality, new ArrayList<FeatureNode>());
		groups.put(groupName, group);
		//if group icon was over a feature in the diagram, the new GroupNode is added to the corresponding FeatureNode
		if(parent!=null) parent.getSubGroups().add(group);
	  }
	  
	  //adding the feature to the group
	  group.getSubFeatures().add(sub);
	  if(parent!=null) sub.setParent(parent);
	  setChanged();
	  notifyObservers("Merged a Connector");
	  		
	}
	
	/**
	 * Adds a mandatory link between parent feature and the sub feature.<br>
	 * If the two features are already connected in any way, this method does nothing.<br>
	 * As a side effect, if the minimum cardinality of subFeature is 0, it is set to 1.
	 * 
	 * @param parentFeatureID - the ID of parent feature to link
	 * @param subFeatureID - the ID of sub-feature to link
	 */
	public void addMandatoryLink(String parentFeatureID, String subFeatureID){
	  FeatureNode parent= searchFeature(parentFeatureID);
	  FeatureNode sub= searchFeature(subFeatureID);
	  Point subCard=null;
	  
	  if ( parent!=null && sub!=null && parent!=sub && sub.getParent()==null  
		   && !isDescendantOf(parent, sub) && !isDescendantOf(sub, parent)){
		parent.getSubFeatures().add(sub);
		sub.setParent(parent);
		//setting a mandatory cardinality
		subCard=sub.getCardinality();
		sub.setCardinality(subCard.x>0? subCard.x : 1, subCard.y);
		setChanged();
		notifyObservers("Two Features Directly Linked");			
	  }
	  else{
		setChanged();
		notifyObservers("Two Features Not Linked");			
	  }

	}
	
	/**
	 * Adds a optional link between parent feature and the sub feature.<br>
	 * If the two features are already connected in any way, this method does nothing.
	 * As a side effect, the minimum cardinality of subFeature is set to 0.
	 * 
	 * @param parentFeatureID - the ID of parent feature to link
	 * @param subFeatureID - the ID of sub-feature to link
	 */
	public void addOptionalLink(String parentFeatureID, String subFeatureID){
	  FeatureNode parent= searchFeature(parentFeatureID);
	  FeatureNode sub= searchFeature(subFeatureID);
	  
	  if ( parent!=null && sub!=null && parent!=sub && sub.getParent()==null  
		   && !isDescendantOf(parent, sub) && !isDescendantOf(sub, parent)){
		parent.getSubFeatures().add(sub);
		sub.setParent(parent);
		//setting an optional cardinality
		sub.setCardinality(0, sub.getCardinality().y);
		setChanged();
		notifyObservers("Two Features Directly Linked");			
	  }
	  else{
		setChanged();
		notifyObservers("Two Features Not Linked");			
	  }

	}
	
	/**
	 * Adds a constraint from startingFeature to endingFeature, if it is legal.<br>
	 * 
	 * @param startingFeatureID - ID of the feature from which the constraint starts
	 * @param endingFeatureID - ID of the feature on which the constraint ends
	 * @param type - a ConstraintTypes value representing the type of constraint
	 * @param IDnum - a String representing the unique sequential number of this constraint
	 */
	public void addConstraint(String startingFeatureID, String endingFeatureID, ConstraintTypes type, String IDnum){
	  FeatureNode starting= searchFeature(startingFeatureID);
	  FeatureNode ending= searchFeature(endingFeatureID);
	  String[] newConstraint=null;
	  
	  if ( starting==null || ending==null || starting==ending){
		setChanged();
		notifyObservers("Constraint Not Added");			
		return;
	  }

	  switch(type){
	    case INCLUDES:
	      for(String[] strArr: constraints){
		    if (strArr[1].compareTo(startingFeatureID)==0
		    	&& strArr[2].compareTo(endingFeatureID)==0){//exclude or equivalent include already present
		  	  setChanged();
		  	  notifyObservers("Constraint Not Added");			
		  	  return;	    	  
		    }
		    if (strArr[0].startsWith(excludesConstraintNamePrefix)
		    	&& strArr[1].compareTo(endingFeatureID)==0
		    	&& strArr[2].compareTo(startingFeatureID)==0){//exclude already present
		      setChanged();
		      notifyObservers("Constraint Not Added");
		      return;	    	  
		    }	    	  
	      }
//	      //multiple includes with the same orientation are not possible between two features
//	      tmpID=includeConstraints.get(startingFeature);
//	      if(tmpID!=null && tmpID.compareTo(endingFeature)==0){
//	  		setChanged();
//			notifyObservers("Constraint Not Added");			
//			return;	    	  
//	      }
//	      //no excludes must be present between two features to add an includes
//	      tmpID=excludeConstraints.get(startingFeature);
//	      if(tmpID!=null && tmpID.compareTo(endingFeature)==0){
//	  		setChanged();
//			notifyObservers("Constraint Not Added");			
//			return;	    	  
//	      }
	      break;
	    case EXCLUDES:
		  for(String[] strArr: constraints){
			if ( (strArr[1].compareTo(startingFeatureID)==0
			      && strArr[2].compareTo(endingFeatureID)==0) ||
			     (strArr[1].compareTo(endingFeatureID)==0
			      && strArr[2].compareTo(startingFeatureID)==0) ){//exclude or include already present
			  setChanged();
			  notifyObservers("Constraint Not Added");			
			  return;	    	  
			} 	  
		  }	    	
			
//	      //multiple excludes are not possible between two features
//	      tmpID=excludeConstraints.get(startingFeature);
//	      if(tmpID!=null && tmpID.compareTo(endingFeature)==0){
//	    	setChanged();
//	    	notifyObservers("Constraint Not Added");			
//	    	return;	    	  
//	      }
//	      //no includes must be present between two features to add an excludes
//	      tmpID=includeConstraints.get(startingFeature);
//	      if(tmpID!=null && tmpID.compareTo(endingFeature)==0){
//	  		setChanged();
//			notifyObservers("Constraint Not Added");			
//			return;	    	  
//	      }
//	      tmpID=includeConstraints.get(endingFeature);
//	      if(tmpID!=null && tmpID.compareTo(startingFeature)==0){
//	  		setChanged();
//			notifyObservers("Constraint Not Added");			
//			return;	    	  
//	      }
	      break;
	  }
	  
	  //adding constraint
	  switch(type){
	    case INCLUDES:
	      newConstraint=new String[3];
	      newConstraint[0]=includesConstraintNamePrefix+IDnum;
//	      newConstraint[0]=includesConstraintNamePrefix+includesCount;
	      newConstraint[1]=startingFeatureID;
	      newConstraint[2]=endingFeatureID;
	      constraints.add(newConstraint);
//	      ++includesCount;
	      break;
	    case EXCLUDES:
		  newConstraint=new String[3];
		  newConstraint[0]=excludesConstraintNamePrefix+IDnum;
//		  newConstraint[0]=excludesConstraintNamePrefix+excludesCount;
		  newConstraint[1]=startingFeatureID;
		  newConstraint[2]=endingFeatureID;
		  constraints.add(newConstraint);
//		  ++excludesCount;
		  break;
	  }
	  setChanged();
	  notifyObservers("Constraint Added");			
	}
	
	/**
	 * Removes a constraint from startingFeature to endingFeature, if present.<br>
	 * 
	 * @param startingFeatureID - the feature from which the constraint starts
	 * @param endingFeatureID - the feature on which the constraint ends
	 * @param type - a ConstraintTypes value representing the type of constraint
	 */
	public void removeConstraint(String startingFeatureID, String endingFeatureID, ConstraintTypes type){
	  boolean found=false;
	  String[] strArr=null;
	  switch(type){
	  case INCLUDES:
		for(int i=0; i<constraints.size(); ++i){
		  strArr=constraints.get(i);
		  if ( strArr[0].startsWith(includesConstraintNamePrefix)
			   && strArr[1].compareTo(startingFeatureID)==0
			   && strArr[2].compareTo(endingFeatureID)==0 ){ found=true; break;}
		}
		if(found) constraints.remove(strArr);
	    break;
	  case EXCLUDES:
		for(int i=0; i<constraints.size(); ++i){
		  strArr=constraints.get(i);
		  if ( strArr[0].startsWith(excludesConstraintNamePrefix)
			   && strArr[1].compareTo(startingFeatureID)==0
			   && strArr[2].compareTo(endingFeatureID)==0 ){ found=true; break;}
		  else if ( strArr[0].startsWith(excludesConstraintNamePrefix)
				   && strArr[1].compareTo(endingFeatureID)==0
				   && strArr[2].compareTo(startingFeatureID)==0 ){ found=true; break;}
		}
		if(found) constraints.remove(strArr);
		break;		
	  }
	  if(found){
		setChanged();
		notifyObservers("Constraint Removed");		  
	  }
	  else{
		setChanged();
		notifyObservers("Constraint Not Removed");		  
	  }	  
	}	
	
	/**
	 * Adds a group to a feature.<br>
	 * If the feature and the members of the group and are already connected in any way, this method does nothing.
	 * 
	 * @param featureID - ID of the feature that will take the group
	 * @param group - the group to be added
	 */
	public void addGroupToFeature(String featureID, String group){
	  FeatureNode featureNode = searchFeature(featureID);
	  GroupNode groupNode = searchGroup(group);
	  System.out.println("addGroupToFeature(): "+featureID+" = "+featureNode+"  "+group+" = "+groupNode);
	  
	  if ( featureNode==null){
		setChanged();
		notifyObservers("Group Not Added To Feature");			
		return;
	  };
	  //if the operation would induce a cycle in the diagram, it is aborted
	  if (groupNode!=null){
		for(FeatureNode member : groupNode.getSubFeatures())
		  if (member==featureNode || isDescendantOf(featureNode, member) || isDescendantOf(member, featureNode)){
			setChanged();
			notifyObservers("Group Not Added To Feature");			
			return;
		  };
		
		featureNode.getSubGroups().add(groupNode);
		for(FeatureNode member : groupNode.getSubFeatures()) member.setParent(featureNode);
		setChanged();
		notifyObservers("Group Added To Feature");			
	  }
	  else{
		setChanged();
		notifyObservers("Group Added To Feature");			
	  }
	}	
	
	/**
	 * Removes a group from a feature in the model.
	 * 
	 * @param featureID - ID of the feature owner of the group
	 * @param group - the group to remove
	 */
	public void removeGroupFromFeature(String featureID, String group){
	  System.out.println("RemoveGroupFromfeature");
	  FeatureNode featureNode = searchFeature(featureID);
	  GroupNode groupNode = searchGroup(group);
	  
	  if ( featureNode!=null && groupNode!=null){
		if (!featureNode.getSubGroups().contains(groupNode) ){
			setChanged();
			notifyObservers("Group Not Removed From Feature");			
			return;
		};
		
		featureNode.getSubGroups().remove(groupNode);
		for(FeatureNode member : groupNode.getSubFeatures()) member.setParent(null);
		setChanged();
		notifyObservers("Group Removed From Feature");			
	  }
	  else{
		setChanged();
		notifyObservers("Group Removed From Feature");			
	  }
	}	

	/**
	 * Removes a direct link(not grouped) between two features in the model.
	 * 
	 * @param parentFeatureID - ID of the parent feature
	 * @param subFeatureID - ID of the sub-feature
	 */
	public void removeLink(String parentFeatureID, String subFeatureID){
	  FeatureNode parent= searchFeature(parentFeatureID);
	  FeatureNode sub= searchFeature(subFeatureID);
	  if (parent!=null && sub!=null){
		if (parent.getSubFeatures().contains(sub)){
		  parent.getSubFeatures().remove(sub);
		  sub.setParent(null);
		  setChanged();
		  notifyObservers("Direct Link Destroyed");
		  return;
		}
	  }
	  setChanged();
	  notifyObservers("Direct Link Not Destroyed");
	}
	
	/**
	 * Removes a feature from a group.
	 * 
	 * @param groupOwnerID - the group's owner feature ID
	 * @param groupMemberID - the group's member feature ID
	 * @param groupName - the group's name
	 */
	public void removeFeatureFromGroup(String groupOwnerID, String groupMemberID, String groupName){		
	  FeatureNode parent = null;
	  FeatureNode sub= searchFeature(groupMemberID);
	  GroupNode group = searchGroup(groupName);
		  
	  if(groupOwnerID!=null) parent = searchFeature(groupOwnerID);

	  //if the group or the member was not found, operation is aborted
	  if(sub==null || group==null) {
		setChanged();
		notifyObservers("Direct Link Not Destroyed");	
		return;
	  }
		  
	  //the group was owned by a feature
	  if (parent!=null){
		for(GroupNode featureGroup : parent.getSubGroups()){
		  System.out.println("featureGroup.getName()="+featureGroup.getID());
		  if(featureGroup.getID().equals(groupName) )
			if (featureGroup.getSubFeatures().contains(sub)){
			  featureGroup.getSubFeatures().remove(sub);
			  sub.setParent(null);
			  //if the group size is 0, it gets removed form the model
			  if(featureGroup.getSubFeatures().size()==0){
				groups.remove(groupName);
				parent.getSubGroups().remove(featureGroup);
			  }
			  setChanged();
			  notifyObservers("Direct Link Destroyed");
			  return;
		    }
			else{
			  //the member feature 'sub' is not present in the group found
			  setChanged();
			  notifyObservers("Direct Link Not Destroyed");		
			  return;
			}
		}
		//the group was not found 
		setChanged();
		notifyObservers("Direct Link Not Destroyed");	
		return;
	  }
	  //the group was not owned by any feature
	  else{
		if (group.getSubFeatures().contains(sub)){
		  group.getSubFeatures().remove(sub);
//		  sub.setHasParent(false);
		  //if the group size is 0, it gets removed form the model
		  if(group.getSubFeatures().size()==0) groups.remove(groupName);
		  setChanged();
		  notifyObservers("Direct Link Destroyed");
		  return;
		}		
		else{
		  //the member feature 'sub' is not present in the group found
		  setChanged();
		  notifyObservers("Direct Link Not Destroyed");		
		  return;
		}
	  }
	}

	/**
	 * Deletes a feature from the model.
	 * 
	 * @param name - the name of the feature to be deleted
	 */
	public void deleteFeature(String name) {
	  FeatureNode featurefound=featuresList.get(name);
	  if (featurefound==null){
		setChanged();
		notifyObservers("Feature Not Deleted");
		return;
	  }
	  //removing the feature from the parent, if any
	  if(featurefound.getParent()!=null){
		//the parent feature is directly linked
	    if(((FeatureNode)featurefound.getParent()).getSubFeatures().contains(featurefound)){
		  System.out.println("The feature: "+featurefound+" has a direct parent feature: "+
				  ((FeatureNode)featurefound.getParent()).getID());
		  ((FeatureNode)featurefound.getParent()).getSubFeatures().remove(featurefound);
	    }
		//the parent feature is linked through a group
	    else{
	      for(GroupNode group : ((FeatureNode)featurefound.getParent()).getSubGroups())
	    	if(group.getSubFeatures().contains(featurefound)){
	  		  System.out.println("The feature: "+featurefound.getID()+" has a parent feature: "+
					  ((FeatureNode)featurefound.getParent()).getID()+" through the group: "+group.getID());
	    	  group.getSubFeatures().remove(featurefound);
	    	}	      
	    }
	  }

	  //setting parent attribute of this feature childs to null
	  for(GroupNode group : featurefound.getSubGroups())
	    for(FeatureNode member : group.getSubFeatures()) member.setParent(null);
	  for(FeatureNode child : featurefound.getSubFeatures()) child.setParent(null);

	  //removing constraints related to this feature
	  ArrayList<String[]> strArrList=new ArrayList<String[]>();
	  for(String[] strArr: constraints)
		if ( strArr[1].compareTo(name)==0 || strArr[2].compareTo(name)==0 ) strArrList.add(strArr);

	  for(String[] strArr: strArrList) constraints.remove(strArr);

	  //removing the feature from his feature list
	  if(featuresList.containsKey(name)) featuresList.remove(name);
//	  if(rootLinkedFeatures.containsKey(name)) rootLinkedFeatures.remove(name);
	  setChanged();
	  notifyObservers("Feature Deleted");
	}

	/**
	 * Deletes a group from the model.
	 * 
	 * @param name - the name of the group to be deleted
	 */
	public void deleteUnattachedGroup(String name) {
	  GroupNode group = searchGroup(name);
	  if (group!=null) for(FeatureNode member : group.getSubFeatures()) member.setParent(null);
	  setChanged();
	  notifyObservers("Group Deleted");		  
	}

	/**
	 * Changes the name of a feature, if it is present in the model.
	 * 
	 * @param id - ID of the feature
	 * @param newName - the new name of the feature
	 */
	public void changeFeatureName(String id, String newName){
	  FeatureNode featurefound=featuresList.get(id);
	  if (featurefound==null){
		setChanged();
		notifyObservers("Feature Not Renamed");		  
	  }
	  else{
		featurefound.setName(newName);
		setChanged();
		notifyObservers("Feature Renamed");		
	  }
	}
	
	/**
	 * Search for a feature node named name in the model.
	 * 
	 * @param featureID - the ID of the feature to search for
	 * @return - a FeatureNode object representing the feature found, or null if it's not present in the model
	 */
	private FeatureNode searchFeature(String featureID){
	  FeatureNode featurefound=featuresList.get(featureID);
//	  if(featurefound==null) featurefound=rootLinkedFeatures.get(name);
//	  if(featurefound==null) featurefound=unrootedFeatures.get(name);
	  
	  /* ***DEBUG*** */
	  if(debug && featurefound!=null) System.out.println("searchFeature("+featureID+"): "+featurefound.getID());
	  /* ***DEBUG*** */

	  return featurefound;
	}

	/**
	 * Search for a group node named name in the model.
	 * 
	 * @param name - the name of the group to search for
	 * @return - a GroupNode object representing the group found, or null if it's not present in the model
	 */
	private GroupNode searchGroup(String name){
	  GroupNode groupfound=groups.get(name);

	  /* ***DEBUG*** */
	  if(debug && groupfound!=null) System.out.println("searchGroup("+name+"): "+groupfound.getID());
	  /* ***DEBUG*** */

	  return groupfound;
	}
	
	/**
	 * Returns the list of all features in the model.
	 * 
	 * @return - an HashMap<String, FeatureNode>, mapping each feature ID to the correspondent featureNode
	 */
	public HashMap<String, FeatureNode> getFeaturesList(){
		return featuresList;
	}
	
	/**
	 * Returns the root feature of this model, if it is unique.
	 * 
	 * @return - the unique root featureNode if present, null otherwise
	 */
	public FeatureNode getUniqueRootfeature(){
		FeatureNode unique = null;
		for(Entry<String, FeatureNode> feature : featuresList.entrySet()){
		  if(feature.getValue().getParent()!=null) continue;
		  else if(unique!=null) return null;//there is more than 1 unrooted feature
		  else unique=feature.getValue();
		};
		return unique;
	}
	
	
	/**
	 * Uses a Depth first recursive search algorithm to check if the feature descendant <br>
	 * really is a descendant of the feature ancestor.
	 * 
	 * @param ancestor - the possible ancestor feature
	 * @param descendant - the possible descendant feature
	 * @return true if the feature descendant really is a descendant of the feature ancestor, false otherwise
	 */
	private boolean isDescendantOf(FeatureNode ancestor, FeatureNode descendant){
	  System.out.println("ancestor: "+ancestor.getID()+"\tdescendant: "+descendant.getID());
	  for(FeatureNode child : ancestor.getSubFeatures()){
		System.out.println("child.getName()="+child.getID());
		if (child.getID().equals(descendant.getID())
			|| isDescendantOf(child, descendant)) return true;
	  }
	  for(GroupNode group : ancestor.getSubGroups()){
		System.out.println("group .getName()="+group.getID());
		for(FeatureNode child : group.getSubFeatures() ){
		  System.out.println("child.getName()="+child.getID());
		  if (child.getID().equals(descendant.getID())
			|| isDescendantOf(child, descendant)) return true;
		}
	  }
	  return false;	
	}

	/**
	 * Saves the elements of the feature model on file.
	 * @param pathProject - the directory path where to save the model
	 * @param s - the name of the file in which to save the model
	 */
	public ArrayList<String> saveModel(String pathProject, final String s) {
	  String xml = null;
	  final String savePathPrefix = pathProject + "/" + s + "_DiagModel"; 
	  String savePathSuffix= ".xml";
	  String date=null;
	  ArrayList<String> modelPaths=new ArrayList<String>();
	  File[] oldSaveFiles=null;
	  File saveDir=null;
	  
	  //calculating save time in a 'yyyyy-mm-dd hh:mm' format
	  Calendar cal= Calendar.getInstance();
	  date=cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH+1)
			  +"-"+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE);

	  //cleaning previous model save files
	  System.out.println("About to delete files starting with: "+s + "_DiagModel");
	  saveDir = new File(pathProject);
	  oldSaveFiles = saveDir.listFiles(new FilenameFilter() {
	    public boolean accept(File dir, String name) {
	      return name.startsWith(s + "_DiagModel") && name.endsWith(".xml");
	    }
	  });
	  for(File oldFile : oldSaveFiles){
		  System.out.println("About to delete file: "+oldFile.getName()
			+"\nDelete done?"+(oldFile.delete()?"yes":"no"));
	  }
	  
	  for(Map.Entry<String,FeatureNode> feature : featuresList.entrySet()){
	    //skipping features that have a parent feature
		if(feature.getValue().getParent()!=null) continue;
		
		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			  +"<feature_model name=\""+s+"_"+feature.getValue().getID()+"\">"
				+"<meta>"
			    +"<data name=\"date\">"+date+"</data>"
				+"</meta>"
				+"<feature_tree>";
		
		xml+=":r "+feature.getValue().getName()+" ("+feature.getValue().getID()+") ["
				+feature.getValue().getMinCardinality()+"."+feature.getValue().getMaxCardinality()+"]"
				+recursiveXMLTreeBuilder(feature.getValue(), 1);

		xml+=	 "</feature_tree>"
				+"<constraints>";

		for(String[] strArr: constraints){
		  if(strArr[0].startsWith(includesConstraintNamePrefix))
			xml+=strArr[0].substring(includesConstraintNamePrefix.length())+": ~"+strArr[1]+" or "+strArr[2]+"\n";
		  else 
			xml+=strArr[0].substring(excludesConstraintNamePrefix.length())+": ~"+strArr[1]+" or ~"+strArr[2]+"\n";
		}

		xml+=	 "</constraints>"
				+"</feature_model>";

		//saving xml string on file
		try{
		  //checking if the diagrams save directory must be created
		  File dir=new File(pathProject);		
		  if(!dir.isDirectory() && !dir.mkdirs()) throw new IOException("Save Directory can't be created.");

		  PrintWriter pw1 = new PrintWriter(new BufferedWriter(
				  new FileWriter(savePathPrefix+feature.getValue().getID()+savePathSuffix) ));
		  pw1.print(xml);
		  pw1.close();
		  modelPaths.add(savePathPrefix+feature.getValue().getID()+savePathSuffix);
		} 
		catch (IOException e){
		  System.out.println("Exception saveModel: " + e.getMessage());
		  e.printStackTrace();
		  return null;
		}		  

	  }
	  return modelPaths;
	}

	/**
	 * Saves the elements of the feature model on  an XML String.
	 * @param pathProject - the directory path where to save the model
	 * @param s - the name of the file in which to save the model
	 * 
	 * @return - the XML String produced
	 */
	public ArrayList<String> saveModel2(/*String pathProject, final */  /*String*/File s) {
	  String xml = null;
	  String date=null;
	  ArrayList<String> modelPaths=new ArrayList<String>();
	  
	  //calculating save time in a 'yyyyy-mm-dd hh:mm' format
	  Calendar cal= Calendar.getInstance();
	  date=cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH+1)
			  +"-"+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE);

	  for(Map.Entry<String,FeatureNode> feature : featuresList.entrySet()){
	    //skipping features that have a parent feature
		if(feature.getValue().getParent()!=null) continue;
		
		xml = "<feature_model name=\""+s.getName()+"_"+feature.getValue().getID()+"\">"
				+"<meta>"
			    +"<data name=\"date\">"+date+"</data>"
				+"</meta>"
				+"<feature_tree>";
		
		xml+=":r "+feature.getValue().getName()+" ("+feature.getValue().getID()+") ["
				+feature.getValue().getMinCardinality()+"."+feature.getValue().getMaxCardinality()+"]"
				+recursiveXMLTreeBuilder(feature.getValue(), 1);

		xml+=	 "</feature_tree>"
				+"<modelConstraints>";

		for(String[] strArr: constraints){
		  if(strArr[0].startsWith(includesConstraintNamePrefix))
			xml+=strArr[0].substring(includesConstraintNamePrefix.length())+": ~"+strArr[1]+" or "+strArr[2]+"\n";
		  else 
			xml+=strArr[0].substring(excludesConstraintNamePrefix.length())+": ~"+strArr[1]+" or ~"+strArr[2]+"\n";
		}

		xml+=	 "</modelConstraints>"
				+"</feature_model>";
		
		modelPaths.add(xml);
	  }
	  return modelPaths;
	}

	/**
	 * Export the feature model in the SXFM xml format on file.
	 * @param savePath - the directory path where to save the exported file
	 * @param s - the name of the file in which to esport the model
	 */
	public ArrayList<String> exportAsSXFM(String savePath, String s) {
	  String xml = null;
	  String savePathPrefix = savePath + "/" + s + "_SXFM"; 
	  String savePathSuffix= ".xml";
	  String date=null;
	  ArrayList<String> modelPaths=new ArrayList<String>();

	  //calculating save time in a 'yyyyy-mm-dd hh:mm' format
	  Calendar cal= Calendar.getInstance();
	  date=cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH+1)
			  +"-"+cal.get(Calendar.DAY_OF_MONTH)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE);

	  for(Map.Entry<String,FeatureNode> feature : featuresList.entrySet()){
	    //skipping features that have a parent feature
		if(feature.getValue().getParent()!=null) continue;
		
		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			  +"<feature_model name=\""+s+"_"+feature.getValue().getID()+"\">"
				+"<meta>"
			    +"<data name=\"date\">"+date+"</data>"
				+"</meta>"
				+"<feature_tree>";
		
		xml+=":r "+feature.getValue().getName()+" ("+feature.getValue().getID()+")"
				+recursiveSXFMTreeBuilder(feature.getValue(), 1);

		xml+=	 "</feature_tree>"
				+"<constraints>";

		for(String[] strArr: constraints){
		  if(strArr[0].startsWith(includesConstraintNamePrefix))
			xml+=strArr[0].substring(includesConstraintNamePrefix.length())+": ~"+strArr[1]+" or "+strArr[2]+"\n";
		  else
			xml+=strArr[0].substring(excludesConstraintNamePrefix.length())+": ~"+strArr[1]+" or ~"+strArr[2]+"\n";
		}

		xml+=	 "</constraints>"
				+"</feature_model>";

		//saving xml string on file
		try{
		  //checking if the SXFM files save directory must be created
		  File dir=new File(savePath);		
		  if(!dir.isDirectory() && !dir.mkdirs()) throw new IOException("Save Directory can't be created.");

		  PrintWriter pw1 = new PrintWriter(new BufferedWriter(
				  new FileWriter(savePathPrefix+feature.getValue().getID()+savePathSuffix) ));
		  pw1.print(xml);
		  pw1.close();
		  modelPaths.add(savePathPrefix+feature.getValue().getID()+savePathSuffix);
		} 
		catch (IOException e){
		  System.out.println("Exception saveModel: " + e.getMessage());
		  e.printStackTrace();
		  return null;
		}		  

	  }
	  return modelPaths;
	}

/*
<feature_model name="My feature model">     <-- feature model start tag and name attribute (mandatory)
  <meta>                                                                  <-- Optional
  <data name="description">Model description</data>                 <-- Optional
  <data name="creator">Model's creator</data>                       <-- Optional
  <data name="email">Model creator's email</data>                   <-- Optional
  <data name="date">Model creation date</data>                      <-- Optional
  <data name="department">Model creator's department</data>         <-- Optional
  <data name="organization">Model creator's organization</data>     <-- Optional
  <data name="address">Model creator's address</data>               <-- Optional
  <data name="phone">Model creator's phone</data>                   <-- Optional
  <data name="website">Model creator's website</data>               <-- Optional
  <data name="reference">Model's related publication</data>         <-- Optional
  </meta>
  <feature_tree>                <-- feature tree start tag
    :r root (root_id)                 <-- root feature named 'root' with unique ID 'root_id'   						
      :o opt1 (id_opt1)               <-- an optional feature named opt1 with unique id id_opt1
      :o opt2 (id_opt2)               <-- an optional feature named opt2, child of opt1 with unique id id_opt2
      :m man1                         <-- an mandatory feature named man1 with unique id id_man1
        :g [1,*]                      <-- an inclusive-OR feature group with cardinality [1..*] ([1..3] also allowed)
          : a (id_a)                  <-- a grouped feature name a with ID id_a
          : b (id_b)                  <-- a grouped feature name b with ID id_b
            :o opt3 (id_opt3)         <-- an optional feature opt3 child of b with unique id id_opt3
          : c (id_c)                  <-- a grouped feature name c with ID id_c
        :g [1,1]                      <-- an exclusive-OR feature group with cardinality [1..1]
          : d (id_d)                  <-- a grouped feature name d with ID id_d
          : e (id_e)                  <-- a grouped feature name e with ID id_e
            :g [2,3]                      <-- a feature group with cardinality [2..3] children of feature e
              : f (id_f)                  <-- a grouped feature name f with ID id_f
              : g (id_g)                  <-- a grouped feature name g with ID id_g
              : h (id_h)                  <-- a grouped feature name h with ID id_h
  </feature_tree>               <-- feature tree end tag (mandatory)
  <constraints>                 <-- extra constraints start tag (mandatory)
    c1: ~id_a or id_opt2        <-- extra constraint named c1: id_a implies id_opt2 (must be a CNF clause)
    c2: ~id_c or ~id_e          <-- extra constraint named c2: id_c excludes id_e (must be a CNF clause)
  </constraints>                <-- extra constraint end tag (mandatory)
</feature_model>  	
 */
	
	/**
	 * Recursevely visits all features and groups in the feature tree <br>
	 * rooted in featureRoot, creating the xml string of the tree.
	 * 
	 * @param featureRoot - the root feature from which to build the xml string
	 * @param depth - the current tree level depth 
	 * @return - the xml string representing the tree rooted in featureRoot
	 */
	private String recursiveXMLTreeBuilder(FeatureNode featureRoot, int depth) {
	  String xml="";
	  for(FeatureNode child : featureRoot.getSubFeatures()){
		for(int i=0; i<depth; ++i) xml+="\t";
		if(child.getMinCardinality()==0) xml+=":o ";
		else xml+=":m ";
		
		xml+= child.getName()+" ("+child.getID()+") ["
			 +child.getMinCardinality()+"."+child.getMaxCardinality()+"]"
			 +recursiveXMLTreeBuilder(child, depth+1);
	  }
	  for(GroupNode group : featureRoot.getSubGroups()){
		for(int i=0; i<depth; ++i) xml+="\t";
		xml+=":g "+group.getID()+" ["+group.getMinCardinality()+"."+group.getMaxCardinality()+"]";
		for(FeatureNode member : group.getSubFeatures()){
			for(int i=0; i<depth+1; ++i) xml+="\t";
			xml+= ": "+member.getName()+" ("+member.getID()+") ["
			   +member.getMinCardinality()+"."+member.getMaxCardinality()+"]"
			   +recursiveXMLTreeBuilder(member, depth+2);			
		}

	  }

	  return xml;
	}
	
	/**
	 * Recursevely visits all features and groups in the feature tree <br>
	 * rooted in featureRoot, creating the xml string of the tree in SXFM format.
	 * 
	 * @param featureRoot - the root feature from which to build the xml string
	 * @param depth - the current tree level depth 
	 * @return - the xml string representing the tree rooted in featureRoot
	 */
	private String recursiveSXFMTreeBuilder(FeatureNode featureRoot, int depth) {
	  String xml="";
	  for(FeatureNode child : featureRoot.getSubFeatures()){
		for(int i=0; i<depth; ++i) xml+="\t";
		if(child.getMinCardinality()==0) xml+=":o ";
		else xml+=":m ";
		
		xml+= child.getName()+" ("+child.getID()+")"
			 +recursiveSXFMTreeBuilder(child, depth+1);
	  }
	  for(GroupNode group : featureRoot.getSubGroups()){
		for(int i=0; i<depth; ++i) xml+="\t";
		xml+=":g ["+group.getMinCardinality()+"."+group.getMaxCardinality()+"]";
		for(FeatureNode member : group.getSubFeatures()){
			for(int i=0; i<depth+1; ++i) xml+="\t";
			xml+= ": "+member.getName()+" ("+member.getID()+")"
			   +recursiveSXFMTreeBuilder(member, depth+2);			
		}

	  }

	  return xml;
	}
	
	/**
	 * Creates a model from an xml file in SXFM format.
	 * 
	 * @param pathSXFM - the path to SXFM file
	 * @return - the created feature model
	 */
	public static EditorModel createModelFromSXFM(String pathSXFM) {
	  String xml="";
	  String s=null;
	  SAXParser saxParser = null;
	  InputStream stream = null;
	  SAXParserFactory saxFactory = SAXParserFactory.newInstance();
	  ModelXMLHandler xmlHandler = new ModelXMLHandler();
	  StringWrapper featureTree=null;
	  String[] strArr=null, tmpArr=null, newConstraint=null;
	  int negations=0;
	  BufferedReader str1 = null;
//	  String cleanedFeatureTree = null;

	  EditorModel newModel=new EditorModel();

	  try{
		xml="";
		BufferedReader br1 = new BufferedReader(new FileReader(pathSXFM));

		/* ***DEBUG*** */
		if(debug) System.out.println("**READING: "+pathSXFM);
		/* ***DEBUG*** */

		while( (s = br1.readLine()) != null ){			  

		  /* ***DEBUG*** */
		  if(debug) System.out.println("s= "+s);
		  /* ***DEBUG*** */
		  
		  xml+=s+"\n";
		}
		br1.close();
		stream = new ByteArrayInputStream(xml.getBytes());

		/* ***DEBUG*** */
		if(debug) System.out.println("**PARSING: "+pathSXFM);
		/* ***DEBUG*** */

//		FileInputStream fi1 = new FileInputStream(pathSXFM);

		saxParser = saxFactory.newSAXParser();
		saxParser.parse(stream, xmlHandler);
//		saxParser.parse(fi1, xmlHandler);

		/* ***DEBUG*** */
		if(debug){
			System.out.println("\nResulting Feature Tree from parsing:\n"+xmlHandler.featureTree);

			str1 = new BufferedReader(new StringReader(xmlHandler.featureTree));

			System.out.println("\nPrinting Resulting Feature Tree by lines:\n");

			xml="";

			while( (s = str1.readLine()) != null ){		
			  if(s.length()==0) continue;
				
			  for(int i=0; i<s.length(); ++i)
				if(s.charAt(i)=='(' && s.charAt(i-1)!=' ') s=s.substring(0, i)+" "+s.substring(i);

			  xml+=s;

			  System.out.println("Line: "+s);
			}
			str1.close();				
		}
		/* ***DEBUG*** */
		

		featureTree=new StringWrapper(xml);
		
		System.out.println("xml:\n"+xml);

		newModel.recursiveFeatureTreeBuilderFromSXFM(null, 0, featureTree);

	  }catch (Exception e) {
		System.out.println("Error while loading saved model");
		e.printStackTrace();
		throw new RuntimeException("Error while load saved model");
	  }

		/* ***DEBUG*** */
		if(debug){
			System.out.println("\nResulting Constraints from parsing:\n"+xmlHandler.constraints);

			str1 = new BufferedReader(new StringReader(xmlHandler.constraints));

			System.out.println("\nPrinting Resulting Constraints by lines:\n");
			try {
				while( (s = str1.readLine()) != null ){			  				
				  System.out.println("Line: "+s);
				}
				str1.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
				
		}
		/* ***DEBUG*** */

		 
	  //loading constraints
	  if(xmlHandler.constraints!=null){
		strArr=xmlHandler.constraints.split("\n");
		
		for(String constr: strArr){
		  if(constr.length()==0) continue;
		  
		  for(int i=0; i<constr.length(); ++i)
			if(constr.charAt(i)==':' && constr.charAt(i+1)!=' ') constr=constr.substring(0, i+1)+" "+constr.substring(i+1);

		  negations=0;
		  tmpArr=constr.split(" ");		
		  
		  for(int u=0; u<tmpArr.length; ++u) System.out.println("tmpArr["+u+"]="+tmpArr[u]);

		  
		  newConstraint=new String[3];
		  newConstraint[0]=tmpArr[0].substring(0, tmpArr[0].length()-1);
		  if(tmpArr[1].charAt(0)=='~'){ ++negations; newConstraint[1]=tmpArr[1].substring(1);}
		  else newConstraint[1]=tmpArr[1];
		  if(tmpArr[3].charAt(0)=='~'){ ++negations; newConstraint[2]=tmpArr[3].substring(1);}
		  else newConstraint[2]=tmpArr[3];

		  /*new stuff*/
		  if(negations==2) newConstraint[0]=excludesConstraintNamePrefix+newConstraint[0];
		  else newConstraint[0]=includesConstraintNamePrefix+newConstraint[0];
		  /*new stuff*/

		  newModel.constraints.add(newConstraint);
		}
	  }

	  newModel.printModel();
	  return newModel;
	}
	
	/**
	 * Loads a saved feature model from a list of files, each describing a feature tree.
	 * 
	 * @param featureModelDataPaths - the list of files describing the feature trees
	 * @return - the saved feature model
	 */
	public static EditorModel loadSavedModel(ArrayList<String> featureModelDataPaths) {
	  String xml="";
	  String s=null;
	  SAXParser saxParser = null;
	  InputStream stream = null;
	  SAXParserFactory saxFactory = SAXParserFactory.newInstance();
	  ModelXMLHandler xmlHandler = new ModelXMLHandler();
	  StringWrapper featureTree=null;
	  String[] strArr=null, tmpArr=null, newConstraint=null;
	  int negations=0;
	  
	  EditorModel newModel=new EditorModel();
	  
	  if(debug) for(int i=0; i<featureModelDataPaths.size(); ++i)
		System.out.println("featureModelDataPaths["+i+"]: "+featureModelDataPaths.get(i));	  
	  if(debug) System.out.println("EditorModel: ***PARSING ALL XML MODEL FILES***");

	  //building feature trees
	  for(int i=0; i< featureModelDataPaths.size(); ++i){
	    try{
	      xml="";
		  BufferedReader br1 = new BufferedReader(new FileReader(featureModelDataPaths.get(i)));
		  
		  /* ***DEBUG*** */
		  if(debug) System.out.println("**READING: "+featureModelDataPaths.get(i));
		  /* ***DEBUG*** */
		  
		  while( (s = br1.readLine()) != null ){			  

			/* ***DEBUG*** */
			if(debug) System.out.println("s= "+s);
			/* ***DEBUG*** */

			xml+=s+"\n";
		  }
		  br1.close();
		  stream = new ByteArrayInputStream(xml.getBytes());
		  
		  /* ***DEBUG*** */
		  if(debug) System.out.println("**PARSING: "+featureModelDataPaths.get(i));
		  /* ***DEBUG*** */

		  saxParser = saxFactory.newSAXParser();
		  saxParser.parse(stream, xmlHandler);

		  /* ***DEBUG*** */
		  /*if(debug) */System.out.println("\nResulting Feature Tree from parsing:\n"+xmlHandler.featureTree);
		  /* ***DEBUG*** */

		  featureTree=new StringWrapper(xmlHandler.featureTree);
		  newModel.recursiveFeatureTreeBuilder(null, 0, featureTree);
		  xmlHandler.featureTree="";
		  
	    }catch (Exception e) {
	      System.out.println("Error while loading saved model");
	      e.printStackTrace();
	      throw new RuntimeException("Error while load saved model");
	    }
	  }

	  //loading constraints
	  if(xmlHandler.constraints!=null){
		strArr=xmlHandler.constraints.split("\n");
	  
		for(String constr: strArr){
		  negations=0;
		  tmpArr=constr.split(" ");		
		  newConstraint=new String[3];
		  newConstraint[0]=tmpArr[0].substring(0, tmpArr[0].length()-1);
		  if(tmpArr[1].charAt(0)=='~'){ ++negations; newConstraint[1]=tmpArr[1].substring(1);}
		  else newConstraint[1]=tmpArr[1];
		  if(tmpArr[3].charAt(0)=='~'){ ++negations; newConstraint[2]=tmpArr[3].substring(1);}
		  else newConstraint[2]=tmpArr[3];
		  
		  /*new stuff*/
		  if(negations==2) newConstraint[0]=excludesConstraintNamePrefix+newConstraint[0];
		  else newConstraint[0]=includesConstraintNamePrefix+newConstraint[0];
		  /*new stuff*/

		  newModel.constraints.add(newConstraint);
		}
	  }
	  
	  newModel.printModel();
	  return newModel;
	}

	/**
	 * Loads a saved feature model from a list of files, each describing a feature tree.
	 * 
	 * @param featureModelDataPaths - the list of files describing the feature trees
	 * @return - the saved feature model
	 */
	public static EditorModel loadSavedModel2(FDEXMLHandler xmlHandler) {
	  StringWrapper featureTree=null;
	  String[] strArr=null, tmpArr=null, newConstraint=null;
	  int negations=0;
	  
	  EditorModel newModel=new EditorModel();

	  //building feature trees
	  for(int i=0; i< xmlHandler.featureModels.size(); ++i){
	    try{

		  featureTree=new StringWrapper(xmlHandler.featureModels.get(i)[0]);
		  newModel.recursiveFeatureTreeBuilder(null, 0, featureTree);
		  xmlHandler.featureTree="";
		  
	    }catch (Exception e) {
	      System.out.println("Error while loading saved model");
	      e.printStackTrace();
	      throw new RuntimeException("Error while load saved model");
	    }
	  }

	  //loading constraints
	  if(xmlHandler.featureModels.get(xmlHandler.featureModels.size()-1)[1]!=null &&
		 xmlHandler.featureModels.get(xmlHandler.featureModels.size()-1)[1].length()>0){
		  
		strArr=xmlHandler.featureModels.get(xmlHandler.featureModels.size()-1)[1].split("\n");
	  
		for(String constr: strArr){
		  negations=0;
		  tmpArr=constr.split(" ");		
		  newConstraint=new String[3];
		  newConstraint[0]=tmpArr[0].substring(0, tmpArr[0].length()-1);
		  if(tmpArr[1].charAt(0)=='~'){ ++negations; newConstraint[1]=tmpArr[1].substring(1);}
		  else newConstraint[1]=tmpArr[1];
		  if(tmpArr[3].charAt(0)=='~'){ ++negations; newConstraint[2]=tmpArr[3].substring(1);}
		  else newConstraint[2]=tmpArr[3];
		  
		  /*new stuff*/
		  if(negations==2) newConstraint[0]=excludesConstraintNamePrefix+newConstraint[0];
		  else newConstraint[0]=includesConstraintNamePrefix+newConstraint[0];
		  /*new stuff*/

		  newModel.constraints.add(newConstraint);
		}
	  }
	  
	  newModel.printModel();
	  return newModel;
	}
	
	/**
	 * Recursevely parse the String contained in featureTree and creates the feature model tree.
	 * It is called when the next element is a feature.
	 * 
	 * @param parent - the feature to which childs must be added
	 * @param tabs - number of tabulation characters preceding parent in featureTree
	 * @param featureTree - contains the String representing the tree
	 * 
	 * @return - the number of tags preceding the first element of another subtree
	 */
	private int recursiveFeatureTreeBuilder(FeatureNode parent, int tabs, StringWrapper featureTree) {
	  int nextTabs=0;
	  FeatureNode lastFeatureAdded=null;
	  int i=0;
	  
	  //building current feature element
	  lastFeatureAdded=addChildToParent(parent, tabs, featureTree);
	  
	  //if featureTree has been consumed, return
	  while (featureTree.string.length()>0){
		nextTabs=0; i=0;
		
		//counting the tabulations preceding the next element
		for( ;i<featureTree.string.length(); ++i){
		  if(featureTree.string.charAt(i)=='\t') ++nextTabs;
		  else break;
		}

		//next element is a child of parent
		if(nextTabs==tabs){
		  //next element is a group
		  if(featureTree.string.substring(nextTabs, nextTabs+2).startsWith(":g"))
			nextTabs=recursiveGroupTreeBuilder(parent, tabs, featureTree);
		  //next element is a feature
		  else lastFeatureAdded=addChildToParent(parent, tabs, featureTree);				
		}
		//next element is a child of the last feature added
		else if(nextTabs>tabs){
		  if(featureTree.string.startsWith(":g", nextTabs)) 
			nextTabs=recursiveGroupTreeBuilder(lastFeatureAdded, nextTabs, featureTree);
		  else nextTabs=recursiveFeatureTreeBuilder(lastFeatureAdded, nextTabs, featureTree);
		}
		//next element belongs to another sub-tree
		else return nextTabs;		  
	  }	  
	  return 0;
	}
	
	/**
	 * Recursevely parse the String contained in featureTree and creates the feature model tree.<br>
	 * It is called when the next element is a group.
	 * 
	 * @param parent - the feature to which childs must be added
	 * @param tabs - number of tabulation characters preceding parent in featureTree
	 * @param featureTree - contains the String representing the tree
	 * 
	 * @return - the number of tags preceding the first element of another subtree
	 */
	private int recursiveGroupTreeBuilder(FeatureNode parent, int tabs, StringWrapper featureTree) {
	  int nextTabs=0;
	  GroupNode lastGroupAdded=null;//last group added to parent feature
	  FeatureNode lastMemberAdded=null;//last member of added to this group
	  FeatureNode lastChildAdded=null;//last feature added to parent feature
	  int i=0;

	  //building current feature element
	  lastGroupAdded=addGroupToParent(parent, tabs, featureTree);

	  //if featureTree has been consumed, return
	  while (featureTree.string.length()>0){
		nextTabs=0; i=0;

		//counting the tabulations preceding the next element
		for( ;i<featureTree.string.length(); ++i){
		  if(featureTree.string.charAt(i)=='\t') ++nextTabs;
		  else break;
		}

		//next element is a child of parent
		if(nextTabs==tabs){
		  //next element is a group
		  if(featureTree.string.substring(nextTabs, nextTabs+2).startsWith(":g"))
			nextTabs=recursiveGroupTreeBuilder(parent, tabs, featureTree);
		  //next element is a feature
		  else lastChildAdded=addChildToParent(parent, tabs, featureTree);
		}
		else if(nextTabs==tabs+1){
		  //next element is a member of the last group added
		  if (lastChildAdded==null) lastMemberAdded=addMemberToGroup(lastGroupAdded, parent, tabs+1, featureTree);
		  //next element is a sub feature or sub group of the last child added to parent
		  else{
			if(featureTree.string.substring(nextTabs, nextTabs+2).startsWith(":g"))
			  nextTabs=recursiveGroupTreeBuilder(lastChildAdded, nextTabs, featureTree);
			else nextTabs=recursiveFeatureTreeBuilder(lastChildAdded, nextTabs, featureTree);
		  }
		}
		//next element is a child of the last feature added
		else if(nextTabs>tabs+1){
		  if(featureTree.string.startsWith(":g", nextTabs)) 
			  nextTabs=recursiveGroupTreeBuilder(lastMemberAdded, nextTabs, featureTree);
		  else nextTabs=recursiveFeatureTreeBuilder(lastMemberAdded, nextTabs, featureTree);
		}
		//next element belongs to another sub-tree
		else return nextTabs;		  
	  }	  
	  return 0;
	}
	
	/**
	 * Retrieves the current element data from the String contained in featureTree and <br>
	 *  creates the corresponding FeatureNode, adding it to the parent feature,<br>
	 *  or to the list unrootedFeatures if parent is null.<br>
	 * Each call to this method will remove from featureTree the prefix String representing the current element.
	 * 
	 * @param parent - the parent feature
	 * @param tabs - number of tabulation characters preceding the element to be created in featureTree
	 * @param featureTree - contains the String representing the tree
	 * 
	 * @return - the new FeatureNode created
	 */
	private FeatureNode addChildToParent(FeatureNode parent, int tabs, StringWrapper featureTree) {
	  String element=null;
	  String[] elementData=null;
	  String featureName="";
	  String featureID=null;
	  FeatureNode newFeature=null;
	  int k=0, h=0, i=0;
	  FeatureTypes type=null;
	  int minCard=0, maxCard=0;
	  
	  //splitting element prefix from the rest of featureTree String
	  i=tabs;
	  while(i<featureTree.string.length() && featureTree.string.charAt(i)!='\t') ++i;

	  element=featureTree.string.substring(tabs, i);
	  
	  //cutting off current element from featureTree
	  featureTree.string=featureTree.string.substring(i);
		  
	  System.out.println("The element String is: "+element+"\nfeatureTree: "+featureTree.string+"\nTabs: "+tabs);
	  elementData=element.split(" ");
	  for(int l=0; l<elementData.length; ++l) System.out.println("elementData["+l+"]: "+elementData[l]);
	  
	  //getting the name of the feature
	  featureName+=elementData[1];
	  for(int l=2; l<elementData.length-2; ++l) featureName+=" "+elementData[l];
	  
	  //getting ID of the feature
	  featureID=elementData[elementData.length-2].substring(1, elementData[elementData.length-2].length()-1);
	  
	  //getting cardinalities of the feature
	  for(k=1; k<elementData[elementData.length-1].length(); ++k) if(elementData[elementData.length-1].charAt(k)=='.') break;
	  minCard=Integer.valueOf(elementData[elementData.length-1].substring(1, k));
	  for(h=k+1; h<elementData[elementData.length-1].length(); ++h) if(elementData[elementData.length-1].charAt(h)==']') break;
	  maxCard=Integer.valueOf(elementData[elementData.length-1].substring(k+1, h));

	  if(minCard>0) type=FeatureTypes.COMMONALITY;
	  else type=FeatureTypes.VARIABILITY;

	  //adding new feature to the model
	  newFeature=new FeatureNode(type, featureName, featureID, minCard, maxCard);
	  System.out.println("***Adding child '"+newFeature.getID()+"' to parent '"
	    +(parent==null ? "null":parent.getID())+"'");
	  if (parent!=null){
		parent.getSubFeatures().add(newFeature);
		newFeature.setParent(parent);
	  }
	  else newFeature.setParent(null);
	  featuresList.put(featureID, newFeature);
//	  unrootedFeatures.put(featureName, newFeature);
	  
	  return newFeature;
	}

	/**
	 * Retrieves the current element data from the String contained in featureTree and <br>
	 *  creates the corresponding GroupNode, adding it to the parent feature.
	 * Each call to this method will remove from featureTree the prefix String representing the current element.
	 * 
	 * @param parent - the parent feature
	 * @param tabs - number of tabulation characters preceding the element to be created in featureTree
	 * @param featureTree - contains the String representing the tree
	 * 
	 * @return - the new GroupNode created
	 */
	private GroupNode addGroupToParent(FeatureNode parent, int tabs, StringWrapper featureTree) {
	  String element=null;
	  String[] elementData=null;
	  GroupNode newGroup=null;
	  String groupName="";
	  int k=0, h=0, i=0;
	  int minCard=0, maxCard=0;
		  
	  if(parent==null) throw new RuntimeException("A group cannot be added to a null parent");
	  //splitting element prefix from the rest of featureTree String
	  i=tabs;
	  while(i<featureTree.string.length() && featureTree.string.charAt(i)!='\t') ++i;

	  element=featureTree.string.substring(tabs, i);
		  
	  //cutting off current element from featureTree
	  featureTree.string=featureTree.string.substring(i);
			  
	  System.out.println("The element String is: "+element+"\nfeatureTree: "+featureTree.string+"\nTabs: "+tabs);
	  elementData=element.split(" ");
	  for(int l=0; l<elementData.length; ++l) System.out.println("elementData["+l+"]: "+elementData[l]);
		  
	  //getting cardinalities of the group
	  for(k=1; k<elementData[elementData.length-1].length(); ++k) if(elementData[elementData.length-1].charAt(k)=='.') break;
	  minCard=Integer.valueOf(elementData[elementData.length-1].substring(1, k));
	  for(h=k+1; h<elementData[elementData.length-1].length(); ++h) if(elementData[elementData.length-1].charAt(h)==']') break;
//	  maxCard=Integer.valueOf(elementData[2].substring(k+1, h));
	  if(elementData[elementData.length-1].substring(k+1, h).compareTo("*")==0) maxCard=-1;
	  else maxCard=Integer.valueOf(elementData[elementData.length-1].substring(k+1, h));


	  //getting the name of the group	  
	  groupName=elementData[1];
	  
	  //adding new group to the model
	  newGroup=new GroupNode(groupName, minCard, maxCard);
	  parent.getSubGroups().add(newGroup);
	  groups.put(groupName, newGroup);
	  return newGroup;
	}

	/**
	 * Retrieves the current element data from the String contained in featureTree and <br>
	 *  creates the corresponding FeatureNode, adding it to the parent group.
	 * Each call to this method will remove from featureTree the prefix String representing the current element.
	 * 
	 * @param parentGroup - the parent group
	 * @param groupOwner - the FeatureNode owner of the parent group
	 * @param tabs - number of tabulation characters preceding the element to be created in featureTree
	 * @param featureTree - contains the String representing the tree
	 * 
	 * @return - the new FeatureNode created
	 * 
	 */
	private FeatureNode addMemberToGroup(GroupNode parentGroup, FeatureNode groupOwner, int tabs, StringWrapper featureTree) {
		  String element=null;
		  String[] elementData=null;
		  String featureName="";
		  String featureID=null;
		  FeatureNode newFeature=null;
		  int k=0, h=0, i=0;
		  FeatureTypes type=null;
		  int minCard=0, maxCard=0;
		  
		  if(parentGroup==null) throw new RuntimeException("A feature cannot be added as member to a null group");
		  //splitting element prefix from the rest of featureTree String
		  i=tabs;
		  while(i<featureTree.string.length() && featureTree.string.charAt(i)!='\t') ++i;

		  element=featureTree.string.substring(tabs, i);
		  
		  //cutting off current element from featureTree
		  featureTree.string=featureTree.string.substring(i);
			  
		  System.out.println("The element String is: "+element+"\nfeatureTree: "+featureTree.string+"\nTabs: "+tabs);
		  elementData=element.split(" ");
		  for(int l=0; l<elementData.length; ++l) System.out.println("elementData["+l+"]: "+elementData[l]);
		  
		  //getting the name of the feature
		  featureName+=elementData[1];
		  for(int l=2; l<elementData.length-2; ++l) featureName+=" "+elementData[l];
		  
		  //getting ID of the feature
		  featureID=elementData[elementData.length-2].substring(1, elementData[elementData.length-2].length()-1);
		  
		  //getting cardinalities of the feature
		  for(k=1; k<elementData[elementData.length-1].length(); ++k) if(elementData[elementData.length-1].charAt(k)=='.') break;
		  minCard=Integer.valueOf(elementData[elementData.length-1].substring(1, k));
		  for(h=k+1; h<elementData[elementData.length-1].length(); ++h) if(elementData[elementData.length-1].charAt(h)==']') break;
		  maxCard=Integer.valueOf(elementData[elementData.length-1].substring(k+1, h));

		  if(minCard>0) type=FeatureTypes.COMMONALITY;
		  else type=FeatureTypes.VARIABILITY;

		  //adding new feature to the model
		  newFeature=new FeatureNode(type, featureName, featureID, minCard, maxCard);
		  System.out.println("***Adding member '"+newFeature.getID()+"' to parentGroup '"
		    +(parentGroup==null ? "null":parentGroup.getID())
		    +"' with parent feature '"+(groupOwner==null ? "null": groupOwner.getID())+"'");
		  parentGroup.getSubFeatures().add(newFeature);
		  newFeature.setParent(groupOwner);
		  featuresList.put(featureID, newFeature);
//		  unrootedFeatures.put(featureName, newFeature);
		  
		  return newFeature;
	}

	/**
	 * Recursevely parse the String contained in featureTree and creates the feature model tree.
	 * It is called when the next element is a feature.
	 * 
	 * @param parent - the feature to which childs must be added
	 * @param tabs - number of tabulation characters preceding parent in featureTree
	 * @param featureTree - contains the String representing the tree
	 * 
	 * @return - the number of tags preceding the first element of another subtree
	 */
	private int recursiveFeatureTreeBuilderFromSXFM(FeatureNode parent, int tabs, StringWrapper featureTree){
	  int nextTabs=0;
	  FeatureNode lastFeatureAdded=null;
	  int i=0;
	  
	  //building current feature element
	  lastFeatureAdded=addChildToParentFromSXFM(parent, tabs, featureTree);
	  
	  //if featureTree has been consumed, return
	  while (featureTree.string.length()>0){
		nextTabs=0; i=0;
		
		//counting the tabulations preceding the next element
		for( ;i<featureTree.string.length(); ++i){
		  if(featureTree.string.charAt(i)=='\t') ++nextTabs;
		  else break;
		}

		//next element is a child of parent
		if(nextTabs==tabs){
		  //next element is a group
		  if(featureTree.string.substring(nextTabs, nextTabs+2).startsWith(":g"))
			nextTabs=recursiveGroupTreeBuilderFromSXFM(parent, tabs, featureTree);
		  //next element is a feature
		  else lastFeatureAdded=addChildToParentFromSXFM(parent, tabs, featureTree);				
		}
		//next element is a child of the last feature added
		else if(nextTabs>tabs){
		  if(featureTree.string.startsWith(":g", nextTabs)) 
			nextTabs=recursiveGroupTreeBuilderFromSXFM(lastFeatureAdded, nextTabs, featureTree);
		  else nextTabs=recursiveFeatureTreeBuilderFromSXFM(lastFeatureAdded, nextTabs, featureTree);
		}
		//next element belongs to another sub-tree
		else return nextTabs;		  
	  }	  
	  return 0;
	}
	
	/**
	 * Recursevely parse the String contained in featureTree and creates the feature model tree.<br>
	 * It is called when the next element is a group.
	 * 
	 * @param parent - the feature to which childs must be added
	 * @param tabs - number of tabulation characters preceding parent in featureTree
	 * @param featureTree - contains the String representing the tree
	 * 
	 * @return - the number of tags preceding the first element of another subtree
	 */
	private int recursiveGroupTreeBuilderFromSXFM(FeatureNode parent, int tabs, StringWrapper featureTree){
	  int nextTabs=0;
	  GroupNode lastGroupAdded=null;//last group added to parent feature
	  FeatureNode lastMemberAdded=null;//last member of added to this group
	  FeatureNode lastChildAdded=null;//last feature added to parent feature
	  int i=0;

	  //building current feature element
	  lastGroupAdded=addGroupToParentFromSXFM(parent, tabs, featureTree);

	  //if featureTree has been consumed, return
	  while (featureTree.string.length()>0){
		nextTabs=0; i=0;

		//counting the tabulations preceding the next element
		for( ;i<featureTree.string.length(); ++i){
		  if(featureTree.string.charAt(i)=='\t') ++nextTabs;
		  else break;
		}

		//next element is a child of parent
		if(nextTabs==tabs){
		  //next element is a group
		  if(featureTree.string.substring(nextTabs, nextTabs+2).startsWith(":g"))
			nextTabs=recursiveGroupTreeBuilderFromSXFM(parent, tabs, featureTree);
		  //next element is a feature
		  else lastChildAdded=addChildToParentFromSXFM(parent, tabs, featureTree);
		}
		else if(nextTabs==tabs+1){
		  //next element is a member of the last group added
		  if (lastChildAdded==null) lastMemberAdded=addMemberToGroupFromSXFM(lastGroupAdded, parent, tabs+1, featureTree);
		  //next element is a sub feature or sub group of the last child added to parent
		  else{
			if(featureTree.string.substring(nextTabs, nextTabs+2).startsWith(":g"))
			  nextTabs=recursiveGroupTreeBuilderFromSXFM(lastChildAdded, nextTabs, featureTree);
			else nextTabs=recursiveFeatureTreeBuilderFromSXFM(lastChildAdded, nextTabs, featureTree);
		  }
		}
		//next element is a child of the last feature added
		else if(nextTabs>tabs+1){
		  if(featureTree.string.startsWith(":g", nextTabs)) 
			  nextTabs=recursiveGroupTreeBuilderFromSXFM(lastMemberAdded, nextTabs, featureTree);
		  else nextTabs=recursiveFeatureTreeBuilderFromSXFM(lastMemberAdded, nextTabs, featureTree);
		}
		//next element belongs to another sub-tree
		else return nextTabs;		  
	  }	  
	  return 0;
	}
	
	/**
	 * Retrieves the current element data from the String contained in featureTree and <br>
	 *  creates the corresponding FeatureNode, adding it to the parent feature,<br>
	 *  or to the list unrootedFeatures if parent is null.<br>
	 * Each call to this method will remove from featureTree the prefix String representing the current element.
	 * 
	 * @param parent - the parent feature
	 * @param tabs - number of tabulation characters preceding the element to be created in featureTree
	 * @param featureTree - contains the String representing the tree
	 * 
	 * @return - the new FeatureNode created
	 */
	private FeatureNode addChildToParentFromSXFM(FeatureNode parent, int tabs, StringWrapper featureTree){
	  String element=null;
	  String[] elementData=null;
	  String featureName="";
	  String featureID=null;
	  FeatureNode newFeature=null;
	  int i=0;
	  FeatureTypes type=null;
	  int minCard=0, maxCard=0;
	  
	  //splitting element prefix from the rest of featureTree String
	  i=tabs;
	  while(i<featureTree.string.length() && featureTree.string.charAt(i)!='\t') ++i;

	  element=featureTree.string.substring(tabs, i);
	  
	  //cutting off current element from featureTree
	  featureTree.string=featureTree.string.substring(i);
		  
	  System.out.println("The element String is: "+element+"\nfeatureTree: "+featureTree.string+"\nTabs: "+tabs);
	  elementData=element.split(" ");
	  for(int l=0; l<elementData.length; ++l) System.out.println("elementData["+l+"]: "+elementData[l]);
	  
	  //getting the name of the feature
	  featureName+=elementData[1];
	  for(int l=2; l<elementData.length-1; ++l) featureName+=" "+elementData[l];
	  
	  //getting ID of the feature
	  featureID=elementData[elementData.length-1].substring(1, elementData[elementData.length-1].length()-1);
	  
	  //getting cardinalities of the feature
	  if(elementData[0].compareTo(":r")==0 || elementData[0].compareTo(":m")==0){ minCard=1; maxCard=1;}
	  else{ minCard=0; maxCard=1;}
	  
	  
//	  for(k=1; k<elementData[elementData.length-1].length(); ++k) if(elementData[elementData.length-1].charAt(k)=='.') break;
//	  minCard=Integer.valueOf(elementData[elementData.length-1].substring(1, k));
//	  for(h=k+1; h<elementData[elementData.length-1].length(); ++h) if(elementData[elementData.length-1].charAt(h)==']') break;
//	  maxCard=Integer.valueOf(elementData[elementData.length-1].substring(k+1, h));

	  if(minCard>0) type=FeatureTypes.COMMONALITY;
	  else type=FeatureTypes.VARIABILITY;

	  //adding new feature to the model
	  newFeature=new FeatureNode(type, featureName, featureID, minCard, maxCard);
	  System.out.println("***Adding child '"+newFeature.getID()+"' to parent '"
	    +(parent==null ? "null":parent.getID())+"'");
	  if (parent!=null){
		parent.getSubFeatures().add(newFeature);
		newFeature.setParent(parent);
	  }
	  featuresList.put(featureID, newFeature);
//	  unrootedFeatures.put(featureName, newFeature);
	  
	  return newFeature;
	}

	/**
	 * Retrieves the current element data from the String contained in featureTree and <br>
	 *  creates the corresponding GroupNode, adding it to the parent feature.
	 * Each call to this method will remove from featureTree the prefix String representing the current element.
	 * 
	 * @param parent - the parent feature
	 * @param tabs - number of tabulation characters preceding the element to be created in featureTree
	 * @param featureTree - contains the String representing the tree
	 * 
	 * @return - the new GroupNode created
	 */
	private GroupNode addGroupToParentFromSXFM(FeatureNode parent, int tabs, StringWrapper featureTree){
	  String element=null;
	  String[] elementData=null;
	  GroupNode newGroup=null;
	  String groupID="";
	  int k=0, h=0, i=0;
	  int minCard=0, maxCard=0;
		  
	  if(parent==null) throw new RuntimeException("A group cannot be added to a null parent");
	  //splitting element prefix from the rest of featureTree String
	  i=tabs;
	  while(i<featureTree.string.length() && featureTree.string.charAt(i)!='\t') ++i;

	  element=featureTree.string.substring(tabs, i);
		  
	  //cutting off current element from featureTree
	  featureTree.string=featureTree.string.substring(i);
			  
	  System.out.println("The element String is: "+element+"\nfeatureTree: "+featureTree.string+"\nTabs: "+tabs);
	  elementData=element.split(" ");
	  for(int l=0; l<elementData.length; ++l) System.out.println("elementData["+l+"]: "+elementData[l]);
		  
	  //getting cardinalities of the group
	  for(k=1; k<elementData[elementData.length-1].length(); ++k) if(elementData[elementData.length-1].charAt(k)==',') break;
	  minCard=Integer.valueOf(elementData[elementData.length-1].substring(1, k));
	  for(h=k+1; h<elementData[elementData.length-1].length(); ++h) if(elementData[elementData.length-1].charAt(h)==']') break;
	  if(elementData[elementData.length-1].substring(k+1, h).compareTo("*")==0) maxCard=-1;
	  else maxCard=Integer.valueOf(elementData[elementData.length-1].substring(k+1, h));

	  //getting the ID of the group	  
	  groupID=elementData[elementData.length-2].substring(1, elementData[elementData.length-2].length()-1);
	  
	  //adding new group to the model
	  newGroup=new GroupNode(groupID, minCard, maxCard);
	  parent.getSubGroups().add(newGroup);
	  groups.put(groupID, newGroup);
	  return newGroup;
	}

	/**
	 * Retrieves the current element data from the String contained in featureTree and <br>
	 *  creates the corresponding FeatureNode, adding it to the parent group.
	 * Each call to this method will remove from featureTree the prefix String representing the current element.
	 * 
	 * @param parentGroup - the parent group
	 * @param groupOwner - the FeatureNode owner of the parent group
	 * @param tabs - number of tabulation characters preceding the element to be created in featureTree
	 * @param featureTree - contains the String representing the tree
	 * 
	 * @return - the new FeatureNode created
	 * 
	 */
	private FeatureNode addMemberToGroupFromSXFM(GroupNode parentGroup, FeatureNode groupOwner, int tabs, StringWrapper featureTree){
		  String element=null;
		  String[] elementData=null;
		  String featureName="";
		  String featureID=null;
		  FeatureNode newFeature=null;
		  int i=0;
		  FeatureTypes type=null;
		  int minCard=0, maxCard=0;
		  
		  if(parentGroup==null) throw new RuntimeException("A feature cannot be added as member to a null group");
		  //splitting element prefix from the rest of featureTree String
		  i=tabs;
		  while(i<featureTree.string.length() && featureTree.string.charAt(i)!='\t') ++i;

		  element=featureTree.string.substring(tabs, i);
		  
		  //cutting off current element from featureTree
		  featureTree.string=featureTree.string.substring(i);
			  
		  System.out.println("The element String is: "+element+"\nfeatureTree: "+featureTree.string+"\nTabs: "+tabs);
		  elementData=element.split(" ");
		  for(int l=0; l<elementData.length; ++l) System.out.println("elementData["+l+"]: "+elementData[l]);
		  
		  //getting the name of the feature
		  featureName+=elementData[1];
		  for(int l=2; l<elementData.length-1; ++l) featureName+=" "+elementData[l];
		  
		  //getting ID of the feature
		  featureID=elementData[elementData.length-1].substring(1, elementData[elementData.length-1].length()-1);
		  
		  //getting cardinalities of the feature
//		  for(k=1; k<elementData[elementData.length-1].length(); ++k) if(elementData[elementData.length-1].charAt(k)=='.') break;
//		  minCard=Integer.valueOf(elementData[elementData.length-1].substring(1, k));
//		  for(h=k+1; h<elementData[elementData.length-1].length(); ++h) if(elementData[elementData.length-1].charAt(h)==']') break;
//		  maxCard=Integer.valueOf(elementData[elementData.length-1].substring(k+1, h));

		  minCard=1; maxCard=1;

		  if(minCard>0) type=FeatureTypes.COMMONALITY;
		  else type=FeatureTypes.VARIABILITY;

		  //adding new feature to the model
		  newFeature=new FeatureNode(type, featureName, featureID, minCard, maxCard);
		  System.out.println("***Adding member '"+newFeature.getID()+"' to parentGroup '"
		    +(parentGroup==null ? "null":parentGroup.getID())
		    +"' with parent feature '"+(groupOwner==null ? "null": groupOwner.getID())+"'");
		  parentGroup.getSubFeatures().add(newFeature);
		  newFeature.setParent(groupOwner);
		  featuresList.put(featureID, newFeature);
//		  unrootedFeatures.put(featureName, newFeature);
		  
		  return newFeature;
	}	

	/**
	 * Print the feature model indenting the lower levels. <br>
	 * Prints a tree for each feature without parent in the model.
	 * 
	 */
	public void printModel() {
      System.out.println("\n\nPRINTING TREES");
      for(Map.Entry<String,FeatureNode> feature : getUnrootedFeatures().entrySet()){
    	if(feature.getValue().getParent()==null) treePrint(feature.getValue(), "*R*");
      }		
      System.out.println("\n\nPRINTING CONSTRAINTS");
      for(String[] strArr : constraints) System.out.println(strArr[0]+": "+strArr[1]+" - "+strArr[2]);      
      System.out.println("\n\nLISTING ALL FEATURES");
      for(Map.Entry<String,FeatureNode> feature : getUnrootedFeatures().entrySet()){
        System.out.println( "\n\nID: "+feature.getValue().getID()+
        					"\nName: "+feature.getValue().getName()+
        					"\nCardinality: "+feature.getValue().getCardinality()+
        					"\nParent: "+feature.getValue().getParent());
      }

	}
	
	/**
	 * Recursively print the feature tree rooted in feature, indenting the lower levels.
	 * 
	 * @param indent - String printed before the name of root element, for the lower leves <br>
	 * it will be printed a number of times equals to 1+depth.
	 */
	private void treePrint(FeatureNode feature, String indent) {
		System.out.println(indent+feature.getName()+"("+feature.getID()+")");
		for(FeatureNode child : feature.getSubFeatures()){
		  if(child.getCardinality().x>0) treePrint(child, indent+"MAN>");
		  else treePrint(child, indent+"OPT>");
		}
		for(GroupNode group : feature.getSubGroups()) 
		  for(FeatureNode member : group.getSubFeatures()){
			treePrint(member, indent+group.getDecompositionType()+"|"); 
		  }
	}	
}
