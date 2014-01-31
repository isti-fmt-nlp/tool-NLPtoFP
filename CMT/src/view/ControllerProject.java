
/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ControllerProject implements ActionListener, WindowListener, MouseListener
{
	private static boolean verbose=true;//variabile usata per attivare stampe nel codice
	
	private ViewProject viewProject = null;
	
	private ModelProject modelProject = null;
	
	/** Costruttore
	 * 
	 * @param viewProject
	 * @param modelProject
	 */
	public ControllerProject(ViewProject viewProject, ModelProject modelProject) 
	{
		this.viewProject = viewProject;
		this.modelProject = modelProject;
	}
	
	/** Gestisce gli eventi generati dalla JMenuBar, dai JButton e dal JPopupMenu
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		if(ae.getActionCommand().equals("Create Project"))
		{
			String s = null;			
			
			if((s = viewProject.assignNameProjectDialog()) != null)
			{
				if(!modelProject.createProject(s))
					viewProject.errorDialog("Project already exists");
				
				else{
					
					/* ***VERBOSE****/
					if (verbose) System.out.println("apro il LateralPanel con s="+s);
					/* ***VERBOSE****/
					
					viewProject.loadPanelLateral(s, null);
				}
			}
		}	
		else if(ae.getActionCommand().equals("Delete Project"))
		{
			viewProject.deleteProjectDialog();
		}
		else if(ae.getActionCommand().equals("Load Project"))
		{
			String s = null;
			
			if((s = viewProject.loadProjectDialog()) != null)
				viewProject.loadPanelLateral(
						s.substring(0, s.length() - 4), modelProject.loadProject(s));
		}
		else if(ae.getActionCommand().equals("Save Project"))
		{
			modelProject.saveProject();
		}
		else if(ae.getActionCommand().equals("Load File"))
		{
			String [] s = null;
			
			if((s = viewProject.loadFileDialog()) != null)
				modelProject.addFileProject(s[1]);
			
		}
		else if(ae.getActionCommand().equals("Delete Selected File"))
		{
			int i = -1;
			
			if((i = viewProject.deleteSelectedFileDialog()) != 1)
				modelProject.removeFileProject(i);
		}
		else if(ae.getActionCommand().equals("Delete File"))
		{
			int i = -1;
			
			if((i = viewProject.deleteFileDialog()) != 1){

				/* ***VERBOSE *** */
				if(verbose) System.out.println("Ho ricevuto i="+i);

				/* ***VERBOSE *** */

				modelProject.removeFileProject(i);
			}
		}
		else if(ae.getActionCommand().equals("Extract Commonalities"))
		{
			viewProject.extractCommonalitiesdDialog();
		}
		else if(ae.getActionCommand().equals("Extract Variabilities"))
		{
			viewProject.extractVariabilitiesDialog();
		}
		else if(ae.getActionCommand().equals("Select Commonalities"))
		{
			viewProject.showFeaturesSelected(ViewPanelCentral.FeatureType.COMMONALITIES);
		}
		else if(ae.getActionCommand().equals("Select Variabilities"))
		{
			viewProject.showFeaturesSelected(ViewPanelCentral.FeatureType.VARIABILITIES);
		}
		else if(ae.getActionCommand().equals("Exit"))
		{
			if(modelProject.readStateProject()[1])
			{
				if(viewProject.saveProjectDialog() == 0)
				{
					if(modelProject.readStateProject()[0])
						modelProject.deleteProject();
				}
				else
					modelProject.saveProject();
			}
			viewProject.closeProject();
		}
		else System.out.println("Unknown action: "+ae.getActionCommand());
	}
	
	/** Gestisce gli eventi generati dal mouse
	 * 
	 */
	@Override
	public void mouseClicked(MouseEvent me)
	{
		if(me.getClickCount() == 2)
			viewProject.loadPanelCentral();
	}

	/** Gestisce gli eventi generati dalla chiusura del JFrame
	 * 
	 */
	@Override
	public void windowClosing(WindowEvent we) 
	{
		if(modelProject.readStateProject()[1])
		{
			if(viewProject.saveProjectDialog() == 0)
			{
				if(modelProject.readStateProject()[0])
					modelProject.deleteProject();
			}
			else
				modelProject.saveProject();
		}
		viewProject.closeProject();
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) 
	{}
	
	@Override
	public void windowClosed(WindowEvent arg0) 
	{}

	@Override
	public void windowDeactivated(WindowEvent arg0) 
	{}

	@Override
	public void windowDeiconified(WindowEvent arg0) 
	{}

	@Override
	public void windowIconified(WindowEvent arg0) 
	{}

	@Override
	public void windowOpened(WindowEvent arg0) 
	{}

	@Override
	public void mouseEntered(MouseEvent arg0) 
	{}

	@Override
	public void mouseExited(MouseEvent arg0) 
	{}

	@Override
	public void mousePressed(MouseEvent arg0) 
	{}

	@Override
	public void mouseReleased(MouseEvent arg0) 
	{}
}
