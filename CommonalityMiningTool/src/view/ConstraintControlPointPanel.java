package view;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ConstraintControlPointPanel extends JLabel{

	private static final long serialVersionUID = 1L;

	private boolean alreadyShifted = false;
	
	public ConstraintControlPointPanel(){
	  super();
	  this.alreadyShifted=false;
	}
	
	public ConstraintControlPointPanel(ImageIcon icon){
	  super(icon);
	  this.alreadyShifted=false;
	}
	
	public boolean isAlreadyShifted(){
	  return this.alreadyShifted;
	}
	
	public void setAlreadyShifted(boolean shifted){
	  this.alreadyShifted=shifted;
	}
}
