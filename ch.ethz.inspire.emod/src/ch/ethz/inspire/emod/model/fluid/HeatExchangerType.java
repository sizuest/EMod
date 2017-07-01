package ch.ethz.inspire.emod.model.fluid;

/**
 * Enumeration of different heat exchnager types
 * @author simon
 *
 */
public enum HeatExchangerType {
	/**
	 * Parallel flow
	 */
	PARALLELFLOW,
	/**
	 * Counter flow
	 */
	COUNTERFLOW,
	/**
	 * One shell pass
	 */
	ONESHELLPASS,
	/**
	 * N shell pass
	 */
	NSHELLPASS,
	/**
	 * Cross both unmixed
	 */
	CROSSUNMIXED,
	/**
	 * Cross Cmax unmixed
	 */
	CROSSCMAXMIXED,
	/**
	 * Cross Cmin unmixed
	 */
	CROSSCMINMIXED;
}
