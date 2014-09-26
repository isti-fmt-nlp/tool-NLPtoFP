/**
 * @author Manuel Musetti
 */
package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.RepaintManager;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.IconView;
import javax.swing.text.JTextComponent;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.Highlighter.Highlight;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import main.XMLFileFilter;

import com.ibm.icu.impl.InvalidFormatException;

import main.FDEXMLHandler;
import main.GroupAnimationTimer;
import main.ImageUtils;
import main.OSUtils;
import main.OrderedList;
import main.OrderedListNode;
import main.SortUtils;
import main.StringUtils;
import main.ViewXMLHandler;


public class EditorView extends JFrame implements Observer{
	
	/** variables used for debugging*/
	private static boolean debug=true;
	private static boolean debug2=false;
	private static boolean debug3=false;
	private static boolean debug4=false;
	private static boolean debug5=false;
	
	private static final long serialVersionUID = 1L;

	/** Class used to implement the text area of the features */
	class CenteredTextPane extends JTextPane{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean contains(int x, int y){
		  if(!isEditable()) return false;
		  else return super.contains(x, y);
		}		
	}
	
	/** StyledEditorKit to be set on CenteredTextPane objects */
	class CenteredEditorKit extends StyledEditorKit {
				
		private static final long serialVersionUID = 1L;

		public ViewFactory getViewFactory() {
	        return new StyledViewFactory();
	    }	    
	}
	
	/** JLabel extended in order to not get mouse events */
	static class PressThroughLabel extends JLabel {
				
		private static final long serialVersionUID = 1L;

		public PressThroughLabel(ImageIcon toolImage) {
		  super(toolImage);
		}

		@Override
		public boolean contains(int x, int y){
		  return false;
		}    
	}
	
	/** ViewFactory used by CenteredEditorKit */
    class StyledViewFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) return new LabelView(elem);
                else if (kind.equals(AbstractDocument.ParagraphElementName)) return new ParagraphView(elem);
                else if (kind.equals(AbstractDocument.SectionElementName)) return new CenteredBoxView(elem, View.Y_AXIS);
                else if (kind.equals(StyleConstants.ComponentElementName)) return new ComponentView(elem);
                else if (kind.equals(StyleConstants.IconElementName)) return new IconView(elem);
            }	 

            return new LabelView(elem);
        }
    }
    
	/** BoxView used by StyledViewFactory */
	class CenteredBoxView extends BoxView {
		
	    public CenteredBoxView(Element elem, int axis) {
	        super(elem,axis);
	    }
	    
	    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
	        super.layoutMajorAxis(targetSpan,axis,offsets,spans);
	        
	        int textBlockHeight = 0;
	        int offset = 0;
	 
	        for (int i = 0; i < spans.length; i++) textBlockHeight += spans[i];

	        offset = (targetSpan - textBlockHeight) / 2;
	        for (int i = 0; i < offsets.length; i++) offsets[i] += offset;
	    }
	}    
	
	/** Class used to implement the draw area of the editor. */
	class ScrollLayeredPane extends JLayeredPane implements Scrollable{

		private static final long serialVersionUID = 1L;

		@Override
		public Dimension getPreferredScrollableViewportSize() {
		  Dimension viewportSize=new Dimension(getPreferredSize());
		  viewportSize.width*=scaleFactor;
		  viewportSize.height*=scaleFactor;		  
		  return viewportSize;
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		  if(orientation==SwingConstants.HORIZONTAL)
			return getWidth()/15;
		  else return getHeight()/15;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		  if(orientation==SwingConstants.HORIZONTAL)
			return getWidth()/5;
		  else return getHeight()/5;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
		  return false;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
		  return false;
		}
		
		@Override
		public void paint(Graphics g){
		  
		  ((Graphics2D)g).scale(scaleFactor, scaleFactor);

		  Graphics2D g2 = (Graphics2D)g.create();		
		  paintComponent(g);
		  paintBorder(g);
		  drawAllConnectors(g2);
		  paintChildren(g);//panels in the diagram panel are drawn over lines			  
		}
	}
	
	/** Class used to implement a zoomable JScrollPane. */
/*
	class ZoomableScrollPane extends JScrollPane{

		private static final long serialVersionUID = 1L;
		
		public ZoomableScrollPane(ScrollLayeredPane panel, int verticalScrollbarAsNeeded, int horizontalScrollbarAsNeeded){
		  super(panel, verticalScrollbarAsNeeded, horizontalScrollbarAsNeeded);
		}

		@Override
		public void paint(Graphics g){
			((Graphics2D)g).scale(scaleFactor, scaleFactor);
		    // Change the size of the panel
//		    setSize(origWidth * zoomFactor, origHeight * zoomFactor);
		    // Re-Layout the panel
//		    validate();
		    super.paint(g);
		}
	}
*/	

//	/** Class used to implement the editor contained in the frame. */
//	class EditorSplitPane extends JSplitPane{
//
//		public static final long serialVersionUID = 1L;
//
//		public EditorSplitPane(int horizontalSplit) {
//			super(horizontalSplit);
//		}
//
//		@Override
//		public void paint(Graphics g){
////			Graphics2D g2 = (Graphics2D)g.create();	
////			((Graphics2D)g).scale(scaleFactor, scaleFactor);
//			paintComponent(g);
//			paintBorder(g);
////			drawAllConnectors(g2);
//			paintChildren(g);//panels in the diagram panel are drawn over lines
//		}		
//	}


	/** prefix of any feature ID*/
	public static final String featureNamePrefix="---FEATURE---#";
	/** prefix of any text area owned by feature panels*/
	public static final String textAreaNamePrefix="---TEXTAREA---";
	/** prefix of any ImageIcon owned by anchor panels*/
	public static final String connectorImageNamePrefix="---IMAGEICON---";
	/** prefix of any connector starting dot name*/
	public static final String startMandatoryNamePrefix="---START_MANDATORY---#";
	/** prefix of any connector ending dot name*/
	public static final String endMandatoryNamePrefix="---END_MANDATORY---#";
	/** prefix of any connector starting dot name*/
	public static final String startOptionalNamePrefix="---START_OPTIONAL---#";
	/** prefix of any connector ending dot name*/
	public static final String endOptionalNamePrefix="---END_OPTIONAL---#";
	/** prefix of any connector starting dot name*/
	public static final String startIncludesNamePrefix="---START_INCLUDES---#";
	/** prefix of any connector ending dot name*/
	public static final String endIncludesNamePrefix="---END_INCLUDES---#";
	/** prefix of any connector starting dot name*/
	public static final String startExcludesNamePrefix="---START_EXCLUDES---#";
	/** prefix of any connector ending dot name*/
	public static final String endExcludesNamePrefix="---END_EXCLUDES---#";
	/** prefix of any constraint control point dot name*/
	public static final String constraintControlPointNamePrefix="---CONSTRAINT_CONTROL_POINT---#";
	/** prefix of any group Alternative Gtarting dot name*/
	public static final String altGroupNamePrefix="---ALT_GROUP---#";
	/** prefix of any Or Group starting dot name*/
	public static final String orGroupNamePrefix="---OR_GROUP---#";
	/** name ofthe diagram panel*/
	public static final String diagramPanelName="---DIAGRAM_PANEL---";
	/** name ofthe diagram panel*/
	public static final String toolsPanelname="---TOOLS_PANEL---";
	
	/** URL of the connector starting dot icon*/
	private static final URL connectorStartDotIconURL=EditorView.class.getResource("/Connector Start Dot.png");
	/** URL of the new group starting dot icon*/
	private static final URL ALTGroupDotIconURL=EditorView.class.getResource("/ALTGroup Start Dot.png");
	/** URL of the new group starting dot icon*/
	private static final URL ORGroupDotIconURL=EditorView.class.getResource("/ORGroup Start Dot.png");
	/** URL of the new mandatory connector ending dot icon*/
	private static final URL mandatoryConnectorEndDotIconURL=EditorView.class.getResource("/Mandatory End Dot2.png");
	/** URL of the new optional connector ending dot icon*/
	private static final URL optionalConnectorEndDotIconURL=EditorView.class.getResource("/Optional End Dot2.png");
	/** URL of the new constraint dot icon*/
	private static final URL constraintDotIconURL=EditorView.class.getResource("/Constraint Dot.png");
	/** URL of the new constraint control point dot icon*/
	private static final URL constraintControlPointDotIconURL=EditorView.class.getResource("/Constraint Control Point Dot.png");
	/** URL of the connector line-only icon*/
	private static final URL connectorLineLengthIconURL=EditorView.class.getResource("/Connector Line Length.png");
	/** URL of the group line-only icon*/
	private static final URL groupLineLengthIconURL=EditorView.class.getResource("/Group Line Length.png");
	
	/** Base preferred size for the diagramPanel*/
	private static Dimension diagramPanelBasePreferredSize = 
		new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width-160,
					  Toolkit.getDefaultToolkit().getScreenSize().height);
	
	/** Unmutable Base preferred size fot the diagramPanel at normal scale*/
//	private final static Dimension diagramPanelBasePreferredSizeNormalScale = new Dimension(diagramPanelBasePreferredSize);
	
	/** Base preferred size fot the diagramPanel*/
	private static Dimension diagramPanelMinimumPreferredSize = 
		new Dimension((Toolkit.getDefaultToolkit().getScreenSize().width-160)/6,
					   Toolkit.getDefaultToolkit().getScreenSize().height);
	
	/** Base preferred size fot the diagramScroller*/
	private static Dimension diagramScrollerBasePreferredSize = 
		new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width-160,
					  Toolkit.getDefaultToolkit().getScreenSize().height);
	
	/** Base preferred size fot the diagramScroller*/
	private static Dimension diagramScrollerMinimumPreferredSize = 
		new Dimension((Toolkit.getDefaultToolkit().getScreenSize().width-160)/6,
					   Toolkit.getDefaultToolkit().getScreenSize().height);
	

	/** maps tool names in the corresponding icon resource path*/
	private static HashMap<String, String> toolIconPaths=null;
	
	private static int featureBorderSize=20;

	/** enumeration of items that can become active, for instance in a drag motion*/
	public static enum ActiveItems {
		NO_ACTIVE_ITEM, DRAGGING_FEATURE, DRAGGING_EXTERN_ANCHOR, DRAGGING_EXTERN_GROUP, DRAGGING_EXTERN_CONSTRAINT,
		DRAGGING_CONSTRAINT_CONTROL_POINT, DRAGGING_SELECTION_RECT, DRAGGING_SELECTION_GROUP, 
		DRAGGING_TOOL_NEWFEATURE, DRAGGING_TOOL_MANDATORY_LINK, DRAGGING_TOOL_OPTIONAL_LINK, 
		DRAGGING_TOOL_ALT_GROUP, DRAGGING_TOOL_OR_GROUP, DRAGGING_TOOL_INCLUDES, DRAGGING_TOOL_EXCLUDES
	}

	/** enumeration used to specify a item type through the program*/
	public static enum ItemsType {
		START_MANDATORY_CONNECTOR, END_MANDATORY_CONNECTOR, START_OPTIONAL_CONNECTOR, END_OPTIONAL_CONNECTOR,
		START_INCLUDES_DOT, END_INCLUDES_DOT, START_EXCLUDES_DOT, END_EXCLUDES_DOT, CONSTRAINT_CONTROL_POINT,
		ALT_GROUP_START_CONNECTOR, OR_GROUP_START_CONNECTOR
	}
	
	public enum ArcSizes {
		MAXIMUM(1.0), HUGE(0.8), BIG(0.6), HALFSIZE(0.5), MEDIUM(0.4), SMALL(0.2), TINY(0.1);
		private double value;

		private ArcSizes (double value) {
		  this.value = value;
		}
	};  


	/** names used to specify specific tools through the program*/
	public static final String TOOL_NEWFEATURE="New Feature";
	public static final String TOOL_MANDATORY_LINK="Mandatory Link";
	public static final String TOOL_OPTIONAL_LINK="Optional Link";
	public static final String TOOL_INCLUDES="Includes";
	public static final String TOOL_EXCLUDES="Excludes";
	public static final String TOOL_OR_GROUP="Or Group";
	public static final String TOOL_ALT_GROUP="Alternative Group";
	
	/** Tells which action should be performed when the JFrame is closed*/
	private int onCloseOperation = JFrame.DISPOSE_ON_CLOSE;
	
	/** The popup menu for all diagram panel elements*/
	private JPopupMenu diagramElementsMenu = new JPopupMenu();
	/** The element interested by the popup menu*/
	private JComponent popUpElement = null;
	
	/** Popup menu items*/
	private JMenuItem popMenuItemDelete = new JMenuItem("Delete Element");
	private JMenuItem popMenuItemDeleteFeature = new JMenuItem("Delete Feature");

	private ImageIcon colorIcon = new ImageIcon(getClass().getResource("/Color/color.png"));
	private JMenuItem popMenuItemChangeColor=new JMenuItem("Change Color", colorIcon);	

	private ImageIcon searchIcon = new ImageIcon(getClass().getResource("/Search/magnifier glasses-min3.png"));
	private JMenuItem popMenuItemSearchFeature=new JMenuItem("Search Feature", searchIcon);	

	private JMenuItem popMenuItemRenameFeature = new JMenuItem("Rename Feature");
	private JMenuItem popMenuItemDeleteConnector = new JMenuItem("Delete Connector");
	private JMenuItem popMenuItemDeleteGroup = new JMenuItem("Delete Group");
	private JMenuItem popMenuItemDeleteConstraint = new JMenuItem("Delete Constraint");
	private JMenuItem popMenuItemUngroup = new JMenuItem("Ungroup");

	private JMenuItem popMenuItemShowControlPoint = new JMenuItem("Show Control Point");
	private JMenuItem popMenuItemHideControlPoint = new JMenuItem("Hide Control Point");

	private JMenuItem popMenuItemPrintModelDebug = new JMenuItem("Print Model[DEBUG COMMAND]");
	private JMenuItem popMenuItemFitDiagram = new JMenuItem("Fit Diagram");
	
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
	private JMenuItem menuFilesNew=null, menuFilesLoad=null, menuFilesSave=null, menuFilesImportFromSXFM=null,
					  menuFilesExportAsSXFM=null, menuFilesExportAsPNG=null, menuFilesExportAsGIF=null,
					  menuFilesDelete=null, menuFilesExit=null;

	/** View Menu items*/
	private JMenuItem /*menuViewColored=null,*/ menuViewCommsOrVars=null, 
					  menuViewExtrOrInsert=null, menuViewFields=null, 
					  menuViewZoomUp=null, menuViewZoomDown=null/*,
					  menuViewVisibleConstraints=null*/;
	
	/** Modify Menu items*/
	private JMenuItem menuModifyBasicFM=null, menuModifyAdvancedFM=null;
		
	
	/** Number of connector dots created*/
	private int connectorsCount=0;
	/** Number of constraint dots created*/
	private int constraintsCount=0;
	/** Number of constraint control point dots created*/
	private int constraintControlsCount=0;
	/** Number of Alternative Groups created*/
	private int altGroupsCount=0;
	/** Number of Or Groups created*/
	private int orGroupsCount=0;
	/** Number of features created*/
	private int featuresCount=0;
	/** Current number of timers*/
//	private int timersCount=0;
	
	/** List of all connector starting dots*/
	private ArrayList<JComponent> startConnectorDots=null;
	/** List of Includes costraints starting dots*/
	private ArrayList<JComponent> startIncludesDots=null;
	/** List of Excludes costraints starting dots*/
	private ArrayList<JComponent> startExcludesDots=null;
	/** List of Alternative Groups*/
	private ArrayList<GroupPanel> altGroupPanels=null;	
	/** List of Or Groups*/
	private ArrayList<GroupPanel> orGroupPanels=null;	
	
	/** The group opener Timer*/
	private GroupAnimationTimer openerTimer=null;
	/** List of group closer Timers*/
	private ArrayList<GroupAnimationTimer> closerTimers=new ArrayList<GroupAnimationTimer>();	
	/** Timer used to animate opening and closing group circles*/
	private Timer globalTimer=null;
	/** maximum opening radius for groups dock areas*/
	private double groupDockRadius = 14.0;
	/** radius step increment amount for opening animations*/
	private double groupDockOpeningStepAmount = 1.6;
	/** radius step increment amount for closing animations*/
	private double groupDockClosingStepAmount = -0.6;
	/** scaling factor*/
	private double scaleFactor=1.0;
	
	/** Size of all group arcs, 1 is the maximum*/
	private double arcSize=ArcSizes.MEDIUM.value;
	
	/** List of starting commonalities selected by the user */
	private ArrayList<String> startingCommonalities=new ArrayList<String>();
	/** List of starting commonalities and variabilities selected by the user */
	private ArrayList<String> startingVariabilities=new ArrayList<String>();

	/** List of features which name has been changed by applying an alternative visual style*/
	private ArrayList<CenteredTextPane> textAreaList = new ArrayList<EditorView.CenteredTextPane>();

	/** Contains the color for all starting features*/
	private HashMap<String, int[]> termsColor=null;
	
	/** Occurences of features in all input files*/
	private HashMap<String, HashMap<String, ArrayList<int[]>>> relevantTerms=null;
	
	/** Contains both versions of feature names,
	 * with the computed version at index 0 and the extracted one at index 1
	 */
	private HashMap<String, HashMap<String, ArrayList<String>>> relevantTermsVersions=null;
	
	/** OrderedList containing the panels children of the diagram panel*/
	private OrderedList visibleOrderDraggables = null;
	
	/** X coordinate of last mouse pression*/
	private int lastPositionX=-1;
	/** Y coordinate of last mouse pression*/
	private int lastPositionY=-1;
	/** X amount of last mouse move*/
	private int lastMoveX=0;
	/** Y amount of last mouse move*/
	private int lastMoveY=0;
	
	/** Top level frame*/
	private JFrame frameRoot = null;//frame root		
	
	/** Image used to drag tools*/
	private BufferedImage toolDragImage = null;
	/** Position of the dragged image*/
	private Point toolDragPosition = null;	
	
	/** Old name of the feature about to be renamed*/
	private String oldFeatureName=null;
	
	/** Name of the next feature to add*/
	private String featureToAddName = null;
	
	private int verticalShift=0;
	private int horizontalShift=0;
	
	/** The panel containing the diagram */
	private ScrollLayeredPane diagramPanel=null;	
	
	/** The JScrollPane containing the diagramPanel */
	private JScrollPane diagramScroller=null;
		
	/** The horizontal JScrollBar of the diagramScroller */
	private  JScrollBar hori = null;	
	/** The vertical JScrollBar of the diagramScroller */
	private  JScrollBar vert = null;	
	/** Current position of hori*/
	private int horPos=0;
	/** Current position of vert*/
	private int verPos=0;
	
	/** The panel containing the tools */
	private JPanel toolsPanel=null;
	
	/** The JFrame used to display search panels*/
	private JFrame searchFrame=null;
	/** The panel searchFrame used to search for feature occurrences*/
	private JSplitPane searchPanel = null;
	/**buttons for navigating through commonalitie occurences in tab texts, the X...wardButtons move of x occurences, 
	 where x is defined by occurrJumpSpan constant*/
	private JButton nextOccurrButton = null, prevOccurrButton = null, XForwardOccurrButton = null, XBackwardOccurrButton = null;
	/**defines the number x of occurences jumped by XForwardOccurrButton and XBackwardOccurrButton*/
	private int occurrJumpSpan=4;
	/**label for occurrences navigation*/
	private JLabel occurrsLabel = null;	
	/**label for occurrences navigation*/
	private JPanel occurrsLabelPanel = null;
	/**last highlighted tag for each feature and file*/
	private HashMap<String, HashMap<String, Object>> lastHighlightedTag=null;
	/** For each feature name(outer map's Key) and file name(inner map's Key), there is a list
	 *  of last removed highlight tags, each potentially having a list of replacement tags*/
	private HashMap<String, HashMap<String, ArrayList<Entry<Highlight, ArrayList<Highlight>>>>> lastRemovedHighlights=null;	
	/**relevant terms occurrences panel*/
	private JTabbedPane occursTabbedPane = null;	
	/**relevant terms search buttons panel*/
	private JPanel buttonPanel = null;
	/**the JTextAreas of search panel*/
	private HashMap<String, JTextArea> textTabs = null;
	/**association between relevant terms and current selected tab file names in search panel*/
	private HashMap<String, String> currentFiles = null;
	/**indexes of current selected occurrences in project input files*/
	private HashMap<String, HashMap<String, Integer>> textIndexes = null;	
	/**current selected checkbox*/
	private String currentSelectedFeatureName=null;
	
	/** RGB values of the colors used in the search for feature occurrences*/
	private static int[] PCcol={160, 160, 0}, PVcol={0, 160, 160}, ACcol={255, 255, 0}, AVcol={0, 255, 255}, NEcol={160, 0, 0};

	/** HighlightPainter objects used to apply the colors in the search for feature occurrences*/
	private static Highlighter.HighlightPainter passiveCommHighlightPainter = 
			new DefaultHighlighter.DefaultHighlightPainter(getNewColor(PCcol)), 
		passiveVarsHighlightPainter= new DefaultHighlighter.DefaultHighlightPainter(getNewColor(PVcol)), 
		activeCommHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(getNewColor(ACcol)), 
		activeVarsHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(getNewColor(AVcol));
	
	/** The splitter panel containing diagramPanel and toolsPanel*/
//	private EditorSplitPane splitterPanel=null;
	private JSplitPane splitterPanel=null;	
	
	/** The active Feature panel*/
	private FeaturePanel lastFeatureFocused=null;
	/** The active Anchor panel*/
	private JComponent lastAnchorFocused=null;
	/** List of active elements selected as group by the user*/
	private ArrayList<JComponent> selectionGroupFocused=null;
	
	/** The component on which a drop is about to be done*/
	private JComponent underlyingComponent=null;

	/** Tells what item is interested in the current action*/
	private ActiveItems isActiveItem=ActiveItems.NO_ACTIVE_ITEM;

	/** Variable used to draw lines on the diagram */
	private Point lineStart=new Point(), lineEnd=new Point();
	/** Variable used to draw selection rectangle on the diagram */
	private Point startSelectionRect=new Point(), endSelectionRect=new Point();
	/** Variable used to draw selection rectangle on the diagram */
	private Rectangle selectionRect=new Rectangle();
	/** Variable used to track elements that collided with horizontal border during group drag*/
	private ArrayList<JComponent> collidedElementX=new ArrayList<JComponent>();
	/** Variable used to track elements that collided with vertical border during group drag*/
	private ArrayList<JComponent> collidedElementY=new ArrayList<JComponent>();

	public EditorView(){
	  this.termsColor = new HashMap<String, int[]>();
	  this.relevantTerms = new HashMap<String, HashMap<String, ArrayList<int[]>>>();
	  this.relevantTermsVersions = new HashMap<String, HashMap<String, ArrayList<String>>>();	
	  this.setTitle("Feature Diagram Editor");
	}
	
	/**
	 * Creates a new EditorView, with lists of starting features and associated colors,
	 * name versions and occurences in the input files.
	 * 
	 * @param commonalitiesSelected - list of starting commonalities
	 * @param variabilitiesSelected - list of starting variabilities
	 * @param colorsMap - associations between feature names and colors
	 * @param relevantTerms - occurences of features in all input files
	 * @param relevantTermsVersions - list of String[] with both versions of feature names, 
	 * with the computed version at index 0 and the extracted one at index 1
	 */
	public EditorView(ArrayList<String> commonalitiesSelected,
			   		  ArrayList<String> variabilitiesSelected,
			   		  HashMap<String, int[]> colorsMap,
			   		  HashMap<String, HashMap<String, ArrayList<int[]>>> relevantTerms,
			   		  HashMap<String, HashMap<String, ArrayList<String>>> relevantTermsVersions) {
		
	  this.termsColor = new HashMap<String, int[]>();
	  this.relevantTerms = new HashMap<String, HashMap<String, ArrayList<int[]>>>();
	  this.relevantTermsVersions = new HashMap<String, HashMap<String, ArrayList<String>>>();
	  this.setTitle("Feature Diagram Editor");
		  
	  /* ***VERBOSE*** */
	  //printing all relevantTerms occurrences
	  Iterator<Entry<String, HashMap<String, ArrayList<int[]>>>> termIterVERB = relevantTerms.entrySet().iterator();
	  Entry<String, HashMap<String, ArrayList<int[]>>> termEntryVERB=null;

	  Iterator<Entry<String, ArrayList<int[]>>> fileIterVERB = null;
	  Entry<String, ArrayList<int[]>> fileEntryVERB=null;
	  
	  String termName=null;
	  while(termIterVERB.hasNext()){
		termEntryVERB=termIterVERB.next();
		termName=termEntryVERB.getKey();
		System.out.println("\n***Term: "+termName);
		fileIterVERB=termEntryVERB.getValue().entrySet().iterator();
		while(fileIterVERB.hasNext()){
		  fileEntryVERB=fileIterVERB.next();
		  System.out.println("******File: "+fileEntryVERB.getKey());
		}
	  }	  
	  /* ***VERBOSE*** */
	  
	  if(commonalitiesSelected!=null)
		for(String name : commonalitiesSelected){
		  startingCommonalities.add(name);
		  this.termsColor.put(name, colorsMap.get(name));
		  this.relevantTerms.put(name, relevantTerms.get(name));
		  if(relevantTermsVersions.get(name)!=null) 
		      this.relevantTermsVersions.put(name, relevantTermsVersions.get(name));		  
		}
	  
	  if(variabilitiesSelected!=null)
		for(String name : variabilitiesSelected){
		  startingVariabilities.add(name);
		  this.termsColor.put(name, colorsMap.get(name));		  
		  this.relevantTerms.put(name, relevantTerms.get(name));
		  if(relevantTermsVersions.get(name)!=null) 
		      this.relevantTermsVersions.put(name, relevantTermsVersions.get(name));
		}
	  

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
		menuFiles.setMnemonic(KeyEvent.VK_F);

		menuView = new JMenu("View");
		menuView.setMnemonic(KeyEvent.VK_V);

		menuModify = new JMenu("Modify");
		menuModify.setMnemonic(KeyEvent.VK_M);

		/*Menu Files items*/
		menuFilesNew = new JMenuItem("New Diagram");
		menuFilesNew.addActionListener(editorController);
		menuFilesNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		
		menuFilesSave = new JMenuItem("Save Diagram");
		menuFilesSave.addActionListener(editorController);
		menuFilesSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		
		menuFilesLoad = new JMenuItem("Load Diagram");
		menuFilesLoad.addActionListener(editorController);
		menuFilesLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		
		menuFilesImportFromSXFM = new JMenuItem("Import from SXFM");
		menuFilesImportFromSXFM.addActionListener(editorController);
		menuFilesImportFromSXFM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		
		menuFilesExportAsSXFM = new JMenuItem("Export as SXFM");
		menuFilesExportAsSXFM.addActionListener(editorController);
		menuFilesExportAsSXFM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		
		menuFilesExportAsPNG = new JMenuItem("Export as PNG");
		menuFilesExportAsPNG.addActionListener(editorController);
		menuFilesExportAsPNG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		
		menuFilesExportAsGIF = new JMenuItem("Export as GIF");
		menuFilesExportAsGIF.addActionListener(editorController);
		menuFilesExportAsGIF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));

		menuFilesDelete = new JMenuItem("Delete Diagram");
		menuFilesDelete.addActionListener(editorController);
		
		menuFilesExit = new JMenuItem("Exit");
		menuFilesExit.addActionListener(editorController);
		menuFilesExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		
		
		menuFiles.add(menuFilesNew);
		menuFiles.add(menuFilesSave);
		menuFiles.add(menuFilesLoad);
		menuFiles.addSeparator();
		menuFiles.add(menuFilesImportFromSXFM);
		menuFiles.add(menuFilesExportAsSXFM);
		menuFiles.add(menuFilesExportAsPNG);
		menuFiles.add(menuFilesExportAsGIF);
		menuFiles.addSeparator();
		menuFiles.add(menuFilesDelete);
		menuFiles.add(menuFilesExit);

		/*Menu View items*/		
		menuViewCommsOrVars = new JCheckBoxMenuItem("View Commonality/Variability");
		menuViewCommsOrVars.addActionListener(editorController);
		menuViewCommsOrVars.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		
		menuViewExtrOrInsert = new JMenuItem("View Extracted/Inserted");
		menuViewExtrOrInsert.addActionListener(editorController);
		menuViewExtrOrInsert.setEnabled(false);

		menuViewFields = new JMenuItem("View Feature's Fields");
		menuViewFields.addActionListener(editorController);
		menuViewFields.setEnabled(false);

		menuViewZoomUp = new JMenuItem("ZoomUp +");
		menuViewZoomUp.addActionListener(editorController);
		menuViewZoomUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, ActionEvent.CTRL_MASK));

		menuViewZoomDown = new JMenuItem("ZoomDown -");
		menuViewZoomDown.addActionListener(editorController);
		menuViewZoomDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, ActionEvent.CTRL_MASK));
		
		
		menuView.add(menuViewCommsOrVars);
		menuView.add(menuViewExtrOrInsert);
		menuView.add(menuViewFields);
		menuView.add(menuViewZoomUp);
		menuView.add(menuViewZoomDown);

		/*Menu Modify items*/
		menuModifyBasicFM = new JRadioButtonMenuItem("Basic Feature Model");
		menuModifyBasicFM.addActionListener(editorController);
		menuModifyBasicFM.setEnabled(false);
		
		menuModifyAdvancedFM = new JRadioButtonMenuItem("Advanced Feature Model");
		menuModifyAdvancedFM.addActionListener(editorController);	
		menuModifyAdvancedFM.setEnabled(false);
		
		
		menuModify.add(menuModifyBasicFM);
		menuModify.add(menuModifyAdvancedFM);
		
		
		//adding JMenus to JMenuBar
		menu.add(menuFiles);
		menu.add(menuView);
		menu.add(menuModify);
		
		setJMenuBar(menu);

		//initializing diagram popup menu
		popMenuItemDeleteFeature.setText("Delete Feature");
		popMenuItemDeleteFeature.setActionCommand("Delete Element");
		popMenuItemDeleteFeature.addActionListener(editorController);
		
		popMenuItemRenameFeature.addActionListener(editorController);
		popMenuItemChangeColor.addActionListener(editorController);
		popMenuItemSearchFeature.addActionListener(editorController);
		
		popMenuItemDeleteConnector.setText("Delete Connector");
		popMenuItemDeleteConnector.setActionCommand("Delete Element");
		popMenuItemDeleteConnector.addActionListener(editorController);

		popMenuItemDeleteGroup.setText("Delete Group");
		popMenuItemDeleteGroup.setActionCommand("Delete Element");
		popMenuItemDeleteGroup.addActionListener(editorController);
		
		popMenuItemDeleteConstraint.setText("Delete Constraint");
		popMenuItemDeleteConstraint.setActionCommand("Delete Element");
		popMenuItemDeleteConstraint.addActionListener(editorController);
		
		popMenuItemDelete.addActionListener(editorController);
        popMenuItemUngroup.addActionListener(editorController);        
        popMenuItemPrintModelDebug.addActionListener(editorController);  
    	popMenuItemFitDiagram.addActionListener(editorController);  

    	popMenuItemShowControlPoint.addActionListener(editorController);
    	popMenuItemHideControlPoint.addActionListener(editorController);
    	
    	//initializing main structures
		visibleOrderDraggables = new OrderedList();
		startConnectorDots = new ArrayList<JComponent>();
		startIncludesDots = new ArrayList<JComponent>();
		startExcludesDots = new ArrayList<JComponent>();
		altGroupPanels = new ArrayList<GroupPanel>();
		orGroupPanels = new ArrayList<GroupPanel>();

		selectionGroupFocused = new ArrayList<JComponent>();
		
		
		//creating root frame
		frameRoot=this;
		setLayout(new BorderLayout());		
		setDefaultCloseOperation(onCloseOperation);
		addWindowListener(editorController);
		System.out.println("current DefaultCloseOperation: "+onCloseOperation);

		//creating tools panel
		toolsPanel = new JPanel();		
		toolsPanel.setLayout(new GridLayout(0, 2, 2, 2));		
//		toolsPanel.setPreferredSize(new Dimension(140, Toolkit.getDefaultToolkit().getScreenSize().height));
		toolsPanel.setPreferredSize(new Dimension(140, Toolkit.getDefaultToolkit().getScreenSize().height/2));
		toolsPanel.setBackground(Color.white);
		toolsPanel.setName(toolsPanelname);
//		toolsPanel.setBorder(BorderFactory.createCompoundBorder(
//				BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
//						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		
		//creating tools items
		toolIconPaths=new HashMap<String, String>();
//		toolIconPaths.put("New Feature", "/New Feature2.png");
		toolIconPaths.put("New Feature", "/New Feature3.png");
//		toolIconPaths.put("Mandatory Link", "/Mandatory Link.png");
		toolIconPaths.put("Mandatory Link", "/Mandatory Link_2nd.png");		
//		toolIconPaths.put("Optional Link", "/Optional Link.png");
		toolIconPaths.put("Optional Link", "/Optional Link_2nd.png");
		toolIconPaths.put("Excludes", "/Excludes.png");
		toolIconPaths.put("Includes", "/Includes.png");
//		toolIconPaths.put("Alternative Group", "/Alternative Group.png");
		toolIconPaths.put("Alternative Group", "/Alternative Group_2nd_tmp4.png");
//		toolIconPaths.put("Or Group", "/Or Group.png");
//		toolIconPaths.put("Or Group", "/Or Group_2nd_tmp2.png");
		toolIconPaths.put("Or Group", "/Or Group_2nd_tmp4.png");		
		toolIconPaths.put("Start Link Dot", "/Connector Start Dot.png");		
				
		JComponent iconTmpPanel=null;

		iconTmpPanel=getToolIcon(TOOL_MANDATORY_LINK, true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

		iconTmpPanel=getToolIcon(TOOL_OPTIONAL_LINK, true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

		iconTmpPanel=getToolIcon(TOOL_INCLUDES, true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

		iconTmpPanel=getToolIcon(TOOL_EXCLUDES, true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

		iconTmpPanel=getToolIcon(TOOL_ALT_GROUP, true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

		iconTmpPanel=getToolIcon(TOOL_OR_GROUP, true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);	

		iconTmpPanel=getToolIcon(TOOL_NEWFEATURE, true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

//		toolsPanel.addMouseListener(editorController);
//		toolsPanel.addMouseMotionListener(editorController);

		//creating diagram panel, that will be inserted in the scroller
		diagramPanel = new ScrollLayeredPane();
		diagramPanel.setName(diagramPanelName);
		diagramPanel.setLayout(null);		
		diagramPanel.setPreferredSize(diagramPanelBasePreferredSize);
		diagramPanel.setMinimumSize(diagramPanelMinimumPreferredSize);
		

		//creating diagram scroller, which will fit the rest of the root frame		
		diagramScroller=new JScrollPane( diagramPanel, 
				   ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		diagramScroller=new ZoomableScrollPane( diagramPanel, 
//				   ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
//				   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		

//		diagramScroller.getViewport().getExtentSize(); 
//		diagramScroller.getViewport().getSize(); 

		diagramScroller.setPreferredSize(diagramScrollerBasePreferredSize);
		diagramScroller.setMinimumSize(diagramScrollerMinimumPreferredSize);
		diagramScroller.setWheelScrollingEnabled(false);
		
		hori = diagramScroller.getHorizontalScrollBar();
		vert = diagramScroller.getVerticalScrollBar();

		
//		Dimension minDim=diagramScroller.getMinimumSize();
//		System.out.println("********\nminDim.width: "+minDim.width
//				+"\nminDim.height: "+minDim.height
//				+"\nminDim.getWidth(): "+minDim.getWidth()
//				+"\nminDim.getHeight(): "+minDim.getHeight());
//		
//		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
//		System.out.println("********\nscreenDim.width: "+screenDim.width
//				+"\nscreenDim.height: "+screenDim.height
//				+"\nscreenDim.getWidth(): "+screenDim.getWidth()
//				+"\nscreenDim.getHeight(): "+screenDim.getHeight());

		toolsPanel.setMinimumSize(new Dimension((Toolkit.getDefaultToolkit().getScreenSize().width-160)/12,
				Toolkit.getDefaultToolkit().getScreenSize().height));		
//		toolsPanel.setMinimumSize(new Dimension((Toolkit.getDefaultToolkit().getScreenSize().width-160)/12,
//				Toolkit.getDefaultToolkit().getScreenSize().height/2));
		
//		splitterPanel = new EditorSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitterPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);		
		splitterPanel.setContinuousLayout(true);
		splitterPanel.setDividerSize(6);
//		splitterPanel.setOneTouchExpandable(true);
//		splitterPanel.setDividerLocation(0.5);
//		splitterPanel.setDoubleBuffered(true);
//		splitterPanel.setLocation(0,0);
//		splitterPanel.setResizeWeight(0.5);
		splitterPanel.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width,
				Toolkit.getDefaultToolkit().getScreenSize().height));
		
		JPanel controllerPanel=new JPanel();
		controllerPanel.setLayout(new BorderLayout());

		
		JPanel treePanel=new JPanel();		
		treePanel.setPreferredSize(new Dimension(140, Toolkit.getDefaultToolkit().getScreenSize().height));
		treePanel.setMinimumSize(new Dimension(140, Toolkit.getDefaultToolkit().getScreenSize().height));
		treePanel.setSize(new Dimension(140, Toolkit.getDefaultToolkit().getScreenSize().height));
		treePanel.setBackground(Color.RED);
		treePanel.add(new JLabel("Caio!"));

//		controllerPanel.add(toolsPanel, BorderLayout.NORTH);		
//		controllerPanel.add(treePanel, BorderLayout.SOUTH);
//		
//		splitterPanel.add(controllerPanel);
		
		splitterPanel.add(toolsPanel);
		splitterPanel.add(diagramScroller);
		
				
		add(splitterPanel);


		diagramPanel.addMouseListener(editorController);
		diagramPanel.addMouseMotionListener(editorController);
		diagramPanel.addMouseWheelListener(editorController);
		

//		setSize(Toolkit.getDefaultToolkit().getScreenSize());
//		setUndecorated(true);
		setLocation(0, 0);
		setMinimumSize(new Dimension(500, 500));
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		setVisible(true);
		setPreferredSize(getSize());
		validate();
		
//		OSUtils.createAndShowFDETray(trayIconURL, editorController);
//
		
/*		
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
		
*/				
//		Dimension currentDim=diagramScroller.getSize();
//		System.out.println("********\ncurrentDim.width: "+currentDim.width
//		+"\ncurrentDim.height: "+currentDim.height
//		+"\ncurrentDim.getWidth(): "+currentDim.getWidth()
//		+"\ncurrentDim.getHeight(): "+currentDim.getHeight());
        return true;
	}

	@Override
	public void paint(Graphics g) {
//		double radius=0.;		

//		((Graphics2D)g).scale(scaleFactor, scaleFactor);
		
		super.paint(g);

		Graphics2D g2 = (Graphics2D)g.create();
		Graphics2D g3 = null;
		
		/* ***DEBUG*** */
		if(debug3) System.out.println("PAINT: isActiveItem="+isActiveItem);
		/* ***DEBUG*** */
		
		//drawing tool icon being dragged
		if(toolDragImage!=null) 
		  g2.drawImage(toolDragImage, toolDragPosition.x, toolDragPosition.y, null);
		//drawing selection rectangle
		if(isActiveItem==ActiveItems.DRAGGING_SELECTION_RECT){
//		  g3=(Graphics2D)g2.create();
//		  g3.scale(scaleFactor, scaleFactor);
		  g2.setColor(Color.BLUE);
		  g2.draw(selectionRect);			
		}
		//drawing selection group borders
		if(selectionGroupFocused.size()>0){
//		  g3=(Graphics2D)g2.create();
//		  g3.scale(scaleFactor, scaleFactor);
//		  g2.setColor(Color.BLUE);
		  g3=(Graphics2D)diagramPanel.getGraphics();
		  g3.setColor(Color.BLUE);
		  
		  Rectangle elementSelectionFrame=new Rectangle();
		  for(Object selectedElement : selectionGroupFocused.toArray()){
			/* ***DEBUG*** */
			if(debug3) System.out.println("selectionGroup element: "+((JComponent)selectedElement).getName()
					  +"coords: "+((JComponent)selectedElement).getLocationOnScreen());
			/* ***DEBUG*** */
				
			elementSelectionFrame.setLocation(
			  (int)( (((JComponent)selectedElement).getLocationOnScreen().getX()
					  -diagramPanel.getLocationOnScreen().getX())*scaleFactor
					  /*+this.getLocationOnScreen().getX()*/-3 ),
			  (int)( (((JComponent)selectedElement).getLocationOnScreen().getY()
					  -diagramPanel.getLocationOnScreen().getY())*scaleFactor
					  /*+this.getLocationOnScreen().getY()*/-3 ));
			elementSelectionFrame.setSize(
			  (int)(((JComponent)selectedElement).getWidth()*scaleFactor)+6,
			  (int)(((JComponent)selectedElement).getHeight()*scaleFactor)+6);

			g3.draw(elementSelectionFrame);
		  }
		}
		//drawing groups dock opening animation
		if(openerTimer!=null && openerTimer.getRadius()>0.0){
		  drawCirclesAroundGroups(openerTimer.getRadius(), g2);
		}
		//drawing groups dock closing animations
		drawAllGroupClosingAnimations(g2);
	}


	/**
	 * Draws the circles around groups for the dock opening and closing animations, for all closer timers.
	 * 
	 * @param g2 - Graphics2D object on which to draw
	 */
	public void drawAllGroupClosingAnimations(Graphics2D g2) {
	  for(int i=0; i<closerTimers.size(); ++i){
		if(closerTimers.get(i).getRadius()>=1) drawCirclesAroundGroups(closerTimers.get(i).getRadius(), g2);
		drawMergingAnchor(g2, closerTimers.get(i));
	  }
	}

	/**
	 * Draws the image of a connector starting anchor on the frame.
	 * 
	 * @param g2 - Graphics2D object on which to draw
	 * @param timer - the GroupAnimationTimer containing image and image location data 
	 */
	public void drawMergingAnchor(Graphics2D g2, GroupAnimationTimer timer) {
		int anchorImageLocationX=0;
		int anchorImageLocationY=0;
		BufferedImage imageToDraw=timer.getAnchorImage();
		if(imageToDraw!=null){
		  //repainting previous location of anchor image
		  if(timer.getAnchorImagePrevLocation()!=null){
//			anchorImageLocationX=(int)(timer.getAnchorImagePrevLocation().getX()-frameRoot.getLocationOnScreen().getX());
//			anchorImageLocationY=(int)(timer.getAnchorImagePrevLocation().getY()-frameRoot.getLocationOnScreen().getY());
			anchorImageLocationX=(int)(timer.getAnchorImagePrevLocation().getX());
			anchorImageLocationY=(int)(timer.getAnchorImagePrevLocation().getY());
		    this.repaint(anchorImageLocationX, anchorImageLocationY, imageToDraw.getWidth(), imageToDraw.getHeight());
		  }
			
//		  anchorImageLocationX=(int)(timer.getAnchorImageLocation().getX()-frameRoot.getLocationOnScreen().getX());
//		  anchorImageLocationY=(int)(timer.getAnchorImageLocation().getY()-frameRoot.getLocationOnScreen().getY());
		  anchorImageLocationX=(int)(timer.getAnchorImageLocation().getX());
		  anchorImageLocationY=(int)(timer.getAnchorImageLocation().getY());

//		  System.out.println("anchorImageLocationX: "+anchorImageLocationX
//				  		  +"\nanchorImageLocationY: "+anchorImageLocationY);	
//		  g2.drawImage(imageToDraw, anchorImageLocationX, anchorImageLocationY, null);	
		  g2.drawImage(
			imageToDraw, anchorImageLocationX, anchorImageLocationY,
			(int)(imageToDraw.getWidth()*scaleFactor), (int)(imageToDraw.getHeight()*scaleFactor), null);	
		}
	}

	/**
	 * Draws the circles around groups for the dock opening and closing animations.
	 * 
	 * @param radius - radius of the circles
	 * @param g2 - Graphics2D object on which to draw
	 */
	public void drawCirclesAroundGroups(double radius, Graphics2D g2) {
		Ellipse2D ellipse=null;
		Point2D ellipseCenter = null;
		g2.setColor(Color.BLUE);
		  if(radius>=1.){
			for(GroupPanel group : orGroupPanels){
			  ellipseCenter = getVisibleStartAnchorCenterOnView(group);
			  ellipse = new Ellipse2D.Double(ellipseCenter.getX()-radius, ellipseCenter.getY()-radius, radius*2, radius*2);

			  repaint((int)(ellipseCenter.getX()-radius)-3, (int)(ellipseCenter.getY()-radius)-3,
					  (int)(radius*2)+6, (int)(radius*2)+6);
			  g2.draw(ellipse);
			}
			for(GroupPanel group : altGroupPanels){
			  ellipseCenter = getVisibleStartAnchorCenterOnView(group);
			  ellipse = new Ellipse2D.Double(ellipseCenter.getX()-radius, ellipseCenter.getY()-radius, radius*2, radius*2);

			  repaint((int)(ellipseCenter.getX()-radius)-3, (int)(ellipseCenter.getY()-radius)-3,
					  (int)(radius*2)+6, ((int)radius*2)+6);
			  g2.draw(ellipse);					
			}
		  }
	}

	/**
	 * Draws all connectors lines inside the diagram panel coordinate system.
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
		
		//drawing constraints
		g2.setColor(Color.BLACK);
		for (int i=0; i< startIncludesDots.size(); ++i){
		  g2.setStroke(new BasicStroke());
		  drawConstraint(g2, getVisibleStartAnchorCenterOnDiagram(startIncludesDots.get(i)),
			getVisibleStartAnchorCenterOnDiagram(((ConstraintPanel)startIncludesDots.get(i)).getOtherEnd()), 
			((ConstraintPanel)startIncludesDots.get(i)).getControlPoint().getLocation(), ItemsType.START_INCLUDES_DOT);
		}
		for (int i=0; i< startExcludesDots.size(); ++i){
		  g2.setStroke(new BasicStroke());
		  drawConstraint(g2, getVisibleStartAnchorCenterOnDiagram(startExcludesDots.get(i)),
			getVisibleStartAnchorCenterOnDiagram(((ConstraintPanel)startExcludesDots.get(i)).getOtherEnd()), 
			((ConstraintPanel)startExcludesDots.get(i)).getControlPoint().getLocation(), ItemsType.START_EXCLUDES_DOT);
		}
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
		Arc2D groupArc=null;
		
		Graphics2D tempGraphics = (Graphics2D)g2.create();

		  
		for (int i=0; i< list.size(); ++i){
		  startPanel=list.get(i);

		  groupArc=getGroupArc(startPanel, startPanel.getMembers());
		  
		  for (JComponent member : startPanel.getMembers()) drawConnectionLine(g2, startPanel, member);				
		  
		  //drawing the group arc
		  if(!filled){
			  groupArc.setArcType(Arc2D.Double.OPEN);
			  tempGraphics.draw(groupArc);
		  }
		  else{
			  groupArc.setArcType(Arc2D.Double.PIE);
			  tempGraphics.fill(groupArc);
		  }
		  
		}
	}
	
	/**
	 * Draws a constraint line.
	 * 
	 * @param g2d - the Graphics2D object used to draw
	 * @param start - start point of constraint
	 * @param end - end point of constraint
	 * @param control - control point of constraint
	 */
	private void drawConstraint(Graphics2D g2d, Point2D start, Point2D end, Point control, ItemsType type) {
//		Line2D intersectionSide=null;
	    int radius=0;
	    int intersectAngle=0;
	    int startAngle=0;
	    int endAngle=0;
	    Point2D trianglePoint1=null;
	    Point2D trianglePoint2=null;
	    int x1Points[]=null;
	    int y1Points[]=null;
	    Point2D intersectionPoint=null;
	    GeneralPath polygon=null;
//	    Line2D.Double endLine=null;
//	    Rectangle camera=null;
	    
	    //creating the QuadCurve2D.Float
	    QuadCurve2D quadcurve = new QuadCurve2D.Float();
	    
	    //setting coordinates
	    quadcurve.setCurve(start.getX(), start.getY(), control.getX(), control.getY(), end.getX(), end.getY());

//	    camera=new Rectangle((int)end.getX()-20, (int)end.getY()-20, 40, 40);
//	    endLine=new Line2D.Double(control, end);
	    
	    //getting intersection point between camera and endLine

	    int lineLength=
			(int)Math.sqrt((end.getX()-control.x)*(end.getX()-control.x)+(end.getY()-control.y)*(end.getY()-control.y));		
		double Xi=end.getX()+(30*(control.x-end.getX())/lineLength);
		double Yi=end.getY()+(30*(control.y-end.getY())/lineLength);
		intersectionPoint=new Point2D.Double(Xi, Yi);
				
//		intersectionPoint = getTriangleTipPoint(control, camera, endLine);	    
//	    if(intersectionPoint==null){
//	      System.out.println("drawConstraint(): intersectionPoint is null!");
//	      intersectionPoint=new Point2D.Double(
//					end.getX()-20>0 ? end.getX()-20 : end.getX()+20, 
//		  			end.getY()-20>0 ? end.getY()-20 : end.getY()+20);
//	    }
	    
//	    System.out.println("intersectionPoint: "+intersectionPoint);
	    
//	    Point2D[] intersectPoints=getIntersectionPoint(endLine, camera);
//	    for(Point2D p : intersectPoints) System.out.println("Point: "+p);
//	    for(Point2D p : intersectPoints) if(p!=null) intersectionPoint=p;
	    
	    radius=(int)Point2D.distance( intersectionPoint.getX(), intersectionPoint.getY(), end.getX(), end.getY());

	    intersectAngle =getDegreeAngle(end.getX(), end.getY(), intersectionPoint.getX(), intersectionPoint.getY(), radius);

	    startAngle=intersectAngle-15;
	    endAngle=intersectAngle+15;
	    
//	    System.out.println("startAngle="+startAngle+"\tendAngle="+endAngle);
	    
//	    trianglePoint1=getPointFromAngle(end, startAngle, radius);
//	    trianglePoint2=getPointFromAngle(end, endAngle, radius);
	    trianglePoint1=getPointFromAngle(end, -startAngle, radius);
	    trianglePoint2=getPointFromAngle(end, -endAngle, radius);
	    
	    // draw GeneralPath (polygon)
	    x1Points = new int[4];
	    x1Points[0]=(int)trianglePoint1.getX();
	    x1Points[1]=(int)trianglePoint2.getX();
	    x1Points[2]=(int)end.getX();
	    x1Points[3]=(int)trianglePoint1.getX();
	    
	    y1Points = new int[4];	    
	    y1Points[0]=(int)trianglePoint1.getY();
	    y1Points[1]=(int)trianglePoint2.getY();
	    y1Points[2]=(int)end.getY();
	    y1Points[3]=(int)trianglePoint1.getY();
	    
	    polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, x1Points.length);
	    polygon.moveTo(x1Points[0], y1Points[0]);

	    for (int index = 1; index < x1Points.length; index++) polygon.lineTo(x1Points[index], y1Points[index]);
	    
	    
//	    g2d.draw(polygon);
//	    g2d.draw(endLine);
//	    if(intersectionSide!=null) g2d.draw(intersectionSide);
	    
	    g2d.setStroke( new BasicStroke(
			 	1.5f/*Line width*/,
				BasicStroke.CAP_BUTT/*End-cap style*/,
				BasicStroke.JOIN_BEVEL/*Vertex join style*/, 
				1.0f/*miter trim limit(min=1)*/,
				new float[]{10.0f, 10.0f}/*dash structure*/, 
				0f/*dash phase*/) ); 

	    g2d.draw(quadcurve);
	    
	    if(type==ItemsType.START_INCLUDES_DOT){
	      g2d.setColor(Color.BLUE);
	      g2d.fill(polygon);
		  g2d.setColor(Color.BLACK);
		  return;
	    }
	    else{
		  g2d.setColor(Color.RED);
		  g2d.fill(polygon);	    	
	    }

//	    g2d.draw(camera);
//	    g2d.fillArc((int)intersectionPoint.getX()-5, (int)intersectionPoint.getY()-5, 10, 10, 0, 360);

	    
//	    System.out.println("Intersection Point="+intersectionPoint
//	    				  +"\ncamera="+camera);		
	    
//	    g2d.setColor(Color.RED);
//	    g2d.fillArc((int)start.getX()-5, (int)start.getY()-5, 10, 10, 0, 360);
//	    g2d.fillArc((int)end.getX()-5, (int)end.getY()-5, 10, 10, 0, 360);
//	    g2d.fillArc((int)control.x-5, (int)control.y-5, 10, 10, 0, 360);
	    
	    //this is an excludes constraint, the starting triangle will be drawn also
//	    camera=new Rectangle((int)start.getX()-20, (int)start.getY()-20, 40, 40);
//	    endLine=new Line2D.Double(control, start);

	    
	    lineLength=
			(int)Math.sqrt((start.getX()-control.x)*(start.getX()-control.x)+(start.getY()-control.y)*(start.getY()-control.y));		
		Xi=start.getX()+(30*(control.x-start.getX())/lineLength);
		Yi=start.getY()+(30*(control.y-start.getY())/lineLength);
		intersectionPoint=new Point2D.Double(Xi, Yi);

//	    intersectionPoint = getTriangleTipPoint(control, camera, endLine);	    
//	    if(intersectionPoint==null){
//	      System.out.println("drawConstraint2(): intersectionPoint is null!");
//	      intersectionPoint=new Point2D.Double(
//	    		  start.getX()-20>0 ? end.getX()-20 : start.getX()+20, 
//	    		  start.getY()-20>0 ? end.getY()-20 : start.getY()+20);
//	    }
	    
	    radius=(int)Point2D.distance( intersectionPoint.getX(), intersectionPoint.getY(), start.getX(), start.getY());

	    intersectAngle =getDegreeAngle(start.getX(), start.getY(), intersectionPoint.getX(), intersectionPoint.getY(), radius);

	    startAngle=intersectAngle-15;
	    endAngle=intersectAngle+15;
	    
	    trianglePoint1=getPointFromAngle(start, -startAngle, radius);
	    trianglePoint2=getPointFromAngle(start, -endAngle, radius);
	    
	    // draw GeneralPath (polygon)
	    x1Points = new int[4];
	    x1Points[0]=(int)trianglePoint1.getX();
	    x1Points[1]=(int)trianglePoint2.getX();
	    x1Points[2]=(int)start.getX();
	    x1Points[3]=(int)trianglePoint1.getX();
	    
	    y1Points = new int[4];	    
	    y1Points[0]=(int)trianglePoint1.getY();
	    y1Points[1]=(int)trianglePoint2.getY();
	    y1Points[2]=(int)start.getY();
	    y1Points[3]=(int)trianglePoint1.getY();
	    
	    polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, x1Points.length);
	    polygon.moveTo(x1Points[0], y1Points[0]);

	    for (int index = 1; index < x1Points.length; index++) polygon.lineTo(x1Points[index], y1Points[index]);
	    
	    g2d.fill(polygon);	

	    g2d.setColor(Color.BLACK);
	}

//	/**
//	 * Finds the intersection point between the Rectangle camera and the Line2d.Double endLine,
//	 *  starting at the point control.
//	 * 
//	 * @param control - the starting point of endLine
//	 * @param camera - the Rectangle used to calculate the intersection point
//	 * @param endLine - the Line2d.Double used to calculate the intersection point
//	 * @return
//	 */
//	private Point2D getTriangleTipPoint(Point control, Rectangle camera, Line2D.Double endLine) {
//		Line2D intersectionSide;
//		Point2D intersectionPoint=null;
//	    if(control.x>=camera.x && control.x<=camera.x+camera.width){
//	      if(control.y<=camera.y){//control point is directly over the camera
//	    	intersectionSide=
//	    	  new Line2D.Double(camera.x, camera.y, camera.x+camera.width, camera.y);
//	    	intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);
//	      }
//	      else if(control.y>=camera.y){//control point is directly below the camera
//	      	intersectionSide=
//	      	  new Line2D.Double(camera.x, camera.y+camera.height, camera.x+camera.width, camera.y+camera.height);
//	      	intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);    	  
//	      }
//	    }
//	    else if(control.y>=camera.y && control.y<=camera.y+camera.height){
//	      if(control.x<=camera.x){//control point is directly at left of the camera
//	        intersectionSide=
//	        	new Line2D.Double(camera.x, camera.y, camera.x, camera.y+camera.height);
//	        intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);
//	      }
//	      else if(control.x>=camera.x){//control point is directly at right of the camera
//	        intersectionSide=
//	        	new Line2D.Double(camera.x+camera.width, camera.y, camera.x+camera.width, camera.y+camera.height);
//	        intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);    	  
//	      }    	
//	    }
//	    else if(control.x<camera.x){
//	      if(control.y<camera.y){//control point is in a top-left position respect to the camera
//	        intersectionSide=//trying the top side
//	        	new Line2D.Double(camera.x, camera.y, camera.x+camera.width, camera.y);
//	        intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);   
//	        if(intersectionPoint==null){//trying the left side
//	          intersectionSide=
//	        	new Line2D.Double(camera.x, camera.y, camera.x, camera.y+camera.height);
//	          intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);           	
//	        }    	  
//	      }
//	      else if(control.y>camera.y+camera.height){//control point is in a bottom-left position respect to the camera
//	    	intersectionSide=//trying the bottom side
//	    	    new Line2D.Double(camera.x, camera.y+camera.height, camera.x+camera.width, camera.y+camera.height);
//	    	intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);   
//	    	if(intersectionPoint==null){//trying the left side
//	    	  intersectionSide=
//	    		new Line2D.Double(camera.x, camera.y, camera.x, camera.y+camera.height);
//	    	  intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);           	
//	    	}    	      	  
//	      }
//	    }
//	    else if(control.x>camera.x){
//	      if(control.y<camera.y){//control point is in a top-right position respect to the camera
//	      	intersectionSide=//trying the top side
//	            new Line2D.Double(camera.x, camera.y, camera.x+camera.width, camera.y);
//	      	intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);   
//	      	if(intersectionPoint==null){//trying the right side
//	          intersectionSide=
//	        	new Line2D.Double(camera.x+camera.width, camera.y, camera.x+camera.width, camera.y+camera.height);
//	          intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);           	
//	      	}    	      	        	  
//	      }
//	      else if(control.y>camera.y+camera.height){//control point is in a bottom-right position respect to the camera
//	      	intersectionSide=//trying the bottom side
//	      		new Line2D.Double(camera.x, camera.y+camera.height, camera.x+camera.width, camera.y+camera.height);
//	      	intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);   
//	      	if(intersectionPoint==null){//trying the right side
//	      	  intersectionSide=
//	            new Line2D.Double(camera.x+camera.width, camera.y, camera.x+camera.width, camera.y+camera.height);
//	      	  intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);
//	      	}    	      	      	  
//	      }
//	    }
//	    else{//control is inside of camera, the intersection point is arbitrary
//	   	  intersectionPoint=new Point2D.Double(camera.x, camera.y+camera.height/2);    	
//	    }
//		return intersectionPoint;
//	}	
	
	/**
	 * Returns the point at startAngle degrees, given a circle centered in center with radius radius.
	 * 
	 * @param center - Point2D object representing the circle center
	 * @param startAngle - angle of the requested point, in degrees
	 * @param radius - centre redius
	 * @return - a new Point2D object representing the calculated point
	 */
	private Point2D getPointFromAngle(Point2D center, int startAngle, int radius) {
	  double  x = Math.cos(startAngle * Math.PI / 180) * radius + center.getX();
	  double  y = Math.sin(startAngle * Math.PI / 180) * radius + center.getY();
		  
	  return new Point2D.Double(x, y);
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
		Point2D startCenter=getVisibleStartAnchorCenterOnDiagram(groupPanel);
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
		  childCenter=getVisibleStartAnchorCenterOnDiagram(groupChild);

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
		groupArc = new Arc2D.Double(startCenter.getX()-radius*arcSize, startCenter.getY()-radius*arcSize,
				radius*2*arcSize, radius*2*arcSize, 0, 360, Arc2D.Double.OPEN);

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
	private static int getDegreeAngle(double centreX, double centreY, double pointX, double pointY, double radius) {
		double cosin=(pointX-centreX)/radius;
		double sin=(pointY-centreY)/radius;
		double acos = Math.acos(cosin);
		double asin = Math.asin(sin);

		asin*=-1;
		//using acos and asin calculated values to get actual angle in degree
		if(asin>0) return (int)Math.toDegrees(acos);
		else{
		  if(acos<Math.PI/2) return (int)Math.toDegrees(2*Math.PI+asin);
		  else return (int)Math.toDegrees(Math.PI-asin);
		}	
	}

//	/**
//	 * Draws the group arc from leftMost to rightMost anchors
//	 * 
//	 * @param g2 - the Graphics2D object used for drawing
//	 * @param startComp - start anchor of the group
//	 * @param leftMost - left-most anchor of the group
//	 * @param rightMost - right-most anchor of the group
//	 * @param filled - if true, the group arc is drawm as a filled shape, otherwise only the boundary line is drawn  
//	 */
//	private void drawGroupArc(Graphics2D g2, JComponent startComp, JComponent leftMost, JComponent rightMost, boolean filled) {
//	  double lineFraction=2.5;
//	  double leftX=0, rightX=0, leftY=0, rightY=0;
//	  int leftHeight=0, rightHeight=0, leftWidth=0, rightWidth=0, leftLength=0, rightLength=0;
//	  int rectangleWidth=0, rectangleHeight=0;
//	  Line2D intersectingLine=null;
//	  Line2D leftLine=null, rightLine=null;
//	  Point2D startCenter=null, leftCenter=null, rightCenter=null;
//	  Point2D leftLineIntersectPoint=null, rightLineIntersectPoint=null;
//	  List<Point2D> intersectionPoints=null;
//	  Arc2D groupArc = null;
//	  
//	  /* ***DEBUG*** */
//	  if (debug3) System.out.println(""
//			  +"\nstart: "+startComp
//			  +"\nleftMost: "+leftMost
//			  +"\nrightMost: "+rightMost
//		);
//	  /* ***DEBUG*** */
//	  
//	  if(!startComp.isVisible()) return;
//
//	  Graphics2D tempGraphics = (Graphics2D)g2.create();
//	  
//	  //getting actual visible center points of components
//	  startCenter=getVisibleStartAnchorCenter(startComp);
//	  leftCenter=getVisibleStartAnchorCenter(leftMost);
//	  rightCenter=getVisibleStartAnchorCenter(rightMost);
//	  
//	  //getting the lenghts of the two lines
//	  leftHeight=(int)(leftCenter.getY()-startCenter.getY());
//	  leftWidth=(int)(leftCenter.getX()-startCenter.getX());
//	  leftLength=(int)Math.sqrt(leftWidth*leftWidth+leftHeight*leftHeight);
//	  rightHeight=(int)(rightCenter.getY()-startCenter.getY());
//	  rightWidth=(int)(rightCenter.getX()-startCenter.getX());
//	  rightLength=(int)Math.sqrt(rightWidth*rightWidth+rightHeight*rightHeight);
//
//	  //getting the coordinates of the two points at 1/lineFraction line length for the two lines
//	  leftX=startCenter.getX()+leftWidth/lineFraction;
//	  leftY=startCenter.getY()+leftHeight/lineFraction;
//	  rightX=startCenter.getX()+rightWidth/lineFraction;
//	  rightY=startCenter.getY()+rightHeight/lineFraction;
//
//	  //creating the lines to calculate the two actual points of the arc
//	  leftLine= new Line2D.Double(startCenter, leftCenter);
//	  rightLine= new Line2D.Double(startCenter, rightCenter);
//	  if(leftHeight<0) leftHeight*=-1;
//	  if(rightHeight<0) rightHeight*=-1;
//	  //intersecting line depends on the shortest line
//	  if(leftLength<rightLength) intersectingLine= new Line2D.Double(-3000000, leftY, +3000000, leftY);
////	  if(leftHeight<rightHeight) intersectingLine= new Line2D.Double(-3000000, leftY, +3000000, leftY);
//	  else intersectingLine= new Line2D.Double(-3000000, rightY, +3000000, rightY);
//	  
//	  //calculating groupArc radius
//	  double groupArcRadius = (leftLength<rightLength)? leftLength/lineFraction:rightLength/lineFraction;
//
//	  //calculating actual intersection point of the arc with leftLine
//	  intersectionPoints=getCircleLineIntersectionPoints(startCenter, leftCenter, startCenter, groupArcRadius);
//	  if(leftLine.ptSegDist(intersectionPoints.get(0))==0) leftLineIntersectPoint=intersectionPoints.get(0);
//	  else leftLineIntersectPoint=intersectionPoints.get(1);
//
//	  //calculating actual intersection point of the arc with rightLine
//	  intersectionPoints=getCircleLineIntersectionPoints(startCenter, rightCenter, startCenter, groupArcRadius);
//	  if(rightLine.ptSegDist(intersectionPoints.get(0))==0) rightLineIntersectPoint=intersectionPoints.get(0);
//	  else rightLineIntersectPoint=intersectionPoints.get(1);
//
//	  //calculating the two actual points of the arc
////	  leftIntersectionPoint=getIntersectionPoint(leftLine, intersectingLine);
////	  rightIntersectionPoint=getIntersectionPoint(rightLine, intersectingLine);
//
//	  
//	  /* ***DEBUG*** */
//	  if (debug3) System.out.println(""
//			  +"\nleftIntersectionPoint: "+leftLineIntersectPoint
//			  +"\nrightIntersectionPoint: "+rightLineIntersectPoint
//		);
//	  /* ***DEBUG*** */
//	  
//	  if(leftLineIntersectPoint==null || rightLineIntersectPoint==null) return;
//	  //calculating width and height to draw the arc
//	  rectangleWidth=(int)(rightLineIntersectPoint.getX()-leftLineIntersectPoint.getX());
//	  rectangleHeight=(int)(leftLineIntersectPoint.getY()-startCenter.getY());
////	  rectangleHeight=(leftHeight>rightHeight)? leftHeight:rightHeight;
//	  if (rectangleHeight<0)rectangleHeight*=-1;
////	  Rectangle2D rect2D= new Rectangle2D.Double(			  
////			  (startCenter.getX()-groupArcRadius),
////			  (startCenter.getY()-groupArcRadius),
////			  groupArcRadius*2, groupArcRadius*2);
//	  if(!filled) groupArc = new Arc2D.Double(			  
//			  (startCenter.getX()-groupArcRadius),
//			  (startCenter.getY()-groupArcRadius),
//			  groupArcRadius*2, groupArcRadius*2, 0, 360, Arc2D.Double.OPEN);
//	  else groupArc = new Arc2D.Double(			  
//			  (startCenter.getX()-groupArcRadius),
//			  (startCenter.getY()-groupArcRadius),
//			  groupArcRadius*2, groupArcRadius*2, 0, 360, Arc2D.Double.PIE);
////	  Arc2D groupArc = new Arc2D.Double(			  
////			  (startCenter.getX()-rectangleHeight),
////			  (startCenter.getY()-rectangleHeight),
////			  rectangleHeight*2, rectangleHeight*2, 0, 360, Arc2D.Double.OPEN);
////	  if(rightLineIntersectPoint.getX()<leftLineIntersectPoint.getX())
////		  groupArc.setAngles(
////				  rightLineIntersectPoint.getX(), rightLineIntersectPoint.getY(), 
////				  leftLineIntersectPoint.getX(), leftLineIntersectPoint.getY());
////	  else groupArc.setAngles(
////				  leftLineIntersectPoint.getX(), leftLineIntersectPoint.getY(),
////				  rightLineIntersectPoint.getX(), rightLineIntersectPoint.getY());
//
//	  groupArc.setAngles(
//		  leftLineIntersectPoint.getX(), leftLineIntersectPoint.getY(),
//		  rightLineIntersectPoint.getX(), rightLineIntersectPoint.getY());
//
//	  if(groupArc.getAngleExtent()>180) groupArc.setAngles(
//		  rightLineIntersectPoint.getX(), rightLineIntersectPoint.getY(), 
//		  leftLineIntersectPoint.getX(), leftLineIntersectPoint.getY());
//	  
//	  if(!filled) tempGraphics.draw(groupArc);
//	  else  tempGraphics.fill(groupArc);
//
////	  tempGraphics.draw(rect2D);
////	  tempGraphics.fill(arco);
////	  tempGraphics.drawArc(
////			  (int)(startPoint.getLocationOnScreen().getX()-splitterPanel.getLocationOnScreen().getX()-rectangleHeight),
////			  (int)(startPoint.getLocationOnScreen().getY()-splitterPanel.getLocationOnScreen().getY()-rectangleHeight),
////			  rectangleHeight*2, rectangleHeight*2, 0, 360);
////	  tempGraphics.setClip(
////			  (int)(leftPoint.getX()-splitterPanel.getLocationOnScreen().getX())-1,
////			  (int)(leftPoint.getY()-splitterPanel.getLocationOnScreen().getY())-1,
////			  rectangleWidth+6, rectangleHeight);
////	  tempGraphics.drawArc(
////			  (int)(leftPoint.getX()-splitterPanel.getLocationOnScreen().getX()),
////			  (int)(leftPoint.getY()-splitterPanel.getLocationOnScreen().getY()-rectangleHeight),
////			  rectangleWidth, rectangleHeight*2, 0, 360);
//////	  tempGraphics.drawRect(25, 25, 240, 120);
////	  tempGraphics.setColor(Color.RED);
////	  tempGraphics.fillOval((int)leftPoint.getX()-2, (int)leftPoint.getY()-2, 7, 7);
////	  tempGraphics.fillOval((int)rightPoint.getX()-2, (int)rightPoint.getY()-2, 7, 7);
//	}

	private void drawConnectionLine(Graphics2D g2, JComponent startPanel, JComponent endPanel) {
		lineStart.setLocation(getVisibleStartAnchorCenterOnDiagram(startPanel));
		lineEnd.setLocation(getVisibleStartAnchorCenterOnDiagram(endPanel));
		g2.drawLine((int)lineStart.getX(), (int)lineStart.getY(), (int)lineEnd.getX(), (int)lineEnd.getY() );
	};
	
	/**
     * Returns a Point2D representing the visible center of a starting anchor image on the diagram panel coordinates system.
     * 
     * @param anchor - the JComponent representing a visible starting anchor
     * @return the visible center point of the anchor
     */
    private Point2D getVisibleStartAnchorCenterOnDiagram(JComponent anchor) {
    	double x=(anchor.getLocationOnScreen().getX()-diagramPanel.getLocationOnScreen().getX()+anchor.getWidth()/2);
    	double y=(anchor.getLocationOnScreen().getY()-diagramPanel.getLocationOnScreen().getY()+anchor.getHeight()/2+2);
    	
    	return new Point2D.Double(x, y);
    }
    
	/**
     * Returns a Point2D representing the visible center of a starting anchor image on the diagram panel coordinates system.
     * 
     * @param anchor - the JComponent representing a visible starting anchor
     * @return the visible center point of the anchor
     */
    public Point2D.Double getVisibleStartAnchorCenterOnView(JComponent anchor) {
//    	double x=(anchor.getLocationOnScreen().getX()-this.getLocationOnScreen().getX()+anchor.getWidth()/2);
//    	double y=(anchor.getLocationOnScreen().getY()-this.getLocationOnScreen().getY()+anchor.getHeight()/2+1);
    	
    	double x=((anchor.getLocationOnScreen().getX()+anchor.getWidth()/2
				-diagramPanel.getLocationOnScreen().getX())*scaleFactor
				+diagramPanel.getLocationOnScreen().getX()-this.getLocationOnScreen().getX());
    	double y=((anchor.getLocationOnScreen().getY()+anchor.getHeight()/2+1
				-diagramPanel.getLocationOnScreen().getY())*scaleFactor
				+diagramPanel.getLocationOnScreen().getY()-this.getLocationOnScreen().getY());
    	
    	return new Point2D.Double(x, y);
    }
	
	/**
	 * Returns a JLabel named name and containing the corresponding icon image, <br>
	 * the method call setOpaque(backgroundVisible) on the panel containig the icon.
	 * 
	 * @param name - the name of the new JLabel
	 * @param backgroundVisible - if true the panel will be opaque, otherwise it will be transparent.
	 * @return - the new JLabel with the icon, or null if a problem occurrs.
	 */
	private static JLabel getToolIcon(String name, boolean backgroundVisible) {
		JLabel iconPanel=null;
		ImageIcon toolImage = getIconImage(name);
		
		iconPanel = new JLabel(toolImage);
		
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
			System.out.println("\n***getIconImage()***\nName= "+name);
			System.out.println("toolIconPaths.get(name)= "+toolIconPaths.get(name));
			
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
	 * Adds a JComponent to the diagramPanel, in the topmost position.
	 * @param comp - the JComponent to be added
	 */
	private void addToDiagramOnTop(JComponent comp) {
	  diagramPanel.setLayer(comp, 0);
	  diagramPanel.add(comp);
	  diagramPanel.setComponentZOrder(comp, 0);
	}

	/**
	 * Adds a JComponent to the diagramPanel, in the topmost position.
	 * 
	 * @param container - the JLayeredPane on which comp must be added
	 * @param comp - the JComponent to be added
	 * @param layer - the layer at which comp should be added
	 */
	private void addToComponentOnLayer(JLayeredPane container, JComponent comp, int layer) {
		boolean done = false;
		
		/* ***DEBUG*** */
		if(debug){
			System.out.println("**container.getLocation(): "+container.getLocation()
							+"\n**comp.getLocation(): "+comp.getLocation());
		}
		/* ***DEBUG*** */

		//adjustign comp location
		Point loc = comp.getLocation();		
		if(loc.getY()<0) loc.y=-3;
		else if(loc.getY()>container.getHeight()-comp.getHeight()) loc.y=container.getHeight()-comp.getHeight()+1;
		if(loc.getX()<0) loc.x=0;
		else if(loc.getX()>container.getWidth()-comp.getWidth()) loc.x=container.getWidth()-comp.getWidth();
		comp.setLocation(loc);
		//adding comp to container
		container.setLayer(comp, layer);
		
		while(!done)try{//sometimes it throws an exception, a second try seems to resolve always...
		  container.add(comp);
		  done=true;
		}catch(RuntimeException e){ }
		
		container.setComponentZOrder(comp, layer);
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
	  frameRoot.repaint();
	}

	/**
	 * Drags the selection rectangle, updating the two defining points.
	 *
	 * @param e - the current MouseEvent
	 */
	public void dragSelectionRect(MouseEvent e) {
	  Point loc = new Point();
	  
	  loc.x=(int)(e.getLocationOnScreen().getX()-this.getLocationOnScreen().getX());
	  loc.y=(int)(e.getLocationOnScreen().getY()-this.getLocationOnScreen().getY());
	  
	  selectionRect.setFrameFromDiagonal(startSelectionRect, loc);  	  
	  
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
	  boolean leftCollision=false, upperCollision=false;
	  boolean rightCollision=false, bottomCollision=false;
	  boolean mustResizeX=false, mustResizeY=false;
	  boolean mustScrollX=false, mustScrollY=false;
	  boolean mustShiftX=false, mustShiftY=false;
	  JComponent nearestElementX=null, nearestElementY=null;
	  Point location=null;
	  Dimension diagramSize=null;
	  ConstraintPanel otherEnd = null;
	  int elementWidth=0, elementHeight=0;
	  int diagramWidth=0, diagramHeight=0;
	  
	  diagramWidth=diagramPanel.getWidth();
	  diagramHeight=diagramPanel.getHeight();
	  if(scaleFactor!=1.0){
		diagramWidth=(int)(diagramWidth*scaleFactor);
		diagramHeight=(int)(diagramHeight*scaleFactor);		  
	  }
	  
	  //calculating move
	  moveX = e.getX()-lastPositionX;
	  moveY = e.getY()-lastPositionY;

	  //checking if some elements in the group collide with borders
	  collidedElementX.clear();
	  collidedElementY.clear();
	  for(JComponent element : selectionGroupFocused){	
		  
		elementWidth=(int)(element.getWidth()*scaleFactor);
		elementHeight=(int)(element.getHeight()*scaleFactor);
		  
		newLocationX=element.getX()+moveX;
		newLocationY=element.getY()+moveY;
		if(newLocationX<0){
		  leftCollision=true; collidedElementX.add(element);
		}
//		if(newLocationX+element.getWidth()>diagramPanel.getWidth()){
		else if( newLocationX*scaleFactor+elementWidth>diagramWidth){
		  rightCollision=true; collidedElementX.add(element);
		}
		if(newLocationY<0){
		  upperCollision=true; collidedElementY.add(element);
		}
//		if(newLocationY+element.getHeight()>diagramPanel.getHeight()){
		else if( newLocationY*scaleFactor+elementHeight>diagramHeight){
		  bottomCollision=true; collidedElementY.add(element);
		}
		System.out.println("Component "+element.getName()+" collisions:\n"
			+(leftCollision? "left ":"")+(rightCollision? "right ":"")
			+(upperCollision? "upper ":"")+(bottomCollision? "bottom ":""));
	  }
		  
	  //if there is a collision, the element nearest to the border is used for group move
	  if(leftCollision){
		nearestElementX=collidedElementX.get(0);
		for(int k=1; k<collidedElementX.size(); ++k)
		  if(collidedElementX.get(k).getX()< nearestElementX.getX()) nearestElementX=collidedElementX.get(k);
			
		newLocationX=nearestElementX.getX()+moveX;
			
		//resizing diagram and moving out-of-selection components
		if(newLocationX<-10 && moveX<=lastMoveX){
		  mustResizeX=true; mustShiftX=true;
		}
		adjustedMoveX=-nearestElementX.getX();
		lastPositionX=lastPositionX+adjustedMoveX;
	  }
	  if(rightCollision){
		nearestElementX=collidedElementX.get(0);
		for(int k=1; k<collidedElementX.size(); ++k)
		  if(collidedElementX.get(k).getX()+collidedElementX.get(k).getWidth()>nearestElementX.getX()+nearestElementX.getWidth())
			nearestElementX=collidedElementX.get(k);				
		  
		elementWidth=(int)(nearestElementX.getWidth()*scaleFactor);
			
		newLocationX=nearestElementX.getX()+moveX;
			
		//resizing diagram and setting scrollbar to max
		if(newLocationX*scaleFactor+elementWidth>diagramWidth+(int)(10*scaleFactor) && moveX>=lastMoveX){ 
//		if(newLocationX+nearestElementX.getWidth()>diagramPanel.getWidth()+10 && moveX>=lastMoveX){
		  mustResizeX=true; mustScrollX=true;
		}
		adjustedMoveX=diagramPanel.getWidth()-(nearestElementX.getX()+nearestElementX.getWidth());
		lastPositionX=lastPositionX+adjustedMoveX;
	  }
	  if(upperCollision){
		nearestElementY=collidedElementY.get(0);
		for(int k=1; k<collidedElementY.size(); ++k)
		  if(collidedElementY.get(k).getY()< nearestElementY.getY()) nearestElementY=collidedElementY.get(k);
			
		newLocationY=nearestElementY.getY()+moveY;
			
		//resizing diagram and moving out-of-selection components
		if(newLocationY<-10 && moveY<=lastMoveY){
		  mustResizeY=true; mustShiftY=true;
		}
		adjustedMoveY=-nearestElementY.getY();
		lastPositionY=lastPositionY+adjustedMoveY;
	  }
	  if(bottomCollision){
		nearestElementY=collidedElementY.get(0);
		for(int k=1; k<collidedElementY.size(); ++k)
		  if( collidedElementY.get(k).getY()+collidedElementY.get(k).getHeight()>
		  	  nearestElementY.getY()+nearestElementY.getHeight()) nearestElementY=collidedElementY.get(k);		
		
		elementHeight=(int)(nearestElementY.getHeight()*scaleFactor);		
			
		newLocationY=nearestElementY.getY()+moveY;
			
		//resizing diagram and setting scrollbar to max
//		if(newLocationY+nearestElementY.getHeight()>diagramPanel.getHeight()+10 && moveY>=lastMoveY){
		if(newLocationY*scaleFactor+elementHeight>diagramHeight+(int)(10*scaleFactor) && moveY>=lastMoveY){ 
		  mustResizeY=true; mustScrollY=true;
		}
		adjustedMoveY=diagramPanel.getHeight()-(nearestElementY.getY()+nearestElementY.getHeight());
		lastPositionY=lastPositionY+adjustedMoveY;
	  }		  
	  
	  //resizing diagram if necessary
	  if(mustResizeX || mustResizeY){
		diagramSize= diagramPanel.getPreferredSize();
		if(mustResizeX) diagramSize.width+=20;
		if(mustResizeY) diagramSize.height+=20;
		diagramPanel.setPreferredSize(diagramSize);
		diagramPanel.revalidate();		  
	  }
	  //shifting out-of-selection components if necessary
	  if(mustShiftX || mustShiftY)
		shiftAllDraggablesButGroupBothDirections(mustResizeX? 20:0, mustResizeY? 20:0, selectionGroupFocused, false);
	  //setting scrollbars to max if necessary
	  if(mustScrollX)
		hori.setValue(hori.getValue()+(int)(20*scaleFactor));				
//		hori.setValue(diagramScroller.getHorizontalScrollBar().getMaximum());
	  if(mustScrollY)
 	    vert.setValue(vert.getValue()+(int)(20*scaleFactor));	
//		vert.setValue(diagramScroller.getVerticalScrollBar().getMaximum());
		
	  if(!leftCollision && !rightCollision) adjustedMoveX=moveX;
	  if(!upperCollision && !bottomCollision) adjustedMoveY=moveY;
	  	  
	  //moving selection components
	  if(adjustedMoveX!=0 || adjustedMoveY!=0) for(JComponent element : selectionGroupFocused){		
		location=element.getLocation();
		location.x+=adjustedMoveX;
		location.y+=adjustedMoveY;
		element.setLocation(location);		
		
		//moving constraint control points, if both ends are being dragged
		if(  element.getName().startsWith(startExcludesNamePrefix) || element.getName().startsWith(endExcludesNamePrefix)
		  || element.getName().startsWith(startIncludesNamePrefix) || element.getName().startsWith(endIncludesNamePrefix) ){
		  otherEnd=(ConstraintPanel)((ConstraintPanel)element).getOtherEnd();
		  if(!selectionGroupFocused.contains(otherEnd.getControlPoint()) &&
			  (selectionGroupFocused.contains(otherEnd) || selectionGroupFocused.contains(otherEnd.getParent())) ){
			//moving control point if it's not already been moved
			if(((ConstraintControlPointPanel)otherEnd.getControlPoint()).isAlreadyShifted())
			  ((ConstraintControlPointPanel)otherEnd.getControlPoint()).setAlreadyShifted(false);			
			else{
			  location=otherEnd.getControlPoint().getLocation();
			  
			  location.x+=adjustedMoveX;
			  location.y+=adjustedMoveY;

			  otherEnd.getControlPoint().setLocation(location);	
			  ((ConstraintControlPointPanel)otherEnd.getControlPoint()).setAlreadyShifted(true);		
			}
		  }
		}
		else for(Component comp : element.getComponents()){
		  if(  comp.getName().startsWith(startExcludesNamePrefix) || comp.getName().startsWith(endExcludesNamePrefix)
		    || comp.getName().startsWith(startIncludesNamePrefix) || comp.getName().startsWith(endIncludesNamePrefix) ){
			otherEnd=(ConstraintPanel)((ConstraintPanel)comp).getOtherEnd();			  
			if(!selectionGroupFocused.contains(otherEnd.getControlPoint()) &&
			    (selectionGroupFocused.contains(otherEnd) || selectionGroupFocused.contains(otherEnd.getParent())) ){
			  //moving control point if it's not alrady been moved
			  if(((ConstraintControlPointPanel)otherEnd.getControlPoint()).isAlreadyShifted())
				((ConstraintControlPointPanel)otherEnd.getControlPoint()).setAlreadyShifted(false);			
			  else{
				location=otherEnd.getControlPoint().getLocation();

				location.x+=adjustedMoveX;
				location.y+=adjustedMoveY;

				otherEnd.getControlPoint().setLocation(location);	
				((ConstraintControlPointPanel)otherEnd.getControlPoint()).setAlreadyShifted(true);		
			  }
			}
		  }
		}

	  }

	  if(!leftCollision && !rightCollision) lastPositionX=e.getX();
	  if(!upperCollision && !bottomCollision) lastPositionY=e.getY();

	  lastMoveX=moveX;
	  lastMoveY=moveY;
		
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
		  int newLocationX=0, newLocationY=0;
		  int elementWidth=0, elementHeight=0;
		  int diagramWidth=0, diagramHeight=0;
		  boolean normalUpdateX=true, normalUpdateY=true;

		  moveX = e.getX()-lastPositionX;
		  moveY = e.getY()-lastPositionY;
		  newLocationX=element.getX()+moveX;
		  newLocationY=element.getY()+moveY;
		  
		  elementWidth=(int)(element.getWidth()*scaleFactor);
		  elementHeight=(int)(element.getHeight()*scaleFactor);
		  diagramWidth=(int)(diagramPanel.getWidth()*scaleFactor);
		  diagramHeight=(int)(diagramPanel.getHeight()*scaleFactor);
		  
		  //checking horizontal borders
		  if( newLocationX<0 ){
			if(newLocationX<-10 && moveX<=lastMoveX){
			  Dimension diagramSize= diagramPanel.getPreferredSize();
			  diagramSize.width+=20;
			  diagramPanel.setPreferredSize(diagramSize);
			  diagramPanel.revalidate();
			  shiftAllDraggablesButOneHorizontal(20, element);
			}
			newLocationX=0;
			lastPositionX=lastPositionX-element.getX();
			normalUpdateX=false;
		  }
//		  else if( newLocationX+element.getWidth()>diagramPanel.getWidth() ){
//			if( newLocationX+element.getWidth()>diagramPanel.getWidth()+10 
		  else if( newLocationX*scaleFactor+elementWidth>diagramWidth){
			if( newLocationX*scaleFactor+elementWidth>diagramWidth+(int)(10*scaleFactor) 
				&& moveX>=lastMoveX){
			  Dimension diagramSize= diagramPanel.getPreferredSize();
			  diagramSize.width+=20;
			  diagramPanel.setPreferredSize(diagramSize);
			  diagramPanel.revalidate();
//			  diagramScroller.getHorizontalScrollBar().setValue(
//				diagramScroller.getHorizontalScrollBar().getMaximum());				
			  hori.setValue(hori.getValue()+(int)(20*scaleFactor));				
			}
			newLocationX=diagramPanel.getWidth()-element.getWidth();
//			lastPositionX=lastPositionX+(newLocationX-element.getX());
			lastPositionX=newLocationX;
			normalUpdateX=false;
		  }
		  
		  //checking vertical borders
		  if( newLocationY<0 ){
			if(newLocationY<-10 && moveY<=lastMoveY){
			  Dimension diagramSize= diagramPanel.getPreferredSize();
			  diagramSize.height+=20;
			  diagramPanel.setPreferredSize(diagramSize);
			  diagramPanel.revalidate();
			  shiftAllDraggablesButOneVertical(20, element);
			}
			newLocationY=0;
			lastPositionY=lastPositionY-element.getY();
			normalUpdateY=false;
		  }
//		  else if( newLocationY+element.getHeight()>=diagramPanel.getHeight() ){
//			if( newLocationY+element.getHeight()>diagramPanel.getHeight()+10 
		  else if( newLocationY*scaleFactor+elementHeight>diagramHeight){
			if( newLocationY*scaleFactor+elementHeight>diagramHeight+(int)(10*scaleFactor) 
				&& moveY>=lastMoveY){
			  Dimension diagramSize= diagramPanel.getPreferredSize();
			  diagramSize.height+=20;
			  diagramPanel.setPreferredSize(diagramSize);
			  diagramPanel.revalidate();
//			  vert.setValue(diagramScroller.getVerticalScrollBar().getMaximum());				
			  vert.setValue(vert.getValue()+(int)(20*scaleFactor));				
			}
			newLocationY=diagramPanel.getHeight()-element.getHeight();
//			lastPositionY=lastPositionY+(newLocationY-element.getY());
			lastPositionY=newLocationY;
			normalUpdateY=false;			  
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
		  if(normalUpdateY) lastPositionY=e.getY();
		  lastMoveX=moveX;
		  lastMoveY=moveY;
		  element.setLocation(newLocationX, newLocationY);
	}

	/**
	 * Shifts horizontally the position of all draggables in the diagram panel, 
	 * including all invisible constraint control panels.<br>
	 * If element is not null and is a draggable, it is not shifted.
	 * 
	 * @param enlargeX - the amount of horizontal shift
	 * @param element - the JComponent that must not be shifted
	 */
	private void shiftAllDraggablesButOneHorizontal(int enlargeX, JComponent element) {
	  Point location=null;
	  JComponent controlPanel = null;
	  OrderedListNode tmp= visibleOrderDraggables.getFirst();

	  while (tmp!=null){
		if( !((JComponent)tmp.getElement()).equals(element) 
		    && ((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)){
		  location=((JComponent)tmp.getElement()).getLocation();
		  location.x+=enlargeX;
		  ((JComponent)tmp.getElement()).setLocation(location);
		}
		tmp=tmp.getNext();
	  }		
	  for(JComponent constraint : startIncludesDots){
		controlPanel=((ConstraintPanel)constraint).getControlPoint();
		if(controlPanel.isVisible()) continue;
		location=controlPanel.getLocation();
		location.x+=enlargeX;
		controlPanel.setLocation(location);
	  }
	  for(JComponent constraint : startExcludesDots){
		controlPanel=((ConstraintPanel)constraint).getControlPoint();
		if(controlPanel.isVisible()) continue;
		location=controlPanel.getLocation();
		location.x+=enlargeX;
		controlPanel.setLocation(location);
	  }
	}

//	/**
//	 * Shifts horizontally the position of all draggables in the diagram panel.<br>
//	 * If group is not null, its elements are not shifted.
//	 * 
//	 * @param enlargeX - the amount of horizontal shift
//	 * @param group - the ArrayList<JComponent> of elements that must not be shifted. If null, all components will be shifted
//	 */
//	private void shiftAllDraggablesButGroupHorizontal(int enlargeX, ArrayList<JComponent> group) {
//	  OrderedListNode tmp= visibleOrderDraggables.getFirst();
//	  Point location=null;
//	  if(group==null) group = new ArrayList<JComponent>();
//	  while (tmp!=null){
//		if( !group.contains((JComponent)tmp.getElement()) 
//			&& ((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)){
//		  location=((JComponent)tmp.getElement()).getLocation();
//		  location.x+=enlargeX;
//		  ((JComponent)tmp.getElement()).setLocation(location);
//		}
//		tmp=tmp.getNext();
//	  }	
//	}

	/**
	 * Shifts vertically the position of all draggables in the diagram panel,
	 * including all invisible constraint control panels.<br>
	 * If element is not null and is a draggable, it is not shifted.
	 * 
	 * @param enlargeY - the amount of vertical shift
	 * @param element - the JComponent that must not be shifted
	 */
	private void shiftAllDraggablesButOneVertical(int enlargeY, JComponent element) {
	  Point location=null;
	  JComponent controlPanel = null;
	  OrderedListNode tmp= visibleOrderDraggables.getFirst();
	  while (tmp!=null){
		if( !((JComponent)tmp.getElement()).equals(element) 
			&& ((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)){
		  location=((JComponent)tmp.getElement()).getLocation();
		  location.y+=enlargeY;
		  ((JComponent)tmp.getElement()).setLocation(location);
		}
		tmp=tmp.getNext();
	  }		
	  for(JComponent constraint : startIncludesDots){
		controlPanel=((ConstraintPanel)constraint).getControlPoint();
		if(controlPanel.isVisible()) continue;
		location=controlPanel.getLocation();
		location.y+=enlargeY;
		controlPanel.setLocation(location);
	  }
	  for(JComponent constraint : startExcludesDots){
		controlPanel=((ConstraintPanel)constraint).getControlPoint();
		if(controlPanel.isVisible()) continue;
		location=controlPanel.getLocation();
		location.y+=enlargeY;
		controlPanel.setLocation(location);
	  }
	}

//	/**
//	 * Shifts vertically the position of all draggables in the diagram panel.<br>
//	 * If group is not null, its elements are not shifted.
//	 * 
//	 * @param enlargeY - the amount of vertical shift
//	 * @param group - the ArrayList<JComponent> of elements that must not be shifted. If null, all components will be shifted
//	 */
//	private void shiftAllDraggablesButGroupVertical(int enlargeY, ArrayList<JComponent> group) {
//	  OrderedListNode tmp= visibleOrderDraggables.getFirst();
//	  Point location=null;
//	  if(group==null) group = new ArrayList<JComponent>();
//	  while (tmp!=null){
//		if( !group.contains((JComponent)tmp.getElement()) 
//			&& ((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)){
//		  location=((JComponent)tmp.getElement()).getLocation();
//		  location.y+=enlargeY;
//		  ((JComponent)tmp.getElement()).setLocation(location);
//		}
//		tmp=tmp.getNext();
//	  }	
//	}

	/**
	 * Shifts in both direction the position of all draggables in the diagram panel.<br>
	 * If group is not null, its elements are not shifted.
	 * 
	 * @param enlargeY - the amount of vertical shift
	 * @param enlargeY - the amount of horizontal shift
	 * @param group - the ArrayList<JComponent> of elements that must not be shifted. If null, all components will be shifted
	 * @param onFit - if true this call is needed to refit the diagram, otherwise is needed by a drag operation
	 */
	private void shiftAllDraggablesButGroupBothDirections(int enlargeX, int enlargeY, ArrayList<JComponent> group, boolean onFit) {
	  JComponent controlPanel = null;
	  OrderedListNode tmp= visibleOrderDraggables.getFirst();
	  Point location=null;
	  if(enlargeX==0 && enlargeY==0) return;

	  if(group==null) group = new ArrayList<JComponent>();
	  
	  while (tmp!=null){
		if( !group.contains((JComponent)tmp.getElement()) 
			&& (!((JComponent)tmp.getElement()).getName().startsWith(constraintControlPointNamePrefix) || onFit)
			&& ((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)){
		  location=((JComponent)tmp.getElement()).getLocation();
		  location.x+=enlargeX;
		  location.y+=enlargeY;
		  ((JComponent)tmp.getElement()).setLocation(location);
		}
		tmp=tmp.getNext();
	  }		
	  for(JComponent constraint : startIncludesDots){
		controlPanel=((ConstraintPanel)constraint).getControlPoint();
//		System.out.println("*****\ncontrolPanel.getName(): "+controlPanel.getName()+"\nisVisibe(): "+controlPanel.isVisible());
		if(controlPanel.isVisible()) continue;
		if( ( group.contains(constraint) || group.contains(constraint.getParent()) ) &&
			( group.contains(((ConstraintPanel)constraint).getOtherEnd()) 
			  || group.contains(((ConstraintPanel)constraint).getOtherEnd().getParent()) ) ) continue;
		
//		if(group.contains(controlPanel)) continue;
		location=controlPanel.getLocation();
		location.x+=enlargeX;
		location.y+=enlargeY;
		controlPanel.setLocation(location);
	  }
	  for(JComponent constraint : startExcludesDots){
		controlPanel=((ConstraintPanel)constraint).getControlPoint();
//		System.out.println("*****\ncontrolPanel.getName(): "+controlPanel.getName()+"\nisVisibe(): "+controlPanel.isVisible());
		if(controlPanel.isVisible()) continue;
		if( ( group.contains(constraint) || group.contains(constraint.getParent()) ) &&
			( group.contains(((ConstraintPanel)constraint).getOtherEnd()) 
			  || group.contains(((ConstraintPanel)constraint).getOtherEnd().getParent()) ) ) continue;
//		if(group.contains(controlPanel)) continue;
		location=controlPanel.getLocation();
		location.x+=enlargeX;
		location.y+=enlargeY;
		controlPanel.setLocation(location);
	  }
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
	 * Drags an Includes tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	public void dragToolIncludes(MouseEvent e) {
		  dragTool(e);
	}
	
	/**
	 * Drags an Excludes tool.
	 * @param e - the MouseEvent passed by the mouseDragged() method of a listener.
	 *
	 *@see MouseMotionListener
	 */
	public void dragToolExcludes(MouseEvent e) {
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
	 * @param e - MouseEvent of the type Mouse Released
	 * @return - the anchor's underlying JComponent
	 */
	public Component dropAnchorOnDiagram(MouseEvent e) {
	  Ellipse2D ellipse = null;
	  Point2D ellipseCenter = null;
	  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
	  Point2D relativePosition2D = getVisibleStartAnchorCenterOnView(lastAnchorFocused);

	  /* ***DEBUG*** */
	  if(debug && openerTimer!=null) System.out.println("openerTimer.getRadius(): "+openerTimer.getRadius());
	  /* ***DEBUG*** */
	  
	  //first we check wether a starting anchor should be merged with a group
	  if(   lastAnchorFocused.getName().startsWith(startMandatoryNamePrefix)
		 || lastAnchorFocused.getName().startsWith(startOptionalNamePrefix) ){
		while(tmpNode!=null){	  
		  if(   ((Component)tmpNode.getElement()).getName().startsWith(altGroupNamePrefix)
			 || ((Component)tmpNode.getElement()).getName().startsWith(orGroupNamePrefix)){
		    ellipseCenter = getVisibleStartAnchorCenterOnView((GroupPanel)tmpNode.getElement());
		    ellipse = new Ellipse2D.Double(
		    	ellipseCenter.getX()-openerTimer.getRadius(),
				ellipseCenter.getY()-openerTimer.getRadius(),
				openerTimer.getRadius()*2, openerTimer.getRadius()*2);
		    
		    /* ***DEBUG*** */
		    if(debug) System.out.println("relativePosition2D: "+relativePosition2D+"\nellipse: "+ellipse.getBounds());		    	
		    /* ***DEBUG*** */
		    
		    if(ellipse.contains(relativePosition2D.getX(), relativePosition2D.getY())){
			  underlyingComponent=(JComponent)tmpNode.getElement();
			  return underlyingComponent;			  
		    }
		  }
		  tmpNode=tmpNode.getNext();			  
		}		  
	  }

	  //checking others elements
	  relativePosition2D = getVisibleStartAnchorCenterOnDiagram(lastAnchorFocused);
	  tmpNode=visibleOrderDraggables.getFirst();
	  while(tmpNode!=null){	  
//		if (((Component)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY()) ){	
		if (((Component)tmpNode.getElement()).getBounds().contains(relativePosition2D.getX(), relativePosition2D.getY()) ){	

		  //anchor will be dropped over a feature panel
		  if (((Component)tmpNode.getElement()).getName().startsWith(featureNamePrefix)){

			//checking if an ending anchor will be dropped over a feature that already owns an ending anchor
			if(lastAnchorFocused.getName().startsWith(endMandatoryNamePrefix)
			   || lastAnchorFocused.getName().startsWith(endOptionalNamePrefix)){
			  for( Component child : ((FeaturePanel)tmpNode.getElement()).getComponents())
				if (child.getName()!=null 
				&& ( child.getName().startsWith(endMandatoryNamePrefix)
					 || child.getName().startsWith(endOptionalNamePrefix) ) ) return null;				
			}
			
			underlyingComponent=(JComponent)tmpNode.getElement();			  
			return underlyingComponent;					  
		  }
		}
		tmpNode=tmpNode.getNext();
	  }		
	  return null;
	}
	
	/**
	 * Drops a group on the diagram panel, adding it to the underlying feature panel, if any is present.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 * @return - the group's underlying JComponent
	 */
/*
	public Component dropGroupOnDiagram(MouseEvent e) {
	  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
	  Point2D relativePosition2D = getVisibleStartAnchorCenterOnDiagram(lastAnchorFocused);
	  while(tmpNode!=null){	  
	    if ( ((JComponent)tmpNode.getElement()).getName().startsWith(featureNamePrefix) && 
	    	 ((JComponent)tmpNode.getElement()).getBounds().contains(relativePosition2D.getX(), relativePosition2D.getY()) ){			
	      underlyingComponent=(JComponent)tmpNode.getElement();
	      return underlyingComponent;
	    }
	    tmpNode=tmpNode.getNext();
	  }				
	  return null;
	}
*/

	/**
	 * Drops a group or constraint on the diagram panel, adding it to the underlying feature panel, if any is present.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 * @return the constraint's underlying JComponent
	 */
	public Component dropGroupOrConstraintOnDiagram(MouseEvent e) {
	  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
	  Point2D relativePosition2D = getVisibleStartAnchorCenterOnDiagram(lastAnchorFocused);
	  while(tmpNode!=null){	  
	    if ( ((JComponent)tmpNode.getElement()).getName().startsWith(featureNamePrefix) &&
	    	 ((JComponent)tmpNode.getElement()).getBounds().contains(relativePosition2D.getX(), relativePosition2D.getY()) ){			
	      underlyingComponent=(JComponent)tmpNode.getElement();
	      return underlyingComponent;
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
		
		if( ( lastAnchorFocused.getName().startsWith(startMandatoryNamePrefix)
			|| lastAnchorFocused.getName().startsWith(startOptionalNamePrefix) ) ){
			
		  stopGroupOpeningAnimation();
		  addNewCloserTimer(null, null, null);
		  if(globalTimer!=null && globalTimer.isRunning() && closerTimers.size()==0) globalTimer.stop();
		}		
		
		moveComponentToTop(underlyingComponent);
		diagramPanel.validate();
		
		//if it is an ending anchor, location is set to top-middle of underlying feature
		if( lastAnchorFocused.getName().startsWith(endMandatoryNamePrefix)
			|| lastAnchorFocused.getName().startsWith(endOptionalNamePrefix) ){
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
				
		addToComponentOnLayer((JLayeredPane)underlyingComponent, lastAnchorFocused, 0);

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
	  Rectangle scaledSelectionrect = null;
	  scaledSelectionrect = 
		new Rectangle((int)(selectionRect.x+this.getLocationOnScreen().getX()-diagramPanel.getLocationOnScreen().getX()), 
					  (int)(selectionRect.y+this.getLocationOnScreen().getY()-diagramPanel.getLocationOnScreen().getY()),
			  		  (int)selectionRect.width, (int)selectionRect.height);
//	  ConstraintPanel otherEnd = null;
	  
	  while(tmpNode!=null){
//		locOnScreen=((Component)tmpNode.getElement()).getLocationOnScreen();
//		if(selectionRect.contains(locOnScreen)){
				
//		locOnScreen=((Component)tmpNode.getElement()).getLocation();
		locOnScreen=new Point();
//		locOnScreen.x=(int)(((Component)tmpNode.getElement()).getLocationOnScreen().getX()-this.getLocationOnScreen().getX());
//		locOnScreen.y=(int)(((Component)tmpNode.getElement()).getLocationOnScreen().getY()-this.getLocationOnScreen().getY());
		locOnScreen.x=(int)(((Component)tmpNode.getElement()).getLocationOnScreen().getX()-diagramPanel.getLocationOnScreen().getX());
		locOnScreen.y=(int)(((Component)tmpNode.getElement()).getLocationOnScreen().getY()-diagramPanel.getLocationOnScreen().getY());
		locOnScreen.x*=scaleFactor; locOnScreen.y*=scaleFactor;
		
//		if(selectionRect.contains(locOnScreen)){
		if(scaledSelectionrect.contains(locOnScreen)){
			
					
		  /* ***DEBUG*** */
		  if(debug)System.out.println("Checking: "+((Component)tmpNode.getElement()).getName());
		  /* ***DEBUG*** */

		  //if it's not a feature panel, we check that it's not anchored to a feature panel
		  if (!((Component)tmpNode.getElement()).getName().startsWith(featureNamePrefix)){

//			comp = (JComponent) diagramPanel.getComponentAt(				
//					(int)(locOnScreen.getX()-diagramPanel.getLocationOnScreen().getX()), 
//					(int)(locOnScreen.getY()-diagramPanel.getLocationOnScreen().getY()) );
			  
			comp=((Component)tmpNode.getElement()).getParent();
			  
			System.out.println("Underlying comp: "+comp.getName());
			if(comp==null || comp.getName().startsWith(featureNamePrefix)){
			  tmpNode=tmpNode.getNext(); continue;
			}
		  }
		  
		  //if it's not anchored to a feature, or it's a feature, it gets added to group
		  selectionGroupFocused.add((JComponent) tmpNode.getElement());
		}
		tmpNode=tmpNode.getNext();						
	  }
	  System.out.println("Selected Group Elements:\n");
	  for(JComponent element : selectionGroupFocused){
		System.out.println(element.getName());
//		//adding constraint control points, if both ends are being dragged
//		if(  element.getName().startsWith(startExcludesNamePrefix) || element.getName().startsWith(endExcludesNamePrefix)
//		  || element.getName().startsWith(startIncludesNamePrefix) || element.getName().startsWith(endIncludesNamePrefix) ){
//		  otherEnd=(ConstraintPanel)((ConstraintPanel)element).getOtherEnd();
//		  if(selectionGroupFocused.contains(otherEnd) || selectionGroupFocused.contains(otherEnd.getParent())){
//			if(!selectionGroupFocused.contains(otherEnd.getControlPoint())) 
//			  selectionGroupFocused.add(otherEnd.getControlPoint());
//		  }
//		}
//		else for(Component comp2 : element.getComponents()){	 
//		  if(  comp2.getName().startsWith(startExcludesNamePrefix) || comp2.getName().startsWith(endExcludesNamePrefix)
//			|| comp2.getName().startsWith(startIncludesNamePrefix) || comp2.getName().startsWith(endIncludesNamePrefix) ){
//			otherEnd=(ConstraintPanel)((ConstraintPanel)comp2).getOtherEnd();			  
//			if(selectionGroupFocused.contains(otherEnd) || selectionGroupFocused.contains(otherEnd.getParent())){
//			  if(!selectionGroupFocused.contains(otherEnd.getControlPoint())) 
//			    selectionGroupFocused.add(otherEnd.getControlPoint());
//			}
//		  }
//		}
	  }
	  frameRoot.repaint();
	}

	/**
	 * Removes a starting connector anchor from the diagram panel and attach it to a group.
	 */
	private void addStartAnchorToGroup() {
		AnchorPanel otherEnd = (AnchorPanel)((AnchorPanel)lastAnchorFocused).getOtherEnd();
		BufferedImage anchorImage = null;
//		Point2D.Double lastAnchorFocusedLocation = 
//				  new Point2D.Double(lastAnchorFocused.getLocationOnScreen().getX(), lastAnchorFocused.getLocationOnScreen().getY());
		Point2D.Double lastAnchorFocusedLocation = getVisibleStartAnchorCenterOnView(lastAnchorFocused);
		
		
		//changing the connector icon to the one requiered for and ending group connector
		for( Component comp : otherEnd.getComponents())
		  if(comp.getName().compareTo(connectorImageNamePrefix)==0){
			otherEnd.remove(comp);
			break;
		  }
		ImageIcon connectorIcon = new ImageIcon(mandatoryConnectorEndDotIconURL);
		ConstraintControlPointPanel imageLabel = new ConstraintControlPointPanel(connectorIcon);
		imageLabel.setBounds(0,  +2, connectorIcon.getIconWidth(), connectorIcon.getIconHeight()+5);
		imageLabel.setName(connectorImageNamePrefix);		
		otherEnd.add(imageLabel);
		
		((GroupPanel)underlyingComponent).getMembers().add(otherEnd);
		otherEnd.setOtherEnd(underlyingComponent);
		diagramPanel.remove(lastAnchorFocused);
		diagramPanel.validate();
		visibleOrderDraggables.remove(lastAnchorFocused);
		startConnectorDots.remove(lastAnchorFocused);
		frameRoot.repaint();
		
		//adding groups dock closing animation timer
		try {
			anchorImage = ImageIO.read(this.getClass().getResourceAsStream(getToolIconPath("Start Link Dot")));
		} catch (IOException e) {
			System.out.println("Anchor Image for animation not found");
			e.printStackTrace();
		}

		//?
		lastAnchorFocusedLocation.setLocation(lastAnchorFocusedLocation.getX(), lastAnchorFocusedLocation.getY());
		//?

		addNewCloserTimer((GroupPanel)underlyingComponent, lastAnchorFocusedLocation, anchorImage);		

		return;
	}

	/**
	 * Adds a new feature to the diagram panel, incrementing featuresCount.
	 */
	private void addNewFeatureToDiagram() {
		addFeatureToDiagram(null);
	}
	
	/**
	 * Adds a new named feature to the diagram panel, incrementing featuresCount.
	 */
	private void addNamedFeatureToDiagram() {		
		addFeatureToDiagram(featureToAddName);
	}

	/**
	 * Adds a new feature to the diagram panel, incrementing featuresCount.<br>
	 * The feature will be named name, or with a default name if name is null.
	 * 
	 * @param name - the name of the new feature
	 */
	private void addFeatureToDiagram(String name) {
	  Color featureColor=null;
		int actualPositionX=0;
		int actualPositionY=0;
		
		actualPositionX=
		  (toolDragPosition.x+(int)frameRoot.getLocationOnScreen().getX()-(int)diagramPanel.getLocationOnScreen().getX());
		actualPositionY=
		  (toolDragPosition.y+(int)frameRoot.getLocationOnScreen().getY()-(int)diagramPanel.getLocationOnScreen().getY());
		
		if(scaleFactor!=1.0){
		  actualPositionX=(int)(actualPositionX/scaleFactor);
		  actualPositionY=(int)(actualPositionY/scaleFactor);  
		}

		//the new feature must be dropped on the diagram panel for it to be added
		if( diagramPanel.getX()>actualPositionX ||
			diagramPanel.getX()+diagramPanel.getWidth()<=actualPositionX ||
			diagramPanel.getY()>actualPositionY ||
			diagramPanel.getX()+diagramPanel.getHeight()<=actualPositionY ){	
			
//	  //the new feature must be dropped on the diagram panel for it to be added
//	  if( diagramPanel.getLocationOnScreen().getX()>toolDragPosition.x ||
//		  diagramPanel.getLocationOnScreen().getX()+diagramPanel.getWidth()<=toolDragPosition.x ||
//		  diagramPanel.getLocationOnScreen().getY()>toolDragPosition.y ||
//		  diagramPanel.getLocationOnScreen().getX()+diagramPanel.getHeight()<=toolDragPosition.y ){
			
		cancelToolDrag();

		/* ***DEBUG*** */
		if(debug4) System.out.println("Cannot drop a new feature on tools panel.");
		/* ***DEBUG*** */
		return;
	  }

	  if (name==null){
		name=featureNamePrefix+featuresCount;
		featureColor=Color.BLACK;
	  }
	  else featureColor = getNewColor(termsColor.get(name));		
		
	  FeaturePanel newFeature=getDraggableFeature(name, actualPositionX, actualPositionY, featureColor);

	  visibleOrderDraggables.addToTop(newFeature);
	  addToDiagramOnTop(newFeature);
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
	public void addConnectorToDiagram() {
		int actualPositionX=0;
		int actualPositionY=0;
		boolean startDotInsertedInPanel=false;
		AnchorPanel newConnectorStartDot=null;			
		AnchorPanel newConnectorEndDot=null;
		FeaturePanel featurePanel = null;
		JComponent underlyingPanel = null;

		actualPositionX=
		  (toolDragPosition.x+(int)frameRoot.getLocationOnScreen().getX()-(int)diagramPanel.getLocationOnScreen().getX());
		actualPositionY=
		  (toolDragPosition.y+(int)frameRoot.getLocationOnScreen().getY()-(int)diagramPanel.getLocationOnScreen().getY());
		
		if(scaleFactor!=1.0){
		  actualPositionX=(int)(actualPositionX/scaleFactor);
		  actualPositionY=(int)(actualPositionY/scaleFactor);  
		}
		
		//the new connector must be dropped on the diagram panel for it to be added				
		if( diagramPanel.getX()>actualPositionX ||
			diagramPanel.getX()+diagramPanel.getWidth()<=actualPositionX ||
			diagramPanel.getY()>actualPositionY ||
			diagramPanel.getX()+diagramPanel.getHeight()<=actualPositionY ){
					
			cancelToolDrag();

			/* ***DEBUG*** */
			if(debug) System.out.println("Cannot drop a new connector on tools panel.");
			/* ***DEBUG*** */
			return;
		}
		
		//retrieving the underlying feature panel, if any
		underlyingPanel = getUnderlyingComponent(actualPositionX, actualPositionY+4);

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

			newConnectorStartDot=(AnchorPanel)getDraggableConnectionDot(
			  (isActiveItem==ActiveItems.DRAGGING_TOOL_MANDATORY_LINK) ?
				ItemsType.START_MANDATORY_CONNECTOR : ItemsType.START_OPTIONAL_CONNECTOR,
			  actualPositionX-featurePanel.getX(), actualPositionY-featurePanel.getY()-5);			

			moveComponentToTop(featurePanel);

			addToComponentOnLayer(featurePanel, newConnectorStartDot, 0);

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
		
		if(!startDotInsertedInPanel){//if there are no underlying features, starting dot is anchored to diagram panel				
		  newConnectorStartDot=(AnchorPanel)getDraggableConnectionDot(
			(isActiveItem==ActiveItems.DRAGGING_TOOL_MANDATORY_LINK) ?
			  ItemsType.START_MANDATORY_CONNECTOR : ItemsType.START_OPTIONAL_CONNECTOR,
			actualPositionX, actualPositionY-5);		
		}
		
		ImageIcon lineLengthIcon = new ImageIcon(connectorLineLengthIconURL);
		ImageIcon startConnectorIcon = new ImageIcon(connectorStartDotIconURL);

		//ending dot is anchored to diagram panel	
		newConnectorEndDot=(AnchorPanel)getDraggableConnectionDot(
		  (isActiveItem==ActiveItems.DRAGGING_TOOL_MANDATORY_LINK) ?
			ItemsType.END_MANDATORY_CONNECTOR : ItemsType.END_OPTIONAL_CONNECTOR,					  
		  actualPositionX+lineLengthIcon.getIconWidth()+startConnectorIcon.getIconWidth(),
		  actualPositionY-5+lineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

		/* ***DEBUG*** */
		if(debug) System.out.println(
		   "\nLocation where the icon has been released: ("+toolDragPosition.x+", "+toolDragPosition.y+")."
		  +"\nActualPositionX: "+actualPositionX+"\nActualPositionY: "+actualPositionY);
		/* ***DEBUG*** */
		
		visibleOrderDraggables.addToTop(newConnectorStartDot);
		if(!startDotInsertedInPanel) addToDiagramOnTop(newConnectorStartDot);

		visibleOrderDraggables.addToTop(newConnectorEndDot);
		addToDiagramOnTop(newConnectorEndDot);

		/* ***DEBUG*** */
		if(debug) System.out.println("Actual location of Connector1: ("+newConnectorStartDot.getLocationOnScreen()
		  +"\nActual location of Connector2: ("+newConnectorEndDot.getLocationOnScreen()
		  +"\nframeRoot.getLocationOnScreen(): ("+frameRoot.getLocationOnScreen()
		  +"\ndiagramPanel Coords get: ("+(int)diagramPanel.getLocationOnScreen().getX()+"-"
		  		+(int)diagramPanel.getLocationOnScreen().getY()+")"		  
		  +"\ndiagramPanel.getLocationOnScreen(): ("+diagramPanel.getLocationOnScreen());
		/* ***DEBUG*** */

		//setting other ends of connector dots
		newConnectorStartDot.setOtherEnd(newConnectorEndDot);
		newConnectorEndDot.setOtherEnd(newConnectorStartDot);

		addConnectorsToDrawLists(newConnectorStartDot, newConnectorEndDot);
		cancelToolDrag();		
	}

	/**
	 * Adds a new connector to the diagram. If the starting connector dot is dropped over a feature panel, <br>
	 * it gets attached to it.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	public void addConstraintToDiagram() {
		int actualPositionX=0;
		int actualPositionY=0;
		boolean startDotInsertedInPanel=false;
		ConstraintPanel newConstraintStartDot=null;			
		ConstraintPanel newConstraintEndDot=null;
		JComponent newConstraintControlPointDot=null;
		FeaturePanel featurePanel = null;
		JComponent underlyingPanel = null;
		
		actualPositionX=
		  (toolDragPosition.x+(int)frameRoot.getLocationOnScreen().getX()-(int)diagramPanel.getLocationOnScreen().getX());
		actualPositionY=
		  (toolDragPosition.y+(int)frameRoot.getLocationOnScreen().getY()-(int)diagramPanel.getLocationOnScreen().getY());
		
		if(scaleFactor!=1.0){
		  actualPositionX=(int)(actualPositionX/scaleFactor);
		  actualPositionY=(int)(actualPositionY/scaleFactor);  
		}

		//the new connector must be dropped on the diagram panel for it to be added				
		if( diagramPanel.getX()>actualPositionX ||
			diagramPanel.getX()+diagramPanel.getWidth()<=actualPositionX ||
			diagramPanel.getY()>actualPositionY ||
			diagramPanel.getX()+diagramPanel.getHeight()<=actualPositionY ){
			
			cancelToolDrag();

			/* ***DEBUG*** */
			if(debug4) System.out.println("Cannot drop a new connector on tools panel.");
			/* ***DEBUG*** */
			return;
		}
			
		//retrieving the underlying feature panel, if any
		underlyingPanel = getUnderlyingComponent(actualPositionX, actualPositionY+4);

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

			newConstraintStartDot=(ConstraintPanel)getDraggableConnectionDot(
			  (isActiveItem==ActiveItems.DRAGGING_TOOL_INCLUDES) ?
				ItemsType.START_INCLUDES_DOT : ItemsType.START_EXCLUDES_DOT,
			  actualPositionX-(int)featurePanel.getX(), actualPositionY-(int)featurePanel.getY()-5);			

			moveComponentToTop(featurePanel);

			addToComponentOnLayer(featurePanel, newConstraintStartDot, 0);

			startDotInsertedInPanel=true;
			
			/* ***DEBUG*** */
			System.out.println("Placing start connector in ("
					+(toolDragPosition.x-(int)underlyingPanel.getLocationOnScreen().getX())
					+", "+(toolDragPosition.y-(int)underlyingPanel.getLocationOnScreen().getY())+")");			
			System.out.println("Start connector Position(feature relative): ("
					+newConstraintStartDot.getX()+", "+newConstraintStartDot.getY()+")");			
			System.out.println("Start connector Position(screen relative): ("
					+newConstraintStartDot.getLocationOnScreen().getX()
					+", "+newConstraintStartDot.getLocationOnScreen().getY()+")");			
			/* ***DEBUG*** */
		  
		  }
		}
		
		if(!startDotInsertedInPanel) 
		  newConstraintStartDot=(ConstraintPanel)getDraggableConnectionDot(
			(isActiveItem==ActiveItems.DRAGGING_TOOL_INCLUDES) ?
			  ItemsType.START_INCLUDES_DOT : ItemsType.START_EXCLUDES_DOT,
			actualPositionX, actualPositionY-5);			
		
		ImageIcon lineLengthIcon = new ImageIcon(connectorLineLengthIconURL);
		ImageIcon startConnectorIcon = new ImageIcon(connectorStartDotIconURL);

		newConstraintEndDot=(ConstraintPanel)getDraggableConnectionDot(
		    (isActiveItem==ActiveItems.DRAGGING_TOOL_INCLUDES) ?
		      ItemsType.END_INCLUDES_DOT : ItemsType.END_EXCLUDES_DOT,
		    actualPositionX+lineLengthIcon.getIconWidth()+startConnectorIcon.getIconWidth(),
		    actualPositionY-5+lineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

		/* ***DEBUG*** */
		if(debug) System.out.println(
		   "\nLocation where the Constraint will be placed: ("+toolDragPosition.x+", "+toolDragPosition.y+")."
		  +"\nActualPositionX: "+actualPositionX+"\nActualPositionY: "+actualPositionY);
		/* ***DEBUG*** */
		
		visibleOrderDraggables.addToTop(newConstraintStartDot);
		if(!startDotInsertedInPanel) addToDiagramOnTop(newConstraintStartDot);

		visibleOrderDraggables.addToTop(newConstraintEndDot);
		addToDiagramOnTop(newConstraintEndDot);

		newConstraintControlPointDot=(JLabel)getDraggableConnectionDot(
		  ItemsType.CONSTRAINT_CONTROL_POINT,
		  (actualPositionX*2+lineLengthIcon.getIconWidth()+startConnectorIcon.getIconWidth())/2,
		  (actualPositionY*2-10+lineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight())/2);

		/* ***DEBUG*** */
		if(debug) System.out.println("Actual location of startDot: ("+newConstraintStartDot.getLocationOnScreen()
		  +"\nActual location of endDot: ("+newConstraintEndDot.getLocationOnScreen()
		  +"\ndiagramPanel.getLocationOnScreen(): ("+diagramPanel.getLocationOnScreen());
		/* ***DEBUG*** */

		//setting other ends of constraint dots
		newConstraintStartDot.setOtherEnd(newConstraintEndDot);
		newConstraintStartDot.setControlPoint(newConstraintControlPointDot);
		newConstraintEndDot.setOtherEnd(newConstraintStartDot);
		newConstraintEndDot.setControlPoint(newConstraintControlPointDot);
		newConstraintControlPointDot.setVisible(false);

		++constraintsCount;
		
		addConstraintToDrawLists( newConstraintStartDot, 
		  (isActiveItem==ActiveItems.DRAGGING_TOOL_INCLUDES) ?
			ItemsType.START_INCLUDES_DOT : ItemsType.START_EXCLUDES_DOT);
		
		cancelToolDrag();		
	}

	/**
	 * Adds a new Or Group to the diagram. If the starting connector dot is dropped over a feature panel, <br>
	 * it gets attached to it.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	public void addOrGroupToDiagram() {
		GroupPanel newGroupStartDot = addGroupToDiagram(ItemsType.OR_GROUP_START_CONNECTOR);
		if (newGroupStartDot==null) return;
		addOrGroupToDrawLists(newGroupStartDot);
	}

	/**
	 * Adds a new Alternative Group to the diagram. If the starting connector dot is dropped over a feature panel, <br>
	 * it gets attached to it.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	public void addAltGroupToDiagram() {
		GroupPanel newGroupStartDot = addGroupToDiagram(ItemsType.ALT_GROUP_START_CONNECTOR);
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
	private GroupPanel addGroupToDiagram(ItemsType requestedType) {
		int actualPositionX;
		int actualPositionY;
		boolean startDotInsertedInPanel=false;
		GroupPanel newGroupStartDot=null;			
		AnchorPanel newGroupEndpoint1=null, newGroupEndpoint2=null;
		FeaturePanel featurePanel = null;
		JComponent underlyingPanel = null;
		
		actualPositionX=
		  (toolDragPosition.x+(int)frameRoot.getLocationOnScreen().getX()-(int)diagramPanel.getLocationOnScreen().getX());
		actualPositionY=
		  (toolDragPosition.y+(int)frameRoot.getLocationOnScreen().getY()-(int)diagramPanel.getLocationOnScreen().getY());
		
		if(scaleFactor!=1.0){
		  actualPositionX=(int)(actualPositionX/scaleFactor);
		  actualPositionY=(int)(actualPositionY/scaleFactor);  
		}

		//the new group must be dropped on the diagram panel for it to be added
		if( diagramPanel.getX()>actualPositionX ||
			diagramPanel.getX()+diagramPanel.getWidth()<=actualPositionX ||
			diagramPanel.getY()>actualPositionY ||
			diagramPanel.getX()+diagramPanel.getHeight()<=actualPositionY ){

			cancelToolDrag();

			/* ***DEBUG*** */
			if(debug4) System.out.println("Cannot drop a new group on tools panel.");
			/* ***DEBUG*** */
			
			return null;
		}
			
		ImageIcon groupLineLengthIcon = new ImageIcon(groupLineLengthIconURL);
		ImageIcon startConnectorIcon = new ImageIcon(connectorStartDotIconURL);
		ImageIcon groupIcon = getIconImage(TOOL_ALT_GROUP);

		//retrieving the underlying feature panel, if any
		underlyingPanel = getUnderlyingComponent(actualPositionX, actualPositionY+4);

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
			  actualPositionX-(int)featurePanel.getX()+groupIcon.getIconWidth()/2-startConnectorIcon.getIconWidth()/2,
			  actualPositionY-(int)featurePanel.getY()/*-5*/-1);			

			moveComponentToTop(featurePanel);

			addToComponentOnLayer(featurePanel, newGroupStartDot, 0);

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
			actualPositionX+groupIcon.getIconWidth()/2-startConnectorIcon.getIconWidth()/2,
			actualPositionY/*-5*/-1);			
		
		newGroupEndpoint1=(AnchorPanel)getDraggableConnectionDot(ItemsType.END_MANDATORY_CONNECTOR,
		  actualPositionX+groupIcon.getIconWidth()/2-startConnectorIcon.getIconWidth()/2+groupLineLengthIcon.getIconWidth(),
		  actualPositionY/*-5*/-1+groupLineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

		newGroupEndpoint2=(AnchorPanel)getDraggableConnectionDot(ItemsType.END_MANDATORY_CONNECTOR,
		  actualPositionX+groupIcon.getIconWidth()/2-startConnectorIcon.getIconWidth()/2-groupLineLengthIcon.getIconWidth(),
		  actualPositionY/*-5*/-1+groupLineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());
		/*-startConnectorIcon.getIconWidth()*/
		
		/* ***DEBUG*** */
		if(debug) System.out.println(
		  "\nLocation where the Connector will be placed: ("+toolDragPosition.x+", "+toolDragPosition.y+")."
		  +"\nActualPositionX: "+actualPositionX+"\nActualPositionY: "+actualPositionY);
		/* ***DEBUG*** */
		
		visibleOrderDraggables.addToTop(newGroupStartDot);
		if(!startDotInsertedInPanel) addToDiagramOnTop(newGroupStartDot);

		visibleOrderDraggables.addToTop(newGroupEndpoint1);
		addToDiagramOnTop(newGroupEndpoint1);

		visibleOrderDraggables.addToTop(newGroupEndpoint2);
		addToDiagramOnTop(newGroupEndpoint2);

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
	protected JComponent buildConnectionDot(ItemsType type, String name, int x, int y) {
		JComponent imagePanel=null;

		ImageIcon connectorIcon=null;
		JLabel imageLabel = null;

		switch(type){
		  case START_MANDATORY_CONNECTOR:
			imagePanel = new AnchorPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(startMandatoryNamePrefix+connectorsCount);
			  ++connectorsCount;
			}
			connectorIcon = new ImageIcon(connectorStartDotIconURL);
			break;
		  case END_MANDATORY_CONNECTOR:
			imagePanel = new AnchorPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(endMandatoryNamePrefix+connectorsCount);
			  ++connectorsCount;
			}
			connectorIcon = new ImageIcon(mandatoryConnectorEndDotIconURL);
			break;
		  case START_OPTIONAL_CONNECTOR:
			imagePanel = new AnchorPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(startOptionalNamePrefix+connectorsCount);
			  ++connectorsCount;
			}
			connectorIcon = new ImageIcon(connectorStartDotIconURL);
			break;			
		  case END_OPTIONAL_CONNECTOR:
			imagePanel = new AnchorPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(endOptionalNamePrefix+connectorsCount);
			  ++connectorsCount;
			}
			connectorIcon = new ImageIcon(optionalConnectorEndDotIconURL);
			break;		
		  case START_INCLUDES_DOT:
			imagePanel = new ConstraintPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(startIncludesNamePrefix+constraintsCount);
			}
			connectorIcon = new ImageIcon(constraintDotIconURL);
			break;
		  case END_INCLUDES_DOT:
			imagePanel = new ConstraintPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(endIncludesNamePrefix+constraintsCount);
			}
			connectorIcon = new ImageIcon(constraintDotIconURL);
			break;		
		  case START_EXCLUDES_DOT:
			imagePanel = new ConstraintPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(startExcludesNamePrefix+constraintsCount);
			}
			connectorIcon = new ImageIcon(constraintDotIconURL);
			break;
		  case END_EXCLUDES_DOT:
			imagePanel = new ConstraintPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(endExcludesNamePrefix+constraintsCount);
			}
			connectorIcon = new ImageIcon(constraintDotIconURL);
			break;
		  case CONSTRAINT_CONTROL_POINT:
			//returning the JLabel directly
			connectorIcon = new ImageIcon(constraintControlPointDotIconURL);
			imagePanel = new ConstraintControlPointPanel(connectorIcon);
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(constraintControlPointNamePrefix+constraintControlsCount);
			  ++constraintControlsCount;
			}
			imagePanel.setBounds(x,  y, connectorIcon.getIconWidth(), connectorIcon.getIconHeight()+5);
			
//			imageLabel.setBounds(0,  +2, connectorIcon.getIconWidth(), connectorIcon.getIconHeight()+5);
			imagePanel.setLayout(null);
//			imagePanel.add(imageLabel);
			imagePanel.setOpaque(false);
			imagePanel.setVisible(true);
			return imagePanel;
//			break;		
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
		imageLabel.setName(connectorImageNamePrefix);
		imageLabel.setBounds(0,  +2, connectorIcon.getIconWidth(), connectorIcon.getIconHeight()+5);
		imagePanel.setBounds(x,  y, connectorIcon.getIconWidth(), connectorIcon.getIconHeight()+5);
		imagePanel.setLayout(null);
		imagePanel.add(imageLabel);
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
	private FeaturePanel getDraggableFeature(String name, int x, int y, Color color) {
		return buildFeaturePanel(name, null, x, y, color);
	}

	/**
	 * Creates a JPanel containing a Feature
	 * 
	 * @param name - the name of the feature
	 * @param containerName - the name of the FeaturePanel object
	 * @param x - x coordinate of the feature in the diagram panel
	 * @param y - y coordinate of the feature in the diagram panel
	 * @param color - the background color of the FeaturePanel object 
	 * @return A new JPanel representing the feature
	 */
	private FeaturePanel buildFeaturePanel(String name, String containerName, int x, int y, Color color) {
		StyledDocument doc = null;
		SimpleAttributeSet attrs = null;
		int layer=-1;	
		
		CenteredTextPane textLabel=new CenteredTextPane();

		
    	textLabel.setEditorKit(new CenteredEditorKit());
        attrs=new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_CENTER);
        doc=(StyledDocument)textLabel.getDocument();
        doc.setParagraphAttributes(0,doc.getLength()-1,attrs,false);
        		
		textLabel.setBounds(featureBorderSize/2, featureBorderSize/2, 120, 60);
//		textLabel.setHorizontalAlignment(JTextField.CENTER);
		
//		textLabel.setFont(new Font("Serif", Font.PLAIN, 12));
		textLabel.setFont(new Font("Arial", Font.BOLD, 12));
		
//		textLabel.setOpaque(true);
		textLabel.setEditable(false);
//		textLabel.setFocusable(true);
		textLabel.setName(textAreaNamePrefix);	
		textLabel.setText(name);
		
		//creating text
		FeaturePanel container = new FeaturePanel(textLabel);
		if(containerName==null) container.setName(featureNamePrefix+featuresCount);
		else container.setName(containerName);
		container.setLayout(null);
		container.setBounds(x, y, 120+featureBorderSize, 60+featureBorderSize);
		container.setOpaque(true);
		container.setBackground(color);
		
		//adding the text
		layer=container.getComponentCount();

		addToComponentOnLayer(container, textLabel, layer);
		
		/* ***DEBUG*** */
		/*if(debug)*/ System.out.println("container.getBounds(): "+container.getBounds());
		/* ***DEBUG*** */

		return container;
	}
	
	/**
	 * Resizes the diagram panel to fit all components in it.
	 */
	protected void fitDiagram() {
		int diagramMinX=0;
		int diagramMinY=0;
		int diagramMaxX=0;
		int diagramMaxY=0;
		OrderedListNode tmp=null;
		ConstraintControlPointPanel controlPoint=null;

//		Dimension baseDim=Toolkit.getDefaultToolkit().getScreenSize();
//		Dimension baseDim=diagramScroller.getSize();
//		Dimension baseDim=diagramPanel.getPreferredSize();
		Dimension baseDim=diagramPanelBasePreferredSize;
		
//		System.out.println("********\nbaseDim.width: "+baseDim.width+"\nbaseDim.height: "+baseDim.height
//						  +"\nbaseDim.getWidth(): "+baseDim.getWidth()+"\nbaseDim.getHeight(): "+baseDim.getHeight());
		
		tmp=visibleOrderDraggables.getFirst();
/*
		while(tmp!=null){
		  if (((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)) break;
		  tmp=tmp.getNext();
		}
*/		
		
		if(tmp==null /*&& startIncludesDots.size()==0 && startExcludesDots.size()==0*/) return;//no draggables found
		diagramMinX=((JComponent)tmp.getElement()).getX();
		diagramMinY=((JComponent)tmp.getElement()).getY();
		diagramMaxX=((JComponent)tmp.getElement()).getX()+((JComponent)tmp.getElement()).getWidth();
		diagramMaxY=((JComponent)tmp.getElement()).getY()+((JComponent)tmp.getElement()).getHeight();

		while(tmp!=null){
		  if (((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)){
			if (((JComponent)tmp.getElement()).getX()<diagramMinX)
			  diagramMinX=((JComponent)tmp.getElement()).getX();
			if (((JComponent)tmp.getElement()).getY()<diagramMinY)
			  diagramMinY=((JComponent)tmp.getElement()).getY();
			if (((JComponent)tmp.getElement()).getX()+((JComponent)tmp.getElement()).getWidth()>diagramMaxX)
			  diagramMaxX=((JComponent)tmp.getElement()).getX()+((JComponent)tmp.getElement()).getWidth();
			if (((JComponent)tmp.getElement()).getY()+((JComponent)tmp.getElement()).getHeight()>diagramMaxY)
			  diagramMaxY=((JComponent)tmp.getElement()).getY()+((JComponent)tmp.getElement()).getHeight();			
		  }
		  tmp=tmp.getNext();
		}

		
		//checking hidden constraint control points
		for(JComponent constraint : startIncludesDots){
		  controlPoint=(ConstraintControlPointPanel)((ConstraintPanel)constraint).getControlPoint();
		  
		  if (controlPoint.getX()<diagramMinX)
			diagramMinX=controlPoint.getX();
		  if (controlPoint.getY()<diagramMinY)
			diagramMinY=controlPoint.getY();
		  if (controlPoint.getX()+controlPoint.getWidth()>diagramMaxX)
			diagramMaxX=controlPoint.getX()+controlPoint.getWidth();
		  if (controlPoint.getY()+controlPoint.getHeight()>diagramMaxY)
			diagramMaxY=controlPoint.getY()+controlPoint.getHeight();		
		}
		for(JComponent constraint : startExcludesDots){
		  controlPoint=(ConstraintControlPointPanel)((ConstraintPanel)constraint).getControlPoint();
		  
		  if (controlPoint.getX()<diagramMinX)
			diagramMinX=controlPoint.getX();
		  if (controlPoint.getY()<diagramMinY)
			diagramMinY=controlPoint.getY();
		  if (controlPoint.getX()+controlPoint.getWidth()>diagramMaxX)
			diagramMaxX=controlPoint.getX()+controlPoint.getWidth();
		  if (controlPoint.getY()+controlPoint.getHeight()>diagramMaxY)
			diagramMaxY=controlPoint.getY()+controlPoint.getHeight();	
		}
		
		
		//moving components and resizing diagram
		shiftAllDraggablesButGroupBothDirections(-diagramMinX, -diagramMinY, null, true);

		Dimension diagramSize = diagramPanel.getPreferredSize();
		diagramSize.width=(diagramMaxX-diagramMinX>baseDim.width)? diagramMaxX-diagramMinX : baseDim.width;
		diagramSize.height=(diagramMaxY-diagramMinY>baseDim.height)? diagramMaxY-diagramMinY : baseDim.height;
		
		diagramScroller.getHorizontalScrollBar().setMaximum(
				(int)(diagramScroller.getHorizontalScrollBar().getMaximum()/scaleFactor));
		diagramScroller.getVerticalScrollBar().setMaximum(
				(int)(diagramScroller.getVerticalScrollBar().getMaximum()/scaleFactor));
		
		diagramPanel.setPreferredSize(diagramSize);
		diagramPanel.setSize(diagramSize);

		hori.setValue(diagramScroller.getHorizontalScrollBar().getMinimum());				
		vert.setValue(diagramScroller.getVerticalScrollBar().getMinimum());		

		
		diagramScroller.getHorizontalScrollBar().setMaximum(
				(int)(diagramScroller.getHorizontalScrollBar().getMaximum()*scaleFactor));
		diagramScroller.getVerticalScrollBar().setMaximum(
				(int)(diagramScroller.getVerticalScrollBar().getMaximum()*scaleFactor));
		
		diagramScroller.getViewport().setBounds(0, 0, (int)diagramSize.getWidth(), (int)diagramSize.getHeight());

		diagramPanel.revalidate();
		frameRoot.repaint();
	}

	/**
	 * Activate the global timer, responsable for drawing groups animations.
	 * If the timer needs to be created, it will be created; if the timer is already running, 
	 * this call does nothing.
	 */
	private void activateGlobalAnimationTimer() {
		if(globalTimer==null) globalTimer = new Timer(20, new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent ae){
		      if(!(openerTimer!=null && openerTimer.getRadius()>0.0) && closerTimers.size()==0){
		    	globalTimer.stop(); return;
		      }
		      
		      Graphics2D g2 = (Graphics2D)frameRoot.getGraphics();
			  //drawing groups dock opening animation
			  if(openerTimer!=null && openerTimer.getRadius()>0.0){
				drawCirclesAroundGroups(openerTimer.getRadius(), g2);
			  }
			  //drawing groups dock closing animations
			  drawAllGroupClosingAnimations(g2);	
			}
		});

		if(!globalTimer.isRunning()) globalTimer.start();
	}

	/**
	 * Starts the group dock opening animation.
	 */
	protected void startGroupOpeningAnimation() {
		if(openerTimer==null) openerTimer = 
			new GroupAnimationTimer(0.0, groupDockOpeningStepAmount, groupDockRadius, this, null, null, null);
		else openerTimer.clearRadius();
		openerTimer.start();
		activateGlobalAnimationTimer();
	}

	/**
	 * Stops the group dock opening animation.
	 */
	protected void stopGroupOpeningAnimation() {
//		if(openerTimer!=null){
//		    if(openerTimer.isRunning()) openerTimer.stop();
//		    openerTimer.clearRadius();
//		}
//		repaintRootFrame();
		
		if(openerTimer!=null && openerTimer.isRunning()) openerTimer.stop();
	}

	/**
	 * Adds a new timer for the group dock closing animation.
	 */
	protected void addNewCloserTimer(GroupPanel group, Point2D.Double anchorLocation, BufferedImage anchorImage){
		if(openerTimer.getRadius()<1){ openerTimer.clearRadius(); return;}
		GroupAnimationTimer closerTimer =
			new GroupAnimationTimer(openerTimer.getRadius(), groupDockClosingStepAmount, 1,
									this, group, anchorLocation, anchorImage);
		openerTimer.clearRadius();
		closerTimers.add(closerTimer);
		closerTimer.start();
		activateGlobalAnimationTimer();
	}

	/**
	 * Removes a group dock closer timer.
	 */
	public void removeCloserTimer(GroupAnimationTimer timer) {
		System.out.println("removing closing timer from list");
		closerTimers.remove(timer);
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
//		x=x+(int)(diagramPanel.getLocationOnScreen().getX()-underlyingPanel.getLocationOnScreen().getX());
//		y=y+(int)(diagramPanel.getLocationOnScreen().getY()-underlyingPanel.getLocationOnScreen().getY());
		x=x-underlyingPanel.getX();
		y=y-underlyingPanel.getY();
		
		subComponent = (JComponent) underlyingPanel.getComponentAt(x, y);

		if(subComponent==null ||
		  (!subComponent.getClass().equals(AnchorPanel.class) && !subComponent.getClass().equals(GroupPanel.class)) )
		  return underlyingPanel;
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
	 * Adds the connector ending and starting dots to the lists used to draw connectors.
	 * 
	 * @param newConnectorStartDot - the starting connector dot
	 * @param type - an ItemsType object that representing the type of the constraint
	 */
	private void addConstraintToDrawLists(JComponent newConstraintStartDot, ItemsType type) {
		if(type==ItemsType.START_EXCLUDES_DOT) startExcludesDots.add(newConstraintStartDot);
		else startIncludesDots.add(newConstraintStartDot);
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
	private void deleteFeature(JComponent feature) {
	  //attaching all feature anchors to the diagram
  	  for(Component comp : feature.getComponents())
  		if(comp.getName()!=null 
  		   && ( comp.getName().startsWith(altGroupNamePrefix) || comp.getName().startsWith(orGroupNamePrefix) 
  				|| comp.getName().startsWith(endMandatoryNamePrefix) 
  				|| comp.getName().startsWith(startMandatoryNamePrefix)
  				|| comp.getName().startsWith(endOptionalNamePrefix) 
  				|| comp.getName().startsWith(startOptionalNamePrefix)
  				|| comp.getName().startsWith(startExcludesNamePrefix) 
  				|| comp.getName().startsWith(endExcludesNamePrefix)
  				|| comp.getName().startsWith(startIncludesNamePrefix) 
  				|| comp.getName().startsWith(endIncludesNamePrefix)
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
	  if(group.getMembers().size()<=2){
		System.out.println("Ungroup is not possible, this group is already minimal.");
		return;
	  }
	  AnchorPanel startConnectorDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.START_MANDATORY_CONNECTOR, 0, 0);
	  startDotlocationX=(int)(anchor.getLocationOnScreen().getX()-diagramPanel.getLocationOnScreen().getX());
	  startDotlocationY=(int)(anchor.getLocationOnScreen().getY()-diagramPanel.getLocationOnScreen().getY());
	  group.getMembers().remove(anchor);
	  
	  if(startDotlocationX-(anchor.getWidth()*2)>0)
		  startDotlocationX=startDotlocationX-(anchor.getWidth()*2);
	  else startDotlocationX=startDotlocationX+(anchor.getWidth()*2);
	  if(startDotlocationY-(anchor.getHeight()*2)>0)
		  startDotlocationY=startDotlocationY-(anchor.getHeight()*2);
	  else startDotlocationY=startDotlocationY+(anchor.getHeight()*2);
	  
	  startConnectorDot.setLocation(startDotlocationX, startDotlocationY);
	  if(anchor.getName().startsWith(startMandatoryNamePrefix))
	    startConnectorDot.setName(startMandatoryNamePrefix+anchor.getName().substring(anchor.getName().indexOf("#")+1));
	  else startConnectorDot.setName(startOptionalNamePrefix+anchor.getName().substring(anchor.getName().indexOf("#")+1));
	  startConnectorDot.setOtherEnd(anchor);
	  anchor.setOtherEnd(startConnectorDot);
	  visibleOrderDraggables.addToTop(startConnectorDot);
	  startConnectorDots.add(startConnectorDot);

	  
	  addToDiagramOnTop(startConnectorDot);
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
		
		/* ****DEBUG*** */
		if(debug) System.out.println("feature="+feature.getName()+"\tanchor="+anchor.getName());
		/* ****DEBUG*** */
		
		anchor.setLocation(
		  (int)(anchorPanelOnScreenX-diagramPanel.getLocationOnScreen().getX()),
		  (int)(anchorPanelOnScreenY-diagramPanel.getLocationOnScreen().getY()));
		
		addToDiagramOnTop(anchor);
		moveComponentToTop(anchor);
		
		if (anchor.getName().startsWith(startMandatoryNamePrefix) || anchor.getName().startsWith(startOptionalNamePrefix))
		  startGroupOpeningAnimation();
		
		activateGlobalAnimationTimer();

		return;
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
        }
        else if(anchor.getParent().getName().startsWith(featureNamePrefix)){
          anchor.getParent().remove(anchor);
      	  visibleOrderDraggables.remove(anchor);
        }

        if (anchor.getName().startsWith(startMandatoryNamePrefix) ||
        	anchor.getName().startsWith(startOptionalNamePrefix)) startConnectorDots.remove(anchor);
        else if(anchor.getName().startsWith(startExcludesNamePrefix)) startExcludesDots.remove(anchor);
        else if(anchor.getName().startsWith(startIncludesNamePrefix)) startIncludesDots.remove(anchor);        
	}

	/**
	 * Return a new Color, created using rgb parameters values.
	 * 
	 * @param RGBValues - an int[] containing color RGB values in order: red in position 0, green in pos. 1, blue in pos. 2.
	 * @return - the new Color object required
	 */
	private static Color getNewColor(int[] RGBValues) {
		if(RGBValues==null) return Color.BLACK;
		float[] colorHBS=null;
		Color featureColor=null;
		colorHBS=Color.RGBtoHSB(RGBValues[0], RGBValues[1], RGBValues[2], null);		
		featureColor=Color.getHSBColor(colorHBS[0], colorHBS[1], colorHBS[2]);
		return featureColor;
	}
	
	/** Sets the action that should be performed when the JFrame is closed*/
	public void setOnCloseOperation(int operation){
		onCloseOperation=operation;
	};
	
	/** Gets the action that should be performed when the JFrame is closed*/
	public int getOnCloseOperation(){
		return onCloseOperation;
	};

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
	
	/** Returns the 'Rename Feature' popup menu item */
	public JMenuItem getPopMenuItemRenameFeature(){
		return popMenuItemRenameFeature;
	};	
	
	/** Returns the 'Search Feature' popup menu item */
	public JMenuItem getPopMenuItemSearchFeature(){
		return popMenuItemSearchFeature;
	};	

	/** Returns the 'Change Color' popup menu item */
	public JMenuItem getPopMenuItemChangeColor() {
		return popMenuItemChangeColor;
	}
	
	/** Returns the 'Delete Connector' popup menu item */
	public JMenuItem getPopMenuItemDeleteConnector(){
		return popMenuItemDeleteConnector;
	};
	
	/** Returns the 'Delete Group' popup menu item */
	public JMenuItem getPopMenuItemDeleteGroup(){
		return popMenuItemDeleteGroup;
	};
	
	/** Returns the 'Delete Constraint' popup menu item */
	public JMenuItem getPopMenuItemDeleteConstraint(){
		return popMenuItemDeleteConstraint;
	};
	
	/** Returns the 'Show Control Point' popup menu item */
	public JMenuItem getPopMenuItemShowControlPoint(){
		return popMenuItemShowControlPoint;
	};
	
	/** Returns the 'Hide Control Point' popup menu item */
	public JMenuItem getPopMenuItemHideControlPoint(){
		return popMenuItemHideControlPoint;
	};
	
	/** Returns the 'Ungroup Element' popup menu item */
	public JMenuItem getPopMenuItemUngroup(){
		return popMenuItemUngroup;
	};
	
	/** Returns the 'Print Model[DEBUG COMMAND]' popup menu item */
	public JMenuItem getPopMenuItemPrintModelDebug(){
		return popMenuItemPrintModelDebug;
	};

	/** Returns the 'Print Model[DEBUG COMMAND]' popup menu item */
	public JMenuItem getPopMenuItemFitDiagram(){
		return popMenuItemFitDiagram;
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
	
	/** Returns the 'View Commonality/Variability' menuView item */
	public JMenuItem getMenuViewCommsOrVars(){
		return menuViewCommsOrVars;
	};
	
	/** Returns the last active item type*/
	public ActiveItems getActiveItem(){
		return isActiveItem;
	};
	
	/** Sets the last active item type*/
	public void setActiveItem(ActiveItems item){
		isActiveItem=item;
	};
	
	/**
	 * Resets the static variables used during drag operations.
	 */
	private void resetActiveItems(){
		isActiveItem=ActiveItems.NO_ACTIVE_ITEM;
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

	/**
	 * Returns the ImageIcon of a starting connector line between a group and one of its ending anchor.
	 * 
	 * @return an ImageIcon of a starting connector line
	 */
	public ImageIcon getGroupLineIcon(){
	  return new ImageIcon(groupLineLengthIconURL);
	}
	
	/** Tells the size of a feature panel*/
	public Dimension getFeatureSize(){
		return new Dimension(120+featureBorderSize, 60+featureBorderSize);
	}
	
	/** Tells the size of an anchor panel*/
	public Dimension getAnchorSize(){
		return new Dimension(10, 10);
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

	/** Returns the diagram scroller*/
	public JScrollPane getDiagramScroller(){
		return diagramScroller;
	}

	/** Returns the toolbar panel*/
	public JPanel getToolsPanel(){
		return toolsPanel;
	}

	/** Returns the splitter panel containing diagram and toolbar panels*/
	public JSplitPane/*EditorSplitPane*/ getSplitterPanel(){
		return splitterPanel;
	}

//	/** Returns the JFrame containing the search panels*/
//	public JFrame getSearchFrame(){
//		return searchFrame;
//	}

	/** Sets the JFrame containing the search panels*/
	public void setSearchFrame(JFrame frame){
		this.searchFrame=frame;
	}
	
	/** Returns a String containing the resource name of the requested tool image */
	public String getToolIconPath(String toolName){
		return toolIconPaths.get(toolName);
	}	
	
	/** Returns the number of features added to the diagram */
	public int getFeaturesCount(){
		return featuresCount;
	}
	
	/** Sets the number of features added to the diagram */
	public void setFeaturesCount(int featuresCount){
		this.featuresCount=featuresCount;
	}
	
	/** Increments the value of field featuresCount by 1. 
	 * {@link featuresCount}
	 */
	public void incrFeaturesCount(){
		++featuresCount;
	}	
	
	/** Returns the number of alternative groups added to the diagram */
	public int getAltGroupsCount(){
		return altGroupsCount;
	}
	
	/** Returns the number of alternative groups added to the diagram */
	public void setAltGroupsCount(int altGroupsCount){
		this.altGroupsCount=altGroupsCount;
	}
	
	/** Increments the value of field altGroupsCount by 1. 
	 * {@link featuresCount}
	 */
	public void incrAltGroupsCount(){
		++altGroupsCount;
	}	
	
	/** Returns the number of or groups added to the diagram */
	public int getOrGroupsCount(){
		return orGroupsCount;
	}
	
	/** Sets the number of or groups added to the diagram */
	public void setOrGroupsCount(int orGroupsCount){
		this.orGroupsCount=orGroupsCount;
	}
	
	/** Increments the value of field orGroupsCount by 1. 
	 * {@link featuresCount}
	 */
	public void incrOrGroupsCount(){
		++orGroupsCount;
	}
	
	/** Returns the number of includes added to the diagram */
	public int getConstraintsCount(){
		return constraintsCount;
	}
	
	/** Sets the number of includes added to the diagram */
	public void setConstraintsCount(int constraintsCount){
		this.constraintsCount=constraintsCount;
	}
	
	/** Increments the value of field includesCount by 1. 	 */
	public void incrConstraintsCount(){
		++constraintsCount;
	}
	
	/** Returns the number of connectors added to the diagram */
	public int getConnectorsCount(){
		return connectorsCount;
	}
	
	/** Increments the value of field connectorsCount by 1. */
	public void incrConnectorsCount(){
		++connectorsCount;
	}
	
	/** Returns the number of constraint controls pointsadded to the diagram */
	public int getConstraintControlsCount(){
		return constraintControlsCount;
	}
	
	/** Increments the value of field constraintControlsCount by 1. */
	public void incrConstraintControlsCount(){
		++constraintControlsCount;
	}

	/** Returns an ArrayList<String> representing the starting commonalities*/
	public ArrayList<String> getStartingCommonalities(){
	  return startingCommonalities;
	}
	
	/** Returns an ArrayList<String> representing the starting variabilities*/
	public ArrayList<String> getStartingVariabilities(){
	  return startingVariabilities;
	}

	/** Returns the name of next feature to add to the diagram.*/
	public String getFeatureToAddName(){
	  return featureToAddName;
	}
	
	/** Sets the name of next feature to add to the diagram.*/
	public void setFeatureToAddName(String name){
	  featureToAddName=name;
	}
	
	/** Sets the old name of the feature about to be renamed.*/
	public void setOldFeatureName(String name){
	  oldFeatureName=name;
	}
	
	/** Returns the verticalShift of the diagram.*/
	public int getVerticalShift(){
	  return verticalShift;
	}
	
	/** Sets the verticalShift of the diagram.*/
	public void setVerticalShift(int y){
	  verticalShift+=y;
	  repaintRootFrame();
	}
	
	/** Returns the verticalShift of the diagram.*/
	public int getHorizontalShift(){
	  return horizontalShift;
	}
	
	/** Sets the verticalShift of the diagram.*/
	public void setHorizontalShift(int y){
	  horizontalShift+=y;
	  repaintRootFrame();
	}

	/** Returns the scaleFactor of the diagram.*/
	public double getScaleFactor(){
	  return scaleFactor;
	}
	
	/** Raises the scaleFactor of the diagram.*/
	public void raiseScaleFactor(){
//	  double newWidth=diagramPanelBasePreferredSize.width;
//	  double newHeight=diagramPanelBasePreferredSize.height;
//	  double oldScaleFactor=scaleFactor;
//	  JScrollBar hori = diagramScroller.getHorizontalScrollBar();
//	  JScrollBar vert = diagramScroller.getVerticalScrollBar();

/*	  
	  int oldHoriValue = hori.getValue();
	  int oldVertValue = vert.getValue();
	  
	  int oldHoriMinimum = hori.getMinimum();
	  int oldVertMinimum = vert.getMinimum();
	  
	  int oldHoriVisibleAmount = hori.getVisibleAmount();
	  int oldVertVisibleAmount = vert.getVisibleAmount();
	  
	  int oldHoriMaximum = hori.getMaximum();
	  int oldVertMaximum = vert.getMaximum();
*/

	  if(scaleFactor<1.0){
		  
//		System.out.println(
//		 "HorizontalScrollBar: ["+hori.getValue()+", "+hori.getVisibleAmount()+", "+hori.getMinimum()+", "+hori.getMaximum()+"]");
//		System.out.println(
//		 "VerticalScrollBar: ["+vert.getValue()+", "+vert.getVisibleAmount()+", "+vert.getMinimum()+", "+vert.getMaximum()+"]");

//		if(scaleFactor==0.9){
////		  diagramPanelBasePreferredSize=new Dimension(diagramPanelBasePreferredSizeNormalScale);
//		  scaleFactor=1.0;
//
//		  resizeToolsIcons();
//			
//		  fitDiagram();

/*		  
		  hori.setValues(
//		   (int)((((hori.getValue()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),.
		   oldHoriValue,
//		   (int)((((hori.getVisibleAmount()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		   oldHoriVisibleAmount,
//		   (int)((((hori.getMinimum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		   oldHoriMinimum,
		   oldHoriMaximum);
//		   (int)((((hori.getMaximum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor));
			
		  vert.setValues(
//		   (int)((((vert.getValue()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		   oldVertValue,
//		   (int)((((vert.getVisibleAmount()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		   oldVertVisibleAmount,
//		   (int)((((vert.getMinimum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		   oldVertMinimum,
		   oldVertMaximum);
//		   (int)((((vert.getMaximum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor));
*/

//		  repaintRootFrame();
//		  return;
//		}
		
		if(scaleFactor==0.5) scaleFactor=0.6;
		else if(scaleFactor==0.6) scaleFactor=0.7;
		else if(scaleFactor==0.7) scaleFactor=0.8;
		else if(scaleFactor==0.8) scaleFactor=0.9;
		else if(scaleFactor==0.9) scaleFactor=1.0;

		resizeToolsIcons();

//		newWidth=(newWidth*oldScaleFactor)/scaleFactor;
//		newHeight=(newHeight*oldScaleFactor)/scaleFactor;
//		
//		diagramPanelBasePreferredSize.width=((int)newWidth);
//		diagramPanelBasePreferredSize.height=((int)newHeight);
		
		fitDiagram();		

/*		
		hori.setValues(
//		 (int)((((hori.getValue()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldHoriValue,
//		 (int)((((hori.getVisibleAmount()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldHoriVisibleAmount,
//		 (int)((((hori.getMinimum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldHoriMinimum,
		 oldHoriMaximum);
//		 (int)((((hori.getMaximum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor));
		
		vert.setValues(
//		 (int)((((vert.getValue()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldVertValue,
//		 (int)((((vert.getVisibleAmount()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldVertVisibleAmount,
//		 (int)((((vert.getMinimum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldVertMinimum,
		 oldVertMaximum);
//		 (int)((((vert.getMaximum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor));
*/
		  
//		System.out.println(
//		 "HorizontalScrollBar: ["+hori.getValue()+", "+hori.getVisibleAmount()+", "+hori.getMinimum()+", "+hori.getMaximum()+"]");
//		System.out.println(
//		 "VerticalScrollBar: ["+vert.getValue()+", "+vert.getVisibleAmount()+", "+vert.getMinimum()+", "+vert.getMaximum()+"]");

		repaintRootFrame();
	  }
	}
	
	/** Lowers the scaleFactor of the diagram.*/
	public void lowerScaleFactor(){
//	  double newWidth=diagramPanelBasePreferredSize.width;
//	  double newHeight=diagramPanelBasePreferredSize.height;
//	  double oldScaleFactor=scaleFactor;
//	  JScrollBar hori = diagramScroller.getHorizontalScrollBar();
//	  JScrollBar vert = diagramScroller.getVerticalScrollBar();

/*	  
	  int oldHoriValue = hori.getValue();
	  int oldVertValue = vert.getValue();
	  
	  int oldHoriMinimum = hori.getMinimum();
	  int oldVertMinimum = vert.getMinimum();
	  
	  int oldHoriVisibleAmount = hori.getVisibleAmount();
	  int oldVertVisibleAmount = vert.getVisibleAmount();
	  
	  int oldHoriMaximum = hori.getMaximum();
	  int oldVertMaximum = vert.getMaximum();
*/

	  if(scaleFactor>0.5){

//	    System.out.println(
//	     "HorizontalScrollBar: ["+hori.getValue()+", "+hori.getVisibleAmount()+", "+hori.getMinimum()+", "+hori.getMaximum()+"]");
//	    System.out.println(
//	     "VerticalScrollBar: ["+vert.getValue()+", "+vert.getVisibleAmount()+", "+vert.getMinimum()+", "+vert.getMaximum()+"]");

		if(scaleFactor==1.0) scaleFactor=0.9;
		else if(scaleFactor==0.9) scaleFactor=0.8;
		else if(scaleFactor==0.8) scaleFactor=0.7;
		else if(scaleFactor==0.7) scaleFactor=0.6;
		else if(scaleFactor==0.6) scaleFactor=0.5;
		

		resizeToolsIcons();
		
		
//		newWidth=(newWidth*oldScaleFactor)/scaleFactor;
//		newHeight=(newHeight*oldScaleFactor)/scaleFactor;
//		diagramPanelBasePreferredSize.width=((int)newWidth);
//		diagramPanelBasePreferredSize.height=((int)newHeight);
		
		fitDiagram();		

/*		
		hori.setValues(
//		 (int)((((hori.getValue()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldHoriValue,
//		 (int)((((hori.getVisibleAmount()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldHoriVisibleAmount,
//		 (int)((((hori.getMinimum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldHoriMinimum,
		 oldHoriMaximum);
//		 (int)((((hori.getMaximum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor));
		
		vert.setValues(
//		 (int)((((vert.getValue()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldVertValue,
//		 (int)((((vert.getVisibleAmount()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldVertVisibleAmount,
//		 (int)((((vert.getMinimum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor),
		 oldVertMinimum,
		 oldVertMaximum);
//		 (int)((((vert.getMaximum()/oldScaleFactor)*scaleFactor)/oldScaleFactor)*scaleFactor));
*/
		
//	    System.out.println(
//	     "HorizontalScrollBar: ["+hori.getValue()+", "+hori.getVisibleAmount()+", "+hori.getMinimum()+", "+hori.getMaximum()+"]");
//	    System.out.println(
//	   	 "VerticalScrollBar: ["+vert.getValue()+", "+vert.getVisibleAmount()+", "+vert.getMinimum()+", "+vert.getMaximum()+"]");
		
		repaintRootFrame();
	  }
	}
	
	/**
	 * Resizes all tools in the toolbar by the current scale factor.
	 */
	private void resizeToolsIcons(){
	  String compName;
	  BufferedImage toolIcon;
	  ImageIcon icon;
	  try {
		for (Component comp : toolsPanel.getComponents()){
		  compName = comp.getName();
		  if (   compName.compareTo(TOOL_NEWFEATURE)==0
				 || compName.compareTo(TOOL_MANDATORY_LINK)==0
				 || compName.compareTo(TOOL_OPTIONAL_LINK)==0
				 || compName.compareTo(TOOL_ALT_GROUP)==0
				 || compName.compareTo(TOOL_OR_GROUP)==0
				 || compName.compareTo(TOOL_INCLUDES)==0
				 || compName.compareTo(TOOL_EXCLUDES)==0 ){
			toolIcon = ImageIO.read(this.getClass().getResourceAsStream(getToolIconPath(compName)));

			if(scaleFactor!=1.0) 
			  toolIcon = ImageUtils.toBufferedImage(toolIcon.getScaledInstance(
				(int)(toolIcon.getWidth()*scaleFactor), (int)(toolIcon.getHeight()*scaleFactor), Image.SCALE_SMOOTH));
			
			icon = new ImageIcon(toolIcon);
			((JLabel)comp).setIcon(icon);
		  }
		}
	  } catch (IOException e){ e.printStackTrace();}
	}
	
	
	/**
	 * Checks whether the dragged tool has been dropped on the diagram panel or not.
	 * 
	 * @return true if tool has been dropped on the diagram panel, false otherwise
	 */
	public boolean checkDroppedOnDiagram() {
		int actualPositionX=0;
		int actualPositionY=0;
		
		actualPositionX=
		  (toolDragPosition.x+(int)frameRoot.getLocationOnScreen().getX()-(int)diagramPanel.getLocationOnScreen().getX());
		actualPositionY=
		  (toolDragPosition.y+(int)frameRoot.getLocationOnScreen().getY()-(int)diagramPanel.getLocationOnScreen().getY());

		//the new feature must be dropped on the diagram panel for it to be added
		if( diagramPanel.getX()>actualPositionX ||
			diagramPanel.getX()+diagramPanel.getWidth()<=actualPositionX ||
			diagramPanel.getY()>actualPositionY ||
			diagramPanel.getX()+diagramPanel.getHeight()<=actualPositionY ){		
		
//		if( diagramPanel.getLocationOnScreen().getX()>toolDragPosition.x ||
//			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getWidth()<=toolDragPosition.x ||
//			diagramPanel.getLocationOnScreen().getY()>toolDragPosition.y ||
//			diagramPanel.getLocationOnScreen().getX()+diagramPanel.getHeight()<=toolDragPosition.y ){

			cancelToolDrag();

			/* ***DEBUG*** */
			if(debug4) System.out.println("Cannot drop a new feature on tools panel.");
			/* ***DEBUG*** */
			
			return false;
		}
		else{
		  setLastPositionX(actualPositionX);
		  setLastPositionY(actualPositionY);

		  return true;
		}
	}

	/** shows the popup menu on the diagram, at the clicked location.*/
	public void showDiagramElementsMenu(){
	    diagramElementsMenu.show(diagramPanel, diagramElementsMenuPosX, diagramElementsMenuPosY);		
	}

	/** Repaints frameRoot.*/
	public void repaintRootFrame(){
		frameRoot.repaint();		
	}

	/**
	 * Reverts to the old name the last renamed feature.
	 */
	private void undoRenameFeature() {
	  ((FeaturePanel)popUpElement).getTextArea().setText(oldFeatureName);
	}

	/**
	 * Renames a feature, adding the proper suffix if Commonalities/Variabilities distinction is active.
	 */
	private void renameFeature(){
	  String newName=null;
	  
	  if(!getMenuViewCommsOrVars().isSelected()) return;
	  else newName=((FeaturePanel)popUpElement).getLabelName();
	  
	  for(String tmp : startingCommonalities) if(tmp.compareTo(newName)==0){
		((FeaturePanel)popUpElement).getTextArea().setText(newName+"\n{C}");
		return;
	  }
	  
	  for(String tmp : startingVariabilities) if(tmp.compareTo(newName)==0){
		((FeaturePanel)popUpElement).getTextArea().setText(newName+"\n{V}");
		return;
	  }
	}	
	
	/**
	 * Enables or disables the visualization of Commonalities/Variabilities distinction in the feature names.
	 * 
	 * @param activate - if true, visualization will be activated, otherwise it will be deactivated
	 */
	public void viewCommVarsDistinction(boolean activate) {
	  OrderedListNode tmp=null;
	  FeaturePanel featTmp=null;
	  boolean found=false;
	  horPos = hori.getValue();
	  verPos = vert.getValue();

//	  Dimension diagramSize = null;
			  
//	  Document newDoc = null;
	  
	  if(activate){//activating the visualization
		tmp = visibleOrderDraggables.getFirst();
		while(tmp!=null){
		  if(((JComponent)tmp.getElement()).getName().startsWith(featureNamePrefix)){
			featTmp = (FeaturePanel)tmp.getElement();
			found=false;
			  
			for(String comm : startingCommonalities) if(featTmp.getLabelName().compareTo(comm)==0){
//			  featTmp.getTextArea().setIgnoreRepaint(true);
			  featTmp.getTextArea().setVisible(false);
			  textAreaList.add(featTmp.getTextArea());

//			  featTmp.getTextArea().setMustDraw(false);

/*				
			  System.out.println("getPropertyChangeListeners().length: "
					  +featTmp.getTextArea().getPropertyChangeListeners().length);
			  for(PropertyChangeListener pop: featTmp.getTextArea().getPropertyChangeListeners()){
				  System.out.println("Property changed = " + pop.toString()+" will be removed.");
//				  featTmp.getTextArea().removePropertyChangeListener(pop);				  
			  }
			  
			  featTmp.getTextArea().addPropertyChangeListener(new PropertyChangeListener() {				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
				  System.out.println("PropertyChangeEvent.getPropertyName: "+evt.getPropertyName());					
				}
			  });
*/

			  featTmp.getTextArea().setText(comm+"\n{C}"); 
			  RepaintManager.currentManager(featTmp).markCompletelyClean(featTmp);

//			  newDoc = featTmp.getTextArea().getEditorKit().createDefaultDocument();
//			  try { newDoc.insertString(0, comm+"\n{C}", null);
//			  } catch (BadLocationException e) { e.printStackTrace();}
//
//			  featTmp.getTextArea().setDocument(newDoc);
		      
//		      RepaintManager.currentManager(featTmp.getTextArea()).markCompletelyClean(featTmp.getTextArea());
		      /*featTmp.invalidate();*/ 
		      found=true; break;
			}
			  
			if(!found) for(String comm : startingVariabilities) if(featTmp.getLabelName().compareTo(comm)==0){
//			  featTmp.getTextArea().setIgnoreRepaint(true);
			  featTmp.getTextArea().setVisible(false);
			  textAreaList.add(featTmp.getTextArea());
//			  RepaintManager.currentManager(featTmp.getTextArea()).markCompletelyClean(featTmp.getTextArea());

//			  featTmp.getTextArea().setMustDraw(false);
				
/*				
			  System.out.println("getPropertyChangeListeners().length: "
					  +featTmp.getTextArea().getPropertyChangeListeners().length);
			  for(PropertyChangeListener pop: featTmp.getTextArea().getPropertyChangeListeners())
				  System.out.println("Property changed = " + pop.toString());

			  featTmp.getTextArea().addPropertyChangeListener(new PropertyChangeListener() {				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
				  System.out.println("PropertyChangeEvent.getPropertyName: "+evt.getPropertyName());					
				}
			  });
*/
				
			  featTmp.getTextArea().setText(comm+"\n{V}"); 
			  RepaintManager.currentManager(featTmp).markCompletelyClean(featTmp);

//			  newDoc = featTmp.getTextArea().getEditorKit().createDefaultDocument();
//			  try { newDoc.insertString(0, comm+"\n{V}", null);
//			  } catch (BadLocationException e) { e.printStackTrace();}
//
//			  featTmp.getTextArea().setDocument(newDoc);

//			  RepaintManager.currentManager(featTmp.getTextArea()).markCompletelyClean(featTmp.getTextArea());
			  /*featTmp.invalidate();*/ 
			  found=true; break;
			}
			
//			if(found) RepaintManager.currentManager(featTmp).markCompletelyClean(featTmp);
		  }
		  
		  tmp=tmp.getNext();
		}		  
	  }
	  else{//deactivating the visualization
		tmp = visibleOrderDraggables.getFirst();
		while(tmp!=null){
		  if(((JComponent)tmp.getElement()).getName().startsWith(featureNamePrefix)){
			featTmp = (FeaturePanel)tmp.getElement();
			found=false;
				  
			for(String comm : startingCommonalities) if(featTmp.getLabelName().compareTo(comm+"\n{C}")==0){
//			  featTmp.getTextArea().setIgnoreRepaint(true);
			  featTmp.getTextArea().setVisible(false);
			  textAreaList.add(featTmp.getTextArea());
//			  RepaintManager.currentManager(featTmp.getTextArea()).markCompletelyClean(featTmp.getTextArea());

//			  featTmp.getTextArea().setMustDraw(false);

/*				
			  System.out.println("getPropertyChangeListeners().length: "
					  +featTmp.getTextArea().getPropertyChangeListeners().length);
			  for(PropertyChangeListener pop: featTmp.getTextArea().getPropertyChangeListeners())
				  System.out.println("Property changed = " + pop.toString());

			  featTmp.getTextArea().addPropertyChangeListener(new PropertyChangeListener() {				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
				  System.out.println("PropertyChangeEvent.getPropertyName: "+evt.getPropertyName());					
				}
			  });
*/
				
			  featTmp.getTextArea().setText(comm); 
			  RepaintManager.currentManager(featTmp).markCompletelyClean(featTmp);

//			  newDoc = featTmp.getTextArea().getEditorKit().createDefaultDocument();
//			  try { newDoc.insertString(0, comm, null);
//			  } catch (BadLocationException e) { e.printStackTrace();}
//
//			  featTmp.getTextArea().setDocument(newDoc);
			  
//			  RepaintManager.currentManager(featTmp.getTextArea()).markCompletelyClean(featTmp.getTextArea());
			  /*featTmp.invalidate();*/ 
			  found=true; break;
			}
				  
			if(!found) for(String comm : startingVariabilities) if(featTmp.getLabelName().compareTo(comm+"\n{V}")==0){
//			  featTmp.getTextArea().setIgnoreRepaint(true);
			  featTmp.getTextArea().setVisible(false);
			  textAreaList.add(featTmp.getTextArea());
//			  RepaintManager.currentManager(featTmp.getTextArea()).markCompletelyClean(featTmp.getTextArea());

//			  featTmp.getTextArea().setMustDraw(false);
				
/*				
			  System.out.println("getPropertyChangeListeners().length: "
					  +featTmp.getTextArea().getPropertyChangeListeners().length);
			  for(PropertyChangeListener pop: featTmp.getTextArea().getPropertyChangeListeners())
				  System.out.println("Property changed = " + pop.toString());
			  
			  featTmp.getTextArea().addPropertyChangeListener(new PropertyChangeListener() {				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
				  System.out.println("PropertyChangeEvent.getPropertyName: "+evt.getPropertyName());					
				}
			  });
*/
				
			  featTmp.getTextArea().setText(comm); 
			  RepaintManager.currentManager(featTmp).markCompletelyClean(featTmp);

//			  newDoc = featTmp.getTextArea().getEditorKit().createDefaultDocument();
//			  try { newDoc.insertString(0, comm, null);
//			  } catch (BadLocationException e) { e.printStackTrace();}
//
//			  featTmp.getTextArea().setDocument(newDoc);
			  
//			  RepaintManager.currentManager(featTmp.getTextArea()).markCompletelyClean(featTmp.getTextArea());
			  /*featTmp.invalidate();*/ 
			  found=true; break;
			}
			  
//			if(found) RepaintManager.currentManager(featTmp).markCompletelyClean(featTmp);
		  }

		  tmp=tmp.getNext();
		}		  	  
	  }
//	  tmp=visibleOrderDraggables.getFirst();
//	  ((JComponent)tmp.getElement()).setVisible(false);
//	  frameRoot.repaint();
//	  ((JComponent)tmp.getElement()).setVisible(true);
//	  frameRoot.repaint();
//	  if(featTmp!=null) moveComponentToTop(featTmp);
//	  frameRoot.invalidate();
//	  frameRoot.validate();
//	  diagramPanel.validate();
//	  diagramPanel.setComponentZOrder((Component) visibleOrderDraggables.getFirst().getElement(), 0);
//	  visibleOrderDraggables.moveToTop(comp);
//	  diagramPanel.repaint();
//	  diagramPanel.paint(diagramPanel.getGraphics());
//	  getLocationOnScreen();
//	  diagramPanel.repaint();
//	  getLocationOnScreen();
//	  diagramScroller.getHorizontalScrollBar();
//	  diagramScroller.getVerticalScrollBar();

	  ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
	  
	  Runnable task = new Runnable() {
		public void run() {
		  for(CenteredTextPane pane : textAreaList){
//			RepaintManager.currentManager(pane).markCompletelyClean(pane);
			pane.setVisible(true);
		  }
		  repaintRootFrame();
			
		  hori.setValue(horPos);
		  vert.setValue(verPos);
		  
		  textAreaList.clear();
		}
	  };
	  worker.schedule(task, 12, TimeUnit.MILLISECONDS);
		  
//	  diagramSize= diagramPanel.getPreferredSize();
//	  diagramSize.width+=20;
//	  diagramSize.height+=20;
//	  diagramPanel.setPreferredSize(diagramSize);
//	  diagramPanel.revalidate();	
//	  diagramSize.width-=20;
//	  diagramSize.height-=20;
//	  diagramPanel.setPreferredSize(diagramSize);
//	  diagramPanel.revalidate();		

	  
	}
	
	@Override
	public void update(Observable o, Object arg) {
		System.out.println("Message received: "+arg);
		if(		arg.equals("New Feature Correctly Added")) addNewFeatureToDiagram();		
		else if(arg.equals("Feature Renamed")) renameFeature();	
		else if(arg.equals("Feature Not Renamed")) undoRenameFeature();	
		else if(arg.equals("New Named Feature Correctly Added")) addNamedFeatureToDiagram();		
		else if(arg.equals("Feature Deleted")) deleteFeature(popUpElement);
		else if(arg.equals("Group Deleted")) deleteGroup(popUpElement);
		else if(arg.equals("Feature Not Deleted")) System.out.println("Cannot delete this feature!");
		else if(arg.equals("Grouped a Feature") ) addAnchorToFeature();
		else if(arg.equals("Group Added To Feature") ) addAnchorToFeature();	
		else if(arg.equals("Constraint Added") ) addAnchorToFeature();
		else if(arg.equals("Constraint Removed") ) detachAnchor(lastFeatureFocused, lastAnchorFocused);
		else if(arg.equals("Two Features Directly Linked")){
			addAnchorToFeature();
		}
		else if(arg.equals("Merged a Connector") ){
			stopGroupOpeningAnimation();
			addStartAnchorToGroup();
		}
		else if(arg.equals("Two Features Not Linked")
			 || arg.equals("Not Merged a Connector")){
			System.out.println("Operation would create a cycle and it has been aborted!");
			stopGroupOpeningAnimation();
			addNewCloserTimer(null, null, null);
		}
		else if(arg.equals("Not Grouped a Feature")
			 || arg.equals("Group Not Added To Feature")){
			System.out.println("Operation would create a cycle and it has been aborted!");
		}
		else if(arg.equals("Direct Link Destroyed")
			 || arg.equals("Group Removed From Feature")){
			detachAnchor(lastFeatureFocused, lastAnchorFocused);
		}
		else if(arg.equals("Direct Link Not Destroyed")
			 || arg.equals("Group Not Removed From Feature")
			 || arg.equals("Constraint Not Added")
			 || arg.equals("Constraint Not Removed")
			 || arg.equals("Direct Link Not Destroyed") ){
			resetActiveItems();
		}
	}
	
	/** 
	 * Assigns a name to the diagram to be saved.
	 * 
	 * @return s - String representing the diagram name, or null if dialog has been aborted
	 */
	public File saveDiagramDialog(String diagramPath, String diagramName){		
	  JFileChooser saveChooser= new JFileChooser(diagramPath);
	  String fileName=null;
	  File chosenFile=null;
	  
	  saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
	  saveChooser.setDialogTitle("Save Diagram");
	  saveChooser.setSelectedFile(new File(diagramName));
	  System.out.println("diagramName: "+diagramName);

      if (saveChooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION){
    	chosenFile = saveChooser.getSelectedFile();
    	fileName = chosenFile.getAbsolutePath();
    	if(!fileName.endsWith(".xml")){
    	  fileName = fileName+".xml";
    	  chosenFile = new File(fileName);
    	}    	
    	return chosenFile;
      }
      else return null;	  
	}
	
	/** 
	 * Assigns a name to the SXFM file to be created as result of model exportation.
	 * 
	 * @return s - String representing the SXFM name, or null if dialog has been aborted
	 */
	public File saveImageDialog(String imagesPath, String type){				
	  JFileChooser saveChooser= new JFileChooser(imagesPath);
	  String fileName=null;
	  File chosenFile=null;
	  saveChooser.setDialogTitle("Export as "+type.toUpperCase());
	  saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);

      if (saveChooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION){
    	chosenFile = saveChooser.getSelectedFile();
    	fileName = chosenFile.getAbsolutePath();
    	if(!fileName.endsWith("."+type)){
    	  fileName = fileName+"."+type;
    	  chosenFile = new File(fileName);
    	}    	
    	return chosenFile;
      }
      else return null;	  
	}

	/** 
	 * Assigns a name to the SXFM file to be created as result of model exportation.
	 * 
	 * @return s - String representing the SXFM name, or null if dialog has been aborted
	 */
	public String exportAsSXFMDialog() {
	  String s = null;			
	  JTextField jtf = new JTextField();
			 	
	  Object[] o1 = {"SXFM file name: ", jtf};
	  Object[] o2 = { "Cancel", "OK" };

	  int i = JOptionPane.showOptionDialog(new JFrame(), o1, "Export As SXFM",
		JOptionPane.YES_NO_OPTION, JOptionPane.DEFAULT_OPTION, null, o2, o2[1]);

	  if(i == JOptionPane.NO_OPTION){
		if((s = jtf.getText()) != null && !s.trim().equals("")) return s;
		else{
		  errorDialog("Invalid name");
		  return null;
		}
	  }		    		      
	  else return null;
	}
	
	/** 
	 * Prompts a file dialog and asks the user to select a diagram file.
	 * 
	 * @param message - the message to be shown to the user
	 * @param dirPath - the starting directory of the file dialog
	 * 
	 * @return s - the selected file path 
	 */
	public String loadXMLDialog(String message, String dirPath){
    	//checking if the diagrams save directory must be created
    	File dir=new File(dirPath);		
    	if(!dir.isDirectory() && !dir.mkdirs()){
    		errorDialog("Save Directory can't be created.");
    		return null;
    	}

		JFileChooser loadChooser= new JFileChooser(dirPath);
		
		loadChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    loadChooser.setFileFilter(new XMLFileFilter());		
		loadChooser.setDialogTitle(message);
		loadChooser.setDialogType(JFileChooser.OPEN_DIALOG);

		if (loadChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION){
		  return loadChooser.getSelectedFile().getAbsolutePath();
		}
		else return null;
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
	 * Asks user confirmation for saving this diagram or aborting the operation.
	 * 
	 * @return 1 if the user confirmed, 0 if the user don't wants to save, 2, if the user wants to abort the operation
	 */
	public int confirmOrAbortSaveDiagramDialog(String message){
		JFrame f = new JFrame("Save Diagram");
		
    	Object[] options = {"No","Yes", "Cancel"};			
		
		int i = JOptionPane.showOptionDialog(
				f, message, "Save Diagram",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
//		if(i == 1) return true;		
//		else return false;
		return i;
	}	
	
	/** 
	 * Asks user confirmation for saving this diagram.
	 * 
	 * @return true if the user confirmed, false otherwise
	 */
	public boolean confirmSaveDiagramDialog(String message){
		JFrame f = new JFrame("Save Diagram");
		
    	Object[] options = {"No","Yes"};			
		
		int i = JOptionPane.showOptionDialog(
				f, message, "Save Diagram",
				JOptionPane.YES_NO_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
		if(i == 1) return true;		
		else return false;
	}

	/**
	 * Export the content of the diagram panel as a PNG file.
	 * 
	 * @param imagesPath - standard path where diagram images will be saved
	 * @param type - the type of image to export, valid values are PNG and GIF
	 */
	public void exportAsImageFile(String imagesPath, String type){				
	  if(type.compareTo("png")!=0 && type.compareTo("gif")!=0){
		errorDialog("Invalid format type: "+type);
		return;		  
	  }		
		
	  //checking if the diagrams save directory must be created
	  File dir=new File(imagesPath);
	  if(!dir.isDirectory() && !dir.mkdirs() ){
		errorDialog("Images Save Directory can't be created.");
		return;
	  }

	  File fileName=saveImageDialog(imagesPath, type);
	  if(fileName==null) return;

	  //saving xml string on file
	  BufferedImage bi = new BufferedImage(diagramPanel.getSize().width,
			diagramPanel.getSize().height, BufferedImage.TYPE_INT_ARGB); 
	  Graphics g = bi.createGraphics();
	  diagramPanel.paint(g);
	  g.dispose();	  

	  try{
		ImageIO.write(bi, type, fileName);
	  }catch(Exception e) {
		errorDialog("Error while exporting to "+type+" format");
		e.printStackTrace();
	  }
	}	

	/**
	 * Saves the visual elements of the diagram and the diagram state on file.
	 * @param pathProject - the directory path where to save the diagram
	 * @param s - the name of the file in which to save the diagram
	 */
	public String saveDiagram(String pathProject, String s) {
		OrderedListNode tmp = null;
		String xml = null;
		FeaturePanel featTmp=null;
		AnchorPanel anchTmp=null;
		AnchorPanel endTmp=null;
		ConstraintPanel constrTmp=null;
		ConstraintPanel endConstrTmp=null;
		JComponent constControlPoint=null;
		String startOwner=null;
		String endOwner=null;
		Iterator<Entry<String, int[]>> colorIter = null;
		Entry<String, int[]> colorEntry=null;
		int[] color=null;
		Color featColor=null;

		Iterator<Entry<String, HashMap<String, ArrayList<int[]>>>> termIter = null;
		Entry<String, HashMap<String, ArrayList<int[]>>> termEntry = null;

		Iterator<Entry<String, ArrayList<int[]>>> fileIter = null;
		Entry<String, ArrayList<int[]>> fileEntry = null;
		
		Iterator<Entry<String, HashMap<String, ArrayList<String>>>> termVersionIter = null;
		Entry<String, HashMap<String, ArrayList<String>>> termVersionEntry = null;

		Iterator<Entry<String, ArrayList<String>>> fileVersionIter = null;
		Entry<String, ArrayList<String>> fileVersionEntry = null;
		
		String tmpLine=null;
		
		
		//saving diagram graphic elements data
		String savePath = pathProject + OSUtils.getFilePathSeparator() + s + "_DiagView.xml"; 

		xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			  +"<Diagram name=\"" + s + "\">"
				+"<features>";
		
		/* ***DEBUG*** */
		if(debug) System.out.println("***Printing draggables in reverse order before save***\n");
		/* ***DEBUG*** */

		//saving features
		tmp = visibleOrderDraggables.getLast();
		while(tmp!=null){
		  if(((JComponent)tmp.getElement()).getName().startsWith(featureNamePrefix)){
			featTmp = (FeaturePanel)tmp.getElement();
			featColor = featTmp.getBackground();
			
			/* ***DEBUG*** */
			if(debug) System.out.println("Adding element: "+featTmp.getID());
			/* ***DEBUG*** */

			xml+="Name="+featTmp.getLabelName()
			   +" ContName="+featTmp.getID()
			   +" Color="+featColor.getRed()+"-"+featColor.getGreen()+"-"+featColor.getBlue()
			   +" Loc="+featTmp.getX()+"."+featTmp.getY()
			   +" Size="+featTmp.getWidth()+"."+featTmp.getHeight()+"\n";
		  }
		  tmp=tmp.getPrev();
		}

		xml+=	 "</features>"
			    +"<connectors>";
		
		//saving connectors
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

		//saving groups
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
			    +"<constraints>";

		//saving constraints
		for(JComponent constraint : startIncludesDots){
		  constrTmp=(ConstraintPanel)constraint;
		  endConstrTmp=(ConstraintPanel)constrTmp.getOtherEnd();
		  constControlPoint=constrTmp.getControlPoint();
		  if(constrTmp.getParent().getName().startsWith(featureNamePrefix)) 
			startOwner=constrTmp.getParent().getName();
		  else startOwner="";
		  if(endConstrTmp.getParent().getName().startsWith(featureNamePrefix))
			endOwner=endConstrTmp.getParent().getName();
		  else endOwner="";
		  
		  xml+="StartName="+constrTmp.getName()+" Loc="+constrTmp.getX()+"."+constrTmp.getY()+" StartOwner="+startOwner
			 +" EndName="+endConstrTmp.getName()+" Loc="+endConstrTmp.getX()+"."+endConstrTmp.getY()+" EndOwner="+endOwner
			 +" ControlName="+constControlPoint.getName()+" Loc="+constControlPoint.getX()+"."+constControlPoint.getY()+"\n";		  
		}		
		
		for(JComponent constraint : startExcludesDots){
		  constrTmp=(ConstraintPanel)constraint;
		  endConstrTmp=(ConstraintPanel)constrTmp.getOtherEnd();
		  constControlPoint=constrTmp.getControlPoint();
		  if(constrTmp.getParent().getName().startsWith(featureNamePrefix)) 
			startOwner=constrTmp.getParent().getName();
		  else startOwner="";
		  if(endConstrTmp.getParent().getName().startsWith(featureNamePrefix))
			endOwner=endConstrTmp.getParent().getName();
		  else endOwner="";
			  
		  xml+="StartName="+constrTmp.getName()+" Loc="+constrTmp.getX()+"."+constrTmp.getY()+" StartOwner="+startOwner
			 +" EndName="+endConstrTmp.getName()+" Loc="+endConstrTmp.getX()+"."+endConstrTmp.getY()+" EndOwner="+endOwner
			 +" ControlName="+constControlPoint.getName()+" Loc="+constControlPoint.getX()+"."+constControlPoint.getY()+"\n";		  
		}		
		
		//saving miscellaneous data, mainly counters
		xml+=	 "</constraints>"
			    +"<misc>"
				+"connectorsCount="+connectorsCount+" includesCount="+constraintsCount+" excludesCount="+constraintsCount
				+" constraintControlsCount="+constraintControlsCount+" altGroupsCount="+altGroupsCount
				+" orGroupsCount="+orGroupsCount+" featuresCount="+featuresCount
			    +"</misc>"
			    +"<startingCommonalities>";
		
		//saving starting commonalities
		for(String name : startingCommonalities){
		  xml+=name+"\t";
		}

		xml+=	 "</startingCommonalities>"
			    +"<startingVariabilities>";
		
		//saving starting variabilities
		for(String name : startingVariabilities){
		  xml+=name+"\t";
		}		

		xml+=	 "</startingVariabilities>"
				+"<featureColors>";

		//saving color associations
		colorIter = termsColor.entrySet().iterator();
		while(colorIter.hasNext()){
		  colorEntry=colorIter.next();
		  color=colorEntry.getValue();
		  xml+=colorEntry.getKey()+"\t"+color[0]+"-"+color[1]+"-"+color[2]+"\n";
		}
		
		xml+=	 "</featureColors>"
				+"<featureOccurrences>";
		
		//saving feature occurrences in files
		termIter=relevantTerms.entrySet().iterator();
		tmpLine=null;
		while(termIter.hasNext()){
		  termEntry=termIter.next();
				
		  tmpLine=termEntry.getKey()+"\t";
		  fileIter=termEntry.getValue().entrySet().iterator();
		  while(fileIter.hasNext()){
			fileEntry=fileIter.next();
				  
			tmpLine+="f: "+fileEntry.getKey()+" i: ";
			for(int[] index : fileEntry.getValue()) tmpLine+=index[0]+"-"+index[1]+" ";		
		  }
		  xml+=tmpLine+"\n";			
		}
		
		xml+=	 "</featureOccurrences>"
				+"<featureVersions>";
		
		//saving feature versions
		termVersionIter = relevantTermsVersions.entrySet().iterator();
		tmpLine=null;
		while(termVersionIter.hasNext()){
		  termVersionEntry=termVersionIter.next();
		  tmpLine=termVersionEntry.getKey();
			  
		  fileVersionIter=termVersionEntry.getValue().entrySet().iterator();
		  while(fileVersionIter.hasNext()){
			fileVersionEntry=fileVersionIter.next();
			
			tmpLine+="\tf:\t"+fileVersionEntry.getKey();
			for(String version : fileVersionEntry.getValue()) tmpLine+="\t"+version;
		  }
		  xml+=tmpLine+"\n";			
		}	  

		xml+=	 "</featureVersions>"
				+"</Diagram>";
		
		//saving xml string on file
		try{
		  //checking if the diagrams save directory must be created
		  File dir=new File(pathProject);		
		  if(!dir.isDirectory() && !dir.mkdirs() ) throw new IOException("Save Directory can't be created.");
			
		  PrintWriter pw1 = new PrintWriter(new BufferedWriter(new FileWriter(savePath)));
		  pw1.print(xml);
		  pw1.close();
			
		} 
		catch (IOException e){
		  System.out.println("Exception saveDiagram for save file "+savePath+" : " + e.getMessage());
		  e.printStackTrace();
		  return null;
		}
		return savePath;
	}

	/**
	 * Saves the visual elements of the diagram and the diagram state as XML String.
	 * @param s - the name of the file in which to save the diagram
	 * 
	 * @return - the XML String produced
	 */
	public String saveDiagramView(/*String pathProject,*/ /*String*/File s) {
		OrderedListNode tmp = null;
		String xml = null;
		FeaturePanel featTmp=null;
		AnchorPanel anchTmp=null;
		AnchorPanel endTmp=null;
		ConstraintPanel constrTmp=null;
		ConstraintPanel endConstrTmp=null;
		JComponent constControlPoint=null;
		String startOwner=null;
		String endOwner=null;
		Iterator<Entry<String, int[]>> colorIter = null;
		Entry<String, int[]> colorEntry=null;
		int[] color=null;
		Color featColor=null;

		Iterator<Entry<String, HashMap<String, ArrayList<int[]>>>> termIter = null;
		Entry<String, HashMap<String, ArrayList<int[]>>> termEntry = null;

		Iterator<Entry<String, ArrayList<int[]>>> fileIter = null;
		Entry<String, ArrayList<int[]>> fileEntry = null;
		
		Iterator<Entry<String, HashMap<String, ArrayList<String>>>> termVersionIter = null;
		Entry<String, HashMap<String, ArrayList<String>>> termVersionEntry = null;

		Iterator<Entry<String, ArrayList<String>>> fileVersionIter = null;
		Entry<String, ArrayList<String>> fileVersionEntry = null;
		
		String tmpLine=null;
		
		
		//saving diagram graphic elements data
		xml = "<Diagram name=\"" + s.getName() + "\">"
				+"<features>";

		//saving features
		tmp = visibleOrderDraggables.getLast();
		while(tmp!=null){
		  if(((JComponent)tmp.getElement()).getName().startsWith(featureNamePrefix)){
			featTmp = (FeaturePanel)tmp.getElement();
			featColor = featTmp.getBackground();

			xml+="Name="+featTmp.getLabelName()
			   +" ContName="+featTmp.getID()
			   +" Color="+featColor.getRed()+"-"+featColor.getGreen()+"-"+featColor.getBlue()
			   +" Loc="+featTmp.getX()+"."+featTmp.getY()
			   +" Size="+featTmp.getWidth()+"."+featTmp.getHeight()+"\n";
		  }
		  tmp=tmp.getPrev();
		}

		xml+=	 "</features>"
			    +"<connectors>";
		
		//saving connectors
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

		//saving groups
		System.out.println("saving groups: altGroupPanels.size()= "+altGroupPanels.size());
		System.out.println("saving groups: orGroupPanels.size()= "+orGroupPanels.size());
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
			    +"<constraints>";

		//saving constraints
		for(JComponent constraint : startIncludesDots){
		  constrTmp=(ConstraintPanel)constraint;
		  endConstrTmp=(ConstraintPanel)constrTmp.getOtherEnd();
		  constControlPoint=constrTmp.getControlPoint();
		  if(constrTmp.getParent().getName().startsWith(featureNamePrefix)) 
			startOwner=constrTmp.getParent().getName();
		  else startOwner="";
		  if(endConstrTmp.getParent().getName().startsWith(featureNamePrefix))
			endOwner=endConstrTmp.getParent().getName();
		  else endOwner="";
		  
		  xml+="StartName="+constrTmp.getName()+" Loc="+constrTmp.getX()+"."+constrTmp.getY()+" StartOwner="+startOwner
			 +" EndName="+endConstrTmp.getName()+" Loc="+endConstrTmp.getX()+"."+endConstrTmp.getY()+" EndOwner="+endOwner
			 +" ControlName="+constControlPoint.getName()+" Loc="+constControlPoint.getX()+"."+constControlPoint.getY()+"\n";		  
		}		
		
		for(JComponent constraint : startExcludesDots){
		  constrTmp=(ConstraintPanel)constraint;
		  endConstrTmp=(ConstraintPanel)constrTmp.getOtherEnd();
		  constControlPoint=constrTmp.getControlPoint();
		  if(constrTmp.getParent().getName().startsWith(featureNamePrefix)) 
			startOwner=constrTmp.getParent().getName();
		  else startOwner="";
		  if(endConstrTmp.getParent().getName().startsWith(featureNamePrefix))
			endOwner=endConstrTmp.getParent().getName();
		  else endOwner="";
			  
		  xml+="StartName="+constrTmp.getName()+" Loc="+constrTmp.getX()+"."+constrTmp.getY()+" StartOwner="+startOwner
			 +" EndName="+endConstrTmp.getName()+" Loc="+endConstrTmp.getX()+"."+endConstrTmp.getY()+" EndOwner="+endOwner
			 +" ControlName="+constControlPoint.getName()+" Loc="+constControlPoint.getX()+"."+constControlPoint.getY()+"\n";		  
		}		
		
		//saving miscellaneous data, mainly counters
		xml+=	 "</constraints>"
			    +"<misc>"
				+"connectorsCount="+connectorsCount+" includesCount="+constraintsCount+" excludesCount="+constraintsCount
				+" constraintControlsCount="+constraintControlsCount+" altGroupsCount="+altGroupsCount
				+" orGroupsCount="+orGroupsCount+" featuresCount="+featuresCount
			    +"</misc>"
			    +"<startingCommonalities>";
		
		//saving starting commonalities
		for(String name : startingCommonalities){
		  xml+=name+"\t";
		}

		xml+=	 "</startingCommonalities>"
			    +"<startingVariabilities>";
		
		//saving starting variabilities
		for(String name : startingVariabilities){
		  xml+=name+"\t";
		}		

		xml+=	 "</startingVariabilities>"
				+"<featureColors>";

		//saving color associations
		colorIter = termsColor.entrySet().iterator();
		while(colorIter.hasNext()){
		  colorEntry=colorIter.next();
		  color=colorEntry.getValue();
		  xml+=colorEntry.getKey()+"\t"+color[0]+"-"+color[1]+"-"+color[2]+"\n";
		}
		
		xml+=	 "</featureColors>"
				+"<featureOccurrences>";
		
		//saving feature occurrences in files
		termIter=relevantTerms.entrySet().iterator();
		tmpLine=null;
		while(termIter.hasNext()){
		  termEntry=termIter.next();
				
		  tmpLine=termEntry.getKey()+"\t";
		  fileIter=termEntry.getValue().entrySet().iterator();
		  while(fileIter.hasNext()){
			fileEntry=fileIter.next();
				  
			tmpLine+="f: "+fileEntry.getKey()+" i: ";
			for(int[] index : fileEntry.getValue()) tmpLine+=index[0]+"-"+index[1]+" ";		
		  }
		  xml+=tmpLine+"\n";			
		}
		
		xml+=	 "</featureOccurrences>"
				+"<featureVersions>";
		
		//saving feature versions
		termVersionIter = relevantTermsVersions.entrySet().iterator();
		tmpLine=null;
		while(termVersionIter.hasNext()){
		  termVersionEntry=termVersionIter.next();
		  tmpLine=termVersionEntry.getKey();
			  
		  fileVersionIter=termVersionEntry.getValue().entrySet().iterator();
		  while(fileVersionIter.hasNext()){
			fileVersionEntry=fileVersionIter.next();
			
			tmpLine+="\tf:\t"+fileVersionEntry.getKey();
			for(String version : fileVersionEntry.getValue()) tmpLine+="\t"+version;
		  }
		  xml+=tmpLine+"\n";			
		}	  

		xml+=	 "</featureVersions>"
				+"</Diagram>";
		
		return xml;
	}

	/**
	 * Loads a saved feature diagram from a file describing the graphic elements.
	 * 
	 * @param diagramDataPath - the file to load from
	 */
	public void loadSavedDiagram(String diagramDataPath) {
	  SAXParser saxParser = null;
	  InputStream stream = null;
	  SAXParserFactory saxFactory = SAXParserFactory.newInstance();
	  ViewXMLHandler xmlHandler = new ViewXMLHandler();
	  try {
		stream=new FileInputStream(diagramDataPath);

		/* ***DEBUG*** */
		if(debug2) System.out.println("EditorView: *** PARSING: "+diagramDataPath+" ***");
		/* ***DEBUG*** */

		saxParser = saxFactory.newSAXParser();
		saxParser.parse(stream, xmlHandler);

		/* ***DEBUG*** */
		if(debug2) System.out.println("\nResult of parsing:\n"
				+"Features:\n"+xmlHandler.featuresList
				+"\nConnectors:\n"+xmlHandler.connectorsList
				+"\nGroups:\n"+xmlHandler.groupsList
				+"\nConstraintsList:\n"+xmlHandler.constraintsList
				+"\nMisc:\n"+xmlHandler.misc
				+"\nStarting Commonalities:\n"+xmlHandler.startingComm
				+"\nStarting Variabilities:\n"+xmlHandler.startingVars
				+"\nFeatures Colors:\n"+xmlHandler.featureColors
				+"\nFeatures Occurrences:\n"+xmlHandler.featureOccurrences
				+"\nFeatures Versions:\n"+xmlHandler.featureVersions
				+"\n");
		/* ***DEBUG*** */

		if(xmlHandler.featureColors!=null) loadStartingTermsColor(xmlHandler.featureColors);
		if(xmlHandler.featureOccurrences!=null) loadStartingTermsOccurrences(xmlHandler.featureOccurrences);
		if(xmlHandler.featureVersions!=null) loadStartingTermsVersions(xmlHandler.featureVersions);
		if(xmlHandler.featuresList!=null) loadFeatures(xmlHandler.featuresList);
		if(xmlHandler.connectorsList!=null) loadConnectors(xmlHandler.connectorsList);
		if(xmlHandler.groupsList!=null) loadGroups(xmlHandler.groupsList);
		if(xmlHandler.constraintsList!=null) loadConstraints(xmlHandler.constraintsList);
		if(xmlHandler.misc!=null) loadMiscellaneous(xmlHandler.misc);
		if(xmlHandler.startingComm!=null) loadStartingCommonalities(xmlHandler.startingComm);
		if(xmlHandler.startingVars!=null) loadStartingVariabilities(xmlHandler.startingVars);

		//resizing diagram to fit all components
		fitDiagram();		
		//hiding control points after fitting
		for(JComponent constr : startIncludesDots) hideControlPoint(constr);
		for(JComponent constr : startExcludesDots) hideControlPoint(constr);
		frameRoot.repaint();
		
		
//		diagramPanel.remove(constControlPoint);
//		visibleOrderDraggables.remove(constControlPoint);
//		constControlPoint.setVisible(false);		  
		
	  } catch (Exception e) {
		e.printStackTrace(); 
		throw new RuntimeException("Error while loading saved diagram");
	  }
	  return;
	}

	/**
	 * Loads a saved feature diagram from a file describing the graphic elements.
	 * 
	 * @param diagramDataPath - the file to load from
	 */
	public void loadSavedDiagram2(FDEXMLHandler xmlHandler) {
	  try {

		/* ***DEBUG*** */
		if(debug2) System.out.println("\n(loadSavedDiagram2)Result of parsing:\n"
				+"Features:\n"+xmlHandler.featuresList
				+"\nConnectors:\n"+xmlHandler.connectorsList
				+"\nGroups:\n"+xmlHandler.groupsList
				+"\nConstraintsList:\n"+xmlHandler.constraintsList
				+"\nMisc:\n"+xmlHandler.misc
				+"\nStarting Commonalities:\n"+xmlHandler.startingComm
				+"\nStarting Variabilities:\n"+xmlHandler.startingVars
				+"\nFeatures Colors:\n"+xmlHandler.featureColors
				+"\nFeatures Occurrences:\n"+xmlHandler.featureOccurrences
				+"\nFeatures Versions:\n"+xmlHandler.featureVersions
				+"\n");
		/* ***DEBUG*** */

		if(xmlHandler.featureColors!=null) loadStartingTermsColor(xmlHandler.featureColors);
		if(xmlHandler.featureOccurrences!=null) loadStartingTermsOccurrences(xmlHandler.featureOccurrences);
		if(xmlHandler.featureVersions!=null) loadStartingTermsVersions(xmlHandler.featureVersions);
		if(xmlHandler.featuresList!=null) loadFeatures(xmlHandler.featuresList);
		if(xmlHandler.connectorsList!=null) loadConnectors(xmlHandler.connectorsList);
		if(xmlHandler.groupsList!=null) loadGroups(xmlHandler.groupsList);
		if(xmlHandler.constraintsList!=null) loadConstraints(xmlHandler.constraintsList);
		if(xmlHandler.misc!=null) loadMiscellaneous(xmlHandler.misc);
		if(xmlHandler.startingComm!=null) loadStartingCommonalities(xmlHandler.startingComm);
		if(xmlHandler.startingVars!=null) loadStartingVariabilities(xmlHandler.startingVars);

		//resizing diagram to fit all components
		fitDiagram();		
		//hiding control points after fitting
		for(JComponent constr : startIncludesDots) hideControlPoint(constr);
		for(JComponent constr : startExcludesDots) hideControlPoint(constr);
		frameRoot.repaint();		  
		
	  } catch (Exception e) {
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
	  String[] rgbValues=null;
	  int[] rgbIntValues=null;
	  Color featureColor=null;
	  
	  for(String feature : features){
		System.out.println("feature: "+feature);
		//getting data of this feature
		featureData=feature.split(" ");

		//getting feature name
		featureName=featureData[0].substring(5);
		for(int k=1; k<featureData.length-4; ++k) featureName+=" "+featureData[k];

		//getting feature ID
		System.out.println("featureData.length: "+featureData.length);
		containerName=featureData[featureData.length-4].substring(9);
		
		//getting RGB color as a String
		rgbValues=featureData[featureData.length-3].split("-");		
		rgbIntValues=new int[3];
		rgbIntValues[0]=new Integer(rgbValues[0].substring(6));
		rgbIntValues[1]=new Integer(rgbValues[1]);
		rgbIntValues[2]=new Integer(rgbValues[2]);		
		featureColor=getNewColor(rgbIntValues);		

		//getting feature location in the diagram
		for (i=4; i<featureData[featureData.length-2].length(); ++i)
			if (featureData[featureData.length-2].charAt(i)=='.') break;

		x=Integer.valueOf(featureData[featureData.length-2].substring(4, i));
		y=Integer.valueOf(featureData[featureData.length-2].substring(i+1));

		for (i=5; i<featureData[featureData.length-1].length(); ++i) 
		    if (featureData[featureData.length-1].charAt(i)=='.') break;
		
		width=Integer.valueOf(featureData[featureData.length-1].substring(5, i));
		height=Integer.valueOf(featureData[featureData.length-1].substring(i+1));
		
		//building feature panel
		directlyAddFeatureToDiagram(featureName, featureNamePrefix+containerName, x, y, width, height, featureColor);
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
		
		/* ***DEBUG*** */
		if(debug2){
			System.out.println("Printing connectors strings:");
			for (String s : connectorData) System.out.println("s: "+s);
			System.out.println("startConnectorName="+connectorData[0].substring(10));
			System.out.println("startOwnerName="+connectorData[2].substring(11));
			System.out.println("endConnectorName="+connectorData[3].substring(8));
			System.out.println("endOwnerName="+connectorData[5].substring(9));
		}
		/* ***DEBUG*** */

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
		startConnector=(AnchorPanel)buildConnectionDot(ItemsType.START_MANDATORY_CONNECTOR, startConnectorName, startX, startY);
		if(startOwnerName.length()==0){//adding connector to the diagram panel directly
		  visibleOrderDraggables.addToTop(startConnector);
		  addToDiagramOnTop(startConnector);			
		}
		else{//adding connector to its owner feature panel
		  OrderedListNode tmp=visibleOrderDraggables.getFirst();
		  while(tmp!=null){
			System.out.println("((JComponent)tmp.getElement()).getName(): "+((JComponent)tmp.getElement()).getName()
					+"\nstartOwnerName: "+startOwnerName);
			if(((JComponent)tmp.getElement()).getName().compareTo(startOwnerName)==0){
			  owner=(FeaturePanel)tmp.getElement(); break;
			}
			tmp=tmp.getNext();
		  }
		  if(owner==null)
			throw new RuntimeException("Couldn't find feature '"+startOwnerName+"' as owner of '"+startConnectorName+"'");

		  visibleOrderDraggables.addToTop(startConnector);
		  addToComponentOnLayer(owner, startConnector, 0);
		}
		
		
		//adding end connector
		if(endConnectorName.contains("OPTIONAL"))
			endConnector=(AnchorPanel)buildConnectionDot(ItemsType.END_OPTIONAL_CONNECTOR, endConnectorName, endX, endY);
		if(endConnectorName.contains("MANDATORY")) 
			endConnector=(AnchorPanel)buildConnectionDot(ItemsType.END_MANDATORY_CONNECTOR, endConnectorName, endX, endY);
		if(endOwnerName.length()==0){//adding connector to the diagram panel directly
		  visibleOrderDraggables.addToTop(endConnector);
		  addToDiagramOnTop(endConnector);			
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
		  addToComponentOnLayer(owner, endConnector, 0);
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
		if(groupOwnerName.compareTo("")==0){//adding connector to the diagram panel directly
		  visibleOrderDraggables.addToTop(group);
		  addToDiagramOnTop(group);
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

		  directlyAddGroupToFeature(group, owner, groupType);
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
		  member=(AnchorPanel)buildConnectionDot(ItemsType.END_MANDATORY_CONNECTOR, memberName, memberX, memberY);
		  if(memberOwnerName.compareTo("")==0){//adding member to the diagram panel directly
			visibleOrderDraggables.addToTop(member);
			addToDiagramOnTop(member);			
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
			addToComponentOnLayer(owner, member, 0);
		  }	

		  //adding mutual references
		  member.setOtherEnd(group);
		  group.getMembers().add(member);
		}

//		//adding group to draw list
//		if(groupType==ItemsType.ALT_GROUP_START_CONNECTOR) altGroupPanels.add(group);
//		else orGroupPanels.add(group);
	  }	  

	}
	
	/**
	 * Adds to the diagram panel all the saved constraints described in the list.
	 * 
	 * @param constraintsList - String describing the constraints to load, one per line
	 */
	private void loadConstraints(String constraintsList) {
	  String[] constraintData=null;
	  String startConstraintName=null;
	  String startOwnerName=null;
	  int startX=0, startY=0;	  
	  String endConstraintName=null;
	  String endOwnerName=null;
	  int endX=0, endY=0;
	  String controlName=null;
	  int controlX=0, controlY=0;
	  int i=0;
	  String[] constraints=constraintsList.split("\n");
	  ConstraintPanel startConstraint=null, endConstraint=null;
	  JComponent constControlPoint=null;
	  FeaturePanel owner=null;
	  
	  for(String constraint : constraints){
		constraintData=constraint.split(" ");

		/* ***DEBUG*** */
//		if(debug2){
			System.out.println("Printing constraint strings:");
			for (String s : constraintData) System.out.println("s: "+s);
			System.out.println("startConstraintName="+constraintData[0].substring(10));
			System.out.println("startOwnerName="+constraintData[2].substring(11));
			System.out.println("endConstraintName="+constraintData[3].substring(8));
			System.out.println("endOwnerName="+constraintData[5].substring(9));
			System.out.println("controlName="+constraintData[6].substring(12));
//		}
		/* ***DEBUG*** */

		//getting data of start anchor of this constraint
		startConstraintName=constraintData[0].substring(10);

		for (i=4; i<constraintData[1].length(); ++i) if (constraintData[1].charAt(i)=='.') break;
		startX=Integer.valueOf(constraintData[1].substring(4, i));
		startY=Integer.valueOf(constraintData[1].substring(i+1));

		startOwnerName=constraintData[2].substring(11);

		//getting data of end anchor of this constraint
		endConstraintName=constraintData[3].substring(8);

		for (i=4; i<constraintData[4].length(); ++i) if (constraintData[4].charAt(i)=='.') break;
		endX=Integer.valueOf(constraintData[4].substring(4, i));
		endY=Integer.valueOf(constraintData[4].substring(i+1));

		endOwnerName=constraintData[5].substring(9);		
		
		//getting data of control point of this constraint
		controlName=constraintData[6].substring(12);

		for (i=4; i<constraintData[7].length(); ++i) if (constraintData[7].charAt(i)=='.') break;
		
		controlX=Integer.valueOf(constraintData[7].substring(4, i));
		controlY=Integer.valueOf(constraintData[7].substring(i+1));		
		
		//adding constraint start anchor
		startConstraint=(ConstraintPanel)buildConnectionDot(
				startConstraintName.startsWith(startExcludesNamePrefix) ? 
				ItemsType.START_EXCLUDES_DOT : ItemsType.START_INCLUDES_DOT,
				startConstraintName, startX, startY);
		
		if(startOwnerName.length()==0){//adding anchor to the diagram panel directly
		  visibleOrderDraggables.addToTop(startConstraint);
		  addToDiagramOnTop(startConstraint);
		}
		else{//adding anchor to its owner feature panel
		  OrderedListNode tmp=visibleOrderDraggables.getFirst();
		  while(tmp!=null){
			if(((JComponent)tmp.getElement()).getName().compareTo(startOwnerName)==0){
			  owner=(FeaturePanel)tmp.getElement(); break;
			}
			tmp=tmp.getNext();
		  }
		  if(owner==null)
			throw new RuntimeException("Couldn't find feature '"+startOwnerName+"' as owner of '"+startConstraintName+"'");

		  visibleOrderDraggables.addToTop(startConstraint);
		  addToComponentOnLayer(owner, startConstraint, 0);
		}

		//adding constraint end anchor
		endConstraint=(ConstraintPanel)buildConnectionDot(
				endConstraintName.startsWith(endExcludesNamePrefix) ? 
				ItemsType.END_EXCLUDES_DOT : ItemsType.END_INCLUDES_DOT,
				endConstraintName, endX, endY);
		
		if(endOwnerName.length()==0){//adding anchor to the diagram panel directly
		  visibleOrderDraggables.addToTop(endConstraint);
		  addToDiagramOnTop(endConstraint);
		}
		else{//adding anchor to its owner feature panel
		  OrderedListNode tmp=visibleOrderDraggables.getFirst();
		  while(tmp!=null){
			if(((JComponent)tmp.getElement()).getName().compareTo(endOwnerName)==0){
			  owner=(FeaturePanel)tmp.getElement(); break;
			}
			tmp=tmp.getNext();
		  }
		  if(owner==null)
			throw new RuntimeException("Couldn't find feature '"+endOwnerName+"' as owner of '"+endConstraintName);

		  visibleOrderDraggables.addToTop(endConstraint);
		  addToComponentOnLayer(owner, endConstraint, 0);
		}	
		
		//adding constraint control point
		constControlPoint=buildConnectionDot(ItemsType.CONSTRAINT_CONTROL_POINT, controlName, controlX, controlY);
		
		//adding anchor to the diagram panel directly
		constControlPoint.setLocation(controlX, controlY);
		visibleOrderDraggables.addToTop(constControlPoint);
		addToDiagramOnTop(constControlPoint);
	    constControlPoint.setVisible(true);
		
	    //setting other ends of constraint dots
	    startConstraint.setOtherEnd(endConstraint);
	    startConstraint.setControlPoint(constControlPoint);
	    endConstraint.setOtherEnd(startConstraint);
	    endConstraint.setControlPoint(constControlPoint);
//	    constControlPoint.setVisible(false);
	    
		//adding constraint to draw list
		addConstraintToDrawLists(startConstraint, (startConstraintName.startsWith(startIncludesNamePrefix)) ?
			ItemsType.START_INCLUDES_DOT : ItemsType.START_EXCLUDES_DOT);	
		
		++constraintsCount;
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
	  constraintsCount=Integer.valueOf(values[1].substring(14));
	  constraintsCount=Integer.valueOf(values[2].substring(14));
	  constraintControlsCount=Integer.valueOf(values[3].substring(24));
	  altGroupsCount=Integer.valueOf(values[4].substring(15));
	  orGroupsCount=Integer.valueOf(values[5].substring(14));
	  featuresCount=Integer.valueOf(values[6].substring(14));
	}

	/**
	 * Loads the list of starting commonalities.
	 * 
	 * @param startingComm - String containing the names of starting commonalities, separated by a singol blank
	 */
	private void loadStartingCommonalities(String startingComm) {
	  String[] commonalityNames=startingComm.split("\t");
	  for(String name : commonalityNames) startingCommonalities.add(name);
	}

	/**
	 * Loads the list of starting variabilities.
	 * 
	 * @param startingVars - String containing the names of starting variabilities, separated by a singol blank
	 */
	private void loadStartingVariabilities(String startingVars) {
	  String[] variabilityNames=startingVars.split("\t");
	  for(String name : variabilityNames) startingVariabilities.add(name);
	}

	/**
	 * Loads the colors of starting features.
	 * 
	 * @param featureColors - String containing the colors associated with starting features, one per line
	 */
	private void loadStartingTermsColor(String featureColors) {
	  String[] colorLines=featureColors.split("\n");
	  String[] lineElements=null;
	  String featureName=null;
	  String[] rgbValues=null;
	  int[] rgbIntValues=null;

	  termsColor=new HashMap<String, int[]>();
	  
	  for(String colorLine : colorLines){
		lineElements=colorLine.split("\t");
		//getting feature name
		featureName=lineElements[0];
		for(int i=1; i<lineElements.length-1; ++i) featureName+=lineElements[i];
		//getting RGB values as String
		rgbValues=lineElements[lineElements.length-1].split("-");
		
		/* ***DEBUG*** */
		if(debug2) System.out.println("featureName: "+featureName+", rgbValues: "+lineElements[lineElements.length-1]);
		/* ***DEBUG*** */

		rgbIntValues=new int[3];
		rgbIntValues[0]=new Integer(rgbValues[0]);
		rgbIntValues[1]=new Integer(rgbValues[1]);
		rgbIntValues[2]=new Integer(rgbValues[2]);
		termsColor.put(featureName, rgbIntValues);
	  }
	  
	}
	
	/**
	 * Loads the different versions of starting features, as they appear in input files.
	 * 
	 * @param featureVersions - String containing the versions of starting features in input files, one feature per line
	 */
	private void loadStartingTermsVersions(String featureVersions) {
		String[] versionLines=featureVersions.split("\n");
		String[] strArr=null;
		String termName=null, fileName=null;
		relevantTermsVersions=new HashMap<String, HashMap<String, ArrayList<String>>>();
		HashMap<String, ArrayList<String>> filesVersionsMap=null;
		ArrayList<String> versions=null;
		
		for(String versionLine : versionLines){
		  filesVersionsMap = new HashMap<String, ArrayList<String>>();

		  strArr=versionLine.split("\t");
		  termName=strArr[0];

		  for(int i=1;i<strArr.length; ++i){
			//a new file name has been found
			if(strArr[i].compareTo("f:")==0){ 
			  versions=new ArrayList<String>();
			  
			  ++i; fileName=strArr[i]; ++i;
			  for(; i<strArr.length; ++i){
				if(strArr[i].compareTo("f:")==0){
				  filesVersionsMap.put(fileName, versions);
				  --i; break;
				}				
				else versions.add(strArr[i]);
			  }

			}

		  }
		  //adding last association file-versions
		  filesVersionsMap.put(fileName, versions);
		  relevantTermsVersions.put(termName, filesVersionsMap);

		}
		
	}
	
	/**
	 * Loads the occurrences of starting features in input files.
	 * 
	 * @param featureOccurrences - String containing the occurrences for each starting feature and file, one feature per line
	 * @throws InvalidFormatException  - if the string format is invalid
	 */
	private void loadStartingTermsOccurrences(String featureOccurrences) throws InvalidFormatException {
	  String[] occurrenceLines=featureOccurrences.split("\n");
	  String termName=null;
	  String fileName=null;
	  String[] tokens=null;
	  HashMap<String, ArrayList<int[]>> fileMap=null;
	  ArrayList<int[]> occurrences=null;
	  int[] occurrIndexes=null;
	  String[] occurrStringIndexes=null;

	  relevantTerms=new HashMap<String, HashMap<String,ArrayList<int[]>>>();

	  for(String occurrenceLine : occurrenceLines){
		fileMap=new HashMap<String, ArrayList<int[]>>();
		tokens=occurrenceLine.split("\t");

		if(tokens.length!=2) throw new InvalidFormatException("Uncorrect format for relevant terms string");

		termName=tokens[0];
		tokens=tokens[1].split(" ");

		/* ***DEBUG*** */
		if(debug2){
		  System.out.println("Found Term: "+termName);
		  System.out.println("Printing tokens!");
		  for(String str:tokens) System.out.println(str);
		}
		/* ***DEBUG*** */

		for(int i=0;i<tokens.length; ++i){
		  //a new file name has been found
		  if(tokens[i].compareTo("f:")==0){ 
			occurrences=new ArrayList<int[]>();

			++i; fileName=tokens[i]; ++i;
			while(tokens[i].compareTo("i:")!=0){ fileName+=" "+tokens[i]; ++i;}

			/* ***DEBUG*** */
			if(debug2) System.out.println("\tFound file: "+fileName);
			/* ***DEBUG*** */

			if(tokens[i].compareTo("i:")!=0) throw new InvalidFormatException("Uncorrect format for relevant terms string");			  
			else ++i;

			//loading occurrence indexes of this file
			for(; i<tokens.length; ++i){

			  /* ***DEBUG*** */
			  if(debug2) System.out.println("***Token: "+tokens[i]);
			  /* ***DEBUG*** */

			  if(tokens[i].compareTo("f:")==0){ --i; break;}

			  occurrStringIndexes=tokens[i].split("-");
			  occurrIndexes=new int[2];
			  occurrIndexes[0]=Integer.valueOf(occurrStringIndexes[0]);
			  occurrIndexes[1]=Integer.valueOf(occurrStringIndexes[1]);
			  occurrences.add(occurrIndexes);

			  /* ***DEBUG*** */
			  if(debug2) System.out.println("\t\tFound occurrence: "+tokens[i]);
			  /* ***DEBUG*** */

			}
			fileMap.put(fileName, occurrences);			  
		  }

		}
		relevantTerms.put(termName, fileMap);
	  }

	}

	/**
	 * Shows on the diagram panel the control point of constraint.
	 * 
	 * @param constraint - one of the two ConstraintPanels owner of the control point
	 */
	public void showControlPoint(ConstraintPanel constraint) {
	  JComponent controlPoint = constraint.getControlPoint();
	  visibleOrderDraggables.addToTop(controlPoint);
	  addToDiagramOnTop(controlPoint);
	  controlPoint.setVisible(true);
	  frameRoot.repaint();
	}

	/**
	 * Removes from the diagram panel the control point of constraint.
	 * 
	 * @param constraint - the control point or one of its two ConstraintPanels
	 */
	public void hideControlPoint(JComponent constraint) {
	  if(constraint.getName().startsWith(constraintControlPointNamePrefix)){
		diagramPanel.remove(constraint);
		visibleOrderDraggables.remove(constraint);
		constraint.setVisible(false);		  
	  }
	  else{
		JComponent controlPoint = ((ConstraintPanel)constraint).getControlPoint();
		diagramPanel.remove(controlPoint);
		visibleOrderDraggables.remove(controlPoint);
		controlPoint.setVisible(false);
	  }
	  frameRoot.repaint();
	}
	
	/** 
	 * Returns a tab for commonalities candidates using JCheckBox to add terms.
	 * 
	 * @param featuresTyped - ArrayList containing not extracted features
	 * @param commonalitiesExtracted - ArrayList containing starting extracted commonalities
	 * @param variabilitiesExtracted - ArrayList containing starting extracted variabilities
	 * 
	 * @return - the JScrollPane created
	 */
	protected JSplitPane getTabFeaturesCandidates(){
		JPanel featuresPanel = null, legendPanel=null;
		
		JPanel occurrencesPanel = null;
		JPanel termsPanel = null;

		JLabel passiveCommColorLabel=null, passiveVarsColorLabel=null, nonExtractedColorLabel=null;
		JLabel activeCommColorLabel=null, activeVarsColorLabel=null;

		JLabel passiveCommTextLabel=null, passiveVarsTextLabel=null, nonExtractedTextLabel=null;
		JLabel activeCommTextLabel=null, activeVarsTextLabel=null;

		
		ImageIcon iconSearch = new ImageIcon(getClass().getResource("/Search/magnifier glasses-min3.png"));
		ImageIcon iconNoSearch = new ImageIcon(getClass().getResource("/Search/magnifier glasses_NO_SEARCH.png"));
		JLabel iconLabel = null;
//		int[] colorRGB=new int[3];
		Color commBackColor=null, varsBackColor=null, nonExtrBackColor=null;
		ArrayList<String> commonalitiesToHighlight = new ArrayList<String>();//commonalities to highlight in the texts
		ArrayList<String> variabilitiesToHighlight = new ArrayList<String>();//variabilities to highlight in the texts

		ArrayList<String> commonalitiesExtracted = new ArrayList<String>();//starting extracted commonalities		
    	ArrayList<String> variabilitiesExtracted = new ArrayList<String>();//starting extracted variabilities		
    	ArrayList<String> featuresTyped = new ArrayList<String>();//ArrayList containing not extracted features
    	String featureName=null;

		//building features lists
    	if(selectionGroupFocused.size()>0){
    	  for(JComponent groupMember : selectionGroupFocused){
    		if(!groupMember.getName().startsWith(featureNamePrefix)) continue;//element is not a feature
    		
    		//getting feature name
    		featureName=((FeaturePanel)groupMember).getLabelName();
    		if (menuViewCommsOrVars.isSelected()) featureName=featureName.substring(0, featureName.length()-4);

    		//getting the type of the feature: extracted commonality, extracted variability or added by the user
    		if(relevantTerms.get(featureName)==null) featuresTyped.add(featureName);//a non-extracted feature
    		else{
    	      for(String tmp : startingCommonalities) if(tmp.compareTo(featureName)==0){
    	    	commonalitiesExtracted.add(featureName);
    	    	featureName=null; break;
    	      }
    	      if(featureName!=null)//feature still not found
    	    	for(String tmp : startingVariabilities) if(tmp.compareTo(featureName)==0){
    	    	  variabilitiesExtracted.add(featureName); break;
      	      	}
    		}
    	  }
    	  getSelectionGroup().clear();//clearing selection group
    	}
    	else{//search asked on a single feature
    	  featureName=((FeaturePanel)getPopUpElement()).getLabelName();
    	  if (menuViewCommsOrVars.isSelected()) featureName=featureName.substring(0, featureName.length()-4);

    	  //getting the type of the feature: extracted commonality, extracted variability or added by the user
    	  if(relevantTerms.get(featureName)==null) featuresTyped.add(featureName);//a non-extracted feature
    	  else{
    		for(String tmp : startingCommonalities) if(tmp.compareTo(featureName)==0){
  	    	  commonalitiesExtracted.add(featureName);
  	    	  featureName=null; break;
    		}
    		if(featureName!=null)//feature still not found
  	    	  for(String tmp : startingVariabilities) if(tmp.compareTo(featureName)==0){
  	    	    variabilitiesExtracted.add(featureName); break;
  	    	  }
    	  }
    	}
    	
    	/* ***DEBUG*** */
    	if(debug5){
    	  System.out.println("commonalitiesExtracted: ");
    	  for(String tmp : commonalitiesExtracted) System.out.println(tmp);

    	  System.out.println("variabilitiesExtracted: ");
    	  for(String tmp : variabilitiesExtracted) System.out.println(tmp);

    	  System.out.println("featuresTyped: ");
    	  for(String tmp : featuresTyped) System.out.println(tmp);    		
    	}
    	/* ***DEBUG*** */


		//creating list of features to highlight in the text
		for(String tmp: commonalitiesExtracted) commonalitiesToHighlight.add(tmp);
		for(String tmp: variabilitiesExtracted) variabilitiesToHighlight.add(tmp);
		
		//creating panel elements
		searchPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//		searchPanel.setLayout(new BorderLayout());
		
		occurrencesPanel = new JPanel();
		occurrencesPanel.setLayout(new BorderLayout());
		termsPanel = new JPanel();
		termsPanel.setLayout(new BorderLayout());
		
		searchPanel.setBackground(Color.WHITE);
		searchPanel.setPreferredSize(searchFrame.getPreferredSize());
		searchPanel.setContinuousLayout(true);
		searchPanel.setDividerSize(5);
		searchPanel.setResizeWeight(0.);
//		searchPanel.setOpaque(true);
//		searchPanel.setBounds(0, 0, 900, 700);
//		searchPanel.setLayout(null);
		
		featuresPanel = new JPanel();
		featuresPanel.setBackground(Color.WHITE);
//		panelFeatures.setOpaque(true);
		featuresPanel.setBounds(5,5,770,200);
		featuresPanel.setLayout(new GridLayout(0, 1));
		featuresPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		//creating panelFeatures
		//getting color for extracted commonalities
		commBackColor=getNewColor(PCcol);
		for(int i = 0; i < commonalitiesExtracted.size(); i++){
		  iconLabel = new JLabel(commonalitiesExtracted.get(i), iconSearch, JLabel.LEFT);
		  iconLabel.setOpaque(true);
		  iconLabel.setBackground(commBackColor);

		  iconLabel.addMouseListener(getTermSearchIconListener("Extracted", commonalitiesExtracted.get(i), 
				  					 commonalitiesToHighlight, variabilitiesToHighlight));
		  featuresPanel.add(iconLabel);
		}
		
		//getting color for extracted variabilities
		varsBackColor=getNewColor(PVcol);
		for(int i = 0; i < variabilitiesExtracted.size(); i++){
		  iconLabel = new JLabel(variabilitiesExtracted.get(i), iconSearch, JLabel.LEFT);
		  iconLabel.setOpaque(true);
		  iconLabel.setBackground(varsBackColor);

		  iconLabel.addMouseListener(getTermSearchIconListener("Extracted", variabilitiesExtracted.get(i), 
				  					 commonalitiesToHighlight, variabilitiesToHighlight));
		  featuresPanel.add(iconLabel);
		}

		//getting color for not extracted features
		nonExtrBackColor=getNewColor(NEcol);
		for(int i = 0; i < featuresTyped.size(); i++){
		  iconLabel = new JLabel(featuresTyped.get(i), iconNoSearch, JLabel.LEFT);
		  iconLabel.setOpaque(true);
		  iconLabel.setBackground(nonExtrBackColor);
		  
		  featuresPanel.add(iconLabel);
		}
		
		//creating scrollingFeaturesPanel from panelFeatures
		JScrollPane scrollingFeaturesPanel = new JScrollPane(featuresPanel);
//		scrollingFeaturesPanel.setBounds(270, 10, 620, 210);
		
		//creating legend panel
		legendPanel = new JPanel();
//		legendPanel.setBounds(5, 10, 260, 210);
		legendPanel.setLayout(new GridLayout(5, 2, 2, 10));
		
		passiveCommColorLabel=new JLabel();
		passiveCommColorLabel.setBackground(commBackColor);
		passiveCommColorLabel.setBounds(15, 10, 30, 15);
		passiveCommColorLabel.setOpaque(true);
		passiveCommTextLabel=new JLabel(": commonalities"); 
		passiveCommTextLabel.setLocation(48, 10);
		passiveCommTextLabel.setSize(new Dimension(220, 15));
		passiveCommTextLabel.setFont(new Font("Dialog", Font.ITALIC|Font.BOLD, 12));
		
		passiveVarsColorLabel=new JLabel();
		passiveVarsColorLabel.setBackground(varsBackColor);
		passiveVarsColorLabel.setBounds(15, 40, 30, 15);
		passiveVarsColorLabel.setOpaque(true);
		passiveVarsTextLabel=new JLabel(": variabilities"); 
		passiveVarsTextLabel.setLocation(48, 40);
		passiveVarsTextLabel.setSize(new Dimension(220, 15));
		passiveVarsTextLabel.setFont(new Font("Dialog", Font.ITALIC|Font.BOLD, 12));
		
		nonExtractedColorLabel=new JLabel();
		nonExtractedColorLabel.setBackground(nonExtrBackColor);
		nonExtractedColorLabel.setBounds(15, 70, 30, 15);
		nonExtractedColorLabel.setOpaque(true);
		nonExtractedTextLabel=new JLabel(": non extracted features"); 
		nonExtractedTextLabel.setLocation(48, 70);
		nonExtractedTextLabel.setSize(new Dimension(220, 15));
		nonExtractedTextLabel.setFont(new Font("Dialog", Font.ITALIC|Font.BOLD, 12));

		activeCommColorLabel=new JLabel();
		activeCommColorLabel.setBackground(getNewColor(ACcol));
		activeCommColorLabel.setBounds(15, 100, 30, 15);
		activeCommColorLabel.setOpaque(true);
		activeCommTextLabel=new JLabel(": current commonality occurrence"); 
		activeCommTextLabel.setLocation(48, 100);
		activeCommTextLabel.setSize(new Dimension(220, 15));
		activeCommTextLabel.setFont(new Font("Dialog", Font.ITALIC|Font.BOLD, 12));
		
		activeVarsColorLabel=new JLabel();
		activeVarsColorLabel.setBackground(getNewColor(AVcol));
		activeVarsColorLabel.setBounds(15, 130, 30, 15);
		activeVarsColorLabel.setOpaque(true);
		activeVarsTextLabel=new JLabel(": current variability occurrence"); 
		activeVarsTextLabel.setLocation(48, 130);
		activeVarsTextLabel.setSize(new Dimension(220, 15));
		activeVarsTextLabel.setFont(new Font("Dialog", Font.ITALIC|Font.BOLD, 12));
		
		legendPanel.add(passiveCommColorLabel);
		legendPanel.add(passiveCommTextLabel);

		legendPanel.add(passiveVarsColorLabel);
		legendPanel.add(passiveVarsTextLabel);

		legendPanel.add(nonExtractedColorLabel);
		legendPanel.add(nonExtractedTextLabel);

		legendPanel.add(activeCommColorLabel);
		legendPanel.add(activeCommTextLabel);

		legendPanel.add(activeVarsColorLabel);		
		legendPanel.add(activeVarsTextLabel);
		
		legendPanel.setBackground(Color.LIGHT_GRAY);
		legendPanel.setOpaque(true);
		
		//adding control buttons and a label for term occurences navigation
		XBackwardOccurrButton = new JButton("<<("+occurrJumpSpan+")");
		XBackwardOccurrButton.setPreferredSize(new Dimension(76, 22));
		XBackwardOccurrButton.addActionListener(getOccurrNavButtonListener(-occurrJumpSpan));

		prevOccurrButton = new JButton("<");
		prevOccurrButton.setPreferredSize(new Dimension(76, 22));
		prevOccurrButton.addActionListener(getOccurrNavButtonListener(-1));
		
		occurrsLabel = new JLabel("<html><div style=\"text-align: center;\">" + "x/y" + "</html>");

		occurrsLabelPanel = new JPanel();
		occurrsLabelPanel.add(occurrsLabel);
		occurrsLabelPanel.setPreferredSize(new Dimension(150, 22));
		occurrsLabelPanel.setBackground(Color.LIGHT_GRAY);

		nextOccurrButton = new JButton(">");
		nextOccurrButton.setPreferredSize(new Dimension(76, 22));
		nextOccurrButton.addActionListener(getOccurrNavButtonListener(1));

		XForwardOccurrButton = new JButton(">>("+occurrJumpSpan+")");
		XForwardOccurrButton.setPreferredSize(new Dimension(76, 22));
		XForwardOccurrButton.addActionListener(getOccurrNavButtonListener(occurrJumpSpan));
		

		//adding text area for term occurences visualization		
		occursTabbedPane = new JTabbedPane();
		occursTabbedPane.setPreferredSize(new Dimension(880, 390));
		
		//adding buttons panel for term occurences navigation
		buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(occursTabbedPane.getPreferredSize().width, 40));
		buttonPanel.setLayout(null);
		
		//initializing utility maps
		textTabs = new HashMap<String, JTextArea>();
		textIndexes = new HashMap<String, HashMap<String, Integer>>();
		currentFiles = new HashMap<String, String>();
		lastHighlightedTag = new HashMap<String, HashMap<String, Object>>();
//		lastRemovedHighlights = new HashMap<String, HashMap<String, ArrayList<Highlight>>>();
		lastRemovedHighlights = new HashMap<String, HashMap<String, ArrayList<Entry<Highlight, ArrayList<Highlight>>>>>();

		//adding components to panel		
		termsPanel.add(legendPanel, BorderLayout.WEST);
		termsPanel.add(scrollingFeaturesPanel, BorderLayout.CENTER);
		termsPanel.setPreferredSize(
			new Dimension(searchPanel.getPreferredSize().width, searchPanel.getPreferredSize().height/3));		
//		searchPanel.add(legendPanel);
//		searchPanel.add(scrollingFeaturesPanel);
//		searchPanel.add(occursTabbedPane);
		occurrencesPanel.add(buttonPanel, BorderLayout.NORTH);
		occurrencesPanel.add(occursTabbedPane, BorderLayout.CENTER);
		occurrencesPanel.setPreferredSize(
				new Dimension(searchPanel.getPreferredSize().width, 2*searchPanel.getPreferredSize().height/3));
//		searchPanel.add(termsPanel, BorderLayout.NORTH);
//		searchPanel.add(occurrencesPanel, BorderLayout.CENTER);
		searchPanel.setLeftComponent(termsPanel);
		searchPanel.setRightComponent(occurrencesPanel);
		
		return searchPanel;
	}

	/**
	 * Returns a new ActionListener for features view buttons. The behaviour changes based on the parameter.
	 * 
	 * @param type - String representing the required behaviour type
	 * @param term - String representing the name of the feature candidate
	 * @param commonalitiesToHighlight - if not null, these terms will be highlighted with the commonalities color
	 * @param variabilitiesToHighlight - if not null, these terms will be highlighted with the variabilities color
	 * 
	 * @return - the new ActionListener
	 */
	private MouseListener getTermSearchIconListener(String type, final String term,
			final ArrayList<String> commonalitiesToHighlight, final ArrayList<String> variabilitiesToHighlight) {

	  if (type=="Extracted") return new MouseAdapter(){			

		@Override
		public void mouseClicked(MouseEvent me){				

		  //creation of occurrences navigation panel
		  occursTabbedPane.removeAll();
		  textTabs.clear();
		  HashMap<String, ArrayList<int[]>> filesListTmp = null;//files list for a term

		  Iterator<Entry<String, ArrayList<int[]>>> filesIterator = null;
		  Entry<String, ArrayList<int[]>> occurrencesList = null;

		  //variables used to remember last selected tab for each relevant term
		  Component[] compArrTmp = null;
		  String tabTitle = null;

		  currentSelectedFeatureName=term;

		  /* ***DEBUG*** */
		  if (debug) System.out.println("\nCLICCATO SU UN TERMINE!()\nTerm="+term);
		  /* ***DEBUG*** */

		  /* ***DEBUG*** */
		  if (debug) System.out.println("relevantTerms.get(term)="+relevantTerms.get(term));
		  /* ***DEBUG*** */

		  filesListTmp = relevantTerms.get(term);

		  /* ***DEBUG*** */
		  if (debug) System.out.println("filesListTmp.entrySet().iterator()="+filesListTmp.entrySet().iterator());
		  /* ***DEBUG*** */

		  filesIterator = filesListTmp.entrySet().iterator();


		  while (filesIterator.hasNext()) {//for each file a JScrollPane is added as tab

			occurrencesList = filesIterator.next();

			JScrollPane scrollingFilePanel = getRegisteredTabTextFile(term, 
					(String)occurrencesList.getKey(), commonalitiesToHighlight, variabilitiesToHighlight);

			occursTabbedPane.addTab((String)occurrencesList.getKey(), scrollingFilePanel);

			/* ***VERBOSE****/
			if (debug4){
			  System.out.println("\n***\nNumero di componenti di jScrP: "+scrollingFilePanel.getComponentCount());
			  for (int h=0; h<scrollingFilePanel.getComponentCount(); h++){
				System.out.println("Classe del componente "+h+": "+scrollingFilePanel.getComponent(h).getClass());
			  }
			  System.out.println("Classe del ViewPort: "+scrollingFilePanel.getViewport().getClass());
			}			  
			/* ***VERBOSE****/

		  }

		  //adding occurences navigation controls
		  if(buttonPanel.getLayout()==null){
		    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		    buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 5, 6, 5));

		    buttonPanel.add(Box.createHorizontalGlue());
		    buttonPanel.add(XBackwardOccurrButton);
		    buttonPanel.add(Box.createHorizontalGlue());
		    buttonPanel.add(prevOccurrButton);
		    buttonPanel.add(Box.createHorizontalGlue());
		    buttonPanel.add(occurrsLabelPanel);
		    buttonPanel.add(Box.createHorizontalGlue());
		    buttonPanel.add(nextOccurrButton);
		    buttonPanel.add(Box.createHorizontalGlue());
		    buttonPanel.add(XForwardOccurrButton);
		    buttonPanel.add(Box.createHorizontalGlue());
		  }

		  //restoring previous occurences panel state, if any.
		  if (!currentFiles.containsKey(term)) currentFiles.put(term, 
				  ((JScrollPane)occursTabbedPane.getSelectedComponent() ).getName());

		  else {
			tabTitle = currentFiles.get(term);
			compArrTmp = occursTabbedPane.getComponents();
			for (int k=0; k< compArrTmp.length; ++k)
			  if (compArrTmp[k].getName()==tabTitle){ occursTabbedPane.setSelectedComponent(compArrTmp[k]); break;}
		  }

		  selectCurrentOccurrence(currentSelectedFeatureName,
			  ((JScrollPane)occursTabbedPane.getSelectedComponent() ).getName());

		  occursTabbedPane.addMouseListener(
			new MouseAdapter(){
			  @Override
			  public void mouseClicked(MouseEvent me){						
				selectCurrentOccurrence(currentSelectedFeatureName,
					((JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
				currentFiles.put(currentSelectedFeatureName, 
					((JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
			  }
			}
		  );
		  
		}

	  };	
	  else return null;
	}	

	/**
	 * Returns an ActionListener used for occurrences navigation buttons. 
	 * 
	 * @param jump - dictates the number fo occurrences to jump in the file from the current one, must not be 0.
	 * @return - the new ActionListener
	 */
	private ActionListener getOccurrNavButtonListener(final int jump) {
	  if (jump==0) return null;
	  return new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent ae){							
		  ArrayList<int[]> occurrIndexesList=null;
		  int currentIndex=0;//current index in occurrIndexesList of occurrence to highlite
		  int[] occurrence=null;//current index in the text of occurrence to highlite
		  Object highlightTag=null;//highlight tag that will be added to the text
		  ArrayList<Entry<Highlight,ArrayList<Highlight>>> tagsToRemove=null;//highlight tags to remove
		  ArrayList<Highlight> tagToRemoveReplacements=null;//replacements to last removed tags
		  ArrayList<Entry<Highlight,ArrayList<Highlight>>> lastRemovedTags=null;//last removed or modified highlight tags
		  Highlight tagToRestore=null;//last removed tag, to be restored
		  File tabFile = null;

		  
		  String fileName=((JScrollPane)occursTabbedPane.getSelectedComponent()).getName();
		  JTextArea jta= textTabs.get(fileName);
		  currentIndex= textIndexes.get(currentSelectedFeatureName).get(fileName);

		  //calculate next occurrence index to use, depending on the jump parameter
		  occurrIndexesList=relevantTerms.get(currentSelectedFeatureName).get(fileName);
		  
		  if (jump>0){
			if (currentIndex<occurrIndexesList.size()-jump) currentIndex+=jump;
			else currentIndex=occurrIndexesList.size()-1;
		  }
		  else{
			if (currentIndex+jump>=0) currentIndex+=jump;
			else currentIndex=0;
		  }

		  occurrence = occurrIndexesList.get(currentIndex);

		  Highlighter hilite = jta.getHighlighter();

		  //initializing of lastHighlightedTag, if necessary
		  if(lastHighlightedTag.get(currentSelectedFeatureName)==null) 
		    lastHighlightedTag.put(currentSelectedFeatureName, new HashMap<String, Object>());

		  //removing last highlighted tag for this term and file
		  highlightTag=lastHighlightedTag.get(currentSelectedFeatureName).get(fileName);

		  if(highlightTag!=null) hilite.removeHighlight(highlightTag);

		  
		  //initializing of lastRemovedHighlights, if necessary
		  if(lastRemovedHighlights.get(currentSelectedFeatureName)==null) 
			  lastRemovedHighlights.put(currentSelectedFeatureName, 
				new HashMap<String, ArrayList<Entry<Highlight,ArrayList<Highlight>>>>());

		  //restoring last removed highlighted tags for this term and file
		  lastRemovedTags=lastRemovedHighlights.get(currentSelectedFeatureName).get(fileName);

		  if(lastRemovedTags!=null) try{
			for(int h=0; h<lastRemovedTags.size(); ++h){
			  tagToRestore=lastRemovedTags.get(h).getKey();
			  
			  //removing replacements tags
			  if(lastRemovedTags.get(h).getValue()!=null)
			    for(Highlight tmp : lastRemovedTags.get(h).getValue()) hilite.removeHighlight(tmp);
				
			  //re-putting last removed tag
			  hilite.addHighlight(tagToRestore.getStartOffset(), tagToRestore.getEndOffset(), tagToRestore.getPainter());
			}
		  } catch (BadLocationException e) {
			System.out.println("BadLocationException\nTerm: "+currentSelectedFeatureName+" - occurrence: "+occurrence);
			e.printStackTrace();
		  }

		  tabFile = new File(fileName);
		  if (!tabFile.exists()){//input file was moved or deleted
			occurrsLabel.setText("0/0[Index: missing]");
			return;	
		  }
		  
		  //checking what highlighted tags already are in next occurrence text interval
		  tagsToRemove = new ArrayList<Entry<Highlight, ArrayList<Highlight>>>();
		  for (Highlight tmp: hilite.getHighlights()){

			//tag will be removed
			if (tmp.getStartOffset()>=occurrence[0] && tmp.getEndOffset()<=occurrence[1]){
			  hilite.removeHighlight(tmp);
			  tagsToRemove.add(new  AbstractMap.SimpleEntry<Highlight, ArrayList<Highlight>>(tmp, null));
			}			
			//tag will be replaced
			else if(tmp.getStartOffset()>=occurrence[0] && tmp.getStartOffset()<=occurrence[1]
					&& tmp.getEndOffset()>occurrence[1]){//tmp surround ending part of new tag
			  tagToRemoveReplacements = new ArrayList<Highlight>();
			  try {
				tagToRemoveReplacements.add((Highlight)hilite.addHighlight(
					occurrence[1]+1, tmp.getEndOffset(), tmp.getPainter()));
			  } catch (BadLocationException e) { e.printStackTrace();}
			  
			  hilite.removeHighlight(tmp);
			  tagsToRemove.add(new  AbstractMap.SimpleEntry<Highlight, ArrayList<Highlight>>(tmp, tagToRemoveReplacements));				
			}
			else if(tmp.getStartOffset()<occurrence[0] && tmp.getEndOffset()>=occurrence[0]
					&& tmp.getEndOffset()<=occurrence[1]){//tmp surround starting part of new tag
			  tagToRemoveReplacements = new ArrayList<Highlight>();
			  try {
				tagToRemoveReplacements.add((Highlight)hilite.addHighlight(
					tmp.getStartOffset(), occurrence[0], tmp.getPainter()));
			  } catch (BadLocationException e) { e.printStackTrace();}
			  
			  hilite.removeHighlight(tmp);
			  tagsToRemove.add(new  AbstractMap.SimpleEntry<Highlight, ArrayList<Highlight>>(tmp, tagToRemoveReplacements));				
			}
			else if(tmp.getStartOffset()<occurrence[0] 
					&& tmp.getEndOffset()>occurrence[1]){//tmp entirely surround the new tag
			  tagToRemoveReplacements = new ArrayList<Highlight>();
			  try {
				tagToRemoveReplacements.add((Highlight)hilite.addHighlight(
					tmp.getStartOffset(), occurrence[0], tmp.getPainter()));
				tagToRemoveReplacements.add((Highlight)hilite.addHighlight(
					occurrence[1]+1, tmp.getEndOffset(), tmp.getPainter()));
			  } catch (BadLocationException e) { e.printStackTrace();}
			  
			  hilite.removeHighlight(tmp);
			  tagsToRemove.add(new  AbstractMap.SimpleEntry<Highlight, ArrayList<Highlight>>(tmp, tagToRemoveReplacements));				
			}

		  }
		  
		  //saving last removed highlight tags in lastRemovedHighlights
		  lastRemovedHighlights.get(currentSelectedFeatureName).put(fileName, tagsToRemove);

		  //getting correct highlighter for this feature name
		  Highlighter.HighlightPainter occurrPainter=null;
		  for(String tmp: startingCommonalities)
			if(currentSelectedFeatureName.compareTo(tmp)==0){ occurrPainter=activeCommHighlightPainter; break;}
		  if(occurrPainter==null) occurrPainter=activeVarsHighlightPainter;

		  //highlighting current occurrence and saving it in lastHighlightedTag
		  try {
			lastHighlightedTag.get(currentSelectedFeatureName).put(fileName, hilite.addHighlight(
					occurrence[0], occurrence[1], occurrPainter));
		  } catch (BadLocationException e) {
			System.out.println("BadLocationException\nTerm: "+currentSelectedFeatureName+" - occurrence: "+occurrence);
			e.printStackTrace();
		  }

		  //set Caret position and text selection
		  jta.requestFocusInWindow();
		  jta.getCaret().setVisible(true);
		  jta.setCaretPosition(occurrence[0]);

		  //updating current occurrence index for this file
		  textIndexes.get(currentSelectedFeatureName).put(fileName, currentIndex);

		  //updating occurrences label
		  occurrsLabel.setText( (currentIndex+1)+ OSUtils.getFilePathSeparator() 
				  +occurrIndexesList.size()+"[Index: "+occurrence[0]+"]");

		  /* ***VERBOSE****/					
		  if (debug4) System.out.println(
			 "\n*****\nSELECTED TAB: "+fileName
			+"\noccurrenceIndex: "+occurrence
			+"\ncurrentSelectedFeatureName: "+currentSelectedFeatureName
			+"\ncurrentSelectedFeatureName.length(): "+currentSelectedFeatureName.length()
			+"\ncurrentIndex: "+currentIndex
			+"\n*****\n");					
		  /* ***VERBOSE****/

		}
	  };
	}

	/** Returns a scrollable panel with the content of file s1, highlighting the relevant terms if al is not null,
	 *  the inner JTextArea is added to the list of text Tabs, and a list of selected occurences indexes is built.
	 * 
	 * @param term - relevant term by which occurences indexes will be added to the list of indexes
	 * @param file - path of the file 
	 * @param commonalitiesToHighlight - if not null, these terms will be highlighted with the commonalities color
	 * @param variabilitiesToHighlight - if not null, these terms will be highlighted with the variabilities color
	 * @return JScrollPane - the scrollable panel containing the text of the file
	 */
	private JScrollPane getRegisteredTabTextFile(String term, String file, 
			ArrayList <String> commonalitiesToHighlight, ArrayList<String> variabilitiesToHighlight){
		try{
			String s = getFileContent(file);
			if(OSUtils.isMac()) s = StringUtils.cleanTextCompatibilityForMac(s);
			if(OSUtils.isWindows()) s = StringUtils.cleanTextCompatibilityForWindows(s);

//	        s = ""+0xEF+0xBB+0xBF+s;
//	        s = '\ufeff'+s;
//			String alt="";
//			char k = 0;
//            BufferedReader bufReader2= new BufferedReader(new StringReader(s));
//            for(int i=0; i<1700; ++i){
//              k=(char) bufReader2.read();
//              alt+=k+": "+Character.getNumericValue(k)+"\n";
//            }
//			s=alt;
					
    		/* ***VERBOSE****/
            if (debug4){
              String tmpTest=null;
              System.out.println("getRegisteredTabTextFile - printing file '"+file+"' lines:");
              BufferedReader bufReader= new BufferedReader(new StringReader(s));
              while((tmpTest=bufReader.readLine())!=null) System.out.println(tmpTest+"\n");
              System.out.println("end printing lines");
            }
    		/* ***VERBOSE****/
		    
		    return getRegisteredTabTextString(term, file, s, commonalitiesToHighlight, variabilitiesToHighlight);
//		    return getRegisteredTabTextString(term, file, s, null, null);

		}catch(FileNotFoundException e){
			System.out.println("Exception getRegisteredTabTextFile(): " + e.getMessage());
			return null;
		}catch (IOException e) {
			System.out.println("Exception getRegisteredTabTextFile(): " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Returns the content of a file as a String, preserving newlines.
	 * 
	 * @param s1 - the local path of the file
	 * @return a string with content of the file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private String getFileContent(String s1){
		String s = "";
		String tmp=null;   
		try {
			BufferedReader br =
					new BufferedReader(
							new FileReader(s1));    
			  
			while((tmp = br.readLine()) != null) s = s + tmp + "\n";
			br.close();
		}catch(FileNotFoundException e){
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		} 
		catch (IOException e) {
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		}
		return s;
	}
	
	/**
	 * Returns a JScrollPane containing the String fileContent as a JTextArea with highlights on the words 
	 * commonalitiesToHighlight and variabilitiesToHighlight, if they're not null.
	 * The JTextArea is added to the list of text tabs, and a list of selected occurences indexes is built.<br>
	 *  NOTE: The String fileContent should contain the content of the file
	 * 
	 * @param term - relevant term by which occurences indexes will be added to the list of indexes
	 * @param fileContent - the string to use, containing the content of file
	 * @param file - path of the file 
	 * @param commonalitiesToHighlight - if not null, these terms will be highlighted with the commonalities color
	 * @param variabilitiesToHighlight - if not null, these terms will be highlighted with the variabilities color
	 * @return - a new JScrollPane with the highlighted text
	 */
	private JScrollPane getRegisteredTabTextString(String term, String file, String fileContent,
			ArrayList<String> commonalitiesToHighlight, ArrayList<String> variabilitiesToHighlight) {
		JTextArea jta = getTextAreaString(file, fileContent);
		textTabs.put(file, jta);
		if (!textIndexes.containsKey(term)){
			
    		/* ***DEBUG****/			
			if (debug) System.out.println("\n****textIndexes.containsKey("+term+")="+textIndexes.containsKey(term)
					+". Creating indexes list for term "+term+" ****\n");
    		/* ***DEBUG****/

			textIndexes.put(term, new HashMap<String, Integer>());
		}
		if (!textIndexes.get(term).containsKey(file)) textIndexes.get(term).put(file, 0);		

		if (fileContent!=null && (commonalitiesToHighlight!=null || variabilitiesToHighlight!=null) )
			jta=(JTextArea)setHighlightText(jta, commonalitiesToHighlight, variabilitiesToHighlight, file);
		
		JScrollPane jscr = new JScrollPane(jta);
		jscr.setName(file);
		return jscr;
	}	

	/**
	 * Returns a JTextArea containing the String s.
	 * 
	 * @param name - name of the new JTextArea component
	 * @param s - string to be cointained in the JTextArea
	 * @return - a new JTextArea containing s
	 */
	private JTextArea getTextAreaString(String name, String s) {
		if(s==null) s="INPUT FILE CANNOT BE RETRIEVED!";
		JTextArea jta = new JTextArea(s);
		jta.setName(name);
		jta.setLineWrap(true);
		return jta;
	}

	/** 
	 * Highlights a JTextComponent with the specified terms.
	 * 
	 * @param jtc - JTextComponent to be highlighted
	 * @param commonalitiesToHighlight - if not null, these terms will be highlighted with the commonalities color
	 * @param variabilitiesToHighlight - if not null, these terms will be highlighted with the variabilities color
	 * @param file - the path of the file displayed by jtc
	 * 
	 * @return - the modified JTextComponent
	 */
	private JTextComponent setHighlightText(JTextComponent jtc,
			ArrayList<String> commonalitiesToHighlight, ArrayList<String> variabilitiesToHighlight, String file){
		ArrayList<int[]> occurrences = null;
		
		if(commonalitiesToHighlight==null && variabilitiesToHighlight==null) return jtc;
		try{   
			Highlighter hilite = jtc.getHighlighter();
		    
			//adding highlights to commonalities occurrences
			for(int i = 0; i < commonalitiesToHighlight.size(); i++){
			  occurrences = relevantTerms.get(commonalitiesToHighlight.get(i)).get(file);
			  for(int[] occurr: occurrences) hilite.addHighlight(occurr[0], occurr[1], passiveCommHighlightPainter);
			}
				
			//adding highlights to variabilities occurrences
			for(int i = 0; i < variabilitiesToHighlight.size(); i++){
			  occurrences = relevantTerms.get(variabilitiesToHighlight.get(i)).get(file);
			  if(occurrences!=null) for(int[] occurr: occurrences) 
				hilite.addHighlight(occurr[0], occurr[1], passiveVarsHighlightPainter);
			}
			
		}catch(BadLocationException e){
		  System.out.println("Exception tabTextFile: " + e.getMessage());
		  return null;
		}
		return jtc;
	}

	/**
	 * Select the first occurrence of the current selected feature in the tab containing the text of file.
	 * 
	 * @param currentSelectedFeatureName - index of current selected checkbox in checkBoxCommonalities
	 * @param file - file name of the file contained in the tab
	 */
	private void selectCurrentOccurrence(String currentSelectedFeatureName, String file) {
		  HashMap<String, ArrayList<int[]>> occurrFilesList=null;		
		  ArrayList<int[]> occurrIndexesList=null;
		  int[] occurrence=null;
		  Object highlightTag=null;//highlight tag that will be added to the text
		  ArrayList<Entry<Highlight,ArrayList<Highlight>>> tagsToRemove=null;//highlight tags to remove
		  ArrayList<Highlight> tagToRemoveReplacements=null;//replacements to last removed tags
		  ArrayList<Entry<Highlight,ArrayList<Highlight>>> lastRemovedTags=null;//last removed or modified highlight tags
		  Highlight tagToRestore=null;//last removed tag, to be restored
		  File tabFile = null;
		  JTextArea jta= textTabs.get(file);
		  
		  int currentIndex= textIndexes.get(currentSelectedFeatureName).get(file);

		  //calculating current occurrence index for selection
		  occurrFilesList = relevantTerms.get(currentSelectedFeatureName);
		  occurrIndexesList=occurrFilesList.get(file);
		  occurrence = occurrIndexesList.get(currentIndex);

		  Highlighter hilite = jta.getHighlighter();
		  
		  //initializing of lastHighlightedTag, if necessary
		  if(lastHighlightedTag.get(currentSelectedFeatureName)==null) 
			  lastHighlightedTag.put(currentSelectedFeatureName, new HashMap<String, Object>());

		  //removing last highlighted tag for this term and file, if any
		  highlightTag=lastHighlightedTag.get(currentSelectedFeatureName).get(file);
		  
		  if(highlightTag!=null) hilite.removeHighlight(highlightTag);

		  
		  //initializing of lastRemovedHighlights, if necessary
		  if(lastRemovedHighlights.get(currentSelectedFeatureName)==null) 
			  lastRemovedHighlights.put(currentSelectedFeatureName, 
					  new HashMap<String, ArrayList<Entry<Highlight,ArrayList<Highlight>>>>());
		  
		  //re-putting last removed highlighted commonality tags for this term and file, if any
		  lastRemovedTags=lastRemovedHighlights.get(currentSelectedFeatureName).get(file);
		  
		  if(lastRemovedTags!=null) try{			  
			for(int h=0; h<lastRemovedTags.size(); ++h){
			  tagToRestore = lastRemovedTags.get(h).getKey();

			  //removing replacements tags
			  if(lastRemovedTags.get(h).getValue()!=null)
				for(Highlight tmp : lastRemovedTags.get(h).getValue()) hilite.removeHighlight(tmp);

			  //re-putting last removed tag
			  hilite.addHighlight(tagToRestore.getStartOffset(), tagToRestore.getEndOffset(), tagToRestore.getPainter());
			}
		  } catch (BadLocationException e) {
			System.out.println("BadLocationException\nTerm: "+currentSelectedFeatureName+" - occurrence: "+occurrence);
			e.printStackTrace();
		  }
		  
		  tabFile = new File(file);
		  if (!tabFile.exists()){//input file was moved or deleted
			occurrsLabel.setText("0/0[Index: missing]");
			return;	
		  }		  		  
		  
		  //checking what highlighted tags already are in next occurrence text interval
		  tagsToRemove = new ArrayList<Entry<Highlight, ArrayList<Highlight>>>();
		  for (Highlight tmp: hilite.getHighlights()){

			//tag will be removed
			if (tmp.getStartOffset()>=occurrence[0] && tmp.getEndOffset()<=occurrence[1]){
			  hilite.removeHighlight(tmp);
			  tagsToRemove.add(new  AbstractMap.SimpleEntry<Highlight, ArrayList<Highlight>>(tmp, null));
			}			
			//tag will be replaced
			else if(tmp.getStartOffset()>=occurrence[0] && tmp.getStartOffset()<=occurrence[1]
					&& tmp.getEndOffset()>occurrence[1]){//tmp surround ending part of new tag
			  tagToRemoveReplacements = new ArrayList<Highlight>();
			  try {
				tagToRemoveReplacements.add((Highlight)hilite.addHighlight(
					occurrence[1]+1, tmp.getEndOffset(), tmp.getPainter()));
			  } catch (BadLocationException e) { e.printStackTrace();}
			  
			  hilite.removeHighlight(tmp);
			  tagsToRemove.add(new  AbstractMap.SimpleEntry<Highlight, ArrayList<Highlight>>(tmp, tagToRemoveReplacements));				
			}
			else if(tmp.getStartOffset()<occurrence[0] && tmp.getEndOffset()>=occurrence[0]
					&& tmp.getEndOffset()<=occurrence[1]){//tmp surround starting part of new tag
			  tagToRemoveReplacements = new ArrayList<Highlight>();
			  try {
				tagToRemoveReplacements.add((Highlight)hilite.addHighlight(
					tmp.getStartOffset(), occurrence[0], tmp.getPainter()));
			  } catch (BadLocationException e) { e.printStackTrace();}
			  
			  hilite.removeHighlight(tmp);
			  tagsToRemove.add(new  AbstractMap.SimpleEntry<Highlight, ArrayList<Highlight>>(tmp, tagToRemoveReplacements));				
			}
			else if(tmp.getStartOffset()<occurrence[0] 
					&& tmp.getEndOffset()>occurrence[1]){//tmp entirely surround the new tag
			  tagToRemoveReplacements = new ArrayList<Highlight>();
			  try {
				tagToRemoveReplacements.add((Highlight)hilite.addHighlight(
					tmp.getStartOffset(), occurrence[0], tmp.getPainter()));
				tagToRemoveReplacements.add((Highlight)hilite.addHighlight(
					occurrence[1]+1, tmp.getEndOffset(), tmp.getPainter()));
			  } catch (BadLocationException e) { e.printStackTrace();}
			  
			  hilite.removeHighlight(tmp);
			  tagsToRemove.add(new  AbstractMap.SimpleEntry<Highlight, ArrayList<Highlight>>(tmp, tagToRemoveReplacements));				
			}

		  }
		  
		  //saving last removed Commonality tags in lastRemovedCommHighlights
		  lastRemovedHighlights.get(currentSelectedFeatureName).put((
		    (JScrollPane)occursTabbedPane.getSelectedComponent()).getName(), 
		    tagsToRemove);		  
		  
		  //getting correct highlighter for this feature name
		  Highlighter.HighlightPainter occurrPainter=null;
		  for(String tmp: startingCommonalities)
			if(currentSelectedFeatureName.compareTo(tmp)==0){ occurrPainter=activeCommHighlightPainter; break;}
		  if(occurrPainter==null) occurrPainter=activeVarsHighlightPainter;
		  
		  //highlighting current occurrence and saving it in lastHighlightedTag		  		  
		  try {
			lastHighlightedTag.get(currentSelectedFeatureName).put(
			  ((JScrollPane)occursTabbedPane.getSelectedComponent()).getName(),
			  hilite.addHighlight(occurrence[0], occurrence[1], occurrPainter));
			  
		  } catch (BadLocationException e) {
			  System.out.println("BadLocationException\nTerm: "+currentSelectedFeatureName+" - occurrence: "+occurrence);
			  e.printStackTrace();
		  }
		  
		  //setting Caret position and text selection
		  jta.requestFocusInWindow();
		  jta.getCaret().setVisible(true);
		  jta.setCaretPosition(occurrence[0]);

		  //updating occurrences label
		  occurrsLabel.setText( (currentIndex+1)+ OSUtils.getFilePathSeparator() 
				  +occurrIndexesList.size()+"[Index: "+occurrence[0]+"]");
		
	}

	/**
	 * Returns the feaure panel with the given ID.
	 * 
	 * @param id - the feature panel ID
	 * @return - the feaure panel with the given ID, or null if not present
	 */
	public FeaturePanel getFeaturePanel(String id) {
	  boolean found = false;
	  
	  OrderedListNode tmp = visibleOrderDraggables.getFirst();
	  while(tmp!=null){
		if(((JComponent)tmp.getElement()).getName().startsWith(featureNamePrefix) 
			&& ((FeaturePanel)tmp.getElement()).getID().compareTo(id)==0){ found=true; break;}
		tmp=tmp.getNext();
	  }		  	  

	  if(found) return (FeaturePanel)tmp.getElement();
	  else return null;
	}

	/**
	 * Adds a feature panel to the diagram panel without need of notifications from the model.
	 * 
	 * @param featureName - the feature name
	 * @param featureID - the feature ID
	 * @param x - x position of new feature panel on the diagram panel
	 * @param y - y position of new feature panel on the diagram panel
	 * @param width - width of the new feature panel
	 * @param height - heightof of the new feature panel
	 * @param featureColor - color of the new feature panel
	 * @return - the new feature panel added to the diagram
	 */
	public FeaturePanel directlyAddFeatureToDiagram(String featureName, String featureID, 
													int x, int y, int width, int height, Color featureColor) {
//		Color featureColor = null;
		
		if (featureName==null){
		  featureName=featureNamePrefix+featuresCount;
		  featureColor=Color.BLACK;
		}
		else if(featureColor==null) featureColor = getNewColor(termsColor.get(featureName));	
		

		
		FeaturePanel newFeature=buildFeaturePanel(featureName, featureID, x, y, featureColor);
		if(width<=0) width=120+featureBorderSize;
		if(height<=0) height=60+featureBorderSize;
		newFeature.setSize(width, height);
		
		visibleOrderDraggables.addToTop(newFeature);
		addToDiagramOnTop(newFeature);
		
		return newFeature;
	}

	/**
	 * Directly adds an anchor to a feature, without removing it from the diagram panel.<br>
	 * This method is used to add anchors that have never been added to the diagram panel, on a feature panel.
	 * 
	 * @param anchorPanel - the anchor to add
	 * @param featurePanel - the feature on which the anchor must be added
	 */
	public void directlyAddAnchorToFeature(AnchorPanel anchorPanel, FeaturePanel featurePanel) {
		visibleOrderDraggables.addToTop(anchorPanel);
		addToComponentOnLayer(featurePanel, anchorPanel, 0);

		//adding start anchor to draw list
		if(anchorPanel.getName().startsWith(startMandatoryNamePrefix)
		   || anchorPanel.getName().startsWith(startOptionalNamePrefix) )
		  startConnectorDots.add(anchorPanel);
		else if(anchorPanel.getName().startsWith(startExcludesNamePrefix))
		  startExcludesDots.add(anchorPanel);
		else if(anchorPanel.getName().startsWith(startIncludesNamePrefix))
		  startIncludesDots.add(anchorPanel);
		
/*
 		//adding members
 		for(int k=4; k<groupData.length; k+=3){
		  //adding member
		  member=(AnchorPanel)buildConnectionDot(ItemsType.END_MANDATORY_CONNECTOR, memberName, memberX, memberY);
		  if(memberOwnerName.compareTo("")==0){//adding member to the diagram panel directly
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
*/

		
		
		
		
	}

	/**
	 * Adds a group to a feature panel without need of notifications from the model.
	 * 
	 * @param group - the group to be added to the feature
	 * @param owner - the feature taht will be the owner of the group
	 * @param groupType 
	 */
	public void directlyAddGroupToFeature(GroupPanel group, FeaturePanel owner, ItemsType groupType) {
		visibleOrderDraggables.addToTop(group);
		addToComponentOnLayer(owner, group, 0);

		//adding group to draw list
		if(groupType==ItemsType.ALT_GROUP_START_CONNECTOR) altGroupPanels.add(group);
		else orGroupPanels.add(group);
	}

	/**
	 * Directly adds an anchor to the diagram.<br>
	 * This method is used to add single anchors, it's up to the caller to be sure the anchor is part of a group or link.
	 * 
	 * @param anchorPanel - the anchor to add
	 */
	public void directlyAddAnchorToDiagram(JComponent anchorPanel) {
		visibleOrderDraggables.addToTop(anchorPanel);
		addToDiagramOnTop(anchorPanel);

		//adding start anchor to draw list
		if(anchorPanel.getName().startsWith(startMandatoryNamePrefix)
		   || anchorPanel.getName().startsWith(startOptionalNamePrefix) )
		  startConnectorDots.add(anchorPanel);
	}		
	
	/**
	 * Sets the root frame to maximum size and brings it to front.
	 */
	public void maximize(){
      frameRoot.setExtendedState(JFrame.MAXIMIZED_BOTH);
      frameRoot.setAlwaysOnTop(true);
      frameRoot.requestFocus();
      frameRoot.setAlwaysOnTop(false);		
	}
	
	/**
	 * Sets the root frame to iconified state and sends it to tray.
	 */
	public void minimize(){
      frameRoot.setState(JFrame.ICONIFIED);
	}
	
}
