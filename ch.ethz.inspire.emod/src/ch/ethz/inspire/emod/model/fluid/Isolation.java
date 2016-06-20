package ch.ethz.inspire.emod.model.fluid;

import ch.ethz.inspire.emod.utils.ParameterSet;
import ch.ethz.inspire.emod.utils.Parameterizable;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.units.SiUnit;

/**
 * General implementation of a duct element isolation
 * @author sizuest
 *
 */
@XmlRootElement
public class Isolation implements Parameterizable {
	@XmlElement
	private double thickness;
	@XmlElement
	private String type = "none";
	private Material material;
	
	
	/**
	 * 
	 */
	public Isolation(){}
	
	/**
	 * @param type
	 * @param thickness
	 */
	public Isolation(String type, double thickness){
		this.type = type;
		this.thickness = thickness;
		init();
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	private void init(){
		if(this.thickness == 0)
			this.material = null;
		else
			this.material = new Material(type);
	}
	
	/**
	 * Returns the area specific thermal resistance
	 * @return [W/m^2/K]
	 */
	public double getThermalResistance(){
		if(null==this.material)
			return Double.POSITIVE_INFINITY;
		
		return this.material.getThermalConductivity()/this.thickness;
	}
	
	/**
	 * Returns the area specific thermal resistance for a pipe
	 * @param ri
	 * @return [W/m^2/K]
	 */
	public double getThermalResistanceCircular(double ri){
		return this.material.getThermalConductivity()/Math.log(1+this.thickness/ri)/ri;
	}
	
	public String toString(){
		return this.material.getType()+": "+this.thickness;
	}
	
	public void setMaterial(String type){
		this.type = type;
		init();
	}

	public ParameterSet getParameterSet() {
		ParameterSet ps = new ParameterSet("Isolation");
		ps.setParameter("Thickness", this.thickness, new SiUnit("m"));
		return ps;
	}

	@XmlTransient
	public void setParameterSet(ParameterSet ps) {
		this.thickness = ps.getParameter("Thickness").getValue();		
	}

	/**
	 * Returns a copy of the isolation
	 * @return {@link Isolation}
	 */
	public Isolation clone() {
		return new Isolation(type, thickness);
	}
	
	/**
	 * @param iso
	 */
	public void setIsolation(Isolation iso){
		this.type = iso.type;
		this.thickness = iso.thickness;
		init();
	}

	/**
	 * @return
	 */
	public Material getMaterial() {
		return this.material;
	}

	public double getThickness() {
		return this.thickness;
	}
}
