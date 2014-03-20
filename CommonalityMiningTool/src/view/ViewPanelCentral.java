/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
	private static boolean verbose=false;

	/**variable used to activate debug prints in the code*/
	private static boolean debug=true;

	private JPanel panelAnalysis = null;
	
	private JTabbedPane tabFile = null;
	
	/**the root central panel used for feature selection*/
	private JPanel panelSelection = null;
	
	private JTabbedPane tabFeaturesCandidates = null;
	
	/**the panel containing the candidate feature's checkboxes */
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
	private HashMap<String,HashMap<String,ArrayList<int[]>>> relevantTerms=null;
	
	/** Contains all project's relevant terms, in both original version and term-extraction version*/
	private HashMap<String,HashMap<String,ArrayList<String>>> originalTermsVersions=null; 

	/**current selected checkbox*/
	private String currentSelectedCheckBox=null;

	/**last highlighted tag for each relevant term and file*/
	private HashMap<String, HashMap<String, Object>> lastHighlightedTag=null;

	/**last removed commonality highlight tags for each relevant term and file*/
	private HashMap<String, HashMap<String, ArrayList<Highlight>>> lastRemovedHighlights=null;

	
	/**relevant terms occurrences panel*/
	private JTabbedPane occursTabbedPane = null;
	
	/**relevant terms search buttons panel*/
	private JPanel buttonPanel = null;
	
	/**relevant terms search panel*/
	private JPanel searchPanel= null;
	

	/**the JTextAreas of search panel*/
	private HashMap<String, JTextArea> textTabs = null;
	/**association between relevant terms and current selected tab file names in search panel*/
	private HashMap<String, String> currentFiles = null;
	/**indexes of current selected occurrences in project input files*/
	private HashMap<String, HashMap<String, Integer>> textIndexes = null;

	
	//costanti enumerative
	public static enum FeatureType{ COMMONALITIES, VARIABILITIES };
	
	private final Highlighter.HighlightPainter[] highlightPainter = {
			  new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW),
			  new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN)
	};

	public ViewPanelCentral(){
		panelAnalysis = new JPanel();
		panelAnalysis.setLayout(new BorderLayout());
		panelAnalysis.setPreferredSize(panelAnalysis.getMinimumSize());
		panelAnalysis.setLocation(0, 0);		
	}
	
	/** 
	 * Create the analysis tabs of a file
	 * 
	 * @param s - String array containing analysis result files paths of the chosen file
	 * @param relevantTerm ArrayList containing file relevant terms 
	 */
	public void createTabFile(String [] s, ArrayList <String> relevantTerm){
		if(s == null || relevantTerm == null)
			return;
		
		tabFile = new JTabbedPane();
		tabFile.setPreferredSize(panelAnalysis.getPreferredSize());
		
		tabFile.addTab("Text to analyze", getTabTextFile(s[0], relevantTerm)); 		
		tabFile.addTab("Sentence splitting", getTabHTMLFile(s[1])); 
		tabFile.addTab("Term extraction", getTabHTMLFile(s[2])); 

		panelAnalysis.removeAll();
		panelAnalysis.add(tabFile, BorderLayout.CENTER);
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
			String selectFilePath, HashMap<String, HashMap<String, ArrayList<int[]>>> relevantTerms,
			HashMap<String, HashMap<String, ArrayList<String>>> relevantTermsFilesVersions, FeatureType fType,
			ArrayList<String> alFeaturesToHighlight, JButton buttonSelectionEnd){
		
		String displayText = fType==FeatureType.COMMONALITIES? "Commonality":"Variability";
		if(alF == null || alFeaturesCand == null) return;
		
		this.relevantTerms=relevantTerms;
		this.originalTermsVersions=relevantTermsFilesVersions;

		createSearchPanel();

		tabFeaturesCandidates = new JTabbedPane(); 

		tabFeaturesCandidates.add( displayText+"Candidates", getTabFeaturesCandidates(alFeaturesCand, alFeaturesSel,
									alFeaturesToHighlight, buttonSelectionEnd, fType));
		if( selectFilePath != null) tabFeaturesCandidates.add("Selected"+displayText, getTabHTMLFile(selectFilePath));		
		
		panelAnalysis.removeAll();
		
		JSplitPane splitterPanelInner = null;
		splitterPanelInner = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitterPanelInner.setContinuousLayout(true);
		splitterPanelInner.setDividerSize(6);
		splitterPanelInner.setResizeWeight(0.5);
		splitterPanelInner.setPreferredSize(
		  new Dimension(9*Toolkit.getDefaultToolkit().getScreenSize().width/10-6, Toolkit.getDefaultToolkit().getScreenSize().height));
		
		searchPanel.setPreferredSize(
				  new Dimension(splitterPanelInner.getPreferredSize().width/2-3, splitterPanelInner.getPreferredSize().height));

		tabFeaturesCandidates.setPreferredSize(
				  new Dimension(splitterPanelInner.getPreferredSize().width/2-3, splitterPanelInner.getPreferredSize().height));

		splitterPanelInner.setLeftComponent(searchPanel);
		splitterPanelInner.setRightComponent(tabFeaturesCandidates);

		panelAnalysis.add(splitterPanelInner, BorderLayout.CENTER);
	}
	
	/** 
	 * Creates or updates the tab showing selected features
	 * 
	 * @param s -  path of the HTML file containing the selected commonalities
	 * @param type - type of the selected features
	 */
	public void refreshTabFeaturesSelected(String s, FeatureType type){
		if(tabFeaturesCandidates.getTabCount() == 2) tabFeaturesCandidates.remove(1);
		String title=null;
		if (type == ViewPanelCentral.FeatureType.COMMONALITIES) title="Selected Commonalities";
		else title="Selected Variabilities";
		tabFeaturesCandidates.add(title, getTabHTMLFile(s));
		panelAnalysis.removeAll();
		
		JSplitPane splitterPanelInner = null;
		splitterPanelInner = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitterPanelInner.setContinuousLayout(true);
		splitterPanelInner.setDividerSize(6);
		splitterPanelInner.setResizeWeight(0.5);
		splitterPanelInner.setPreferredSize(
		  new Dimension(9*Toolkit.getDefaultToolkit().getScreenSize().width/10-6, Toolkit.getDefaultToolkit().getScreenSize().height));
		
		searchPanel.setPreferredSize(
				  new Dimension(splitterPanelInner.getPreferredSize().width/2, splitterPanelInner.getPreferredSize().height));

		tabFeaturesCandidates.setPreferredSize(
				  new Dimension(splitterPanelInner.getPreferredSize().width/2, splitterPanelInner.getPreferredSize().height));

		splitterPanelInner.setLeftComponent(searchPanel);
		splitterPanelInner.setRightComponent(tabFeaturesCandidates);

		panelAnalysis.add(splitterPanelInner, BorderLayout.CENTER);

	}
	
	/* -= FUNZIONI lettura parametri =- */
	
	/**
	 * Lettura del pannello panelAnalysis
	 * 
	 * @return panelAnalysis
	 */
	public JPanel getPanelAnalysis(){
		return panelAnalysis;
	}
	
	/**
	 * Returns an ArrayList containing the selected commonalities.
	 * 
	 * @return an ArrayList containing the selected commonalities
	 */
	public ArrayList <String> getSelectedFeatures(){
		ArrayList <String> al = new ArrayList <String> ();

		for(int i = 0; i < checkBoxFeatures.size(); i++)
			if(checkBoxFeatures.get(i).isSelected()) al.add(checkBoxFeatures.get(i).getText());
		
		return al;
	}
	
	/* -= FUNZIONI Ausiliarie =- */
	
	
	/** 
	 * Returns a scrollable panel with the content of file s1, highlighting the relevant terms if al is not null.
	 * 
	 * @param s1 - path of the file 
	 * @param al - if not null, the terms in al will be highlighted 
	 * @return JScrollPane - the scrollable panel containing the text of the file
	 */
	private JScrollPane getTabTextFile(String s1, ArrayList <String> al){
		try{
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

		}catch (IOException e){
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		}
	}

	/** Returns a scrollable panel with the content of file s1, highlighting the relevant terms if al is not null,
	 *  the inner JTextArea is added to the list of text Tabs, and a list of selected occurences indexes is built.
	 * 
	 * @param term - relevant term by which occurences indexes will be added to the list of indexes
	 * @param file - path of the file 
	 * @param al - if not null, the terms in al will be highlighted 
	 * @return JScrollPane - the scrollable panel containing the text of the file
	 */
	private JScrollPane getRegisteredTabTextFile(String term, String file, ArrayList <String> al){
		try{
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

		}catch(FileNotFoundException e){
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		}catch (IOException e) {
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Returns a JScrollPane containing the String s as a JTextArea with the words in al highlighted if al is not null.
	 * 
	 * @param s - the string to use
	 * @param name - the name of this Component
	 * @param al - if not null, the terms in al will be highlighted 
	 * @return JScrollPane - the scrollable panel containing the text of the file
	 */
	private JScrollPane getTabTextString(String name, String s, ArrayList<String> al) {
		JTextArea jta = getTextAreaString(name, s);
		if (al!=null) jta=(JTextArea)setHighlightText(name, jta, al);
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

		if (al!=null) jta=(JTextArea)setHighlightText(name, jta, al);
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
			BufferedReader br = new BufferedReader(new FileReader(s1));    
			  
			while((tmp = br.readLine()) != null) s = s + tmp + "\n";
			br.close();
		}catch (IOException e) {
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		}
		return s;
	}

	/** 
	 * Highlights a JTextComponent with the specified terms.
	 * 
	 * @param fileName - the path of the file displayed by jtc
	 * @param jtc - JTextComponent to be highlighted
	 * @param al - if not null, these terms will be highlighted
	 * 
	 * @return - the modified JTextComponent
	 */	
	private JTextComponent setHighlightText(String fileName, JTextComponent jtc, ArrayList<String> al){
		ArrayList<int[]> occurrences = null;
		if(relevantTerms==null) return jtc;
		
		try{   
			Highlighter hilite = jtc.getHighlighter();
		    
			Document doc = jtc.getDocument();
		    
			String text = doc.getText(0, doc.getLength());
		    
			
			//adding highlights to terms occurrences
			for(int i = 0; i < al.size(); i++){
			  occurrences = relevantTerms.get(al.get(i)).get(fileName);
			  for(int[] occurr: occurrences) hilite.addHighlight(occurr[0], occurr[1], highlightPainter[0]);
			}
			
		}catch(BadLocationException e){
			System.out.println("Exception tabTextFile: " + e.getMessage());
			return null;
		}
		return jtc;
	}
	
	/** 
	 * Create a tab containing an HTML file.
	 * 
	 * @param s - HTML file path
	 * @return - the new JScrollPane
	 */
	private JScrollPane getTabHTMLFile(String s){	
		String html=null;
		StringReader strReader=null;
		System.out.println("entered in getTabHTMLFile("+s+")");
		if(s == null) return null;
		
		FileReader fr = null;
		  
		JEditorPane ep = new JEditorPane();
		ep.setEditorKit(new HTMLEditorKit());

		try{	
			File file= new File(s);
			System.out.println("file.exists()="+file.exists());
		
			if (file.exists()){
			  fr = new FileReader(s);
			  System.out.println("getTabHTMLFile("+s+"), fr="+fr);
			  ep.read(fr, s);
			  fr.close();
			}
			else{
			  html="<html><head></head><body><h1>NO FEATURES SELECTED YET</h1></body></html>";
			  strReader=new StringReader(html);
			  ep.read(strReader, html);
			  strReader.close();
			}
		}catch (IOException e){
			System.out.println("IOException getTabHTMLFile: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return new JScrollPane(ep);
	}
	
	/** 
	 * Returns a tab for commonalities candidates using JCheckBox to add terms.
	 * 
	 * @param alFeaturesCand - ArrayList containing le commonalities candidates
	 * @param alFeaturesSel - ArrayList containing le commonalities selected
	 * @param alFeaturesToHighlight - if not null, the terms in alFeaturesToHighlight will be highlighted 
	 * @param fType - a FeatureType constant representing the type of features
	 * @return - the JScrollPane created
	 */
	private JPanel getTabFeaturesCandidates(ArrayList <String> alFeaturesCand, ArrayList <String> alFeaturesSel,
				ArrayList <String> alFeaturesToHighlight, JButton buttonSelectionEnd, FeatureType fType){

		String addTermText=null;
		JCheckBox checkBoxTmp=null;//temporary variable for CheckBoxes creation
		JPanel checkBoxIconPanelTmp=null;//temporary variable for CheckBoxes panels creation
		
		ImageIcon iconSearch = new ImageIcon(getClass().getResource("/Search/magnifier glasses-min3.png"));
		ImageIcon iconNoSearch = new ImageIcon(getClass().getResource("/Search/magnifier glasses_NO_SEARCH.png"));
		JLabel iconLabel = null;

		panelSelection = new JPanel();
		panelSelection.setBackground(Color.WHITE);
//		panelSelection.setBounds(0, 0, 550, 652);
		panelSelection.setLayout(new BorderLayout());
		
		panelFeatures = new JPanel();
		panelFeatures.setBackground(Color.WHITE);
//		panelFeatures.setBounds(30,10,510,260);
		panelFeatures.setLayout(new GridLayout(0, 1));
//		panelFeatures.setBorder(BorderFactory.createCompoundBorder(
//				BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
//				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		checkBoxFeatures = new ArrayList <JCheckBox> ();		

		if(alFeaturesSel == null){//there are no Features Selected
		  for(int i = 0; i < alFeaturesCand.size(); i++){
			//each entry has a panel with a JCheckBox and an icon inside
			checkBoxTmp=new JCheckBox(alFeaturesCand.get(i));
			checkBoxTmp.setSelected(false);

			iconLabel = new JLabel(iconSearch);
			iconLabel.setOpaque(false);

			checkBoxIconPanelTmp = new JPanel();
			checkBoxIconPanelTmp.setLayout(new BoxLayout(checkBoxIconPanelTmp, BoxLayout.X_AXIS));
			checkBoxIconPanelTmp.add(checkBoxTmp);

			checkBoxIconPanelTmp.add(iconLabel);

			iconLabel.addMouseListener(getTermSearchIconListener("Extracted", alFeaturesCand.get(i), alFeaturesToHighlight));

			checkBoxFeatures.add(checkBoxTmp);	panelFeatures.add(checkBoxIconPanelTmp);
		  }
		}
		else{//there are some Features Selected
		  for(int i = 0; i < alFeaturesCand.size(); i++){//adding Features Extracted
			checkBoxTmp=new JCheckBox(alFeaturesCand.get(i));
			if(alFeaturesSel.contains(alFeaturesCand.get(i))) checkBoxTmp.setSelected(true);
			else checkBoxTmp.setSelected(false);

			iconLabel = new JLabel(iconSearch);
			iconLabel.setOpaque(false);

			checkBoxIconPanelTmp = new JPanel();
			checkBoxIconPanelTmp.setLayout(new BoxLayout(checkBoxIconPanelTmp, BoxLayout.X_AXIS));
			checkBoxIconPanelTmp.add(checkBoxTmp);

			checkBoxIconPanelTmp.add(iconLabel);

			iconLabel.addMouseListener(getTermSearchIconListener("Extracted", alFeaturesCand.get(i), alFeaturesToHighlight));
			checkBoxFeatures.add(checkBoxTmp); panelFeatures.add(checkBoxIconPanelTmp);
		  }

		  for(int i = 0; i < alFeaturesSel.size(); i++){//adding Features typed
			if(!alFeaturesCand.contains(alFeaturesSel.get(i))){
			  checkBoxTmp=new JCheckBox(alFeaturesSel.get(i));
			  checkBoxTmp.setSelected(true);
			  checkBoxTmp.setForeground(Color.RED);

			  iconLabel = new JLabel(iconNoSearch);
			  iconLabel.setOpaque(false);

			  checkBoxIconPanelTmp = new JPanel();
			  checkBoxIconPanelTmp.setLayout(new BoxLayout(checkBoxIconPanelTmp, BoxLayout.X_AXIS));
			  checkBoxIconPanelTmp.add(checkBoxTmp);

			  checkBoxIconPanelTmp.add(iconLabel);

			  checkBoxFeatures.add(checkBoxTmp); panelFeatures.add(checkBoxIconPanelTmp);
			}
		  }
		}
		
		JScrollPane scrollPanelFeatures = new JScrollPane(panelFeatures, 
				   ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				   ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPanelFeatures.setBounds(15, 10, 490, 260);
		scrollPanelFeatures.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.RAISED), 
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));


		if(fType==FeatureType.COMMONALITIES) addTermText="Add term at the commonality candidates:";
		else addTermText="Add term at the variability candidates:";
		JLabel addTermLabel = new JLabel(addTermText);
		addTermLabel.setBounds(16, 271, 400, 30);
		
		final JTextField addTermField = new JTextField();
		addTermField.setBounds(12, 293, 400, 30);

		buttonFeaturesAdd = new JButton("Add");
		buttonFeaturesAdd.setBounds(425, 293, 80, 30);
		buttonFeaturesAdd.addActionListener(
				new ActionListener(){
					
					@Override
					public void actionPerformed(ActionEvent arg0){
						addCheckBox(addTermField.getText());
						addTermField.setText("");
					}
				});
		
		JPanel addTermPanel = new JPanel();
		addTermPanel.setLayout(new BoxLayout(addTermPanel, BoxLayout.LINE_AXIS));
		addTermPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		addTermPanel.add(Box.createHorizontalGlue());
		addTermPanel.add(addTermField);
		addTermPanel.add(Box.createHorizontalGlue());
		addTermPanel.add(buttonFeaturesAdd);
		addTermPanel.add(Box.createHorizontalGlue());
		
		JPanel selectionControlPanel = new JPanel();
		selectionControlPanel.setLayout(new BoxLayout(selectionControlPanel, BoxLayout.PAGE_AXIS));
		selectionControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//		selectionControlPanel.add(Box.createVerticalGlue());
		selectionControlPanel.add(addTermPanel);
		selectionControlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		selectionControlPanel.add(buttonSelectionEnd);

		
//		  buttonPane.add(Box.createHorizontalGlue());
//		  buttonPane.add(prevOccurrButton);
//		  buttonPane.add(Box.createHorizontalGlue());
//		  buttonPane.add(occurrsLabelPanel);
//		  buttonPane.add(Box.createHorizontalGlue());
//		  buttonPane.add(nextOccurrButton);
//		  buttonPane.add(Box.createHorizontalGlue());
//		  buttonPane.add(XForwardOccurrButton);
//		  buttonPane.add(Box.createHorizontalGlue());

		
		//adding components to panel		
		panelSelection.add(scrollPanelFeatures, BorderLayout.CENTER);
//		panelSelection.add(addTermField);
//		panelSelection.add(addTermLabel);
//		panelSelection.add(buttonFeaturesAdd);
		panelSelection.add(selectionControlPanel, BorderLayout.SOUTH);
//		panelSelection.add(buttonSelectionEnd, BorderLayout.SOUTH);

		return panelSelection;
	}
	
	/**
	 * Returns the panel used to search terms occurrences.
	 * 
	 * @return - the JPanel used to search terms occurrences
	 */
	private JPanel createSearchPanel(){
		searchPanel = new JPanel();
		searchPanel.setLayout(new BorderLayout());
		
		//adding control buttons and a label for term occurences navigation
		XBackwardOccurrButton = new JButton("<<("+occurrJumpSpan+")");
		XBackwardOccurrButton.setPreferredSize(new Dimension(76, 22));
		XBackwardOccurrButton.addActionListener(getOccurrNavButtonListener(-occurrJumpSpan));

		prevOccurrButton = new JButton("<");
		prevOccurrButton.setPreferredSize(new Dimension(76, 22));
		prevOccurrButton.addActionListener(getOccurrNavButtonListener(-1));
		
		occurrsLabel = new JLabel("<html><div style=\"text-align: center;\">" + "x/y" + "</html>");

		occurrsLabelPanel = new JPanel();
		occurrsLabelPanel.add(occurrsLabel);
		occurrsLabelPanel.setPreferredSize(new Dimension(150, 22));
		occurrsLabelPanel.setBackground(Color.LIGHT_GRAY);

		nextOccurrButton = new JButton(">");
		nextOccurrButton.setPreferredSize(new Dimension(76, 22));
		nextOccurrButton.addActionListener(getOccurrNavButtonListener(1));

		XForwardOccurrButton = new JButton(">>("+occurrJumpSpan+")");
		XForwardOccurrButton.setPreferredSize(new Dimension(76, 22));
		XForwardOccurrButton.addActionListener(getOccurrNavButtonListener(occurrJumpSpan));
		
		//adding text area for term occurences visualization		
		occursTabbedPane = new JTabbedPane();
		occursTabbedPane.setPreferredSize(new Dimension(490, 574));
		
		//adding buttons panel for term occurences navigation
		buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(occursTabbedPane.getPreferredSize().width, 40));
		
		//initializing utility maps
		textTabs = new HashMap<String, JTextArea>();
		textIndexes = new HashMap<String, HashMap<String, Integer>>();
		currentFiles = new HashMap<String, String>();
		lastHighlightedTag = new HashMap<String, HashMap<String, Object>>();
		lastRemovedHighlights = new HashMap<String, HashMap<String, ArrayList<Highlight>>>();

		searchPanel.add(buttonPanel, BorderLayout.NORTH);
		searchPanel.add(occursTabbedPane, BorderLayout.CENTER);
		return searchPanel;
	}

	/**
	 * (Manuel M.) Returns a new ActionListener for features view buttons. The behaviour changes based on the parameter.
	 * 
	 * @param type - String representing the required behaviour type
	 * @param term - String representing the name of the feature candidate
	 * @param alFeaturesToHighlight - if not null, the terms in alFeaturesToHighlight will be highlighted 
	 * @return the new ActionListener
	 */
	private MouseListener getTermSearchIconListener(String type, final String term, final ArrayList<String> alFeaturesToHighlight) {
		
	  if (type=="Extracted") return new MouseAdapter(){			
			
		@Override
		public void mouseClicked(MouseEvent me){				
			
		  //creation of occurrences navigation panel
		  occursTabbedPane.removeAll();
		  textTabs.clear();
		  HashMap<String, ArrayList<int[]>> filesListTmp = null;//files list for a term

		  Iterator<Entry<String, ArrayList<int[]>>> filesIterator = null;
		  Entry<String, ArrayList<int[]>> occurrencesList = null;

		  //variables used to remember last selected tab for each relevant term
		  Component[] compArrTmp = null;
		  String tabTitle = null;

		  currentSelectedCheckBox=term;

		  /* ***DEBUG*** */
		  if (debug) System.out.println("\nCLICCATO SU UN TERMINE!()\nTerm="+term);
		  /* ***DEBUG*** */

		  /* ***DEBUG*** */
		  if (debug) System.out.println("relevantTerms.get(term)="+relevantTerms.get(term));
		  /* ***DEBUG*** */

		  filesListTmp = relevantTerms.get(term);

		  /* ***DEBUG*** */
		  if (debug) System.out.println("filesListTmp.entrySet().iterator()="+filesListTmp.entrySet().iterator());
		  /* ***DEBUG*** */

		  filesIterator = filesListTmp.entrySet().iterator();
		  while (filesIterator.hasNext()) {//for each file a JScrollPane is added
			occurrencesList = filesIterator.next();

			JScrollPane jScrP = getRegisteredTabTextFile(term, occurrencesList.getKey(), alFeaturesToHighlight);

			occursTabbedPane.addTab(occurrencesList.getKey(), jScrP);

			/* ***VERBOSE****/
			if (verbose) System.out.println("\n***\nNumero di componenti di jScrP: "+jScrP.getComponentCount());
			for (int h=0; h<jScrP.getComponentCount(); h++){
			  System.out.println("Classe del componente "+h+": "+jScrP.getComponent(h).getClass());
			}
			System.out.println("Classe del ViewPort: "+jScrP.getViewport().getClass());
			/* ***VERBOSE****/

		  }

		  //adding occurences navigation controls
		  buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		  buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 5, 6, 5));
//		  buttonPanel.setLocation(0, 10);
		  
		  buttonPanel.add(Box.createHorizontalGlue());
		  buttonPanel.add(XBackwardOccurrButton);
		  buttonPanel.add(Box.createHorizontalGlue());
		  buttonPanel.add(prevOccurrButton);
		  buttonPanel.add(Box.createHorizontalGlue());
		  buttonPanel.add(occurrsLabelPanel);
		  buttonPanel.add(Box.createHorizontalGlue());
		  buttonPanel.add(nextOccurrButton);
		  buttonPanel.add(Box.createHorizontalGlue());
		  buttonPanel.add(XForwardOccurrButton);
		  buttonPanel.add(Box.createHorizontalGlue());
//		  buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		  
		  searchPanel.repaint();
		  
		  //restoring previous occurences panel state, if any.
		  if (!currentFiles.containsKey(term)) currentFiles.put(term, 
				  ((JScrollPane)occursTabbedPane.getSelectedComponent()).getName());

		  else {
			tabTitle = currentFiles.get(term);
			compArrTmp = occursTabbedPane.getComponents();
			for (int k=0; k< compArrTmp.length; ++k)
				if (compArrTmp[k].getName()==tabTitle) occursTabbedPane.setSelectedComponent(compArrTmp[k]);
		  }

		  selectCurrentOccurrence(currentSelectedCheckBox,
			  ((JScrollPane)occursTabbedPane.getSelectedComponent()).getName());

		  occursTabbedPane.addMouseListener(
			new MouseAdapter(){
			  @Override
			  public void mouseClicked(MouseEvent me){						
			    selectCurrentOccurrence(currentSelectedCheckBox,
					((JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
			    currentFiles.put(currentSelectedCheckBox,
			    	((JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
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
			  HashMap<String, ArrayList<int[]>> occurrFilesList=null;		
			  ArrayList<int[]> occurrIndexesList=null;
//			  int previousIndex=0;//previous index in occurrIndexesList of highlited occurrence
//			  int previousOccurrenceIndex =0;//current index in the text of occurrence to highlite
			  int currentIndex=0;//current index in occurrIndexesList of occurrence to highlite
			  int[] occurrence=null;//current occurrence to highlite in the text
			  Object highlightTag=null;//highlight tag that will be added to the text
			  ArrayList<Highlight> lastRemovedTags=null;//last removed highlighted commonality tags 
			  ArrayList<Highlight> commTagsToRemove=null;//commonality tags to highlight
			  String fileName=((JScrollPane)occursTabbedPane.getSelectedComponent()).getName();
			  
			  JTextArea jta= textTabs.get(( (JScrollPane)occursTabbedPane.getSelectedComponent()).getName());
			  currentIndex= textIndexes.get(currentSelectedCheckBox).get(fileName);

			  //calculate next occurrence index to use, depending on the jump parameter
			  occurrFilesList = relevantTerms.get(currentSelectedCheckBox);
			  occurrIndexesList=occurrFilesList.get(fileName);
			  			  
			  if (jump>0){
				if (currentIndex<occurrIndexesList.size()-jump) currentIndex+=jump;
				else currentIndex=occurrIndexesList.size()-1;
			  }
			  else{
				if (currentIndex+jump>=0) currentIndex+=jump;
				else currentIndex=0;
			  }

			  occurrence = occurrIndexesList.get(currentIndex);
			  
			  Highlighter hilite = jta.getHighlighter();
			  
			  //initializing of lastHighlightedTag, if necessary
			  if(lastHighlightedTag.get(currentSelectedCheckBox)==null) 
			    lastHighlightedTag.put(currentSelectedCheckBox, new HashMap<String, Object>());
			  
			  //removing last highlighted tag for this term and file
			  highlightTag=lastHighlightedTag.get(currentSelectedCheckBox).get(fileName);
				  
			  if(highlightTag!=null) hilite.removeHighlight(highlightTag);			  
			  
			  
			  //initializing of lastRemovedHighlights, if necessary
			  if(lastRemovedHighlights.get(currentSelectedCheckBox)==null) 
				  lastRemovedHighlights.put(currentSelectedCheckBox, new HashMap<String, ArrayList<Highlight>>());
			  
			  //re-putting last removed highlighted commonality tags for this term and file
			  lastRemovedTags=lastRemovedHighlights.get(currentSelectedCheckBox).get(fileName);
			  
			  if(lastRemovedTags!=null) try{
			    for(int h=0; h<lastRemovedTags.size(); ++h)
				  hilite.addHighlight(lastRemovedTags.get(h).getStartOffset(), lastRemovedTags.get(h).getEndOffset(),
				  lastRemovedTags.get(h).getPainter());
			  } catch (BadLocationException e) {
				System.out.println("BadLocationException\nTerm: "+currentSelectedCheckBox+" - occurrence: "+occurrence);
				e.printStackTrace();
			  }
			  
			  //checking what highlighted tags already are in next occurrence text interval
			  commTagsToRemove = new ArrayList<Highlight>();
			  for (Highlight tmp: hilite.getHighlights())
//				if (tmp.getStartOffset()>=occurrence 
//					&& tmp.getEndOffset()<=
//					occurrence+originalTermsVersions.get(currentSelectedCheckBox).get(fileName).length())
//					  commTagsToRemove.add(tmp);
				if (tmp.getStartOffset()>=occurrence[0] && tmp.getEndOffset()<=occurrence[1]) commTagsToRemove.add(tmp);

			  //removing highlighted tags that already are in next occurrence text interval
			  for (Highlight tmp: commTagsToRemove) hilite.removeHighlight(tmp);
			  
			  //saving last removed Commonality tags in lastRemovedCommHighlights
			  lastRemovedHighlights.get(currentSelectedCheckBox).put(fileName, commTagsToRemove);
			  
			  
			  //highlighting current occurrence and saving it in lastHighlightedTag
			  try {
//				  lastHighlightedTag.get(currentSelectedCheckBox).put(
//					fileName, hilite.addHighlight(occurrence,
//					 occurrence+originalTermsVersions.get(currentSelectedCheckBox).get(fileName).length(),
//					highlightPainter[1]) );
				  lastHighlightedTag.get(currentSelectedCheckBox).put(
					fileName, hilite.addHighlight(occurrence[0], occurrence[1], highlightPainter[1]) );
				  				
			  } catch (BadLocationException e) {
				System.out.println("BadLocationException\nTerm: "+currentSelectedCheckBox+" - occurrence: "+occurrence);
				e.printStackTrace();
			  }
			  
			  //set Caret position and text selection
			  jta.requestFocusInWindow();
			  jta.getCaret().setVisible(true);
			  jta.setCaretPosition(occurrence[0]);
//			  jta.setSelectionStart(occurrenceIndex);
//			  jta.setSelectionEnd(occurrenceIndex+checkBoxCommonalities.get(currentSelectedCheckBox).getText().length());
//			  jta.setSelectionEnd(occurrenceIndex+currentSelectedCheckBox.length());
//			  jta.setSelectionColor(Color.CYAN);

			  //updating current occurrence index for this file
			  textIndexes.get(currentSelectedCheckBox).put(fileName, currentIndex);

			  //updating occurrences label
			  occurrsLabel.setText( (currentIndex+1)+"/"+occurrIndexesList.size()+"[Index: "+occurrence[0]+"]");

			  /* ***VERBOSE****/					
			  if (verbose) System.out.println(
				 "\n*****\nSELECTED TAB: "+fileName
				+"\noccurrenceIndex: "+occurrence
				+"\ncurrentSelectedCheckBox(il testo): "+currentSelectedCheckBox
				+"\ncurrentSelectedCheckBox(lunghezza): "+currentSelectedCheckBox.length()
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
	private void selectCurrentOccurrence(String currentSelectedCheckBox, String file) {
		  HashMap<String, ArrayList<int[]>> occurrFilesList=null;		
		  ArrayList<int[]> occurrIndexesList=null;
		  int[] occurrence=null;
		  Object highlightTag=null;//highlight tag that will be added to the text
		  ArrayList<Highlight> lastRemovedTags=null;//last removed highlighted commonality tags 
		  ArrayList<Highlight> commTagsToRemove=null;//commonality tags to highlight
			
		  JTextArea jta= textTabs.get(file);
		  int currentIndex= textIndexes.get(currentSelectedCheckBox).get(file);

		  //calculating current occurrence index for selection
		  occurrFilesList = relevantTerms.get(currentSelectedCheckBox);
		  occurrIndexesList=occurrFilesList.get(file);
		  occurrence = occurrIndexesList.get(currentIndex);

		  Highlighter hilite = jta.getHighlighter();
		  
		  //initializing of lastHighlightedTag, if necessary
		  if(lastHighlightedTag.get(currentSelectedCheckBox)==null) 
			  lastHighlightedTag.put(currentSelectedCheckBox, new HashMap<String, Object>());

		  //removing last highlighted tag for this term and file, if any
		  highlightTag=lastHighlightedTag.get(currentSelectedCheckBox).get(file);

		  if(highlightTag!=null) hilite.removeHighlight(highlightTag);
		  
		  
		  //initializing of lastRemovedCommHighlights, if necessary
		  if(lastRemovedHighlights.get(currentSelectedCheckBox)==null) 
			  lastRemovedHighlights.put(currentSelectedCheckBox, new HashMap<String, ArrayList<Highlight>>());
		  
		  //re-putting last removed highlighted commonality tags for this term and file
		  lastRemovedTags=lastRemovedHighlights.get(currentSelectedCheckBox).get(file);
		  
		  if(lastRemovedTags!=null) try{
		    for(int h=0; h<lastRemovedTags.size(); ++h)
			  hilite.addHighlight(lastRemovedTags.get(h).getStartOffset(), lastRemovedTags.get(h).getEndOffset(),
			  lastRemovedTags.get(h).getPainter());
		  } catch (BadLocationException e) {
			System.out.println("BadLocationException\nTerm: "+currentSelectedCheckBox+" - occurrence: "+occurrence);
			e.printStackTrace();
		  }
		  
		  //checking what highlighted tags already are in next occurrence text interval
		  commTagsToRemove = new ArrayList<Highlight>();
		  for (Highlight tmp: hilite.getHighlights())
//			if (tmp.getStartOffset()>=occurrenceIndex && tmp.getEndOffset()<=
//			    occurrenceIndex+originalTermsVersions.get(currentSelectedCheckBox).get(file).length())
//			  commTagsToRemove.add(tmp);
			if (tmp.getStartOffset()>=occurrence[0] && tmp.getEndOffset()<=occurrence[1]) commTagsToRemove.add(tmp);

		  //removing highlighted tags that already are in next occurrence text interval
		  for (Highlight tmp: commTagsToRemove) hilite.removeHighlight(tmp);
		  
		  //saving last removed Commonality tags in lastRemovedCommHighlights
		  lastRemovedHighlights.get(currentSelectedCheckBox).put(file, commTagsToRemove);
		  
		  //highlighting current occurrence and saving it in lastHighlightedTag		  		  
		  try {
//			lastHighlightedTag.get(currentSelectedCheckBox).put(
//			  ((JScrollPane)occursTabbedPane.getSelectedComponent()).getName(),
//			  hilite.addHighlight(occurrenceIndex, 
//				occurrenceIndex+originalTermsVersions.get(currentSelectedCheckBox).get(file).length(), highlightPainter[1]));
			lastHighlightedTag.get(currentSelectedCheckBox).put(
					  ((JScrollPane)occursTabbedPane.getSelectedComponent()).getName(),
					  hilite.addHighlight(occurrence[0], occurrence[1], highlightPainter[1]));
			  
		  } catch (BadLocationException e) {
			  System.out.println("BadLocationException\nTerm: "+currentSelectedCheckBox+" - occurrence: "+occurrence);
			  e.printStackTrace();
		  }
		  
		  //setting Caret position and text selection
		  jta.requestFocusInWindow();
		  jta.getCaret().setVisible(true);
		  jta.setCaretPosition(occurrence[0]);
//		  jta.setSelectionStart(occurrenceIndex);
//		  jta.setSelectionEnd(occurrenceIndex+currentSelectedCheckBox.length());
//		  jta.setSelectionEnd(occurrenceIndex+checkBoxCommonalities.get(currentSelectedCheckBox).getText().length());
//		  jta.setSelectionColor(Color.CYAN);
		  
		  //updating occurrences label
		  occurrsLabel.setText( (currentIndex+1)+"/"+occurrIndexesList.size()+"[Index: "+occurrence[0]+"]");
		
	}	

	/** 
	 * Adds a JCheckBox to the commonalities candidates tab.
	 * 
	 * @param s - String containing the feature name to be added
	 */
	private void addCheckBox(String s){
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
	
	/** 
	 * Removes a JCheckBox from the commonalities candidates tab.
	 * 
	 * @param s - String containing the feature name to be removed
	 */
	private void removeCheckBox(String s){
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
