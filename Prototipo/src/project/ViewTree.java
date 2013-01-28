package project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Observable;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class ViewTree extends Observable implements ViewTreeI, ActionListener
{
	private DefaultTreeModel graficTree = null;
	
	private DefaultMutableTreeNode rootTree = null;
	
	private JTree tree = null;

	private TreePath selectNode = null;
	
	private String pathTree = null;
	
	private ParserTreeXML parserTreeXml = null;
	
	private JPopupMenu menuTree = null;
	
	private JMenuItem menuTree0, menuTree1, menuTree2, menuTree3;
	
	public ViewTree()
	{
		menuTree0 = new JMenuItem("View..");
		menuTree0.addActionListener(this);
		
		menuTree1 = new JMenuItem("Add File");
		menuTree1.addActionListener(this);
		
		menuTree2 = new JMenuItem("Remove..");
		menuTree2.addActionListener(this);
		
		menuTree3 = new JMenuItem("Run..");
		menuTree3.addActionListener(this);
		
		menuTree = new JPopupMenu("Menù");
		menuTree.add(menuTree0);
		menuTree.addSeparator();
		menuTree.add(menuTree1);
		menuTree.add(menuTree2);
		menuTree.addSeparator();
		menuTree.add(menuTree3);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		if(ae.getActionCommand().equals("View.."))
		{
			setChanged();
			notifyObservers("0");
		}
		else if(ae.getActionCommand().equals("Add File"))
		{
			setChanged();
			notifyObservers("1");
		}
		else if(ae.getActionCommand().equals("Remove.."))
		{
			setChanged();
			notifyObservers("2");
		}
		else if(ae.getActionCommand().equals("Run.."))
		{
			setChanged();
			notifyObservers("3");
		}
	}
	
	public void createTree()
	{
		rootTree = new DefaultMutableTreeNode("Project");
		rootTree.add(new DefaultMutableTreeNode("Input"));
		rootTree.add(new DefaultMutableTreeNode("Commonalities"));
		rootTree.add(new DefaultMutableTreeNode("Variabilities"));
		
		tree = new JTree(graficTree = new DefaultTreeModel(rootTree));
		setFormTreeProject();
	}
	
	public void loadTree()
	{
		parserTreeXml = new ParserTreeXML();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
			
		try 
		{
			if(pathTree != null && (new File(pathTree).exists()))
			{
				SAXParser parser = factory.newSAXParser();
				parser.parse(pathTree, parserTreeXml);
	
				tree = new JTree(
						graficTree = new DefaultTreeModel(
								rootTree = parserTreeXml.getRootTree()));
				setFormTreeProject();
			}
			else 
				return;
		} 
		catch (ParserConfigurationException e) 
		{

		}
		catch (SAXException e) 
		{
			
		}
		catch (IOException e) 
		{
			
		} 
	}
	
	public void saveTree()
	{
		PrintWriter pw;
		
		ArrayList <String> array = new ArrayList <String>();
		
		visitPreOrder(rootTree, array);
		
		try 
		{
			pw =  new PrintWriter(
				      new BufferedWriter(
					      new FileWriter(pathTree, false)));

			for(int i = 0; i < array.size(); i++)
				pw.print(array.get(i));

			pw.close();
		} 
		catch (UnsupportedEncodingException e) 
		{
			return;
		} 
		catch (FileNotFoundException e) 
		{
			return;
		} 
		catch (IOException e) 
		{
			return;
		}
	}

	public void closeTree()
	{

	}

	public void addLeafTree(String s)
	{
	    if(s != null)
	    {
			graficTree.insertNodeInto(
					new DefaultMutableTreeNode(s), (MutableTreeNode) rootTree.getChildAt(0), rootTree.getChildAt(0).getChildCount());	

	    }
	}
	
	public int deleteLeafTree()
	{
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) selectNode.getLastPathComponent();
		
		if(dmtn != null && !rootTree.toString().equals(dmtn.toString())
			&& !rootTree.getChildAt(0).toString().equals(dmtn.toString())
		    && !rootTree.getChildAt(1).toString().equals(dmtn.toString()) 
		    && !rootTree.getChildAt(2).toString().equals(dmtn.toString()))
		{
			if(dmtn.getParent().toString().equals(rootTree.getChildAt(0).toString()))
			{
				for(int i = 0; i < rootTree.getChildAt(0).getChildCount(); i++)
				{
					if(rootTree.getChildAt(0).getChildAt(i).toString().equals(dmtn.toString()))
					{
						graficTree.removeNodeFromParent(dmtn);	
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	public int getSelectLeafInput()
	{
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) selectNode.getLastPathComponent();
		
		if(dmtn != null && !rootTree.toString().equals(dmtn.toString())
			&& !rootTree.getChildAt(0).toString().equals(dmtn.toString())
		    && !rootTree.getChildAt(1).toString().equals(dmtn.toString()) 
		    && !rootTree.getChildAt(2).toString().equals(dmtn.toString()))
		{
			if(dmtn.getParent().toString().equals(rootTree.getChildAt(0).toString()))
			{
				for(int i = 0; i < rootTree.getChildAt(0).getChildCount(); i++)
				{
					if(rootTree.getChildAt(0).getChildAt(i).toString().equals(dmtn.toString()))
						return i;
				}
			}
		}
		return -1;
	}
	
	public void setMenuPopup(int i, boolean b)
    {
    	switch(i)
    	{
    		case 0:
    		{
    			menuTree0.setEnabled(b);
    			break;
    		}
    		case 1:
    		{
    			menuTree1.setEnabled(b);
    			break;
    		}
    		case 2:
    		{
    			menuTree2.setEnabled(b);
    			break;
    		}
    		case 3:
    		{
    			menuTree3.setEnabled(b);
    			break;
    		}
    	}
    }
	
	public JTree getTreeTool()
	{
		return tree;
	}
	
	public void setPathTree(String s)
	{
		pathTree = s;
	}
	
	public String getPathTree()
	{
		return pathTree;
	}
	
	public DefaultMutableTreeNode getRootTree()
	{
		return rootTree;
	}

	private void visitPreOrder(DefaultMutableTreeNode p, ArrayList <String> array)
	{		
		if(p.equals(rootTree))
			array.add("<root>" + p.toString());
		
		else if(p.equals(rootTree.getChildAt(0)))
		{
			array.add("<node>" + p.toString());
			
			for(int j = 0; j < rootTree.getChildAt(0).getChildCount(); j++)
				array.add("<leaf>" + rootTree.getChildAt(0).getChildAt(j) + "</leaf>");
			
			array.add("</node>");
		}
		
		else if(p.equals(rootTree.getChildAt(1)))
		{
			array.add("<node>" + p.toString());
			
			for(int j = 0; j < rootTree.getChildAt(1).getChildCount(); j++)
				array.add("<leaf>" + rootTree.getChildAt(1).getChildAt(j) + "</leaf>");
			
			array.add("</node>");
		}
		else if(p.equals(rootTree.getChildAt(2)))
		{
			array.add("<node>" + p.toString());
			
			for(int j = 0; j < rootTree.getChildAt(2).getChildCount(); j++)
				array.add("<leaf>" + rootTree.getChildAt(2).getChildAt(j) + "</leaf>");
			
			array.add("</node>");
		}
		
		for(int j = 0; j < p.getChildCount(); j++)
		{
			visitPreOrder((DefaultMutableTreeNode) p.getChildAt(j), array);
			
			if(j == rootTree.getChildCount() - 1)
				array.add("</root>");
		}
	}
	
	private void ClickTree(MouseEvent me)
	{	
		selectNode = tree.getPathForLocation(me.getX(), me.getY());	
		tree.setSelectionPath(this.selectNode);
		tree.setEditable(true);
		tree.setRootVisible(true);
	    	
	    DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
	    
	    TreeCellEditor editor = new DefaultTreeCellEditor(tree, renderer);
		    
	    tree.setCellEditor(editor);
	}
	
	private void setFormTreeProject()
	{
		if(tree != null)
		{
			tree.setBounds(5,5,290,440);
			tree.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent me) 
			    {	
					ClickTree(me);
					
			        if(me.getButton() == 3)
					{
			        	menuTree.show(me.getComponent(), me.getX(), me.getY());
						me.consume();
					}
			    }
			});
		}
	}
	
	/** Classe effettua il parsing del file xml selezionato */
    class ParserTreeXML extends DefaultHandler
    {   
        private DefaultMutableTreeNode root = null;
        
        private int i = 0;
        
        private  boolean r = false, n = false, l = false;
        
        @Override
        public void startElement(String uri, String localName, String gName, Attributes attributes)
        {
        	if(gName.equals("root"))
	        	r = true;
        	
        	else if(gName.equals("node"))
        		n = true;
        	
        	else
        		l = true;
        }
        
        @Override
        public void characters(char [] ch, int start, int length)
        {
        	if(r && !n && !l)
        		root = new DefaultMutableTreeNode(new String(ch, start, length));
        	
        	else if(r && n && !l)
        		root.add(new DefaultMutableTreeNode(new String(ch, start, length)));
        	
        	else if(r && n && l)
        	{
        		DefaultMutableTreeNode n = (DefaultMutableTreeNode) root.getChildAt(i);
        		n.add(new DefaultMutableTreeNode(new String(ch, start, length)));
        	}
        	else
        	{}
        }
        
        @Override
        public void endElement(String uri, String localName, String gName)
        {
        	if(gName.equals("root"))
	        	r = false;
        	
        	else if(gName.equals("node"))
        	{
        		n = false;
        		i = i + 1;	
        	}
        	else 
        		l = false;
        }
        
        public DefaultMutableTreeNode getRootTree()
        {
        	return root;
        }
    }
}
