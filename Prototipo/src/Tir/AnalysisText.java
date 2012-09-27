package Tir;

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
    private final String http_URL ="http://www.ilc.cnr.it/dylanlab/index.php?page=texttools&hl=en_US&showtemplate=false";

    /* ArrayList contenenti i vari termini rilevanti */
    public ArrayList<String> Relevant = new ArrayList<String>();
    public ArrayList<String> Domain = new ArrayList<String>();
    public ArrayList<String> Single = new ArrayList<String>();
    
    /* ArrayList contenenti le path dove sono situate le pagine Html */
    public ArrayList<String> Path_html = new ArrayList<String>();

    /** Costruttore

 	      @param pathPDF: Stringa contenente la path dove è situato il pdf da analizzare

	       @note Vengono inizializzate le path del file.pdf e del file.txt

    */
    public AnalysisText(String pathPDF)
    {
        super(pathPDF);
    }

    /** Funzione carica i dati della precedente analisi */
    public void LoadAnalysis()
    {
        for(int i = 0; i < 4; i++)
            Path_html.add(path_pdf.substring(0, path_pdf.length()-4) + i +".html");

        for(int i = 0; i < 3; i++)
        {
            try
            {
                String s;

                BufferedReader reader =
                   new BufferedReader(
                        new FileReader(path_pdf.substring(0, path_pdf.length()-4) + i +".txt"));

                while( (s = reader.readLine()) != null )
                {
                    if(!s.equals("") && !s.equals("\n") && !s.equals(" "))
                    {
                        if(i == 0)
                            Relevant.add(s);

                        else if(i == 1)
                            Domain.add(s);

                        else
                            Single.add(s);
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
    }

    /** Funzione si connette al sito "http_URL" per ottenere le
        analisi effettuate

        @return true: Set di pagine html contenenti le analisi
        @return false: Se vi sono stati errori.

    */
    public boolean RunAnalysis()
    {
        int i = 0;

        ArrayList<String> AUrl = null;

        if(Convert() == null)
            return false;

        if(((AUrl = (ArrayList<String>) ConnectionAnalysis()))==null)
            return false;

        try
        {
            while(i < AUrl.size())
            {
                String HttpURL = AUrl.get(i), p = "", temp = null, s = null;

                URL myURL = new URL(HttpURL);

                HttpURLConnection con =
                   (HttpURLConnection)
                        myURL.openConnection();

                con.setRequestMethod("GET");
                con.setDoOutput(true);
                con.setDoInput(true);

                DataOutputStream output =
                    new DataOutputStream(
                        con.getOutputStream());

                output.close();

                BufferedReader buffer =
                    new BufferedReader(
                        new InputStreamReader(con.getInputStream()));

                while ((temp=buffer.readLine()) != null)
                    p = p + temp;

                buffer.close();

                if(!Refresh(p))
                {
                    con.disconnect();
                    Thread.sleep(10000);
                    continue;
                }
                if((s = CleanHTML(p))!= null)
                {
                    /* Pagine Html contenenti le analisi */
                    PrintWriter write =
                        new PrintWriter(
                            new BufferedWriter(
                                new FileWriter(path_pdf.substring(0, path_pdf.length()-4) + i +".html",false)));

                    write.print(s);
                    write.close();
                    Path_html.add(path_pdf.substring(0, path_pdf.length()-4) + i +".html");   

                    if(i == 2)
                    {
                        File f = new File(path_pdf.substring(0, path_pdf.length()-4) + i +".html");

                        if(!TermExtraction(f))
                            return false;
                    }
                }
                else
                    return false;

                con.disconnect();

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
    private Collection<String> ConnectionAnalysis()
    {
        String text = text_utf8, p = " ", temp, jid = null;

        ArrayList<String> AUrl = new ArrayList<String>();

        try
        {
            String query = "input_text=" + URLEncoder.encode(text, "UTF-8");

            URL myURL = new URL(http_URL);

            HttpURLConnection con =
                (HttpURLConnection)
                    myURL.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-lenght", String.valueOf(query.length()));
            con.setDoOutput(true);
            con.setDoInput(true);

            DataOutputStream output =
                new DataOutputStream(
                    con.getOutputStream());

            output.writeBytes(query);
            output.close();

            BufferedReader buffer =
                new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            while ((temp=buffer.readLine()) != null)
                p = p + temp;

            buffer.close();

            if((jid = FindJid(p)) == null)
            {
                System.out.println("Error not find yid");
                return null;
            }

            AUrl.add((http_URL + jid));
            AUrl.add((http_URL + jid + "&tmid=tm_sentence_splitter"));
            AUrl.add((http_URL + jid + "&tmid=tm_term_extractor"));
            AUrl.add((http_URL + jid + "&tmid=tm_parser"));

            con.disconnect();

            return AUrl;
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
    private String FindJid(String s)
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
    private boolean Refresh(String s)
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
    private String CleanHTML(String s)
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
    private boolean TermExtraction(File f)
    {
        int i = 2;

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
            m = es2.matcher(p);
            p = m.replaceAll(" ");
            m = es3.matcher(p);
            p = m.replaceAll(" %% ");
            m = es4.matcher(p);
            p = m.replaceAll(" ");
            m = es5.matcher(p);
            p = m.replaceAll("%%");
            temp = p.split("%%");

            while(!temp[i].trim().equals(t[1]))
            {
                if(!temp[i].equals("") && !temp[i].equals("\n") && !temp[i].equals(" "))
                {
                    Relevant.add(temp[i].trim());
                    i = i + 1;
                }
            }

            i = i + 1;

            while(!temp[i].trim().equals(t[2]))
            {
                if(!temp[i].equals("") && !temp[i].equals("\n") && !temp[i].equals(" "))
                {
                    Domain.add(temp[i].trim());
                    i = i + 1;
                }
            }

            i = i + 1;

            while(i < temp.length)
            {
                if(!temp[i].equals("") && !temp[i].equals("\n") && !temp[i].equals(" "))
                {
                    Single.add(temp[i].trim());
                    i = i + 1;
                }
            }

            for(i = 0; i < 3; i++)
            {
                PrintWriter writer =
                    new PrintWriter(
                        new BufferedWriter(
                            new FileWriter(path_pdf.substring(0, path_pdf.length()-4) + i + ".txt")));

                if(i == 0)
                    for(int j = 0; j< Relevant.size(); j++)
                        writer.print(Relevant.get(j) + "\n");

                else if(i == 1)
                    for(int j = 0; j< Domain.size(); j++)
                        writer.print(Domain.get(j) + "\n");

                else
                    for(int j = 0; j< Single.size(); j++)
                        writer.print(Single.get(j) + "\n");

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
