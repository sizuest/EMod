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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Handles error, warning, info and debug messages. One or more message
 * logger can be registered and the messages are written to the loggers.
 * 
 * @author dhampl
 *
 */
public class MessageHandler {

	private static MessageHandler handler=null; 
	private static LogLevel logLevel=LogLevel.ALL;
	private List<BufferedWriter> fileWriter;
	
	private MessageHandler() {
		fileWriter = new ArrayList<BufferedWriter>();
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
			for(BufferedWriter bw : fileWriter) {
				bw.write(msg);
				bw.newLine();
				bw.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeLog() {
		handler.closeFileLog();
	}
	
	private void closeFileLog() {
		try {
			for(BufferedWriter bw : fileWriter)
				bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void addLogger(BufferedWriter logger) {
		if(handler == null)
			handler = new MessageHandler();
		handler.fileWriter.add(logger);
	}
}
