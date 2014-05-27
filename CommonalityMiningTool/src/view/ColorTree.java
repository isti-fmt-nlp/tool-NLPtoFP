/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

	/** Manages the graphics of the project tree */
	public class ColorTree extends JLabel implements TreeCellRenderer{

		private static final long serialVersionUID = -6097517753655989657L;

		private String nameTree = null;
		
		private DefaultMutableTreeNode rootTree = null;
		
		private ArrayList <String> analysisNodeTree = new ArrayList <String> ();
			
		private ImageIcon folderTree = new ImageIcon(getClass().getResource("/Tree/folder.gif"));
		
		private ImageIcon squareRedTree = new ImageIcon(getClass().getResource("/Tree/squarer.gif"));
		
		private ImageIcon squareGreenTree = new ImageIcon(getClass().getResource("/Tree/squareg.gif"));

		private boolean bSelected = false;
		
		public ColorTree(String nameTree, DefaultMutableTreeNode rootTree, ArrayList <String> analysisNodeTree){
			this.nameTree = nameTree;
			this.rootTree = rootTree;
			this.analysisNodeTree = analysisNodeTree;
		}
		
		@Override
		public Component getTreeCellRendererComponent( JTree tree, Object value, boolean bSelected, boolean bExpanded,
				boolean bLeaf, int iRow, boolean bHasFocus ){
			this.bSelected = bSelected;			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;			
			String labelText = (String)node.getUserObject();		
			
			if(labelText.equals(nameTree)) setIcon(folderTree);
			else if(labelText.equals("Input")) setIcon(folderTree);
			else if( labelText.equals("Commonality")) setIcon(folderTree);
			else if( labelText.equals("Variability")) setIcon(folderTree);
			else{
			  for(int i = 0; i < analysisNodeTree.size(); i++){
				if(labelText.equals(rootTree.getChildAt(0).getChildAt(i).toString()) && analysisNodeTree.get(i).equals("NO"))
				  setIcon(squareRedTree);
				else if(labelText.equals(rootTree.getChildAt(0).getChildAt(i).toString()) && analysisNodeTree.get(i).equals("YES"))
				  setIcon(squareGreenTree);		
			  }
			}
			
			setText(labelText);			
			return this;
		}
		
		public void paint(Graphics g){
			Color c = bSelected ? SystemColor.textHighlight : Color.WHITE;			
			g.setColor(c);
			g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);			
			super.paint(g);
		}
	}
