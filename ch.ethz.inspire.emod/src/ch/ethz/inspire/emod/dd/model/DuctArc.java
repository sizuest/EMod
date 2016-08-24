package ch.ethz.inspire.emod.dd.model;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;

import ch.ethz.inspire.emod.model.fluid.Fluid;
import ch.ethz.inspire.emod.model.parameters.ParameterSet;
import ch.ethz.inspire.emod.model.units.SiUnit;

public class DuctArc extends ADuctElement {
	@XmlElement
	private double radius;
	@XmlElement
	private double count;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 */
	public DuctArc() {
		super();
	}
	
	/**
	 * Constructor by name
	 * @param name
	 */
	public DuctArc(String name){
		super();
		this.name     = name;
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Constructor for Testing
	 * 
	 * @param name 
	 * @param d 
	 * @param r 
	 * @param c 
	 */
	public DuctArc(String name, double d, double r, double c){
		this.name          = name;
		this.profile       = new HPCircular(d/2);
		this.radius		   = r;
		this.count         = c;
		init();
	}

	private void init() {
		super.length = this.radius*Math.PI/2;
	}
	
	@Override
	public double getSurface(){
		return this.count*super.getSurface();
	}
	
	@Override
	public double getHydraulicSurface(){
		return this.count*super.getHydraulicSurface();
	}
	
	@Override
	public double getVolume(){
		return this.count*super.getVolume();
	}

	@Override
	public ParameterSet getParameterSet() {
		ParameterSet ps = new ParameterSet(this.name);
		ps.setPhysicalValue("Radius", this.radius, new SiUnit("m"));
		ps.setPhysicalValue("Count", this.count, new SiUnit(""));
		return ps;
	}

	@Override
	public void setParameterSet(ParameterSet ps) {
		this.radius        = ps.getPhysicalValue("Radius").getValue();
		this.count         = ps.getPhysicalValue("Count").getValue();
		init();
	}

	@Override
	public double getHTC(double flowRate, double pressure,
			double temperatureFluid, double temperatureWall) {
		return Fluid.convectionForcedPipe(material, temperatureFluid, temperatureWall, length, this.profile, flowRate/this.count);
	}

	@Override
	public double getPressureDrop(double flowRate, double pressure,
			double temperatureFluid) {
		return Fluid.pressureLossArc(getMaterial(), temperatureFluid, getProfile(), radius, flowRate);
	}

	@Override
	public ADuctElement clone() {
		DuctArc clone = new DuctArc();
		
		clone.setParameterSet(this.getParameterSet());
		if(null==this.isolation)
			clone.setIsolation(null);
		else
			clone.setIsolation(this.isolation.clone());
		clone.setName(this.getName());
		
		clone.setProfile(getProfile().clone());
		
		return clone;
	}

}
