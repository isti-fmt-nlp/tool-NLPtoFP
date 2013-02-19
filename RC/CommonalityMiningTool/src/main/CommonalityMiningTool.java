/**
 * 
 * @author Daniele Cicciarella
 *
 */
public final class CommonalityMiningTool 
{
	public static void main(String[] args) 
	{
		ModelProject modelProject = new ModelProject();
		
		ViewProject viewProject = new ViewProject(modelProject);
		
		ControllerProject controllerProject = new ControllerProject(viewProject, modelProject);
		
		modelProject.addObserver(viewProject);
		viewProject.addListener(controllerProject);
	}
}
