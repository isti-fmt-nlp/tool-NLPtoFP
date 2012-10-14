import java.io.*;
import java.util.regex.*;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
   La classe PdfToTxt ha come obiettivo di convertire il file.pdf
   in un file.txt. Il contenuto del file.txt avrà codifica UTF-8 ed
   il testo sarà "ripulito".
*/
public class PdfToTxt
{
	/* Percorso in cui si trova il file.pdf */
    private String pathPdf = null;

    /* Percorso in cui viene creato il file.txt */
    private String pathTxt = null;
    
    /* Stringa contenente il contenuto del file.pdf*/
    private String textUtf8 = null;

    /** Costruttore

     	  @param pathPDF: Stringa contenente la path dove è situato il pdf da analizzare

		  @return Vengono inizializzate le path del file.pdf e del file.txt

    */
    public PdfToTxt(String s)
    {
    	/* Settiamo path del file.pdf */
        setPathPdf(s);       
        /* Settiamo path del file.txt */
        setPathTxt(pathPdf.substring(0, pathPdf.length()-4) + ".txt");
    }

    /** Funzione per convertire file.pdf in file.txt "pulito" e con codifica "UTF-8

     	@return f: File con codifica UTF-8 e pulitura file
     	@return null: Se vi sono stati errori

    */
    public File convertFile()
    {
        PrintStream ps = null;

        File f = new File(pathTxt);

        setTextUtf8(cleanString(pdfToStringUTF8()));
        
        if(getTextUtf8() == null)
        	return null;

        try
        {
            /*
               Apro in scrittura il file.txt scrivendoci
               il contenuto di "text_UTF_8"
            */
            ps =
                new PrintStream(
                    new FileOutputStream(pathTxt),false,"UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
            System.out.println("Exception Convert [0]: " + ex.getMessage());
            return null;
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("Exception Convert [1]: " + ex.getMessage());
            return null;
        }

        ps.print(getTextUtf8());
        ps.close();

        return f;
    }

    /** Funzione per convertire file.pdf in stringa

     	  @return s: Stringa contenente il contenuto
     			           del file.pdf con codifica UTF-8
       	@return null: Se vi sono stati errori

    */
    private String pdfToStringUTF8()
    {
        PDFParser pp = null;

        COSDocument cd = null;

        File f = new File(pathPdf);

        try
        {
            /*
               Apro in lettura il file.pdf ed il suo contenuto
               è convertito in stringa con codifica UTF-8
            */
            pp =
                new PDFParser(
                    new FileInputStream(f));

            pp.parse();

            cd = pp.getDocument();

            /* Stringa con codifica specificata */
            String s =
                new String(
                    new PDFTextStripper().getText(
                        new PDDocument(cd)).getBytes(),"UTF-8");

            cd.close();

            return s;
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Exception PdfToString [0]: " + e.getMessage());
            return null;
        }
        catch (IOException e)
        {
            System.out.println("Exception PdfToString [1]: " + e.getMessage());
            return null;
        }
    }

    /** Funzione che effettua la pulizia del testo utilizzando
     	le espressioni regolari

     	  @param s1: Stringa da ripulire

     	  @return s: Stringa ripulita
     	  @return null: Se vi sono argomenti invalidi

    */
    private String cleanString(String s)
    {
        Matcher m = null;

        Pattern p0 = Pattern.compile("-\\s");
        Pattern p1 = Pattern.compile("(http|www)[a-zA-Z0-9\\-\\.\\_\\?\\!\\&\\:\\/\\%\\-\\+\\=]+");
        Pattern p2 = Pattern.compile("http[a-zA-Z0-9\\-\\.\\_\\?\\!\\&\\:\\/\\%\\s\\-\\=]+\\s");
        Pattern p3 = Pattern.compile("[^a-zA-Z0-9\\-\\.\\_\\-\\,\\;\\?\\!\\s\\(\\)\\:\\/\\%]");
        Pattern p4 = Pattern.compile("(\\..)[\\.]+");
        Pattern p5 = Pattern.compile("\\.[\\s]+");
        Pattern p6 = Pattern.compile("\\s[\\s]+");

        if(s == null || s.equals(""))
            return null;

        /* Elimino i '-' alla fine di ogni riga */
        m = p0.matcher(s);
        s = m.replaceAll("");
        /* Sostituisco i siti (www || http) con spazi bianchi */
        m = p1.matcher(s);
        s = m.replaceAll(" ");
        /* Sostituisco i siti http aventi il riferimento alla pagine web con spazio bianchi */
        m = p2.matcher(s);
       	s = m.replaceAll(" ");
        /* Sostituisco i caratteri speciali con spazio bianco */
        m = p3.matcher(s);
        s = m.replaceAll(" ");
        /* Sostituisco la sequenza consecutiva di puntini > 3 con spazio bianco */
        m = p4.matcher(s);
        s = m.replaceAll(" ");
        /* Elimino tutti i break line */
        s = s.replaceAll("\n", " ");
        /* Inserisco dopo i '.' i break line */
        m = p5.matcher(s);
        s = m.replaceAll(".\n");
        /* Sostituisco la sequenza di spazi bianchi > 2 con un solo spazio bianco */
        m = p6.matcher(s);
        s = m.replaceAll(" ");

        return s;
    }
    
    /** 
     
        @return
     
    */
    public String getPathPdf()
    {
    	return this.pathPdf;
    }
    
    /** 
     
        @param pathPdf
        
    */
    public void setPathPdf(String pathPdf)
    {
    	this.pathPdf = new String(pathPdf);
    }
    
    /**
    
     * @return
     
    */
    public String getPathTxt()
    {
    	return this.pathTxt;
    }
    
    /** 
    
    @param pathPdf
    
    */
    public void setPathTxt(String pathTxt)
    {
    	this.pathTxt = new String(pathTxt);
    }
    
    /**
    
     * @return
     
    */
    public String getTextUtf8()
    {
    	return this.textUtf8;
    }
    
    /** 
    
    @param pathPdf
    
    */
    public void setTextUtf8(String textUtf8)
    {
    	this.textUtf8 = new String(textUtf8);
    }
}
