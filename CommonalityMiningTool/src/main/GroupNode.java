package main;

import java.util.ArrayList;

/**
 * Each instance of this class represents a features group. 
 * @author natan
 *
 */
public class GroupNode extends FeatureTreeNode{
	
	/** GroupTypes describes the possible types for a group decomposition*/
//	/** enumeration used to specify a group type in the model*/
	public static enum GroupTypes { ALT_GROUP, OR_GROUP, N_M_GROUP};
	
	/**
	 * Creates a new GroupNode, based on the parameters. 
	 * 
	 * @param id - the name of this group
	 * @param minCardinality - group minimum cardinality
	 * @param maxCardinality - group maximum cardinality
	 * @param subFeatures - the features which will be member of this group.
	 */
	public GroupNode(String id, int minCardinality, int maxCardinality, ArrayList<FeatureNode> subFeatures){
		this.minCardinality=minCardinality;
		this.maxCardinality=maxCardinality;
		this.subFeatures=subFeatures;
		this.id=id;
	}
	
	/**
	 * Creates a new GroupNode, based on the parameters. 
	 * 
	 * @param id - the name of this group
	 * @param minCardinality - group minimum cardinality
	 * @param maxCardinality - group maximum cardinality
	 */
	public GroupNode(String id, int minCardinality, int maxCardinality){
		this.minCardinality=minCardinality;
		this.maxCardinality=maxCardinality;
		this.id=id;
	}

	/**
	 * Returns the type of this group node decomposition.
	 * 
	 * @return - a GroupTypes value that tells the type of this group decomposition.
	 */
	public GroupTypes getDecompositionType(){
		if(maxCardinality==1) return GroupTypes.ALT_GROUP;
		else return GroupTypes.OR_GROUP;
	}
}
