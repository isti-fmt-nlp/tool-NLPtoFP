
/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.awt.Point;
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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.IIOException;

public class ModelAnalysis extends ModelParserUTF8{
	private static boolean debug=false;//variable used to activate prints in the code
	
	private static boolean debug2=false;//variable used to activate prints in the code
	
	/** If true, the relevant terms file will be created after analisys, otherwise it will be created after save*/
	private static final boolean SAVE_RELEVANT_TERMS_IMMEDIATELY = true;

	/* URL da cui ottenere le analisi del file */
//	public final String URL_ANALYSIS = "http://www.ilc.cnr.it/dylanlab/index.php?page=texttools&hl=en_US&showtemplate=false";
//    									http://www.ilc.cnr.it/dylanlab/apps/texttools/?tt_lang=it&tt_tmid=tm_sentencesplitter
//										http://www.ilc.cnr.it/dylanlab/apps/texttools/?tt_lang=it&tt_tmid=tm_sentencesplitter&tt_jid=1390844913949403_it

	public static final String URL_ANALYSIS = "http://www.ilc.cnr.it/dylanlab/apps/texttools/?tt_user=guest"; //---TRY1
	
	public static final String URL_SENTENCE_SPLITTER_PART = "&tt_tmid=tm_sentencesplitter";
	public static final String URL_PARSER_PART = "&tt_tmid=tm_parser";
	public static final String URL_TOKENIZER_PART = "&tt_tmid=tm_tokenizer";

	//currently a work in progress
	public final String URL_TERM_EXTRACTOR_PART = "&tt_tmid=tm_term_extractor";
	
	public static final String SENTENCE_PREFIX="*** + + SENTENCE#";
	public static final String SENTENCE_SUFFIX=" + + ***" + "\n";
	
	
	/** ArrayList containing the relevant terms of input file */
	private HashMap<String, ArrayList<String>> termRelevant = null;
//	/** ArrayList containing the relevant terms of input file */
//	private ArrayList <String[]> termRelevant = null;
		
	/** Contains the arity for all terms of the input file */
	private HashMap<String, Integer> termsArity=null;

	/** Contains a terms set for each sentence of the input file */
	private ArrayList<ArrayList<String>> termsInSentencesSet=null;
	
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
	
	/**
	 * Loads analisys data for the already analyzed input file 
	 * 
	 * @return true if loading has been successful, false otherwise
	 */
	public boolean loadAnalysisFile(){
      String s=null;
      String[]splitted=null;
        
//      termRelevant = new ArrayList <String[]> ();		
      termRelevant = new HashMap<String, ArrayList<String>>();
      pathPageHTML = new ArrayList <String> ();
		
      
//		//this model represents an analisys directory, analisys is already done
//		if (isAnalisysDir){ result=true; return;}

      
      // inserting html path 
      for(int i = 0; i < 4; i++)
    	pathPageHTML.add(new String(readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + i +".html"));
		
      //loading relevant terms
      try{
    	BufferedReader br = 
    	  new BufferedReader(new FileReader(readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + ".log"));

//    	while( (s = br.readLine()) != null )
//      	  if(!s.equals("") && !s.equals("\n") && !s.equals(" ")) termRelevant.add(s);
    	while( (s = br.readLine()) != null )
          if(!s.equals("") && !s.equals("\n") && !s.equals(" ")){        	  
//          	termRelevant.add(s.split("\t"));    	
        	splitted=s.split("\t");
        	  
        	//initializing termRelevant, if necessary
        	if(termRelevant.get(splitted[0])==null) termRelevant.put(splitted[0], new ArrayList<String>());
        	for(int i=1; i<splitted.length; ++i) termRelevant.get(splitted[0]).add(splitted[i]);
        	  
          }

    	br.close(); 
      }
      catch (FileNotFoundException ex){
    	System.out.println("Exception LoadAnalysis: " + ex.getMessage());
    	return false;
      }catch (IOException ex){
    	System.out.println("Exception LoadAnalysis: " + ex.getMessage());
    	return false;
      }        
      
//      Collections.sort(termRelevant);	
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
        termRelevant = new HashMap<String, ArrayList<String>>();
		
        if(filterFile() == null) return false;
        if((jid = connectAnalysis())==null) return false;
        
        try{
            //saving result html pages as project files
        	if (!saveResultPage(URL_ANALYSIS+jid, pathPrefix+0+".html")) return false;
        	if (!saveResultPage(URL_ANALYSIS+jid+URL_SENTENCE_SPLITTER_PART, pathPrefix+1+".html")) return false;
        	if (!saveResultPage(URL_ANALYSIS+jid+URL_TOKENIZER_PART, pathPrefix+2+".html")) return false;
//        	if (!saveResultPage(URL_ANALYSIS+jid+URL_PARSER_PART, pathPrefix+3+".html")) return false;
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
	 * @return - true if the file has been saved, false if the analysis was not completed yet
	 * 
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

		if(debug) System.out.println("s2=\n"+s2+"\nHtml cleaned=\n"+s4);

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
	 * @return - the ArrayList<String> containing the paths
	 */
	public ArrayList <String> readPathFileHTML(){
		return pathPageHTML;
	}
	
	/**
	 * Sets the ArrayList<String> containing the paths to html analysis result files.
	 * 
	 * @param al - the ArrayList<String> containing the paths
	 */
	public void setPathFileHTML(ArrayList<String> al){
		pathPageHTML=al;
	}
	
	/**
	 * Returns a String representing the path to the file containing the relevant terms sets, <br>
	 * one set per sentence in the input file.
	 * 
	 * @return - a String representing the path to the file containing the relevant terms sets
	 */
	public String readPathFileSets(){
		return readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + "SETS.log";
	}
	
	/**
	 * Returns a String representing the path to the file containing the relevant terms arities.
	 * 
	 * @return - a String representing the path to the file containing the relevant terms arities
	 */
	public String readPathFileArities(){
		return readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + "ARITY.log";
	}
	
	/**
	 * Returns an HashMap<String, ArrayList<String>> containing the versions of this file's relevant terms.
	 * 
	 * @return - the HashMap<String, ArrayList<String>> containing the paths
	 */
	public HashMap<String, ArrayList<String>> readTermRelevant(){
		return termRelevant;
	}
	
	/**
	 * Returns an ArrayList&ltArrayList&ltString>> containing the sets of relevant terms, one per sentence.
	 * 
	 * @return the ArrayList&ltString> containing the paths
	 */
	public ArrayList<ArrayList<String>> getTermsInSentencesSet(){
		return termsInSentencesSet;
	}
	
	/**
	 * Sets an ArrayList<ArrayList<String>> containing the sets of relevant terms, one per sentence.
	 */
	public void setTermsInSentencesSet(ArrayList<ArrayList<String>> termsInSentencesSet){
		this.termsInSentencesSet=termsInSentencesSet;
	}
	
	/**
	 * Returns an HashMap<String, Integer> containing the arities of relevant terms, <br>
	 * the arity of a term is the number of different sentences in which occurs.
	 * 
	 * @return - the HashMap<String, Integer> containing the arities of this model relevant terms
	 */
	public HashMap<String, Integer> getTermsAriety(){
		return termsArity;
	}
	
	
	/* -= FUNZIONI Ausiliarie =- */

    /** 
     * Asks URL_ANALYSIS web site to analyze the file.
     *	
     * @return jid - a String representing the jabber ID assigned by the server, or null if an errore occurred
     */
	private String connectAnalysis(){
		String s1 = " ", s2 = null, jid = null;

        ArrayList <String> al = new ArrayList<String>();

        try{
            String query = /*"input_text=" +*/ "tt_text="+URLEncoder.encode(readTextUTF8(), "UTF-8");

            query+="&tt_textlang=it&tt_settext=Avvia+l%27analisi+del+testo...";
            
//            query ="tt_text=pomodoro&tt_textlang=it&tt_settext=Avvia+l%27analisi+del+testo..."; 

            URL url = new URL(URL_ANALYSIS);

            HttpURLConnection huc = (HttpURLConnection)url.openConnection();
            huc.setRequestMethod("POST");
            huc.setRequestProperty("REFERER", "http://www.ilc.cnr.it/dylanlab/apps/texttools/");
            huc.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0");
            huc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            huc.setRequestProperty("Accept-Encoding","gzip, deflate");
            huc.setRequestProperty("Content-lenght", String.valueOf(query.length()));
            huc.setDoOutput(true);
            huc.setDoInput(true);
            
            DataOutputStream dos = new DataOutputStream(huc.getOutputStream());
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
        }catch (MalformedURLException ex){
            System.out.println("Exception ConnectionAnalysis_1: " + ex.getMessage());
            ex.printStackTrace(); return null;
        }catch (UnsupportedEncodingException ex){
            System.out.println("Exception ConnectionAnalysis_2: " + ex.getMessage());
            ex.printStackTrace(); return null;
        }catch (IOException ex){
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
        
        return s.substring(0, headBeforeMetaAttr) + s.substring(styleStartIndex, i1) 
        	 + s.substring(i2, i3) + "</div></div></body></html>";
	}
	
	/** 
	 * Extract relevant terms from File f.<br>
	 * (VERSION FOR THE TOKENIZER OF TextTools, NEW PROJECT OF DylanLab)
	 * 
	 * @param f - the file containing the relevant terms
	 * @return - true if extraction was successful, false otherwise
	 */
	private boolean extractTermRelevant(File f){
        boolean extracted=false;
        
//        //extracting relevant terms
//        extracted=extractTermsFrom_DylanLabTermExtractor(f);
//        if(!extracted) return false;        
        
        //extracting relevant terms
        extracted=extractTermsFrom_TextTools(f);
        
        //saving relevant terms on file, so sequent analisys operation can simply read from that
        if (SAVE_RELEVANT_TERMS_IMMEDIATELY) try {
			saveRelevantTerms();
		} catch (IOException e) {
		  System.out.println("Relevant terms file could not be saved");
		  e.printStackTrace();
		}
        return extracted;        
	}
		
	/**
	 * Extract relevant terms from File f.<br>
	 * (VERSION FOR THE OLD TERM EXTRACTOR OF DylanLab)
	 * 
	 * @param f - the file containing the relevant terms
	 * @return - true if extraction was successful, false otherwise
	 */
	private boolean extractTermsFrom_DylanLabTermExtractor(File f){
		String[] termVersions=null;
        int i = 2;

        Pattern es1 = Pattern.compile("<[^<]+?>");
        Pattern es2 = Pattern.compile("\\s[\\s]+");
        Pattern es3 = Pattern.compile("\\s[0-9]+\\s");
        Pattern es4 = Pattern.compile("\\s[0-9.]+%\\s");
        Pattern es5 = Pattern.compile("\\s[I][D]\\s");

        Matcher m=null;

        String s1, s2 = "";
        String[] s3, s4 = {"relevant multiple terms (general purpose relevance) relevance",
                             "domain-specific multiple terms (text-dependent relevance) relevance",
                             "single terms relevance"};

        try{
            BufferedReader reader = new BufferedReader(new FileReader(f.getPath()));

            while( (s1 = reader.readLine()) != null ) s2 = s2 + s1;

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
            	 && !s3[i].trim().equals(s4[0]) && !s3[i].trim().equals(s4[1]) && !s3[i].trim().equals(s4[2]) ){
//              if (!termRelevant.contains(s3[i].trim()) ){
//                termVersions=new String[2];
//                termVersions[0]=s3[i].trim();
//                termVersions[1]=s3[i].trim();
//                termRelevant.add(termVersions);
//              }
                    
            	//initializing termrelevant, if necessary
            	if (termRelevant.get(s3[i].trim())==null) termRelevant.put(s3[i].trim(), new ArrayList<String>());
                termRelevant.get(s3[i].trim()).add(s3[i].trim());
                	  
//                termVersions=new String[2];
//                termVersions[0]=s3[i].trim();
//                termVersions[1]=s3[i].trim();
//                termRelevant.add(termVersions);
              }
              
              i = i + 1;
                
            }
        }catch (IOException e){
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
	private boolean extractTermsFrom_TextTools(File f){
		String html="", s1=null;
		int startIndex=0, endIndex=0;
		
		int startSidIndex=0, endSidIndex=0;
		String sid=null, term=null; 
		String[] termVersions=null;
		ArrayList<String> termSet = new ArrayList<String>();

		termRelevant = new HashMap<String, ArrayList<String>>();
		termsInSentencesSet = new ArrayList<ArrayList<String>>();
		termsArity= new HashMap<String, Integer>();

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
              if(startIndex==-1){
            	termsInSentencesSet.add(termSet);
            	return true;
              }
            }
            startIndex=html.indexOf(">", startIndex);
            if(startIndex==-1){
              termsInSentencesSet.add(termSet);
              return true;
            }
            else startIndex+=1;

            //at each change of SID, we must skip 2 more <td> tags
            if(html.startsWith("<b>", startIndex)){
            	
              //changing the sentence set
              startSidIndex=startIndex+3;
              endSidIndex=html.indexOf("<", startSidIndex);
              sid=html.substring(startSidIndex, endSidIndex);
              System.out.println("Found a SID: "+sid);
              termsInSentencesSet.add(termSet);
              termSet= new ArrayList<String>();
              
              for(int i=0; i<2; ++i){
                startIndex=html.indexOf("<td", startIndex+1);
                if(startIndex==-1){
                  termsInSentencesSet.add(termSet);
                  return true;
                }
              }
              startIndex=html.indexOf(">", startIndex);
              if(startIndex==-1){
              	termsInSentencesSet.add(termSet);
              	return true;
              }
              else startIndex+=1;
            }
            
            //getting end index of a term
            endIndex=html.indexOf("</td>", startIndex);
            if(endIndex==-1){
              termsInSentencesSet.add(termSet);
              return true;
            }
            
            //adding relevant term
            term=html.substring(startIndex, endIndex);
            System.out.println("Extracted term: "+term);
            
        	//initializing termrelevant, if necessary
            if (termRelevant.get(term)==null) termRelevant.put(term, new ArrayList<String>());
            termRelevant.get(term).add(term);                                    
//            if(!termRelevant.contains(term)){
//              termVersions=new String[2];
//              termVersions[0]=term;
//              termVersions[1]=term;
//              termRelevant.add(termVersions);
//            }

            //adding term to sentence set and updating its arity
            if(!termSet.contains(term)){
              termSet.add(term);
              if(termsArity.get(term)==null) termsArity.put(term, 1);
              else termsArity.put(term, termsArity.get(term)+1);
            }
          }

        }catch(Exception e){
            System.out.println("extractTerms2 - Exception during read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        termsInSentencesSet.add(termSet);
        return true;
	}

	/**
	 * Creates the HTML analisys result file containing the input text, using the filePath as source. 
	 * 
	 * @param filePath - a String representing the path of input text file
	 */
	public void createResultFileInputText(String filePath) {
	  File tmp=null;
	  PrintWriter printer=null;
	  String fileContent="";
      String pathPrefix = readPathFileUTF8().substring(0, readPathFileUTF8().length()-4);
	  
	  //reading input text file content
      fileContent=readTextUTF8();
//	  tmp=new File(filePath);
//	  try {
//		reader=new BufferedReader(new FileReader(tmp));
//		while((line=reader.readLine())!=null) fileContent+=line+"\n";
//		reader.close();	  
//	  } catch (IOException e1) {
//		System.out.println("createResultFileInputText(): File Read Problem!");
//		e1.printStackTrace(); return;
//	  }
	  
	  //creating html content of the result file
	  fileContent=
	   "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
	  +"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
	  +"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"it\" lang=\"it\">"
	  +"<head><title>DyLan - TextTools</title></head><body>"
	  +fileContent
	  +"</body></html>";
	  
	  //printing html result file
	  try {
		tmp=new File(pathPrefix+0+".html");
		printer = new PrintWriter(tmp);
		printer.print(fileContent);
		printer.close();
	  } catch (IOException e1) {
		System.out.println("createResultFileInputText(): File Write Problem!");
		e1.printStackTrace(); return;
	  }
	  
	  pathPageHTML.add(pathPrefix+0+".html");

	}

	/**
	 * Creates the HTML analisys result file containing the post tagging, using filePath as source. 
	 * 
	 * @param filePath - a String representing the path of post tagging file
	 * @return - an ArrayList<Point>, representing start and end indexes of each sentence in the input file
	 */
	public ArrayList<Point> createResultFilePostTagging(String filePath) {
	  File tmp=null;
	  PrintWriter printer=null;
	  BufferedReader reader=null;		
	  String line=null;
	  String inputTextContent=""/*, sentenceBoundaries=""*/;
      String pathPrefix = readPathFileUTF8().substring(0, readPathFileUTF8().length()-4);
      int sentencesCount=0;
      int inputTextIndex=0;
      String[] elementData=null;
      String currentToken=null, previousToken=null;
      ArrayList<String> arStr=new ArrayList<String>();
      ArrayList<Point> sentencesBounds=new ArrayList<Point>();
      Point tempPoint=null;
	  
//	  //reading input text file content, it will be used to compute sentences boundaries      
////      inputTextContent=cleanTextContent(readTextUTF8());
//      inputTextContent=readTextUTF8();
    		  
	  arStr.add(
		"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
		+"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
		+"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"it\" lang=\"it\">"
		+"<head>"
		+"<title>DyLan - TextTools</title>"
		+"</head>"
		+"<body>"
		+"<table style=\"width: 100%; margin: 0px; padding: 0px;\">"
		+"<tr>"
		+"<td style=\"color: #ffffff; background: #fe6531; text-align: center;\"><b>TID</b></td>"
		+"<td style=\"color: #ffffff; background: #fe6531; text-align: center;\"><b>Token</b></td>"
		+"<td style=\"color: #ffffff; background: #fe6531; text-align: center;\"><b>token</b></td>"
		+"<td style=\"color: #ffffff; background: #fe6531; text-align: center;\"><b>data1</b></td>"
		+"<td style=\"color: #ffffff; background: #fe6531; text-align: center;\"><b>data2</b></td>"
		+"<td style=\"color: #ffffff; background: #fe6531; text-align: center;\"><b>data3</b></td>"
		+"</tr>");
	  
	  //creating html content of the result file
	  tmp=new File(filePath);
	  
	  try {
		reader=new BufferedReader(new FileReader(tmp));
//		tempPoint=new Point(0, 0);
//		sentenceBoundaries+="0\t0";//da guardare se il file è vuoto, non deve scrivere la prima riga(errata poi...)
		while((line=reader.readLine())!=null){
		  if(line.length()>0){//new token found
			//building html content of the result file
			elementData=line.split("\t");
			arStr.add("<tr>"
			+"<td bgcolor=\"#cccccc\" valign=\"top\" width=\"50px\" align=\"left\"><b>"+elementData[0]+"</b></td>"
			+"<td bgcolor=\"#cccccc\" valign=\"top\" align=\"left\">"+elementData[1]+"</td>"
			+"<td bgcolor=\"#cccccc\" valign=\"top\" align=\"left\">"+elementData[2]+"</td>"
			+"<td bgcolor=\"#cccccc\" valign=\"top\" align=\"left\">"+elementData[3]+"</td>"
			+"<td bgcolor=\"#cccccc\" valign=\"top\" align=\"left\">"+elementData[4]+"</td>"
			+"<td bgcolor=\"#cccccc\" valign=\"top\" align=\"left\">"+elementData[5]+"</td>"
					 +"</tr>");
			
//			if(currentToken==null && sentencesCount>0){//this is the first token found after the end of last sentence
////			  sentenceBoundaries+=sentencesCount+"\t"+inputTextContent.indexOf(elementData[1], inputTextIndex);
//			  tempPoint=new Point(inputTextContent.indexOf(elementData[1], inputTextIndex), 0);
//			}
			
			//moving in input text to reach current token index
			previousToken=currentToken; currentToken=elementData[1];
//			inputTextIndex=inputTextContent.indexOf(currentToken, inputTextIndex);
//			if(inputTextIndex<0) System.out.println("Got index <0! Term was: "+currentToken+"\tPrevious: "+previousToken);
//			inputTextIndex+=currentToken.length();
		  }
		  else{//found a newLine, calculating sentence boundaries
			if(sentencesCount%100==0)System.out.println("Found newLine! #"+sentencesCount);
////			sentenceBoundaries+="\t"+(inputTextIndex-1)+"\n";
//			tempPoint.y=(inputTextIndex-1);
//			sentencesBounds.add(tempPoint);
			++sentencesCount;
			previousToken=currentToken; currentToken=null;
		  }
		}

		//adding last part to result file html content
		arStr.add("</table></body></html>");
		
//		//adding last end boundary to sentenceBoundaries
//		if(currentToken!=null){
////		  sentenceBoundaries+="\t"+(inputTextIndex/*+currentToken.length()*/-1)+"\n";
//		  tempPoint.y=(inputTextIndex/*+currentToken.length()*/-1);
//		  sentencesBounds.add(tempPoint);		  
//		}
	  
		reader.close();
	  } catch (IOException e1) {
		System.out.println("createResultFilePostTagging(): File Write Problem!");
		try{ reader.close();}catch(IOException e){}
		e1.printStackTrace(); return null;
	  }
	  
//	  for(Point p : sentencesBounds) System.out.println(p.x+"-"+p.y);
	  
	  //printing files
	  try {
		//printing html result file
		tmp=new File(pathPrefix+1+".html");
		printer = new PrintWriter(tmp);
		for(String a: arStr) printer.print(a);
		printer.close();			
//		//printing sentences boundaries file
//		tmp=new File(pathPrefix+"SENTENCES_BOUNDS.log");
//		printer = new PrintWriter(tmp);
//		for(Point p : sentencesBounds) printer.println(p.x+" - "+p.y);
//		printer.close();	
	  } catch (IOException e1) {
		System.out.println("createResultFileInputText(): File Write Problem!");
		if(printer!=null) printer.close();	
		e1.printStackTrace(); return null;
	  }

	  
//	  inputTextIndex=inputTextContent.indexOf(";");
//	  System.out.println("Index of ';' - "+inputTextIndex);
//	  while ( (inputTextIndex=inputTextContent.indexOf(";", inputTextIndex+1))!=-1)
//		  System.out.println("Index of ';' - "+inputTextIndex);

	  pathPageHTML.add(pathPrefix+1+".html");

	  return sentencesBounds;
	}

	/**
	 * Creates the HTML analisys result file containing the term extraction, using filePath as source. <br>
	 * The other 2 analisys result files must already be present.
	 * 
	 * @param filePath - a String representing the path of term extraction file
	 * @param conllPath - a String representing the path of CoNLL file
	 * @param sentencesBoundaries - an ArrayList<Point>, representing start and end indexes of each sentence in the input file
	 */
	public void createResultFileTermExtractor(String filePath, String conllPath/*, ArrayList<Point> sentencesBoundaries*/) {
	  File tmp=null;
	  PrintWriter printer=null;
	  BufferedReader reader=null;		
	  String line=null;
      String[] elementData=null;
      ArrayList<String> arStr=new ArrayList<String>();
      String pathPrefix = readPathFileUTF8().substring(0, readPathFileUTF8().length()-4);
      ArrayList<String> termsFromExtractor=new ArrayList<String>();
      boolean inSequence=false;
      ArrayList<String> originalTokensInSequence=null;
      ArrayList<String> modifiedTokensInSequence=null;
      String possibleRelevantTerm=null, computedRelevantTerm=null;
      int start=0, limit=0;
      HashMap<String, String> termsVersions=null;
      ArrayList<String> termSet =null;    		  
//	  termRelevant=new ArrayList<String[]>();
	  termRelevant=new HashMap<String, ArrayList<String>>();
	  String[] singleTermBothVersions=null;
	  int i=0;
	  
	  //creating html content of the result file
	  tmp=new File(filePath);
	  
	  arStr.add(
		"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
		+"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
		+"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"it\" lang=\"it\">"
		+"<head>"
		+"<title>DyLan - TextTools</title>"
		+"</head>"
		+"<body>"
		+"<table style=\"width: 100%; margin: 0px; padding: 0px;\">"
		+"<tr>"
		+"<td style=\"color: #ffffff; background: #fe6531; text-align: center;\"><b>Lemma of Term</b></td>"
		+"<td style=\"color: #ffffff; background: #fe6531; text-align: center;\"><b>Domain Relevance</b></td>"
		+"<td style=\"color: #ffffff; background: #fe6531; text-align: center;\"><b>Frequency</b></td>"
		+"</tr>");
	  
	  try {
		reader=new BufferedReader(new FileReader(tmp));
		//skipping first line, it's the header
		if((line=reader.readLine())==null){
		  try { reader.close();}catch(Exception e1){}
		  return;
		}
		
		while((line=reader.readLine())!=null){
		  if(line.length()==0) break;//terms list is over
		  
		  elementData=line.split("\t");
		  if(elementData.length<3) break;
		  arStr.add("<tr>"
			+"<td bgcolor=\"#cccccc\" valign=\"top\" width=\"50px\" align=\"left\"><b>"+elementData[0]+"</b></td>"
			+"<td bgcolor=\"#cccccc\" valign=\"top\" align=\"left\">"+elementData[1]+"</td>"
			+"<td bgcolor=\"#cccccc\" valign=\"top\" align=\"left\">"+elementData[2]+"</td>"
				   +"</tr>");

		  //adding term to relevant terms list
		  termsFromExtractor.add(elementData[0]);
		}
		
		//adding last div to html result file
		arStr.add("</table><div>");
		if(line!=null) arStr.add(line+"<br>");
		while((line=reader.readLine())!=null) arStr.add(line+"<br>");
		arStr.add("</div></body></html>");

		reader.close();			
	  }catch(IOException e){
		try { reader.close();}catch(Exception e1){}
		System.out.println("createResultFileTermExtractor(): File Read Problem!");
		e.printStackTrace(); return;
	  }

	  //printing html result file
	  try {
		tmp=new File(pathPrefix+2+".html");
		printer = new PrintWriter(tmp);
		for(String a: arStr) printer.print(a);
		printer.close();
	  } catch (IOException e1) {
		System.out.println("createResultFileTermExtractor(): File Write Problem!");
		if(printer!=null) printer.close();
		e1.printStackTrace(); return;
	  }	
	  
	  //computing original versions of relevant terms, terms in sentences and terms arities
	  tmp=new File(conllPath);	
	  termSet = new ArrayList<String>();
	  termsInSentencesSet = new ArrayList<ArrayList<String>>();
	  termsArity= new HashMap<String, Integer>();
	  originalTokensInSequence=new ArrayList<String>();
	  modifiedTokensInSequence=new ArrayList<String>();
	  termsVersions=new HashMap<String, String>();
	  try {
		reader=new BufferedReader(new FileReader(tmp));
		while((line=reader.readLine())!=null){
		  if(line.length()>0){//new token found
			elementData=line.split("\t");
			if (elementData[8].startsWith("B")){
			  if(inSequence){
				//testing possible modified relevant terms computed from original tokens				  
				for(start=0; start<modifiedTokensInSequence.size(); ++start){
				  for(limit=modifiedTokensInSequence.size()-1; limit>=start; --limit){
					possibleRelevantTerm=modifiedTokensInSequence.get(start);
					for(int l=start+1; l<=limit; ++l) possibleRelevantTerm+=" "+modifiedTokensInSequence.get(l);

					if(termsFromExtractor.contains(possibleRelevantTerm)){//found a relevant term
					  //computing the original version of the term
					  computedRelevantTerm=originalTokensInSequence.get(start);
					  for(int l=start+1; l<=limit; ++l) computedRelevantTerm+=" "+originalTokensInSequence.get(l);
					  possibleRelevantTerm=cleanTermRelevant(possibleRelevantTerm);
					  computedRelevantTerm=cleanTermRelevant(computedRelevantTerm);

					  /* ***DEBUG*** */
					  if(debug2) System.out.println("\n***Found term: "+possibleRelevantTerm+"\n***Original:"+ computedRelevantTerm);
					  /* ***DEBUG*** */

					  //adding cleaned extracted version term to sentence set and updating its arity
					  if(!termSet.contains(possibleRelevantTerm)){						
						termSet.add(possibleRelevantTerm);
						if(termsArity.get(possibleRelevantTerm)==null) termsArity.put(possibleRelevantTerm, 1);
						else termsArity.put(possibleRelevantTerm, termsArity.get(possibleRelevantTerm)+1);
					  }

			          //initializing termRelevant, if necessary
					  if (termRelevant.get(possibleRelevantTerm)==null)
						termRelevant.put(possibleRelevantTerm, new ArrayList<String>());
					  arStr=termRelevant.get(possibleRelevantTerm);
					  for(i=0; i<arStr.size(); ++i) 
						if(arStr.get(i).toUpperCase().compareTo(computedRelevantTerm.toUpperCase())==0) break;

					  //adding original relevant term version if it is not already been added
					  if(i==arStr.size()) arStr.add(computedRelevantTerm);
					  
//					  if(termsVersions.get(possibleRelevantTerm)==null){
//						//adding cleaned original version term to versions association map
//						termsVersions.put(possibleRelevantTerm, computedRelevantTerm);
//
//						/* ***DEBUG*** */
//						if(debug2) System.out.println("Added term: "+possibleRelevantTerm);
//						/* ***DEBUG*** */
//
//						singleTermBothVersions=new String[2];
//						singleTermBothVersions[0]=possibleRelevantTerm;
//						singleTermBothVersions[1]=computedRelevantTerm;
//						termRelevant.add(singleTermBothVersions);
//					  }
					}
				  }
				}				  
				  
			  }//previous sequence is ended
			  //start of a new token sequence for a possible relevant term
			  inSequence=true;
			  originalTokensInSequence.clear();
			  modifiedTokensInSequence.clear();
			  originalTokensInSequence.add(elementData[1]);
			  modifiedTokensInSequence.add(elementData[2]);
			}
			else if (elementData[8].startsWith("I")){
			  //building the token sequence for a possible relevant term
			  originalTokensInSequence.add(elementData[1]);
			  modifiedTokensInSequence.add(elementData[2]);
			}
			else{
			  if(!inSequence) continue;//there are currently no tokens sequence to test
			  //testing possible modified relevant terms computed from originale tokens
			  inSequence=false;
			  for(start=0; start<modifiedTokensInSequence.size(); ++start){
				for(limit=modifiedTokensInSequence.size()-1; limit>=start; --limit){
				  possibleRelevantTerm=modifiedTokensInSequence.get(start);
				  for(int l=start+1; l<=limit; ++l) possibleRelevantTerm+=" "+modifiedTokensInSequence.get(l);

				  if(termsFromExtractor.contains(possibleRelevantTerm)){//found a relevant term
					//computing the original version of the term
					computedRelevantTerm=originalTokensInSequence.get(start);
					for(int l=start+1; l<=limit; ++l) computedRelevantTerm+=" "+originalTokensInSequence.get(l);
					possibleRelevantTerm=cleanTermRelevant(possibleRelevantTerm);
					computedRelevantTerm=cleanTermRelevant(computedRelevantTerm);
					System.out.println("\n***Found term: "+possibleRelevantTerm+"\n***Original:"+ computedRelevantTerm);
		            //adding cleaned original version term to sentence set and updating its arity
		            if(!termSet.contains(possibleRelevantTerm)){						
		              termSet.add(possibleRelevantTerm);
		              if(termsArity.get(possibleRelevantTerm)==null) termsArity.put(possibleRelevantTerm, 1);
		              else termsArity.put(possibleRelevantTerm, termsArity.get(possibleRelevantTerm)+1);
		            }
		            //initializing termrelevant, if necessary
		            if (termRelevant.get(possibleRelevantTerm)==null)
		              termRelevant.put(possibleRelevantTerm, new ArrayList<String>());
		            arStr=termRelevant.get(possibleRelevantTerm);
		            for(i=0; i<arStr.size(); ++i)
		              if(arStr.get(i).toUpperCase().compareTo(computedRelevantTerm.toUpperCase())==0) break;

		            //adding original relevant term version if it is not already been added
		            if(i==arStr.size()) arStr.add(computedRelevantTerm);
		            
//		            //adding relevant term if it is not already been added
//		            if(termsVersions.get(possibleRelevantTerm)==null){
//		              //adding cleaned original version term to versions association map
//		              termsVersions.put(possibleRelevantTerm, computedRelevantTerm);
//		              System.out.println("Added term: "+possibleRelevantTerm);
//		              singleTermBothVersions=new String[2];
//		              singleTermBothVersions[0]=possibleRelevantTerm;
//		              singleTermBothVersions[1]=computedRelevantTerm;
//		              termRelevant.add(singleTermBothVersions);
//		            }
				  }
				}
			  }	
			}
		  }
		  else{//found a newLine, changing the sentence set
			if(termSet.size()>0) termsInSentencesSet.add(termSet);
			termSet= new ArrayList<String>();
		  }
		}
      }catch(Exception e){
        System.out.println("createResultFileTermExtractor - Exception during read: " + e.getMessage());
        e.printStackTrace();
        return;
      }	  
	  
	  
/*	  
	  //reading input text file content
      inputTextContent=readTextUTF8();
      
	  //saving the positions in input file of relevant terms occurences 
	  relevantTerms=new HashMap<String, ArrayList<Integer>>();
	  try {
//		reader = new BufferedReader(new StringReader(cleanTextContent(inputTextContent)));
		reader = new BufferedReader(new StringReader(inputTextContent));
		charcount=0;
		while((line = reader.readLine()) != null){//for each line
		  for(int h=0; h<termRelevant.size(); h++){//for each relevant term
			index=0;
//			currentTerm=cleanTermRelevant(termRelevant.get(h));
			currentTerm=termRelevant.get(h);
			  
			while(index<line.length()){//for each occurrence
			  //get next occurrence
			  index = line.toUpperCase().indexOf(currentTerm.toUpperCase(), index);			  
			  if (index == -1) break;//start checking next relevant term occurrences in this line
			  //add occurrence to relevantTerms, if it is valid
			  if (ModelProject.isValidOccurrence(currentTerm, line, index))
				addIndexToOccursList(relevantTerms, charcount+index, h);
			  else System.out.println("Not Valid Occurrence!\nterm: "+currentTerm+"\nline: "+line);
			  //incrementing index to search for next occurrence
			  index+=termRelevant.get(h).length();
			}
		  }
		  charcount+=line.length()+1;
		}
		
		reader.close();
	  } catch (IOException e) {
		try{ reader.close();}catch(Exception e2){}
		e.printStackTrace();
	  }
	  
	  

	  

	  Iterator<Entry<String, ArrayList<Integer>>> iter = relevantTerms.entrySet().iterator();
	  Entry<String, ArrayList<Integer>> entry=null;
	  ArrayList<Integer> tmpList=null;
	  String tmpTerm=null;
	  System.out.println("\n*****printing terms in relevantTerms: ");
	  while(iter.hasNext()){
		entry=iter.next();
		tmpTerm=entry.getKey();
		tmpList=entry.getValue();
		System.out.println(tmpTerm);
//		for(int i : tmpList) System.out.println(i);
	  }

	  System.out.println("\n*****printing terms in termRelevant: ");
	  for(String term: termRelevant) System.out.println(term);
	  
	  //calculating sentences sets and terms arities
	  termsInSentencesSet = new ArrayList<ArrayList<String>>();
	  for(int i=0; i<sentencesBoundaries.size(); ++i) termsInSentencesSet.add(new ArrayList<String>());
	  termsArity= new HashMap<String, Integer>();
	  
	  termsIter = relevantTerms.entrySet().iterator();
	  termsEntry=null;
	  ArrayList<Integer> occurrsList=null;
	  String termName=null;
	  while(termsIter.hasNext()){//for each relevant term
		termsEntry=termsIter.next();
		termName=termsEntry.getKey();
		occurrsList=termsEntry.getValue();
//		System.out.println("\n***Term: "+termName);
		for(int i=0, l=0; i<sentencesBoundaries.size() && l<occurrsList.size(); ){
//		  for(int occurrence : occurrsList){//for each occurrence//for each sentence
		  if(occurrsList.get(l)>=sentencesBoundaries.get(i).x && occurrsList.get(l)<=sentencesBoundaries.get(i).y){
			termsInSentencesSet.get(i).add(termName);
			if(termsArity.get(termName)==null) termsArity.put(termName, 1);
			else termsArity.put(termName, termsArity.get(termName)+1);
			++i; ++l;
		  }
		  else if(occurrsList.get(l)<sentencesBoundaries.get(i).x) ++l;
		  else if(occurrsList.get(l)>sentencesBoundaries.get(i).y) ++i;
			
		}
	  }
*/
	  
	  
	  
	  pathPageHTML.add(pathPrefix+2+".html");
	  
	  if (SAVE_RELEVANT_TERMS_IMMEDIATELY) try {
		saveRelevantTerms();
	  } catch (IOException e) {
		System.out.println("Relevant terms file could not be saved");
		e.printStackTrace();
	  }

	}

	/**
	 * Returns a cleaned version of term, removing a whitespace if it precedes an apex.
	 * 
	 * @param inputTextContent - the String to be cleaned
	 * @return - a new cleaned String
	 */
	private String cleanTermRelevant(String term) {
		Matcher m = null;    
		
		Pattern p1 = Pattern.compile("\\s'");

        m = p1.matcher(term);
        term = m.replaceAll("'");		
        
		return term;
	}
	
//	/**
//	 * Adds to relevantTerms the starting index of an occurrence of a relevant term.
//	 * 
//	 * @param relevantTerms - the occurrences list
//	 * @param position - the index of starting character of this occurrence in the file
//	 * @param h - the index of the relevant term in the global field 'termRelevant'
//	 */
//	private void addIndexToOccursList(HashMap<String, ArrayList<Integer>> relevantTerms, int position, int h) {
//	  if(relevantTerms.get(termRelevant.get(h))==null)
//		relevantTerms.put(termRelevant.get(h), new ArrayList<Integer>());
//	  relevantTerms.get(termRelevant.get(h)).add(position);
//	}
	  
	
	/**
	 * Saves the state of this model on file.
	 */
	public void saveState(){

	  try{
		//saving relevant terms on file
		saveRelevantTerms();
		//saving relevant terms sets, one per sentence, on file
		saveRelevantTermsSets();
		//saving relevant terms arities on file
		saveRelevantTermsArities();
		
	  }catch(FileNotFoundException e){
		System.out.println("ModelAnalysis.saveState(): Exception during write: " + e.getMessage());
		e.printStackTrace();
	  }catch (IOException e){
		System.out.println("ModelAnalysis.saveState(): Exception during write: " + e.getMessage());
		e.printStackTrace();
	  }
	}
	
	/**
	 * Saves relevant terms of this model on file.
	 */
	private void saveRelevantTerms() throws IOException {
	  String line=null;
	  if(termRelevant==null){//just creating an empty file
		File emptyFile=new File(readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + ".log");
		emptyFile.getParentFile().mkdirs(); emptyFile.createNewFile();
		return;
	  }
	  
	  PrintWriter writer =
		new PrintWriter(
		  new BufferedWriter(
				  new FileWriter(readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + ".log")));

	  Iterator<Entry<String, ArrayList<String>>> termIter = termRelevant.entrySet().iterator();
	  Entry<String, ArrayList<String>> termEntry=null;
	  while(termIter.hasNext()){
		termEntry=termIter.next();
		line=termEntry.getKey();
		for(String version : termEntry.getValue()) line+="\t"+version;
		writer.println(line);				
	  }
//	  for(int j = 0; j< termRelevant.size(); j++) 
//		writer.print(termRelevant.get(j)[0]+"\t"+termRelevant.get(j)[1]+"\n");

	  writer.close();
	}

	/**
	 * Saves relevant terms sets of this model on file, one per sentence.
	 */
	private void saveRelevantTermsSets() throws IOException {
	  PrintWriter writer=null;
	  if(termsInSentencesSet==null){//just creating an empty file
		File emptyFile=new File(readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + "SETS.log");
		emptyFile.getParentFile().mkdirs(); emptyFile.createNewFile();
		return;
	  }
			  
	  writer =
		new PrintWriter(
		  new BufferedWriter(
			new FileWriter(readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + "SETS.log")));

	  for(int j = 0; j< termsInSentencesSet.size(); j++){
		writer.print(SENTENCE_PREFIX+j+SENTENCE_SUFFIX);
		for(String term : termsInSentencesSet.get(j)) writer.print(term + "\n");
	  }

	  writer.close();
	}

	/**
	 * Saves relevant terms arities of this model on file.
	 */
	private void saveRelevantTermsArities() throws IOException {
	  PrintWriter writer=null;
	  if(termsArity==null){//just creating an empty file
		File emptyFile=new File(readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + "ARITY.log");
		emptyFile.getParentFile().mkdirs(); emptyFile.createNewFile();
		return;
	  }
	  writer =
		new PrintWriter(
		  new BufferedWriter(
			new FileWriter(readPathFileUTF8().substring(0, readPathFileUTF8().length()-4) + "ARITY.log")));

	  Iterator<Entry<String, Integer>> iter = termsArity.entrySet().iterator();
	  Entry<String, Integer> entry=null;
	  while(iter.hasNext()){
		entry=iter.next();
		writer.print(entry.getKey()+ " "+entry.getValue()+"\n");  
	  }

	  writer.close();
	}
		
	/**
	 * Loads the state of this model from file.
	 */	
	public void loadState(){
	  BufferedReader br1 =null;
	  String s1=null;		
	  ArrayList<String> termSet=null;
	  String[] arityLineSplitted=null;
	  String termName=null;
	  
	  //loading relevant terms sets, one per sentence, from file
	  try{
		br1 = new BufferedReader(new FileReader(readPathFileSets()));	
		termsInSentencesSet=new ArrayList<ArrayList<String>>();		
	    while( (s1 = br1.readLine()) != null ){
		  if(s1.startsWith(ModelFile.SENTENCE_PREFIX)){
		    //a new sentence is starting, adding termSet to general set for this model
		    if(termSet!=null) termsInSentencesSet.add(termSet);
		    termSet=new ArrayList<String>();
		  }
		  else termSet.add(s1);

		  /* ***VERBOSE*** */
		  if(debug) System.out.println("loadProjectTermsInSentencesSets() - Ho letto: "+s1);
		  /* ***VERBOSE*** */
			  
	    }
	    //adding last termSet to general set for this model
	    if(termSet!=null) termsInSentencesSet.add(termSet);
			
	    br1.close();
	    
		//loading relevant terms arities from file
	    termsArity= new HashMap<String, Integer>();
		br1 = new BufferedReader(new FileReader(readPathFileArities()));	

	    while( (s1 = br1.readLine()) != null ){
	    	
	      /* ***VERBOSE*** */
		  if(debug) System.out.println("loadProjectTermsInSentencesSets() - Ho letto: "+s1);
		  /* ***VERBOSE*** */

		  arityLineSplitted=s1.split(" ");
		  termName=arityLineSplitted[0];
		  for(int k=1; k<arityLineSplitted.length-1; ++k) termName+=" "+arityLineSplitted[k];
		  termsArity.put(termName, new Integer(arityLineSplitted[arityLineSplitted.length-1]));
			  
	    }
			
	    br1.close();		
		
	  }catch(FileNotFoundException e){ 
		e.printStackTrace();
	  }catch (IOException e) {
		e.printStackTrace();
	  }
	  
	}

}
