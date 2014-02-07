package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class FeaturePanel extends JLayeredPane {

	/** variables used for debugging*/
	private static boolean debug=false;
	private static boolean debug2=false;
	private static boolean debug3=true;

	/** used to activate paint overriding*/
	private static boolean activePaint=true;

	private static final long serialVersionUID = 1L;

	/** points used for drawing*/
	private static Point start=new Point(), end=new Point();
	
	/** the parent component*/
	private static Component splitterPanel = null;
	
	private static boolean hasParentLink = false;
	
	private String labelName=null;

	
	public FeaturePanel(Component parent){
		super();
		splitterPanel=parent;
	}

	public String getLabelName(){
		return labelName;
	}

	public void setLabelName(String labelName){
		this.labelName=labelName;
	}
	
	

}
