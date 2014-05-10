package main;

public class OSUtils {
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	 
	/** Tells if whether the Operative System is Windows*/
	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0); 
	}
 
	/** Tells if whether the Operative System is Mac*/
	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0); 
	}
 
	/** Tells if whether the Operative System is Unix or Linux*/
	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ); 
	}
 
	/** Tells if whether the Operative System is Solaris*/
	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0); 
	}
	
	/**
	 * Returns the correct file separator for the current Operative System.
	 * @return a Strign containing the separator
	 */
	public static String getFilePathSeparator(){
		if(isWindows()) return "\\";
		else return "/";
	}
}
