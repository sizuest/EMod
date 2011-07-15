/***********************************
 * $$Id$$
 *
 * $$URL$$
 * $$Author$$
 * $$Date$$
 * $$Rev$$
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/

package ch.ethz.inspire.emod.simulation;

/**
 * States of machine components.
 * A component implements a subset of the defined states only. 
 * 
 * @author andreas
 *
 */
public enum ComponentState {
	ON, OFF, STANDBY, PERIODIC, CONTROLLED;
}
