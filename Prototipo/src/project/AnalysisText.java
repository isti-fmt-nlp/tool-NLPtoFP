package project;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

class AnalysisText extends PdfToTxt implements AnalysisTextI
{
    /* ArrayList contenenti i vari termini rilevanti */
    private ArrayList <String> termRelevant = null, termDomain = null, termSingle = null;
    
    /* ArrayList contenenti le path dove sono situate le pagine Html */
    private ArrayList <String> pageHtml = null;

    /** Costruttore
     * 
     * @param pathPdf: Stringa contenente la path dove è situato il pdf da analizzare
     */
    public AnalysisText(String pathPdf)
    {
        super(pathPdf);
    }

    public boolean loadAnalysis()
    {
        termRelevant = new ArrayList <String> ();
        termDomain = new ArrayList <String> ();
        termSingle = new ArrayList <String> ();
        pageHtml = new ArrayList <String> ();
    	
        for(int i = 0; i < 4; i++)
            pageHtml.add(getPathPdf().substring(0, getPathPdf().length()-4) + i +".html");

        for(int i = 0; i < 3; i++)
        {
            try
            {
                String s;

                BufferedReader br = 
                		new BufferedReader(
                				new FileReader(
                						getPathPdf().substring(0, getPathPdf().length()-4) + i +".txt"));

                while( (s = br.readLine()) != null )
                {
                    if(!s.equals("") && !s.equals("\n") && !s.equals(" "))
                    {
                        if(i == 0)
                        	termRelevant.add(s);
                        
                        else if(i == 1)
                        	termDomain.add(s);
                        
                        else
                        	termSingle.add(s);
                    }
                }
                br.close(); 
            }

            catch (FileNotFoundException ex)
            {
                System.out.println("Exception LoadAnalysis [1]: " + ex.getMessage());
                return false;
            }
            catch (IOException ex)
            {
                System.out.println("Exception LoadAnalysis [2]: " + ex.getMessage());
                return false;
            }
        }
        
        Collections.sort(termRelevant);
        Collections.sort(termDomain);
        Collections.sort(termSingle);
        
        return true;
    }

    public boolean runAnalysis()
    {
        int i = 0;

        ArrayList <String> s1 = null;

        pageHtml = new ArrayList <String> ();

        if(convertFile() == null)
            return false;

        if(((s1 = (ArrayList<String>) connectionAnalysis()))==null)
            return false;

        try
        {
            while(i < s1.size())
            {
                String httpURL = s1.get(i), s2 = "", s3 = null, s4 = null;

                URL myURL = new URL(httpURL);

                HttpURLConnection huc = 
                		(HttpURLConnection)
                        		myURL.openConnection();
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

                if(!refreshAnalysis(s2))
                {
                    huc.disconnect();    
                    Thread.sleep(10000);
                    continue;
                }
                
                if((s4 = cleanHTML(s2))!= null)
                {
                    /* Creo pagine html contenenti le varie analisi */
                    PrintWriter pw = 
                    		new PrintWriter(
                            		new BufferedWriter(
                            				new FileWriter(
                            						getPathPdf().substring(0, getPathPdf().length()-4) + i +".html", false)));
                    pw.print(s4);
                    pw.close();   
							
                    /* Aggiungo le path delle pagine html */
                    pageHtml.add(
                    		getPathPdf().substring(0, getPathPdf().length()-4) + i +".html");   

                    if(i == 2)
                    {
                        File f = new File(getPathPdf().substring(0, getPathPdf().length()-4) + i +".html");

                        if(!termRDS(f))
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
            System.out.println("Exception RunAnalysis [0]: " + ex.getMessage());
            return false;
        }
        catch (MalformedURLException ex)
        {
            System.out.println("Exception RunAnalysis [1]: " + ex.getMessage());
            return false;
        }
        catch (UnsupportedEncodingException ex)
        {
            System.out.println("Exception RunAnalysis [2]: " + ex.getMessage());
            return false;
        }
        catch (IOException ex)
        {
            System.out.println("Exception RunAnalysis [3]: " + ex.getMessage());
            return false;
        }
    }
    
    public ArrayList <String> getTermRelevant()
    {
    	return termRelevant;
    }

    public ArrayList <String> getTermDomain()
    {
    	return termDomain;
    }
    
    public ArrayList <String> getTermSingle()
    {
    	return termSingle;
    }

    public String getPageHtml(int i)
    {
    	return pageHtml.get(i);
    }
    
    /* -= FUNZIONI Ausiliarie =- */

    /** Funzione si connette al sito http_URL passandogli
        il testo da analizzare

        @return true: Set di URL contenenti i
      		              riferimenti alle varie analisi
        @return false: Se vi sono stati errori

    */
    private Collection<String> connectionAnalysis()
    {
        String s1 = " ", s2 = null, s3 = null;

        ArrayList<String> pageUrl = new ArrayList<String>();

        try
        {
            String query = "input_text=" + URLEncoder.encode(getTextUtf8(), "UTF-8");

            URL myURL = new URL(URL_ANALYSIS);

            HttpURLConnection huc =
            		(HttpURLConnection)
                    		myURL.openConnection();
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

            while ((s2=br.readLine()) != null)
                s1 = s1 + s2;

            br.close();

            if((s3 = findJid(s1)) == null)
            {
                System.out.println("Error not find yid");
                return null;
            }

            pageUrl.add((URL_ANALYSIS + s3));
            pageUrl.add((URL_ANALYSIS + s3 + "&tmid=tm_sentence_splitter"));
            pageUrl.add((URL_ANALYSIS + s3 + "&tmid=tm_term_extractor"));
            pageUrl.add((URL_ANALYSIS + s3 + "&tmid=tm_parser"));
            huc.disconnect();

            return pageUrl;
        }
        catch (MalformedURLException ex)
        {
            System.out.println("Exception ConnectionAnalysis [0]: " + ex.getMessage());
            return null;
        }
        catch (UnsupportedEncodingException ex)
        {
            System.out.println("Exception ConnectionAnalysis [1]: " + ex.getMessage());
            return null;
        }
        catch (IOException ex)
        {
            System.out.println("Exception ConnectionAnalysis [2]: " + ex.getMessage());
            return null;
        }
    }

    /** Funzione che restituisce jid personale assegnatoci dal sito

        @param s: Stringa inviatoci dal sito, controlliamo se jid è stato
                  assegnato

        @return p: Stringa contenente jid
        @return null: Se jid non ci è stato assegnato

    */
    private String findJid(String s)
    {
        int i;

        String p = "&";

        if(s == null || s.equals(""))
           return null;

        if((i = s.indexOf("jid=")) == -1)
           return null;

        while(s.charAt(i) != '&')
        {
            p = p + String.valueOf(s.charAt(i));
            i = i + 1;
        }
        return p;
    }

    /** Funzione che controlla se l'analisi del testo è stata completata

        @param s: Stringa da controllare per verificare se l'analisi è
                  terminata

        @return true: Analisi terminata
        @return false: Analisi non terminata

    */
    private boolean refreshAnalysis(String s)
    {
        if(s == null || s.equals(""))
            return false;

         /*
            Campo che ci informa, se presente,
            che l'analisi non è ancora terminata
         */
         if(s.indexOf("<meta HTTP-EQUIV") != -1)
            return false;
         else
            return true;
    }

    /** Funzione che effettua la pulizia di una pagine html

     	@param s: Stringa contenente la pagina html da ripulire

     	@return Stringa html ripulita
     	@return null: Se vi sono stati errori

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

    /** Funzione estrae i termini rilevanti

        @return true: Termini rilevanti e, se non esistono,
                      crea un set di file.txt che li contiene.
        @return false: Se vi sono stati errori

    */
    private boolean termRDS(File f)
    {
        int i = 2;

        termRelevant = new ArrayList <String> ();
        termDomain = new ArrayList <String> ();
        termSingle = new ArrayList <String> ();

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
                    new FileReader(f.getAbsolutePath()));

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

            while(!s3[i].trim().equals(s4[1]))
            {
                if(!s3[i].equals("") && !s3[i].equals("\n") && !s3[i].equals(" "))
                    termRelevant.add(s3[i].trim());
                    
                i = i + 1;
            }
            
            i = i + 1;

            while(!s3[i].trim().equals(s4[2]))
            {
                if(!s3[i].equals("") && !s3[i].equals("\n") && !s3[i].equals(" "))
                    termDomain.add(s3[i].trim());
                    
                i = i + 1;
            }

            i = i + 1;

            while(i < s3.length)
            {
                if(!s3[i].equals("") && !s3[i].equals("\n") && !s3[i].equals(" "))
                    termSingle.add(s3[i].trim());
                    
                i = i + 1;
            }
            
            Collections.sort(termRelevant);
            Collections.sort(termDomain);
            Collections.sort(termSingle);

            for(i = 0; i < 3; i++)
            {
                PrintWriter writer =
                    new PrintWriter(
                        new BufferedWriter(
                            new FileWriter(getPathPdf().substring(0, getPathPdf().length()-4) + i + ".txt")));
                
                if(i == 0)
                {
                    for(int j = 0; j< termRelevant.size(); j++)
                        writer.print(termRelevant.get(j) + "\n");
                }
                else if(i == 1)
                {
                    for(int j = 0; j< termDomain.size(); j++)
                        writer.print(termDomain.get(j) + "\n");
                }
                else
                {
                    for(int j = 0; j< termSingle.size(); j++)
                        writer.print(termSingle.get(j) + "\n");
                }
                writer.close();
            }
            return true;
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Exception TermExtraction [0]: " + e.getMessage());
            return false;
        }
        catch (IOException e)
        {
            System.out.println("Exception TermExtraction [1]: " + e.getMessage());
            return false;
        }
    }
}