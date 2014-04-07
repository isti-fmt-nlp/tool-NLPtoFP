/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

public class ViewProject implements Observer, Runnable{

	private static boolean verbose=false;//variable used to activate prints in the code
	
	private static final String savedProjectsDir = "Usage Tries/ANALISYS";
	
	private ModelProject modelProject = null;
	
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
	private JMenuItem menuFilesLoad=null, menuFilesDelete=null, menuFilesLoadFolder=null;


	//Features Management Menu items
	private JMenuItem menuFeaturesExtractComm=null, menuFeaturesExtractVari=null, 
					  menuFeaturesSelectComm=null, menuFeaturesSelectVari=null;

	//Diagram Management Menu items	
	private JMenuItem menuDiagramOpen=null, menuDiagramCreate=null;

	private JPopupMenu menuTreeProject = null;
	
	private JMenuItem menuTree = null;
	
	private Thread throbber=null;
	
	private boolean stateThrobber = false;

	private JButton lastButtonSelectionEnd = null;//the last selection button loaded into the central panel 
	
	private JButton buttonCommonalitiesSelectionEnd = null;//the commonalities selection button 
	
	private JButton buttonVariabilitiesSelectionEnd = null;//the variabilities selection button 
	
	private ViewPanelLateral panelLateralProject = null;
	
	private ViewPanelCentral panelCentralProject = null;
	
	private JSplitPane splitterPanelMain = null;
	
//	private JSplitPane splitterPanelInner = null;
	
	/** 
	 * Constructor of the project's view.
	 * 
	 * @param modelProject - the model that will be represented by this view.
	 */
	public ViewProject(ModelProject modelProject){
		this.modelProject = modelProject;
		
		/* Initializing JFrame */
		frameProject.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameProject.setLayout(new BorderLayout());				
		frameProject.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		frameProject.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());

		frameProject.setJMenuBar(menu);
		
		splitterPanelMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitterPanelMain.setContinuousLayout(true);
//		splitterPanel.setOneTouchExpandable(true);
//		splitterPanel.setDividerLocation(0.5);
		splitterPanelMain.setDividerSize(6);
		splitterPanelMain.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width,
				Toolkit.getDefaultToolkit().getScreenSize().height));
		
//		splitterPanelInner = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//		splitterPanelInner.setContinuousLayout(true);
//		splitterPanelInner.setDividerSize(6);
//		splitterPanelInner.setPreferredSize(
//		  new Dimension(4*Toolkit.getDefaultToolkit().getScreenSize().width/5, Toolkit.getDefaultToolkit().getScreenSize().height));
		
		frameProject.add(splitterPanelMain, BorderLayout.CENTER);

	}
	
	/** 
	 * Thread: Manages the throbber.
	 */
	@Override
	public void run(){
		JLabel jl1 = new JLabel("Analysing input files...");
		jl1.setBounds(new Rectangle(20,10,250,30));
		
		System.out.println("getClass().getResource(/Throbber/\"throbber.gif\"): "+getClass().getResource("/Throbber/throbber.gif"));
		System.out.println("getClass(): "+getClass());
		ImageIcon i = new ImageIcon(getClass().getResource("/Throbber/throbber.gif"));

		JLabel jl2  = new JLabel();
		jl2.setBounds(new Rectangle(270,10,35,35));
		jl2.setIcon(i);		
		
		JFrame jf = new JFrame("Loading...");
		jf.setLayout(null);
		jf.setBackground(Color.WHITE);
		jf.setBounds(375, 375, 350, 80);
		jf.add(jl1);
		jf.add(jl2);
		jf.setVisible(true);
		
		while(!stateThrobber){ jf.repaint(); /*System.out.println("throbbing...");*/}
		
		jf.setVisible(false);
		jf.dispose();
	}
	
	/** 
	 * Sets the throbber's state.
	 * 
	 * @param b - boolean indicating throbber state to be set
	 */
	private void setStateThrobber(boolean b){
		stateThrobber = b; 
	}
	
	/** 
	 * Creates ed initializes the GUI structures, adding the controller to each component that needs it.
	 * 
	 * @param controllerProject - the controller
	 */
	public void addListener(ControllerProject controllerProject){
		//creating MenuBar
		
		/*MenuProject*/
		menuProject = new JMenu("Project");
		menuProject.setMnemonic(KeyEvent.VK_P);

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
		menuFiles.setMnemonic(KeyEvent.VK_F);

		menuFilesLoad = new JMenuItem("Load File");
		menuFilesLoad.addActionListener(controllerProject);
		menuFilesLoad.setEnabled(false);
		
		menuFilesDelete = new JMenuItem("Delete File");
		menuFilesDelete.addActionListener(controllerProject);
		menuFilesDelete.setEnabled(false);
		
		menuFilesLoadFolder = new JMenuItem("Load Analisys Folder");
		menuFilesLoadFolder.addActionListener(controllerProject);
		menuFilesLoadFolder.setEnabled(false);
				
//		menuFiles.add(menuFilesLoad);		
		menuFiles.add(menuFilesDelete);
		menuFiles.add(menuFilesLoadFolder);
		
		menu.add(menuFiles);
		
		/*MenuFeatures*/
		menuFeatures=new JMenu("Features");
		menuFeatures.setMnemonic(KeyEvent.VK_E);

		menuFeaturesExtractComm = new JMenuItem("Extract Commonalities");
		menuFeaturesExtractComm.addActionListener(controllerProject);
		menuFeaturesExtractComm.setEnabled(false);
		
		menuFeaturesExtractVari = new JMenuItem("Extract Variabilities");
		menuFeaturesExtractVari.addActionListener(controllerProject);
		menuFeaturesExtractVari.setEnabled(false);

		menuFeatures.addSeparator();
		
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
		menuDiagram.setMnemonic(KeyEvent.VK_D);

		menuDiagramCreate = new JMenuItem("Create Diagram");
		menuDiagramCreate.addActionListener(controllerProject);
		menuDiagramCreate.setEnabled(false);
		
		menuDiagramOpen = new JMenuItem("Open Diagram");
		menuDiagramOpen.addActionListener(controllerProject);
		menuDiagramOpen.setEnabled(true);
		
		menuDiagram.add(menuDiagramCreate);		
		menuDiagram.add(menuDiagramOpen);
		
		menu.add(menuDiagram);

		
		menuTree = new JMenuItem("Delete Selected File");
		menuTree.addActionListener(controllerProject);
		
		menuTreeProject = new JPopupMenu("Men� Tree");
		menuTreeProject.add(menuTree);
		
		//creating lateral panel
		panelLateralProject = new ViewPanelLateral(menuTreeProject);

		//creating buttons to select feature candidates
		buttonCommonalitiesSelectionEnd = new JButton("Select Commonalities");
//		buttonCommonalitiesSelectionEnd.setBounds(330, 640, 180, 30);//+50?
//		buttonCommonalitiesSelectionEnd.setLocation(330, 640);//+50?
		buttonCommonalitiesSelectionEnd.setPreferredSize(new Dimension(180, 20));//+50?
		buttonCommonalitiesSelectionEnd.addActionListener(controllerProject);
		
		buttonVariabilitiesSelectionEnd= new JButton("Select Variabilities");
//		buttonVariabilitiesSelectionEnd.setBounds(330, 640, 180, 30);//+50?
//		buttonVariabilitiesSelectionEnd.setLocation(330, 640);//+50?
		buttonVariabilitiesSelectionEnd.setPreferredSize(new Dimension(180, 20));//+50?
			buttonVariabilitiesSelectionEnd.addActionListener(controllerProject);
		
		//creating central panel
		panelCentralProject = new ViewPanelCentral();
		
		frameProject.addWindowListener(controllerProject);
		frameProject.setVisible(true);
		frameProject.setLocation(0, 0);
		frameProject.setExtendedState(frameProject.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frameProject.validate();
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
		  //stopping throbber
		  frameProject.setEnabled(true);
		  setStateThrobber(true);
			
		  //setting color Green to all input file nodes in the tree
		  panelLateralProject.setAnalysisLeafTree();	
			
		  //activating menu items
		  menuFeaturesExtractComm.setEnabled(false);
		  menuFeaturesExtractVari.setEnabled(true);
	    	
			
//	 		File f = new File("./src/DATA/Sound/analysis.wav");
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

		  //activating menu items
		  menuFeaturesExtractVari.setEnabled(false);
		  menuDiagramCreate.setEnabled(true);
	    	
		  /* ***VERBOSE****/
		  if (verbose){
			System.out.println("\n***\nStampo le Variabilities Candidates: ");
			for (String tmp: modelProject.readVariabilitiesCandidates()) System.out.println("-"+tmp);
			System.out.println("FrameProject is enabled? "+frameProject.isEnabled());
		  }
		  /* ***VERBOSE****/	    
		}
		else if(o.equals("End Commonalities Selected")){
		  panelCentralProject.refreshTabFeaturesSelected(
			modelProject.readPathCommonalitiesSelectedHTML(), ViewPanelCentral.FeatureType.COMMONALITIES);
		  splitterPanelMain.setRightComponent(panelCentralProject.getPanelAnalysis());
		}
		else if(o.equals("End Variabilities Selected")){
		  panelCentralProject.refreshTabFeaturesSelected(
			modelProject.readPathVariabilitiesSelectedHTML(), ViewPanelCentral.FeatureType.VARIABILITIES);
		  splitterPanelMain.setRightComponent(panelCentralProject.getPanelAnalysis());
		}
		else if(o.equals("Input File Deleted")){
		  menuFeaturesExtractComm.setEnabled(true);
		  menuFeaturesExtractVari.setEnabled(false);
		  menuDiagramCreate.setEnabled(false);
		}		
		else if(o.equals("New File Loaded")){
		  menuFeaturesExtractComm.setEnabled(true);
		  menuFeaturesExtractVari.setEnabled(false);			
		  menuDiagramCreate.setEnabled(false);
		}
		else if(o.equals("New Analisys Folder Loaded")){
		  //stopping throbber
		  frameProject.setEnabled(true);
		  setStateThrobber(true);

		  //setting color Green to all input file nodes in the tree
		  panelLateralProject.setAnalysisLeafTree();	

		  //activating menu items
		  menuFeaturesExtractComm.setEnabled(true);
		  menuFeaturesExtractVari.setEnabled(false);
		  menuDiagramCreate.setEnabled(false);
			
		}
		else if(o.equals("Analisys folder can't be accepted")){
		  errorDialog("Analisys folder can't be accepted");
		}
		else if(o.equals("Project Loaded With Commonalities")){
	      menuFeaturesExtractComm.setEnabled(false);
	      menuFeaturesExtractVari.setEnabled(true);			
		}		
		else if(o.equals("Project Loaded With Commonalities And Variabilities")){
		  menuFeaturesExtractComm.setEnabled(false);
		  menuFeaturesExtractVari.setEnabled(false);
		  menuDiagramCreate.setEnabled(true);
		}
		
		frameProject.repaint(); 	
	}
	
	/** 
	 * Asks the user for a project name.
	 * 
	 * @return - a String containing the name of the project
	 */
	public String assignNameProjectDialog(){
			String s = null;
			
		 	JTextField jtf = new JTextField();
		 	
		    Object[] o1 = {"Name project: ", jtf};
		    Object[] o2 = { "Cancel", "OK" };
		    
		    int i = JOptionPane.showOptionDialog(new JFrame("Create Project"),
		            o1, "",
		            JOptionPane.YES_NO_OPTION, JOptionPane.DEFAULT_OPTION, null,
		            o2, o2[1]);
		    
		    if(i == JOptionPane.NO_OPTION){
		      if((s = jtf.getText()) != null){
		    	if(!s.trim().equals("")){
		    	  return s;
		    	}
		    	else{
		    	  errorDialog("You did not put a name to the project");
		    	  return null;
		    	}
		      }
		      else{
		    	errorDialog("You did not put a name to the project");
		    	return null;
		      }
		    }		    		      
		    else return null;
	}
	
	/** 
	 * Deletes the project.
	 */
	public void deleteProjectDialog(){
		JFrame f = new JFrame("Delete Project");
		
    	Object[] options = {"No","Yes"};			
		
		int i = JOptionPane.showOptionDialog(
				f, "Do you want delete the project?", "Delete Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
		if(i == 1){
			modelProject.deleteProject();
//			splitterPanelInner.removeAll();
			splitterPanelMain.removeAll();
//			frameProject.remove(panelLateralProject.getPanelTree());
//			frameProject.remove(panelCentralProject.getPanelAnalysis());
			panelLateralProject = new ViewPanelLateral(menuTreeProject);
			panelCentralProject = new ViewPanelCentral();
			frameProject.repaint();
			
			menuDiagramCreate.setEnabled(false);
			menuDiagramOpen.setEnabled(false);
			
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
	    d.setDirectory("../"+savedProjectsDir);
	    d.setVisible(true);
	    
	    if(d.getFile() == null) return null;
//	    System.out.println("File: "+d.getFile());
//	    System.out.println("Dir: "+d.getDirectory());
	    
	//	    if(!buttonProjectEC.isEnabled())
	//    		buttonProjectEC.setEnabled(true);

	    return d.getFile().toString();
	}
	
	/** 
	 * Loads a project file.
	 * 
	 * @return s - the selected project file path 
	 */
	public String loadDiagramDialog(String pathProject){
		FileDialog d = new FileDialog(new JFrame("Load File"));
	    d.setResizable(true);
    	d.setMode(FileDialog.LOAD);
    	
    	//checking if the diagrams save directory must be created
    	File dir=new File(pathProject);		
    	if(!dir.isDirectory() && !dir.mkdirs()){
    		errorDialog("Save Directory can't be created.");
    		return null;
    	}

	    d.setDirectory(pathProject);
	    d.setVisible(true);
	    
	    if(d.getFile() == null) return null;

	    System.out.println("DIR IS: "+d.getDirectory()+"\nFILE IS: "+d.getFile());
	    return d.getDirectory()+d.getFile().toString();
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
	
	/** 
 	 * Loads a file into the project.
	 * 
	 * @return - a String[] containing the file name and the file path
	 */
	public String [] loadFileDialog(){
		String [] s = new String[2];
		
		FileDialog d = new FileDialog(new JFrame("Load File"));
    	d.setMode(FileDialog.LOAD);
    	d.setFilenameFilter(new FilterFileInput());
	    d.setDirectory("../"+savedProjectsDir);
	    d.setVisible(true);
	    
	    if(d.getFile() == null) return null;

	    s[0] = d.getFile().toString();
    	s[1] = d.getDirectory() + d.getFile().toString();    	
    	
    	//activating menu items
    	menuFilesDelete.setEnabled(true);
    	menuFeaturesExtractComm.setEnabled(true);
    	
    	
    	if((panelLateralProject.addNodeInput(s[0])) == false){
    		errorDialog("The file" + s[0] + " has already been inserted");
    		return null;
    	}
    	
    	frameProject.repaint();
      
	    return s;
	}
	
	/** 
 	 * Loads analisys files from a folder into the project.
	 * 
	 * @return - a String[] containing the paths to the analisys files, <br>
	 * or null if the content is not correct.
	 */
	public String [] loadFolderDialog(){		
		String [] analisysFiles = new String[4];
	    JFileChooser chooser = new JFileChooser();
	    File analisysDir=null;
	    //used to check if 1 and only one of such files exist inside selected folder
	    boolean txtFound=false;
	    boolean termTmpFound=false;
	    boolean posFound=false;
	    boolean conllFound=false;
	    
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    int returnVal = chooser.showOpenDialog(new JFrame("Select Analisys Folder"));

//	    if(returnVal == JFileChooser.APPROVE_OPTION) {
//	    }

	    if(returnVal == JFileChooser.CANCEL_OPTION) return null;
	    
	    System.out.println("Path: " +chooser.getSelectedFile().getAbsolutePath());
	    System.out.println("name: " +chooser.getSelectedFile().getName());           

    	if(panelLateralProject.addNodeInput(chooser.getSelectedFile().getName()) == false){
    	  errorDialog("The file "+chooser.getSelectedFile().getName()+" has already been loaded");
    	  return null;
    	}    	
    	frameProject.repaint();	    	    
	    
	    //no more than 1 file per type(suffix) will be accepted
	    analisysDir=new File(chooser.getSelectedFile().getAbsolutePath());
    	for(File file : analisysDir.listFiles()){
      	  if(file.getName().endsWith(".txt")){
      		if(txtFound){ System.out.println("Double .txt file!"); return null;}
      		else{
      		  txtFound=true;
      		  analisysFiles[0]=file.getAbsolutePath();
      		}
      	  }      	  
    	  if(file.getName().endsWith(".term.tmp")){
    		if(termTmpFound){ System.out.println("Double .term.tmp file!"); return null;}
      		else{
      		  termTmpFound=true;
      		  analisysFiles[1]=file.getAbsolutePath();
      		}
    	  }
    	  if(file.getName().endsWith(".pos")){
    		if(posFound){ System.out.println("Double .pos file!"); return null;}
      		else{
      		  posFound=true;
      		  analisysFiles[2]=file.getAbsolutePath();
      		}
    	  }
    	  if(file.getName().endsWith(".conll")){
    		if(conllFound){ System.out.println("Double .conll file!"); return null;}
      		else{
      			conllFound=true;
      		  analisysFiles[3]=file.getAbsolutePath();
      		}
    	  }
    	}

    	//1 file per type(suffix) is needed
    	if(!txtFound || !termTmpFound || !posFound || !conllFound){
    	  System.out.println("one kind of file is missing!"); return null;
    	}

    	//activating throbber
	    frameProject.setEnabled(false);
	    setStateThrobber(false);
	    throbber = new Thread(this);
	    throbber.start();
	    frameProject.repaint();

	    return analisysFiles;
	}

	/** 
	 * Delete from the project the current selected input file in the project tree
	 *  
	 * @return i - the index of the deleted file
	 */
	public int deleteSelectedFileDialog(){
	  JFrame f = new JFrame("Delete File");
		
	  Object[] options = {"No","Yes"};			
		
	  int i = JOptionPane.showOptionDialog(f, "Do you want delete the file?", "Delete File",
			  			JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);

	  if(i == 1){
		if((i = panelLateralProject.deleteSelectedInputNode()) != -1){
		  if(panelLateralProject.getAnalysisLeafTree().size() == 0){

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

		if(i==-1) return -1;
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
	
	/** 
	 * Extracts the commonalities from input files.
	 */
	public void extractCommonalitiesDialog(){
		JFrame f = new JFrame("Extract Commonalities");
		
    	Object[] options = {"No","Yes"};			
		
		int i = JOptionPane.showOptionDialog(f, "Do you want extract commonalities from the file?",
			"Extract Commonalities", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
		if(i == 1){
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
	 */
	public void extractVariabilitiesDialog(){
		JFrame f = new JFrame("Extract Variabilities");
		
    	Object[] options = {"No","Yes"};			
		
		int i = JOptionPane.showOptionDialog(f, "Do you want extract variabilities from the file?",
			"Extract Variabilities", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
		if(i == 1){
			frameProject.setEnabled(false);
			modelProject.extractVariabilities();
			setStateThrobber(false);
			throbber = new Thread(this);
			throbber.start();
			frameProject.repaint();
		}
	}
	
	/** 
	 * Shows a frame with text, to display the user error
	 * 
	 * @param s - String representing the user error
	 */
	public void errorDialog(String s){
		JFrame f = new JFrame("Error");		
    	Object[] options = {"OK"};			
		
		JOptionPane.showOptionDialog(
				f, s, "Error", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[0]);
	}
	
	/** 
	 * Loads the lateral panel
	 * 
	 * @param projectName - Project name
	 * @param al - array containing input files names, it is null if the project doesn't yet have any
	 * @param newProject - tells if this is a new project or it has been loaded from a save file
	 */
	public void loadPanelLateral(String projectName, ArrayList <String> al, ControllerProject controllerProject, boolean newProject){
		panelLateralProject = new ViewPanelLateral(menuTreeProject);				
		panelCentralProject = new ViewPanelCentral();
		
		if(al == null){
			panelLateralProject.createTree(projectName);
	    	panelLateralProject.getTree().addMouseListener(controllerProject);
		}
		else{
			panelLateralProject.loadTree(projectName, al);
	    	panelLateralProject.getTree().addMouseListener(controllerProject);
	    	panelLateralProject.setAnalysisLeafTree(
	    			modelProject.loadAnalysisFileProject());
		}

		//activating menu items
		menuProjectDelete.setEnabled(true);
    	menuProjectSave.setEnabled(true);

    	menuFilesLoad.setEnabled(true);
    	menuFilesLoadFolder.setEnabled(true);
    	
    	if (panelLateralProject.getAnalysisLeafTree().size()>0){
    	  menuFilesDelete.setEnabled(true);
    	  //if the project was loaded from a save file the load method already set menuFeatures right
    	  if (newProject) menuFeaturesExtractComm.setEnabled(true);
    	}
    	
		splitterPanelMain.setLeftComponent(panelLateralProject.getPanelTree());

		frameProject.repaint();   
	}
	
	/** 
	 * Loads the central panel according to user selection on lateral project tree.
	 */
	public void loadPanelCentral(){
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
				  modelProject.getRelevantTermsVersions(), ViewPanelCentral.FeatureType.COMMONALITIES, 
				  null, lastButtonSelectionEnd);
			}
			else if(i==-3){//the variability node has been selected
				lastButtonSelectionEnd=buttonVariabilitiesSelectionEnd;
				panelCentralProject.createTabFeatures( modelProject.readPathHTMLTermRelevantFile(), 
				  modelProject.readVariabilitiesCandidates(), modelProject.readVariabilitiesSelected(), 
				  modelProject.readPathVariabilitiesSelectedHTML(), modelProject.getRelevantTerms(), 
				  modelProject.getRelevantTermsVersions(), ViewPanelCentral.FeatureType.VARIABILITIES, 
				  modelProject.readCommonalitiesCandidates(), lastButtonSelectionEnd);
			}

//			splitterPanelInner.setLeftComponent(panelCentralProject.getPanelAnalysis());
			splitterPanelMain.setRightComponent(panelCentralProject.getPanelAnalysis());
//			frameProject.remove(panelCentralProject.getPanelAnalysis());
//			frameProject.add(panelCentralProject.getPanelAnalysis());
			frameProject.repaint();
		}
	}

	/** 
	 * Shows the selected features.
	 */
	public void showFeaturesSelected(ViewPanelCentral.FeatureType type){
		modelProject.setFeaturesSelected(panelCentralProject.getSelectedFeatures(), type);
	}
	
	/** 
	 * Closes the project.
	 */
	public void closeProject(){
		frameProject.dispose();
		System.exit(0);
	}

	/** Class used to filter input files*/
	class FilterFileInput implements FilenameFilter{
		@Override
		public boolean accept(File dir, String name){
			return name.endsWith( ".pdf" ) || name.endsWith(".txt");
	    }
	}

	/** Class used to filter input folders containing analisys files*/
	class FilterFolderInput implements FilenameFilter{
		@Override
		public boolean accept(File dir, String name){
			return true;
	    }
	}
	
	/** Class used to filter project files*/
	class FilterFileProject implements FilenameFilter{
		@Override
		public boolean accept(File dir, String name){
//			System.out.println("loadFileDialog: "+dir.getAbsolutePath()+name);
			return name.endsWith( ".xml" );
		}
	}
}
