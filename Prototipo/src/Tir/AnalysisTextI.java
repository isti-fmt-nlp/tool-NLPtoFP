package project;

import java.util.ArrayList;

public interface AnalysisTextI 
{
	/* URL a cui ci dobbiamo connettere per ottenere le analisi */
    public final String URL_ANALYSIS = "http://www.ilc.cnr.it/dylanlab/index.php?page=texttools&hl=en_US&showtemplate=false";
    
	/** Funzione carica i dati della precedente analisi */
    public boolean loadAnalysis();
    
    /** Funzione si connette al sito "http_URL" per ottenere le
    	analisi effettuate

    @return true: Set di pagine html contenenti le analisi
    @return false: Se vi sono stati errori.

    */
    public boolean runAnalysis();
    
    /**  
     * 
     * @return
     */
    public ArrayList <String> getTermRelevant();
    
    /** 
     * 
     * @return
     */
    public ArrayList <String> getTermDomain();
    
    /**  
     * 
     * @return
     */
    public ArrayList <String> getTermSingle();
    
    
    /**
     * 
     * @param i:
     * @return
     */
    public String getPageHtml(int i);
    
}
