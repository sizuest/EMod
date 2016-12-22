/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date: 2014-10-30 16:24:44$
 * 
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.gui.utils;

/**
 * Class to handle which buttons are to be displayed in a config gui
 * Possible buttons are:
 * - OK
 * - RESET
 * - CANCEL
 * @author sizuest
 *
 */
public class ShowButtons {
	/**
	 * NONE = 0b000
	 */
	public static final int NONE = 0;
	/**
	 * OK = 0b001
	 */
	public static final int OK = 1;
	/**
	 * RESET = 0b010
	 */
	public static final int RESET = 2;
	/**
	 * CANCEL = 0b100
	 */
	public static final int CANCEL = 4;
	/**
	 * ALL = 0b111
	 */
	public static final int ALL = 7;

	/**
	 * Return if the OK button is to be shown
	 * @param button
	 * @return
	 */
	public static boolean ok(int button) {
		return (1 & button) > 0;
	}

	/**
	 * Return if the RESET button is to be shown
	 * @param button
	 * @return
	 */
	public static boolean reset(int button) {
		return (2 & button) > 0;
	}

	/**
	 * Return if the CANCEL button is to be shown
	 * @param button
	 * @return
	 */
	public static boolean cancel(int button) {
		return (4 & button) > 0;
	}

	
	/**
	 * Return the amount of buttons to be shown
	 * @param button
	 * @return
	 */
	public static int count(int button) {
		int count = Integer.bitCount(button);
		return count;
	}

}
