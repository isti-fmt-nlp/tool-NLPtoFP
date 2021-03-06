/**
 * @author Manuel Musetti, Daniele Cicciarella
 */
package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

public class ViewPanelLateral{

	/** JPanel containing the project tree*/
	private JPanel panelTree = new JPanel();
	
	/** DefaultMutableTreeNode containing the tree root*/
	private DefaultMutableTreeNode rootTree = null;
	
	/** the tree model of  the project*/
	private DefaultTreeModel treeModel = null;
	
	/** view for the project tree model*/
	private JTree treeView = null;
	
	/** TreePath containing the selected node*/
	private TreePath selectNode = null;
	
	/** JPopupMenu for the project tree  */
	private JPopupMenu menuTree = null;
	
	/** ArrayList containing info about the analysis of input node elements*/
	private ArrayList <String> analysisLeafTree = new ArrayList <String> ();
	
	private static boolean debug=true;//variabile used to activate debug prints

	/** 
	 * Constructor.
	 * 
	 * @param menuTree - the JPopupMenu to be used with the laterale panel
	 */
	public ViewPanelLateral(JPopupMenu menuTree){
		this.menuTree = menuTree;
		
		panelTree.setLayout(new BorderLayout());
//		panelTree.setBounds(new Rectangle(18,10,300,702));//+50?
		panelTree.setMinimumSize(
		  new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width/10, Toolkit.getDefaultToolkit().getScreenSize().height));
		panelTree.setPreferredSize(panelTree.getMinimumSize());
		panelTree.setLocation(0, 0);
		panelTree.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
	}
	
	/** 
	 * Builds the project tree.
	 * 
	 * @param s - the project name
	 */
	public void createTree(String s){
	    final String tmp = s;
		
		if(s.length() == 0 || s == null)
			return;

		rootTree = new DefaultMutableTreeNode(s);
		rootTree.add(new DefaultMutableTreeNode("Input"));
		rootTree.add(new DefaultMutableTreeNode("Commonality"));
		rootTree.add(new DefaultMutableTreeNode("Variability"));		
		
		treeModel = new DefaultTreeModel(rootTree);
			
		treeView = new JTree(treeModel);
//		treeView.setBounds(5,5,290,690);//-50?
		treeView.setPreferredSize(panelTree.getPreferredSize());
		treeView.setMinimumSize(panelTree.getMinimumSize());
//		treeProject.setBounds(5,5,290,640);//-50?
		treeView.setCellRenderer(new ColorTree(s, rootTree, analysisLeafTree));
		treeView.addMouseListener(getTreeMouseAdapter(tmp));	
		
		panelTree.removeAll();
		panelTree.add(treeView, BorderLayout.CENTER);
	}
	
	/** 
	 * Loads the project tree.
	 * 
	 * @param s - the project name
	 * @param al - ArrayList containing input files names
	 */
	public void loadTree(String s, ArrayList <String> al)
	{
		final String tmp = s;
		
		if(s == null || al == null)
			return;

		rootTree = new DefaultMutableTreeNode(s);
		rootTree.add(new DefaultMutableTreeNode("Input"));
		rootTree.add(new DefaultMutableTreeNode("Commonality"));
		rootTree.add(new DefaultMutableTreeNode("Variability"));
		
		treeModel = new DefaultTreeModel(rootTree);

		treeView = new JTree(treeModel);
		treeView.setBounds(5,5,290,575);
		treeView.setCellRenderer(new ColorTree(s, rootTree, analysisLeafTree));
		treeView.addMouseListener(getTreeMouseAdapter(tmp));
		
		for(int i = 0; i < al.size(); i++){
		  treeModel.insertNodeInto(
			new DefaultMutableTreeNode(al.get(i)), (MutableTreeNode) rootTree.getChildAt(0),
			rootTree.getChildAt(0).getChildCount() );
		  analysisLeafTree.add("NO");
		}	
		panelTree.removeAll();
		panelTree.add(treeView, BorderLayout.CENTER);
	}

	/**
	 * Return a MouseAdapter per the project tree
	 * 
	 * @param tmp - nome del progetto
	 * @return a new MouseAdapter for the project Tree
	 */
	private MouseAdapter getTreeMouseAdapter(final String tmp) {
	  return new MouseAdapter(){			
		@Override
		public void mouseClicked(MouseEvent me){							
		  clickNodeTree(me);
		  if(me.getButton() == 3) showMenuTree(tmp, me);
		}
	  };
	}
	
	/** 
	 * Adds an element to the Input node.
	 * 
	 * @param s - Stringa containing the name of the node to be added
	 * @return - true if the node can be added, false if a node with the same name is already present
	 */
	public boolean addNodeInput(String s){
		for(int i = 0; i < rootTree.getChildAt(0).getChildCount(); i++){
			if(rootTree.getChildAt(0).getChildAt(i).toString().equals(s.toString()))
				return false;
		}
		
		treeModel.insertNodeInto(
			new DefaultMutableTreeNode(s), (MutableTreeNode) rootTree.getChildAt(0), rootTree.getChildAt(0).getChildCount());	
		analysisLeafTree.add("NO");
		return true;
	}
	
	/** 
	 * Deletes the current selected input node.
	 * 
	 * @return i - index of deleted node
	 */
	public int deleteSelectedInputNode(){
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) selectNode.getLastPathComponent();
		
		for(int i = 0; i < rootTree.getChildAt(0).getChildCount(); i++){
			if(rootTree.getChildAt(0).getChildAt(i).toString().equals(dmtn.toString())){
				treeModel.removeNodeFromParent(dmtn);	
				analysisLeafTree.remove(i);
				return i;
			}
		}
		return -1;
	}

	/** 
	 * Deletes the input node specified by index.
	 * 
	 * @return true if deletion was successful, false otherwise 
	 */
	public boolean deleteSpecifiedInputNode(int index){
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) rootTree.getChildAt(0).getChildAt(index);
		if (dmtn==null) return false;
		treeModel.removeNodeFromParent(dmtn);	
		analysisLeafTree.remove(index);
		return true;
	}

	/**
	 * Returns a String[] containing the names of input files in the same order they have in the tree model.
	 * 
	 * @return - String[] containing the names of input files 
	 */
	public String[] getInputFiles(){
	  String[] inputFilesNames= new String[rootTree.getChildAt(0).getChildCount()];
	  for(int i=0; i<rootTree.getChildAt(0).getChildCount(); ++i){
		  inputFilesNames[i]=(String)((DefaultMutableTreeNode)rootTree.getChildAt(0).getChildAt(i)).getUserObject();
	  }
	  return inputFilesNames;
		
	}
	
	/* -= FUNZIONI lettura/scrittura parametri =- */
	
	/** 
	 * Returns an index representing the selected node in the tree, with the following semantic:<br>
	 *   index = -3 node selected is Variabilities<br>
	 *   index = -2 node selected is Commonalities<br>
	 *   index = -1 node selected is non relevant<br>
	 *   index >= 0 index of a selected input file node
	 *   
	 *   @return the index above described
	 */
	public int getAnalysisLeaf(){
		if(selectNode == null || selectNode.getLastPathComponent() == null)	return -1;
		
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) selectNode.getLastPathComponent();
		
		if(rootTree.getChildAt(2).toString().equals(dmtn.toString())){

			/* ***DEBUG****/
            if (debug) System.out.println("\n***Restituisco -3***\n");
            /* ***DEBUG****/

            return -3;
		}
		
		if(rootTree.getChildAt(1).toString().equals(dmtn.toString())){

			/* ***DEBUG****/
            if (debug) System.out.println("\n***Restituisco -2***\n");
            /* ***DEBUG****/

			return -2;
		}	
		
		for(int i = 0; i < rootTree.getChildAt(0).getChildCount(); i++)
			if(rootTree.getChildAt(0).getChildAt(i).toString().equals(dmtn.toString())){

				/* ***DEBUG****/
	            if (debug) System.out.println("\n***Restituisco "+i+"***\n");
	            /* ***DEBUG****/

				return i;				
			}

		return -1;
	}
	
	/* -= FUNZIONI lettura parametri =- */
	
	/** 
	 * Return the JPanel containing the project tree.
	 * 
	 * @return the tree's JPanel
	 */
	public JPanel getPanelTree(){
		return panelTree;
	}
	
	/** 
	 * Return the JTree representing the project tree.
	 * 
	 * @return the project JTree
	 */
	public JTree getTree(){
		return treeView;
	}
	
	/** 
	 * Return the ArrayList containing info about the analysis of input node elements
	 * 
	 * @return analysisLeafTree
	 */
	public ArrayList <String> getAnalysisLeafTree(){
		return analysisLeafTree;
	}
	
	/** 
	 * Sets all elements of Input node ad analyzed.
	 */
	public void setAnalysisLeafTree(){
		for(int i = 0; i < analysisLeafTree.size(); i++)
			analysisLeafTree.set(i, "YES");
	}
	
	/**
	 * Sets the specified elements of Input node ad analyzed.
	 * 
	 * @param al - ArrayList containing the elements to be set as analyzed
	 */

	public void setAnalysisLeafTree(ArrayList <String> al){
		for(int i = 0; i < al.size(); i++)
			analysisLeafTree.set(Integer.valueOf(al.get(i)), "YES");
	}
	
	/* -= FUNZIONI Ausiliarie =- */
	
	/** 
	 * Highlights the selected node.
	 * 
	 * @param me - the MouseEvent
	 */
	private void clickNodeTree(MouseEvent me){
		selectNode = treeView.getPathForLocation(me.getX(), me.getY());
		treeView.setSelectionPath(selectNode);
	}
	
	/**
	 * Show the project tree's menu.
	 * 
	 * @param s - String containing the name of selected node
	 * @param me - the MouseEvent
	 */
	private void showMenuTree(String s, MouseEvent me){
		if(s == null) return;
		
		if(selectNode != null && selectNode.getLastPathComponent() != null){
		  if( s != selectNode.getLastPathComponent().toString() && selectNode.getLastPathComponent().toString() != "Input" 
			  && selectNode.getLastPathComponent().toString() != "Commonalities"){
			menuTree.show(me.getComponent(), me.getX(), me.getY());
			me.consume();
		  }	
		}
	}
}