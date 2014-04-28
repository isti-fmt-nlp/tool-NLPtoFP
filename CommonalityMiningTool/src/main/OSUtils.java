package main;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.ImageIcon;

public class OSUtils {

	/** values used to specify one of the tools*/
	public static enum ToolNames{CMT, FDET};

	/** tells if the tray icon for the FDET tool is already present*/
	private static boolean fdetTrayIconPresent=false;
	/** tells if the tray icon for the CMT tool is already present*/
	private static boolean cmtTrayIconPresent=false;
	
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	 
	/** Tells if whether the Operative System is Windows*/
	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0); 
	}
 
	/** Tells if whether the Operative System is Mac*/
	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0); 
	}
 
	/** Tells if whether the Operative System is Unix or Linux*/
	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ); 
	}
 
	/** Tells if whether the Operative System is Solaris*/
	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0); 
	}
	
	/** Adds the tray icon for the tool that requested it, if the OS supports it.
	 * 
	 * @param imageURL - URL of the icon
	 * @param trayName - type of tool requesting the icon
	 * @param exitListener - listener to be set on the 'Exit' item in the tray icon's popup menu
	 */
    public static void createAndShowGUI(URL imageURL, final ToolNames trayName, 
    									ActionListener exitListener, String exitActionName) {
    	
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        
        final SystemTray tray = SystemTray.getSystemTray();
        String trayDescr="";
        switch(trayName){
          case CMT: 
        	if(cmtTrayIconPresent) return;
        	else{
        	  trayDescr="Commonality Mining Tool"; cmtTrayIconPresent=true;
        	}
          	break;
          case FDET:
            if(fdetTrayIconPresent) return;
            else{
            	trayDescr="Feature Diagram Editor Tool"; fdetTrayIconPresent=true;
            }
        	break;
        }
        
        final String aboutTitle=trayDescr;


        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon((new ImageIcon(imageURL, trayDescr)).getImage());
        
        // Create a popup menu components
        MenuItem aboutItem = new MenuItem("About");
//        CheckboxMenuItem cb1 = new CheckboxMenuItem("Set auto size");
//        CheckboxMenuItem cb2 = new CheckboxMenuItem("Set tooltip");
        MenuItem exitItem = new MenuItem(exitActionName);
        
        //Add components to popup menu
        popup.add(aboutItem);
        popup.addSeparator();
//        popup.add(cb1);
//        popup.add(cb2);
//        popup.addSeparator();
//        popup.add(displayMenu);
//        displayMenu.add(errorItem);
//        displayMenu.add(warningItem);
//        displayMenu.add(infoItem);
//        displayMenu.add(noneItem);
        popup.add(exitItem);
        
        trayIcon.setPopupMenu(popup);
        
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
            	  "This tool is used to extract possible feature names from a set of documents"
            	 +"each of with describing a product, thus helping the user to design a"
            	 +" family product line capable of expressing the described products, and more, using"
            	 +" the formalization of Feature Models."            			 
            	 , TrayIcon.MessageType.INFO); break;            	 
              case FDET:
              	trayIcon.displayMessage("About "+aboutTitle,
              	  "This tool is edit Feature Models using diagrams."
              	 +"It can be used as a standalone application, or be launched by the"
              	 +" Commonality Mining Tool, thus creating a simple starting diagram"
              	 +" with the user selected features."            			 
              	 , TrayIcon.MessageType.INFO); break;            	 
              }
            }
        });
        
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              tray.remove(trayIcon);
              switch(trayName){
                case CMT: cmtTrayIconPresent=false; break;
                case FDET: fdetTrayIconPresent=false; break;
              }
            }
        });
        
        exitItem.addActionListener(exitListener);
        
        trayIcon.setImageAutoSize(true);
    }
	

}
