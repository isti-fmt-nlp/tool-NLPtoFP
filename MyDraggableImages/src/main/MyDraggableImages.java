package main;

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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.text.LayeredHighlighter;



public class MyDraggableImages extends JFrame{
	/** variables used for debugging*/
	private static boolean debug=false;
	private static boolean debug2=false;
	private static boolean debug3=false;
	private static boolean debug4=false;


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
	/** current amount of groups in the diagram panel*/
	private static int groupsCount=0;
	/** current amount of features in the diagram panel*/
	private static int featuresCount=0;
	
	/** list of all connector starting dots,
	 *  corresponding ending dots can be found in endConnectorDots at the same index
	 */
	private static ArrayList<JPanel> startConnectorDots=null;
	
	/** list of all connector ending dots,
	 *  corresponding starting dots can be found in startConnectorDots at the same index
	 */
	private static ArrayList<JPanel> endConnectorDots=null;
	
	//still unused
	/** previous location of all connector starting dots, with same indexes of the lists above*/
	private static ArrayList<Point> prevStartConnectorDotsLocation=null;
	
	//still unused
	/** previous location of all connector ending dots, with same indexes of the lists above*/
	private static ArrayList<Point> prevEndConnectorDotsLocation=null;
	
	//to revise
	/** list of all connector dots that must be redrawn, with same indexes of the lists above*/
	private static ArrayList<Boolean> connectorDotsToRedraw=null;
	
	/** list of all groups*/
	private static ArrayList<GroupPanel> groupPanels=null;
	
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
	private static JPanel lastAnchorFocused=null;
//	private static boolean isDraggingFeature=false;
	
	/** URL of the new feature icon*/
	private static URL newFeatureIconURL=MyDraggableImages.class.getResource("/feature rectangle2.png");
//	private static URL newFeatureIconURL=MyDraggableImages.class.getResource("/feature rectangle.png");
//	private static URL newFeatureIconURL=MyDraggableImages.class.getResource("/balzac.jpg");
	/** URL of the connector starting dot icon*/
	private static URL connectorStartDotIconURL=MyDraggableImages.class.getResource("/Connector Start Dot.png");
	/** URL of the new group starting dot icon*/
	private static URL groupDotIconURL=MyDraggableImages.class.getResource("/Group Start Dot.png");
	/** URL of the new connector ending dot icon*/
	private static URL connectorEndDotIconURL=MyDraggableImages.class.getResource("/Connector End Dot.png");
	/** URL of the line-only connector icon*/
	private static URL connectorLineLengthIconURL=MyDraggableImages.class.getResource("/Connector Line Length.png");
	
	/** prefix of any feature name*/
	private static String featureNamePrefix="---FEATURE---#";
	/** prefix of any connector starting dot name*/
	private static String startConnectorsNamePrefix="---START_CONNECTOR---#";
	/** prefix of any connector ending dot name*/
	private static String endConnectorsNamePrefix="---END_CONNECTOR---#";
	/** prefix of any group starting dot name*/
	private static String groupNamePrefix="---GROUP---#";
	/** name ofthe diagram panel*/
	private static String diagramPanelName="---DIAGRAM_PANEL---";


	/** enumeration of items that can become active, for instance in a drag motion*/
	private static enum activeItems {
		DRAGGING_FEATURE, DRAGGING_TOOL_NEWFEATURE, DRAGGING_TOOL_CONNECTOR, NO_ACTIVE_ITEM,
		DRAGGING_EXTERN_ANCHOR, DRAGGING_TOOL_ALT_GROUP, DRAGGING_EXTERN_GROUP	
	}

	/** enumeration used to specify a item type through the program*/
	private static enum itemsTypes {
		START_CONNECTOR, END_CONNECTOR, GROUP_START_CONNECTOR
	}

	/** tells what item is interested in the current action*/
	private static activeItems isActiveItem=activeItems.NO_ACTIVE_ITEM;

	/** maps tool names in the corresponding icon resource path*/
	private static HashMap<String, String> toolIconPaths=null;
	
	private static int featureBorderSize=20;

	private static Point start=new Point(), end=new Point();

//	public static void main(String[] args){
	public MyDraggableImages(){
		OrderedListNode tmpNode=null;
		visibleOrderDraggables = new OrderedList();
		startConnectorDots = new ArrayList<JPanel>();
		endConnectorDots = new ArrayList<JPanel>();
		prevStartConnectorDotsLocation = new ArrayList<Point>();
		prevEndConnectorDotsLocation = new ArrayList<Point>();
		connectorDotsToRedraw = new ArrayList<Boolean>();
		groupPanels = new ArrayList<GroupPanel>();

		//creating root frame
//		frameRoot=new OrderedListPaintJFrame(visibleOrderDraggables);
		frameRoot=this;
		setLayout(new BorderLayout());		
//		frameRoot.setLayout(new BorderLayout());		
//		frameRoot.setLayout(null);		
//		frameRoot.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		
		//icons for toolspanel
		
		
		//stupid tries panels
		JPanel tryGreen= new JPanel();
		tryGreen.setBackground(Color.green);
		tryGreen.setSize(50, 50);
		JPanel tryOrange= new JPanel();
		tryOrange.setBackground(Color.orange);
		tryOrange.setSize(50, 50);
		JPanel tryPink= new JPanel();
		tryPink.setBackground(Color.pink);
		tryPink.setSize(50, 50);
		
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
		
		JPanel iconTmpPanel=null;
		for(int i=0; i<8; ++i){
		  iconTmpPanel=getToolIcon("New Feature", true);
		  iconTmpPanel.addMouseListener(getToolbarMouseListener());
		  iconTmpPanel.addMouseMotionListener(getToolbarMouseMotionListener());
		  toolsPanel.add(iconTmpPanel);

		  iconTmpPanel=getToolIcon("Connector Line", true);
		  iconTmpPanel.addMouseListener(getToolbarMouseListener());
		  iconTmpPanel.addMouseMotionListener(getToolbarMouseMotionListener());
		  toolsPanel.add(iconTmpPanel);

		  iconTmpPanel=getToolIcon("Alternative Group", true);
		  iconTmpPanel.addMouseListener(getToolbarMouseListener());
		  iconTmpPanel.addMouseMotionListener(getToolbarMouseMotionListener());
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

		
		//creo i draggables
		String label[]={"Questo è un nome lungo per una Feature!", "Questo è un nome ammodo", "Corto!"};
		for(int i=0; i<label.length; ++i){
			visibleOrderDraggables.addToTop(getDraggableFeature(label[i], 15+i*75, 15+i*75));
			++featuresCount;
		}
						
		//aggiungo i draggables al diagram panel
		int index=0;

//		JPanel tmpPanel=null;
		
		tmpNode=visibleOrderDraggables.getFirst();
		while(tmpNode!=null){
//			((JPanel)tmpNode.getElement()).addMouseListener(getDiagramMouseListener());
//			((JPanel)tmpNode.getElement()).addMouseMotionListener(getDiagramMouseMotionListener());
			diagramPanel.setLayer((Component)tmpNode.getElement(), index);
			diagramPanel.add((Component)tmpNode.getElement());
			diagramPanel.setComponentZOrder((Component)tmpNode.getElement(), index);
//			tmpPanel=(JPanel)tmpNode.getElement();
//			diagramPanel.setLayer(tmpPanel, index);
//			diagramPanel.add(tmpPanel);
//			tmpPanel.setOpaque(false);
//			diagramPanel.setComponentZOrder(tmpPanel, index);
			
			
			
			tmpNode=tmpNode.getNext();
			++index;
		}

//		tmpNode=visibleOrderDraggables.getLast();
//		while(tmpNode!=null){
//			diagramPanel.add((JPanel)tmpNode.getElement(), index);
//			tmpNode=tmpNode.getPrev();
//			++index;
//		}

		diagramPanel.addMouseListener(getDiagramMouseListener());
		diagramPanel.addMouseMotionListener(getDiagramMouseMotionListener());

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
		JPanel startPanel=null;

		//drawing connectors
		for (int i=0; i< connectorDotsToRedraw.size(); ++i){
		  if (connectorDotsToRedraw.get(i)){
			drawConnectionLine(g2, startConnectorDots.get(i), endConnectorDots.get(i));
		  }
		}

		//drawing groups
		for (int i=0; i< groupPanels.size(); ++i){
		  startPanel=groupPanels.get(i);
		  for (JPanel endPanel : ((GroupPanel)startPanel).getMembers()){
			drawConnectionLine(g2, startPanel, endPanel);				
		  }
		}
	}

	private void drawConnectionLine(Graphics2D g2, JPanel startPanel, JPanel endPanel) {
		start.setLocation(startPanel.getLocationOnScreen());
		end.setLocation(endPanel.getLocationOnScreen());
		g2.drawLine(
		  (int)(start.getX()-splitterPanel.getLocationOnScreen().getX()+startPanel.getWidth()/2),
		  (int)(start.getY()-splitterPanel.getLocationOnScreen().getY()+startPanel.getHeight()/2+3),
		  (int)(end.getX()-splitterPanel.getLocationOnScreen().getX()+endPanel.getHeight()/2-3),
		  (int)(end.getY()-splitterPanel.getLocationOnScreen().getY()+endPanel.getHeight()/2+2) );
	};
	
	/**
	 * Returns a JPanel named name and containing the icon image having the path iconPath, <br>
	 * the method call setOpaque(backgroundVisible) on the panel containig the icon.
	 * 
	 * @param name - the name of the new JPanel
	 * @param backgroundVisible - if true the panel will be opaque, otherwise it will be transparent.
	 * @return - the new JPanel with the icon, or null if a problem occurrs.
	 */
	private static JPanel getToolIcon(String name, boolean backgroundVisible) {
		JPanel iconPanel=null;
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
	 * Returns an ImageIcon named name and using image having the path iconPath.
	 * 
	 * @param name - the name of the new JPanel
	 * @return - the new ImageIcon, or null if a problem occurrs.
	 */
	private static ImageIcon getIconImage(String name) {
		ImageIcon toolImage=null;
		
		try{
			toolImage = new ImageIcon(MyDraggableImages.class.getResource(toolIconPaths.get(name)));	
			
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
			/* ***DEBUG*** */
			if (debug3) System.out.println("mouse clicked on a tool, Divider at: "+splitterPanel.getDividerLocation());
			/* ***DEBUG*** */
		}

		@Override
		public void mousePressed(MouseEvent e) {
//		  lastPositionX=(int)e.getLocationOnScreen().getX();
//		  lastPositionY=(int)e.getLocationOnScreen().getY();
			  lastPositionX=e.getX();
			  lastPositionY=e.getY();
		  

		  Component[] compList=toolsPanel.getComponents();
		  Component comp=(Component)e.getSource();
		  Component imageLabel=((JPanel)comp).getComponent(0);
//		  Component imageLabel=comp.getComponentAt(new Point(e.getX(), e.getY()));
		  
		  /* ***DEBUG*** */
		  if(debug4) System.out.println("e.getSource(): "+e.getSource()+"\nimageLabel: "+imageLabel);
		  /* ***DEBUG*** */

		  try {
			toolDragImage = ImageIO.read(this.getClass().getResourceAsStream(toolIconPaths.get(((JPanel)comp).getName())));
			toolDragPosition= new Point((int)imageLabel.getLocationOnScreen().getX(),
					(int)imageLabel.getLocationOnScreen().getY());
//			toolDragPosition= new Point((int)((JPanel)comp).getLocationOnScreen().getX(),
//					(int)((JPanel)comp).getLocationOnScreen().getY());
			
		  } catch (IOException e2) {
			System.out.println("toolDragImage is null");
			e2.printStackTrace();
			return;
		  }
		  if (((JPanel)comp).getName()=="New Feature")
			  isActiveItem=activeItems.DRAGGING_TOOL_NEWFEATURE;
		  else if (((JPanel)comp).getName()=="Connector Line")
			  isActiveItem=activeItems.DRAGGING_TOOL_CONNECTOR;
		  else if (((JPanel)comp).getName()=="Alternative Group")
			  isActiveItem=activeItems.DRAGGING_TOOL_ALT_GROUP;

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
		  switch(isActiveItem){
		    case DRAGGING_TOOL_NEWFEATURE:
		    	addNewFeatureToDiagram(e);
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
		    default: break;
		  }
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

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
			    default: break;
			  }			  
			}


			@Override
			public void mouseMoved(MouseEvent e) {
			}
			
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
			  int featurePanelX=0, featurePanelY=0;
			  int anchorPanelOnScreenX=0, anchorPanelOnScreenY=0;
			  FeaturePanel featurePanel=null;
			  Component anchorPanel=null;
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
					anchorPanel=featurePanel.getComponentAt(e.getX()-featurePanelX, e.getY()-featurePanelY);
					anchorPanelName=anchorPanel.getName();
					//mouse pressed on an anchor inside the feature panel
					if(anchorPanelName!=null && anchorPanel.getClass().equals(AnchorPanel.class) &&(
					   anchorPanelName.startsWith(startConnectorsNamePrefix) ||
					   anchorPanelName.startsWith(endConnectorsNamePrefix) ) ){
						
					  isActiveItem=activeItems.DRAGGING_EXTERN_ANCHOR;
					  lastAnchorFocused=(JPanel)anchorPanel;

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
					   anchorPanelName.startsWith(groupNamePrefix) ) ){
						
					  isActiveItem=activeItems.DRAGGING_EXTERN_GROUP;
					  lastAnchorFocused=(JPanel)anchorPanel;

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
						  ((GroupPanel)tmpNode.getElement()).getName().startsWith(groupNamePrefix) ){
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

				/* ***DEBUG *** */
				if(debug3) System.out.println(splitterPanel.getDividerLocation());
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
	private static void moveComponentToTop(Component comp) {
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
	private static void dragAnchor(MouseEvent e) {
	  int moveX=0, moveY=0;
	  int adjustedMoveX=0, adjustedMoveY=0;	  
	  int newLocationX=0, newLocationY=0;
	  boolean normalUpdateX=true, normalUpdateY=true;
//	  int actualPositionX=(toolDragPosition.x-(int)diagramPanel.getLocationOnScreen().getX());
//	  int actualPositionY=(toolDragPosition.y-(int)diagramPanel.getLocationOnScreen().getY());

	  if(lastAnchorFocused==null) return;

	  moveX = e.getX()-lastPositionX;
	  moveY = e.getY()-lastPositionY;
	  newLocationX=lastAnchorFocused.getX()+moveX;
	  newLocationY=lastAnchorFocused.getY()+moveY;

	  //the anchor must not be dragged beyond the borders of the diagram panel
	  if( diagramPanel.getLocation().getX()>newLocationX ){
		newLocationX=(int)diagramPanel.getLocation().getX()+1;
		normalUpdateX=false;
		adjustedMoveX=newLocationX-lastAnchorFocused.getX();
	  }
	  if( diagramPanel.getLocation().getX()+diagramPanel.getWidth()<=newLocationX+lastAnchorFocused.getWidth() ){
		newLocationX=(int)diagramPanel.getLocation().getX()+diagramPanel.getWidth()-lastAnchorFocused.getWidth()-1;
		normalUpdateX=false;
		adjustedMoveX=newLocationX-lastAnchorFocused.getX();
	  }
	  if( diagramPanel.getLocation().getY()>newLocationY ){
		newLocationY=(int)diagramPanel.getLocation().getY()+1;		  
		normalUpdateY=false;
		adjustedMoveY=newLocationY-lastAnchorFocused.getY();
	  }
	  if( diagramPanel.getLocation().getX()+diagramPanel.getHeight()<=newLocationY+lastAnchorFocused.getHeight() ){
		newLocationY=(int)diagramPanel.getLocation().getY()+diagramPanel.getHeight()-lastAnchorFocused.getHeight()-1;
		normalUpdateY=false;
		adjustedMoveY=newLocationY-lastAnchorFocused.getY();
	  }

	  /* ***DEBUG*** */
	  if (debug4){
		  System.out.println("oldPosX: "+lastPositionX+"\toldPosY: "+lastPositionY);
		  System.out.println("newPosX: "+e.getX()+"\tnewPosY: "+e.getY());
		  System.out.println("moveX: "+moveX+"\tmoveY: "+moveY);
	  }
	  /* ***DEBUG*** */

	  if(normalUpdateX) lastPositionX=e.getX();
	  else lastPositionX=lastPositionX+adjustedMoveX;
	  if(normalUpdateY) lastPositionY=e.getY();
	  else lastPositionY=lastPositionY+adjustedMoveY;

	  lastAnchorFocused.setLocation(newLocationX, newLocationY);

	  /*
	  JLayeredPane underlyingPanel=null;
	  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
	  while(tmpNode!=null){	  
		if ( tmpNode.getElement().getClass().equals(JLayeredPane.class) &&
			 ((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY()) ){
			underlyingPanel=(JLayeredPane)tmpNode.getElement();
			moveComponentToTop(underlyingPanel);

			lastAnchorFocused.setLocation(lastPositionX-diagramPanel.getX(), lastPositionY-diagramPanel.getY());			

			diagramPanel.remove(lastAnchorFocused);
			diagramPanel.validate();
			underlyingPanel.setLayer(lastAnchorFocused, 0);
			underlyingPanel.add(lastAnchorFocused);
			underlyingPanel.setComponentZOrder(lastAnchorFocused, 0);
		}
		tmpNode=tmpNode.getNext();
	  }
	  */
//	  diagramPanel.repaint();
	  frameRoot.repaint();
	}

	/**
	 * Drags a feature panel inside the diagram panel.
	 *
	 * @param e - the current MouseEvent
	 */
	private static void dragFeature(MouseEvent e) {
	  int moveX=0, moveY=0;
	  int adjustedMoveX=0, adjustedMoveY=0;	  
	  int newLocationX=0, newLocationY=0;
	  boolean normalUpdateX=true, normalUpdateY=true;

	  if(lastFeatureFocused==null) return;

	  moveX = e.getX()-lastPositionX;
	  moveY = e.getY()-lastPositionY;
	  newLocationX=lastFeatureFocused.getX()+moveX;
	  newLocationY=lastFeatureFocused.getY()+moveY;
	  
	  //the feature must not be dragged beyond the borders of the diagram panel
	  if( newLocationX<0 ){
//		  if( diagramPanel.getLocation().getX()>newLocationX ){
//			newLocationX=(int)diagramPanel.getLocation().getX()+1;
		newLocationX=1;
		normalUpdateX=false;
		adjustedMoveX=newLocationX-lastFeatureFocused.getX();
	  }
	  if( diagramPanel.getWidth()<=newLocationX+lastFeatureFocused.getWidth() ){
//		  if( diagramPanel.getLocation().getX()+diagramPanel.getWidth()<=newLocationX+lastFeatureFocused.getWidth() ){
		newLocationX=diagramPanel.getWidth()-lastFeatureFocused.getWidth()-1;
//			newLocationX=(int)diagramPanel.getLocation().getX()+diagramPanel.getWidth()-lastFeatureFocused.getWidth()-1;
		normalUpdateX=false;
		adjustedMoveX=newLocationX-lastFeatureFocused.getX();
	  }
	  if( newLocationY<0 ){
//		  if( diagramPanel.getLocation().getY()>newLocationY ){
//			newLocationY=(int)diagramPanel.getLocation().getY()+1;
		newLocationY=1;
		normalUpdateY=false;
		adjustedMoveY=newLocationY-lastFeatureFocused.getY();
	  }
	  if( diagramPanel.getHeight()<=newLocationY+lastFeatureFocused.getHeight() ){
//		  if( diagramPanel.getLocation().getY()+diagramPanel.getHeight()<=newLocationY+lastFeatureFocused.getHeight() ){
		newLocationY=diagramPanel.getHeight()-lastFeatureFocused.getHeight()-1;
//			newLocationY=(int)diagramPanel.getLocation().getY()+diagramPanel.getHeight()-lastFeatureFocused.getHeight()-1;
		normalUpdateY=false;
		adjustedMoveY=newLocationY-lastFeatureFocused.getY();
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

	  lastFeatureFocused.setLocation(newLocationX, newLocationY);
//	  diagramPanel.repaint();
	  frameRoot.repaint();
	}

	/**
	 * Drags a Connector tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	private static void dragToolConnector(MouseEvent e) {
		  dragTool(e);
	}

	/**
	 * Drags a NewFeature tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	private static void dragToolNewFeature(MouseEvent e) {
		  dragTool(e);
	}

	/**
	 * Drags an AltGroup tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	private static void dragToolAltGroup(MouseEvent e) {
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
	private static void dropAnchorOnDiagram(MouseEvent e) {
	  FeaturePanel underlyingPanel=null;
	  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
	  while(tmpNode!=null){	  
		if ( tmpNode.getElement().getClass().equals(FeaturePanel.class) &&
			 ((FeaturePanel)tmpNode.getElement()).getName().startsWith(featureNamePrefix) &&
			 ((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY()) ){			
		  underlyingPanel=(FeaturePanel)tmpNode.getElement();
		  addAnchorToFeature(lastAnchorFocused, underlyingPanel);
//		  diagramPanel.repaint();
		  frameRoot.repaint();
		  break;		
		}
		tmpNode=tmpNode.getNext();
	  }				
	}

	/**
	 * Drops a group on the diagram panel, adding it to the underlying feature panel, if any is present.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	private static void dropGroupOnDiagram(MouseEvent e) {
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
	 * Removes a visible anchor from the diagram panel and attach it to a feature panel.<br>
	 * If the anchor is not visible and attached to the diagram panel, no operation is performed <br>
	 * and false value is returned.
	 * 
	 * @param anchor - the JPanel object of the anchor
	 * @param featurePanel - the feature panel on which the anchor will be added
	 * @return - false if the operation is not possible, true otherwise
	 */
	private static boolean addAnchorToFeature(JPanel anchor, FeaturePanel featurePanel) {
		int anchorPanelOnScreenX;
		int anchorPanelOnScreenY;
		
		/* ***DEBUG*** */
		if(debug4) System.out.println("anchor.getParent()="+anchor.getParent()
			+"\nanchor.isDisplayable(): "+anchor.isDisplayable());
		/* ***DEBUG*** */

		if(anchor.getParent()==null || !anchor.isDisplayable()) return false;
		
		moveComponentToTop(featurePanel);

		/* ***DEBUG*** */
		if(debug) System.out.println("Adding anchor to the feature: "+featurePanel.getName()
				+"\nunderlyingPanel.getLocationOnScreen(): "+featurePanel.getLocationOnScreen()
				+"\nlastAnchorFocused.getLocationOnScreen(): "+anchor.getLocationOnScreen());
		/* ***DEBUG*** */

		anchorPanelOnScreenX=(int)anchor.getLocationOnScreen().getX();
		anchorPanelOnScreenY=(int)anchor.getLocationOnScreen().getY();

		anchor.setLocation((int)(anchorPanelOnScreenX-featurePanel.getLocationOnScreen().getX()),
				(int)(anchorPanelOnScreenY-featurePanel.getLocationOnScreen().getY()) );		

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
		return true;
	}

	private static void addNewFeatureToDiagram(MouseEvent e) {

		//the new feature must be dropped on the diagram panel for it to be added
		if( diagramPanel.getLocationOnScreen().getX()>toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getWidth()<=toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getY()>toolDragPosition.y ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getHeight()<=toolDragPosition.y ){

			toolDragImage=null;
			toolDragPosition=null;
			frameRoot.repaint();

			/* ***DEBUG*** */
			if(debug4) System.out.println("Cannot drop a new feature on tools panel.");
			/* ***DEBUG*** */
			return;
		}

		/* ***DEBUG*** */
		if(debug) System.out.println("Mouse rilasciato(Drag relative) su: ("+e.getX()+", "+e.getY()+")."
		  +"\nMouse rilasciato(Screen relative) su: ("+e.getXOnScreen()+", "+e.getYOnScreen()+")."
		  +"\nLocation dove verrà aggiunta la nuova Feature: ("+toolDragPosition.x+", "+toolDragPosition.y+").");
		/* ***DEBUG*** */
		
		FeaturePanel newFeature=getDraggableFeature("Default Feature Name"/*featureNamePrefix+featuresCount*/,
			toolDragPosition.x-(int)diagramPanel.getLocationOnScreen().getX(),
			toolDragPosition.y-(int)diagramPanel.getLocationOnScreen().getY());

		toolDragImage=null;
		toolDragPosition=null;
		visibleOrderDraggables.addToTop(newFeature);
		diagramPanel.setLayer(newFeature, 0);
		diagramPanel.add(newFeature);
		diagramPanel.setComponentZOrder(newFeature, 0);
//		diagramPanel.repaint();
		frameRoot.repaint();

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
	private static void addConnectorToDiagram(MouseEvent e) {
//		int actualPositionX=((int)(e.getLocationOnScreen().getX()-diagramPanel.getLocationOnScreen().getX()));
//		int actualPositionY=((int)(e.getLocationOnScreen().getY()-diagramPanel.getLocationOnScreen().getY()));
		int actualPositionX;
		int actualPositionY;
		boolean startDotInsertedInPanel=false;
		AnchorPanel newConnectorStartDot=null;			
		AnchorPanel newConnectorEndDot=null;
		FeaturePanel featurePanel = null;
		Component underlyingPanel = null;

		
		//the new connector must be dropped on the diagram panel for it to be added
		if( diagramPanel.getLocationOnScreen().getX()>toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getWidth()<=toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getY()>toolDragPosition.y ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getHeight()<=toolDragPosition.y ){
			
			toolDragImage=null;
			toolDragPosition=null;
			frameRoot.repaint();

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

			newConnectorStartDot=(AnchorPanel)getDraggableConnectionDot(itemsTypes.START_CONNECTOR, 
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
		  newConnectorStartDot=(AnchorPanel)getDraggableConnectionDot(itemsTypes.START_CONNECTOR,
			  actualPositionX, actualPositionY-5);			
		
		ImageIcon lineLengthIcon = new ImageIcon(connectorLineLengthIconURL);
		ImageIcon startConnectorIcon = new ImageIcon(connectorStartDotIconURL);

//		newConnectorEndDot=getDraggableConnectionDot(itemsTypes.END_CONNECTOR, actualPositionX+40, actualPositionY+40);
		newConnectorEndDot=(AnchorPanel)getDraggableConnectionDot(itemsTypes.END_CONNECTOR,
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
		++connectorsCount;

		toolDragImage=null;
		toolDragPosition=null;

//		diagramPanel.repaint();
		frameRoot.repaint();
	}

	/**
	 * Adds a new group to the diagram. If the starting connector dot is dropped over a feature panel, <br>
	 * it gets attached to it.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	private static void addAltGroupToDiagram(MouseEvent e) {
		int actualPositionX;
		int actualPositionY;
		boolean startDotInsertedInPanel=false;
		GroupPanel newGroupStartDot=null;			
		AnchorPanel newGroupEndpoint1=null, newGroupEndpoint2=null;
		FeaturePanel featurePanel = null;
		Component underlyingPanel = null;
		
		//the new group must be dropped on the diagram panel for it to be added
		if( diagramPanel.getLocationOnScreen().getX()>toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getWidth()<=toolDragPosition.x ||
			diagramPanel.getLocationOnScreen().getY()>toolDragPosition.y ||
			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getHeight()<=toolDragPosition.y ){
			
			toolDragImage=null;
			toolDragPosition=null;
			frameRoot.repaint();

			/* ***DEBUG*** */
			if(debug4) System.out.println("Cannot drop a new group on tools panel.");
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
//		if(underlyingPanel!=null){//if the underlying panel is a feature, the group start dot is anchored to it
//		  if (!underlyingPanel.getName().startsWith(diagramPanelName)&&
//			  !underlyingPanel.getName().startsWith(startConnectorsNamePrefix)&&
//			  !underlyingPanel.getName().startsWith(endConnectorsNamePrefix)  ){			  
			newGroupStartDot=(GroupPanel)getDraggableConnectionDot(itemsTypes.GROUP_START_CONNECTOR, 
				toolDragPosition.x-(int)featurePanel.getLocationOnScreen().getX(),
				toolDragPosition.y-(int)featurePanel.getLocationOnScreen().getY()-5);			

//			System.out.println("adding to feature: "+addAnchorToFeature(newConnectorStartDot, underlyingPanel));
			moveComponentToTop(featurePanel);

			featurePanel.setLayer(newGroupStartDot, 0);
			featurePanel.add(newGroupStartDot);
			featurePanel.setComponentZOrder(newGroupStartDot, 0);

			startDotInsertedInPanel=true;
			
			/* ***DEBUG*** */
			System.out.println("Placing group start dot in ("
					+(toolDragPosition.x-(int)underlyingPanel.getLocationOnScreen().getX())
					+", "+(toolDragPosition.y-(int)underlyingPanel.getLocationOnScreen().getY())+")");			
			System.out.println("Group start dot Position(feature relative): ("
					+newGroupStartDot.getX()+", "+newGroupStartDot.getY()+")");			
			System.out.println("Group start dot Position(screen relative): ("
					+newGroupStartDot.getLocationOnScreen().getX()
					+", "+newGroupStartDot.getLocationOnScreen().getY()+")");			
			/* ***DEBUG*** */
		  
		  }
		}
		
		if(!startDotInsertedInPanel) 
			newGroupStartDot=(GroupPanel)getDraggableConnectionDot(itemsTypes.GROUP_START_CONNECTOR, actualPositionX, actualPositionY-5);			
		
		ImageIcon lineLengthIcon = new ImageIcon(connectorLineLengthIconURL);
		ImageIcon startConnectorIcon = new ImageIcon(connectorStartDotIconURL);

//		newConnectorEndDot=getDraggableConnectionDot(itemsTypes.END_CONNECTOR, actualPositionX+40, actualPositionY+40);
		newGroupEndpoint1=(AnchorPanel)getDraggableConnectionDot(itemsTypes.END_CONNECTOR,
				actualPositionX+lineLengthIcon.getIconWidth()+startConnectorIcon.getIconWidth(),
				actualPositionY-5+lineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

		newGroupEndpoint2=(AnchorPanel)getDraggableConnectionDot(itemsTypes.END_CONNECTOR,
				actualPositionX-lineLengthIcon.getIconWidth(),
				actualPositionY-5+lineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

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

		toolDragImage=null;
		toolDragPosition=null;

//		diagramPanel.repaint();
		frameRoot.repaint();

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
		
		addGroupToDrawLists(newGroupStartDot);
		
		++groupsCount;

		
	}

	/**
	 * Creates a JPanel containing a Connection Dot, with a sequential name given by the static variable connectorsCount. 
	 * 
	 * @param x - x coordinate of the connection dot in the diagram panel
	 * @param y - y coordinate of the connection dot in the diagram panel
	 * @param incoURL - URL of the connection dot icon
	 * @return A new JPanel representing the connection dot
	 */
	private static JPanel getDraggableConnectionDot(itemsTypes type, int x, int y) {
		JPanel imagePanel=null;

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
		  case GROUP_START_CONNECTOR:
			imagePanel = new GroupPanel();  
			imagePanel.setName(groupNamePrefix+groupsCount);
			connectorIcon = new ImageIcon(groupDotIconURL);
			break;
 		  default: return null;
		}
		
		imageLabel = new JLabel(connectorIcon);
		imagePanel.setBounds(x,  y, connectorIcon.getIconWidth(), connectorIcon.getIconHeight()+5);
//		imagePanel.setBackground(Color.RED);
		imagePanel.add(imageLabel);
//		imagePanel.add(new JPanel());
		imagePanel.setOpaque(false);
//		imagePanel.setVisible(true);

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
		ImageIcon newFeatureIcon=new ImageIcon(newFeatureIconURL);
		
		JPanel imagePanel=new JPanel();
		JLabel imageLabel = new JLabel(newFeatureIcon);

		imagePanel.setLayout(null);
		
		imageLabel.setBounds(0, 0, newFeatureIcon.getIconWidth(), newFeatureIcon.getIconHeight());
		imagePanel.add(imageLabel);
		imageLabel.setBackground(Color.BLACK);
		imageLabel.setOpaque(true);
		imageLabel.setVisible(true);
		imagePanel.setBackground(Color.BLACK);
		imagePanel.setOpaque(true);
		imagePanel.setBounds(0+featureBorderSize/2, +featureBorderSize/2,
			newFeatureIcon.getIconWidth(), newFeatureIcon.getIconHeight());

		JPanel textPanel = new JPanel();
		JLabel textLabel=new JLabel(name);
		textLabel.setForeground(Color.GRAY);
		textPanel.add(textLabel);
		textPanel.setBounds(0+featureBorderSize/2, newFeatureIcon.getIconHeight()+featureBorderSize/2,
			newFeatureIcon.getIconWidth(), 25);
		textPanel.setOpaque(true);
		textPanel.setBackground(Color.BLACK);
		
		FeaturePanel container = new FeaturePanel(splitterPanel);
		
		container.setName(featureNamePrefix+featuresCount);
		container.setLayout(null);
		container.setBounds(x,  y,  newFeatureIcon.getIconWidth()+featureBorderSize,
			newFeatureIcon.getIconHeight()+25+featureBorderSize);

//		container.setOpaque(false);
		container.setOpaque(true);
		container.setBackground(Color.DARK_GRAY);
		  
		layer=container.getComponentCount();
		container.setLayer(imagePanel, layer);
		container.add(imagePanel);
		container.setComponentZOrder(imagePanel, layer);

		layer=container.getComponentCount();
		container.setLayer(textPanel, layer);
		container.add(textPanel);
		container.setComponentZOrder(textPanel, layer);

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
	private static Component getUnderlyingComponent(int x, int y) {
	  Component underlyingPanel=null;
	  underlyingPanel = diagramPanel.getComponentAt(x, y);

	  /* ***DEBUG*** */
	  if(debug) System.out.println("underlyingPanel="+underlyingPanel
			  +"\nunderlyingPanel.getClass()"+underlyingPanel.getClass());
	  /* ***DEBUG*** */

	  if(underlyingPanel==null ||
		  ( !underlyingPanel.getClass().equals(AnchorPanel.class)
			&& !underlyingPanel.getClass().equals(GroupPanel.class)
			&& !underlyingPanel.getClass().equals(FeaturePanel.class) )
		) return null;
	  return underlyingPanel;
	}

	/**
	 * Adds the connector ending and starting dots to the lists used to draw connectors.
	 * 
	 * @param newConnectorStartDot - the starting connector dot
	 * @param newConnectorEndDot - the ending connector dot
	 */
	private static void addConnectorsToDrawLists(JPanel newConnectorStartDot, JPanel newConnectorEndDot) {
		startConnectorDots.add(newConnectorStartDot);
		prevStartConnectorDotsLocation.add(new Point());
		endConnectorDots.add(newConnectorEndDot);
		prevEndConnectorDotsLocation.add(new Point());
		connectorDotsToRedraw.add(true);
	}
	
	/**
	 * Adds the group to the lists used to draw connectors.
	 * 
	 * @param newConnectorStartDot - the starting connector dot
	 * @param newConnectorEndDot - the ending connector dot
	 */
	private static void addGroupToDrawLists(GroupPanel group) {
		groupPanels.add(group);		
	}
	
	/**
	 * Main Method.
	 * @param args
	 */
	public static void main(String[] args){
		MyDraggableImages editor= new MyDraggableImages();
		editor.setVisible(true);
//		editor.setResizable(false);
		editor.setExtendedState(editor.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		System.out.println("Ready! frameRoot.getLocationOnScreen(): "+frameRoot.getLocationOnScreen());


	}
}
