package ch.ethz.inspire.emod.model.units;

/**
 * Implements varoius SI constants
 * @author sizuest
 *
 */
public class SiConstants {
	/**
	 *  R: Universal gas constant [J/mol/K]
	 */
	public static final PhysicalValue R = new PhysicalValue(8.3144598, new SiUnit("J mol^-1 K^-1"));
	/**
	 *  g: Normal acceleration due to gravitation [m/s²]
	 */
	public static final PhysicalValue g = new PhysicalValue(9.81, new SiUnit("m s⁻2"));
	

}
