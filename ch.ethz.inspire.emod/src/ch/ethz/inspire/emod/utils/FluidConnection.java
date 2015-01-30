package ch.ethz.inspire.emod.utils;

import java.util.List;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.APhysicalComponent;
import ch.ethz.inspire.emod.model.Material;
import ch.ethz.inspire.emod.model.Tank;

public class FluidConnection extends IOConnection {
//public interface FluidConnection{
	///*public abstract*/ void init();
	///*public abstract*/ void update();
//public class FluidConnection<T> extends IOConnection<T> {
	
	
	
	
	protected Material material;
	
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
	
	public FluidConnection(FluidContainer source, FluidContainer target) throws Exception{
		super((IOContainer)source, (IOContainer)target);		
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
	 * @param io IOConnection without Fluid
	 * @param material to set
	 * @return fio FluidConnection with added Fluid
	 * @throws Exception
	 */
	/*/
	public FluidConnection addFluid(IOConnection io, Material material) throws Exception{
		//io = (FluidConnection)io.setFluid(material);
		//io.setFluid(material);
		//return io;
		FluidConnection fio = new FluidConnection(io.getSource(), io.getTarget(), material);
		return fio;
	}
	//*/
	
	/**
	 * @param material to set
	 * @throws Exception
	 */
	public void setFluid(Material material) throws Exception{
		this.material = material;
	}
	
	/**
	 * @return get material
	 */
	public Material getMaterial(){
		return material;
	}
	
	/**
	 * @param fio FluidConnection to remove Fluid from
	 * @return io IOConnection without Fluid
	 * @throws Exception
	 */
	//TODO manick: test
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

	public void init(double temperature, double pressure, double flowRate){
		((FluidContainer)source).setValues(temperature, pressure, flowRate);
		((FluidContainer)target).setValues(temperature, pressure, flowRate);
	}
		
	public void update(){
		//direction of calculation
		//temperature [K]    : source --> target
		//pressure    [Pa]   : source --> target
		//flowRate:   [m^3/s]: source <-- target
		((FluidContainer)target).setTemperature(((FluidContainer)source).getTemperature());
		//TODO manick: just do this, if source is not a pump!!!
		//if(!source.getClass().equals(new Pump())){
			((FluidContainer)target).setPressure   (((FluidContainer)source).getPressure());
		//}
		((FluidContainer)source).setFlowRate   (((FluidContainer)target).getFlowRate());
		
		//maybe there should be a case switch?
		//pipe --> pump: pressure: source <-- target and control <-- target
		//pump --> pipe: massflow: source --> target and source  <-- control
		//or similar
	}
}