/**
 * 
 * @author Daniele Cicciarella
 *
 */
package main;

//import java.awt.Image;
//import java.awt.Toolkit;

//import com.apple.eawt.Application;

import view.*;

public /*final */class CommonalityMiningTool 
{
	public static void main(String[] args) 
	{
//		Application application = Application.getApplication();
//		Image image = Toolkit.getDefaultToolkit().getImage("./src/DATA/Program/CMT.png");
//		application.setDockIconImage(image);
		System.out.println("Cartella attuale"+System.getProperty("user.dir"));

		ModelProject modelProject = new ModelProject();
		ViewProject viewProject = new ViewProject(modelProject);		
		ControllerProject controllerProject = new ControllerProject(viewProject, modelProject);
		
		modelProject.addObserver(viewProject);
		viewProject.addListener(controllerProject);
	}
}
