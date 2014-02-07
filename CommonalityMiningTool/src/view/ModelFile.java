/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.io.File;

public class ModelFile extends ModelAnalysis implements Runnable
{
	/* Boolean che indica l'esito dell'analisi */
	private boolean result = false;
	
	/** Costruttore
	 * 
	 * @param pathFile
	 * @param pathProject
	 */
	public ModelFile(String pathFile, String pathProject)
	{
		super(pathFile, pathProject);
	}
	
	/** Thread: Analizza il file
	 * 
	 */
	@Override
	public void run() 
	{
		/* Controlla se il file � stato analizzato in precedenza */
		if((new File(readPathFileUTF8()).exists()) && (new File(readPathFileUTF8()).lastModified() > new File(readPathFile()).lastModified()))
		{
			/* Carica i dati del file */
			if(loadAnalysisFile())
				result = true;
            
            else 
            	result = false;
			
		}
		else
		{
			/* Effettua analisi del file */
			if(runAnalysisFile())
				result = true;
            
            else 
            	result = false;
		}
	}
	
	/** Lettura dell'esito del'analisi
	 * 
	 * @return result
	 */
	public boolean readResult()
	{
		return result;
	}
}
