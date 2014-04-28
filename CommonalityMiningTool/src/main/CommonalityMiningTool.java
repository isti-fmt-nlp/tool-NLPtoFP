/**
 * 
 * @author Daniele Cicciarella
 *
 */
package main;

//import java.awt.Image;
//import java.awt.Toolkit;

//import com.apple.eawt.Application;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import view.*;

public /*final */class CommonalityMiningTool{
	public static void main(String[] args){		 
 
		if(OSUtils.isWindows()){
		  try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		  }catch(ClassNotFoundException e){ 	
		  }catch(InstantiationException e){
		  }catch(IllegalAccessException e){
		  }catch(UnsupportedLookAndFeelException e){
		  }
		}else if (OSUtils.isMac()){			
//		  Application application = Application.getApplication();
//		  Image image = Toolkit.getDefaultToolkit().getImage("./src/DATA/Program/CMT.png");
//		  application.setDockIconImage(image);
		  try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		  }catch(ClassNotFoundException e){ 	
		  }catch(InstantiationException e){
		  }catch(IllegalAccessException e){
		  }catch(UnsupportedLookAndFeelException e){
		  }
		}else if (OSUtils.isUnix()){
	      try{
	    	UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	      }catch(ClassNotFoundException e){ 	
	      }catch(InstantiationException e){
	      }catch(IllegalAccessException e){
	      }catch(UnsupportedLookAndFeelException e){
	      }
		}else if (OSUtils.isSolaris()){
		  try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		  }catch(ClassNotFoundException e){ 	
		  }catch(InstantiationException e){
		  }catch(IllegalAccessException e){
		  }catch(UnsupportedLookAndFeelException e){
		  }
		}


		ModelProject modelProject = new ModelProject();
		ViewProject viewProject = new ViewProject(modelProject);		
		ControllerProject controllerProject = new ControllerProject(viewProject, modelProject);
		
		modelProject.addObserver(viewProject);
		viewProject.addListener(controllerProject);
	}
}
