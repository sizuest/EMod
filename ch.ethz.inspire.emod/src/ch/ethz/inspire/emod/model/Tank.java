package ch.ethz.inspire.emod.model;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.ethz.inspire.emod.model.thermal.ThermalArray;
import ch.ethz.inspire.emod.model.units.*;
import ch.ethz.inspire.emod.simulation.DynamicState;
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
 * -Pipe wall is rigid
 * 
 * Inputlist:
 *   1: FluidIn       : [-]    : Fluid Container with temperature, pressure, massflow, material
 *   2: PressureAmb   : [Pa]   : Ambient Pressure (assuming free surface of fluid in tank)
 *   3: TemperatureAmb: [K]    : Ambient temperature
 * Outputlist:
 *   1: FluidOut      : [-]    : Fluid Container with temperature, pressure, massflow, material
 *   
 * Config parameters:
 *   TankLength	    : [m]
 *   TankWidth      : [m] 
 *   TankHeight		: [m]
 *   ...?
 *   
 * 
 * @author manick
 *
 */
@XmlRootElement
public class Tank extends APhysicalComponent /*implements Floodable*/{

	@XmlElement
	protected String type;
	
	// Input parameters:
	//TODO manick: test for fluid
	private FluidContainer fluidIn;
	private double pressureAmb = 100000;
	private IOContainer temperatureAmb;
		
	// Output parameters:
	//TODO manick: test for fluid
	private FluidContainer fluidOut;
	
	// Parameters used by the model.
	//TODO manick: combine to Object?
	private String fluidType;
	private double volume;
	private ThermalArray fluid;
	private double lastpressure  = 0.00;
	private double lastmassflow  = 0.00;
	private double lasttemperature  = 293.00;
	double temperatureInit = 0;
	
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
	 * 
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
	public Tank(String type, double temperatureInit, String fluidType) {
		super();
		
		this.type = type;
		this.temperatureInit = temperatureInit;
		this.fluidType = fluidType;
		init();
	}
	
	/**
	 * Tank constructor
	 * @param type
	 * @param temperatureInit
	 * @param fluid
	 * @throws Exception
	 */
	public Tank(String type, double temperatureInit, String materialName, double volume, int numElements) {
		super();
		
		this.type = type;
		this.temperatureInit = temperatureInit;
		this.fluid = new ThermalArray(materialName, volume, numElements);
		init();
	}
	
	/**
	 * Called from constructor or after unmarshaller.
	 * @throws Exception 
	 */
	private void init()
	{
		/* Define Input parameters */
		inputs         = new ArrayList<IOContainer>();
		temperatureAmb = new IOContainer("TemperatureAmb", Unit.KELVIN, temperatureInit, ContainerType.THERMAL);
		inputs.add(temperatureAmb);
		
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
			e.printStackTrace();
			System.exit(-1);
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

		/* Thermal Array */
		fluid = new ThermalArray(fluidType, volume, 10);
		fluid.getTemperature().setInitialCondition(temperatureInit);
		
		//TODO manick: test for Fluid
		fluidIn        = new FluidContainer("FluidIn", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		inputs.add(fluidIn);
		//TODO manick: test for Fluid
		fluidOut        = new FluidContainer("FluidOut", Unit.NONE, ContainerType.FLUIDDYNAMIC);
		outputs.add(fluidOut);
		
		/* State */
		dynamicStates = new ArrayList<DynamicState>();
		dynamicStates.add(fluid.getTemperature());
	}
	
	/**
	 * Validate the model parameters.
	 * 
	 * @throws Exception
	 */
    private void checkConfigParams() throws Exception
	{		
		if(0>volume){
			throw new Exception("Tank, type:" +type+ 
					": Non physical value: Variable 'volume' must be bigger than zero!");
		}
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.model.APhysicalComponent#update()
	 */
	@Override
	public void update() {
		// Update inputs
		//direction of calculation
		//temperature [K]    : fluidIn --> fluidOut
		//pressure    [Pa]   : fluidIn --> fluidOut
		//flowRate:   [m^3/s]: fluidIn <-- fluidOut
		fluid.setTemperatureIn(fluidIn.getTemperature());
		fluid.setPressure(fluidIn.getPressure());
		//fluid.setPressure(pressureAmb);
		fluid.setFlowRate(fluidOut.getFlowRate());
		
		//TODO manick: calculate heat source!!
		fluid.setHeatSource(-Math.pow(volume, 2/3)*(fluid.getTemperature().getValue()-temperatureAmb.getValue()));
		fluid.setTemperatureExternal(temperatureAmb.getValue());
		
		// Integration step
		fluid.integrate(timestep);
		
		// Update outputs
		//direction of calculation
		//temperature [K]    : fluidIn --> fluidOut
		//pressure    [Pa]   : fluidIn --> fluidOut
		//flowRate:   [m^3/s]: fluidIn <-- fluidOut
		fluidOut.setTemperature(fluid.getTemperature().getValue());
		fluidOut.setPressure(fluid.getPressure());
		fluidIn.setFlowRate(fluid.getFlowRate());
		
		System.out.println("tank fluidvalues: " + lastpressure + " " + 0.00 + " " + lastmassflow + " " + lasttemperature + " " + 0.00 + " " + 0.00 + " " + 0.00 + " " + 0.00);	
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
	
	public ThermalArray getFluid(){
		return fluid;
	}

	public void setFluid(ThermalArray fluid) {
		this.fluid = fluid;
		
	}
}
