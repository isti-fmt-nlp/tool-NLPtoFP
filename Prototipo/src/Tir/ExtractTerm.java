package project;

import java.util.*;
import javax.swing.*;

public final class ExtractTerm
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
    
  //  JScrollPane jCommonalities = null;
  //  JScrollPane jVariabilities = null
    
    public JTextArea jCommonalities = null;
    public JTextArea jVariabilities = null;

    public ExtractTerm(RecordPdf Record[] )
    {
        this.recordPdf = Record;
    }
    
    /** Funzione esegue le funzioni Commonalities e Variabilities */
    public final void RunExtract()
    {
    	extractCommonalities();		
    	extractVariabilities();
    	createJCommonalities();
    	createJVariabilities();
    }

    /** Funzione estrae i vari Commonalities utilizzando la ricorsione */
    private void extractCommonalities()
    {
        this.Cr = new ArrayList<String>();
        this.Cd = new ArrayList<String>();
        this.Cs = new ArrayList<String>();
        
        /* Estraiamo i Commonalities Relevant */
        for(int i = 0; i < this.recordPdf[0].getTermRelevant().size(); i = i + 1)
        	if(intersect(this.recordPdf[0].getTermRelevant().get(i), 1, 0))
        		this.Cr.add(this.recordPdf[0].getTermRelevant().get(i));
        
        /* Estraiamo i Commonalities Domain */
        for(int i = 0; i < this.recordPdf[0].getTermDomain().size(); i = i + 1)
        	if(intersect(this.recordPdf[0].getTermDomain().get(i), 1, 1))
        		this.Cd.add(this.recordPdf[0].getTermDomain().get(i));
 
        /* Estraiamo i Commonalities Single */
        for(int i = 0; i < this.recordPdf[0].getTermSingle().size(); i = i + 1)
        	if(intersect(this.recordPdf[0].getTermSingle().get(i), 1, 2))
        		this.Cs.add(this.recordPdf[0].getTermSingle().get(i));
    }

    /** Funzione estrai i vari Vsriabilities utilizzando la ricorsione */
    private void extractVariabilities()
    {
        ArrayList <String> Xr , Xd, Xs;

        Xr = new ArrayList<String>();
        Xd = new ArrayList<String>();
        Xs = new ArrayList<String>();

        for(int i = 0; i < recordPdf.length; i = i + 1)
        {
            Xr = union(recordPdf[i].getTermRelevant(),Xr);
            Xd = union(recordPdf[i].getTermDomain(), Xd);
            Xs = union(recordPdf[i].getTermSingle(), Xs);
        }

        /* Estraiamo i Variabilities Relevant */
        this.Vr = difference(Xr, this.Cr);
        /* Estraiamo i Variabilities Domain */
        this.Vd = difference(Xd, this.Cd);
        /* Estraiamo i Variabilities Single */
        this.Vs = difference(Xs, this.Cs);
    }

    /** Funzione che effettua l'intersezione tra due ArrayList

       @param k: Stringa da cercare
       @param i: Intero che indica quale ArrayList controllare
       @param campo: Intero che indica quale tipo di termine controllare

       @return true: Elemento trovato nell'ArrayList
       @return false: Elemento non trovato nell'ArrayList
       
    */
    private boolean intersect(String k, int i, int type)
    {
    	switch(type)
    	{
    		case 0:
    		{		
    			if(i == this.recordPdf.length) return true;
    			
    			if(!intersect(k, i+1, type)) return false;
    			
    			if(searchRecursive(this.recordPdf[i].getTermRelevant(), k, 0, this.recordPdf[i].getTermRelevant().size()-1))
    				return true;
    				
    			else return false;
    		} 		
    		case 1:
    		{
    			if(i == recordPdf.length) return true;
    			
    			if(!intersect(k, i+1, type)) return false;
    			
    			if(searchRecursive(this.recordPdf[i].getTermDomain(), k, 0, this.recordPdf[i].getTermDomain().size()-1))
    				return true;
    				
    			else return false;
    		}  		
    		case 2:
    		{
    			if(i == this.recordPdf.length) return true;
    			
    			if(!intersect(k, i+1, type)) return false;
    			
    			if(searchRecursive(this.recordPdf[i].getTermSingle(), k, 0, this.recordPdf[i].getTermSingle().size()-1))
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
    private ArrayList <String> union(ArrayList <String> C, ArrayList <String> X)
    {
    	if(C.isEmpty())
    		return X;
    	
        if(X.isEmpty())
            X.add(C.get(0));

        Collections.sort(X);

        for(int i = 0; i < C.size(); i = i + 1)
            if(!searchRecursive(X, C.get(i), 0, X.size()-1))
                X.add(C.get(i));

        return X;
    }

    /** Funzione effettua la differenza fra due ArrayList

       @param X: ArrayList
       @param C: ArrayList 

       @return A: ArrayList contenente la differenza tra X ed C
    */
    private ArrayList <String> difference(ArrayList <String> X, ArrayList <String> B)
    {
        ArrayList<String> A = new ArrayList<String>();
        
        if(B.isEmpty())
        {
        	for(int i = 0 ; i < X.size(); i = i + 1)
        		A.add(X.get(i));
        }
        else
        {
        	for(int i = 0 ; i < X.size(); i = i + 1)
        		if(!searchRecursive(B, X.get(i), 0, B.size()-1))
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
    private boolean searchRecursive(ArrayList <String> A, String k, int sx, int dx)
    {
    	if (sx == dx)
    	{
    		if (k.equals(A.get(sx))) return true; 
    		
    		else return false;
    	}
    	
    	int cx = (sx+dx)/2; 
    	
    	if (k.compareTo(A.get(cx)) <= 0) return searchRecursive(A, k, sx, cx); 
    	
    	else return searchRecursive(A, k, cx + 1, dx);
    }
    
    private void createJCommonalities()
    {
    	int j = 1;
    	
    	String s = "";
    			
    	for(int i = 0; i < Cr.size(); i++)
    	{
    		s = s + String.valueOf(j) + ":" + Cr.get(i) + "\n";
  			j = j + 1;
   		}
    			
    	s = s + "\n";
    			
    	for(int i = 0; i < Cd.size(); i++)
    	{
    		s = s + String.valueOf(j) + ":" + Cd.get(i) + "\n";
    		j = j + 1;
    	}
    			
    	s = s + "\n";
    			
    	for(int i = 0; i < Cs.size(); i++)
    	{
    		s = s + String.valueOf(j) + ":" + Cs.get(i) + "\n";
    		j = j + 1;
    	}
    			
    	jCommonalities = new JTextArea(s);
    }
    
    private void createJVariabilities()
    {
    	int j = 1;
    	
    	String s = "";
    			
    	for(int i = 0; i < Vr.size(); i++)
    	{
    		s = s + String.valueOf(j) + ":" + Vr.get(i) + "\n";
  			j = j + 1;
   		}
    			
    	s = s + "\n";
    			
    	for(int i = 0; i < Vd.size(); i++)
    	{
    		s = s + String.valueOf(j) + ":" + Vd.get(i) + "\n";
    		j = j + 1;
    	}
    			
    	s = s + "\n";
    			
    	for(int i = 0; i < Vs.size(); i++)
    	{
    		s = s + String.valueOf(j) + ":" + Vs.get(i) + "\n";
    		j = j + 1;
    	}
    			
    	jVariabilities = new JTextArea(s);
    }
}