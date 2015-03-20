package ch.ethz.inspire.emod.utils;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.Material;
import ch.ethz.inspire.emod.model.Tank;


public class FluidConnection extends IOConnection {
	
	protected Material material;
	protected String stringSource = "null";
	protected String stringTarget = "null";
	
	//TODO manick: does this even work??
	/**
	 * create FluidConnection with two components, source has to have output "FluidOut", target has to have input "FluidIn"
	 * @param source
	 * @param target
	 * @throws Exception
	 */
	public FluidConnection(APhysicalComponent source, APhysicalComponent target) throws Exception{
		// connect if source has "FluidOut" and target has "FluidIn" and both Material are same!
		super(source.getOutput("FluidOut"), target.getInput("FluidIn"));

		
		//System.out.println("FluidConnection constructor: " + source.getClass().toString() + " " + target.getClass().toString());
		
		/*
		if(target instanceof ch.ethz.inspire.emod.utils.Floodable){
			System.out.println("target is Floodable ***");
			((Floodable) target).setFluid("GlycolWater");;
		}
		*/
		
		if(source instanceof ch.ethz.inspire.emod.model.Tank){
			//String type = ((Tank)source).getFluid().getMaterial().getType();
			String type = ((Tank)source).getFluidType();
			if(target instanceof Floodable){
				((Floodable)target).setFluid(type);
				//System.out.println("flooding component after tank with " + type);
			}
		} else if (source instanceof Floodable){
			String type = ((Floodable)source).getFluidType();
			if(target instanceof Floodable && !(target instanceof ch.ethz.inspire.emod.model.Tank)){
				((Floodable)target).setFluid(type);
				//System.out.println("flooding component after other comp with " + type);
			}
		}
		
		stringSource = source.getModelType();
		stringTarget = target.getModelType();
		
		//get material from tank (respectively source), then add to target
		
		/*
		if(	source.getOutput("FluidOut") != null &&
			target.getInput("FluidIn") != null &&
			((FluidContainer) source.getOutput("FluidOut")).getFluid().getMaterial() == ((FluidContainer) target.getInput("FluidIn")).getFluid().getMaterial()){
				new FluidConnection(source.getOutput("FluidOut"), target.getInput("FluidIn"), ((FluidContainer)source.getOutput("FluidOut")).getFluid().getMaterial());		
				System.out.println("FluidConnection: created!");
		}
		// if Material are not same: set FluidIn to same Material as FluidOut
		else if(source.getOutput("FluidOut") != null && target.getInput("FluidIn") != null ){
			//TODO manick: darf das das selbe Objekt des Typs Material sein? oder brauchts zwei Objekte davon?
			//d.h. entweder setMaterial(getMaterial()) oder getMaterial.setType(getMaterial().getType())
			((FluidContainer)target.getInput("FluidIn")).getFluid().setMaterial(((FluidContainer)source.getOutput("FluidOut")).getFluid().getMaterial());
			new FluidConnection(source.getOutput("FluidOut"), target.getInput("FluidIn"), ((FluidContainer)source.getOutput("FluidOut")).getFluid().getMaterial());			
			System.out.println("FluidConnection: set MaterialIn to same as MaterialOut, created!");
		}
		else{
			System.out.println("FluidConnection: can't create connection.");
		}
		*/
	}
	
	//TODO manick: check

	/**
	 * 
	 * @param source
	 * @param target
	 * @throws Exception
	 */
	public FluidConnection(FluidContainer source, FluidContainer target) throws Exception{
		super((IOContainer)source, (IOContainer)target);
		
		ArrayList<MachineComponent> components = Machine.getInstance().getMachineComponentList();
		for(MachineComponent mc : components){
			List<IOContainer> inputs = mc.getComponent().getInputs();
			for(IOContainer io : inputs){
				if(io.equals(source)){
					System.out.println(io.getClass().toString());
				}
			}
		}
	}
	
	
	
	/*
	public FluidConnection(IOContainer source, IOContainer target, Material material) throws Exception{
	//public FluidConnection(IOContainer<T> source, IOContainer<T> target, Material material) throws Exception{
		super(source, target);
		//this.gain = 1; //wird bereits im super() Konstruktor =1 gesetzt!

		if(source.getType().isFluidConnection() & target.getType().isFluidConnection()){
			System.out.println("before*** In and Output are of type FLUID");
		} else {
			System.out.println("before*** In or Output is not of type FLUID");
		}
		
		// try to add a Fluid to the source and target. Only possible if they are of Type FLUIDDYNAMIC or THERMAL
		source.getType().addFluid();
		target.getType().addFluid();
		
		// the material of the FluidConnection is set 
		this.material = material;
		if(source.getType().isFluidConnection() & target.getType().isFluidConnection()){
			System.out.println("after *** In and Output are of type FLUID");
		} else {
			System.out.println("after *** In or Output is not of type FLUID");
		}		
		
		// TODO manick: Errorhandling if Fluid couldn't be added to connection?
		
		System.out.println("FluidConnection Konstruktor MIT Material:" + material.getClass().toString());
		
		if(source.getUnit()!=target.getUnit()) {
			unitConversion();
		}
	}
	*/
	
	/**
	 * @param source
	 * @param target
	 * @return
	 */
	public boolean checkConnection(IOConnection io){
	//public boolean checkConnection(IOConnection<T> io){

		boolean equalType = false;
		//ContainerType type = ContainerType.FLUID;
		if (io.getSource().getType().isFluidConnection()){
			equalType = (io.getSource().getType().isFluidConnection() == io.getTarget().getType().isFluidConnection() ? true : false);
		}
		return equalType;
	}
	
	/**
	 * @param material to set
	 * @throws Exception
	 */
	public void setMaterial(Material material) throws Exception{
		this.material = material;
	}
	
	/**
	 * @return get material
	 */
	public Material getMaterial(){
		return material;
	}

	/**
	 * init a fluidconnection with values for temperature/pressure/flowRate
	 * @param temperature
	 * @param pressure
	 * @param flowRate
	 */
	public void init(double temperature, double pressure, double flowRate){
		((FluidContainer)source).setValues(temperature, pressure, flowRate);
		((FluidContainer)target).setValues(temperature, pressure, flowRate);
	}
	
	/**
	 * update from source to target or vice versa according to the direction of calculation
	 */
	public void update(){
		/* direction of calculation
		 * temperature [K]    : source --> target
		 * pressure    [Pa]   : source --> target
		 * flowRate:   [m^3/s]: source <-- target
		 */

		/* exception 1:
		 * pump as source --> the flowrate is created by the pump
		 */
		/*
		if(stringSource.equals("Pump")){
			((FluidContainer)target).setFlowRate   (((FluidContainer)source).getFlowRate());
		} else {
			((FluidContainer)source).setFlowRate   (((FluidContainer)target).getFlowRate());
		}
		*/
		
		/* exception 2:
		 * tank as target --> 
		 */
		/*
		if(stringSource.equals("Pipe") && stringTarget.equals("Tank")){

		}
		*/
		
		((FluidContainer)target).setTemperature(((FluidContainer)source).getTemperature());
		((FluidContainer)target).setPressure   (((FluidContainer)source).getPressure());
		((FluidContainer)source).setFlowRate   (((FluidContainer)target).getFlowRate());
		
		//TODO manick: some special cases: pump defines pressure etc.
		//maybe there should be a case switch?
		//pipe --> pump: pressure: source <-- target and control <-- target
		//pump --> pipe: massflow: source --> target and source  <-- control
	}
}

/**
 * @param io IOConnection without Fluid
 * @param material to set
 * @return fio FluidConnection with added Fluid
 * @throws Exception
 */
/*
public FluidConnection addFluid(IOConnection io, Material material) throws Exception{
	//io = (FluidConnection)io.setFluid(material);
	//io.setFluid(material);
	//return io;
	FluidConnection fio = new FluidConnection(io.getSource(), io.getTarget(), material);
	return fio;
}
*/

/**
 * @param fio FluidConnection to remove Fluid from
 * @return io IOConnection without Fluid
 * @throws Exception
 */
//TODO manick: should not be allowed! 
/*
public IOConnection removeFluid() throws Exception{
//public IOConnection<T> removeFluid() throws Exception{

	// create new Connection without Fluid
	IOConnection io = new IOConnection(this.getSource(), this.getTarget());
	//IOConnection<T> io = new IOConnection<T>(this.getSource(), this.getTarget());
	
	// check if current connection is present in Machine IOLinkList, if yes, replace
	List<IOConnection> listIO = Machine.getInstance().getIOLinkList();
	if(listIO.contains(this)){
		listIO.remove(this);
		listIO.add(io);
	}
	
	// return new FluidConnection
	return io;
}
*/