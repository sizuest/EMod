/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2015 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.utils;

import ch.ethz.inspire.emod.model.Material;

/**
 * General implementation of the properties of a component within a fluid circuit.
 * The FCP are implemented as a binary three, where as the leaves of a object
 * indicate the up and downstream elements.
 * @author sizuest
 *
 */
public class FluidCircuitProperties {
	/* Flow rate to the component in [m³/s] */
	private double flowRateIn;
	/* Flow rate out of the component in [m³/s] */
	private double flowRateOut;
	/* Pressure drop over the component in [Pa] */
	private double pressureDrop;
	/* Material of the fluid flowing through the component {@link Material} */ 
	private Material material;
	/* Up- and downstream elemente */
	private FluidCircuitProperties post, pre;
	/* Cupple in- and out left [default on] */
	private boolean coupledInAndOut = true;
	/* Pressure reference */
	private FluidContainer pressureReference = null;
	
	/**
	 * Public constructor
	 */
	public FluidCircuitProperties(){
		coupledInAndOut = true;
		init();
	}
	
	private void init(){
		flowRateIn = 0;
		pressureDrop = 0;
		material = new Material("Example");
		post = null;
		pre  = null;
	}
	
	/**
	 * Checks if the objects is part of a closed fluid circle
	 * @return true/false
	 */
	public boolean isCircuit(){
		return isCircuit(this);
	}
	
	private boolean isCircuit(FluidCircuitProperties caller){
		if(this.post == caller)
			return true;
		else if(this.post == null)
			return false;
		else
			return this.post.isCircuit(caller);
					
	}
	
	private boolean isDirectCircuit(){
		return isDirectCircuit(this);
	}
	private boolean isDirectCircuit(FluidCircuitProperties caller){
		if(this.post == caller)
			return true;
		else if(this.post == null)
			return false;
		else if(this.coupledInAndOut)
			return false;
		else
			return this.post.isCircuit(caller);
	}
	
	
	/**
	 * Set the down-stream element
	 * This method will automatically set the the current element as
	 * the up-stream element of the indicated down-stream element.
	 * @param post {@link FluidCircuitProperties} Down-stream element
	 */
	public void setPost(FluidCircuitProperties post){
		this.post = post;
		this.post.setPre(this);
	}
	
	/**
	 * Calculates the pressure drops of all down-stream elements, until
	 * - A pressure reference is reached
	 * - The caller is reached again (circle), or
	 * - A pressure drop of NaN occurs,  or
	 * - Null occurs as leave in the tree (end of line)
	 * @return Down-stream pressure drop
	 */
	public Double getPressureFront(){
		return getPressureFront(this);
	}
	
	private Double getPressureFront(FluidCircuitProperties caller){
		if(null!=this.pressureReference)
			return this.pressureReference.getPressure();
		if(this.post == caller)
			return 0.0;
		else if(null==this.post)
			return 0.0;
		else if(Double.isNaN(this.post.getPressureDrop()))
			return 0.0;
		else if(coupledInAndOut)
			return this.post.getPressureDrop()+this.post.getPressureFront(caller);
		else
			return 0.0;
	}
	
	/**
	 * Calculates the pressure resulting from all up-stream elements, until
	 * - A pressure reference is reached
	 * - The caller is reached again (circle), or
	 * - A pressure drop of NaN occurs,  or
	 * - Null occurs as leave in the tree (end of line)
	 * @return Down-stream pressure drop
	 */
	public Double getPressureBack(){
		return getPressureBack(this);
	}
	
	private Double getPressureBack(FluidCircuitProperties caller){
		
		if(this.pre == caller)
			return 0.0;
		else if(null==this.pre)
			return 0.0;
		else if(null!=this.pre.pressureReference)
			return this.pre.pressureReference.getPressure();
		else if(Double.isNaN(pressureDrop))
			return 0.0;
		else 
			return this.pre.getPressureDrop()+this.pre.getPressureBack(caller);
	}
	
	private void setPre(FluidCircuitProperties pre){
		this.pre = pre;
	}
	
	/**
	 * Set the pressure drop according to the input value [Pa]
	 * @param value
	 */
	public void setPressureDrop(double value){
		this.pressureDrop = value;
	}
	
	/**
	 * Set the inlet flow rate according to the value [m³/s]
	 * @param value
	 */
	public void setFlowRateIn(double value){	
		setFlowRateIn(value, this, false );
		
		if(!isDirectCircuit() & coupledInAndOut)
			setFlowRateOut(value, this, true);
	}
	
	/**
	 * Set the outlet flow rate according to the value [m³/s]
	 * @param value
	 */
	public void setFlowRateOut(double value){
		setFlowRateOut(value, this, true);
		
		if(!isDirectCircuit() & coupledInAndOut)
			setFlowRateIn(value, this, false);
	}
	
	/**
	 * Set the material according to the value {@link Material}
	 * @param value
	 */
	public void setMaterial(Material value){
		setMaterial(value, this);
	}
	
	private void setFlowRateIn(double flowRate, FluidCircuitProperties caller, boolean moveDownStream) {
		flowRateIn = flowRate;
		
		/* Check if a a pre-element exists. If so, and the
		 * Procedure is set to upstream, move up!
		 */
		if(pre!=null & !moveDownStream)
			pre.setFlowRateOut(flowRate, caller, moveDownStream);
		/* Check if in- and outut are coupled, and if we
		 * are moving downstream, move down!
		 */
		else if(coupledInAndOut & moveDownStream & this!=caller)
			setFlowRateOut(flowRate, caller, moveDownStream);
		else if(coupledInAndOut & moveDownStream & this==caller)
			flowRateOut = flowRate;
	}
	
	private void setFlowRateOut(double flowRate, FluidCircuitProperties caller, boolean moveDownStream) {	
		flowRateOut = flowRate;
		
		/* If there is a post element which and if we 
		 * are moving downstream, change it's input 
		 * flow rate!
		 */
		if(post!=null & moveDownStream)
			post.setFlowRateIn(flowRate, caller, moveDownStream);
		/* If there is a pre element which is not the caller,
		 * and if we are moving upstream, change it's input 
		 * flow rate!
		 */
		else if(coupledInAndOut & !moveDownStream & this!=caller)
			setFlowRateIn(flowRate, caller, moveDownStream);
		else if(coupledInAndOut & !moveDownStream & this==caller)
			flowRateIn = flowRate;

	}
	
	private void setMaterial(Material material, FluidCircuitProperties caller) {
		this.material = material;
		if(this.post!=caller & post!=null){
			this.post.setMaterial(material, caller);
		}
	}
	
	/**
	 * Set the coupplet in- and output label (true/false)
	 * @param c
	 */
	public void setCuppledInAndOut(boolean c){
		coupledInAndOut = c;
	}
	
	/**
	 * Set the pressure reference
	 * @param p
	 */
	public void setPressureReference(FluidContainer p){
		pressureReference = p;
	}
	
	/**
	 * Returns the pressure reference in Pa
	 * @return [Pa]
	 */
	public double getPressureReference(){
		if(null==pressureReference)
			return Double.NaN;
		else
			return pressureReference.getPressure();
	}
	
	
	
	/**
	 * getFlowRate()
	 * @return current flow rate [m³/s]
	 * @deprecated
	 */
	public double getFlowRate(){
		return getFlowRateIn();
	}
	
	/**
	 * getFlowRateIn()
	 * @return current inlet flow rate [m³/s]
	 */
	public double getFlowRateIn(){
		return this.flowRateIn;
	}
	
	/**
	 * getFlowRate()
	 * @return current outlet flow rate [m³/s]
	 */
	public double getFlowRateOut(){
		if(coupledInAndOut)
			return this.flowRateIn;
		else
			return this.flowRateOut;
	}
	
	/**
	 * getMaterial()
	 * @return current material {@link Material}
	 */
	public Material getMaterial(){
		return this.material;
	}
	
	/**
	 * getPressureDrop
	 * @return Current pressure drop [Pa]
	 */
	public double getPressureDrop(){
		return this.pressureDrop;
	}
}
