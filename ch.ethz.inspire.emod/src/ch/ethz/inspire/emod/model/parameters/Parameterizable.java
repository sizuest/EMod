package ch.ethz.inspire.emod.model.parameters;

/**
 * Interface for a parametrizable (physical parameters!) class
 * @author sizuest
 *
 */

public interface Parameterizable {
	
	/**
	 * Return the parameter set
	 * @return {@link ParameterSet.java}
	 */
	
	public ParameterSet getParameterSet();
	
	/**
	 * Write the parameter set given as ps
	 * @param ps {@link ParameterSet.java}
	 */
	
	public void setParameterSet(ParameterSet ps);

}
