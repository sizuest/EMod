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

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.logging.Level;

/**
 * @author dhampl
 *
 */
public class LogLevel extends Level {

	private LogLevel(String name, int value) {
		super(name, value);
	}
	public static Level STDOUT = new LogLevel("STDOUT", Level.INFO.intValue()+53);

	public static Level STDERR = new LogLevel("STDERR", Level.INFO.intValue()+54);
	
	public static Level DEBUG = new LogLevel("DEBUG", Level.CONFIG.intValue()+54);

	protected Object readResolve() throws ObjectStreamException {
		if (this.intValue() == STDOUT.intValue())
			return STDOUT;
		if (this.intValue() == STDERR.intValue())
			return STDERR;
		throw new InvalidObjectException("Unknown instance :" + this);
	} 
}
