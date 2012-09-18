/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Tir;

import java.io.*;
import java.net.*;
import java.util.*;

public class AnalysisText extends PdfToTxt
{

	private ArrayList<String> AUrl = null;

    public AnalysisText(String pathPDF) {
		super(pathPDF);
		this.Convert();	
		
		
	}

    public ArrayList<String> Analysis(){

    	int i = 0,j=0;
    	
    	String p;
    	
        AUrl = (ArrayList<String>) ConnectionAnalysis(); 
        
        ArrayList<String> A = new ArrayList<String>();
        
        boolean fine_analisi = false;

        try
        {
        	while(i < AUrl.size())
        	{   
        		p = "";
        		
        		String HttpURL = (String) AUrl.get(i), temp;
	
	            URL myURL = new URL(HttpURL);
	
	            HttpURLConnection con = (HttpURLConnection) myURL.openConnection();
	
	            con.setRequestMethod("GET");
	            con.setDoOutput(true);
	            con.setDoInput(true);
	
	            DataOutputStream output = new DataOutputStream(con.getOutputStream());
	
	            output.close();
	
	            BufferedReader buffer = new BufferedReader(new InputStreamReader(con.getInputStream()));
	
	            while ((temp=buffer.readLine()) != null)
	            {
	                p = p + temp;
	            }
	            
	            if(FindMeta(p) == false)
	            {
	            	Thread.sleep(5000);
	            	j++;
	            	System.out.println(j + " " + p);
	            	continue;
	            }
	            
	            if(i != 0)
	            	A.add(p);

	            i++;            
	            	
           	}      
        	
        	return A;
        }
        catch (InterruptedException ex)
        {
            System.out.println("Errore " + ex.getMessage());
            return null;
        }
        catch (MalformedURLException ex)
        {
            System.out.println("Errore " + ex.getMessage());
            return null;
        }
        catch (UnsupportedEncodingException ex)
        {
            System.out.println("Errore " + ex.getMessage());
            return null;

        }
        catch (IOException ex)
        {
            System.out.println("Errore " + ex.getMessage());
            return null;
        }


    }

    private Collection<String> ConnectionAnalysis()
    {
        //String text = new PdfToTxt("/Users/danielecicciarella/Downloads/handbook.pdf").text_utf8;
        ArrayList<String> A = new ArrayList<String>();

        String text = this.text_utf8, s0 = "" ,s1 = "",s2 = "",s3 ="",s4 = "", s5 ="", temp, p = " ", yit = null;
        String HttpURL="http://www.ilc.cnr.it/dylanlab/index.php?page=texttools&hl=en_US&showtemplate=false";

        boolean trovato = false; 
        
        try 
        {
            String query = "input_text=" + URLEncoder.encode(text, "UTF-8");
            
            URL myURL = new URL(HttpURL);

            HttpURLConnection con = (HttpURLConnection) myURL.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-lenght", String.valueOf(query.length()));
            con.setDoOutput(true);
            con.setDoInput(true);

            DataOutputStream output = new DataOutputStream(con.getOutputStream());

            output.writeBytes(query);
            output.close();
            
            
            BufferedReader buffer = new BufferedReader(new InputStreamReader(con.getInputStream()));
            
            while ((temp=buffer.readLine()) != null)
            {
            	p = p + temp;
            	
                if(!trovato)
                {
                	if((yit = FindYit(p)) != null)
                		trovato = true;
                
                }
            }          
            
            buffer.close();
                
            if(!trovato)
            {
                System.out.println("Error not find yid");
                return null;
            }     

        
            s0 = HttpURL + "&jid=" + yit;
            s1 = HttpURL + "&jid=" + yit + "&tmid=tm_sentence_splitter";
            s2 = HttpURL + "&jid=" + yit + "&tmid=tm_term_extractor";
            s3 = HttpURL + "&jid=" + yit + "&tmid=tm_parser";
            s4 = HttpURL + "&jid=" + yit + "&tmid=tm_readability";
            s5 = HttpURL + "&jid=" + yit + "&tmid=tm_readability_projection";
            
            A.add(s0);
            A.add(s1);
            A.add(s2);
            A.add(s3);
            A.add(s4);
            A.add(s5);

            return A;
        }
        catch (MalformedURLException ex)
        {
            System.out.println("Errore " + ex.getMessage());
            return null;
        }
        catch (UnsupportedEncodingException ex)
        {
            System.out.println("Errore " + ex.getMessage());
            return null;
        }
        catch (IOException ex)
        {
            System.out.println("Errore " + ex.getMessage());
            return null;
        }

    }
    
    
    private boolean FindMeta(String s)
    {
    	for(int i = 0; i < s.length(); i++)
    	{
    		if(s.charAt(i) == '<' && s.charAt(i+1) == 'm' && s.charAt(i+2) == 'e' && s.charAt(i+3) == 't' && s.charAt(i+4) == 'a')
    		{
    			return false;
    		}
    	}
    	
    	return true;
    }

    private String FindYit(String s)
    {
        String p = "";

        int i = 0;

        while(i < s.length())
        {
            if(s.charAt(i) == 'j' && s.charAt(i+1) == 'i' && s.charAt(i+2) == 'd' && s.charAt(i+3) == '=')
            {
                i = i + 4;
                break;
            }

            i++;
        }

        if(i == s.length())
            return null;

        while(s.charAt(i) != '&')
        {
            p = p + String.valueOf(s.charAt(i));
            i++;
        }

        return p;
    }
    
    public static void main(String[] args)
    {
    	AnalysisText a = new AnalysisText("/Users/danielecicciarella/Downloads/handbook.pdf");
    	ArrayList<String> A = a.Analysis();
    	
    	for(int i = 0; i < A.size(); i++)
    		System.out.println(A.get(i));
    }
 
 }
