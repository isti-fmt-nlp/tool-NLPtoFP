package view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import main.FeatureNode;
import main.FeatureNode.FeatureTypes;
import main.GroupNode;

/**
 * INVARIANTS: 
 * - a groupNode exists only when its size() is > 0
 * - names of FeaturePanel objects correspond to name of corresponding FeatureNode objects
 * - names of GroupPanel objects correspond to name of corresponding GroupNode objects
 */
public class EditorModel extends Observable{

	
	private static boolean debug = false;

	/** root feature of the feature model*/
	private FeatureNode featureRoot = null;
	
	/** feature nodes that are descendant of root */
	private HashMap<String, FeatureNode> rootLinkedFeatures = new HashMap<String, FeatureNode>();

	/** feature nodes that are not descendant of root */
	private HashMap<String, FeatureNode> unrootedFeatures = new HashMap<String, FeatureNode>();

	/** groups in the diagram */
	private HashMap<String, GroupNode> groups= new HashMap<String, GroupNode>();

	/**
	 * Creates an editor model and adds to it the features contained in commonalities and variabilities lists.
	 * 
	 * @param commonalitiesSelected - list of commonality names
	 * @param variabilitiesSelected - list of variabilities names
	 */
	public EditorModel(ArrayList<String> commonalitiesSelected,
					   ArrayList<String> variabilitiesSelected) {
	  for(String name : commonalitiesSelected) addUnrootedFeature(name, FeatureTypes.COMMONALITY);
	  for(String name : variabilitiesSelected) addUnrootedFeature(name, FeatureTypes.VARIABILITY);
	}

	/**
	 * Adds a newly created Commonality feature named name to the unrooted features.
	 * 
	 * @param name - String containing the name of the new features
	 */
	public void addUnrootedCommonality(String name){
		System.out.println("Creating new commonality: "+name);
		addUnrootedFeature(name, FeatureTypes.COMMONALITY);
	}

	/**
	 * Adds a newly created Variability feature named name to the unrooted features.
	 * 
	 * @param name - String containing the name of the new features
	 */
	public void addUnrootedVariabilities(String name){
		System.out.println("Creating new variability: "+name);
		addUnrootedFeature(name, FeatureTypes.VARIABILITY);
	}

	/**
	 * Adds a newly created feature named name of the specified type to the unrooted features.
	 * 
	 * @param name - String containing the name of the new features
	 * @param type - type of the feature to create, a value from the FeatureTypes enum type
	 */
	private void addUnrootedFeature(String name, FeatureTypes type) {
		FeatureNode newFeature = new FeatureNode(type, name, 1, 1);
		unrootedFeatures.put(name, newFeature);
		setChanged();
		notifyObservers("New Feature Correctly Added");
	}

	/**
	 * Returns the HashMap containing all features that are not descendant of root 
	 * 
	 * @return - unrootedFeatures, an object of type: HashMap<String, FeatureNode>
	 */
	public HashMap<String, FeatureNode> getUnrootedFeatures(){
		return unrootedFeatures;
	}
	
	/**
	 * Group the feature groupMember with the group groupName of the feature groupOwner.<br>
	 * If the two features are already connected in any way, this method does nothing.<br>
	 * This method is called when an existing ending anchor of a group is dropped, <br>
	 * to merge connectors see mergeConnectorWithGroup(String, String, String).
	 * 
	 * @param groupOwner - the feature owner of the group
	 * @param groupMember - the feature to be grouped
	 * @param groupName - the name of the group
	 * 
	 * @see {@link EditorModel#mergeConnectorWithGroup(String, String, String)}
	 */
	public void addFeatureToGroup(String groupOwner, String groupMember, String groupName){
	  FeatureNode parent= null;
	  GroupNode group = searchGroup(groupName);
	  FeatureNode sub= searchFeature(groupMember);
	  boolean groupFound= (group==null)? false : true;
	  
	  if(groupOwner!=null) parent=searchFeature(groupOwner);
		  
	  //if the candidate member is not found, operation is aborted
	  if (sub==null){ 
//		  if (sub==null || sub.getParent()!=null){ 
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
		group = new GroupNode(groupName, 1, 1, new ArrayList<FeatureNode>());
		groups.put(groupName, group);
		//if group icon was over a feature in the diagram, the new GroupNode is added to the corresponding FeatureNode
		if(parent!=null) parent.getSubGroups().add(group);
	  }
	  
	  //adding the feature to the group
	  group.getMembers().add(sub);
	  if ( parent!=null) sub.setParent(parent);
	  setChanged();
	  notifyObservers("Grouped a Feature");		
	}
	
	/**
	 * Merges a connector with a group.<br>
	 * This method is called when a starting anchor must be merged with a group, <br>
	 * to drop a group ending anchor on a feature see addFeatureToGroup(String, String, String).
	 * 
	 * @param groupOwner - the feature owner of the group, or null if the group is not owned by a feature
	 * @param groupMember - the feature to be grouped, or null if only an anchor must be grouped
	 * @param groupName - the name of the group	 
	 * 
	 * @see {@link EditorModel#addFeatureToGroup(String, String, String)}
	 */
	public void mergeConnectorWithGroup(String groupOwner, String groupMember, String groupName){
	  FeatureNode parent = null;
	  GroupNode group = searchGroup(groupName);
	  FeatureNode sub = null;
	  boolean groupFound = false;
	  
	  if(groupOwner!=null) parent = searchFeature(groupOwner);
	  if(groupMember!=null) sub = searchFeature(groupMember);
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
		group = new GroupNode(groupName, 1, 1, new ArrayList<FeatureNode>());
		groups.put(groupName, group);
		//if group icon was over a feature in the diagram, the new GroupNode is added to the corresponding FeatureNode
		if(parent!=null) parent.getSubGroups().add(group);
	  }
	  
	  //adding the feature to the group
	  group.getMembers().add(sub);
	  if(parent!=null) sub.setParent(parent);
	  setChanged();
	  notifyObservers("Merged a Connector");
	  		
	}
	
	/**
	 * Adds a mandatory link between parent feature and the sub feature.<br>
	 * If the two features are already connected in any way, this method does nothing.
	 * 
	 * @param parentFeature - the parent feature to link
	 * @param subFeature - the sub-feature to link
	 */
	public void addMandatoryLink(String parentFeature, String subFeature){
	  FeatureNode parent= searchFeature(parentFeature);
	  FeatureNode sub= searchFeature(subFeature);
//	  boolean isDesc=false;
//	  boolean isDescRev=false;
//	  if ( parent!=null && sub!=null){
//		  isDesc=isDescendantOf(parent, sub);
//		  isDescRev=isDescendantOf(sub, parent);
//		  System.out.println("parent="+parent.getName()+"\tsub="+sub.getName()
//				 +"\nisDesc="+isDesc+"\tisDescRev="+isDescRev);
//	  }
	  
	  if ( parent!=null && sub!=null && parent!=sub && sub.getParent()==null  
		   && !isDescendantOf(parent, sub) && !isDescendantOf(sub, parent)){
		parent.getSubFeatures().add(sub);
		sub.setParent(parent);
		setChanged();
		notifyObservers("Two Features Directly Linked");			
	  }
	  else{
		setChanged();
		notifyObservers("Two Features Not Linked");			
	  }

	}
	
	/**
	 * Adds a group to a feature.<br>
	 * If the feature and the members of the group and are already connected in any way, this method does nothing.
	 * 
	 * @param feature - the feature that will take the group
	 * @param group - the group to be added
	 */
	public void addGroupToFeature(String feature, String group){
	  FeatureNode featureNode = searchFeature(feature);
	  GroupNode groupNode = searchGroup(group);
	  System.out.println("addGroupToFeature(): "+feature+" = "+featureNode+"  "+group+" = "+groupNode);
	  
	  if ( featureNode==null){
		setChanged();
		notifyObservers("Group Not Added To Feature");			
		return;
	  };
	  //if the operation would induce a cycle in the diagram, it is aborted
	  if (groupNode!=null){
		for(FeatureNode member : groupNode.getMembers())
		  if (member==featureNode || isDescendantOf(featureNode, member) || isDescendantOf(member, featureNode)){
			setChanged();
			notifyObservers("Group Not Added To Feature");			
			return;
		  };
		
		featureNode.getSubGroups().add(groupNode);
		for(FeatureNode member : groupNode.getMembers()) member.setParent(featureNode);
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
	 * @param feature - the feature owner of the group
	 * @param group - the group to remove
	 */
	public void removeGroupFromFeature(String feature, String group){
	  System.out.println("RemoveGroupFromfeature");
	  FeatureNode featureNode = searchFeature(feature);
	  GroupNode groupNode = searchGroup(group);
	  
	  if ( featureNode!=null && groupNode!=null){
		if (!featureNode.getSubGroups().contains(groupNode) ){
			setChanged();
			notifyObservers("Group Not Removed From Feature");			
			return;
		};
		
		featureNode.getSubGroups().remove(groupNode);
		for(FeatureNode member : groupNode.getMembers()) member.setParent(null);
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
	 * @param parentFeature - the parent feature
	 * @param subFeature - the sub-feature
	 */
	public void removeLink(String parentFeature, String subFeature){
	  FeatureNode parent= searchFeature(parentFeature);
	  FeatureNode sub= searchFeature(subFeature);
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
	 * @param parentFeature - the parent feature
	 * @param subFeature - the sub-feature
	 */
	public void removeFeatureFromGroup(String groupOwner, String groupMember, String groupName){		
	  FeatureNode parent = null;
	  FeatureNode sub= searchFeature(groupMember);
	  GroupNode group = searchGroup(groupName);
		  
	  if(groupOwner!=null) parent = searchFeature(groupOwner);

	  //if the group or the member was not found, operation is aborted
	  if(sub==null || group==null) {
		setChanged();
		notifyObservers("Direct Link Not Destroyed");	
		return;
	  }
		  
	  //the group was owned by a feature
	  if (parent!=null){
		for(GroupNode featureGroup : parent.getSubGroups()){
		  System.out.println("featureGroup.getName()="+featureGroup.getName());
		  if(featureGroup.getName().equals(groupName) )
			if (featureGroup.getMembers().contains(sub)){
			  featureGroup.getMembers().remove(sub);
			  sub.setParent(null);
			  //if the group size is 0, it gets removed form the model
			  if(featureGroup.getMembers().size()==0){
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
		if (group.getMembers().contains(sub)){
		  group.getMembers().remove(sub);
//		  sub.setHasParent(false);
		  //if the group size is 0, it gets removed form the model
		  if(group.getMembers().size()==0) groups.remove(groupName);
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
//	  FeatureNode parentFeature=null;
//	  GroupNode parentGroup=null;	  
	  FeatureNode featurefound=unrootedFeatures.get(name);
	  if (featurefound==null) featurefound=rootLinkedFeatures.get(name);
	  if (featurefound==null){
		setChanged();
		notifyObservers("Feature Not Deleted");
		return;
	  }
	  //removing the feature from the parent, if any
	  if(featurefound.getParent()!=null){//NO, parent is always a FeatureNode, or null....
//		if(featurefound.getParent().getClass().equals(FeatureNode.class)) 
//		  parentFeature=(FeatureNode)featurefound.getParent();
//		if(featurefound.getParent().getClass().equals(GroupNode.class)) 
//		  parentGroup=(GroupNode)featurefound.getParent();
//	  }
		//the parent feature is diretly linked
	    if(((FeatureNode)featurefound.getParent()).getSubFeatures().contains(featurefound)){
		  System.out.println("The feature: "+featurefound+" has a direct parent feature: "+
				  ((FeatureNode)featurefound.getParent()).getName());
		  ((FeatureNode)featurefound.getParent()).getSubFeatures().remove(featurefound);
	    }
		//the parent feature is linked through a group
	    else{
	      for(GroupNode group : ((FeatureNode)featurefound.getParent()).getSubGroups())
	    	if(group.getMembers().contains(featurefound)){
	  		  System.out.println("The feature: "+featurefound.getName()+" has a parent feature: "+
					  ((FeatureNode)featurefound.getParent()).getName()+" through the group: "+group.getName());
	    	  group.getMembers().remove(featurefound);
	    	}	      
	    }
	  }

	  //setting parent attribute of this feature childs to null
	  for(GroupNode group : featurefound.getSubGroups())
	    for(FeatureNode member : group.getMembers()) member.setParent(null);
	  for(FeatureNode child : featurefound.getSubFeatures()) child.setParent(null);

	  //removing the feature from his feature list
	  if(unrootedFeatures.containsKey(name)) unrootedFeatures.remove(name);
	  if(rootLinkedFeatures.containsKey(name)) rootLinkedFeatures.remove(name);
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
	  if (group!=null) for(FeatureNode member : group.getMembers()) member.setParent(null);
	  setChanged();
	  notifyObservers("Group Deleted");		  
	}
	
	/**
	 * Search for a feature node named name in the model.
	 * 
	 * @param name - the name of the feature to search for
	 * @return - a FeatureNode object representing the feature found, or null if it's not present in the model
	 */
	private FeatureNode searchFeature(String name){
	  FeatureNode featurefound=rootLinkedFeatures.get(name);
	  if(featurefound==null) featurefound=unrootedFeatures.get(name);
	  
	  /* ***DEBUG*** */
	  if(debug && featurefound!=null) System.out.println("searchFeature("+name+"): "+featurefound.getName());
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
	  if(debug && groupfound!=null) System.out.println("searchGroup("+name+"): "+groupfound.getName());
	  /* ***DEBUG*** */

	  return groupfound;
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
	  System.out.println("ancestor: "+ancestor.getName()+"\tdescendant: "+descendant.getName());
	  for(FeatureNode child : ancestor.getSubFeatures()){
		System.out.println("child.getName()="+child.getName());
		if (child.getName().equals(descendant.getName())
			|| isDescendantOf(child, descendant)) return true;
	  }
	  for(GroupNode group : ancestor.getSubGroups()){
		System.out.println("group .getName()="+group.getName());
		for(FeatureNode child : group.getMembers() ){
		  System.out.println("child.getName()="+child.getName());
		  if (child.getName().equals(descendant.getName())
			|| isDescendantOf(child, descendant)) return true;
		}
	  }
	  return false;	
	}

	public void saveModel(String pathProject, String s) {
		
	}
}
