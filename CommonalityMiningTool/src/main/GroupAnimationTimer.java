package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class GroupAnimationTimer extends Timer{

	private static final long serialVersionUID = 1L;

	private double radius =0.;	
	
	public GroupAnimationTimer(double startFrom, final double amount){
	  super(50, null);
	  this.radius=startFrom;
	  
	  addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		  radius+=amount;
		};
	  });
	}
	
	public double getRadius(){
	  return radius;
	}
	
	public void clearRadius(){
	  radius=0.;
	}
	
	
}
