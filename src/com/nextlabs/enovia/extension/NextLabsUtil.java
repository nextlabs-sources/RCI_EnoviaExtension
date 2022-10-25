package com.nextlabs.enovia.extension;

public class NextLabsUtil {

	private static String OS = null;
	
	// Configuration file path	
	public static final String NXL_WIN_CONFIG_SUB_PATH = "\\java\\custom\\nextlabs\\conf\\";
	public static final String NXL_SOL_CONFIG_SUB_PATH = "/java/custom/nextlabs/conf/";
	
	private NextLabsUtil() {};
	
	public static String getOsName() {
		if (OS == null) {
			OS = System.getProperty("os.name"); 
		}

		return OS;
	}
	
	public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}

	public static String getConfigPath() {	
		String sPath = null;

		//For windows and Solaris system checking
		if (isWindows()) {
			//Studio platform 
			if (System.getProperty("user.dir").toLowerCase().indexOf("studio") > -1) {
				sPath = System.getenv("ESMP_HOME");
				
				if (null != sPath) {
					sPath = sPath + NXL_WIN_CONFIG_SUB_PATH;
				}
			} else {
				sPath = System.getenv("ELCS_HOME");

				if (null != sPath) {
					sPath = sPath + NXL_WIN_CONFIG_SUB_PATH;
				}
			}
		} else {
			//Studio platform 
			if (System.getProperty("user.dir").toLowerCase().indexOf("studio") > -1) {
				sPath = System.getenv("ESMP_HOME");

				if (null != sPath) {
					sPath = sPath + NXL_SOL_CONFIG_SUB_PATH;
				}
			} else {
				sPath = System.getenv("ELCS_HOME");
				
				if (null != sPath) {
					sPath = sPath + NXL_SOL_CONFIG_SUB_PATH;
				}
			}
		}
		
		return sPath;		
	}

}
