package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.security.InvalidParameterException;
import javax.swing.Timer;

import view.EditorView;
import view.GroupPanel;

public class GroupAnimationTimer extends Timer{

	private static final long serialVersionUID = 1L;

	private double radius = 0.0;	
	private double amountPerIter = 0.0;	
	private double limitVal = 0.0;	
	private EditorView viewRef	= null;	
	private GroupAnimationTimer timer = null;
//	private int totalSteps = 0;
	private int steps = 0;
	private GroupPanel groupToMergeWith = null;
	private BufferedImage anchorImage = null;
	private Point2D.Double anchorToBeMergedLocation = null;
	private Point2D.Double anchorToBeMergedPrevLocation = null;
	private Point2D.Double groupToMergeWithLocation = null;
	/**
	 * Creates a new timer to handle group animations, which will draw an enlarging or shrinking circle around all groups.
	 * If the amount parameter is positive, a timer for an group dock opening animation is returned; if it is negative
	 * a timer for an group dock closing animation is returned instead. In the latter case, group and anchor MUST be not null.
	 * 
	 * @param startFrom - starting radius of the circle
	 * @param amount - how much the radius will change per iteration
	 * @param limit - radius limit, when reached the animation will stop.
	 * @param view - the EditorView object on which the animation will be drawn
	 * @param group- the GroupPanel object to which the anchor will be merged, can be null
	 * @param anchorLocation - the location of the anchor to be merged, can be null
	 * @param image - the BufferedImage object representing the anchor to be merged, can be null
	 * 
	 * @throws InvalidParameterException - this Exception will be thrown in the following cases: <br>
	 * 1) amount&lt0 && startFrom&ltlimit <br>
	 * 2) amount&gt=0 && startFrom&gtlimit <br>
	 * 3) amount==0
	 */
	public GroupAnimationTimer(double startFrom, double amount, double limit, EditorView view,
							   GroupPanel group, Point2D.Double anchorLocation, BufferedImage image)
																throws InvalidParameterException{
	  super(20, null);
	  this.radius=startFrom;
	  this.amountPerIter=amount;
	  this.viewRef=view;
	  this.limitVal=limit;
	  this.timer=this;
	  this.groupToMergeWith=group;
	  this.anchorToBeMergedLocation=anchorLocation;
	  this.anchorImage=image;
	  //calculating total number of steps that are needed for the closing animation
	  this.steps=(int)((startFrom-limit)/(-amount))+1;
//	  this.totalSteps=steps;
	  
	  if(amount==0.0) 
		throw new InvalidParameterException("amount parameter must be non zero"); 
	  if(amount<0.0 && startFrom<limit) 
		throw new InvalidParameterException("startFrom is lower than limit with negative amount parameter"); 
	  if(amount>0.0 && startFrom>limit)
		throw new InvalidParameterException("startFrom is greater than limit with positive amount parameter"); 
//	  if(amount<0 && (group==null || anchorLocation==null))
//		throw new InvalidParameterException("group or anchor location is null with negative amount parameter"); 
	  //a timer for the opening animation has been requested
	  if(amountPerIter>0.0) addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		  //updating radius
		  radius+=amountPerIter;
//		  viewRef.drawCirclesAroundGroups(radius, (Graphics2D)viewRef.getGraphics());
		  if(radius>limitVal) stop();
		};
	  });
	  //a timer for the closing animation has been requested
	  else addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		  double distanceFromGroupX=0;
		  double distanceFromGroupY=0;
		  anchorToBeMergedPrevLocation = anchorToBeMergedLocation;
		  //drawing circles around groups
		  radius+=amountPerIter;
//		  if(radius>=1.0) viewRef.drawAllGroupClosingAnimations((Graphics2D)viewRef.getGraphics());
		  
		  //drawing anchor image in a location more near to the group than previous iteration
		  if(anchorToBeMergedLocation!=null && groupToMergeWith!=null && anchorImage!=null){
			//getting group location and adjusting it
			if(groupToMergeWithLocation==null) groupToMergeWithLocation = 
			  new Point2D.Double(0, 0);
			groupToMergeWithLocation.setLocation(
			  groupToMergeWith.getLocationOnScreen().getX()+1, groupToMergeWith.getLocationOnScreen().getY()+5);
			  
			distanceFromGroupX=groupToMergeWithLocation.getX()-anchorToBeMergedLocation.getX();
			distanceFromGroupY=groupToMergeWithLocation.getY()-anchorToBeMergedLocation.getY();
			
			System.out.println("Distanza Pre Calcolo"
					+"\ndistanceFromGroupX: "+distanceFromGroupX
					+"\ndistanceFromGroupY: "+distanceFromGroupY);
			
			distanceFromGroupX=(distanceFromGroupX/(double)steps)*(double)(steps-1);
			distanceFromGroupY=(distanceFromGroupY/(double)steps)*(double)(steps-1);
			
			System.out.println("Distanza Post Calcolo"
							+"\ndistanceFromGroupX: "+distanceFromGroupX
							+"\ndistanceFromGroupY: "+distanceFromGroupY);
			
			anchorToBeMergedLocation.setLocation(
				groupToMergeWithLocation.getX()-distanceFromGroupX,
				groupToMergeWithLocation.getY()-distanceFromGroupY);
//			viewRef.drawMergingAnchor((Graphics2D)viewRef.getGraphics(), timer);			  
		  }
		  
		  --steps;
		  System.out.println("Current steps: "+steps);
		  if(steps==0){
			System.out.println("Reached steps 0");
			stop();
			viewRef.removeCloserTimer(timer);
		  }
		};
	  });
	}
	
	/**
	 * Returns the current circles radius.
	 * 
	 * @return a double representing the radius
	 */
	public double getRadius(){
	  return radius;
	}
	
	/**
	 * Sets the circles radius to 0.
	 */
	public void clearRadius(){
	  radius=0.;
	}
	
	/**
	 * Returns the current anchor image location.
	 * 
	 * @return a Point representing the location
	 */
	public Point2D.Double getAnchorImageLocation(){
	  return anchorToBeMergedLocation;
	}
	
	/**
	 * Returns the previous anchor image location.
	 * 
	 * @return a Point representing the location
	 */
	public Point2D.Double getAnchorImagePrevLocation(){
	  return anchorToBeMergedPrevLocation;
	}
	
	/**
	 * Returns the anchor image.
	 * 
	 * @return a BufferedImage containing the anchor image
	 */
	public BufferedImage getAnchorImage(){
	  return anchorImage;
	}
	
	
}
