
/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelAnalysis extends ModelParserUTF8
{
	private static boolean verbose=true;//variabile usata per attivare stampe nel codice

	/* URL da cui ottenere le analisi del file */
	public final String URL_ANALYSIS = "http://www.ilc.cnr.it/dylanlab/index.php?page=texttools&hl=en_US&showtemplate=false";
	
	/* ArrayList contenente i termini rilevanti del file */
	private ArrayList <String> termRelevant = null;
	
	/* ArrayList contenente le path delle pagine Html */
    private ArrayList <String> pathPageHTML = null;
	
	/** Costruttore
	 * 
	 * @param pathFile
	 * @param pathProject
	 */
	public ModelAnalysis(String pathFile, String pathProject)
	{
		super(pathFile, pathProject);
	}
	
	/** Carica analisi del file
	 * 
	 * @return true caricamento dell'analisi del file � avvenuta in maniera corretta
	 * @return false se si � verificato un errore 
	 */
	public boolean loadAnalysisFile()
	{
		termRelevant = new ArrayList <String> ();
		
		pathPageHTML = new ArrayList <String> ();
		
		/* Inserisce le path Html */
		for(int i = 0; i < 4; i++)
			pathPageHTML.add(
					new String(
							readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + i +".html"));
		
		try
        {
            String s;

            BufferedReader br = 
            		new BufferedReader(
            				new FileReader(
            						readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + ".log"));

            while( (s = br.readLine()) != null )
                if(!s.equals("") && !s.equals("\n") && !s.equals(" "))
                	termRelevant.add(s);

            br.close(); 
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("Exception LoadAnalysis: " + ex.getMessage());
            return false;
        }
        catch (IOException ex)
        {
            System.out.println("Exception LoadAnalysis: " + ex.getMessage());
            return false;
        }        
        Collections.sort(termRelevant);	
		return true;
	}
	
	/** Effettua analisi del file
	 * 
	 * @return true l'analisi del file � avvenuta in maniera corretta
	 * @return false se si � verificato un errore
	 */
	public boolean runAnalysisFile()
	{
		int i = 0;

        ArrayList <String> al = null;

        pathPageHTML = new ArrayList <String> ();
		
        if(filterFile() == null)
        	return false;
        
        if(((al = (ArrayList <String>) connectAnalysis()))==null)
            return false;
        
        try
        {
            while(i < al.size())
            {
                String s1 = al.get(i), s2 = "", s3 = null, s4 = null;

                URL url = new URL(s1);

                HttpURLConnection huc = 
                		(HttpURLConnection)
                        		url.openConnection();
                huc.setRequestMethod("GET");
                huc.setDoOutput(true);
                huc.setDoInput(true);
							
                DataOutputStream dot =
                		new DataOutputStream(
                				huc.getOutputStream());
                dot.close();

                BufferedReader br =
                		new BufferedReader(
                				new InputStreamReader(huc.getInputStream()));

                while ((s3 = br.readLine()) != null)
                    s2 = s2 + s3;

                br.close();

                if(!checkAnalysis(s2))
                {
                    huc.disconnect();    
                    Thread.sleep(10000);
                    continue;
                }
                
                if((s4 = cleanHTML(s2))!= null)
                {
                    /* Crea pagine html contenenti le varie analisi */
                    PrintWriter pw = 
                    		new PrintWriter(
                            		new BufferedWriter(
                            				new FileWriter(
                            						readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + i +".html", false)));
                    pw.print(s4);
                    pw.close();   
							
                    /* Aggiunge le path delle pagine html */
                    pathPageHTML.add(
                    		readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + i +".html");   

                    if(i == 2)
                    {
                        File f = 
                        		new File(
                        				readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + i +".html");

                        if(!extractTermRelevant(f))
                            return false;
                    }
                }
                else
                    return false;

                huc.disconnect();
                i = i + 1;
            }
            return true;
        }
        catch (InterruptedException ex)
        {
            System.out.println("Exception RunAnalysis: " + ex.getMessage());
            return false;
        }
        catch (MalformedURLException ex)
        {
            System.out.println("Exception RunAnalysis: " + ex.getMessage());
            return false;
        }
        catch (UnsupportedEncodingException ex)
        {
            System.out.println("Exception RunAnalysis: " + ex.getMessage());
            return false;
        }
        catch (IOException ex)
        {
            System.out.println("Exception RunAnalysis: " + ex.getMessage());
            return false;
        }
	}
	
	/* -= FUNZIONI lettura parametri =- */
	
	public ArrayList <String> readPathFileHTML()
	{
		return pathPageHTML;
	}
	
	public ArrayList <String> readTermRelevant()
	{
		return termRelevant;
	}
	
	/* -= FUNZIONI Ausiliarie =- */

    /** Si connette al sito URL_ANALYSIS per effettuare l'analisi del file
     *	
     * @return url ArrayList contenenti i riferimenti alle varie analisi     
	 * @return null se si � verificato un errore
     */
	private ArrayList <String> connectAnalysis()
	{
		String s1 = " ", s2 = null, s3 = null;

        ArrayList <String> al = new ArrayList<String>();

        try
        {
            String query = "input_text=" + URLEncoder.encode(readTextUTF8(), "UTF-8");

            URL url = new URL(URL_ANALYSIS);

            HttpURLConnection huc =
            		(HttpURLConnection)
                    		url.openConnection();
            huc.setRequestMethod("POST");
            huc.setRequestProperty("Content-lenght", String.valueOf(query.length()));
            huc.setDoOutput(true);
            huc.setDoInput(true);
            
            DataOutputStream dos =
            		new DataOutputStream(
            				huc.getOutputStream());
            dos.writeBytes(query);
            dos.close();

            BufferedReader br =
            		new BufferedReader(
            				new InputStreamReader(huc.getInputStream()));

            while ((s2 = br.readLine()) != null)
                s1 = s1 + s2;

            br.close();

            if((s3 = returnJid(s1)) == null)
            {
                System.out.println("Error not find yid");
                return null;
            }

    		/* ***VERBOSE****/
            if (verbose){
            	System.out.println("s1:\n"+s1);
            	System.out.println("s3:\n"+s3);
            	System.out.flush();
            }
            /* ***VERBOSE****/            
            
            al.add((URL_ANALYSIS + s3));
            al.add((URL_ANALYSIS + s3 + "&tmid=tm_sentence_splitter"));
            al.add((URL_ANALYSIS + s3 + "&tmid=tm_term_extractor"));
            al.add((URL_ANALYSIS + s3 + "&tmid=tm_parser"));
            huc.disconnect();

            return al;
        }
        catch (MalformedURLException ex)
        {
            System.out.println("Exception ConnectionAnalysis: " + ex.getMessage());
            return null;
        }
        catch (UnsupportedEncodingException ex)
        {
            System.out.println("Exception ConnectionAnalysis: " + ex.getMessage());
            return null;
        }
        catch (IOException ex)
        {
            System.out.println("Exception ConnectionAnalysis: " + ex.getMessage());
            return null;
        }

	}
	
	/** Restituisce il jid assegnatoci dal sito URL_ANALYSIS
	 * 
	 * @param s stringa inviatoci dal sito
	 * 
	 * @return jid stringa contenente il jid assegnatoci
	 * @return null jid non assegnato
	 */
	private String returnJid(String s)
	{
		int i;

		/* ***VERBOSE****/
		if (verbose){
			System.out.println("ModelAnalysis.returnJid(): String s= "+s);
			System.out.flush();
		}
		/* ***VERBOSE****/

		
		String jid = "&";

        if(s == null || s.equals(""))
           return null;

        if((i = s.indexOf("jid=")) == -1)
           return null;

        while(s.charAt(i) != '&')
        {
            jid = jid + String.valueOf(s.charAt(i));
            i = i + 1;
        }
        return jid;
	}
	
	/** Controlla se l'analisi del testo � stata completata
	 * 
	 * @param s stringa da cui verificare se l'analisi � stata completata 
	 * 
	 * @return true analisi completata
	 * @return false analisi non completata
	 */
	private boolean checkAnalysis(String s)
	{
		if(s == null || s.equals(""))
            return false;

		/* 
		   Se � presente il campo "<meta HTTP-EQUIV" 
		   l'analisi � terminata 
		 */
         if(s.indexOf("<meta HTTP-EQUIV") != -1)
            return false;
         else
            return true;
	}
	
	/** Pulisce pagina html
	 * 
	 * @param s Stringa contenente la pagina html da ripulire
	 * 
	 * @return p stringa pulita
	 * @reutn null se si � verificato un errore
	 */
	private String cleanHTML(String s)
	{
		int i1, i2, i3, i4;

        if(s == null)
        {
            System.out.println("Error not find page html");
            return null;
        }

        i1 = s.indexOf("<table");
        i2 = s.indexOf("<center>");
        i3 = s.indexOf("</center>") + 9;
        i4 = s.indexOf("</p>");

        return s.substring(0, i1) + s.substring(i2, i3) + s.substring(i4, s.length());
	}
	
	/** Estrae i termini rilevanti
	 * 
	 * @param f file contenente i termini rilevanti
	 * 
	 * @return true termini rilevanti estratti
	 * @return false se si � verificato un errore
	 */
	private boolean extractTermRelevant(File f)
	{
		int i = 2;

        termRelevant = new ArrayList <String> ();

        Pattern es1 = Pattern.compile("<[^<]+?>");
        Pattern es2 = Pattern.compile("\\s[\\s]+");
        Pattern es3 = Pattern.compile("\\s[0-9]+\\s");
        Pattern es4 = Pattern.compile("\\s[0-9.]+%\\s");
        Pattern es5 = Pattern.compile("\\s[I][D]\\s");

        Matcher m;

        String s1, s2 = "";
        String [] s3, s4 = {"relevant multiple terms (general purpose relevance) relevance",
                             "domain-specific multiple terms (text-dependent relevance) relevance",
                             "single terms relevance"};

        try
        {
            BufferedReader reader =
            		new BufferedReader(
            				new FileReader(f.getPath()));

            while( (s1 = reader.readLine()) != null )
                s2 = s2 + s1;

            reader.close();

            m = es1.matcher(s2);
            s2 = m.replaceAll("\n");
            /* */
            m = es2.matcher(s2);
            s2 = m.replaceAll(" ");
            /* */
            m = es3.matcher(s2);
            s2 = m.replaceAll(" %% ");
            /* */
            m = es4.matcher(s2);
            s2 = m.replaceAll(" ");
            /* */
            m = es5.matcher(s2);
            s2 = m.replaceAll("%%");
            /* */
            s3 = s2.split("%%");

            while(i < s3.length)
            {
            	//cos'è ciò?
                if(!s3[i].equals("") && !s3[i].equals("\n") && !s3[i].equals(" ") && !s3[i].equals(" ")
                   && !s3[i].trim().equals(s4[0]) && !s3[i].trim().equals(s4[1]) && !s3[i].trim().equals(s4[2]) )
                  if (!termRelevant.contains(s3[i].trim()) ) termRelevant.add(s3[i].trim());
                    
                i = i + 1;
            }
            
            Collections.sort(termRelevant);
            
            
            PrintWriter writer =
                    new PrintWriter(
                    		new BufferedWriter(
                    				new FileWriter(readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + ".log")));

            for(int j = 0; j< termRelevant.size(); j++)
            	writer.print(termRelevant.get(j) + "\n");

            writer.close();

            return true;
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Exception extractTermRelevant: " + e.getMessage());
            return false;
        }
        catch (IOException e)
        {
            System.out.println("Exception extractTermRelevant: " + e.getMessage());
            return false;
        }
    }
}
