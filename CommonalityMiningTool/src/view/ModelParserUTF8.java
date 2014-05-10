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

import main.OSUtils;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class ModelParserUTF8{

	/* Stringa contenente il percorso del file */
	private String pathFile = null;
	
	/* Stringa contenente il percorso del nuovo file */
	private String pathFileUTF8 = null;
	
	/* Stringa contenente il contenuto del nuovo file */
	private String textUTF8 = null;
	
	private boolean isAnalisysDir=false;
	
	/** 
	 * Constructor.
	 * 
	 * @param pathFile - input file path
	 * @param pathProject - project path
	 */
	public ModelParserUTF8(String pathFile, String pathProject){
		this.pathFile = pathFile;
		this.pathFileUTF8 = pathProject + OSUtils.getFilePathSeparator()
		  + new File(pathFile).getName().substring(0, new File(pathFile).getName().length() - 4) + ".txt";
	}
	
	/**
	 * Sets the 'isAnalisysDir' property, which tells if this model <br>
	 * is that of an analisys directory or an input file. Default property value is false.
	 * 
	 * @param value - the boolean value, if true this model represents an analisys directory
	 */
	public void setIsAnalisysDir(boolean value){
	  isAnalisysDir=value;
	}
	
	/**
	 * Returns the 'isAnalisysDir' property, which tells if this model <br>
	 * is that of an analisys directory or an input file. Default property value is false.
	 * 
	 * @return - a boolean value, true if this model represents an analisys directory, false otherwise
	 */
	public boolean isAnalisysDir(){
	  return isAnalisysDir;
	}

	/**
	 * Returns a cleaned version of inputTextContent, making the following substitutions:<br>
	 * -each ' • ' become ' . '<br>
	 * -each ' ” ' become ' " '<br>
	 * -each ' “ ' become ' " '<br>
	 * -each ' – ' become ' - '<br>
	 * -each ' ’ ' become ' ' '
	 * 
	 * @param inputTextContent - the String to be cleaned
	 * @return - a new cleaned String
	 */
	protected static String cleanTextContent(String inputTextContent) {
		Matcher m = null;
		
        Pattern p0 = Pattern.compile("•");//.
        Pattern p1 = Pattern.compile("”");//"
        Pattern p2 = Pattern.compile("“");//"
        Pattern p3 = Pattern.compile("–");//-
        Pattern p4 = Pattern.compile("’");//'
        Pattern p5 = Pattern.compile("‘");//'        

		Pattern p6 = Pattern.compile("([^\\s])\"");
		Pattern p7 = Pattern.compile("\"([^\\s])");
//		Pattern p8 = Pattern.compile("([^\\s])'");
         
        m = p0.matcher(inputTextContent);
        inputTextContent = m.replaceAll(".");		
        
        m = p1.matcher(inputTextContent);
        inputTextContent = m.replaceAll("\"");	

        m = p2.matcher(inputTextContent);
        inputTextContent = m.replaceAll("\"");		
        
        m = p3.matcher(inputTextContent);
        inputTextContent = m.replaceAll("-");		

        m = p4.matcher(inputTextContent);
        inputTextContent = m.replaceAll("'");		

        m = p5.matcher(inputTextContent);
        inputTextContent = m.replaceAll("'");		

        m = p6.matcher(inputTextContent);
        inputTextContent = m.replaceAll("$1 \"");		

        m = p7.matcher(inputTextContent);
        inputTextContent = m.replaceAll("\" $1");		

//        m = p8.matcher(inputTextContent);
//        inputTextContent = m.replaceAll("$1 '");		
        
        return inputTextContent;
	}
	
	/** 
	 * Cleans the file content and creates an UTF8-compatible version.
	 * 
	 * @return - a File representing UTF8 file created, or null if an error occurred
	 */
	public File filterFile(){          		
        //Clean the file content
		if (isAnalisysDir()){
	      if((textUTF8 = /*cleanString*/cleanTextContent(encodeFileToStringUTF8()))==null) return null;   
		}	
		else if((textUTF8 = /*cleanString*/(encodeFileToStringUTF8()))==null) return null;        
        try{
          //writes UTF8 version file
          PrintStream ps = new PrintStream(new FileOutputStream(pathFileUTF8),false,"UTF-8");      	
          ps.print(textUTF8);
          ps.close();
        }catch (UnsupportedEncodingException ex){
          System.out.println("Exception filterFile: " + ex.getMessage());
          return null;
        }catch (FileNotFoundException ex){
          System.out.println("Exception filterFile: " + ex.getMessage());
          return null;
        }
      return new File(pathFileUTF8);
	}
	
	/**
	 * Returns the file path.
	 * 
	 * @return - a String representing the file path
	 */
	public String readPathFile(){
		return pathFile;
	}
	
	/** 
	 * Returns the path to UTF8 version of the input file.
	 * 
	 * @return - a String representing the file path
	 */
	public String readPathFileUTF8(){
		return pathFileUTF8;
	}
	
	/** 
	 * Returns the content of UTF8 version of the input file.
	 * 
	 * @return - a String containing the text of UTF8 version of the input file
	 */
	public String readTextUTF8(){
		return textUTF8;
	}
	
	
	/* -= FUNZIONI Ausiliarie =- */
	
	/** 
	 * Encode file content.
	 * 
	 * @return - a String representing the file content encoded as UTF-8
	 */
	private String encodeFileToStringUTF8(){
	  File f = new File(pathFile);	    
	  String s = "";
	    
	  if(pathFile.substring(pathFile.length() - 4, pathFile.length()).equals(".pdf")){
		PDFParser pp = null;			
		try{
		  //Extracts the content of the PDF file using UTF-8 encoder
		  pp = new PDFParser(new FileInputStream(f));				
		  pp.parse();              
	            
		  s = new String(new PDFTextStripper().getText(new PDDocument(pp.getDocument())).getBytes(),"UTF-8");
		}catch (IOException e){
		  System.out.println("Exception encodeFileToStringUTF8: " + e.getMessage());
		  return null;
		}
	  }
	  else try{
		String s1, s2 = "";
		BufferedReader reader = new BufferedReader(new FileReader(f.getAbsolutePath()));
				
		//Estrae il contenuto del file txt con codifica UTF-8
		while((s1 = reader.readLine()) != null) s2+=s1+"\n";

		s = new String(s2.getBytes(), "UTF-8");
				
		reader.close();
	  }catch (IOException e){
		System.out.println("Exception encodeFileToStringUTF8: " + e.getMessage());
		return null;
	  }
	  
	  return s;
	}
	
}