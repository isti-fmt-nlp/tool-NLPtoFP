package main;

import java.util.ArrayList;

import javax.swing.JPanel;

public class GroupPanel extends JPanel{
	
	private ArrayList<JPanel> members=null;

	private static final long serialVersionUID = 1L;

	public GroupPanel(){
		super();
		members = new ArrayList<JPanel>();
	}

	/**
	 * Returns this group's members list as a ArrayList\<JPanel\>.
	 * 
	 * @return - this group's members list
	 */
	public ArrayList<JPanel> getMembers(){
		return members;
	}
}
