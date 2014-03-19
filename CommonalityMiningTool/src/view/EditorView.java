package view;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.Font;
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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
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
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box.Filler;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.IconView;
import javax.swing.text.JTextComponent;
import javax.swing.text.LabelView;
import javax.swing.text.LayeredHighlighter;
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
import javax.xml.stream.events.StartDocument;

import view.EditorModel.StringWrapper;
import view.ViewPanelCentral.FeatureType;
import main.ModelXMLHandler;
import main.OrderedList;
import main.OrderedListNode;
import main.SortUtils;
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
	      return getPreferredSize();
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
		  Graphics2D g2 = (Graphics2D)g.create();		
		  paintComponent(g);
		  paintBorder(g);
		  drawAllConnectors(g2);
		  paintChildren(g);//panels in the diagram panel are drawn over lines			  
		}
	}

	/** Class used to implement the editor contained in the frame. */
	class EditorSplitPane extends JSplitPane{

		public static final long serialVersionUID = 1L;

		public EditorSplitPane(int horizontalSplit) {
			super(horizontalSplit);
		}

		@Override
		public void paint(Graphics g){
//			Graphics2D g2 = (Graphics2D)g.create();		
			paintComponent(g);
			paintBorder(g);
//			drawAllConnectors(g2);
			paintChildren(g);//panels in the diagram panel are drawn over lines
		}		
	}
	
	/** Class used to filter project files. */
	class FilterFileProject implements FilenameFilter{
		@Override
		public boolean accept(File dir, String name){
			return name.endsWith( ".xml" );
	    }
	}

	/** prefix of any feature ID*/
	public static String featureNamePrefix="---FEATURE---#";
	/** prefix of any text area owned by feature panels*/
	public static String textAreaNamePrefix="---TEXTAREA---";
	/** prefix of any connector starting dot name*/
	public static String startMandatoryNamePrefix="---START_MANDATORY---#";
	/** prefix of any connector ending dot name*/
	public static String endMandatoryNamePrefix="---END_MANDATORY---#";
	/** prefix of any connector starting dot name*/
	public static String startOptionalNamePrefix="---START_OPTIONAL---#";
	/** prefix of any connector ending dot name*/
	public static String endOptionalNamePrefix="---END_OPTIONAL---#";
	/** prefix of any connector starting dot name*/
	public static String startIncludesNamePrefix="---START_INCLUDES---#";
	/** prefix of any connector ending dot name*/
	public static String endIncludesNamePrefix="---END_INCLUDES---#";
	/** prefix of any connector starting dot name*/
	public static String startExcludesNamePrefix="---START_EXCLUDES---#";
	/** prefix of any connector ending dot name*/
	public static String endExcludesNamePrefix="---END_EXCLUDES---#";
	/** prefix of any constraint control point dot name*/
	public static String constraintControlPointNamePrefix="---CONSTRAINT_CONTROL_POINT---#";
	/** prefix of any group Alternative Gtarting dot name*/
	public static String altGroupNamePrefix="---ALT_GROUP---#";
	/** prefix of any Or Group starting dot name*/
	public static String orGroupNamePrefix="---OR_GROUP---#";
	/** name ofthe diagram panel*/
	public static String diagramPanelName="---DIAGRAM_PANEL---";
	
	
//	/** URL of the new feature icon*/
//	private static URL newFeatureIconURL=EditorView.class.getResource("/feature rectangle2.png");
	/** URL of the connector starting dot icon*/
	private static URL connectorStartDotIconURL=EditorView.class.getResource("/Connector Start Dot.png");
	/** URL of the new group starting dot icon*/
	private static URL ALTGroupDotIconURL=EditorView.class.getResource("/ALTGroup Start Dot.png");
	/** URL of the new group starting dot icon*/
	private static URL ORGroupDotIconURL=EditorView.class.getResource("/ORGroup Start Dot.png");
	/** URL of the new mandatory connector ending dot icon*/
	private static URL mandatoryConnectorEndDotIconURL=EditorView.class.getResource("/Mandatory Connector End Dot.png");
	/** URL of the new optional connector ending dot icon*/
	private static URL optionalConnectorEndDotIconURL=EditorView.class.getResource("/Optional Connector End Dot.png");
	/** URL of the new constraint dot icon*/
	private static URL constraintDotIconURL=EditorView.class.getResource("/Constraint Dot.png");
	/** URL of the new constraint control point dot icon*/
	private static URL constraintControlPointDotIconURL=EditorView.class.getResource("/Constraint Control Point Dot.png");
	/** URL of the connector line-only icon*/
	private static URL connectorLineLengthIconURL=EditorView.class.getResource("/Connector Line Length.png");
	/** URL of the group line-only icon*/
	private static URL groupLineLengthIconURL=EditorView.class.getResource("/Group Line Length.png");

	/** maps tool names in the corresponding icon resource path*/
	private static HashMap<String, String> toolIconPaths=null;
	
	private static int featureBorderSize=20;

	/** enumeration of items that can become active, for instance in a drag motion*/
	public static enum activeItems {
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
	
	/** Tells if the diagram has been modified after last save*/
	private boolean modified=true;
	
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
	private JMenuItem menuFilesSave=null, menuFilesOpen=null, menuFilesExportXML=null,
					  menuFilesDelete=null, menuFilesExit=null, menuFilesExportAsPNG=null, menuFilesExportAsGIF=null;

	/** View Menu items*/
	private JMenuItem menuViewColored=null, menuViewCommsOrVars=null, 
					  menuViewExtrOrInsert=null, menuViewFields=null,
					  menuViewVisibleConstraints=null;
	
	/** Modify Menu items*/
	private JMenuItem menuModifyBasicFM=null, menuModifyAdvancedFM=null;
		
	
	/** Number of connector dots created*/
	private int connectorsCount=0;
	/** Number of includes constraint dots created*/
	private int includesCount=0;
	/** Number of excludes constraint dots created*/
	private int excludesCount=0;
	/** Number of constraint control point dots created*/
	private int constraintControlsCount=0;
	/** Number of Alternative Groups created*/
	private int altGroupsCount=0;
	/** Number of Or Groups created*/
	private int orGroupsCount=0;
	/** Number of features created*/
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
	
	/** List of all connector starting dots*/
//	corresponding ending dots can be found in endConnectorDots at the same index*/
	private ArrayList<JComponent> startConnectorDots=null;
	/** List of Includes costraints starting dots*/
	private ArrayList<JComponent> startIncludesDots=null;
	/** List of Excludes costraints starting dots*/
	private ArrayList<JComponent> startExcludesDots=null;
	/** List of Alternative Groups*/
	private ArrayList<GroupPanel> altGroupPanels=null;	
	/** List of Or Groups*/
	private ArrayList<GroupPanel> orGroupPanels=null;
	
	/** List of starting commonalities selected by the user */
	private ArrayList<String> startingCommonalities=new ArrayList<String>();
	/** List of starting commonalities and variabilities selected by the user */
	private ArrayList<String> startingVariabilities=new ArrayList<String>();

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
	
//	private static JFrame toolDragPanel = null;//temporary frame used to drag tools
	/** Image used to drag tools*/
	private BufferedImage toolDragImage = null;
	/** Position of the dragged image*/
	private Point toolDragPosition = null;	
	
	/** Old name of the feature about to be renamed*/
	private String oldFeatureName=null;
	
	/** Name of the next feature to add*/
	private String featureToAddName = null;
	
	private int verticalShift=0;
	
	/** The panel containing the diagram */
//	private JLayeredPane diagramPanel=null;
	/** The panel containing the diagram */
	private ScrollLayeredPane diagramPanel=null;	
	
	/** The JScrollPane containing the diagramPanel */
	private JScrollPane diagramScroller=null;
	
	/** The panel containing the tools */
	private JPanel toolsPanel=null;
	
	/** The JFrame used to display search panels*/
	private JFrame searchFrame=null;

//	/**the panel containing the candidate feature's checkboxes */
//	private JPanel panelFeatures = null;
	
	/** The panel searchFrame used to search for feature occurrences*/
	private JPanel searchPanel = null;

//	/** List of selected features names*/
//	private ArrayList<JLabel> labelFeatures = new ArrayList<JLabel> ();
	
	/**buttons for navigating through commonalitie occurences in tab texts, the X...wardButtons move of x occurences, 
	 where x is defined by occurrJumpSpan constant*/
	private JButton nextOccurrButton = null, prevOccurrButton = null, XForwardOccurrButton = null, XBackwardOccurrButton = null;

	/**defines the number x of occurences jumped by XForwardOccurrButton and XBackwardOccurrButton*/
	private int occurrJumpSpan=4;

	/**label for occurrences navigation*/
	private JLabel occurrsLabel = null;
	
	/**label for occurrences navigation*/
	private JPanel occurrsLabelPanel = null;

	/**last highlighted tag for each relevant term and file*/
	private HashMap<String, HashMap<String, Object>> lastHighlightedTag=null;

	/**last removed highlight tags for each relevant term and file*/
	private HashMap<String, HashMap<String, ArrayList<Highlight>>> lastRemovedHighlights=null;
	
	/**relevant terms occurrences panel*/
	private JTabbedPane occursTabbedPane = null;
	
	/**the JTextAreas of search panel*/
	private HashMap<String, JTextArea> textTabs = null;
	/**association between relevant terms and current selected tab file names in search panel*/
	private HashMap<String, String> currentFiles = null;
	/**indexes of current selected occurrences in project input files*/
	private HashMap<String, HashMap<String, Integer>> textIndexes = null;	

	/**current selected checkbox*/
	private String currentSelectedFeatureName=null;
	
	/** HighlightPainter objects used for search command*/
	private final Highlighter.HighlightPainter[] highlightPainter = {
			  new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW),
			  new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN)
	};
	
	private static int[] PCcol={160, 160, 0};
	private static int[] PVcol={0, 160, 160};
	private static int[] ACcol={255, 255, 0};
	private static int[] AVcol={0, 255, 255};
	private static int[] NEcol={160, 0, 0};

	private static Highlighter.HighlightPainter passiveCommHighlightPainter =
			new DefaultHighlighter.DefaultHighlightPainter(getNewColor(PCcol));
	private static Highlighter.HighlightPainter passiveVarsHighlightPainter= 
			new DefaultHighlighter.DefaultHighlightPainter(getNewColor(PVcol));
	private static Highlighter.HighlightPainter activeCommHighlightPainter = 
			new DefaultHighlighter.DefaultHighlightPainter(getNewColor(ACcol));
	private static Highlighter.HighlightPainter activeVarsHighlightPainter = 
			new DefaultHighlighter.DefaultHighlightPainter(getNewColor(AVcol));
	
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
	/** Variable used to track elements that collided with horizontal border during group drag*/
	private ArrayList<JComponent> collidedElementX=new ArrayList<JComponent>();
	/** Variable used to track elements that collided with vertical border during group drag*/
	private ArrayList<JComponent> collidedElementY=new ArrayList<JComponent>();

	public EditorView(){}
	
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
		  
//	  System.out.println("relevantTermsVersions: ");
//	  for(String[] strArr : relevantTermsVersions) System.out.println(strArr[0]+" - "+strArr[1]);

//	  Iterator<Entry<String, HashMap<String, ArrayList<int[]>>>> termVersionsIter = relevantTerms.entrySet().iterator();
//	  Entry<String, HashMap<String, ArrayList<int[]>>> termVersionsEntry=null;
//	  Iterator<Entry<String, ArrayList<int[]>>> fileVersionsIter = null;
//	  Entry<String, ArrayList<int[]>> fileVersionsEntry=null;

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
		  
//		  for(String[] str : relevantTermsVersions)
//			if(str[0].compareTo(name)==0) this.relevantTermsVersions.put(str[0], str[1]);
//		  for(int i=0; i<relevantTermsVersions.size(); ++i)
//			if(relevantTermsVersions.get(i)[1].compareTo(name)==0)
//			  this.relevantTermsVersions.add(relevantTermsVersions.get(i));
				  
		}
	  
	  if(variabilitiesSelected!=null)
		for(String name : variabilitiesSelected){
		  startingVariabilities.add(name);
		  this.termsColor.put(name, colorsMap.get(name));		  
		  this.relevantTerms.put(name, relevantTerms.get(name));
		  if(relevantTermsVersions.get(name)!=null) 
		      this.relevantTermsVersions.put(name, relevantTermsVersions.get(name));
//		  for(String[] str : relevantTermsVersions)
//			if(str[0].compareTo(name)==0) this.relevantTermsVersions.put(str[0], str[1]);
//		  for(int i=0; i<relevantTermsVersions.size(); ++i)
//			if(relevantTermsVersions.get(i)[1].compareTo(name)==0)
//			  this.relevantTermsVersions.add(relevantTermsVersions.get(i));
		}
	  

	}

	/**
	 * Initializes editor UI.
	 * @param editorController - the EditorController for this editor
	 * @return - false if editorController is null, true otherwise.
	 */
	public boolean prepareUI(EditorController editorController){
		if(editorController==null) return false;
//		this.editorController=editorController;
		
		/* initializing JMenuBar */		
		menuFiles = new JMenu("Files");
		menuFiles.setMnemonic(KeyEvent.VK_F);

		menuView = new JMenu("View");
		menuView.setMnemonic(KeyEvent.VK_V);

		menuModify = new JMenu("Modify");
		menuModify.setMnemonic(KeyEvent.VK_M);

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
		
		menuFilesExportAsPNG = new JMenuItem("Export as PNG");
		menuFilesExportAsPNG.addActionListener(editorController);
		
		menuFilesExportAsGIF = new JMenuItem("Export as GIF");
		menuFilesExportAsGIF.addActionListener(editorController);

		
		menuFiles.add(menuFilesSave);
		menuFiles.add(menuFilesOpen);
		menuFiles.addSeparator();
		menuFiles.add(menuFilesExportXML);
		menuFiles.add(menuFilesExportAsPNG);
		menuFiles.add(menuFilesExportAsGIF);
		menuFiles.addSeparator();
		menuFiles.add(menuFilesDelete);
		menuFiles.add(menuFilesExit);

		/*Menu View items*/
		menuViewColored = new JCheckBoxMenuItem("Colour 'near' Features", false);
		menuViewColored.addActionListener(editorController);
		
		menuViewCommsOrVars = new JCheckBoxMenuItem("View Commonality/Variability");
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

    	popMenuItemShowControlPoint.addActionListener(editorController);
    	popMenuItemHideControlPoint.addActionListener(editorController);
    	
		visibleOrderDraggables = new OrderedList();
		startConnectorDots = new ArrayList<JComponent>();
		startIncludesDots = new ArrayList<JComponent>();
		startExcludesDots = new ArrayList<JComponent>();
//		endConnectorDots = new ArrayList<JComponent>();
//		prevStartConnectorDotsLocation = new ArrayList<Point>();
//		prevEndConnectorDotsLocation = new ArrayList<Point>();
//		connectorDotsToRedraw = new ArrayList<Boolean>();
		altGroupPanels = new ArrayList<GroupPanel>();
		orGroupPanels = new ArrayList<GroupPanel>();

		selectionGroupFocused = new ArrayList<JComponent>();
		
		
		//creating root frame
		frameRoot=this;
		setLayout(new BorderLayout());		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		//creating tools panel
		toolsPanel = new JPanel();		
		toolsPanel.setLayout(new GridLayout(0, 2, 2, 2));		
//		toolsPanel.setPreferredSize(new Dimension(140, Toolkit.getDefaultToolkit().getScreenSize().height));
		toolsPanel.setPreferredSize(new Dimension(140, Toolkit.getDefaultToolkit().getScreenSize().height/2));
		toolsPanel.setBackground(Color.white);
//		toolsPanel.setBorder(BorderFactory.createCompoundBorder(
//				BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
//						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		
		//creating tools items
		toolIconPaths=new HashMap<String, String>();
		toolIconPaths.put("New Feature", "/New Feature2.png");
		toolIconPaths.put("Mandatory Link", "/Mandatory Link.png");
		toolIconPaths.put("Optional Link", "/Optional Link.png");
		toolIconPaths.put("Excludes", "/Excludes.png");
		toolIconPaths.put("Includes", "/Includes.png");
		toolIconPaths.put("Alternative Group", "/Alternative Group.png");
		toolIconPaths.put("Or Group", "/Or Group.png");
		
		
		JComponent iconTmpPanel=null;

		iconTmpPanel=getToolIcon("Mandatory Link", true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

		iconTmpPanel=getToolIcon("Optional Link", true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

		iconTmpPanel=getToolIcon("Includes", true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

		iconTmpPanel=getToolIcon("Excludes", true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

		iconTmpPanel=getToolIcon("Alternative Group", true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

		iconTmpPanel=getToolIcon("Or Group", true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);	

		iconTmpPanel=getToolIcon("New Feature", true);
		iconTmpPanel.addMouseListener(editorController);
		iconTmpPanel.addMouseMotionListener(editorController);
		toolsPanel.add(iconTmpPanel);

		//creating diagram panel, that will be inserted in the scroller
		diagramPanel = new ScrollLayeredPane();
		diagramPanel.setName(diagramPanelName);
		diagramPanel.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width-160,
		Toolkit.getDefaultToolkit().getScreenSize().height));
		diagramPanel.setLayout(null);		
		diagramPanel.setMinimumSize(new Dimension((Toolkit.getDefaultToolkit().getScreenSize().width-160)/6,
				Toolkit.getDefaultToolkit().getScreenSize().height));
		

		//creating diagram scroller, which will fit the rest of the root frame		
		diagramScroller=new JScrollPane( diagramPanel, 
				   ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		diagramScroller.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width-160,
		Toolkit.getDefaultToolkit().getScreenSize().height));
		diagramScroller.setMinimumSize(new Dimension((Toolkit.getDefaultToolkit().getScreenSize().width-160)/6,
				Toolkit.getDefaultToolkit().getScreenSize().height));

//		toolsPanel.setMinimumSize(new Dimension((Toolkit.getDefaultToolkit().getScreenSize().width-160)/12,
//				Toolkit.getDefaultToolkit().getScreenSize().height));		
		toolsPanel.setMinimumSize(new Dimension((Toolkit.getDefaultToolkit().getScreenSize().width-160)/12,
				Toolkit.getDefaultToolkit().getScreenSize().height/2));
		
		splitterPanel = new EditorSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitterPanel.setContinuousLayout(true);
//		splitterPanel.setOneTouchExpandable(true);
//		splitterPanel.setDividerLocation(0.5);
		splitterPanel.setDividerSize(6);
//		splitterPanel.setDoubleBuffered(true);
		splitterPanel.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width,
				Toolkit.getDefaultToolkit().getScreenSize().height));
//		splitterPanel.setLocation(0,0);
//		splitterPanel.setResizeWeight(0.5);
		
		JPanel controllerPanel=new JPanel();
		controllerPanel.setLayout(new BorderLayout());

		
		JPanel treePanel=new JPanel();		
		treePanel.setPreferredSize(new Dimension(140, Toolkit.getDefaultToolkit().getScreenSize().height));
		treePanel.setMinimumSize(new Dimension(140, Toolkit.getDefaultToolkit().getScreenSize().height));
		treePanel.setSize(new Dimension(140, Toolkit.getDefaultToolkit().getScreenSize().height));
		treePanel.setBackground(Color.RED);
		treePanel.add(new JLabel("Caio!"));

//		controllerPanel.add(toolsPanel, BorderLayout.SOUTH);		
//		controllerPanel.add(treePanel, BorderLayout.NORTH);		
		controllerPanel.add(toolsPanel, BorderLayout.NORTH);		
		controllerPanel.add(treePanel, BorderLayout.SOUTH);
//		controllerPanel.add(toolsPanel);		
//		controllerPanel.add(treePanel);
		
//		splitterPanel.add(toolsPanel);
		splitterPanel.add(controllerPanel);
		splitterPanel.add(diagramScroller);
		
				
		add(splitterPanel);


		diagramPanel.addMouseListener(editorController);
		diagramPanel.addMouseMotionListener(editorController);
//		diagramPanel.addMouseWheelListener(editorController);
		

		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		setVisible(true);
		setLocation(0, 0);
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		validate();
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
        return true;
	}

	@Override
	public void paint(java.awt.Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D)g.create();
		
		/* ***DEBUG*** */
		if(debug3) System.out.println("Mi han chiamato, son la paint()");
		/* ***DEBUG*** */
		
		System.out.println("PAINT: isActiveItem="+isActiveItem);

		if(toolDragImage!=null) 
			g2.drawImage(toolDragImage, toolDragPosition.x+1, toolDragPosition.y+4, null);
		if(isActiveItem==activeItems.DRAGGING_SELECTION_RECT){
			System.out.println("Disegno il rect");
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
		  drawConstraint(g2, getVisibleStartAnchorCenter(startIncludesDots.get(i)),
			getVisibleStartAnchorCenter(((ConstraintPanel)startIncludesDots.get(i)).getOtherEnd()), 
			((ConstraintPanel)startIncludesDots.get(i)).getControlPoint().getLocation(), ItemsType.START_INCLUDES_DOT);
		}
		for (int i=0; i< startExcludesDots.size(); ++i){
		  g2.setStroke(new BasicStroke());
		  drawConstraint(g2, getVisibleStartAnchorCenter(startExcludesDots.get(i)),
			getVisibleStartAnchorCenter(((ConstraintPanel)startExcludesDots.get(i)).getOtherEnd()), 
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
	    Line2D.Double endLine=null;
	    Rectangle camera=null;
	    
	    //creating the QuadCurve2D.Float
	    QuadCurve2D quadcurve = new QuadCurve2D.Float();
	    
	    //setting coordinates
	    quadcurve.setCurve(start.getX(), start.getY(), control.getX(), control.getY(), end.getX(), end.getY());

	    camera=new Rectangle((int)end.getX()-20, (int)end.getY()-20, 40, 40);
	    endLine=new Line2D.Double(control, end);
	    
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
	    camera=new Rectangle((int)start.getX()-20, (int)start.getY()-20, 40, 40);
	    endLine=new Line2D.Double(control, start);

	    
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

	/**
	 * Finds the intersection point between the Rectangle camera and the Line2d.Double endLine,
	 *  starting at the point control.
	 * 
	 * @param control - the starting point of endLine
	 * @param camera - the Rectangle used to calculate the intersection point
	 * @param endLine - the Line2d.Double used to calculate the intersection point
	 * @return
	 */
	private Point2D getTriangleTipPoint(Point control, Rectangle camera, Line2D.Double endLine) {
		Line2D intersectionSide;
		Point2D intersectionPoint=null;
	    if(control.x>=camera.x && control.x<=camera.x+camera.width){
	      if(control.y<=camera.y){//control point is directly over the camera
	    	intersectionSide=
	    	  new Line2D.Double(camera.x, camera.y, camera.x+camera.width, camera.y);
	    	intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);
	      }
	      else if(control.y>=camera.y){//control point is directly below the camera
	      	intersectionSide=
	      	  new Line2D.Double(camera.x, camera.y+camera.height, camera.x+camera.width, camera.y+camera.height);
	      	intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);    	  
	      }
	    }
	    else if(control.y>=camera.y && control.y<=camera.y+camera.height){
	      if(control.x<=camera.x){//control point is directly at left of the camera
	        intersectionSide=
	        	new Line2D.Double(camera.x, camera.y, camera.x, camera.y+camera.height);
	        intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);
	      }
	      else if(control.x>=camera.x){//control point is directly at right of the camera
	        intersectionSide=
	        	new Line2D.Double(camera.x+camera.width, camera.y, camera.x+camera.width, camera.y+camera.height);
	        intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);    	  
	      }    	
	    }
	    else if(control.x<camera.x){
	      if(control.y<camera.y){//control point is in a top-left position respect to the camera
	        intersectionSide=//trying the top side
	        	new Line2D.Double(camera.x, camera.y, camera.x+camera.width, camera.y);
	        intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);   
	        if(intersectionPoint==null){//trying the left side
	          intersectionSide=
	        	new Line2D.Double(camera.x, camera.y, camera.x, camera.y+camera.height);
	          intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);           	
	        }    	  
	      }
	      else if(control.y>camera.y+camera.height){//control point is in a bottom-left position respect to the camera
	    	intersectionSide=//trying the bottom side
	    	    new Line2D.Double(camera.x, camera.y+camera.height, camera.x+camera.width, camera.y+camera.height);
	    	intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);   
	    	if(intersectionPoint==null){//trying the left side
	    	  intersectionSide=
	    		new Line2D.Double(camera.x, camera.y, camera.x, camera.y+camera.height);
	    	  intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);           	
	    	}    	      	  
	      }
	    }
	    else if(control.x>camera.x){
	      if(control.y<camera.y){//control point is in a top-right position respect to the camera
	      	intersectionSide=//trying the top side
	            new Line2D.Double(camera.x, camera.y, camera.x+camera.width, camera.y);
	      	intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);   
	      	if(intersectionPoint==null){//trying the right side
	          intersectionSide=
	        	new Line2D.Double(camera.x+camera.width, camera.y, camera.x+camera.width, camera.y+camera.height);
	          intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);           	
	      	}    	      	        	  
	      }
	      else if(control.y>camera.y+camera.height){//control point is in a bottom-right position respect to the camera
	      	intersectionSide=//trying the bottom side
	      		new Line2D.Double(camera.x, camera.y+camera.height, camera.x+camera.width, camera.y+camera.height);
	      	intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);   
	      	if(intersectionPoint==null){//trying the right side
	      	  intersectionSide=
	            new Line2D.Double(camera.x+camera.width, camera.y, camera.x+camera.width, camera.y+camera.height);
	      	  intersectionPoint=getIntersectionPoint3(endLine, intersectionSide);
	      	}    	      	      	  
	      }
	    }
	    else{//control is inside of camera, the intersection point is arbitrary
	   	  intersectionPoint=new Point2D.Double(camera.x, camera.y+camera.height/2);    	
	    }
		return intersectionPoint;
	}	
	
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
	public static int getDegreeAngle(double centreX, double centreY, double pointX, double pointY, double radius) {
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
//    	double x=(anchor.getLocationOnScreen().getX()-splitterPanel.getLocationOnScreen().getX()+anchor.getWidth()/2);
//    	double y=(anchor.getLocationOnScreen().getY()-splitterPanel.getLocationOnScreen().getY()+anchor.getHeight()/2+3);
    	double x=(anchor.getLocationOnScreen().getX()-diagramPanel.getLocationOnScreen().getX()+anchor.getWidth()/2);
    	double y=(anchor.getLocationOnScreen().getY()-diagramPanel.getLocationOnScreen().getY()+anchor.getHeight()/2+3);
    	
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
    public Point getIntersectionPoint3(Line2D lineA, Line2D lineB){
        double x1 = lineA.getX1();
        double y1 = lineA.getY1();
        double x2 = lineA.getX2();
        double y2 = lineA.getY2();

        double x3 = lineB.getX1();
        double y3 = lineB.getY1();
        double x4 = lineB.getX2();
        double y4 = lineB.getY2();		  
    		  
    	double d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
    	if (d == 0.){
    	  System.out.println("d==0");
    	  return null;
    	}

    	int xi = (int)(((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d);
    	int yi = (int)(((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d);

    	Point p = new Point(xi,yi);
    	if (xi < Math.min(x1,x2) || xi > Math.max(x1,x2)) return null;
    	if (xi < Math.min(x3,x4) || xi > Math.max(x3,x4)) return null;

    	return p;
      }
    
    //if they're parallel to each other and they're not horizontal or vertical.
    /**
     * Returns a Point2D representing the intersection point of segments lineA and lineB,
     * or null if there is no intersection.<br>
     * If the two segments overlap, the intersection point is considered to be the end point of lineB<br>
     * owned by both lines. If one of the segment is a sub-segment of the other, one of the two end point of<br>
     * lineB is returned, without any assumption. 
     * 
     * @param lineA - line A
     * @param lineB - line B
     * @return the intersection point of lineA and lineB, if any, null otherwise
     */
    public Point getIntersectionPoint4(Line2D lineA, Line2D lineB){
    	double t=0, u=0;
    	Point p=null;
    	
        double x1 = lineA.getX1();
        double y1 = lineA.getY1();
        double x2 = lineA.getX2();
        double y2 = lineA.getY2();

        double x3 = lineB.getX1();
        double y3 = lineB.getY1();
        double x4 = lineB.getX2();
        double y4 = lineB.getY2();		  
    		  
        //using 2 variables(t and u) to calculate intersection based on line vectors
        double tmpY4MinusY3=(y4-y3);
        double tmpX4MinusX3=(x4-x3);
        double tmpX1MinusX2=(x1-x2);
        double tmpY2MinusY1=(y2-y1);
        double tmpY3MinusY1=(y3-y1);
    	double numerator=tmpY3MinusY1*tmpX4MinusX3+(x1-x3)*tmpY4MinusY3;
    	double denominator =tmpY2MinusY1*tmpX4MinusX3+tmpX1MinusX2*tmpY4MinusY3; 
    	
    	if (denominator == 0.){//the two lines are parallel or overlapping
    	  System.out.println("d=0");
    	  //checking if the lines are vertical or horizontal
    	  if(x1==x2){//lines are vertical
    		if(y3<=y4){
    		  if( (y1<y3 && y2<y3) || (y1>y4 && y2>y4)) return null;
    		  if(y1<y3 || y2<y3) return new Point((int)x3, (int)y3);
    		  else return new Point((int)x4, (int)y4);
    		}
    		else{
    		  if( (y1<y4 && y2<y4) || (y1>y3 && y2>y3)) return null;
    		  if(y1<y4 || y2<y4) return new Point((int)x4, (int)y4);
    		  else  return new Point((int)x3, (int)y3);
    		}
    	  }
    	  if(y1==y2){//lines are horizontal
      		if(x3<=x4){
      		  if( (x1<x3 && x2<x3) || (x1>x4 && x2>x4)) return null;
    		  if(x1<x3 || x2<x3) return new Point((int)x3, (int)y3);
    		  else return new Point((int)x4, (int)y4);      		  
      		}
      		else{
      		  if( (x1<x4 && x2<x4) || (x1>x3 && x2>x3)) return null;
    		  if(x1<x4 || x2<x4) return new Point((int)x4, (int)y4);
    		  else return new Point((int)x3, (int)y3);      			
      		}    		  
    	  }
    	  else{//lines are not horizontal nor vertical, checking the X-axis projections
        	if(x3<=x4){
              if( (x1<x3 && x2<x3) || (x1>x4 && x2>x4)) return null;
              if(x1<x3 || x2<x3) return new Point((int)x3, (int)y3);
              else return new Point((int)x4, (int)y4);      		  
        	}
        	else{
        	  if( (x1<x4 && x2<x4) || (x1>x3 && x2>x3)) return null;
        	  if(x1<x4 || x2<x4) return new Point((int)x4, (int)y4);
        	  else return new Point((int)x3, (int)y3);      			
        	}     		  
    	  }
    	}

    	t=numerator/denominator;
    	if(t<0. || t>1.) return null;//no intersection
    	
    	if(tmpY4MinusY3==0.){//lineB is horizontal, but lines are not parallel
    		if( (int)(y1+t*tmpY2MinusY1)==(int)y4){
              if(x3<=x4){
            	if((int)(x1+t*(-tmpX1MinusX2))>=(int)x3 || (int)(x1+t*(-tmpX1MinusX2))<= (int)x4)
            	  return new Point((int)(x1+t*(-tmpX1MinusX2)), (int)(y1+t*tmpY2MinusY1));
              }
              else{
              	if((int)(x1+t*(-tmpX1MinusX2))>=(int)x4 || (int)(x1+t*(-tmpX1MinusX2))<= (int)x3)
              	  return new Point((int)(x1+t*(-tmpX1MinusX2)), (int)(y1+t*tmpY2MinusY1));            	  
              }
    		}
    		else return null;//no intersection
    	}
    	
    	u=(-tmpY3MinusY1/tmpY4MinusY3)+t*tmpY2MinusY1/tmpY4MinusY3;
    	
    	if(u<0. || u>1.) return null;//no intersection
    			
    	int xi = (int)(x1+t*(-tmpX1MinusX2));
    	int yi = (int)(y1+t*tmpY2MinusY1);
    	
    	p = new Point(xi,yi);
    	System.out.println("t="+t+"\tu="+u
    			+"\n-tmpY3MinusY1="+(-tmpY3MinusY1)+"\ttmpY4MinusY3="+tmpY4MinusY3
    			+"\ntmpY2MinusY1="+(tmpY2MinusY1)+"\ttmpY4MinusY3="+tmpY4MinusY3
    			+"\nPoint="+p);
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
	  boolean leftCollision=false, upperCollision=false;
	  boolean rightCollision=false, bottomCollision=false;
	  boolean mustResizeX=false, mustResizeY=false;
	  boolean mustScrollX=false, mustScrollY=false;
	  boolean mustShiftX=false, mustShiftY=false;
	  JComponent nearestElementX=null, nearestElementY=null;
	  Point location=null;
	  Dimension diagramSize=null;
	  
	  //calculating move
	  moveX = e.getX()-lastPositionX;
	  moveY = e.getY()-lastPositionY;

	  //checking if some elements in the group collide with borders
	  collidedElementX.clear();
	  collidedElementY.clear();
	  for(JComponent element : selectionGroupFocused){	
		newLocationX=element.getX()+moveX;
		newLocationY=element.getY()+moveY;
		if(newLocationX<0){
		  leftCollision=true; collidedElementX.add(element);
		}
		if(newLocationX+element.getWidth()>diagramPanel.getWidth()){
		  rightCollision=true; collidedElementX.add(element);
		}
		if(newLocationY<0){
		  upperCollision=true; collidedElementY.add(element);
		}
		if(newLocationY+element.getHeight()>diagramPanel.getHeight()){
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
//		  Dimension diagramSize= diagramPanel.getPreferredSize();
//		  diagramSize.width+=20;
//		  diagramPanel.setPreferredSize(diagramSize);
//		  diagramPanel.revalidate();
//		  shiftAllDraggablesButGroupHorizontal(20, selectionGroupFocused);
		}
		adjustedMoveX=-nearestElementX.getX();
		lastPositionX=lastPositionX+adjustedMoveX;
	  }
	  if(rightCollision){
		nearestElementX=collidedElementX.get(0);
		for(int k=1; k<collidedElementX.size(); ++k)
		  if(collidedElementX.get(k).getX()+collidedElementX.get(k).getWidth()>
		  	 nearestElementX.getX()+nearestElementX.getWidth()) nearestElementX=collidedElementX.get(k);				
			
		newLocationX=nearestElementX.getX()+moveX;
			
		//resizing diagram and setting scrollbar to max
		if(newLocationX+nearestElementX.getWidth()>diagramPanel.getWidth()+10 && moveX>=lastMoveX){
		  mustResizeX=true; mustScrollX=true;
//		  Dimension diagramSize= diagramPanel.getPreferredSize();
//		  diagramSize.width+=20;
//		  diagramPanel.setPreferredSize(diagramSize);
//		  diagramPanel.revalidate();
//		  diagramScroller.getHorizontalScrollBar().setValue(
//				  diagramScroller.getHorizontalScrollBar().getMaximum());				
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
//		  Dimension diagramSize= diagramPanel.getPreferredSize();
//		  diagramSize.height+=20;
//		  diagramPanel.setPreferredSize(diagramSize);
//		  diagramPanel.revalidate();
//		  shiftAllDraggablesButGroupVertical(20, selectionGroupFocused);
		}
		adjustedMoveY=-nearestElementY.getY();
		lastPositionY=lastPositionY+adjustedMoveY;
	  }
	  if(bottomCollision){
		nearestElementY=collidedElementY.get(0);
		for(int k=1; k<collidedElementY.size(); ++k)
		  if( collidedElementY.get(k).getY()+collidedElementY.get(k).getHeight()>
		  	  nearestElementY.getY()+nearestElementY.getHeight()) nearestElementY=collidedElementY.get(k);				
			
		newLocationY=nearestElementY.getY()+moveY;
			
		//resizing diagram and setting scrollbar to max
		if(newLocationY+nearestElementY.getHeight()>diagramPanel.getHeight()+10 && moveY>=lastMoveY){
		  mustResizeY=true; mustScrollY=true;
//		  Dimension diagramSize= diagramPanel.getPreferredSize();
//		  diagramSize.height+=20;
//		  diagramPanel.setPreferredSize(diagramSize);
//		  diagramPanel.revalidate();
//		  diagramScroller.getVerticalScrollBar().setValue(
//				  diagramScroller.getVerticalScrollBar().getMaximum());				
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
		shiftAllDraggablesButGroupBothDirections(mustResizeX? 20:0, mustResizeY? 20:0, selectionGroupFocused);
	  //setting scrollbars to max if necessary
	  if(mustScrollX)
		diagramScroller.getHorizontalScrollBar().setValue(diagramScroller.getHorizontalScrollBar().getMaximum());
	  if(mustScrollY)
		diagramScroller.getVerticalScrollBar().setValue(diagramScroller.getVerticalScrollBar().getMaximum());

	  System.out.println("nearestElementX "+nearestElementX+" newLocationX: "+newLocationX);
	  System.out.println("nearestElementY "+nearestElementY+" newLocationY: "+newLocationY);
		
	  if(!leftCollision && !rightCollision) adjustedMoveX=moveX;
	  if(!upperCollision && !bottomCollision) adjustedMoveY=moveY;

	  System.out.println("adjustedMoveX "+adjustedMoveX+" moveX: "+moveX);
	  System.out.println("adjustedMoveY "+adjustedMoveY+" moveY: "+moveY);
	  	  
	  //moving selection components
	  if(adjustedMoveX!=0 || adjustedMoveY!=0) for(JComponent element : selectionGroupFocused){		
		location=element.getLocation();
		location.x+=adjustedMoveX;
		location.y+=adjustedMoveY;
		element.setLocation(location);		  
	  }
			  
			  
//		for(JComponent element : selectionGroupFocused){		  
//		  newLocationX=element.getX()+moveX;
//		  newLocationY=element.getY()+moveY;
//		  
//		  //the feature must not be dragged beyond the borders of the diagram panel
//		  
//		  //checking horizontal borders
//		  if( newLocationX<0 ){
//			newLocationX=1;
//			leftCollision=false;
//			adjustedMoveX=newLocationX-element.getX();
//		  }
//		  if( diagramPanel.getWidth()<=newLocationX+element.getWidth() ){
//			newLocationX=diagramPanel.getWidth()-element.getWidth()-1;
//			leftCollision=false;
//			adjustedMoveX=newLocationX-element.getX();
//		  }
//		  
//		  //checking vertical borders
//		  if( newLocationY<0 ){
//			newLocationY=1;
//			upperCollision=false;
//			adjustedMoveY=newLocationY-element.getY();
//		  }
//		  if( diagramPanel.getHeight()<=newLocationY+element.getHeight() ){
//			newLocationY=diagramPanel.getHeight()-element.getHeight()-1;
//			upperCollision=false;
//			adjustedMoveY=newLocationY-element.getY();
//		  }
//
//		  //adjusting last drag position depending on eventual border collisions
//		  if(leftCollision) lastPositionX=e.getX();
//		  else lastPositionX=lastPositionX+adjustedMoveX;
//
//		  if(upperCollision) lastPositionY=e.getY();
//		  else lastPositionY=lastPositionY+adjustedMoveY;
//
//		  if(!leftCollision&&!upperCollision) break;
//		  element.setLocation(newLocationX, newLocationY);
//		}
//		

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
		  boolean normalUpdateX=true, normalUpdateY=true;

		  moveX = e.getX()-lastPositionX;
		  moveY = e.getY()-lastPositionY;
		  newLocationX=element.getX()+moveX;
		  newLocationY=element.getY()+moveY;
		  
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
		  else if( newLocationX+element.getWidth()>diagramPanel.getWidth() ){
			if( newLocationX+element.getWidth()>diagramPanel.getWidth()+10 
				&& moveX>=lastMoveX){
			  Dimension diagramSize= diagramPanel.getPreferredSize();
			  diagramSize.width+=20;
			  diagramPanel.setPreferredSize(diagramSize);
			  diagramPanel.revalidate();
			  diagramScroller.getHorizontalScrollBar().setValue(
				diagramScroller.getHorizontalScrollBar().getMaximum());				
			}
			newLocationX=diagramPanel.getWidth()-element.getWidth();
			lastPositionX=lastPositionX+(newLocationX-element.getX());
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
		  else if( newLocationY+element.getHeight()>=diagramPanel.getHeight() ){
			if( newLocationY+element.getHeight()>diagramPanel.getHeight()+10 
				&& moveY>=lastMoveY){
			  Dimension diagramSize= diagramPanel.getPreferredSize();
			  diagramSize.height+=20;
			  diagramPanel.setPreferredSize(diagramSize);
			  diagramPanel.revalidate();
			  diagramScroller.getVerticalScrollBar().setValue(
					  diagramScroller.getVerticalScrollBar().getMaximum());				
			}
			newLocationY=diagramPanel.getHeight()-element.getHeight();
			lastPositionY=lastPositionY+(newLocationY-element.getY());
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
	 * Shifts horizontally the position of all draggables in the diagram panel.<br>
	 * If element is not null and is a draggable, it is not shifted.
	 * 
	 * @param enlargeX - the amount of horizontal shift
	 * @param element - the JComponent that must not be shifted
	 */
	private void shiftAllDraggablesButOneHorizontal(int enlargeX, JComponent element) {
	  OrderedListNode tmp= visibleOrderDraggables.getFirst();
	  Point location=null;
	  while (tmp!=null){
		if( !((JComponent)tmp.getElement()).equals(element) 
		    && ((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)){
		  location=((JComponent)tmp.getElement()).getLocation();
		  location.x+=enlargeX;
		  ((JComponent)tmp.getElement()).setLocation(location);
		}
		tmp=tmp.getNext();
	  }		
	}

	/**
	 * Shifts horizontally the position of all draggables in the diagram panel.<br>
	 * If group is not null, its elements are not shifted.
	 * 
	 * @param enlargeX - the amount of horizontal shift
	 * @param group - the ArrayList<JComponent> of elements that must not be shifted. If null, all components will be shifted
	 */
	private void shiftAllDraggablesButGroupHorizontal(int enlargeX, ArrayList<JComponent> group) {
	  OrderedListNode tmp= visibleOrderDraggables.getFirst();
	  Point location=null;
	  if(group==null) group = new ArrayList<JComponent>();
	  while (tmp!=null){
		if( !group.contains((JComponent)tmp.getElement()) 
			&& ((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)){
		  location=((JComponent)tmp.getElement()).getLocation();
		  location.x+=enlargeX;
		  ((JComponent)tmp.getElement()).setLocation(location);
		}
		tmp=tmp.getNext();
	  }	
	}

	/**
	 * Shifts vertically the position of all draggables in the diagram panel.<br>
	 * If element is not null and is a draggable, it is not shifted.
	 * 
	 * @param enlargeY - the amount of vertical shift
	 * @param element - the JComponent that must not be shifted
	 */
	private void shiftAllDraggablesButOneVertical(int enlargeY, JComponent element) {
	  OrderedListNode tmp= visibleOrderDraggables.getFirst();
	  Point location=null;
	  while (tmp!=null){
		if( !((JComponent)tmp.getElement()).equals(element) 
			&& ((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)){
		  location=((JComponent)tmp.getElement()).getLocation();
		  location.y+=enlargeY;
		  ((JComponent)tmp.getElement()).setLocation(location);
		}
		tmp=tmp.getNext();
	  }		
	}

	/**
	 * Shifts vertically the position of all draggables in the diagram panel.<br>
	 * If group is not null, its elements are not shifted.
	 * 
	 * @param enlargeY - the amount of vertical shift
	 * @param group - the ArrayList<JComponent> of elements that must not be shifted. If null, all components will be shifted
	 */
	private void shiftAllDraggablesButGroupVertical(int enlargeY, ArrayList<JComponent> group) {
	  OrderedListNode tmp= visibleOrderDraggables.getFirst();
	  Point location=null;
	  if(group==null) group = new ArrayList<JComponent>();
	  while (tmp!=null){
		if( !group.contains((JComponent)tmp.getElement()) 
			&& ((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)){
		  location=((JComponent)tmp.getElement()).getLocation();
		  location.y+=enlargeY;
		  ((JComponent)tmp.getElement()).setLocation(location);
		}
		tmp=tmp.getNext();
	  }	
	}

	/**
	 * Shifts in both direction the position of all draggables in the diagram panel.<br>
	 * If group is not null, its elements are not shifted.
	 * 
	 * @param enlargeY - the amount of vertical shift
	 * @param enlargeY - the amount of horizontal shift
	 * @param group - the ArrayList<JComponent> of elements that must not be shifted. If null, all components will be shifted
	 */
	private void shiftAllDraggablesButGroupBothDirections(int enlargeX, int enlargeY, ArrayList<JComponent> group) {
	  OrderedListNode tmp= visibleOrderDraggables.getFirst();
	  Point location=null;
	  if(group==null) group = new ArrayList<JComponent>();
	  while (tmp!=null){
		if( !group.contains((JComponent)tmp.getElement()) 
			&& ((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)){
		  location=((JComponent)tmp.getElement()).getLocation();
		  location.x+=enlargeX;
		  location.y+=enlargeY;
		  ((JComponent)tmp.getElement()).setLocation(location);
		}
		tmp=tmp.getNext();
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
			
//			underlyingPanel=(FeaturePanel)tmpNode.getElement();
			underlyingComponent=(JComponent)tmpNode.getElement();			  

			//checking if anchor must be added to a group
			anchorPanelOnScreenX=(int)lastAnchorFocused.getLocationOnScreen().getX();
			anchorPanelOnScreenY=(int)lastAnchorFocused.getLocationOnScreen().getY();
			relativePosition = new Point((int)(anchorPanelOnScreenX-underlyingComponent.getLocationOnScreen().getX()),
					(int)(anchorPanelOnScreenY-underlyingComponent.getLocationOnScreen().getY()) );	
			  
			if( lastAnchorFocused.getName().startsWith(startMandatoryNamePrefix)
			   || lastAnchorFocused.getName().startsWith(startOptionalNamePrefix) ){
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
		  //anchor dropped directly over the diagram panel, checking if anchor must be added to a group
		  if ( (lastAnchorFocused.getName().startsWith(startMandatoryNamePrefix)
				|| lastAnchorFocused.getName().startsWith(startOptionalNamePrefix) ) && 
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
	 * @return - the group's underlying JComponent
	 */
	public Component dropGroupOnDiagram(MouseEvent e) {
	  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
	  while(tmpNode!=null){	  
	    if (/*tmpNode.getElement().getClass().equals(FeaturePanel.class) 
	    	&&*/ ((JComponent)tmpNode.getElement()).getName().startsWith(featureNamePrefix)
	    	&& ((JComponent)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY()) ){			
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
	 * Drops a constraint on the diagram panel, adding it to the underlying feature panel, if any is present.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 * @return - the constraint's underlying JComponent
	 */
	public Component dropConstraintOnDiagram(MouseEvent e) {
	  OrderedListNode tmpNode=visibleOrderDraggables.getFirst();
	  while(tmpNode!=null){	  
	    if (/*tmpNode.getElement().getClass().equals(FeaturePanel.class) 
	    	&&*/ ((JComponent)tmpNode.getElement()).getName().startsWith(featureNamePrefix)
	    	&& ((JComponent)tmpNode.getElement()).getBounds().contains(e.getX(), e.getY()) ){			
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
			
		  /* ***DEBUG*** */
		  if(debug)System.out.println("Checking: "+((Component)tmpNode.getElement()).getName());
		  /* ***DEBUG*** */

		  //if it's not a feature panel, we check that it's not anchored to a feature panel
		  if (!((Component)tmpNode.getElement()).getName().startsWith(featureNamePrefix)){

			comp = (JComponent) diagramPanel.getComponentAt(				
					(int)(locOnScreen.getX()-diagramPanel.getLocationOnScreen().getX()), 
					(int)(locOnScreen.getY()-diagramPanel.getLocationOnScreen().getY()) );

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

	/**
	 * Adds a new feature to the diagram panel, incrementing featuresCount.
	 */
	public void addNewFeatureToDiagram() {
		addFeatureToDiagram(null);
	}
	
	/**
	 * Adds a new named feature to the diagram panel, incrementing featuresCount.
	 */
	public void addNamedFeatureToDiagram() {		
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

	  if (name==null){
		name=featureNamePrefix+featuresCount;
		featureColor=Color.BLACK;
	  }
	  else featureColor = getNewColor(termsColor.get(name));		
		
	  FeaturePanel newFeature=getDraggableFeature(name,
		toolDragPosition.x-(int)diagramPanel.getLocationOnScreen().getX(),
		toolDragPosition.y-(int)diagramPanel.getLocationOnScreen().getY(), 
		featureColor);

	  visibleOrderDraggables.addToTop(newFeature);
	  diagramPanel.setLayer(newFeature, 0);
	  diagramPanel.add(newFeature);
	  diagramPanel.setComponentZOrder(newFeature, 0);
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
		int actualPositionX=0;
		int actualPositionY=0;
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


			if(isActiveItem==activeItems.DRAGGING_TOOL_MANDATORY_LINK)
				newConnectorStartDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.START_MANDATORY_CONNECTOR, 
				toolDragPosition.x-(int)featurePanel.getLocationOnScreen().getX(),
				toolDragPosition.y-(int)featurePanel.getLocationOnScreen().getY()-5);			

			else if(isActiveItem==activeItems.DRAGGING_TOOL_OPTIONAL_LINK)
				newConnectorStartDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.START_OPTIONAL_CONNECTOR, 
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
		
		if(!startDotInsertedInPanel){
		  if(isActiveItem==activeItems.DRAGGING_TOOL_MANDATORY_LINK)
			newConnectorStartDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.START_MANDATORY_CONNECTOR,
								  actualPositionX, actualPositionY-5);			
					
		  else if(isActiveItem==activeItems.DRAGGING_TOOL_OPTIONAL_LINK)
			newConnectorStartDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.START_OPTIONAL_CONNECTOR,
								  actualPositionX, actualPositionY-5);								
		}
		
		ImageIcon lineLengthIcon = new ImageIcon(connectorLineLengthIconURL);
		ImageIcon startConnectorIcon = new ImageIcon(connectorStartDotIconURL);

		if(isActiveItem==activeItems.DRAGGING_TOOL_MANDATORY_LINK)
			  newConnectorEndDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.END_MANDATORY_CONNECTOR,
				  actualPositionX+lineLengthIcon.getIconWidth()+startConnectorIcon.getIconWidth(),
				  actualPositionY-5+lineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

		else if(isActiveItem==activeItems.DRAGGING_TOOL_OPTIONAL_LINK)
			  newConnectorEndDot=(AnchorPanel)getDraggableConnectionDot(ItemsType.END_OPTIONAL_CONNECTOR,
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
		cancelToolDrag();		
	}

	/**
	 * Adds a new connector to the diagram. If the starting connector dot is dropped over a feature panel, <br>
	 * it gets attached to it.
	 * 
	 * @param e - MouseEvent of the type Mouse Released.
	 */
	public void addConstraintToDiagram(MouseEvent e) {
		int actualPositionX=0;
		int actualPositionY=0;
		boolean startDotInsertedInPanel=false;
		ConstraintPanel newConstraintStartDot=null;			
		ConstraintPanel newConstraintEndDot=null;
		JComponent newConstraintControlPointDot=null;
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

			newConstraintStartDot=(ConstraintPanel)getDraggableConnectionDot(
			  (isActiveItem==activeItems.DRAGGING_TOOL_INCLUDES) ?
				ItemsType.START_INCLUDES_DOT : ItemsType.START_EXCLUDES_DOT,
			  toolDragPosition.x-(int)featurePanel.getLocationOnScreen().getX(),
			  toolDragPosition.y-(int)featurePanel.getLocationOnScreen().getY()-5);			

			moveComponentToTop(featurePanel);

			featurePanel.setLayer(newConstraintStartDot, 0);
			featurePanel.add(newConstraintStartDot);
			featurePanel.setComponentZOrder(newConstraintStartDot, 0);

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
			(isActiveItem==activeItems.DRAGGING_TOOL_INCLUDES) ?
			  ItemsType.START_INCLUDES_DOT : ItemsType.START_EXCLUDES_DOT,
			actualPositionX, actualPositionY-5);			
		
		ImageIcon lineLengthIcon = new ImageIcon(connectorLineLengthIconURL);
		ImageIcon startConnectorIcon = new ImageIcon(connectorStartDotIconURL);

		newConstraintEndDot=(ConstraintPanel)getDraggableConnectionDot(
		    (isActiveItem==activeItems.DRAGGING_TOOL_INCLUDES) ?
		      ItemsType.END_INCLUDES_DOT : ItemsType.END_EXCLUDES_DOT,
		    actualPositionX+lineLengthIcon.getIconWidth()+startConnectorIcon.getIconWidth(),
		    actualPositionY-5+lineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

		/* ***DEBUG*** */
		if(debug) System.out.println("Mouse released(Drag relative) on: ("+e.getX()+", "+e.getY()+")."
		  +"\nMouse released(Screen relative) on: ("+e.getXOnScreen()+", "+e.getYOnScreen()+")."
		  +"\nLocation where the Constraint will be placed: ("+toolDragPosition.x+", "+toolDragPosition.y+")."
		  +"\nActualPositionX: "+actualPositionX+"\nActualPositionY: "+actualPositionY);
		/* ***DEBUG*** */
		
		visibleOrderDraggables.addToTop(newConstraintStartDot);
		if(!startDotInsertedInPanel){
		  diagramPanel.setLayer(newConstraintStartDot, 0);
		  diagramPanel.add(newConstraintStartDot);
		  diagramPanel.setComponentZOrder(newConstraintStartDot, 0);
		}

		visibleOrderDraggables.addToTop(newConstraintEndDot);
		diagramPanel.setLayer(newConstraintEndDot, 0);
		diagramPanel.add(newConstraintEndDot);
		diagramPanel.setComponentZOrder(newConstraintEndDot, 0);

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

		addConstraintToDrawLists(
			newConstraintStartDot, 
		  (isActiveItem==activeItems.DRAGGING_TOOL_INCLUDES) ?
			ItemsType.START_INCLUDES_DOT : ItemsType.START_EXCLUDES_DOT);
		
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
		
		newGroupEndpoint1=(AnchorPanel)getDraggableConnectionDot(ItemsType.END_MANDATORY_CONNECTOR,
				actualPositionX+groupIcon.getIconWidth()/2-startConnectorIcon.getIconWidth()/2
				+groupLineLengthIcon.getIconWidth(),
				actualPositionY-5+groupLineLengthIcon.getIconHeight()+startConnectorIcon.getIconHeight());

		newGroupEndpoint2=(AnchorPanel)getDraggableConnectionDot(ItemsType.END_MANDATORY_CONNECTOR,
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
			  imagePanel.setName(startIncludesNamePrefix+includesCount);
			  ++includesCount;
			}
			connectorIcon = new ImageIcon(constraintDotIconURL);
			break;
		  case END_INCLUDES_DOT:
			imagePanel = new ConstraintPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(endIncludesNamePrefix+includesCount);
			  ++includesCount;
			}
			connectorIcon = new ImageIcon(constraintDotIconURL);
			break;		
		  case START_EXCLUDES_DOT:
			imagePanel = new ConstraintPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(startExcludesNamePrefix+excludesCount);
			  ++excludesCount;
			}
			connectorIcon = new ImageIcon(constraintDotIconURL);
			break;
		  case END_EXCLUDES_DOT:
			imagePanel = new ConstraintPanel();  
			if(name!=null) imagePanel.setName(name);
			else{
			  imagePanel.setName(endExcludesNamePrefix+excludesCount);
			  ++excludesCount;
			}
			connectorIcon = new ImageIcon(constraintDotIconURL);
			break;
		  case CONSTRAINT_CONTROL_POINT:
			//returning the JLabel directly
			connectorIcon = new ImageIcon(constraintControlPointDotIconURL);
			imagePanel = new JLabel(connectorIcon);
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
		
//		JTextArea textLabel=null;		
//		textLabel=new JTextArea(name, 5, 10){
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public boolean contains(int x, int y){
//			  if(!isEditable()) return false;
//			  else return super.contains(x, y);
//			}
//		};
//
//		textLabel.setLineWrap(true);
//		textLabel.setWrapStyleWord(true);		
		
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
		container.setLayer(textLabel, layer);
		container.add(textLabel);
		container.setComponentZOrder(textLabel, layer);
		
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

		Dimension screenDim=Toolkit.getDefaultToolkit().getScreenSize();
		
		tmp=visibleOrderDraggables.getFirst();
		while(tmp!=null){
		  if (((JComponent)tmp.getElement()).getParent().getName().startsWith(EditorView.diagramPanelName)) break;
		  tmp=tmp.getNext();
		}
		if(tmp==null) return;//no draggables found
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
		//moving components and resizing diagram
		shiftAllDraggablesButGroupBothDirections(-diagramMinX, -diagramMinY, null);
		Dimension diagramSize = diagramPanel.getPreferredSize();
		diagramSize.width=(diagramMaxX-diagramMinX>screenDim.width)? diagramMaxX-diagramMinX : screenDim.width;
		diagramSize.height=(diagramMaxY-diagramMinY>screenDim.height)? diagramMaxY-diagramMinY : screenDim.height;
//		diagramSize.width=diagramMaxX-diagramMinX;
//		diagramSize.height=diagramMaxY-diagramMinY;
		diagramPanel.setPreferredSize(diagramSize);
		diagramPanel.revalidate();
		frameRoot.repaint();
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
	public void deleteFeature(JComponent feature) {
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
	  System.out.println("Group size: "+group.getMembers().size());
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
		
		/* ****DEBUG*** */
		if(debug) System.out.println("feature="+feature.getName()+"\tanchor="+anchor.getName());
		/* ****DEBUG*** */
		
		anchor.setLocation(
		  (int)(anchorPanelOnScreenX-diagramPanel.getLocationOnScreen().getX()),
		  (int)(anchorPanelOnScreenY-diagramPanel.getLocationOnScreen().getY()));
		diagramPanel.setLayer(anchor, 0);
		diagramPanel.add(anchor);
		diagramPanel.setComponentZOrder(anchor, 0);
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

	/** Returns the JFrame containing the search panels*/
	public JFrame getSearchFrame(){
		return searchFrame;
	}

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
		((FeaturePanel)popUpElement).getTextArea().setText(newName+"{C}");
		return;
	  }
	  
	  for(String tmp : startingVariabilities) if(tmp.compareTo(newName)==0){
		((FeaturePanel)popUpElement).getTextArea().setText(newName+"{V}");
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
	  
	  if(activate){//activating the visualization
		tmp = visibleOrderDraggables.getFirst();
		while(tmp!=null){
		  if(((JComponent)tmp.getElement()).getName().startsWith(featureNamePrefix)){
			featTmp = (FeaturePanel)tmp.getElement();
			found=false;
			  
			for(String comm : startingCommonalities) if(featTmp.getLabelName().compareTo(comm)==0){
		      featTmp.getTextArea().setText(comm+"{C}"); found=true; break;
			}
			  
			if(!found) for(String comm : startingVariabilities) if(featTmp.getLabelName().compareTo(comm)==0){
			  featTmp.getTextArea().setText(comm+"{V}"); found=true; break;
			}
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
				  
			for(String comm : startingCommonalities) if(featTmp.getLabelName().compareTo(comm+"{C}")==0){
			  featTmp.getTextArea().setText(comm); found=true; break;
			}
				  
			if(!found) for(String comm : startingVariabilities) if(featTmp.getLabelName().compareTo(comm+"{V}")==0){
			  featTmp.getTextArea().setText(comm); found=true; break;
			}
		  }
			  
		  tmp=tmp.getNext();
		}		  	  
	  }

	}
	
	@Override
	public void update(Observable o, Object arg) {
		System.out.println("Message received: "+arg);
		if(arg.equals("New Feature Correctly Added")) addNewFeatureToDiagram();		
		else if(arg.equals("Feature Renamed")) renameFeature();	
		else if(arg.equals("Feature Not Renamed")) undoRenameFeature();	
		else if(arg.equals("New Named Feature Correctly Added")) addNamedFeatureToDiagram();		
		else if(arg.equals("Feature Deleted")) deleteFeature(popUpElement);
		else if(arg.equals("Group Deleted")) deleteGroup(popUpElement);
		else if(arg.equals("Feature Not Deleted")) System.out.println("Cannot delete this feature!");
		else if(arg.equals("Two Features Directly Linked")) addAnchorToFeature();
		else if(arg.equals("Grouped a Feature") ) addAnchorToFeature();
		else if(arg.equals("Group Added To Feature") ) addAnchorToFeature();	
		else if(arg.equals("Merged a Connector") ) addStartAnchorToGroup();
		else if(arg.equals("Constraint Added") ) addAnchorToFeature();
		else if(arg.equals("Constraint Removed") ) detachAnchor(lastFeatureFocused, lastAnchorFocused);
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
				|| arg.equals("Group Not Removed From Feature")
				|| arg.equals("Constraint Not Added")
				|| arg.equals("Constraint Not Removed")
				|| arg.equals("Direct Link Not Destroyed") ){
			resetActiveItems();
		}
//		else if(arg.equals("Direct Link Not Destroyed") ) resetActiveItems();
	}
	
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
	 * Assigns a name to the SXFM file to be created as result of model exportation.
	 * 
	 * @return s - String representing the SXFM name, or null if dialog has been aborted
	 */
	public String assignNameImageDialog(){				
	  return assignNameDialog("Image filename: ");	  
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
    	if(!dir.isDirectory() && !dir.mkdirs()){
    		errorDialog("Save Directory can't be created.");
    		return null;
    	}

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
	 * Export the content of the diagram panel as a PNG file.
	 */
	public void exportAsImageFile(String imagesPath, String type){
	  String fileName=assignNameImageDialog();
	  //saving xml string on file
	  try{
		//checking if the diagrams save directory must be created
		File dir=new File(imagesPath);		
		if(!dir.isDirectory() && !dir.mkdirs() ) throw new IOException("Save Directory can't be created.");
	  }catch(IOException e){
		System.out.println("Can't create PNG save directory");
		e.printStackTrace();
	  }	  
	  
	  BufferedImage bi = new BufferedImage(diagramPanel.getSize().width,
			diagramPanel.getSize().height, BufferedImage.TYPE_INT_ARGB); 
	  Graphics g = bi.createGraphics();
	  diagramPanel.paint(g);  //this == JComponent
	  g.dispose();	  
	  
	  try{
		if(type.compareTo("PNG")==0) ImageIO.write(bi,"png", new File(imagesPath+"/"+fileName+".png"));
		else if(type.compareTo("GIF")==0){
//		  ImageIO.write(bi,"BMP", new File(imagesPath+"/"+fileName+".BMP"));
//		  ImageIO.write(bi,"bmp", new File(imagesPath+"/"+fileName+".bmp"));
//		  ImageIO.write(bi,"jpg", new File(imagesPath+"/"+fileName+".jpg"));
//		  ImageIO.write(bi,"JPG", new File(imagesPath+"/"+fileName+".JPG"));
//		  ImageIO.write(bi,"jpeg", new File(imagesPath+"/"+fileName+".jpeg"));
//		  ImageIO.write(bi,"wbmp", new File(imagesPath+"/"+fileName+".wbmp"));
//		  ImageIO.write(bi,"PNG", new File(imagesPath+"/"+fileName+".PNG"));
//		  ImageIO.write(bi,"JPEG", new File(imagesPath+"/"+fileName+".JPEG"));
//		  ImageIO.write(bi,"WBMP", new File(imagesPath+"/"+fileName+".WBMP"));
//		  ImageIO.write(bi,"GIF", new File(imagesPath+"/"+fileName+".GIF"));
		  ImageIO.write(bi,"gif", new File(imagesPath+"/"+fileName+".gif"));
		}
	  }catch(Exception e) {
		System.out.println("Error while exporting to "+type+" format");
		e.printStackTrace();
	  }
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
		ConstraintPanel constrTmp=null;
		ConstraintPanel endConstrTmp=null;
		JComponent constControlPoint=null;
		String startOwner=null;
		String endOwner=null;
		Iterator<Entry<String, int[]>> colorIter = null;
		Entry<String, int[]> colorEntry=null;
		int[] color=null;
		Color featColor=null;
		
		//saving diagram graphic elements data
		String savePath = pathProject + "/" + s + "_DiagView.xml"; 

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
				+"connectorsCount="+connectorsCount+" includesCount="+includesCount+" excludesCount="+excludesCount
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
	 * Loads a saved feature diagram from a file describing the graphic elements.
	 * 
	 * @param diagramDataPath - the file to load from
	 */
	public void loadSavedDiagram(String diagramDataPath) {
	  SAXParser saxParser = null;
	  InputStream stream = null;
	  SAXParserFactory saxFactory = SAXParserFactory.newInstance();
	  ViewXMLHandler xmlHandler = new ViewXMLHandler();
	  OrderedListNode tmp=null;
	  try {
		stream=new FileInputStream(diagramDataPath);
		System.out.println("EditorView: *** PARSING: "+diagramDataPath+" ***");
		saxParser = saxFactory.newSAXParser();
		saxParser.parse(stream, xmlHandler);

		System.out.println("\nResult of parsing:\n"
				+"Features:\n"+xmlHandler.featuresList
				+"\nConnectors:\n"+xmlHandler.connectorsList
				+"\nGroups:\n"+xmlHandler.groupsList
				+"\nConstraintsList:\n"+xmlHandler.constraintsList
				+"\nMisc:\n"+xmlHandler.misc
				+"\nStarting Commonalities:\n"+xmlHandler.startingComm
				+"\nStarting Variabilities:\n"+xmlHandler.startingVars
				+"\nFeatures Colors:\n"+xmlHandler.featureColors
				+"\n");

		if(xmlHandler.featureColors!=null) loadStartingTermsColor(xmlHandler.featureColors);
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
	  String[] rgbValues=null;
	  int[] rgbIntValues=null;
	  Color featureColor=null;
	  
	  for(String feature : features){
		//getting data of this feature
		featureData=feature.split(" ");

		//getting feature name
		featureName=featureData[0].substring(5);
		for(int k=1; k<featureData.length-4; ++k) featureName+=" "+featureData[k];

		//getting feature ID
		containerName=featureData[featureData.length-4].substring(9);
		
		//getting RGB color as a String
		rgbValues=featureData[featureData.length-3].split("-");		
		rgbIntValues=new int[3];
		System.out.println("rgbValues: ");
		for(String k: rgbValues) System.out.println("-"+k);
		rgbIntValues[0]=new Integer(rgbValues[0].substring(6));
		rgbIntValues[1]=new Integer(rgbValues[1]);
		rgbIntValues[2]=new Integer(rgbValues[2]);		
		featureColor=getNewColor(rgbIntValues);
		
//		featureColor = getNewColor(termsColor.get(featureName));

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
		FeaturePanel newFeature=buildFeaturePanel(featureName, containerName, x, y, featureColor);
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
		
		System.out.println("Printing connectors strings:");
		for (String s : connectorData) System.out.println("s: "+s);
		System.out.println("startConnectorName="+connectorData[0].substring(10));
		System.out.println("startOwnerName="+connectorData[2].substring(11));
		System.out.println("endConnectorName="+connectorData[3].substring(8));
		System.out.println("endOwnerName="+connectorData[5].substring(9));

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
			throw new RuntimeException("Couldn't find feature '"+startOwnerName+"' as owner of '"+startConnectorName+"'");

		  visibleOrderDraggables.addToTop(startConnector);
		  owner.setLayer(startConnector, 0);
		  owner.add(startConnector);
		  owner.setComponentZOrder(startConnector, 0);
		}
		
		
		//adding end connector
		if(endConnectorName.contains("OPTIONAL"))
			endConnector=(AnchorPanel)buildConnectionDot(ItemsType.END_OPTIONAL_CONNECTOR, endConnectorName, endX, endY);
		if(endConnectorName.contains("MANDATORY")) 
			endConnector=(AnchorPanel)buildConnectionDot(ItemsType.END_MANDATORY_CONNECTOR, endConnectorName, endX, endY);
		if(endOwnerName.length()==0){//adding connector to the diagram panel directly
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
		  member=(AnchorPanel)buildConnectionDot(ItemsType.END_MANDATORY_CONNECTOR, memberName, memberX, memberY);
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
	  String[] connectors=constraintsList.split("\n");
	  ConstraintPanel startConstraint=null, endConstraint=null;
	  JComponent constControlPoint=null;
	  FeaturePanel owner=null;
	  
	  for(String connector : connectors){
		constraintData=connector.split(" ");

		System.out.println("Printing constraint strings:");
		for (String s : constraintData) System.out.println("s: "+s);
		System.out.println("startConstraintName="+constraintData[0].substring(10));
		System.out.println("startOwnerName="+constraintData[2].substring(11));
		System.out.println("endConstraintName="+constraintData[3].substring(8));
		System.out.println("endOwnerName="+constraintData[5].substring(9));
		System.out.println("controlName="+constraintData[6].substring(12));

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

		System.out.println("startX="+startX+", startY="+startY+", endX="+endX+", endY="+endY);
		
		//getting data of control point of this constraint
		controlName=constraintData[6].substring(12);

		for (i=4; i<constraintData[7].length(); ++i) if (constraintData[7].charAt(i)=='.') break;
		
		System.out.println("constraintData[7].substring(4, "+i+")="+constraintData[7].substring(4, i)
						  +"\tconstraintData[7].substring("+i+"+1)="+constraintData[7].substring(i+1));

		controlX=Integer.valueOf(constraintData[7].substring(4, i));
		controlY=Integer.valueOf(constraintData[7].substring(i+1));		
		
		//adding constraint start anchor
		startConstraint=(ConstraintPanel)buildConnectionDot(
				startConstraintName.startsWith(startExcludesNamePrefix) ? 
				ItemsType.START_EXCLUDES_DOT : ItemsType.START_INCLUDES_DOT,
				startConstraintName, startX, startY);
		
		if(startOwnerName.length()==0){//adding anchor to the diagram panel directly
		  visibleOrderDraggables.addToTop(startConstraint);
		  diagramPanel.setLayer(startConstraint, 0);
		  diagramPanel.add(startConstraint);
		  diagramPanel.setComponentZOrder(startConstraint, 0);			
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
		  owner.setLayer(startConstraint, 0);
		  owner.add(startConstraint);
		  owner.setComponentZOrder(startConstraint, 0);
		}

		//adding constraint end anchor
		endConstraint=(ConstraintPanel)buildConnectionDot(
				endConstraintName.startsWith(endExcludesNamePrefix) ? 
				ItemsType.END_EXCLUDES_DOT : ItemsType.END_INCLUDES_DOT,
				endConstraintName, endX, endY);
		
		if(endOwnerName.length()==0){//adding anchor to the diagram panel directly
		  visibleOrderDraggables.addToTop(endConstraint);
		  diagramPanel.setLayer(endConstraint, 0);
		  diagramPanel.add(endConstraint);
		  diagramPanel.setComponentZOrder(endConstraint, 0);			
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
		  owner.setLayer(endConstraint, 0);
		  owner.add(endConstraint);
		  owner.setComponentZOrder(endConstraint, 0);
		}	
		
		//adding constraint control point
		constControlPoint=buildConnectionDot(ItemsType.CONSTRAINT_CONTROL_POINT, controlName, controlX, controlY);
		
		//adding anchor to the diagram panel directly
		constControlPoint.setLocation(controlX, controlY);
		visibleOrderDraggables.addToTop(constControlPoint);
		diagramPanel.setLayer(constControlPoint, 0);
		diagramPanel.add(constControlPoint);
		diagramPanel.setComponentZOrder(constControlPoint, 0);
	    constControlPoint.setVisible(true);
		
	    //setting other ends of constraint dots
	    startConstraint.setOtherEnd(endConstraint);
	    startConstraint.setControlPoint(constControlPoint);
	    endConstraint.setOtherEnd(startConstraint);
	    endConstraint.setControlPoint(constControlPoint);
//	    constControlPoint.setVisible(false);
	    
	    System.out.println("constControlPoint.getLocation(): "+constControlPoint.getLocation());

		//adding constraint to draw list
		addConstraintToDrawLists(startConstraint, (startConstraintName.startsWith(startIncludesNamePrefix)) ?
			ItemsType.START_INCLUDES_DOT : ItemsType.START_EXCLUDES_DOT);	  
	  
	  
//	  	startConstraint.setOtherEnd(endConstraint);
//		endConstraint.setOtherEnd(startConstraint);
//		
//		//adding connector to draw list
//		startConnectorDots.add(startConnector);
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
	  includesCount=Integer.valueOf(values[1].substring(14));
	  excludesCount=Integer.valueOf(values[2].substring(14));
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
		
		System.out.println("featureName: "+featureName+", rgbValues: "+lineElements[lineElements.length-1]);
		for(String k : rgbValues) System.out.println("rgb element: "+k);
//		System.out.println("Red="+rgbValues[0]+"Green="+rgbValues[1]+"Blue="+rgbValues[2]);

		rgbIntValues=new int[3];
		rgbIntValues[0]=new Integer(rgbValues[0]);
		rgbIntValues[1]=new Integer(rgbValues[1]);
		rgbIntValues[2]=new Integer(rgbValues[2]);
		termsColor.put(featureName, rgbIntValues);
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
	  diagramPanel.setLayer(controlPoint, 0);
	  diagramPanel.add(controlPoint);
	  diagramPanel.setComponentZOrder(controlPoint, 0);	
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
	protected JScrollPane getTabFeaturesCandidates(){
		JPanel featuresPanel = null, legendPanel=null;
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
    		if (menuViewCommsOrVars.isSelected()) featureName=featureName.substring(0, featureName.length()-3);

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
    	  if (menuViewCommsOrVars.isSelected()) featureName=featureName.substring(0, featureName.length()-3);

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
		searchPanel = new JPanel();
		searchPanel.setBackground(Color.WHITE);
//		searchPanel.setOpaque(true);
		searchPanel.setBounds(0, 0, 900, 700);
		searchPanel.setLayout(null);
		
		featuresPanel = new JPanel();
		featuresPanel.setBackground(Color.WHITE);
//		panelFeatures.setOpaque(true);
		featuresPanel.setBounds(5,5,770,200);
		featuresPanel.setLayout(new GridLayout(0, 1));
		featuresPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		//creating panelFeatures
//		colorRGB[0]=160; colorRGB[1]=160; colorRGB[2]=0;
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
		
//		colorRGB[0]=0; colorRGB[1]=160; colorRGB[2]=160;
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

//		colorRGB[0]=160; colorRGB[1]=0; colorRGB[2]=0;
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
		scrollingFeaturesPanel.setBounds(270, 10, 620, 210);
		
		//creating legend panel
		legendPanel = new JPanel();
		legendPanel.setBounds(5, 10, 260, 210);
		legendPanel.setLayout(null);
		
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
		legendPanel.add(passiveVarsColorLabel);
		legendPanel.add(nonExtractedColorLabel);
		legendPanel.add(activeCommColorLabel);
		legendPanel.add(activeVarsColorLabel);
		
		legendPanel.add(passiveCommTextLabel);
		legendPanel.add(passiveVarsTextLabel);
		legendPanel.add(nonExtractedTextLabel);
		legendPanel.add(activeCommTextLabel);
		legendPanel.add(activeVarsTextLabel);
		
		legendPanel.setBackground(Color.LIGHT_GRAY);
		legendPanel.setOpaque(true);
		
		//adding control buttons and a label for term occurences navigation
		XBackwardOccurrButton = new JButton("<<("+occurrJumpSpan+")");
		XBackwardOccurrButton.setBounds(30, 230, 100, 22);
		XBackwardOccurrButton.addActionListener(getOccurrNavButtonListener(-occurrJumpSpan));

		prevOccurrButton = new JButton("<");
		prevOccurrButton.setBounds(185, 230, 100, 22);
		prevOccurrButton.addActionListener(getOccurrNavButtonListener(-1));
		
		occurrsLabel = new JLabel("<html><div style=\"text-align: center;\">" + "x/y" + "</html>");

		occurrsLabelPanel = new JPanel();
		occurrsLabelPanel.add(occurrsLabel);
		occurrsLabelPanel.setBounds(315, 230, 270, 22);
		occurrsLabelPanel.setBackground(Color.LIGHT_GRAY);

		nextOccurrButton = new JButton(">");
		nextOccurrButton.setBounds(615, 230, 100, 22);
		nextOccurrButton.addActionListener(getOccurrNavButtonListener(1));

		XForwardOccurrButton = new JButton(">>("+occurrJumpSpan+")");
		XForwardOccurrButton.setBounds(770, 230, 100, 22);
		XForwardOccurrButton.addActionListener(getOccurrNavButtonListener(occurrJumpSpan));
		

		//adding text area for term occurences visualization		
		occursTabbedPane = new JTabbedPane();
		occursTabbedPane.setBounds(10, 270, 880, 390);
		
		//initializing utility maps
		textTabs = new HashMap<String, JTextArea>();
		textIndexes = new HashMap<String, HashMap<String, Integer>>();
		currentFiles = new HashMap<String, String>();
		lastHighlightedTag = new HashMap<String, HashMap<String, Object>>();
		lastRemovedHighlights = new HashMap<String, HashMap<String, ArrayList<Highlight>>>();

		//adding components to panel		
		searchPanel.add(legendPanel);
		searchPanel.add(scrollingFeaturesPanel);
		searchPanel.add(occursTabbedPane);

		return new JScrollPane(searchPanel);
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
		  searchPanel.add(nextOccurrButton);
		  nextOccurrButton.setVisible(true);
		  searchPanel.add(prevOccurrButton);
		  prevOccurrButton.setVisible(true);
		  searchPanel.add(XForwardOccurrButton);
		  XForwardOccurrButton.setVisible(true);
		  searchPanel.add(XBackwardOccurrButton);
		  XBackwardOccurrButton.setVisible(true);
		  searchPanel.add(occurrsLabelPanel);
		  occurrsLabelPanel.setVisible(true);
		  searchPanel.repaint();

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
//		  int occurrenceIndex =0;//current index in the text of occurrence to highlite
		  int[] occurrence=null;//current index in the text of occurrence to highlite
		  Object highlightTag=null;//highlight tag that will be added to the text
		  ArrayList<Highlight> lastRemovedTags=null;//last removed highlight tags
		  ArrayList<Highlight> tagsToRemove=null;//highlight tags to remove
		  
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
			  lastRemovedHighlights.put(currentSelectedFeatureName, new HashMap<String, ArrayList<Highlight>>());

		  //re-putting last removed highlighted commonality tags for this term and file
		  lastRemovedTags=lastRemovedHighlights.get(currentSelectedFeatureName).get(fileName);

		  if(lastRemovedTags!=null) try{
			for(int h=0; h<lastRemovedTags.size(); ++h)
			  hilite.addHighlight(lastRemovedTags.get(h).getStartOffset(), lastRemovedTags.get(h).getEndOffset(),
				  lastRemovedTags.get(h).getPainter());
		  } catch (BadLocationException e) {
			System.out.println("BadLocationException\nTerm: "+currentSelectedFeatureName+" - occurrence: "+occurrence);
			e.printStackTrace();
		  }

		  //checking what highlighted tags already are in next occurrence text interval
		  tagsToRemove = new ArrayList<Highlight>();
		  for (Highlight tmp: hilite.getHighlights())
//			if (tmp.getStartOffset()>=occurrence && tmp.getEndOffset()<=
//			    occurrence+relevantTermsVersions.get(currentSelectedFeatureName).get(fileName).length())
//			  tagsToRemove.add(tmp);
			if (tmp.getStartOffset()>=occurrence[0] && tmp.getEndOffset()<=occurrence[1]) tagsToRemove.add(tmp);

		  //removing highlight tags that already are in next occurrence text interval
		  for (Highlight tmp: tagsToRemove) hilite.removeHighlight(tmp);

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
//			occurrence, 
//			occurrence+relevantTermsVersions.get(currentSelectedFeatureName).get(fileName).length(),
//			occurrPainter));

		  } catch (BadLocationException e) {
			System.out.println("BadLocationException\nTerm: "+currentSelectedFeatureName+" - occurrence: "+occurrence);
			e.printStackTrace();
		  }

		  //set Caret position and text selection
		  jta.requestFocusInWindow();
		  jta.getCaret().setVisible(true);
		  jta.setCaretPosition(occurrence[0]);
		  //		  jta.setSelectionStart(occurrenceIndex);
		  //		  jta.setSelectionEnd(occurrenceIndex+checkBoxCommonalities.get(currentSelectedCheckBox).getText().length());
		  //		  jta.setSelectionEnd(occurrenceIndex+currentSelectedCheckBox.length());
		  //		  jta.setSelectionColor(Color.CYAN);

		  //updating current occurrence index for this file
		  textIndexes.get(currentSelectedFeatureName).put(fileName, currentIndex);
		  //		  textIndexes.get(checkBoxCommonalities.get(currentSelectedCheckBox).getText()).put(
		  //			  		((JScrollPane)occursTabbedPane.getSelectedComponent()).getName(), currentIndex);

		  //updating occurrences label
		  occurrsLabel.setText( (currentIndex+1)+"/"+occurrIndexesList.size()+"[Index: "+occurrence[0]+"]");


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
		    
    		/* ***VERBOSE****/
            if (debug4){
              System.out.println("uso lo StringReader");
              String tmpTest=null;
              StringReader strReader= new StringReader(s);
              BufferedReader bufReader= new BufferedReader(strReader);
              while((tmpTest=bufReader.readLine())!=null){
            	  System.out.println(tmpTest+"\n");
              }
              System.out.println("fatto con lo StringReader");
            }
    		/* ***VERBOSE****/
		    
		    return getRegisteredTabTextString(term, file, s, commonalitiesToHighlight, variabilitiesToHighlight);

		}catch(FileNotFoundException e){
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		}catch (IOException e) {
			System.out.println("Exception tabTextFile: " + e.getMessage());
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
					+". Creo la lista di indici per il termine "+term+"****\n");
    		/* ***DEBUG****/

			textIndexes.put(term, new HashMap<String, Integer>());
		}
		if (!textIndexes.get(term).containsKey(file)) textIndexes.get(term).put(file, 0);		

		if (commonalitiesToHighlight!=null || variabilitiesToHighlight!=null)
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
		String originalVersionFeaturename=null;
		ArrayList<int[]> occurrences = null;
		
		if(commonalitiesToHighlight==null && variabilitiesToHighlight==null) return jtc;
		try{   
			Highlighter hilite = jtc.getHighlighter();
		    
			Document doc = jtc.getDocument();
		    
			String text = doc.getText(0, doc.getLength());
		    
			//adding highlights to commonalities occurrences
			for(int i = 0; i < commonalitiesToHighlight.size(); i++){
			  occurrences = relevantTerms.get(commonalitiesToHighlight.get(i)).get(file);
			  for(int[] occurr: occurrences) hilite.addHighlight(occurr[0], occurr[1], passiveCommHighlightPainter);
			}
				
//			int pos = 0;
//			if(commonalitiesToHighlight!=null) for(int i = 0; i < commonalitiesToHighlight.size(); i++){
//				originalVersionFeaturename=relevantTermsVersions.get(commonalitiesToHighlight.get(i)).get(file);
//
//				while((pos = text.toUpperCase().indexOf(originalVersionFeaturename.toUpperCase(), pos)) >= 0){	
//					if(ModelProject.isValidOccurrence( originalVersionFeaturename, text, pos) )
//						hilite.addHighlight(pos, pos+originalVersionFeaturename.length(), passiveCommHighlightPainter);
//					
//					pos += originalVersionFeaturename.length();
//				}    
//			}		

			//adding highlights to variabilities occurrences
			for(int i = 0; i < variabilitiesToHighlight.size(); i++){
			  occurrences = relevantTerms.get(variabilitiesToHighlight.get(i)).get(file);
			  if(occurrences!=null) for(int[] occurr: occurrences) 
				hilite.addHighlight(occurr[0], occurr[1], passiveVarsHighlightPainter);
			}
			
//			if(variabilitiesToHighlight!=null) for(int i = 0; i < variabilitiesToHighlight.size(); i++){
//				originalVersionFeaturename=relevantTermsVersions.get(variabilitiesToHighlight.get(i)).get(file);
//
//				if(originalVersionFeaturename!=null)
//				  while((pos = text.toUpperCase().indexOf(originalVersionFeaturename.toUpperCase(), pos)) >= 0){	
//					if(ModelProject.isValidOccurrence(originalVersionFeaturename, text, pos) )
//						hilite.addHighlight(pos, pos+originalVersionFeaturename.length(), passiveVarsHighlightPainter);
//					
//					pos += originalVersionFeaturename.length();
//				  }    
//			}		
			
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
		  ArrayList<Highlight> lastRemovedTags=null;//last removed highlighted commonality tags 
		  ArrayList<Highlight> commTagsToRemove=null;//commonality tags to highlight
			
		  JTextArea jta= textTabs.get(file);
		  
		  System.out.println("\n***selectCurrentOccurrence***\n");
		  System.out.println(
			 "currentSelectedFeatureName: "+currentSelectedFeatureName+"\tfile: "+file
			+"\nlastHighlightedTag.get("+currentSelectedFeatureName+"): "+lastHighlightedTag.get(currentSelectedFeatureName)
			);
		  if(lastHighlightedTag.get(currentSelectedFeatureName)!=null)
			System.out.println(
	"lastHighlightedTag.get("+currentSelectedFeatureName+").get("+((JScrollPane)occursTabbedPane.getSelectedComponent()).getName()+"):\n"
	+lastHighlightedTag.get(currentSelectedFeatureName).get(((JScrollPane)occursTabbedPane.getSelectedComponent()).getName()));
			  
		  System.out.println("lastRemovedHighlights.get("+currentSelectedFeatureName+"): "
		  +lastRemovedHighlights.get(currentSelectedFeatureName));
		  if(lastRemovedHighlights.get(currentSelectedFeatureName)!=null){
			System.out.println("lastRemovedHighlights.get("+currentSelectedFeatureName+").get(file): ");
			for(Highlight tmp : lastRemovedHighlights.get(currentSelectedFeatureName).get(file))
			  System.out.println("*) "+tmp.toString());
			  
		  }
					  
		  int currentIndex= textIndexes.get(currentSelectedFeatureName).get(file);

		  //calculating current occurrence index for selection
		  occurrFilesList = relevantTerms.get(currentSelectedFeatureName);
//		  occurrFilesList = relevantTerms.get(checkBoxCommonalities.get(currentSelectedCheckBox).getText());
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
			  lastRemovedHighlights.put(currentSelectedFeatureName, new HashMap<String, ArrayList<Highlight>>());
		  
		  //re-putting last removed highlighted commonality tags for this term and file, if any
		  lastRemovedTags=lastRemovedHighlights.get(currentSelectedFeatureName).get(file);
		  
		  if(lastRemovedTags!=null) try{
		    for(int h=0; h<lastRemovedTags.size(); ++h)
			  hilite.addHighlight(lastRemovedTags.get(h).getStartOffset(), lastRemovedTags.get(h).getEndOffset(),
			  lastRemovedTags.get(h).getPainter());
		  } catch (BadLocationException e) {
			System.out.println("BadLocationException\nTerm: "+currentSelectedFeatureName+" - occurrence: "+occurrence);
			e.printStackTrace();
		  }
		  
		  //checking what highlighted tags already are in next occurrence text interval
		  commTagsToRemove = new ArrayList<Highlight>();
		  for (Highlight tmp: hilite.getHighlights())
			if (tmp.getStartOffset()>=occurrence[0] && tmp.getEndOffset()<=occurrence[1]) commTagsToRemove.add(tmp);

		  //removing highlighted tags that already are in next occurrence text interval
		  for (Highlight tmp: commTagsToRemove) hilite.removeHighlight(tmp);
		  
		  //saving last removed Commonality tags in lastRemovedCommHighlights
		  lastRemovedHighlights.get(currentSelectedFeatureName).put((
		    (JScrollPane)occursTabbedPane.getSelectedComponent()).getName(), 
		    commTagsToRemove);		  

		  
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
//		  jta.setSelectionStart(occurrenceIndex);
//		  jta.setSelectionEnd(occurrenceIndex+currentSelectedCheckBox.length());
//		  jta.setSelectionEnd(occurrenceIndex+checkBoxCommonalities.get(currentSelectedCheckBox).getText().length());
//		  jta.setSelectionColor(Color.CYAN);
		  
		  //updating occurrences label
		  occurrsLabel.setText( (currentIndex+1)+"/"+occurrIndexesList.size()+"[Index: "+occurrence[0]+"]");
		
	}		
	
}
