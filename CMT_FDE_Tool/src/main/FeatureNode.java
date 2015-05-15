package main;

import java.util.ArrayList;

/**
 * Each instance of this class represents a feature. 
 * @author natan
 *
 */
public class FeatureNode extends FeatureTreeNode{
	/** FeatureTypes describes the possible types for a feature*/
	public static enum FeatureTypes{COMMONALITY, VARIABILITY};
	
	/** The feature type*/
	private FeatureTypes type=FeatureTypes.COMMONALITY;
	/** The feature name*/
	private String name="";
	/** The list of feature groups linked to this feature*/
	private ArrayList<GroupNode> subGroups= new ArrayList<GroupNode>();
	/** Parent of this feature. If this feature has no parent, it is null*/
	private Object parent = null;
	
	
	/**
	 * Creates a new default FeatureNode. <br>
	 * The Default is a mandatory, not clonable, nameless commonality.
	 * 
	 */
	public FeatureNode(){}
	
	/**
	 * Creates a new FeatureNode, based on the parameters. 
	 * 
	 * @param type - the type of the feature, COMMONALITY or VARIABILITY
	 * @param name - the name of the feature
	 * @param id - the ID of the feature
	 * @param minCardinality - feature's minimum cardinality
	 * @param maxCardinality - feature's maximum cardinality
	 */
	public FeatureNode(FeatureTypes type, String name, String id, int minCardinality, int maxCardinality){
		this.type=type;
		this.name=name;
		this.id=id;
		this.minCardinality=minCardinality;
		this.maxCardinality=maxCardinality;
	}
	
	/**
	 * Sets the name of this feature node.
	 * 
	 * @param name - a String containing the name.
	 */
	public void setName(String name){
		this.name=name;
	}

	/**
	 * Returns the name of this feature node.
	 * 
	 * @return - a String containing the name.
	 */
	public String getName(){
		return name;
	}

	/**
	 * Returns the type of this feature node.
	 * 
	 * @return - a FeatureTypes value that tells the type of this feature.
	 */
	public FeatureTypes getFeatureType(){
		return type;
	}
	
	/**
	 * Returns the sub groups of this features.
	 * 
	 * @return - an ArrayList\<GroupNode\> object containing the sub groups of this feature
	 */
	public ArrayList<GroupNode> getSubGroups(){
		return subGroups;
	}
	
	/**
	 * Returns the parent of this feature, that can be a FeatureNode or a GroupNode.
	 * 
	 * @return an Object which is the parent of this feature, or null if there is no parent
	 */
	public Object getParent(){
		return parent;
	}
	
	/**
	 * Sets the parent of this feature as an Object. Current actual types supported are FeatureNode and GroupNode.
	 * 
	 * @param parent - an Object which is the parent of this feature, or null to set no parent
	 * @throws java.lang.ClassCastException - if the actual type of parent is not supported.
	 */
	public void setParent(Object parent){
		boolean castSucceded=false;
		try{
		  this.parent=(FeatureNode)parent;			
		  castSucceded=true;
		}catch(ClassCastException e){}
		if(castSucceded) return;
		try{
		  this.parent=(GroupNode)parent;			
		  castSucceded=true;
		}catch(ClassCastException e){}
		if (!castSucceded) throw new ClassCastException(parent.getClass()+" type is not supported as parent");
	}
}
