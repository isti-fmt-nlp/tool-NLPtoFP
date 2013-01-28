package project;

/**
	La classe ThreadRecordPdf ha come obiettivo di assegnare
	un RecordPdf a un thread. Ogni Thread analizzerà parallelamente,
	agli altri thread, il RecordPdf assegnatogli.
*/
public class ThreadRecordPdf extends Thread
{
    /* RecordPdf da calcolare */
    public RecordPdf recordPdf = null;

    /* Percorso in cui si trova il file.pdf */
    public String pathPdf = null;
    
    /** Costruttore

 	      @param pathPdf: Stringa contenente la path dove è situato il pdf da analizzare

	      @note Vengono inizializzate le path del file.pdf

    */
    public ThreadRecordPdf(String pathPdf)
    {
        this.pathPdf = pathPdf;
    }
    
    /** 
        Funzione eseguita da ogni thread. Ognuno di essi esegue 
        l'analisi del file.pdf assegnatogli
    */
    @Override
    public void run()
    {
        recordPdf = new RecordPdf(pathPdf);
        /*
            Inizio analisi del file.pdf
        */
        recordPdf.RunRecord();
    }
}