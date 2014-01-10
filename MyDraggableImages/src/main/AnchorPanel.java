package main;

import javax.swing.JPanel;

public class AnchorPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JPanel otherEnd = null;
	
	public AnchorPanel() {
		super();
	}
	
	/**
	 * Returns a new AnchorPanel pointing to the other end of the connector line.
	 * 
	 * @param other - the JPanel used as other end of the connector line
	 */
	public AnchorPanel(JPanel other) {
		super();
		otherEnd=other;
	}

	/**
	 * Returns the other end of the connector line.
	 * 
	 * @return the JPanel representing the other end of the connector line
	 */
	public JPanel getOtherEnd(){
	    return otherEnd;
	}

	/**
	 * Sets the other end of the connector line.
	 * 
	 * @param other - the JPanel representing the other end of the connector line
	 */
	public void setOtherEnd(JPanel other){
	    otherEnd=other;
	}
}
