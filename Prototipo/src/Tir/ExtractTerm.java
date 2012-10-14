import java.util.*;

public class ExtractTerm
{
    /* Array di RecordPdf da analizzare */
    public RecordPdf recordPdf[] = null;
    /* ArrayList contenenti i vari Commonalities */
    public ArrayList <String> Cr = null;
    public ArrayList <String> Cd = null;
    public ArrayList <String> Cs = null;
    /* ArrayList contenenti i vari Variabilities */
    public ArrayList <String> Vr = null;
    public ArrayList <String> Vd = null;
    public ArrayList <String> Vs = null;

    public ExtractTerm(RecordPdf Record[] )
    {
        this.recordPdf = Record;
    }
    
    /** Funzione esegue le funzioni Commonalities e Variabilities */
    public final void RunExtract()
    {
    	Commonalities();	
    	
    	Variabilities();
    	
    	/* Salviamo risultati in due textarea */
    }

    /** Funzione estrae i vari Commonalities utilizzando la ricorsione */
    private void Commonalities()
    {
        this.Cr = new ArrayList<String>();
        this.Cd = new ArrayList<String>();
        this.Cs = new ArrayList<String>();
        
        /* Estraiamo i Commonalities Relevant */
        for(int i = 0; i < this.recordPdf[0].termRelevant.size(); i = i + 1)
        	if(Intersect(this.recordPdf[0].termRelevant.get(i), 1, 0))
        		this.Cr.add(this.recordPdf[0].termRelevant.get(i));
        
        /* Estraiamo i Commonalities Domain */
        for(int i = 0; i < this.recordPdf[0].termDomain.size(); i = i + 1)
        	if(Intersect(this.recordPdf[0].termDomain.get(i), 1, 1))
        		this.Cd.add(this.recordPdf[0].termDomain.get(i));
 
        /* Estraiamo i Commonalities Single */
        for(int i = 0; i < this.recordPdf[0].termSingle.size(); i = i + 1)
        	if(Intersect(this.recordPdf[0].termSingle.get(i), 1, 2))
        		this.Cs.add(this.recordPdf[0].termSingle.get(i));
    }

    /** Funzione estrai i vari Vsriabilities utilizzando la ricorsione */
    private void Variabilities()
    {
        ArrayList <String> Xr , Xd, Xs;

        Xr = new ArrayList<String>();
        Xd = new ArrayList<String>();
        Xs = new ArrayList<String>();

        for(int i = 0; i < recordPdf.length; i = i + 1)
        {
            Xr = Union(recordPdf[i].termRelevant,Xr);
            Xd = Union(recordPdf[i].termDomain, Xd);
            Xs = Union(recordPdf[i].termSingle, Xs);
        }

        /* Estraiamo i Variabilities Relevant */
        this.Vr = Difference(Xr, this.Cr);
        /* Estraiamo i Variabilities Domain */
        this.Vd = Difference(Xd, this.Cd);
        /* Estraiamo i Variabilities Single */
        this.Vs = Difference(Xs, this.Cs);
    }

    /** Funzione che effettua l'intersezione tra due ArrayList

       @param k: Stringa da cercare
       @param i: Intero che indica quale ArrayList controllare
       @param campo: Intero che indica quale tipo di termine controllare

       @return true: Elemento trovato nell'ArrayList
       @return false: Elemento non trovato nell'ArrayList
       
    */
    public boolean Intersect(String k, int i, int type)
    {
    	switch(type)
    	{
    		case 0:
    		{		
    			if(i == this.recordPdf.length) return true;
    			
    			if(!Intersect(k, i+1, type)) return false;
    			
    			if(SearchRecursive(this.recordPdf[i].termRelevant, k, 0, this.recordPdf[i].termRelevant.size()-1))
    				return true;
    				
    			else return false;
    		} 		
    		case 1:
    		{
    			if(i == recordPdf.length) return true;
    			
    			if(!Intersect(k, i+1, type)) return false;
    			
    			if(SearchRecursive(this.recordPdf[i].termDomain, k, 0, this.recordPdf[i].termDomain.size()-1))
    				return true;
    				
    			else return false;
    		}  		
    		case 2:
    		{
    			if(i == this.recordPdf.length) return true;
    			
    			if(!Intersect(k, i+1, type)) return false;
    			
    			if(SearchRecursive(this.recordPdf[i].termSingle, k, 0, this.recordPdf[i].termSingle.size()-1))
    				return true;
    				
    			else return false;
    		}	
    		default: return false;
    	}
    }
    /** Funzione che effettua l'unione tra due ArrayList

       @param C: ArrayList
       @param X: ArrayList

       @return X: ArrayList contenente l'unione tra i due ArrayList

    */
    private ArrayList<String> Union(ArrayList <String> C, ArrayList <String> X)
    {
    	if(C.isEmpty())
    		return X;
    	
        if(X.isEmpty())
            X.add(C.get(0));

        Collections.sort(X);

        for(int i = 0; i < C.size(); i = i + 1)
            if(!SearchRecursive(X, C.get(i), 0, X.size()-1))
                X.add(C.get(i));

        return X;
    }

    /** Funzione effettua la differenza fra due ArrayList

       @param X: ArrayList
       @param C: ArrayList 

       @return A: ArrayList contenente la differenza tra X ed C
    */
    private ArrayList<String> Difference(ArrayList <String> X, ArrayList <String> C)
    {
        ArrayList<String> A = new ArrayList<String>();
        
        if(C.isEmpty())
        {
        	for(int i = 0 ; i < X.size(); i = i + 1)
        		A.add(X.get(i));
        }
        else
        {
        	for(int i = 0 ; i < X.size(); i = i + 1)
        		if(!SearchRecursive(C, X.get(i), 0, C.size()-1))
        			A.add(X.get(i));
        }
        return A;
    }

    /** Funzione che effettua la ricerca ricorsiva
      
       @param A:  ArrayList dove dobbiamo effettuare la ricerca
       @param k:  Stringa da ricercare nell'ArrayList
       @param sx: Intero che indica dove parte la ricerca
       @param dx: Intero che indica dove finisce la ricerca

       @return true: L'elemento si trova nell'array
       @return false: L'elemento non si trova nell'array
    */
    private boolean SearchRecursive(ArrayList <String> A, String k, int sx, int dx)
    {
    	if (sx == dx)
    	{
    		if (k.equals(A.get(sx))) return true; 
    		
    		else return false;
    	}
    	
    	int cx = (sx+dx)/2; 
    	
    	if (k.compareTo(A.get(cx)) <= 0) return SearchRecursive(A, k, sx, cx); 
    	
    	else return SearchRecursive(A, k, cx + 1, dx);
    }
    
    private void initializeCommonalities()
    {
    	
    }
    
    private void initializeVariabilities()
    {
    	
    }
}