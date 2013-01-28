package project;

import java.awt.Rectangle;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;


public class ToolCheckBox extends JFrame implements ActionListener
{	
	/* ExtractTerm contiene tutti i dati estratti dall'analisi dei file pdf */
	private ExtractTerm extractTerm = null;
	
	private int indTerm = 0;
	
	private int height = 0;
	
	private JTextArea area = null;
	
	private JScrollPane scroll = null; 
	
	private String selectCommonalities = null;
	
	private String selectVariabilities = null;
	
	private ArrayList <JCheckBox> jcb = null; 
	
	private JButton button = null;

	public ToolCheckBox(ExtractTerm extractTerm)
	{
		this.extractTerm = extractTerm;
		
		setLayout(null);
		
		button = new JButton("OK");
		button.setBounds(new Rectangle(500,500,40,40));
		button.addActionListener(this);
		
		add(button);	
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getActionCommand().equals("OK"))
		{
			if(indTerm != 2)
			{
				scroll.remove(area);
				selectCommonalities = area.getText();
				

				for(int i = 0 ; i < this.jcb.size(); i++)
					this.remove(this.jcb.get(i));
				
				this.remove(scroll);
				
				setLayout(null);
				indTerm = indTerm + 1;
				newCheckBox();
			}
			else
			{
				scroll.remove(area);
				selectVariabilities = area.getText();	
				dispose();			
				System.exit(0);
			}
		}
		repaint();
	}
	
	public void newCheckBox()
	{
		switch(indTerm)
		{
			case 0:
			{
				height = 0;
				
				this.jcb = new ArrayList <JCheckBox>();
				
				for(int i = 0; i < extractTerm.Cr.size(); i++)
				{
					JCheckBox jcb1 = new JCheckBox(extractTerm.Cr.get(i));
					jcb1.setBounds(new Rectangle(10, height + 10, 600, 20));
					this.jcb.add(jcb1);
					
					this.add(this.jcb.get(i));
					
					height = height + 30;
					
				}
				
				for(int i = 0; i < extractTerm.Cd.size(); i++)
				{
					JCheckBox jcb = new JCheckBox(extractTerm.Cd.get(i));				
					jcb.setBounds(new Rectangle(10, height + 10, 600, 20));
					this.jcb.add(jcb);
					
					this.add(this.jcb.get(i));
					
					height = height + 30;
				}
				
				for(int i = 0; i < extractTerm.Cs.size(); i++)
				{
					JCheckBox jcb = new JCheckBox(extractTerm.Cs.get(i));
					jcb.setBounds(new Rectangle(10, height + 10, 600, 20));
					this.jcb.add(jcb);
					
					this.add(this.jcb.get(i));
					
					height = height + 30;
				}
				
				area = new JTextArea();
				area.setBounds(new Rectangle(10,height + 10, 400,400));
				
				scroll = new JScrollPane(area);
				scroll.setBounds(new Rectangle(10,height + 10, 400,400));
				
				add(scroll);
				
				break;
			}
			case 1:
			{
				height = 0;
				
				this.jcb = new ArrayList <JCheckBox>();
				
				for(int i = 0; i < extractTerm.Vr.size(); i++)
				{
					JCheckBox jcb = new JCheckBox(extractTerm.Vr.get(i));
					jcb.setBounds(new Rectangle(10, height + 10, 600, 20));
					this.jcb.add(jcb);
					
					this.add(this.jcb.get(i));
					
					height = height + 30;
					
				}
				
				for(int i = 0; i < extractTerm.Vd.size(); i++)
				{
					JCheckBox jcb = new JCheckBox(extractTerm.Vd.get(i));				
					jcb.setBounds(new Rectangle(10, height + 10, 600, 20));
					this.jcb.add(jcb);
					
					this.add(this.jcb.get(i));
					
					height = height + 30;
				}
				
				for(int i = 0; i < extractTerm.Vs.size(); i++)
				{
					JCheckBox jcb = new JCheckBox(extractTerm.Vs.get(i));
					jcb.setBounds(new Rectangle(10, height + 10, 600, 20));
					this.jcb.add(jcb);
					
					this.add(this.jcb.get(i));
					
					height = height + 30;
				}
				
				area = new JTextArea();
				area.setBounds(new Rectangle(10,height + 10, 400,400));
				
				scroll = new JScrollPane(area);
				scroll.setBounds(new Rectangle(10,height + 10, 400,400));
				
				add(scroll);
				
				break;
			}
		}
	}
}
