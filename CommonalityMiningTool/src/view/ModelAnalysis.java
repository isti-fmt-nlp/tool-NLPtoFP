
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
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelAnalysis extends ModelParserUTF8
{
	private static boolean debug=true;//variabile usata per attivare stampe nel codice

	/* URL da cui ottenere le analisi del file */
//	public final String URL_ANALYSIS = "http://www.ilc.cnr.it/dylanlab/index.php?page=texttools&hl=en_US&showtemplate=false";
//    									http://www.ilc.cnr.it/dylanlab/apps/texttools/?tt_lang=it&tt_tmid=tm_sentencesplitter
//										http://www.ilc.cnr.it/dylanlab/apps/texttools/?tt_lang=it&tt_tmid=tm_sentencesplitter&tt_jid=1390844913949403_it

	public final String URL_ANALYSIS = "http://www.ilc.cnr.it/dylanlab/apps/texttools/?tt_user=guest"; //---TRY1
	
	public final String URL_SENTENCE_SPLITTER_PART = "&tt_tmid=tm_sentencesplitter";
	public final String URL_PARSER_PART = "&tt_tmid=tm_parser";
	public final String URL_TOKENIZER_PART = "&tt_tmid=tm_tokenizer";

	//currently a work in progress
	public final String URL_TERM_EXTRACTOR_PART = "&tt_tmid=tm_term_extractor";
	
	
	/** ArrayList containing the relevant tems of input file */
	private ArrayList <String> termRelevant = null;
	
	/** ArrayList containing the paths to html analysis result files.*/
    private ArrayList <String> pathPageHTML = null;
	
	/** Costruttore
	 * 
	 * @param pathFile
	 * @param pathProject
	 */
	public ModelAnalysis(String pathFile, String pathProject){
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
	
	/** Analyzes the input file.
	 * 
	 * @return true if analysis completed successfully, false if an error occurred
	 */
	public boolean runAnalysisFile(){
		String jid=null;
        String pathPrefix = readPathFileUTF8().substring(0, readPathFileUTF8().length()-4);
        pathPageHTML = new ArrayList <String> ();
		
        if(filterFile() == null) return false;
        if((jid = connectAnalysis())==null) return false;
        
        try{
            //saving result html pages as project files
        	if (!saveResultPage(URL_ANALYSIS+jid, pathPrefix+0+".html")) return false;
        	if (!saveResultPage(URL_ANALYSIS+jid+URL_SENTENCE_SPLITTER_PART, pathPrefix+1+".html")) return false;
        	if (!saveResultPage(URL_ANALYSIS+jid+URL_TOKENIZER_PART, pathPrefix+2+".html")) return false;
        	if (!saveResultPage(URL_ANALYSIS+jid+URL_PARSER_PART, pathPrefix+3+".html")) return false;
        	//extracting relevant terms of this input file
        	if(!extractTermRelevant(new File(pathPrefix+2+".html"))) return false;
            return true;
        }
        catch (Exception ex){
            System.out.println("Exception RunAnalysis: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
	}

	/**
	/** Download an analysis result html page and saves it as a project file.
	 * 
	 * @param stringURL - a String representing the URL of html page to download
	 * @param path - the path in which to save the html page
	 * @return true if the file has been saved, false if the analysis was not completed yet
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ProtocolException
	 * @throws InterruptedException
	 */
	private boolean saveResultPage(String stringURL, String path)
			throws MalformedURLException, IOException, ProtocolException, InterruptedException {
		String s2 = "", s3 = null, s4 = null;

		//getting HTML result file
		URL url = new URL(stringURL);                		
		HttpURLConnection huc = (HttpURLConnection)url.openConnection();
		huc.setRequestMethod("GET");
		huc.setDoOutput(true);
		huc.setDoInput(true);
					
		DataOutputStream dot = new DataOutputStream(huc.getOutputStream());
		dot.close();

		BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));

		while ((s3 = br.readLine()) != null) s2 = s2 + s3;
		
		/* ***DEBUG*** */
		if (debug) System.out.println("Analyzing file "+stringURL+":\n"+s2);
		/* ***DEBUG*** */

		br.close();

		if(s2==""){ huc.disconnect(); return false;}
		
//		System.out.println("checkAnalysis(s2)="+checkAnalysis(s2));
//		if(!checkAnalysis(s2)){
//		    huc.disconnect();    
//		    Thread.sleep(10000);
//		    return false;
//		}
		
		//cleaning HTML result file
		if(stringURL.contains(URL_SENTENCE_SPLITTER_PART)) s4 = cleanSentenceSplitterHTML(s2);
		else if(stringURL.contains(URL_TOKENIZER_PART)) s4 = cleanSentenceSplitterHTML(s2);
		else if(stringURL.contains(URL_PARSER_PART)) s4 = cleanSentenceSplitterHTML(s2);
		else s4 = cleanAnalysisTextHTML(s2);

		System.out.println("s2=\n"+s2);
		System.out.println("Html cleaned=\n"+s4);

		if(s4!= null){
		    /* Create project file containing the html result page */
		    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
		    pw.print(s4);
		    pw.close();   
					
		    /* Adding file path of html result page */
		    pathPageHTML.add(path);   

		}
		else return false;

		huc.disconnect();
		return true;
	}
	
	/* -= FUNZIONI lettura parametri =- */
	/**
	 * Returns an ArrayList<String> containing the paths to html analysis result files.
	 * 
	 * @return the ArrayList<String> containing the paths
	 */
	public ArrayList <String> readPathFileHTML()
	{
		return pathPageHTML;
	}
	
	/**
	 * Returns an ArrayList<String> containing the relevant terms to html analysis result files.
	 * 
	 * @return the ArrayList<String> containing the paths
	 */
	public ArrayList<String> readTermRelevant()
	{
		return termRelevant;
	}
	
	/* -= FUNZIONI Ausiliarie =- */

    /** 
     * Asks URL_ANALYSIS web site to analyze the file.
     *	
     * @return jid - a String representing the jabber ID assigned by the server, or null if an errore occurred
     */
	private String connectAnalysis()
	{
		String s1 = " ", s2 = null, jid = null;

        ArrayList <String> al = new ArrayList<String>();

        try{
            String query = /*"input_text=" +*/ 
            		"tt_text=" 
            		+ URLEncoder.encode(readTextUTF8()
            		, "UTF-8");

            query+="&tt_textlang=it&tt_settext=Avvia+l%27analisi+del+testo...";
            
//            query ="tt_text=pomodoro&tt_textlang=it&tt_settext=Avvia+l%27analisi+del+testo..."; 

            URL url = new URL(URL_ANALYSIS);

            HttpURLConnection huc =
            		(HttpURLConnection)
                    		url.openConnection();
            huc.setRequestMethod("POST");
            huc.setRequestProperty("REFERER", "http://www.ilc.cnr.it/dylanlab/apps/texttools/");
            huc.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0");
            huc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            huc.setRequestProperty("Accept-Encoding","gzip, deflate");
            huc.setRequestProperty("Content-lenght", String.valueOf(query.length()));
            huc.setDoOutput(true);
            huc.setDoInput(true);
            
            DataOutputStream dos =
            		new DataOutputStream(
            				huc.getOutputStream());
            dos.writeBytes(query);
            dos.close();

    		/* ***VERBOSE****/
            if (debug){
            	System.out.println("huc.getURL():\n"+huc.getURL());
            	System.out.println("huc.toString():\n"+huc.toString());
            	System.out.println("huc.getContent():\n"+huc.getContent());
            	System.out.println("query:\n"+query);
            	System.out.flush();
            }
            /* ***VERBOSE****/   
            
            BufferedReader br =
            		new BufferedReader(
            				new InputStreamReader(huc.getInputStream()));

            while ((s2 = br.readLine()) != null)
                s1 = s1 + s2;

            br.close();

            if((jid = returnJid(s1)) == null){
                System.out.println("Error not find jid");
                return null;
            }

    		/* ***VERBOSE****/
            if (debug){
            	System.out.println("s1:\n"+s1);
            	System.out.println("s3:\n"+jid);
            	System.out.flush();
            }
            /* ***VERBOSE****/            
            
//            http://www.ilc.cnr.it/dylanlab/apps/texttools/?tt_lang=it&tt_tmid=tm_sentencesplitter
//            al.add((URL_ANALYSIS + s3));
//            al.add((URL_ANALYSIS + s3 + "&tt_tmid=tm_sentence_splitter"));
//            al.add((URL_ANALYSIS + s3 + "&tt_tmid=tm_term_extractor"));
//            al.add((URL_ANALYSIS + s3 + "&tt_tmid=tm_parser"));
            huc.disconnect();

            return jid;
        }
        catch (MalformedURLException ex)
        {
            System.out.println("Exception ConnectionAnalysis_1: " + ex.getMessage());
            ex.printStackTrace(); return null;
        }
        catch (UnsupportedEncodingException ex)
        {
            System.out.println("Exception ConnectionAnalysis_2: " + ex.getMessage());
            ex.printStackTrace(); return null;
        }
        catch (IOException ex)
        {
            System.out.println("Exception ConnectionAnalysis_3: " + ex.getMessage());
            ex.printStackTrace(); return null;
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
		if (debug){
			System.out.println("ModelAnalysis.returnJid(): String s= "+s);
			System.out.flush();
		}
		/* ***VERBOSE****/

		
		String jid = "&";

        if(s == null || s.equals(""))
           return null;

        if((i = s.indexOf("tt_jid=")) == -1)
           return null;

//        while(s.charAt(i) != '&')
        while(s.charAt(i) != '\"' && s.charAt(i) != '&'){
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
	
	/** 
	 * Cleans html page containing the text to analyze.
	 * 
	 * @param s - String containing the html page 
	 * @return - the cleaned html as a String, or null if an error occurred
	 */
	private String cleanAnalysisTextHTML(String s){
		int i1, i2, i3, i4;
		char c=0;
        if(s == null){
            System.out.println("Error not find page html");
            return null;
        }

        i1 = s.indexOf("<table");
        
        i2 = s.indexOf("<textarea")+9;
        while((c=s.charAt(i2))!='>') ++i2;
        ++i2;
        
        i3 = s.indexOf("</textarea>");
        
//        i2 = s.indexOf("<center>");
//        i3 = s.indexOf("</center>") + 9;
        i4 = s.indexOf("</p>");
        
        System.out.println("Indici della clean:\ni1="+i1+"\ti2="+i2+"\ti3="+i3+"\ti4="+i4);
        
//        return s.substring(0, i1) + s.substring(i2, i3) + s.substring(i4, s.length());
        return s.substring(0, i1) + s.substring(i2, i3) + "</body></html>";
	}
	
	/** 
	 * Cleans html page containing the result of sentence splitting.
	 * 
	 * @param s - String containing the html page 
	 * @return - the cleaned html as a String, or null if an error occurred
	 */
	private String cleanSentenceSplitterHTML(String s)
	{
		int headBeforeMetaAttr, styleStartIndex;
		int i1, i2, i3;
		char c=0;
        if(s == null){
            System.out.println("Error not find page html");
            return null;
        }

        
        headBeforeMetaAttr = s.indexOf("</title>")+8;
        
        styleStartIndex = s.indexOf("<style");

        i1 = s.indexOf("<table");
        
        i2 = s.indexOf("Proiezione della leggibilità sul testo");
        i2 += s.substring(i2).indexOf("<table");
        
        i3 = i2+s.substring(i2).indexOf("</table>")+8;
        
        System.out.println("Indici della clean:\ni1="+i1+"\ti2="+i2+"\ti3="+i3);
        
//        return s;
//        return s.substring(0, i1) + s.substring(i2, i3) + "</div></div></body></html>";
        return s.substring(0, headBeforeMetaAttr) + s.substring(styleStartIndex, i1) 
        	 + s.substring(i2, i3) + "</div></div></body></html>";
	}
	
	/** Estrae i termini rilevanti
	 * 
	 * @param f file contenente i termini rilevanti
	 * 
	 * @return true termini rilevanti estratti
	 * @return false se si � verificato un errore
	 */
	private boolean extractTermRelevant(File f){
        termRelevant = new ArrayList <String> ();
        boolean extracted=false;
        
//        //extracting relevant terms
//        extracted=extractTerms(f);
//        if(!extracted) return false;        
        
        //extracting relevant terms
        extracted=extractTerms2(f);
        if(!extracted) return false;        
        
        try{
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
            System.out.println("extractTermRelevant - Exception during write: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            System.out.println("extractTermRelevant - Exception during write: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
		
	/**
	 * Extract relevant terms from File f.
	 * 
	 * @param f - the file containing the relevant terms
	 * @return - true if extraction was successful, false otherwise
	 */
	private boolean extractTerms(File f){

        int i = 2;

        Pattern es1 = Pattern.compile("<[^<]+?>");
        Pattern es2 = Pattern.compile("\\s[\\s]+");
        Pattern es3 = Pattern.compile("\\s[0-9]+\\s");
        Pattern es4 = Pattern.compile("\\s[0-9.]+%\\s");
        Pattern es5 = Pattern.compile("\\s[I][D]\\s");

        Matcher m=null;

        String s1, s2 = "";
        String [] s3, s4 = {"relevant multiple terms (general purpose relevance) relevance",
                             "domain-specific multiple terms (text-dependent relevance) relevance",
                             "single terms relevance"};

        try{
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

            while(i < s3.length){
            	//cos'è ciò?
                if(!s3[i].equals("") && !s3[i].equals("\n") && !s3[i].equals(" ") && !s3[i].equals(" ")
                   && !s3[i].trim().equals(s4[0]) && !s3[i].trim().equals(s4[1]) && !s3[i].trim().equals(s4[2]) )
                  if (!termRelevant.contains(s3[i].trim()) ) termRelevant.add(s3[i].trim());
                    
                i = i + 1;
            }
        }
        catch(FileNotFoundException e){
            System.out.println("extractTerms - Exception during read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        catch (IOException e){
            System.out.println("extractTerms - Exception during read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }		
        return true;
	}
	
	/**
	 * Extract relevant terms from File f.
	 * 
	 * @param f - the file containing the relevant terms
	 * @return - true if extraction was successful, false otherwise
	 */
	private boolean extractTerms2(File f){
		
		String html="", s1=null;
		int startIndex=0, endIndex=0;
		
        try{
          BufferedReader reader = new BufferedReader(new FileReader(f.getPath()));
          while( (s1 = reader.readLine()) != null ) html += s1;
          reader.close();

          //skipping table header
          startIndex=html.indexOf("<table");
          for(int i=0; i<4; ++i){
        	if(startIndex==-1) return false;
        	else startIndex=html.indexOf("<td", startIndex+1);
          }
          if(startIndex==-1) return false;
            
          //adding terms
          while(startIndex!=-1 && startIndex<html.length()){
            //getting start index of a term
            for(int i=0; i<3; ++i){
              startIndex=html.indexOf("<td", startIndex+1);
              if(startIndex==-1) return true;
            }
            startIndex=html.indexOf(">", startIndex);
            if(startIndex==-1) return true;
            else startIndex+=1;

            //at each change of SID, we must skip 2 more <td> tags
            if(html.startsWith("<b>", startIndex)){
              for(int i=0; i<2; ++i){
                startIndex=html.indexOf("<td", startIndex+1);
                if(startIndex==-1) return true;
              }
              startIndex=html.indexOf(">", startIndex);
              if(startIndex==-1) return true;
              else startIndex+=1;
            }
            
            //getting end index of a term
            endIndex=html.indexOf("</td>", startIndex);
            if(endIndex==-1) return true;
            
            System.out.println("Extracted term: "+html.substring(startIndex, endIndex));
            if(!termRelevant.contains(html.substring(startIndex, endIndex))) termRelevant.add(html.substring(startIndex, endIndex));
          }

        }catch(Exception e){
            System.out.println("extractTerms2 - Exception during read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
		return true;
	}

}