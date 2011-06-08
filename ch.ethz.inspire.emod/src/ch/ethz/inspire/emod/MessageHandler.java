/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author dhampl
 *
 */
public class MessageHandler {

	private static MessageHandler handler=null; 
	private static LogLevel logLevel=LogLevel.ALL;
	private BufferedWriter fileWriter;
	
	private MessageHandler() {
		try {
			fileWriter = new BufferedWriter(new FileWriter(PropertiesHandler.getProperty("app.logfile"),true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setLogLevel(LogLevel level) {
		logLevel = level;
	}
		
	public static void logMessage(LogLevel level, String message) {
		if(handler == null)
			handler = new MessageHandler();
		if(logLevel.getCode() <= level.getCode()) {
			String logmsg = (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS")).format(new Date())+ " " + level + " " + message;
			handler.writeLine(logmsg);
		}
	}
	
	private void writeLine(String msg) {
		try {
			fileWriter.write(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void closeLog() {
		handler.closeFileLog();
	}
	
	private void closeFileLog() {
		try {
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
