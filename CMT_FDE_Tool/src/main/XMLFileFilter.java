package main;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.filechooser.FileFilter;

/** Class used to filter project files*/
public class XMLFileFilter extends FileFilter implements  FilenameFilter{
	@Override
	public boolean accept(File dir, String name){
//		System.out.println("loadFileDialog: "+dir.getAbsolutePath()+name);
		return name.endsWith( ".xml" );
	}

	@Override
	public boolean accept(File arg0) {
		return (arg0.isDirectory() || arg0.getName().endsWith( ".xml" ));
	}

	@Override
	public String getDescription() {
		return null;
	}
}