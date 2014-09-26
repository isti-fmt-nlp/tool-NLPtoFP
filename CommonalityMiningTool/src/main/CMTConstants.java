package main;

public class CMTConstants {
	
	/** parent of top level save folder*/
	private static final String topSaveFolderParent = "..";

	/*Linux Versions*/	
	/** top level save folder for both tools*/
	private static final String topSaveFolder = topSaveFolderParent+"/CMT_FDE";

	/*Windows Versions*/
	/** top level save folder for both tools*/
	private static final String topSaveFolder_Win = topSaveFolderParent+"\\CMT_FDE";

	/* Save paths for analisys tool*/

	/*Linux Versions*/	
	/** general save path for analisys tool*/
	private static final String saveAnalisysDir = topSaveFolder+"/ANALISYS";
	
	/** sub path used to save commonalities candidates*/
	private static final String CCsubpath = "/CommonalitiesC.log";
	/** sub path used to save commonalities selected*/
	private static final String CSsubpath = "/CommonalitiesS.log";
	/** sub path used to save HTML file with the commonalities selected table*/
	private static final String CSHsubpath = "/CommonalitiesS.html";
	/** sub path used to save variabilities candidates*/
	private static final String VCsubpath = "/VariabilitiesC.log";
	/** sub path used to save variabilities selected*/
	private static final String VSsubpath = "/VariabilitiesS.log";
	/** sub path used to save HTML file with the variabilities selected table*/
	private static final String VSHsubpath = "/VariabilitiesS.html";
	/** sub path used to save relevant terms occurrences*/
	private static final String RTsubpath = "/RelevantTerms.log";
	/** sub path used to save relevant terms versions*/
	private static final String TVsubpath = "/TermsVersions.log";
	

	/*Windows Versions*/
	/** general save path for analisys tool*/
	private static final String saveAnalisysDir_Win = topSaveFolder_Win+"\\ANALISYS";
	
	/** sub path used to save commonalities candidates*/
	private static final String CCsubpath_Win = "\\CommonalitiesC.log";
	/** sub path used to save commonalities selected*/
	private static final String CSsubpath_Win = "\\CommonalitiesS.log";
	/** sub path used to save HTML file with the commonalities selected table*/
	private static final String CSHsubpath_Win = "\\CommonalitiesS.html";
	/** sub path used to save variabilities candidates*/
	private static final String VCsubpath_Win = "\\VariabilitiesC.log";
	/** sub path used to save variabilities selected*/
	private static final String VSsubpath_Win = "\\VariabilitiesS.log";
	/** sub path used to save HTML file with the variabilities selected table*/
	private static final String VSHsubpath_Win = "\\VariabilitiesS.html";
	/** sub path used to save relevant terms occurrences*/
	private static final String RTsubpath_Win = "\\RelevantTerms.log";
	/** sub path used to save relevant terms versions*/
	private static final String TVsubpath_Win = "\\TermsVersions.log";
	
	/* Suffixes for analisys tool*/
	public static final String SETSsuffix = ".sets";
	public static final String ARITYsuffix = ".arities";
	public static final String TERMSsuffix = ".terms";
	
	
	/* Save paths for diagram editor tool*/

	/*Linux Versions*/
	/** general save path for diagram editor tool*/
	private static final String saveDiagramDir = topSaveFolder+"/DIAGRAMS";
	
	/*Windows Versions*/
	private static final String saveDiagramDir_Win = topSaveFolder_Win+"\\DIAGRAMS";


	/** sub path used to save exported SXFM diagram files*/
	public static String sxfmSubPath = "_SXFM"; 	
	/** sub path used to save exported PNG diagram files*/
	public static String imagesSubPath = "_IMAGES"; 
	/** sub path used to save custom diagram files, without using data from the analisys tool*/
	public static String customSaveDiagramDir = "_CUSTOM_DIAGRAMS_";

	/** Returns the path of the top save directory's parent directory*/
	public static String getTopSaveDirParent(){
	  return topSaveFolderParent;
	}

	/** Returns the path of the top save directory*/
	public static String getTopSaveDir(){
	  if(OSUtils.isWindows()) return topSaveFolder_Win;
	  else return topSaveFolder;	  
	}

	/** Returns the general sub path used for analisys tool*/
	public static String getSaveAnalisysDir(){
	  if(OSUtils.isWindows()) return saveAnalisysDir_Win;
	  else return saveAnalisysDir;	  
	}
	
	/** Returns the sub path used to save commonalities candidates*/
	public static String getCCsubpath(){		
	  if(OSUtils.isWindows()) return CCsubpath_Win;
	  else return CCsubpath;
	}

	/** Returns the sub path used to save commonalities selected*/
	public static String getCSsubpath(){
		  if(OSUtils.isWindows()) return CSsubpath_Win;
		else return CSsubpath;
	}

	/** Returns the sub path used to save HTML file with the commonalities selected table*/
	public static String getCSHsubpath(){
	  if(OSUtils.isWindows()) return CSHsubpath_Win;
	  else return CSHsubpath;
	}

	/** Returns the sub path used to save variabilities candidates*/
	public static String getVCsubpath(){
	  if(OSUtils.isWindows()) return VCsubpath_Win;
	  else return VCsubpath;
	}

	/** Returns the sub path used to save variabilities selected*/
	public static String getVSsubpath(){
	  if(OSUtils.isWindows()) return VSsubpath_Win;
	  else return VSsubpath;
	}

	/** Returns the sub path used to save HTML file with the variabilities selected table*/
	public static String getVSHsubpath(){
	  if(OSUtils.isWindows()) return VSHsubpath_Win;
	  else return VSHsubpath;
	}

	/** Returns the sub path used to save relevant terms occurrences*/
	public static String getRTsubpath(){
	  if(OSUtils.isWindows()) return RTsubpath_Win;
	  else return RTsubpath;
	}

	/** Returns the sub path used to save relevant terms versions*/
	public static String getTVsubpath(){
	  if(OSUtils.isWindows()) return TVsubpath_Win;
	  else return TVsubpath;
	}

	/** Returns the general sub path used for diagram editor tool*/
	public static String getSaveDiagramDir(){
	  if(OSUtils.isWindows()) return saveDiagramDir_Win;
	  else return saveDiagramDir;
	}

}
