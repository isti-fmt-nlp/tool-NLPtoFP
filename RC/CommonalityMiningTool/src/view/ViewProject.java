/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;


public class ViewProject implements Observer, Runnable
{
	private ModelProject modelProject = null;
	
	private ControllerProject controllerProject = null;
	
	private JFrame frameProject = new JFrame("Commonality Mining Tool");
	
	private JMenuBar menu = new JMenuBar();
	
	private JMenu menuProject=null;//Project Management Menu
	private JMenu menuFiles;//Files Management Menu
	private JMenu menuFeatures=null;//Features Management Menu
	private JMenu menuDiagram=null;//Diagram Management Menu
	
	//Project Management Menu items
	private JMenuItem menuProjectCreate=null, menuProjectDelete=null, 
					  menuProjectLoad=null, menuProjectSave=null,
					  menuProjectExit=null;

	//Files Management Menu items	
	private JMenuItem menuFilesLoad=null, menuFilesDelete=null;


	//Features Management Menu items
	private JMenuItem menuFeaturesExtractComm=null, menuFeaturesExtractVari=null, 
					  menuFeaturesSelectComm=null, menuFeaturesSelectVari=null;

	//Diagram Management Menu items	
	private JMenuItem menuDiagramRestart=null, menuDiagramOpen=null;

//	private JButton buttonProjectCP=null, buttonProjectSP=null,
//					buttonProjectLF=null, buttonProjectEC=null, buttonProjectEV=null;
	
	private JPopupMenu menuTreeProject = null;
	
	private JMenuItem menuTree = null;
	
	private Thread throbber=null;
	
	private boolean stateThrobber = false;

	private JButton lastButtonSelectionEnd = null;//the last selection button loaded into the central panel 
	
	private JButton buttonCommonalitiesSelectionEnd = null;//the commonalities selection button 
	
	private JButton buttonVariabilitiesSelectionEnd = null;//the variabilities selection button 
	
	private ViewPanelLateral panelLateralProject = null;
	
	private ViewPanelCentral panelCentralProject = null;

	private static boolean verbose=true;//variabile usata per attivare stampe nel codice
	
	/** Costruttore
	 * 
	 * @param modelProject
	 */
	public ViewProject(ModelProject modelProject)
	{
		this.modelProject = modelProject;
		
		/* Creazione JFrame */
		frameProject.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameProject.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frameProject.setLocationRelativeTo(null);
		frameProject.setLayout(null);		
		frameProject.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		frameProject.setLocation(0,  0);
//		frameProject.setSize(700, 400);
//		frameProject.setMaximizedBounds(null);
		frameProject.setJMenuBar(menu);
	}
	
	/** Thread: Gestisce la funzionalit� del throbber
	 * 
	 */
	@Override
	public void run() 
	{
		JLabel jl1 = new JLabel("Analysing input files...");
		jl1.setBounds(new Rectangle(20,10,250,30));
		
		System.out.println("getClass().getResource(/Throbber/\"throbber.gif\"): "+getClass().getResource("/Throbber/throbber.gif"));
		System.out.println("getClass(): "+getClass());
		
//		ImageIcon i=null;
//		try {
//			i = new ImageIcon(ImageIO.read(this.getClass().getResource("throbber.gif")));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		ImageIcon i = new ImageIcon(getClass().getResource("/Throbber/throbber.gif"));

//	    System.out.println("./src/DATA/Throbber/throbber.gif: "+getClass().getResource("./src/DATA/Throbber/throbber.gif"));


//	    System.out.println("throbber.gif: "+getClass().getResource("/throbber.gif"));

//		ImageIcon i = new ImageIcon("./src/DATA/Throbber/throbber.gif");
		
		JLabel jl2  = new JLabel();
		jl2.setBounds(new Rectangle(270,10,35,35));
		jl2.setIcon(i);		
		
		JFrame jf = new JFrame("Extracting commonalities...");
		jf.setLayout(null);
		jf.setBackground(Color.WHITE);
		jf.setBounds(375, 375, 350, 80);
		jf.add(jl1);
		jf.add(jl2);
		jf.setVisible(true);
		
		while(!stateThrobber)
			jf.repaint();
		
		jf.setVisible(false);
		jf.dispose();
	}
	
	/** Setta lo stato del throbber
	 * 
	 * @param b
	 */
	private void setStateThrobber(boolean b)
	{
		stateThrobber = b; 
	}
	
	/** Crea ed inizializza le strutture della GUI, aggiungendo ad ognuno di esse come ascoltare degli eventi il controllore 
	 * 
	 * @param controllerProject controllore
	 */
	public void addListener(ControllerProject controllerProject) 
	{
		this.controllerProject = controllerProject;
		
		/* Creazione MenuBar */
		
		/*MenuProject*/
		menuProject = new JMenu("Project");

		menuProjectCreate = new JMenuItem("Create Project");
		menuProjectCreate.addActionListener(controllerProject);
		
		menuProjectDelete = new JMenuItem("Delete Project");
		menuProjectDelete.addActionListener(controllerProject);
		menuProjectDelete.setEnabled(false);
		
		menuProjectLoad = new JMenuItem("Load Project");
		menuProjectLoad.addActionListener(controllerProject);
		
		menuProjectSave = new JMenuItem("Save Project");
		menuProjectSave.addActionListener(controllerProject);
		menuProjectSave.setEnabled(false);
		
		menuProjectExit = new JMenuItem("Exit");
		menuProjectExit.addActionListener(controllerProject);	
		
		menuProject.add(menuProjectCreate);
		menuProject.add(menuProjectDelete);
		menuProject.addSeparator();
		menuProject.add(menuProjectLoad);		
		menuProject.add(menuProjectSave);
		menuProject.addSeparator();
		menuProject.add(menuProjectExit);
		
		menu.add(menuProject);

		/*MenuFiles*/
		menuFiles=new JMenu("Files");

		menuFilesLoad = new JMenuItem("Load File");
		menuFilesLoad.addActionListener(controllerProject);
		menuFilesLoad.setEnabled(false);
		
		menuFilesDelete = new JMenuItem("Delete File");
		menuFilesDelete.addActionListener(controllerProject);
		menuFilesDelete.setEnabled(false);
		
		menuFiles.add(menuFilesLoad);		
		menuFiles.add(menuFilesDelete);
		
		menu.add(menuFiles);
		
		/*MenuFeatures*/
		menuFeatures=new JMenu("Features");

		menuFeaturesExtractComm = new JMenuItem("Extract Commonalities");
		menuFeaturesExtractComm.addActionListener(controllerProject);
		menuFeaturesExtractComm.setEnabled(false);
		
		menuFeaturesExtractVari = new JMenuItem("Extract Variabilities");
		menuFeaturesExtractVari.addActionListener(controllerProject);
		menuFeaturesExtractVari.setEnabled(false);

		menuFeaturesSelectComm = new JMenuItem("Select Commonalities");
		menuFeaturesSelectComm.addActionListener(controllerProject);
		menuFeaturesSelectComm.setEnabled(false);
		
		menuFeaturesSelectVari = new JMenuItem("Select Variabilities");
		menuFeaturesSelectVari.addActionListener(controllerProject);
		menuFeaturesSelectVari.setEnabled(false);
		
		menuFeatures.add(menuFeaturesExtractComm);
		menuFeatures.add(menuFeaturesExtractVari);
		menuFeatures.addSeparator();
		menuFeatures.add(menuFeaturesSelectComm);		
		menuFeatures.add(menuFeaturesSelectVari);
		
		menu.add(menuFeatures);

		/*MenuDiagram*/
		menuDiagram=new JMenu("Diagram");

		menuDiagramOpen = new JMenuItem("Open Diagram");
		menuDiagramOpen.addActionListener(controllerProject);
		menuDiagramOpen.setEnabled(false);
		
		menuDiagramRestart = new JMenuItem("Restart Diagram");
		menuDiagramRestart.addActionListener(controllerProject);
		menuDiagramRestart.setEnabled(false);
		
		menuDiagram.add(menuDiagramOpen);		
		menuDiagram.add(menuDiagramRestart);
		
		menu.add(menuDiagram);

		
//		/* Creazione Bottoni */
//		buttonProjectCP = new JButton("Create Project");
//		buttonProjectCP.addActionListener(controllerProject);
//		buttonProjectCP.setBounds(new Rectangle(10,5,180,35));
//		
//		buttonProjectSP = new JButton("Save Project");
//		buttonProjectSP.addActionListener(controllerProject);
//		buttonProjectSP.setBounds(new Rectangle(220,5,180,35));
//		buttonProjectSP.setEnabled(false);
//		
//		buttonProjectLF = new JButton("Load File");
//		buttonProjectLF.addActionListener(controllerProject);
//		buttonProjectLF.setBounds(new Rectangle(430,5,180,35));
//		buttonProjectLF.setEnabled(false);
//		
//		buttonProjectEC = new JButton("Extract Commonalities");
//		buttonProjectEC.addActionListener(controllerProject);
//		buttonProjectEC.setBounds(new Rectangle(650,5,180,35));
//		buttonProjectEC.setEnabled(false);
//		
//		buttonProjectEV = new JButton("Extract Variabilities");
//		buttonProjectEV.addActionListener(controllerProject);
//		buttonProjectEV.setBounds(new Rectangle(870,5,180,35));
//		buttonProjectEV.setEnabled(false);
//		
//		frameProject.add(buttonProjectCP);
//		frameProject.add(buttonProjectSP);
//		frameProject.add(buttonProjectLF);
//		frameProject.add(buttonProjectEC);
//		frameProject.add(buttonProjectEV);
		
		
		menuTree = new JMenuItem("Delete Selected File");
		menuTree.addActionListener(controllerProject);
		
		menuTreeProject = new JPopupMenu("Men� Tree");
		menuTreeProject.add(menuTree);
		
		/* Creazione pannello laterale */
		panelLateralProject = new ViewPanelLateral(menuTreeProject);

		buttonCommonalitiesSelectionEnd = new JButton("Select Commonalities");
		buttonCommonalitiesSelectionEnd.setBounds(330, 640, 180, 30);//+50?
//		buttonCommonalitiesSelectionEnd.setBounds(330, 590, 180, 30);//+50?
		buttonCommonalitiesSelectionEnd.addActionListener(controllerProject);
		
		buttonVariabilitiesSelectionEnd= new JButton("Select Variabilities");
		buttonVariabilitiesSelectionEnd.setBounds(330, 640, 180, 30);//+50?
//		buttonVariabilitiesSelectionEnd.setBounds(330, 590, 180, 30);//+50?
		buttonVariabilitiesSelectionEnd.addActionListener(controllerProject);
		
		/* Creazione pannello centrale */
//		panelCentralProject = new ViewPanelCentral(lastButtonSelectionEnd);
		panelCentralProject = new ViewPanelCentral();
		
		frameProject.addWindowListener(controllerProject);
		frameProject.setVisible(true);
	}
	
	/** 
	 * Manages notify events sent by viewProject.
	 * 
	 * @param os - the observed Object 
	 * @param o - the argument of the notify event
	 */
	@Override
	public void update(Observable os, Object o){
		if(o.equals("End Extract Commonalities")){
			frameProject.setEnabled(true);
			setStateThrobber(true);
//			buttonProjectEC.setEnabled(false);
//			buttonProjectEV.setEnabled(true);
			
	    	//activating menu items
	    	menuFeaturesExtractComm.setEnabled(false);
			menuFeaturesExtractVari.setEnabled(true);
	    	
			panelLateralProject.setAnalysisLeafTree();	
			
//			File f = new File("./src/DATA/Sound/analysis.wav");
//		    AudioInputStream ais;
//			try 
//			{
//				ais = AudioSystem.getAudioInputStream(f);
//				
//			    DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
//			    
//			    Clip clip = (Clip) AudioSystem.getLine(info);
//			    clip.open(ais);
//			    clip.start();
//			} 
//			catch (UnsupportedAudioFileException ex) 
//			{
//				System.out.println("Exception update: " + ex.getMessage());
//				ex.printStackTrace();
//	            return;
//			} 
//			catch (IOException ex) 
//			{
//				System.out.println("Exception update: " + ex.getMessage());
//				ex.printStackTrace();
//	            return;
//			} 
//			catch (LineUnavailableException ex) 
//			{
//				System.out.println("Exception update: " + ex.getMessage());
//				ex.printStackTrace();
//	            return;
//			}		    
		}
		else if(o.equals("End Extract Variabilities")){
			frameProject.setEnabled(true);
			setStateThrobber(true);
//			buttonProjectEV.setEnabled(false);

	    	//activating menu items
			menuFeaturesExtractVari.setEnabled(false);
	    	
    		/* ***VERBOSE****/
			if (verbose){
				System.out.println("\n***\nStampo le Variabilities Candidates: ");
				for (String tmp: modelProject.readVariabilitiesCandidates()) System.out.println("-"+tmp);
				System.out.println("FrameProject is enabled? "+frameProject.isEnabled());
			}
			/* ***VERBOSE****/

			
//			File f = new File("./src/DATA/Sound/analysis.wav");
//		    AudioInputStream ais;
//			try 
//			{
//				ais = AudioSystem.getAudioInputStream(f);
//				
//			    DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
//			    
//			    Clip clip = (Clip) AudioSystem.getLine(info);
//			    clip.open(ais);
//			    clip.start();
//			} 
//			catch (UnsupportedAudioFileException ex) 
//			{
//				System.out.println("Exception update: " + ex.getMessage());
//				ex.printStackTrace();
//	            return;
//			} 
//			catch (IOException ex) 
//			{
//				System.out.println("Exception update: " + ex.getMessage());
//				ex.printStackTrace();
//	            return;
//			} 
//			catch (LineUnavailableException ex) 
//			{
//				System.out.println("Exception update: " + ex.getMessage());
//				ex.printStackTrace();
//	            return;
//			}		    
		}
		else if(o.equals("End Commonalities Selected"))
		{
			panelCentralProject.refreshTabFeaturesSelected(
				modelProject.readPathCommonalitiesSelectedHTML(), ViewPanelCentral.FeatureType.COMMONALITIES);
			frameProject.remove(panelCentralProject.getPanelAnalysis());
			frameProject.add(panelCentralProject.getPanelAnalysis());	
		}
		else if(o.equals("End Variabilities Selected"))
		{
			panelCentralProject.refreshTabFeaturesSelected(
				modelProject.readPathVariabilitiesSelectedHTML(), ViewPanelCentral.FeatureType.VARIABILITIES);
			frameProject.remove(panelCentralProject.getPanelAnalysis());
			frameProject.add(panelCentralProject.getPanelAnalysis());	
		}
		frameProject.repaint(); 	
	}
	
	/** Assegna un nome al progetto
	 * 
	 * @return s stringa contenente il nome del progetto
	 */
	public String assignNameProjectDialog()
	{				
			String s = null;
			
		 	JTextField jtf = new JTextField();
		 	
		    Object[] o1 = {"Name project: ", jtf};


		    Object[] o2 = { "Cancel", "OK" };
		    
		    int i = JOptionPane.showOptionDialog(new JFrame("Create Project"),
		            o1, "",
		            JOptionPane.YES_NO_OPTION, JOptionPane.DEFAULT_OPTION, null,
		            o2, o2[1]);
		    
		    if(i == JOptionPane.NO_OPTION)
		    {
		    	if((s = jtf.getText()) != null)
		    	{
		    		if(!s.trim().equals(""))
		    		{
				    	return s;
		    		}
		    		else
		    		{
		    			errorDialog("You did not put a name to the project");
		    			return null;
		    		}
		    	}
		    	else
		    	{
		    		errorDialog("You did not put a name to the project");
	    			return null;
		    	}
		    }		    		      
		    else
		    	return null;
	}
	
	/** Cancella il progetto
	 * 
	 */
	public void deleteProjectDialog()
	{
		JFrame f = new JFrame("Delete Project");
		
    	Object[] options = {"No","Yes"};			
		
		int i = JOptionPane.showOptionDialog(
				f, "Do you want delete the project?", "Delete Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
		if(i == 1)
		{
			modelProject.deleteProject();
			frameProject.remove(panelLateralProject.getPanelTree());
			frameProject.remove(panelCentralProject.getPanelAnalysis());
			panelLateralProject = new ViewPanelLateral(menuTreeProject);
			panelCentralProject = new ViewPanelCentral();
			frameProject.repaint();
			
			menuDiagramOpen.setEnabled(false);
			menuDiagramRestart.setEnabled(false);
			
			menuFeaturesExtractComm.setEnabled(false);
			menuFeaturesExtractVari.setEnabled(false);
			menuFeaturesSelectComm.setEnabled(false);
			menuFeaturesSelectVari.setEnabled(false);
			
			menuFilesLoad.setEnabled(false);
			menuFilesDelete.setEnabled(false);
			
			menuProjectSave.setEnabled(false);
			menuProjectDelete.setEnabled(false);
		}
		
		frameProject.repaint();
	}
	
	/** Carica un progetto
	 * 
	 * @return s path del file contenente il progetto da caricare
	 */
	public String loadProjectDialog()
	{
		FileDialog d = new FileDialog(new JFrame("Load File"));
    	d.setMode(FileDialog.LOAD);
    	d.setFilenameFilter(new FilterFileProject());
	    d.setDirectory(".");
	    d.setVisible(true);
	    
	    if(d.getFile() == null)
	    	return null;
	    
	//	    if(!buttonProjectEC.isEnabled())
	//    		buttonProjectEC.setEnabled(true);

	    return d.getFile().toString();
	}
	
	/** Salva il progetto
	 * 
	 *  @return 1 se l'utente vuole salvare il progetto
	 *  @return 0 altrimenti
	 */
	public int saveProjectDialog()
	{
		JFrame f = new JFrame("Save Project");
		
    	Object[] options = {"No","Yes"};			
		
		int i = JOptionPane.showOptionDialog(
				f, "Do you want save the project?", "Save Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
		if(i == 1)
			return 1;
		
		else
			return 0;
	}
	
	/** Carica un file nel progetto
	 * 
	 * @return s[2] array di stringhe contenente rispettivamente il nome del file e la path
	 * 			    del file
	 */
	public String [] loadFileDialog()
	{
		String [] s = new String[2];
		
		FileDialog d = new FileDialog(new JFrame("Load File"));
    	d.setMode(FileDialog.LOAD);
    	d.setFilenameFilter(new FilterFileInput());
	    d.setDirectory(".");
	    d.setVisible(true);
	    
//	    System.out.println("1]d.getFile()="+d.getFile());
	    
	    if(d.getFile() == null)
	    	return null;
	    
//	    System.out.println("1]d.getFile()="+d.getFile());

	    s[0] = d.getFile().toString();
    	s[1] = d.getDirectory() + d.getFile().toString();
    	
//    	buttonProjectEC.setEnabled(true);
    	
    	//activating menu items
    	menuFilesDelete.setEnabled(true);
    	menuFeaturesExtractComm.setEnabled(true);
    	
    	
    	if((panelLateralProject.addNodeInput(s[0])) == false)
    	{
    		errorDialog("The file" + s[0] + " has been inserted");
    		return null;
    	}
    	
    	frameProject.repaint();
      
	    return s;
	}
	
	/** 
	 * Delete from the project the current selected input file in the project tree
	 *  
	 * @return i - the index of the deleted file
	 */
	public int deleteSelectedFileDialog()
	{
		JFrame f = new JFrame("Delete File");
		
    	Object[] options = {"No","Yes"};			
		
		int i = JOptionPane.showOptionDialog(
				f, "Do you want delete the file?", "Delete File", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
		if(i == 1)
		{
			if((i = panelLateralProject.deleteSelectedInputNode()) != -1)
			{
				if(panelLateralProject.getAnalysisLeafTree().size() == 0){
//				  buttonProjectEC.setEnabled(false);
					
				  //activating menu items
				  menuFilesDelete.setEnabled(false);
				  menuFeaturesExtractComm.setEnabled(false);
				}

			}
			return i;
		}	
		return -1;
	}

	/** 
	 * Deletes from the project an input file  chosen by the user
	 *  
	 * @return i - the index of the deleted file
	 */
	public int deleteFileDialog(){
		boolean deleted=false;
		JFrame f = new JFrame("Delete File");
		
    	Object[] options = panelLateralProject.getInputFiles();			
		
		int i = JOptionPane.showOptionDialog(
				f, "Choose the file you want to delete", "Delete File", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION,
				null, options, options[0]);
//		int i = JOptionPane.showOptionDialog(
//				f, "Choose the file you want to delete", "Delete File", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE
//				, null, options, options[1]);

		/* ***VERBOSE*** */
		if(verbose)System.out.println("Scelta fatta: "+i);
		/* ***VERBOSE*** */
		
//    buttonProjectEC.setEnabled(false);

		deleted=panelLateralProject.deleteSpecifiedInputNode(i);
		if(deleted){
		  if(panelLateralProject.getAnalysisLeafTree().size() == 0){
			  //activating menu items
			  menuFilesDelete.setEnabled(false);
			  menuFeaturesExtractComm.setEnabled(false);
		  }
		  return i;
		}
		else return -1;
	}
	
	/** Estrae le Commonalities
	 * 
	 */
	public void extractCommonalitiesdDialog()
	{
		JFrame f = new JFrame("Extract Commonalities");
		
    	Object[] options = {"No","Yes"};			
		
		int i = JOptionPane.showOptionDialog(
				f, "Do you want extract commonalities from the file?", "Extract Commonalities", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
		if(i == 1)
		{
			frameProject.setEnabled(false);
			modelProject.analyzesFileProject();
			setStateThrobber(false);
			throbber = new Thread(this);
			throbber.start();
			frameProject.repaint();
		}
	}
	
	/** 
	 * Extract variabilities from input files.
	 * 
	 */
	public void extractVariabilitiesDialog()
	{
		JFrame f = new JFrame("Extract Variabilities");
		
    	Object[] options = {"No","Yes"};			
		
		int i = JOptionPane.showOptionDialog(
				f, "Do you want extract variabilities from the file?", "Extract Variabilities", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
		if(i == 1)
		{
			frameProject.setEnabled(false);
			modelProject.extractVariabilities();
			setStateThrobber(false);
			throbber = new Thread(this);
			throbber.start();
			frameProject.repaint();
		}
	}
	
	/** Mostra l'errore commesso dall'utente
	 * 
	 * @param s stringa contenente l'errore commesso dall'utente
	 */
	public void errorDialog(String s)
	{
		JFrame f = new JFrame("Error");
		
    	Object[] options = {"OK"};			
		
		JOptionPane.showOptionDialog(
				f, s, "Error", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[0]);
	}
	
	/** 
	 * Loads the lateral panel
	 * 
	 * @param s - Project name
	 * @param al - array containing input files names, it is null if the project doesn't yet have any.
	 */
	public void loadPanelLateral(String s, ArrayList <String> al){

		frameProject.remove(panelLateralProject.getPanelTree());
		frameProject.remove(panelCentralProject.getPanelAnalysis());   
		
		panelLateralProject = new ViewPanelLateral(menuTreeProject);
		
		panelCentralProject = new ViewPanelCentral();
		
		if(al == null){
			panelLateralProject.createTree(s);
	    	panelLateralProject.getTree().addMouseListener(controllerProject);
		}
		else{
			panelLateralProject.loadTree(s, al);
	    	panelLateralProject.getTree().addMouseListener(controllerProject);
	    	panelLateralProject.setAnalysisLeafTree(
	    			modelProject.loadAnalysisFileProject());
		}

//    	buttonProjectSP.setEnabled(true);
//    	buttonProjectLF.setEnabled(true);

		//activating menu items
		menuProjectDelete.setEnabled(true);
    	menuProjectSave.setEnabled(true);
    	
    	menuFilesLoad.setEnabled(true);
    	
    	frameProject.add(panelLateralProject.getPanelTree());	    	
    	frameProject.repaint();   

	}
	
	/** 
	 * Loads the central panel according to user selection on lateral project tree.
	 * 
	 */
	public void loadPanelCentral()
	{
		int i = -1;
		
		if((i = panelLateralProject.getAnalysisLeaf()) != -1){
			if(i >= 0){//an input file node has been selected
				panelCentralProject.createTabFile(
						modelProject.readAnalysisFile(i), modelProject.readTermRelevantFile(i));
			}
			else if(i==-2){//the commonality node has been selected
				lastButtonSelectionEnd=buttonCommonalitiesSelectionEnd;
				panelCentralProject.createTabFeatures( modelProject.readPathHTMLTermRelevantFile(), 
				  modelProject.readCommonalitiesCandidates(), modelProject.readCommonalitiesSelected(),
				  modelProject.readPathCommonalitiesSelectedHTML(), modelProject.getRelevantTerms(), 
				  ViewPanelCentral.FeatureType.COMMONALITIES, null, lastButtonSelectionEnd);
			}
			else if(i==-3){//the variability node has been selected
				lastButtonSelectionEnd=buttonVariabilitiesSelectionEnd;
				panelCentralProject.createTabFeatures( modelProject.readPathHTMLTermRelevantFile(), 
				  modelProject.readVariabilitiesCandidates(), modelProject.readVariabilitiesSelected(),
				  modelProject.readPathCommonalitiesSelectedHTML(), modelProject.getRelevantTerms(), 
				  ViewPanelCentral.FeatureType.VARIABILITIES, modelProject.readCommonalitiesCandidates(), lastButtonSelectionEnd);
			}

			frameProject.remove(panelCentralProject.getPanelAnalysis());
			frameProject.add(panelCentralProject.getPanelAnalysis());
			frameProject.repaint();
		}
	}

	/** Mostra le features selezionate
	 * 
	 */
	public void showFeaturesSelected(ViewPanelCentral.FeatureType type)
	{
		modelProject.setFeaturesSelected(panelCentralProject.getSelectedFeatures(), type);
	}
	
	/** Chiude il progetto
	 * 
	 */
	public void closeProject() 
	{
		frameProject.dispose();
		System.exit(0);
	}

	/** Assegna il filtro ai file di Input*/
	class FilterFileInput implements FilenameFilter 
	{
		@Override
		public boolean accept(File dir, String name) 
		{
			return name.endsWith( ".pdf" ) || name.endsWith(".txt");
	    }
	}
	
	/** Assegna il filtro ai file del progetto*/
	class FilterFileProject implements FilenameFilter 
	{
		@Override
		public boolean accept(File dir, String name) 
		{
			return name.endsWith( ".xml" );
	    }
	}
}