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
 * Extension of java.util.logging.Level for extra levels of System.err, debug
 * and System.out output logging
 * 
 * @author dhampl
 * 
 */
@SuppressWarnings("serial")
public class LogLevel extends Level {

	private LogLevel(String name, int value) {
		super(name, value);
	}

	/**
	 * DEBUG is equivalent to FINE!
	 */
	public static Level DEBUG = new LogLevel("DEBUG", Level.FINE.intValue());

	protected Object readResolve() throws ObjectStreamException {
		if (this.intValue() == DEBUG.intValue())
			return DEBUG;
		throw new InvalidObjectException("Unknown instance :" + this);
	}
}
