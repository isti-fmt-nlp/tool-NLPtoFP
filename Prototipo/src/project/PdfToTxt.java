package project;

import java.io.*;
import java.util.regex.*;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class PdfToTxt implements PdfToTxtI
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
    public PdfToTxt(String pathPdf)
    {
    	/* Settiamo le path */
        this.pathPdf = pathPdf;
        this.pathTxt = pathPdf.substring(0, pathPdf.length()-4) + ".txt";
    }


    public File convertFile()
    {
        File f = new File(pathTxt);

        /*   
            Estraiamo e puliamo il contenuto del file pdf
        */
        if((textUtf8 = cleanString(convertPdfToStringUTF8()))==null)
        	return null;
        
        try
        {
            /*
               Apro in scrittura il file txt scrivendoci
               il contenuto di "textUtf8"
            */
        	PrintStream ps =
            		new PrintStream(
            				new FileOutputStream(pathTxt),false,"UTF-8");      	
        	ps.print(textUtf8);
            ps.close();
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
        return f;
    }
    
    public String getPathPdf()
    {
    	return this.pathPdf;
    }
    
    public String getPathTxt()
    {
    	return this.pathTxt;
    }
    
    public String getTextUtf8()
    {
    	return this.textUtf8;
    }
    
    /* -= FUNZIONI Ausiliarie =- */

    /** Funzione per convertire file.pdf in stringa

     	  @return s: Stringa contenente il contenuto
     			           del file.pdf con codifica UTF-8
       	@return null: Se vi sono stati errori

    */
	private String convertPdfToStringUTF8()
    {
	    File f = new File(pathPdf);

        PDFParser pp = null;
		
        try
        {
            /* 
               Estraggo il contenuto del file pdf con codifica UTF-8
            */
            pp = new PDFParser(new FileInputStream(f));				
            pp.parse();              
            
            String s = new String(
            		new PDFTextStripper().getText(
            			new PDDocument(pp.getDocument())).getBytes(),"UTF-8");
			
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
}
