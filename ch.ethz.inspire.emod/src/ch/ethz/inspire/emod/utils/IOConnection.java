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

package ch.ethz.inspire.emod.utils;

import java.util.logging.Logger;

import ch.ethz.inspire.emod.EModMain;
import ch.ethz.inspire.emod.utils.IOContainer;
//import ch.ethz.inspire.emod.model.units.ContainerType;

/**
 * contains information on simulation input sources and targets
 * through references to IOContainers of MachineComponents and
 * SimulationControls.
 * 
 * @author dhampl
 * @param <T>
 *
 */
public class IOConnection {
//public class IOConnection<T> {
	//TODO manick: FluidConnection extends IOConnection
	
	protected IOContainer source;
	//protected IOContainer<T> source;
	protected IOContainer target;
	//protected IOContainer<T> target;
	protected double gain;
	
	//TODO manick: create IOConnection and IOContainer with fluid!
	//private Material material;
	
	private static Logger logger = Logger.getLogger(EModMain.class.getName());
	
	public IOConnection(){
		
	}
	
	
	/**
	 * 
	 * @param <T>
	 * @param source
	 * @param target 
	 * @throws Exception thrown if units don't match
	 */
	public IOConnection(IOContainer source, IOContainer target) throws Exception {
	//public IOConnection(IOContainer<T> source, IOContainer<T> target) throws Exception {
		this.source = source;
		this.target = target;
		this.gain   = 1;
		
		if(source.getUnit()!=target.getUnit()) {
			unitConversion();
		}
	}
	
	
	/**
	 * @param material to set
	 * @return fio FluidConnection with added Fluid
	 * @throws Exception
	 */
	/*
	public FluidConnection addFluid(Material material) throws Exception{
	//public FluidConnection<T> addFluid(Material material) throws Exception{
		// create new Connection with Fluid
		FluidConnection fio = new FluidConnection(this.getSource(), this.getTarget(), material);
		//FluidConnection<T> fio = new FluidConnection<T>(this.getSource(), this.getTarget(), material);
		
		// check if current connection is present in Machine IOLinkList, if yes, replace
		List<IOConnection> listIO = Machine.getInstance().getIOLinkList();
		if(listIO.contains(this)){
			listIO.remove(this);
			listIO.add(fio);
		}
		
		// return new FluidConnection
		return fio;
	}
	*/
	
	/**
	 * 
	 * @param <T>
	 * @param source
	 * @param target 
	 * @throws Exception thrown if units don't match
	 */
	/*/
	public IOConnection(T t, IOContainer source, IOContainer target) throws Exception {
		this.source = source;
		this.target = target;
		this.gain   = 1;
		//manick new
		
		//System.out.println("***** typ von connection " + t.getType());
		
		//t.hello();
		//System.out.println("**** eine Verbindung vom Typ " + t.getClass().toString() + " wurde erstellt");
		
		if(source.getUnit()!=target.getUnit()) {
			unitConversion();
		}
	}
	//*/
	
	//TODO manick: new Connection LOGIC
	/*/
	public static IOConnection<LogicConnection> newLogicConnection(IOContainer source, IOContainer target) throws Exception{
		System.out.println("**** endlich eine LogicConnection");
		return new IOConnection<LogicConnection>(source, target);
	}
	//*/
	
	//TODO manick: new Connection mit FLUID
	/*/
	public static IOConnection<FluidConnection> newFluidConnection(IOContainer source, IOContainer target) throws Exception{
		System.out.println("%%%%%% endlich eine FluidConnection?");
		return new IOConnection<FluidConnection>(source, target);
		//return new IOConnection<FluidConnection>();
	}
	//*/
	
	/*/
	public IOConnection(IOContainer source, IOContainer target, Material material) throws Exception{
		this.source = source;
		this.target = target;
		this.gain = 1;
		this.material = material;
		if(source.getUnit()!=target.getUnit()) {
			unitConversion();
		}
	}
	//*/
	
	public IOContainer getSource() {
	//public IOContainer<T> getSource() {
		return source;
	}
	
	public IOContainer getTarget() {
	//public IOContainer<T> getTarget() {
		return target;
	}
	
	/**
	 * @return gain
	 */
	public double getGain() {
		return gain;
	}

	public void unitConversion() throws Exception{
		/*
		 * We have a unit missmatch: lets check if it can be solved by 
		 * a unit conversion. If not: drop an exception.
		 */
		
		switch (source.getUnit()) {
			// Meter
			case M:
				switch (target.getUnit()) {
					// Milimeter
					case MM:
						this.gain = 1000;
						break;
					default:
						this.gain = -1;						
				}
				break;
			// Milimeter
			case MM:
				switch (target.getUnit()) {
					// Meter
					case M:
						this.gain = 0.001;
						break;
					default:
						this.gain = -1;						
				}
				break;
			case L_S:
				switch (target.getUnit()) {
					case METERCUBIC_S:
						this.gain = 0.001;
						break;
					case L_MIN:
						this.gain = 60;
						break;
					default:
						this.gain=-1;
				}
				break;
			case METERCUBIC_S:
				switch (target.getUnit()) {
					case L_S:
						this.gain = 1000;
						break;
					case L_MIN:
						this.gain = 60000;
						break;
					default:
						this.gain=-1;
				}
				break;
			case L_MIN:
				switch (target.getUnit()) {
					case L_S:
						this.gain = 1/60;
						break;
					case METERCUBIC_S:
						this.gain = 1/60000;
						break;
					default:
						this.gain=-1;
				}
				break;				
			default:
				this.gain = -1;
		}

		//TODO manick: don't abort if there is a mismatch but inform the user!
		if (this.gain<=0)
			throw new Exception("units do not match "+source.getName()+
					": "+source.getUnit()+" <-> "+target.getName()+": "+
					target.getUnit());
		else if (this.gain!=1)
			logger.info("explicit unit conversion " +source.getName()+
					": "+source.getUnit()+" > * " +this.gain+ " > "+target.getName()+": "+
					target.getUnit());
	}
}
