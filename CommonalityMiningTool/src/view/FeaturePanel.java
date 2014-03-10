package view;

//import java.awt.BasicStroke;
//import java.awt.Color;
import java.awt.Component;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Point;
//import java.awt.Rectangle;
//import java.awt.geom.Line2D;
//import java.awt.geom.Point2D;
//import java.awt.geom.Rectangle2D;



//import javax.swing.JComponent;
//import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JTextArea;
//import javax.swing.JPanel;
import javax.swing.JTextField;

public class FeaturePanel extends JLayeredPane {

//	/** variables used for debugging*/
//	private static boolean debug=false;
//	private static boolean debug2=false;
//	private static boolean debug3=true;

//	/** used to activate paint overriding*/
//	private static boolean activePaint=true;

	private static final long serialVersionUID = 1L;

	/** points used for drawing*/
//	private static Point start=new Point(), end=new Point();
	
//	/** the parent component*/
//	private static Component parent = null;
	
//	private static boolean hasParentLink = false;
	
	private String labelName=null;

//	private JTextField jta=null;
	private JTextArea jta=null;

	/**
	 * Creates a new default FeaturePanel. <br>
	 */
	public FeaturePanel(Component parent){
		super();
//		this.parent=parent;
	}
	
//	/**
//	 * Creates a new default FeaturePanel. <br>
//	 */
//	public FeaturePanel(JTextField jta){
//		super();
//		this.jta=jta;
//	}
	
	/**
	 * Creates a new default FeaturePanel. <br>
	 */
	public FeaturePanel(JTextArea jta){
		super();
		this.jta=jta;
	}

	/**
	 * Returns the ID of this feature.
	 * 
	 * @return - a String containing the ID.
	 */
	public String getID(){
		return getName();
	}

//	/**
//	 * Returns the name of this feature.
//	 * 
//	 * @return - a String containing the name.
//	 */
//	public String getLabelName(){
//		return labelName;
//	}

	/**
	 * Returns the name of this feature.
	 * 
	 * @return - a String containing the name.
	 */
	public String getLabelName(){
		return jta.getText();
	}

//	/**
//	 * Returns the JTextArea of this feature panel.
//	 * 
//	 * @return - a String containing the name.
//	 */
//	public JTextField getTextArea(){
//		return jta;
//	}

	/**
	 * Returns the JTextArea of this feature panel.
	 * 
	 * @return - a String containing the name.
	 */
	public JTextArea getTextArea(){
		return jta;
	}

//	/**
//	 * Sets the name of this feature node.
//	 * 
//	 * @return - a String containing the name.
//	 */
//	public void setLabelName(String labelName){
//		this.labelName=labelName;
//	}
	
	

}
