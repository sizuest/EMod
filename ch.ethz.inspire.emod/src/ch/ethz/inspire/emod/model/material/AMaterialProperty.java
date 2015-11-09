package ch.ethz.inspire.emod.model.material;

/**
 * Implements a general material property
 * @author simon
 *
 */
public abstract class AMaterialProperty {
	
	private String name;
	
	public AMaterialProperty(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public abstract String toString();

}
