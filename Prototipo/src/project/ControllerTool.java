package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Observable;
import java.util.Observer;

import project.ModelTool.ProjectStatus;

public class ControllerTool extends Observable implements Observer
{
	private ModelTool modelTool = null;
	
	public final String PATH_RECENT_FILE = "./Recent.txt";
	
	public ControllerTool()
	{
		setDefaultProject();
	}
	
	@Override
	public void update(Observable os, Object o) 
	{
		if(os instanceof ModelTool)
		{
			if(o.equals("1"))
			{
				
				setChanged();
				notifyObservers("10B");
			}
		}
	}
	
	public void sendActionModel(ViewTool viewTool, int i)
	{
		switch(i)
		{
			case 0:
			{	
				manageSave(viewTool, true);
				setDefaultProject();
				
				modelTool = new ModelTool();
				modelTool.addObserver(this);
				modelTool.createProject();
				
				setChanged();
				notifyObservers("0");
				
				break;
			}
			case 1:
			{
				manageSave(viewTool, true);
				
				String s1[] = null;
				
				if((s1 = viewTool.getViewDialog().dialogLoadTree()) != null)
				{
					viewTool.getViewTree().setPathTree(s1[1]);
					
					modelTool = new ModelTool();
					modelTool.addObserver(this);
					modelTool.setPathInput(s1[1].substring(0, s1[1].length()-4) + "txt");
					modelTool.loadProject();
					
					setDefaultProject();
					setChanged();
					notifyObservers("1");
				}					
				break;
			}
			case 2:
			{
				manageSave(viewTool, false);
				
				String s[] = null;
				
				if((s = loadRecentFile()) != null)
				{
					viewTool.getViewTree().setPathTree(s[0]);
					
					modelTool = new ModelTool();
					modelTool.addObserver(this);
					modelTool.setPathInput(s[1]);
					modelTool.loadProject();
					
					setDefaultProject();
					setChanged();
					notifyObservers("2");
				}
				break;
			}
			case 3:
			{
				manageSave(viewTool, true);				
				setChanged();
				notifyObservers("3");
				
				break;
			}
			case 4:
			{
				String s = null;
				
				if((s = viewTool.getViewDialog().dialogSaveTree(0)) != null)
				{
					viewTool.getViewTree().setPathTree(s + ".xml");
					viewTool.getViewTree().saveTree();
					modelTool.setPathInput(s + ".txt");
					modelTool.saveProject();
					updateRecentFile(viewTool);
				}
				
				setChanged();
				notifyObservers("4");
				
				break;
			}
			case 5:
			{
				manageSave(viewTool, true);
				setDefaultProject();
				setChanged();
				notifyObservers("5");
				
				break;
			}
			case 6:
			{
				manageSave(viewTool, true);
				setChanged();
				notifyObservers("6");
				
				break;
			}
			case 7:
			{
				int i1 = -1;
				
				File f[] = null;
				
				if((i1 = viewTool.getViewTree().getSelectLeafInput()) != -1)
					f = modelTool.getAnalysisFileProject(i1);
				
				if(f != null)
				{
					viewTool.getViewTab().setFileAnalysis(f);
					setChanged();
					notifyObservers("7");
				}
				break;
			}
			case 8:
			{
				String s[] = null;
				
				if((s = viewTool.getViewDialog().dialogLoadLeafTree()) != null)
				{
					viewTool.getViewTree().addLeafTree(s[0]);
					modelTool.addFileProject(s[1]);
				}
				
				setChanged();
				notifyObservers("8");
				
				break;
			}
			case 9:
			{
				int i1 = -1;
				
				if((i1 = viewTool.getViewTree().deleteLeafTree()) != -1)
					modelTool.removeFileProject(i1);
				
				break;
			}
			case 10:
			{
				
				modelTool.startProject();
				
				setChanged();
				notifyObservers("10A");
				
				break;
			}
		}
	}
	
	private void setDefaultProject()
	{
		ProjectStatus.NEW.setStatus(false);
		ProjectStatus.MODIFY.setStatus(false);
	}
	
	private void manageSave(ViewTool viewTool, boolean b)
	{
		String s = null;
		
		if(ProjectStatus.NEW.getStatus())
		{
			if((s = viewTool.getViewDialog().dialogSaveTree(0)) != null)
			{
				viewTool.getViewTree().setPathTree(s + ".xml");
				viewTool.getViewTree().saveTree();
				modelTool.setPathInput(s + ".txt");
				modelTool.saveProject();
				
				if(b)
					updateRecentFile(viewTool);
			}
		}		
		else if(ProjectStatus.MODIFY.getStatus())
		{
			if((s = viewTool.getViewDialog().dialogSaveTree(1)) != null)
			{
				viewTool.getViewTree().saveTree();
				modelTool.saveProject();
				
				if(b)
					updateRecentFile(viewTool);
			}
		}
		else
		{}
	}
	
	public void updateRecentFile(ViewTool viewTool)
	{
        try
        {
        	PrintStream fr =
        			new PrintStream(
        					new FileOutputStream(PATH_RECENT_FILE),false,"UTF-8");
        	
        	fr.print(viewTool.getViewTree().getPathTree() + "\n");
        	fr.print(modelTool.getPathInput());
            fr.close();
        }
        catch (UnsupportedEncodingException ex)
        {
            return;
        }
        catch (FileNotFoundException ex)
        {
            return;
        }
	}
	
	public String[] loadRecentFile()
	{
		int i = 0;
		
		File f = new File(PATH_RECENT_FILE);
		
		if(f.exists())
		{
			try 
			{
				BufferedReader reader =
				        new BufferedReader(
				             new FileReader(PATH_RECENT_FILE));
				
				String s = null;
				
				String s1[] = new String[2];
				
				while((s = reader.readLine()) != null)
				{
					s1[i] = s;
					i = i + 1;
				}
				
				reader.close();
				
				return s1;
			} 
			catch (FileNotFoundException e) 
			{
				return null;
			} 
			catch (IOException e) 
			{
				return null;
			}
		}
		else 
			return null;	
	}
}
