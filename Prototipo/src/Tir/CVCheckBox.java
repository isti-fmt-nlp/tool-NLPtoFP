import java.awt.Rectangle;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;


public class CVCheckBox extends JFrame implements ActionListener
{
	/* */
	private JPanel panelTerm = null;
	
	/* ExtractTerm contiene tutti i dati estratti dall'analisi dei file pdf */
	private ExtractTerm extractTerm = null;
	
	/* */
	private int indTerm = 0;
	
	/* */
	private ArrayList <JCheckBox> jcbTerm = null;
	
	/* */
	private ArrayList<ArrayList<String>> matrixTerm = null;
	
	/* */
	private JButton buttonTerm = new JButton("OK");

	public CVCheckBox(ExtractTerm ex)
	{
		this.setLayout(null);		
		this.initializeExtractTerm(ex);
		this.initializeMatrixTerm();
		this.newCheckBox();
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getActionCommand().equals(buttonTerm))
		{
			if(this.indTerm != 6)
			{
				/* Gestire checkbox e textarea ed iniz checkbox e scrollbar glob */
				this.setVisible(false);
				this.remove(this.panelTerm);
				this.panelTerm = null;
				this.incIndTerm();
				this.newCheckBox();
				this.repaint();
			}
			else
			{
				this.setVisible(false);
				this.dispose();
			}
		}
	}
	
	private void newCheckBox()
	{
		int y = 40;
		
		JTextArea jta = new JTextArea();
		
		JScrollPane jsp = new JScrollPane(jta);
		
		this.panelTerm = new JPanel();
		
		this.panelTerm.setLayout(null);
		
		for(int j = 0; j < this.getIndMatrixTerm(this.indTerm).size(); j = j +1)
		{
			this.jcbTerm.add(new JCheckBox(this.getIndMatrixTerm(this.indTerm).get(j)));
		}
		
		for(int j = 0; j < this.jcbTerm.size(); j = j + 1)
		{
			this.jcbTerm.get(j).setSelected(true);
			this.jcbTerm.get(j).setBounds(new Rectangle(10,y,600,20));
			this.panelTerm.add(this.jcbTerm.get(j));		
			y = y + 40;	
		}
		
		jta.setBounds(new Rectangle(10, y + 40, 300, 300));
		jsp.setBounds(new Rectangle(10, y + 40, 300, 300));	
		this.buttonTerm.setBounds(new Rectangle(700,700,50,50));
		this.buttonTerm.addActionListener(this);
		this.panelTerm.add(this.buttonTerm);	
		this.add(panelTerm);
		this.setVisible(true);
	}
	
	private ExtractTerm initializeExtractTerm(ExtractTerm ex)
	{
		return this.extractTerm = ex;
	}
	
	private ExtractTerm getExtractTerm()
	{
		return this.extractTerm;
	}
	
	private ArrayList<ArrayList<String>> initializeMatrixTerm()
	{
		this.matrixTerm = new ArrayList<ArrayList<String>>();
		
		this.matrixTerm.add(this.getExtractTerm().Cr);
		this.matrixTerm.add(this.getExtractTerm().Cd);
		this.matrixTerm.add(this.getExtractTerm().Cs);
		this.matrixTerm.add(this.getExtractTerm().Vr);
		this.matrixTerm.add(this.getExtractTerm().Vd);
		this.matrixTerm.add(this.getExtractTerm().Vs);
		
		return this.matrixTerm;
	}
	
	private ArrayList<String> getIndMatrixTerm(int i)
	{
		return this.matrixTerm.get(i);
	}

	private void incIndTerm() 
	{
		this.indTerm = this.indTerm + 1;
	}
}
