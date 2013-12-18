package project;

import java.io.*;

/**
   La classe RecordPdf si presenta come una struttura dati
   contenente tutti i dati dell'analisi del testo del file.pdf
*/
public final class RecordPdf extends AnalysisText
{
    /** Costruttore
     * 
     * @param pathPdf: Stringa contenente la path dove è situato il pdf da analizzare
     */
    public RecordPdf(String pathPdf)
    {
        super(pathPdf);
    }

    /** Funzione che carica tutte le informazioni del file.pdf

        @param true: Record del file.pdf caricati in maniera corretta
        @param false: Se vi sono stati errori
    */
    public boolean RunRecord()
    {
        /* Carico i dati del file.pdf già in precedenza calcolati */
        if((new File(this.getPathTxt()).exists()) && (new File(this.getPathTxt()).lastModified() > new File(this.getPathPdf()).lastModified()))
        {
            if(loadAnalysis())
            	return true;
            
            else 
            	return false;
        }
        /* Calcolo i dati del nuovo file.pdf */
        else
        {
            if(runAnalysis())
                return true;
            
            else 
            	return false;
        }
    }
}