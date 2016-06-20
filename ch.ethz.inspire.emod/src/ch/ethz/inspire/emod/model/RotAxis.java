/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/

package ch.ethz.inspire.emod.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.dd.Duct;
import ch.ethz.inspire.emod.model.fluid.FECDuct;
import ch.ethz.inspire.emod.model.thermal.ThermalArray;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.simulation.DynamicState;

/**
 * General rotational axis model class.
 * Implements the physical model of a linear axis.
 * From the input parameter rotational speed and process torque, 
 * the requested motor torque is calculated
 * 
 * Assumptions:
 * The inertia of mass and frictional forces are negligible
 * 
 * Inputlist:
 * 	 1: State        : [-]  : State of the axis (0/1)
 *   2: Speed        : [Hz] : Actual rotational speed
 *   3: ProcessTorque: [Nm] : Actual process torque
 *   4: CoolantIn    : [-]  : Coolant fluid in
 * Outputlist:
 *   1: PTotal      : [W]    : Input power
 *   2: PUse        : [W]    : Output power (usable)
 *   3: PLoss       : [W]    : Output power (losses)
 *   4: CoolantOut  : [-]    : Coolant fluid out
 *   
 * Config parameters:
 *   Transmission         : [-]    : Transmission between the motor (rpm) 
 *   								 and the axis (rpm)
 *   Inertia			  : [kg m] : Inertia of the moving part
 *   MotorType
 *   StructureMass
 *   StructureMaterial
 *   StructureDuct
 * 
 * @author simon
 *
 */
@XmlRootElement
public class RotAxis extends APhysicalComponent implements Floodable{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer state;
	private IOContainer speed;
	private IOContainer torque;
	private FluidContainer fluidIn;
	// Output parameters:
	private IOContainer puse;
	private IOContainer ploss;
	private IOContainer ptotal;
	private FluidContainer fluidOut;
	
	// Save last input values
	private double lastspeed, 
				   lasttorque;
	
	// Parameters used by the model. 
	private double transmission;	// [mm/rev] Transmission ratio
	private double inertia;         // [kg]     Moved mass
	private double movingMass;
	private double lever;
	private String motorType;
	private double mass;
	private double powerBreakOn, powerBreakOff;
	
	// SubModels
	private AMotor motor;
	private ThermalElement massCooled;
	private ThermalArray coolant;
	private Duct duct;
	private MovingMass massMoved;
	
	// FluidProperties
	FluidCircuitProperties fluidProperties;
	private boolean coolantConnected = false;

	
	
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public RotAxis() {
		super();
	}
	
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Linear Axis constructor
	 * 
	 * @param type
	 */
	public RotAxis(String type) {
		super();
		
		this.type=type;
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 */
	private void init()
	{
		/* Define Input parameters */
		inputs = new ArrayList<IOContainer>();
		state  = new IOContainer("State",         new SiUnit(Unit.NONE), 0, ContainerType.CONTROL);
		speed  = new IOContainer("Speed",         new SiUnit(Unit.REVOLUTIONS_S), 0, ContainerType.MECHANIC);	
		torque = new IOContainer("ProcessTorque", new SiUnit(Unit.NEWTONMETER), 0, ContainerType.MECHANIC);
		inputs.add(state);
		inputs.add(speed);
		inputs.add(torque);
		
		/* Define output parameters */
		outputs  = new ArrayList<IOContainer>();
		puse     = new IOContainer("PUse",     new SiUnit(Unit.WATT),          0, ContainerType.MECHANIC);
		ploss    = new IOContainer("PLoss",    new SiUnit(Unit.WATT),          0, ContainerType.THERMAL);
		ptotal   = new IOContainer("PTotal",   new SiUnit(Unit.WATT),          0, ContainerType.MECHANIC);
		outputs.add(puse);
		outputs.add(ploss);
		outputs.add(ptotal);
		
		/* ************************************************************************/
		/*         Read configuration parameters: */
		/* ************************************************************************/
		ComponentConfigReader params = null;
		/* Open file containing the parameters of the model type: */
		try {
			params = new ComponentConfigReader(getModelType(), type);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Read the config parameter: */
		try {
			transmission  = params.getDoubleValue("Transmission");
			inertia       = params.getDoubleValue("Inertia");
			motorType     = params.getString("MotorType");
			mass          = params.getDoubleValue("StructureMass");
			powerBreakOn  = params.getValue("PowerBreakOn", 0.0);
			powerBreakOff = params.getValue("PowerBreakOff", 0.0);
			movingMass    = params.getValue("Mass", 0.0);
			lever         = params.getValue("Lever", 0.0);
			
			/* Sub Model Motor */
			String[] mdlType = motorType.split("_",2);
			
			// Create Sub Elements
			try {
				// Get class and constructor objects
				Class<?>        cl = Class.forName("ch.ethz.inspire.emod.model."+mdlType[0]);
				Constructor<?>  co = cl.getConstructor(String.class);
				// initialize new component
				motor = (AMotor) co.newInstance(mdlType[1]);
			} catch (Exception e) {
				Exception ex = new Exception("Unable to create component "+mdlType[0]+"("+mdlType[1]+")"+" : " + e.getMessage());
				ex.printStackTrace();
				motor = null;
			} 
			
			/* Duct */
			duct       = Duct.buildFromFile(getModelType(), getType(), params.getString("StructureDuct"));
			coolant    = new ThermalArray("Example", duct.getVolume(), 20);
			massCooled = new ThermalElement(params.getString("StructureMaterial"), mass);
			
			/* Fluid properties */
			fluidProperties = new FluidCircuitProperties(new FECDuct(duct, coolant.getTemperature()), coolant.getTemperature());
			
			/* Define fluid in-/outputs */
			fluidIn        = new FluidContainer("CoolantIn",  new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
			inputs.add(fluidIn);
			fluidOut       = new FluidContainer("CoolantOut", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC,fluidProperties);
			outputs.add(fluidOut);
			
			// Change state name
			coolant.getTemperature().setName("TemperatureCoolant");
			massCooled.getTemperature().setName("TemperatureStructure");
			
			/* Fluid circuit parameters */
			coolant.setMaterial(fluidProperties.getMaterial());
			duct.setMaterial(fluidProperties.getMaterial());
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */
		
		// Validate the parameters:
		try {
		    checkConfigParams();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		
		/* Initialize sub-model */
		massMoved = new MovingMass(movingMass, inertia, 0, lever);
		
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(0, massMoved.getDynamicStateList().get(1));
		dynamicStates.add(1, massCooled.getTemperature());
		dynamicStates.add(2, coolant.getTemperature());
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{		
		// Check model parameters:
		// Parameter must be non negative
    	if (inertia < 0) {
    		throw new Exception("RotAxis, type:" + type +
    				": Negative value: Inertia must be non negative");
    	}
    	
		// Transmission must be non zero
    	if (0==transmission) {
    		throw new Exception("LinearAxis, type:" + type +
			": Zero value: Transmission must be non zero");
    	}
    	
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		double alphaCoolant;
		
		/* Update sub model */
		massMoved.setSimulationTimestep(timestep);
		massMoved.getInput("SpeedRot").setValue(speed.getValue());
		massMoved.update();
		
		lastspeed  = speed.getValue();                                             // [rad/s]
		lasttorque = torque.getValue()+massMoved.getOutput("Torque").getValue();   // [N]
		
		if(1==state.getValue()){
		
			/* Rotation speed
			 * The requested rotational speed is given by the transmission
			 */
			motor.getInput("RotSpeed").setValue(lastspeed/transmission);
			
			/* Torque
			 * The absolute torque  is given as
			 * T = k*Tp
			 */
			motor.getInput("Torque").setValue(transmission * lasttorque );
			motor.update();
			/* Powers */
			puse.setValue(Math.abs(lastspeed*2*Math.PI*lasttorque));
			ptotal.setValue(motor.getOutput("PTotal").getValue()+powerBreakOff);
			ploss.setValue(ptotal.getValue()-puse.getValue());
		}
		else{
			motor.getOutput("PUse").setValue(0);
			motor.getOutput("PLoss").setValue(powerBreakOn);
			motor.getOutput("PTotal").setValue(powerBreakOn);
			
			puse.setValue(0);
			ptotal.setValue(0);
			ploss.setValue(0);
		}
		
		if(coolantConnected) {
		
			// Thermal resistance
			alphaCoolant = duct.getThermalResistance(fluidProperties.getFlowRate(), 
					fluidProperties.getPressureIn(), 
					coolant.getTemperature().getValue(), 
					massCooled.getTemperature().getValue());
					
			
					
			// Coolant
			coolant.setThermalResistance(alphaCoolant);
			coolant.setFlowRate(fluidProperties.getFlowRate());
			coolant.setHeatSource(0.0);
			coolant.setTemperatureAmb(massCooled.getTemperature().getValue());
			coolant.setTemperatureIn(fluidIn.getTemperature());
			
			
			// Thermal flows
			massCooled.setHeatInput(motor.getOutput("PLoss").getValue());
			massCooled.addHeatInput(coolant.getHeatLoss());
					
			// Update submodels
			massCooled.integrate(timestep);
			coolant.integrate(timestep, 0, 0, fluidProperties.getPressure());
		}
		
		
			
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public ArrayList<FluidCircuitProperties> getFluidPropertiesList() {
		ArrayList<FluidCircuitProperties> out = new ArrayList<FluidCircuitProperties>();
		if(coolantConnected)
			out.add(fluidProperties);
		return out;
	}
	
	@Override
	public void flood(){
		if(fluidProperties.getPost().size() != 0 | fluidProperties.getPre().size() != 0)
			coolantConnected = true;
	}
	
}