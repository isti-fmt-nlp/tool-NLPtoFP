import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

/**
   La classe AnalysisText ha come obiettivo l'analisi del testo
   del file.pdf. Connettendoci al sito "http_URL" otteremo i
   termini rilevanti e i modelli grafici dell'analisi.
*/
class AnalysisText extends PdfToTxt
{
    /* URL a cui ci dobbiamo connettere per ottenere le analisi */
    private final String URL_ANALYSIS ="http://www.ilc.cnr.it/dylanlab/index.php?page=texttools&hl=en_US&showtemplate=false";

    /* ArrayList contenenti i vari termini rilevanti */
    public ArrayList <String> termRelevant, termDomain, termSingle;
    
    /* ArrayList contenenti le path dove sono situate le pagine Html */
    private ArrayList<String> pageHtml;

    /** Costruttore

 	      @param pathPdf: Stringa contenente la path dove è situato il pdf da analizzare

	       @note Vengono inizializzate le path del file.pdf e del file.txt

    */
    public AnalysisText(String s)
    {
        super(s);
    }

    /** Funzione carica i dati della precedente analisi */
    public void loadAnalysis()
    {
    	initializePageHtml();
        initializeTermRelevant();
        initializeTermDomain();
        initializeTermSingle();
    	
        for(int i = 0; i < 4; i++)
            pageHtml.add(getPathPdf().substring(0, getPathPdf().length()-4) + i +".html");

        for(int i = 0; i < 3; i++)
        {
            try
            {
                String s;

                BufferedReader reader =
                   new BufferedReader(
                        new FileReader(
                        		getPathPdf().substring(0, getPathPdf().length()-4) + i +".txt"));

                while( (s = reader.readLine()) != null )
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
                reader.close(); 
            }

            catch (FileNotFoundException ex)
            {
                System.out.println("Exception LoadAnalysis [1]: " + ex.getMessage());
                return ;
            }
            catch (IOException ex)
            {
                System.out.println("Exception LoadAnalysis [2]: " + ex.getMessage());
                return ;
            }
        }
        Collections.sort(termRelevant);
        Collections.sort(termDomain);
        Collections.sort(termSingle);
    }

    /** Funzione si connette al sito "http_URL" per ottenere le
        analisi effettuate

        @return true: Set di pagine html contenenti le analisi
        @return false: Se vi sono stati errori.

    */
    public boolean runAnalysis()
    {
        int i = 0;

        ArrayList <String> listUrl = null;

        initializePageHtml();

        if(convertFile() == null)
            return false;

        if(((listUrl = (ArrayList<String>) connectionAnalysis()))==null)
            return false;

        try
        {
            while(i < listUrl.size())
            {
                String httpURL = listUrl.get(i), p = "", temp = null, s = null;

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

                while ((temp=br.readLine()) != null)
                    p = p + temp;

                br.close();

                if(!refreshAnalysis(p))
                {
                    huc.disconnect();
                    
                    Thread.sleep(10000);
                    
                    continue;
                }
                if((s = cleanHTML(p))!= null)
                {
                    /* Pagine Html contenenti le analisi */
                    PrintWriter pw =
                        new PrintWriter(
                            new BufferedWriter(
                                new FileWriter(
                                		getPathPdf().substring(0, getPathPdf().length()-4) + i +".html",false)));

                    pw.print(s);
                    pw.close();
                    
                    pageHtml.add(getPathPdf().substring(0, getPathPdf().length()-4) + i +".html");   

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

    /** Funzione si connette al sito http_URL passandogli
        il testo da analizzare

        @return true: Set di URL contenenti i
      		              riferimenti alle varie analisi
        @return false: Se vi sono stati errori

    */
    private Collection<String> connectionAnalysis()
    {
        String p = " ", temp, jid = null;

        ArrayList<String> listUrl = new ArrayList<String>();

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

            while ((temp=br.readLine()) != null)
                p = p + temp;

            br.close();

            if((jid = findJid(p)) == null)
            {
                System.out.println("Error not find yid");
                return null;
            }

            listUrl.add((URL_ANALYSIS + jid));
            listUrl.add((URL_ANALYSIS + jid + "&tmid=tm_sentence_splitter"));
            listUrl.add((URL_ANALYSIS + jid + "&tmid=tm_term_extractor"));
            listUrl.add((URL_ANALYSIS + jid + "&tmid=tm_parser"));

            huc.disconnect();

            return listUrl;
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
        int i, j, t, u;

        if(s == null)
        {
            System.out.println("Error not find page html");
            return null;
        }

        i = s.indexOf("<table");
        j = s.indexOf("<center>");
        t = s.indexOf("</center>") + 9;
        u = s.indexOf("</p>");

        return s.substring(0, i) + s.substring(j, t) + s.substring(u, s.length());
    }

    /** Funzione estrae i termini rilevanti

        @return true: Termini rilevanti e, se non esistono,
                      crea un set di file.txt che li contiene.
        @return false: Se vi sono stati errori

    */
    private boolean termRDS(File f)
    {
        int i = 2;

        initializeTermRelevant();
        initializeTermDomain();
        initializeTermSingle();

        Pattern es1 = Pattern.compile("<[^<]+?>");
        Pattern es2 = Pattern.compile("\\s[\\s]+");
        Pattern es3 = Pattern.compile("\\s[0-9]+\\s");
        Pattern es4 = Pattern.compile("\\s[0-9.]+%\\s");
        Pattern es5 = Pattern.compile("\\s[I][D]\\s");

        Matcher m;

        String s, p = "";
        String [] temp, t = {"relevant multiple terms (general purpose relevance) relevance",
                             "domain-specific multiple terms (text-dependent relevance) relevance",
                             "single terms relevance"};

        try
        {
            BufferedReader reader =
                new BufferedReader(
                    new FileReader(f.getAbsolutePath()));

            while( (s = reader.readLine()) != null )
                p = p + s;

            reader.close();

            m = es1.matcher(p);
            p = m.replaceAll("\n");
            /* */
            m = es2.matcher(p);
            p = m.replaceAll(" ");
            /* */
            m = es3.matcher(p);
            p = m.replaceAll(" %% ");
            /* */
            m = es4.matcher(p);
            p = m.replaceAll(" ");
            /* */
            m = es5.matcher(p);
            p = m.replaceAll("%%");
            temp = p.split("%%");

            while(!temp[i].trim().equals(t[1]))
            {
                if(!temp[i].equals("") && !temp[i].equals("\n") && !temp[i].equals(" "))
                    termRelevant.add(temp[i].trim());
                    
                i = i + 1;
            }
            
            i = i + 1;

            while(!temp[i].trim().equals(t[2]))
            {
                if(!temp[i].equals("") && !temp[i].equals("\n") && !temp[i].equals(" "))
                    termDomain.add(temp[i].trim());
                    
                i = i + 1;
            }

            i = i + 1;

            while(i < temp.length)
            {
                if(!temp[i].equals("") && !temp[i].equals("\n") && !temp[i].equals(" "))
                    termSingle.add(temp[i].trim());
                    
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
                
                /* Controllare vuoto ?*/
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

    private void initializeTermRelevant()
    {
    	termRelevant = null;
    	termRelevant = new ArrayList<String>();
    }
    
    private void initializeTermDomain()
    {
    	termDomain = null;
    	termDomain = new ArrayList<String>();
    }
    
    private void initializeTermSingle()
    {
    	termSingle = null;
    	termSingle = new ArrayList<String>();
    }
    
    private void initializePageHtml()
    {
    	pageHtml = null;
    	pageHtml = new ArrayList<String>();
    }
    
    public String getPageHtml(int i)
    {
    	return pageHtml.get(i);
    }
}