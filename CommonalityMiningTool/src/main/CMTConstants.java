package main;

public class CMTConstants {
	
	/* Save paths for analisys tool*/
	/** general save path for analisys tool*/
	public static final String saveAnalisysDir ="../Usage Tries/ANALISYS";
	
	/** sub path used to save commonalities candidates*/
	public static final String CCsubpath = "/CommonalitiesC.log";
	/** sub path used to save commonalities selected*/
	public static final String CSsubpath = "//CommonalitiesS.log";
	/** sub path used to save HTML file with the commonalities selected table*/
	public static final String CSHsubpath = "/CommonalitiesS.html";
	/** sub path used to save variabilities candidates*/
	public static final String VCsubpath = "/VariabilitiesC.log";
	/** sub path used to save variabilities selected*/
	public static final String VSsubpath = "/VariabilitiesS.log";
	/** sub path used to save HTML file with the variabilities selected table*/
	public static final String VSHsubpath = "/VariabilitiesS.html";
	/** sub path used to save relevant terms occurrences*/
	public static final String RTsubpath = "/RelevantTerms.log";
	/** sub path used to save relevant terms versions*/
	public static final String TVsubpath = "/TermsVersions.log";
	
	/* Suffixes for analisys tool*/
//	public static final String SETSsuffix = "SETS.log";
//	public static final String ARITYsuffix = "ARITY.log";
//	public static final String TERMSsuffix = ".log";
	
	public static final String SETSsuffix = ".sets";
	public static final String ARITYsuffix = ".arities";
	public static final String TERMSsuffix = ".terms";
	
	
	/* Save paths for diagram editor tool*/
	/** general save path for diagram editor tool*/
	public static final String saveDiagramDir = "../Usage Tries/DIAGRAMS";

	/** sub path used to save exported SXFM diagram files*/
	public static String sxfmSubPath = "_SXFM"; 	
	/** sub path used to save exported PNG diagram files*/
	public static String imagesSubPath = "_IMAGES"; 
	/** sub path used to save custom diagram files, without using data from the analisys tool*/
	public static String customSaveDiagramDir = "_CUSTOM_DIAGRAMS_";



}
