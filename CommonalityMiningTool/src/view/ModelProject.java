/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;


public class ModelProject extends Observable implements Runnable{
	

	private static final String savedProjectsDir = "Usage Tries";
	
	/* Stringa contenente il nome del progetto */
	private String nameProject = null;
	
	/* Stringa contenente il percorso del progetto */
	private String pathProject = null;
	
	/* Stringa contenente il percorso del file xml */
	private String pathXML = null;
	
	/* ParserXML contenente il parser del file xml */
	private ParserXML parserXML = null;
	
	/* ArrayList contenente i file del progetto */
	private ArrayList <ModelFile> fileProject = null;
	
	/* ArrayList contenente i thread assegnati ai file */
	private ArrayList <Thread> workerProject = null;
	
	/* Thread che gestisce la chiusura dell'analisi del progetto */
	private Thread handlerProject = null;
	
	/* Stringa contenente il percorso delle commonalities candidates */
	private String pathCommonalitiesCandidates = null;
	
	/* Stringa contenente il percorso delle commonalities selected */
	private String pathCommonalitiesSelected = null;
	
	/* Stringa contenente il percorso della pagina HTML delle commonalities selected */
	private String pathCommonalitiesSelectedHTML = null;
	
	/* Stringa contenente il percorso delle variabilities candidates */
	private String pathVariabilitiesCandidates = null;
	
	/* Stringa contenente il percorso delle variabilities selected */
	private String pathVariabilitiesSelected = null;
	
	/* Stringa contenente il percorso della pagina HTML delle variabilities selected */
	private String pathVariabilitiesSelectedHTML = null;
	
	/*(MANUEL M.) insieme dei termini rilevanti, ad ognuno corrisponde una lista di file di input
	 e ad ogni file corrisponde una lista di indici di caratteri, le occorrenze del termine*/
	/** Relevant terms set. For each, there is a corresponding input file names list, and for each file
	 * there is a corresponding list of integers, the indexes of term occurrence in that file*/
	private HashMap<String, HashMap<String, ArrayList<Integer>>> relevantTerms=null;
	
	/** String representing the path of variabilities selected HTML page*/
	private String pathRelevantTerms = null;
	
	/** ArrayList containing the commonalities candidates */
	private ArrayList <String> commonalitiesCandidates = null;
	
	/** ArrayList containing the commonalities selected */
	private ArrayList <String> commonalitiesSelected = null;
	
	/** ArrayList containing the commonalities candidates */
	private ArrayList <String> variabilitiesCandidates = null;
	
	/** ArrayList containing the commonalities selected */
	private ArrayList <String> variabilitiesSelected = null;
	
	/* boolean contenente lo stato del progetto */
	private boolean [] stateProject = {false, false};
	
	private static boolean verbose=false;//variabile usata per attivare stampe nel codice
	
	private static boolean verbose2=false;//variabile usata per attivare stampe nel codice
	
	private static boolean verbose3=true;//variabile usata per attivare stampe nel codice
	
	/** 
	 * Waits for threads workerProject to end their work, and computes commonalities candidates
	 */
	@Override
	public void run(){
	  //variables used to calculate occurencies lists
	  String line=null;		//line read from an input file 
	  int charcount=0;		//starting position of a line in the file
	  int index=0;			//index of a possible relevant term occurence in line
		
	  commonalitiesCandidates = new ArrayList <String> ();
		
	  for(int i = 0; i < workerProject.size(); i++){
		try{

		  /* ***VERBOSE****/
		  if (verbose){
			System.out.println("ModelProject.run(): starting join with workerProject.get("+i+").");
			System.out.flush();
		  }
		  /* ***VERBOSE****/

		  workerProject.get(i).join();

		  /* ***VERBOSE****/
		  if (verbose){
			System.out.println("ModelProject.run(): ended join with workerProject.get("+i+").");
			System.out.flush();
		  }
		  /* ***VERBOSE****/

		} 
		catch (InterruptedException e){
		  System.out.println("Exception Handler: " + e.getMessage());
		  return;
		}	
	  }
		
	  //saving the positions in input files of relevant terms occurences 
	  relevantTerms=new HashMap<String, HashMap<String, ArrayList<Integer>>>();
	  for(int k=0; k<fileProject.size(); k++){//for each file
		BufferedReader reader = null;
		try {
		  reader = new BufferedReader(new FileReader(fileProject.get(k).readPathFileUTF8()));
		  charcount=0;
			
		  while((line = reader.readLine()) != null){//for each line
			for(int h=0; h<fileProject.get(k).readTermRelevant().size(); h++){//for each relevant term
			  index=0;
			  while(index<line.length()){//for each occurrence
				//get next occurrence
				index = line.toUpperCase().indexOf(fileProject.get(k).readTermRelevant().get(h).toUpperCase(), index);

				if (index == -1) break;//start checking next relevant term occurrences in this line

				//add occurrence to relevantTerms, if it is valid
				//  				  if (isValidOccurrence(fileProject.get(k).readTermRelevant().get(h), line, index))
				addCharIndexToOccursList(fileProject.get(k).readTermRelevant().get(h),
						  fileProject.get(k).readPathFileUTF8(), charcount+index);

				//incrementing index to search for next occurrence
				index+=fileProject.get(k).readTermRelevant().get(h).length();
			  }
			}
			charcount+=line.length()+1;
		  }
		  
		  reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	finally{
			if (reader!=null) try {reader.close();}catch(Exception e){}
		}		

	  }

	  //extracting communalities candidates from first input File
	  for(int j = 0; j < fileProject.get(0).readTermRelevant().size(); j = j + 1)
       	if(intersectTermRelevant(fileProject.get(0).readTermRelevant().get(j)))
       		  commonalitiesCandidates.add(fileProject.get(0).readTermRelevant().get(j));
		
	  //removing duplicates among communalities candidates
	  for(int i = 0; i < commonalitiesCandidates.size(); i++)
		for(int j = i + 1; j < commonalitiesCandidates.size(); j++)
		  if(commonalitiesCandidates.get(i).equals(commonalitiesCandidates.get(j)))
			  	commonalitiesCandidates.remove(j);
		
	  /* ***VERBOSE****/
	  if(verbose){
		System.out.println("Sono ModelProject.run(): inizio a stampare le occorrenze delle communalitiesCandidates");
		for(String comm: commonalitiesCandidates){
		  System.out.println("***["+comm+"]***");
		  for(ModelFile file: fileProject){
			if (relevantTerms.get(comm)!=null && relevantTerms.get(comm).get(file.readPathFileUTF8())!=null){
			  System.out.println("----File: "+file.readPathFileUTF8());
			  for(int lineNum: relevantTerms.get(comm).get(file.readPathFileUTF8())){
				System.out.println("\tline: "+lineNum);
			  }
			}
		  }
		}
	  }
	  /* ***VERBOSE****/
		
	  setChanged();
	  notifyObservers("End Extract Commonalities");
	}
	
	/**
	 * Check if there is a valid occurrence of term in line, starting at index.<br>
	 * An occurrence is valid when: <br>
	 * - is completely contained in line<br>
	 * - characters in line at the positions (index-1) and (index+term.lenght() ), if present,<br>
	 *  are ' ', '.', ',', '(', ')', '[', ']', '{', '}', '<', '>', '-' or newlines.
	 * 
	 * @param term - the term that occurs
	 * @param line - the line containing the occurence
	 * @param index - the starting index if the occurrence
	 * @return true if this is a valid occurrence, false otherwise
	 */
	protected static boolean isValidOccurrence(String term, String line, int index) {
		if (index<0) return false;
		//checking previous character, if present
		if (index>0)
		  if (!isValidPrevOrSeqOccurrChar(line.charAt(index-1)) ) return false;
		//checking sequent character, if present
		if(index+term.length()<line.length())
		  if (!isValidPrevOrSeqOccurrChar(line.charAt(index+term.length())) ) return false;
		return true;
	}

	/**
	 * Check if c is a valid before-start or after-end character for a relevant term occurrence.<br>
	 * To be valid, c must be ' ', '.', ',', '(', ')', '[', ']', '{', '}', '<', '>', '-' or newline.
	 * 
	 * @param c - the character to be checked
	 * @return true if c is valid, false otherwise
	 */
	protected static boolean isValidPrevOrSeqOccurrChar(char c) {
		  switch(c){
		  	case ' ': case '.': case ',': case '(': case ')': case '[': case ']': 
		  	case '{': case '}': case '<': case '>': case '-': case '\n': case ':' : return true;
		  	default: return false;
		  }
	}

	/**
	 * Adds to relevantTerms an integer representing the index of starting char of an occurrence of relevantTerm.
	 * 
	 * @param relevantTerm - the term wich occurred
	 * @param readPathFileUTF8 - the file in which relevantTerm occurred
	 * @param position - the index of starting character of this occurrence in the file
	 */
	private void addCharIndexToOccursList(String relevantTerm, String readPathFileUTF8, int position) {

		HashMap<String, ArrayList<Integer>> occurrFilesListTmp=null;
		ArrayList<Integer> occurrLinesListTmp=null;

		//prendo la lista di file associata al termine h 
		occurrFilesListTmp=relevantTerms.get(relevantTerm);

		/* ***VERBOSE****/
		if(verbose2) System.out.println("\nSono ModelProject.addLineToOccursList():"
				+"\nrelevantTerm= "+relevantTerm
				+"\nreadPathFileUTF8= "+readPathFileUTF8
				+"\nposition= "+position);
		/* ***VERBOSE****/

		if(occurrFilesListTmp==null){//se il relevantTerm non ha una lista di file associata la creo
		  relevantTerms.put( relevantTerm, new HashMap<String, ArrayList<Integer>>() );
		  occurrFilesListTmp=relevantTerms.get(relevantTerm);
		}

		//prendo la lista di occorrenze di relevantTerm associata al file readPathFileUTF8
		occurrLinesListTmp=occurrFilesListTmp.get(readPathFileUTF8);

		if(occurrLinesListTmp==null){//se il relevantTerm non ha una lista di occorrenze per quel file la creo
			occurrFilesListTmp.put( readPathFileUTF8, new ArrayList<Integer>() );
			occurrLinesListTmp=occurrFilesListTmp.get(readPathFileUTF8);
		}
		
		//aggiungo l'occorrenza
		occurrLinesListTmp.add(position);
		
	}

	/** Crea il nuovo progetto
	 * 
	 * @param s stringa contenente il nome del progetto
	 * 
	 * @return true se il progetto � stato creato correttamente
	 * @return false se si � verificato un errore
	 */
	public boolean createProject(String s)
	{
		fileProject = new ArrayList <ModelFile> ();
		
		workerProject = new ArrayList <Thread> ();
		
		nameProject = s;
		pathProject = "../"+savedProjectsDir+"/" + s;
		pathXML = pathProject + "/" + s + ".xml"; 
		pathCommonalitiesCandidates = pathProject + "/CommanalitiesC.log";
		pathCommonalitiesSelected = pathProject + "/CommanalitiesS.log";
		pathCommonalitiesSelectedHTML = pathProject + "/CommanalitiesS.html";

		pathVariabilitiesCandidates = pathProject + "/VariabilitiesC.log";
		pathVariabilitiesSelected = pathProject + "/VariabilitiesS.log";
		pathVariabilitiesSelectedHTML = pathProject + "/VariabilitiesS.html";
		
		pathRelevantTerms = pathProject + "/RelevantTerms.log";
		
		stateProject[0] = true;
		stateProject[1] = true;
		
		if(new File(pathProject).mkdir() == false)
			return false;		
		
		else{
//			stateProject[0] = true;
//			stateProject[1] = true;
			return true;
		}
	}
	
	/** Carica il progetto
	 * 
	 * @param s stringa contenente la path del progetto da caricare
	 * 
	 * @return al ArrayList contenenti i nomi dei file del progetto
	 */
	public ArrayList <String> loadProject(String s){
		if(s == null) return null;

		parserXML = new ParserXML();
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		
		try{
			fileProject = new ArrayList <ModelFile> ();
			
			workerProject = new ArrayList <Thread> ();
			
			nameProject = s.substring(0, s.length() - 4);
			pathProject = "../"+savedProjectsDir+"/" + nameProject;
			pathXML = pathProject + "/" + nameProject + ".xml"; 
			pathCommonalitiesCandidates = pathProject + "/CommanalitiesC.log";
			pathCommonalitiesSelected = pathProject + "/CommanalitiesS.log";
			pathCommonalitiesSelectedHTML = pathProject + "/CommanalitiesS.html";

			pathVariabilitiesCandidates = pathProject + "/VariabilitiesC.log";
			pathVariabilitiesSelected = pathProject + "/VariabilitiesS.log";
			pathVariabilitiesSelectedHTML = pathProject + "/VariabilitiesS.html";
			
			pathRelevantTerms = pathProject + "/RelevantTerms.log";
			
			SAXParser parser = spf.newSAXParser();
			parser.parse(pathXML, parserXML);
			
			fileProject = new ArrayList <ModelFile> ();
			
			for(int i = 0; i < parserXML.readPathInput().size(); i++){		
				fileProject.add(
						new ModelFile(
								parserXML.readPathInput().get(i), pathProject));
				workerProject.add(
						new Thread(
								fileProject.get(i)));
			}	
			
			commonalitiesCandidates = new ArrayList <String> ();
			commonalitiesSelected = new ArrayList <String> ();
			variabilitiesCandidates = new ArrayList <String> ();
			variabilitiesSelected = new ArrayList <String> ();
			
			loadFeaturesList(commonalitiesCandidates, pathCommonalitiesCandidates);
			loadFeaturesList(commonalitiesSelected, pathCommonalitiesSelected);
			loadFeaturesList(variabilitiesCandidates, pathVariabilitiesCandidates);
			loadFeaturesList(variabilitiesSelected, pathVariabilitiesSelected);
			
			if(!loadProjectRelevantTerms()) System.out.println("Project files corrupted!");
            
			return parserXML.readNameFile();
		} 
		catch (ParserConfigurationException e) 
		{
			System.out.println("Exception loadProject: " + e.getMessage());
			return null;
		}
		catch (SAXException e) 
		{
			System.out.println("Exception loadProject: " + e.getMessage());
			return null;
		}
		catch (IOException e) 
		{
			System.out.println("Exception loadProject: " + e.getMessage());
			return null;
		} 
	}

	/**
	 * Loads a list of features from a file, one per line.
	 * 
	 * @param features - the feature list to be loaded
	 * @param path - the path of the file to be used
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void loadFeaturesList(ArrayList<String> features, String path)
			throws FileNotFoundException, IOException {
		String s1=null;		
		BufferedReader br1 = new BufferedReader(new FileReader(path));
		while( (s1 = br1.readLine()) != null ) features.add(s1);
		br1.close();
		return;
	}
	
	/** Saves the project.
	 * 
	 * @return an xml file containing the project saved informations
	 */
	public File saveProject(){		
		String s ="<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>" + nameProject + "<node>Input";
		
		for(int i = 0; i < fileProject.size(); i++)
			s +=  "<leaf>" + new File(fileProject.get(i).readPathFile()).getName()
				  + "<path>" + fileProject.get(i).readPathFile() + "</path>" 
			    + "</leaf>";
		
		s += "</node><node>Commonalities</node></root>";
		
		try{
			PrintWriter pw1 =
			        new PrintWriter(
			        		new BufferedWriter(
			        				new FileWriter(pathXML)));
			pw1.print(s);
			pw1.close();
			
			//saving feature lists on files
			saveFeaturesList(commonalitiesCandidates, pathCommonalitiesCandidates);
			saveFeaturesList(commonalitiesSelected, pathCommonalitiesSelected);
			saveFeaturesList(variabilitiesCandidates, pathVariabilitiesCandidates);
			saveFeaturesList(variabilitiesSelected, pathVariabilitiesSelected);
			//saving selected feature lists on html tables	
			saveSelectedFeaturesHTML(commonalitiesSelected, pathCommonalitiesSelectedHTML, "Commonalities Selected");
			saveSelectedFeaturesHTML(variabilitiesSelected, pathVariabilitiesSelectedHTML, "Variabilities Selected");			

			saveProjectRelevantTerms();

			stateProject[0] = false;
			stateProject[1] = false;
		} 
		catch (IOException e){
			System.out.println("Exception saveProject: " + e.getMessage());
			return null;
		}
		return new File(pathXML);
	}

	/**
	 * Saves occurrences of relevant terms in every input files in a file, one term per line.
	 * 
	 * @throws IOException
	 */
	private void saveProjectRelevantTerms() throws IOException {
		String tmpLine="";
		HashMap<String, ArrayList<Integer>> tmpMap=null;
		Iterator<Entry<String, HashMap<String, ArrayList<Integer>>>> termIter=null;
		Entry<String, HashMap<String, ArrayList<Integer>>> termEntry=null;
		Iterator<Entry<String, ArrayList<Integer>>> fileIter=null;
		Entry<String, ArrayList<Integer>> fileEntry=null;
		
		PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(pathRelevantTerms)));
		
		if(relevantTerms != null){
		  termIter=relevantTerms.entrySet().iterator();
		  while(termIter.hasNext()){
			termEntry=termIter.next();
			
			tmpLine=termEntry.getKey()+" ";
			tmpMap=termEntry.getValue();
			fileIter=tmpMap.entrySet().iterator();
			while(fileIter.hasNext()){
			  fileEntry=fileIter.next();
			  
			  tmpLine+="f: "+fileEntry.getKey()+" i: ";
			  for(int index : fileEntry.getValue()) tmpLine+=index+" ";		
			}
			pw2.print(tmpLine+"\n");
			
		  }
		}
		pw2.close();
		return;
	}	

	/**
	 * Loads occurrences of relevant terms for every input files from a file, one term per line.
	 * 
	 * @throws IOException
	 */
	private boolean loadProjectRelevantTerms() throws IOException {
		String termName=null;
		String fileName=null;
		String[] tokens=null;
		HashMap<String, ArrayList<Integer>> fileMap=null;
		ArrayList<Integer> occurrences=null;
		BufferedReader br1 =null;
		
		String s1=null;		
		
		try{
		  br1 = new BufferedReader(new FileReader(pathRelevantTerms));			
		}catch(FileNotFoundException e){ return true;}
		relevantTerms=new HashMap<String, HashMap<String,ArrayList<Integer>>>();
		
		while( (s1 = br1.readLine()) != null ){
		  fileMap=new HashMap<String, ArrayList<Integer>>();
		  tokens=s1.split(" ");
		  System.out.println("Stampo i tokens!");
		  if(verbose3) for(String str:tokens) System.out.println(str);
		  termName=tokens[0];
		  if(verbose3) System.out.println("Trovato termine: "+termName);
		  for(int i=1;i<tokens.length; ++i){
			//a new file name has been found
			if(tokens[i].compareTo("f:")==0){ 
			  occurrences=new ArrayList<Integer>();
			  
			  ++i; fileName=tokens[i]; ++i;
			  while(tokens[i].compareTo("i:")!=0){
				fileName+=" "+tokens[i]; ++i;
			  }
			  
			  if(verbose3) System.out.println("\tTrovato file: "+fileName);
			  if(tokens[i].compareTo("i:")!=0){
				  if(verbose3) System.out.println("Uncorrect format for relevant terms file");
				  br1.close();
				  return false;
			  }
			  else ++i;
			  //loading occurrence indexes of this file
			  for(; i<tokens.length; ++i){
				if(verbose3) System.out.println("***Token: "+tokens[i]);
				if(tokens[i].compareTo("f:")==0){
				  fileMap.put(fileName, occurrences);
				  break;
				}
				occurrences.add(Integer.valueOf(tokens[i]));
				if(verbose3) System.out.println("\t\tTrovata occorrenza: "+tokens[i]);
			  }
			}
			fileMap.put(fileName, occurrences);
		  }
		  relevantTerms.put(termName, fileMap);
		}
		br1.close();
		return true;
	}	

	/**
	 * Stores a list of features in a file, one per line.
	 * 
	 * @param features - the feature list to be stored
	 * @param path - the path of the file to be used
	 * @throws IOException
	 */
	private void saveFeaturesList(ArrayList<String> features, String path) throws IOException {
		PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(path)));
		if(features != null) 
		  for(int i = 0; i < features.size(); i++) pw2.print(features.get(i) + "\n");
		pw2.close();
		return;
	}

	/**
	 * Saves a list of selected features in a table of an HTML file.
	 * 
	 * @param features - the feature list to be stored
	 * @param path - the path of the file  to be used
	 * @param tableHeader - string used as table header
	 * @throws IOException
	 */
	private void saveSelectedFeaturesHTML(ArrayList<String> features, String path, String tableHeader) throws IOException {
		String s=null;
		PrintWriter pw4 = new PrintWriter(new BufferedWriter(new FileWriter(path)));
		
		if(features != null){
			s = "<table border=\"2\" align=\"center\">";
//			s = "<table border=" + String.valueOf('"') + String.valueOf('2') + String.valueOf('"') + "align=" + String.valueOf('"') + "center" + String.valueOf('"') + ">";
			s += "<tr><th>n.</th><th>"+tableHeader+"</th></tr>";
			
			for(int i = 0; i < features.size(); i++)
				s += "<tr><td>" +i+ "</td><td>" + features.get(i) + "</td></tr>";
			
			s += "</table>";		
			pw4.print(s);	
			pw4.close();
		}
	}
	
	/** Cancella il progetto
	 * 
	 */
	public void deleteProject()
	{
		File f1 = new File(pathProject);
		
		File [] f2 = f1.listFiles();
		
		for(int i = 0; i < f2.length; i++)
			f2[i].delete();
		
		f1.delete();
		
		stateProject[0] = false;
		stateProject[1] = false;

	}
	
	/**
	 * Analizes project input files.
	 */
	public void analizesFileProject(){
	  for(int i = 0; i < fileProject.size(); i++){
		if(workerProject.get(i).getState() != Thread.State.TERMINATED) workerProject.get(i).start();
		else{
		  workerProject.set(i, new Thread(fileProject.get(i)));
		  workerProject.get(i).start();
		}
	  }
		
	  handlerProject = new Thread(this);
	  handlerProject.start();
	}
	
	/**
	 * Extract variabilities from relevant terms in the model files
	 * 
	 */
	public void extractVariabilities(){
	  variabilitiesCandidates= new ArrayList<String>();
	  Thread variabilitiesExtraction = new Thread(
	    new Runnable() {
			
		  @Override
		  public void run() {
		    //extracting variabilities candidates from first input File
			for(int i = 0; i < fileProject.size(); i++){
			  for(int j = 0; j < fileProject.get(i).readTermRelevant().size(); j = j + 1){
			    if( !commonalitiesCandidates.contains(fileProject.get(i).readTermRelevant().get(j)) &&
			    	!variabilitiesCandidates.contains(fileProject.get(i).readTermRelevant().get(j)))
				  variabilitiesCandidates.add(fileProject.get(i).readTermRelevant().get(j));
			  }
		  	}
			setChanged();
			notifyObservers("End Extract Variabilities");
		  }

		});
		
		variabilitiesExtraction.start();

	}


	
	/**
	 * Loads the analisys files of the project.
	 */
	public ArrayList <String> loadAnalysisFileProject(){
	  ArrayList <String> al = new ArrayList <String> ();

	  for(int i = 0; i < fileProject.size(); i++){
		if(new File(fileProject.get(i).readPathFileUTF8()).exists()){
		  if(workerProject.get(i).getState() != Thread.State.TERMINATED){
			workerProject.get(i).start();
			al.add(String.valueOf(i));
			try{
			  workerProject.get(i).join();
			}catch (InterruptedException e){
			  System.out.println("Exception loadAnalysisFileProject: " + e.getMessage());
			}
		  }
		  else{
			workerProject.set(i, new Thread(fileProject.get(i)));
			workerProject.get(i).start();
			al.add(String.valueOf(i));
			try{
			  workerProject.get(i).join();
			}catch (InterruptedException e){
			  System.out.println("Exception loadAnalysisFileProject: " + e.getMessage());
			}
		  }
		}
	  }
	  return al;
	}
	
	/** Aggiunge un elemento al progetto
	 * 
	 * @param s path del file 
	 */
	public void addFileProject(String s)
	{
		fileProject.add(new ModelFile(s, pathProject));
		workerProject.add(
				new Thread(
						fileProject.get(fileProject.size()-1)));
		stateProject[1] = true;
	}
	
	/**
	 * Removes an input file from the project.
	 * 
	 * @param i - index of the file
	 */
	public void removeFileProject(int i){
	  File logFile =null;
	  if(fileProject.get(i).readPathFileUTF8() != null)
		  new File(fileProject.get(i).readPathFileUTF8()).delete();
		
	  if(fileProject.get(i).readPathFileHTML() != null)
		  for(int j = 0; j < fileProject.get(i).readPathFileHTML().size(); j++)
			  new File(fileProject.get(i).readPathFileHTML().get(j)).delete();

	  logFile=new File(
		(fileProject.get(i).readPathFileUTF8().substring(0, fileProject.get(i).readPathFileUTF8().length()-4)) + ".log");
	  
	  if(logFile.exists()) logFile.delete();

	  fileProject.remove(i);
	  workerProject.remove(i);
//	  stateProject[1] = false;
	  stateProject[1] = true;
	}
	
	/* -= FUNZIONI lettura parametri =- */
	
	/** Legge le path dell'analisi associate ad un file 
	 * 
	 * @param i intero contenente l'indice del file
	 * 
	 * @return s[4] stringhe contenenti le path dell'analisi associate al file scelto
	 */
	
	public String [] readAnalysisFile(int i)
	{
		String [] s = new String[4];
		
		s[0] = fileProject.get(i).readPathFileUTF8();
		
		if(fileProject.get(i).readPathFileHTML() == null)
			return null;
		
		s[1] = fileProject.get(i).readPathFileHTML().get(1);
		s[2] = fileProject.get(i).readPathFileHTML().get(2);
		s[3] = fileProject.get(i).readPathFileHTML().get(3);
		
		return s;
	}
	
	/** Legge i termini rilevanti di un file
	 * 
	 * @param i intero contenente l'indice del file
	 * 
	 * @return al ArrayList contenente i termini rilevanti del file scelto
	 */
	
	public ArrayList <String> readTermRelevantFile(int i)
	{
		return fileProject.get(i).readTermRelevant();
	}
	
	/** Legge le path dei file HTML contenenti i termini rilevanti 
	 * 
	 * @return al ArrayList contenenti le path dei file HTML contenenti i termini rilevanti
	 */
	public ArrayList <String> readPathHTMLTermRelevantFile()
	{
		ArrayList <String> al = new ArrayList <String> ();
		
		for(int i = 0; i < fileProject.size(); i++)
			if(fileProject.get(i).readPathFileHTML() != null)
				al.add(fileProject.get(i).readPathFileHTML().get(2));
		
		return al;
	}
	
	/** Legge le commonalities candidate
	 * 
	 * @return ArrayList contenente le commonalities candidates
	 */
	public ArrayList <String> readCommonalitiesCandidates()
	{
		return commonalitiesCandidates;
	}
	
	/** Legge le commonalities selezionate
	 * 
	 * @return ArrayList contenente le commonalities candidates
	 */
	public ArrayList <String> readCommonalitiesSelected()
	{
		return commonalitiesSelected;
	}
	
	/** Legge le commonalities candidate
	 * 
	 * @return ArrayList contenente le commonalities candidates
	 */
	public ArrayList <String> readVariabilitiesCandidates()
	{
		return variabilitiesCandidates;
	}
	
	/** Legge le commonalities selezionate
	 * 
	 * @return ArrayList contenente le commonalities candidates
	 */
	public ArrayList <String> readVariabilitiesSelected()
	{
		return variabilitiesSelected;
	}
	
//	/** Legge la stringa contenente il percorso della pagina HTML delle commonalities selected
//	 * 
//	 * @return pathCommanalitiesSelectedHTML
//	 */
//	public String readPathCommonalitiesSelectedHTML()
//	{
//		return pathCommonalitiesSelectedHTML;
//	}
	
	/** Legge la stringa contenente il percorso della pagina HTML delle commonalities selected
	 * 
	 * @return pathCommanalitiesSelectedHTML
	 */
	public String readPathVariabilitiesSelectedHTML()
	{
		return pathVariabilitiesSelectedHTML;
	}

	/** 
	 * Create the HTML file containing the selected features
	 * 
	 * @param al - ArrayList containing the selected features
	 * @param type - type of the selected features, a constant from ViewPanelCentral.FeatureType
	 */
	public void setFeaturesSelected(ArrayList<String> al, ViewPanelCentral.FeatureType type)
	{
		ArrayList<String> tmp = new ArrayList<String> ();
		
		PrintWriter pw = null;
		
		for(int i = 0; i < al.size(); i++) tmp.add(al.get(i));

		if(type==ViewPanelCentral.FeatureType.COMMONALITIES) commonalitiesSelected=tmp;
		else variabilitiesSelected=tmp;

		try {
			if(type==ViewPanelCentral.FeatureType.COMMONALITIES)
				pw = new PrintWriter(new BufferedWriter(new FileWriter(pathCommonalitiesSelectedHTML)));
			else
				pw = new PrintWriter(new BufferedWriter(new FileWriter(pathVariabilitiesSelectedHTML)));
			
			String s = "<table border=\"2\" align=\"center\">";
			
			if(type==ViewPanelCentral.FeatureType.COMMONALITIES)
				s += "<tr><th>n.</th><th>Selected Commonalities</th></tr>";
			else
				s += "<tr><th>n.</th><th>Selected Variabilities</th></tr>";
				
			for(int i = 0; i < tmp.size(); i++)
				s += "<tr><td style=\"width: auto;\">" +(i+1)+ "</td><td style=\"width: auto;\">" + tmp.get(i) + "</td></tr>";
			
			s += "</table>";		
			pw.print(s);
	        pw.close();
	        setChanged();
			if(type==ViewPanelCentral.FeatureType.COMMONALITIES)
				notifyObservers("End Commonalities Selected");
			else
				notifyObservers("End Variabilities Selected");
		} 
		catch (IOException e) 
		{
			System.out.println("Exception readCommonalitiesSelectedFileHTML: " + e.getMessage());
			return;
		}
	}
	
	/** Legge la path in cui risiede il file HTML contenente le commonalities selezionate
	 * 
	 * @return Stringa contenente la path corrispondente
	 */
	public String readPathCommonalitiesSelectedHTML()
	{
		return pathCommonalitiesSelectedHTML;
	}
	
	/** Legge lo stato del progetto
	 * 
	 * @return stateProject
	 */
	public boolean [] readStateProject()
	{
		return stateProject;
	}
	/* -= FUNZIONI Ausiliarie =- */
	
	/** Effettua l'intersezione dei termini rilevanti dei file
	 * 
	 * @param s Stringa contenente un termine rilevante del primo file
	 * 
	 * @return true se tutti gli altri file contengono il valore s
	 * @return false se esiste almeno un file che non contiene s
	 */
	
	private boolean intersectTermRelevant(String s)
	{
		for(int i = 1; i < fileProject.size(); i++)
		{
			int j = 0;
			
			while(j < fileProject.get(i).readTermRelevant().size())
			{
				if(fileProject.get(i).readTermRelevant().get(j).equals(s))
					break;
				
				j++;
			}
			
			if(j >= fileProject.get(i).readTermRelevant().size())
				return false;
		}
		return true;
	}

	/**
	 * Returns the HashMap containing the relevant Terms for the project input files.
	 * 
	 * @return - relevantTerms, an object having type HashMap<String, HashMap<String, ArrayList<Integer>>>
	 */
	public HashMap<String, HashMap<String, ArrayList<Integer>>> getRelevantTerms() {		
		return relevantTerms;
	}

	/**
	 * Returns the path used for saving the project.
	 * @return - the path used for saving the project
	 */
	public String getPathProject() {
		return pathProject;
	}
}
