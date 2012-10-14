

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.xml.parsers.*;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TreeProject
{
	/* DefaultTreeModel utilizzato per aggiornare graficamente l'albero */
	private DefaultTreeModel DTree = null;
	
	/* JTree per costruire l'albero del progetto */
	public JTree Tree = null;
	
	/* DefaultMutableTreeNode che rappresenta la radice del nostro albero */
	public DefaultMutableTreeNode father;
	
	/* Boolean che mi indica se l'albero è stato modificato */
	public boolean modify = false;
	
	/* Boolean che mi indica se l'albero è nuovo */
	public boolean proj_new = false;
	
	/* Stringa contenente la path del file xml */
	public String path_xml = null;
	
	/* Classe utilizzata per il parsing del file XML */
	private ParserXML parser_xml = new ParserXML();
	
	/* ArrayList contenenti i RecordPdf della directory Input */
	public ArrayList <RecordPdf> Input_file = new ArrayList<RecordPdf>();
	
	/* Array contenenti i thread per l'analisi del file in parallelo */
	public ThreadRecordPdf Trp[] = null;
	
	/* Boolean che indica se l'analisi dei file è andata a buon fine */
	public boolean run_record = false;
	
	/** Funzione crea nuovo progetto */
	public void CreateTree()
	{
		/* Creo radice project */
		this.father = new DefaultMutableTreeNode("Project");
		 
		for(int i = 0; i< 3; i++)
		{
			switch(i)
			{
				case 0:
				{
					/* Creo nodo Input */
					this.father.add(new DefaultMutableTreeNode("Input"));
					break;
				}		
				case 1:
				{
					/* Creo nodo Commonalities con i suoi relativi figli */
					this.father.add(new DefaultMutableTreeNode("Commonalities"));
					
					DefaultMutableTreeNode f = (DefaultMutableTreeNode) father.getChildAt(i);
					
					f.add(new DefaultMutableTreeNode("Show Commonalities"));
					f.add(new DefaultMutableTreeNode("Show Commonalities Select"));
					
					break;
				}		
				case 2:
				{
					/* Creo nodo Variabilities con i suoi relativi figli */
					this.father.add(new DefaultMutableTreeNode("Variabilities"));
					
					DefaultMutableTreeNode f = (DefaultMutableTreeNode) father.getChildAt(i);
					
					f.add(new DefaultMutableTreeNode("Show Variabilities"));
					f.add(new DefaultMutableTreeNode("Show Commonalities Select"));
					
					break;
				}
			}
		}
		/* Creiamo il nuovo albero */
		this.DTree = new DefaultTreeModel(father);
		this.Tree = new JTree(DTree);
		this.proj_new = true;
	}
	
	/** Funzione salva il progetto corrente */
	public void SaveTree()
	{
		PrintStream fr = null;
		
		ArrayList <String> T = new ArrayList<String>();
		
		VisitPreOrder(this.father, T, 0);
		T.add("</directory>");
		T.add("</project>");
		
		try 
		{
			fr = 
				new PrintStream(
					new FileOutputStream(this.path_xml),false,"UTF-8");
			
			for(int i = 0; i < T.size(); i++)
				fr.print(T.get(i));
			
			fr.close();
		} 
		catch (UnsupportedEncodingException e) 
		{
			System.out.println("Exception SaveTree [0]: " + e.getMessage());
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("Exception SaveTree [1]: " + e.getMessage());
		}
		this.modify = false;
		this.proj_new = false;
	}
	
	/** Funzione carica il progetto */
	public void LoadTree() 
	{
		try
		{
			int i = -1;
			
	        SAXParserFactory factory = SAXParserFactory.newInstance();
	        
	        SAXParser parser = factory.newSAXParser();
	        
	        parser.parse(this.path_xml, this.parser_xml);
	        
	        ArrayList <String> Node = parser_xml.listContentTag;
			
			this.father = new DefaultMutableTreeNode(Node.get(0));
			
			if(!Node.get(1).equals(" "))
			{
				DefaultMutableTreeNode fI = new DefaultMutableTreeNode(Node.get(1).toString());
				
				int j = 0;
					
				this.father.add(fI);
					
				for(i = 2; Node.get(i).subSequence(Node.get(i).length()-4, Node.get(i).length()).equals(".pdf"); i++)
				{
					j = (j + 1) % 2;
					
					if(j == 1)
						fI.add(new DefaultMutableTreeNode(Node.get(i)));
								
					else 
						Input_file.add(new RecordPdf(Node.get(i)));
				}
				
				DefaultMutableTreeNode fC = new DefaultMutableTreeNode(Node.get(i).toString());
				DefaultMutableTreeNode fV = new DefaultMutableTreeNode(Node.get(i+3).toString());
				
				fC.add(new DefaultMutableTreeNode(Node.get(i+1).toString()));
				fC.add(new DefaultMutableTreeNode(Node.get(i+2).toString()));
			
				fV.add(new DefaultMutableTreeNode(Node.get(i+4).toString()));
				fV.add(new DefaultMutableTreeNode(Node.get(i+5).toString()));
				
				/* Aggiungiamo directory Commonalities */
				this.father.add(fC);
				/* Aggiungiamo directory Variabilities */
				this.father.add(fV);
				/* Aggiorniamo modifica albero */
				this.modify = false;
				/* Creiamo il nuovo albero */
				this.DTree = new DefaultTreeModel(father);
				this.Tree = new JTree(DTree);	
				this.proj_new = false;
			}
			this.parser_xml = null;
			this.parser_xml = new ParserXML();
		}
		catch(SAXException e)
		{
			System.out.println("Exception LoadTree [0]: " + e.getMessage());
			return;
		}
		catch(IOException e)
		{
			System.out.println("Exception LoadTree [1]: " + e.getMessage());
			return;
		}
		catch(ParserConfigurationException e)
		{
			System.out.println("Exception LoadTree [2]: " + e.getMessage());
			return;
		}
	}
	
	/** Funzione aggiunge un nodo file
	 
	   @param s: Stringa che rappresenta il nome del nodo file
	   @param path: Stringa che rappresenta la path del file
	   
	*/
	public void AddNode(String s, String path)
	{
		/* Aggiungiamo nuovo RecordPdf */
		this.Input_file.add(new RecordPdf(path));	
		/* Inseriamo un nuovo figlio al nodo Input */
		this.DTree.insertNodeInto(new DefaultMutableTreeNode(s), (MutableTreeNode) father.getChildAt(0), father.getChildAt(0).getChildCount());
		/* Aggiorniamo la modifica dell'albero */
		this.modify = true;
	}
	
	/** Funzione cancella il nodo selezionato */
	public void DeleteNode(DefaultMutableTreeNode node)
	{	
		if(node != null && !node.toString().equals("Input") 
				&& !node.toString().equals("Commonalities") 
					&& !node.toString().equals("Variabilities"))
		{
			for(int i = 0; i < father.getChildAt(0).getChildCount(); i++)
				if(father.getChildAt(0).getChildAt(i).toString().equals(node.toString()))
					Input_file.remove(i);

			this.modify = true;
			this.DTree.removeNodeFromParent(node);	
		}
	}
	
	/** Funzione modifica il nome della radice

	   @param s: Stringa che rappresenta il nuovo nome del nodo
	   
	*/
	public void ModificyNode(String s)
	{
		this.father.setUserObject(s);		
		this.modify = true;
	}
	
	/** Funzione effettua la visita anticipa dell'albero
	 
	   @param p: Nodo dell'albero che sto visitando
	   @param AXML: ArrayList contenente il codice xml del progetto
	   @param ind: Intero che rappresenta quale nodo dell'albero dovrò visitare successivamente
	 
	*/
	private void VisitPreOrder(DefaultMutableTreeNode p, ArrayList <String> listXml, int ind)
	{
		if(p.equals(this.father))
		{
			listXml.add("<project>");
			listXml.add(p.toString());
		}
		else if(p.equals(this.father.getChildAt(0)))
		{
			listXml.add("<directory>");
			listXml.add(p.toString());
		}
		else if(p.equals(this.father.getChildAt(1)))
		{
			listXml.add("</directory>");
			listXml.add("<directory>");
			listXml.add(p.toString());
		}
		else if(p.equals(this.father.getChildAt(2)))
		{
			listXml.add("</directory>");
			listXml.add("<directory>");
			listXml.add(p.toString());
		}
		else if(p.getParent().toString().equals(this.father.getChildAt(0).toString()))
		{
			listXml.add("<file>");
			listXml.add(p.toString());
			listXml.add("<path>");
			listXml.add(Input_file.get(ind).getPathPdf());
			listXml.add("</path>");
			listXml.add("</file>");
		}
		else if(p.getParent().toString().equals(this.father.getChildAt(1).toString()))
		{
			listXml.add("<file>");
			listXml.add(p.toString());
			listXml.add("</file>");
		}
		else if(p.getParent().toString().equals(this.father.getChildAt(2).toString()))
		{
			listXml.add("<file>");
			listXml.add(p.toString());
			listXml.add("</file>");
		}
		
		for(int i = 0; i < p.getChildCount(); i++)
			VisitPreOrder((DefaultMutableTreeNode) p.getChildAt(i), listXml, i);
	}
	
	/** Classe effettua il parsing del file xml selezionato */
	class ParserXML extends DefaultHandler
	{
		public ArrayList <String> listContentTag = new ArrayList<String>();
		
		@Override
		public void characters(char[] ch, int start, int length)
		{
			this.listContentTag.add(new String(ch,start,length));
		}
	}
}
