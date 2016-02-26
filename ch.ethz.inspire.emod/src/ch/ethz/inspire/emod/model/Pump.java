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

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.fluid.FECPump;
import ch.ethz.inspire.emod.model.fluid.Fluid;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.thermal.ThermalElement;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidCircuitProperties;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Pump model class.
 * Implements the physical model of a pump with reservoir.
 * From the input parameter mass flow, the electrical power
 * and the supply mass flow are calculated.
 * 
 * Assumptions:
 * Perfect gas
 * 
 * 
 * Inputlist:
 *   1: State       : [-]    : State of the Pump
 *   2: FluidIn     : [-]    : Fluid flowing into Pump
 * Outputlist:
 *   1: PTotal      : [W]    : Demanded electrical power
 *   2: PLoss       : [W]    : Thermal pump losses
 *   3: PUse        : [W]    : Power in the pluid
 *   4: Temperature : [K]    : Pump structural Temperature
 *   5: FluidOut    : [-]    : Fluid flowing out of the Pump
 *   
 * Config parameters:
 *   PressureSamples      : [Pa]    : Pressure samples for liner interpolation
 *   FlowRateSamples      : [m^3/s] : Volumetric flow samples for liner interpolation
 *   ElectricalPower	  : [W]     : Power samples for liner interpolation
 * 
 * @author manick
 *
 */
@XmlRootElement
public class Pump extends APhysicalComponent implements Floodable{

	@XmlElement
	protected String type;
	
	// Input parameters:
	private IOContainer pumpCtrl;
	private IOContainer temperatureAmb;
	private FluidContainer fluidIn;
	
	// Output parameters:
	private IOContainer pel;
	private IOContainer pth;
	private IOContainer pmech;
	private FluidContainer fluidOut;
	private IOContainer heatFlowAmbient;
	private IOContainer heatFlowTank;
	
	
	// Parameters used by the model. 
	private double[] pressureSamples;  			// Samples of pressure [Pa]
	private double[] flowRateSamples;   		// Samples of flow rate [m^3/s]
	private double[] effPumpSamples;			// Samples of the pump eff [-]
	private double[] powerSamples;				// Samples of power demand [W]
	private double   massFluid       = 3;		// Mass of the fluid in the pump [kg]
	private double   massMotor       = 29;		// Mass of the motor [kg]
	private boolean  hasMotorCooling = true;	// Forced convection at the motor
	private boolean  isSubmerged     = true;	// Submerged Pump (heat flow to tank)
	private double   diameterPump    = .13;		// Diameter of the pump [m]
	private double   lengthPump      = .385;	// Length of the pump [m]
	private double   diameterMotor   = .178;	// Diameter of the motor [m]
	private double   lengthMotor     = .321;	// Length of the motor [m]
	private double   rotSpeed    = 2900;		// Nominal rotational speed [rpm]
	private int      numImpEyes  = 1;			// Number of impeller entries [-]
	private int      numStages   = 3;			// Number of stages [-]
	private double   flowRateOpt = 66.7/6e4;	// Nominal flow rate [m^3/s]
	private double   pressureOpt = 80.1*9810;	// Nominal pressure [Pa]
	
	// Parameters calculated by the model
	private double[] effMotorSamples;			// Samples of the motor eff [-]
	private double surfaceMotor,				// Surface available for convetion [m2];
	               surfacePump;
	
	// Corrected efficiency map
	private double[] powerSamplesV, pressureSamplesV, flowRateSamplesV, effPumpSamplesV;
	private double lastDensity = 0, lastViscosity = 0; 
	private double htcMotorForced;
	
	private double temperatureInit;
		
	// Pump Structure
	private ThermalElement structure, fluid;
	
	
	// Fluid Properties
	FluidCircuitProperties fluidProperties;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Pump() {
		super();
		
		this.type = "Example";
		this.temperatureInit = 293;
		init();
		this.fluidProperties.getMaterial().setMaterial("Monoethylenglykol_34");
	}
	
	/**
	 * @param type
	 * @param temperatureInit
	 * @param fluidType
	 */
	public Pump(String type, double temperatureInit, String fluidType){
		super();
		
		this.type = type;
		this.temperatureInit = temperatureInit;
		init();
		this.fluidProperties.getMaterial().setMaterial(fluidType);
	}
	
	/**
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Pump constructor
	 * 
	 * @param type
	 */
	public Pump(String type) {
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
		inputs      = new ArrayList<IOContainer>();
		pumpCtrl       = new IOContainer("State", new SiUnit(Unit.NONE), 0, ContainerType.CONTROL);
		temperatureAmb = new IOContainer("TemperatureAmb", new SiUnit(Unit.KELVIN), temperatureInit, ContainerType.THERMAL);
		inputs.add(pumpCtrl);
		inputs.add(temperatureAmb);
		
		/* Define output parameters */
		outputs    = new ArrayList<IOContainer>();
		pel        = new IOContainer("PTotal",     new SiUnit(Unit.WATT), 0.00, ContainerType.ELECTRIC);
		pth        = new IOContainer("PLoss",      new SiUnit(Unit.WATT), 0.00, ContainerType.THERMAL);
		pmech      = new IOContainer("PUse",       new SiUnit(Unit.WATT), 0.00, ContainerType.FLUIDDYNAMIC);
		heatFlowAmbient = new IOContainer("HeatFlowAmbient", new SiUnit(Unit.WATT)  , 0, ContainerType.THERMAL);
		heatFlowTank    = new IOContainer("HeatFlowTank",    new SiUnit(Unit.WATT)  , 0, ContainerType.THERMAL);
		outputs.add(pel);
		outputs.add(pth);
		outputs.add(pmech);
		outputs.add(heatFlowAmbient);
		outputs.add(heatFlowTank);

		
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
			pressureSamples = params.getDoubleArray("PressureSamples");
			flowRateSamples = params.getDoubleArray("FlowRateSamples");
			powerSamples    = params.getDoubleArray("PowerSamples");
			effPumpSamples  = params.getDoubleArray("EfficiencySamples");
			
			massFluid       = params.getDoubleValue("MassFluid");
			massMotor       = params.getDoubleValue("MassMotor");
			hasMotorCooling = params.getBooleanValue("HasMotorCooling");
			isSubmerged     = params.getBooleanValue("IsSubmerged");
			diameterPump    = params.getDoubleValue("DiameterPump");
			lengthPump      = params.getDoubleValue("LengthPump");
			diameterMotor   = params.getDoubleValue("DiameterMotor");
			lengthMotor     = params.getDoubleValue("LengthMotor");
			rotSpeed        = params.getDoubleValue("NominalRotSpeed");
			numImpEyes      = params.getIntValue("NumberImpellerEyes");
			numStages       = params.getIntValue("NumberStages");
			flowRateOpt     = params.getDoubleValue("NominalFlowRate");
			pressureOpt     = params.getDoubleValue("NominalPressure");
			
			
			/* Motor efficiency */
			effMotorSamples = new double[effPumpSamples.length];
			for(int i=flowRateSamples.length-1; i>=0; i--){
				if(0==flowRateSamples[i] & flowRateSamples.length-i>1)
					effMotorSamples[i] = effMotorSamples[i+1];
				else if(effPumpSamples[i]==0)
					effMotorSamples[i] = 0;
				else
					effMotorSamples[i] = pressureSamples[i]*flowRateSamples[i]/powerSamples[i]/effPumpSamples[i];
			}
			
			/* Surface for convection */
			surfaceMotor = lengthMotor*diameterMotor*Math.PI;
			surfacePump  = lengthPump *diameterPump *Math.PI;
			
			/* Forced convection on motor */
			double lossMax = 0;
			for(int i=0; i<effPumpSamples.length; i++)
				lossMax = Math.max(powerSamples[i]*(1-effMotorSamples[i]), lossMax);
			htcMotorForced = lossMax / surfaceMotor / 20;
			

			pressureSamplesV = new double[pressureSamples.length];
			flowRateSamplesV = new double[flowRateSamples.length];
			powerSamplesV    = new double[powerSamples.length];
			effPumpSamplesV  = new double[effPumpSamples.length];
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		dynamicStates = new ArrayList<DynamicState>();
		
		
		params.Close(); /* Model configuration file not needed anymore. */
		
		//* Validate the parameters: */
		try {
		    checkConfigParams();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		
		/* Structure */
		structure = new ThermalElement(new Material("Motor"), massMotor);
		structure.getTemperature().setName("TemperatureStructure");
		dynamicStates.add(structure.getTemperature());
		
		/* Fluid */
		fluid = new ThermalElement(new Material("Example"), massFluid);
		fluid.getTemperature().setName("TemperatureFluid");
		dynamicStates.add(fluid.getTemperature());
		
		/* Define FlowRate */
		fluidProperties = new FluidCircuitProperties(new FECPump(this, pumpCtrl), fluid.getTemperature());
		fluidProperties.setMaterial(new Material("Example"));
		fluid.setMaterial(fluidProperties.getMaterial());
		
		/* Define FluidIn parameter */
		fluidIn        = new FluidContainer("FluidIn", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
		inputs.add(fluidIn);

		/* Define FluidOut parameter */
		fluidOut        = new FluidContainer("FluidOut", new SiUnit(Unit.NONE), ContainerType.FLUIDDYNAMIC, fluidProperties);
		outputs.add(fluidOut);
		fluidOut.getFluidCircuitProperties().setTemperature(fluid.getTemperature());		
		
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{		
		// Check model parameters:
		// Check dimensions:
		if (pressureSamples.length != flowRateSamples.length) {
			throw new Exception("Pump, type:" +type+ 
					": Dimension missmatch: Vector 'pressureSamples' must have same dimension as " +
					"'flowRateSamples' (" + pressureSamples.length + "!=" + flowRateSamples.length + ")!");
		}
		if (pressureSamples.length != powerSamples.length) {
			throw new Exception("Pump, type:" +type+ 
					": Dimension missmatch: Vector 'pressureSamples' must have same dimension as " +
					"'powertSamples' (" + pressureSamples.length + "!=" + powerSamples.length + ")!");
		}		
		// Check if sorted:
		for (int i=1; i<flowRateSamples.length; i++) {
			if (flowRateSamples[i] <= flowRateSamples[i-1]) {
				throw new Exception("Pump, type:" +type+ 
						": Sample vector 'flowRateSamples' must be sorted!");
			}
		}
		// Check size
		for(int i=1; i<powerSamples.length;i++)
			if (powerSamples[i]<=0){
				throw new Exception("Pump, type:" +type+ 
						": Negative or zero value: Pump power must be strictly positive!");
			}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		
		fluid.setMaterial(fluidProperties.getMaterial());
		
		double heatLossMotor, heatLossFluid;
		double htcMotor, htcPump;
		
		/* Check if pump map has to be updated */
		if(  fluid.getMaterial().getDensity(fluid.getTemperature().getValue()) != lastDensity |
		     fluid.getMaterial().getViscosityKinematic(fluid.getTemperature().getValue()) != lastViscosity ){
			lastDensity   = fluid.getMaterial().getDensity(fluid.getTemperature().getValue());
			lastViscosity = fluid.getMaterial().getViscosityKinematic(fluid.getTemperature().getValue());
			updatePumpMap(lastDensity, lastViscosity);
		}
		
		
		
		/* If pump is running calculate flow rate and power demand */
		if(pumpCtrl.getValue()>0){
			// Resulting power demand
			pel.setValue(Algo.linearInterpolation(fluidProperties.getFlowRate(), flowRateSamplesV, powerSamplesV));
		}
		else{
			pel.setValue(0);
		}
			
		
		/* 
		 * The mechanical power is given by the pressure and the voluminal flow:
		 * Pmech = pFluid [Pa] * Vdot [m3/s]
		 */
		pmech.setValue( -fluidProperties.getFlowRate() * fluidProperties.getPressureDrop() );
		
		/* 
		 * The Losses are the difference between electrical and mechanical power
		 */
		pth.setValue(pel.getValue()-pmech.getValue());		
				
		/* Losses */
		heatLossMotor = pel.getValue()*(1-Algo.linearInterpolation(fluidProperties.getFlowRate(), flowRateSamplesV, effMotorSamples));
		heatLossFluid = pel.getValue()-heatLossMotor-pmech.getValue();
		
		/* Heat fluxes */
		
		// HTC estimations
		if(pumpCtrl.getValue()>0 & hasMotorCooling)
			htcMotor = htcMotorForced;
		else
			htcMotor = Fluid.convectionFreeCylinderVert(new Material("Air"), structure.getTemperature().getValue(), temperatureAmb.getValue(), lengthMotor, diameterMotor);
		
		if(isSubmerged)
			htcPump = Fluid.convectionFreeCylinderVert(fluid.getMaterial(), fluid.getTemperature().getValue(), fluidIn.getTemperature(), lengthPump, diameterPump);
		else
			htcPump = Fluid.convectionFreeCylinderVert(new Material("Air"), fluid.getTemperature().getValue(), temperatureAmb.getValue(), lengthPump, diameterPump);
		
		// Structure
		structure.setHeatInput(heatLossMotor);
		structure.setThermalResistance(htcMotor*surfaceMotor);
		structure.setTemperatureAmb(temperatureAmb.getValue());
		
		// Fluid
		fluid.setHeatInput(heatLossFluid);
		fluid.setThermalResistance(htcPump*surfacePump);
		fluid.setTemperatureIn(fluidIn.getTemperature());
		if(isSubmerged)
			fluid.setTemperatureAmb(fluidIn.getTemperature());
		else
			fluid.setTemperatureAmb(temperatureAmb.getValue());
		
		/* Integrate */
		structure.integrate(timestep);
		fluid.integrate(timestep, fluidProperties.getFlowRate(), fluidProperties.getFlowRate(), fluidProperties.getPressure());
		
		/* Write outputs */
		if(isSubmerged){
			heatFlowAmbient.setValue(structure.getBoundaryHeatFlux());
			heatFlowTank.setValue(fluid.getBoundaryHeatFlux());
		}
		else{
			heatFlowAmbient.setValue(structure.getBoundaryHeatFlux()+fluid.getBoundaryHeatFlux());
			heatFlowTank.setValue(0);
		}
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#getType()
	 */
	@Override
	public String getType() {
		return type;
	}
	
	/**
	 * set Type of the Pump
	 * @param type the type of the pump to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * get the fluid type
	 * @return the fluid type
	 */
	public String getFluidType(){
		return fluidProperties.getMaterial().getType();
	}
	
	
	/**
	 * Recalculate pump map for new viscosity
	 * @param temperature Temperature [K]
	 */
	public void updatePumpMap(double temperature){
		double density   = fluid.getMaterial().getDensity(temperature);
		double viscosity = fluid.getMaterial().getViscosityKinematic(temperature);
		updatePumpMap(density, viscosity);
	}
	
	
	/**
	 * Recalculate pump map for new viscosity
	 * @param rho Density [kg/m^3]
	 * @param nu  Viscosity [m^2/s]
	 */
	private void updatePumpMap(double rho, double nu){
		double Re, ReMod, fHopt, fEta, fQ, fH, omega, omegaS;
		
		if(numImpEyes == 0 | numStages == 0){
			/* Update map */
			for(int i=0; i<flowRateSamples.length; i++){
				flowRateSamplesV[i] = flowRateSamples[i]; 
				pressureSamplesV[i] = pressureSamples[i];
				effPumpSamplesV[i]  = effPumpSamples[i];
				powerSamplesV[i]    = powerSamples[i];
			}
		}
		else {
			omega = rotSpeed/30*Math.PI;
			
			/* Reynolds number */
			Re = omega*Math.pow(diameterPump/2,2)/nu;
			
			/* Univ. spec. speed */
			omegaS = omega*Math.sqrt(flowRateOpt/numImpEyes) / Math.pow(pressureOpt/rho/numStages, .75);
			
			/* Modified reynolds number */
			ReMod = Re*Math.pow(omegaS, 1.5)*Math.pow(numImpEyes, 0.75);
			
			/* Correction factors */
			fHopt = Math.pow(ReMod, -6.7/Math.pow(ReMod, .735));
			
			fEta  = Math.pow(ReMod, -19.0/Math.pow(ReMod, .705));
			fQ    = fHopt;
		
			/* Update map */
			for(int i=0; i<flowRateSamples.length; i++){
				if(0 != flowRateSamples[i]){
					fH = 1-(1-fHopt)*Math.pow(flowRateSamples[i]/flowRateOpt, .75);
					
					flowRateSamplesV[i] = fQ*flowRateSamples[i]; 
					pressureSamplesV[i] = fH*pressureSamples[i]*rho/1000;
					effPumpSamplesV[i]  = fEta*effPumpSamples[i];
					powerSamplesV[i]    = flowRateSamplesV[i]*pressureSamplesV[i]/effPumpSamplesV[i]/effMotorSamples[i];
				}
				else{
					flowRateSamplesV[i] = flowRateSamples[i]; 
					pressureSamplesV[i] = pressureSamples[i];
					effPumpSamplesV[i]  = effPumpSamples[i];
					powerSamplesV[i]    = powerSamples[i];
				}
			}	
		}
	}
	

	@Override
	public ArrayList<FluidCircuitProperties> getFluidPropertiesList() {
		ArrayList<FluidCircuitProperties> out = new ArrayList<FluidCircuitProperties>();
		out.add(fluidProperties);
		return out;
	}

	public double getPressure(double flowRate) {
		//if(flowRate<flowRateSamplesV[0] | flowRate>flowRateSamplesV[flowRateSamplesV.length-1])
		//	return 0;
		return Algo.linearInterpolation(flowRate, flowRateSamplesV, pressureSamplesV);
	}
	
	public double getPressureDrivative(double flowRate) {
		return Algo.numericalDerivative(flowRate, flowRateSamplesV, pressureSamplesV);
	}
	
	@Override
	public void flood(){/* Not used */}
}
