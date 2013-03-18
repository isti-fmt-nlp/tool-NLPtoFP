/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

public class ViewPanelLateral extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
		rootTree.add(new DefaultMutableTreeNode("Commonality"));
		
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
		rootTree.add(new DefaultMutableTreeNode("Commonality"));
		
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
}