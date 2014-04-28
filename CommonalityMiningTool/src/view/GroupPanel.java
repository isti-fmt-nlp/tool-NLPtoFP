package view;

import java.util.ArrayList;
import javax.swing.JComponent;

public class GroupPanel extends JComponent{
	
	private ArrayList<AnchorPanel> members=null;

	private static final long serialVersionUID = 1L;

	public GroupPanel(){
		super();
		members = new ArrayList<AnchorPanel>();
	}

	/**
	 * Returns this group's members list as a ArrayList\<JPanel\>.
	 * 
	 * @return - this group's members list
	 */
	public ArrayList<AnchorPanel> getMembers(){
		return members;
	}
}
