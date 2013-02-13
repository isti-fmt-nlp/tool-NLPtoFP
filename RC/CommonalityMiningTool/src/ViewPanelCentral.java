/**
 * 
 * @author Daniele Cicciarella
 *
 */
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;

public class ViewPanelCentral
{
	private JPanel panelAnalysis = null;
	
	private JTabbedPane tabFile = null;
	
	private JTabbedPane [] tabCommonalities = null;
	
	private JPanel panelCommonalities = null;
	
	private JButton buttonCommonalitiesAdd, buttonCommonalitiesEnd;
	
	ArrayList <JCheckBox> checkBoxCommonalities = null;
	
	private final Highlighter.HighlightPainter[] highlightPainter = 
	{
			  new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW),
			  new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN)
	};
	
	/** Costruttore
	 * 
	 * @param buttonCommonalitiesEnd
	 */
	public ViewPanelCentral(JButton buttonCommonalitiesEnd)
	{
		this.buttonCommonalitiesEnd = buttonCommonalitiesEnd;
		
		panelAnalysis = new JPanel();
		panelAnalysis.setLayout(null);
		panelAnalysis.setBounds(320, 100, 1100, 612);
	}
	
	/** Crea i tab dell'analisi di un file
	 * 
	 * @param s stringhe contenenti le path all'analisi del file scelto
	 * @param al ArrayList contenenti i termini rilevanti del file
	 */
	public void createTabFile(String [] s, ArrayList <String> al)
	{
		if(s == null || al == null)
			return;
		
		tabFile = new JTabbedPane();
		tabFile.setBounds(0, 0, 1100, 612);
		
		for(int i = 0; i < 4; i++)
	    {
	    	switch(i)
	    	{
	    		case 0:
	    		{
	    			tabFile.addTab("Text to analyze", getTabTextFile(s[0], al)); 
	    			break;
	    		}
	    		case 1:
	    		{
	    			tabFile.addTab("Sentence splitting", getTabHTMLFile(s[1])); 
	    			break;
	    		}
	    		case 2:
	    		{
	    			tabFile.addTab("Term extraction", getTabHTMLFile(s[2])); 
	    			break;
	    		}
	    		case 3:
	    		{
	    			tabFile.addTab("Annotation", getTabHTMLFile(s[3])); 
	    			break;
	    		}
	    	}
	    }
		panelAnalysis.removeAll();
		panelAnalysis.add(tabFile);
	}
	
	/** Crea i tab per le commonalities
	 * 
	 * @param alF ArrayList contenenti le path HTML dei file relativi hai termini rilevanti
	 * @param alCC ArrayList contenenti le commonalities candidates
	 * @param alCS ArrayList contenenti le commonalities selected
	 * @param s stringa contente la path del file HTML relativo alle commonalities selected 
	 */
	public void createTabCommonalities(ArrayList <String> alF, ArrayList <String> alCC, ArrayList <String> alCS, String s)
	{
		
		if(alF == null || alCC == null)
			return;
		
		tabCommonalities = new JTabbedPane[2];
		
		tabCommonalities[0] = new JTabbedPane(); 
		tabCommonalities[0].setBounds(0, 0, 550, 612);
		
		for(int i = 0; i < alF.size(); i++)
			tabCommonalities[0].add(
					new File(alF.get(i)).getName().substring(0, new File(alF.get(i)).getName().length() - 6), getTabHTMLFile(alF.get(i)));
					
		tabCommonalities[1] = new JTabbedPane(); 
		tabCommonalities[1].setBounds(550, 0, 550, 612);
		
		for(int i = 0; i < 2; i++)
		{
			switch(i)
			{
				case 0:
				{
					tabCommonalities[1].add("Commonalities Candidates", getTabCommonalitiesCandidates(alCC, alCS));
					break;
				}
				case 1:
				{
					if( s == null)
						break;
					
					tabCommonalities[1].add("Commonalities Selected", getTabHTMLFile(s));		
					break;
				}
				default: break;
			}
		}
		
		panelAnalysis.removeAll();
		panelAnalysis.add(tabCommonalities[0]);
		panelAnalysis.add(tabCommonalities[1]);
	}
	
	/** Crea o aggiorna il tab per commonalities selected 
	 * 
	 * @param s path del file HTML contenente le commonalities selected
	 */
	public void refreshTabCommonalitesSelected(String s) 
	{
		if(tabCommonalities[1].getTabCount() != 2)
			tabCommonalities[1].add("Commonalities Selected", getTabHTMLFile(s));
		
		else
		{
			tabCommonalities[1].remove(1);
			tabCommonalities[1].add("Commonalities Selected", getTabHTMLFile(s));
		}
		
		panelAnalysis.removeAll();
		panelAnalysis.add(tabCommonalities[0]);
		panelAnalysis.add(tabCommonalities[1]);
	}
	
	/* -= FUNZIONI lettura parametri =- */
	
	/** Lettura del pannello panelAnalysis
	 * 
	 * @return panelAnalysis
	 */
	public JPanel getPanelAnalysis()
	{
		return panelAnalysis;
	}
	
	/** Lettura dei nomi delle JCheckBox selezionate
	 * 
	 * @return al ArrayList contenenti le checkBox selezionate
	 */
	public ArrayList <String> getNameCheckBoxCommonalities()
	{
		ArrayList <String> al = new ArrayList <String> ();
		
		for(int i = 0; i < checkBoxCommonalities.size(); i++)
			if(checkBoxCommonalities.get(i).isSelected())
				al.add(checkBoxCommonalities.get(i).getText());
		
		return al;
	}
	
	/* -= FUNZIONI Ausiliarie =- */
	
	/** Crea un tab per il contenuto del file scelto, evidenziando i termini rilevanti
	 * 
	 * @param s1 path del file contenente il contenuto del file scelto
	 * @param al ArrayList contenente i termini rilevanti del file scelto
	 * @return JScrollPane corrispondente 
	 */
	private JScrollPane getTabTextFile(String s1, ArrayList <String> al)
	{
		try
		{
			String tmp, s = "";
			  
			BufferedReader br =
					new BufferedReader(
							new FileReader(s1));    
		      
		    while((tmp = br.readLine()) != null)
		    	s = s + tmp + "\n";

		    br.close();
		      
		    JTextArea jta = new JTextArea(s);  
		    jta.setLineWrap(true);
		    	      
		    return 
		    	new JScrollPane((JTextArea)setHighlightText(jta, al));

		}
		catch(FileNotFoundException e)
		{
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		} 
		catch (IOException e) 
		{
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		}
	}
	
	/** Evidenzia le parole di un testo
	 * 
	 * @param jtc JTextComponent da modificare
	 * @param al ArrayList contententi le stringa da evidenziare
	 * 
	 * @return jtc JTextComponent modificato
	 */
	private JTextComponent setHighlightText(JTextComponent jtc, ArrayList <String> al) 
	{
		try
		{   
			Highlighter hilite = jtc.getHighlighter();
		    
			Document doc = jtc.getDocument();
		    
			String text = doc.getText(0, doc.getLength());
		    
			int pos = 0;
			
			for(int i = 0; i < al.size(); i++)
			{
				while((pos = text.indexOf(al.get(i), pos)) >= 0) 
				{	
					if(text.charAt(pos + al.get(i).length()) == ' ' && text.charAt(pos - 1) == ' ')
						hilite.addHighlight(pos, pos + al.get(i).length(), highlightPainter[0]);
					
					pos += al.get(i).length();
				}    
			}
		}
		catch(BadLocationException e) 
		{
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		}
		return jtc;
	}
	
	/** Crea un tab per un file HTML
	 * 
	 * @param s path del file HTML
	 * 
	 * @return JScrollPane creato
	 */
	private JScrollPane getTabHTMLFile(String s)
	{	
		if(s == null)
			return null;
		
		FileReader fr = null;
		  
		JEditorPane ep = new JEditorPane();
		ep.setEditorKit(new HTMLEditorKit());

		try 
		{	
			fr = new FileReader(s);
			
			ep.read(fr, s);
			fr.close();
		} 
		catch (FileNotFoundException e)
		{
			System.out.println("Exception getTabHTMLFile: " + e.getMessage());
			return null;
		} 
		catch (IOException e) 
		{
			System.out.println("Exception getTabHTMLFile: " + e.getMessage());
			return null;
		}
		return new JScrollPane(ep);
	}
	
	/** Crea un tab per le commonalities candidates ed utilizza le JCheckBox per aggiungerne termini
	 * 
	 * @param alCC ArrayList contentente le commonalities candidates
	 * @param alCS ArrayList contentente le commonalities selected
	 * 
	 * @return JScrollPane creato
	 */
	private JScrollPane getTabCommonalitiesCandidates(ArrayList <String> alCC, ArrayList <String> alCS)
	{
		JPanel jpG = new JPanel();
		jpG.setBackground(Color.WHITE);
		jpG.setBounds(0, 0, 550, 466);
		jpG.setLayout(null);
		
		panelCommonalities = new JPanel();
		panelCommonalities.setBackground(Color.WHITE);
		panelCommonalities.setBounds(30,30,490,260);
		panelCommonalities.setLayout(new GridLayout(0, 1));
		panelCommonalities.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		checkBoxCommonalities = new ArrayList <JCheckBox> ();
		
		if(alCS == null)
		{
			for(int i = 0; i < alCC.size(); i++)
			{
				checkBoxCommonalities.add(new JCheckBox(alCC.get(i)));
				checkBoxCommonalities.get(i).setSelected(true);
				checkBoxCommonalities.get(i).setEnabled(false);
				panelCommonalities.add(checkBoxCommonalities.get(i));
			}
		}
		else
		{
			for(int i = 0; i < alCC.size(); i++)
			{
				checkBoxCommonalities.add(new JCheckBox(alCC.get(i)));
				checkBoxCommonalities.get(i).setSelected(true);
				checkBoxCommonalities.get(i).setEnabled(false);
				panelCommonalities.add(checkBoxCommonalities.get(i));
			}
			
			for(int i = 0; i < alCS.size(); i++)
			{
				System.out.println(alCS.get(i));
				if(!alCC.contains(alCS.get(i)))
				{
					checkBoxCommonalities.add(new JCheckBox(alCS.get(i)));
					checkBoxCommonalities.get(i).setSelected(true);
					checkBoxCommonalities.get(i).setForeground(Color.RED);
					panelCommonalities.add(checkBoxCommonalities.get(i));		
				}
			}
		}
		
		JScrollPane jsp = new JScrollPane(panelCommonalities, 
				   ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setBounds(15,30,490,260);;
		
		JLabel jl = new JLabel("Add term at the commonalities candidates:");
		jl.setBounds(16, 297, 400, 30);
		
		final JTextField jtf = new JTextField();
		jtf.setBounds(12, 320, 400, 30);
		
		buttonCommonalitiesAdd = new JButton("Add");
		buttonCommonalitiesAdd.setBounds(423, 320, 30, 30);
		buttonCommonalitiesAdd.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent arg0) 
					{
						addCheckBox(jtf.getText());
						jtf.setText("");
					}
				});
				
		jpG.add(jsp);
		jpG.add(jtf);
		jpG.add(jl);
		jpG.add(buttonCommonalitiesAdd);
		jpG.add(buttonCommonalitiesEnd);
		
		return new JScrollPane(jpG);
	}
	
	/** Aggiunge una JCheckBox al tab delle commonalities candidates
	 * 
	 * @param s stringa contenente il nome da aggiungere
	 */
	private void addCheckBox(String s) 
	{
		if(s == null || s.trim().equals(""))
			return;
		
		checkBoxCommonalities.add(new JCheckBox(s));
		checkBoxCommonalities.get(checkBoxCommonalities.size()-1).setSelected(true);
		checkBoxCommonalities.get(checkBoxCommonalities.size()-1).setForeground(Color.RED);
		panelCommonalities.add(checkBoxCommonalities.get(checkBoxCommonalities.size()-1));	
		panelCommonalities.validate();
	}
}
