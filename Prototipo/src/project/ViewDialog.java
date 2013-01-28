package project;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Observable;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ViewDialog extends Observable implements ViewDialogI
{
	@Override
	public String dialogSaveTree(int i) 
	{
		if(i == 0)
		{
			if(alertDialog("Do you want to save the new project?", "Save New Project") == 1)
				return saveDialog("Save New Project");
			
			else
				return null;
		}
		else
		{
			if(alertDialog("Do you want to save the project?", "Save Project") == 1)
				return "OK";
			
			else 
				return null;
		}
	}

	@Override
	public String[] dialogLoadTree() 
	{
		return loadDialog("Open Project", new FilterXml());
	}

	@Override
	public String[] dialogLoadLeafTree() 
	{
		return loadDialog("Add File", new FilterPdf());
	}
	
	private int alertDialog(String s1, String s2)
    {
		JFrame f = new JFrame(s2);
		
    	Object[] options = {"No","Yes"};			
		
		int i = JOptionPane.showOptionDialog(
				f, s1, s2, JOptionPane.OK_OPTION, JOptionPane.NO_OPTION, null, options, options[1]);
		
		f.setVisible(false);
		
		return i;
    }

	private String saveDialog(String s)
    {
    	FileDialog d = new FileDialog(new JFrame(s));
    	d.setMode(FileDialog.SAVE);
	    d.setDirectory(".");
	    d.setVisible(true);
	    
	    if(d.getFile() == null)
	    	return null;
	   
	    else 
	    	return d.getDirectory() + d.getFile().toString();  
    }
	
	private String [] loadDialog(String s1, FilenameFilter fnf)
	{
		String s[] = new String[2];
		
		FileDialog d = new FileDialog(new JFrame(s1));
    	d.setMode(FileDialog.LOAD);
    	d.setFilenameFilter(fnf);
	    d.setDirectory(".");
	    d.setVisible(true);
	    
	    if(d.getFile() == null)
	    	return null;
	    
	    else
	    {
	    	s[0] = d.getFile().toString();
	    	s[1] = d.getDirectory() + d.getFile().toString();
	    }	    
	    return s;
	}
	
	class FilterXml implements FilenameFilter 
	{
		public boolean accept(File dir, String name) 
		{
			return name.endsWith( ".xml" );
	    }
	}
	
	class FilterPdf implements FilenameFilter 
	{
		@Override
		public boolean accept(File dir, String name) 
		{
			return name.endsWith( ".pdf" );
	    }
	}
}
