/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
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
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;

public class ViewPanelCentral{
	
	/**variable used to activate prints in the code*/
	private static boolean verbose=true;

	/**variable used to activate debug prints in the code*/
	private static boolean debug=true;

	private JPanel panelAnalysis = null;
	
	private JTabbedPane tabFile = null;
	
	/**the root central panel used for commonalities*/
	private JPanel jpG = null;
	
	private JTabbedPane [] tabFeatures = null;
	
	private JPanel panelFeatures = null;

	/**buttons for adding commonalities and extracting selected commonalities*/
	private JButton buttonFeaturesAdd = null, buttonSelectionEnd = null;
	
	/**buttons for navigating through commonalitie occurences in tab texts, the X...wardButtons move of x occurences, 
	 where x is defined by occurrJumpSpan constant*/
	private JButton nextOccurrButton = null, prevOccurrButton = null, XForwardOccurrButton = null, XBackwardOccurrButton = null;

	/**defines the number x of occurences jumped by X...wardButtons*/
	private int occurrJumpSpan=4;

	/**label for occurrences navigation*/
	private JLabel occurrsLabel = null;
	
	/**label for occurrences navigation*/
	private JPanel occurrsLabelPanel = null;

	private ArrayList <JCheckBox> checkBoxFeatures = null;
	
	/**(MANUEL M.) relevant terms set, each term X has a list of input files
	 and each file has a list of characters indexex, which are the start positions of X occorrences*/
	private HashMap<String, HashMap<String, ArrayList<Integer>>> relevantTerms=null;

	/**current selected checkbox*/
	private String currentSelectedCheckBox=null;

	/**last highlighted tag for each relevant term and file*/
	private HashMap<String, HashMap<String, Object>> lastHighlightedTag=null;

	/**last removed commonality highlight tags for each relevant term and file*/
	private HashMap<String, HashMap<String, ArrayList<Highlight>>> lastRemovedCommHighlights=null;

	
	
	/**relevant terms occurrencese panel*/
	private JTabbedPane occursTabbedPane = null;
	
	/**the JTextAreas of search panel*/
	private HashMap<String, JTextArea> textTabs = null;
	/**association between relevant terms and current selected tab file names in search panel*/
	private HashMap<String, String> currentFiles = null;
	/**indexes of current selected occurrences in project input files*/
	private HashMap<String, HashMap<String, Integer>> textIndexes = null;

	
	//costanti enumerative
	public static enum FeatureType{ COMMONALITIES, VARIABILITIES };
	//	private int occurrLinesSpan=2;	
	
	private final Highlighter.HighlightPainter[] highlightPainter = 
	{
			  new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW),
			  new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN)
	};

	/** Costruttore
	 * 
	 * @param buttonCommonalitiesEnd
	 */
	public ViewPanelCentral(/*JButton buttonCommonalitiesEnd*/)
	{
//		this.buttonSelectionEnd = buttonCommonalitiesEnd;

		panelAnalysis = new JPanel();
		panelAnalysis.setLayout(null);
		panelAnalysis.setBounds(320, 10, 1100, 702);//+50? 
//		panelAnalysis.setBounds(320, 10, 1100, 652);//+50? 
//		panelAnalysis.setBounds(320, 60, 1100, 652); 
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
//		tabFile.setBounds(0, 0, 1100, 612); /*original*/
//		tabFile.setBounds(0, 0, 750, 652); /*modified*///+50?
		tabFile.setBounds(0, 0, 750, 702); /*modified*///+50?
		
		for(int i = 0; i < 4; i++)
	    {
	    	switch(i)
	    	{
	    		case 0:
	    		{
//	    			tabFile.addTab("Text to analyze", (new JPanel()).add(getTabTextFile(s[0], al)) ); 

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
//		panelAnalysis.setBounds(320, 100, 700, 450); /*modified*/
	}
	
	/** 
	 * Builds the tab for features selection.
	 * 
	 * @param alF - ArrayList containing paths to relevant terms related HTML files
	 * @param alFeaturesCand - ArrayList containing the features candidates
	 * @param alFeaturesSel - ArrayList containing the features selected
	 * @param selectFilePath - String containing paths to selected features related HTML file
	 * @param relevantTerms - an Hashmap mapping each relevant term to an 
	 *   Hashmap containing a list of term occurences for each file in which it is present.
	 * @param fType - a FeatureType constant representing the type of features to be inspected
	 * @param alFeaturesToHighlight - if not null, the terms in alFeaturesToHighlight will be highlighted 
	 */
	public void createTabFeatures(ArrayList<String> alF, ArrayList<String> alFeaturesCand, ArrayList<String> alFeaturesSel,
			String selectFilePath, HashMap<String, HashMap<String, ArrayList<Integer>>> relevantTerms,
			FeatureType fType, ArrayList<String> alFeaturesToHighlight, JButton buttonSelectionEnd){
		
		String displayText = fType==FeatureType.COMMONALITIES? "Commonality":"Variability";
		if(alF == null || alFeaturesCand == null)
			return;
		
//		JTabbedPane tmpTab= new JTabbedPane();
//		tmpTab.setLayout();
		
		this.relevantTerms=relevantTerms;
		tabFeatures = new JTabbedPane[2];
		
		tabFeatures[0] = new JTabbedPane(); 
//		tabFeatures[0].setBounds(0, 0, 510, 652);//+50?
		tabFeatures[0].setBounds(0, 0, 510, 702);//+50?
		
		for(int i = 0; i < alF.size(); i++)
			tabFeatures[0].add(new File(alF.get(i)).getName().substring(0,
			    new File(alF.get(i)).getName().length() - 6), getTabHTMLFile(alF.get(i)));
					
		tabFeatures[1] = new JTabbedPane(); 
//		tabFeatures[1].setBounds(510, 0, 535, 652);//+50?
		tabFeatures[1].setBounds(510, 0, 535, 702);//+50?
		
		tabFeatures[1].add( displayText+"Candidates", getTabFeaturesCandidates(alFeaturesCand, alFeaturesSel,
									alFeaturesToHighlight, buttonSelectionEnd));
		if( selectFilePath != null) tabFeatures[1].add("Selected"+displayText, getTabHTMLFile(selectFilePath));		
		
		panelAnalysis.removeAll();
		panelAnalysis.add(tabFeatures[0]);
		panelAnalysis.add(tabFeatures[1]);

//		for(int i = 0; i < 2; i++)
//		{
//			switch(i)
//			{
//				case 0:
//				{
//					tabFeatures[1].add("Commonality Candidates", getTabFeaturesCandidates(alFeaturesCand, alFeaturesSel));
//					break;
//				}
//				case 1:
//				{
//					if( selectFilePath == null)
//						break;
//					
//					tabFeatures[1].add("Selected Commonality", getTabHTMLFile(selectFilePath));		
//					break;
//				}
//				default: break;
//			}
//		}
//
// INVECE DI
//
//		tabFeatures[1].add("Commonality Candidates", getTabFeaturesCandidates(alFeaturesCand, alFeaturesSel));
//		if( selectFilePath != null) tabFeatures[1].add("Selected Commonality", getTabHTMLFile(selectFilePath));		

	}
	
	/** Creates or updates the tab showing selected features
	 * 
	 * @param s -  path of the HTML file containing the selected commonalities
	 * @param type - type of the selected features
	 */
	public void refreshTabFeaturesSelected(String s, FeatureType type){
		if(tabFeatures[1].getTabCount() == 2) tabFeatures[1].remove(1);
		String title=null;
		if (type == ViewPanelCentral.FeatureType.COMMONALITIES) title="Selected Commonalities";
		else title="Selected Variabilities";
		tabFeatures[1].add(title, getTabHTMLFile(s));
		panelAnalysis.removeAll();
		panelAnalysis.add(tabFeatures[0]);
		panelAnalysis.add(tabFeatures[1]);
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
	
	/**
	 * Returns an ArrayList containing the selected commonalities.
	 * 
	 * @return an ArrayList containing the selected commonalities
	 */
	public ArrayList <String> getSelectedFeatures()
	{
		ArrayList <String> al = new ArrayList <String> ();
		
		for(int i = 0; i < checkBoxFeatures.size(); i++)
			if(checkBoxFeatures.get(i).isSelected())
				al.add(checkBoxFeatures.get(i).getText());
		
		return al;
	}
	
	/* -= FUNZIONI Ausiliarie =- */
	
	
	/** Returns a scrollable panel with the content of file s1, highlighting the relevant terms if highlight is true.
	 * 
	 * @param s1 - path of the file 
	 * @param al - if not null, the terms in al will be highlighted 
	 * @param highlight - if true the relevant terms in the file  will be highlighted
	 * @return JScrollPane - the scrollable panel containing the text of the file
	 */
	private JScrollPane getTabTextFile(String s1, ArrayList <String> al)
	{
		try
		{
			String s = getFileContent(s1);
		    
    		/* ***VERBOSE****/
            if (verbose){
              System.out.println("uso lo StringReader");
              String tmpTest=null;
              StringReader strReader= new StringReader(s);
              BufferedReader bufReader= new BufferedReader(strReader);
              while((tmpTest=bufReader.readLine())!=null){
            	  System.out.println(tmpTest+"\n");
              }
              System.out.println("fatto con lo StringReader");
            }
    		/* ***VERBOSE****/
		    
		    return getTabTextString(s1, s, al);

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

	/** Returns a scrollable panel with the content of file s1, highlighting the relevant terms if highlight is true,
	 *  the inner JTextArea is added to the list of text Tabs, and a list of selected occurences indexes is built.
	 * 
	 * @param term - relevant term by which occurences indexes will be added to the list of indexes
	 * @param file - path of the file 
	 * @param al - if not null, the terms in al will be highlighted 
	 * @return JScrollPane - the scrollable panel containing the text of the file
	 */
	private JScrollPane getRegisteredTabTextFile(String term, String file, ArrayList <String> al)
	{
		try
		{
			String s = getFileContent(file);
		    
    		/* ***VERBOSE****/
            if (verbose){
              System.out.println("uso lo StringReader");
              String tmpTest=null;
              StringReader strReader= new StringReader(s);
              BufferedReader bufReader= new BufferedReader(strReader);
              while((tmpTest=bufReader.readLine())!=null){
            	  System.out.println(tmpTest+"\n");
              }
              System.out.println("fatto con lo StringReader");
            }
    		/* ***VERBOSE****/
		    
		    return getRegisteredTabTextString(term, file, s, al);

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

	/**
	 * Returns a JScrollPane containing the String s as a JTextArea with the words in al highlighted if highlight is true.
	 * 
	 * @param s - the string to use
	 * @param name - the name of this Component
	 * @param al - if not null, the terms in al will be highlighted 
	 * @return JScrollPane - the scrollable panel containing the text of the file
	 */
	private JScrollPane getTabTextString(String name, String s, ArrayList<String> al) {
		JTextArea jta = getTextAreaString(name, s);
		if (al!=null) jta=(JTextArea)setHighlightText(jta, al);
		JScrollPane jscr = new JScrollPane(jta);
		jscr.setName(name);
		return jscr;
	}

	/**
	 * Returns a JScrollPane containing the String s as a JTextArea with the words in al highlighted if highlight is true,
	 *  the JTextArea is added to the list of text Tabs, and a list of selected occurences indexes is built..
	 * 
	 * @param term - relevant term by which occurences indexes will be added to the list of indexes
	 * @param s - the string to use
	 * @param al - if not null, the terms in al will be highlighted 
	 * @param name - the name of this Component
	 * @return - a new JScrollPane with the highlighted text
	 */
	private JScrollPane getRegisteredTabTextString(String term, String name, String s, ArrayList<String> al) {
		JTextArea jta = getTextAreaString(name, s);
		textTabs.put(name, jta);
		if (!textIndexes.containsKey(term)){
			
    		/* ***DEBUG****/			
			if (debug) System.out.println("\n****textIndexes.containsKey("+term+")="+textIndexes.containsKey(term)
					+". Creo la lista di indici per il termine "+term+"****\n");
    		/* ***DEBUG****/

			textIndexes.put(term, new HashMap<String, Integer>());
		}
		if (!textIndexes.get(term).containsKey(name)) textIndexes.get(term).put(name, 0);		

		if (al!=null) jta=(JTextArea)setHighlightText(jta, al);
		JScrollPane jscr = new JScrollPane(jta);
		jscr.setName(name);
		return jscr;
	}

	/**
	 * Returns a JTextArea containing the String s.
	 * 
	 * @param name - name of the new JTextArea component
	 * @param s - string to be cointained in the JTextArea
	 * @return - a new JTextArea containing s
	 */
	private JTextArea getTextAreaString(String name, String s) {
		JTextArea jta = new JTextArea(s);
		jta.setName(name);
		jta.setLineWrap(true);
		return jta;
	}
	
	/**
	 * Returns the content of a file as a String, preserving newlines.
	 * 
	 * @param s1 - the local path of the file
	 * @return a string with content of the file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private String getFileContent(String s1){
		String s = "";
		String tmp=null;   
		try {
			BufferedReader br =
					new BufferedReader(
							new FileReader(s1));    
			  
			while((tmp = br.readLine()) != null)
				s = s + tmp + "\n";

			br.close();
		}catch(FileNotFoundException e){
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		} 
		catch (IOException e) {
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		}
		return s;
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
				while((pos = text.toUpperCase().indexOf(al.get(i).toUpperCase(), pos)) >= 0) 
				{	
					if(ModelProject.isValidOccurrence(al.get(i), text, pos) )
//					if(text.charAt(pos + al.get(i).length()) == ' ' && text.charAt(pos - 1) == ' ')
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
	
	/** (Manuel M.) Returns a tab for commonalities candidates using JCheckBox to add terms.
	 * 
	 * @param alFeaturesCand - ArrayList containing le commonalities candidates
	 * @param alFeaturesSel - ArrayList containing le commonalities selected
	 * @param alFeaturesToHighlight - if not null, the terms in alFeaturesToHighlight will be highlighted 
	 * @return the JPanel created
	 */
	private JScrollPane getTabFeaturesCandidates(ArrayList <String> alFeaturesCand, ArrayList <String> alFeaturesSel,
				ArrayList <String> alFeaturesToHighlight, JButton buttonSelectionEnd){

		JCheckBox checkBoxTmp=null;//temporary variable for CheckBoxes creation
		JPanel checkBoxIconPanelTmp=null;//temporary variable for CheckBoxes panels creation
		
		ImageIcon icon = new ImageIcon(getClass().getResource("/Search/magnifier glasses-min2.png"));
		JButton fileSearchButton = null;

		jpG = new JPanel();
		jpG.setBackground(Color.WHITE);
//		jpG.setOpaque(true);
		jpG.setBounds(0, 0, 550, 652);
		jpG.setLayout(null);
		
		panelFeatures = new JPanel();
		panelFeatures.setBackground(Color.WHITE);
//		panelCommonalities.setOpaque(true);
		panelFeatures.setBounds(30,10,510,260);
		panelFeatures.setLayout(new GridLayout(0, 1));
		panelFeatures.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
						BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		checkBoxFeatures = new ArrayList <JCheckBox> ();
		

		if(alFeaturesSel == null){//there are no Features Selected
			for(int i = 0; i < alFeaturesCand.size(); i++){
				//each entry has a panel with a JCheckBox and an icon button inside
				checkBoxTmp=new JCheckBox(alFeaturesCand.get(i));
				checkBoxTmp.setSelected(true);

				fileSearchButton = new JButton(icon);
				fileSearchButton.setBackground(Color.WHITE);
				fileSearchButton.setSize(icon.getIconWidth(), icon.getIconHeight());
				checkBoxIconPanelTmp = new JPanel();
				checkBoxIconPanelTmp.setLayout(new BoxLayout(checkBoxIconPanelTmp, BoxLayout.X_AXIS));
				checkBoxIconPanelTmp.add(checkBoxTmp);
				checkBoxIconPanelTmp.add(fileSearchButton);

				fileSearchButton.addActionListener(getTermSearchButtonListener("Extracted", alFeaturesCand.get(i),
							alFeaturesToHighlight));
//				checkBoxTmp.addMouseListener(getCheckBoxMouseListener("CommExtracted"));
				checkBoxFeatures.add(checkBoxTmp);	panelFeatures.add(checkBoxIconPanelTmp);
			}
		}
		else{//there are some Features Selected
			for(int i = 0; i < alFeaturesCand.size(); i++){//adding Features Extracted
				checkBoxTmp=new JCheckBox(alFeaturesCand.get(i));
				if(alFeaturesSel.contains(alFeaturesCand.get(i))) checkBoxTmp.setSelected(true);
				else checkBoxTmp.setSelected(false);

				fileSearchButton = new JButton(icon);
				fileSearchButton.setBackground(Color.WHITE);
				fileSearchButton.setSize(icon.getIconWidth(), icon.getIconHeight());
				checkBoxIconPanelTmp = new JPanel();
				checkBoxIconPanelTmp.setLayout(new BoxLayout(checkBoxIconPanelTmp, BoxLayout.X_AXIS));
				checkBoxIconPanelTmp.add(checkBoxTmp);
				checkBoxIconPanelTmp.add(fileSearchButton);

				fileSearchButton.addActionListener(getTermSearchButtonListener("Extracted", alFeaturesCand.get(i),
							alFeaturesToHighlight));
//				checkBoxTmp.addMouseListener(getCheckBoxMouseListener("CommExtracted"));
				checkBoxFeatures.add(checkBoxTmp); panelFeatures.add(checkBoxIconPanelTmp);
			}
			
			for(int i = 0; i < alFeaturesSel.size(); i++){//adding Features typed
				if(!alFeaturesCand.contains(alFeaturesSel.get(i))){
					checkBoxTmp=new JCheckBox(alFeaturesSel.get(i));
					checkBoxTmp.setSelected(true);
					checkBoxTmp.setForeground(Color.RED);

					fileSearchButton = new JButton(icon);
					fileSearchButton.setBackground(Color.WHITE);
					fileSearchButton.setSize(icon.getIconWidth(), icon.getIconHeight());
					checkBoxIconPanelTmp = new JPanel();
					checkBoxIconPanelTmp.setLayout(new BoxLayout(checkBoxIconPanelTmp, BoxLayout.X_AXIS));
					checkBoxIconPanelTmp.add(checkBoxTmp);
					checkBoxIconPanelTmp.add(fileSearchButton);

					fileSearchButton.addActionListener(getTermSearchButtonListener("Inserted",
							alFeaturesSel.get(i), alFeaturesToHighlight));
//					checkBoxTmp.addMouseListener(getCheckBoxMouseListener("CommInserted"));
					checkBoxFeatures.add(checkBoxTmp); panelFeatures.add(checkBoxIconPanelTmp);
				}
			}
		}
		
		JScrollPane jsp = new JScrollPane(panelFeatures, 
				   ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setBounds(15, 10, 490, 260);
		
		JLabel jl = new JLabel("Add term at the commonality candidates:");
		jl.setBounds(16, 271, 400, 30);
		
		final JTextField jtf = new JTextField();
		jtf.setBounds(12, 293, 400, 30);

		buttonFeaturesAdd = new JButton("Add");
		buttonFeaturesAdd.setBounds(425, 293, 80, 30);
		buttonFeaturesAdd.addActionListener(
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent arg0) 
					{
						addCheckBox(jtf.getText());
						jtf.setText("");
					}
				});

		//adding control buttons and a label for term occurences navigation
		XBackwardOccurrButton = new JButton("<<("+occurrJumpSpan+")");
//		XBackwardOccurrButton.setBounds(20, 370, 76, 22);
		XBackwardOccurrButton.setBounds(20, 340, 76, 22);
		XBackwardOccurrButton.addActionListener(getOccurrNavButtonListener(-occurrJumpSpan));

		prevOccurrButton = new JButton("<");
		prevOccurrButton.setBounds(100, 340, 76, 22);
		prevOccurrButton.addActionListener(getOccurrNavButtonListener(-1));
		
		occurrsLabel = new JLabel("<html><div style=\"text-align: center;\">" + "x/y" + "</html>");

		occurrsLabelPanel = new JPanel();
		occurrsLabelPanel.add(occurrsLabel);
		occurrsLabelPanel.setBounds(186, 340, 146, 22);
		occurrsLabelPanel.setBackground(Color.LIGHT_GRAY);

//		occurrsLabel.setBounds(210, 370, 80, 20);
//		occurrsLabel.setBackground(Color.BLUE);

		nextOccurrButton = new JButton(">");
		nextOccurrButton.setBounds(342, 340, 76, 22);
		nextOccurrButton.addActionListener(getOccurrNavButtonListener(1));

		XForwardOccurrButton = new JButton(">>("+occurrJumpSpan+")");
		XForwardOccurrButton.setBounds(422, 340, 76, 22);
		XForwardOccurrButton.addActionListener(getOccurrNavButtonListener(occurrJumpSpan));
		
//		private int occurrJumpSpan=4;	//defines the number x of occurences jumped by X...wardButtons

		
		
		//adding text area for term occurences visualization		
		occursTabbedPane = new JTabbedPane();
//		occursTabbedPane.setBounds(15, 395, 490, 194);//+50?
//		occursTabbedPane.setBounds(15, 395, 490, 244);
		occursTabbedPane.setBounds(15, 365, 490, 274);//+50?
		
		//initializing utility maps
		textTabs = new HashMap<String, JTextArea>();
		textIndexes = new HashMap<String, HashMap<String, Integer>>();
		currentFiles = new HashMap<String, String>();
		lastHighlightedTag = new HashMap<String, HashMap<String, Object>>();
		lastRemovedCommHighlights = new HashMap<String, HashMap<String, ArrayList<Highlight>>>();

		//adding components to panel		
		jpG.add(jsp);
		jpG.add(jtf);
		jpG.add(jl);
		jpG.add(buttonFeaturesAdd);
		jpG.add(occursTabbedPane);
		jpG.add(buttonSelectionEnd);

//		return jpG;
		return new JScrollPane(jpG);

//		return new JScrollPane(jpG, 
//				   ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
//				   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	/**
	 * (Manuel M.) Returns a new ActionListener for features view buttons. The behaviour changes based on the parameter.
	 * 
	 * @param type - String representing the required behaviour type
	 * @param term - String representing the name of the feature candidate
	 * @param alFeaturesToHighlight - if not null, the terms in alFeaturesToHighlight will be highlighted 
	 * @return the new ActionListener
	 */
//	private MouseAdapter getCheckBoxMouseListener(String type) {
	private ActionListener getTermSearchButtonListener(String type, final String term, final ArrayList<String> alFeaturesToHighlight) {
		
		
//		private ActionListener getOccurrNavButtonListener(final int jump) {
//			if (jump==0) return null;
//			return new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent ae){		
		
//		if (type=="CommInserted") return new MouseAdapter(){			
//		if (type=="CommInserted") return new ActionListener(){			
					
//			@Override
//			public void mouseClicked(MouseEvent me){							

//			public void actionPerformed(ActionEvent ae){							
//			  if(me.getButton() == 3){
//				for(int i = 0; i < checkBoxCommonalities.size(); i++){
//				  if(checkBoxCommonalities.get(i).equals(me.getSource())) {
//					removeCheckBox(checkBoxCommonalities.get(i).getText());
//					break;  
//				  }
//				}	
//			  }
//			}			
//		};

//		if (type=="CommExtracted") return new MouseAdapter(){			
		if (type=="Extracted") return new ActionListener(){			
			
//			@SuppressWarnings({ "rawtypes" })
//			@Override
//			public void mouseClicked(MouseEvent me){	

			@SuppressWarnings({ "rawtypes" })
			@Override
			public void actionPerformed(ActionEvent ae){	
			
			
			//creation of occurrences navigation panel
			  occursTabbedPane.removeAll();
			  textTabs.clear();
//			  textIndexes.clear();
			  HashMap<String, ArrayList<Integer>> filesListTmp = null;//files list for a term
//			  ArrayList<String> termArrListTmp = null;//temporary array to call getTabTextString method
//			  int index=0;
			  
			  Iterator filesIterator = null;
			  Map.Entry occurrencesList = null;//			  ArrayList<String> termArrListTmp = null;//temporary array to call getTabTextString method
//			  int index=0;

			  
			  //variables used to remember last selected tab for each relevant term
			  Component[] compArrTmp = null;
			  String tabTitle = null;

//			  for(int i = 0; i < checkBoxCommonalities.size(); i++){
//				if(checkBoxCommonalities.get(i).equals(me.getSource())){
//				  currentSelectedCheckBox=i;
				  currentSelectedCheckBox=term;
//				  termArrListTmp = new ArrayList<String>();
//				  termArrListTmp.add(checkBoxCommonalities.get(i).getText());
				  
				  
				  
				  
				  /* ***DEBUG*** */
				  if (debug) System.out.println("\nCLICCATO SU UN TERMINE!()\nTerm="+term);
				  /* ***DEBUG*** */

//				  filesListTmp = relevantTerms.get(checkBoxCommonalities.get(i).getText());

				  /* ***DEBUG*** */
				  if (debug) System.out.println("relevantTerms.get(term)="+relevantTerms.get(term));
				  /* ***DEBUG*** */

				  filesListTmp = relevantTerms.get(term);

				  /* ***DEBUG*** */
				  if (debug) System.out.println("filesListTmp.entrySet().iterator()="+filesListTmp.entrySet().iterator());
				  /* ***DEBUG*** */

				  filesIterator = filesListTmp.entrySet().iterator();
				  
//				  if(alFeaturesToHighlight!=null){//removing from the list features that are substrings of term
//					termArrListTmp = new ArrayList<String>();
//					for(int f=0; f<alFeaturesToHighlight.size(); ++f){
//					  index=0;
//					  //get first occurrence, if any
//					  index = term.toUpperCase().indexOf(alFeaturesToHighlight.get(f).toUpperCase());
//
//					  if (index > -1 && ModelProject.isValidOccurrence(alFeaturesToHighlight.get(f), term, index))
//						  break;//start checking next relevant term occurrences in this line
//
//					  //add occurrence to relevantTerms, if it is valid
//					  if ()
//						addCharIndexToOccursList(fileProject.get(k).readTermRelevant().get(h),
//						fileProject.get(k).readPathFileUTF8(), charcount+index);
//
//					  //incrementing index to search for next occurrence
//					  index+=fileProject.get(k).readTermRelevant().get(h).length();
//					}	
//				  }
				  
				  
				  while (filesIterator.hasNext()) {//for each file a JScrollPane is added
					  
					occurrencesList = (Map.Entry)filesIterator.next();

//					JScrollPane jScrP = getTabTextFile((String)occurrencesList.getKey(), termArrListTmp);
					JScrollPane jScrP = getRegisteredTabTextFile(term, 
							(String)occurrencesList.getKey(), alFeaturesToHighlight/*termArrListTmp*/);
//					JScrollPane jScrP = getRegisteredTabTextFile(checkBoxCommonalities.get(i).getText(), 
//							  (String)occurrencesList.getKey(), null/*termArrListTmp*/, false);
					occursTabbedPane.addTab((String)occurrencesList.getKey(), jScrP);
//					selectCurrentOccurrence(currentSelectedCheckBox, (String)occurrencesList.getKey());
					
		    		/* ***VERBOSE****/
					if (verbose) System.out.println("\n***\nNumero di componenti di jScrP: "+jScrP.getComponentCount());
					for (int h=0; h<jScrP.getComponentCount(); h++){
						System.out.println("Classe del componente "+h+": "+jScrP.getComponent(h).getClass());
					}
					System.out.println("Classe del ViewPort: "+jScrP.getViewport().getClass());
					/* ***VERBOSE****/

				  }

				  //adding occurences navigation controls
				  jpG.add(nextOccurrButton);
				  nextOccurrButton.setVisible(true);
				  jpG.add(prevOccurrButton);
				  prevOccurrButton.setVisible(true);
				  jpG.add(XForwardOccurrButton);
				  XForwardOccurrButton.setVisible(true);
				  jpG.add(XBackwardOccurrButton);
				  XBackwardOccurrButton.setVisible(true);
				  jpG.add(occurrsLabelPanel);
				  occurrsLabelPanel.setVisible(true);
//				  jpG.validate();
				  jpG.repaint();
				  
				  //restoring previous occurences panel state, if any.
				  if (!currentFiles.containsKey(term)) currentFiles.put(term, 
							  ( (JScrollPane)occursTabbedPane.getSelectedComponent() ).getName());
//				  if (!currentFiles.containsKey(checkBoxCommonalities.get(i).getText()))
//					  currentFiles.put(checkBoxCommonalities.get(i).getText(), 
//							  ( (JScrollPane)occursTabbedPane.getSelectedComponent() ).getName());
				  else {
					  tabTitle = currentFiles.get(term);
//					  tabTitle = currentFiles.get(checkBoxCommonalities.get(i).getText());
					  compArrTmp = occursTabbedPane.getComponents();
					  for (int k=0; k< compArrTmp.length; ++k)
						  if (compArrTmp[k].getName()==tabTitle) occursTabbedPane.setSelectedComponent(compArrTmp[k]);
				  }
				  
				  selectCurrentOccurrence(currentSelectedCheckBox, term);
//				  selectCurrentOccurrence(currentSelectedCheckBox, (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
//				  break;				  
//				}
//			  }
			  
			  occursTabbedPane.addMouseListener(
				new MouseAdapter(){

				  @Override
				  public void mouseClicked(MouseEvent me){						
					selectCurrentOccurrence(currentSelectedCheckBox,
						((JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
					currentFiles.put(currentSelectedCheckBox, ((JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
//					currentFiles.put(checkBoxCommonalities.get(currentSelectedCheckBox).getText(),
//							((JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
				  }
				  
				}
			  );
			  
			}

		};	
		else return null;
	
	}
	
	/**
	 * Returns an ActionListener used for occurrences navigation buttons. 
	 * 
	 * @param jump - dictates the number fo occurrences to jump in the file from the current one, must not be 0.
	 * @return the new ActionListener
	 */
	private ActionListener getOccurrNavButtonListener(final int jump) {
		if (jump==0) return null;
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae){							
			  HashMap<String, ArrayList<Integer>> occurrFilesList=null;		
			  ArrayList<Integer> occurrIndexesList=null;
//			  int previousIndex=0;//previous index in occurrIndexesList of highlited occurrence
//			  int previousOccurrenceIndex =0;//current index in the text of occurrence to highlite
			  int currentIndex=0;//current index in occurrIndexesList of occurrence to highlite
			  int occurrenceIndex =0;//current index in the text of occurrence to highlite
			  Object highlightTag=null;//highlight tag that will be added to the text
			  ArrayList<Highlight> lastRemovedTags=null;//last removed highlighted commonality tags 
			  ArrayList<Highlight> commTagsToRemove=null;//commonality tags to highlight
			  
			  
			  JTextArea jta= textTabs.get(( (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
			  currentIndex= textIndexes.get(currentSelectedCheckBox).get((
						(JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
//			  int currentIndex= textIndexes.get(checkBoxCommonalities.get(currentSelectedCheckBox).getText()).get((
//						(JScrollPane)occursTabbedPane.getSelectedComponent()).getName());

			  //calculate next occurrence index to use, depending on the jump parameter
			  occurrFilesList = relevantTerms.get(currentSelectedCheckBox);
//			  occurrFilesList = relevantTerms.get(checkBoxCommonalities.get(currentSelectedCheckBox).getText());
			  occurrIndexesList=occurrFilesList.get(( (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
			  
//			  previousIndex=currentIndex;
//			  previousOccurrenceIndex = occurrIndexesList.get(previousIndex);
			  
			  if (jump>0){
				if (currentIndex<occurrIndexesList.size()-jump) currentIndex+=jump;
				else currentIndex=occurrIndexesList.size()-1;
			  }
			  else{
				if (currentIndex+jump>=0) currentIndex+=jump;
				else currentIndex=0;
			  }

			  occurrenceIndex = occurrIndexesList.get(currentIndex);
			  
			  Highlighter hilite = jta.getHighlighter();

			  //initializing of lastRemovedCommHighlights, if necessary
			  if(lastRemovedCommHighlights.get(currentSelectedCheckBox)==null) 
				  lastRemovedCommHighlights.put(currentSelectedCheckBox, new HashMap<String, ArrayList<Highlight>>());
			  
			  //re-putting last removed highlighted commonality tags for this term and file
			  lastRemovedTags=lastRemovedCommHighlights.get(currentSelectedCheckBox).get((
			      (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
			  
			  if(lastRemovedTags!=null) try{
			    for(int h=0; h<lastRemovedTags.size(); ++h)
				  hilite.addHighlight(lastRemovedTags.get(h).getStartOffset(), lastRemovedTags.get(h).getEndOffset(),
				  lastRemovedTags.get(h).getPainter());
			  } catch (BadLocationException e) {
				System.out.println("BadLocationException\nTerm: "+currentSelectedCheckBox+" - occurrence: "+occurrenceIndex);
				e.printStackTrace();
			  }
			  
			  //checking what highlighted tags already are in next occurrence text interval
			  commTagsToRemove = new ArrayList<Highlight>();
			  for (Highlight tmp: hilite.getHighlights())
				if (tmp.getStartOffset()>=occurrenceIndex && tmp.getEndOffset()<=occurrenceIndex+currentSelectedCheckBox.length())
				  commTagsToRemove.add(tmp);

			  //removing highlighted tags that already are in next occurrence text interval
			  for (Highlight tmp: commTagsToRemove) hilite.removeHighlight(tmp);
			  
			  //saving last removed Commonality tags in lastRemovedCommHighlights
			  lastRemovedCommHighlights.get(currentSelectedCheckBox).put((
			    (JScrollPane)occursTabbedPane.getSelectedComponent()).getName(), 
			    commTagsToRemove);
			  
			  
			  //initializing of lastHighlightedTag, if necessary
			  if(lastHighlightedTag.get(currentSelectedCheckBox)==null) 
			    lastHighlightedTag.put(currentSelectedCheckBox, new HashMap<String, Object>());
			  
			  //removing last highlighted tag for this term and file
			  highlightTag=lastHighlightedTag.get(currentSelectedCheckBox).get((
			      (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
				  
			  if(highlightTag!=null) hilite.removeHighlight(highlightTag);


//			  if(lastHighlightedTag!=null) hilite.removeHighlight(lastHighlightedTag);
			  
//			  hilite.removeHighlight(tag);
			  
			  //highlighting current occurrence and saving it in lastHighlightedTag
			  try {
				  lastHighlightedTag.get(currentSelectedCheckBox).put(
				    ((JScrollPane)occursTabbedPane.getSelectedComponent()).getName(),
				    hilite.addHighlight(occurrenceIndex, occurrenceIndex+currentSelectedCheckBox.length(), highlightPainter[1]));
				
			  } catch (BadLocationException e) {
				System.out.println("BadLocationException\nTerm: "+currentSelectedCheckBox+" - occurrence: "+occurrenceIndex);
				e.printStackTrace();
			  }
			  
			  //set Caret position and text selection
			  jta.requestFocusInWindow();
			  jta.getCaret().setVisible(true);
			  jta.setCaretPosition(occurrenceIndex);
//			  jta.setSelectionStart(occurrenceIndex);
//			  jta.setSelectionEnd(occurrenceIndex+checkBoxCommonalities.get(currentSelectedCheckBox).getText().length());
//			  jta.setSelectionEnd(occurrenceIndex+currentSelectedCheckBox.length());
//			  jta.setSelectionColor(Color.CYAN);

			  //updating current occurrence index for this file
			  textIndexes.get(currentSelectedCheckBox).put(
				  		((JScrollPane)occursTabbedPane.getSelectedComponent()).getName(), currentIndex);
//			  textIndexes.get(checkBoxCommonalities.get(currentSelectedCheckBox).getText()).put(
//				  		((JScrollPane)occursTabbedPane.getSelectedComponent()).getName(), currentIndex);

			  //updating occurrences label
			  occurrsLabel.setText( (currentIndex+1)+""+"/"+occurrIndexesList.size()+"[Line: "+occurrIndexesList.get(currentIndex)+"]");


			  /* ***VERBOSE****/					
			  if (verbose) System.out.println("\n*****\nSELECTED TAB: "
				+( (JScrollPane)occursTabbedPane.getSelectedComponent()).getName()
				+"\noccurrenceIndex: "+occurrenceIndex
				+"\ncurrentSelectedCheckBox(il testo): "+currentSelectedCheckBox
				+"\ncurrentSelectedCheckBox(lunghezza): "+currentSelectedCheckBox.length()
//				+"\ncurrentSelectedCheckBox(il testo): "+checkBoxCommonalities.get(currentSelectedCheckBox).getText()
//				+"\ncurrentSelectedCheckBox(lunghezza): "+checkBoxCommonalities.get(currentSelectedCheckBox).getText().length()
				+"\ncurrentIndex: "+currentIndex
				+"\n*****\n");					
			  /* ***VERBOSE****/

			}
		  };
	}

	/**
	 * Select the first occurrence of the current selected feature in the tab containing the text of file.
	 * 
	 * @param currentSelectedCheckBox - index of current selected checkbox in checkBoxCommonalities
	 * @param file - file name of the file contained in the tab
	 */
//	private void selectCurrentOccurrence(int currentSelectedCheckBox, String file) {
	private void selectCurrentOccurrence(String currentSelectedCheckBox, String file) {
		  HashMap<String, ArrayList<Integer>> occurrFilesList=null;		
		  ArrayList<Integer> occurrIndexesList=null;
		  int occurrenceIndex =0;
		  Object highlightTag=null;//highlight tag that will be added to the text
		  ArrayList<Highlight> lastRemovedTags=null;//last removed highlighted commonality tags 
		  ArrayList<Highlight> commTagsToRemove=null;//commonality tags to highlight
			
		  JTextArea jta= textTabs.get(( (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
		  int currentIndex= textIndexes.get(currentSelectedCheckBox).get(
					( (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
//		  int currentIndex= textIndexes.get(checkBoxCommonalities.get(currentSelectedCheckBox).getText()).get(
//					( (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());

		  //calculating current occurrence index for selection
		  occurrFilesList = relevantTerms.get(currentSelectedCheckBox);
//		  occurrFilesList = relevantTerms.get(checkBoxCommonalities.get(currentSelectedCheckBox).getText());
		  occurrIndexesList=occurrFilesList.get(( (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
		  occurrenceIndex = occurrIndexesList.get(currentIndex);

		  Highlighter hilite = jta.getHighlighter();
		  
		  //initializing of lastRemovedCommHighlights, if necessary
		  if(lastRemovedCommHighlights.get(currentSelectedCheckBox)==null) 
			  lastRemovedCommHighlights.put(currentSelectedCheckBox, new HashMap<String, ArrayList<Highlight>>());
		  
		  //re-putting last removed highlighted commonality tags for this term and file
		  lastRemovedTags=lastRemovedCommHighlights.get(currentSelectedCheckBox).get((
		      (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
		  
		  if(lastRemovedTags!=null) try{
		    for(int h=0; h<lastRemovedTags.size(); ++h)
			  hilite.addHighlight(lastRemovedTags.get(h).getStartOffset(), lastRemovedTags.get(h).getEndOffset(),
			  lastRemovedTags.get(h).getPainter());
		  } catch (BadLocationException e) {
			System.out.println("BadLocationException\nTerm: "+currentSelectedCheckBox+" - occurrence: "+occurrenceIndex);
			e.printStackTrace();
		  }
		  
		  //checking what highlighted tags already are in next occurrence text interval
		  commTagsToRemove = new ArrayList<Highlight>();
		  for (Highlight tmp: hilite.getHighlights())
			if (tmp.getStartOffset()>=occurrenceIndex && tmp.getEndOffset()<=occurrenceIndex+currentSelectedCheckBox.length())
			  commTagsToRemove.add(tmp);

		  //removing highlighted tags that already are in next occurrence text interval
		  for (Highlight tmp: commTagsToRemove) hilite.removeHighlight(tmp);
		  
		  //saving last removed Commonality tags in lastRemovedCommHighlights
		  lastRemovedCommHighlights.get(currentSelectedCheckBox).put((
		    (JScrollPane)occursTabbedPane.getSelectedComponent()).getName(), 
		    commTagsToRemove);
		  

		  //initializing of lastHighlightedTag, if necessary
		  if(lastHighlightedTag.get(currentSelectedCheckBox)==null) 
			  lastHighlightedTag.put(currentSelectedCheckBox, new HashMap<String, Object>());

		  //removing last highlighted tag for this term and file
		  highlightTag=lastHighlightedTag.get(currentSelectedCheckBox).get((
				  (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());

		  if(highlightTag!=null) hilite.removeHighlight(highlightTag);

		  
//		  if(lastHighlightedTag!=null) hilite.removeHighlight(lastHighlightedTag);
		  
		  //highlighting current occurrence and saving it in lastHighlightedTag		  		  
		  try {
			lastHighlightedTag.get(currentSelectedCheckBox).put(
			  ((JScrollPane)occursTabbedPane.getSelectedComponent()).getName(),
			  hilite.addHighlight(occurrenceIndex, occurrenceIndex+currentSelectedCheckBox.length(), highlightPainter[1]));
			  
//			lastHighlightedTag=hilite.addHighlight(occurrenceIndex, occurrenceIndex+currentSelectedCheckBox.length(),
//				highlightPainter[1]);
		  } catch (BadLocationException e) {
			  System.out.println("BadLocationException\nTerm: "+currentSelectedCheckBox+" - occurrence: "+occurrenceIndex);
			  e.printStackTrace();
		  }
		  
		  //setting Caret position and text selection
		  jta.requestFocusInWindow();
		  jta.getCaret().setVisible(true);
		  jta.setCaretPosition(occurrenceIndex);
//		  jta.setSelectionStart(occurrenceIndex);
//		  jta.setSelectionEnd(occurrenceIndex+currentSelectedCheckBox.length());
//		  jta.setSelectionEnd(occurrenceIndex+checkBoxCommonalities.get(currentSelectedCheckBox).getText().length());
//		  jta.setSelectionColor(Color.CYAN);
		  
		  //updating occurrences label
		  occurrsLabel.setText( (currentIndex+1)+""+"/"+occurrIndexesList.size()+"[Line: "+occurrIndexesList.get(currentIndex)+"]");
		
	}	

	/** Aggiunge una JCheckBox al tab delle commonalities candidates
	 * 
	 * @param s stringa contenente il nome da aggiungere
	 */
	private void addCheckBox(String s) 
	{
		if(s == null || s.trim().equals(""))
			return;
		
		checkBoxFeatures.add(new JCheckBox(s));
		checkBoxFeatures.get(checkBoxFeatures.size()-1).setSelected(true);
		checkBoxFeatures.get(checkBoxFeatures.size()-1).setForeground(Color.RED);
		checkBoxFeatures.get(checkBoxFeatures.size()-1).addMouseListener(
				new MouseAdapter()
				{			
						@Override
						public void mouseClicked(MouseEvent me) 
						{							
							if(me.getButton() == 3)
							{
								for(int i = 0; i < checkBoxFeatures.size(); i++)
								{
									if(checkBoxFeatures.get(i).equals(me.getSource()))
										removeCheckBox(checkBoxFeatures.get(i).getText());
								}	
							}
						}
				});
		panelFeatures.add(checkBoxFeatures.get(checkBoxFeatures.size()-1));	
		panelFeatures.validate();
	}
	
	/** Rimuove una JCheckBox dal tab delle commonalities candidates
	 * 
	 * @param s stringa contenente il nome da rimuovere
	 */
	private void removeCheckBox(String s) 
	{
		if(s == null)
			return;
		
		for(int i = 0; i < checkBoxFeatures.size(); i++)
		{
			if(s.equals(checkBoxFeatures.get(i).getText()))
			{
				panelFeatures.remove(checkBoxFeatures.get(i));
				checkBoxFeatures.remove(i);
				panelFeatures.validate();
			}
		}
	}
}
