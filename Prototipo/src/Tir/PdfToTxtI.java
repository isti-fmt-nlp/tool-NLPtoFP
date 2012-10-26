package project;

import java.io.*;

public interface PdfToTxtI 
{
	/** Funzione per convertire file.pdf in file.txt "pulito" e con codifica "UTF-8
	 * 
	 * @return f: File con codifica UTF-8 e pulitura contenuto oppure null se vi
	 * 			  sono stati problemi
	 */
	public File convertFile();

	/** 
	 * 
	 * @return
	 */
	public String getPathPdf();
	
	/** 
	 * 
	 * @return
	 */
	public String getPathTxt();
	
	/** 
	 * 
	 * @return
	 */
	public String getTextUtf8();
}

