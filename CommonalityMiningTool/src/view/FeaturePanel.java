package view;

import javax.swing.JLayeredPane;
import view.EditorView.CenteredTextPane;

public class FeaturePanel extends JLayeredPane {
	private static final long serialVersionUID = 1L;

	private CenteredTextPane textPane=null;
	
	/**
	 * Creates a new default FeaturePanel. <br>
	 */
	public FeaturePanel(CenteredTextPane jta){
		super();
		this.textPane=jta;
	}

	/**
	 * Returns the ID of this feature.
	 * 
	 * @return - a String containing the ID.
	 */
	public String getID(){
		String compName=getName();
		return compName.substring(EditorView.featureNamePrefix.length(), compName.length());
		//		return getName();
	}

	/**
	 * Returns the name of this feature.
	 * 
	 * @return - a String containing the name.
	 */
	public String getLabelName(){
		return textPane.getText();
	}

	/**
	 * Returns the JTextArea of this feature panel.
	 * 
	 * @return - a String containing the name.
	 */
	public CenteredTextPane getTextArea(){
		return textPane;
	}

}
