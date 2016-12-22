package ch.ethz.inspire.emod.simulation;

/**
 * Representation of the status of a configuration
 * @author simon
 *
 */
public enum ConfigState {
	/**
	 * Everything ok, no problems expected
	 */
	OK, 
	/**
	 * Minor issues, no technical problems expected
	 */
	WARNING, 
	/**
	 * Major issues, technical problems expected
	 */
	ERROR
}
