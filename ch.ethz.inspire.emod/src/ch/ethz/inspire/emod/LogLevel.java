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

/**
 * Loglevel for the message handler
 * 
 * @author dhampl
 *
 */
public enum LogLevel {
	ALL(0), DEBUG(1), INFO(2), WARN(3), ERROR(4);
	
	private int code;
	
	private LogLevel(int c) {
		code = c;
	}
	
	public int getCode() {
		return code;
	}
}
