package Tir;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    public String path_pdf = null;

    /* Percorso in cui viene creato il file.txt */
    public String path_txt = null;
    
    /* Stringa contenente il contenuto del file.pdf*/
    public String text_utf8 = null;

    /** Costruttore

     	  @param pathPDF: Stringa contenente la path dove è situato il pdf da analizzare

		      @return Vengono inizializzate le path del file.pdf e del file.txt

    */
    public PdfToTxt(String pathPDF)
    {
        path_pdf = pathPDF;
        path_txt = path_pdf.substring(0, path_pdf.length()-4) + ".txt";
    }

    /** Funzione per convertire file.pdf in file.txt "pulito" e con codifica "UTF-8

     	@return f: File con codifica UTF-8 e pulitura file
     	@return null: Se vi sono stati errori

    */
    public File Convert()
    {
        PrintStream fr = null;

        File f = new File(this.path_txt);

        if((text_utf8 = CleanString(PdfToStringUTF8()))== null)
            return null;

        try
        {
            /*
               Apro in scrittura il file.txt scrivendoci
               il contenuto di "text_UTF_8"
            */
            fr =
                new PrintStream(
                    new FileOutputStream(this.path_txt),false,"UTF-8");
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

        fr.print(text_utf8);
        fr.close();

        return f;
    }

    /** Funzione per convertire file.pdf in stringa

     	  @return s: Stringa contenente il contenuto
     			           del file.pdf con codifica UTF-8
       	@return null: Se vi sono stati errori

    */
    private String PdfToStringUTF8()
    {
        PDFParser parser = null;

        COSDocument cosDoc = null;

        File f = new File(this.path_pdf);

        try
        {
            /*
               Apro in lettura il file.pdf ed il suo contenuto
               è convertito in stringa con codifica UTF-8
            */
            parser =
                new PDFParser(
                    new FileInputStream(f));

            parser.parse();

            cosDoc = parser.getDocument();

            /* Stringa con codifica specificata */
            String s =
                new String(
                    new PDFTextStripper().getText(
                        new PDDocument(cosDoc)).getBytes(),"UTF-8");

            cosDoc.close();

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
    private String CleanString(String s)
    {
        Matcher m = null;

        Pattern es0 = Pattern.compile("-\\s");
        Pattern es1 = Pattern.compile("(http|www)[a-zA-Z0-9\\-\\.\\_\\?\\!\\&\\:\\/\\%\\-\\+\\=]+");
        Pattern es2 = Pattern.compile("http[a-zA-Z0-9\\-\\.\\_\\?\\!\\&\\:\\/\\%\\s\\-\\=]+\\s");
        Pattern es3 = Pattern.compile("[^a-zA-Z0-9\\-\\.\\_\\-\\,\\;\\?\\!\\s\\(\\)\\:\\/\\%]");
        Pattern es4 = Pattern.compile("(\\..)[\\.]+");
        Pattern es5 = Pattern.compile("\\.[\\s]+");
        Pattern es6 = Pattern.compile("\\s[\\s]+");

        if(s == null || s.equals(""))
            return null;


        /* Elimino i '-' alla fine di ogni riga */
        m = es0.matcher(s);
        s = m.replaceAll("");
        /* Sostituisco i siti (www || http) con spazi bianchi */
        m = es1.matcher(s);
        s = m.replaceAll(" ");
        /* Sostituisco i siti http aventi il riferimento alla pagine web con spazio bianchi */
        m = es2.matcher(s);
       	s = m.replaceAll(" ");
        /* Sostituisco i caratteri speciali con spazio bianco */
        m = es3.matcher(s);
        s = m.replaceAll(" ");
        /* Sostituisco la sequenza consecutiva di puntini > 3 con spazio bianco */
        m = es4.matcher(s);
        s = m.replaceAll(" ");
        /* Elimino tutti i break line */
        s = s.replaceAll("\n", " ");
        /* Inserisco dopo i '.' i break line solo se la lettera dopo è maiuscola */
        m = es5.matcher(s);
        s = m.replaceAll(".\n");
        /* Sostituisco la sequenza di spazi bianchi > 2 con un solo spazio bianco */
        m = es6.matcher(s);
        s = m.replaceAll(" ");

        return s;
    }
}
