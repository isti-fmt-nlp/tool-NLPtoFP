package view;

import javax.swing.JComponent;

public class ConstraintPanel extends AnchorPanel {
	
	private static final long serialVersionUID = 1L;

	private JComponent controlPoint = null;
	
	public ConstraintPanel() {
		super();
	}
	
	/**
	 * Returns a new AnchorPanel pointing to the other end of the connector line.
	 * 
	 * @param other - the JPanel used as other end of the connector line
	 */
	public ConstraintPanel(JComponent other, JComponent controlPoint) {
		super();
		setOtherEnd(other);
		this.controlPoint=controlPoint;
	}

	/**
	 * Returns the control point for the curve.
	 */
	public JComponent getControlPoint(){
	  return controlPoint;	
	}

	/**
	 * Sets the control point for the curve. If it already exists, only its coordinates are changed.
	 * 
	 * @param x - the new x coordinate
	 * @param y - the new y coordinate
	 */
	public void setControlPoint(JComponent controlPoint){
	  this.controlPoint=controlPoint;	
	}

	/**
	 * Sets the control point for the curve. If it already exists, only its coordinates are changed.
	 * 
	 * @param x - the new x coordinate
	 * @param y - the new y coordinate
	 */
	public void moveControlPoint(int x, int y){
	  controlPoint.setLocation(x, y);
	}
	
}
