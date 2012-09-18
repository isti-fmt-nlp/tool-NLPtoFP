package tirocinio;

import java.io.*;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class PdfToTxt
{

    public String path_pdf = null;
    public String path_txt = null; 
    public String text_utf8 = null;
    public File file_txt = null;

    /* Costruttore */
    public PdfToTxt(String pathPDF)
    {
        this.path_pdf = pathPDF;
    }

    /* Funzione per convertire file.pdf in file.txt "pulito" e avente codifica "UTF-8 */
    public File Convert()
    {
    	
    	File f = null;
    	
        try
        {
            /* Inserisco e pulisco la stringa */
            String s = Clean(PdfToStringUTF8());
            this.text_utf8 = s;
            this.path_txt = this.path_pdf.substring(0, this.path_pdf.length()-4) + ".txt";
            f = new File(this.path_txt);
            PrintStream fr = new PrintStream(new FileOutputStream(this.path_txt),false,"UTF-8");
            fr.print(s);
            fr.close();
        }
        catch (IOException ex)
        {
            System.out.println("Errore " + ex.getMessage());
            return null;
        }

        return f;
    }

    /* Funzione per convertire file.pdf in stringa */
    private String PdfToStringUTF8()
    {
        String s1 = null, s2 = null;
        
        PDFParser parser;

        File f = new File(this.path_pdf);

        if(!f.isFile())
            return null;

        try 
        {
            /* Leggo il file.pdf e lo converto in stringa avente codifica "UTF-8" */
            parser = new PDFParser(new FileInputStream(f));
            parser.parse();
            COSDocument cosDoc = parser.getDocument();
            PDFTextStripper pdfStripper = new PDFTextStripper();
            PDDocument pdDoc = new PDDocument(cosDoc);
            s1 = pdfStripper.getText(pdDoc);
            s2 = new String(s1.getBytes(),"UTF-8");
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("Errore " + ex.getMessage());
            return null;
        }
        catch (IOException ex)
        {
            System.out.println("Errore " + ex.getMessage());
            return null;
        }
        
        return s2;
    }

    /* Funzione che effettua la pulizia del testo */
    private String Clean(String s1)
    {
        int j = -1;
        
        String [] suffix = {"ment", "ity", "ty", "tion", "sion", "ance", "ence", "age", "dom", "hood", "ness",
                            "ism", "our" , "logy", "er", "or", "es", "ist", "ess", "able", "ible", "ous", "al",
                            "ent", "ate", "ete", "ing", "ish", "ful", "less", "ly", "wise", "wards", "ward", "where", "ize"};
        String s2 = "";

        for(int i = 0; i < s1.length(); i++)
        {
            if(s1.charAt(i) == '\n')
            {
                j = i - 1;

                while(s1.charAt(j) == ' ') j--;

                if(s1.charAt(j) == '.')
                    s2 = s2 + String.valueOf(s1.charAt(i));
            }
            else
                s2 = s2 + String.valueOf(s1.charAt(i));
        }

        s2 = this.DeleteDashSuffix(suffix, s2);

        return s2;
    }

    /* Funzione aggiuntiva di Clean
     per l'eliminazione dei trattini "corretti"
    */
    private String DeleteDashSuffix(String [] s, String str )
    {
        int j ,z;

        boolean trovato = false;

        String p = "";

        for(int i = 0; i < str.length(); i++)
        {
            if(str.charAt(i) == '–' ||str.charAt(i) == '­')
            {

                for(int x = 0; x < s.length; x++)
                {
                    j = i + 1;
                    z = 0;

                    while(j < str.length() && z < s[x].length() && str.charAt(j) != s[x].charAt(z))
                        j++;

                    while(j < str.length() && z < s[x].length() && str.charAt(j) == s[x].charAt(z))
                    {
                        j++;
                        z++;
                    }

                    if(z == s[x].length()) trovato = true;
                }

                if(!trovato) p = p + String.valueOf(str.charAt(i));
            }
           
            
            else p = p + String.valueOf(str.charAt(i));
            	
        }
        p=p.replaceAll("\u25cf", " ");
        p=p.replaceAll("\u2022", " ");
        return p;
    }
/*
    public static void main(String[] args)
    {
    	PdfToTxt f = new PdfToTxt("/Users/danielecicciarella/Desktop/Tirocinio/tool-NLPtoFP/data/CBTC Vendors Evaluation.pdf");
    	f.Convert();
    }
*/
}
