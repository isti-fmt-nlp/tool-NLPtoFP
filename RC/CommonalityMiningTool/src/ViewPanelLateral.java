/**
 * 
 * @author Daniele Cicciarella
 *
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class ViewPanelLateral extends JFrame
{
	/* JPanel in cui viene inserito l'albero del progetto */
	private JPanel panelTree = new JPanel();
	
	/* DefaultMutableTreeNode contenente la radice dell'albaro */
	private DefaultMutableTreeNode rootTree = null;
	
	/* DefaultTreeModel gestisce grafica del progetto */
	private DefaultTreeModel graficTree = null;
	
	/* JTree contenente l'albero del progetto */
	private JTree treeProject = null;
	
	/* TreePath contenente il nodo selezionato */
	private TreePath selectNode = null;
	
	/* JPopupMenu contenente il menù dell'albero */
	private JPopupMenu menuTree = null;
	
	/* ArrayList contenente le informazioni sull'analisi degli elementi del nodo Input */
	private ArrayList <String> analysisLeafTree = new ArrayList <String> ();
	
	/** Costruttore
	 * 
	 * @param menuTree
	 */
	public ViewPanelLateral(JPopupMenu menuTree)
	{
		this.menuTree = menuTree;
		
		panelTree.setLayout(null);
		panelTree.setBounds(new Rectangle(18,115,300,585));
		panelTree.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
	}
	
	/** Crea albero del progetto 
	 * 
	 * @param s stringa contenente il nome del progetto
	 */
	public void createTree(String s)
	{
	    final String tmp = s;
		
		if(s.length() == 0 || s == null)
			return;

		rootTree = new DefaultMutableTreeNode(s);
		rootTree.add(new DefaultMutableTreeNode("Input"));
		rootTree.add(new DefaultMutableTreeNode("Commonalities"));
		
		graficTree = new DefaultTreeModel(rootTree);
			
		treeProject = new JTree(graficTree);
		treeProject.setBounds(5,5,290,575);
		treeProject.setCellRenderer(new ColorTree(s, rootTree, analysisLeafTree));
		treeProject.addMouseListener(
				new MouseAdapter()
				{			
						@Override
						public void mouseClicked(MouseEvent me) 
						{							
							clickNodeTree(me);
							
							if(me.getButton() == 3)
								showMenuTree(tmp, me);
						}
				});	
		
		panelTree.removeAll();
		panelTree.add(treeProject);
	}
	
	/** Carica l'albero di un progetto 
	 * 
	 * @param s stringa contenente il nome del progetto
	 * @param al ArrayList contenente i nomi dei file 
	 */
	public void loadTree(String s, ArrayList <String> al)
	{
		final String tmp = s;
		
		if(s == null || al == null)
			return;

		rootTree = new DefaultMutableTreeNode(s);
		rootTree.add(new DefaultMutableTreeNode("Input"));
		rootTree.add(new DefaultMutableTreeNode("Commonalities"));
		
		graficTree = new DefaultTreeModel(rootTree);

		treeProject = new JTree(graficTree);
		treeProject.setBounds(5,5,290,575);
		treeProject.setCellRenderer(new ColorTree(s, rootTree, analysisLeafTree));
		treeProject.addMouseListener(
				new MouseAdapter()
				{			
						@Override
						public void mouseClicked(MouseEvent me) 
						{							
							clickNodeTree(me);
							
							if(me.getButton() == 3)
								showMenuTree(tmp, me);
						}
				});
		
		for(int i = 0; i < al.size(); i++)
		{
			graficTree.insertNodeInto(
					new DefaultMutableTreeNode(al.get(i)), (MutableTreeNode) rootTree.getChildAt(0), rootTree.getChildAt(0).getChildCount());
			analysisLeafTree.add("NO");
		}	
		panelTree.removeAll();
		panelTree.add(treeProject);
	}
	
	/** Aggiunge un elemento al nodo Input
	 * 
	 * @param s stringa contenente il nome del nodo da aggiungere
	 */
	public boolean addNodeInput(String s)
	{
		for(int i = 0; i < rootTree.getChildAt(0).getChildCount(); i++)
		{
			if(rootTree.getChildAt(0).getChildAt(i).toString().equals(s.toString()))
				return false;
		}
		
		graficTree.insertNodeInto(
				new DefaultMutableTreeNode(s), (MutableTreeNode) rootTree.getChildAt(0), rootTree.getChildAt(0).getChildCount());	
		analysisLeafTree.add("NO");
		return true;
	}
	
	/** Cancella un elemento al nodo Input
	 * 
	 * @return i indice del nodo eliminato
	 */
	public int deleteNodeInput()
	{
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) selectNode.getLastPathComponent();
		
		for(int i = 0; i < rootTree.getChildAt(0).getChildCount(); i++)
		{
			if(rootTree.getChildAt(0).getChildAt(i).toString().equals(dmtn.toString()))
			{
				graficTree.removeNodeFromParent(dmtn);	
				analysisLeafTree.remove(i);
				return i;
			}
		}
		return -1;
	}
	
	/* -= FUNZIONI lettura/scrittura parametri =- */
	
	/** Legge il nodo selezionato
	 * 
	 * @return i >= 0 nodo selezionato è l'indice di un elemento di Input
	 * @return i = -2 nodo selezionato è Commonalities
	 * @return i = -1 nodo selezionato non rilevante
	 */
	public int getAnalysisLeaf()
	{
		if(selectNode == null || selectNode.getLastPathComponent() == null)
			return -1;
		
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) selectNode.getLastPathComponent();
		
		if(rootTree.getChildAt(1).toString().equals(dmtn.toString()))
			return -2;
		
		for(int i = 0; i < rootTree.getChildAt(0).getChildCount(); i++)
		{
			if(rootTree.getChildAt(0).getChildAt(i).toString().equals(dmtn.toString()))
				return i;
		}
		return -1;
	}
	
	/* -= FUNZIONI lettura parametri =- */
	
	/** Lettura del panello panelTree
	 * 
	 * @return panelTree
	 */
	public JPanel getPanelTree()
	{
		return panelTree;
	}
	
	/** Lettura del JTree treeProject
	 * 
	 * @return treeProject
	 */
	public JTree getTree()
	{
		return treeProject;
	}
	
	/** Lettura delle informazioni sull'analisi degli elementi del nodo Input
	 * 
	 * @return analysisLeafTree
	 */
	public ArrayList <String> getAnalysisLeafTree()
	{
		return analysisLeafTree;
	}
	
	/** Setta gli elementi del nodo Input come analizzati
	 * 
	 */
	public void setAnalysisLeafTree()
	{
		for(int i = 0; i < analysisLeafTree.size(); i++)
			analysisLeafTree.set(i, "YES");
	}
	
	/** Setta gli elementi del nodo Input come analizzati
	 * 
	 * @param al ArrayList contenente gli elementi del nodo Input da settare come analizzati
	 */

	public void setAnalysisLeafTree(ArrayList <String> al)
	{
		for(int i = 0; i < al.size(); i++)
			analysisLeafTree.set(Integer.valueOf(al.get(i)), "YES");
	}
	
	/* -= FUNZIONI Ausiliarie =- */
	
	/** Lettura nodo selezionato
	 * 
	 * @param me evento mouse
	 */
	private void clickNodeTree(MouseEvent me)
	{
		selectNode = treeProject.getPathForLocation(me.getX(), me.getY());	
		treeProject.setSelectionPath(selectNode);
	}
	
	/** Mostra menù dell'albero del progetto
	 * 
	 * @param s Stringa contenente il nome del nodo selezionato
	 * 
	 * @param me evento mouse
	 */
	private void showMenuTree(String s, MouseEvent me) 
	{
		if(s == null)
			return;
		
		if(selectNode != null && selectNode.getLastPathComponent() != null)
		{
			if(s != selectNode.getLastPathComponent().toString() && selectNode.getLastPathComponent().toString() != "Input" && selectNode.getLastPathComponent().toString() != "Commonalities")
			{
				menuTree.show(me.getComponent(), me.getX(), me.getY());
				me.consume();
			}	
		}
	}
	
	/** Gestisce la grafica dell'albero del progetto */
	class ColorTree extends JLabel implements TreeCellRenderer
	{
		
		private String nameTree = null;
		
		private DefaultMutableTreeNode rootTree = null;
		
		private ArrayList <String> analysisNodeTree = new ArrayList <String> ();
			
		private ImageIcon folderTree = new ImageIcon("./src/DATA/Tree/folder.gif");
		
		private ImageIcon squareRedTree = new ImageIcon("./src/DATA/Tree/squarer.gif");
		
		private ImageIcon squareGreenTree = new ImageIcon("./src/DATA/Tree/squareg.gif");

		private boolean bSelected = false;
		
		public ColorTree(String nameTree, DefaultMutableTreeNode rootTree, ArrayList <String> analysisNodeTree)
		{
			this.nameTree = nameTree;
			this.rootTree = rootTree;
			this.analysisNodeTree = analysisNodeTree;
		}
		
		@Override
		public Component getTreeCellRendererComponent( JTree tree,
						Object value, boolean bSelected, boolean bExpanded,
								boolean bLeaf, int iRow, boolean bHasFocus )
		{
			this.bSelected = bSelected;
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			
			String labelText = (String)node.getUserObject();		
			
			if(labelText.equals(nameTree))
				setIcon(folderTree);
			
			else if(labelText.equals("Input"))
				setIcon(folderTree);
				
			else if( labelText.equals("Commonalities"))
				setIcon(folderTree);
			
			else 
			{
				for(int i = 0; i < analysisNodeTree.size(); i++)
				{
					if(labelText.equals(rootTree.getChildAt(0).getChildAt(i).toString()) && analysisNodeTree.get(i).equals("NO"))
						setIcon(squareRedTree);

					else if(labelText.equals(rootTree.getChildAt(0).getChildAt(i).toString()) && analysisNodeTree.get(i).equals("YES"))
						setIcon(squareGreenTree);		
				}
			}
			setText(labelText);			
			return this;
		}
		
		public void paint(Graphics g)
		{
			Color c = bSelected ? SystemColor.textHighlight : Color.WHITE;
			
			g.setColor(c);
			g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
			
			super.paint(g);
		}
	}
}