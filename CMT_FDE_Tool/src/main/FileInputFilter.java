package main;

import java.io.File;
import java.io.FilenameFilter;

/** Class used to filter input files*/
public class FileInputFilter implements FilenameFilter{
	@Override
	public boolean accept(File dir, String name){
		return name.endsWith( ".pdf" ) || name.endsWith(".txt");
    }
}
