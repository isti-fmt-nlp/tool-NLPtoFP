/**
 * @author Manuel Musetti, Daniele Cicciarella
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

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
import javax.swing.KeyStroke;
import javax.swing.Timer;

import main.CMTConstants;
import main.CreateT2kFile;
import main.FileInputFilter;
import main.XMLFileFilter;

public class ViewProject implements Observer/*, Runnable*/{ 

	private static boolean verbose=false;//variable used to activate prints in the code
	
//	/** Class used to filter input files*/
//	class FileInputFilter implements FilenameFilter{
//		@Override
//		public boolean accept(File dir, String name){
//			return name.endsWith( ".pdf" ) || name.endsWith(".txt");
//	    }
//	}
	
//	/** Class used to filter project files*/
//	class XMLFileFilter extends FileFilter implements  FilenameFilter{
//		@Override
//		public boolean accept(File dir, String name){
////			System.out.println("loadFileDialog: "+dir.getAbsolutePath()+name);
//			return name.endsWith( ".xml" );
//		}
//
//		@Override
//		public boolean accept(File arg0) {
//			return (arg0.isDirectory() || arg0.getName().endsWith( ".xml" ));
//		}
//
//		@Override
//		public String getDescription() {
//			return null;
//		}
//	}
	
	private ModelProject modelProject = null;
	
	/** the root Frame*/
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
	private JMenuItem menuFilesLoad=null, menuFilesDelete=null, menuFilesLoadFolderFile=null, menuFilesLoadFolder=null;


	//Features Management Menu items
	private JMenuItem menuFeaturesExtractComm=null, menuFeaturesExtractVari=null/*, 
					  menuFeaturesSelectComm=null, menuFeaturesSelectVari=null*/;

	//Diagram Management Menu items	
	private JMenuItem menuDiagramOpen=null, menuDiagramCreate=null;

	private JPopupMenu menuTreeProject = null;
	
	private JMenuItem menuTree = null;
	
//	private Thread throbber=null;
	private Timer timer=null;
	private JFrame waitFrame = null;
	
//	private boolean stateThrobber = false;

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
//		frameProject.setSize(Toolkit.getDefaultToolkit().getScreenSize());
//		frameProject.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());

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
	
//	/** 
//	 * Thread: Manages the throbber.
//	 */
//	@Override
//	public void run(){
//		JLabel jl1 = new JLabel("Analysing input files...");
//		jl1.setBounds(new Rectangle(20,10,250,30));
//		
//		ImageIcon throbberIcon = new ImageIcon(getClass().getResource("/Throbber/throbber.gif"));
//
//		JLabel jl2  = new JLabel();
//		jl2.setBounds(new Rectangle(270,10,35,35));
//		jl2.setIcon(throbberIcon);		
//		
//		waitFrame = new JFrame("Loading...");
//		waitFrame.setLayout(null);
//		waitFrame.setBackground(Color.WHITE);
//		waitFrame.setBounds(375, 375, 350, 80);
//		waitFrame.add(jl1);
//		waitFrame.add(jl2);
//		waitFrame.setVisible(true);
//		
//		timer = new Timer(100, new ActionListener() {
//		    @Override
//		    public void actionPerformed(ActionEvent ae) {
//		        waitFrame.repaint();
//		    }
//		});
//		
//		/*if(!stateThrobber)*/ timer.start();
////		while(!stateThrobber) jf.repaint(); 
//		
////		waitFrame.setVisible(false);
////		waitFrame.dispose();
//	}
	
//	/** 
//	 * Sets the throbber's state.
//	 * 
//	 * @param b - boolean indicating throbber state to be set
//	 */
//	private void setStateThrobber(boolean b){
//		stateThrobber = b; 
//	}
	
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
		menuProjectCreate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		
		menuProjectDelete = new JMenuItem("Delete Project");
		menuProjectDelete.addActionListener(controllerProject);
		menuProjectDelete.setEnabled(false);
		
		menuProjectLoad = new JMenuItem("Load Project");
		menuProjectLoad.addActionListener(controllerProject);
		menuProjectLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		
		menuProjectSave = new JMenuItem("Save Project");
		menuProjectSave.addActionListener(controllerProject);
		menuProjectSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuProjectSave.setEnabled(false);
		
		menuProjectExit = new JMenuItem("Exit");
		menuProjectExit.addActionListener(controllerProject);	
		menuProjectExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		
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
		menuFilesLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK|ActionEvent.SHIFT_MASK));
		menuFilesLoad.setEnabled(false);
		
		menuFilesDelete = new JMenuItem("Delete File");
		menuFilesDelete.addActionListener(controllerProject);
		menuFilesDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK|ActionEvent.SHIFT_MASK));
		menuFilesDelete.setEnabled(false);
		
		menuFilesLoadFolder = new JMenuItem("Load Analysis Folder");
		menuFilesLoadFolder.addActionListener(controllerProject);
		menuFilesLoadFolder.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK|ActionEvent.SHIFT_MASK));
		menuFilesLoadFolder.setEnabled(false);
		
		menuFilesLoadFolderFile = new JMenuItem("Load2 File");
		menuFilesLoadFolderFile.addActionListener(controllerProject);
		menuFilesLoadFolderFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK|ActionEvent.SHIFT_MASK));
		menuFilesLoadFolderFile.setEnabled(false);
		
				
//		menuFiles.add(menuFilesLoad);		
		menuFiles.add(menuFilesDelete);
		menuFiles.add(menuFilesLoadFolder);
		menuFiles.add(menuFilesLoadFolderFile);
		
		menu.add(menuFiles);
		
		/*MenuFeatures*/
		menuFeatures=new JMenu("Features");
		menuFeatures.setMnemonic(KeyEvent.VK_E);

		menuFeaturesExtractComm = new JMenuItem("Extract Commonalities");
		menuFeaturesExtractComm.addActionListener(controllerProject);
		menuFeaturesExtractComm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK|ActionEvent.SHIFT_MASK));
		menuFeaturesExtractComm.setEnabled(false);
		
		menuFeaturesExtractVari = new JMenuItem("Extract Variabilities");
		menuFeaturesExtractVari.addActionListener(controllerProject);
		menuFeaturesExtractVari.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK|ActionEvent.SHIFT_MASK));
		menuFeaturesExtractVari.setEnabled(false);

		menuFeatures.addSeparator();
		
//		menuFeaturesSelectComm = new JMenuItem("Select Commonalities");
//		menuFeaturesSelectComm.addActionListener(controllerProject);
//		menuFeaturesSelectComm.setEnabled(false);
//		
//		menuFeaturesSelectVari = new JMenuItem("Select Variabilities");
//		menuFeaturesSelectVari.addActionListener(controllerProject);
//		menuFeaturesSelectVari.setEnabled(false);
		
		menuFeatures.add(menuFeaturesExtractComm);
		menuFeatures.add(menuFeaturesExtractVari);
//		menuFeatures.addSeparator();
//		menuFeatures.add(menuFeaturesSelectComm);		
//		menuFeatures.add(menuFeaturesSelectVari);
		
		menu.add(menuFeatures);

		/*MenuDiagram*/
		menuDiagram=new JMenu("Diagram");
		menuDiagram.setMnemonic(KeyEvent.VK_D);

		menuDiagramCreate = new JMenuItem("Create Diagram");
		menuDiagramCreate.addActionListener(controllerProject);
		menuDiagramCreate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		menuDiagramCreate.setEnabled(false);
		
		menuDiagramOpen = new JMenuItem("Open Diagram");
		menuDiagramOpen.addActionListener(controllerProject);
		menuDiagramOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
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
		
		frameProject.setLocation(0, 0);
		frameProject.setMinimumSize(new Dimension(500, 500));
		frameProject.setExtendedState(frameProject.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frameProject.setVisible(true);
		frameProject.setPreferredSize(frameProject.getSize());
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
//		  setStateThrobber(true);
		  stopThrobber();
			
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
		  //stopping throbber
		  frameProject.setEnabled(true);
		  System.out.println("End Extract Variabilities!!!");
//		  setStateThrobber(true);
		  stopThrobber();

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
		  menuFeaturesExtractVari.setEnabled(false);
		  menuDiagramCreate.setEnabled(false);
		  if(panelLateralProject.getAnalysisLeafTree().size() == 0){
			//updating menu items
			menuFilesDelete.setEnabled(false);
			menuFeaturesExtractComm.setEnabled(false);
		  }
		  else menuFeaturesExtractComm.setEnabled(true);
		}		
		else if(o.equals("New File Loaded")){
		  //activating menu items
		  menuFeaturesExtractComm.setEnabled(true);
		  menuFeaturesExtractVari.setEnabled(false);	
		  menuFilesDelete.setEnabled(true);		
		  menuDiagramCreate.setEnabled(false);
		}
		else if(o.equals("New Analisys Folder Loaded")){
		  //stopping throbber
		  frameProject.setEnabled(true);
		  stopThrobber();

		  //setting color Green to all input file nodes in the tree
		  panelLateralProject.setAnalysisLeafTree();	

		  //activating menu items
		  menuFeaturesExtractComm.setEnabled(true);
		  menuFeaturesExtractVari.setEnabled(false);
		  menuFilesDelete.setEnabled(true);
		  menuDiagramCreate.setEnabled(false);
			
		}
		else if(o.equals("Analisys folder can't be accepted")){
		  errorDialog("Analisys folder can't be accepted");
		}
		else if(o.equals("Project Loaded Without Commonalities")){
		  menuFeaturesExtractComm.setEnabled(true);
		  menuFeaturesExtractVari.setEnabled(false);			
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
	 * Asks user confirmation for deleting this project.
	 * 
	 * @return - 1 if the user confirmed, 0 otherwise
	 */
	public int deleteProjectDialog(){
		JFrame f = new JFrame("Delete Project");
		
    	Object[] options = {"No","Yes"};			
		
    	return JOptionPane.showOptionDialog(
			f, "Do you want delete the project?", "Delete Project",
			JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
//		int i = JOptionPane.showOptionDialog(
//				f, "Do you want delete the project?", "Delete Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
//		if(i == 1){
//			modelProject.deleteProject();
//
//			resetView();
//		}
//		
//		frameProject.repaint();
	}

	/**
	 * Reset view panels and menu items, making them as they are after program start.
	 */
	protected void resetView() {
		splitterPanelMain.removeAll();
		panelLateralProject = new ViewPanelLateral(menuTreeProject);
		panelCentralProject = new ViewPanelCentral();
		frameProject.repaint();
		
		menuProjectCreate.setEnabled(true);
		menuProjectDelete.setEnabled(false);
		menuProjectLoad.setEnabled(true);
		menuProjectSave.setEnabled(false);
		menuProjectExit.setEnabled(true);

		menuFilesLoad.setEnabled(false);
		menuFilesLoadFolder.setEnabled(false);
		menuFilesDelete.setEnabled(false);

		menuFeaturesExtractComm.setEnabled(false);
		menuFeaturesExtractVari.setEnabled(false);

		menuDiagramCreate.setEnabled(false);
		menuDiagramOpen.setEnabled(true);
		
//		menuFeaturesSelectComm.setEnabled(false);
//		menuFeaturesSelectVari.setEnabled(false);
		
		

	}
	
	/** 
	 * Loads a project file.
	 * 
	 * @return s - the selected project file path 
	 */
	public String loadProjectDialog(){

	    JFileChooser chooser = new JFileChooser();
	    
	    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    chooser.setFileFilter(new XMLFileFilter());
	    chooser.setCurrentDirectory(new File(CMTConstants.getSaveAnalisysDir()));
	    
	    int returnVal = chooser.showOpenDialog(new JFrame("Load Project"));
	    if(returnVal == JFileChooser.CANCEL_OPTION) return null;
/*
		FileDialog d = new FileDialog(new JFrame("Load Project"));
    	d.setMode(FileDialog.LOAD);
    	d.setFilenameFilter(new XMLFileFilter());
	    d.setDirectory(CMTConstants.getSaveAnalisysDir());
	    d.setVisible(true);
	    
	    if(d.getFile() == null) return null;

//	    return d.getFile().toString();
	    return d.getDirectory()+d.getFile().toString();
*/	    
	    return chooser.getSelectedFile().getAbsolutePath();
	}
	
	/** 
	 * Loads a project file.
	 * 
	 * @return s - the selected project file path 
	 */
	public String loadDiagramDialog(String pathProject){

	    JFileChooser chooser = new JFileChooser();
	    
	    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    chooser.setFileFilter(new XMLFileFilter());
	    chooser.setCurrentDirectory(new File(pathProject));
	    
	    int returnVal = chooser.showOpenDialog(new JFrame("Load Diagram"));
	    if(returnVal == JFileChooser.CANCEL_OPTION) return null;
	    
	    /*
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

	    return d.getDirectory()+d.getFile().toString();
*/
//	    System.out.println("Path: " +chooser.getSelectedFile().getAbsolutePath());
//	    System.out.println("name: " +chooser.getSelectedFile().getName());           

	    return chooser.getSelectedFile().getAbsolutePath();

	}
	
	/** 
	 * Asks user confirmation for saving this project.
	 * 
	 * @return - 1 if the user confirmed, 0 otherwise
	 */
	public int saveProjectDialog(){
		JFrame f = new JFrame("Save Project");
		
    	Object[] options = {"No","Yes"};			
		
		int i = JOptionPane.showOptionDialog(
				f, "Do you want save the project?", "Save Project", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
		if(i == 1) return 1;		
		else return 0;
	}
	
	/** 
 	 * Loads a file into the project. Used with DylanLab tools.
	 * 
	 * @return - a String[] containing the file name and the file path
	 */
	public String [] loadFileDialog(){
		String [] s = new String[2];
		
		FileDialog d = new FileDialog(new JFrame("Load File"));
    	d.setMode(FileDialog.LOAD);
    	d.setFilenameFilter(new FileInputFilter());
	    d.setDirectory(CMTConstants.getSaveAnalisysDir());
	    d.setVisible(true);
	    
	    if(d.getFile() == null) return null;

	    s[0] = d.getFile().toString();
    	s[1] = d.getDirectory() + d.getFile().toString();    	
    	
    	if((panelLateralProject.addNodeInput(s[0])) == false){
    		errorDialog("The file" + s[0] + " has already been inserted");
    		return null;
    	}

    	//activating menu items
    	menuFilesDelete.setEnabled(true);
    	menuFeaturesExtractComm.setEnabled(true);
    	
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
		File chosenFile = null;
		JFileChooser chooser = new JFileChooser();
	    

	    chooser.setDialogType(JFileChooser.OPEN_DIALOG);		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setCurrentDirectory(new File(CMTConstants.getTopSaveDirParent()));
	    int returnVal = chooser.showOpenDialog(new JFrame("Load Analysis Folder"));

//	    if(returnVal == JFileChooser.APPROVE_OPTION) {
//	    }

	    if(returnVal == JFileChooser.CANCEL_OPTION) return null;
	    
//	    System.out.println("Path: " +chooser.getSelectedFile().getAbsolutePath());
//	    System.out.println("name: " +chooser.getSelectedFile().getName());           

/*
	    if(panelLateralProject.addNodeInput(chooser.getSelectedFile().getName()) == false){
    	  errorDialog("The file "+chooser.getSelectedFile().getName()+" has already been loaded");
    	  return null;
    	}    	
    	frameProject.repaint();	    	    
	    
//	    analisysDir=new File(chooser.getSelectedFile().getAbsolutePath());
	    //checking that all needed files are present
	    String[] analisysFiles = checkValidFolder(chooser.getSelectedFile());

    	//activating throbber
    	if (analisysFiles != null) startThrobber();
*/

	    
//	    analisysDir=new File(chooser.getSelectedFile().getAbsolutePath());

	    //checking that all needed files are present
	    String[] analisysFiles = checkValidFolder(chooser.getSelectedFile());

    	if (analisysFiles == null) return null;

    	chosenFile = new File(analisysFiles[0]);
	    if(panelLateralProject.addNodeInput(chosenFile.getName()) == false){
	      errorDialog("The file "+chosenFile.getName()+" has already been loaded");
	      return null;
	    }    	
	    frameProject.repaint();	    	
	    
    	//activating throbber
	    startThrobber();
	    
	    return analisysFiles;
	}
	
	
	public String [] loadFile(){		
		File chosenFile = null;
		JFileChooser chooser = new JFileChooser();
	    

	    chooser.setDialogType(JFileChooser.OPEN_DIALOG);		
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    chooser.setCurrentDirectory(new File(CMTConstants.getTopSaveDirParent()));
	    int returnVal = chooser.showOpenDialog(new JFrame("Load File"));

//	    if(returnVal == JFileChooser.APPROVE_OPTION) {
//	    }

	    if(returnVal == JFileChooser.CANCEL_OPTION) return null;
	    
//	    System.out.println("Path: " +chooser.getSelectedFile().getAbsolutePath());
//	    System.out.println("name: " +chooser.getSelectedFile().getName());           

/*
	    if(panelLateralProject.addNodeInput(chooser.getSelectedFile().getName()) == false){
    	  errorDialog("The file "+chooser.getSelectedFile().getName()+" has already been loaded");
    	  return null;
    	}    	
    	frameProject.repaint();	    	    
	    
//	    analisysDir=new File(chooser.getSelectedFile().getAbsolutePath());
	    //checking that all needed files are present
	    String[] analisysFiles = checkValidFolder(chooser.getSelectedFile());

    	//activating throbber
    	if (analisysFiles != null) startThrobber();
*/
	    CreateT2kFile t2k = null;
	    File dir = null;
	    while(dir==null){
	    	t2k = new CreateT2kFile(chooser.getSelectedFile());
		    dir = t2k.getresultdir();
	    }
//	    analisysDir=new File(chooser.getSelectedFile().getAbsolutePath());

	    //checking that all needed files are present
	    String[] analisysFiles = checkValidFolder(dir);

    	if (analisysFiles == null) return null;

    	chosenFile = new File(analisysFiles[0]);
	    if(panelLateralProject.addNodeInput(chosenFile.getName()) == false){
	      errorDialog("The file "+chosenFile.getName()+" has already been loaded");
	      return null;
	    }    	
	    frameProject.repaint();	    	
	    
    	//activating throbber
	    startThrobber();
	    
	    return analisysFiles;
	}

	/**
	 * Checks if the selected input folder contains all needed analysis files.
	 * 
	 * @param analisysDir - directory that must contain the analysis files
	 * @return a String[] of size 4 containing the analysis files paths in this order:<br>
	 * index 0: the document '.txt' file <br>
	 * index 1: the 'term.tmp' file containing the term extraction<br>
	 * index 2: the '.pos' file containing the part-of-speech classification<br>
	 * index 3: the '.conll' file containing the candidate-terms token sequences<br>
	 */
	private String[] checkValidFolder(File analisysDir) {
		String [] analisysFiles = new String[4];
	    //booleans used to check if 1 and only one of such files exist inside selected folder
	    boolean txtFound=false;
	    boolean termTmpFound=false;
	    boolean posFound=false;
	    boolean conllFound=false;
	    //no more than 1 file per type(suffix) will be accepted
	    for(File file : analisysDir.listFiles()){
      	  if(file.getName().endsWith(".txt")){
      		if(txtFound){ System.out.println("Double .txt file!"); return null;}
      		else{
      		  txtFound=true;
      		  analisysFiles[0]=file.getAbsolutePath();
      		}
      	  }      	  
    	  if(file.getName().endsWith(".term.tmp") || file.getName().endsWith(".ter")){
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
    	  errorDialog("one kind of analysis file is missing!"); return null;
    	}
    	
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

	  if(i == 1) return panelLateralProject.deleteSelectedInputNode();
	  else return -1;
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

		/* ***VERBOSE*** */
		if(verbose)System.out.println("Scelta fatta: "+i);
		/* ***VERBOSE*** */

		if(i==-1) return -1;
		deleted=panelLateralProject.deleteSpecifiedInputNode(i);
		if(deleted) return i;
		else return -1;
	}
	
	/**.
	 * Asks user confirmation to extract the commonalities from input files.
	 * 
	 * @return - 1 if the user confirmed, 0 otherwise
	 */
	public int extractCommonalitiesDialog(){
		JFrame f = new JFrame("Extract Commonalities");
    	Object[] options = {"No","Yes"};			
		
		return JOptionPane.showOptionDialog(f, "Do you want extract commonalities from the file?",
			"Extract Commonalities", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
	}
	
	/** 
	 * Asks user confirmation to extract the variabilities from input files.
	 * 
	 * @return - 1 if the user confirmed, 0 otherwise
	 */
	public int extractVariabilitiesDialog(){
		JFrame f = new JFrame("Extract Variabilities");
		
    	Object[] options = {"No","Yes"};			
		
		return JOptionPane.showOptionDialog(f, "Do you want extract variabilities from the file?",
			"Extract Variabilities", JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);		
	}
	
	/**
	 * Starts the throbber and blocks the user interface.
	 */
	protected void startThrobber() {
		frameProject.setEnabled(false);
		
		JLabel jl1 = new JLabel("Analysing input files...");
		jl1.setBounds(new Rectangle(10,10,250,30));
		
		ImageIcon throbberIcon = new ImageIcon(getClass().getResource("/Throbber/throbber.gif"));
//		ImageIcon throbberIcon = new ImageIcon(getClass().getResource("/Throbber/throbber2.gif"));		

		JLabel jl2  = new JLabel();
		jl2.setBounds(new Rectangle(270,0,50,50));
		jl2.setIcon(throbberIcon);		
		
		waitFrame = new JFrame("Loading...");
		waitFrame.setLayout(null);
		waitFrame.setBackground(Color.WHITE);
//		waitFrame.setBounds(375, 275, 800, 500);
		waitFrame.setBounds(375, 375, 320, 100);
		waitFrame.add(jl1);
		waitFrame.add(jl2);
		waitFrame.setVisible(true);
		
		timer = new Timer(50, new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent ae) {
		        waitFrame.repaint();
		    }
		});
		
		/*if(!stateThrobber)*/ timer.start();
//		while(!stateThrobber) jf.repaint(); 
		
//		waitFrame.setVisible(false);
//		waitFrame.dispose();
//		setStateThrobber(false);
//		throbber = new Thread(this);
//		throbber.start();
		frameProject.repaint();
	}

	/**
	 * Stops the throbber.
	 */
	protected void stopThrobber() {
		timer.stop();
		waitFrame.setVisible(false);
		waitFrame.dispose();
//		setStateThrobber(true);
		frameProject.repaint();
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
	 * Loads the lateral panel.
	 * 
	 * @param projectName - Project name
	 * @param al - array containing input files names, it is null if the project doesn't yet have any
	 * @param controllerProject - controller to be set on the lateral panel
	 * @param newProject - tells if this is a new project or it has been loaded from a save file
	 */
	public void loadPanelLateral(String projectName, ArrayList <String> al, ControllerProject controllerProject,
								 boolean newProject){
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

    	menuFilesLoadFolderFile.setEnabled(true);
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
				panelCentralProject.createTabFile(modelProject.readAnalysisFile(i), modelProject.readTermRelevantFile(i));
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
	
	/**
	 * Sets the root frame to maximum size and brings it to front.
	 */
	public void maximize(){
      frameProject.setExtendedState(JFrame.MAXIMIZED_BOTH);
      frameProject.setAlwaysOnTop(true);
      frameProject.requestFocus();
      frameProject.setAlwaysOnTop(false);		
	}
	
	/**
	 * Sets the root frame to iconified state and sends it to tray.
	 */
	public void minimize(){
		frameProject.setState(JFrame.ICONIFIED);
	}
	
	/**
	 * Brings the root frame to front.
	 */
	public void bringToFront(){
	  frameProject.setState(JFrame.ICONIFIED);		
	  try {
		Thread.sleep(350);
	  } catch (InterruptedException e) {
		e.printStackTrace();
	  }
	  
	  frameProject.setExtendedState(JFrame.MAXIMIZED_BOTH);
	  frameProject.setAlwaysOnTop(true);
	  frameProject.requestFocus();
	  frameProject.setAlwaysOnTop(false);		
	}
}
