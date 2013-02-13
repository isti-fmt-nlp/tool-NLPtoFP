package project;

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
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

public class ViewTool extends JFrame implements ActionListener, WindowListener , Observer
{	
	private ControllerTool controllerTool = null;
	
	private ViewDialog viewDialog = null;
	
	private ViewTree viewTree = null;
	
	private ViewThrobber viewThrobber = null; 
	
	private ViewTab viewTab = null; 
	
	private JMenuBar barMenu = new JMenuBar();
	
	private JMenu menuFile, menuView;
	
	private JMenuItem menuFile0, menuFile1, menuFile2, menuFile3, menuFile4, menuFile5, menuFile6;

	private JMenu menuView0 = null;
	
	private JPanel panelTree = null;

	public ViewTool()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(950,600));
		setJMenuBar(barMenu);
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		addWindowListener(this);
		setLocationRelativeTo(null);
		setLayout(null);
		
		controllerTool = new ControllerTool();
		controllerTool.addObserver(this);
		
		viewTree = new ViewTree();
		viewTree.addObserver(this);
		
		viewDialog = new ViewDialog();
		viewDialog.addObserver(this);
		
		viewThrobber = new ViewThrobber();
		viewThrobber.addObserver(this);
	
		viewTab = new ViewTab();
	
		/* 
		 * Menu file 
		 */
		menuFile0 = new JMenuItem("New Project");
		menuFile0.addActionListener(this);
		
		menuFile1 = new JMenuItem("Open..");
		menuFile1.addActionListener(this);
		
		menuFile2 = new JMenuItem("Open Recent");
		menuFile2.addActionListener(this);
		
		menuFile3 = new JMenuItem("Save");
		menuFile3.addActionListener(this);
		menuFile3.setEnabled(false);
		
		menuFile4 = new JMenuItem("Save As..");
		menuFile4.addActionListener(this);
		menuFile4.setEnabled(false);
		
		menuFile5 = new JMenuItem("Close");
		menuFile5.addActionListener(this);
		menuFile5.setEnabled(false);
		
		menuFile6 = new JMenuItem("Exit");	
		menuFile6.addActionListener(this);	
		
		menuFile = new JMenu("File");
		menuFile.add(menuFile0);
		menuFile.add(menuFile1);
		menuFile.add(menuFile2);
		menuFile.addSeparator();
		menuFile.add(menuFile3);
		menuFile.add(menuFile4);
		menuFile.add(menuFile5);
		menuFile.addSeparator();
		menuFile.add(menuFile6);
		
		/* 
		 * Menu view 
		 */
		menuView0 = new JMenu("File ");
		menuView0.addActionListener(this);
		menuView0.setEnabled(false);
				
		menuView = new JMenu("View");
		menuView.add(menuView0);
		
		/* 
		 * Menu generale 
		 */
		barMenu.add(menuFile);
		barMenu.add(menuView);
		
		panelTree = new JPanel();
		panelTree.setBounds(new Rectangle(18,70,300,450));
		panelTree.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
							BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));	
		panelTree.setLayout(null);
	}
	
	@Override
	public void update(Observable os, Object o) 
	{
		if(os instanceof ControllerTool)
		{
			if(o.equals("0"))
			{
				viewTree.createTree();
				viewTree.setMenuPopup(3, false);
				menuFile2.setEnabled(true);
				menuFile3.setEnabled(true);
				menuFile4.setEnabled(true);
				menuFile5.setEnabled(true);
				panelTree.removeAll();
				panelTree.add(viewTree.getTreeTool());
				remove(panelTree);
				add(panelTree);
			}
			else if(o.equals("1"))
			{
				viewTree.loadTree();
				viewTree.setMenuPopup(3, true);
				menuFile2.setEnabled(true);
				menuFile3.setEnabled(false);
				menuFile4.setEnabled(true);
				menuFile5.setEnabled(true);
				panelTree.removeAll();
				panelTree.add(viewTree.getTreeTool());
				remove(panelTree);
				add(panelTree);
			}
			else if(o.equals("2"))
			{
				viewTree.loadTree();
				viewTree.setMenuPopup(3, true);
				menuFile2.setEnabled(false);
				menuFile3.setEnabled(false);
				menuFile4.setEnabled(true);
				menuFile5.setEnabled(true);
				panelTree.removeAll();
				panelTree.add(viewTree.getTreeTool());
				remove(panelTree);
				add(panelTree);
			}	
			else if(o.equals("3"))
			{
				menuFile3.setEnabled(false);
				menuFile4.setEnabled(false);
			}
			else if(o.equals("4"))
			{
				menuFile3.setEnabled(false);
				menuFile4.setEnabled(false);
			}	
			else if(o.equals("5"))
			{
				menuFile2.setEnabled(true);
				menuFile3.setEnabled(false);
				menuFile4.setEnabled(false);
				menuFile5.setEnabled(false);
				panelTree.remove(viewTree.getTreeTool());
				remove(panelTree);
			}
			else if(o.equals("6"))
			{
				dispose();
				System.exit(EXIT_ON_CLOSE);
			}
			else if(o.equals("7"))
			{
				viewTab.createTab();
				remove(viewTab);
				add(viewTab);
			}
			else if(o.equals("8"))
			{
				viewTree.setMenuPopup(3, true);
			}
			else if(o.equals("9"))
			{
				if(viewTree.getRootTree().getChildAt(0).getChildCount() != 0)
					viewTree.setMenuPopup(3, true);
				
				else
					viewTree.setMenuPopup(3, false);
			}
			else if(o.equals("10A"))
			{
				viewThrobber.startThrobber();
				setEnabled(false);
			}
			else if(o.equals("10B"))
			{
				setEnabled(true);
				viewThrobber.stopThrobber();
				viewTree.setMenuPopup(3, false);
			}
		}
		else if(os instanceof ViewTree)
		{
			if(o.equals("0"))
				controllerTool.sendActionModel(this, 7);

			else if(o.equals("1"))
				controllerTool.sendActionModel(this, 8);

			else if(o.equals("2"))
				controllerTool.sendActionModel(this, 9);

			else if(o.equals("3"))
				controllerTool.sendActionModel(this, 10);
		}
		repaint();
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		if(ae.getActionCommand().equals("New Project"))
			controllerTool.sendActionModel(this, 0);
		
		else if(ae.getActionCommand().equals("Open.."))
			controllerTool.sendActionModel(this, 1);
		
		else if(ae.getActionCommand().equals("Open Recent"))
			controllerTool.sendActionModel(this, 2);
		
		else if(ae.getActionCommand().equals("Save"))
			controllerTool.sendActionModel(this, 3);
		
		else if(ae.getActionCommand().equals("Save As.."))
			controllerTool.sendActionModel(this, 4);
		
		else if(ae.getActionCommand().equals("Close"))
			controllerTool.sendActionModel(this, 5);
		
		else if(ae.getActionCommand().equals("Exit"))
		{
			controllerTool.sendActionModel(this, 6);
			dispose();
			System.exit(EXIT_ON_CLOSE);
		}	
		repaint();
	}

	public void sendActionView(int i)
	{
		controllerTool.sendActionModel(this,i);
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) 
	{}

	@Override
	public void windowClosed(WindowEvent arg0) 
	{}

	@Override
	public void windowClosing(WindowEvent arg0) 
	{
		controllerTool.sendActionModel(this, 6);
		dispose();
		System.exit(EXIT_ON_CLOSE);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) 
	{}

	@Override
	public void windowDeiconified(WindowEvent arg0) 
	{}

	@Override
	public void windowIconified(WindowEvent arg0) 
	{}

	@Override
	public void windowOpened(WindowEvent arg0) 
	{}

	public ViewDialog getViewDialog()
	{
		return viewDialog;
	}
	
	public ViewTree getViewTree()
	{
		return viewTree;
	}
	
	public ViewThrobber getViewThrobber()
	{
		return viewThrobber;
	}
	
	public ViewTab getViewTab()
	{
		return viewTab;
	}
	
    public static void main(String[] args) 
	{
		ViewTool viewTool = new ViewTool();
		viewTool.setVisible(true);
	}
}
