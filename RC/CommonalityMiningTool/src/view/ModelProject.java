/**
 * 
 * @author Daniele Cicciarella
 *
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Observable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class ModelProject extends Observable implements Runnable
{
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
	private String pathCommanalitiesCandidates = null;
	
	/* Stringa contenente il percorso delle commonalities selected */
	private String pathCommanalitiesSelected = null;
	
	/* Stringa contenente il percorso della pagina HTML delle commonalities selected */
	private String pathCommanalitiesSelectedHTML = null;
	
	/* ArrayList contenente le commonalities candidates */
	private ArrayList <String> commonalitiesCandidates = null;
	
	/* ArrayList contenente le commonalities selected */
	private ArrayList <String> commonalitiesSelected = null;
	
	/* boolean contenente lo stato del progetto */
	private boolean [] stateProject = new boolean[2];
	
	/** Thread: Attende la terminazione dei thread workerProject e calcala i commonalities candidates
	 * 
	 */
	@Override
	public void run() 
	{
		commonalitiesCandidates = new ArrayList <String> ();
		
		for(int i = 0; i < workerProject.size(); i++)
		{
			try 
			{
				workerProject.get(i).join();
			} 
			catch (InterruptedException e) 
			{
				System.out.println("Exception Handler: " + e.getMessage());
				return;
			}	
		}
		
		for(int j = 0; j < fileProject.get(0).readTermRelevant().size(); j = j + 1)
        	if(intersectTermRelevant(fileProject.get(0).readTermRelevant().get(j)))
        		commonalitiesCandidates.add(fileProject.get(0).readTermRelevant().get(j));
		
		for(int i = 0; i < commonalitiesCandidates.size(); i++)
			for(int j = i + 1; j < commonalitiesCandidates.size(); j++)
				if(commonalitiesCandidates.get(i).equals(commonalitiesCandidates.get(j)))
					commonalitiesCandidates.remove(j);
					
		setChanged();
		notifyObservers("End Extract Commonalities");
	}
	
	/** Crea il nuovo progetto
	 * 
	 * @param s stringa contenente il nome del progetto
	 * 
	 * @return true se il progetto  stato creato correttamente
	 * @return false se si  verificato un errore
	 */
	public boolean createProject(String s)
	{
		fileProject = new ArrayList <ModelFile> ();
		
		workerProject = new ArrayList <Thread> ();
		
		nameProject = s;
		pathProject = "./" + s;
		pathXML = pathProject + "/" + s + ".xml"; 
		pathCommanalitiesCandidates = pathProject + "/CommanalitiesC.log";
		pathCommanalitiesSelected = pathProject + "/CommanalitiesS.log";
		pathCommanalitiesSelectedHTML = pathProject + "/CommanalitiesS.html";
		stateProject[0] = true;
		stateProject[1] = true;
		
		if(new File(pathProject).mkdir() == false)
			return false;		
		
		else
			return true;
	}
	
	/** Carica il progetto
	 * 
	 * @param s stringa contenente la path del progetto da caricare
	 * 
	 * @return al ArrayList contenenti i nomi dei file del progetto
	 */
	public ArrayList <String> loadProject(String s)
	{
		if(s == null)
			return null;

		parserXML = new ParserXML();
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		
		try 
		{
			fileProject = new ArrayList <ModelFile> ();
			
			workerProject = new ArrayList <Thread> ();
			
			nameProject = s.substring(0, s.length() - 4);
			pathProject = "./" + nameProject;
			pathXML = pathProject + "/" + nameProject + ".xml"; 
			pathCommanalitiesCandidates = pathProject + "/CommanalitiesC.log";
			pathCommanalitiesSelected = pathProject + "/CommanalitiesS.log";
			pathCommanalitiesSelectedHTML = pathProject + "/CommanalitiesS.html";
			stateProject[0] = false;
			stateProject[1] = false;
			
			SAXParser parser = spf.newSAXParser();
			parser.parse(pathXML, parserXML);
			
			fileProject = new ArrayList <ModelFile> ();
			
			for(int i = 0; i < parserXML.readPathInput().size(); i++)
			{		
				fileProject.add(
						new ModelFile(
								parserXML.readPathInput().get(i), pathProject));
				workerProject.add(
						new Thread(
								fileProject.get(i)));
			}	
			
			commonalitiesCandidates = new ArrayList <String> ();
			
			commonalitiesSelected = new ArrayList <String> ();
			
			String s1;
			
			BufferedReader br1 =
            		new BufferedReader(
            				new FileReader(pathCommanalitiesCandidates));

            while( (s1 = br1.readLine()) != null )
                commonalitiesCandidates.add(s1);
            
            BufferedReader br2 =
            		new BufferedReader(
            				new FileReader(pathCommanalitiesSelected));

            while( (s1 = br2.readLine()) != null )
                commonalitiesSelected.add(s1);
            
            br1.close();
            br2.close();
            
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
	
	/** Salva il progetto
	 * 
	 * @return f file xml contenente il salvataggio del progetto
	 */
	public File saveProject()
	{		
		String s = "<root>" + nameProject + "<node>Input";
		
		for(int i = 0; i < fileProject.size(); i++)
			s += "<leaf>" + new File(fileProject.get(i).readPathFile()).getName() + "<path>" + fileProject.get(i).readPathFile() + "</path>" + "</leaf>";
		
		s += "</node><node>Commonalities</node></root>";
		
		try 
		{
			PrintWriter pw1 =
			        new PrintWriter(
			        		new BufferedWriter(
			        				new FileWriter(pathXML)));
			pw1.print(s);
			
			
			PrintWriter pw2 =
			        new PrintWriter(
			        		new BufferedWriter(
			        				new FileWriter(pathCommanalitiesCandidates)));
			
			if(commonalitiesCandidates != null)
				for(int i = 0; i < commonalitiesCandidates.size(); i++)
					pw2.print(commonalitiesCandidates.get(i) + "\n");
			
			PrintWriter pw3 =
			        new PrintWriter(
			        		new BufferedWriter(
			        				new FileWriter(pathCommanalitiesSelected)));
			
			if(commonalitiesSelected != null)
				for(int i = 0; i < commonalitiesSelected.size(); i++)
					pw3.print(commonalitiesSelected.get(i) + "\n");
		
			PrintWriter pw4 = new PrintWriter(
					new BufferedWriter(
							new FileWriter(pathCommanalitiesSelectedHTML)));
			
			if(commonalitiesSelected != null)
			{
				s = "<table border=" + String.valueOf('"') + String.valueOf('2') + String.valueOf('"') + "align=" + String.valueOf('"') + "center" + String.valueOf('"') + ">";
				
				s += "<tr><th>n.</th><th>Commonalities Selected</th></tr>";
				
				for(int i = 0; i < commonalitiesSelected.size(); i++)
					s += "<tr><td>" + String.valueOf(i) + "</td><td>" + commonalitiesSelected.get(i) + "</td></tr>";
				
				s += "</table>";		
				pw4.print(s);	
			}
			
			pw1.close();
			pw2.close();
			pw3.close();
			pw4.close();
			stateProject[0] = false;
			stateProject[1] = false;
		} 
		catch (IOException e) 
		{
			System.out.println("Exception saveProject: " + e.getMessage());
			return null;
		}
		return new File(pathXML);
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
	}
	
	/** Analizza il progetto
	 * 
	 */
	public void analyzesFileProject()
	{
		for(int i = 0; i < fileProject.size(); i++)
		{
			if(workerProject.get(i).getState() != Thread.State.TERMINATED)
				workerProject.get(i).start();
			
			else
			{
				workerProject.set(i, new Thread(fileProject.get(i)));
				workerProject.get(i).start();
			}
		}
		
		handlerProject = new Thread(this);
		handlerProject.start();
	}
	
	public ArrayList <String> loadAnalysisFileProject()
	{
		ArrayList <String> al = new ArrayList <String> ();
		
		for(int i = 0; i < fileProject.size(); i++)
		{
			if(new File(fileProject.get(i).readPathFileUTF8()).exists())
			{
				if(workerProject.get(i).getState() != Thread.State.TERMINATED)
				{
					workerProject.get(i).start();
					al.add(String.valueOf(i));
					try 
					{
						workerProject.get(i).join();
					} 
					catch (InterruptedException e) 
					{
						System.out.println("Exception loadAnalysisFileProject: " + e.getMessage());
					}
				}
				
				else
				{
					workerProject.set(i, new Thread(fileProject.get(i)));
					workerProject.get(i).start();
					al.add(String.valueOf(i));
					try 
					{
						workerProject.get(i).join();
					} 
					catch (InterruptedException e) 
					{
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
	
	/** Rimuove un elemento dal progetto
	 * 
	 * @param i indice del file
	 */
	public void removeFileProject(int i)
	{
		fileProject.remove(i);
		workerProject.remove(i);
		stateProject[1] = false;
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
	
	/** Legge la stringa contenente il percorso della pagina HTML delle commonalities selected
	 * 
	 * @return pathCommanalitiesSelectedHTML
	 */
	public String readPathCommanalitiesSelectedHTML()
	{
		return pathCommanalitiesSelectedHTML;
	}
	/** Crea il file HTML contenente le commonalities selected
	 * 
	 * @param al ArrayList contenente le commonalities selected
	 */
	public void setCommonalitiesSelected(ArrayList <String> al)
	{
		commonalitiesSelected = new ArrayList <String> ();
		
		PrintWriter pw = null;
		
		for(int i = 0; i < al.size(); i++)
			commonalitiesSelected.add(al.get(i));
		
		try 
		{
			pw = new PrintWriter(
					new BufferedWriter(
							new FileWriter(pathCommanalitiesSelectedHTML)));
			
			String s = "<table border=" + String.valueOf('"') + String.valueOf('2') + String.valueOf('"') + "align=" + String.valueOf('"') + "center" + String.valueOf('"') + ">";
			
			s += "<tr><th>n.</th><th>Commonalities Selected</th></tr>";
			
			for(int i = 0; i < commonalitiesSelected.size(); i++)
				s += "<tr><td>" + String.valueOf(i) + "</td><td>" + commonalitiesSelected.get(i) + "</td></tr>";
			
			s += "</table>";		
			pw.print(s);
	        pw.close();
	        setChanged();
			notifyObservers("End Commonalities Selected");
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
		return pathCommanalitiesSelectedHTML;
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
	
	/** Effettua il parsing del file xml */
    class ParserXML extends DefaultHandler
    {  	
    	private  boolean r = false, n = false, l = false, p = false;
    	
    	private int i = 0;

    	private ArrayList <String> nameInput = new ArrayList <String> (), pathInput = new ArrayList <String> ();
    	
    	@Override
        public void startElement(String uri, String localName, String gName, Attributes attributes)
        {
    		if(gName.equals("root"))
	        	r = true;
        	
        	else if(gName.equals("node"))
        		n = true;
        	
        	else if(gName.equals("leaf"))
        		l = true;
        	
        	else
        		p = true;
        }
        
    	@Override
        public void characters(char [] ch, int start, int length)
        {
    		if(p)
    			pathInput.add(new String(ch,start,length));
        	
        	else if(i == 0 && l && !p)
        		nameInput.add(new String(ch, start, length));
        		
        	else {}
        }
    	
    	@Override
        public void endElement(String uri, String localName, String gName)
        {
    		if(gName.equals("root"))
	        	r = false;
        	
        	else if(gName.equals("node"))
        	{
        		i = i + 1;
        		n = false;
        	}
        	else if(gName.equals("leaf"))
        		l = false;
        	
        	else
        		p = false;
        }
    	
    	/** Lettura delle path dei file 
    	 * 
    	 * @return pathInput
    	 */
    	public ArrayList <String> readPathInput()
    	{
    		return pathInput;
    	}
    	
    	/** Lettura dei nomi dei file
    	 * 
    	 * @return
    	 */
    	public ArrayList <String> readNameFile()
    	{
    		return nameInput;
    	}
    }
}
