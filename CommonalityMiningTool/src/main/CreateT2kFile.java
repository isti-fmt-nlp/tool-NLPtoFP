package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;

import cnr.ilc.t2k.*;

public class CreateT2kFile {

	private String NomeFile;
	private t2kCore t2k;
	private File file;

	public CreateT2kFile(File f){
		NomeFile = f.getName();
		t2k = new t2kCore("FMTLab","FMTLab2013");
		file = f;
		t2k.executeNewCorpus(file, file.getName(), Language.English);

	}

	public File run(){


		String absolutePath = file.getAbsolutePath();
		System.out.println("File path : " + absolutePath);

		String filePath = absolutePath.
				substring(0,absolutePath.lastIndexOf(File.separator));

		String namedir = filePath+"/"+file.getName().
				substring(0,file.getName().lastIndexOf("."))+"/";

		new File(namedir).mkdirs();

		t2k.executePartOfSpeech();

		while(!t2k.QueryPartofSpeech()){
			try {
				Thread.sleep(5000);
				System.out.println("Pausa POS ");
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

		t2k.downloadPartofSpeech(namedir);

		List<Term_Extraction_Configuration> col = t2k.getListTerm_Extraction_Configuration();

		t2k.executeTerm_Extraction(col.get(2));

		while(!t2k.QueryTerm_Extraction()){
			try {
				Thread.sleep(5000);
				System.out.println("Pausa TE ");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		t2k.downloadTerm_Extraction(namedir);


		t2k.executeTerm_Extraction_Indexer();

		while(!t2k.QueryTerm_Extraction_Indexer()){
			try {
				Thread.sleep(5000);
				System.out.println("Pausa TEI ");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		t2k.downloadTerm_Extraction_Indexer(namedir);

		t2k.delCorpus();
		
		File dir = new File(namedir);
		if (dir.isDirectory()) { // make sure it's a directory
		    for (final File f : dir.listFiles()) {
		        try {
		            File newfile =new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")).replace(".txt", ""));

		            if(f.renameTo(newfile)){
		                System.out.println("Rename succesful");
		            }else{
		                System.out.println("Rename failed");
		            }
		        } catch (Exception e) {
		            // TODO: handle exception
		            e.printStackTrace();
		        }
		    }
		}
		File f = new File(namedir+file.getName());
		copyFile(file,f);

		
		return dir;


	}

	private static void copyFile(File sourceFile, File destFile)
	{
		try {
			if (!sourceFile.exists()) {
				return;
			}
			if (!destFile.exists()) {

				destFile.createNewFile();

			}
			FileChannel source = null;
			FileChannel destination = null;
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			if (destination != null && source != null) {
				destination.transferFrom(source, 0, source.size());
			}
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
