package main;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Each instance of this class represents a features group. 
 * @author natan
 *
 */
public class GroupNode {
	
	public static final int CARD_UNDEF=-1;
	
	/** The group name*/
	private String name="";
	/** The group minimum cardinality*/
	private int minCardinality=1;
	/** The group maximum cardinality*/
	private int maxCardinality=1;
	/** The list of features that are members of this group*/
	private ArrayList<FeatureNode> members= new ArrayList<FeatureNode>();

	/**
	 * Creates a new default GroupNode. <br>
	 * The Default is a XOR group created without members.
	 * 
	 */
	public GroupNode(){}
	
	/**
	 * Creates a new GroupNode, based on the parameters. 
	 * 
	 * @param name - the name of this group
	 * @param minCardinality - group minimum cardinality
	 * @param maxCardinality - group maximum cardinality
	 * @param subFeatures - the features which will be member of this group.
	 */
	public GroupNode(String name, int minCardinality, int maxCardinality, ArrayList<FeatureNode> members){
		this.minCardinality=minCardinality;
		this.maxCardinality=maxCardinality;
		this.members=members;
		this.name=name;
	}
	
	/**
	 * Creates a new GroupNode, based on the parameters. 
	 * 
	 * @param name - the name of this group
	 * @param minCardinality - group minimum cardinality
	 * @param maxCardinality - group maximum cardinality
	 */
	public GroupNode(String name, int minCardinality, int maxCardinality){
		this.minCardinality=minCardinality;
		this.maxCardinality=maxCardinality;
		this.name=name;
	}

	/**
	 * Returns the name of this group.
	 * 
	 * @return - a String representing the name of this group
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Changes the cardinality of this group with the one given in the parameters.
	 * 
	 * @param min - group new minimum cardinality
	 * @param max - group new maximum cardinality
	 */
	public void setCardinality(int min, int max){
		this.minCardinality=min;
		this.maxCardinality=max;
	}

	/**
	 * Returns the cardinality of this group in the form of a Point object, <br>
	 * where the x coordinate represents the minimum cardinality and the y coordinate represents the maximum cardinality.
	 * 
	 * @return a Point object representing the cardinality of this group.
	 */
	public Point getCardinality(){
		return new Point(minCardinality, maxCardinality);
	}

	/**
	 * Returns the minimum cardinality of this feature .
	 * 
	 * @return - an int value representing the minimum cardinality of this feature
	 */
	public int getMinCardinality(){
		return minCardinality;
	}	

	/**
	 * Returns the maximum cardinality of this feature .
	 * 
	 * @return - an int value representing the maximum cardinality of this feature
	 */
	public int getMaxCardinality(){
		return maxCardinality;
	}
	
	/**
	 * Returns the members of this group.
	 * 
	 * @return - an ArrayList\<FeatureNode\> object containing the members of this group
	 */
	public ArrayList<FeatureNode> getMembers(){
	  return members;
	}
}
