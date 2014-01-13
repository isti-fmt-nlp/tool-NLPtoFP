package main;

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

	
	public FeaturePanel(Component parent){
		super();
		splitterPanel=parent;
	}
	
	@Override
	public void paint(Graphics g){
	  /* ***DEBUG*** */
	  if(debug2) System.out.println("Sono "+this.getName());
	  /* ***DEBUG*** */

	  AnchorPanel anchor=null;
	  GroupPanel group=null;
	  JComponent otherEnd=null;
	  Line2D line=null;
	  Rectangle2D rectangle=null;
	  Point2D[] intersectionPoints=null;
	  
	  super.paint(g);

	  activePaint=false;
	  if(!activePaint) return;
	  
//	  Graphics2D g2 = (Graphics2D)g.create();
	  if(!splitterPanel.isVisible()) return;
	  Graphics2D g2 = (Graphics2D)splitterPanel.getGraphics().create();
	  g2.setStroke(new BasicStroke(2.5F));  
	  g2.setColor(Color.ORANGE);
	  
	  Component[] children=this.getComponents();
	  for(Component comp: children){
		if (comp.getClass().equals(AnchorPanel.class)){
		  //getting the intersection point
		  anchor=(AnchorPanel)comp;
		  otherEnd=anchor.getOtherEnd();
//		  rectangle=this.getBounds();
//		  rectangle=this.getVisibleRect();

//		  rectangle=new Rectangle((int)this.getLocationOnScreen().getX(), (int)this.getLocationOnScreen().getY(),
//				  				  (int)this.getSize().getWidth(), (int)this.getSize().getHeight());
//		  line=new Line2D.Double((int)anchor.getLocationOnScreen().getX(), (int)anchor.getLocationOnScreen().getY(),
//				  			 	 (int)otherEnd.getLocationOnScreen().getX(), (int)otherEnd.getLocationOnScreen().getY() );
		  rectangle=new Rectangle(this.getLocationOnScreen(), this.getSize());
		  line=new Line2D.Double(anchor.getLocation(), otherEnd.getLocation());
		  
		  /*prova */
		  Rectangle rectangle2=new Rectangle((int)(rectangle.getX()-splitterPanel.getLocationOnScreen().getX()),
			(int)(rectangle.getY()-splitterPanel.getLocationOnScreen().getY()),
			(int)rectangle.getWidth(), (int)rectangle.getHeight());
		  Line2D line2=new Line2D.Double(anchor.getLocation(), otherEnd.getLocation());
		  g2.setColor(Color.BLUE);
		  g2.draw(rectangle2);
		  g2.drawLine((int)(line2.getX1()-splitterPanel.getLocationOnScreen().getX()),
			(int)(line2.getY1()-splitterPanel.getLocationOnScreen().getY()), 
			(int)(line2.getX2()-splitterPanel.getLocationOnScreen().getX()),  
			(int)(line2.getY2()-splitterPanel.getLocationOnScreen().getY()) );
		  g2.setColor(Color.ORANGE);
		  /*prova */
		  
		  
		  intersectionPoints=getIntersectionPoints(line, rectangle);
		  for (Point2D endPoint: intersectionPoints){

			/* ***DEBUG*** */
			if(debug2) System.out.println("Printing lines over panels");
			/* ***DEBUG*** */

			if(endPoint!=null){
				g2.drawLine((int)(anchor.getLocationOnScreen().getX()-splitterPanel.getLocationOnScreen().getX()),
				(int)(anchor.getLocationOnScreen().getY()-splitterPanel.getLocationOnScreen().getY()), 
				(int)(endPoint.getX()-splitterPanel.getLocationOnScreen().getX()), 
				(int)(endPoint.getY()-splitterPanel.getLocationOnScreen().getY()) );

				/* ***DEBUG*** */
				if(debug2)
				  System.out.println("anchor.getLocationOnScreen().getX()="+anchor.getLocationOnScreen().getX()
					+"\nanchor.getLocationOnScreen().getY()="+anchor.getLocationOnScreen().getY()
					+"\nendPoint.getX()="+endPoint.getX()
					+"\nendPoint.getY()="+endPoint.getY()
					);
				/* ***DEBUG*** */
				
			}
//			if(endPoint!=null) g2.drawLine(anchor.getX(), anchor.getY(), (int)endPoint.getX(), (int)endPoint.getY());

		  }
		}

	  
	  
	  }
	}

	private static void drawConnectionLine(Graphics2D g2, JPanel startPanel, JPanel endPanel) {
//		start.setLocation(startPanel.getLocationOnScreen());
//		end.setLocation(endPanel.getLocationOnScreen());
		start.setLocation(startPanel.getLocation());
		end.setLocation(endPanel.getLocation());
		g2.drawLine(
//				  (int)(start.getX()-splitterPanel.getLocationOnScreen().getX()+startPanel.getWidth()/2),
//				  (int)(start.getY()-splitterPanel.getLocationOnScreen().getY()+startPanel.getHeight()/2+3),
//				  (int)(end.getX()-splitterPanel.getLocationOnScreen().getX()+endPanel.getHeight()/2-3),
//				  (int)(end.getY()-splitterPanel.getLocationOnScreen().getY()+endPanel.getHeight()/2+2) );
		  (int)(start.getX()),
		  (int)(start.getY()),
		  (int)(end.getX()),
		  (int)(end.getY()) );
	};

	/**
	 * Returns an array of Point2D representing the intersection point between line and rectangle.
	 * 
	 * @param line - the Line2D Object
	 * @param rectangle - Rectangle2D Object
	 * @return an array of Point2D representing the intersection points
	 */
    public Point2D[] getIntersectionPoints(Line2D line, Rectangle2D rectangle) {

    	Line2D top=null, bottom=null, left=null, right=null;
        Point2D[] p = new Point2D[4];

        
//        // Top line
//        top=new Line2D.Double(rectangle.getX(), rectangle.getY(),
//        		rectangle.getX() + rectangle.getWidth(), rectangle.getY());
//        
//        // Bottom line
//        bottom=new Line2D.Double(rectangle.getX(), rectangle.getY() + rectangle.getHeight(),
//        		rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight());
//
//        //Left line
//        left=new Line2D.Double(rectangle.getX(), rectangle.getY(),
//        		rectangle.getX(), rectangle.getY() + rectangle.getHeight());
//
//        // Right line
//        right=new Line2D.Double(rectangle.getX() + rectangle.getWidth(), rectangle.getY(),
//        		rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight());
        
        // Top line
        top=new Line2D.Double((int)rectangle.getX(), (int)rectangle.getY(),
        		(int)(rectangle.getX() + rectangle.getWidth()), (int)rectangle.getY());
        
        // Bottom line
        bottom=new Line2D.Double((int)rectangle.getX(), (int)(rectangle.getY() + rectangle.getHeight()),
        		(int)(rectangle.getX() + rectangle.getWidth()), (int)(rectangle.getY() + rectangle.getHeight()));

        //Left line
        left=new Line2D.Double((int)rectangle.getX(), (int)rectangle.getY(),
        		(int)rectangle.getX(), (int)(rectangle.getY() + rectangle.getHeight()));

        // Right line
        right=new Line2D.Double((int)(rectangle.getX() + rectangle.getWidth()), (int)rectangle.getY(),
        		(int)(rectangle.getX() + rectangle.getWidth()), (int)(rectangle.getY() + rectangle.getHeight()));

        p[0] = getIntersectionPoint(line, top);
        p[1] = getIntersectionPoint(line, bottom);
        p[2] = getIntersectionPoint(line, left);
        p[3] = getIntersectionPoint(line, right);

        /* ***DEBUG*** */
        if (debug3){
//          System.out.println("Before\np[1]="+p[0]+"\np[2]="+p[1]+"\np[3]="+p[2]+"\np[4]"+p[3]);
//          System.out.println("top.intersectsLine(new Line2D.Double(p[0], p[0])?"+top.intersectsLine(new Line2D.Double(p[0], p[0])));
//          System.out.println("line.intersectsLine(new Line2D.Double(p[0], p[0])?"+line.intersectsLine(new Line2D.Double(p[0], p[0])));
//          System.out.println("bottom.intersectsLine(new Line2D.Double(p[1], p[1])?"+bottom.intersectsLine(new Line2D.Double(p[1], p[1])));
//          System.out.println("line.intersectsLine(new Line2D.Double(p[1], p[1])?"+line.intersectsLine(new Line2D.Double(p[1], p[1])));
//          System.out.println("left.intersectsLine(new Line2D.Double(p[2], p[2])?"+left.intersectsLine(new Line2D.Double(p[2], p[2])));
//          System.out.println("line.intersectsLine(new Line2D.Double(p[2], p[2])?"+line.intersectsLine(new Line2D.Double(p[2], p[2])));
//          System.out.println("right.intersectsLine(new Line2D.Double(p[3], p[3])?"+right.intersectsLine(new Line2D.Double(p[3], p[3])));
//          System.out.println("line.intersectsLine(new Line2D.Double(p[3], p[3])?"+line.intersectsLine(new Line2D.Double(p[3], p[3])));
            System.out.println("Before\np[1]="+p[0]+"\np[2]="+p[1]+"\np[3]="+p[2]+"\np[4]"+p[3]);
            System.out.println("Distanza(top, p[0]): "+Line2D.ptSegDist(top.getX1(), top.getY1(), top.getX2(), top.getY2(), p[0].getX(), p[0].getY()));
            System.out.println("Distanza(line, p[0]): "+Line2D.ptSegDist(line.getX1(), line.getY1(), line.getX2(), line.getY2(), p[0].getX(), p[0].getY()));
            System.out.println("Distanza(bottom, p[1]): "+Line2D.ptSegDist(bottom.getX1(), bottom.getY1(), bottom.getX2(), bottom.getY2(), p[1].getX(), p[1].getY()));
            System.out.println("Distanza(line, p[1]): "+Line2D.ptSegDist(line.getX1(), line.getY1(), line.getX2(), line.getY2(), p[1].getX(), p[1].getY()));
            System.out.println("Distanza(left, p[0]): "+Line2D.ptSegDist(left.getX1(), left.getY1(), left.getX2(), left.getY2(), p[2].getX(), p[2].getY()));
            System.out.println("Distanza(line, p[0]): "+Line2D.ptSegDist(line.getX1(), line.getY1(), line.getX2(), line.getY2(), p[2].getX(), p[2].getY()));
            System.out.println("Distanza(right, p[1]): "+Line2D.ptSegDist(right.getX1(), right.getY1(), right.getX2(), right.getY2(), p[3].getX(), p[3].getY()));
            System.out.println("Distanza(line, p[1]): "+Line2D.ptSegDist(line.getX1(), line.getY1(), line.getX2(), line.getY2(), p[3].getX(), p[3].getY()));
        }
        /* ***DEBUG*** */

        //checking that points are really contained in the lines
        if(p[0]!=null && (
            Line2D.ptSegDist(top.getX1(), top.getY1(), top.getX2(), top.getY2(), p[0].getX(), p[0].getY())!=0
         || Line2D.ptSegDist(line.getX1(), line.getY1(), line.getX2(), line.getY2(), p[0].getX(), p[0].getY())!=0 )
          ) p[0]=null;
        if(p[1]!=null && (
     	    Line2D.ptSegDist(bottom.getX1(), bottom.getY1(), bottom.getX2(), bottom.getY2(), p[1].getX(), p[1].getY())!=0
     	    || Line2D.ptSegDist(line.getX1(), line.getY1(), line.getX2(), line.getY2(), p[1].getX(), p[1].getY())!=0 )
          ) p[1]=null;
        if(p[2]!=null && (
        	Line2D.ptSegDist(left.getX1(), left.getY1(), left.getX2(), left.getY2(), p[2].getX(), p[2].getY())!=0
        	|| Line2D.ptSegDist(line.getX1(), line.getY1(), line.getX2(), line.getY2(), p[2].getX(), p[2].getY())!=0 )
          ) p[2]=null;
        if(p[3]!=null && (
        	Line2D.ptSegDist(right.getX1(), right.getY1(), right.getX2(), right.getY2(), p[3].getX(), p[3].getY())!=0
        	|| Line2D.ptSegDist(line.getX1(), line.getY1(), line.getX2(), line.getY2(), p[3].getX(), p[3].getY())!=0 )
          ) p[3]=null;

//        if(p[0]!=null && 
//        		( !top.intersectsLine(new Line2D.Double(p[0], p[0]) )                
///*        	   || !line.intersectsLine(new Line2D.Double(p[0], p[0]) )  */)
//          ) p[0]=null;
//        if(p[1]!=null && 
//        		( !bottom.intersectsLine(new Line2D.Double(p[1], p[1]) )                
///*        	   || !line.intersectsLine(new Line2D.Double(p[1], p[1]) )  */)
//          ) p[1]=null;
//        if(p[2]!=null &&
//        		( !left.intersectsLine(new Line2D.Double(p[2], p[2]) )                
///*               || !line.intersectsLine(new Line2D.Double(p[2], p[2]) )  */)
//          ) p[2]=null;
//        if(p[3]!=null &&
//        		( !right.intersectsLine(new Line2D.Double(p[3], p[3]) )                
///*        	   || !line.intersectsLine(new Line2D.Double(p[3], p[3]) )  */)
//          ) p[3]=null;

        /* ***DEBUG*** */
        if (debug3) System.out.println("After\np[1]="+p[0]+"\tp[2]="+p[1]+"\tp[3]="+p[2]+"\tp[4]"+p[3]);
        /* ***DEBUG*** */

        return p;

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

}
