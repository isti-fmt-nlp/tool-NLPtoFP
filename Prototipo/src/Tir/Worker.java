package project;

import java.util.*;

public class Worker extends Observable implements Runnable
{
	private ArrayList <RecordPdf> recordPdf = null;
	
	private ThreadRecordPdf[] threadRecordPdf = null;
	
	private ExtractTerm extractTerm = null;
	
	public Worker(ArrayList <RecordPdf> recordPdf)
	{
		this.recordPdf = recordPdf;
	}
	
	@Override 
	public void run()
	{
		threadRecordPdf = new ThreadRecordPdf[recordPdf.size()];
		
		for(int i = 0; i < recordPdf.size(); i++)
		{
			threadRecordPdf[i] = new ThreadRecordPdf(recordPdf.get(i).getPathPdf());		
			threadRecordPdf[i].start();
		}		
		
		for(int i = 0; i < recordPdf.size(); i++)
		{
			try 
			{
				threadRecordPdf[i].join();
			} 
			catch (InterruptedException e) 
			{

			}
		}
		
		RecordPdf recordPdf[] = new RecordPdf[threadRecordPdf.length];
		
		for(int i = 0; i < threadRecordPdf.length; i++)
			recordPdf[i] = threadRecordPdf[i].recordPdf;
		
		extractTerm = new ExtractTerm(recordPdf);
		extractTerm.RunExtract();
		
		setChanged();
		notifyObservers("1");
	}
}
