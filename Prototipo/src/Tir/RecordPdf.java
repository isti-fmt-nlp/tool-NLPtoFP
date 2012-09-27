package Tir;

import java.io.*;

/**
   La classe RecordPdf si presenta come una struttura dati
   contenente tutti i dati dell'analisi del testo del file.pdf
*/
public final class RecordPdf extends AnalysisText
{
    /** Costruttore

 	      @param pathPDF: Stringa contenente la path dove è situato il pdf da analizzare

	       @note Vengono inizializzate le path del file.pdf e del file.txt

    */
    public RecordPdf(String pathPDF)
    {
        super(pathPDF);
    }

    /** Funzione che carica tutte le informazioni del file.pdf

        @param true: Record del file.pdf caricati in maniera corretta
        @param false: Se vi sono stati errori
    */
    public boolean Run()
    {
        /* Carico i dati del file.pdf se in precedenza è stato calcolato */
        if((new File(path_txt).exists()) && (new File(path_txt).lastModified() > new File(path_pdf).lastModified()))
        {
            LoadAnalysis();
            return true;
        }
        /* Calcolo i dati del nuovo file.pdf */
        else
        {
            if(!RunAnalysis())
                return false;

            else return true;
        }
    }
}
