package ch.ethz.inspire.emod.model;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.thermal.ThermalArray;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;

/**
 * General Tank model class.
 * Implements the physical model of a tank
 * 
 * Assumptions:
 * -No leakage
 * -Separation between laminar and turbulent flow
 * -Smooth surface
 * -Tank wall is rigid
 * 
 * Inputlist:
 *   1: FluidIn       : [-]    : Fluid Container with temperature, pressure, massflow
 *   2: PressureAmb   : [Pa]   : Ambient Pressure (assuming free surface of fluid in tank)
 *   3: TemperatureAmb: [K]    : Ambient temperature
 * Outputlist:
 *   1: FluidOut      : [-]    : Fluid Container with temperature, pressure, massflow
 *   
 * Config parameters:
 * 	 Volume			: [m^3]
 * 		or
 *   Length	    	: [m]
 *   Width      	: [m] 
 *   Height			: [m]
 *   
 *	 Material		: [-]
 *   
 * 
 * @author manick
 *
 */
@XmlRootElement
public class Tank extends APhysicalComponent implements Floodable {

	@XmlElement
	protected String type;
	
	// Input parameters:
	//TODO manick: test for fluid
	private FluidContainer fluidIn;
	private IOContainer temperatureAmb;
	private IOContainer pressureAmb;
	private IOContainer heatFlowIn;
	
	//TODO manick: test heatexchanger
	private IOContainer heatExchangerIn;
		
	// Output parameters:
	//TODO manick: test for fluid
	private FluidContainer fluidOut;
	
	// Parameters used by the model.
	//TODO manick: combine to Object?
	private String material;
	private double volume;
	private double length = 0.00;
	private double width = 0.00;
	private double height = 0.00;
	private ThermalArray fluid;
	private double alphaFluid = 0.00;
	//private double lastpressure  = 0.00;
	//private double lastmassflow  = 0.00;
	//private double lasttemperature  = 293.00;
	double temperatureInit = 293.00;
	
	/**
	 * Constructor called from XmlUnmarshaller.
	 * Attribute 'type' is set by XmlUnmarshaller.
	 */
	public Tank() {
		super();
	}
	
	/**
	 * @param u
	 * @param parent
	 * @throws Exception 
	 */
	public void afterUnmarshal(final Unmarshaller u, final Object parent) throws Exception {
		//post xml init method (loading physics data)
		init();
	}
	
	/**
	 * Tank constructor
	 * @param type
	 * @throws Exception 
	 */
	public Tank(String type) {
		super();
		
		this.type = type;
		init();
	}
	
	/**
	 * Tank constructor
	 * @param type
	 * @param temperatureInit
	 * @param material type
	 * @throws Exception
	 */
	/*
	public Tank(String type, double temperatureInit, String material) {
		super();
		
		this.type = type;
		this.temperatureInit = temperatureInit;
		this.material = material;
		init();
	}
	*/
	
	/**
	 * Called from constructor or after unmarshaller.
	 * @throws Exception 
	 */
	private void init()
	{
		/* Define Input parameters */
		inputs         = new ArrayList<IOContainer>();
		temperatureAmb = new IOContainer("TemperatureAmb", Unit.KELVIN, temperatureInit, ContainerType.THERMAL);
		pressureAmb    = new IOContainer("PressureAmb", Unit.PA, 0.00, ContainerType.FLUIDDYNAMIC);
		heatFlowIn     = new IOContainer("HeatFlowIn", Unit.WATT, 0.00, ContainerType.THERMAL);
		heatExchangerIn= new IOContainer("HeatExchangerIn", Unit.WATT, 0.00, ContainerType.THERMAL);
		inputs.add(temperatureAmb);
		inputs.add(pressureAmb);
		inputs.add(heatFlowIn);
		inputs.add(heatExchangerIn);
		
		/* Define output parameters */
		outputs        = new ArrayList<IOContainer>();		
			
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
			System.exit(-1);
		}
		
		/* Read the config parameter: */
		try {
			volume		 = params.getDoubleValue("Volume");
		}
		catch (Exception e) {
			System.out.println("no property 'Volume', checking for 'Length'/'Depth'/'Height':");
			try{
				length   = params.getDoubleValue("Length");
				width    = params.getDoubleValue("Width");
				height   = params.getDoubleValue("Height");
				volume   = length * width * height;
			}
			catch (Exception ee){
				e.printStackTrace();
				ee.printStackTrace();
				//System.exit(-1);
			}
		}
		try {
			material     = params.getString("Material");
			/* Thermal Array */

			System.out.println("tank.init: setting the fluid " + material);
			if(material != null){
				setFluid(material);
			}
			//TODO manick: how many elements are necessary?
			//fluid = new ThermalArray(material, volume, 1);
			//System.out.println("tank init: " + material + volume + fluid.getMaterial().getType());
			
			//TODO manick: when get temperature INIT?
			//fluid.getTemperature().setInitialCondition(temperatureInit);
			//fluid.getTemperature().setInitialCondition(293);
			//fluid.setThermalResistance(1);
			
			/* State */
			//dynamicStates = new ArrayList<DynamicState>();
			//dynamicStates.add(fluid.getTemperature());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		params.Close(); /* Model configuration file not needed anymore. */
		
		// Validate the parameters:
		try {
		    checkConfigParams();
		}
		catch (Exception e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
		
		//TODO manick: test for Fluid
		fluidIn        = new FluidContainer("FluidIn", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		inputs.add(fluidIn);
		//TODO manick: test for Fluid
		fluidOut        = new FluidContainer("FluidOut", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		outputs.add(fluidOut);
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{
		if(0>volume){
			throw new Exception("Tank, type: " + type + ": Non physical value: Variable 'volume' must be bigger than zero!");
		}
		if(material==null){
			throw new Exception("Tank, type: " + type + ": empty Material!");
		}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		/* ************************************************************************/
		/*         Update inputs, direction of calculation:                       */
		/*         temperature [K]    : fluidIn --> fluid                         */
		/*         pressure    [Pa]   : fluidIn --> fluid                         */
		/*         flowRate:   [m^3/s]: fluid   <-- fluidOut                      */
		/* ************************************************************************/
		fluid.setTemperatureIn(fluidIn.getTemperature());
		if(pressureAmb.getValue() > 0){
			fluid.setPressure(pressureAmb.getValue());
		} else {
			fluid.setPressure(100000); //DIN 1343 normpressure = 1.01325 bar = 101325
		}
		fluid.setFlowRate(fluidOut.getFlowRate());
		
		/*
		 * if the given Pressure given from the pipe is bigger than ambientPressure
		 * --> calculate a FlowRate!
		 */
		/*
		if(fluidIn.getPressure() - fluid.getPressure() >= 0){
			fluid.setFlowRate(0.00014);
			fluidIn.setFlowRate(0.00014);
		}
		*/
		
		/* ************************************************************************/
		/*         Calculate and set fluid values:                                */
		/*         TemperatureIn, Pressure, FlowRate, ThermalResistance,          */
		/*         HeatSource, TemperatureExternal                                */
		/* ************************************************************************/
		//TODO manick: alphaFluid = heat transfer coefficient
		alphaFluid = 2100 * Math.sqrt(fluid.getFlowRate()/(2*Math.pow(volume, 1/3)))+580;
		
		/*
		if(temperatureAmb.getValue() != fluid.getTemperature().getValue()){
			alphaFluid =  580;// Q_dot / (A (T_1 - T_2)) //Water alpha = 2100*sqrt(velocity) + 580
		} else {
			alphaFluid = 1;
		}
		*/
		
		fluid.setThermalResistance(1/alphaFluid);
		fluid.setHeatSource(heatFlowIn.getValue() + heatExchangerIn.getValue());
		fluid.setTemperatureExternal(temperatureAmb.getValue());
		
		/* ************************************************************************/
		/*         Integration step:                                              */
		/* ************************************************************************/
		fluid.integrate(timestep);
		
		/* ************************************************************************/
		/*         Update outputs, direction of calculation:                      */
		/*         temperature [K]    : fluid   --> fluidOut                      */
		/*         pressure    [Pa]   : fluid   --> fluidOut                      */
		/*         flowRate:   [m^3/s]: fluidIn <-- fluid                         */
		/* ************************************************************************/
		fluidOut.setTemperature(fluid.getTemperature().getValue());
		//fluidOut.setTemperature(fluid.getTemperatureOut());
		fluidOut.setPressure(fluid.getPressure());
		fluidIn.setFlowRate(fluid.getFlowRate());
		
		System.out.println("tank: " + fluid.getPressure() + " " + fluid.getFlowRate() + " " + fluid.getTemperature().getValue());

		
		
		//TODO manick: calculate alphaFluid and ThermalResistance
			// alpha = Q_dot / (A * (T1 - T2))
			//alphaFluid = 2760;
			/*
			if(temperatureAmb.getValue()-fluid.getTemperature().getValue() != 0){
				//alphaFluid = 100 / (0.24 * (temperatureAmb.getValue() - fluid.getTemperature().getValue()));
				alphaFluid = 2760;
			}
			 */
			
			
		//TODO manick: calculate heat source
			//Q=alpha*A*deltaT*deltat=11340kWh 
			//Q=m*cp*deltaT
			/*
			if(length != 0.00){
				//if length/width/height are known: calculate the loss over the free surface and the 
				double openSurface = length * width;
				double wallSurface = openSurface + 2*(length*height) + 2*(width*height);
				//fluid.setHeatSource(-100);
			
			} else {
				//fluid.setHeatSource(-Math.pow(volume, 2/3)*(fluid.getTemperature().getValue()-temperatureAmb.getValue()));		
			}
			*/

		//set fluid values
		//fluid.setTemperatureIn(); //--> already done with FluidIn
		//fluid.setFlowRate();      //--> already done with FluidOut
		//System.out.println("tank fluidvalues: " + fluid.getPressure() + " " + 0.00 + " " + fluid.getFlowRate() + " " + fluidOut.getTemperature() + " " + 0.00 + " " + 0.00 + " " + 0.00 + " " + 0.00);	
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
	
	/*
	public ThermalArray getFluid(){
		return fluid;
	}
	*/
	/**
	 * get the type of the Fluid
	 * @return type of the fluid
	 */
	public String getFluidType(){
		return fluid.getMaterial().getType();
	}

	/*
	public void setFluid(ThermalArray fluid) {
		this.fluid = fluid;
	}
	*/
	/**
	 * set Fluid to type and initialize it
	 * @param type of the fluid
	 */
	public void setFluid(String type){
		this.fluid = new ThermalArray(type, volume, 1);
		
		System.out.println("tank.setFluid: " + type +" "+ volume + " " + temperatureAmb.getValue());
		
		//TODO manick: fluid has to be initialized, but when?
		if (temperatureAmb.getValue() > 0) {
			fluid.setInitialTemperature(temperatureAmb.getValue());
			fluid.getTemperature().setInitialCondition(temperatureAmb.getValue());
			System.out.println("tank.setFluid if statement " + fluid.getTemperature());
		} else {
			fluid.setInitialTemperature(293);
			fluid.getTemperature().setInitialCondition(293);
			System.out.println("tank.setFluid else statement");
		}
		
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(fluid.getTemperature());
	}
	
	public double getVolume(){
		return volume;
	}
	/*
	public void setMaterial(String material){
		this.material = material;
	}
	*/
	
	/*
	public String getMaterial(){
		return material;
	}
	*/
}
