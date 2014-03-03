/**
 * 
 * @author Daniele Cicciarella
 *
 */
package view;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import main.CombinatoryUtils;

import org.xml.sax.SAXException;

import view.ViewPanelCentral.FeatureType;


public class ModelProject extends Observable implements Runnable{

	private static boolean verbose=false;//variable used to activate prints in the code
	
	private static boolean verbose2=false;//variable used to activate prints in the code

	private static boolean verbose3=false;//variable used to activate prints in the code
	
	private static boolean debug=false;//variable used to activate debug prints in the code

	private static boolean debug2=false;//variable used to activate debug prints in the code
	
	private static boolean debugColors=true;//variable used to activate debug prints in the code
	
	
	private static final String savedProjectsDir = "Usage Tries";
	
	/** String containing the project name*/
	private String nameProject = null;
	
	/** String containing the project path*/
	private String pathProject = null;
	
	/** String containing the project's XML file path*/
	private String pathXML = null;
	
	/** ParserXML containing the XML file parser*/
	private ParserXML parserXML = null;
	
	/** ArrayList containing the project's file models*/
	private ArrayList <ModelFile> filesProject = null;
	
	/** ArrayList containing the file model threads*/
	private ArrayList <Thread> workerProject = null;
	
	/** Thread that manages project analisys*/
	private Thread handlerProject = null;
	
	/** String containing the project's commonalities candidates path*/
	private String pathCommonalitiesCandidates = null;
	
	/** String containing the project's commonalities selected path*/
	private String pathCommonalitiesSelected = null;
	
	/** String containing the commonalities selected HTML page path*/
	private String pathCommonalitiesSelectedHTML = null;
	
	/** String containing the project's variabilities candidates path*/
	private String pathVariabilitiesCandidates = null;
	
	/** String containing the project's variabilities selected path*/
	private String pathVariabilitiesSelected = null;
	
	/** String containing the variabilities selected HTML page path*/
	private String pathVariabilitiesSelectedHTML = null;
	
	/** Contains all project's relevant terms, in both original version and term-extraction version*/
	private ArrayList<String[]> relevantTermsVersions=null; 

	/** Relevant terms set. For each, there is a corresponding input file names list, and for each file
	 * there is a corresponding list of integers, the indexes of term occurrence in that file*/
	private HashMap<String, HashMap<String, ArrayList<Integer>>> relevantTerms=null;

	/** Contains a terms set for each sentence of all input files */
	private ArrayList<ArrayList<String>> termsInSentencesSet=null;

	/** Contains the arity for all terms of all input files */
	private HashMap<String, Integer> termsArity=null;

	/** Contains the color for all terms of all input files */
	private HashMap<String, int[]> termsColor=null;
	
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
	
	/** Minimum intersection size required to join two sets of terms into one, used to assign color to terms */
	private static int minimumIntersectionSize=2;
	
	/** 
	 * Waits for threads workerProject to end their work, and computes commonalities candidates
	 */
	@Override
	public void run(){
	  //variables used to calculate occurencies lists
	  String line=null;							//line read from an input file 	  
	  int charcount=0;							//starting position of a line in the file
	  int index=0;								//index of a possible relevant term occurence in line

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
	  
	  //collecting relevant terms from all models, in both original version and term-extraction version
	  relevantTermsVersions=new ArrayList<String[]>();
	  for(int k=0; k<filesProject.size(); k++){
		  
		/* ****VERBOSE */
		if(verbose){
		  System.out.println("***Printing all terms from "+filesProject.get(k).readPathFile());
		  for(String[] str : filesProject.get(k).readTermRelevant())
			  System.out.println("array: "+str+"\tlength: "+str.length);
//			  System.out.println("computed: "+str[0]+"\noriginal: "+str[1]);
		}
		/* ****VERBOSE */

		joinRelevantTerms(relevantTermsVersions, filesProject.get(k).readTermRelevant());
		  
	  }

	  /* ****VERBOSE */
	  if(verbose){
		System.out.println("Listing allFilesRelevantTerms:");
		for(String[] str: relevantTermsVersions) System.out.println("Computed: "+str[0]+"\nOriginal:"+str[1]);
	  }
	  /* ****VERBOSE */

	  //saving the positions in input files of relevant terms occurences 
	  relevantTerms=new HashMap<String, HashMap<String, ArrayList<Integer>>>();
	  for(int k=0; k<filesProject.size(); k++){//for each file
		BufferedReader reader = null;
		try {
//		  reader = new BufferedReader(new FileReader(filesProject.get(k).readPathFileUTF8()));
		  reader = new BufferedReader(new StringReader(filesProject.get(k).readTextUTF8()));
		  charcount=0;
		  while((line = reader.readLine()) != null){//for each line
//			for(int h=0; h<filesProject.get(k).readTermRelevant().size(); h++){//for each relevant term
			for(int h=0; h<relevantTermsVersions.size(); h++){//for each relevant term
			  index=0;
			  while(index<line.length()){//for each occurrence
				//get next occurrence
//				index = line.toUpperCase().indexOf(filesProject.get(k).readTermRelevant().get(h).toUpperCase(), index);
				index = line.toUpperCase().indexOf(relevantTermsVersions.get(h)[1].toUpperCase(), index);

				if (index == -1) break;//start checking next relevant term occurrences in this line

				//add occurrence to relevantTerms, if it is valid
//				if (isValidOccurrence(filesProject.get(k).readTermRelevant().get(h), line, index))
//					  addCharIndexToOccursList(filesProject.get(k).readTermRelevant().get(h),
//							  filesProject.get(k).readPathFileUTF8(), charcount+index);

				if (isValidOccurrence(relevantTermsVersions.get(h)[1], line, index))
					  addCharIndexToOccursList(relevantTermsVersions.get(h)[0],
							  filesProject.get(k).readPathFileUTF8(), charcount+index);

				//incrementing index to search for next occurrence
//				index+=filesProject.get(k).readTermRelevant().get(h).length();
				index+=relevantTermsVersions.get(h)[1].length();
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
	  
	  
	  if(verbose){
		Iterator<Entry<String, HashMap<String, ArrayList<Integer>>>> termIter = relevantTerms.entrySet().iterator();
		Entry<String, HashMap<String, ArrayList<Integer>>> termEntry=null;

		Iterator<Entry<String, ArrayList<Integer>>> fileIter = null;
		Entry<String, ArrayList<Integer>> fileEntry=null;

		String termName=null;
		while(termIter.hasNext()){
		  termEntry=termIter.next();
		  termName=termEntry.getKey();
		  System.out.println("\n***Term: "+termName);
		  fileIter=termEntry.getValue().entrySet().iterator();
		  while(fileIter.hasNext()){
			fileEntry=fileIter.next();
			System.out.println("******File: "+fileEntry.getKey());
		  }
		}	  
	  }
	  
	  //calculating relevant terms colors
	  /* ***DEBUG*** */
	  if(debugColors) for(int k=0; k<filesProject.size(); k++){//for each file
	    termsInSentencesSet=filesProject.get(k).getTermsInSentencesSet();
	    System.out.println("\n***Printing all sentences sets for file "+filesProject.get(k).readPathFile()+":\n");
	    for(int i=0; i<termsInSentencesSet.size(); ++i){
		  System.out.println("\nSet#"+i+":");
		  for(String relTerm: termsInSentencesSet.get(i)) System.out.println(relTerm);
	    }
	  }
      /* ***DEBUG*** */  


	  //creating global structures needed to assign colors
	  buildColorStructures();

	  //extracting communalities candidates from first input File
	  for(int j = 0; j < filesProject.get(0).readTermRelevant().size(); j = j + 1)
       	if(intersectTermRelevant(filesProject.get(0).readTermRelevant().get(j)[0]))
       	  commonalitiesCandidates.add(filesProject.get(0).readTermRelevant().get(j)[0]);
		
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
		  for(ModelFile file: filesProject){
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
	  
	  
//	  Iterator<Entry<String, HashMap<String, ArrayList<Integer>>>> iter = relevantTerms.entrySet().iterator();
//	  Entry<String, HashMap<String, ArrayList<Integer>>> entry=null;
//	  Iterator<Entry<String, HashMap<String, ArrayList<Integer>>>> fileIter=null;
//	  ArrayList<Integer> tmpList=null;
//	  String tmpTerm=null;
//	  System.out.println("\n*****printing terms in relevantTerms: ");
//	  while(iter.hasNext()){
//		entry=iter.next();
//		tmpTerm=entry.getKey();
////		tmpList=entry.getValue();
//		System.out.println(tmpTerm);
////		for(int i : tmpList) System.out.println(i);
//	  }
	  
	  setChanged();
	  notifyObservers("End Extract Commonalities");
	}

	/**
	 * Adds all elements from list to globalList, except for those already present in it.
	 * 
	 * @param globalList - the list in which to add elements from list
	 * @param list - the list from which to get elements to be added
	 */
	private void joinRelevantTerms(ArrayList<String[]> globalList, ArrayList<String[]> list) {
	  int i=0;
	  for(String[] elementToAdd : list){
		for(i=0; i<globalList.size(); ++i)
		  if(globalList.get(i)[0].compareTo(elementToAdd[0])==0) break;

		if(i==globalList.size()) globalList.add(elementToAdd);
	  }
	}

	/**
	 * Builds the global structures used to assign a color to each term. More specifically, this will build <br>
	 * the following object members: termsInSentencesSet, termsArity, termsColor.
	 * 
	 * @see termsInSentencesSet
	 * @see termsArity
	 * @see termsColor
	 */
	private void buildColorStructures() {
	  int intersectionSize;	  
	  Iterator<Entry<String, Integer>> arityIterator=null;
	  Entry<String, Integer> arityEntry=null;
	  double card=0;
	  int cardUpper=0;
	  int[] values=null;
	  int colorOffset=0;
	  int[][] colors=null;
	  double maxColorReduction=0;
	  double colorReductionUnit=0;
	  int maxArity=0;
	  int[] baseColor=null;
	  int[] termColor=null;
	  Iterator<Entry<String, int[]>> colorIter = null;
	  Entry<String, int[]> colorEntry = null;
	  
	  //creating a global set, containing a set of terms for each sentence in all input files
	  termsInSentencesSet=new ArrayList<ArrayList<String>>();
	  //creating an arity-among-all-files-sentences map for all relevant terms
	  termsArity=new HashMap<String, Integer>();
	  //creating a color map for all relevant terms
	  termsColor= new HashMap<String, int[]>();

	  for(ModelFile model: filesProject ){
		for(ArrayList<String> set : model.getTermsInSentencesSet()) termsInSentencesSet.add(set);
		arityIterator=model.getTermsAriety().entrySet().iterator();
		while(arityIterator.hasNext()){
		  arityEntry=arityIterator.next();
		  if(termsArity.get(arityEntry.getKey())==null)
			termsArity.put(arityEntry.getKey(), arityEntry.getValue());
		  else termsArity.put(arityEntry.getKey(), termsArity.get(arityEntry.getKey())+arityEntry.getValue());
		}
	  }
	  
	  //analisys phase is still to be done
	  if(termsInSentencesSet.size()==0) return;
	  
	  /* ***DEBUG*** */
	  if(debugColors){
	    System.out.println("\nNumero totale di insiemi: "+termsInSentencesSet.size());
	    System.out.println("Lista delle arietà totali dei termini:\n");
	    arityIterator=termsArity.entrySet().iterator();
	    while(arityIterator.hasNext()){
	      arityEntry=arityIterator.next();
	      System.out.println(arityEntry.getKey()+": "+arityEntry.getValue());		  
	    }
	  }
	  /* ***DEBUG*** */

	  //joining all intersected sets until only disjointed sets remains
	  for(int k=0; k<termsInSentencesSet.size(); ++k){
		for(int l=k+1; l<termsInSentencesSet.size(); ++l){  
		  intersectionSize=0;
		  for(String term : termsInSentencesSet.get(k)) if(termsInSentencesSet.get(l).contains(term)){
			++intersectionSize;
			if(minimumIntersectionSize==intersectionSize) break;
		  }		  
		  if(minimumIntersectionSize==intersectionSize){
			//joining two sets into the first
			joinSets(termsInSentencesSet.get(k), termsInSentencesSet.get(l));
			termsInSentencesSet.remove(l);
			//restarting cycle after join
			k=-1; break;
		  }
		}
	  }

	  //calculating all possible RGB combinations to get base colors for the sets
	  if(termsInSentencesSet.size()==1){
		colors=new int[1][]; int[] oneColor=new int[3]; 
		oneColor[0]=255; oneColor[1]=0; oneColor[2]=0;
		colors[0]=oneColor;
		colorOffset=255;
	  }
	  else{
		card=Math.pow(termsInSentencesSet.size(), 1.0/3);
		cardUpper=((int)card<card)? (int)card+1: (int)card;
		values=new int[cardUpper];
		colorOffset=255/(cardUpper-1);
		values[0]=0;
		for(int i=1; i<values.length-1; ++i){
		  values[i]=colorOffset*i;
		}
		values[values.length-1]=255;
		colors=CombinatoryUtils.threePositionsCombinationsAsIntegers(values);
	  }
		  
	  /* ***DEBUG*** */
	  if(debugColors){
	    System.out.println("\n\nNumero totale di insiemi: "+termsInSentencesSet.size());
	    System.out.println("Radice cubica: "+card);
	    System.out.println("Radice cubica[UPPER]: "+cardUpper);
	    System.out.println("Elenco elementi per ogni insieme: ");
	    for(int k=0; k<termsInSentencesSet.size(); ++k){
		  System.out.println("**Set#"+k);
		  for(String term : termsInSentencesSet.get(k)) System.out.println(term);
	    }
	    for(int k=0; k<colors.length; ++k){
		  System.out.println("**Color#"+k+": "+colors[k][0]+"."+colors[k][1]+"."+colors[k][2]);
	    }
	  }
	  /* ***DEBUG*** */
		  
	  //assigning colors to terms
	  maxColorReduction=((double)colorOffset/2.0)/255.0;
	  colorReductionUnit=0;
	  maxArity=0;
	  baseColor=null;
	  termColor=null;

	  for(int k=0; k<termsInSentencesSet.size(); ++k){
		//getting the term with max arity in the set
		for(String term : termsInSentencesSet.get(k)){
		  if(termsArity.get(term)>maxArity){
		    maxArity=termsArity.get(term);
		  }
		}
		colorReductionUnit=maxColorReduction/(maxArity-1);

		//assigning colors
		baseColor=colors[k];
		for(String term : termsInSentencesSet.get(k)){
		  termColor=new int[3];
		  termColor[0]=(int)((double)baseColor[0]*(1.0-colorReductionUnit*(maxArity-termsArity.get(term))));			
		  termColor[1]=(int)((double)baseColor[1]*(1.0-colorReductionUnit*(maxArity-termsArity.get(term))));			
		  termColor[2]=(int)((double)baseColor[2]*(1.0-colorReductionUnit*(maxArity-termsArity.get(term))));			
		  termsColor.put(term, termColor);
		  
		  /* ***DEBUG*** */
		  if(debugColors)
		    System.out.println("Base Color for term '"+term+"' is: ("+baseColor[0]+"."+baseColor[1]+"."+baseColor[2]+")"
			+"\nmaxArity: "+maxArity+"\ttermsArity.get(term): "+termsArity.get(term)+"\tcolorReductionUnit: "+colorReductionUnit
		    +"\nExact Color for term '"+term+"' is: ("+termColor[0]+"."+termColor[1]+"."+termColor[2]+")");
		  /* ***DEBUG*** */
		}
	  }
		  
	}
	
	/**
	 * Join two sets into the first. After a call to this method,<br>
	 * array1 will be the union of the two arrays, array2 will remain unchanged
	 * 
	 * @param array1 - first array to be joined, this will be changed
	 * @param array2 - first array to be joined, this will not change 
	 */
	private void joinSets(ArrayList<String> array1, ArrayList<String> array2) {
	  for(String tmp : array2) if(!array1.contains(tmp)) array1.add(tmp);
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
		  if (!isValidPrevOccurrChar(line.charAt(index-1)) ) return false;
		//checking sequent character, if present
		if(index+term.length()<line.length())
		  if (!isValidSeqOccurrChar(line.charAt(index+term.length())) ) return false;
		return true;
	}

	/**
	 * Check if c is a valid before-start or after-end character for a relevant term occurrence.<br>
	 * To be valid, c must be ' ', '.', ',', '(', ')', '[', ']', '{', '}', '<', '>', '-', '"' or newline.
	 * 
	 * @param c - the character to be checked
	 * @return true if c is valid, false otherwise
	 */
	protected static boolean isValidPrevOccurrChar(char c) {
	  switch(c){
	    case ' ': case '.': case ',': case '(': case ')': case '[': case ']': 
	    case '{': case '}': case '<': case '>': case '-': case ':': case '"':
	    case '\n': case '/': case '\\': case '\'': case ';': return true;
	    default: return false;
	  }
	}

	/**
	 * Check if c is a valid before-start or after-end character for a relevant term occurrence.<br>
	 * To be valid, c must be ' ', '.', ',', '(', ')', '[', ']', '{', '}', '<', '>', '-', '"' or newline.
	 * 
	 * @param c - the character to be checked
	 * @return true if c is valid, false otherwise
	 */
	protected static boolean isValidSeqOccurrChar(char c) {
	  switch(c){
	    case ' ': case '.': case ',': case '(': case ')': case '[': case ']': 
	    case '{': case '}': case '<': case '>': case '-': case ':': case '"':
	    case '\n': case '/': case '\\': case '\'': case ';': return true;
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
		filesProject = new ArrayList <ModelFile> ();
		
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
	
	/**
	 * Loads the analisys files of the project.
	 */
	public ArrayList <String> loadAnalysisFileProject(){
	  ArrayList <String> al = new ArrayList <String> ();

	  for(int i = 0; i < filesProject.size(); i++){
		if(new File(filesProject.get(i).readPathFileUTF8()).exists()){
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
			workerProject.set(i, new Thread(filesProject.get(i)));
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
	public void analyzesFileProject(){
	  for(int i = 0; i < filesProject.size(); i++){
		if(workerProject.get(i).getState() != Thread.State.TERMINATED) workerProject.get(i).start();
		else{
		  workerProject.set(i, new Thread(filesProject.get(i)));
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
			for(int i = 0; i < filesProject.size(); i++){
			  for(int j = 0; j < filesProject.get(i).readTermRelevant().size(); j = j + 1){
			    if( !commonalitiesCandidates.contains(filesProject.get(i).readTermRelevant().get(j)[0]) &&
			    	!variabilitiesCandidates.contains(filesProject.get(i).readTermRelevant().get(j)[0]))
				  variabilitiesCandidates.add(filesProject.get(i).readTermRelevant().get(j)[0]);
			  }
		  	}
			setChanged();
			notifyObservers("End Extract Variabilities");
		  }

		});
		
		variabilitiesExtraction.start();

	}
	
	/** 
 	 * Adds an input file to the  project.
	 * 
	 * @param s - file path 
	 */
	public void addFileProject(String s){
		filesProject.add(new ModelFile(s, pathProject));
		workerProject.add(new Thread(filesProject.get(filesProject.size()-1)));
		stateProject[1] = true;
	}
	
	/** 
 	 * Adds an analisys folder to the  project, generating input file and analisys files.
	 * 
	 * @param s - analisys folder path 
	 */
	public void addAnalisysFolderProject(String[] files){
		ArrayList<Point> sentencesBoundaries=null;
		
		//creating model
		ModelFile newModel=new ModelFile(files[0], pathProject);
		newModel.setIsAnalisysDir(true);
		filesProject.add(newModel);
		workerProject.add(new Thread(newModel));
		stateProject[1] = true;

		//creating model UTF8 input file
		newModel.setPathFileHTML(new ArrayList<String>());
		if(newModel.filterFile()==null){
		  setChanged();
		  notifyObservers("Analisys folder can't be accepted");
		  return;
		};         
        
        //creating result html pages as project files
		newModel.createResultFileInputText(files[0]);
		sentencesBoundaries=newModel.createResultFilePostTagging(files[2]);
		//post tagging HTML file is needed to build term extractor HTML file
		newModel.createResultFileTermExtractor(files[1], files[3], sentencesBoundaries);

		setChanged();
		notifyObservers("New Analisys Folder Loaded");
		
//		frameProject.setEnabled(false);
//		modelProject.analyzesFileProject();
//		setStateThrobber(false);
//		throbber = new Thread(this);
//		throbber.start();
//		frameProject.repaint();

//		frameProject.setEnabled(false);
//		modelProject.analyzesFileProject();
//		setStateThrobber(false);
//		throbber = new Thread(this);
//		throbber.start();
//		frameProject.repaint();
		        

	}

	/**
	 * Removes an input file from the project.
	 * 
	 * @param i - index of the file
	 */
	public void removeFileProject(int i){
	  File termLogFile =null;
	  //deleting UTF8 version of input file
	  if(filesProject.get(i).readPathFileUTF8() != null)
		  new File(filesProject.get(i).readPathFileUTF8()).delete();
		
	  //deleting html analisys files
	  if(filesProject.get(i).readPathFileHTML() != null)
		  for(int j = 0; j < filesProject.get(i).readPathFileHTML().size(); j++)
			  new File(filesProject.get(i).readPathFileHTML().get(j)).delete();

	  //deleting relevant terms file
	  termLogFile=new File(
		(filesProject.get(i).readPathFileUTF8().substring(0, filesProject.get(i).readPathFileUTF8().length()-4)) + ".log");
	  
	  if(termLogFile.exists()) termLogFile.delete();

	  filesProject.remove(i);
	  workerProject.remove(i);
//	  stateProject[1] = false;
	  stateProject[1] = true;
	}
	
	/* -= FUNZIONI lettura parametri =- */
	
	
	/** 
	 * Reads analisys files paths.
	 * 
	 * @param i - input file index in the project
	 * @return - a String[] containing the paths to the chosen input file analisys files
	 */
	
	public String [] readAnalysisFile(int i){
//		String [] s = new String[4];		
		String [] s = new String[3];		
		s[0] = filesProject.get(i).readPathFileUTF8();
		
		if(filesProject.get(i).readPathFileHTML() == null) return null;
		
		s[1] = filesProject.get(i).readPathFileHTML().get(1);
		s[2] = filesProject.get(i).readPathFileHTML().get(2);
//		s[3] = filesProject.get(i).readPathFileHTML().get(3);		
		return s;
	}
	
	/** 
	 * Reads the relevant terms of a file.
	 * 
	 * @param i - file index in the project
	 * @return - an ArrayList<String> contining the relevant terms of the chosen file
	 */	
	public ArrayList<String> readTermRelevantFile(int i){
		ArrayList<String> terms = new ArrayList<String>();
		for(String[] tmp : filesProject.get(i).readTermRelevant())
		  terms.add(tmp[0]);
//		return filesProject.get(i).readTermRelevant();
		return terms;
	}
	
	/** Legge le path dei file HTML contenenti i termini rilevanti 
	 * 
	 * @return al ArrayList contenenti le path dei file HTML contenenti i termini rilevanti
	 */
	public ArrayList <String> readPathHTMLTermRelevantFile()
	{
		ArrayList <String> al = new ArrayList <String> ();
		
		for(int i = 0; i < filesProject.size(); i++)
			if(filesProject.get(i).readPathFileHTML() != null)
				al.add(filesProject.get(i).readPathFileHTML().get(2));
		
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
	
	/** 
	 * Returns the String containing the path to the commonalities selected HTML page
	 * 
	 * @return - the String pathCommanalitiesSelectedHTML
	 */
	public String readPathCommonalitiesSelectedHTML(){
	  return pathCommonalitiesSelectedHTML;
	}
	
	/** 
	 * Returns the String containing the path to the variabilities selected HTML page
	 * 
	 * @return - the String pathVariabilitiesSelectedHTML
	 */
	public String readPathVariabilitiesSelectedHTML(){
	  return pathVariabilitiesSelectedHTML;
	}

	/** 
	 * Create the HTML file containing the selected features and the related structure.
	 * 
	 * @param al - ArrayList containing the selected features
	 * @param type - type of the selected features, a constant from ViewPanelCentral.FeatureType
	 */
	public void setFeaturesSelected(ArrayList<String> al, ViewPanelCentral.FeatureType type){
		ArrayList<String> tmp = new ArrayList<String> ();
		
		if(al!=null) for(int i = 0; i < al.size(); i++) tmp.add(al.get(i));

		if(type==ViewPanelCentral.FeatureType.COMMONALITIES) commonalitiesSelected=tmp;
		else variabilitiesSelected=tmp;

		saveFeaturesSelected(tmp, type);
		setChanged();
		if(type==ViewPanelCentral.FeatureType.COMMONALITIES)
		  notifyObservers("End Commonalities Selected");
		else
		  notifyObservers("End Variabilities Selected");
	}

	/**
	 * Writes selected feature on a save file.
	 * 
	 * @param al - ArrayList containing the selected features
	 * @param type - type of the selected features, a constant from ViewPanelCentral.FeatureType
	 */
	private void saveFeaturesSelected(ArrayList<String> al, ViewPanelCentral.FeatureType type) {
		PrintWriter pw=null;
		try {
		  if(type==ViewPanelCentral.FeatureType.COMMONALITIES)
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pathCommonalitiesSelectedHTML)));
		  else
			pw = new PrintWriter(new BufferedWriter(new FileWriter(pathVariabilitiesSelectedHTML)));
			
		  String s = 
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"it\" lang=\"it\"><head>"						
			+"<style type=\"text/css\">html { overflow-y: scroll; }"
			+"body{	font-family: Arial, Verdana, sans-serif;	font-size: 100%;}"
			+"input.tt_button{border: 1px solid #006;}"
			+"table.tt_footer{text-align: center; font-family: Verdana, Geneva, Arial, Helvetica, sans-serif ;"
			+"font-size: 10px;	color: #444;}"
			+"#wrapper{	margin: 0 auto;	width: 800px;}#content{	width: 100%;}"	
			+"</style></head><body>"			
			+"<table border=\"2\" align=\"center\">";
			
		  if(type==ViewPanelCentral.FeatureType.COMMONALITIES)
			s += "<tr><th>n.</th><th>Selected Commonalities</th><th>Color</th></tr>";
		  else
			s += "<tr><th>n.</th><th>Selected Variabilities</th><th>Color</th></tr>";
				
		  if(al!=null) for(int i = 0; i < al.size(); i++)
			s += "<tr><td style=\"width: auto;\">" +(i+1)
			   + "</td><td style=\"width: auto;\">" + al.get(i) 
			   + "</td><td style=\"width: auto; background: #"+getHexRGB(termsColor.get(al.get(i)))
			   + ";\"></td></tr>";

		  s += "</table></body></html>";	
			
		  pw.print(s);
		  pw.close();
		}catch (IOException e){
		  System.out.println("Exception readCommonalitiesSelectedFileHTML: " + e.getMessage());
		  return;
		}
	}
	
	/**
	 * Returns a String representing color in hexadecimal RGB form, like 'ffffff' for pure white.

	 * @param color - an int[] of length 3, containing RGB integer values of the color
	 * @return - the String representing the color in hexadecimal RGB form
	 */
	private String getHexRGB(int[] color) {
		String red=null, green=null, blue=null;

		red=Integer.toHexString(color[0]);
		green=Integer.toHexString(color[1]);
		blue=Integer.toHexString(color[2]);
		
		if(red.length()==1) red="0"+red;
		if(green.length()==1) green="0"+green;
		if(blue.length()==1) blue="0"+blue;
		
//		System.out.println("red is: "+red);
//		System.out.println("green is: "+green);
//		System.out.println("blue is: "+blue);

//		return Integer.toHexString(color[0])
//				   +Integer.toHexString(color[1])
//				   +Integer.toHexString(color[2]);
		return red+green+blue;
	}

	/** 
	 * Reads the state of the project.
	 * 
	 * @return - stateProject
	 */
	public boolean [] readStateProject(){
		return stateProject;
	}
	
	
	/* -= FUNZIONI Ausiliarie =- */
	
	/** 
	 * Tells if the term s intersect the relevant terms sets of all files.
	 * 
	 * @param s - the term to test for intersection
	 * @return - true if s intersect the relevant terms sets of all files, false otherwise
	 */	
	private boolean intersectTermRelevant(String s){
	  for(int i = 1; i < filesProject.size(); i++){
		int j = 0;
			
		while(j < filesProject.get(i).readTermRelevant().size()){
		  if(filesProject.get(i).readTermRelevant().get(j)[0].compareTo(s)==0) break;				
		  j++;
		}
			
		if(j >= filesProject.get(i).readTermRelevant().size()) return false;
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
	 * Returns the ArrayList<String[]> containing both relevant terms versions for the project input files.
	 * 
	 * @return - allFilesRelevantTerms, the list of all relevant terms in both versions
	 */
	public ArrayList<String[]> getRelevantTermsVersions() {		
		return relevantTermsVersions;
	}

	/**
	 * Returns the HashMap containing the relevant terms colors for the project input files.
	 * 
	 * @return - relevantTerms, an object having type HashMap<String, HashMap<String, ArrayList<Integer>>>
	 */
	public HashMap<String, int[]> getTermsColor() {		
		return termsColor;
	}

	/**
	 * Returns the path used for saving the project.
	 * @return - the path used for saving the project
	 */
	public String getPathProject() {
		return pathProject;
	}
	
	/** Saves the project.
	 * 
	 * @return an xml file containing the project saved informations
	 */
	public File saveProject(){		
		String s ="<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>" + nameProject + "<node>Input";
		
		for(int i = 0; i < filesProject.size(); i++)
			s +=  "<leaf>" + new File(filesProject.get(i).readPathFile()).getName()
				  + "<path>" + filesProject.get(i).readPathFile() + "</path>" 
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
			saveFeaturesSelected(commonalitiesSelected, FeatureType.COMMONALITIES);
			saveFeaturesSelected(variabilitiesSelected, FeatureType.VARIABILITIES);
			
//			saveSelectedFeaturesHTML(commonalitiesSelected, pathCommonalitiesSelectedHTML, "Commonalities Selected");
//			saveSelectedFeaturesHTML(variabilitiesSelected, pathVariabilitiesSelectedHTML, "Variabilities Selected");			

			//saving occurrences of relevant terms
			saveProjectRelevantTerms();
			//saving models state
			saveProjectModelsState();

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
	 * Saves the models state on files.
	 * 
	 * @throws IOException
	 */
	private void saveProjectModelsState() throws IOException {
	  for(ModelFile model : filesProject) model.saveState();
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
	
	/** Carica il progetto
	 * 
	 * @param s stringa contenente la path del progetto da caricare
	 * 
	 * @return al ArrayList contenenti i nomi dei file del progetto
	 */
	public ArrayList<String> loadProject(String s){
		if(s == null) return null;

		parserXML = new ParserXML();
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		
		try{
			filesProject = new ArrayList <ModelFile> ();
			
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
			
			filesProject = new ArrayList <ModelFile> ();
			
			for(int i = 0; i < parserXML.readPathInput().size(); i++){		
				filesProject.add(
						new ModelFile(
								parserXML.readPathInput().get(i), pathProject));
				workerProject.add(
						new Thread(
								filesProject.get(i)));
			}	
			
			commonalitiesCandidates = new ArrayList <String> ();
			commonalitiesSelected = new ArrayList <String> ();
			variabilitiesCandidates = new ArrayList <String> ();
			variabilitiesSelected = new ArrayList <String> ();
			
			loadFeaturesList(commonalitiesCandidates, pathCommonalitiesCandidates);
			loadFeaturesList(commonalitiesSelected, pathCommonalitiesSelected);
			loadFeaturesList(variabilitiesCandidates, pathVariabilitiesCandidates);
			loadFeaturesList(variabilitiesSelected, pathVariabilitiesSelected);
			
			if(!loadProjectRelevantTerms()) System.out.println("Relevant terms files corrupted!");
			
			loadProjectModelsState();
			
			//building global structures to calculate terms colors, after load
			buildColorStructures();
            
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

	/**
	 * Loads the models state from files.
	 * 
	 * @throws IOException
	 */
	private void loadProjectModelsState() throws IOException {
		for(ModelFile model : filesProject) model.loadState();
	}

	/**
	 * Loads occurrences of relevant terms for every input files from a file, one term per line.
	 * 
	 * @return - true if successful, false otherwise
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
		  
		  /* ***VERBOSE*** */
		  if(verbose3){
			System.out.println("Stampo i tokens!");
			for(String str:tokens) System.out.println(str);
			termName=tokens[0];
			System.out.println("Trovato termine: "+termName);
		  }
		  /* ***VERBOSE*** */

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
	
}
