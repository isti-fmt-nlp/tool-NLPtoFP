package view;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.LayeredHighlighter;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.events.StartDocument;

import view.EditorModel.StringWrapper;
import main.ModelXMLHandler;
import main.OrderedList;
import main.OrderedListNode;
import main.SortUtils;
import main.ViewXMLHandler;


public class EditorView extends JFrame implements Observer{
	
	/** variables used for debugging*/
	private static boolean debug=false;
	private static boolean debug2=false;
	private static boolean debug3=false;
	private static boolean debug4=false;
//	private static Robot eventsRobot = null;
	
	private static final long serialVersionUID = 1L;

	/** Class used to implement the editor. */
	class EditorSplitPane extends JSplitPane{

		public static final long serialVersionUID = 1L;

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
	
	/** Class used to filter project files. */
	class FilterFileProject implements FilenameFilter{
		@Override
		public boolean accept(File dir, String name){
			return name.endsWith( ".xml" );
	    }
	}

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
	
	
	/** URL of the new feature icon*/
	private static URL newFeatureIconURL=EditorView.class.getResource("/feature rectangle2.png");
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

	/** maps tool names in the corresponding icon resource path*/
	private static HashMap<String, String> toolIconPaths=null;
	
	private static int featureBorderSize=20;

	/** enumeration of items that can become active, for instance in a drag motion*/
	public static enum activeItems {
		NO_ACTIVE_ITEM, DRAGGING_FEATURE, DRAGGING_EXTERN_ANCHOR, DRAGGING_EXTERN_GROUP,
		DRAGGING_SELECTION_RECT, DRAGGING_SELECTION_GROUP, 
		DRAGGING_TOOL_NEWFEATURE, DRAGGING_TOOL_CONNECTOR, DRAGGING_TOOL_ALT_GROUP, DRAGGING_TOOL_OR_GROUP	
	}

	/** enumeration used to specify a item type through the program*/
	public static enum ItemsType {
		START_CONNECTOR, END_CONNECTOR, ALT_GROUP_START_CONNECTOR, OR_GROUP_START_CONNECTOR
	}
	
	/** Tells if the diagram has been modified after last save*/
	private boolean modified=true;
	
	/** The popup menu for all diagram panel elements*/
	private JPopupMenu diagramElementsMenu = new JPopupMenu();
	/** The element interested by the popup menu*/
	private JComponent popUpElement = null;
	
	/** Popup menu items*/
	private JMenuItem popMenuItemDelete = new JMenuItem("Delete Element");
	private JMenuItem popMenuItemDeleteFeature = new JMenuItem("Delete Feature");
	private JMenuItem popMenuItemDeleteConnector = new JMenuItem("Delete Connector");
	private JMenuItem popMenuItemDeleteGroup = new JMenuItem("Delete Group");
	private JMenuItem popMenuItemUngroup = new JMenuItem("Ungroup");
	private JMenuItem popMenuItemPrintModelDebug = new JMenuItem("Print Model[DEBUG COMMAND]");
	
	/** Popup menu coordinates*/
	private int diagramElementsMenuPosX=0;
	private int diagramElementsMenuPosY=0;
	
	
	/** The editor menu bar*/
	private JMenuBar menu = new JMenuBar();
	
	/** The menus in the menu bar*/
	private JMenu menuFiles;//Diagram Files Management Menu
	private JMenu menuView=null;//Diagram View Management Menu
	private JMenu menuModify=null;//Diagram Properties Management Menu
	
	/** Files Menu items*/
	private JMenuItem menuFilesSave=null, 	   menuFilesOpen=null,
					  menuFilesExportXML=null, menuFilesDelete=null, menuFilesExit=null;
	
	/** View Menu items*/
	private JMenuItem menuViewColored=null, menuViewCommsOrVars=null, 
					  menuViewExtrOrInsert=null, menuViewFields=null,
					  menuViewVisibleConstraints=null;
	
	/** Modify Menu items*/
	private JMenuItem menuModifyBasicFM=null, menuModifyAdvancedFM=null;
		
	
	/** Current amount of connector lines in the diagram panel*/
	private int connectorsCount=0;
	/** Current amount of Alternative Groups in the diagram panel*/
	private int altGroupsCount=0;
	/** Current amount of Or Groups in the diagram panel*/
	private int orGroupsCount=0;
	/** Current amount of features in the diagram panel*/
	private int featuresCount=0;
	
	/** List of all connector ending dots,
	 *  corresponding starting dots can be found in startConnectorDots at the same index
	 */
//	private static ArrayList<JComponent> endConnectorDots=null;
	
	//still unused
	/** Previous location of all connector starting dots, with same indexes of the lists above*/
//	private static ArrayList<Point> prevStartConnectorDotsLocation=null;
	
	//still unused
	/** Previous location of all connector ending dots, with same indexes of the lists above*/
//	private static ArrayList<Point> prevEndConnectorDotsLocation=null;
	
	//to revise
	/** List of all connector dots that must be redrawn, with same indexes of the lists above*/
//	private static ArrayList<Boolean> connectorDotsToRedraw=null;
	
	/** List of all connector starting dots,
	 *  corresponding ending dots can be found in endConnectorDots at the same index
	 */
	private ArrayList<JComponent> startConnectorDots=null;
	/** List of Alternative Groups*/
	private ArrayList<GroupPanel> altGroupPanels=null;	
	/** List of Or Groups*/
	private ArrayList<GroupPanel> orGroupPanels=null;
	
	/** List of starting commonalities selected by the user */
	private ArrayList<String> startingCommonalities=new ArrayList<String>();
	/** List of starting commonalities and variabilities selected by the user */
	private ArrayList<String> startingVariabilities=new ArrayList<String>();
	
	/** OrderedList containing the panels children of the diagram panel*/
	private OrderedList visibleOrderDraggables = null;
	
	/** X coordinate of last mouse pression*/
	private int lastPositionX=-1;
	/** Y coordinate of last mouse pression*/
	private int lastPositionY=-1;
	
	/** Top level frame*/
	private JFrame frameRoot = null;//frame root		
	
//	private static JFrame toolDragPanel = null;//temporary frame used to drag tools
	/** Image used to drag tools*/
	private BufferedImage toolDragImage = null;
	/** Position of the dragged image*/
	private Point toolDragPosition = null;
	
	
	/** The panel containing the diagram */
	private JLayeredPane diagramPanel=null;

	/** The panel containing the tools */
	private JPanel toolsPanel=null;
	
	/** The splitter panel containing diagramPanel and toolsPanel*/
	private EditorSplitPane splitterPanel=null;
	
	/** The active Feature panel*/
	private FeaturePanel lastFeatureFocused=null;
	/** The active Anchor panel*/
	private JComponent lastAnchorFocused=null;
	/** List of active elements selected as group by the user*/
	private ArrayList<JComponent> selectionGroupFocused=null;
	
	/** The component on which a drop is about to be done*/
	private JComponent underlyingComponent=null;
//	private static boolean isDraggingFeature=false;

	/** Tells what item is interested in the current action*/
	private activeItems isActiveItem=activeItems.NO_ACTIVE_ITEM;

	/** Variable used to draw lines on the diagram */
	private Point lineStart=new Point(), lineEnd=new Point();
	/** Variable used to draw selection rectangle on the diagram */
	private Point startSelectionRect=new Point(), endSelectionRect=new Point();
	/** Variable used to draw selection rectangle on the diagram */
	private Rectangle selectionRect=new Rectangle();
	

	public EditorView(){}
	
	public EditorView(ArrayList<String> commonalitiesSelected,
			   		  ArrayList<String> variabilitiesSelected) {
	  for(String name : commonalitiesSelected) startingCommonalities.add(name);
	  for(String name : variabilitiesSelected) startingVariabilities.add(name);
	}

	/**
	 * Initializes editor UI.
	 * @param editorController - the EditorController for this editor
	 * @return - false if editorController is null, true otherwise.
	 */
	public boolean prepareUI(EditorController editorController){
		if(editorController==null) return false;

		/* initializing JMenuBar */		
		menuFiles = new JMenu("Files");
		menuView = new JMenu("View");
		menuModify = new JMenu("Modify");

		/*Menu Files items*/
		menuFilesSave = new JMenuItem("Save Diagram");
		menuFilesSave.addActionListener(editorController);
		
		menuFilesOpen = new JMenuItem("Open Diagram");
		menuFilesOpen.addActionListener(editorController);
		
		menuFilesExportXML = new JMenuItem("Export as SXFM");
		menuFilesExportXML.addActionListener(editorController);
		
		menuFilesDelete = new JMenuItem("Delete Diagram");
		menuFilesDelete.addActionListener(editorController);
		
		menuFilesExit = new JMenuItem("Exit");
		menuFilesExit.addActionListener(editorController);
		
		menuFiles.add(menuFilesSave);
		menuFiles.add(menuFilesOpen);
		menuFiles.add(menuFilesExportXML);
		menuFiles.add(menuFilesDelete);
		menuFiles.add(menuFilesExit);

		/*Menu View items*/
		menuViewColored = new JCheckBoxMenuItem("Colour 'near' Features", false);
		menuViewColored.addActionListener(editorController);
		
		menuViewCommsOrVars = new JMenuItem("View Commonality/Variability");
		menuViewCommsOrVars.addActionListener(editorController);
		
		menuViewExtrOrInsert = new JMenuItem("View Extracted/Inserted");
		menuViewExtrOrInsert.addActionListener(editorController);
		
		menuViewFields = new JMenuItem("View Feature's Fields");
		menuViewFields.addActionListener(editorController);
		
		menuViewVisibleConstraints = new JMenuItem("View Diagram Constraints");
		menuViewVisibleConstraints.addActionListener(editorController);
		
		menuView.add(menuViewColored);
		menuView.add(menuViewCommsOrVars);
		menuView.add(menuViewExtrOrInsert);
		menuView.add(menuViewFields);
		menuView.add(menuViewVisibleConstraints);

		/*Menu Modify items*/
		menuModifyBasicFM = new JRadioButtonMenuItem("Basic Feature Model");
		menuModifyBasicFM.addActionListener(editorController);
		
		menuModifyAdvancedFM = new JRadioButtonMenuItem("Advanced Feature Model");
		menuModifyAdvancedFM.addActionListener(editorController);	
		
		ButtonGroup basicExtendedGroup = new ButtonGroup();
		basicExtendedGroup.add(menuModifyBasicFM);
		basicExtendedGroup.add(menuModifyAdvancedFM);
		
		menuModify.add(menuModifyBasicFM);
		menuModify.add(menuModifyAdvancedFM);
		
		menu.add(menuFiles);
		menu.add(menuView);
		menu.add(menuModify);
		
		setJMenuBar(menu);

		//initializing diagram popup menu
		popMenuItemDeleteFeature.setText("Delete Feature");
		popMenuItemDeleteFeature.setActionCommand("Delete Element");
		popMenuItemDeleteFeature.addActionListener(editorController);
		popMenuItemDeleteConnector.setText("Delete Connector");
		popMenuItemDeleteConnector.setActionCommand("Delete Element");
		popMenuItemDeleteConnector.addActionListener(editorController);
		popMenuItemDeleteGroup.setText("Delete Group");
		popMenuItemDeleteGroup.setActionCommand("Delete Element");
		popMenuItemDeleteGroup.addActionListener(editorController);
		
		popMenuItemDelete.addActionListener(editorController);        
        popMenuItemUngroup.addActionListener(editorController);        
        popMenuItemPrintModelDebug.addActionListener(editorController);        

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


		visibleOrderDraggables = new OrderedList();
		startConnectorDots = new ArrayList<JComponent>();
//		endConnectorDots = new ArrayList<JComponent>();
//		prevStartConnectorDotsLocation = new ArrayList<Point>();
//		prevEndConnectorDotsLocation = new ArrayList<Point>();
//		connectorDotsToRedraw = new ArrayList<Boolean>();
		altGroupPanels = new ArrayList<GroupPanel>();
		orGroupPanels = new ArrayList<GroupPanel>();

		selectionGroupFocused = new ArrayList<JComponent>();
		
		
		//creating root frame
//		frameRoot=new OrderedListPaintJFrame(visibleOrderDraggables);
		frameRoot=this;
		setLayout(new BorderLayout());		
//		frameRoot.setLayout(new BorderLayout());		
//		frameRoot.setLayout(null);		
//		frameRoot.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
		for(int i=0; i<4; ++i){
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


		diagramPanel.addMouseListener(editorController);
		diagramPanel.addMouseMotionListener(editorController);

		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setVisible(true);
		setLocation(0, 0);
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

		
		//adding starting commonalities and variabilities
		int i=10, j=10;
		for(String name : startingCommonalities){
		  System.out.println("diagramPanel.getWidth(): "+diagramPanel.getWidth());
		  if(i>=diagramPanel.getWidth()){ i=10; j+=70;}
		  FeaturePanel newFeature=getDraggableFeature(name, i, j);
		  visibleOrderDraggables.addToTop(newFeature);
		  diagramPanel.setLayer(newFeature, 0);
		  diagramPanel.add(newFeature);
		  diagramPanel.setComponentZOrder(newFeature, 0);
		  ++featuresCount; i+=70;
		}
		for(String name : startingVariabilities){
		  if(i>=diagramPanel.getWidth()){ i=10; j+=55;}
		  FeaturePanel newFeature=getDraggableFeature(name, i, j);
		  visibleOrderDraggables.addToTop(newFeature);
		  diagramPanel.setLayer(newFeature, 0);
		  diagramPanel.add(newFeature);
		  diagramPanel.setComponentZOrder(newFeature, 0);
		  ++featuresCount; i+=70;
		}
		
        return true;
	}

	@Override
	public void paint(java.awt.Graphics g) {
		int rectX=0, rectY=0, rectWidth=0, rectHeight=0;
		super.paint(g);

		Graphics2D g2 = (Graphics2D)g.create();
//		drawAllConnectors(g2);
		
//		paintComponents(g);
//		paintAll(g);
		
		
		/* ***DEBUG*** */
		if(debug3) System.out.println("Mi han chiamato, son la paint()");
		/* ***DEBUG*** */
		System.out.println("PAINT: isActiveItem="+isActiveItem);

		if(toolDragImage!=null) 
			g2.drawImage(toolDragImage, toolDragPosition.x+1, toolDragPosition.y+4, null);
		if(isActiveItem==activeItems.DRAGGING_SELECTION_RECT){
			System.out.println("Disegno il rect");
			g2.setColor(Color.BLUE);
//			rectX=(int)(startSelectionRect.getX()+diagramPanel.getX());
//			rectY=(int)(startSelectionRect.getY()+diagramPanel.getY());

//			rectX=(int)(startSelectionRect.getX());
//			rectY=(int)(startSelectionRect.getY());
//			rectWidth=(int)(endSelectionRect.getX()-startSelectionRect.getX());
//			rectHeight=(int)(endSelectionRect.getY()-startSelectionRect.getY());			
//			g2.drawRect(rectX, rectY, rectWidth, rectHeight);
			g2.setColor(Color.BLUE);
			g2.draw(selectionRect);
			
		}
		if(selectionGroupFocused.size()>0){
		  g2.setColor(Color.BLUE);
		  Rectangle elementSelectionFrame=new Rectangle();
		  for(Object selectedElement : selectionGroupFocused.toArray()){
			  System.out.println("selected element: "+((JComponent)selectedElement).getName()
					  +"coords: "+((JComponent)selectedElement).getLocationOnScreen());
			/*elementSelectionframe=*/((JComponent)selectedElement).getBounds(elementSelectionFrame);
			elementSelectionFrame.setLocation(
					(int)(elementSelectionFrame.getX()+diagramPanel.getLocationOnScreen().getX()-2 ),
					(int)(elementSelectionFrame.getY()+diagramPanel.getLocationOnScreen().getY() ));
			elementSelectionFrame.setSize(
					(int)elementSelectionFrame.getWidth()+6,
					(int)elementSelectionFrame.getHeight()+6);
			g2.draw(elementSelectionFrame);
		  }
		}
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

		//drawing connectors
		for (int i=0; i< startConnectorDots.size(); ++i){
		  drawConnectionLine(g2, startConnectorDots.get(i), ((AnchorPanel)startConnectorDots.get(i)).getOtherEnd());

		}

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
	private void drawGroupList(Graphics2D g2, ArrayList<GroupPanel> list, boolean filled) {
		GroupPanel startPanel=null;
		JComponent leftMost=null;
		JComponent rightMost=null;
		int[] orderedAnglesList=null;
		int minX;
		int maxX;
		Arc2D groupArc=null;
		
		Graphics2D tempGraphics = (Graphics2D)g2.create();

		  
		for (int i=0; i< list.size(); ++i){
		  startPanel=list.get(i);

		  groupArc=getGroupArc(startPanel, startPanel.getMembers());
		  
//		  leftMost = null; rightMost = null;
//		  minX=100000; maxX=-100000;
		  for (JComponent member : startPanel.getMembers()){
//			//searching for the 2 extern anchor of the group
//			if(member.getLocationOnScreen().getX()<minX){
//			  minX=(int)member.getLocationOnScreen().getX();
//			  leftMost=member;
//			}
//			if(member.getLocationOnScreen().getX()>maxX){
//			  maxX=(int)member.getLocationOnScreen().getX();
//			  rightMost=member;
//			}
			drawConnectionLine(g2, startPanel, member);				
		  }
		  
		  /* ***DEBUG*** */
		  if(debug3) System.out.println(
				  "\nLeftMost:"+leftMost
				  +"\nrightMost:"+rightMost);
		  /* ***DEBUG*** */

		  
		  //drawing the group arc
		  if(!filled){
			  groupArc.setArcType(Arc2D.Double.OPEN);
			  tempGraphics.draw(groupArc);
		  }
		  else{
			  groupArc.setArcType(Arc2D.Double.PIE);
			  tempGraphics.fill(groupArc);
		  }
		  
//		  //drawing the group arc
//		  drawGroupArc(g2, startPanel, leftMost, rightMost, filled);
		}
	}

	/**
	 * Returns an Arc2D object that intersects each connector lines of the group.<br>
	 * The arc is calculated using the angles corresponding to connector lines.
	 * 
	 * @param groupPanel - the GroupPanel of the group in the diagram panel
	 * @param members - ArrayList containing the AnchorPanel objects of group members
	 * @return - an Arc2D object that intersects each connector lines 
	 */
	private Arc2D getGroupArc(GroupPanel groupPanel, ArrayList<AnchorPanel> members) {
		int[] angles= new int[members.size()];
		Point2D startCenter=getVisibleStartAnchorCenter(groupPanel);
		Point2D childCenter=null;
		double centreX=startCenter.getX();
		double centreY=startCenter.getY();
		double radius=1000000;
		double lineLength=0;
		Arc2D groupArc=null;
		int index=0;
		int angleGap=0;
		int arcStartPoint=0;

		//calculating arc minimum radius and intersection points angles
		for(AnchorPanel groupChild : members){
		  childCenter=getVisibleStartAnchorCenter(groupChild);

		  //getting line length
		  lineLength=Math.sqrt(Math.pow(childCenter.getX()-startCenter.getX(), 2)
				  			   +Math.pow(childCenter.getY()-startCenter.getY(), 2));
		  if(lineLength<radius) radius=lineLength;

//		  //getting intersection point
//		  intersectionPoints = getCircleLineIntersectionPoints(startCenter, childCenter, startCenter, 100);
//		  if(new Line2D.Double(startCenter, childCenter).ptSegDist(intersectionPoints.get(0))==0)
//			intersectionPoint=intersectionPoints.get(0);
//		  else intersectionPoint=intersectionPoints.get(1);

		  //adding the corresponding angle to angles array
		  angles[index++]=getDegreeAngle(centreX, centreY, childCenter.getX(), childCenter.getY(), lineLength);
//		  angles[index++]=getDegreeAngle(centreX, centreY, intersectionPoint.getX(), intersectionPoint.getY(), lineLength);
		  
		}
		
		//sorting the array
		SortUtils.recQuickSort(angles, 0, angles.length-1);

		/* ***DEBUG*** */
		if(debug3) {
		  System.out.println("!!!PRINTING SORTED ANGLES!!!");
		  for(int angle : angles){
			System.out.println("***"+angle);
		  }
		}
		/* ***DEBUG*** */
		
		//searching for the maximum degree gap between two subsequent points
		for(index=0; index<angles.length-1; ++index)
		  if (angles[index+1]-angles[index] > angleGap){
			angleGap=angles[index+1]-angles[index];
			arcStartPoint=angles[index+1];
		  }
		//checking gap between first and last points
		if ((360-angles[angles.length-1])+angles[0] > angleGap){
			angleGap=(360-angles[angles.length-1])+angles[0];
			arcStartPoint=angles[0];
		}
		
		//creating circle
//		groupArc = new Arc2D.Double(startCenter.getX()-radius, startCenter.getY()-radius,
//				radius*2, radius*2, 0, 360, Arc2D.Double.OPEN);
		groupArc = new Arc2D.Double(startCenter.getX()-radius/3, startCenter.getY()-radius/3,
				radius*2/3, radius*2/3, 0, 360, Arc2D.Double.OPEN);

		/* ***DEBUG*** */
		if(debug3) {
		  System.out.println("!!!AngleStart: "+arcStartPoint+"\tAngleExtent!!!"+(360-angleGap));			
		}
		/* ***DEBUG*** */

		groupArc.setAngleStart(arcStartPoint);
		groupArc.setAngleExtent(360-angleGap);
		
		return groupArc;
	}

	/**
	 * Returns the angle(in degree metric) corresponding to the line from the specified centre to the point.
	 * 
	 * @param centreX - the X coordinate of the centre
	 * @param centreY - the Y coordinate of the centre
	 * @param pointX - the X coordinate of the point
	 * @param pointY - the Y coordinate of the point
	 * @param radius - the distance from centre to point
	 * 
	 * @return - the corresponding angle, in degree metric
	 */
	private int getDegreeAngle(double centreX, double centreY, double pointX, double pointY, double radius) {
		double cosin=(pointX-centreX)/radius;
		double sin=(pointY-centreY)/radius;
		double acos = Math.acos(cosin);
		double asin = Math.asin(sin);

//		//using acos and asin calculated values to get actual angle in degree
//		if(asin==0) return 0;
//		if(asin>0) return (int)Math.toDegrees(acos);
//		else{
//		  if(acos<Math.PI/2) return (int)Math.toDegrees(Math.PI+asin);
//		  else return (int)Math.toDegrees(Math.PI/2-asin);
//		}

		asin*=-1;
		//using acos and asin calculated values to get actual angle in degree
//		if(asin==0) return 0;
		if(asin>0) return (int)Math.toDegrees(acos);
		else{
		  if(acos<Math.PI/2) return (int)Math.toDegrees(2*Math.PI+asin);
		  else return (int)Math.toDegrees(Math.PI-asin);
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

	private void drawConnectionLine(Graphics2D g2, JComponent startPanel, JComponent endPanel) {
		lineStart.setLocation(getVisibleStartAnchorCenter(startPanel));
		lineEnd.setLocation(getVisibleStartAnchorCenter(endPanel));
//		start.setLocation(startPanel.getLocationOnScreen());
//		end.setLocation(endPanel.getLocationOnScreen());
		g2.drawLine(
//				  (int)(start.getX()-splitterPanel.getLocationOnScreen().getX()+startPanel.getWidth()/2),
//				  (int)(start.getY()-splitterPanel.getLocationOnScreen().getY()+startPanel.getHeight()/2+3),
//				  (int)(end.getX()-splitterPanel.getLocationOnScreen().getX()+endPanel.getHeight()/2),
//				  (int)(end.getY()-splitterPanel.getLocationOnScreen().getY()+endPanel.getHeight()/2+3) );
		  (int)lineStart.getX(), (int)lineStart.getY(), (int)lineEnd.getX(), (int)lineEnd.getY() );
//		  (int)(end.getX()-splitterPanel.getLocationOnScreen().getX()+endPanel.getHeight()/2-3),
//		  (int)(end.getY()-splitterPanel.getLocationOnScreen().getY()+endPanel.getHeight()/2+2) );
	};
	
	/**
     * Returns a Point2D representing the visible center of a starting anchor image on the splitterPanel coordinates system.
     * 
     * @param anchor - the JComponent representing a visible starting anchor
     * @return the visible center point of the anchor
     */
    public Point2D getVisibleStartAnchorCenter(JComponent anchor) {
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

	/**
	 * Moves a component in the diagram panel to the top layer.
	 * 
	 * @param comp - the component to move to top.
	 */
	public void moveComponentToTop(JComponent comp) {
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
	 * Moves all components in the selection group to the top layer of diagram panel.
	 * 
	 */
	public void moveSelectionGroupToTop() {
	  for(JComponent element : selectionGroupFocused) moveComponentToTop(element);
	}

	/**
	 * Drags an anchor panel inside the diagram panel.
	 *
	 * @param e - the current MouseEvent
	 */
	public void dragAnchor(MouseEvent e) {
	  if(lastAnchorFocused==null) return;
	  dragDiagramElement(lastAnchorFocused, e);
//		  diagramPanel.repaint();
	  frameRoot.repaint();	
	}

	/**
	 * Drags a feature panel inside the diagram panel.
	 *
	 * @param e - the current MouseEvent
	 */
	public void dragFeature(MouseEvent e) {
	  if(lastFeatureFocused==null) return;
	  dragDiagramElement(lastFeatureFocused, e);
//	  diagramPanel.repaint();
	  frameRoot.repaint();
	}

	/**
	 * Drags the selection rectangle, updating the two defining points.
	 *
	 * @param e - the current MouseEvent
	 */
	public void dragSelectionRect(MouseEvent e) {
	  System.out.println("start: "+startSelectionRect+"\tend: "+e.getLocationOnScreen().getLocation());
//	  endSelectionRect=e.getLocationOnScreen().getLocation();


	  selectionRect.setFrameFromDiagonal(startSelectionRect, e.getLocationOnScreen().getLocation());  	  
	  
	  frameRoot.repaint();	
	}

	/**
	 * Drags all components in the selection group inside the diagram panel.
	 *
	 * @param e - the current MouseEvent
	 */
	public void dragSelectionGroup(MouseEvent e) {
		  int moveX=0, moveY=0;
		  int adjustedMoveX=0, adjustedMoveY=0;	  
		  int newLocationX=0, newLocationY=0;
		  boolean normalUpdateX=true, normalUpdateY=true;

		  moveX = e.getX()-lastPositionX;
		  moveY = e.getY()-lastPositionY;
		  

		for(JComponent element : selectionGroupFocused){		  
		  newLocationX=element.getX()+moveX;
		  newLocationY=element.getY()+moveY;
		  
		  //the feature must not be dragged beyond the borders of the diagram panel
		  if( newLocationX<0 ){
			newLocationX=1;
			normalUpdateX=false;
			adjustedMoveX=newLocationX-element.getX();
		  }
		  if( diagramPanel.getWidth()<=newLocationX+element.getWidth() ){
			newLocationX=diagramPanel.getWidth()-element.getWidth()-1;
			normalUpdateX=false;
			adjustedMoveX=newLocationX-element.getX();
		  }
		  if( newLocationY<0 ){
			newLocationY=1;
			normalUpdateY=false;
			adjustedMoveY=newLocationY-element.getY();
		  }
		  if( diagramPanel.getHeight()<=newLocationY+element.getHeight() ){
			newLocationY=diagramPanel.getHeight()-element.getHeight()-1;
			normalUpdateY=false;
			adjustedMoveY=newLocationY-element.getY();
		  }

		  //adjusting last drag position depending on eventual border collisions
		  if(normalUpdateX) lastPositionX=e.getX();
		  else lastPositionX=lastPositionX+adjustedMoveX;

		  if(normalUpdateY) lastPositionY=e.getY();
		  else lastPositionY=lastPositionY+adjustedMoveY;

		  if(!normalUpdateX&&normalUpdateY) break;
		  element.setLocation(newLocationX, newLocationY);
		}
		frameRoot.repaint();	
		
	}

	/**
	 * Drags an element inside the diagram panel.
	 *
	 * @param element - the element to drag
	 * @param e - the current MouseEvent
	 */
	private void dragDiagramElement(JComponent element, MouseEvent e) {
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
	public void dragToolConnector(MouseEvent e) {
		  dragTool(e);
	}

	/**
	 * Drags a NewFeature tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	public void dragToolNewFeature(MouseEvent e) {
		  dragTool(e);
	}

	/**
	 * Drags an Alternative Group tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	public void dragToolAltGroup(MouseEvent e) {
		  dragTool(e);
	}
	
	/**
	 * Drags an Or Group tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	public void dragToolOrGroup(MouseEvent e) {
		  dragTool(e);
	}
	
	
	/**
	 * Drags a generic tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	private void dragTool(MouseEvent e) {
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
	public Component dropAnchorOnDiagram(MouseEvent e) {
	  Component comp = null;
	  int anchorPanelOnScreenX =0;
	  int anchorPanelOnScreenY =0;
	  Point relativePosition=null;
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
			
		  //anchor dropped over a feature panel
		  if (((Component)tmpNode.getElement()).getName().startsWith(featureNamePrefix)){

			//checking if an ending anchor has been dropped over a feature that already owns an ending anchor
			if(lastAnchorFocused.getName().startsWith(endConnectorsNamePrefix)){
			  for( Component child : ((FeaturePanel)tmpNode.getElement()).getComponents())
				if (child.getName()!=null && child.getName().startsWith(endConnectorsNamePrefix)) return null;				
			}
			
//			underlyingPanel=(FeaturePanel)tmpNode.getElement();
			underlyingComponent=(JComponent)tmpNode.getElement();			  

			//checking if anchor must be added to a group
			anchorPanelOnScreenX=(int)lastAnchorFocused.getLocationOnScreen().getX();
			anchorPanelOnScreenY=(int)lastAnchorFocused.getLocationOnScreen().getY();
			relativePosition = new Point((int)(anchorPanelOnScreenX-underlyingComponent.getLocationOnScreen().getX()),
					(int)(anchorPanelOnScreenY-underlyingComponent.getLocationOnScreen().getY()) );	
			  
			if(lastAnchorFocused.getName().startsWith(startConnectorsNamePrefix)){
			  comp = underlyingComponent.getComponentAt(relativePosition);
			  if (comp!=null && comp.getName()!=null && (comp.getName().startsWith(altGroupNamePrefix) 
				  || comp.getName().startsWith(orGroupNamePrefix) )){
//			    startAnchor=(AnchorPanel)lastAnchorFocused;
//			    group=(GroupPanel)comp;
				underlyingComponent=(JComponent)comp;
//				return underlyingComponent;
//				addStartAnchorToGroup(startAnchor, group);
//			    return true;
			  }
			}			  
			  
			  
//			addAnchorToFeature(lastAnchorFocused, underlyingPanel);
//			frameRoot.repaint();
			return underlyingComponent;					  
		  }
		  //anchor dropped directly over the diagram panel
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
	public Component dropGroupOnDiagram(MouseEvent e) {
	  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
	  while(tmpNode!=null){	  
	    if ( tmpNode.getElement().getClass().equals(FeaturePanel.class) &&
			 ((FeaturePanel)tmpNode.getElement()).getName().startsWith(featureNamePrefix) &&
		     ((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY()) ){			
	      underlyingComponent=(JComponent)tmpNode.getElement();
	      return underlyingComponent;
//		  addAnchorToFeature(lastAnchorFocused, underlyingPanel);
//		  frameRoot.repaint();
//		  break;		
	    }
	    tmpNode=tmpNode.getNext();
	  }				
	  return null;
	}
	



	/**
	 * Removes a visible anchor from the diagram panel and attach it to a feature panel.<br>
	 * 
	 * @return - false if the operation is not possible, true otherwise
	 */
	public void addAnchorToFeature() {
		int anchorPanelOnScreenX;
		int anchorPanelOnScreenY;
		
//		if(lastAnchorFocused.getParent()==null || !lastAnchorFocused.isDisplayable()) return false;
		
		moveComponentToTop(underlyingComponent);

		//if it is an ending anchor, location is set to top-middle of underlying feature
		if(lastAnchorFocused.getName().startsWith(endConnectorsNamePrefix)){
		  lastAnchorFocused.setLocation(underlyingComponent.getWidth()/2-lastAnchorFocused.getWidth()/2, 0);		
		}
		
		//if it is a starting anchor, location is set relative to underlying feature, on the same visible location
		else {
		  anchorPanelOnScreenX=(int)lastAnchorFocused.getLocationOnScreen().getX();
		  anchorPanelOnScreenY=(int)lastAnchorFocused.getLocationOnScreen().getY();
		  lastAnchorFocused.setLocation((int)(anchorPanelOnScreenX-underlyingComponent.getLocationOnScreen().getX()),
				  (int)(anchorPanelOnScreenY-underlyingComponent.getLocationOnScreen().getY()) );		
		}

		//Adding anchor to the feature
		diagramPanel.remove(lastAnchorFocused);
		diagramPanel.validate();
		((JLayeredPane)underlyingComponent).setLayer(lastAnchorFocused, 0);
		((JLayeredPane)underlyingComponent).add(lastAnchorFocused);
		((JLayeredPane)underlyingComponent).setComponentZOrder(lastAnchorFocused, 0);

		return;
	}

	/**
	 * Check each draggable element in the diagram panel and adds it to the <br>
	 * selection list if it's inside the selection rectangle.
	 * 
	 * @param e
	 */
	public void createSelectionGroup(MouseEvent e) {
	  Component comp=null;
	  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
	  Point locOnScreen=null;
	  while(tmpNode!=null){
		locOnScreen=((Component)tmpNode.getElement()).getLocationOnScreen();
		if(selectionRect.contains(locOnScreen)){
		  System.out.println("Checking: "+((Component)tmpNode.getElement()).getName());

		  //if it's not a feature panel, we check that it's not anchored to a feature panel
		  if (!((Component)tmpNode.getElement()).getName().startsWith(featureNamePrefix)){

			  comp = (JComponent) diagramPanel.getComponentAt(				
				(int)(locOnScreen.getX()-diagramPanel.getLocationOnScreen().getX()), 
				(int)(locOnScreen.getY()-diagramPanel.getLocationOnScreen().getY()) );
			  
//			  comp=getUnderlyingComponent(
//				(int)(locOnScreen.getX()-diagramPanel.getLocationOnScreen().getX()), 
//				(int)(locOnScreen.getY()-diagramPanel.getLocationOnScreen().getY()) );

			System.out.println("Underlying comp: "+comp.getName());
			if(comp!=null && comp.getName().startsWith(featureNamePrefix)){
			  tmpNode=tmpNode.getNext(); continue;
			}
		  }
		  
		  System.out.println("About to add to group: "+((Component)tmpNode.getElement()).getName());
		  //if it's not anchored to a feature, or it's a feature, it gets added to group
		  selectionGroupFocused.add((JComponent) tmpNode.getElement());
		}
		tmpNode=tmpNode.getNext();						
	  }
	  System.out.println("Selected Group Elements:\n");
	  for(JComponent elem : selectionGroupFocused) System.out.println(elem.getName());
	  frameRoot.repaint();
	}

	/**
	 * Removes a starting connector anchor from the diagram panel and attach it to a group.
	 */
	private void addStartAnchorToGroup() {
		((GroupPanel)underlyingComponent).getMembers().add((AnchorPanel)((AnchorPanel)lastAnchorFocused).getOtherEnd());
		((AnchorPanel)((AnchorPanel)lastAnchorFocused).getOtherEnd()).setOtherEnd(underlyingComponent);
		diagramPanel.remove(lastAnchorFocused);
		diagramPanel.validate();
		visibleOrderDraggables.remove(lastAnchorFocused);
		startConnectorDots.remove(lastAnchorFocused);
		frameRoot.repaint();
		return;
	}

//	/**
//	 * Removes a starting connector anchor from the diagram panel and attach it to a group.<br>
//	 * 
//	 * @param startAnchor - the starting connector anchor to be added
//	 * @param group - the group 
//	 */
//	private static void addStartAnchorToGroup(AnchorPanel startAnchor, GroupPanel group) {
//		group.getMembers().add((AnchorPanel)startAnchor.getOtherEnd());
//		((AnchorPanel)startAnchor.getOtherEnd()).setOtherEnd(group);
//		diagramPanel.remove(startAnchor);
//		diagramPanel.validate();
//		visibleOrderDraggables.remove(startAnchor);
//		startConnectorDots.remove(startAnchor);
//		return;
//	}

	/**
	 * Adds a new feature to the diagram panel, incrementing featuresCount.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	public void addNewFeatureToDiagram(/*MouseEvent e*/) {
		
		//the new feature must be dropped on the diagram panel for it to be added
		if( diagramPanel.getLocationOnScreen().getX()>toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getWidth()<=toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getY()>toolDragPosition.y ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getHeight()<=toolDragPosition.y ){
			
			cancelToolDrag();

			/* ***DEBUG*** */
			if(debug4) System.out.println("Cannot drop a new feature on tools panel.");
			/* ***DEBUG*** */
			return;
		}

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
	public void addConnectorToDiagram(MouseEvent e) {
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

			newConnectorStartDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.START_CONNECTOR, 
				toolDragPosition.x-(int)featurePanel.getLocationOnScreen().getX(),
				toolDragPosition.y-(int)featurePanel.getLocationOnScreen().getY()-5);			

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

		newConnectorEndDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.END_CONNECTOR,
			actualPositionX+lineLengthIcon.getIconWidth()+startConnectorIcon.getIconWidth(),
			actualPositionY-5+lineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

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
//		++connectorsCount;

		cancelToolDrag();
	}

	public void addOrGroupToDiagram(MouseEvent e) {
		GroupPanel newGroupStartDot = addGroupToDiagram(e, ItemsType.OR_GROUP_START_CONNECTOR);
		if (newGroupStartDot==null) return;
		addOrGroupToDrawLists(newGroupStartDot);
	}

	/**
	 * Adds a new Alternative Group to the diagram. If the starting connector dot is dropped over a feature panel, <br>
	 * it gets attached to it.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	public void addAltGroupToDiagram(MouseEvent e) {
		GroupPanel newGroupStartDot = addGroupToDiagram(e, ItemsType.ALT_GROUP_START_CONNECTOR);
		if (newGroupStartDot==null) return;
		addAltGroupToDrawLists(newGroupStartDot);
	}

	/**
	 * Adds a new group to the diagram. If the starting connector dot is dropped over a feature panel, <br>
	 * it gets attached to it.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 * @param requestedType - the type of the requested group, an ItemsType value.
	 */
	private GroupPanel addGroupToDiagram(MouseEvent e, ItemsType requestedType) {
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

			newGroupStartDot=(GroupPanel)getDraggableConnectionDot(requestedType, 
				toolDragPosition.x-(int)featurePanel.getLocationOnScreen().getX()
					+groupIcon.getIconWidth()/2-startConnectorIcon.getIconWidth()/2,
				toolDragPosition.y-(int)featurePanel.getLocationOnScreen().getY()-5);			

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
	 * @return A new JComponent representing the connection dot
	 */
	private JComponent getDraggableConnectionDot(ItemsType type, int x, int y) {
		return buildConnectionDot(type, null, x, y);
	}

	/**
	 * Creates a JComponent containing a Connection Dot with the specified name, <br>
	 * or with a sequential name given by the static variable connectorsCount if name is null. 
	 * 
	 * @param x - x coordinate of the connection dot in the diagram panel
	 * @param y - y coordinate of the connection dot in the diagram panel
	 * @param name - String representing the name of the connection dot
	 * @return A new JComponent representing the connection dot
	 */
	private JComponent buildConnectionDot(ItemsType type, String name, int x, int y) {
		JComponent imagePanel=null;

		ImageIcon connectorIcon=null;
		JLabel imageLabel = null;

		switch(type){
		  case START_CONNECTOR:
			imagePanel = new AnchorPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(startConnectorsNamePrefix+connectorsCount);
			  ++connectorsCount;
			}
			connectorIcon = new ImageIcon(connectorStartDotIconURL);
			break;
		  case END_CONNECTOR:
			imagePanel = new AnchorPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(endConnectorsNamePrefix+connectorsCount);
			  ++connectorsCount;
			}
			connectorIcon = new ImageIcon(connectorEndDotIconURL);
			break;
		  case ALT_GROUP_START_CONNECTOR:
			imagePanel = new GroupPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(altGroupNamePrefix+altGroupsCount);
			  ++altGroupsCount;
			}
			connectorIcon = new ImageIcon(ALTGroupDotIconURL);
			break;
		  case OR_GROUP_START_CONNECTOR:
			imagePanel = new GroupPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(orGroupNamePrefix+orGroupsCount);
			  ++orGroupsCount;
			}
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
	private FeaturePanel getDraggableFeature(String name, int x, int y) {
		return buildFeaturePanel(name, null, x, y);
	}

	/**
	 * Creates a JPanel containing a Feature
	 * 
	 * @param name - the name of the feature
	 * @param containerName - the name of the FeaturePanel object
	 * @param x - x coordinate of the feature in the diagram panel
	 * @param y - y coordinate of the feature in the diagram panel
	 * @return A new JPanel representing the feature
	 */
	private FeaturePanel buildFeaturePanel(String name, String containerName, int x, int y) {
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
		container.setLabelName(name);
		if(containerName==null) container.setName(featureNamePrefix+featuresCount);
		else container.setName(containerName);
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
	private JComponent getUnderlyingComponent(int x, int y) {
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
	private void addConnectorsToDrawLists(JComponent newConnectorStartDot, JComponent newConnectorEndDot) {
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
	private void addAltGroupToDrawLists(GroupPanel group) {
		altGroupPanels.add(group);		
	}

	/**
	 * Adds the Or Group to the lists used to draw connectors.
	 * 
	 * @param group - the GrouPanel object to be added to the lists
	 */
	private void addOrGroupToDrawLists(GroupPanel group) {
		orGroupPanels.add(group);		
	}
	
	/**
	 * Deletes a feature from the diagram.
	 * 
	 * @param feature - the feature to delete
	 */
	public void deleteFeature(JComponent feature) {
	  //attaching all feature anchors to the diagram
  	  for(Component comp : feature.getComponents())
  		if(comp.getName()!=null 
  		   && ( comp.getName().startsWith(altGroupNamePrefix) || comp.getName().startsWith(orGroupNamePrefix) 
  				|| comp.getName().startsWith(endConnectorsNamePrefix) 
  				|| comp.getName().startsWith(startConnectorsNamePrefix)
  			  ) ){
  		  detachAnchor((FeaturePanel)feature, (JComponent)comp);
  		}
  	  diagramPanel.remove(feature);
  	  visibleOrderDraggables.remove(feature);		
	}	
	
	/**
	 * Deletes a group from the diagram.
	 * 
	 * @param group - the group to delete
	 */
	private void deleteGroup(JComponent group) {
	  for(AnchorPanel member : ((GroupPanel)group).getMembers()) deleteAnchor(member);
	  diagramPanel.remove(group);
	  visibleOrderDraggables.remove(group);
	  if(group.getName().startsWith(altGroupNamePrefix)) altGroupPanels.remove(group);
	  if(group.getName().startsWith(orGroupNamePrefix)) orGroupPanels.remove(group);
	}
	
	/**
	 * Ungroups an ending anchor from his group, then connects it with a starting anchor <br>
	 * attached to the diagram and named accordingly.
	 * 
	 * @param anchor - the anchor to ungroup
	 */ 
	public void ungroupAnchor(AnchorPanel anchor) {
	  int startDotlocationX=0;
	  int startDotlocationY=0;
	  GroupPanel group = (GroupPanel)anchor.getOtherEnd();
	  System.out.println("Group size: "+group.getMembers().size());
	  if(group.getMembers().size()<=2){
		System.out.println("Ungroup is not possible, this group is already minimal.");
		return;
	  }
	  AnchorPanel startConnectorDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.START_CONNECTOR, 0, 0);
	  startDotlocationX=(int)(anchor.getLocationOnScreen().getX()-diagramPanel.getLocationOnScreen().getX());
	  startDotlocationY=(int)(anchor.getLocationOnScreen().getY()-diagramPanel.getLocationOnScreen().getY());
	  group.getMembers().remove(anchor);
	  if(startDotlocationX-(anchor.getWidth()*2)>0)
		  startDotlocationX=startDotlocationX-(anchor.getWidth()*2);
	  else startDotlocationX=startDotlocationX+(anchor.getWidth()*2);
//	  if(startDotlocationX>=diagramPanel.getWidth()-startConnectorDot.getWidth())
//		  startDotlocationX=diagramPanel.getWidth()-startConnectorDot.getWidth();
	  if(startDotlocationY-(anchor.getHeight()*2)>0)
		  startDotlocationY=startDotlocationY-(anchor.getHeight()*2);
	  else startDotlocationY=startDotlocationY+(anchor.getHeight()*2);
//	  if(startDotlocationY>=diagramPanel.getHeight()-startConnectorDot.getHeight())
//		  startDotlocationY=diagramPanel.getHeight()-startConnectorDot.getHeight();
	  
	  startConnectorDot.setLocation(startDotlocationX, startDotlocationY);
	  startConnectorDot.setName(startConnectorsNamePrefix+anchor.getName().substring(anchor.getName().indexOf("#")+1));
	  startConnectorDot.setOtherEnd(anchor);
	  anchor.setOtherEnd(startConnectorDot);
	  visibleOrderDraggables.addToTop(startConnectorDot);
	  startConnectorDots.add(startConnectorDot);
	  
	  diagramPanel.setLayer(startConnectorDot, 0);
	  diagramPanel.add(startConnectorDot);
	  diagramPanel.setComponentZOrder(startConnectorDot, 0);
	}
	
	/**
	 * Detach an anchor or group from the feature featurePanel, attaching it to the diagram.
	 * 
	 * @param feature - the feature from wich the anchor must be detached
	 * @param anchor - the anchor to detach
	 */
	public void detachAnchor(FeaturePanel feature, JComponent anchor) {
		int anchorPanelOnScreenX;
		int anchorPanelOnScreenY;
		
		anchorPanelOnScreenX=(int)anchor.getLocationOnScreen().getX();
		anchorPanelOnScreenY=(int)anchor.getLocationOnScreen().getY();
		feature.remove(anchor);
		feature.validate();
		System.out.println("feature="+feature.getName());
		System.out.println("anchor="+anchor.getName());
//		System.out.println("lastFeatureFocused="+lastFeatureFocused);
//		System.out.println("lastAnchorFocused="+lastAnchorFocused);
//		lastFeatureFocused.remove(lastAnchorFocused);
//		lastFeatureFocused.validate();
		
		anchor.setLocation(
		  (int)(anchorPanelOnScreenX-diagramPanel.getLocationOnScreen().getX()),
		  (int)(anchorPanelOnScreenY-diagramPanel.getLocationOnScreen().getY()));
		diagramPanel.setLayer(anchor, 0);
		diagramPanel.add(anchor);
		diagramPanel.setComponentZOrder(anchor, 0);
//		EditorView.moveComponentToTop(anchor);
		moveComponentToTop(anchor);
	}
	
	/**
	 * Deletes an anchor from the diagram.
	 * 
	 * @param anchor - the anchor to delete
	 */
	public void deleteAnchor(JComponent anchor) {
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
	
	/** Returns the 'Delete Feature' popup menu item */
	public JMenuItem getPopMenuItemDeleteFeature(){
		return popMenuItemDeleteFeature;
	};
	
	/** Returns the 'Delete Connector' popup menu item */
	public JMenuItem getPopMenuItemDeleteConnector(){
		return popMenuItemDeleteConnector;
	};
	
	/** Returns the 'Delete Group' popup menu item */
	public JMenuItem getPopMenuItemDeleteGroup(){
		return popMenuItemDeleteGroup;
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
	
	/**
	 * Resets the static variables used during drag operations.
	 */
	private void resetActiveItems(){
		isActiveItem=activeItems.NO_ACTIVE_ITEM;
		lastAnchorFocused=null;
		lastFeatureFocused=null;
	}
	
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
	
	/** Returns the starting point on the diagram of the selection rectangle*/
	public Point getStartSelectionRect(){
		return startSelectionRect;
	};
	
	/** Sets the starting point on the diagram of the selection rectangle*/
	public void setStartSelectionRect(Point p){
		startSelectionRect=p;
	};	
	
	/** Returns the ending point on the diagram of the selection rectangle*/
	public Point getEndSelectionRect(){
		return endSelectionRect;
	};
	
	/** Sets the ending point on the diagram of the selection rectangle*/
	public void setEndSelectionRect(Point p){
		endSelectionRect=p;
	};	
	
	/** Returns the selection rectangle*/
	public Rectangle getSelectionRect(){
		return selectionRect;
	};
	
	/** Sets the selection rectangle*/
	public void setSelectionRect(Rectangle p){
		selectionRect=p;
	};	
	
	/** Returns the selection group*/
	public ArrayList<JComponent> getSelectionGroup(){
		return selectionGroupFocused;
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

	/** Tells if the view has been modified since last save*/
	public boolean getModified(){
		return modified;
	}

	/** Sets the value of the modified field*/
	public void setModified(boolean mod){
		modified=mod;
	}
	

	/**
	 * Reset item used for dragging tools.*/
	private void cancelToolDrag() {
		toolDragImage=null;
		toolDragPosition=null;
		frameRoot.repaint();
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
	public boolean checkDroppedOnDiagram() {
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

	/** shows the popup menu on the diagram, at the clicked location.*/
	public void showDiagramElementsMenu(){
	    diagramElementsMenu.show(diagramPanel, diagramElementsMenuPosX, diagramElementsMenuPosY);		
	}

	/** Repaints frameRoot.*/
	public void repaintRootFrame(){
		frameRoot.repaint();		
	}

	@Override
	public void update(Observable o, Object arg) {
		System.out.println("Message received: "+arg);
		if(arg.equals("New Feature Correctly Added")) addNewFeatureToDiagram();			
		else if(arg.equals("Feature Deleted")) deleteFeature(popUpElement);
		else if(arg.equals("Group Deleted")) deleteGroup(popUpElement);
		else if(arg.equals("Feature Not Deleted"))
			System.out.println("Cannot delete this feature!");
		else if(arg.equals("Grouped a Feature")
			     || arg.equals("Two Features Directly Linked")){
			addAnchorToFeature();
		}
		else if(arg.equals("Group Added To Feature") ) addAnchorToFeature();	
		else if(arg.equals("Merged a Connector") ) addStartAnchorToGroup();
		else if(arg.equals("Not Grouped a Feature")
			     || arg.equals("Two Features Not Linked")
			     || arg.equals("Group Not Added To Feature")
			     || arg.equals("Not Merged a Connector")){
			System.out.println("Operation would create a cycle and it has been aborted!");
		}
		else if(arg.equals("Direct Link Destroyed")
				|| arg.equals("Group Removed From Feature")){
			detachAnchor(lastFeatureFocused, lastAnchorFocused);
		}
		else if(arg.equals("Direct Link Not Destroyed")
				|| arg.equals("Group Not Removed From Feature")){
			resetActiveItems();
		}
		else if(arg.equals("Direct Link Not Destroyed") ) resetActiveItems();
	}

	/*
	/**
	 * Main Method.
	 * @param args
	 */
	/*
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
		


	}*/

	
	/** 
	 * Assigns a name to the diagram to be saved.
	 * 
	 * @return s - String representing the diagram name, or null if dialog has been aborted
	 */
	public String assignNameDiagramDialog(){				
	  return assignNameDialog("Diagram name: ");	  
	}
	
	/** 
	 * Assigns a name to the SXFM file to be created as result of model exportation.
	 * 
	 * @return s - String representing the SXFM name, or null if dialog has been aborted
	 */
	public String assignNameSXFMDialog(){				
	  return assignNameDialog("SXFM filename: ");	  
	}

	/** 
	 * opens a dialog to ask user for a name.
	 * 
	 * @return s - String representing the name, or null if dialog has been aborted
	 */
	private String assignNameDialog(String message) {
		String s = null;			
		  JTextField jtf = new JTextField();
			 	
		  Object[] o1 = {message, jtf};
		  Object[] o2 = { "Cancel", "OK" };
			    
		  int i = JOptionPane.showOptionDialog(new JFrame("Save Diagram"), o1, "",
				  JOptionPane.YES_NO_OPTION, JOptionPane.DEFAULT_OPTION, null, o2, o2[1]);
			    
		  if(i == JOptionPane.NO_OPTION){
			if((s = jtf.getText()) != null){
			  if(!s.trim().equals("")) return s;
			  else{
				errorDialog("Invalid name");
				return null;
			  }
			}
			else{
			  errorDialog("Invalid name");
			  return null;
			}
		  }		    		      
		  else return null;
	}
	
	/** 
	 * Loads a project file.
	 * 
	 * @return s - the selected project file path 
	 */
	public String loadXMLDialog(String pathProject){
		FileDialog d = new FileDialog(new JFrame("Load File"));
    	d.setMode(FileDialog.LOAD);
    	d.setFilenameFilter(new FilterFileProject());
    	
    	//checking if the diagrams save directory must be created
    	File dir=new File(pathProject);		
    	if(!dir.isDirectory() && !dir.mkdir()){
    		errorDialog("Save Directory can't be created.");
    		return null;
    	}

    	
//	    d.setDirectory(".");
	    d.setDirectory(pathProject);
	    d.setVisible(true);
	    
	    if(d.getFile() == null) return null;

	    return d.getFile().toString();
	}
	
	/** 
	 * Shows a message when the user makes an error.
	 * 
	 * @param s - the error message
	 */
	public void errorDialog(String s){
		JFrame f = new JFrame("Error");
    	Object[] options = {"OK"};			
		
		JOptionPane.showOptionDialog(f, s, "Error", 
				JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[0]);
	}

	/**
	 * Saves the visual elements of the digram and the diagram state on file.
	 * @param pathProject - the directory path where to save the diagram
	 * @param s - the name of the file in which to save the diagram
	 */
	public String saveDiagram(String pathProject, String s) {
		OrderedListNode tmp = null;
		String xml = null;
		FeaturePanel featTmp=null;
		AnchorPanel anchTmp=null;
		AnchorPanel endTmp=null;
		String startOwner=null;
		String endOwner=null;
		String savePath = pathProject + "/" + s + "_DiagView.xml"; 

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			  +"<Diagram name=\"" + s + "\">"
				+"<features>";
		
		System.out.println("***Printing draggables in reverse order before save***\n");
		tmp = visibleOrderDraggables.getLast();
		while(tmp!=null){
		  if(((JComponent)tmp.getElement()).getName().startsWith(featureNamePrefix)){
			featTmp = (FeaturePanel)tmp.getElement();
			System.out.println("Adding element: "+featTmp.getLabelName());
			xml+="Name="+featTmp.getLabelName()+" ContName="+featTmp.getName()
			    +" Loc="+featTmp.getX()+"."+featTmp.getY()
			    +" Size="+featTmp.getWidth()+"."+featTmp.getHeight()+"\n";
		  }
		  tmp=tmp.getPrev();
		}

		xml+=	 "</features>"
			    +"<connectors>";
		
		for(JComponent anchor : startConnectorDots){
		  anchTmp=(AnchorPanel)anchor;
		  endTmp=(AnchorPanel)anchTmp.getOtherEnd();
		  if(anchTmp.getParent().getName().startsWith(featureNamePrefix)) 
			startOwner=anchTmp.getParent().getName();
		  else startOwner="";
		  if(endTmp.getParent().getName().startsWith(featureNamePrefix))
			endOwner=endTmp.getParent().getName()+"\n";
		  else endOwner="\n";
		  
		  xml+="StartName="+anchTmp.getName()+" Loc="+anchTmp.getX()+"."+anchTmp.getY()+" StartOwner="+startOwner
		      +" EndName="+endTmp.getName()+" Loc="+endTmp.getX()+"."+endTmp.getY()+" EndOwner="+endOwner;		  
		}

		xml+=	 "</connectors>"
			    +"<groups>";

		for(GroupPanel group : altGroupPanels){
		  if(group.getParent().getName().startsWith(featureNamePrefix)) 
			startOwner=group.getParent().getName();
		  else startOwner="";
			  
		  xml+="GroupName="+group.getName()+" type=ALT"+" Loc="+group.getX()+"."+group.getY()+" StartOwner="+startOwner;

		  for(AnchorPanel member : group.getMembers()){
			if(member.getParent().getName().startsWith(featureNamePrefix)) 
			  endOwner=member.getParent().getName();
			else endOwner="";
			  
			xml+=" MemberName="+member.getName()+" Loc="+member.getX()+"."+member.getY()+" EndOwner="+endOwner;		  
		  }
		  xml+="\n";
		}

		for(GroupPanel group : orGroupPanels){
		  if(group.getParent().getName().startsWith(featureNamePrefix)) 
			startOwner=group.getParent().getName();
		  else startOwner="";
			  
		  xml+="GroupName="+group.getName()+" type=OR"+" Loc="+group.getX()+"."+group.getY()+" StartOwner="+startOwner;

		  for(AnchorPanel member : group.getMembers()){
			if(member.getParent().getName().startsWith(featureNamePrefix)) 
			  endOwner=member.getParent().getName();
			else endOwner="";
			  
			xml+=" MemberName="+member.getName()+" Loc="+member.getX()+"."+member.getY()+" EndOwner="+endOwner;		  
		  }
		  xml+="\n";
		  
		}
		
		xml+=	 "</groups>"
			    +"<misc>"
				+"connectorsCount="+connectorsCount+" altGroupsCount="+altGroupsCount
				+" orGroupsCount="+orGroupsCount+" featuresCount="+featuresCount
			    +"</misc>"
			    +"<startingCommonalities>";
		
		for(String name : startingCommonalities){
		  xml+=name+" ";
		}

		xml+=	 "</startingCommonalities>"
			    +"<startingVariabilities>";
		
		for(String name : startingVariabilities){
		  xml+=name+" ";
		}		

		xml+=	 "</startingVariabilities>"
			  +"</Diagram>";
		
		//saving xml string on file
		try{
		  //checking if the diagrams save directory must be created
		  File dir=new File(pathProject);		
		  if(!dir.isDirectory() && !dir.mkdir() ) throw new IOException("Save Directory can't be created.");
			
		  PrintWriter pw1 = new PrintWriter(new BufferedWriter(new FileWriter(savePath)));
		  pw1.print(xml);
		  pw1.close();
			
		} 
		catch (IOException e){
			System.out.println("Exception saveDiagram: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return savePath;
	}

	/**
	 * Loads a saved feature model from a list of files, each describing a feature tree.
	 * @param featureModelDataPaths - the list of files describing the feature trees
	 * @return - the saved feature model
	 */
	public void loadSavedDiagram(String diagramDataPath) {
	  SAXParser saxParser = null;
	  InputStream stream = null;
	  SAXParserFactory saxFactory = SAXParserFactory.newInstance();
	  ViewXMLHandler xmlHandler = new ViewXMLHandler();


	  try {
		stream=new FileInputStream(diagramDataPath);
		  
//		  BufferedReader br1 = new BufferedReader(new FileReader(diagramDataPath));
//		  while( (s = br1.readLine()) != null ) xml+=s;
//		  br1.close();
//		  stream = new ByteArrayInputStream(xml.getBytes());
		  
		System.out.println("EditorView: *** PARSING: "+diagramDataPath+" ***");
		saxParser = saxFactory.newSAXParser();
		saxParser.parse(stream, xmlHandler);

		System.out.println("\nResult of parsing:\n"
				+"Features:\n"+xmlHandler.featuresList
				+"\nConnectors:\n"+xmlHandler.connectorsList
				+"\nGroups:\n"+xmlHandler.groupsList
				+"\nMisc:\n"+xmlHandler.misc
				+"\nStarting Commonalities:\n"+xmlHandler.startingComm
				+"\nStarting Variabilities:\n"+xmlHandler.startingVars
				+"");

		loadFeatures(xmlHandler.featuresList);
		loadConnectors(xmlHandler.connectorsList);
		loadGroups(xmlHandler.groupsList);
		loadMiscellaneous(xmlHandler.misc);
		loadStartingCommonalities(xmlHandler.startingComm);
		loadStartingVariabilities(xmlHandler.startingVars);
	  } catch (Exception e) {
		System.out.println("Error while loading saved diagram");
		e.printStackTrace(); 
		throw new RuntimeException("Error while loading saved diagram");
	  }
	  return;
	}

	/**
	 * Adds to the diagram panel all the saved features described in the list.
	 * 
	 * @param featuresList - String describing the features to load, one per line
	 */
	private void loadFeatures(String featuresList) {
	  String[] featureData=null;
	  String featureName=null;
	  String containerName=null;
	  int x=0, y=0, width=0, height=0, i=0;
	  String[] features=featuresList.split("\n");
	  
	  for(String feature : features){
		//getting data of this feature
		featureData=feature.split(" ");

		featureName=featureData[0].substring(5);
		containerName=featureData[1].substring(9);

		for (i=4; i<featureData[2].length(); ++i) if (featureData[2].charAt(i)=='.') break;
		x=Integer.valueOf(featureData[2].substring(4, i));
		y=Integer.valueOf(featureData[2].substring(i+1));

		for (i=5; i<featureData[3].length(); ++i) if (featureData[3].charAt(i)=='.') break;
		width=Integer.valueOf(featureData[3].substring(5, i));
		height=Integer.valueOf(featureData[3].substring(i+1));
		
		FeaturePanel newFeature=buildFeaturePanel(featureName, containerName, x, y);
		newFeature.setSize(width, height);
		
		visibleOrderDraggables.addToTop(newFeature);
		diagramPanel.setLayer(newFeature, 0);
		diagramPanel.add(newFeature);
		diagramPanel.setComponentZOrder(newFeature, 0);
	  }
	}

	/**
	 * Adds to the diagram panel all the saved connectors described in the list.
	 * 
	 * @param connectorsList - String describing the connectors to load, one per line
	 */
	private void loadConnectors(String connectorsList) {
	  String[] connectorData=null;
	  String startConnectorName=null;
	  String startOwnerName=null;
	  int startX=0, startY=0;	  
	  String endConnectorName=null;
	  String endOwnerName=null;
	  int endX=0, endY=0;
	  int i=0;
	  String[] connectors=connectorsList.split("\n");
	  AnchorPanel startConnector=null, endConnector=null;
	  FeaturePanel owner=null;
	  for(String connector : connectors){
		connectorData=connector.split(" ");

		//getting data of start anchor of this connector
		startConnectorName=connectorData[0].substring(10);

		for (i=4; i<connectorData[1].length(); ++i) if (connectorData[1].charAt(i)=='.') break;
		startX=Integer.valueOf(connectorData[1].substring(4, i));
		startY=Integer.valueOf(connectorData[1].substring(i+1));

		startOwnerName=connectorData[2].substring(11);

		//getting data of end anchor of this connector
		endConnectorName=connectorData[3].substring(8);

		for (i=4; i<connectorData[4].length(); ++i) if (connectorData[4].charAt(i)=='.') break;
		endX=Integer.valueOf(connectorData[4].substring(4, i));
		endY=Integer.valueOf(connectorData[4].substring(i+1));

		endOwnerName=connectorData[5].substring(9);		
		
		//adding start connector
		startConnector=(AnchorPanel)buildConnectionDot(ItemsType.START_CONNECTOR, startConnectorName, startX, startY);
		if(startOwnerName==""){//adding connector to the diagram panel directly
		  visibleOrderDraggables.addToTop(startConnector);
		  diagramPanel.setLayer(startConnector, 0);
		  diagramPanel.add(startConnector);
		  diagramPanel.setComponentZOrder(startConnector, 0);			
		}
		else{//adding connector to its owner feature panel
		  OrderedListNode tmp=visibleOrderDraggables.getFirst();
		  while(tmp!=null){
			if(((JComponent)tmp.getElement()).getName().compareTo(startOwnerName)==0){
			  owner=(FeaturePanel)tmp.getElement(); break;
			}
			tmp=tmp.getNext();
		  }
		  if(owner==null)
			throw new RuntimeException("Couldn't find feature '"+startOwnerName+"' as owner of '"+startConnectorName);

		  visibleOrderDraggables.addToTop(startConnector);
		  owner.setLayer(startConnector, 0);
		  owner.add(startConnector);
		  owner.setComponentZOrder(startConnector, 0);
		}
		
		
		//adding end connector
		endConnector=(AnchorPanel)buildConnectionDot(ItemsType.END_CONNECTOR, endConnectorName, endX, endY);
		if(endOwnerName==""){//adding connector to the diagram panel directly
		  visibleOrderDraggables.addToTop(endConnector);
		  diagramPanel.setLayer(endConnector, 0);
		  diagramPanel.add(endConnector);
		  diagramPanel.setComponentZOrder(endConnector, 0);			
		}
		else{//adding connector to its owner feature panel
		  OrderedListNode tmp=visibleOrderDraggables.getFirst();
		  while(tmp!=null){
			if(((JComponent)tmp.getElement()).getName().compareTo(endOwnerName)==0){
			  owner=(FeaturePanel)tmp.getElement(); break;
			}
			tmp=tmp.getNext();
		  }
		  if(owner==null)
			throw new RuntimeException("Couldn't find feature '"+endOwnerName+"' as owner of '"+endConnectorName);

		  visibleOrderDraggables.addToTop(endConnector);
		  owner.setLayer(endConnector, 0);
		  owner.add(endConnector);
		  owner.setComponentZOrder(endConnector, 0);
		}		
		//adding mutual references
		startConnector.setOtherEnd(endConnector);
		endConnector.setOtherEnd(startConnector);
		//adding connector to draw list
		startConnectorDots.add(startConnector);
	  }

	}

	/**
	 * Adds to the diagram panel all the saved groups described in the list.
	 * 
	 * @param groupsList - String describing the groups to load, one per line
	 */
	private void loadGroups(String groupsList) {
	  String[] groupData=null;
	  String groupConnectorName=null;
	  String groupOwnerName=null;
	  int groupX=0, groupY=0;	
	  String typeString=null;
	  ItemsType groupType=null;

	  String memberName=null;
	  String memberOwnerName=null;
	  int memberX=0, memberY=0;
	  int i=0;
	  String[] groups=groupsList.split("\n");

	  GroupPanel group=null;
	  AnchorPanel member=null;
	  FeaturePanel owner=null;
	  for(String connector : groups){
		groupData=connector.split(" ");

		//getting data of group
		groupConnectorName=groupData[0].substring(10);

		typeString=groupData[1].substring(5);
		groupType=(typeString.compareTo("ALT")==0)? ItemsType.ALT_GROUP_START_CONNECTOR : ItemsType.OR_GROUP_START_CONNECTOR;

		for (i=4; i<groupData[2].length(); ++i) if (groupData[2].charAt(i)=='.') break;
		groupX=Integer.valueOf(groupData[2].substring(4, i));
		groupY=Integer.valueOf(groupData[2].substring(i+1));

		groupOwnerName=groupData[3].substring(11);

		//adding group
		group=(GroupPanel)buildConnectionDot(groupType, groupConnectorName, groupX, groupY);
		if(groupOwnerName==""){//adding connector to the diagram panel directly
		  visibleOrderDraggables.addToTop(group);
		  diagramPanel.setLayer(group, 0);
		  diagramPanel.add(group);
		  diagramPanel.setComponentZOrder(group, 0);			
		}
		else{//adding group to its owner feature panel
		  OrderedListNode tmp=visibleOrderDraggables.getFirst();
		  while(tmp!=null){
			if(((JComponent)tmp.getElement()).getName().compareTo(groupOwnerName)==0){
			  owner=(FeaturePanel)tmp.getElement(); break;
			}
			tmp=tmp.getNext();
		  }
		  if(owner==null)
		    throw new RuntimeException("Couldn't find feature '"+groupOwnerName+"' as owner of '"+groupConnectorName);

		  visibleOrderDraggables.addToTop(group);
		  owner.setLayer(group, 0);
		  owner.add(group);
		  owner.setComponentZOrder(group, 0);
		}


		//adding members
		for(int k=4; k<groupData.length; k+=3){
		  //getting member's data
		  memberName=groupData[k].substring(11);

		  for (i=4; i<groupData[k+1].length(); ++i) if (groupData[k+1].charAt(i)=='.') break;
		  memberX=Integer.valueOf(groupData[k+1].substring(4, i));
		  memberY=Integer.valueOf(groupData[k+1].substring(i+1));

		  memberOwnerName=groupData[k+2].substring(9);		

		  //adding member
		  member=(AnchorPanel)buildConnectionDot(ItemsType.END_CONNECTOR, memberName, memberX, memberY);
		  if(memberOwnerName==""){//adding member to the diagram panel directly
			visibleOrderDraggables.addToTop(member);
			diagramPanel.setLayer(member, 0);
			diagramPanel.add(member);
			diagramPanel.setComponentZOrder(member, 0);			
		  }
		  else{//adding member to its owner feature panel
			OrderedListNode tmp=visibleOrderDraggables.getFirst();
			while(tmp!=null){
			  if(((JComponent)tmp.getElement()).getName().compareTo(memberOwnerName)==0){
				owner=(FeaturePanel)tmp.getElement(); break;
			  }
			  tmp=tmp.getNext();
			}
			if(owner==null)
			  throw new RuntimeException("Couldn't find feature '"+memberOwnerName+"' as owner of '"+memberName);

			visibleOrderDraggables.addToTop(member);
			owner.setLayer(member, 0);
			owner.add(member);
			owner.setComponentZOrder(member, 0);
		  }	
		  //adding mutual references
		  member.setOtherEnd(group);
		  group.getMembers().add(member);
		}

		//adding group to draw list
		if(groupType==ItemsType.ALT_GROUP_START_CONNECTOR) altGroupPanels.add(group);
		else orGroupPanels.add(group);
	  }
	  
	}

	/**
	 * Loads a set of values used by the view for various purposes.
	 * 
	 * @param misc - String describing the values to load, separated by a singol blank
	 */
	private void loadMiscellaneous(String misc) {
	  String[] values=misc.split(" ");
	  connectorsCount=Integer.valueOf(values[0].substring(16));
	  altGroupsCount=Integer.valueOf(values[1].substring(15));
	  orGroupsCount=Integer.valueOf(values[2].substring(14));
	  featuresCount=Integer.valueOf(values[3].substring(14));
	}

	/**
	 * Loads the list of starting commonalities.
	 * 
	 * @param startingComm - String containing the names of starting commonalities, separated by a singol blank
	 */
	private void loadStartingCommonalities(String startingComm) {
	  String[] commonalityNames=startingComm.split(" ");
	  for(String name : commonalityNames) startingCommonalities.add(name);
	}

	/**
	 * Loads the list of starting variabilities.
	 * 
	 * @param startingVars - String containing the names of starting variabilities, separated by a singol blank
	 */
	private void loadStartingVariabilities(String startingVars) {
	  String[] variabilityNames=startingVars.split(" ");
	  for(String name : variabilityNames) startingVariabilities.add(name);
	}
	
	
}
