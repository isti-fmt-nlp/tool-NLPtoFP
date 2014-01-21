package main;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Each instance of this class represents a feature. 
 * @author natan
 *
 */
public class FeatureNode {
	/** FeatureTypes describes the possible types for a feature*/
	public static enum FeatureTypes{COMMONALITY, VARIABILITY};
	
	/** The feature type*/
	private FeatureTypes type=FeatureTypes.COMMONALITY;
	/** The feature name*/
	private String name="";
	/** The feature minimum cardinality*/
	private int minCardinality=1;
	/** The feature maximum cardinality*/
	private int maxCardinality=1;
	/** The list of features directly linked to this feature*/
	private ArrayList<FeatureNode> subFeatures= new ArrayList<FeatureNode>();
	/** The list of feature groups linked to this feature*/
	private ArrayList<GroupNode> subGroups= new ArrayList<GroupNode>();
	/** Tells if this feature is a top-level feature(false) or it is a sub-feature or a member of a group(true)*/
	private boolean hasParent = false;
	
	/**
	 * Creates a new default FeatureNode. <br>
	 * The Default is a mandatory nameless commonality without sub-features or sub-groups.
	 * 
	 */
	public FeatureNode(){}
	
	/**
	 * Creates a new FeatureNode, based on the parameters. 
	 * 
	 * @param type - the type of the feature, COMMONALITY or VARIABILITY
	 * @param name - the name of the feature
	 * @param minCardinality - feature's minimum cardinality
	 * @param maxCardinality - feature's maximum cardinality
	 */
	public FeatureNode(FeatureTypes type, String name, int minCardinality, int maxCardinality){
		this.type=type;
		this.name=name;
		this.minCardinality=minCardinality;
		this.maxCardinality=maxCardinality;
	}
	

	/**
	 * Returns the cardinality of this feature in the form of a Point object, <br>
	 * where the x coordinate represents the minimum cardinality and the y coordinate represents the maximum cardinality.
	 * 
	 * @return a Point object representing the cardinality of this feature.
	 */
	public Point getCardinality(){
		return new Point(minCardinality, maxCardinality);
	}
	
	/**
	 * Changes the cardinality of this feature with the one given in the parameters.
	 * 
	 * @param min - feature's new minimum cardinality
	 * @param max - feature's new maximum cardinality
	 */
	public void setCardinality(int min, int max){
		this.minCardinality=min;
		this.maxCardinality=max;
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
	 * Sets the name of this feature node.
	 * 
	 * @param name - a String containing the name.
	 */
	public void setName(String name){
		this.name=name;
	}
	
	/**
	 * Returns the sub features of this features.
	 * 
	 * @return - an ArrayList\<FeatureNode\> object containing the sub features of this feature
	 */
	public ArrayList<FeatureNode> getSubFeatures(){
		return subFeatures;
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
	 * Tells whether or not this feature is a sub-feature or member of a group owned by another feature.
	 * 
	 * @return true if this feature is a sub-feature or member of a group owned by another feature, false otherwisse
	 */
	public boolean hasParent(){
		return hasParent;
	}
	
	/**
	 * Set the hasParent field, that tells whether or not this feature is a sub-feature or member of a group owned by another feature.
	 * 
	 * @param hasParent - false value means that this feature has no parent feature, true means the opposite
	 */
	public void setHasParent(boolean hasParent){
		this.hasParent=hasParent;
	}
}
