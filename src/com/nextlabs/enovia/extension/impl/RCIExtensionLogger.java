// obsolete
package com.nextlabs.enovia.extension.impl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.nextlabs.enovia.extension.NextLabsUtil;


public final class RCIExtensionLogger {

	private final transient Logger logg; // NOPMD by klee on 6/6/12 3:09 PM

	public transient boolean isNXLDebugEnable = false;
	
	private static String sConfigPath = null;
	
	static 	{
		sConfigPath = NextLabsUtil.getConfigPath();
	}
	
	/**
	 * 
	 * @param log
	 */
	public RCIExtensionLogger(final Logger logger) {		
		logg = logger;
		
		//Configure log4j rolling properties
		PropertyConfigurator.configure(sConfigPath + RCIExtensionConstant.LOG4J_CONFIG_FILE);

		if (logg.isDebugEnabled()) {
			isNXLDebugEnable = true;
		}
	}
	
	/**
	 * 
	 * @param formatter
	 * @param args
	 */
	public void debug(final String formatter, final Object... args) {
		log(Level.DEBUG, formatter, args);
	}

	/**
	 * 
	 * @param formatter
	 * @param args
	 */
	public void info(final String formatter, final Object... args) {
		log(Level.INFO, formatter, args);
	}
	
	/**
	 * 
	 * @param formatter
	 * @param args
	 */
	public void fatal(final String formatter, final Object... args) {
		log(Level.FATAL, formatter, args);
	}

	/**
	 * 
	 * @param formatter
	 * @param args
	 */
	public void error(final String formatter, final Object... args) {
		log(Level.ERROR, formatter, args);
	}
	
	/**
	 * 
	 * @param formatter
	 * @param thr
	 * @param args
	 */
	public void error(final String formatter, final Throwable thr ,final Object... args) {
		log(Level.ERROR, formatter, thr, args);
	}

	/**
	 * 
	 * @param level
	 * @param formatter
	 * @param args
	 */
	public void log(final Level level, final String formatter, final Object... args) {
		if (logg.isEnabledFor(level)) {
			/* 
			 * Now the message is constructed with the invocation of toString()
			 */
			logg.log(level, String.format(formatter, args));
		}
	}

	/**
	 * 
	 * @param level
	 * @param formatter
	 * @param thr
	 * @param args
	 */
	public void log(final Level level, final String formatter, final Throwable thr, final Object... args) {
		if (logg.isEnabledFor(level)) {
			/* 
			 * Now the message is constructed with the invocation of toString()
			 */
			logg.log(level, String.format(formatter, args),thr);
		}
	}

}
