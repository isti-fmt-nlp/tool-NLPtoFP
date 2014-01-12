package main;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class AnchorPanel extends JComponent {

	private static final long serialVersionUID = 1L;
	
	private JComponent otherEnd = null;
	
	public AnchorPanel() {
		super();
	}
	
	/**
	 * Returns a new AnchorPanel pointing to the other end of the connector line.
	 * 
	 * @param other - the JPanel used as other end of the connector line
	 */
	public AnchorPanel(JComponent other) {
		super();
		otherEnd=other;
	}

	/**
	 * Returns the other end of the connector line.
	 * 
	 * @return the JPanel representing the other end of the connector line
	 */
	public JComponent getOtherEnd(){
	    return otherEnd;
	}

	/**
	 * Sets the other end of the connector line.
	 * 
	 * @param other - the JPanel representing the other end of the connector line
	 */
	public void setOtherEnd(JComponent other){
	    otherEnd=other;
	}
}
