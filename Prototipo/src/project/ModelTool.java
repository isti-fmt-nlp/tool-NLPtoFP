package project;

import java.io.*;
import java.util.*;

public class ModelTool extends Observable implements Observer
{
	private ArrayList <RecordPdf> inputFile = new ArrayList <RecordPdf> ();
	
	private String pathInput = null;
	
	private Worker worker = null;
	
	public enum ProjectStatus
	{
		NEW (false),
		MODIFY (false);
		
		private boolean bool;
		
		private ProjectStatus(boolean bool)
		{
			this.bool = bool;
		}
		
		public boolean getStatus()
		{
			return bool;
		}
		
		public void setStatus(boolean bool)
		{
			this.bool = bool;
		}
	}
	
	@Override
	public void update(Observable os, Object o) 
	{
		if(os instanceof Worker)
		{
			if(o.equals("1"))
			{
				System.out.println("si");
				setChanged();
				notifyObservers("1");
			}
		}
	}
	
	public void createProject()
	{
		ProjectStatus.NEW.setStatus(true);
	}
	
	public void loadProject()
	{
		String s = null;
		
		try 
		{
			BufferedReader reader =
			        new BufferedReader(
			        		new FileReader(pathInput));
			
			while((s = reader.readLine())!=null)
				if(!s.equals("\n"))
					inputFile.add(new RecordPdf(s));
				
			reader.close();
		} 
		catch (FileNotFoundException e) 
		{
			return;
		} 
		catch (IOException e) 
		{
			return;
		}	
	}
	
	public void saveProject()
	{	
		try 
		{		
			PrintWriter pw =  
					new PrintWriter(
							new BufferedWriter(
									new FileWriter(pathInput, false)));
			
			for(int i = 0; i < inputFile.size(); i++)
				pw.print(inputFile.get(i).getPathPdf() + "\n");

			pw.close();
		} 
		catch (UnsupportedEncodingException e) 
		{
			return;
		} 
		catch (FileNotFoundException e) 
		{
			return;
		} 
		catch (IOException e) 
		{
			return;
		}
		
		ProjectStatus.NEW.setStatus(false);
		ProjectStatus.MODIFY.setStatus(false);
	}
	
	public void addFileProject(String s)
	{
		inputFile.add(new RecordPdf(s));
		ProjectStatus.MODIFY.setStatus(true);
	}
	
	public void removeFileProject(int i)
	{
		inputFile.remove(i);
		ProjectStatus.MODIFY.setStatus(true);
	}	
	
	public void startProject() 
	{
		worker = new Worker(inputFile);
		worker.addObserver(this);
		
		Thread t = new Thread(worker);
		t.start();
			
	}
	
	public File[] getAnalysisFileProject(int i)
	{
		File f[] = new File[4];
		
		inputFile.get(i).loadAnalysis();
		
		if(inputFile.get(i).getPathTxt() != null)
			f[0] = new File(inputFile.get(i).getPathTxt());
		else 
			return null;
		
		if(inputFile.get(i).getPageHtml(1) != null)
			f[1] = new File(inputFile.get(i).getPageHtml(1));
		else
			return null;
		
		if(inputFile.get(i).getPageHtml(2) != null)
			f[2] = new File(inputFile.get(i).getPageHtml(2));
		else
			return null;
		
		if(inputFile.get(i).getPageHtml(3) != null)
			f[3] = new File(inputFile.get(i).getPageHtml(3));
		else 
			return null;
			
		return f;
	}
	
	public void setPathInput(String s)
	{
		pathInput = s;
	}

	public String getPathInput() 
	{
		return pathInput;
	}
}
