/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.io.File;

public class ModelFile extends ModelAnalysis implements Runnable{
	/** Tells about the result of the analisys */
	private boolean result = false;
	
	private boolean isAnalisysDir=false;
	
	/** 
	 * Constructor.
	 * 
	 * @param pathFile - input file path
	 * @param pathProject - project path
	 */
	public ModelFile(String pathFile, String pathProject){
		super(pathFile, pathProject);
	}
	
	/** 
	 * Thread: Analyzes the file.
	 */
	@Override
	public void run(){
		/* Checks if the file has already been analyzed*/
		if( (new File(readPathFileUTF8()).exists()) 
			 && (new File(readPathFileUTF8()).lastModified() > new File(readPathFile()).lastModified())){
			/* Loads file data*/
			if(loadAnalysisFile()) result = true;            
            else  result = false;			
		}
		else{
			/* Analyzes the file */
			if(runAnalysisFile()) result = true;            
            else  result = false;
		}
	}
	
	/** 
	 * Tells if the analisys has been successfully done.
	 * 
	 * @return result - a boolean indicating analisys results
	 */
	public boolean readResult(){
		return result;
	}
	
	/**
	 * Sets the 'isAnalisysDir' property, which tells if this model <br>
	 * is that of an analisys directory or an input file. Default property value is false.
	 * 
	 * @param value - the boolean value, if true this model represents an analisys directory
	 */
	public void setIsAnalisysDir(boolean value){
	  isAnalisysDir=value;
	}
}
