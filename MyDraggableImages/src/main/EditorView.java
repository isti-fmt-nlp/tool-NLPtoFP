package main;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box.Filler;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.LayeredHighlighter;
import javax.xml.stream.events.StartDocument;



public class EditorView extends JFrame implements Observer{
	/** variables used for debugging*/
	private static boolean debug=false;
	private static boolean debug2=false;
	private static boolean debug3=false;
	private static boolean debug4=true;
//	private static Robot eventsRobot = null;

	class EditorSplitPane extends JSplitPane{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EditorSplitPane(int horizontalSplit) {
			super(horizontalSplit);
		}

		@Override
		public void paint(Graphics g){
//			System.out.println("getParent().getParent().getParent().getParent(): "+getParent().getParent().getParent().getParent());
			Graphics2D g2 = (Graphics2D)g.create();		

			paintComponent(g);
			paintBorder(g);
//			paintChildren(g);//panels in the diagram panel are drawn over lines
			drawAllConnectors(g2);
			paintChildren(g);//panels in the diagram panel are drawn below lines
//			super.paint(g);
			
			
		}
		
	}

	
	/** current amount of connector lines in the diagram panel*/
	private static int connectorsCount=0;
	/** current amount of Alternative Groups in the diagram panel*/
	private static int altGroupsCount=0;
	/** current amount of Or Groups in the diagram panel*/
	private static int orGroupsCount=0;
	/** current amount of features in the diagram panel*/
	private static int featuresCount=0;
	
	/** list of all connector starting dots,
	 *  corresponding ending dots can be found in endConnectorDots at the same index
	 */
	private static ArrayList<JComponent> startConnectorDots=null;
	
	/** list of all connector ending dots,
	 *  corresponding starting dots can be found in startConnectorDots at the same index
	 */
//	private static ArrayList<JComponent> endConnectorDots=null;
	
	//still unused
	/** previous location of all connector starting dots, with same indexes of the lists above*/
//	private static ArrayList<Point> prevStartConnectorDotsLocation=null;
	
	//still unused
	/** previous location of all connector ending dots, with same indexes of the lists above*/
//	private static ArrayList<Point> prevEndConnectorDotsLocation=null;
	
	//to revise
	/** list of all connector dots that must be redrawn, with same indexes of the lists above*/
//	private static ArrayList<Boolean> connectorDotsToRedraw=null;
	
	/** list of Alternative Groups*/
	private static ArrayList<GroupPanel> altGroupPanels=null;	
	/** list of Or Groups*/
	private static ArrayList<GroupPanel> orGroupPanels=null;
	
	/** OrderedList containing the panels children of the diagram panel*/
	private static OrderedList visibleOrderDraggables = null;
	
	/** X coordinate of last mouse pression*/
	private static int lastPositionX=-1;
	/** Y coordinate of last mouse pression*/
	private static int lastPositionY=-1;
	
	/**top level frame*/
	private static JFrame frameRoot = null;//frame root		
	
//	private static JFrame toolDragPanel = null;//temporary frame used to drag tools
	/**image used to drag tools*/
	private static BufferedImage toolDragImage = null;
	/**position of the dragged image*/
	private static Point toolDragPosition = null;
	
	
	
	/**the panel containing the diagram */
//	private static JPanel diagramPanel=null;
//	private static OrderedListPaintJPanel diagramPanel=null;
	private static JLayeredPane diagramPanel=null;

	/**the panel containing the tools */
	private static JPanel toolsPanel=null;
	
	/** the splitter panel containing diagramPanel and toolsPanel*/
//	private static JSplitPane splitterPanel=null;
	private static EditorSplitPane splitterPanel=null;
	
	/** the active Feature panel*/
	private static FeaturePanel lastFeatureFocused=null;
	/** the active Anchor panel*/
	private static JComponent lastAnchorFocused=null;
	/** the component on which a drop is about to be done*/
	private static JComponent underlyingComponent=null;
//	private static boolean isDraggingFeature=false;
	
	/** URL of the new feature icon*/
	private static URL newFeatureIconURL=EditorView.class.getResource("/feature rectangle2.png");
//	private static URL newFeatureIconURL=MyDraggableImages.class.getResource("/feature rectangle.png");
//	private static URL newFeatureIconURL=MyDraggableImages.class.getResource("/balzac.jpg");
	/** URL of the connector starting dot icon*/
	private static URL connectorStartDotIconURL=EditorView.class.getResource("/Connector Start Dot.png");
	/** URL of the new group starting dot icon*/
	private static URL ALTGroupDotIconURL=EditorView.class.getResource("/ALTGroup Start Dot.png");
	/** URL of the new group starting dot icon*/
	private static URL ORGroupDotIconURL=EditorView.class.getResource("/ORGroup Start Dot.png");
	/** URL of the new connector ending dot icon*/
	private static URL connectorEndDotIconURL=EditorView.class.getResource("/Connector End Dot.png");
	/** URL of the connector line-only icon*/
	private static URL connectorLineLengthIconURL=EditorView.class.getResource("/Connector Line Length.png");
	/** URL of the group line-only icon*/
	private static URL groupLineLengthIconURL=EditorView.class.getResource("/Group Line Length.png");
	
	/** prefix of any feature name*/
	public static String featureNamePrefix="---FEATURE---#";
	/** prefix of any connector starting dot name*/
	public static String startConnectorsNamePrefix="---START_CONNECTOR---#";
	/** prefix of any connector ending dot name*/
	public static String endConnectorsNamePrefix="---END_CONNECTOR---#";
	/** prefix of any group Alternative Gtarting dot name*/
	public static String altGroupNamePrefix="---ALT_GROUP---#";
	/** prefix of any Or Group starting dot name*/
	public static String orGroupNamePrefix="---OR_GROUP---#";
	/** name ofthe diagram panel*/
	public static String diagramPanelName="---DIAGRAM_PANEL---";


	/** enumeration of items that can become active, for instance in a drag motion*/
	public static enum activeItems {
		DRAGGING_FEATURE, DRAGGING_TOOL_NEWFEATURE, DRAGGING_TOOL_CONNECTOR, NO_ACTIVE_ITEM,
		DRAGGING_EXTERN_ANCHOR, DRAGGING_TOOL_ALT_GROUP, DRAGGING_EXTERN_GROUP, DRAGGING_TOOL_OR_GROUP	
	}

	/** enumeration used to specify a item type through the program*/
	public static enum ItemsType {
		START_CONNECTOR, END_CONNECTOR, ALT_GROUP_START_CONNECTOR, OR_GROUP_START_CONNECTOR
	}

	/** tells what item is interested in the current action*/
	private static activeItems isActiveItem=activeItems.NO_ACTIVE_ITEM;

	/** maps tool names in the corresponding icon resource path*/
	private static HashMap<String, String> toolIconPaths=null;
	
	private static int featureBorderSize=20;

	private static Point start=new Point(), endLeft=new Point(), endRight=new Point();

	/** the popup menu for all diagram panel elements*/
	private static JPopupMenu diagramElementsMenu = null;
	/** the element interested by the popup menu*/
	private static JComponent popUpElement = null;
	
	/** popup menu items*/
	private static JMenuItem popMenuItemDelete = null;
	private static JMenuItem popMenuItemUngroup = null;
	private static JMenuItem popMenuItemPrintModelDebug = null;
	
	/** popup menu coordinates*/
	private static int diagramElementsMenuPosX=0;
	private static int diagramElementsMenuPosY=0;
	

	public EditorView(){
		
	}
	
	public boolean prepareUI(EditorController editorController){
		if(editorController==null) return false;
//		System.out.println("GraphicsEnvironment.isHeadless(): "+GraphicsEnvironment.isHeadless());
//		try {
//			eventsRobot = new Robot();
//		} catch (AWTException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		diagramElementsMenu.setFocusable(false);
//		diagramElementsMenu.setLightWeightPopupEnabled(false);
//		diagramElementsMenu.addFocusListener(new FocusListener() {
//			@Override
//			public void focusLost(FocusEvent e) {
//				System.out.println("Menu Lost Focus");				
//			}
//			
//			@Override
//			public void focusGained(FocusEvent e) {
//				System.out.println("Menu Gained Focus");								
//			}
//		});
//		diagramElementsMenu.addPopupMenuListener(new PopupMenuListener() {
//			
//			@Override
//			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
//				System.out.println("Menu Became Visible");
//			}
//			
//			@Override
//			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//				System.out.println("Menu Became Invisible");
//			}
//			
//			@Override
//			public void popupMenuCanceled(PopupMenuEvent e) {
//				System.out.println("Menu Canceled");
//			}
//		});
		
//		diagramElementsMenu.setIgnoreRepaint(true);
//		diagramElementsMenu.setLayout(new BorderLayout());
        
//        diagramElementsMenu.add(popMenuItemDelete);
//        diagramElementsMenu.add(popMenuItemUngroup);


		OrderedListNode tmpNode=null;
		visibleOrderDraggables = new OrderedList();
		startConnectorDots = new ArrayList<JComponent>();
//		endConnectorDots = new ArrayList<JComponent>();
//		prevStartConnectorDotsLocation = new ArrayList<Point>();
//		prevEndConnectorDotsLocation = new ArrayList<Point>();
//		connectorDotsToRedraw = new ArrayList<Boolean>();
		altGroupPanels = new ArrayList<GroupPanel>();
		orGroupPanels = new ArrayList<GroupPanel>();

		//creating root frame
//		frameRoot=new OrderedListPaintJFrame(visibleOrderDraggables);
		frameRoot=this;
		setLayout(new BorderLayout());		
//		frameRoot.setLayout(new BorderLayout());		
//		frameRoot.setLayout(null);		
//		frameRoot.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//creating tools panel
		toolsPanel = new JPanel();		
		toolsPanel.setLayout(new GridLayout(0, 2, 2, 2));		
//		toolsPanel.setLayout(new GridLayout(0, 2));		
		toolsPanel.setPreferredSize(new Dimension(140, Toolkit.getDefaultToolkit().getScreenSize().height));
		toolsPanel.setBackground(Color.white);
//		toolsPanel.setBorder(BorderFactory.createCompoundBorder(
//				BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
//						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		
		//creating tools items
		toolIconPaths=new HashMap<String, String>();
		toolIconPaths.put("New Feature", "/New Feature2.png");
		toolIconPaths.put("Connector Line", "/Connector Line.png");
		toolIconPaths.put("Alternative Group", "/Alternative Group.png");
		toolIconPaths.put("Or Group", "/Or Group.png");
		
		
		JComponent iconTmpPanel=null;
		for(int i=0; i<6; ++i){
		  iconTmpPanel=getToolIcon("New Feature", true);
//		  iconTmpPanel.addMouseListener(getToolbarMouseListener());
//		  iconTmpPanel.addMouseMotionListener(getToolbarMouseMotionListener());
		  iconTmpPanel.addMouseListener(editorController);
		  iconTmpPanel.addMouseMotionListener(editorController);
		  toolsPanel.add(iconTmpPanel);

		  iconTmpPanel=getToolIcon("Connector Line", true);
//		  iconTmpPanel.addMouseListener(getToolbarMouseListener());
//		  iconTmpPanel.addMouseMotionListener(getToolbarMouseMotionListener());
		  iconTmpPanel.addMouseListener(editorController);
		  iconTmpPanel.addMouseMotionListener(editorController);
		  toolsPanel.add(iconTmpPanel);

		  iconTmpPanel=getToolIcon("Alternative Group", true);
//		  iconTmpPanel.addMouseListener(getToolbarMouseListener());
//		  iconTmpPanel.addMouseMotionListener(getToolbarMouseMotionListener());
		  iconTmpPanel.addMouseListener(editorController);
		  iconTmpPanel.addMouseMotionListener(editorController);
		  toolsPanel.add(iconTmpPanel);

		  iconTmpPanel=getToolIcon("Or Group", true);
//		  iconTmpPanel.addMouseListener(getToolbarMouseListener());
//		  iconTmpPanel.addMouseMotionListener(getToolbarMouseMotionListener());
		  iconTmpPanel.addMouseListener(editorController);
		  iconTmpPanel.addMouseMotionListener(editorController);
		  toolsPanel.add(iconTmpPanel);
		}

//		toolsPanel.add(tryPink);
//		toolsPanel.add(tryGreen);
//		toolsPanel.add(tryOrange);

		
		//creating diagram panel, which will fit the rest of the root frame
		float[] myColorHBS=Color.RGBtoHSB(0, 0, 0, null);
//		float[] myColorHBS=Color.RGBtoHSB(150, 150, 190, null);
		diagramPanel = new JLayeredPane();
		diagramPanel.setName(diagramPanelName);
//		diagramPanel = new OrderedListPaintJPanel();
//		diagramPanel = new OrderedListPaintJPanel(visibleOrderDraggables);
//		diagramPanel.setPaintList(visibleOrderDraggables);
//		diagramPanel = new JPanel();
		diagramPanel.setBackground(Color.getHSBColor(myColorHBS[0], myColorHBS[1], myColorHBS[2]));
		diagramPanel.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width-160,
		Toolkit.getDefaultToolkit().getScreenSize().height));
		diagramPanel.setLayout(null);		
		diagramPanel.setMinimumSize(new Dimension((Toolkit.getDefaultToolkit().getScreenSize().width-160)/4,
				Toolkit.getDefaultToolkit().getScreenSize().height));
		
//		splitterPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitterPanel = new EditorSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitterPanel.setContinuousLayout(true);
//		splitterPanel.setOneTouchExpandable(true);
//		splitterPanel.setDividerLocation(0.5);
		splitterPanel.setDividerSize(6);
//		splitterPanel.setDoubleBuffered(true);
		splitterPanel.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width,
				Toolkit.getDefaultToolkit().getScreenSize().height));
		splitterPanel.setContinuousLayout(true);
//		splitterPanel.setLocation(0,0);
//		splitterPanel.setResizeWeight(0.5);
		
//		splitterPanel.add(diagramPanel);
		splitterPanel.add(toolsPanel);
		splitterPanel.add(diagramPanel);

//		frameRoot.add(splitterPanel);
		add(splitterPanel);

		
//		//creo i draggables
//		String label[]={"Questo è un nome lungo per una Feature!", "Questo è un nome ammodo", "Corto!"};
//		for(int i=0; i<label.length; ++i){
//			visibleOrderDraggables.addToTop(getDraggableFeature(label[i], 15+i*75, 15+i*75));
//			++featuresCount;
//		}
//						
//		//aggiungo i draggables al diagram panel
//		int index=0;
//
////		JPanel tmpPanel=null;
//		
//		tmpNode=visibleOrderDraggables.getFirst();
//		while(tmpNode!=null){
////			((JPanel)tmpNode.getElement()).addMouseListener(getDiagramMouseListener());
////			((JPanel)tmpNode.getElement()).addMouseMotionListener(getDiagramMouseMotionListener());
//			diagramPanel.setLayer((Component)tmpNode.getElement(), index);
//			diagramPanel.add((Component)tmpNode.getElement());
//			diagramPanel.setComponentZOrder((Component)tmpNode.getElement(), index);
////			tmpPanel=(JPanel)tmpNode.getElement();
////			diagramPanel.setLayer(tmpPanel, index);
////			diagramPanel.add(tmpPanel);
////			tmpPanel.setOpaque(false);
////			diagramPanel.setComponentZOrder(tmpPanel, index);
//			
//			
//			
//			tmpNode=tmpNode.getNext();
//			++index;
//		}

//		tmpNode=visibleOrderDraggables.getLast();
//		while(tmpNode!=null){
//			diagramPanel.add((JPanel)tmpNode.getElement(), index);
//			tmpNode=tmpNode.getPrev();
//			++index;
//		}

		diagramPanel.addMouseListener(editorController);
		diagramPanel.addMouseMotionListener(editorController);
//		diagramPanel.addMouseListener(getDiagramMouseListener());
//		diagramPanel.addMouseMotionListener(getDiagramMouseMotionListener());

//		splitterPanel.addMouseListener(getDiagramMouseListener());
//		splitterPanel.addMouseMotionListener(getDiagramMouseMotionListener());
//		frameRoot.addMouseListener(getDiagramMouseListener());
//		frameRoot.addMouseMotionListener(getDiagramMouseMotionListener());
		
//		toolsPanel.addMouseListener(getToolbarMouseListener());
//		toolsPanel.addMouseMotionListener(getToolbarMouseMotionListener());

		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setLocation(0, 0);
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		addMouseListener(getToolbarMouseListener());
		addMouseMotionListener(getToolbarMouseMotionListener());
		

		//creating diagram popup menu
		diagramElementsMenu = new JPopupMenu();
		popMenuItemDelete = new JMenuItem("Delete Element");
        popMenuItemUngroup = new JMenuItem("Ungroup Element");        
        popMenuItemPrintModelDebug = new JMenuItem("Print Model[DEBUG COMMAND]");

        popMenuItemDelete.addActionListener(editorController);        
        popMenuItemUngroup.addActionListener(editorController);        
        popMenuItemPrintModelDebug.addActionListener(editorController);        
        
//        popMenuItemDelete.addActionListener(
//        		
//          new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
////              System.out.println("e.getSource():"+e.getSource());
////              System.out.println("e.ActionCommand():"+e.getActionCommand());
//              String elementName = null;
//              if (popUpElement!=null) elementName=popUpElement.getName();
//
//              /* ***DEBUG*** */
//              if(debug3) System.out.println("Popup Menu requested delete on "+elementName
//            		  +"\ne = "+e
//            		  /*+"\ncomp.getName()="+comp.getName()*/);
//              /* ***DEBUG*** */
//              
//              if(elementName!=null && elementName.startsWith(startConnectorsNamePrefix)){
////                if(elementName.startsWith(startConnectorsNamePrefix)) ){
//            	deleteAnchor(popUpElement);
//            	deleteAnchor(((AnchorPanel)popUpElement).getOtherEnd());
////                deleteAnchor( ((AnchorPanel)popUpElement).getOtherEnd());
//            	frameRoot.repaint();
//              }
//              if(elementName!=null && elementName.startsWith(endConnectorsNamePrefix)){
////                if(elementName.startsWith(startConnectorsNamePrefix)) ){
//            	if(((AnchorPanel)popUpElement).getOtherEnd().getName().startsWith(startConnectorsNamePrefix)){
//                  deleteAnchor(popUpElement);
//                  deleteAnchor(((AnchorPanel)popUpElement).getOtherEnd());            		
//            	}
//            	if( ( ((AnchorPanel)popUpElement).getOtherEnd().getName().startsWith(orGroupNamePrefix)
//            	   || ((AnchorPanel)popUpElement).getOtherEnd().getName().startsWith(altGroupNamePrefix) )
//            	   && ((GroupPanel)((AnchorPanel)popUpElement).getOtherEnd()).getMembers().size()>2 ){
//
//            		((GroupPanel)((AnchorPanel)popUpElement).getOtherEnd()).getMembers().remove(popUpElement);
//              	    deleteAnchor(popUpElement);
//            	}	
////                deleteAnchor( ((AnchorPanel)popUpElement).getOtherEnd());
//                frameRoot.repaint();
//              }
//              popUpElement=null;
////              diagramElementsMenu.setVisible(false);
//
//            }
//
//          });


//        popMenuItemUngroup.addActionListener(
//          new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//              System.out.println("Detach not implemented yet! ");
//            }
//        });
		
		
//		setExtendedState(MAXIMIZED_BOTH);

//		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
//		device.setFullScreenWindow(this);

//		frameRoot.setSize(Toolkit.getDefaultToolkit().getScreenSize());
//		frameRoot.setLocation(0, 0);

//		frameRoot.repaint();
//		frameRoot.validate();
//		frameRoot.setVisible(true);
		
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//            	frameRoot.setVisible(true);
//            }
//        });
//		diagramPanel.add(diagramElementsMenu);
        return true;
	}

	@Override
	public void paint(java.awt.Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D)g.create();
//		drawAllConnectors(g2);
		
//		paintComponents(g);
//		paintAll(g);
		
		
		/* ***DEBUG*** */
		if(debug3) System.out.println("Mi han chiamato, son la paint()");
		/* ***DEBUG*** */

		if(toolDragImage==null) return;
		
//		   Graphics2D g2 = (Graphics2D) g;
		   g2.drawImage(toolDragImage, toolDragPosition.x+1, toolDragPosition.y+4, null);
//		   g2.finalize();

		    //		BufferedImage image=null;
//		try {
//			image = ImageIO.read(this.getClass().getResourceAsStream(((JPanel)compList[i]).getName()));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		   Graphics2D g2 = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(image);
////		   Graphics2D g2 = (Graphics2D) g;
////
////		    BufferedImage img1 = (BufferedImage) Toolkit.getDefaultToolkit().getImage("yourFile.gif");
//		    g2.drawImage(image, 10, 10, frameRoot);
//		    g2.finalize();

	}

	/**
	 * Draws all connectors lines.
	 * 
	 * @param g2 - the Graphics2D object used for drawing
	 */
	private void drawAllConnectors(Graphics2D g2) {
		//		g2.setStroke();
		g2.setStroke(new BasicStroke(2.5F));  
		g2.setColor(Color.ORANGE);
		JComponent startPanel=null;
		JComponent leftMost = null, rightMost = null;
		int minX=100000, maxX=-100000;
		
		//drawing connectors
		for (int i=0; i< startConnectorDots.size(); ++i){
		  drawConnectionLine(g2, startConnectorDots.get(i), ((AnchorPanel)startConnectorDots.get(i)).getOtherEnd());

		}

//		//drawing connectors
//		for (int i=0; i< connectorDotsToRedraw.size(); ++i){
//		  if (connectorDotsToRedraw.get(i)){
//			drawConnectionLine(g2, startConnectorDots.get(i), endConnectorDots.get(i));
//		  }
//		}

		//drawing Alternative Groups
		drawGroupList(g2, altGroupPanels, false);

		//drawing Or Groups
		drawGroupList(g2, orGroupPanels, true);
	}

	/**
	 * Draws all groups of the specified list, filling the arc if filled is true, drawing only the arc boundary otherwise.
	 * 
	 * @param g2 - the Graphics2D object used for drawing
	 * @param list - the list of groups to be drawn
	 * @param filled - if true, the group arc is drawm as a filled shape, otherwise only the boundary line is drawn  
	 */
	private void drawGroupList(Graphics2D g2, ArrayList<GroupPanel> list,
			boolean filled) {
		JComponent startPanel;
		JComponent leftMost;
		JComponent rightMost;
		int minX;
		int maxX;
		for (int i=0; i< list.size(); ++i){
		  startPanel=list.get(i);
		  leftMost = null; rightMost = null;
		  minX=100000; maxX=-100000;
		  for (JComponent endPanel : ((GroupPanel)startPanel).getMembers()){
			//searching for the 2 extern anchor of the group
			if(endPanel.getLocationOnScreen().getX()<minX){
			  minX=(int)endPanel.getLocationOnScreen().getX();
			  leftMost=endPanel;
			}
			if(endPanel.getLocationOnScreen().getX()>maxX){
				maxX=(int)endPanel.getLocationOnScreen().getX();
				rightMost=endPanel;
			}
			drawConnectionLine(g2, startPanel, endPanel);				
		  }
		  
		  /* ***DEBUG*** */
		  if(debug3) System.out.println(
				  "\nLeftMost:"+leftMost
				  +"\nrightMost:"+rightMost);
		  /* ***DEBUG*** */

		  //drawing thr group arc
		  drawGroupArc(g2, startPanel, leftMost, rightMost, filled);
		}
	}

	/**
	 * Draws the group arc from leftMost to rightMost anchors
	 * 
	 * @param g2 - the Graphics2D object used for drawing
	 * @param startComp - start anchor of the group
	 * @param leftMost - left-most anchor of the group
	 * @param rightMost - right-most anchor of the group
	 * @param filled - if true, the group arc is drawm as a filled shape, otherwise only the boundary line is drawn  
	 */
	private void drawGroupArc(Graphics2D g2, JComponent startComp, JComponent leftMost, JComponent rightMost, boolean filled) {
	  double lineFraction=2.5;
	  double leftX=0, rightX=0, leftY=0, rightY=0;
	  int leftHeight=0, rightHeight=0, leftWidth=0, rightWidth=0, leftLength=0, rightLength=0;
	  int rectangleWidth=0, rectangleHeight=0;
	  Line2D intersectingLine=null;
	  Line2D leftLine=null, rightLine=null;
	  Point2D startCenter=null, leftCenter=null, rightCenter=null;
	  Point2D leftLineIntersectPoint=null, rightLineIntersectPoint=null;
	  List<Point2D> intersectionPoints=null;
	  Arc2D groupArc = null;
	  
	  /* ***DEBUG*** */
	  if (debug3) System.out.println(""
			  +"\nstart: "+startComp
			  +"\nleftMost: "+leftMost
			  +"\nrightMost: "+rightMost
		);
	  /* ***DEBUG*** */
	  
	  if(!startComp.isVisible()) return;

	  Graphics2D tempGraphics = (Graphics2D)g2.create();
	  
	  //getting actual visible center points of components
	  startCenter=getVisibleStartAnchorCenter(startComp);
	  leftCenter=getVisibleStartAnchorCenter(leftMost);
	  rightCenter=getVisibleStartAnchorCenter(rightMost);
	  
	  //getting the lenghts of the two lines
	  leftHeight=(int)(leftCenter.getY()-startCenter.getY());
	  leftWidth=(int)(leftCenter.getX()-startCenter.getX());
	  leftLength=(int)Math.sqrt(leftWidth*leftWidth+leftHeight*leftHeight);
	  rightHeight=(int)(rightCenter.getY()-startCenter.getY());
	  rightWidth=(int)(rightCenter.getX()-startCenter.getX());
	  rightLength=(int)Math.sqrt(rightWidth*rightWidth+rightHeight*rightHeight);

	  //getting the coordinates of the two points at 1/lineFraction line length for the two lines
	  leftX=startCenter.getX()+leftWidth/lineFraction;
	  leftY=startCenter.getY()+leftHeight/lineFraction;
	  rightX=startCenter.getX()+rightWidth/lineFraction;
	  rightY=startCenter.getY()+rightHeight/lineFraction;

	  //creating the lines to calculate the two actual points of the arc
	  leftLine= new Line2D.Double(startCenter, leftCenter);
	  rightLine= new Line2D.Double(startCenter, rightCenter);
	  if(leftHeight<0) leftHeight*=-1;
	  if(rightHeight<0) rightHeight*=-1;
	  //intersecting line depends on the shortest line
	  if(leftLength<rightLength) intersectingLine= new Line2D.Double(-3000000, leftY, +3000000, leftY);
//	  if(leftHeight<rightHeight) intersectingLine= new Line2D.Double(-3000000, leftY, +3000000, leftY);
	  else intersectingLine= new Line2D.Double(-3000000, rightY, +3000000, rightY);
	  
	  //calculating groupArc radius
	  double groupArcRadius = (leftLength<rightLength)? leftLength/lineFraction:rightLength/lineFraction;

	  //calculating actual intersection point of the arc with leftLine
	  intersectionPoints=getCircleLineIntersectionPoints(startCenter, leftCenter, startCenter, groupArcRadius);
	  if(leftLine.ptSegDist(intersectionPoints.get(0))==0) leftLineIntersectPoint=intersectionPoints.get(0);
	  else leftLineIntersectPoint=intersectionPoints.get(1);

	  //calculating actual intersection point of the arc with rightLine
	  intersectionPoints=getCircleLineIntersectionPoints(startCenter, rightCenter, startCenter, groupArcRadius);
	  if(rightLine.ptSegDist(intersectionPoints.get(0))==0) rightLineIntersectPoint=intersectionPoints.get(0);
	  else rightLineIntersectPoint=intersectionPoints.get(1);

	  //calculating the two actual points of the arc
//	  leftIntersectionPoint=getIntersectionPoint(leftLine, intersectingLine);
//	  rightIntersectionPoint=getIntersectionPoint(rightLine, intersectingLine);

	  
	  /* ***DEBUG*** */
	  if (debug3) System.out.println(""
			  +"\nleftIntersectionPoint: "+leftLineIntersectPoint
			  +"\nrightIntersectionPoint: "+rightLineIntersectPoint
		);
	  /* ***DEBUG*** */
	  
	  if(leftLineIntersectPoint==null || rightLineIntersectPoint==null) return;
	  //calculating width and height to draw the arc
	  rectangleWidth=(int)(rightLineIntersectPoint.getX()-leftLineIntersectPoint.getX());
	  rectangleHeight=(int)(leftLineIntersectPoint.getY()-startCenter.getY());
//	  rectangleHeight=(leftHeight>rightHeight)? leftHeight:rightHeight;
	  if (rectangleHeight<0)rectangleHeight*=-1;
//	  Rectangle2D rect2D= new Rectangle2D.Double(			  
//			  (startCenter.getX()-groupArcRadius),
//			  (startCenter.getY()-groupArcRadius),
//			  groupArcRadius*2, groupArcRadius*2);
	  if(!filled) groupArc = new Arc2D.Double(			  
			  (startCenter.getX()-groupArcRadius),
			  (startCenter.getY()-groupArcRadius),
			  groupArcRadius*2, groupArcRadius*2, 0, 360, Arc2D.Double.OPEN);
	  else groupArc = new Arc2D.Double(			  
			  (startCenter.getX()-groupArcRadius),
			  (startCenter.getY()-groupArcRadius),
			  groupArcRadius*2, groupArcRadius*2, 0, 360, Arc2D.Double.PIE);
//	  Arc2D groupArc = new Arc2D.Double(			  
//			  (startCenter.getX()-rectangleHeight),
//			  (startCenter.getY()-rectangleHeight),
//			  rectangleHeight*2, rectangleHeight*2, 0, 360, Arc2D.Double.OPEN);
//	  if(rightLineIntersectPoint.getX()<leftLineIntersectPoint.getX())
//		  groupArc.setAngles(
//				  rightLineIntersectPoint.getX(), rightLineIntersectPoint.getY(), 
//				  leftLineIntersectPoint.getX(), leftLineIntersectPoint.getY());
//	  else groupArc.setAngles(
//				  leftLineIntersectPoint.getX(), leftLineIntersectPoint.getY(),
//				  rightLineIntersectPoint.getX(), rightLineIntersectPoint.getY());

	  groupArc.setAngles(
		  leftLineIntersectPoint.getX(), leftLineIntersectPoint.getY(),
		  rightLineIntersectPoint.getX(), rightLineIntersectPoint.getY());

	  if(groupArc.getAngleExtent()>180) groupArc.setAngles(
		  rightLineIntersectPoint.getX(), rightLineIntersectPoint.getY(), 
		  leftLineIntersectPoint.getX(), leftLineIntersectPoint.getY());
	  
	  if(!filled) tempGraphics.draw(groupArc);
	  else  tempGraphics.fill(groupArc);

//	  tempGraphics.draw(rect2D);
//	  tempGraphics.fill(arco);
//	  tempGraphics.drawArc(
//			  (int)(startPoint.getLocationOnScreen().getX()-splitterPanel.getLocationOnScreen().getX()-rectangleHeight),
//			  (int)(startPoint.getLocationOnScreen().getY()-splitterPanel.getLocationOnScreen().getY()-rectangleHeight),
//			  rectangleHeight*2, rectangleHeight*2, 0, 360);
//	  tempGraphics.setClip(
//			  (int)(leftPoint.getX()-splitterPanel.getLocationOnScreen().getX())-1,
//			  (int)(leftPoint.getY()-splitterPanel.getLocationOnScreen().getY())-1,
//			  rectangleWidth+6, rectangleHeight);
//	  tempGraphics.drawArc(
//			  (int)(leftPoint.getX()-splitterPanel.getLocationOnScreen().getX()),
//			  (int)(leftPoint.getY()-splitterPanel.getLocationOnScreen().getY()-rectangleHeight),
//			  rectangleWidth, rectangleHeight*2, 0, 360);
////	  tempGraphics.drawRect(25, 25, 240, 120);
//	  tempGraphics.setColor(Color.RED);
//	  tempGraphics.fillOval((int)leftPoint.getX()-2, (int)leftPoint.getY()-2, 7, 7);
//	  tempGraphics.fillOval((int)rightPoint.getX()-2, (int)rightPoint.getY()-2, 7, 7);
	}

	private static void drawConnectionLine(Graphics2D g2, JComponent startPanel, JComponent endPanel) {
		start.setLocation(getVisibleStartAnchorCenter(startPanel));
		endLeft.setLocation(getVisibleStartAnchorCenter(endPanel));
//		start.setLocation(startPanel.getLocationOnScreen());
//		end.setLocation(endPanel.getLocationOnScreen());
		g2.drawLine(
//				  (int)(start.getX()-splitterPanel.getLocationOnScreen().getX()+startPanel.getWidth()/2),
//				  (int)(start.getY()-splitterPanel.getLocationOnScreen().getY()+startPanel.getHeight()/2+3),
//				  (int)(end.getX()-splitterPanel.getLocationOnScreen().getX()+endPanel.getHeight()/2),
//				  (int)(end.getY()-splitterPanel.getLocationOnScreen().getY()+endPanel.getHeight()/2+3) );
		  (int)start.getX(), (int)start.getY(), (int)endLeft.getX(), (int)endLeft.getY() );
//		  (int)(end.getX()-splitterPanel.getLocationOnScreen().getX()+endPanel.getHeight()/2-3),
//		  (int)(end.getY()-splitterPanel.getLocationOnScreen().getY()+endPanel.getHeight()/2+2) );
	};
	
	/**
     * Returns a Point2D representing the visible center of a starting anchor image on the splitterPanel coordinates system.
     * 
     * @param anchor - the JComponent representing a visible starting anchor
     * @return the visible center point of the anchor
     */
    public static Point2D getVisibleStartAnchorCenter(JComponent anchor) {
    	double x=(anchor.getLocationOnScreen().getX()-splitterPanel.getLocationOnScreen().getX()+anchor.getWidth()/2);
    	double y=(anchor.getLocationOnScreen().getY()-splitterPanel.getLocationOnScreen().getY()+anchor.getHeight()/2+3);
    	
    	return new Point2D.Double(x, y);
    }
    
//	/**
//     * Returns a Point2D representing the visible center of an ending anchor image on the splitterPanel coordinates system.
//     * 
//     * @param anchor - the JComponent representing a visible ending anchor
//     * @return the visible center point of the anchor
//     */
//    public static Point2D getVisibleEndAnchorCenter(JComponent anchor) {
//    	double x=(anchor.getLocationOnScreen().getX()-splitterPanel.getLocationOnScreen().getX()+anchor.getWidth()/2-3);
//    	double y=(anchor.getLocationOnScreen().getY()-splitterPanel.getLocationOnScreen().getY()+anchor.getHeight()/2+2);
//    	
//    	return new Point2D.Double(x, y);
//    }
	
    /**
     * 
     * @param pointA
     * @param pointB
     * @param center
     * @param radius
     * @return
     */
    public static List<Point2D> getCircleLineIntersectionPoints(Point2D pointA, Point2D pointB, Point2D center, double radius) {
        double baX = pointB.getX() - pointA.getX();
        double baY = pointB.getY() - pointA.getY();
        double caX = center.getX() - pointA.getX();
        double caY = center.getY() - pointA.getY();

        double a = baX * baX + baY * baY;
        double bBy2 = baX * caX + baY * caY;
        double c = caX * caX + caY * caY - radius * radius;

        double pBy2 = bBy2 / a;
        double q = c / a;

        double disc = pBy2 * pBy2 - q;
        if (disc < 0) {
            return Collections.emptyList();
        }
        // if disc == 0 ... dealt with later
        double tmpSqrt = Math.sqrt(disc);
        double abScalingFactor1 = -pBy2 + tmpSqrt;
        double abScalingFactor2 = -pBy2 - tmpSqrt;

        Point2D p1 = new Point2D.Double(pointA.getX() - baX * abScalingFactor1, pointA.getY()
                - baY * abScalingFactor1);
        if (disc == 0) { // abScalingFactor1 == abScalingFactor2
            return Collections.singletonList(p1);
        }
        Point2D p2 = new Point2D.Double(pointA.getX() - baX * abScalingFactor2, pointA.getY()
                - baY * abScalingFactor2);
        return Arrays.asList(p1, p2);
    }
    
    /**
     * Returns a Point2D representing the intersection point of lineA and lineB, or null if they're parallel to each other.
     * @param lineA - line A
     * @param lineB - line B
     * @return the intersection point of lineA and lineB, if any, null otherwise
     */
    public Point2D getIntersectionPoint(Line2D lineA, Line2D lineB) {

//        int x1 = (int)lineA.getX1();
//        int y1 = (int)lineA.getY1();
//        int x2 = (int)lineA.getX2();
//        int y2 = (int)lineA.getY2();
//
//        int x3 = (int)lineB.getX1();
//        int y3 = (int)lineB.getY1();
//        int x4 = (int)lineB.getX2();
//        int y4 = (int)lineB.getY2();

        double x1 = lineA.getX1();
        double y1 = lineA.getY1();
        double x2 = lineA.getX2();
        double y2 = lineA.getY2();

        double x3 = lineB.getX1();
        double y3 = lineB.getY1();
        double x4 = lineB.getX2();
        double y4 = lineB.getY2();

        Point2D p = null;
        
//        int d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d != 0) {//RETTE NON PARALLELE
//        	int xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
//            int yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
            double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
            double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

            p = new Point2D.Double(xi, yi);

        }

        /* ***DEBUG*** */
        if (debug2) System.out.println("Line1=(("+x1+"."+y1+"), ("+x2+"."+y2+"))"
        							+"\nLine2=(("+x3+"."+y3+"), ("+x4+"."+y4+"))"
        							+"\np="+p);
        /* ***DEBUG*** */

        return p;
    }
	
	/**
	 * Returns a JComponent named name and containing the corresponding icon image, <br>
	 * the method call setOpaque(backgroundVisible) on the panel containig the icon.
	 * 
	 * @param name - the name of the new JComponent
	 * @param backgroundVisible - if true the panel will be opaque, otherwise it will be transparent.
	 * @return - the new JComponent with the icon, or null if a problem occurrs.
	 */
	private static JComponent getToolIcon(String name, boolean backgroundVisible) {
		JComponent iconPanel=null;
		ImageIcon toolImage = getIconImage(name);
		
		iconPanel= new JPanel();
		iconPanel.add(new JLabel(toolImage));

		iconPanel.setBounds(0, 0, toolImage.getIconWidth(), toolImage.getIconHeight());

		iconPanel.setOpaque(backgroundVisible);
		iconPanel.setBackground(Color.LIGHT_GRAY);
		iconPanel.setName(name);
		iconPanel.setToolTipText(name);
		  
		return iconPanel;
	}

	/**
	 * Returns an ImageIcon named name containing the image at the path given by toolIconPaths map.
	 * 
	 * @param name - the name of the new ImageIcon
	 * @return - the new ImageIcon, or null if a problem occurrs.
	 */
	private static ImageIcon getIconImage(String name) {
		ImageIcon toolImage=null;
		
		try{
			toolImage = new ImageIcon(EditorView.class.getResource(toolIconPaths.get(name)));	
			toolImage.setDescription(name);
			/* ***DEBUG*** */
			if (debug4) System.out.println("toolImage.getDescription()"+toolImage.getDescription());
			/* ***DEBUG*** */

		}catch(Exception e){
			System.out.println("Eccezione "+e.getMessage());
			e.printStackTrace();
			return null;
		}
		return toolImage;
	}

	
	private static MouseListener getToolbarMouseListener() {
	  MouseListener listener= new MouseListener() {

		@Override
		public void mouseClicked(MouseEvent e) {
			System.out.println("Source e: "+e.getSource());
			System.out.println("Source e.getName(): "+((Component)e.getSource()).getName());
//			if(popUpElement!=null){
//			  System.out.println("clicked! popupElement: "+popUpElement);
//			  diagramElementsMenu.show(diagramPanel, diagramElementsMenuPosX, diagramElementsMenuPosY);
//			  return;
////			  popUpElement=null;				  
////			  eventsRobot.mousePress(InputEvent.BUTTON1_MASK);
////			  diagramElementsMenu.menuSelectionChanged(false);
//			}

//			if(popUpElement!=null) return;

			/* ***DEBUG*** */
			if (debug3) System.out.println("mouse clicked on a tool, Divider at: "+splitterPanel.getDividerLocation());
			/* ***DEBUG*** */
		}

		@Override
		public void mousePressed(MouseEvent e) {
//		  if(popUpElement!=null){
//			System.out.println("popupElement: "+popUpElement);
//			diagramElementsMenu.show(diagramPanel, diagramElementsMenuPosX, diagramElementsMenuPosY);
//			return;
////			popUpElement=null;				  
////			eventsRobot.mousePress(InputEvent.BUTTON1_MASK);
////			diagramElementsMenu.menuSelectionChanged(false);
//		  }

//		  if(popUpElement!=null) return;

		  diagramElementsMenu.setVisible(false);
		  diagramElementsMenu.setEnabled(false);
//		  lastPositionX=(int)e.getLocationOnScreen().getX();
//		  lastPositionY=(int)e.getLocationOnScreen().getY();
			  lastPositionX=e.getX();
			  lastPositionY=e.getY();
		  

		  Component[] compList=toolsPanel.getComponents();
		  JComponent comp=(JComponent)e.getSource();
		  JComponent imageLabel=(JComponent)comp.getComponent(0);
//		  JComponent imageLabel=comp.getComponentAt(new Point(e.getX(), e.getY()));
		  
		  /* ***DEBUG*** */
		  if(debug4) System.out.println("e.getSource(): "+e.getSource()+"\nimageLabel: "+imageLabel);
		  /* ***DEBUG*** */

		  try {
			toolDragImage = ImageIO.read(this.getClass().getResourceAsStream(toolIconPaths.get(((JComponent)comp).getName())));
			toolDragPosition= new Point((int)imageLabel.getLocationOnScreen().getX(),
					(int)imageLabel.getLocationOnScreen().getY());
//			toolDragPosition= new Point((int)((JPanel)comp).getLocationOnScreen().getX(),
//					(int)((JPanel)comp).getLocationOnScreen().getY());
			
		  } catch (IOException e2) {
			System.out.println("toolDragImage is null");
			e2.printStackTrace();
			return;
		  }
		  if (((JComponent)comp).getName()=="New Feature")
			  isActiveItem=activeItems.DRAGGING_TOOL_NEWFEATURE;
		  else if (((JComponent)comp).getName()=="Connector Line")
			  isActiveItem=activeItems.DRAGGING_TOOL_CONNECTOR;
		  else if (((JComponent)comp).getName()=="Alternative Group")
			  isActiveItem=activeItems.DRAGGING_TOOL_ALT_GROUP;
		  else if (((JComponent)comp).getName()=="Or Group")
			  isActiveItem=activeItems.DRAGGING_TOOL_OR_GROUP;

		  frameRoot.repaint();
			
		  /* ***DEBUG*** */
		  if (debug) System.out.println("mousePressed on: "+toolDragPosition);
		  /* ***DEBUG*** */

		  /* ***DEBUG*** */
		  if (debug3) System.out.println("mousePressed, components are "+compList.length);
		  /* ***DEBUG*** */

//		  for(int i=0; i< compList.length; ++i){
//
//			  /* ***DEBUG*** */
//			  if (debug3) System.out.println("mousePressed: iteration "+i+"...");
//			  /* ***DEBUG*** */
//
//			  if (compList[i].getBounds().contains(e.getX(), e.getY())){
//				try {
////					  toolDragImage = ImageIO.read(this.getClass().getResourceAsStream(toolIconPaths.get(((JPanel)compList[i]).getName())));
//
//					  BufferedImage buffy = ImageIO.read(this.getClass().getResourceAsStream(toolIconPaths.get(((JPanel)compList[i]).getName())));
//					  GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
//					  GraphicsDevice device = env.getDefaultScreenDevice();
//					  GraphicsConfiguration config = device.getDefaultConfiguration();
//					  toolDragImage = config.createCompatibleImage(buffy.getWidth(), buffy.getHeight(), Transparency.TRANSLUCENT);
//					  toolDragImage.setData(buffy.getData());
//
//				  /* ***DEBUG*** */
//				  if (debug3) System.out.println("name: "+((JPanel)compList[i]).getName()
//						  +"\nURL: "+this.getClass().getResourceAsStream(toolIconPaths.get(((JPanel)compList[i]).getName()))
//						  +"\ntoolDragImage="+toolDragImage);
//				  /* ***DEBUG*** */
//
//				  //						toolDragPosition= new Point(e.getX(), e.getY());
////				  toolDragPosition= new Point((int)MouseInfo.getPointerInfo().getLocation().getX(), 
////						  (int)MouseInfo.getPointerInfo().getLocation().getY());
//				  
//				  toolDragPosition= new Point((int)((JPanel)compList[i]).getLocationOnScreen().getX(),
//						  (int)((JPanel)compList[i]).getLocationOnScreen().getY());
//				} catch (IOException e1) {
//					System.out.println("toolDragImage is null");
//					e1.printStackTrace();
//					return;
//				}
//
//				//					splitterPanel.getParent().repaint();
//
//				if (((JPanel)compList[i]).getName()=="New Feature")
//					isActiveItem=activeItems.DRAGGING_TOOL_NEWFEATURE;
//				else if (((JPanel)compList[i]).getName()=="Connector Line")
//					isActiveItem=activeItems.DRAGGING_TOOL_CONNECTOR;
//
//
//				frameRoot.repaint();
//				
//				/* ***DEBUG****/
//				if(debug3){
//				  System.out.println("splitterPanel.getParent(): "+splitterPanel.getParent());
//				  System.out.println("splitterPanel.getParent().isLightweight()? "+splitterPanel.getParent().isLightweight());
//				  System.out.println("splitterPanel.getParent().getClass(): "+splitterPanel.getParent().getClass());
//				  System.out.println("splitterPanel.getParent().getParent().getClass(): "+splitterPanel.getParent().getParent().getClass());
//				  System.out.println("MyDraggableImages.class: "+MyDraggableImages.class);
//				}
//				/* ***DEBUG****/
//
//				//					   Graphics2D g2 = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(image);
//				////					   Graphics2D g2 = (Graphics2D) g;
//				//			//
//				////					    BufferedImage img1 = (BufferedImage) Toolkit.getDefaultToolkit().getImage("yourFile.gif");
//				//					    g2.drawImage(image, 10, 10, frameRoot);
//				//					    g2.finalize();
//
//
//
//				//				  toolImage=getIconImage(((JPanel)compList[i]).getName());
//				////				  toolDragPanel= getToolIcon(((JPanel)compList[i]).getName(), false);
//				//				  toolDragPanel= new JFrame();
//				//				  
//				//				  toolDragPanel.add(getToolIcon(((JPanel)compList[i]).getName(), false));
//				//				  toolDragPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//				////				  toolDragPanel.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2,
//				////					  Toolkit.getDefaultToolkit().getScreenSize().height/2);
//				////				  toolDragPanel.setLocation(((JPanel)compList[i]).getLocationOnScreen());
//				//				  toolDragPanel.setLocation((int)MouseInfo.getPointerInfo().getLocation().getX()-5, 
//				//						  (int)MouseInfo.getPointerInfo().getLocation().getY()-5);
//				//				  
//				////				  toolDragPanel.setSize(((JPanel)compList[i]).getSize());
//				//				  toolDragPanel.setSize(toolImage.getIconWidth(), toolImage.getIconHeight());
//				//				  toolDragPanel.setUndecorated(true);
//				//				  toolDragPanel.setVisible(true);
//
//				/* ***DEBUG*** */
//				if (debug3) System.out.println("Mouse pressed on: "+((JPanel)compList[i]).getName()
//						+""+(((JLabel)((JPanel)compList[i]).getComponent(0))).toString());
//				/* ***DEBUG*** */
//
//
//				break;
//			  }
//		  }
		}

		@Override
		public void mouseReleased(MouseEvent e) {
//		  if(popUpElement!=null){
//			System.out.println("popupElement: "+popUpElement);
//			diagramElementsMenu.show(diagramPanel, diagramElementsMenuPosX, diagramElementsMenuPosY);
//			return;
////			popUpElement=null;				  
////			eventsRobot.mousePress(InputEvent.BUTTON1_MASK);
////			diagramElementsMenu.menuSelectionChanged(false);
//		  }
			
//		  if(popUpElement!=null) return;

		  switch(isActiveItem){
		    case DRAGGING_TOOL_NEWFEATURE:
//		    	addNewFeatureToDiagram(e);
		    	isActiveItem=activeItems.NO_ACTIVE_ITEM;
		    	toolDragImage=null; break;
		    case DRAGGING_TOOL_CONNECTOR:
		    	addConnectorToDiagram(e);
		    	isActiveItem=activeItems.NO_ACTIVE_ITEM;
		    	toolDragImage=null; break;
		    case DRAGGING_TOOL_ALT_GROUP:
		    	addAltGroupToDiagram(e);
		    	isActiveItem=activeItems.NO_ACTIVE_ITEM;
		    	toolDragImage=null; break;
		    case DRAGGING_TOOL_OR_GROUP:
		    	addOrGroupToDiagram(e);
		    	isActiveItem=activeItems.NO_ACTIVE_ITEM;
		    	toolDragImage=null; break;
		    default: break;
		  }
		}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

	  };
	  return listener;
	}
			
	private static MouseMotionListener getToolbarMouseMotionListener() {
		MouseMotionListener list= new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
			  
			  /* ***DEBUG*** */
			  if(debug3) System.out.println("Evento Drag, isActiveItem= "+isActiveItem);
			  /* ***DEBUG*** */

			  switch(isActiveItem){
			    case DRAGGING_TOOL_NEWFEATURE: dragToolNewFeature(e); break;
			    case DRAGGING_TOOL_CONNECTOR: dragToolConnector(e); break;
			    case DRAGGING_TOOL_ALT_GROUP: dragToolAltGroup(e); break;
			    case DRAGGING_TOOL_OR_GROUP: dragToolOrGroup(e); break;
			    default: break;
			  }			  
			}


			@Override
			public void mouseMoved(MouseEvent e) {}
			
		};
		
		return list;
	}
		
	/**
	 * Return the MouseMotionListener for the diagram panel.
	 * 
	 * @return list - the listener.
	 */
	private static MouseMotionListener getDiagramMouseMotionListener() {
		MouseMotionListener list= new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {}
			
			@Override
			public void mouseDragged(MouseEvent e) {
			  switch(isActiveItem){
			    case DRAGGING_FEATURE: dragFeature(e); break;
			    case DRAGGING_EXTERN_ANCHOR: dragAnchor(e); break;
			    case DRAGGING_EXTERN_GROUP: dragAnchor(e); break;
			    default: break;
			  }			  
			}

		};
		
		return list;
	}

	private static MouseListener getDiagramMouseListener() {
		MouseListener listener= new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
//			  if(popUpElement!=null){
////				eventsRobot.mousePress(InputEvent.BUTTON1_MASK);
//				popUpElement=null;				  
//				diagramElementsMenu.removeAll();
//				diagramElementsMenu.menuSelectionChanged(false);
//			  }
//			  if (e.getButton() == MouseEvent.BUTTON3) {
//				//if (e.getButton() == MouseEvent.BUTTON1) {
//				diagramElementsMenu.show(diagramPanel, e.getX(), e.getY());
//				//                    diagramElementsMenu.show(e.getComponent(), e.getX(), e.getY());
//				return;
//			  }

			  switch(isActiveItem){
			    case DRAGGING_FEATURE:
			      isActiveItem=activeItems.NO_ACTIVE_ITEM;
			      lastFeatureFocused=null; break;
			    case DRAGGING_EXTERN_ANCHOR:
				      dropAnchorOnDiagram(e);
				      isActiveItem=activeItems.NO_ACTIVE_ITEM;
				      lastAnchorFocused=null; break;
			    case DRAGGING_EXTERN_GROUP:
				      dropGroupOnDiagram(e);
				      isActiveItem=activeItems.NO_ACTIVE_ITEM;
				      lastAnchorFocused=null; break;
			    default: break;
			  }
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
//			  if(popUpElement!=null){
////				eventsRobot.mousePress(InputEvent.BUTTON1_MASK);
//				popUpElement=null;				  
//				diagramElementsMenu.removeAll();
//				diagramElementsMenu.menuSelectionChanged(false);
//			  }
			  int featurePanelX=0, featurePanelY=0;
			  int anchorPanelOnScreenX=0, anchorPanelOnScreenY=0;
			  FeaturePanel featurePanel=null;
			  JComponent anchorPanel=null;
			  String anchorPanelName;
			  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
			  while(tmpNode!=null){
				if (((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY())){
				  lastPositionX=e.getX();
				  lastPositionY=e.getY();

				  //mouse pressed on a feature panel in the diagram panel
				  if(tmpNode.getElement().getClass().equals(FeaturePanel.class) &&
					 ((FeaturePanel)tmpNode.getElement()).getName().startsWith(featureNamePrefix) ){
					  
					featurePanel=(FeaturePanel)tmpNode.getElement();
					featurePanelX=featurePanel.getX();
					featurePanelY=featurePanel.getY();
					anchorPanel=(JComponent)featurePanel.getComponentAt(e.getX()-featurePanelX, e.getY()-featurePanelY);
					anchorPanelName=anchorPanel.getName();
					//mouse pressed on an anchor inside the feature panel
					if(anchorPanelName!=null && anchorPanel.getClass().equals(AnchorPanel.class) &&(
					   anchorPanelName.startsWith(startConnectorsNamePrefix) ||
					   anchorPanelName.startsWith(endConnectorsNamePrefix) ) ){
						
					  isActiveItem=activeItems.DRAGGING_EXTERN_ANCHOR;
					  lastAnchorFocused=(JComponent)anchorPanel;

					  anchorPanelOnScreenX=(int)lastAnchorFocused.getLocationOnScreen().getX();
					  anchorPanelOnScreenY=(int)lastAnchorFocused.getLocationOnScreen().getY();
					  featurePanel.remove(lastAnchorFocused);
					  featurePanel.validate();
					  lastAnchorFocused.setLocation((int)(anchorPanelOnScreenX-diagramPanel.getLocationOnScreen().getX()),
						(int)(anchorPanelOnScreenY-diagramPanel.getLocationOnScreen().getY()));
					  diagramPanel.setLayer(lastAnchorFocused, 0);
					  diagramPanel.add(lastAnchorFocused);
					  diagramPanel.setComponentZOrder(lastAnchorFocused, 0);
					  moveComponentToTop(lastAnchorFocused);
					  break;
					}
					//mouse pressed on a group inside the feature panel
					else if(anchorPanelName!=null && anchorPanel.getClass().equals(GroupPanel.class) && (
					   anchorPanelName.startsWith(altGroupNamePrefix) ||
					   anchorPanelName.startsWith(orGroupNamePrefix) ) ){
						
					  isActiveItem=activeItems.DRAGGING_EXTERN_GROUP;
					  lastAnchorFocused=(JComponent)anchorPanel;

					  anchorPanelOnScreenX=(int)lastAnchorFocused.getLocationOnScreen().getX();
					  anchorPanelOnScreenY=(int)lastAnchorFocused.getLocationOnScreen().getY();
					  featurePanel.remove(lastAnchorFocused);
					  featurePanel.validate();
					  lastAnchorFocused.setLocation((int)(anchorPanelOnScreenX-diagramPanel.getLocationOnScreen().getX()),
						(int)(anchorPanelOnScreenY-diagramPanel.getLocationOnScreen().getY()));
					  diagramPanel.setLayer(lastAnchorFocused, 0);
					  diagramPanel.add(lastAnchorFocused);
					  diagramPanel.setComponentZOrder(lastAnchorFocused, 0);
					  moveComponentToTop(lastAnchorFocused);
					  break;
					}
					//mouse directly pressed on a feature panel, not on an inner anchor
					isActiveItem=activeItems.DRAGGING_FEATURE;
					lastFeatureFocused=(FeaturePanel)((Component)tmpNode.getElement());   
					moveComponentToTop(lastFeatureFocused);
				  }
				  //mouse pressed on an anchor panel in the diagram panel
				  else if(tmpNode.getElement().getClass().equals(AnchorPanel.class) &&(
						  ((AnchorPanel)tmpNode.getElement()).getName().startsWith(startConnectorsNamePrefix) ||
						  ((AnchorPanel)tmpNode.getElement()).getName().startsWith(endConnectorsNamePrefix) ) ){
					isActiveItem=activeItems.DRAGGING_EXTERN_ANCHOR;
					lastAnchorFocused=(AnchorPanel)((Component)tmpNode.getElement());
					moveComponentToTop(lastAnchorFocused);
				  }
				  //mouse pressed on a group panel in the diagram panel
				  else if(tmpNode.getElement().getClass().equals(GroupPanel.class) &&
						  //lastAnchorFocused?
						  ((GroupPanel)tmpNode.getElement()).getName().startsWith(altGroupNamePrefix) ||
						  ((GroupPanel)tmpNode.getElement()).getName().startsWith(orGroupNamePrefix) ){
					isActiveItem=activeItems.DRAGGING_EXTERN_GROUP;
					lastAnchorFocused=(GroupPanel)((Component)tmpNode.getElement());
					moveComponentToTop(lastAnchorFocused);
				  }

				  /* ***DEBUG*** */
				  if (debug2){
					  System.out.println("Source dell'evento: "+e.getSource());
					  OrderedListNode printTmp=visibleOrderDraggables.getFirst();
					  System.out.println("Stampo l'ordine attuale nella lista, partendo da first.");
					  while(printTmp!=null){
						  System.out.println("-"+((Component)printTmp.getElement()).getName());							  
						  printTmp=printTmp.getNext();
					  }
				  }
				  /* ***DEBUG*** */

				  break;
				}
				
				/* ***DEBUG*** */
				if (debug2){
				  System.out.println("Current Panel: "+((Component)tmpNode.getElement()).getName());
				}
				/* ***DEBUG*** */
				
				tmpNode=tmpNode.getNext();
			  }
			}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {

			    /* ***DEBUG *** */
				if (debug2){
				  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
				  while(tmpNode!=null){
					if (((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY())){
					  System.out.println("Sei passato su "+((Component)tmpNode.getElement()).getName()+"!");
					  return;
					}
					tmpNode=tmpNode.getNext();
				  }
				}
				/* ***DEBUG *** */

			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("Button pressed: "+e.getButton());
				System.out.println("Source e: "+e.getSource());
				System.out.println("Source e.getName(): "+((Component)e.getSource()).getName());
            	diagramElementsMenu.removeAll();
//				diagramElementsMenu.setEnabled(false);
                if (e.getButton() == MouseEvent.BUTTON3) {
//                	toolsPanel.setRequestFocusEnabled(false);
//                	toolsPanel.setFocusable(false);
//                	toolsPanel.setEnabled(false);
                	Component comp=diagramPanel.getComponentAt(e.getX(), e.getY());
                	
                	/* ***DEBUG*** */
                	if(debug3) System.out.println("rigth clicked on: "+comp.getName());
                	/* ***DEBUG*** */

                	if(comp.getName()==null || comp.getName()==""|| comp.getName().startsWith(diagramPanelName)) return;

                	popUpElement=(JComponent)comp;
//                	popUpElement=getUnderlyingComponent(e.getX(), e.getY());

                	if(popUpElement.getName().startsWith(startConnectorsNamePrefix)
                     	   || popUpElement.getName().startsWith(endConnectorsNamePrefix)){
                      diagramElementsMenu.add(popMenuItemDelete);
                      diagramElementsMenu.add(popMenuItemUngroup);
                    }
                	if(popUpElement.getName().startsWith(featureNamePrefix)){
                	  diagramElementsMenu.add(popMenuItemDelete);
                   	}
                	
                	diagramElementsMenuPosX=e.getX();
                	diagramElementsMenuPosY=e.getY();
                	diagramElementsMenu.show(diagramPanel, diagramElementsMenuPosX, diagramElementsMenuPosY);
//                	diagramElementsMenu.show(diagramPanel.getComponentAt(e.getX(), e.getY()),
//                		e.getX()-diagramPanel.getComponentAt(e.getX(), e.getY()).getX(),
//                		e.getY()-diagramPanel.getComponentAt(e.getX(), e.getY()).getY());
                }

				/* ***DEBUG *** */
				if(debug) System.out.println("splitterPanel.getDividerLocation(): "+splitterPanel.getDividerLocation());
				/* ***DEBUG *** */

				  
				/* ***DEBUG *** */
				if (!debug) return;
				OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
				while(tmpNode!=null){
					if (((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY())){
						System.out.println("Hai cliccato su "+((Component)tmpNode.getElement()).getName()+"!"+
							"\nLocation: "+((Component)tmpNode.getElement()).getLocationOnScreen()+"!");						
						return;
					}
					tmpNode=tmpNode.getNext();
				}
				/* ***DEBUG *** */

			}
		};
		
		return listener;
	}

	/**
	 * Moves a component in the diagram panel to the top layer.
	 * 
	 * @param comp - the component to move to top.
	 */
	public static void moveComponentToTop(JComponent comp) {
		int currentLayer=diagramPanel.getComponentZOrder(comp);

		  /* ***DEBUG*** */
		  if (debug) System.out.println("Clicked: "+comp.getName()+"\tLayer: "+currentLayer);
		  /* ***DEBUG*** */

		  if(currentLayer>0){//switching layers with top component

			/* ***DEBUG*** */
			if (debug) System.out.println("Switching to Layer 0");
			/* ***DEBUG*** */

			diagramPanel.setComponentZOrder(comp, 0);
			visibleOrderDraggables.moveToTop(comp);
//			diagramPanel.repaint();
			frameRoot.repaint();
		  }
	}

	/**
	 * Drags an anchor panel inside the diagram panel.
	 *
	 * @param e - the current MouseEvent
	 */
	public static void dragAnchor(MouseEvent e) {
	  if(lastAnchorFocused==null) return;
	  dragDiagramElement(lastAnchorFocused, e);
//		  diagramPanel.repaint();
	  frameRoot.repaint();	}

	/**
	 * Drags a feature panel inside the diagram panel.
	 *
	 * @param e - the current MouseEvent
	 */
	public static void dragFeature(MouseEvent e) {
	  if(lastFeatureFocused==null) return;
	  dragDiagramElement(lastFeatureFocused, e);
//	  diagramPanel.repaint();
	  frameRoot.repaint();
	}

	/**
	 * Drags an element inside the diagram panel.
	 *
	 * @param element - the element to drag
	 * @param e - the current MouseEvent
	 */
	private static void dragDiagramElement(JComponent element, MouseEvent e) {
		  int moveX=0, moveY=0;
		  int adjustedMoveX=0, adjustedMoveY=0;	  
		  int newLocationX=0, newLocationY=0;
		  boolean normalUpdateX=true, normalUpdateY=true;

		  moveX = e.getX()-lastPositionX;
		  moveY = e.getY()-lastPositionY;
		  newLocationX=element.getX()+moveX;
		  newLocationY=element.getY()+moveY;
		  
		  //the feature must not be dragged beyond the borders of the diagram panel
		  if( newLocationX<0 ){
//		  if( diagramPanel.getLocation().getX()>newLocationX ){
//			newLocationX=(int)diagramPanel.getLocation().getX()+1;
			newLocationX=1;
			normalUpdateX=false;
			adjustedMoveX=newLocationX-element.getX();
		  }
		  if( diagramPanel.getWidth()<=newLocationX+element.getWidth() ){
//		  if( diagramPanel.getLocation().getX()+diagramPanel.getWidth()<=newLocationX+lastFeatureFocused.getWidth() ){
			newLocationX=diagramPanel.getWidth()-element.getWidth()-1;
//			newLocationX=(int)diagramPanel.getLocation().getX()+diagramPanel.getWidth()-lastFeatureFocused.getWidth()-1;
			normalUpdateX=false;
			adjustedMoveX=newLocationX-element.getX();
		  }
		  if( newLocationY<0 ){
//		  if( diagramPanel.getLocation().getY()>newLocationY ){
//			newLocationY=(int)diagramPanel.getLocation().getY()+1;
			newLocationY=1;
			normalUpdateY=false;
			adjustedMoveY=newLocationY-element.getY();
		  }
		  if( diagramPanel.getHeight()<=newLocationY+element.getHeight() ){
//		  if( diagramPanel.getLocation().getY()+diagramPanel.getHeight()<=newLocationY+lastFeatureFocused.getHeight() ){
			newLocationY=diagramPanel.getHeight()-element.getHeight()-1;
//			newLocationY=(int)diagramPanel.getLocation().getY()+diagramPanel.getHeight()-lastFeatureFocused.getHeight()-1;
			normalUpdateY=false;
			adjustedMoveY=newLocationY-element.getY();
		  }

		  /* ***DEBUG*** */
		  if (debug4){
			System.out.println("oldPosX: "+lastPositionX+"\toldPosY: "+lastPositionY);
			System.out.println("newPosX: "+e.getX()+"\tnewPosY: "+e.getY());
			System.out.println("moveX: "+moveX+"\tmoveY: "+moveY);
		  }
		  /* ***DEBUG*** */

		  //adjusting last drag position depending on eventual border collisions
		  if(normalUpdateX) lastPositionX=e.getX();
		  else lastPositionX=lastPositionX+adjustedMoveX;

		  if(normalUpdateY) lastPositionY=e.getY();
		  else lastPositionY=lastPositionY+adjustedMoveY;

		  element.setLocation(newLocationX, newLocationY);
	}

	/**
	 * Drags a Connector tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	public static void dragToolConnector(MouseEvent e) {
		  dragTool(e);
	}

	/**
	 * Drags a NewFeature tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	public static void dragToolNewFeature(MouseEvent e) {
		  dragTool(e);
	}

	/**
	 * Drags an Alternative Group tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	public static void dragToolAltGroup(MouseEvent e) {
		  dragTool(e);
	}
	
	/**
	 * Drags an Or Group tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	public static void dragToolOrGroup(MouseEvent e) {
		  dragTool(e);
	}
	
	
	/**
	 * Drags a generic tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	private static void dragTool(MouseEvent e) {
		int moveX=0, moveY=0;

//		  moveX = (int)e.getLocationOnScreen().getX()-lastPositionX;
//		  moveY = (int)e.getLocationOnScreen().getY()-lastPositionY;
		  moveX = e.getX()-lastPositionX;
		  moveY = e.getY()-lastPositionY;
		  
		  /* ***DEBUG*** */
		  if (debug4){
			  System.out.println("oldPosX: "+lastPositionX+"\toldPosY: "+lastPositionY);
			  System.out.println("newPosX: "+e.getX()+"\tnewPosY: "+e.getY());
			  System.out.println("moveX: "+moveX+"\tmoveY: "+moveY);
		  }
		  /* ***DEBUG*** */
		  
//		  lastPositionX=(int)e.getLocationOnScreen().getX();
//		  lastPositionY=(int)e.getLocationOnScreen().getY();
		  lastPositionX=e.getX();
		  lastPositionY=e.getY();

//		  toolDragPosition=new Point(toolDragPosition.x+moveX, toolDragPosition.y+moveY);
		  toolDragPosition.setLocation(toolDragPosition.x+moveX, toolDragPosition.y+moveY);
		  frameRoot.repaint();
	}
	
	/**
	 * Drops an anchor on the diagram panel, adding it to the underlying feature panel, if any is present.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	public static Component dropAnchorOnDiagram(MouseEvent e) {
	  FeaturePanel underlyingPanel=null;
	  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
	  while(tmpNode!=null){	  
		if (((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY()) ){	
//		  if (((Component)tmpNode.getElement()).getName().startsWith(featureNamePrefix)){
//			underlyingPanel=(FeaturePanel)tmpNode.getElement();
//			addAnchorToFeature(lastAnchorFocused, underlyingPanel);
//			frameRoot.repaint();
//			break;					  
//		  }
//		  if (lastAnchorFocused.getName().startsWith(startConnectorsNamePrefix) && 
//			   (  ((Component)tmpNode.getElement()).getName().startsWith(altGroupNamePrefix)
//			    || ((Component)tmpNode.getElement()).getName().startsWith(orGroupNamePrefix)) ){
//			addStartAnchorToGroup((AnchorPanel)lastAnchorFocused, (GroupPanel)tmpNode.getElement());
//			frameRoot.repaint();
//			break;					  
//		  }
			
		  if (((Component)tmpNode.getElement()).getName().startsWith(featureNamePrefix)){
//			underlyingPanel=(FeaturePanel)tmpNode.getElement();
			underlyingComponent=(JComponent)tmpNode.getElement();
//			addAnchorToFeature(lastAnchorFocused, underlyingPanel);
//			frameRoot.repaint();
			return underlyingComponent;					  
		  }
		  if (lastAnchorFocused.getName().startsWith(startConnectorsNamePrefix) && 
			   (  ((Component)tmpNode.getElement()).getName().startsWith(altGroupNamePrefix)
				|| ((Component)tmpNode.getElement()).getName().startsWith(orGroupNamePrefix)) ){
			underlyingComponent=(JComponent)tmpNode.getElement();
//			addStartAnchorToGroup((AnchorPanel)lastAnchorFocused, (GroupPanel)tmpNode.getElement());
//			frameRoot.repaint();
			return underlyingComponent;					  
		  }
//		  else return null;
		}
		tmpNode=tmpNode.getNext();
	  }		
	  return null;
	}

	/**
	 * Drops a group on the diagram panel, adding it to the underlying feature panel, if any is present.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	public static void dropGroupOnDiagram(MouseEvent e) {
	  FeaturePanel underlyingPanel=null;
	  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
	  while(tmpNode!=null){	  
	    if ( tmpNode.getElement().getClass().equals(FeaturePanel.class) &&
			 ((FeaturePanel)tmpNode.getElement()).getName().startsWith(featureNamePrefix) &&
		     ((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY()) ){			
		  underlyingPanel=(FeaturePanel)tmpNode.getElement();
		  addAnchorToFeature(lastAnchorFocused, underlyingPanel);
		  //			  diagramPanel.repaint();
		  frameRoot.repaint();
		  break;		
	    }
	    tmpNode=tmpNode.getNext();
	  }				
	}
	

	/**
	NO PARAMETER VERSION, CALLED BY update()
	 */
	public static boolean addAnchorToFeature() {
		int anchorPanelOnScreenX;
		int anchorPanelOnScreenY;
		Component comp = null;
		AnchorPanel startAnchor = null;
		GroupPanel group = null;
		
//		if(lastAnchorFocused.getParent()==null || !lastAnchorFocused.isDisplayable()) return false;
		
		moveComponentToTop(underlyingComponent);

		//if it is an ending anchor, location is set to top-middle of underlying feature
		if(lastAnchorFocused.getName().startsWith(endConnectorsNamePrefix)){
			lastAnchorFocused.setLocation(underlyingComponent.getWidth()/2-lastAnchorFocused.getWidth()/2, -4);		
		}
		
		//if it is a starting anchor, location is set relative to underlying feature, on the same visible location
		else {
		  anchorPanelOnScreenX=(int)lastAnchorFocused.getLocationOnScreen().getX();
		  anchorPanelOnScreenY=(int)lastAnchorFocused.getLocationOnScreen().getY();
		  lastAnchorFocused.setLocation((int)(anchorPanelOnScreenX-underlyingComponent.getLocationOnScreen().getX()),
				  (int)(anchorPanelOnScreenY-underlyingComponent.getLocationOnScreen().getY()) );		
		}

//		//checking if anchor must be added to a group
//		if(lastAnchorFocused.getName().startsWith(startConnectorsNamePrefix)){
//		  comp = underlyingComponent.getComponentAt(lastAnchorFocused.getLocation());
//		  if (comp!=null && comp.getName()!=null && (comp.getName().startsWith(altGroupNamePrefix) 
//			  || comp.getName().startsWith(orGroupNamePrefix) )){
//			startAnchor=(AnchorPanel)lastAnchorFocused;
//			group=(GroupPanel)comp;
//
//	        addStartAnchorToGroup(startAnchor, group);
//			return true;
//		  }
//		}

		//Adding anchor to the feature
		diagramPanel.remove(lastAnchorFocused);
		diagramPanel.validate();
		((JLayeredPane)underlyingComponent).setLayer(lastAnchorFocused, 0);
		((JLayeredPane)underlyingComponent).add(lastAnchorFocused);
		((JLayeredPane)underlyingComponent).setComponentZOrder(lastAnchorFocused, 0);

		return true;
	}

	/**
	 * Removes a visible anchor from the diagram panel and attach it to a feature panel.<br>
	 * If the anchor is not visible and attached to the diagram panel, no operation is performed <br>
	 * and false value is returned.
	 * 
	 * @param anchor - the JComponent object of the anchor
	 * @param featurePanel - the feature panel on which the anchor will be added
	 * @return - false if the operation is not possible, true otherwise
	 */
	private static boolean addAnchorToFeature(JComponent anchor, FeaturePanel featurePanel) {
		int anchorPanelOnScreenX;
		int anchorPanelOnScreenY;
		Component comp = null;
		AnchorPanel startAnchor = null;
		GroupPanel group = null;
		
		/* ***DEBUG*** */
		if(debug4) System.out.println("BEFORE ADDING ANCHOR TO FEATURE"
			+"\nanchor.getParent()="+anchor.getParent()
			+"\nanchor.isDisplayable(): "+anchor.isDisplayable());
		/* ***DEBUG*** */

		if(anchor.getParent()==null || !anchor.isDisplayable()) return false;
		
		moveComponentToTop(featurePanel);

		/* ***DEBUG*** */
		if(debug) System.out.println("Adding anchor to the feature: "+featurePanel.getName()
				+"\nunderlyingPanel.getLocationOnScreen(): "+featurePanel.getLocationOnScreen()
				+"\nlastAnchorFocused.getLocationOnScreen(): "+anchor.getLocationOnScreen());
		/* ***DEBUG*** */

		
		//if it is an ending anchor, location is set to top-middle of underlying feature
		if(anchor.getName().startsWith(endConnectorsNamePrefix)){
		  anchor.setLocation(featurePanel.getWidth()/2-anchor.getWidth()/2, -4);		
		}
		
		//if it is a starting anchor, location is set relative to underlying feature, on the same visible location
		else {
		  anchorPanelOnScreenX=(int)anchor.getLocationOnScreen().getX();
		  anchorPanelOnScreenY=(int)anchor.getLocationOnScreen().getY();
		  anchor.setLocation((int)(anchorPanelOnScreenX-featurePanel.getLocationOnScreen().getX()),
				  (int)(anchorPanelOnScreenY-featurePanel.getLocationOnScreen().getY()) );		
		}

		//checking if anchor must be added to a group
		if(anchor.getName().startsWith(startConnectorsNamePrefix)){
		  comp = featurePanel.getComponentAt(anchor.getLocation());
		  if (comp!=null && comp.getName()!=null && (comp.getName().startsWith(altGroupNamePrefix) 
			  || comp.getName().startsWith(orGroupNamePrefix) )){
			startAnchor=(AnchorPanel)anchor;
			group=(GroupPanel)comp;

     		/* ***DEBUG*** */
	        if(debug) System.out.println("Adding anchor to the group: "+group.getName());
	        /* ***DEBUG*** */
	        
	        addStartAnchorToGroup(startAnchor, group);
			return true;
//			endConnectorDots.remove(((AnchorPanel)anchor).getOtherEnd());
//			connectorDotsToRedraw.remove(anchor);
		  }
		}

		//Adding anchor to the feature
		/* ***DEBUG*** */
		if(debug) System.out.println("Adding anchor to the feature: "+featurePanel.getName()
				+"\nWith origin: "+featurePanel.getLocation()
				+"\nIn the position: "+anchor.getLocation());
		/* ***DEBUG*** */

//		visibleOrderDraggables.
		diagramPanel.remove(anchor);
		diagramPanel.validate();
		featurePanel.setLayer(anchor, 0);
		featurePanel.add(anchor);
		featurePanel.setComponentZOrder(anchor, 0);
		
		/* ***DEBUG*** */
		if(debug4) System.out.println("AFTER ADDING ANCHOR TO FEATURE"
			+"\nanchor.getParent()="+anchor.getParent()
			+"\nanchor.isDisplayable(): "+anchor.isDisplayable());
		/* ***DEBUG*** */

		return true;
	}

	/**
	 * Removes a starting connector anchor from the diagram panel and attach it to a group.<br>
	 * 
	 * @param startAnchor - the starting connector anchor to be added
	 * @param group - the group 
	 */
	private static void addStartAnchorToGroup(AnchorPanel startAnchor, GroupPanel group) {
		group.getMembers().add(startAnchor.getOtherEnd());
		((AnchorPanel)startAnchor.getOtherEnd()).setOtherEnd(group);
		diagramPanel.remove(startAnchor);
		diagramPanel.validate();
		visibleOrderDraggables.remove(startAnchor);
		startConnectorDots.remove(startAnchor);
		return;
	}

	/**
	 * Adds a new feature to the diagram panel, incrementing featuresCount.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	public static void addNewFeatureToDiagram(/*MouseEvent e*/) {

//		if (!checkDroppedOnDiagram()) return;

//		/* ***DEBUG*** */
//		if(debug) System.out.println("Mouse rilasciato(Drag relative) su: ("+e.getX()+", "+e.getY()+")."
//		  +"\nMouse rilasciato(Screen relative) su: ("+e.getXOnScreen()+", "+e.getYOnScreen()+")."
//		  +"\nLocation dove verrà aggiunta la nuova Feature: ("+toolDragPosition.x+", "+toolDragPosition.y+").");
//		/* ***DEBUG*** */
//
		FeaturePanel newFeature=getDraggableFeature(/*"Default Feature Name"*/featureNamePrefix+featuresCount,
			toolDragPosition.x-(int)diagramPanel.getLocationOnScreen().getX(),
			toolDragPosition.y-(int)diagramPanel.getLocationOnScreen().getY());
		

		visibleOrderDraggables.addToTop(newFeature);
		diagramPanel.setLayer(newFeature, 0);
		diagramPanel.add(newFeature);
		diagramPanel.setComponentZOrder(newFeature, 0);
//		diagramPanel.repaint();
		cancelToolDrag();

		/* ***DEBUG*** */
		if(debug) System.out.println("Actual location of the new Feature: ("+newFeature.getLocationOnScreen()
		  +"\ndiagramPanel.getLocationOnScreen(): ("+diagramPanel.getLocationOnScreen());
		/* ***DEBUG*** */

		++featuresCount;
	}

	/**
	 * Adds a new connector to the diagram. If the starting connector dot is dropped over a feature panel, <br>
	 * it gets attached to it.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	public static void addConnectorToDiagram(MouseEvent e) {
//		int actualPositionX=((int)(e.getLocationOnScreen().getX()-diagramPanel.getLocationOnScreen().getX()));
//		int actualPositionY=((int)(e.getLocationOnScreen().getY()-diagramPanel.getLocationOnScreen().getY()));
		int actualPositionX;
		int actualPositionY;
		boolean startDotInsertedInPanel=false;
		AnchorPanel newConnectorStartDot=null;			
		AnchorPanel newConnectorEndDot=null;
		FeaturePanel featurePanel = null;
		JComponent underlyingPanel = null;

		
		//the new connector must be dropped on the diagram panel for it to be added
		if( diagramPanel.getLocationOnScreen().getX()>toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getWidth()<=toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getY()>toolDragPosition.y ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getHeight()<=toolDragPosition.y ){
			
			cancelToolDrag();

			/* ***DEBUG*** */
			if(debug4) System.out.println("Cannot drop a new connector on tools panel.");
			/* ***DEBUG*** */
			return;
		}
			
		actualPositionX=(toolDragPosition.x-(int)diagramPanel.getLocationOnScreen().getX());
		actualPositionY=(toolDragPosition.y-(int)diagramPanel.getLocationOnScreen().getY());

		//retrieving the underlying feature panel, if any
		underlyingPanel = getUnderlyingComponent(actualPositionX, actualPositionY);

		/* ***DEBUG*** */
		if(debug){
		  System.out.println("Component tmp:"+underlyingPanel);
		  if(underlyingPanel!=null)
			System.out.println("Component tmp position on screen:"+underlyingPanel.getLocationOnScreen()
				+"\nComponent tmp name:"+underlyingPanel.getName());
		}
		/* ***DEBUG*** */

		if(underlyingPanel!=null){//if the underlying panel is a feature, the start connection dot is anchored to it
		  if (underlyingPanel.getClass().equals(FeaturePanel.class) ){
			featurePanel=(FeaturePanel)underlyingPanel;
//			  if (!underlyingPanel.getName().startsWith(diagramPanelName)&&
//					  !underlyingPanel.getName().startsWith(startConnectorsNamePrefix)&&
//					  !underlyingPanel.getName().startsWith(endConnectorsNamePrefix)  ){			  

			newConnectorStartDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.START_CONNECTOR, 
				toolDragPosition.x-(int)featurePanel.getLocationOnScreen().getX(),
				toolDragPosition.y-(int)featurePanel.getLocationOnScreen().getY()-5);			

//			System.out.println("adding to feature: "+addAnchorToFeature(newConnectorStartDot, underlyingPanel));
			moveComponentToTop(featurePanel);

			featurePanel.setLayer(newConnectorStartDot, 0);
			featurePanel.add(newConnectorStartDot);
			featurePanel.setComponentZOrder(newConnectorStartDot, 0);

			startDotInsertedInPanel=true;
			
			/* ***DEBUG*** */
			System.out.println("Placing start connector in ("
					+(toolDragPosition.x-(int)underlyingPanel.getLocationOnScreen().getX())
					+", "+(toolDragPosition.y-(int)underlyingPanel.getLocationOnScreen().getY())+")");			
			System.out.println("Start connector Position(feature relative): ("
					+newConnectorStartDot.getX()+", "+newConnectorStartDot.getY()+")");			
			System.out.println("Start connector Position(screen relative): ("
					+newConnectorStartDot.getLocationOnScreen().getX()
					+", "+newConnectorStartDot.getLocationOnScreen().getY()+")");			
			/* ***DEBUG*** */
		  
		  }
		}
		
		if(!startDotInsertedInPanel) 
		  newConnectorStartDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.START_CONNECTOR,
			  actualPositionX, actualPositionY-5);			
		
		ImageIcon lineLengthIcon = new ImageIcon(connectorLineLengthIconURL);
		ImageIcon startConnectorIcon = new ImageIcon(connectorStartDotIconURL);

//		newConnectorEndDot=getDraggableConnectionDot(itemsTypes.END_CONNECTOR, actualPositionX+40, actualPositionY+40);
		newConnectorEndDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.END_CONNECTOR,
			actualPositionX+lineLengthIcon.getIconWidth()+startConnectorIcon.getIconWidth(),
			actualPositionY-5+lineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

//		newConnectorStartDot.add(diagramElementsMenu);
//		newConnectorEndDot.add(diagramElementsMenu);

		/* ***DEBUG*** */
		if(debug) System.out.println("Mouse released(Drag relative) on: ("+e.getX()+", "+e.getY()+")."
		  +"\nMouse released(Screen relative) on: ("+e.getXOnScreen()+", "+e.getYOnScreen()+")."
		  +"\nLocation where the Connector will be placed: ("+toolDragPosition.x+", "+toolDragPosition.y+")."
		  +"\nActualPositionX: "+actualPositionX+"\nActualPositionY: "+actualPositionY);
		/* ***DEBUG*** */
		
		visibleOrderDraggables.addToTop(newConnectorStartDot);
		if(!startDotInsertedInPanel){
		  diagramPanel.setLayer(newConnectorStartDot, 0);
		  diagramPanel.add(newConnectorStartDot);
		  diagramPanel.setComponentZOrder(newConnectorStartDot, 0);
		}

		visibleOrderDraggables.addToTop(newConnectorEndDot);
		diagramPanel.setLayer(newConnectorEndDot, 0);
		diagramPanel.add(newConnectorEndDot);
		diagramPanel.setComponentZOrder(newConnectorEndDot, 0);

		/* ***DEBUG*** */
		if(debug) System.out.println("Actual location of Connector1: ("+newConnectorStartDot.getLocationOnScreen()
		  +"\nActual location of Connector2: ("+newConnectorEndDot.getLocationOnScreen()
		  +"\ndiagramPanel.getLocationOnScreen(): ("+diagramPanel.getLocationOnScreen());
		/* ***DEBUG*** */

		//setting other ends of connector dots
		newConnectorStartDot.setOtherEnd(newConnectorEndDot);
		newConnectorEndDot.setOtherEnd(newConnectorStartDot);

		addConnectorsToDrawLists(newConnectorStartDot, newConnectorEndDot);
		++connectorsCount;

		cancelToolDrag();
	}

	public static void addOrGroupToDiagram(MouseEvent e) {
		GroupPanel newGroupStartDot = addGroupToDiagram(e, ItemsType.OR_GROUP_START_CONNECTOR);
		if (newGroupStartDot==null) return;
		addOrGroupToDrawLists(newGroupStartDot);
		++orGroupsCount;
	}

	/**
	 * Adds a new Alternative Group to the diagram. If the starting connector dot is dropped over a feature panel, <br>
	 * it gets attached to it.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	public static void addAltGroupToDiagram(MouseEvent e) {
		GroupPanel newGroupStartDot = addGroupToDiagram(e, ItemsType.ALT_GROUP_START_CONNECTOR);
		if (newGroupStartDot==null) return;
		addAltGroupToDrawLists(newGroupStartDot);
		++altGroupsCount;
	}

	/**
	 * Adds a new group to the diagram. If the starting connector dot is dropped over a feature panel, <br>
	 * it gets attached to it.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 * @param requestedType - the type of the requested group, an ItemsType value.
	 */
	private static GroupPanel addGroupToDiagram(MouseEvent e, ItemsType requestedType) {
		int actualPositionX;
		int actualPositionY;
		boolean startDotInsertedInPanel=false;
		GroupPanel newGroupStartDot=null;			
		AnchorPanel newGroupEndpoint1=null, newGroupEndpoint2=null;
		FeaturePanel featurePanel = null;
		JComponent underlyingPanel = null;
		
		//the new group must be dropped on the diagram panel for it to be added
		if( diagramPanel.getLocationOnScreen().getX()>toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getWidth()<=toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getY()>toolDragPosition.y ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getHeight()<=toolDragPosition.y ){
			
			cancelToolDrag();

			/* ***DEBUG*** */
			if(debug4) System.out.println("Cannot drop a new group on tools panel.");
			/* ***DEBUG*** */
			
			return null;
		}
			
		ImageIcon groupLineLengthIcon = new ImageIcon(groupLineLengthIconURL);
		ImageIcon startConnectorIcon = new ImageIcon(connectorStartDotIconURL);
		ImageIcon groupIcon = getIconImage("Alternative Group");

		actualPositionX=(toolDragPosition.x-(int)diagramPanel.getLocationOnScreen().getX());
		actualPositionY=(toolDragPosition.y-(int)diagramPanel.getLocationOnScreen().getY());

		//retrieving the underlying feature panel, if any
		underlyingPanel = getUnderlyingComponent(actualPositionX, actualPositionY);

		/* ***DEBUG*** */
		if(debug){
		  System.out.println("Component tmp:"+underlyingPanel);
		  if(underlyingPanel!=null)
			System.out.println("Component tmp position on screen:"+underlyingPanel.getLocationOnScreen()
				+"\nComponent tmp name:"+underlyingPanel.getName());
		}
		/* ***DEBUG*** */

		if(underlyingPanel!=null){//if the underlying panel is a feature, the start connection dot is anchored to it
		  if (underlyingPanel.getClass().equals(FeaturePanel.class) ){
			featurePanel=(FeaturePanel)underlyingPanel;
//		if(underlyingPanel!=null){//if the underlying panel is a feature, the group start dot is anchored to it
//		  if (!underlyingPanel.getName().startsWith(diagramPanelName)&&
//			  !underlyingPanel.getName().startsWith(startConnectorsNamePrefix)&&
//			  !underlyingPanel.getName().startsWith(endConnectorsNamePrefix)  ){			  
			newGroupStartDot=(GroupPanel)getDraggableConnectionDot(requestedType, 
				toolDragPosition.x-(int)featurePanel.getLocationOnScreen().getX()
					+groupIcon.getIconWidth()/2-startConnectorIcon.getIconWidth()/2,
				toolDragPosition.y-(int)featurePanel.getLocationOnScreen().getY()-5);			

//			System.out.println("adding to feature: "+addAnchorToFeature(newConnectorStartDot, underlyingPanel));
			moveComponentToTop(featurePanel);

			featurePanel.setLayer(newGroupStartDot, 0);
			featurePanel.add(newGroupStartDot);
			featurePanel.setComponentZOrder(newGroupStartDot, 0);

			startDotInsertedInPanel=true;
			
			/* ***DEBUG*** */
			if(debug3) System.out.println("Placing group start dot in ("
					+(toolDragPosition.x-(int)underlyingPanel.getLocationOnScreen().getX())
					+", "+(toolDragPosition.y-(int)underlyingPanel.getLocationOnScreen().getY())+")"
					+"\nGroup start dot Position(feature relative): ("
					+newGroupStartDot.getX()+", "+newGroupStartDot.getY()+")"
					+"\nGroup start dot Position(screen relative): ("
					+newGroupStartDot.getLocationOnScreen().getX()
					+", "+newGroupStartDot.getLocationOnScreen().getY()+")");			
			/* ***DEBUG*** */
		  
		  }
		}
		
		if(!startDotInsertedInPanel) 
			newGroupStartDot=(GroupPanel)getDraggableConnectionDot(requestedType,
				actualPositionX+groupIcon.getIconWidth()/2-startConnectorIcon.getIconWidth()/2, actualPositionY-5);			
		
//		newConnectorEndDot=getDraggableConnectionDot(itemsTypes.END_CONNECTOR, actualPositionX+40, actualPositionY+40);
		newGroupEndpoint1=(AnchorPanel)getDraggableConnectionDot(ItemsType.END_CONNECTOR,
				actualPositionX+groupIcon.getIconWidth()/2-startConnectorIcon.getIconWidth()/2
				+groupLineLengthIcon.getIconWidth(),
				actualPositionY-5+groupLineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

		newGroupEndpoint2=(AnchorPanel)getDraggableConnectionDot(ItemsType.END_CONNECTOR,
				actualPositionX+groupIcon.getIconWidth()/2-startConnectorIcon.getIconWidth()/2
				-groupLineLengthIcon.getIconWidth()/*-startConnectorIcon.getIconWidth()*/,
				actualPositionY-5+groupLineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

		/* ***DEBUG*** */
		if(debug) System.out.println("Mouse released(Drag relative) on: ("+e.getX()+", "+e.getY()+")."
		  +"\nMouse released(Screen relative) on: ("+e.getXOnScreen()+", "+e.getYOnScreen()+")."
		  +"\nLocation where the Connector will be placed: ("+toolDragPosition.x+", "+toolDragPosition.y+")."
		  +"\nActualPositionX: "+actualPositionX+"\nActualPositionY: "+actualPositionY);
		/* ***DEBUG*** */
		
		visibleOrderDraggables.addToTop(newGroupStartDot);
		if(!startDotInsertedInPanel){
		  diagramPanel.setLayer(newGroupStartDot, 0);
		  diagramPanel.add(newGroupStartDot);
		  diagramPanel.setComponentZOrder(newGroupStartDot, 0);
		}

		visibleOrderDraggables.addToTop(newGroupEndpoint1);
		diagramPanel.setLayer(newGroupEndpoint1, 0);
		diagramPanel.add(newGroupEndpoint1);
		diagramPanel.setComponentZOrder(newGroupEndpoint1, 0);

		visibleOrderDraggables.addToTop(newGroupEndpoint2);
		diagramPanel.setLayer(newGroupEndpoint2, 0);
		diagramPanel.add(newGroupEndpoint2);
		diagramPanel.setComponentZOrder(newGroupEndpoint2, 0);

		cancelToolDrag();

		/* ***DEBUG*** */
		if(debug) System.out.println("Actual location of Group start: ("+newGroupStartDot.getLocationOnScreen()
		  +"\nActual location of Endpoint1: ("+newGroupEndpoint1.getLocationOnScreen()
		  +"\nActual location of Endpoint2: ("+newGroupEndpoint2.getLocationOnScreen()
		  +"\ndiagramPanel.getLocationOnScreen(): ("+diagramPanel.getLocationOnScreen());
		/* ***DEBUG*** */

		newGroupStartDot.getMembers().add(newGroupEndpoint1);
		newGroupStartDot.getMembers().add(newGroupEndpoint2);

		newGroupEndpoint1.setOtherEnd(newGroupStartDot);
		newGroupEndpoint2.setOtherEnd(newGroupStartDot);
		return newGroupStartDot;
	}

	/**
	 * Creates a JComponent containing a Connection Dot, with a sequential name given by the static variable connectorsCount. 
	 * 
	 * @param x - x coordinate of the connection dot in the diagram panel
	 * @param y - y coordinate of the connection dot in the diagram panel
	 * @param incoURL - URL of the connection dot icon
	 * @return A new JComponent representing the connection dot
	 */
	private static JComponent getDraggableConnectionDot(ItemsType type, int x, int y) {
		JComponent imagePanel=null;

		ImageIcon connectorIcon=null;
		JLabel imageLabel = null;

		switch(type){
		  case START_CONNECTOR:
			imagePanel = new AnchorPanel();  
			imagePanel.setName(startConnectorsNamePrefix+connectorsCount);
			connectorIcon = new ImageIcon(connectorStartDotIconURL);
			break;
		  case END_CONNECTOR:
			imagePanel = new AnchorPanel();  
			imagePanel.setName(endConnectorsNamePrefix+connectorsCount);
			connectorIcon = new ImageIcon(connectorEndDotIconURL);
			break;
		  case ALT_GROUP_START_CONNECTOR:
			imagePanel = new GroupPanel();  
			imagePanel.setName(altGroupNamePrefix+altGroupsCount);
			connectorIcon = new ImageIcon(ALTGroupDotIconURL);
			break;
		  case OR_GROUP_START_CONNECTOR:
			imagePanel = new GroupPanel();  
			imagePanel.setName(orGroupNamePrefix+orGroupsCount);
			connectorIcon = new ImageIcon(ORGroupDotIconURL);
			break;
 		  default: return null;
		}
		
		imageLabel = new JLabel(connectorIcon);
		imageLabel.setBounds(0,  +2, connectorIcon.getIconWidth(), connectorIcon.getIconHeight()+5);
		imagePanel.setBounds(x,  y, connectorIcon.getIconWidth(), connectorIcon.getIconHeight()+5);
		imagePanel.setLayout(null);
//		imagePanel.setBackground(Color.RED);
		imagePanel.add(imageLabel);
//		imagePanel.add(new JPanel());
		imagePanel.setOpaque(false);
		imagePanel.setVisible(true);

		/* ***DEBUG*** */
		if(debug) System.out.println("imagePanel.getBounds(): "+imagePanel.getBounds()+
			"\nimageLabel.getBounds(): "+imageLabel.getBounds());
		/* ***DEBUG*** */

		return imagePanel;
	}

	/**
	 * Creates a JPanel containing a Feature
	 * 
	 * @param name - the name of the feature
	 * @param x - x coordinate of the feature in the diagram panel
	 * @param y - y coordinate of the feature in the diagram panel
	 * @return A new JPanel representing the feature
	 */
	private static FeaturePanel getDraggableFeature(String name, int x, int y) {
		int layer=-1;
		JLabel imageLabel = null, textLabel = null;
		ImageIcon newFeatureIcon = null;
		
		//creating image		
		newFeatureIcon=new ImageIcon(newFeatureIconURL);		
		imageLabel = new JLabel(newFeatureIcon);
		imageLabel.setBounds(0+featureBorderSize/2, +featureBorderSize/2,
				newFeatureIcon.getIconWidth(), newFeatureIcon.getIconHeight());
		imageLabel.setBackground(Color.BLACK);
		imageLabel.setOpaque(true);
		imageLabel.setVisible(true);

		//creating text
		textLabel=new JLabel(name, SwingConstants.CENTER);
		textLabel.setForeground(Color.GRAY);
		textLabel.setBackground(Color.BLACK);
		textLabel.setBounds(0+featureBorderSize/2, newFeatureIcon.getIconHeight()+featureBorderSize/2,
			newFeatureIcon.getIconWidth(), 25);
		textLabel.setOpaque(true);
		
		FeaturePanel container = new FeaturePanel(splitterPanel);
		
		container.setName(featureNamePrefix+featuresCount);
		container.setLayout(null);
		container.setBounds(x,  y,  newFeatureIcon.getIconWidth()+featureBorderSize,
			newFeatureIcon.getIconHeight()+25+featureBorderSize);

		container.setOpaque(true);
		container.setBackground(Color.DARK_GRAY);
		  
		//adding the image
		layer=container.getComponentCount();
		container.setLayer(imageLabel, layer);
		container.add(imageLabel);
		container.setComponentZOrder(imageLabel, layer);

		//adding the text
		layer=container.getComponentCount();
		container.setLayer(textLabel, layer);
		container.add(textLabel);
		container.setComponentZOrder(textLabel, layer);

		/* ***DEBUG*** */
		if(debug) System.out.println("container.getBounds(): "+container.getBounds());
		/* ***DEBUG*** */

		return container;
	}
	
	/**
	 * Returns the Component at the given position inside diagramPanel, if any.
	 * 
	 * @param x - x coordinate relative to diagramPanel
	 * @param y - y coordinate relative to diagramPanel
	 * @return - the Component found, or null if there isn't any Component, children of diagramPanel, <br>
	 * at the specified coordinates.
	 */
	private static JComponent getUnderlyingComponent(int x, int y) {
	  JComponent subComponent = null;
	  JComponent underlyingPanel = (JComponent) diagramPanel.getComponentAt(x, y);

	  /* ***DEBUG*** */
	  if(debug) System.out.println("underlyingPanel="+underlyingPanel
			  +"\nunderlyingPanel.getClass()"+underlyingPanel.getClass());
	  /* ***DEBUG*** */

	  if(underlyingPanel==null ||
		  ( !underlyingPanel.getClass().equals(AnchorPanel.class)
			&& !underlyingPanel.getClass().equals(GroupPanel.class)
			&& !underlyingPanel.getClass().equals(FeaturePanel.class) )
		) return null;
	  
	  //checking if the underlying component is a child of a feature component
	  if(underlyingPanel.getClass().equals(FeaturePanel.class)){
		//switching from diagramPanel coordinate system to underlyingPanel coordinate system
		x=x+(int)(diagramPanel.getLocationOnScreen().getX()-underlyingPanel.getLocationOnScreen().getX());
		y=y+(int)(diagramPanel.getLocationOnScreen().getY()-underlyingPanel.getLocationOnScreen().getY());
		
		subComponent = (JComponent) underlyingPanel.getComponentAt(x, y);

		if(subComponent==null ||
			( !subComponent.getClass().equals(AnchorPanel.class)
						&& !subComponent.getClass().equals(GroupPanel.class) )
		  ) return underlyingPanel;
		else return subComponent;
	  }
	  
	  return underlyingPanel;
	}

	/**
	 * Adds the connector ending and starting dots to the lists used to draw connectors.
	 * 
	 * @param newConnectorStartDot - the starting connector dot
	 * @param newConnectorEndDot - the ending connector dot
	 */
	private static void addConnectorsToDrawLists(JComponent newConnectorStartDot, JComponent newConnectorEndDot) {
		startConnectorDots.add(newConnectorStartDot);
//		prevStartConnectorDotsLocation.add(new Point());
//		endConnectorDots.add(newConnectorEndDot);
//		prevEndConnectorDotsLocation.add(new Point());
//		connectorDotsToRedraw.add(true);
	}
	
	/**
	 * Adds the Alternatibe Group to the lists used to draw connectors.
	 * 
	 * @param group - the GrouPanel object to be added to the lists
	 */
	private static void addAltGroupToDrawLists(GroupPanel group) {
		altGroupPanels.add(group);		
	}

	/**
	 * Adds the Or Group to the lists used to draw connectors.
	 * 
	 * @param group - the GrouPanel object to be added to the lists
	 */
	private static void addOrGroupToDrawLists(GroupPanel group) {
		orGroupPanels.add(group);		
	}
	
	/**
	 * Deletes an anchor from the diagram
	 * 
	 * @param anchor - the anchor to delete
	 */
	public static void deleteAnchor(JComponent anchor) {
		int diagramRelativeX=0;
		int diagramRelativeY=0;
		JComponent underlying = null;
		
		if(anchor==null) return;
		
		//getting the diagram panel-relative position of the element
    	diagramRelativeX=(int)(anchor.getLocationOnScreen().getX()-diagramPanel.getLocationOnScreen().getX());
    	diagramRelativeY=(int)(anchor.getLocationOnScreen().getY()-diagramPanel.getLocationOnScreen().getY());
    	underlying=getUnderlyingComponent(diagramRelativeX, diagramRelativeY);
    	
    	/* ***DEBUG*** */
    	if(debug3) System.out.println("underlying="+underlying+"\nunderlying.getName(): "+underlying.getName()
    			+"\nanchor.getParent(): "+anchor.getParent()
    			+"\nanchor.getParent().getName()"+anchor.getParent().getName()
    			+"\nanchor.getParent().getParent(): "+anchor.getParent().getParent()
    			+"\nanchor.getParent().getParent().getName()"+anchor.getParent().getParent().getName());
    	/* ***DEBUG*** */
    	
        //the element is attached directly to the diagram panel
        if(anchor.getParent().getName().startsWith(diagramPanelName)){
    	  diagramPanel.remove(anchor);
    	  visibleOrderDraggables.remove(anchor);
//    	  diagramPanel.validate();
        }
        else if(anchor.getParent().getName().startsWith(featureNamePrefix)){
          anchor.getParent().remove(anchor);
      	  visibleOrderDraggables.remove(anchor);
//          anchor.getParent().validate();
        }

        if (anchor.getName().startsWith(startConnectorsNamePrefix)) startConnectorDots.remove(anchor);
        
	}

	/** Returns the popup menu for all diagram panel elements*/
	public JPopupMenu getDiagramElementsMenu(){
		return diagramElementsMenu;
	};
	
	/** Returns the element interested by the popup menu*/
	public JComponent getPopUpElement(){
		return popUpElement;
	};
	
	/** Sets the element interested by the popup menu*/
	public void setPopUpElement(JComponent comp){
		popUpElement=comp;
	};
	
	/** Returns the 'Delete Element' popup menu item */
	public JMenuItem getPopMenuItemDelete(){
		return popMenuItemDelete;
	};
	
	/** Returns the 'Ungroup Element' popup menu item */
	public JMenuItem getPopMenuItemUngroup(){
		return popMenuItemUngroup;
	};
	
	/** Returns the 'Print Model[DEBUG COMMAND]' popup menu item */
	public JMenuItem getPopMenuItemPrintModelDebug(){
		return popMenuItemPrintModelDebug;
	};

	/** Returns the popup menu X coordinate on the diagram*/
	public int getDiagramElementsMenuPosX(){
		return diagramElementsMenuPosX;
	};
	
	/** Sets the popup menu X coordinate on the diagram*/
	public void setDiagramElementsMenuPosX(int xPos){
		diagramElementsMenuPosX=xPos;
	};
	
	/** Returns the popup menu Y coordinate on the diagram*/
	public int getDiagramElementsMenuPosY(){
		return diagramElementsMenuPosY;
	};
	
	/** Sets the popup menu Y coordinate on the diagram*/
	public void setDiagramElementsMenuPosY(int yPos){
		diagramElementsMenuPosY=yPos;
	};
	
	/** Returns the last active item type*/
	public activeItems getActiveItem(){
		return isActiveItem;
	};
	
	/** Sets the last active item type*/
	public void setActiveItem(activeItems item){
		isActiveItem=item;
	};
	
	/** Returns the last anchor component focused*/
	public JComponent getLastAnchorFocused(){
		return lastAnchorFocused;
	};
	
	/** Sets the last anchor component focused*/
	public void setLastAnchorFocused(JComponent lastFocused){
		lastAnchorFocused=lastFocused;
	};
	
	/** Returns the last feature component focused*/
	public FeaturePanel getLastFeatureFocused(){
		return lastFeatureFocused;
	};
	
	/** Sets the last feature component focused*/
	public void setLastFeatureFocused(FeaturePanel lastFocused){
		lastFeatureFocused=lastFocused;
	};
	
	/** Returns the last X coordinate on the diagram of the component being dragged*/
	public int getLastPositionX(){
		return lastPositionX;
	};
	
	/** Sets the last X coordinate on the diagram of the component being dragged*/
	public void setLastPositionX(int xPos){
		lastPositionX=xPos;
	};
	
	/** Returns the last Y coordinate on the diagram of the component being dragged*/
	public int getLastPositionY(){
		return lastPositionY;
	};
	
	/** Sets the last Y coordinate on the diagram of the component being dragged*/
	public void setLastPositionY(int yPos){
		lastPositionY=yPos;
	};	
	
	/** Returns the tool image being dragged*/
	public BufferedImage getToolDragImage(){
		return toolDragImage;
	}

	/** Sets the tool image being dragged*/
	public void setToolDragImage(BufferedImage img){
		toolDragImage=img;
	}

	/** Returns the position of the tool being dragged*/
	public Point getToolDragPosition(){
		return toolDragPosition;
	}

	/** Sets the position of the tool being dragged*/
	public void setToolDragPosition(Point p){
		toolDragPosition=p;
	}
	

	/** Returns the Z-ordered list of visible draggable components on the diagram panel*/
	public OrderedList getVisibleOrderDraggables(){
		return visibleOrderDraggables;
	}

	/** Returns the diagram panel*/
	public JLayeredPane getDiagramPanel(){
		return diagramPanel;
	}

	/** Returns the toolbar panel*/
	public JPanel getToolsPanel(){
		return toolsPanel;
	}

	/** Returns the splitter panel containing diagram and toolbar panels*/
	public EditorSplitPane getSplitterPanel(){
		return splitterPanel;
	}
	
	/** Returns a String containing the resource name of the requested tool image */
	public String getToolIconPath(String toolName){
		return toolIconPaths.get(toolName);
	}	
	
	/** Returns the number of features added to the diagram */
	public int getFeaturesCount(){
		return featuresCount;
	}	
	

	/**
	 * Checks whether the dragged tool has been dropped on the diagram panel or not.
	 * 
	 * @return true if tool has been dropped on the diagram panel, false otherwise
	 */
	public static boolean checkDroppedOnDiagram() {
		//the new feature must be dropped on the diagram panel for it to be added
		if( diagramPanel.getLocationOnScreen().getX()>toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getWidth()<=toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getY()>toolDragPosition.y ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getHeight()<=toolDragPosition.y ){

			cancelToolDrag();

			/* ***DEBUG*** */
			if(debug4) System.out.println("Cannot drop a new feature on tools panel.");
			/* ***DEBUG*** */
			
			return false;
		}
		else return true;
	}

	private static void cancelToolDrag() {
		toolDragImage=null;
		toolDragPosition=null;
		frameRoot.repaint();
	}	

	/** shows the popup menu on the diagram, at the clicked location*/
	public void showDiagramElementsMenu(){
	    diagramElementsMenu.show(diagramPanel, diagramElementsMenuPosX, diagramElementsMenuPosY);		
	}

	/** Repaints frameRoot*/
	public void repaintRootFrame(){
		frameRoot.repaint();		
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println("Message received: "+arg);
		if(arg.equals("New Feature Correctly Added")){
			addNewFeatureToDiagram();			
		}
		else if(arg.equals("Grouped a Feature")
			     || arg.equals("Two Features Directly Linked")){
			addAnchorToFeature();
		}
		else if(arg.equals("Not Grouped a Feature")
			     || arg.equals("Two Features Not Linked")){
			System.out.println("Operation would create a cycle and it has been aborted!");
		}
		
	}

	/**
	 * Main Method.
	 * @param args
	 */
	public static void main(String[] args){
		//creating model
		EditorModel editorModel= new EditorModel();
		//creating view
		EditorView editorView= new EditorView();
		//creating controller
		EditorController editorController =new EditorController(editorView, editorModel);
		//adding the view as observer to the model
		editorModel.addObserver(editorView);

		if( !editorView.prepareUI(editorController) ) System.out.println("Controller not set. Closing...");
		else editorView.setVisible(true);
//		editor.setResizable(false);
		editorView.setExtendedState(editorView.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		System.out.println("Ready! frameRoot.getLocationOnScreen(): "+frameRoot.getLocationOnScreen());
		


	}

}
