package main;

import java.io.File;
import java.io.FilenameFilter;

/** Class used to filter project files. */
public class FilterFileProject implements FilenameFilter{
	@Override
	public boolean accept(File dir, String name){
		return name.endsWith( ".xml" );
    }
}
