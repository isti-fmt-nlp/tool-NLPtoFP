package main;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class GroupPanel extends JComponent{
	
	private ArrayList<JComponent> members=null;

	private static final long serialVersionUID = 1L;

	public GroupPanel(){
		super();
		members = new ArrayList<JComponent>();
	}

	/**
	 * Returns this group's members list as a ArrayList\<JPanel\>.
	 * 
	 * @return - this group's members list
	 */
	public ArrayList<JComponent> getMembers(){
		return members;
	}
}
