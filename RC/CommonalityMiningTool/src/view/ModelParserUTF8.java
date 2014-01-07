/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class ModelParserUTF8
{
	private static boolean verbose=true;//variabile usata per attivare stampe nel codice

	/* Stringa contenente il percorso del file */
	private String pathFile = null;
	
	/* Stringa contenente il percorso del nuovo file */
	private String pathFileUTF8 = null;
	
	/* Stringa contenente il contenuto del nuovo file */
	private String textUTF8 = null;
	
	/** Costruttore
	 * 
	 * @param pathFile path del file da analizzare
	 * @param pathProject path del progetto
	 */
	public ModelParserUTF8(String pathFile, String pathProject) 
	{
		this.pathFile = pathFile;
		this.pathFileUTF8 = 
				new String(pathProject + "/" + new File(pathFile).getName().substring(0, new File(pathFile).getName().length() - 4) + ".txt");
	}
	
	/** Filtra il file in modo da rendere il suo contenuto compatibile con la codifica UTF-8.
	 * 
	 * @return f file contenente le specifiche citate in precedenza
	 * @return null se si � verificato un errore
	 */
	public File filterFile()
	{
		/* ***VERBOSE****/
		if (verbose){
			System.out.println("Sono ModelParserUTF8.filterFile(): appena antrato nel metodo");
			System.out.flush();
		}
		/* ***VERBOSE****/            
		
		File f = new File(pathFileUTF8);
        /*   
            Estrae e pulisce il contenuto del file
        */
        if((textUTF8 = cleanString(encodeFileToStringUTF8()))==null){

    		/* ***VERBOSE****/
    		if (verbose){
            	System.out.println("Sono ModelParser.filterFile(): textUTF8="+textUTF8);
    			System.out.flush();
    		}
    		/* ***VERBOSE****/     

    		return null;
        }
        
        try
        {
            /*
               Apre in scrittura il nuovo file inserendoci
               il contenuto ottenuto
            */
        	PrintStream ps =
            		new PrintStream(
            				new FileOutputStream(pathFileUTF8),false,"UTF-8");      	
        	ps.print(textUTF8);
            ps.close();
        }
        catch (UnsupportedEncodingException ex)
        {
            System.out.println("Exception filterFile: " + ex.getMessage());
            return null;
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("Exception filterFile: " + ex.getMessage());
            return null;
        }
        return f;
	}
	
	/** Lettura del percorso in cui si trova il file
	 * 
	 * @return pathFile
	 */
	public String readPathFile()
	{
		return pathFile;
	}
	
	/** Lettura del percorso in cui viene creato il nuovo file
	 * 
	 * @return pathFileUTF8
	 */
	public String readPathFileUTF8()
	{
		return pathFileUTF8;
	}
	
	/** Lettura del contenuto del nuovo file
	 * 
	 * @return textUTF8
	 */
	public String readTextUTF8()
	{
		return textUTF8;
	}
	
	/* -= FUNZIONI Ausiliarie =- */
	
	/** Codifica il contenuto del file 
	 * 
	 * @return s stringa con codifica UTF-8
	 */
	private String encodeFileToStringUTF8()
	{
		File f = new File(pathFile);
	    
	    String s = "";
	    
	    if(pathFile.substring(pathFile.length() - 4, pathFile.length()).equals(".pdf"))
	    {
	        PDFParser pp = null;
			
	        try
	        {
	            /* 
	               Estrae il contenuto del file pdf con codifica UTF-8
	            */
	            pp = new PDFParser(new FileInputStream(f));				
	            pp.parse();              
	            
	            s = new String(
	            		new PDFTextStripper().getText(
	            			new PDDocument(pp.getDocument())).getBytes(),"UTF-8");
	        }
	        catch (FileNotFoundException e)
	        {
	            System.out.println("Exception encodeFileToStringUTF8: " + e.getMessage());
	            return null;
	        }
	        catch (IOException e)
	        {
	            System.out.println("Exception encodeFileToStringUTF8: " + e.getMessage());
	            return null;
	        }
	    }
	    else
	    {
	    	try 
	    	{
	    		String s1, s2 = "";
	    		
				BufferedReader reader =
				        new BufferedReader(
				        		new FileReader(f.getAbsolutePath()));
				
				/* 
	               Estrae il contenuto del file txt con codifica UTF-8
	            */
				while((s1 = reader.readLine()) != null)
					s2 = s2 + s1;
				
				s = new String(s2.getBytes(), "UTF-8");
				
				reader.close();
			} 
	    	catch (FileNotFoundException e) 
			{
	    		System.out.println("Exception encodeFileToStringUTF8: " + e.getMessage());
	            return null;
			} 
	    	catch (IOException e) 
	    	{
	    		System.out.println("Exception encodeFileToStringUTF8: " + e.getMessage());
	            return null;
			}
	    }
	    return s;
	}
	
	/** Pulisce il contenuto del file
	 * 
	 * @param s stringa da pulire
	 * 
	 * @return s stringa pulita
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
        
        /* Elimina i '-' alla fine di ogni riga */
        m = p0.matcher(s);
        s = m.replaceAll("");
        /* Sostituisce i siti (www || http) con spazi bianchi */
        m = p1.matcher(s);
        s = m.replaceAll(" ");
        /* Sostituisce i siti http aventi il riferimento alla pagine web con spazio bianchi */
        m = p2.matcher(s);
       	s = m.replaceAll(" ");
        /* Sostituisce i caratteri speciali con spazio bianco */
        m = p3.matcher(s);
        s = m.replaceAll(" ");
        /* Sostituisce la sequenza consecutiva di puntini > 3 con spazio bianco */
        m = p4.matcher(s);
        s = m.replaceAll(" ");
        /* Elimina tutti i break line */
        s = s.replaceAll("\n", " ");
        /* Inserisce dopo i '.' i break line */
        m = p5.matcher(s);
        s = m.replaceAll(".\n");
        /* Sostituisce la sequenza di spazi bianchi > 2 con un solo spazio bianco */
        m = p6.matcher(s);
        s = m.replaceAll(" ");
        
		return s;
	}
}