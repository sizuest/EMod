package ch.ethz.inspire.emod.model;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.femexport.BoundaryCondition;
import ch.ethz.inspire.emod.femexport.BoundaryConditionType;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.units.ContainerType;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.IOContainer;

/**
 * Implelements the physical model of the friction in an airgap
 * @author simon
 *
 */
@XmlRootElement
public class AirGap extends APhysicalComponent{
	
	@XmlElement
	protected String type;
	
	// Model inputs
	private IOContainer rotspeed, temperature;
	
	// Model outputs
	private IOContainer heatSrc;
	
	// Model parameters
	private double diameter;
	private double gapWidth;
	private double gapLength;
	private Material fluid;
	
	// Boundary Conditions
	private BoundaryCondition bcHeatSrcAirgap;
	
	// Empirical parameters
	/*
	 * ┌──────┬────────┬────────┬─────────┬─────────┬─────────┬─────────┐
	 * │      │ Re=500 │ Re=900 │ Re=1300 │ Re=1700 │ Re=2100 │ Re=2500 │
	 * ├──────┼────────┼────────┼─────────┼─────────┼─────────┼─────────┤
	 * │    a │ 0.0004 │ 0.0046 │  0.0089 │  0.0131 │  0.0174 │  0.0217 │
	 * ├──────┼────────┼────────┼─────────┼─────────┼─────────┼─────────┤
	 * │ tau0 │ 0.1184 │ 0.1899 │  0.2615 │  0.3330 │  0.4046 │  0.4761 │
	 * └──────┴────────┴────────┴─────────┴─────────┴─────────┴─────────┘
	 */
	private double[] ReSamples   = {500, 900, 1300, 1700, 2100, 2500};
	private double[] aSamples    = {0.0004, 0.0046, 0.0089, 0.0131, 0.0174, 0.0217}; 
	private double[] tau0Samples = {0.1184, 0.1899, 0.2615, 0.3330, 0.4046, 0.4761}; 
	
	
	/**
	 * Constructor called from XmlUnmarshaller. Attribute 'type' is set by
	 * XmlUnmarshaller.
	 */
	public AirGap(){
		super();
	}
	
	/**
	 * Air gap constructor
	 * @param type
	 */
	public AirGap(String type){
		super();

		this.type = type;
		init();
		loadParams();
	}
	
	/**
	 * @param diameter
	 * @param gapWidth
	 * @param gapLength
	 * @param fluidType
	 */
	public AirGap(double diameter, double gapWidth, double gapLength, String fluidType){
		super();
		
		this.diameter = diameter;
		this.gapWidth = gapWidth;
		this.gapLength = gapLength;
		this.fluid = new Material(fluidType);
		
		init();
	}
	
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		// post xml init method (loading physics data)
		init();
		loadParams();
	}
	
	
	private void init(){
		inputs = new ArrayList<IOContainer>();
		outputs = new ArrayList<IOContainer>();
		
		rotspeed    = new IOContainer("RotSpeed", new SiUnit("Hz"), 0, ContainerType.MECHANIC);
		temperature = new IOContainer("Temperature", new SiUnit("K"), 293.15, ContainerType.THERMAL);
		heatSrc     = new IOContainer("PLoss",    new SiUnit("W"),  0, ContainerType.THERMAL);
		
		inputs.add(rotspeed);
		inputs.add(temperature);
		outputs.add(heatSrc);
		
		bcHeatSrcAirgap = new BoundaryCondition("HeatSrcAirgap", new SiUnit("W"), 0, BoundaryConditionType.NEUMANN);
	}
	
	private void loadParams(){
		/* *********************************************************************** */
		/* Read configuration parameters: */
		/* *********************************************************************** */
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader(getModelType(), type);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/* Read the config parameter: */
		try {
			diameter  = params.getPhysicalValue("Diameter", new SiUnit("m")).getValue();
			gapWidth  = params.getPhysicalValue("GapWidth", new SiUnit("m")).getValue();
			gapLength = params.getPhysicalValue("GapLength", new SiUnit("m")).getValue();
			fluid     = new Material(params.getString("Material"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */
	}
	
	@Override
	public String getType() {
		return type;
	}

	@Override
	public void update() {
		double v, tau, Re, omega, a ,tau0;
		
		omega = 2*Math.PI*rotspeed.getValue();
		
		// Gap wall velocity
		v = omega*diameter/2;
		
		// Gap Re number
		Re = gapWidth*v/fluid.getViscosityKinematic(temperature.getValue());
		
		// gap shear rate
		a = Algo.linearInterpolation(Re, ReSamples, aSamples);
		tau0 = Algo.linearInterpolation(Re, ReSamples, tau0Samples);
		tau = tau0 + a*v;
		
		// Heat source
		heatSrc.setValue(omega*Math.PI*tau*Math.pow(diameter, 2)*gapLength);
		
	}

	@Override
	public void updateBoundaryConditions() {
		bcHeatSrcAirgap.setValue(heatSrc.getValue());
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

}
