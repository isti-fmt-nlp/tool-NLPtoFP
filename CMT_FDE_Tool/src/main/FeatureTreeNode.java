package main;

import java.awt.Point;
import java.util.ArrayList;

/**
 * This class represents a node inside a EditorModel object.
 * 
 * @author Manuel Musetti
 */
public class FeatureTreeNode {

	public static final int CARD_UNDEF=-1;
	
	/** The node unique identifier*/
	protected String id="";
	/** The node minimum cardinality*/
	protected int minCardinality=1;
	/** The node maximum cardinality*/
	protected int maxCardinality=1;
	/** The list of subfeatures of this node*/
	protected ArrayList<FeatureNode> subFeatures= new ArrayList<FeatureNode>();
	
	
	/**
	 * Returns the unique identifier of this node.
	 * 
	 * @return - a String representing the name of this group
	 */
	public String getID(){
		return id;
	}
	
	/**
	 * Changes the cardinality of this node with the one given in the parameters.
	 * 
	 * @param min - new minimum cardinality
	 * @param max - new maximum cardinality
	 */
	public void setCardinality(int min, int max){
		this.minCardinality=min;
		this.maxCardinality=max;
	}

	/**
	 * Returns the cardinality of this node in the form of a Point object, <br>
	 * where the x coordinate represents the minimum cardinality and the y coordinate represents the maximum cardinality.
	 * 
	 * @return - a Point object representing the cardinality of this node.
	 */
	public Point getCardinality(){
		return new Point(minCardinality, maxCardinality);
	}

	/**
	 * Returns the minimum cardinality of this node.
	 * 
	 * @return - an int value representing the minimum cardinality of this feature
	 */
	public int getMinCardinality(){
		return minCardinality;
	}	

	/**
	 * Returns the maximum cardinality of this node.
	 * 
	 * @return - an int value representing the maximum cardinality of this feature
	 */
	public int getMaxCardinality(){
		return maxCardinality;
	}
	
	/**
	 * Returns the sub features of this node.
	 * 
	 * @return - an ArrayList\<FeatureNode\> object containing the sub features of this node
	 */
	public ArrayList<FeatureNode> getSubFeatures(){
	  return subFeatures;
	}

}
