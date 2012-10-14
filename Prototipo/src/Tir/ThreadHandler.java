import java.util.*;
import javax.swing.*;


public class ThreadHandler extends Thread
{
	private ArrayList <RecordPdf> Al = null;
	
	public ThreadRecordPdf[] Trp = null;
	
	public ExtractTerm extractTerm = null;
	
	public JTextArea jTextCommonalities = null;
	
	public JTextArea jTextVariabilities = null;
	
	public ThreadHandler(ArrayList <RecordPdf> Al)
	{
		this.Al = Al;
	}
	
	@Override 
	public void run()
	{
		int cont = 1;
		
		ThreadThrobber tt = new ThreadThrobber();
		
		tt.start();
		
		Trp = new ThreadRecordPdf[GetListRecordPdf().size()];
		
		for(int i = 0; i < GetListRecordPdf().size(); i++)
		{
			Trp[i] = new ThreadRecordPdf(GetListRecordPdf().get(i).getPathPdf());
			
			Trp[i].start();
		}		
		
		for(int i = 0; i < GetListRecordPdf().size(); i++)
		{
			try 
			{
				Trp[i].join();
			} 
			catch (InterruptedException e) 
			{

			}
		}
		
		RecordPdf recordPdf[] = new RecordPdf[this.Trp.length];
		
		for(int i = 0; i < this.Trp.length; i++)
			recordPdf[i] = this.Trp[i].recordPdf;
		
		this.extractTerm = new ExtractTerm(recordPdf);
		
		this.extractTerm.RunExtract();
		
		// Inseriamo i valori in textarea
		
		String s = "";
		
		for(int i = 0; i < this.extractTerm.Cr.size(); i++)
		{
			s = s + String.valueOf(cont) + ":" + extractTerm.Cr.get(i) + "\n";
			cont = cont + 1;
		}
		
		s = s + "\n";
		
		for(int i = 0; i < this.extractTerm.Cd.size(); i++)
		{
			s = s + String.valueOf(cont) + ":" + extractTerm.Cd.get(i) + "\n";
			cont = cont + 1;
		}
		
		s = s + "\n";
		
		for(int i = 0; i < this.extractTerm.Cs.size(); i++)
		{
			s = s + String.valueOf(cont) + ":" + extractTerm.Cs.get(i) + "\n";
			cont = cont + 1;
		}
		
		jTextCommonalities = new JTextArea(s);
		
		System.out.println("si");
		
		s = "";
		
		for(int i = 0; i < this.extractTerm.Vr.size(); i++)
			s = String.valueOf(cont) + ":" + s + extractTerm.Vr.get(i) + "\n";
		
		for(int i = 0; i < this.extractTerm.Vd.size(); i++)
			s = String.valueOf(cont) + ":" + s + extractTerm.Vd.get(i) + "\n";
		
		for(int i = 0; i < this.extractTerm.Vs.size(); i++)
			s = String.valueOf(cont) + ":" + s + extractTerm.Vs.get(i) + "\n";
		
		jTextVariabilities = new JTextArea(s);

		tt.setRunThrobber(true);
	
		try 
		{
			tt.join();
		} 
		catch (InterruptedException e) 
		{

		}
	}
	
	private ArrayList <RecordPdf> GetListRecordPdf()
	{
		return Al;
	}
}
