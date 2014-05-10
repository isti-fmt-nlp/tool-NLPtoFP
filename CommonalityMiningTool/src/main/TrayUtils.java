package main;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import view.ControllerProject;
import view.EditorController;
import view.EditorView;
import view.ViewProject;

public class TrayUtils {

	/** values used to specify one of the tools*/
	public static enum ToolNames{CMT, FDE};
	
	/** List of EditorController objects, one per each instance of FDE tool*/
	private static ArrayList<JMenu> fdeList = new ArrayList<JMenu>();

	/** List of EditorController objects, one per each instance of CMT tool*/
	private static ArrayList<JMenu> cmtList = new ArrayList<JMenu>();
	
	private static SystemTray tray = SystemTray.getSystemTray();
	
	/** TrayIcon for FDE tool instances*/
    private static TrayIcon fdeTrayIcon = null;
	/** The popup menu for FDE tray icon*/
    private static JPopupMenu fdePopup = null;

	/** TrayIcon for CMT tool instances*/
    private static TrayIcon cmtTrayIcon = null;
	/** The popup menu for CMT tray icon*/
    private static JPopupMenu cmtPopup = null;

	/** URL of the Feature Diagram Editor Tool tray icon*/
	private static final URL trayIconURL_FDE = EditorView.class.getResource("/Tray/Tray Icon FDET_2.png");    

	/** URL of the Commonality Mining Tool tray icon*/
	private static final URL trayIconURL_CMT = ViewProject.class.getResource("/Tray/Tray Icon CMT_2.png");   
	

	/** Adds the tray icon for the tool that requested it, if the OS supports it.
	 * 
	 * @param imageURL - URL of the icon
	 * @param trayName - type of tool requesting the icon
	 * @param exitListener - listener to be set on the 'Exit' item in the tray icon's popup menu
	 */
    public static void createAndShowCMTTray(ControllerProject exitListener){    	
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        if(cmtList.size()>0) addInstanceToCMTTray(exitListener, ToolNames.FDE);
        else{
          setupTrayIconCMT(trayIconURL_CMT, ToolNames.CMT, exitListener, "Commonality Mining Tool");
        }
        
    }

	/** Adds the tray icon for the tool that requested it, if the OS supports it.
	 * 
	 * @param imageURL - URL of the icon
	 * @param trayName - type of tool requesting the icon
	 * @param exitListener - listener to be set on the 'Exit' item in the tray icon's popup menu
	 */
    public static void createAndShowFDETray(EditorController exitListener) {    	
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        if(fdeList.size()>0) addInstanceToFDETray(exitListener);
        else{
          setupTrayIconFDE(trayIconURL_FDE, ToolNames.FDE, exitListener, "Feature Diagram Editor Tool");
        }
        
    }

	/**
     * Builds and setup a TrayIcon for FDE tool.
     * 
     * @param imageURL - URL of the icon image
     * @param trayName - identify the specified tool
     * @param exitListener - a EditorController object, listener of the tool instance
     */
	private static void setupTrayIconFDE(URL imageURL, final ToolNames trayName,
										 EditorController exitListener, String trayDescr){
		String instanceName=exitListener.getDiagramName();
		
		setupTrayIcon(imageURL, trayName, trayDescr, exitListener, instanceName);
	}

	/**
     * Builds and setup a TrayIcon for CMT tool.
     * 
     * @param imageURL - URL of the icon image
     * @param trayName - identify the specified tool
     * @param exitListener - a ControllerProject object, listener of the tool instance
     */
	private static void setupTrayIconCMT(URL imageURL, final ToolNames trayName,
										 ControllerProject exitListener, String trayDescr){
		String instanceName=exitListener.getProjectName();
		
		setupTrayIcon(imageURL, trayName, trayDescr, exitListener, instanceName);
	}


	/**
     * Builds and setup a TrayIcon for the specified tool.
     * 
     * @param imageURL - URL of the icon image
     * @param trayName - identify the specified tool
     * @param trayDescr - description of the specified tool
     * @param exitListener - listener of the tool instance
     * @param instanceName - name of the tool instance
     */
	private static void setupTrayIcon(URL imageURL, final ToolNames trayName, String trayDescr,
			  						  ActionListener exitListener, String instanceName) {
        final String aboutTitle=trayDescr;

        //creating tray icon and popup menu
        final JPopupMenu popup = new JPopupMenu();
        final TrayIcon trayIcon = new TrayIcon((new ImageIcon(imageURL, trayDescr)).getImage());
        
        switch(trayName){
          case CMT: cmtTrayIcon=trayIcon; cmtPopup=popup; break;
          case FDE: fdeTrayIcon=trayIcon; fdePopup=popup; break;
        }

        // Create a popup menu components
        JMenuItem aboutItem = new JMenuItem("About");

        //creating first instance submenu
        JMenu instanceMenu = getInstanceSubMenu(exitListener, trayName, trayIcon, instanceName);
        
        //Add components to popup menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(instanceMenu);
        
        trayIcon.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.getButton()==MouseEvent.BUTTON3) {
                	popup.setLocation(e.getX(), e.getY());
                	popup.setInvoker(popup);
                	popup.setVisible(true);
                }
            }
        });
  
        //adding tray icon to system tray
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }
        
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              switch(trayName){
              case CMT:
            	trayIcon.displayMessage("About "+aboutTitle,
            	  "This tool let you extract feature names from a set of documents"
            	 +" each describing a product, thus helping the user to design a"
            	 +" family product line expressing those products, and more, using"
            	 +" Feature Models."            			 
            	 , TrayIcon.MessageType.INFO); break;            	 
              case FDE:
              	trayIcon.displayMessage("About "+aboutTitle,
              	  "This tool let you edit Feature Models using diagrams."
              	 +"It can be used as a standalone application, or be launched by the"
              	 +" Commonality Mining Tool, thus creating a starting diagram"
              	 +" with the user selected features."            			 
              	 , TrayIcon.MessageType.INFO); break;            	 
              }
            }
        });     
        
        //adding instanceMenu to list
        switch(trayName){
          case CMT: cmtList.add(instanceMenu); break;
          case FDE: fdeList.add(instanceMenu); break;
        }

        trayIcon.setImageAutoSize(true);
	}

	/**
	 * Adds a submenu to FDE tray icon for the new instance
	 * @param exitListener - the controller of the FDE instance
	 */
    private static void addInstanceToFDETray(EditorController exitListener) {
      final TrayIcon trayIcon = fdeTrayIcon;
      String instanceName=exitListener.getDiagramName();

      JMenu instanceMenu = getInstanceSubMenu(exitListener, ToolNames.FDE, trayIcon, instanceName);
      
      fdePopup.add(instanceMenu);        
      fdeList.add(instanceMenu);
		
	}

	/**
	 * Adds a submenu to FDE tray icon for the new instance
	 * @param exitListener - the controller of the CMT instance
	 */
    private static void addInstanceToCMTTray(ControllerProject exitListener, final ToolNames trayName) {
      final TrayIcon trayIcon = cmtTrayIcon;
      String instanceName=exitListener.getProjectName();

      JMenu instanceMenu = getInstanceSubMenu(exitListener, ToolNames.CMT, trayIcon, instanceName);
        
	  cmtPopup.add(instanceMenu);
      cmtList.add(instanceMenu);
		
	}

    /**
     * Creates an isntance submenu for the specified tool.
     * 
     * @param exitListener - listener of the tool instance
     * @param trayName - identify the specified tool
     * @param trayIcon - the tray on which the listener must be able to interact with
     * @param instanceName - name of the tool instance
     * 
     * @return - the new JMenu created
     */
	private static JMenu getInstanceSubMenu(ActionListener exitListener, final ToolNames trayName,
											final TrayIcon trayIcon, String instanceName) {

	  final JMenu instanceMenu= new JMenu(instanceName);
	  instanceMenu.setName(instanceName);

	  JMenuItem toFrontItem = new JMenuItem("To Front");
	  JMenuItem maximizeItem = new JMenuItem("Maximize");
	  JMenuItem minimizeItem = new JMenuItem("Minimize");
	  JMenuItem exitItem = new JMenuItem("Exit");

	  toFrontItem.addActionListener(exitListener); 
	  maximizeItem.addActionListener(exitListener); 
	  minimizeItem.addActionListener(exitListener); 
	  exitItem.addActionListener(exitListener); 
	  
	  instanceMenu.add(toFrontItem);
	  instanceMenu.add(maximizeItem);
	  instanceMenu.add(minimizeItem);
	  instanceMenu.addSeparator();
	  instanceMenu.add(exitItem);
	  
	  return instanceMenu;
	}
	
	/**
	 * Removes an instance submenu from the FDE tray icon.
	 * 
     * @param instanceName - name of the tool instance
	 */
	public static void removeInstanceFDE(String instanceName) {
	  JMenu instanceMenu = null;
	  for( JMenu menu : fdeList) if(menu.getName().compareTo(instanceName)==0){ instanceMenu=menu; break;}
	  
	  removeInstance(ToolNames.FDE, instanceMenu);		
	}

	/**
	 * Removes an instance submenu from the CMT tray icon.
	 * 
     * @param instanceName - name of the tool instance
	 */
	public static void removeInstanceCMT(String instanceName) {
	  JMenu instanceMenu = null;
	  for( JMenu menu : cmtList) if(menu.getName().compareTo(instanceName)==0){ instanceMenu=menu; break;}
	  
	  removeInstance(ToolNames.CMT, instanceMenu);
	}

	/**
	 * Removes an instance submenu from the specified tray icon.
	 * 
     * @param trayName - identify the specified tool
     * @param trayIcon - the specified tray
	 * @param instanceMenu - the submenu to be removed
	 */
	private static void removeInstance(final ToolNames trayName, final JMenu instanceMenu) {
	  if(instanceMenu==null || trayName==null) return;
	  switch(trayName){
		case CMT:
		  //removing instance 
		  cmtList.remove(instanceMenu);
		  cmtPopup.remove(instanceMenu);
		  //if the removed instance was the last one, the tool tray icon is removed
		  if(cmtList.size()==0){
			tray.remove(cmtTrayIcon);
			cmtTrayIcon=null;
		  }
		  break;
		case FDE: 
		  //removing instance 
		  fdeList.remove(instanceMenu);
		  fdePopup.remove(instanceMenu);
		  //if the removed instance was the last one, the tool tray icon is removed
		  if(fdeList.size()==0){
			tray.remove(fdeTrayIcon);
			fdeTrayIcon=null;
		  }
		  break;
	  }
	}
	
	/**
	 * Updates an instance submenu in the FDE tray icon.
	 * 
     * @param instanceName - name of the tool instance
	 */
	public static void updateInstanceFDE(String oldInstanceName, String newInstanceName) {
	  for( JMenu menu : fdeList) if(menu.getName().compareTo(oldInstanceName)==0){ 
		menu.setText(newInstanceName); 
		menu.setName(newInstanceName); 
		break;
	  }
	}

	/**
	 * Updates an instance submenu in the CMT tray icon.
	 * 
     * @param instanceName - name of the tool instance
	 */
	public static void updateInstanceCMT(String oldInstanceName, String newInstanceName) {
	  for( JMenu menu : cmtList) if(menu.getName().compareTo(oldInstanceName)==0){ 
		menu.setText(newInstanceName); 
		menu.setName(newInstanceName); break;
	  }
	}
	
	/**
	 * Brings the specified instance of FDE tool to front.
	 * 
	 * @param instanceName - name of the specified instance
	 */
	public static void bringToFrontFDEInstance(String instanceName){
	  for( JMenu menu : fdeList) if(menu.getName().compareTo(instanceName)==0){
		  ((EditorController)menu.getItem(0).getActionListeners()[0]).bringToFront();
		  break;
	  }
	}
	
	/**
	 * Brings the specified instance of CMT tool to front.
	 * 
	 * @param instanceName - name of the specified instance
	 */
	public static void bringToFrontCMTInstance(String instanceName){
	  for( JMenu menu : cmtList) if(menu.getName().compareTo(instanceName)==0){
		  ((ControllerProject)menu.getItem(0).getActionListeners()[0]).bringToFront();
		  break;
	  }
	}
	
	/**
	 * Try to close the specified instance of FDE tool.
	 * 
	 * @param instanceName - name of the specified instance
	 */
	public static void tryCloseFDEInstance(String instanceName){
	  for( JMenu menu : fdeList) if(menu.getName().compareTo(instanceName)==0){
		  ((EditorController)menu.getItem(0).getActionListeners()[0]).closeToolInstance();
		  break;
	  }
	}
	
	/**
	 * Try to close the specified instance of CMT tool.
	 * 
	 * @param instanceName - name of the specified instance
	 */
	public static void tryCloseCMTInstance(String instanceName){
	  for( JMenu menu : cmtList) if(menu.getName().compareTo(instanceName)==0){
		  ((ControllerProject)menu.getItem(0).getActionListeners()[0]).closeToolInstance();
		  break;
	  }
	}

	/**
	 * Tells if the FDE tray icon has already been set on system tray.
	 * 
	 * @return true if tray icon has been set, false otherwise
	 */
	public static boolean isFDETrayPresent(){
	  return fdeTrayIcon!=null;
	}

	/**
	 * Tells if the CMT tray icon has already been set on system tray.
	 * 
	 * @return true if tray icon has been set, false otherwise
	 */
	public static boolean isCMTTrayPresent(){
	  return cmtTrayIcon!=null;
	}

	/**
	 * Tells if the specified instance submenu icon has already been set on FDE tray.
	 * 
	 * @param instanceName - name of the specified instance
	 * 
	 * @return true if instance submenu has been set, false otherwise
	 */
	public static boolean isFDEInstancePresent(String instanceName){
	  for( JMenu menu : fdeList) if(menu.getName().compareTo(instanceName)==0) return true;
	  return false;
	}

	/**
	 * Tells if the specified instance submenu icon has already been set on FDE tray.
	 * 
	 * @param instanceName - name of the specified instance
	 * 
	 * @return true if instance submenu has been set, false otherwise
	 */
	public static boolean isCMTInstancePresent(String instanceName){
	  for( JMenu menu : cmtList) if(menu.getName().compareTo(instanceName)==0) return true;
	  return false;
	}

}
